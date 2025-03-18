import java.text.DecimalFormat;
import java.util.ArrayList;

import core.GameState;
import core.Move;
import core.Piece;
import core.placing.Placer;
import core.playing.AI;

/**
 * Runs simulations, execute the main method for doing so
 */
public class Runner {
	public static record WinnerEntry(boolean winner, long nanoTime) {};
	public static ArrayList<WinnerEntry> winList = new ArrayList<WinnerEntry>();
	
	public static void main(String[] args) {
		simulate(1, Placer.Type.PREBUILT, AI.Type.RANDOM, Placer.Type.PREBUILT, AI.Type.RANDOM);
		printResults();
	}

	public static void simulate(int repetitions, Placer.Type redPlacement, AI.Type redType, Placer.Type bluePlacement, AI.Type blueType) {
		while(repetitions-- > 0) {
			Piece[] redPieces = Placer.placePiecesWith(true, redPlacement);
			Piece[] bluePieces = Placer.placePiecesWith(false, bluePlacement);
			Mediator mediator = new Mediator(new GameState(redPieces, bluePieces));
			mediator.print();
			AI red = redType.createAI(true, mediator.obfuscateFor(true));
			AI blue = blueType.createAI(false, mediator.obfuscateFor(false));
			
			long startTime = System.nanoTime();
//			while(!mediator.isGameOver()) {
				Move move;
				if(mediator.getCurrentTeam() == red.getTeam())
					move = red.nextMove();
				else
					move = blue.nextMove();
				if(!mediator.makeMove(move)) {
					System.err.println();
					try{Thread.sleep(1000);}catch(Exception e) {};
					continue;
				}
				
				System.out.println("\nMove: "+ move + "\n");
				red.update(move);
				blue.update(move);
				mediator.print();
//			}
			long endTime = System.nanoTime();

			System.out.println(
					(mediator.getCurrentTeam() ? "Red" : "Blue") + 
					" won in " +
					new DecimalFormat("0.000").format(((endTime - startTime) * 0.000001)) +
					" ms");
			winList.add(new WinnerEntry(mediator.getWinnerTeam(), endTime - startTime));
		}
	}
	
	public static void printResults() {
		long time = 0;
		int winsRed = 0;
		int winsBlue = 0;
		for(WinnerEntry entry : winList) {
			time += entry.nanoTime;
			if(entry.winner)
				winsRed++;
			else
				winsBlue++;
		}
		System.out.println(
				"\nWin rate red:  " + (winsRed / winList.size()) * 100 + "%\n" +
				"Win rate blue: " + (winsBlue / winList.size()) * 100 + "%\n" + 
				"Average time:  " + (time / winList.size()) + "ns");
		
	}
}
