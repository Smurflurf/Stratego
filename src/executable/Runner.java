package executable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import core.GameState;
import core.Mediator;
import core.Move;
import core.Piece;
import core.Utils;
import core.placing.Placer;
import core.playing.AI;
import core.playing.mcts.MCTS;
import ui.UI;

/**
 * Runs simulations, execute the main method for doing so
 */
public class Runner {
	public static record WinnerEntry(
			Placer.Type redPlacement, 
			Placer.Type bluePlacement, 
			AI.Type redType, 
			AI.Type blueType, 
			int winner, 
			int moves,
			HashMap<Integer, Double> redOnMoveGuessingAccuracy, 
			HashMap<Integer, Double> blueOnMoveGuessingAccuracy, 
			long nanoTime
			) {};
	public static ArrayList<WinnerEntry> winList = new ArrayList<WinnerEntry>();
	public static UI ui;
	public static boolean ui_initialized = false; 
	public static boolean printGame;
	public static boolean printResults;
	public static boolean use_UI;

	public static void main(String[] args) {
		use_UI = true;		// UI only shows if simulation = 1. If true but simulations > 1 the game is printed onto console
		printGame = false;
		printResults = false;
		int UI_delay = 1000;
		
		Placer.Type redPlacement = Placer.Type.BARRAGE;
		Placer.Type bluePlacement = Placer.Type.DEBOER;
		AI.Type redPlayer = AI.Type.HUMAN;
		AI.Type bluePlayer = AI.Type.MCTS;
		int simulations = 1;
		
		MCTS.printResultsToConsole = true;

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
		winList.clear();
		if(simulations > 1) { if(use_UI) { printGame = true; printResults = true; } use_UI = false;}
		long start = System.currentTimeMillis();
		while(simulations-- > 0) {
			if (use_UI && !ui_initialized) {
				initUI(redType, blueType);
            }
			
			Piece[] redPieces = Placer.placePiecesWith(true, redPlacement);
			Piece[] bluePieces = Placer.placePiecesWith(false, bluePlacement);
			Mediator mediator = new Mediator(new GameState(redPieces, bluePieces));
			mediator.getGameState().changeTeam();
			showGame(mediator, 0);
			AI red = redType.createAI(true, mediator.obfuscateFor(true));
			AI blue = blueType.createAI(false, mediator.obfuscateFor(false));
			
//			((HeuristicAI)blue).moveHeuristic.disableUseTargetNeighborCheck();
//			((MCTS)red).terminalHeuristic.disableEnhancedKnownDeadMultipliers();
//			((MCTS)red).useHeavyPlayout();
//			((MCTS)red).useCommonPlayout();
			
			HashMap<Integer, Double> redOnMoveGuessingAccuracy = new HashMap<Integer, Double>();
			HashMap<Integer, Double> blueOnMoveGuessingAccuracy = new HashMap<Integer, Double>();

			int moves = 0;
			long simTime= 0;
			long startTime;
			long endTime;
			
			if(use_UI)
				Utils.sleep(delay);
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
				
				redOnMoveGuessingAccuracy.put(moves, red.guesser.accuracy(mediator.getStartState(), red.getTeam()));
				blueOnMoveGuessingAccuracy.put(moves, blue.guesser.accuracy(mediator.getStartState(), blue.getTeam()));
//				System.out.println(moves + " red: " +  redOnMoveGuessingAccuracy.get(moves) + " " + red.guesser.currentState.getKnownRed() + " " + red.guesser.currentState.getKnownBlue());
//				System.out.println(moves + " blue: " +  blueOnMoveGuessingAccuracy.get(moves) + " " + red.guesser.currentState.getKnownRed() + " " + red.guesser.currentState.getKnownBlue());
				
				if(!mediator.makeMove(move)) {
					System.err.println("error while executing move " + move + "\n" + Utils.fieldToString(mediator.getGameState().getField()));
					System.err.println(mediator.getGameState().isInChase() + " " + mediator.getGameState().getInChase() + " " + mediator.getGameState().getChasedFields().size() + "\n" +
							blue.gameState.isInChase() + " " + blue.gameState.getInChase() + " " + blue.gameState.getChasedFields().size() + " \n" +
							red.gameState.isInChase() + " " + red.gameState.getInChase() + " " + red.gameState.getChasedFields().size());
					System.exit(0);
					break;
				} else if (moves > 600) {
//					System.err.println("Game going for " + moves + " moves, stopped.");
					break;
				} 

				showGame(mediator, delay);
				
				moves++;
				red.update(mediator.getAIInformer(true));
				blue.update(mediator.getAIInformer(false));
			}

			showWinner(mediator, simTime);
			winList.add(new WinnerEntry(
					redPlacement, 
					bluePlacement, 
					redType, 
					blueType, 
					mediator.getWinnerTeam(), 
					moves,
					redOnMoveGuessingAccuracy, 
					blueOnMoveGuessingAccuracy,
					simTime));
			System.out.println(simulations + " sims left, winner: " + Utils.getWinner(mediator.getGameState()));
		}
		long end = System.currentTimeMillis();
		System.out.println("total time: " + (end - start) + "ms");
	}

	public static void showWinner(Mediator mediator, long simTime) {
		if(printResults) 
			System.out.println(
					(mediator.getWinnerTeam() == 0 ? "Red" : 
						(mediator.getWinnerTeam() == 1 ? "Blue" : "nobody, it's a draw.")) + 
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

	public static void initUI(AI.Type red, AI.Type blue) {
		if(use_UI) {
			SwingUtilities.invokeLater(() -> {
				ui = new UI(red, blue);
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
		double draws = 0;
		double stopped = 0;
		for(WinnerEntry entry : winList) {
			time += entry.nanoTime;
			moves += entry.moves;
			mostMoves = mostMoves < entry.moves ? entry.moves : mostMoves;
			leastMoves = leastMoves > entry.moves ? entry.moves : leastMoves;
			if(entry.winner == 0)
				winsRed++;
			else if (entry.winner == 1)
				winsBlue++;
			else if (entry.winner == 2)
				draws++;
			else
				stopped++;
		}
		try {
		winList.sort((o1, o2) -> ((int)(o1.nanoTime/1000.) - (int)(o2.nanoTime/100.)));
		} catch (Exception e) {
			System.err.println("winList.sort() Error in Runner. Median won't be correct.");
		}
		long median = winList.get(winList.size() / 2).nanoTime;
		System.out.println(
				"Played " + String.format("%,d", winList.size()) + " game" + (winList.size() > 1 ? "s" : "")+ 
				" in " +  String.format("%,d", (winList.stream().mapToLong(e -> e.nanoTime()).sum() / 1000000)) + "ms simulation time.\n" + 
						"red placed with " + winList.get(0).redPlacement + ", played as "+ winList.get(0).redType + "\n" +
						"blue placed with " + winList.get(0).bluePlacement + ", played as " + winList.get(0).blueType + "\n" +
						"Win rate red:  \t" + (winsRed / (winsRed+winsBlue+draws)) * 100 + "%\n" +
						"Win rate blue: \t" + (winsBlue / (winsRed+winsBlue+draws)) * 100 + "%\n" + 
						"Draws: \t" + (draws / (winsRed+winsBlue+draws)) * 100 + "%\n" + 
						"Stopped: \t" + (stopped / winList.size()) * 100 + "%\n" + 
						"Average time:  \t" + String.format("%,d", (time / winList.size())) + "ns\n" + 
						"Median time: \t" + String.format("%,d", median) +  "ns\n" + 
						"Average moves: \t" + (moves / winList.size() + " longest Game: " + mostMoves + ", shortest Game: " + leastMoves));

	}
}
