#!/bin/bash
sed -i '/implementation(libs.androidx.hilt.navigation.compose)/a \  implementation(libs.androidx.hilt.work)\n  ksp(libs.androidx.hilt.compiler)' app/build.gradle.kts
