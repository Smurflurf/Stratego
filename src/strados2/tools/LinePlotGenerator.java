package strados2.tools;

import java.awt.Color;
import java.util.List;

import org.knowm.xchart.XYChart;

import strados2.classic_board_representation.ClassicPiece;
import strados2.classic_board_representation.ClassicPiece.ClassicRank;

/**
 * Generates xy charts from {@link ClassicPiece} board arrays,
 * describing the Pieces distributions.
 */
public class LinePlotGenerator extends GeneralTools {
	/**
	 * Calculates the distribution over the different y ranks.
	 * Both players ranks get overlaid, so rank 9 equals rank 0. 
	 */
	public static double[] getPieceYDistribution(int[][] board) {
		double[] occurances = new double[BOARD_SIZE / 2];
		int allOccurances = 0;
		for(int y=0; y<BOARD_SIZE; y++) {
			int yOccurances = 0;
			for(int x=0; x<BOARD_SIZE; x++) {
				yOccurances += board[x][y];
			}	
			if(y < 5)
				occurances[y] = yOccurances;
			else 
				occurances[BOARD_SIZE - 1 - y] += yOccurances;
			allOccurances += yOccurances;
		}
		
		for(int i=0; i<occurances.length; i++)
			if(occurances[i] != 0)
				occurances[i] /= allOccurances;
		
		return occurances;
	}
	
	/**
	 * Calculates the distribution over the different X ranks.
	 */
	public static double[] getPieceXDistribution(int[][] board) {
		double[] occurances = new double[BOARD_SIZE];
		int allOccurances = 0;
		for(int x=0; x<BOARD_SIZE; x++) {
			int xOccurances = 0;
			for(int y=0; y<BOARD_SIZE; y++) {
				xOccurances += board[x][y];
			}	
			occurances[x] = xOccurances;
			allOccurances += xOccurances;
		}
		
		for(int i=0; i<occurances.length; i++)
			if(occurances[i] != 0)
				occurances[i] /= allOccurances;
		
		return occurances;
	}
	
	/**
	 * Creates and saves a heatmap with XChart
	 *
	 * @param counts array build with {@link #aggregatePlacementCounts(List, ClassicRank)}
	 * @param title the heatmaps title, is located above the heatmap
	 * @param seriesName name the heatmap gets saved as
	 */
	public static void createLinePlot(double[][] lineData, String title, ClassicPiece.ClassicRank[] ranks, String xTitle) {
		XYChart chart = new org.knowm.xchart.XYChartBuilder()
				.width(600)
				.height(600)
				.xAxisTitle(xTitle)
				.yAxisTitle("Figur Vorkommen")
				.build();

		chart.getStyler()
		.setSeriesColors(new Color[] {Color.blue, Color.red, Color.orange, Color.cyan, Color.green, Color.magenta, Color.black})
//		.setPlotContentSize(0.9) 
		.setChartBackgroundColor(Color.white)
		.setLegendVisible(true);
		
		for(int i=0; i<lineData.length; i++) {
			chart.addSeries(ranks[i].getName(), lineData[i]);
		}
		saveChart(chart, title);
	}
}
