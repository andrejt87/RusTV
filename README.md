# RusTV

Русское ТВ на Android — простое приложение для просмотра русскоязычных телеканалов.

## Features
- 📺 38 каналов, категоризированы (Основные, Музыка, Новости, Региональные, и т.д.)
- 🌙 Тёмный дизайн
- ▶️ WebView Fullscreen-Player
- 📱 Min SDK 21 (Android 5.0+)

## Build
```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew assembleDebug
```

## Install
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```
