package core.playing.heuristic;

import core.GameState;
import core.Piece;
import core.PieceType;

public class TerminalHeuristic {
	public boolean useEnhancedKnownDeadMultipliers = true;
	
	public int knownMultiplicator;
	public int deadMultiplicator;

	public int generalFlagDefenderValue;
	public int bombNearFlagValue;
	public int flagDefenderDistance;
	public int generalFlagAttackerValue;
	public int mineurNearFlagValue;
	public int flagAttackerDistance;
	
	public int deadFlagValue;
	public int deadBombValue;
	public int deadSpyValueEnemyMarshallAlive;
	public int deadSpyValueEnemyMarshallDead;
	public int deadScoutValue;
	public int deadMineurValue;
	public int deadGeneralValue;
	public int deadMarshallValue;

	public int knownFlagValue;
	public int knownBombValue;
	public int knownSpyValueEnemyMarshallAlive;
	public int knownSpyValueEnemyMarshallDead;
	public int knownScoutValue;
	public int knownMineurValue;
	public int knownGeneralValue;
	public int knownMarshallValue;


	public TerminalHeuristic() {
		knownMultiplicator = 1;
		deadMultiplicator = 2;

		bombNearFlagValue = 3;
		generalFlagDefenderValue = 1;
		flagDefenderDistance = 2;
		mineurNearFlagValue = 3;
		generalFlagAttackerValue = 1;
		flagAttackerDistance = 3;

		deadFlagValue = 10000;
		deadBombValue = 2;
		deadSpyValueEnemyMarshallDead = 1;
		deadSpyValueEnemyMarshallAlive = 30;
		deadScoutValue = 3;
		deadMineurValue = 3;
		deadGeneralValue = 20;
		deadMarshallValue = 40;

		knownFlagValue = 100;
		knownBombValue = 6;
		knownSpyValueEnemyMarshallDead = 2;
		knownSpyValueEnemyMarshallAlive = 40;
		knownScoutValue = 6;
		knownMineurValue = 6;
		knownGeneralValue = 30;
		knownMarshallValue = 60;
	}


	/**
	 * Evaluates a score representing the players winning chances.
	 * @param state GameState to analyze
	 * @return an Integer the describes the game,
	 * 			>0:	team red got a better position
	 * 			<0: team blue got a better position
	 */
	public int evaluate(GameState state) {
		Piece[][] pieces = state.getPieces();
		if(pieces[0][9] == null) return Integer.MIN_VALUE;
		else if(pieces[1][9] == null) return Integer.MAX_VALUE;
		
		int red, blue;

		// teams having known and dead pieces is generally positive
		red = knownMultiplicator * state.getKnownBlue() + 
				deadMultiplicator * state.getDeadBlue();
		blue = knownMultiplicator * state.getKnownRed() + 
				deadMultiplicator * state.getDeadRed();

		if(useEnhancedKnownDeadMultipliers) {
			// adds the exact dead and known piece values to the score
			for(int i=0; i<10; i++) {
				if(pieces[0][i] == null) {
					// red dead pieces are good for blue
					blue += getDeadPieceValue(pieces[0], i);
				} else if(pieces[0][i].getKnown()) {
					// red known pieces are good for blue
					blue += getKnownPieceValue(pieces[0], i);
				}

				if(pieces[1][i] == null) {
					// blue dead pieces are good for red
					red += getDeadPieceValue(pieces[1], i);
				} else if (pieces[1][i].getKnown()) {
					// blue known pieces are good for red
					red += getKnownPieceValue(pieces[1], i);
				}
			}
		}
		
		red += flagDefenders(pieces[0], pieces[0][9]);
		red -= flagAttackers(pieces[1], pieces[0][9]);
		blue += flagDefenders(pieces[1], pieces[1][9]);
		blue -= flagAttackers(pieces[0], pieces[1][9]);

		return red - blue;
	}
	
	/**
	 * Rewards {@link #generalFlagDefenderValue} for every Piece besides the flag, if the Piece is a Bomb it rewards {@link #bombNearFlagValue}.
	 * The distance to the bomb is {@link #flagDefenderDistance}.
	 * @param pieces
	 * @param flag
	 * @return
	 */
	private int flagDefenders(Piece[] pieces, Piece flag) {
		int flagDefenders = 0;
		for(int i=0; i<9; i++) {
			if(pieces[i] == null)
				continue;
			if(manhattenDistance(pieces[i], flag) < flagDefenderDistance)
				if(pieces[i].getType() == PieceType.BOMBE)
					flagDefenders += bombNearFlagValue;
				else 
					flagDefenders += generalFlagDefenderValue;
		}
		return flagDefenders;
	}
	
	/**
	 * Rewards {@link #generalFlagAttackerValue} for every Piece besides the flag, if the Piece is a Mineur it rewards {@link #mineurNearFlagValue}.
	 * The distance to the bomb is {@link #flagAttackerDistance}.
	 * @param pieces
	 * @param flag
	 * @return
	 */
	private int flagAttackers(Piece[] pieces, Piece flag) {
		int flagAttackers = 0;
		for(int i=0; i<9; i++) {
			if(pieces[i] == null)
				continue;
			if(manhattenDistance(pieces[i], flag) < flagAttackerDistance)
				if(pieces[i].getType() == PieceType.MINEUR)
					flagAttackers += mineurNearFlagValue;
				else 
					flagAttackers += generalFlagAttackerValue;
		}
		return flagAttackers;
	}
	
	/**
	 * Returns the manhatten distance between piece and otherPiece
	 * @param piece
	 * @param otherPiece
	 * @return
	 */
	private int manhattenDistance(Piece piece, Piece otherPiece) {
	    return Math.abs(piece.getX() - otherPiece.getX()) + Math.abs(piece.getY() - otherPiece.getY());
	}

	private int getDeadPieceValue(Piece[] enemyPieces, int index) {
		switch(index) {
		case 0: return deadMarshallValue;
		case 1: return deadGeneralValue;
		case 2: return deadMineurValue;
		case 3: return deadMineurValue;
		case 4: return deadScoutValue;
		case 5: return deadScoutValue;
		case 6: return enemyPieces[0] == null ? deadSpyValueEnemyMarshallDead: deadSpyValueEnemyMarshallAlive;
		case 7: return deadBombValue;
		case 8: return deadBombValue;
		case 9: return deadFlagValue;
		default: return 0;
		}
	}

	private int getKnownPieceValue(Piece[] enemyPieces, int index) {
		switch(index) {
		case 0: return knownMarshallValue;
		case 1: return knownGeneralValue;
		case 2: return knownMineurValue;
		case 3: return knownMineurValue;
		case 4: return knownScoutValue;
		case 5: return knownScoutValue;
		case 6: return enemyPieces[0] == null ? knownSpyValueEnemyMarshallDead: knownSpyValueEnemyMarshallAlive;
		case 7: return knownBombValue;
		case 8: return knownBombValue;
		case 9: return knownFlagValue;
		default: return 0;
		}
	}
	
	public void disableEnhancedKnownDeadMultipliers() {
		useEnhancedKnownDeadMultipliers = false;
	}
}
