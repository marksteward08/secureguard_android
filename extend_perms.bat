@echo off
adb shell appops set com.nud.secureguardtech.dev WRITE_SETTINGS allow
adb shell pm grant com.nud.secureguardtech.dev android.permission.WRITE_SECURE_SETTINGS
pause
