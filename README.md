# Android Notification Restyler 🎨

A minimalist, on-device notification interceptor and aesthetic restyler for Android (API 26+).

Built with modern **Jetpack Compose**, **Material 3**, and Kotlin Coroutines, Notification Restyler intercepts raw system notifications, dismisses their cluttered or mismatched OEM cards, and reposter sleek, unified replacements with customizable accent colors, density presets, and monochrome/glyph icons—all while preserving tap launch targets, action buttons, and inline quick replies (`RemoteInput`).

---

## 🔒 Security & Privacy Guarantees

- **Zero Network Permissions:** `android.permission.INTERNET` is strictly forbidden and absent from `AndroidManifest.xml`. No notification contents, titles, or sender metadata ever leave your physical device.
- **Zero Third-Party Analytics / Telemetry:** No tracking libraries, no Firebase, no advertising SDKs.
- **Zero Heavy Databases:** Stored purely via lightweight, encrypted Android Preferences DataStore.
- **Pure Local Interception:** All transformation (grayscale matrix filtering, accent tinting, and layout composition) runs 100% on-device in memory.

---

## 🌟 Key Features

1. **Seamless Notification Interception:**
   - Powered by Android's native `NotificationListenerService`.
   - Intercepts incoming notifications, prevents duplicates by cancelling the original notification shade entry, and posts a restyled replacement.
   - Intelligent loop detection (`sbn.packageName == packageName`) and cancellation loop breaker.

2. **Full Interactivity & Quick Reply Preservation:**
   - **Content Intent:** Tap-to-open directly launches the source app without trampoline delays.
   - **Action Buttons:** Carries over original quick actions (e.g. "Mark as Read", "Archive").
   - **Inline Quick Reply (`RemoteInput`):** Directly reply to messages (WhatsApp, Telegram, Signal) right from the restyled notification card.

3. **Minimalist Aesthetic Customization:**
   - **Accent Palette:** Material 3 color presets (Slate, Emerald, Indigo, Amber, Rose, Monochrome Charcoal) or custom Hex input (`#RRGGBB`).
   - **Density Modes:**
     - `Comfortable`: `BigTextStyle` multi-line expanded preview with generous spacing.
     - `Compact`: Single-line collapsed layout for maximum information density.
   - **Icon Modes:**
     - `App Icon`: Original full-color app icon.
     - `Monochrome`: Grayscale saturation filter tinted with your chosen accent color.
     - `Generic Glyph`: Clean status-bar glyph only.

4. **Dynamic Live Preview:**
   - Live Jetpack Compose card in the settings UI reflecting changes to accent color, density, and icon modes in real time.

5. **Per-App Granular Control:**
   - Modal bottom sheet displaying all installed launchable applications.
   - Instant search filter by app name or package identifier.
   - Bulk "All On" and "All Off" toggles.
   - Individual exclusions to leave sensitive or specialized apps untouched.

6. **OEM Background Kill Resilience:**
   - Proactive warning banner when notification access is disconnected or revoked.
   - Two-tier reconnection engine (`NotificationListenerService.requestRebind()` + component-toggle fallback).
   - Direct deep-links to request OEM battery optimization exemptions (Samsung One UI, Xiaomi HyperOS, OnePlus OxygenOS).

---

## 🛠️ Architecture & Tech Stack

- **Language:** Kotlin 2.0.20
- **UI Toolkit:** Jetpack Compose (BOM `2024.06.00`), Material 3
- **Build System:** Gradle 8.9, Android Gradle Plugin 8.5.2
- **Minimum SDK:** API 26 (Android 8.0 Oreo)
- **Target SDK:** API 34 (Android 14)
- **Architecture Pattern:** Unidirectional Data Flow (UDF) with MVVM, `StateFlow`, and `CoroutineScope`
- **Data Persistence:** Jetpack Preferences DataStore (Atomic Protobuf file writes)
- **No DI Frameworks:** Clean Kotlin singleton / composition dependency pattern (Zero Dagger/Hilt/Koin overhead)

---

## 🚀 Building & Running

### Prerequisites
- JDK 17 (e.g., Eclipse Temurin 17 or OpenJDK 17)
- Android SDK with Platform 34 installed

### 1. Build Debug APK
```bash
./gradlew assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Run Automated Unit Tests
```bash
./gradlew testDebugUnitTest
```

### 3. Run Android Lint (Static Analysis)
```bash
./gradlew lintDebug
```

---

## 📱 Installation & Setup Guide

### Installing via ADB
With your Android device connected via USB or wireless debugging:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Android 13+ (API 33+) "Restricted Settings" Unlocking
When sideloading APKs on Android 13, 14, 15, or 16, Android OS greys out the Notification Access toggle for security. Follow these steps:
1. Open phone **Settings** -> **Apps** -> **Notification Restyler**.
2. Tap the **three vertical dots (⋮)** in the top-right corner.
3. Tap **Allow restricted settings**.
4. Authenticate with your fingerprint or PIN.
5. Open **Notification Restyler** and tap **Enable Notification Access**.
6. Toggle on **Notification Restyler** in the system list.

---

## 🧪 Testing Runbook & ADB Simulation

You can test notification interception and restyling directly from your computer terminal without needing third-party messaging apps.

### 1. Allow Listener via ADB (Fast Setup)
```bash
adb shell cmd notification allow_listener com.notif.restyler/com.notif.restyler.service.RestylerNotificationListenerService
```

### 2. Post Test Notifications via ADB
**Post a standard single-line notification:**
```bash
adb shell cmd notification post -t "Test Title" 'sample_tag' "Hello from simulated notification!"
```

**Post a BigText expandable notification:**
```bash
adb shell cmd notification post -S bigtext -t "Sender Name" 'msg_tag' "This is a multi-line notification simulating a direct message conversation on Android."
```

### 3. Inspect Active Restyled Notifications
Check active notifications in the system notification shade:
```bash
adb shell cmd notification list
```
*(Look for lines starting with `0|com.notif.restyler|...` which indicates the notification was successfully restyled and reposted under Restyler's alerting channel.)*

Verify notification details in dumpsys:
```bash
adb shell dumpsys notification --noredact | grep -A 8 "pkg=com.notif.restyler"
```

### 4. Test Process Kill Recovery
Simulate aggressive OEM background task killing:
```bash
adb shell am force-stop com.notif.restyler
```
Reopen the app:
```bash
adb shell am start -n com.notif.restyler/.ui.MainActivity
```
The service will automatically reconnect or allow you to tap the **Reconnect Service** action chip.

---

## ✅ Physical Device Verification Checklist

Use this checklist to perform complete manual verification on a physical device:

### 1. Onboarding & Permissions
- [x] Fresh install on Android 13+ device (`adb install -r app/build/outputs/apk/debug/app-debug.apk`).
- [x] Verify `POST_NOTIFICATIONS` runtime prompt appears on first launch.
- [x] Verify tap on "Enable Notification Access" opens system listener settings.
- [x] Verify green "Restyler Active" badge appears once permission is granted.
- [x] Tap "Setup Guide (Help)" icon to verify "Restricted Settings" instructions.

### 2. Basic Restyle
- [x] Post a notification (`adb shell cmd notification post -t "Test" 'tag' "Body"`).
- [x] Verify original notification disappears from shade.
- [x] Verify restyled replacement appears under `channel_restyled_alerting`.
- [x] Verify accent color matches the chosen theme in settings.
- [x] Verify tap opens source application directly (`contentIntent`).

### 3. Inline Reply (`RemoteInput`)
- [ ] Receive a message from a chat app (WhatsApp, Telegram, or Signal).
- [ ] Tap "Reply" directly on the restyled notification card.
- [ ] Type a reply and send.
- [ ] Verify message sends successfully without opening the source app.

### 4. Density & Icon Style Updates
- [x] Switch density from `Comfortable` to `Compact` in settings.
- [x] Post a new notification; verify single-line compact layout is applied.
- [x] Switch icon style from `App Icon` to `Monochrome` or `Generic Glyph`.
- [x] Post a new notification; verify largeIcon transformation matches setting.

### 5. Excluded Categories & Apps
- [ ] Start Google Maps turn-by-turn navigation or receive a phone call.
- [x] Verify ongoing navigation banner and incoming call HUD are untouched.
- [x] In "Manage Apps", toggle off a specific app (e.g., YouTube).
- [x] Verify notifications from excluded apps remain in their original form.

### 6. Dismissal Synchronization
- [ ] Swipe away restyled notification from status bar shade.
- [ ] Verify original notification is not re-posted.
- [ ] Dismiss notification inside source app; verify restyled notification disappears automatically (`onNotificationRemoved`).
