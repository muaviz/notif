# Brief: Android Notification Restyler (MVP)

Build an Android app that intercepts notifications from all other apps and reposts them with a consistent, user-customizable look.

## Scope

- Android only.
- Kotlin, single module, Jetpack Compose for the settings UI.
- minSdk 26, targetSdk latest stable.
- No DI framework, no database, no backend. Settings in DataStore.
- All processing on-device. No network permission.

## Core behavior

1. User grants notification access (`NotificationListenerService`). Onboarding screen sends them to the system settings page.
2. On `onNotificationPosted`:
   - Skip: own package, ongoing notifications, group summaries, and categories `call`, `transport`, `navigation`, `progress`.
   - Cancel the original (`cancelNotification(key)`).
   - Post a replacement through `NotificationCompat` on the app's own channel.
3. Replacement must preserve:
   - Title, text, big text, timestamp.
   - Original `contentIntent` (tap opens the source app as before).
   - Original actions, including inline reply via the original `RemoteInput`.
4. Replacement styling comes from user settings (below). The original small icon and colors are never used.
5. Map original key to replacement ID. Updates from the same source update the replacement in place. `onNotificationRemoved` removes the replacement.
6. Group replacements by source app.

## Settings (v1)

- Accent color (preset palette plus custom hex).
- Density: compact or comfortable.
- Icon style: app icon, monochrome app icon, or a single generic app glyph.
- Per-app toggle: restyle or leave alone.
- Live preview of a sample notification in the settings screen.

## Constraints

- Keep the design simple. One listener service, one repost function, one settings screen. Add a layer only when a concrete need forces it.
- Handle OEM background kills: show a status indicator on the main screen if the listener is not connected, with a button to rebind (`requestRebind`) and a link to battery-optimization settings.
- Do not log notification content. Do not persist it.

## Out of scope for v1

- Custom overlay or heads-up popups (`SYSTEM_ALERT_WINDOW`).
- Rewriting or summarizing notification content.
- Rules engine, digests, quiet hours.
- Other platforms.
- Restyling ongoing notifications.

## Known issues to handle

- The original may alert before it is cancelled. Document in onboarding that users should mute the source channels or use DND, and let the replacement do the alerting.
- Android templates limit custom layouts. Use decorated custom views where needed; do not fight the system template.

## Deliverables

- Working Android Studio project that builds and installs.
- README with build steps and the permission flow.
- Short test checklist covering: tap action, inline reply, update-in-place, dismissal sync, skipped categories, listener reconnect.

## Done when

On a physical device, notifications from at least three different apps (including one messaging app with inline reply) appear in the app's consistent style, tap and reply work as in the original, and changing a setting changes the next notification.
