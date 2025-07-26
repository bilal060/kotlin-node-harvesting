## Mobile App Build & Installation Guide

### Fix Gradle Build Error (Exit Code 130)

The error code 130 typically indicates the build process was interrupted or there are configuration issues. Let's fix this:

#### Step 1: Clean and Reset Flutter Project

```bash
cd /Users/mac/Desktop/simpleApp/App

# Clean Flutter project
flutter clean

# Get dependencies again
flutter pub get

# Clean Gradle cache
cd android
./gradlew clean
cd ..
```

#### Step 2: Check Flutter Configuration

```bash
# Check Flutter doctor for issues
flutter doctor -v

# Check connected devices
flutter devices

# If no devices shown, enable USB debugging on your phone
```

#### Step 3: Fix Android Configuration Issues

1. **Update Android Gradle Plugin** - Create/update `android/build.gradle`:
```gradle
buildscript {
    ext.kotlin_version = '1.7.10'
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath 'com.android.tools.build:gradle:7.3.0'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.buildDir = '../build'
subprojects {
    project.buildDir = "${rootProject.buildDir}/${project.name}"
}
subprojects {
    project.evaluationDependsOn(':app')
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

2. **Update App-level Gradle** - Update `android/app/build.gradle`:
```gradle
def localProperties = new Properties()
def localPropertiesFile = rootProject.file('local.properties')
if (localPropertiesFile.exists()) {
    localPropertiesFile.withReader('UTF-8') { reader ->
        localProperties.load(reader)
    }
}

def flutterRoot = localProperties.getProperty('flutter.sdk')
if (flutterRoot == null) {
    throw new GradleException("Flutter SDK not found. Define location with flutter.sdk in the local.properties file.")
}

def flutterVersionCode = localProperties.getProperty('flutter.versionCode')
if (flutterVersionCode == null) {
    flutterVersionCode = '1'
}

def flutterVersionName = localProperties.getProperty('flutter.versionName')
if (flutterVersionName == null) {
    flutterVersionName = '1.0'
}

apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply from: "$flutterRoot/packages/flutter_tools/gradle/flutter.gradle"

android {
    compileSdkVersion 33
    ndkVersion flutter.ndkVersion

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = '1.8'
    }

    sourceSets {
        main.java.srcDirs += 'src/main/kotlin'
    }

    defaultConfig {
        applicationId "com.example.device_sync_app"
        minSdkVersion 21
        targetSdkVersion 33
        versionCode flutterVersionCode.toInteger()
        versionName flutterVersionName
        multiDexEnabled true
    }

    buildTypes {
        release {
            signingConfig signingConfigs.debug
        }
    }
}

flutter {
    source '../..'
}

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    implementation 'androidx.multidex:multidex:2.0.1'
}
```

#### Step 4: Create Simplified pubspec.yaml

Let's create a minimal version first to avoid dependency conflicts:

```yaml
name: device_sync_app
description: A Flutter app for device synchronization.

publish_to: 'none'
version: 1.0.0+1

environment:
  sdk: '>=3.0.0 <4.0.0'

dependencies:
  flutter:
    sdk: flutter
  
  # Core dependencies only
  http: ^1.1.0
  shared_preferences: ^2.2.2
  permission_handler: ^11.0.1
  device_info_plus: ^9.1.0
  workmanager: ^0.5.1
  flutter_riverpod: ^2.4.5
  
  cupertino_icons: ^1.0.2

dev_dependencies:
  flutter_test:
    sdk: flutter
  flutter_lints: ^3.0.0

flutter:
  uses-material-design: true
```

#### Step 5: Connect Your Mobile Device

1. **Enable Developer Options on your phone:**
   - Go to Settings > About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings > Developer Options
   - Enable "USB Debugging"

2. **Connect via USB and verify:**
```bash
# Check if device is detected
adb devices

# Should show your device like:
# List of devices attached
# ABC123DEF456    device
```

#### Step 6: Install and Run

```bash
# Install directly to connected device
flutter install

# Or run in debug mode
flutter run --debug

# If multiple devices, specify target
flutter run -d <device-id>
```

### Alternative: Build APK and Install Manually

If the direct run still fails, build an APK:

```bash
# Build APK
flutter build apk --debug

# The APK will be in: build/app/outputs/flutter-apk/app-debug.apk

# Install via ADB
adb install build/app/outputs/flutter-apk/app-debug.apk
```

### Troubleshooting Common Issues

#### Issue 1: Android SDK not found
```bash
# Set Android SDK path
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

#### Issue 2: Gradle version mismatch
Update `android/gradle/wrapper/gradle-wrapper.properties`:
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-7.5-all.zip
```

#### Issue 3: Kotlin version conflicts
Ensure consistent Kotlin versions across all gradle files.

#### Issue 4: Multidex issues
Add to `android/app/build.gradle`:
```gradle
android {
    defaultConfig {
        multiDexEnabled true
    }
}

dependencies {
    implementation 'androidx.multidex:multidex:2.0.1'
}
```

### Quick Commands Summary

```bash
# Complete reset and rebuild
cd /Users/mac/Desktop/simpleApp/App
flutter clean
flutter pub get
cd android && ./gradlew clean && cd ..
flutter doctor -v
flutter devices
flutter run --debug
```

### If All Else Fails - Minimal Working Version

I can create a simplified version with just basic functionality to get you started, then we can add features incrementally.
