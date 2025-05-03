Erklärungen zur Ausführung und dem Aufbau der Python Programme:

ProbabilityDifferenceCalculator (für Earth Movers Distance):
* Benötigte Bibliotheken: scipy, numpy
Folgenden Befehl in diesem Verzeichnis ausführen, um ProbabilityDifferenceCalculator auszuführen:
"python ProbabilityDifferenceCalculator.py piece_distributions_classic.txt piece_distributions_barrage.txt"
1. txt Dateien mit Wahrscheinlichkeitsverteilungen werden eingelesen
2. Wahrscheinlichkeiten werden normalisiert
3. Erstellung einer Distanzmatrix mit scipy.spatial.distance Manhatten-Distanz
4. Berechnung des EMD mit scipy.otimize
5. Ausgabe berechneter Werte in cmd
* Gemini 2.5 Pro Preview 03-25 ist maßgeblich für den Code verantwortlich, er wurde von mir überprüft


DifferenceHeatMap (generiert HeatMaps aus text, geschrieben für Differenz Heatmaps der Figurenrelationen)
* Benötigte Bibliotheken: numpy, matplotlib, seaborn
* Folgenden Befehl in diesem Verzeichnis ausführen, um DifferenceHeatMap auszuführen:
"python DifferenceHeatMap.py classic_barrage_difference" 
* Heatmaps werden in Stratego/charts ordner gespeichert, dieser sollte existieren bevor man dieses Programm startet.
1. Datei wird eingelesen
2. Daten werden korrekt geparst
3. seaborn erstellt heatmap
* Gemini 2.5 Pro Preview 03-25 ist maßgeblich für den Code verantwortlich, er wurde von mir überprüft
