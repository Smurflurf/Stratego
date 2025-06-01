package core.playing.heuristic;

import java.util.Comparator;

import core.GameState;
import core.Move;
import core.Utils;
import core.playing.AI;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class HeuristicAI extends AI{
	boolean useTerminalHeuristic = true;
	boolean useMoveHeuristic = true;
	
	public TerminalHeuristic terminalHeuristic;
	public MoveHeuristic moveHeuristic;
	
	
	public HeuristicAI(boolean team, GameState gameState) {
		super(team, gameState);
		terminalHeuristic = new TerminalHeuristic();
		moveHeuristic = new MoveHeuristic(guesser);
	}

	@Override
	public Move nextMove() {
		Object2ObjectOpenHashMap<Move, Integer> evaluated = new Object2ObjectOpenHashMap<Move, Integer>();
		ObjectArrayList<Move> moves = Utils.getAllPossibleMoves(gameState);
		
		for(Move move : moves) {
			evaluated.put(move, 0);
		}
		
		if(useTerminalHeuristic) {
			for(Move move : moves) {
				GameState executedOnCloneState = gameState.clone();
				Move normalized = move.normalize(executedOnCloneState);
				Utils.execute(executedOnCloneState, normalized);
				int oldValue = evaluated.remove(move);
				evaluated.put(move, oldValue + terminalHeuristic.evaluate(executedOnCloneState));
			}
		}

		if(useMoveHeuristic) {
			for(Move move : moves) {
//				System.out.println(evaluated.get(move) + " "  + moveHeuristic.evaluate(move, gameState));
				int oldValue = evaluated.remove(move) + moveHeuristic.evaluate(move, gameState);
				evaluated.put(move, oldValue);
			}
		}
		
		Move bestMove = moves.get(0);
		int bestScore = evaluated.get(bestMove);
		for(Move move : moves) {
			if(betterThan(!getTeam(), bestScore, evaluated.get(move))) {
				bestScore = evaluated.get(move);
				bestMove = move;
			}
		}
//		System.out.println(bestScore + " " + bestMove);
		
//		if(!getTeam())
//			moves.sort(Comparator.comparingInt(evaluated::get));
//		else 
//			moves.sort(Comparator.comparingInt(evaluated::get).reversed());

		return bestMove;
	}
	
	private boolean betterThan(boolean player, int bestScore, int newScore) {
		if(player)
			return newScore > bestScore;
		else
			return newScore < bestScore;
	}
	
	public void disableTerminalHeuristic() {
		useTerminalHeuristic = false;
	}
	public void disableMoveHeuristic() {
		useMoveHeuristic = false;
	}
}
