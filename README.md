# Stratego Quick Battle

Ein Java-Projekt zur Entwicklung einer KI für Stratego Quick Battle, entstanden im Rahmen meiner Bachelorarbeit. 
Das Projekt implementiert die Spielmechanik und nutzt analysierte Spieldaten von [Gravon.de](https://www.gravon.de/strados2/files/), um statistisch fundierte Platzierungsstrategien zu entwickeln.

## Kernkomponenten

*   **Spiel-Logik (`core`):** Implementierung der Quick Battle Regeln, Zustandsverwaltung (`GameState`), Spielfiguren (`Piece`), Züge (`Move`) und Hilfsfunktionen (`Utils`).
*   **Platzierung (`core.placing`):** Verschiedene Algorithmen zur initialen Aufstellung der Figuren, darunter zufällige Platzierung (`RandomAI`) und zwei heuristische Methoden basierend auf analysierten Spieldaten (`HeuristicDeBoer`), (`HeuristicBarrage`).
*   **Spiel-KI (`core.playing`):** Basis für Spielstrategien, implementiert Zufalls-, Heuristische- und MCTS-KI. Eine (`HumanInput`) Klasse verwaltet menschliche Zugwahlen.
*   **Simulation & Profiler:** `executable.Runner` zum Ausführen von Simulationen, `Mediator` zur Spielzustandsverwaltung (nimmt die Rolle eines Servers ein) und  `Profiler` um den Speicherverbrauch der Klassen zu analysieren
*   **UI:** `ui.UI` beinhaltet eine einfache Swing-UI zur Visualisierung eines einzigen Spiels.
*   **Datenanalyse (`strados2`):** Werkzeuge zum Download (`Scraper`), Parsen (`XmlParser`) und Analysieren (`GravonAnalyser`) von Gravon-Spieldaten. Generiert Platzierungsverteilungen und Nachbarschaftsanalysen. Speichert/lädt Analysedaten (`CompressedMapIO`, `NeighborIO`). Generiert Grafiken mithilfe von xChart.
*   **Python (`strados2/py`):** Python-Skripte für Datenanalyse:
       `ProbabilityDifferenceCalculator.py` zur Berechnung der Earth Mover's Distance (EMD) zwischen Platzierungsverteilungen verschiedener Modi mithilfe von `numpy` und `scipy`.
       `DifferenceHeatMap.py` zum Generieren von Differenz Heatmaps die vorher in `GravonAnalyser` textlich erstellt wurden (xChart kann Achsenlabels nicht korrekt darstellen). Nutzt `numpy`, `matplotlib` und `seaborn`.

## Setup

1.  **Java:** JDK 24 empfohlen. Alle verwendeten Java Bibliotheken sind in `lib` enthalten. Die Bibliotheken müssen ggf. im Classpath angegeben werden. Um die Effizienz der Agenten zu steigern, sollte `-XX:+UseParallelGC` als VM-Argument angegeben werden, um den Parallel GC zu nutzen.
2.  **Python:** Python 3 mit `numpy`, `scipy`, `matplotlib` und `seaborn`: (`pip install numpy scipy matplotlib seaborn`).

## Benutzung

1.   **Simulation (executable):**
      *  Konfiguration `Runner.java` (`main`-Methode): wird use_UI auf true gesetzt, wird, falls simulations = 1, eine UI geöffnet. red/bluePlacement geben die möglichen Platzierheuristiken. DEBOER nutzt Classic Wahrscheinlichkeiten, BARRAGE die von Barrage. red/bluePlayer gibt den Spielertyp an. AI.Type.HUMAN erlaubt menschliche Inputs in der UI.
      *  Starten: `java Runner` (zeigt Statistiken nach Abschluss).
2.    **MCTS Konfiguration (cote.playing.mcts):**
      *  Das Einkommentieren von `printResults(bestChild);` in der `MCTS.nextMove()` Methode gibt Informationen zur Zugwahl an.
      *  Die `Constants` Klasse enthält MAX_STEPS, um die Simulationstiefe festzulegen. TIME_IN_MS und MAX_SIMULATIONS begrenzen die Simulationszeit, das was zuerst erreicht wird beendet die Zugwahl.
4.    **Datenanalyse(executable):**
      *  Analysieren & Daten generieren: `GravonAnalyser.java` (Konfiguration in der `main`-Methode). Lädt automatisch die Gravon Spiele herunter, falls noch nicht geschehen.
5.    **Python:**
      *   Python Code ist in `/src/strados2/py` enthalten. Relevante Analyse Dateien werden von `GravonAnalyser.java` in das `py` package geladen. Genaue Informationen sind in der dortigen README.
      *   EMD-Analyse: `python ProbabilityDifferenceCalculator.py file1.txt file2.txt` file1.txt und file2.txt durch relevante files aus `py` package ersetzen.

## Relevantes

*   Spieldaten von [Gravon.de](https://www.gravon.de/strados2/files/).
*   Verwendete Java Bibliotheken: XChart, fastutil, jol-cli, VectorGraphics2D.
*   Verwendete Python Bibliotheken: NumPy, SciPy, matplotlib und seaborn.
*   Coding unterstützt von Gemini 2.5 Pro Preview 03-25, maßgeblich für Python Code und IO Code verantwortlich.
