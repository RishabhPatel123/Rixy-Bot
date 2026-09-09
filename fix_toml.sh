#!/bin/bash
sed -i '/koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }/d' gradle/libs.versions.toml
sed -i '/koin-androidx-compose = { group = "io.insert-koin", name = "koin-androidx-compose", version.ref = "koin" }/d' gradle/libs.versions.toml
sed -i '/koin-androidx-workmanager = { group = "io.insert-koin", name = "koin-androidx-workmanager", version.ref = "koin" }/d' gradle/libs.versions.toml
sed -i '/\[plugins\]/i \koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }\nkoin-androidx-compose = { group = "io.insert-koin", name = "koin-androidx-compose", version.ref = "koin" }\nkoin-androidx-workmanager = { group = "io.insert-koin", name = "koin-androidx-workmanager", version.ref = "koin" }' gradle/libs.versions.toml
