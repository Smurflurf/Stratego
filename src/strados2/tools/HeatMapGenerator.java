package strados2.tools;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.HeatMapChartBuilder;

import it.unimi.dsi.fastutil.Pair;
import strados2.classic_board_representation.ClassicPiece;
import strados2.classic_board_representation.ClassicPiece.ClassicRank;

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
				.height(600 / (10 / heatData[0].length))
				.xAxisTitle("x")
				.yAxisTitle("y")
				.title(title)
				.build();

		chart.getStyler()
		.setShowValue(true)
		.setRangeColors(new Color[] {Color.white, Color.red})
		.setSeriesColors(new Color[] {Color.white, Color.red})
		.setChartBackgroundColor(Color.white)
		.setPlotContentSize(1)
		.setLegendVisible(true)
		.setChartTitleVisible(true);
		

		int[] ROW_LABELS = new int[heatData[0].length];
		for(int i=0; i<ROW_LABELS.length; i++)
			ROW_LABELS[i] = GeneralTools.ROW_LABELS[i];
		
		int[] COL_LABELS = new int[heatData.length];
		for(int i=0; i<COL_LABELS.length; i++)
			COL_LABELS[i] = GeneralTools.COL_LABELS[i];
		
		chart.addSeries(title, COL_LABELS, ROW_LABELS, heatData);
		saveChart(chart, seriesName);
	}
	
	/**
	 * Creates and saves a heatmap with XChart.
	 * xChart axis labels are bugged and do not work correctly.
	 * To target that this method prints the differences to console to be copied into a file at the py package.
	 * Calling "python DifferenceHeatMap.py differenceFile" will generate the heatMap.
	 * Comment in the saveChart(chart, seriesName); to get bugged output.
	 */
	public static void createDifferenceHeatmapChart(Map<Pair <ClassicRank, ClassicRank>, Integer> probMap1, 
			Map<Pair <ClassicRank, ClassicRank>, Integer> probMap2, 
			ClassicRank[] ranks, 
			String title, 
			String seriesName) {
		HeatMapChart chart = new HeatMapChartBuilder()
				.width(600)
				.height(600)
				.xAxisTitle("Nachbar")
				.yAxisTitle("Zentrum")
				.title(title)
				.build();

		chart.getStyler()
		.setShowValue(true)
		.setRangeColors(new Color[] {Color.red, Color.white, Color.green})
		.setSeriesColors(new Color[] {Color.red, Color.white, Color.green})
		.setChartBackgroundColor(Color.white)
		.setPlotContentSize(0.93)
		.setLegendVisible(false)
		.setChartTitleVisible(true);
		
		int[][] heatData = new int[ranks.length][ranks.length];
		
		System.out.println();
		for(int i=0; i<ranks.length; i++)
			System.out.print("," + ranks[i].getName());
		System.out.println();
		
		for(int i=0; i<ranks.length; i++) {
			System.out.print(ranks[i].getName() + ",");
			for(int ii=0; ii<ranks.length; ii++) {
				heatData[i][ii] = probMap2.get(Pair.of(ranks[i], ranks[ii])) - probMap1.get(Pair.of(ranks[i], ranks[ii]));
				System.out.print(heatData[i][ii] + (ii<ranks.length-1 ? "," : ""));
			}
			System.out.println(";");
		}
		

		int[]labels = new int[ranks.length];
		for(int i=0; i<ranks.length; i++)
			labels[i] = i;
		
		chart.addSeries(seriesName, labels, labels, heatData);
		

		final Map<Double, String> overwriteLabelsY = new HashMap<Double, String>();
		for(int i=0; i<ranks.length; i++)
			overwriteLabelsY.put(((double)i), ranks[ranks.length - i -1].getName().substring(0, 3));
		Function<Double, String> labelFormatterY = value -> overwriteLabelsY.get(value);
		
		final Map<Double, String> overwriteLabelsX = new HashMap<Double, String>();
		for(int i=0; i<ranks.length; i++)
			overwriteLabelsX.put(((double)i), ranks[i].getName().substring(0, 3));
		Function<Double, String> labelFormatterX = value -> overwriteLabelsX.get(value);
		
		chart.setCustomXAxisTickLabelsFormatter(labelFormatterX);
		chart.setCustomYAxisTickLabelsFormatter(labelFormatterY);
//		saveChart(chart, seriesName);
	}
}