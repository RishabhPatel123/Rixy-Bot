#!/bin/bash
sed -i '/koin = "4.0.0"/d' gradle/libs.versions.toml
sed -i '/\[versions\]/a \koin = "4.0.0"' gradle/libs.versions.toml
