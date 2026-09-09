#!/bin/bash
sed -i 's/id("dagger.hilt.android.plugin")/id("com.google.dagger.hilt.android")/g' app/build.gradle.kts
