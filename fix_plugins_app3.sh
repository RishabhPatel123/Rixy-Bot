#!/bin/bash
sed -i 's/id("com.google.dagger.hilt.android")//g' app/build.gradle.kts
sed -i 's/alias(libs.plugins.android.application)/alias(libs.plugins.android.application)\n  id("com.google.dagger.hilt.android")/g' app/build.gradle.kts
