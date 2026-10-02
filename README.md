# Bel9ja Board Club — Android Studio Project

A native Android app (Kotlin + Jetpack Compose, Material 3) implementing the
Bel9ja Board Club spec: Home, Sessions with filters, Event Details, Session
Registration (no account, generates a reference number), Games Library,
Weekly Schedule, and a Club section (About / Join / FAQ / Contact).

No login, sign-up, profile, or account system anywhere in the app, per the
original brief.

## Opening the project

1. Install **Android Studio** (Koala or newer recommended).
2. `File > Open`, select this folder (the one containing `settings.gradle.kts`).
3. If Android Studio reports the Gradle wrapper is missing, accept its offer
   to generate one automatically (or run `Tools > Gradle Wrapper` manually).
   It hasn't been committed here since it requires a binary jar file.
4. Let Gradle sync. It will download the Android Gradle Plugin, Kotlin
   plugin, and AndroidX/Compose libraries from Google's Maven — this needs
   an internet connection the first time.
5. Run the `app` configuration on an emulator (API 26+) or a physical device.

## Project structure

```
app/src/main/java/com/bel9ja/boardclub/
  MainActivity.kt              Entry point
  data/Models.kt               Data classes (BoardEvent, BoardGame, etc.)
  data/SampleData.kt           Sample events, games, schedule, FAQs
  ui/Screen.kt                 Navigation state (sealed class)
  ui/AppRoot.kt                Bottom nav + screen switching
  ui/theme/                    Colors, typography, Material 3 theme
  ui/components/               Reusable cards, chips, form fields
  ui/screens/                  One file per screen (Home, Sessions, etc.)

app/src/main/res/
  values/strings.xml           App name lives here
  drawable/ic_launcher_*.xml   Adaptive launcher icon (vector, hex mark)
  mipmap-anydpi-v26/           Adaptive icon wiring
```

## Changing the app name or icon

- **App name**: edit `app_name` in `app/src/main/res/values/strings.xml`.
- **Icon**: right-click `app` → `New > Image Asset` in Android Studio to
  replace the current vector-based hexagon mark with your own artwork.

## What's not included (you'll need to add before shipping)

- **Real registration storage.** Forms currently just generate a reference
  number in memory and show a confirmation — nothing is sent anywhere.
  You'll need a backend (Firebase, a REST API, Google Sheets, email
  forwarding, etc.) wired into the `onSubmitted` / `onClick` handlers in
  `RegistrationScreen.kt`, `ClubScreen.kt` (Join and Contact forms).
- **A real map** on the Contact screen (currently a placeholder box).
- **App screenshots, feature graphic, and store listing copy** for Google
  Play.
- **A signed release build** (keystore) and a Google Play Console developer
  account ($25 one-time fee).

## Submitting to Google Play — outline

1. In Android Studio: `Build > Generate Signed Bundle / APK`, choose
   **Android App Bundle**, create or select a keystore, build the release.
2. Create a Google Play Console developer account if you don't have one.
3. Create a new app, fill in the store listing (title, description,
   screenshots, feature graphic, privacy policy URL — required even for
   apps with no accounts, since you still collect names/emails/phone
   numbers via the forms).
4. Complete the required questionnaires: Content rating, Data safety
   (declare what the registration/contact forms collect and why), Target
   audience, Ads (none, if you haven't added any).
5. Upload the `.aab` file to a testing or production track.
6. Submit for review. Google's review typically takes anywhere from a few
   hours to a few days.

This project gives you a working, submittable app shell — the store
listing, signing, and legal/privacy steps above are account-specific and
need to be done by you in the Play Console.
