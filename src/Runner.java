import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

import core.GameState;
import core.Move;
import core.Piece;
import core.Utils;
import core.placing.Placer;
import core.playing.AI;
import ui.UI;

/**
 * Runs simulations, execute the main method for doing so
 */
public class Runner {
	public static record WinnerEntry(Placer.Type redPlacement, Placer.Type bluePlacement, AI.Type redType, AI.Type blueType, boolean winner, long moves, long nanoTime) {};
	public static ArrayList<WinnerEntry> winList = new ArrayList<WinnerEntry>();
	public static UI ui;
	public static boolean ui_initialized = false; 
	public static boolean printGame;
	public static boolean printResults;
	public static boolean use_UI;

	public static void main(String[] args) {
		use_UI = false;		// UI only shows if simulation = 1
		printGame = false;
		printResults = false;
		
		int UI_delay = 100;
		Placer.Type redPlacement = Placer.Type.PREBUILT;
		Placer.Type bluePlacement = Placer.Type.PREBUILT;
		AI.Type bluePlayer = AI.Type.RANDOM;
		AI.Type redPlayer = AI.Type.RANDOM;
		int simulations = 1_000_000;

		simulate(simulations, redPlacement, redPlayer, bluePlacement, bluePlayer, UI_delay);
		printResults();
	}

	/**
	 * 
	 * @param repetitions number of games to play
	 * @param redPlacement placement algorithm red uses
	 * @param redType AI playing for red
	 * @param bluePlacement placement algorithm blue uses
	 * @param blueType AI playing for blue
	 * @param use_UI true if UI or the text interface should be shown
	 * @param delay in ms
	 */
	public static void simulate(int simulations, Placer.Type redPlacement, AI.Type redType, Placer.Type bluePlacement, AI.Type blueType, int delay) {
		if(simulations > 1) { if(use_UI) { printGame = true; printResults = true; } use_UI = false;}
		while(simulations-- > 0) {
			initUI();
			Piece[] redPieces = Placer.placePiecesWith(true, redPlacement);
			Piece[] bluePieces = Placer.placePiecesWith(false, bluePlacement);
			Mediator mediator = new Mediator(new GameState(redPieces, bluePieces));
			showGame(mediator, 0);
			AI red = redType.createAI(true, mediator.obfuscateFor(true));
			AI blue = blueType.createAI(false, mediator.obfuscateFor(false));

			long moves = 0;
			long simTime= 0;
			long startTime;
			long endTime;

			while(!mediator.isGameOver()) {
				Move move;
				if(mediator.getCurrentTeam() == red.getTeam()) {
					startTime = System.nanoTime();
					move = red.nextMove();
					endTime = System.nanoTime();
					simTime += endTime - startTime;
				} else {
					startTime = System.nanoTime();
					move = blue.nextMove();
					endTime = System.nanoTime();
					simTime += endTime - startTime;
				}
				if(!mediator.makeMove(move)) {
					// TODO wenn ein Piece angegriffen wird soll es nicht mehr obfuscated werden 
					System.err.println("error while executing move " + move + "\n" + Utils.fieldToString(mediator.getGameState().getField()));
					System.err.println(mediator.getGameState().isInChase() + " " + mediator.getGameState().getInChase() + " " + mediator.getGameState().getChasedFields().size() + "\n" +
							blue.gameState.isInChase() + " " + blue.gameState.getInChase() + " " + blue.gameState.getChasedFields().size() + " \n" +
							red.gameState.isInChase() + " " + red.gameState.getInChase() + " " + red.gameState.getChasedFields().size());
					break;
				}

				showGame(mediator, delay);

				moves++;

				red.update(mediator.obfuscateFor(true));
				blue.update(mediator.obfuscateFor(false));
			}

			showWinner(mediator, simTime);
			winList.add(new WinnerEntry(redPlacement, bluePlacement, redType, blueType, mediator.getWinnerTeam(), moves, simTime));
		}
	}

	public static void showWinner(Mediator mediator, long simTime) {
		if(printResults) 
			System.out.println(
					(mediator.getWinnerTeam() ? "Red" : "Blue") + 
					" won in " +
					new DecimalFormat("0.000").format(((simTime) * 0.000001)) +
					" ms");
		if(use_UI) {
			ui.showGameOver(mediator.getWinnerTeam());
		}
	}

	public static void showGame(Mediator mediator, int delay){
		if(printGame) {
			if(mediator.getLastMove() != null)
				System.out.println("\nMove: "+ mediator.getLastMove() + "\n");
			mediator.print();
		}
		if(use_UI) {
			ui.updateBoard(mediator.getGameState(), mediator.getLastMove());
			Utils.sleep(delay);
		} 
	}

	public static void initUI() {
		if(use_UI) {
			SwingUtilities.invokeLater(() -> {
				ui = new UI();
				ui_initialized = true;
			});
			while(use_UI && !ui_initialized) {Utils.sleep(1);};
		}
	}

	public static void printResults() {
		long time = 0;
		long moves = 0;
		long mostMoves = 0;
		long leastMoves = Long.MAX_VALUE;
		double winsRed = 0;
		double winsBlue = 0;
		for(WinnerEntry entry : winList) {
			time += entry.nanoTime;
			moves += entry.moves;
			mostMoves = mostMoves < entry.moves ? entry.moves : mostMoves;
			leastMoves = leastMoves > entry.moves ? entry.moves : leastMoves;
			if(entry.winner)
				winsRed++;
			else
				winsBlue++;
		}
		winList.sort((o1, o2) -> (int)(o1.nanoTime - o2.nanoTime));
		long median = winList.get(winList.size() / 2).nanoTime;
		System.out.println(
				"Played " + String.format("%,d", winList.size()) + " game" + (winList.size() > 1 ? "s" : "")+ 
				" in " +  String.format("%,d", (winList.stream().mapToLong(e -> e.nanoTime()).sum() / 1000000)) + "ms simulation time.\n" + 
						"red placed with " + winList.get(0).redPlacement + ", played as "+ winList.get(0).redType + "\n" +
						"blue placed with " + winList.get(0).bluePlacement + ", played as " + winList.get(0).blueType + "\n" +
						"Win rate red:  \t" + (winsRed / winList.size()) * 100 + "%\n" +
						"Win rate blue: \t" + (winsBlue / winList.size()) * 100 + "%\n" + 
						"Average time:  \t" + String.format("%,d", (time / winList.size())) + "ns\n" + 
						"Median time: \t" + String.format("%,d", median) +  "ns\n" + 
						"Average moves: \t" + (moves / winList.size() + " longest Game: " + mostMoves + ", shortest Game: " + leastMoves));

	}
}
