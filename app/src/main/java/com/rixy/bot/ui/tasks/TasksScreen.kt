package com.rixy.bot.ui.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.rixy.bot.R
import com.rixy.bot.data.model.PlannedTaskEntity
import com.rixy.bot.data.repo.ChatRepository
import com.rixy.bot.ui.components.EmptyState
import com.rixy.bot.ui.theme.Spacing
import com.rixy.bot.ui.theme.TextTertiary
import com.rixy.bot.ui.theme.Warning
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(private val repository: ChatRepository) : ViewModel() {
    val tasks = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markDone(id: Long) = update(id, PlannedTaskEntity.STATUS_DONE)
    fun dismiss(id: Long) = update(id, PlannedTaskEntity.STATUS_DISMISSED)
    fun reopen(id: Long) = update(id, PlannedTaskEntity.STATUS_OPEN)

    fun delete(task: PlannedTaskEntity) {
        viewModelScope.launch { runCatching { repository.deleteTask(task) } }
    }

    private fun update(id: Long, status: String) {
        viewModelScope.launch { runCatching { repository.updateTaskStatus(id, status) } }
    }
}

@Composable
fun TasksScreen(
    onBack: () -> Unit,
    viewModel: TasksViewModel,
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xs, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.drawer_tasks),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                stringResource(R.string.tasks_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        if (tasks.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Checklist,
                title = stringResource(R.string.tasks_empty_title),
                message = stringResource(R.string.tasks_empty_message),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    top = Spacing.md,
                    bottom = Spacing.xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onDone = { viewModel.markDone(task.id) },
                        onDismiss = { viewModel.dismiss(task.id) },
                        onReopen = { viewModel.reopen(task.id) },
                        onDelete = { viewModel.delete(task) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: PlannedTaskEntity,
    onDone: () -> Unit,
    onDismiss: () -> Unit,
    onReopen: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (task.status == PlannedTaskEntity.STATUS_DONE) {
                        TextTertiary
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    },
                    modifier = Modifier.weight(1f),
                )
                PriorityChip(task.priority)
            }
            if (task.description.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (task.status == PlannedTaskEntity.STATUS_OPEN) {
                    TextButton(onClick = onDone) {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            stringResource(R.string.task_mark_done),
                            modifier = Modifier.padding(start = Spacing.xs),
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.RemoveCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            stringResource(R.string.task_dismiss),
                            modifier = Modifier.padding(start = Spacing.xs),
                        )
                    }
                } else {
                    TextButton(onClick = onReopen) {
                        Text(stringResource(R.string.task_reopen))
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.task_delete),
                        tint = TextTertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityChip(priority: String) {
    val (labelRes, color) = when (priority.uppercase()) {
        "HIGH" -> R.string.task_priority_high to Warning
        "LOW" -> R.string.task_priority_low to TextTertiary
        else -> R.string.task_priority_medium to TextTertiary
    }
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = color.copy(alpha = 0.14f),
    ) {
        Text(
            stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
        )
    }
}
