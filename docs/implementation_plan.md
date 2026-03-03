# Pulse Guard – Implementation Plan
### Technischer Blueprint v1.0

---

## 1. Android Studio Setup Vorgaben

| Parameter | Wert |
|---|---|
| **Project Name** | Pulse Guard |
| **Package Name** | `com.example.pulseguard` |
| **Language** | Kotlin |
| **Minimum SDK** | API 31 (Android 12) |
| **Target SDK** | API 35 (Android 15) |
| **Compile SDK** | 35 |
| **Build Configuration** | Kotlin DSL (`build.gradle.kts`) |
| **Template** | Empty Compose Activity |

**Begründung Minimum SDK 31:** Das Samsung Galaxy S25 läuft ab Werk auf Android 15 (API 35). Ein Minimum von API 31 (Android 12) stellt Kompatibilität mit ca. 85%+ aller aktiven Android-Geräte sicher und erlaubt die Nutzung moderner APIs (z.B. SplashScreen API, Bluetooth LE Permissions, Material You Dynamic Colors) ohne Fallbacks. Wer die App ausschließlich auf dem S25 nutzen möchte, kann auf API 35 anheben – das reduziert jedoch die Zielgruppe drastisch.

### Core Dependencies (Versionen zum Projektzeitpunkt aktuell halten)

```
// build.gradle.kts (app)
dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.xx.xx"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.x")

    // Room
    implementation("androidx.room:room-runtime:2.6.x")
    implementation("androidx.room:room-ktx:2.6.x")
    ksp("androidx.room:room-compiler:2.6.x")

    // Lifecycle + StateFlow
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.x")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.x")

    // PDF Generation
    // Nativ via android.graphics.pdf.PdfDocument (kein externer Dep nötig)

    // Charting (Dashboard)
    implementation("com.patrykandpatrick.vico:compose-m3:2.x.x")

    // DI
    implementation("io.insert-koin:koin-androidx-compose:3.5.x")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.room:room-testing:2.6.x")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

### Projektstruktur (Package-Layout)

```
com.example.pulseguard/
├── data/
│   ├── local/
│   │   ├── PulseGuardDatabase.kt
│   │   ├── dao/
│   │   │   └── BloodPressureDao.kt
│   │   └── entity/
│   │       └── BloodPressureEntry.kt
│   └── repository/
│       ├── BloodPressureRepository.kt
│       └── BloodPressureRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   ├── MeasurementArm.kt          // Enum: LEFT, RIGHT
│   │   ├── BloodPressureCategory.kt   // Enum: OPTIMAL, NORMAL, HIGH_NORMAL, etc.
│   │   └── DashboardAggregation.kt    // Data class für aggregierte Werte
│   └── usecase/
│       ├── AddMeasurementUseCase.kt
│       ├── GetDashboardDataUseCase.kt
│       └── ExportToPdfUseCase.kt
├── ui/
│   ├── navigation/
│   │   └── PulseGuardNavGraph.kt
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── screens/
│   │   ├── dashboard/
│   │   │   ├── DashboardScreen.kt
│   │   │   └── DashboardViewModel.kt
│   │   ├── entry/
│   │   │   ├── EntryScreen.kt
│   │   │   └── EntryViewModel.kt
│   │   └── export/
│   │       ├── ExportScreen.kt
│   │       └── ExportViewModel.kt
│   └── components/
│       ├── BloodPressureCard.kt
│       ├── NumericInputField.kt
│       ├── ArmSelector.kt
│       ├── MedicationToggle.kt
│       └── PressureChart.kt
├── di/
│   └── AppModule.kt
└── PulseGuardApp.kt  // Application class
```

---

## 2. Datenbank & Core Models

### 2.1 Room Entity: `BloodPressureEntry`

```kotlin
@Entity(tableName = "blood_pressure_entries")
data class BloodPressureEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "systolic")
    val systolic: Int,                    // mmHg, Bereich: 60–300

    @ColumnInfo(name = "diastolic")
    val diastolic: Int,                   // mmHg, Bereich: 30–200

    @ColumnInfo(name = "pulse")
    val pulse: Int,                       // bpm, Bereich: 30–250

    @ColumnInfo(name = "measurement_arm")
    val measurementArm: MeasurementArm,   // LEFT oder RIGHT

    @ColumnInfo(name = "medication_taken")
    val medicationTaken: Boolean,         // Toggle: Medikament eingenommen?

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,                  // Epoch millis, auto-generiert

    @ColumnInfo(name = "note")
    val note: String? = null              // Optionale Notiz (future-proof)
)
```

### 2.2 Enum: `MeasurementArm`

```kotlin
enum class MeasurementArm {
    LEFT,   // Linker Arm
    RIGHT   // Rechter Arm
}
```

**Room TypeConverter** nötig: `MeasurementArm ↔ String`

### 2.3 DAO: `BloodPressureDao`

Kernabfragen (Signatur-Outline, kein vollständiger Code):

| Methode | Rückgabe | Zweck |
|---|---|---|
| `insertEntry(entry)` | `Long` | Neuen Eintrag speichern |
| `getAllEntries()` | `Flow<List<Entry>>` | Alle Einträge, sortiert nach Timestamp desc |
| `getEntriesForDateRange(start, end)` | `Flow<List<Entry>>` | Einträge in Zeitfenster (Dashboard-Filter) |
| `getAverageForDateRange(start, end)` | `Flow<AggregatedValues?>` | AVG systolisch, diastolisch, Puls |
| `getMinMaxForDateRange(start, end)` | `Flow<MinMaxValues?>` | MIN/MAX für Zeitraum |
| `deleteEntry(id)` | `Unit` | Einzelnen Eintrag löschen |
| `getEntryCount()` | `Flow<Int>` | Gesamtanzahl (für leeren Zustand) |

### 2.4 Datenbank-Klasse

```kotlin
@Database(
    entities = [BloodPressureEntry::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PulseGuardDatabase : RoomDatabase() {
    abstract fun bloodPressureDao(): BloodPressureDao
}
```

**Migrations-Strategie:** `exportSchema = true` für automatische Schema-Dokumentation. Ab Version 2+ werden explizite `Migration`-Objekte erstellt und getestet.

### 2.5 Aggregations-Modell (Domain Layer)

```kotlin
data class DashboardAggregation(
    val periodLabel: String,           // z.B. "März 2026", "KW 10", "03.03.2026"
    val avgSystolic: Float,
    val avgDiastolic: Float,
    val avgPulse: Float,
    val minSystolic: Int,
    val maxSystolic: Int,
    val minDiastolic: Int,
    val maxDiastolic: Int,
    val entryCount: Int,
    val category: BloodPressureCategory // Farbkodierung nach WHO-Skala
)
```

### 2.6 Blutdruck-Kategorien (WHO/ESH-Skala)

| Kategorie | Systolisch | Diastolisch | Farbe (Vorschlag) |
|---|---|---|---|
| Optimal | < 120 | < 80 | Grün `#4CAF50` |
| Normal | 120–129 | 80–84 | Hellgrün `#8BC34A` |
| Hochnormal | 130–139 | 85–89 | Gelb `#FFC107` |
| Hypertonie Grad 1 | 140–159 | 90–99 | Orange `#FF9800` |
| Hypertonie Grad 2 | 160–179 | 100–109 | Rot `#F44336` |
| Hypertonie Grad 3 | ≥ 180 | ≥ 110 | Dunkelrot `#B71C1C` |

---

## 3. UI / UX Blueprint

### 3.1 Navigation (3 Top-Level Destinations)

```
NavGraph: PulseGuardNavGraph
├── DashboardScreen  (startDestination, BottomNav Icon: "Dashboard")
├── EntryScreen      (BottomNav Icon: "+" / FAB, oder eigenes Tab)
└── ExportScreen     (BottomNav Icon: "Teilen")
```

**Navigationskonzept:** Bottom Navigation Bar mit 3 Tabs. Alternativ: Dashboard als Hauptscreen mit FAB für neuen Eintrag und Export über TopAppBar-Action.

**Empfehlung:** FAB-Ansatz, da der Eingabe-Screen der häufigste Interaktionspunkt ist. Die BottomNav hätte dann nur 2 Tabs (Dashboard, Export), und der FAB öffnet den Entry-Screen als Modal/FullScreen.

### 3.2 Dashboard Screen

**State:** `DashboardUiState` (via `StateFlow` im ViewModel)

```kotlin
data class DashboardUiState(
    val selectedPeriod: DashboardPeriod = DashboardPeriod.WEEK,
    val aggregation: DashboardAggregation? = null,
    val recentEntries: List<BloodPressureEntry> = emptyList(),
    val chartData: List<ChartDataPoint> = emptyList(),
    val isLoading: Boolean = true
)

enum class DashboardPeriod { DAY, WEEK, MONTH }
```

**Layout-Hierarchie:**

1. **Period Selector** – SegmentedButton-Row: Tag | Woche | Monat
2. **Summary Card** – Große Darstellung: Ø Systolisch/Diastolisch, Ø Puls, Kategorie-Badge mit Farbe
3. **Trend Chart** – Liniendiagramm (Vico Library) mit systolisch/diastolisch als zwei Linien, Puls optional einblendbar
4. **Letzte Messungen** – LazyColumn mit den letzten 5–10 Einträgen als kompakte Karten

### 3.3 Entry Screen (Datenerfassung)

**State:** `EntryUiState`

```kotlin
data class EntryUiState(
    val systolic: String = "",
    val diastolic: String = "",
    val pulse: String = "",
    val measurementArm: MeasurementArm = MeasurementArm.LEFT,
    val medicationTaken: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val validationErrors: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)
```

**Layout-Hierarchie:**

1. **Zeitstempel-Anzeige** – Automatisch gesetzt, tippbar zum manuellen Überschreiben (DatePicker + TimePicker)
2. **Blutdruck-Eingabebereich**
   - Systolisch: `OutlinedTextField` mit `KeyboardType.Number`, Label "Systolisch (mmHg)", Bereich 60–300
   - Diastolisch: `OutlinedTextField` mit `KeyboardType.Number`, Label "Diastolisch (mmHg)", Bereich 30–200
   - Puls: `OutlinedTextField` mit `KeyboardType.Number`, Label "Puls (bpm)", Bereich 30–250
   - **Kritisch:** `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)` erzwingt Number-Pad
   - **Validierung:** Live-Feedback (rote Umrandung + Fehlertext bei Out-of-Range)
3. **Messort** – `SingleChoiceSegmentedButtonRow`: "Linker Arm" | "Rechter Arm"
4. **Medikamenten-Toggle** – `Switch` mit Label "Medikament eingenommen"
5. **Speichern-Button** – `FilledButton`, disabled wenn Pflichtfelder leer oder invalide

**UX-Flow-Regeln:**
- Nach dem Speichern: Haptic Feedback + kurze Snackbar-Bestätigung, danach automatische Navigation zurück zum Dashboard
- Fokus-Reihenfolge: Systolisch → Diastolisch → Puls (via `FocusRequester`-Kette und `imeAction = ImeAction.Next`)
- Number Pad bleibt während der gesamten Eingabe sichtbar (kein KeyboardType-Wechsel)

### 3.4 Export Screen

**State:** `ExportUiState`

```kotlin
data class ExportUiState(
    val dateRangeStart: Long? = null,
    val dateRangeEnd: Long? = null,
    val previewEntryCount: Int = 0,
    val isGenerating: Boolean = false,
    val generatedPdfUri: Uri? = null
)
```

**Layout-Hierarchie:**

1. **Zeitraum-Auswahl** – DateRangePicker (Material3) für Start- und Enddatum
2. **Vorschau-Info** – "X Messungen im gewählten Zeitraum"
3. **PDF generieren** – Button, startet Generation
4. **Share** – Wird nach Generierung aktiv, öffnet `Intent.ACTION_SEND` mit dem PDF via FileProvider

---

## 4. Export Engine (PDF-Generierung)

### 4.1 Technologie

Nativ über `android.graphics.pdf.PdfDocument` – keine externe Library nötig. Vorteile: Kein zusätzlicher Dependency-Overhead, volle Kontrolle über Layout, keine Lizenzprobleme.

### 4.2 PDF-Layout-Konzept

**Seite 1: Deckblatt & Zusammenfassung**
- App-Logo + "Pulse Guard – Blutdruck-Protokoll"
- Zeitraum: [Startdatum] bis [Enddatum]
- Zusammenfassung: Ø-Werte, Min/Max, Anzahl Messungen, Kategorie

**Seite 2+: Messwerttabelle**
- Tabellenspalten: Datum/Uhrzeit | Systolisch | Diastolisch | Puls | Arm | Medikament
- Farbige Zeilenmarkierung gemäß Kategorie
- Seitenumbruch nach ca. 25 Zeilen pro Seite

**Seite N: Trend-Chart (optional, Phase 2)**
- Statisch gerendertes Liniendiagramm via Canvas-API auf die PDF-Page gezeichnet

### 4.3 Share-Mechanismus

```
1. PDF in App-internem Cache generieren: context.cacheDir
2. FileProvider-URI erstellen (deklariert in AndroidManifest.xml)
3. Intent.ACTION_SEND mit type "application/pdf" und der URI
4. startActivity(Intent.createChooser(...))
```

**AndroidManifest-Einträge nötig:**
- `<provider>` für `FileProvider` mit `file_provider_paths.xml`
- `file_provider_paths.xml`: `<cache-path name="pdfs" path="exports/" />`

---

## 5. Required Assets List

### 5.1 Icons

| Asset | Beschreibung | Format | Quelle |
|---|---|---|---|
| `ic_launcher` | App-Icon (Herz mit Pulslinie) | Adaptive Icon (SVG → PNG) | Muss erstellt werden |
| `ic_launcher_round` | Rundes App-Icon | PNG | Abgeleitet von ic_launcher |
| `ic_dashboard` | BottomNav: Dashboard | 24dp SVG | Material Icons: `monitoring` oder `dashboard` |
| `ic_add_entry` | FAB: Neuer Eintrag | 24dp SVG | Material Icons: `add` |
| `ic_export` | BottomNav/Action: Export | 24dp SVG | Material Icons: `share` oder `picture_as_pdf` |
| `ic_left_arm` | Segmented Button: Linker Arm | 24dp SVG | Custom oder Material: `back_hand` |
| `ic_right_arm` | Segmented Button: Rechter Arm | 24dp SVG | Custom oder gespiegelt |
| `ic_medication` | Toggle-Icon Medikament | 24dp SVG | Material Icons: `medication` |
| `ic_calendar` | Datumsauswahl | 24dp SVG | Material Icons: `calendar_today` |
| `ic_pulse` | Deko-Element Puls | 24dp SVG | Material Icons: `favorite` oder `ecg` |

**Hinweis:** Material Icons können direkt als Compose `ImageVector` über `androidx.compose.material:material-icons-extended` genutzt werden – dadurch entfallen viele separate Icon-Dateien.

### 5.2 Fonts

| Font | Verwendung | Lizenz |
|---|---|---|
| **System Default (Roboto / Sans Serif)** | Body-Text, Labels | Systemfont, keine Einbettung nötig |
| **Roboto Mono** (optional) | Zahlenwerte in Eingabefeldern und Tabellen für gleichmäßige Spaltenbreite | Apache 2.0 (Google Fonts) |

**Empfehlung:** Für Phase 1 ausschließlich Systemfonts verwenden. Ein Custom-Font kann in Phase 2 für Branding hinzugefügt werden.

### 5.3 Sonstige Assets

| Asset | Beschreibung |
|---|---|
| `splash_logo.svg` | Splash-Screen-Grafik (Herz-Icon) |
| `empty_state.svg` | Illustration für leeres Dashboard ("Noch keine Messungen") |
| `pdf_header_logo.png` | Kleines Logo für PDF-Kopfzeile (ca. 120×40px) |
| `file_provider_paths.xml` | XML-Config für FileProvider (res/xml) |

---

## 6. State-Management-Architektur

### Übergreifendes Muster

```
[Room DB] → Flow → [Repository] → Flow → [UseCase] → [ViewModel] → StateFlow → [Compose UI]
                                                              ↑
                                                        User Events
```

Alle ViewModels exponieren genau einen `StateFlow<XyzUiState>` nach außen. UI-Events werden über sealed interfaces modelliert:

```kotlin
sealed interface DashboardEvent {
    data class PeriodChanged(val period: DashboardPeriod) : DashboardEvent
    data class EntryClicked(val entryId: Long) : DashboardEvent
}
```

Kein `mutableStateOf` in ViewModels – ausschließlich `MutableStateFlow` + `.update {}`.

---

## 7. Validierungsregeln (Eingabe-Screen)

| Feld | Typ | Min | Max | Pflicht | Fehlermeldung |
|---|---|---|---|---|---|
| Systolisch | Int | 60 | 300 | Ja | "Wert muss zwischen 60 und 300 mmHg liegen" |
| Diastolisch | Int | 30 | 200 | Ja | "Wert muss zwischen 30 und 200 mmHg liegen" |
| Puls | Int | 30 | 250 | Ja | "Wert muss zwischen 30 und 250 bpm liegen" |
| Zusätzlich | – | – | – | – | Systolisch muss > Diastolisch sein |

---

## 8. Phasenplan (Implementierungsreihenfolge)

| Phase | Inhalt | Abhängigkeiten |
|---|---|---|
| **Phase 1** | Projekt-Setup, Room DB + Entity + DAO + Repository, DI-Setup | Keine |
| **Phase 2** | Entry Screen (Eingabe-UI + ViewModel + Validierung) | Phase 1 |
| **Phase 3** | Dashboard Screen (Aggregation, Chart, Listenansicht) | Phase 1 + 2 |
| **Phase 4** | Export Engine (PDF-Generierung + Share-Intent) | Phase 1 |
| **Phase 5** | Polish (Theming, Animationen, Empty States, Edge Cases) | Phase 2 + 3 + 4 |
| **Phase 6** | Testing (Unit Tests für UseCases, UI Tests für Screens) | Alle |
