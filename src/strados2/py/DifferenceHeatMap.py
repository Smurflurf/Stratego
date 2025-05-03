import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import sys
import os

# Gemini 2.5 Pro Preview 03-25 maßgeblich für den Code verantwortlich
def parse_difference_file(filename):
    """
    Liest eine Differenzdatei im angegebenen Format ein.

    Args:
        filename (str): Der Pfad zur Datei.

    Returns:
        tuple: Ein Tupel (data_array, row_labels, col_labels)
               oder (None, None, None) bei Fehlern.
               data_array (np.array): 2D-Array der Differenzwerte.
               row_labels (list): Liste der Zeilenbeschriftungen (Zentrum).
               col_labels (list): Liste der Spaltenbeschriftungen (Nachbar).
    """
    row_labels = []
    col_labels = []
    data_rows = []

    try:
        with open(filename, 'r') as f:
            # 1. Lese Header (Spaltenbeschriftungen)
            header_line = f.readline().strip()
            if not header_line or not header_line.startswith(','):
                print(f"Error: Invalid header line format in '{filename}'. Expected ',Label1,Label2,...'")
                return None, None, None
            # Ignoriere das erste leere Element nach dem ersten Komma
            col_labels = [label.strip() for label in header_line.split(',')[1:]]
            num_cols = len(col_labels)
            if num_cols == 0:
                 print(f"Error: No column labels found in header of '{filename}'.")
                 return None, None, None

            # 2. Lese Datenzeilen
            for line_num, line in enumerate(f, 2): # Starte Zeilenzählung bei 2 für Fehlermeldungen
                line = line.strip()
                if not line: # Überspringe leere Zeilen
                    continue

                # Entferne das optionale Semikolon am Ende
                if line.endswith(';'):
                    line = line[:-1]

                parts = line.split(',')
                if len(parts) != num_cols + 1:
                    print(f"Error: Line {line_num} in '{filename}' has {len(parts)-1} data values, but expected {num_cols} based on header.")
                    return None, None, None

                row_label = parts[0].strip()
                row_labels.append(row_label)

                try:
                    # Konvertiere die restlichen Teile zu Integern
                    values = [int(val.strip()) for val in parts[1:]]
                    data_rows.append(values)
                except ValueError as e:
                    print(f"Error: Could not convert value to integer on line {line_num} in '{filename}': {e}")
                    return None, None, None

        # Konvertiere die Daten in ein NumPy Array
        if not data_rows:
             print(f"Error: No data rows found in '{filename}'.")
             return None, None, None

        data_array = np.array(data_rows, dtype=float) # Float für die Heatmap-Farbskala

        # Überprüfe, ob Anzahl der Zeilenbeschriftungen passt
        if len(row_labels) != data_array.shape[0]:
             print(f"Error: Number of row labels ({len(row_labels)}) does not match number of data rows ({data_array.shape[0]}) in '{filename}'.")
             return None, None, None

        return data_array, row_labels, col_labels

    except FileNotFoundError:
        print(f"Error: File not found '{filename}'")
        return None, None, None
    except Exception as e:
        print(f"An unexpected error occurred while reading '{filename}': {e}")
        return None, None, None

# --- Hauptskript ---
if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python ProbabilityDifferenceCalculator.py <difference_file>")
        sys.exit(1)

    input_file = sys.argv[1]

    if not os.path.exists(input_file):
        print(f"Error: Input file not found '{input_file}'")
        sys.exit(1)

    print(f"Reading difference data from '{input_file}'...")
    diff_data, row_labels, col_labels = parse_difference_file(input_file)

    if diff_data is None:
        sys.exit(1)

    print("Data read successfully. Generating heatmap...")

    # --- Heatmap erstellen ---
    plt.style.use('seaborn-v0_8-whitegrid') # Setzt einen sauberen Stil
    fig, ax = plt.subplots(figsize=(8, 7)) # Größe anpassen nach Bedarf

    # Bestimme Min/Max für symmetrische Farbskala um 0
    v_abs_max = np.max(np.abs(diff_data))
    vmin = -v_abs_max
    vmax = v_abs_max

    # Wähle eine divergierende Farbskala (Rot-Weiß-Grün ähnlich deinem Beispiel)
    # 'RdYlGn' oder 'coolwarm' (Rot-Weiß-Blau) sind gute Optionen
    cmap = 'RdYlGn' # Rot für negativ, Gelb für 0, Grün für positiv

    # Heatmap zeichnen mit Seaborn
    sns.heatmap(diff_data,
                ax=ax,
                annot=True,        # Werte in Zellen anzeigen
                fmt=".0f",         # Werte als Integer formatieren (ohne Nachkommastellen)
                cmap=cmap,         # Farbskala
                vmin=vmin,         # Minimum für Farbskala
                vmax=vmax,         # Maximum für Farbskala
                linewidths=0.5,    # Linien zwischen Zellen
                linecolor='lightgray',
                cbar=True,         # Farblegende anzeigen
                square=True,       # Zellen quadratisch machen
                xticklabels=col_labels, # Beschriftung X-Achse
                yticklabels=row_labels) # Beschriftung Y-Achse

    # Achsenbeschriftungen und Titel
    ax.set_xlabel('Nachbar Rang (Y)', fontsize=12)
    ax.set_ylabel('Zentrum Rang (X)', fontsize=12)

    # Drehung der Y-Achsenbeschriftung für bessere Lesbarkeit
    plt.yticks(rotation=0)
    plt.xticks(rotation=0) # Keine Rotation der X-Achse

    plt.tight_layout() # Passt Plot-Elemente an, um Überlappungen zu vermeiden

    # --- Speichern ---
    output_png = './../../../charts/'+input_file+'.png'
    output_svg = './../../../charts/'+input_file+'.svg'
    try:
        plt.savefig(output_png, dpi=300) # PNG mit 300 DPI
        plt.savefig(output_svg)          # SVG (Vektorformat)
        print(f"Heatmap saved as '{output_png}' and '{output_svg}'")
    except Exception as e:
        print(f"Error saving heatmap files: {e}")

    # Optional: Heatmap anzeigen
    # plt.show()