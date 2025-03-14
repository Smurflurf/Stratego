import java.text.DecimalFormat;
import java.util.ArrayList;

import core.Move;
import core.ai.AI;

/**
 * Runs simulations, execute the main method for doing so
 */
public class Runner {
	public static record WinnerEntry(boolean winner, long nanoTime) {};
	public static ArrayList<WinnerEntry> winList = new ArrayList<WinnerEntry>();
	
	public static void main(String[] args) {
		simulate(1, AI.Type.RANDOM, AI.Type.RANDOM);
		printResults();
	}
	
	public static void simulate(int repetitions, AI.Type redType, AI.Type blueType, int ... startState) {
		Mediator mediator = new Mediator(StartStates.generateStartState(startState));
		
		AI red = redType.createAI(true, mediator.obfuscateFor(true));
		AI blue = blueType.createAI(false, mediator.obfuscateFor(false));
		
		long startTime = System.nanoTime();
 		while(!mediator.isGameOver()) {
			Move move;
			if(mediator.getCurrentTeam() == red.getTeam())
				move = red.nextMove();
			else
				move = blue.nextMove();
			mediator.makeMove(move);
		}
		long endTime = System.nanoTime();
		
		System.out.println(
				(mediator.getCurrentTeam() ? "Red" : "Blue") + 
				" won in " +
				new DecimalFormat("0.000").format(((endTime - startTime) * 0.000001)) +
				" ms");
		winList.add(new WinnerEntry(mediator.getWinnerTeam(), endTime - startTime));
	}
	
	public static void printResults() {
		
	}
}
