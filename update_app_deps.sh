#!/bin/bash
sed -i '/dependencies {/a \  implementation(libs.koin.android)\n  implementation(libs.koin.androidx.compose)\n  implementation(libs.koin.androidx.workmanager)' app/build.gradle.kts
