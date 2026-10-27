# Auto Contact Saver

An Android app that automatically saves incoming callers as contacts. When a call
comes in, the app detects the caller's number, saves it to the phonebook, stores a
call-log entry locally, and posts a notification once the contact is saved.

## Features

- Automatic detection of incoming calls via a `CallScreeningService` (Android 10+) and a `PhoneStateReceiver`
- Saves unknown callers to your contacts automatically
- Local call-history storage with Room
- Notifications when a contact is saved
- Auto-restarts call monitoring after device reboot
- Jetpack Compose UI with call history, stats, and settings

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Database:** AndroidX Room
- **Architecture:** MVVM (ViewModel + Repository)
- **Build:** Gradle (Kotlin DSL)
- **Min SDK:** 24 (Android 7.0)

## Permissions

The app requests the following permissions, all declared in `AndroidManifest.xml`:

- `READ_PHONE_STATE`, `READ_PHONE_NUMBERS` — detect incoming calls and read the caller number
- `READ_CALL_LOG` — read call-log details
- `READ_CONTACTS`, `WRITE_CONTACTS` — read existing contacts and save new ones
- `POST_NOTIFICATIONS` — notify when a contact is saved
- `RECEIVE_BOOT_COMPLETED` — resume monitoring after reboot
- `CALL_PHONE` — optional direct call action

## Run Locally

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. Open Android Studio.
2. Select **Open** and choose this project directory.
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Connect an emulator or physical device.
5. Run the `MainActivity` configuration.

Runtime permissions are requested from the app UI on first use; grant them for full
functionality.

## Project Structure

```
app/src/main/java/com/example/
├── AutoContactSaverApp.kt        # Application entry, wires repository & settings
├── MainActivity.kt               # Compose host activity
├── contact/ContactHelper.kt      # Saves contacts to the phonebook
├── data/
│   ├── db/                       # Room database, DAO, entities
│   ├── preferences/AppSettings  # User settings (auto-save toggle, etc.)
│   └── repository/CallLogRepository
├── receiver/                     # Call detection & processing receivers
├── service/IncomingCallScreeningService  # Android 10+ real-time screening
└── ui/                           # Compose screens, view model, components
```

## License

Private / unreleased.
