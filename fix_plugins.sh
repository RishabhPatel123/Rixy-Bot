#!/bin/bash
sed -i 's/alias(libs.plugins.hilt.android.plugin) apply false/id("com.google.dagger.hilt.android") version "2.51.1" apply false/g' build.gradle.kts
