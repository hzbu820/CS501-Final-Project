@echo off
echo ======================================
echo PantryPal Project JDK Setup Assistant
echo ======================================
echo.

:: Check if JAVA_HOME is set
if "%JAVA_HOME%"=="" (
    echo JAVA_HOME is not set. 
    echo You need to have Java 11 or Java 17 installed.
    echo.
    goto :SEARCH_JAVA
) else (
    echo Current JAVA_HOME: %JAVA_HOME%
    goto :CHECK_VERSION
)

:SEARCH_JAVA
echo Searching for Java installations...
echo.

:: Check common Java installation locations
set POSSIBLE_PATHS=
if exist "C:\Program Files\Java" set POSSIBLE_PATHS=C:\Program Files\Java
if exist "C:\Program Files\Amazon Corretto" set POSSIBLE_PATHS=%POSSIBLE_PATHS%;C:\Program Files\Amazon Corretto
if exist "C:\Program Files\Eclipse Adoptium" set POSSIBLE_PATHS=%POSSIBLE_PATHS%;C:\Program Files\Eclipse Adoptium
if exist "%LOCALAPPDATA%\Programs\Eclipse Adoptium" set POSSIBLE_PATHS=%POSSIBLE_PATHS%;%LOCALAPPDATA%\Programs\Eclipse Adoptium
if exist "%PROGRAMFILES%\Android\Android Studio\jbr" set POSSIBLE_PATHS=%POSSIBLE_PATHS%;%PROGRAMFILES%\Android\Android Studio\jbr
if exist "%PROGRAMFILES%\JetBrains\IntelliJ IDEA*\jbr" set POSSIBLE_PATHS=%POSSIBLE_PATHS%;%PROGRAMFILES%\JetBrains\IntelliJ IDEA*\jbr
if exist "%PROGRAMFILES%\Android\Android Studio\jre" set POSSIBLE_PATHS=%POSSIBLE_PATHS%;%PROGRAMFILES%\Android\Android Studio\jre

:: Display found paths
echo Found these potential Java paths:
echo %POSSIBLE_PATHS%
echo.
echo You need to set JAVA_HOME manually using one of these paths.
echo Example: set JAVA_HOME=C:\Program Files\Java\jdk-11
echo.
goto :END

:CHECK_VERSION
:: Extract version from java -version output
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)

:: Remove quotes
set JAVA_VERSION=%JAVA_VERSION:"=%

echo Detected Java version: %JAVA_VERSION%
echo.

:: Check if it's version 11 or 17
echo %JAVA_VERSION% | findstr /C:"11." > nul
if %errorlevel% equ 0 (
    echo You have Java 11, which is compatible with this project.
    goto :UPDATE_PROPERTIES
)

echo %JAVA_VERSION% | findstr /C:"17." > nul
if %errorlevel% equ 0 (
    echo You have Java 17, which is compatible with this project.
    goto :UPDATE_PROPERTIES
)

echo %JAVA_VERSION% | findstr /C:"21." > nul
if %errorlevel% equ 0 (
    echo You have Java 21. The project is configured for Java 11.
    echo We recommend using Java 11 or 17 for this project.
    echo.
    goto :UPDATE_PROPERTIES
)

echo Your Java version may not be compatible with this project.
echo Please consider installing Java 11 or Java 17.
echo.
goto :END

:UPDATE_PROPERTIES
echo Checking gradle.properties file...
if exist "gradle.properties" (
    :: Check if org.gradle.java.home is uncommented
    findstr /C:"org.gradle.java.home" gradle.properties > nul
    if %errorlevel% equ 0 (
        echo Found org.gradle.java.home in gradle.properties
        echo We will comment it out to use the system JAVA_HOME instead.
        
        :: Make a backup of the file
        copy gradle.properties gradle.properties.bak
        
        :: Comment out the line with org.gradle.java.home
        powershell -Command "(Get-Content gradle.properties) | ForEach-Object { if ($_ -match 'org.gradle.java.home') { '# ' + $_ } else { $_ } } | Set-Content gradle.properties"
        
        echo Updated gradle.properties.
    ) else (
        echo No org.gradle.java.home found in gradle.properties. No changes needed.
    )
) else (
    echo gradle.properties not found. Make sure you're running this from the project root.
)

:END
echo.
echo If you experience build issues, make sure your system is using the correct Java version.
echo You can also modify gradle.properties to explicitly set the JDK location.
echo.
pause 