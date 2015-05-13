clip-Android-In-app
===================

Clip's Android SDK Library for In-app point of sale payments

## Overview

* Import Libriaries
  * Manual Installation
    * Android Studio
* Android Manifest Permissions
* Example

## Procedure

### Manual Installation

Download `clipcorepayments-1.0.aar`, `clipposlibrary-1.0.aar`, and `PayclipCommonLibrary-1.0.aar` from the aar/ directory into a local directory.

#### Android Studio

Create a new module for each .aar with File -> New Module -> Import .JAR or .AAR Package.

Add the following dependencies into your app's build.gradle (they should match the generated library modules just created):

```
dependencies {
    compile project(':clipcorepayments-1.0')
    compile project(':clipposlibrary-1.0')
    compile project(':PayclipCommonLibrary-1.0')
}
```
### Android Manifest Permissions

Add the following permissions to your app's AndroidManifest.xml:

```xml
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
```

## Example

See the folder Clip-POS-Library for an example project that was setup up using the manual installation instructions. There are three different activities for the three different types of payment methods. Manual/Key-entered, Swipe, and EMV. The best way to test them would be to change which activity is the launcher activity within app/src/main/AndroidManifest.xml
