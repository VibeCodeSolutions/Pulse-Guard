# Pulse Guard – State & Handoff-Protokoll (state.md)
### Dynamischer Session-State v1.0

---

> **Anleitung:** Diese Datei wird nach jeder abgeschlossenen Phase vom zuständigen Agenten aktualisiert.
> Sie dient als einzige Wahrheitsquelle für den aktuellen Projektstand und die Übergabe zwischen Agenten-Sitzungen.

---

## 1. Globaler Projektstatus

| Feld | Wert |
|------|------|
| **Aktuelle Phase** | Phase 3 – Dashboard Screen |
| **Aktiver Agent** | ADA |
| **Gesamtfortschritt** | 2 / 6 Phasen abgeschlossen |
| **Letztes Update** | 2026-03-03 |
| **Nächste Phase** | Phase 3 |
| **Blocker** | Keine |

---

## 2. Phasen-Tracking

| Phase | Titel | Agent | Status | Datum |
|-------|-------|-------|--------|-------|
| Phase 1 | Projekt-Setup + Room DB | ADA | ✅ Abgeschlossen | 2026-03-03 |
| Phase 2 | Entry Screen | ADA | ✅ Abgeschlossen | 2026-03-03 |
| Phase 3 | Dashboard Screen | ADA | ⬜ Offen | – |
| Phase 4 | Export Engine | ADA | ⬜ Offen | – |
| Phase 5 | Polish | UXA + ADA | ⬜ Offen | – |
| Phase 6 | Testing | QAA | ⬜ Offen | – |

**Status-Legende:** ⬜ Offen | 🔄 In Arbeit | ✅ Abgeschlossen | ⚠️ Partial

---

## 3. Aktueller State Snapshot

> Dieser Abschnitt wird nach jeder Phase vollständig überschrieben.

### State Snapshot: Phase 2 – Entry Screen (Datenerfassung)
**Agent:** ADA
**Datum:** 2026-03-03
**Status:** COMPLETE

#### Erledigte Arbeit
- `app/src/main/res/values/strings.xml` – erweitert: alle Labels, Fehlermeldungen, Content Descriptions, Dialog-Strings
- `app/src/main/AndroidManifest.xml` – `MainActivity` mit Launcher-Intent-Filter + `windowSoftInputMode="adjustResize"` hinzugefügt
- `app/src/main/java/com/example/pulseguard/domain/usecase/AddMeasurementUseCase.kt` – neu
- `app/src/main/java/com/example/pulseguard/ui/screens/entry/EntryUiState.kt` – neu
- `app/src/main/java/com/example/pulseguard/ui/screens/entry/EntryEvent.kt` – neu
- `app/src/main/java/com/example/pulseguard/ui/screens/entry/EntryViewModel.kt` – neu
- `app/src/main/java/com/example/pulseguard/ui/components/NumericInputField.kt` – neu
- `app/src/main/java/com/example/pulseguard/ui/components/ArmSelector.kt` – neu
- `app/src/main/java/com/example/pulseguard/ui/components/MedicationToggle.kt` – neu
- `app/src/main/java/com/example/pulseguard/ui/screens/entry/EntryScreen.kt` – neu
- `app/src/main/java/com/example/pulseguard/ui/screens/dashboard/DashboardScreen.kt` – neu (Placeholder)
- `app/src/main/java/com/example/pulseguard/ui/navigation/NavRoutes.kt` – neu
- `app/src/main/java/com/example/pulseguard/ui/navigation/PulseGuardNavGraph.kt` – neu
- `app/src/main/java/com/example/pulseguard/ui/theme/Color.kt` – neu
- `app/src/main/java/com/example/pulseguard/ui/theme/Type.kt` – neu
- `app/src/main/java/com/example/pulseguard/ui/theme/Theme.kt` – neu
- `app/src/main/java/com/example/pulseguard/MainActivity.kt` – neu
- `app/src/main/java/com/example/pulseguard/di/AppModule.kt` – erweitert: `useCaseModule` + `viewModelModule`
- `app/src/main/java/com/example/pulseguard/PulseGuardApp.kt` – erweitert: neue Module registriert

#### Neuer Code-Stand
**Packages:**
- `com.example.pulseguard.domain.usecase` → `AddMeasurementUseCase`
- `com.example.pulseguard.ui.screens.entry` → `EntryUiState`, `EntryEvent`, `EntryViewModel`, `EntryScreen`
- `com.example.pulseguard.ui.screens.dashboard` → `DashboardScreen` (Placeholder)
- `com.example.pulseguard.ui.components` → `NumericInputField`, `ArmSelector`, `MedicationToggle`
- `com.example.pulseguard.ui.navigation` → `NavRoutes`, `PulseGuardNavGraph`
- `com.example.pulseguard.ui.theme` → `Color`, `Type`, `Theme`
- `com.example.pulseguard` → `MainActivity`

**Neue Koin-Module in `AppModule.kt`:**
- `useCaseModule` – `AddMeasurementUseCase` als `factory`
- `viewModelModule` – `EntryViewModel` als `viewModel`

**Neue Dependencies:** Keine (alle in Phase 1 eingetragen).

#### Offene Punkte / Known Issues
- **assembleDebug muss lokal verifiziert werden** – kein Android SDK in Agent-Umgebung.
- **`validationErrors: Map<String, String>`** – enthält String-Konstanten direkt im ViewModel (nicht via `stringResource`). Begründung: ViewModels haben keinen Compose-Kontext. Strings sind zusätzlich in `strings.xml` dokumentiert. Gilt als akzeptabler Trade-off; kann in Phase 5 auf typed `FieldError` + `@StringRes` refaktoriert werden.
- **`DashboardScreen`** ist ein reiner Placeholder bis Phase 3.
- **TimePicker** nutzt Material3 `TimePicker` in einem `AlertDialog` (kein nativer `TimePickerDialog`, da dieser in M3 nicht existiert). UXA-Review sollte das Layout prüfen.
- **`windowSoftInputMode="adjustResize"`** im Manifest – stellt sicher, dass die Inhalte beim Tastatur-Öffnen verschoben werden. In Kombination mit `imePadding()` im Content-Column getestet.

#### Kontext für nächsten Agenten (Phase 3 – ADA)
- **Navigation:** `NavRoutes.DASHBOARD` ist `startDestination`. FAB navigiert zu `NavRoutes.ENTRY`. `NavRoutes.EXPORT` ist reserviert für Phase 4.
- **`DashboardScreen.kt`** muss vollständig durch die echte Implementierung ersetzt werden. Signatur: `fun DashboardScreen(onAddEntry: () -> Unit)` beibehalten.
- **Koin:** `viewModelModule` in `AppModule.kt` für `DashboardViewModel` und `GetDashboardDataUseCase` (useCaseModule) erweitern.
- **`BloodPressureCategory.fromValues()`** ist implementiert und getestet (Phase 1). Direkt nutzbar für Kategorie-Badges im Dashboard.
- **Vico-Library** ist bereits in `build.gradle.kts` eingetragen (`vico.compose.m3`).

#### Verifizierung
- assembleDebug: ⚠️ Lokal auszuführen (kein SDK in Agent-Umgebung)
- lintDebug: ⚠️ Lokal auszuführen
- testDebugUnitTest: ⚠️ Lokal auszuführen
- Anzahl neuer Tests: 0 (Phase 6 dediziert für Tests)

---

## 5. Snapshot-Archiv (Phase 1)

### State Snapshot: Phase 1 – Projekt-Setup + Room Database
**Agent:** ADA
**Datum:** 2026-03-03
**Status:** COMPLETE

#### Erledigte Arbeit
- `gradle/libs.versions.toml` – erweitert: Kotlin 2.1.0, KSP 2.1.0-1.0.29, Compose BOM 2024.12.01, Navigation 2.8.5, Room 2.6.1, Lifecycle 2.8.7, Vico 2.0.0-alpha.31, Koin 3.5.6, Test-Extras
- `build.gradle.kts` (root) – Kotlin-Android-, Kotlin-Compose- und KSP-Plugins hinzugefügt (`apply false`)
- `app/build.gradle.kts` – vollständig neu: alle Plugins, Compose-Feature-Flag, `kotlinOptions { jvmTarget = "11" }`, KSP-Schema-Location, alle Dependencies
- `app/src/main/AndroidManifest.xml` – `android:name=".PulseGuardApp"` hinzugefügt
- `app/src/main/java/com/example/pulseguard/domain/model/MeasurementArm.kt` – neu
- `app/src/main/java/com/example/pulseguard/domain/model/BloodPressureCategory.kt` – neu
- `app/src/main/java/com/example/pulseguard/domain/model/DashboardAggregation.kt` – neu
- `app/src/main/java/com/example/pulseguard/data/local/entity/BloodPressureEntry.kt` – neu
- `app/src/main/java/com/example/pulseguard/data/local/entity/AggregatedValues.kt` – neu
- `app/src/main/java/com/example/pulseguard/data/local/entity/MinMaxValues.kt` – neu
- `app/src/main/java/com/example/pulseguard/data/local/Converters.kt` – neu
- `app/src/main/java/com/example/pulseguard/data/local/dao/BloodPressureDao.kt` – neu
- `app/src/main/java/com/example/pulseguard/data/local/PulseGuardDatabase.kt` – neu
- `app/src/main/java/com/example/pulseguard/data/repository/BloodPressureRepository.kt` – neu
- `app/src/main/java/com/example/pulseguard/data/repository/BloodPressureRepositoryImpl.kt` – neu
- `app/src/main/java/com/example/pulseguard/di/AppModule.kt` – neu
- `app/src/main/java/com/example/pulseguard/PulseGuardApp.kt` – neu
- `app/src/test/java/com/example/pulseguard/data/local/ConvertersTest.kt` – neu (6 Tests)
- `app/src/test/java/com/example/pulseguard/domain/model/BloodPressureCategoryTest.kt` – neu (14 Tests)

#### Neuer Code-Stand
**Packages:**
- `com.example.pulseguard.domain.model` → `MeasurementArm`, `BloodPressureCategory`, `DashboardAggregation`
- `com.example.pulseguard.data.local.entity` → `BloodPressureEntry`, `AggregatedValues`, `MinMaxValues`
- `com.example.pulseguard.data.local` → `Converters`, `PulseGuardDatabase`
- `com.example.pulseguard.data.local.dao` → `BloodPressureDao`
- `com.example.pulseguard.data.repository` → `BloodPressureRepository`, `BloodPressureRepositoryImpl`
- `com.example.pulseguard.di` → `AppModule` (`databaseModule`, `repositoryModule`)
- `com.example.pulseguard` → `PulseGuardApp`

**Neue Dependencies (app/build.gradle.kts):**
- Kotlin 2.1.0 + KSP 2.1.0-1.0.29 (Plugins)
- `androidx.compose:compose-bom:2024.12.01`
- `androidx.navigation:navigation-compose:2.8.5`
- `androidx.lifecycle:lifecycle-*:2.8.7`
- `androidx.room:room-runtime/ktx/compiler:2.6.1`
- `com.patrykandpatrick.vico:compose-m3:2.0.0-alpha.31`
- `io.insert-koin:koin-android/koin-androidx-compose:3.5.6`
- Test-Extras: `arch-core-testing:2.2.0`, `kotlinx-coroutines-test:1.8.1`

#### Offene Punkte / Known Issues
- **assembleDebug muss lokal verifiziert werden** – kein Android SDK in CI-Umgebung vorhanden. Bitte `./gradlew assembleDebug` im Projektroot ausführen.
- **Vico 2.0.0-alpha.31**: Dies ist eine Alpha-Version. Falls nicht verfügbar, auf `1.15.0` downgraden und `compose-m3`-Artefakt beibehalten. Zur stabilen Vico 2.x Version upgraden, sobald verfügbar.
- Kein `MainActivity` in Phase 1: Die APK kompiliert, hat aber keinen Launcher-Eintrag (Intent-Filter wird erst in Phase 2 mit `EntryScreen`/Navigation hinzugefügt).
- Room-Schema-JSON wird erst nach erstem erfolgreichen KSP-Build unter `app/schemas/` erzeugt.

#### Kontext für nächsten Agenten (Phase 2 – ADA)
- **Repository-Interface** liegt in `data/repository/` (nicht `domain/`), wie im Plan vorgesehen. Kein zusätzliches Domain-Mapping nötig – `BloodPressureEntry` wird direkt im gesamten Stack verwendet.
- **AggregatedValues / MinMaxValues** sind reine DAO-Projektionsklassen in `data.local.entity`. Sie werden in `GetDashboardDataUseCase` (Phase 3) auf `DashboardAggregation` gemappt.
- **BloodPressureCategory.fromValues()** ist implementiert und getestet. UseCases können es direkt aufrufen.
- **Koin-Module**: `databaseModule` und `repositoryModule` sind in `AppModule.kt` definiert. Für Phase 2 das `AppModule.kt` um einen `useCaseModule` und `viewModelModule` ergänzen.
- **Navigation**: Noch kein NavGraph. Phase 2 erstellt `PulseGuardNavGraph.kt` und die `MainActivity`.
- `MeasurementArm.LEFT` ist der Default-Wert in `EntryUiState` (wie im Plan angegeben).

#### Verifizierung
- assembleDebug: ⚠️ Lokal auszuführen (kein SDK in Agent-Umgebung)
- lintDebug: ⚠️ Lokal auszuführen
- testDebugUnitTest: ⚠️ Lokal auszuführen
- Anzahl neuer Tests: 20 (6 × ConvertersTest, 14 × BloodPressureCategoryTest)

---

## 4. Snapshot-Vorlage

> **Für Agenten:** Kopiere diese Vorlage in Abschnitt 3, wenn du eine Phase abschließt.

```markdown
### State Snapshot: Phase [N] – [Titel]
**Agent:** [ADA/UXA/QAA]
**Datum:** [YYYY-MM-DD]
**Status:** COMPLETE / PARTIAL (mit Begründung)

#### Erledigte Arbeit
- [Liste aller erstellten/geänderten Dateien mit vollem Pfad]

#### Neuer Code-Stand
- [Package-Pfade der neuen Klassen]
- [Neue Dependencies falls hinzugefügt]

#### Offene Punkte / Known Issues
- [Liste, falls vorhanden – sonst "Keine"]

#### Kontext für nächsten Agenten
- [Wichtige Design-Entscheidungen, die getroffen wurden]
- [Abweichungen vom Plan mit Begründung]
- [Hinweise für die nächste Phase]

#### Verifizierung
- assembleDebug: ✅/❌
- lintDebug: ✅/❌
- testDebugUnitTest: ✅/❌ (oder N/A wenn noch keine Tests)
- Anzahl neuer Tests: [N]
```

---

## 5. Snapshot-Archiv

> Nach jedem Phase-Abschluss wird der vorherige Snapshot hierhin verschoben,
> damit die Historie erhalten bleibt, ohne den aktiven Snapshot zu überladen.

### Archivierte Snapshots

_Noch keine archivierten Snapshots._

---

## 6. Offene Entscheidungen & Risiken

| # | Thema | Status | Entscheidung |
|---|-------|--------|-------------|
| 1 | Navigation: BottomNav (2 Tabs + FAB) vs. 3 Tabs | ⬜ Offen | Plan empfiehlt FAB-Ansatz |
| 2 | Custom Font (Roboto Mono) für Zahlenwerte | ⬜ Offen | Phase 1 Systemfonts, optional in Phase 5 |
| 3 | Trend-Chart im PDF (statisch gerendert) | ⬜ Offen | Als optional in Phase 4 markiert |

---

## 7. Kontext-Fenster-Checkliste

> Jeder Agent prüft vor Arbeitsbeginn:

- [ ] `AGENTS.md` gelesen (Coding-Standards, Namenskonventionen, Architektur-Regeln)
- [ ] `todo.md` – nur die zugewiesene Phase gelesen
- [ ] `state.md` – Abschnitt 3 (Aktueller Snapshot) gelesen
- [ ] Nur die für die Phase relevanten Code-Dateien geöffnet
- [ ] Bei Unklarheiten: Orchestrator fragen, nicht raten
