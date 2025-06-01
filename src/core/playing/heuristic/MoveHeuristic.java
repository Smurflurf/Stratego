package core.playing.heuristic;

import core.ByteMapper;
import core.Direction;
import core.GameState;
import core.Move;
import core.Piece;
import core.PieceType;
import core.playing.guesser.Guesser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MoveHeuristic {
	private boolean useTargetCheck = true;
	private boolean useTargetNeighborCheck = true;
	private boolean useTargetUncertainty = false;
	private boolean useNeighborUncertainty = true;

	private Guesser guesser;
	private Object2ObjectOpenHashMap<PieceType, Integer> rankMap;

	public MoveHeuristic(Guesser guesser) {
		this.guesser = guesser;
		rankMap = new Object2ObjectOpenHashMap<PieceType, Integer>();
		for(int i=0; i<guesser.RANKS.length; i++)
			rankMap.put(guesser.RANKS[i], i);
	}

	/**
	 * Returns the best Move on GameState from a list of moves
	 */
	public Move getBestMove(ObjectArrayList<Move> moves, GameState state) {
		Object2ObjectOpenHashMap<Move, Integer> evaluated = new Object2ObjectOpenHashMap<Move, Integer>();

		for(Move move : moves) {
			evaluated.put(move, evaluate(move, state));
		}

		Move bestMove = moves.get(0);
		int bestScore = evaluated.get(bestMove);
		for(Move move : moves) {
			if(betterThan(state.getTeam(), bestScore, evaluated.get(move))) {
				bestScore = evaluated.get(move);
				bestMove = move;
			}
		}

		return moves.get(0);
	}

	public int evaluate(Move move, GameState state) {


		int manhattenDistancePoints = (int)
				(14 - manhattenDistance(
						move.getEndX(), 
						move.getEndY(), 
						state.getPieces()[move.getPiece().getTeam() ? 1 : 0][9].getPos()));

		if(useTargetCheck)
			manhattenDistancePoints *= moveFieldSafety(move, state.inspect(move.getEndX(), move.getEndY()));
		if(useTargetNeighborCheck)
			manhattenDistancePoints *= moveNeighborSafety(move, state);


		return (move.getPiece().getTeam() ? -1 : 1) * (int)(manhattenDistancePoints);
	}

	public double moveFieldSafety(Move move, Piece moveTo) {
		double safetyMult = 1F;
		if(moveTo != null) {
			if(move.getPiece().attack(moveTo) == moveTo)
				if(useTargetUncertainty)
					safetyMult *= 1 + certainty(moveTo);	
				else
					safetyMult *= 2;
			else
				if(useTargetUncertainty)
					safetyMult *= 1-certainty(moveTo);	
				else
					safetyMult = 0;
		}
		return safetyMult;
	}

	public double moveNeighborSafety(Move move, GameState state) {
		double stronger = 1;
		double weaker = 1;
		for(Direction dir : Direction.values()) {
			byte dirPos = dir.translate(move.getEnd(), 1);
			int x = ByteMapper.getX(dirPos);
			int y = ByteMapper.getY(dirPos);
			if(x<0 || x>7 || y<0 || y>7) continue;

			Piece attacker = state.inspect(x, y);

			if(attacker != null && attacker != move.getPiece()) {
				if(move.getPiece().attack(attacker) == attacker)
					if(useNeighborUncertainty)
						weaker *= 1+certainty(attacker);	
					else
						weaker += 1;

				else
					if(useNeighborUncertainty)
						stronger *= 1 - certainty(attacker);	
					else
						return 0;
			}
		}
		return stronger < 1 ? stronger : weaker;
	}

	private double certainty(Piece piece) {
		return guesser.getPieceProbsFromStartPos(piece.getStartPos())[this.rankMap.get(piece.getType())];
	}

	/**
	 * Returns the manhatten distance between piece and otherPiece
	 * @param piece
	 * @param otherPiece
	 * @return
	 */
	private int manhattenDistance(int x1, int y1, byte flagPos) {
		return Math.abs(x1 - ByteMapper.getX(flagPos)) + Math.abs(y1 - ByteMapper.getY(flagPos));
	}

	private boolean betterThan(boolean player, int bestScore, int newScore) {
		if(player)
			return newScore > bestScore;
			else
				return newScore < bestScore;
	}

	public void enableNeighborUncertainty() {
		useNeighborUncertainty = true;
	}
	public void enableTargetUncertainty() {
		useTargetUncertainty = true;
	}
	public void disableUseTargetCheck() {
		useTargetCheck = false;
	}
	public void disableUseTargetNeighborCheck() {
		useTargetNeighborCheck = false;
	}
}
