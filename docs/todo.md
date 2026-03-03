# Pulse Guard – Sprint-Backlog (todo.md)
### Kontext-isolierte Phasen für Worker-Agenten v1.0

---

> **Anleitung für Worker-Agenten:** Lies nur die Phase, die dir zugewiesen wurde.
> Jede Phase ist eigenständig und enthält alle nötigen Informationen.
> Für Architektur-Regeln und Namenskonventionen siehe `AGENTS.md`.

---

## Phase 1: Projekt-Setup + Room Database
**Agent:** ADA | **Abhängigkeiten:** Keine | **Status:** ⬜ Offen

### Deliverable
Kompilierendes Android-Projekt mit funktionsfähiger Room-Datenbank und DI-Setup.

### Aufgaben

- [ ] **1.1** Projekt-Grundstruktur verifizieren: Package `com.example.pulseguard`, Min SDK 31, Target SDK 35, Kotlin DSL
- [ ] **1.2** `build.gradle.kts` (app): Alle Core Dependencies eintragen (Compose BOM, Navigation, Room + KSP, Lifecycle, Vico Charting, Koin, Testing)
- [ ] **1.3** Package-Struktur anlegen: `data/local/dao/`, `data/local/entity/`, `data/repository/`, `domain/model/`, `domain/usecase/`, `ui/navigation/`, `ui/theme/`, `ui/screens/dashboard/`, `ui/screens/entry/`, `ui/screens/export/`, `ui/components/`, `di/`
- [ ] **1.4** `BloodPressureEntry.kt` Entity erstellen (Felder: id, systolic, diastolic, pulse, measurementArm, medicationTaken, timestamp, note)
- [ ] **1.5** `MeasurementArm.kt` Enum erstellen (LEFT, RIGHT)
- [ ] **1.6** `BloodPressureCategory.kt` Enum erstellen (OPTIMAL, NORMAL, HIGH_NORMAL, HYPERTENSION_1, HYPERTENSION_2, HYPERTENSION_3) mit WHO-Farbwerten
- [ ] **1.7** `Converters.kt` TypeConverter für `MeasurementArm ↔ String` erstellen
- [ ] **1.8** `BloodPressureDao.kt` Interface erstellen mit allen 7 DAO-Methoden (insert, getAll, getForDateRange, getAverage, getMinMax, delete, getCount)
- [ ] **1.9** `PulseGuardDatabase.kt` erstellen (version=1, exportSchema=true), KSP Schema-Location konfigurieren
- [ ] **1.10** `BloodPressureRepository.kt` Interface + `BloodPressureRepositoryImpl.kt` erstellen
- [ ] **1.11** `DashboardAggregation.kt` Data Class im Domain-Layer erstellen
- [ ] **1.12** `AppModule.kt` Koin-Module definieren (Database, DAO, Repository)
- [ ] **1.13** `PulseGuardApp.kt` Application-Klasse mit Koin-Initialisierung erstellen
- [ ] **1.14** `./gradlew assembleDebug` erfolgreich durchlaufen lassen
- [ ] **1.15** State Snapshot in `state.md` schreiben

### Kontext-Hinweise
- Room `exportSchema = true` → KSP arg in build.gradle.kts setzen: `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`
- Noch keine UseCases, ViewModels oder UI nötig – nur Data + Domain Models + DI

---

## Phase 2: Entry Screen (Datenerfassung)
**Agent:** ADA | **Review:** UXA | **Abhängigkeiten:** Phase 1 ✅ | **Status:** ⬜ Offen

### Deliverable
Funktionierender Eingabe-Screen mit Validierung, Number-Pad und Navigation.

### Aufgaben

- [ ] **2.1** `AddMeasurementUseCase.kt` erstellen (validiert Eingaben, delegiert an Repository)
- [ ] **2.2** `EntryUiState.kt` Data Class erstellen (systolic, diastolic, pulse, measurementArm, medicationTaken, timestamp, validationErrors, isSaving, saveSuccess)
- [ ] **2.3** `EntryEvent.kt` Sealed Interface erstellen (SystolicChanged, DiastolicChanged, PulseChanged, ArmChanged, MedicationToggled, TimestampChanged, SaveClicked)
- [ ] **2.4** `EntryViewModel.kt` erstellen (StateFlow, Event-Handling, Validierungslogik, Save-Flow)
- [ ] **2.5** Validierungsregeln implementieren: Systolisch 60–300, Diastolisch 30–200, Puls 30–250, Systolisch > Diastolisch
- [ ] **2.6** `NumericInputField.kt` Composable erstellen (OutlinedTextField mit KeyboardType.Number, Error-State)
- [ ] **2.7** `ArmSelector.kt` Composable erstellen (SingleChoiceSegmentedButtonRow: Links/Rechts)
- [ ] **2.8** `MedicationToggle.kt` Composable erstellen (Switch + Label)
- [ ] **2.9** `EntryScreen.kt` Composable erstellen (Zeitstempel, 3 Eingabefelder, Arm-Selektor, Medikamenten-Toggle, Speichern-Button)
- [ ] **2.10** FocusRequester-Kette implementieren: Systolisch → Diastolisch → Puls via ImeAction.Next
- [ ] **2.11** Save-Feedback implementieren: Haptic Feedback + Snackbar + Navigation zurück
- [ ] **2.12** Koin-Module erweitern (UseCase, ViewModel)
- [ ] **2.13** Basis-Navigation aufsetzen: `PulseGuardNavGraph.kt` mit Dashboard + Entry Routen
- [ ] **2.14** String-Ressourcen in `strings.xml` auslagern (Labels, Fehlermeldungen)
- [ ] **2.15** `./gradlew assembleDebug` + `lintDebug` erfolgreich
- [ ] **2.16** State Snapshot in `state.md` schreiben
- [ ] **2.17** UXA-Review anfordern (Fokus: Number-Pad-Sichtbarkeit, Touch-Targets ≥48dp, Accessibility)

### Kontext-Hinweise
- Number Pad muss während der gesamten Eingabe sichtbar bleiben (kein KeyboardType-Wechsel)
- Keine modale Dialoge für Haupteingabe – Fullscreen bevorzugt
- contentDescription auf allen interaktiven Elementen setzen

---

## Phase 3: Dashboard Screen
**Agent:** ADA | **Review:** UXA | **Abhängigkeiten:** Phase 1 ✅ + Phase 2 ✅ | **Status:** ⬜ Offen

### Deliverable
Dashboard mit Periodenfilter, Aggregation, Trend-Chart und Messungsliste.

### Aufgaben

- [ ] **3.1** `GetDashboardDataUseCase.kt` erstellen (Aggregation + Chart-Daten für gewählten Zeitraum)
- [ ] **3.2** `DashboardPeriod.kt` Enum erstellen (DAY, WEEK, MONTH) mit Zeitraum-Berechnung
- [ ] **3.3** `ChartDataPoint.kt` Data Class erstellen (für Vico-Chart-Mapping)
- [ ] **3.4** `DashboardUiState.kt` Data Class erstellen (selectedPeriod, aggregation, recentEntries, chartData, isLoading)
- [ ] **3.5** `DashboardEvent.kt` Sealed Interface erstellen (PeriodChanged, EntryClicked)
- [ ] **3.6** `DashboardViewModel.kt` erstellen (StateFlow, Perioden-Wechsel, Flow-Collection)
- [ ] **3.7** `BloodPressureCard.kt` Composable erstellen (kompakte Darstellung eines Eintrags mit Farbkodierung)
- [ ] **3.8** `PressureChart.kt` Composable erstellen (Vico LineChart: systolisch + diastolisch als 2 Linien)
- [ ] **3.9** `DashboardScreen.kt` Composable erstellen (Period-Selector, Summary-Card, Chart, LazyColumn mit letzten Einträgen)
- [ ] **3.10** WHO-Farbkodierung in Summary-Card implementieren (Kategorie-Badge)
- [ ] **3.11** FAB auf Dashboard für neuen Eintrag einbinden (Navigation zu Entry)
- [ ] **3.12** Koin-Module erweitern (UseCase, ViewModel)
- [ ] **3.13** `./gradlew assembleDebug` + `lintDebug` erfolgreich
- [ ] **3.14** State Snapshot in `state.md` schreiben
- [ ] **3.15** UXA-Review anfordern (Fokus: Empty State, Chart-Lesbarkeit, Farbkodierung nicht als einziges Unterscheidungsmerkmal)

### Kontext-Hinweise
- Dashboard ist `startDestination` im NavGraph
- Empty State mit Illustration für leeres Dashboard vorsehen
- Vico Library für Charts nutzen (`com.patrykandpatrick.vico:compose-m3`)

---

## Phase 4: Export Engine (PDF + Share)
**Agent:** ADA | **Abhängigkeiten:** Phase 1 ✅ | **Status:** ⬜ Offen

### Deliverable
PDF-Generierung mit Deckblatt, Messwerttabelle und Share-Funktion.

### Aufgaben

- [ ] **4.1** `ExportToPdfUseCase.kt` erstellen (Daten laden, PDF generieren, URI zurückgeben)
- [ ] **4.2** PDF-Deckblatt rendern: App-Titel, Zeitraum, Zusammenfassungswerte (Ø, Min/Max, Anzahl, Kategorie)
- [ ] **4.3** PDF-Messwerttabelle rendern: Spalten Datum/Uhrzeit, Systolisch, Diastolisch, Puls, Arm, Medikament; Seitenumbruch nach ~25 Zeilen
- [ ] **4.4** Farbige Zeilenmarkierung gemäß WHO-Kategorie in Tabelle
- [ ] **4.5** `file_provider_paths.xml` erstellen: `<cache-path name="pdfs" path="exports/" />`
- [ ] **4.6** `AndroidManifest.xml` FileProvider-Eintrag hinzufügen
- [ ] **4.7** `ExportUiState.kt` Data Class erstellen (dateRangeStart, dateRangeEnd, previewEntryCount, isGenerating, generatedPdfUri)
- [ ] **4.8** `ExportEvent.kt` Sealed Interface erstellen (DateRangeSelected, GenerateClicked, ShareClicked)
- [ ] **4.9** `ExportViewModel.kt` erstellen (StateFlow, PDF-Generierung, Share-Intent-Vorbereitung)
- [ ] **4.10** `ExportScreen.kt` Composable erstellen (DateRangePicker, Vorschau-Info, Generate-Button, Share-Button)
- [ ] **4.11** Share-Mechanismus: Intent.ACTION_SEND mit type "application/pdf" via FileProvider-URI
- [ ] **4.12** Koin-Module erweitern (UseCase, ViewModel)
- [ ] **4.13** Navigation erweitern: Export-Route in NavGraph hinzufügen, BottomNav/TopBar-Action
- [ ] **4.14** `./gradlew assembleDebug` + `lintDebug` erfolgreich
- [ ] **4.15** State Snapshot in `state.md` schreiben

### Kontext-Hinweise
- Nativ über `android.graphics.pdf.PdfDocument` – keine externe Library
- PDF wird in `context.cacheDir/exports/` gespeichert
- Kein `WRITE_EXTERNAL_STORAGE` nötig, alles über FileProvider + App-Cache

---

## Phase 5: Polish (Theming, Animationen, Edge Cases)
**Agent:** UXA + ADA | **Abhängigkeiten:** Phase 2 ✅ + Phase 3 ✅ + Phase 4 ✅ | **Status:** ⬜ Offen

### Deliverable
Visuell polierte App mit Theme, Animationen, Empty States und Edge-Case-Handling.

### Aufgaben

- [ ] **5.1** `Theme.kt` finalisieren: Material3 Dynamic Colors (Material You) mit Fallback-Palette
- [ ] **5.2** `Color.kt` finalisieren: WHO-Farbskala als Theme-Colors definieren
- [ ] **5.3** `Type.kt` finalisieren: Typographie-Skala (System-Fonts, optional Roboto Mono für Zahlenwerte)
- [ ] **5.4** Empty State für Dashboard implementieren: Illustration + hilfreicher Text + CTA zum ersten Eintrag
- [ ] **5.5** Animationen: Einblende-Animationen für Karten, Übergangsanimationen zwischen Screens
- [ ] **5.6** Haptic Feedback nach erfolgreichem Speichern verifizieren
- [ ] **5.7** contentDescription auf allen interaktiven Elementen überprüfen und ergänzen
- [ ] **5.8** Touch-Targets ≥ 48dp auf allen interaktiven Elementen überprüfen
- [ ] **5.9** Edge Case: Leere Datenbank → alle Screens korrekt
- [ ] **5.10** Edge Case: Grenzwerte (systolisch = 60/300, diastolisch = 30/200, puls = 30/250)
- [ ] **5.11** Edge Case: Systolisch ≤ Diastolisch → Validierungsfehler
- [ ] **5.12** Splash Screen API (ab API 31) konfigurieren
- [ ] **5.13** App-Icon (Adaptive Icon) einbinden (Platzhalter, falls Asset noch nicht vorhanden)
- [ ] **5.14** `./gradlew assembleDebug` + `lintDebug` erfolgreich
- [ ] **5.15** State Snapshot in `state.md` schreiben

### Kontext-Hinweise
- Farben dürfen nie das einzige Unterscheidungsmerkmal sein (Accessibility)
- Systemfonts für Phase 1 ausreichend, Custom-Font optional
- UXA-Review fokussiert auf Barrierefreiheit und visuelle Konsistenz

---

## Phase 6: Testing
**Agent:** QAA | **Review:** ADA | **Abhängigkeiten:** Alle Phasen ✅ | **Status:** ⬜ Offen

### Deliverable
Vollständige Test-Suite mit Unit Tests, Integration Tests und UI Tests.

### Aufgaben

- [ ] **6.1** Unit Tests: `AddMeasurementUseCase` (gültige Eingabe, ungültige Eingabe, Grenzwerte, systolisch ≤ diastolisch)
- [ ] **6.2** Unit Tests: `GetDashboardDataUseCase` (leere DB, ein Eintrag, mehrere Einträge, Periodenfilter)
- [ ] **6.3** Unit Tests: `ExportToPdfUseCase` (leerer Zeitraum, gefüllter Zeitraum)
- [ ] **6.4** Unit Tests: `EntryViewModel` (Event-Handling, Validierung, State-Updates)
- [ ] **6.5** Unit Tests: `DashboardViewModel` (Periodenauswahl, State-Updates)
- [ ] **6.6** Unit Tests: `ExportViewModel` (Datumsauswahl, Generierung)
- [ ] **6.7** Unit Tests: `Converters` (MeasurementArm TypeConverter round-trip)
- [ ] **6.8** Unit Tests: `BloodPressureCategory`-Zuordnung (alle Grenzwerte der WHO-Skala)
- [ ] **6.9** Integration Tests: `BloodPressureDao` alle 7 Methoden mit In-Memory-DB
- [ ] **6.10** UI Tests: EntryScreen (Eingabe, Validierungsfehler, Speichern-Flow)
- [ ] **6.11** UI Tests: DashboardScreen (Periodenauswahl, Kartendarstellung)
- [ ] **6.12** Namensformat verifizieren: `methodName_condition_expectedResult`
- [ ] **6.13** `./gradlew testDebugUnitTest` alle Tests grün
- [ ] **6.14** `./gradlew connectedDebugAndroidTest` alle Instrumented Tests grün (falls Emulator verfügbar)
- [ ] **6.15** State Snapshot in `state.md` schreiben

### Kontext-Hinweise
- JUnit 4 für Unit Tests, Room Testing für DAO-Tests, Compose UI Test für UI-Tests
- Jeder Test hat genau einen Assert-Fokus
- Keine Sleeps oder feste Delays in Tests
- Optional: Turbine für StateFlow-Testing

---

## Zusammenfassung: Abhängigkeitsgraph

```
Phase 1 ──┬──→ Phase 2 ──┬──→ Phase 3 ──┐
           │              │              │
           └──→ Phase 4 ──┼──────────────┤
                          │              │
                          └──→ Phase 5 ──┤
                                         │
                                   Phase 6
```

| Phase | Parallelisierbar mit |
|-------|---------------------|
| Phase 1 | – |
| Phase 2 | Phase 4 (nach Phase 1) |
| Phase 3 | Phase 4 (nach Phase 1) |
| Phase 4 | Phase 2, Phase 3 (nach Phase 1) |
| Phase 5 | – |
| Phase 6 | – |
