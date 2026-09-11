package com.rixy.bot.agent

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.telephony.SmsManager
import com.rixy.bot.service.NotificationStore
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

private const val NEW_TASK = Intent.FLAG_ACTIVITY_NEW_TASK

/** Current time and timezone. */
class GetTimeTool : AgentTool {
    override val name = "get_time"
    override val description = "Get the device's current date, time, and timezone."
    override val parametersSchema = ToolJson.schema()
    override fun execute(context: Context, args: JSONObject): ToolResult {
        val now = Date()
        val fmt = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' HH:mm", Locale.getDefault())
        return ToolJson.ok(
            "iso_time" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(now),
            "friendly" to fmt.format(now),
            "timezone" to TimeZone.getDefault().id,
            "epoch_ms" to now.time,
        )
    }
}

/** Battery percentage and charging state. */
class BatteryTool : AgentTool {
    override val name = "get_battery"
    override val description = "Get battery level percentage and whether the device is charging."
    override val parametersSchema = ToolJson.schema()
    override fun execute(context: Context, args: JSONObject): ToolResult {
        val bm = context.getSystemService(BatteryManager::class.java)
        val percent = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val sticky = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = sticky?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        return ToolJson.ok(
            "percent" to percent,
            "charging" to charging,
            "plugged" to ((sticky?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0) != 0),
        )
    }
}

/** Last known coarse location. */
class LocationTool : AgentTool {
    override val name = "get_location"
    override val description =
        "Get the device's last known location (approximate, coarse). Returns coordinates and " +
            "reverse-geocoded place name when available."
    override val requiredPermission = Manifest.permission.ACCESS_COARSE_LOCATION
    override val parametersSchema = ToolJson.schema()
    override fun execute(context: Context, args: JSONObject): ToolResult {
        val lm = context.getSystemService(LocationManager::class.java)
        val last = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        ).asSequence()
            .mapNotNull { provider ->
                runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
            }
            .filter { it.time > System.currentTimeMillis() - 30 * 60 * 1000 }
            .maxByOrNull { it.time }
            ?: return ToolJson.error("No recent location fix available. Ask the user to open Maps once.")
        val place = runCatching {
            android.location.Geocoder(context, Locale.getDefault())
                .getFromLocation(last.latitude, last.longitude, 1)
                ?.firstOrNull()?.let { addr ->
                    listOfNotNull(addr.locality, addr.adminArea, addr.countryName).joinToString(", ")
                }
        }.getOrNull().orEmpty()
        return ToolJson.ok(
            "latitude" to (last.latitude * 10000).roundToInt() / 10000.0,
            "longitude" to (last.longitude * 10000).roundToInt() / 10000.0,
            "place" to place,
            "age_minutes" to ((System.currentTimeMillis() - last.time) / 60000),
        )
    }
}

/** Recent notifications (requires notification-listener access enabled by the user). */
class ListNotificationsTool(private val store: NotificationStore) : AgentTool {
    override val name = "list_notifications"
    override val description =
        "List the most recent notifications on the device (app, title, text). " +
            "Requires notification access, which the user may need to enable in system settings."
    override val parametersSchema = ToolJson.schema()
    override fun execute(context: Context, args: JSONObject): ToolResult {
        if (!store.listenerConnected) {
            return ToolJson.error("Notification access is not enabled. Ask the user to enable it: Settings > Notification access > Rixy.")
        }
        val items = store.list().take(20)
        if (items.isEmpty()) return ToolJson.error("No recent notifications.")
        val arr = JSONArray().apply {
            items.forEach {
                put(
                    JSONObject()
                        .put("app", it.appLabel)
                        .put("package", it.packageName)
                        .put("title", it.title)
                        .put("text", it.text)
                        .put("time", SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it.postTime)))
                        .put("can_reply", it.replyable)
                        .put("key", it.key)
                )
            }
        }
        return ToolResult.Ok(JSONObject().put("notifications", arr))
    }
}

/** Reply to a message notification (WhatsApp, SMS app, etc.) via RemoteInput. */
class ReplyNotificationTool(private val store: NotificationStore) : AgentTool {
    override val name = "reply_notification"
    override val description =
        "Reply to a recent message notification (e.g. WhatsApp/SMS/Telegram) by sending a text " +
            "reply through that notification. Use list_notifications first to find the exact app/title."
    override val parametersSchema = ToolJson.schema(
        "app" to ToolJson.stringProp("App name or package substring, e.g. 'WhatsApp'"),
        "reply" to ToolJson.stringProp("The message text to send as the reply"),
        "title_contains" to ToolJson.stringProp("Optional: match the notification title to pick the right conversation"),
        required = listOf("app", "reply"),
    )
    override val requiredPermission = null

    override fun needsConfirmation(args: JSONObject) = true
    override fun summarize(args: JSONObject): String =
        "Reply to ${args.optString("app", "?")}: \"${args.optString("reply", "").take(80)}\""

    override fun execute(context: Context, args: JSONObject): ToolResult {
        val app = args.optString("app").lowercase()
        val title = args.optString("title_contains").lowercase()
        val reply = args.optString("reply")
        if (reply.isBlank()) return ToolJson.error("Empty reply text.")
        val entry = store.list()
            .filter { it.replyable }
            .filter {
                it.appLabel.lowercase().contains(app) || it.packageName.lowercase().contains(app)
            }
            .filter { title.isEmpty() || it.title.lowercase().contains(title) }
            .maxByOrNull { it.postTime }
            ?: return ToolJson.error(
                "No replyable notification matched '$app'" +
                    if (title.isEmpty()) "" else " with title containing '$title'"
            )
        val action = entry.actions.firstOrNull { it.remoteInputs?.isNotEmpty() == true }
            ?: return ToolJson.error("Matched notification has no reply action.")
        return try {
            val remoteInputs = action.remoteInputs
            val bundle = Bundle()
            remoteInputs.firstOrNull()?.let { bundle.putCharSequence(it.resultKey, reply) }
            val intent = Intent()
            android.app.RemoteInput.addResultsToIntent(remoteInputs, intent, bundle)
            action.actionIntent.send(context, 0, intent)
            ToolJson.ok("sent" to true, "app" to entry.appLabel, "conversation" to entry.title)
        } catch (e: Exception) {
            ToolJson.error("Reply failed: ${e.message ?: "unknown error"}")
        }
    }
}

/** Search contacts by name. */
class SearchContactsTool : AgentTool {
    override val name = "search_contacts"
    override val description = "Search the device contacts by (partial) name. Returns names and phone numbers."
    override val requiredPermission = Manifest.permission.READ_CONTACTS
    override val parametersSchema = ToolJson.schema(
        "query" to ToolJson.stringProp("Part of the contact's name to search for"),
        required = listOf("query"),
    )

    override fun execute(context: Context, args: JSONObject): ToolResult {
        val query = args.optString("query").trim()
        if (query.isEmpty()) return ToolJson.error("Empty search query.")
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$query%"),
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ) ?: return ToolJson.error("Contacts unavailable.")
        val results = JSONArray()
        val seen = mutableSetOf<String>()
        cursor.use { c ->
            while (c.moveToNext() && results.length() < 8) {
                val name = c.getString(0) ?: continue
                val number = c.getString(1) ?: continue
                if (seen.add("$name/$number")) {
                    results.put(JSONObject().put("name", name).put("number", number))
                }
            }
        }
        return if (results.length() == 0) ToolJson.error("No contacts matched '$query'.")
        else ToolResult.Ok(JSONObject().put("contacts", results))
    }
}

/** Send an SMS. */
class SendSmsTool : AgentTool {
    override val name = "send_sms"
    override val description = "Send an SMS text message to a phone number."
    override val requiredPermission = Manifest.permission.SEND_SMS
    override val parametersSchema = ToolJson.schema(
        "number" to ToolJson.stringProp("Destination phone number, digits with country code preferred"),
        "message" to ToolJson.stringProp("Message text to send"),
        required = listOf("number", "message"),
    )

    override fun needsConfirmation(args: JSONObject) = true
    override fun summarize(args: JSONObject): String =
        "Send SMS to ${args.optString("number", "?")}: \"${args.optString("message", "").take(80)}\""

    override fun execute(context: Context, args: JSONObject): ToolResult {
        val number = args.optString("number").trim()
        val message = args.optString("message").trim()
        if (number.isEmpty() || message.isEmpty()) return ToolJson.error("number and message are required.")
        return try {
            val sms = context.getSystemService(SmsManager::class.java)
            val parts = sms.divideMessage(message)
            if (parts.size == 1) sms.sendTextMessage(number, null, message, null, null)
            else sms.sendMultipartTextMessage(number, null, parts, null, null)
            ToolJson.ok("sent" to true, "to" to number)
        } catch (e: Exception) {
            ToolJson.error("SMS failed: ${e.message ?: "unknown error"}")
        }
    }
}

/** Place a phone call. */
class DialTool : AgentTool {
    override val name = "dial_number"
    override val description = "Place a phone call to a number (the call starts immediately)."
    override val requiredPermission = Manifest.permission.CALL_PHONE
    override val parametersSchema = ToolJson.schema(
        "number" to ToolJson.stringProp("Phone number to call"),
        required = listOf("number"),
    )

    override fun needsConfirmation(args: JSONObject) = true
    override fun summarize(args: JSONObject): String = "Call ${args.optString("number", "?")}"

    override fun execute(context: Context, args: JSONObject): ToolResult {
        val number = args.optString("number").trim()
        if (number.isEmpty()) return ToolJson.error("number is required.")
        return try {
            context.startActivity(
                Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).addFlags(NEW_TASK)
            )
            ToolJson.ok("calling" to number)
        } catch (e: Exception) {
            ToolJson.error("Call failed: ${e.message ?: "no dialer"}")
        }
    }
}

/** Launch an app by name. */
class OpenAppTool : AgentTool {
    override val name = "open_app"
    override val description = "Launch an installed app by its display name (e.g. 'WhatsApp') or package substring."
    override val parametersSchema = ToolJson.schema(
        "app" to ToolJson.stringProp("App name or package substring"),
        required = listOf("app"),
    )

    override fun execute(context: Context, args: JSONObject): ToolResult {
        val query = args.optString("app").trim().lowercase()
        if (query.isEmpty()) return ToolJson.error("app is required.")
        val pm = context.packageManager
        val launchables = pm.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
            0,
        )
        val match = launchables.firstOrNull {
            it.loadLabel(pm).toString().lowercase().contains(query)
        } ?: launchables.firstOrNull {
            it.activityInfo.packageName.lowercase().contains(query)
        } ?: return ToolJson.error("No installed app matched '$query'.")
        val intent = pm.getLaunchIntentForPackage(match.activityInfo.packageName)
            ?: return ToolJson.error("App has no launch intent.")
        intent.addFlags(NEW_TASK)
        context.startActivity(intent)
        return ToolJson.ok("opened" to match.loadLabel(pm).toString())
    }
}

/** Open a URL in the browser. */
class OpenUrlTool : AgentTool {
    override val name = "open_url"
    override val description = "Open a web URL (http/https) in the browser."
    override val parametersSchema = ToolJson.schema(
        "url" to ToolJson.stringProp("Full http(s) URL"),
        required = listOf("url"),
    )

    override fun execute(context: Context, args: JSONObject): ToolResult {
        val raw = args.optString("url").trim()
        val url = when {
            raw.startsWith("http://") || raw.startsWith("https://") -> raw
            raw.startsWith("localhost") || !raw.contains("://") -> "https://$raw"
            else -> return ToolJson.error("Only http(s) URLs are allowed.")
        }
        return try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(NEW_TASK))
            ToolJson.ok("opened" to url)
        } catch (e: Exception) {
            ToolJson.error("No browser available: ${e.message ?: ""}")
        }
    }
}

/** Set a countdown timer via the system clock app. */
class SetTimerTool : AgentTool {
    override val name = "set_timer"
    override val description = "Set a countdown timer (e.g. 'remind me in 10 minutes')."
    override val parametersSchema = ToolJson.schema(
        "seconds" to ToolJson.intProp("Timer duration in seconds"),
        "message" to ToolJson.stringProp("Optional timer label"),
        required = listOf("seconds"),
    )

    override fun execute(context: Context, args: JSONObject): ToolResult {
        val seconds = args.optInt("seconds", -1)
        if (seconds <= 0) return ToolJson.error("seconds must be positive.")
        val intent = Intent(AlarmClock.ACTION_SET_TIMER)
            .putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            .putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            .addFlags(NEW_TASK)
        args.optString("message").takeIf { it.isNotBlank() }?.let {
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, it)
        }
        return try {
            context.startActivity(intent)
            ToolJson.ok("timer_seconds" to seconds)
        } catch (e: Exception) {
            ToolJson.error("No clock app accepted the timer: ${e.message ?: ""}")
        }
    }
}

/** Set an alarm via the system clock app. */
class SetAlarmTool : AgentTool {
    override val name = "set_alarm"
    override val description = "Set an alarm for a specific time of day (24h clock)."
    override val parametersSchema = ToolJson.schema(
        "hour" to ToolJson.intProp("Hour 0-23"),
        "minute" to ToolJson.intProp("Minute 0-59"),
        "message" to ToolJson.stringProp("Optional alarm label"),
        required = listOf("hour", "minute"),
    )

    override fun execute(context: Context, args: JSONObject): ToolResult {
        val hour = args.optInt("hour", -1)
        val minute = args.optInt("minute", -1)
        if (hour !in 0..23 || minute !in 0..59) return ToolJson.error("hour (0-23) and minute (0-59) required.")
        val intent = Intent(AlarmClock.ACTION_SET_ALARM)
            .putExtra(AlarmClock.EXTRA_HOUR, hour)
            .putExtra(AlarmClock.EXTRA_MINUTES, minute)
            .putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            .addFlags(NEW_TASK)
        args.optString("message").takeIf { it.isNotBlank() }?.let {
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, it)
        }
        return try {
            context.startActivity(intent)
            ToolJson.ok("alarm" to "%02d:%02d".format(hour, minute))
        } catch (e: Exception) {
            ToolJson.error("No clock app accepted the alarm: ${e.message ?: ""}")
        }
    }
}

/** List upcoming calendar events for the next 7 days. */
class CheckCalendarTool : AgentTool {
    override val name = "check_calendar"
    override val description = "List the user's calendar events for the next 7 days."
    override val requiredPermission = Manifest.permission.READ_CALENDAR
    override val parametersSchema = ToolJson.schema()

    override fun execute(context: Context, args: JSONObject): ToolResult {
        val now = System.currentTimeMillis()
        val week = now + 7L * 24 * 60 * 60 * 1000
        return try {
            val events = JSONArray()
            CalendarContract.Instances.query(
                context.contentResolver,
                arrayOf(
                    CalendarContract.Instances.TITLE,
                    CalendarContract.Instances.BEGIN,
                    CalendarContract.Instances.END,
                    CalendarContract.Instances.ALL_DAY,
                ),
                now,
                week,
            ).use { cursor ->
                var count = 0
                while (cursor.moveToNext() && count < 15) {
                    val title = cursor.getString(0) ?: "(untitled)"
                    val begin = cursor.getLong(1)
                    val end = cursor.getLong(2)
                    val allDay = cursor.getInt(3) == 1
                    events.put(
                        JSONObject()
                            .put("title", title)
                            .put(
                                "start",
                                SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault()).format(Date(begin))
                            )
                            .put("duration_minutes", if (allDay) 0 else ((end - begin) / 60000))
                            .put("all_day", allDay)
                    )
                    count++
                }
            }
            if (events.length() == 0) ToolJson.error("No events in the next 7 days.")
            else ToolResult.Ok(JSONObject().put("events", events))
        } catch (e: Exception) {
            ToolJson.error("Calendar read failed: ${e.message ?: "no calendar"}")
        }
    }
}

/** Insert a calendar event. */
class AddCalendarEventTool : AgentTool {
    override val name = "add_calendar_event"
    override val description = "Add an event to the user's primary calendar. Times are epoch milliseconds; omit for 'next full hour'."
    override val requiredPermission = Manifest.permission.WRITE_CALENDAR
    override val parametersSchema = ToolJson.schema(
        "title" to ToolJson.stringProp("Event title"),
        "start_epoch_ms" to ToolJson.intProp("Optional start time in epoch milliseconds"),
        "duration_minutes" to ToolJson.intProp("Optional duration in minutes (default 60)"),
        "description" to ToolJson.stringProp("Optional description"),
        required = listOf("title"),
    )

    override fun needsConfirmation(args: JSONObject) = true
    override fun summarize(args: JSONObject): String =
        "Add calendar event \"${args.optString("title", "?")}\""

    override fun execute(context: Context, args: JSONObject): ToolResult {
        val title = args.optString("title").trim()
        if (title.isEmpty()) return ToolJson.error("title is required.")
        val calendarId = primaryCalendarId(context)
            ?: return ToolJson.error("No writable calendar found on this device.")
        val durationMin = args.optInt("duration_minutes", 60).coerceIn(5, 24 * 60)
        val start = args.optLong("start_epoch_ms", 0L).let {
            if (it > 0) it else nextFullHour()
        }
        return try {
            val uri = context.contentResolver.insert(
                CalendarContract.Events.CONTENT_URI,
                android.content.ContentValues().apply {
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.TITLE, title)
                    put(CalendarContract.Events.DESCRIPTION, args.optString("description"))
                    put(CalendarContract.Events.DTSTART, start)
                    put(CalendarContract.Events.DTEND, start + durationMin * 60_000L)
                    put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                },
            )
            if (uri != null) {
                ToolJson.ok(
                    "added" to true,
                    "title" to title,
                    "start" to SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault()).format(Date(start)),
                )
            } else ToolJson.error("Calendar insert returned no event.")
        } catch (e: Exception) {
            ToolJson.error("Calendar insert failed: ${e.message ?: ""}")
        }
    }

    private fun primaryCalendarId(context: Context): Long? {
        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.IS_PRIMARY,
            ),
            null,
            null,
            null,
        ) ?: return null
        var first: Long? = null
        var primary: Long? = null
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(0)
                if (first == null) first = id
                if (c.getInt(2) == 1 && primary == null) primary = id
            }
        }
        return primary ?: first
    }

    private fun nextFullHour(): Long {
        val cal = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.HOUR_OF_DAY, 1)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}

/** Builds the full v1 tool set. */
object ToolFactory {
    fun all(): List<AgentTool> = listOf(
        GetTimeTool(),
        BatteryTool(),
        LocationTool(),
        ListNotificationsTool(NotificationStore),
        ReplyNotificationTool(NotificationStore),
        SearchContactsTool(),
        SendSmsTool(),
        DialTool(),
        OpenAppTool(),
        OpenUrlTool(),
        SetTimerTool(),
        SetAlarmTool(),
        CheckCalendarTool(),
        AddCalendarEventTool(),
    )
}
