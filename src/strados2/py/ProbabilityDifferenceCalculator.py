import numpy as np
from scipy.optimize import linprog
from scipy.spatial.distance import cdist # Zur einfacheren Distanzberechnung
import sys
import os

# Gemini 2.5 Pro Preview 03-25 maßgeblich für den Code verantwortlich
def parse_distribution_file(filename):
    """
    Liest eine Distributionsdatei im angegebenen Format ein.

    Args:
        filename (str): Der Pfad zur Datei.

    Returns:
        tuple: Ein Tupel (distributions, x_dim, y_dim) oder (None, None, None) bei Fehlern.
               distributions (dict): Ein Dictionary {rank_name: np.array(shape=(y_dim, x_dim))}
    """
    distributions = {}
    try:
        with open(filename, 'r') as f:
            # 1. Lese Dimensionen
            first_line = f.readline().strip()
            if not first_line:
                print(f"Error: File '{filename}' is empty or has invalid first line.")
                return None, None, None
            parts = first_line.split()
            if len(parts) != 3:
                print(f"Error: Invalid dimension format in first line of '{filename}': {first_line}")
                return None, None, None

            num_ranks = int(parts[0])
            x_dim = int(parts[1]) # Spalten
            y_dim = int(parts[2]) # Zeilen

            if num_ranks <= 0 or x_dim <= 0 or y_dim <= 0:
                 print(f"Error: Invalid dimensions (< 0) in '{filename}': {num_ranks}x{x_dim}x{y_dim}")
                 return None, None, None

            # 2. Lese Daten für jeden Rang
            for _ in range(num_ranks):
                rank_name = f.readline().strip()
                if not rank_name:
                     print(f"Error: Unexpected end of file while reading rank name in '{filename}'.")
                     return None, None, None

                rank_data = []
                for r in range(y_dim):
                    line = f.readline().strip()
                    if not line:
                        print(f"Error: Unexpected end of file while reading data for rank '{rank_name}', line {r+1} in '{filename}'.")
                        return None, None, None
                    try:
                        # Lese x_dim Werte pro Zeile
                        row_values = [int(val) for val in line.split()]
                        if len(row_values) != x_dim:
                             print(f"Error: Incorrect number of values ({len(row_values)}) for x_dim ({x_dim}) for rank '{rank_name}', line {r+1} in '{filename}'.")
                             return None, None, None
                        rank_data.append(row_values)
                    except ValueError as e:
                        print(f"Error parsing integer value for rank '{rank_name}', line {r+1} in '{filename}': {e}")
                        return None, None, None

                # Speichere als NumPy Array (y_dim, x_dim)
                distributions[rank_name] = np.array(rank_data, dtype=np.float64) # Float für spätere Normalisierung

            return distributions, x_dim, y_dim

    except FileNotFoundError:
        print(f"Error: File not found '{filename}'")
        return None, None, None
    except Exception as e:
        print(f"An unexpected error occurred while reading '{filename}': {e}")
        return None, None, None

def calculate_emd(dist1_counts, dist2_counts):
    """
    Berechnet die Earth Mover's Distance zwischen zwei 2D-Verteilungen.

    Args:
        dist1_counts (np.array): 2D-Array der Zählungen für Verteilung 1.
        dist2_counts (np.array): 2D-Array der Zählungen für Verteilung 2.

    Returns:
        float: Die berechnete EMD oder np.nan bei Fehlern.
    """
    if dist1_counts.shape != dist2_counts.shape:
        print("Error: Distributions must have the same shape for EMD calculation.")
        return np.nan

    y_dim, x_dim = dist1_counts.shape
    num_fields = y_dim * x_dim

    # --- 1. Normalisierung zu Wahrscheinlichkeiten ---
    sum1 = dist1_counts.sum()
    sum2 = dist2_counts.sum()

    if sum1 <= 1e-9 or sum2 <= 1e-9: # Kleine Toleranz für den Fall fast leerer Maps
        # Wenn eine Verteilung (fast) leer ist, ist EMD weniger aussagekräftig
        # oder erfordert spezielle Behandlung. Wir geben NaN zurück oder 0 wenn beide leer.
        if sum1 <= 1e-9 and sum2 <= 1e-9:
            return 0.0 # Keine Arbeit nötig, wenn nichts zu bewegen ist
        print("Warning: One or both distributions have near-zero sum. EMD might be misleading.")
        # Man könnte hier auch die Total Variation Distance als Fallback berechnen
        return np.nan # Oder einen anderen Fehlerwert

    p1 = dist1_counts / sum1
    p2 = dist2_counts / sum2

    # --- 2. Kostenmatrix (Ground Distance) ---
    # Erstelle Koordinaten für jedes Feld (bin)
    coords = np.array([(r, c) for r in range(y_dim) for c in range(x_dim)])
    # Berechne paarweise Manhattan-Distanzen (effizienter als Doppelschleife)
    # cdist erwartet (n_samples, n_features)
    distance_matrix = cdist(coords, coords, metric='cityblock') # cityblock = Manhattan

    # --- 3. Aufsetzen des Linearen Programmierproblems (Transportproblem) ---
    # Wir wollen die Kosten C = sum(flow[i,j] * distance_matrix[i,j]) minimieren
    # flow[i,j] ist die Menge, die von Feld i (in p1) zu Feld j (in p2) transportiert wird.

    # Flatten der Verteilungen und der Kostenmatrix für linprog
    p1_flat = p1.flatten()
    p2_flat = p2.flatten()
    cost_vector = distance_matrix.flatten() # Kosten c für die Zielfunktion

    # Constraints für linprog: A_eq @ x = b_eq
    # x ist der geflattete flow-Vektor der Größe num_fields * num_fields
    num_vars = num_fields * num_fields

    # Gleichungs-Constraints:
    # a) Summe des ausgehenden Flows von jedem Feld i in p1 muss p1_flat[i] sein
    # b) Summe des eingehenden Flows zu jedem Feld j in p2 muss p2_flat[j] sein
    num_constraints = 2 * num_fields
    A_eq = np.zeros((num_constraints, num_vars))
    b_eq = np.zeros(num_constraints)

    for i in range(num_fields):
        # Constraint a) für Feld i
        start_idx_row = i * num_fields
        end_idx_row = start_idx_row + num_fields
        A_eq[i, start_idx_row:end_idx_row] = 1 # Summiere flow[i, j] für alle j
        b_eq[i] = p1_flat[i]

        # Constraint b) für Feld i (als Zielfeld j)
        indices_col = [k * num_fields + i for k in range(num_fields)]
        A_eq[num_fields + i, indices_col] = 1 # Summiere flow[k, i] für alle k
        b_eq[num_fields + i] = p2_flat[i]

    # Bounds für die Flussvariablen (müssen >= 0 sein)
    bounds = [(0, None)] * num_vars

    # --- 4. Lösen des LPs ---
    try:
        # 'highs' ist oft der robusteste Solver in neueren SciPy-Versionen
        result = linprog(cost_vector, A_eq=A_eq, b_eq=b_eq, bounds=bounds, method='highs')

        if result.success:
            return result.fun # Das ist die minimale Summe der Kosten * Flüsse = EMD
        else:
            print(f"Warning: Linear programming solver did not converge ({result.message}).")
            return np.nan
    except ValueError as e:
         print(f"Error during linear programming setup or execution: {e}")
         # Kann passieren, wenn Summen von p1 und p2 stark abweichen trotz Normalisierung (Rundungsfehler)
         # oder wenn das Problem unzulässig ist.
         return np.nan
    except Exception as e:
        print(f"An unexpected error occurred during linprog: {e}")
        return np.nan


# --- Hauptskript ---
if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python ProbabilityDifferenceCalculator.py <distribution_file1> <distribution_file2>")
        sys.exit(1)

    file1 = sys.argv[1]
    file2 = sys.argv[2]

    if not os.path.exists(file1):
        print(f"Error: File not found '{file1}'")
        sys.exit(1)
    if not os.path.exists(file2):
        print(f"Error: File not found '{file2}'")
        sys.exit(1)

    print(f"Loading distributions from '{file1}'...")
    dist_map1, x_dim1, y_dim1 = parse_distribution_file(file1)
    if dist_map1 is None:
        sys.exit(1)

    print(f"Loading distributions from '{file2}'...")
    dist_map2, x_dim2, y_dim2 = parse_distribution_file(file2)
    if dist_map2 is None:
        sys.exit(1)

    # --- Vergleiche Dimensionen ---
    if x_dim1 != x_dim2 or y_dim1 != y_dim2:
        print(f"Error: Dimensions mismatch between files.")
        print(f"  '{file1}': {x_dim1}x{y_dim1}")
        print(f"  '{file2}': {x_dim2}x{y_dim2}")
        sys.exit(1)

    # --- Finde gemeinsame Ränge ---
    common_ranks = sorted(list(set(dist_map1.keys()) & set(dist_map2.keys())))

    if not common_ranks:
        print("No common ranks found between the two files.")
        sys.exit(0)

    print(f"\nCalculating Earth Mover's Distance (EMD) for {len(common_ranks)} common ranks:")
    print("-" * 40)

    # --- Berechne und drucke EMD für jeden gemeinsamen Rang ---
    results = {}
    for rank in common_ranks:
        counts1 = dist_map1[rank]
        counts2 = dist_map2[rank]

        emd_value = calculate_emd(counts1, counts2)
        results[rank] = emd_value
        if not np.isnan(emd_value):
            print(f"  {rank:<12}: {emd_value:.4f}")
        else:
            print(f"  {rank:<12}: Calculation Failed")

    print("-" * 40)

    # Optional: Gesamt-EMD oder Durchschnitt berechnen (kann irreführend sein)
    valid_emds = [v for v in results.values() if not np.isnan(v)]
    if valid_emds:
         average_emd = sum(valid_emds) / len(valid_emds)
         print(f"Average EMD across common ranks: {average_emd:.4f}")