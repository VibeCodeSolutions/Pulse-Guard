# Pulse-Guard

> Blutdruck und Puls erfassen. Erinnert an Messungen und Medikamente. Daten bleiben auf dem Gerät. 🩺

## 🇩🇪 Was Pulse-Guard macht

Eine Android-App, die zwei Dinge gut können soll:

- **Werte erfassen** – Blutdruck (systolisch/diastolisch) und Puls per manueller Eingabe, Verlauf als Diagramm.
- **Erinnern** – Push-Benachrichtigungen für die nächste Messung oder Medikamenteneinnahme. Nach einem Smartphone-Neustart laufen die Reminder weiter.

Kein Cloud-Account, kein Sync, keine Tracker. Die Daten liegen in einer lokalen Room-Datenbank auf dem Gerät – Punkt.

### Funktionen

- Blutdruck und Puls erfassen, Verlauf als Chart (über Vico)
- Reminder mit individuellen Zeiten – getrennt für Messungen und Medikamente
- Material 3 Theming, Compose-UI, Splash-Screen

### Tech-Stack

Kotlin · Jetpack Compose · Material 3 · Room · Koin · Vico Charts

### Permissions (warum die App was braucht)

- `POST_NOTIFICATIONS` – um Push-Reminder zeigen zu dürfen
- `SCHEDULE_EXACT_ALARM` – um Reminder zur exakten Uhrzeit auszulösen
- `RECEIVE_BOOT_COMPLETED` – damit gesetzte Reminder einen Smartphone-Neustart überleben

Mehr nicht. Kein Standort, keine Kamera, kein Internetzugriff.

### Anforderungen & Releases

- **Android 12+** (API 31), getestet bis API 36
- Releases: [v1.0 + v1.1](https://github.com/VibeCodeSolutions/Pulse-Guard/releases)
- Lizenz: [MIT](LICENSE)

### Ein Hinweis

Pulse-Guard ist ein Werte-Logbuch, kein Medizinprodukt. Die App misst nichts selbst – sie merkt sich nur, was eingetragen wird. Bei Auffälligkeiten gehört das zur Ärztin oder zum Arzt, nicht in die Push-Notification.

---

## 🇬🇧 What Pulse-Guard does

An Android app focused on two things:

- **Log values** – blood pressure (systolic/diastolic) and pulse, manual input, history as a chart.
- **Remind** – push notifications for the next measurement or medication intake. Reminders survive a phone reboot.

No cloud account, no sync, no trackers. Data stays in a local Room database – that's it.

### Stack

Kotlin · Jetpack Compose · Material 3 · Room · Koin · Vico Charts

### Permissions

- `POST_NOTIFICATIONS` – to show push reminders
- `SCHEDULE_EXACT_ALARM` – to fire reminders at exact times
- `RECEIVE_BOOT_COMPLETED` – so reminders survive a reboot

### Requirements

- Android 12+ (API 31), tested up to API 36
- Releases: [v1.0 + v1.1](https://github.com/VibeCodeSolutions/Pulse-Guard/releases)
- License: [MIT](LICENSE)

### A note

Pulse-Guard is a values log, not a medical device. The app doesn't measure anything itself – it just remembers what you enter. If a reading looks off, that's a conversation for your doctor, not for the push notification.
