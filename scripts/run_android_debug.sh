#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ANDROID_DIR="$REPO_ROOT/android"

APP_ID="com.travelfinder"
MAIN_ACTIVITY=".presentation.ui.MainActivity"
AVD_NAME="TravelFinder_API34"

export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/.android-sdk}"
export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$PATH"

if ! adb devices | awk 'NR>1 {print $2}' | rg -q '^device$'; then
  echo "No running emulator/device found. Booting $AVD_NAME ..."
  nohup emulator -avd "$AVD_NAME" -netdelay none -netspeed full >/tmp/travelfinder-emulator.log 2>&1 &
  adb wait-for-device
fi

echo "Building + installing debug APK..."
"$ANDROID_DIR/gradlew" -p "$ANDROID_DIR" :app:installDebug

echo "Launching app..."
adb shell am start -n "$APP_ID/$MAIN_ACTIVITY"

echo "Done."
