package core.playing.heuristic;

import java.util.Comparator;

import core.GameState;
import core.Move;
import core.Utils;
import core.playing.AI;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class HeuristicAI extends AI{
	Heuristic terminalHeuristic;
	
	public HeuristicAI(boolean team, GameState gameState) {
		super(team, gameState);
		terminalHeuristic = new Heuristic();
	}

	@Override
	public Move nextMove() {
		Object2ObjectOpenHashMap<Move, Integer> evaluated = new Object2ObjectOpenHashMap<Move, Integer>();
		ObjectArrayList<Move> moves = Utils.getAllPossibleMoves(gameState);
		
		for(Move move : moves) {
			GameState executedOnCloneState = gameState.clone();
			Move normalized = move.normalize(executedOnCloneState);
			Utils.execute(executedOnCloneState, normalized);
			evaluated.put(move, terminalHeuristic.evaluate(executedOnCloneState));
		}
		
		
		Move bestMove = moves.get(0);
		int bestScore = evaluated.get(bestMove);
		for(Move move : moves) {
			if(betterThan(getTeam(), bestScore, evaluated.get(move))) {
				bestScore = evaluated.get(move);
				bestMove = move;
			}
		}
		
//		if(getTeam())
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
	
	
}
