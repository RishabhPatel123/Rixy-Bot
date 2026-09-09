#!/bin/bash
sed -i 's/id("com.google.dagger.hilt.android")//g' app/build.gradle.kts
sed -i '/alias(libs.plugins.android.application)/a \  id("com.google.dagger.hilt.android")' app/build.gradle.kts
