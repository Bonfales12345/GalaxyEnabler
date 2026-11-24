#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
DIST_DIR="${REPO_ROOT}/dist"
OUT_APK="${DIST_DIR}/GalaxyEnabler.apk"
RELEASE_APK="${SCRIPT_DIR}/app/build/outputs/apk/release/app-release.apk"

if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME}/bin/java" ]; then
  if [ -x "/usr/libexec/java_home" ]; then
    JAVA_HOME="$(/usr/libexec/java_home -v 17+ 2>/dev/null || /usr/libexec/java_home 2>/dev/null || true)"
  elif [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
    JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  elif [ -n "${PROGRAMFILES:-}" ] && [ -d "${PROGRAMFILES}/Android/Android Studio/jbr" ]; then
    JAVA_HOME="${PROGRAMFILES}/Android/Android Studio/jbr"
  elif [ -d "/usr/lib/jvm/java-21-openjdk-amd64" ]; then
    JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
  elif [ -d "/usr/lib/jvm/java-17-openjdk-amd64" ]; then
    JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
  elif [ -d "/usr/lib/jvm/default-java" ]; then
    JAVA_HOME="/usr/lib/jvm/default-java"
  elif command -v java >/dev/null 2>&1; then
    JAVA_BIN="$(command -v java)"
    if command -v readlink >/dev/null 2>&1; then
      JAVA_REAL="$(readlink -f "${JAVA_BIN}" 2>/dev/null || echo "${JAVA_BIN}")"
      JAVA_HOME="$(cd "$(dirname "${JAVA_REAL}")/.." && pwd)"
    fi
  fi
fi

if [ -n "${JAVA_HOME:-}" ] && [ -d "${JAVA_HOME}" ]; then
  export JAVA_HOME
  export PATH="${JAVA_HOME}/bin:${PATH}"
fi

if [ -z "${ANDROID_HOME:-}" ] && [ -n "${ANDROID_SDK_ROOT:-}" ] && [ -d "${ANDROID_SDK_ROOT}" ]; then
  export ANDROID_HOME="${ANDROID_SDK_ROOT}"
fi

if [ -z "${ANDROID_HOME:-}" ]; then
  for candidate in \
    "${HOME}/Library/Android/sdk" \
    "${HOME}/Android/Sdk" \
    "${LOCALAPPDATA:-}/Android/Sdk" \
    "/opt/android-sdk" \
    "/usr/local/lib/android/sdk"; do
    if [ -n "${candidate}" ] && [ -d "${candidate}" ]; then
      export ANDROID_HOME="${candidate}"
      break
    fi
  done
fi

chmod +x "${SCRIPT_DIR}/gradlew" 2>/dev/null || true
"${SCRIPT_DIR}/gradlew" --no-daemon -p "${SCRIPT_DIR}" :app:assembleRelease

if [ ! -f "${RELEASE_APK}" ]; then
  echo "Release APK not found at ${RELEASE_APK}" >&2
  exit 1
fi

mkdir -p "${DIST_DIR}"
cp -f "${RELEASE_APK}" "${OUT_APK}"

SIZE_BYTES="$(wc -c < "${OUT_APK}" | tr -d '[:space:]')"
printf 'Built universal APK: %s (%s bytes)\n' "${OUT_APK}" "${SIZE_BYTES}"
