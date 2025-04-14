# PantryPal Project Setup Guide

## Java Compatibility

This project has been configured to work with Java 11, Java 17, and JetBrains Runtime (JBR). 

The Android Gradle Plugin version has been downgraded to 7.4.2 to ensure compatibility with Java 11, while still maintaining compatibility with Java 17 and JetBrains Runtime.

## Setup Instructions

### Option 1: Use the Setup Script

1. Run the `jdksetup.bat` script from the project root directory (Windows only)
2. Follow the on-screen instructions

### Option 2: Manual Setup

1. Ensure your system has Java 11 or Java 17 installed
2. Verify your `JAVA_HOME` environment variable is set correctly
3. Make sure `gradle.properties` doesn't have a specific Java path set (or comment it out)

## Troubleshooting

If you encounter build errors related to Java version, try these steps:

1. Check your Java version: `java -version`
2. Verify JAVA_HOME: `echo %JAVA_HOME%` (Windows) or `echo $JAVA_HOME` (Mac/Linux)
3. If needed, set JAVA_HOME to your Java 11 or 17 installation:
   - Windows: `set JAVA_HOME=C:\path\to\jdk`
   - Mac/Linux: `export JAVA_HOME=/path/to/jdk`

## Android Studio Configuration

Android Studio comes with its own JetBrains Runtime. To use it:

1. Open File > Settings > Build, Execution, Deployment > Build Tools > Gradle
2. Set "Gradle JDK" to "Embedded JDK" or "JetBrains Runtime"

## Project Changes

The following changes were made to ensure compatibility:

1. Android Gradle Plugin downgraded to 7.4.2
2. Kotlin version set to 1.8.10
3. CompileSDK and targetSDK set to 34
4. Java compatibility set to Java 11
5. Gradle JVM arguments optimized for build performance

These changes should ensure the project builds consistently across different development environments. 