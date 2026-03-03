# AGENTS.md – Pulse Guard
### Das Betriebssystem des Repositories v1.0

---

## 1. Coding-Standards & Architektur-Regeln

### 1.1 Sprache & Stil

- **Kotlin** als einzige Sprache. Kein Java.
- **Kotlin Coding Conventions** gemäß [kotlinlang.org/docs/coding-conventions.html](https://kotlinlang.org/docs/coding-conventions.html)
- Max. 120 Zeichen Zeilenlänge
- Alle öffentlichen Funktionen und Klassen erhalten KDoc-Kommentare
- Keine Wildcards in Imports (`import x.*` verboten)
- Keine `!!` Non-Null-Assertions im Produktionscode – stattdessen `requireNotNull()` oder sichere Behandlung

### 1.2 Architektur-Leitplanken

| Regel | Beschreibung |
|---|---|
| **Clean Architecture** | 3 Layer: `data` → `domain` → `ui`. Abhängigkeiten fließen nur nach innen. |
| **Unidirectional Data Flow** | UI → Event → ViewModel → UseCase → Repository → DB → Flow zurück |
| **StateFlow-Zwang** | ViewModels nutzen ausschließlich `MutableStateFlow` + `.update {}`. Kein `mutableStateOf()` in ViewModels. |
| **Room als Single Source of Truth** | Kein In-Memory-Caching außerhalb von Room. Alle Daten fließen reaktiv via `Flow`. |
| **Compose-Only UI** | Kein XML-Layout. Keine Fragments. Nur `@Composable`-Funktionen. |
| **Koin DI** | Dependency Injection via Koin. Keine manuelle Instanziierung von Repositories oder ViewModels. |
| **Sealed Interfaces für Events** | UI-Events als `sealed interface`, nicht als Lambda-Sammlungen. |
| **Keine Business-Logik in Composables** | Composables rufen ViewModel-Methoden auf, berechnen nichts selbst. |

### 1.3 Namenskonventionen

| Element | Muster | Beispiel |
|---|---|---|
| Entity | `XyzEntry` | `BloodPressureEntry` |
| DAO | `XyzDao` | `BloodPressureDao` |
| Repository Interface | `XyzRepository` | `BloodPressureRepository` |
| Repository Impl | `XyzRepositoryImpl` | `BloodPressureRepositoryImpl` |
| UseCase | `VerbNounUseCase` | `AddMeasurementUseCase` |
| ViewModel | `XyzViewModel` | `DashboardViewModel` |
| UI State | `XyzUiState` | `DashboardUiState` |
| Screen Composable | `XyzScreen` | `DashboardScreen` |
| Event Interface | `XyzEvent` | `DashboardEvent` |

### 1.4 Sicherheits-Leitplanken

- **Keine Netzwerk-Permissions.** Die App arbeitet vollständig offline.
- **Keine externen Analytics oder Tracking-SDKs.**
- **Kein `WRITE_EXTERNAL_STORAGE`.** PDF-Export läuft über `FileProvider` + App-Cache.
- **Input-Sanitization:** Alle numerischen Eingaben werden vor dem Speichern gegen definierte Bereiche validiert (siehe `implementation_plan.md`, Abschnitt 7).
- **Room exportSchema = true** für lückenlose Migrations-Dokumentation.

---

## 2. Das Vibe Ensemble (Agenten-Rollen)

### 2.1 Rollenübersicht

```
┌─────────────────────────────────────┐
│      MASTER ORCHESTRATOR (Du)       │
│  Architektur-Entscheidungen, Plan,  │
│  Phasen-Steuerung, Code-Reviews     │
└──────────────┬──────────────────────┘
               │
    ┌──────────┼──────────┐
    ▼          ▼          ▼
┌────────┐ ┌────────┐ ┌────────┐
│  ADA   │ │  UXA   │ │  QAA   │
│Android │ │  UX    │ │  QA    │
│Dev Agt │ │ Agent  │ │ Agent  │
└────────┘ └────────┘ └────────┘
```

### 2.2 Agent: Android Development Agent (ADA)

**Verantwortung:** Implementierung der gesamten Kotlin/Compose/Room-Codebasis.

**System-Prompt-Kern (für Claude Code oder vergleichbare Tools):**

```
Du bist ein Android-Entwickler, der ausschließlich in Kotlin arbeitet.
Du implementierst Code für die App "Pulse Guard" (com.example.pulseguard).

REGELN:
- Folge strikt der Package-Struktur aus implementation_plan.md
- Nutze Room für alle Datenpersistenz, StateFlow für State-Management
- Schreibe Compose-Only UI (kein XML, keine Fragments)
- Nutze Koin für Dependency Injection
- Jede Datei enthält genau eine öffentliche Klasse/Interface/Funktion
- Schreibe KDoc für alle public APIs
- Validiere ALLE numerischen Inputs gegen die definierten Bereiche
- Commit-Messages im Format: "feat(module): Kurzbeschreibung"

VERBOTEN:
- mutableStateOf() in ViewModels
- !! Non-Null-Assertions
- Business-Logik in @Composable Funktionen
- Wildcard-Imports
- Hardcodierte Strings (nutze strings.xml)
```

**Zugewiesene Phasen:** Phase 1 (Setup + DB), Phase 2 (Entry Screen), Phase 3 (Dashboard), Phase 4 (Export)

### 2.3 Agent: UX Agent (UXA)

**Verantwortung:** Design-Entscheidungen, Number-Pad-Flow, Accessibility, Composable-Layout-Reviews.

**System-Prompt-Kern:**

```
Du bist ein UX-Spezialist für Android Jetpack Compose.
Du reviewst und verbesserst die UI-Schicht der App "Pulse Guard".

FOKUS:
- Number-Pad bleibt durchgehend sichtbar (KeyboardType.Number auf allen Feldern)
- Fokus-Kette: Systolisch → Diastolisch → Puls via ImeAction.Next
- Touch-Targets mindestens 48dp (Material3-Standard)
- Farbkodierung der Blutdruck-Kategorien gemäß WHO-Skala
- Empty States mit hilfreicher Illustration
- Barrierefreiheit: contentDescription auf allen interaktiven Elementen
- Haptic Feedback nach erfolgreichem Speichern

VERBOTEN:
- Scrollbare Eingabeformulare, die das Number-Pad verdecken
- Modale Dialoge für die Haupteingabe (Fullscreen bevorzugt)
- Farben als einziges Unterscheidungsmerkmal (Accessibility)
```

**Zugewiesene Phasen:** Review in Phase 2, Phase 3, Phase 5

### 2.4 Agent: QA / Test Agent (QAA)

**Verantwortung:** Unit Tests, Integration Tests, UI Tests, Edge-Case-Validierung.

**System-Prompt-Kern:**

```
Du bist ein QA-Engineer für Android (Kotlin).
Du schreibst Tests für die App "Pulse Guard".

TESTSTRATEGIE:
- Unit Tests: Alle UseCases, ViewModels, Validierungslogik, TypeConverters
- Integration Tests: Room DAO-Abfragen (mit In-Memory-DB)
- UI Tests: Compose Testing (ComposeTestRule) für kritische Flows
- Edge Cases: Grenzwerte (systolisch = 60, 300), leere DB, gleiche Werte

FRAMEWORKS:
- JUnit 4 für Unit Tests
- Room Testing für DAO-Tests
- Compose UI Test für UI-Tests
- Turbine für StateFlow-Testing (falls hinzugefügt)

REGELN:
- Jeder Test hat genau einen Assert-Fokus
- Test-Namensformat: `methodName_condition_expectedResult`
- Keine Sleeps oder feste Delays in Tests
```

**Zugewiesene Phasen:** Phase 6 (dediziert), Reviews nach jeder Phase

---

## 3. Build & Test Kommandos

### 3.1 Für alle Agenten verbindlich

```bash
# Projekt bauen (Debug)
./gradlew assembleDebug

# Lint prüfen
./gradlew lintDebug

# Unit Tests ausführen
./gradlew testDebugUnitTest

# Instrumented Tests ausführen (Emulator/Gerät nötig)
./gradlew connectedDebugAndroidTest

# Alle Checks in einem Durchlauf
./gradlew check

# APK-Ausgabepfad
# app/build/outputs/apk/debug/app-debug.apk
```

### 3.2 Spezifische Kommandos

```bash
# Room Schema exportieren (für Migrations-Tracking)
# Konfiguriert in build.gradle.kts:
# ksp { arg("room.schemaLocation", "$projectDir/schemas") }

# Einzelnen Test ausführen
./gradlew testDebugUnitTest --tests "com.example.pulseguard.domain.usecase.AddMeasurementUseCaseTest"

# Compose-Previews rendern (Android Studio)
# Kein CLI-Äquivalent, nur über IDE-Preview
```

### 3.3 Pre-Commit Checkliste (für jeden Agenten)

Vor jedem Commit / Handoff muss der Agent bestätigen:

- [ ] `./gradlew assembleDebug` erfolgreich (keine Compile-Errors)
- [ ] `./gradlew lintDebug` ohne neue Warnings
- [ ] `./gradlew testDebugUnitTest` alle Tests grün
- [ ] Keine `TODO`s ohne zugehöriges Issue/Kommentar
- [ ] KDoc auf allen neuen public APIs

---

## 4. Handoff-Protokoll

### 4.1 State-Snapshot-Format

Nach jeder abgeschlossenen Phase erstellt der zuständige Agent einen **State Snapshot** im folgenden Format:

```markdown
## State Snapshot: Phase [N] – [Titel]
**Agent:** [ADA/UXA/QAA]
**Datum:** [ISO-Datum]
**Status:** COMPLETE / PARTIAL (mit Begründung)

### Erledigte Arbeit
- [Liste aller erstellten/geänderten Dateien]

### Neuer Code-Stand
- [Package-Pfade der neuen Klassen]
- [Neue Dependencies falls hinzugefügt]

### Offene Punkte / Known Issues
- [Liste, falls vorhanden]

### Kontext für nächsten Agenten
- [Wichtige Design-Entscheidungen, die getroffen wurden]
- [Abweichungen vom Plan mit Begründung]
- [Hinweise für die nächste Phase]

### Verifizierung
- assembleDebug: ✅/❌
- lintDebug: ✅/❌
- testDebugUnitTest: ✅/❌
- Anzahl neuer Tests: [N]
```

### 4.2 Handoff-Ablauf

```
Phase N abgeschlossen
        │
        ▼
Agent erstellt State Snapshot
        │
        ▼
Orchestrator reviewed Snapshot
        │
        ├── OK → Snapshot wird in /docs/snapshots/phase_N.md abgelegt
        │         Nächster Agent erhält: implementation_plan.md
        │                                + AGENTS.md
        │                                + Letzter Snapshot
        │                                + Zugriff auf Codebase
        │
        └── Rückfragen → Agent bearbeitet nach,
                         neuer Snapshot
```

### 4.3 Kontext-Fenster-Management

Um Kontext-Fäulnis zu vermeiden:

1. **Jeder Agent startet mit maximal 3 Dokumenten:** `implementation_plan.md`, `AGENTS.md`, letzter `phase_N.md` Snapshot
2. **Kein Agent liest die gesamte Codebase.** Er liest nur die Dateien, die er für seine Phase braucht.
3. **State Snapshots sind das Gedächtnis.** Alles Relevante muss im Snapshot stehen – nicht in Chat-Historien.
4. **Bei Unklarheiten:** Agent fragt den Orchestrator, statt Annahmen zu treffen.

### 4.4 Phasen-Zuordnung (Zusammenfassung)

| Phase | Primär-Agent | Review-Agent | Deliverable |
|---|---|---|---|
| Phase 1: DB + Setup | ADA | – | Kompilierendes Projekt mit Room DB |
| Phase 2: Entry Screen | ADA | UXA | Funktionierender Eingabe-Screen |
| Phase 3: Dashboard | ADA | UXA | Dashboard mit Aggregation + Chart |
| Phase 4: Export | ADA | – | PDF-Generierung + Share |
| Phase 5: Polish | UXA + ADA | – | Theme, Animationen, Empty States |
| Phase 6: Testing | QAA | ADA | Vollständige Test Suite |

---

## 5. Eskalationsregeln

| Situation | Aktion |
|---|---|
| Agent kann Phase nicht in einem Durchlauf abschließen | State Snapshot mit Status PARTIAL + Begründung |
| Architektur-Entscheidung weicht vom Plan ab | Orchestrator muss vor Implementierung zustimmen |
| Neue Dependency benötigt | Orchestrator genehmigt (Lizenz + Größe prüfen) |
| Build bricht nach Handoff | Empfangender Agent erstellt Bug-Report, Orchestrator entscheidet Zuordnung |
| Unklare Anforderung | Agent fragt Orchestrator, implementiert nicht auf Verdacht |
