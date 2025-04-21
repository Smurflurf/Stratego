package strados2.tools;

import java.awt.Color;
import java.util.List;

import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.HeatMapChartBuilder;

import strados2.classic.ClassicPiece;
import strados2.classic.ClassicPiece.ClassicRank;

/**
 * Generates heatmaps from {@link ClassicPiece} board arrays. 
 */
public class HeatMapGenerator extends GeneralTools {
	/**
	 * Creates and saves a heatmap with XChart
	 *
	 * @param counts array build with {@link #aggregatePlacementCounts(List, ClassicRank)}
	 * @param title the heatmaps title, is located above the heatmap
	 * @param seriesName name the heatmap gets saved as
	 */
	public static void createHeatmapChart(int[][] heatData, String title, String seriesName) {
		HeatMapChart chart = new HeatMapChartBuilder()
				.width(600)
				.height(600)
				.xAxisTitle("x")
				.yAxisTitle("y")
				.build();

		chart.getStyler()
		.setShowValue(true)
		.setRangeColors(new Color[] {Color.white, Color.red})
		.setSeriesColors(new Color[] {Color.white, Color.red})
		.setChartBackgroundColor(Color.white)
		.setPlotContentSize(0.9) 	// Platz für Achsenbeschriftungen lassen
		.setLegendVisible(true);   // Legende ist bei Heatmaps mit Farbbalken oft redundant

		chart.addSeries(seriesName, COL_LABELS, ROW_LABELS, heatData);

		saveChart(chart, seriesName);
	}
}