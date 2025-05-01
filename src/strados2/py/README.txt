Erklärungen zur Ausführung und dem Aufbau der Python Programme:

ProbabilityDifferenceCalculator (für Earth Movers Distance):
* Benötigte Bibliotheken: scipy, numpy
Folgenden Befehl in diesem Verzeichnis ausführen, um ProbabilityDifferenceCalculator auszuführen:
"python ProbabilityDifferenceCalculator.py piece_distributions_classic.txt piece_distributions_barrage.txt"
1. txt Dateien mit Wahrscheinlichkeitsverteilungen werden eingelesen
2. Wahrscheinlichkeiten werden normalisiert
3. Erstellung einer Distanzmatrix mit Manhatten-Distanz
4. Berechnung des EMD mit scipy.otimize
5. Ausgabe berechneter Werte in cmd
* Gemini 2.5 Pro Preview 03-25 ist maßgeblich für den Code verantwortlich, er wurde von mir überprüft