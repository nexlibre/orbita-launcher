#!/usr/bin/env bash
set -euo pipefail
./gradlew --no-daemon :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.nexlibre.orbita.LauncherSmokeTest :fixture:assembleDebug
run_case() {
  local method="$1"
  adb shell am instrument -w -e class "org.nexlibre.orbita.ProcessAcceptanceTest#$method" org.nexlibre.orbita.test/androidx.test.runner.AndroidJUnitRunner | tee "case-$method.log"
  python3 - "$method" <<'PY'
from pathlib import Path
import sys
text=Path('case-'+sys.argv[1]+'.log').read_text()
assert 'OK (1 test)' in text and 'FAILURES' not in text, text
PY
}
run_case seed
adb shell am force-stop org.nexlibre.orbita
run_case verifyAfterRestart
adb shell cmd package set-home-activity org.nexlibre.orbita/.MainActivity
adb shell input keyevent KEYCODE_HOME
sleep 2
adb shell dumpsys activity activities > home-activity.log
python3 - <<'PY'
from pathlib import Path
import re
text=Path('home-activity.log').read_text()
assert re.search(r'(mResumedActivity|topResumedActivity|ResumedActivity).*org.nexlibre.orbita/.MainActivity',text),text
PY
adb push fixture/build/outputs/apk/debug/fixture-debug.apk /data/local/tmp/orbita-fixture.apk
adb shell am instrument -w -e class org.nexlibre.orbita.LiveCatalogTest org.nexlibre.orbita.test/androidx.test.runner.AndroidJUnitRunner | tee case-live-catalog.log
python3 - <<'PYTEST'
from pathlib import Path
text=Path('case-live-catalog.log').read_text()
assert 'OK (1 test)' in text and 'FAILURES' not in text,text
PYTEST
run_case fixtureAbsentLayoutPreserved
adb shell settings put system font_scale 2.0
adb shell am instrument -w -e class org.nexlibre.orbita.LargeTextTest org.nexlibre.orbita.test/androidx.test.runner.AndroidJUnitRunner | tee case-large-text.log
python3 - <<'PYTEST'
from pathlib import Path
text=Path('case-large-text.log').read_text()
assert 'OK (1 test)' in text and 'FAILURES' not in text,text
PYTEST
adb shell settings put system font_scale 1.0
adb reboot
adb wait-for-device
for i in {1..90}; do
  if [[ "$(adb shell getprop sys.boot_completed | tr -d '\r')" == 1 ]]; then break; fi
  sleep 2
done
adb shell input keyevent KEYCODE_WAKEUP
adb shell input keyevent KEYCODE_MENU
run_case verifyAfterRestart
adb shell am force-stop org.nexlibre.orbita
printf 'invalid-protobuf' | adb shell run-as org.nexlibre.orbita tee files/datastore/orbita_core_v1.preferences_pb >/dev/null
run_case corruptionIsBlockedAndOriginalArchived
run_case explicitRecoveryRestoresValidLayout
