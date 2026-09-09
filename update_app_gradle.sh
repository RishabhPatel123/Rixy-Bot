#!/bin/bash
sed -i '/dependencies {/a \  implementation(libs.hilt.android)\n  ksp(libs.hilt.compiler)\n  implementation(libs.androidx.hilt.navigation.compose)' app/build.gradle.kts
