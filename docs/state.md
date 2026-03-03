# Pulse Guard – State & Handoff-Protokoll (state.md)
### Dynamischer Session-State v1.0

---

> **Anleitung:** Diese Datei wird nach jeder abgeschlossenen Phase vom zuständigen Agenten aktualisiert.
> Sie dient als einzige Wahrheitsquelle für den aktuellen Projektstand und die Übergabe zwischen Agenten-Sitzungen.

---

## 1. Globaler Projektstatus

| Feld | Wert |
|------|------|
| **Aktuelle Phase** | Phase 1 – Projekt-Setup + Room Database |
| **Aktiver Agent** | – (noch nicht gestartet) |
| **Gesamtfortschritt** | 0 / 6 Phasen abgeschlossen |
| **Letztes Update** | – |
| **Nächste Phase** | Phase 1 |
| **Blocker** | Keine |

---

## 2. Phasen-Tracking

| Phase | Titel | Agent | Status | Datum |
|-------|-------|-------|--------|-------|
| Phase 1 | Projekt-Setup + Room DB | ADA | ⬜ Offen | – |
| Phase 2 | Entry Screen | ADA | ⬜ Offen | – |
| Phase 3 | Dashboard Screen | ADA | ⬜ Offen | – |
| Phase 4 | Export Engine | ADA | ⬜ Offen | – |
| Phase 5 | Polish | UXA + ADA | ⬜ Offen | – |
| Phase 6 | Testing | QAA | ⬜ Offen | – |

**Status-Legende:** ⬜ Offen | 🔄 In Arbeit | ✅ Abgeschlossen | ⚠️ Partial

---

## 3. Aktueller State Snapshot

> Dieser Abschnitt wird nach jeder Phase vollständig überschrieben.

### State Snapshot: Phase – [Noch kein Snapshot]

```
Noch keine Phase abgeschlossen.
Erster Snapshot wird nach Abschluss von Phase 1 erstellt.
```

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
