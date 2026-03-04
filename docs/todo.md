/clea# Pulse Guard – Sprint-Backlog (todo.md)
### Kontext-isolierte Phasen für Worker-Agenten v1.0

---

> **Anleitung für Worker-Agenten:** Lies nur die Phase, die dir zugewiesen wurde.
> Jede Phase ist eigenständig und enthält alle nötigen Informationen.
> Für Architektur-Regeln und Namenskonventionen siehe `AGENTS.md`.

---

## Phase 1: Projekt-Setup + Room Database
**Agent:** ADA | **Abhängigkeiten:** Keine | **Status:** ✅ Abgeschlossen (2026-03-03)

### Deliverable
Kompilierendes Android-Projekt mit funktionsfähiger Room-Datenbank und DI-Setup.

### Aufgaben

- [x] **1.1** Projekt-Grundstruktur verifizieren: Package `com.example.pulseguard`, Min SDK 31, Target SDK 35, Kotlin DSL
- [x] **1.2** `build.gradle.kts` (app): Alle Core Dependencies eintragen (Compose BOM, Navigation, Room + KSP, Lifecycle, Vico Charting, Koin, Testing)
- [x] **1.3** Package-Struktur anlegen: `data/local/dao/`, `data/local/entity/`, `data/repository/`, `domain/model/`, `domain/usecase/`, `ui/navigation/`, `ui/theme/`, `ui/screens/dashboard/`, `ui/screens/entry/`, `ui/screens/export/`, `ui/components/`, `di/`
- [x] **1.4** `BloodPressureEntry.kt` Entity erstellen (Felder: id, systolic, diastolic, pulse, measurementArm, medicationTaken, timestamp, note)
- [x] **1.5** `MeasurementArm.kt` Enum erstellen (LEFT, RIGHT)
- [x] **1.6** `BloodPressureCategory.kt` Enum erstellen (OPTIMAL, NORMAL, HIGH_NORMAL, HYPERTENSION_1, HYPERTENSION_2, HYPERTENSION_3) mit WHO-Farbwerten
- [x] **1.7** `Converters.kt` TypeConverter für `MeasurementArm ↔ String` erstellen
- [x] **1.8** `BloodPressureDao.kt` Interface erstellen mit allen 7 DAO-Methoden (insert, getAll, getForDateRange, getAverage, getMinMax, delete, getCount)
- [x] **1.9** `PulseGuardDatabase.kt` erstellen (version=1, exportSchema=true), KSP Schema-Location konfigurieren
- [x] **1.10** `BloodPressureRepository.kt` Interface + `BloodPressureRepositoryImpl.kt` erstellen
- [x] **1.11** `DashboardAggregation.kt` Data Class im Domain-Layer erstellen
- [x] **1.12** `AppModule.kt` Koin-Module definieren (Database, DAO, Repository)
- [x] **1.13** `PulseGuardApp.kt` Application-Klasse mit Koin-Initialisierung erstellen
- [x] **1.14** `./gradlew assembleDebug` erfolgreich durchlaufen lassen
- [x] **1.15** State Snapshot in `state.md` schreiben

### Kontext-Hinweise
- Room `exportSchema = true` → KSP arg in build.gradle.kts setzen: `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`
- Noch keine UseCases, ViewModels oder UI nötig – nur Data + Domain Models + DI

---

## Phase 2: Entry Screen (Datenerfassung)
**Agent:** ADA | **Review:** UXA | **Abhängigkeiten:** Phase 1 ✅ | **Status:** ✅ Abgeschlossen (2026-03-04)

### Deliverable
Funktionierender Eingabe-Screen mit Validierung, Number-Pad und Navigation.

### Aufgaben

- [x] **2.1** `AddMeasurementUseCase.kt` erstellen (validiert Eingaben, delegiert an Repository)
- [x] **2.2** `EntryUiState.kt` Data Class erstellen (systolic, diastolic, pulse, measurementArm, medicationTaken, timestamp, validationErrors, isSaving, saveSuccess)
- [x] **2.3** `EntryEvent.kt` Sealed Interface erstellen (SystolicChanged, DiastolicChanged, PulseChanged, ArmChanged, MedicationToggled, TimestampChanged, SaveClicked)
- [x] **2.4** `EntryViewModel.kt` erstellen (StateFlow, Event-Handling, Validierungslogik, Save-Flow)
- [x] **2.5** Validierungsregeln implementieren: Systolisch 60–300, Diastolisch 30–200, Puls 30–250, Systolisch > Diastolisch
- [x] **2.6** `NumericInputField.kt` Composable erstellen (OutlinedTextField mit KeyboardType.Number, Error-State)
- [x] **2.7** `ArmSelector.kt` Composable erstellen (SingleChoiceSegmentedButtonRow: Links/Rechts)
- [x] **2.8** `MedicationToggle.kt` Composable erstellen (Switch + Label)
- [x] **2.9** `EntryScreen.kt` Composable erstellen (Zeitstempel, 3 Eingabefelder, Arm-Selektor, Medikamenten-Toggle, Speichern-Button)
- [x] **2.10** FocusRequester-Kette implementieren: Systolisch → Diastolisch → Puls via ImeAction.Next
- [x] **2.11** Save-Feedback implementieren: Haptic Feedback + Snackbar + Navigation zurück
- [x] **2.12** Koin-Module erweitern (UseCase, ViewModel)
- [x] **2.13** Basis-Navigation aufsetzen: `PulseGuardNavGraph.kt` mit Dashboard + Entry Routen
- [x] **2.14** String-Ressourcen in `strings.xml` auslagern (Labels, Fehlermeldungen)
- [x] **2.15** `./gradlew assembleDebug` + `lintDebug` erfolgreich
- [x] **2.16** State Snapshot in `state.md` schreiben
- [x] **2.17** UXA-Review anfordern (Fokus: Number-Pad-Sichtbarkeit, Touch-Targets ≥48dp, Accessibility)

### Kontext-Hinweise
- Number Pad muss während der gesamten Eingabe sichtbar bleiben (kein KeyboardType-Wechsel)
- Keine modale Dialoge für Haupteingabe – Fullscreen bevorzugt
- contentDescription auf allen interaktiven Elementen setzen

---

## Phase 3: Dashboard Screen
**Agent:** ADA + Agent 7 (Review) | **Review:** UXA | **Abhängigkeiten:** Phase 1 ✅ + Phase 2 ✅ | **Status:** ✅ Abgeschlossen (2026-03-04)

### Deliverable
Dashboard mit Periodenfilter, Aggregation, Trend-Chart und Messungsliste.

### Aufgaben

- [x] **3.1** `GetDashboardDataUseCase.kt` erstellen (Aggregation + Chart-Daten für gewählten Zeitraum)
- [x] **3.2** `DashboardPeriod.kt` Enum erstellen (DAY, WEEK, MONTH) mit Zeitraum-Berechnung
- [x] **3.3** `ChartDataPoint.kt` Data Class erstellen (für Vico-C
- hart-Mapping)
- [x] **3.4** `DashboardUiState.kt` Data Class erstellen (selectedPeriod, aggregation, recentEntries, chartData, isLoading)
- [x] **3.5** `DashboardEvent.kt` Sealed Interface erstellen (PeriodChanged, EntryDeleted)
- [x] **3.6** `DashboardViewModel.kt` erstellen (StateFlow, Perioden-Wechsel via flatMapLatest, Flow-Collection)
- [x] **3.7** `BloodPressureCard.kt` Composable erstellen (kompakte Darstellung eines Eintrags mit Farbkodierung, IntrinsicSize.Min Fix)
- [x] **3.8** `PressureChart.kt` Composable erstellen (Vico 1.15.0 LineChart: systolisch + diastolisch als 2 Linien, remember-optimiert)
- [x] **3.9** `DashboardScreen.kt` Composable erstellen (Period-Selector, Summary-Card, Chart, LazyColumn mit Keys)
- [x] **3.10** WHO-Farbkodierung in Summary-Card implementieren (Kategorie-Badge)
- [x] **3.11** FAB auf Dashboard für neuen Eintrag einbinden (Navigation zu Entry)
- [x] **3.12** Koin-Module erweitern (UseCase, ViewModel)
- [x] **3.13** `./gradlew assembleDebug` + `lintDebug` erfolgreich
- [x] **3.14** State Snapshot in `state.md` schreiben
- [x] **3.15** Agent-7-Review + alle 5 kritischen Findings behoben (UTC-Midnight, Stale-TimeRange, Vico-Performance, IntrinsicSize, LazyColumn-Keys)

### Kontext-Hinweise
- Dashboard ist `startDestination` im NavGraph
- Empty State mit Illustration für leeres Dashboard vorsehen
- Vico Library für Charts nutzen (`com.patrykandpatrick.vico:compose-m3`)

---

## Phase 4: Export Engine (PDF + Share)
**Agent:** ADA | **Abhängigkeiten:** Phase 1 ✅ | **Status:** ✅ Abgeschlossen (2026-03-04)

### Deliverable
PDF-Generierung mit Deckblatt, Messwerttabelle und Share-Funktion.

### Aufgaben

- [x] **4.1** `ExportToPdfUseCase.kt` erstellen (Daten laden, PDF generieren, URI zurückgeben)
- [x] **4.2** PDF-Deckblatt rendern: App-Titel, Zeitraum, Zusammenfassungswerte (Ø, Min/Max, Anzahl, Kategorie)
- [x] **4.3** PDF-Messwerttabelle rendern: Spalten Datum/Uhrzeit, Systolisch, Diastolisch, Puls, Arm, Medikament; Seitenumbruch nach ~25 Zeilen
- [x] **4.4** Farbige Zeilenmarkierung gemäß WHO-Kategorie in Tabelle
- [x] **4.5** `file_provider_paths.xml` erstellen: `<cache-path name="pdfs" path="exports/" />`
- [x] **4.6** `AndroidManifest.xml` FileProvider-Eintrag hinzufügen
- [x] **4.7** `ExportUiState.kt` Data Class erstellen (dateRangeStart, dateRangeEnd, previewEntryCount, isGenerating, generatedPdfUri)
- [x] **4.8** `ExportEvent.kt` Sealed Interface erstellen (DateRangeSelected, GenerateClicked, ShareClicked)
- [x] **4.9** `ExportViewModel.kt` erstellen (StateFlow, PDF-Generierung, Share-Intent-Vorbereitung)
- [x] **4.10** `ExportScreen.kt` Composable erstellen (DateRangePicker, Vorschau-Info, Generate-Button, Share-Button)
- [x] **4.11** Share-Mechanismus: Intent.ACTION_SEND mit type "application/pdf" via FileProvider-URI
- [x] **4.12** Koin-Module erweitern (UseCase, ViewModel)
- [x] **4.13** Navigation erweitern: Export-Route in NavGraph hinzufügen, BottomNav/TopBar-Action
- [x] **4.14** `./gradlew assembleDebug` + `lintDebug` erfolgreich
- [x] **4.15** State Snapshot in `state.md` schreiben

### Kontext-Hinweise
- Nativ über `android.graphics.pdf.PdfDocument` – keine externe Library
- PDF wird in `context.cacheDir/exports/` gespeichert
- Kein `WRITE_EXTERNAL_STORAGE` nötig, alles über FileProvider + App-Cache

---

## Phase 5: Polish (Theming, Animationen, Edge Cases)
**Agent:** UXA + ADA | **Abhängigkeiten:** Phase 2 ✅ + Phase 3 ✅ + Phase 4 ✅ | **Status:** ✅ Abgeschlossen (2026-03-04)

### Deliverable
Visuell polierte App mit Theme, Animationen, Empty States und Edge-Case-Handling.

### Aufgaben

- [x] **5.1** `Theme.kt` finalisieren: Material3 Dynamic Colors (Material You) mit Fallback-Palette (PulseRed/MedTeal/WarmAmber)
- [x] **5.2** `Color.kt` finalisieren: WHO-Farbskala + vollständige PulseGuard-Palette
- [x] **5.3** `Type.kt` finalisieren: headlineSmall mit FontFamily.Monospace für numerische Werte
- [x] **5.4** Empty State für Dashboard implementiert: FavoriteBorder-Icon + Titel + Body + CTA
- [x] **5.5** Animationen: FAB Bounce (spring, DampingRatioMediumBouncy) + Staggered Entrance für BloodPressureCard-Liste (fadeIn + slideInVertically, 55 ms/Item)
- [x] **5.6** Haptic Feedback nach erfolgreichem Speichern verifiziert (LongPress in EntryScreen)
- [x] **5.7** contentDescription auf allen interaktiven Elementen verifiziert und ergänzt
- [x] **5.8** Touch-Targets ≥ 48dp verifiziert (Material3 Defaults + BloodPressureCard heightIn(72dp))
- [x] **5.9** Edge Case: Leere Datenbank → EmptyState im Dashboard korrekt
- [x] **5.10** Edge Case: Grenzwerte in EntryViewModel.validate() korrekt (60–300 / 30–200 / 30–250)
- [x] **5.11** Edge Case: Systolisch ≤ Diastolisch → error_systolic_greater_diastolic implementiert
- [x] **5.12** Splash Screen API (ab API 31) konfiguriert: core-splashscreen 1.0.1, Theme.SplashScreen, installSplashScreen()
- [x] **5.13** App-Icon (Adaptive Icon) einbinden – Custom Background + Foreground XML in Phase 7 erstellt
- [x] **5.14** `./gradlew assembleDebug` erfolgreich ✅
- [x] **5.15** State Snapshot in `state.md` geschrieben

### Kontext-Hinweise
- Farben dürfen nie das einzige Unterscheidungsmerkmal sein (Accessibility)
- Systemfonts für Phase 1 ausreichend, Custom-Font optional
- UXA-Review fokussiert auf Barrierefreiheit und visuelle Konsistenz

---

## Phase 6: Testing
**Agent:** QAA | **Review:** ADA | **Abhängigkeiten:** Alle Phasen ✅ | **Status:** ✅ Abgeschlossen (2026-03-04)

### Deliverable
Vollständige Test-Suite mit Unit Tests, Integration Tests und UI Tests.

### Aufgaben

- [x] **6.1** Unit Tests: `AddMeasurementUseCase` (gültige Eingabe, ungültige Eingabe, Grenzwerte, systolisch ≤ diastolisch) — 18 Tests
- [x] **6.2** Unit Tests: `GetDashboardDataUseCase` (leere DB, ein Eintrag, mehrere Einträge, Periodenfilter) — 12 Tests
- [x] **6.3** Unit Tests: `ExportToPdfUseCase` (Erfolg, Fehler, Exception-Preservation) — 3 Tests
- [x] **6.4** Unit Tests: `EntryViewModel` (Event-Handling, Validierung, State-Updates) — 27 Tests
- [x] **6.5** Unit Tests: `DashboardViewModel` (Periodenauswahl, State-Updates) — 8 Tests
- [x] **6.6** Unit Tests: `ExportViewModel` (Datumsauswahl, Generierung, URI-Clearing) — 17 Tests
- [x] **6.7** Unit Tests: `Converters` (MeasurementArm TypeConverter round-trip) — bestehend ✅
- [x] **6.8** Unit Tests: `BloodPressureCategory`-Zuordnung (alle Grenzwerte der WHO-Skala) — bestehend ✅
- [x] **6.9** Integration Tests: `BloodPressureDao` alle 7 Methoden mit In-Memory-DB — 29 Tests (androidTest)
- [x] **6.10** UI Tests: EntryScreen (Eingabe, Validierungsfehler, Arm-Selektor) — 7 Tests (androidTest)
- [x] **6.11** UI Tests: DashboardScreen (Periodenauswahl, Kartendarstellung, Empty State) — 8 Tests (androidTest)
- [x] **6.12** Namensformat verifiziert: `methodName_condition_expectedResult` ✅
- [x] **6.13** `./gradlew testDebugUnitTest` — 115 Tests, 0 Fehler ✅
- [ ] **6.14** `./gradlew connectedDebugAndroidTest` alle Instrumented Tests grün (falls Emulator verfügbar)
- [x] **6.15** State Snapshot in `state.md` schreiben
- [x] **6.16** Agent-7-Review: alle 5 Findings behoben — `midnightTicker: Flow<Unit>`-Injection (eliminiert `viewModelScope.cancel()`-Workaround + Hang-Risiko), False-Green-Assertion gefixt, `FakeBloodPressureRepository.reset()` vervollständigt, `advanceUntilIdle()`-No-ops entfernt, `entryNow()` auf `fixedNow` umgestellt

### Kontext-Hinweise
- JUnit 4 für Unit Tests, Room Testing für DAO-Tests, Compose UI Test für UI-Tests
- Jeder Test hat genau einen Assert-Fokus
- Keine Sleeps oder feste Delays in Tests
- Optional: Turbine für StateFlow-Testing

---

## Phase 7: Production Hardening
**Agent:** ADA | **Abhängigkeiten:** Alle Phasen ✅ | **Status:** ✅ Abgeschlossen (2026-03-04)

### Deliverable
Vollständige Delete+Undo-Funktion, explizites IO-Dispatching, Custom Adaptive Icon.

### Aufgaben

- [x] **7.1** `DeleteMeasurementUseCase.kt` erstellen – `execute(id: Long)` delegiert an `repository.deleteEntry(id)`; idempotent
- [x] **7.2** `DashboardEvent` erweitern – `EntryDeleted(entry: BloodPressureEntry)` (volle Entity statt ID für Undo), `UndoDelete`, `SnackbarDismissed`
- [x] **7.3** `DashboardUiState` erweitern – `pendingDeleteEntry: BloodPressureEntry?` als Snackbar-Trigger
- [x] **7.4** `DashboardViewModel` implementieren – `_pendingDeleteEntry: MutableStateFlow`, `combine()` in `uiState`, `handleDelete()`, `handleUndoDelete()` (re-insert mit `id = 0`)
- [x] **7.5** `DashboardScreen` implementieren – `SwipeToDeleteCard` (EndToStart only), `LaunchedEffect(pendingDeleteEntry)` Snackbar mit Undo-Action
- [x] **7.6** String-Ressourcen ergänzen – `snackbar_entry_deleted`, `snackbar_action_undo`
- [x] **7.7** `ExportToPdfUseCase` – `ioDispatcher: CoroutineDispatcher = Dispatchers.IO` als injizierbaren Parameter; `execute()` via `withContext(ioDispatcher)`
- [x] **7.8** Koin `AppModule.kt` – `DeleteMeasurementUseCase` in `useCaseModule`; `DashboardViewModel` mit 3 Parametern
- [x] **7.9** Custom Adaptive Icon vorbereiten – `ic_launcher_background.xml` (Deep-Red + Highlight), `ic_launcher_foreground.xml` (Herz + EKG-Linie, innerhalb Safe Zone)
- [x] **7.10** `DashboardViewModelTest` erweitern – 5 neue Tests für `EntryDeleted`, `UndoDelete`, `SnackbarDismissed`; Gesamt: 121 Unit Tests grün
- [x] **7.11** State Snapshot in `state.md` schreiben

### Kontext-Hinweise
- Swipe-to-Dismiss nur EndToStart (kein versehentliches Auslösen beim Scrollen)
- `LaunchedEffect(pendingDeleteEntry)` als Key stellt sicher, dass bei mehrfachem Löschen jedes Mal eine neue Snackbar erscheint
- `id = 0` beim Undo-Reinsert verhindert `UNIQUE`-Constraint-Verletzungen in Room
- Kein `!!`-Operator, kein `mutableStateOf` im ViewModel (AGENTS.md-Konformität)

---

## Zusammenfassung: Abhängigkeitsgraph

```
Phase 1  ──┬──→ Phase 2 ──┬──→ Phase 3 ──┐
           │              │              │
           └──→ Phase 4 ──┼──────────────┤
                          │              │
                          └──→ Phase 5 ──┤
                                         │
                                   Phase 6
```

| Phase | Parallelisierbar mit |
|-------|---------------------|
| Phase 1 | – 
| Phase 2 | Phase 4 (nach Phase 1) |
| Phase 3 | Phase 4 (nach Phase 1) |
| Phase 4 | Phase 2, Phase 3 (nach Phase 1) |
| Phase 5 | – |
| Phase 6 | – |
