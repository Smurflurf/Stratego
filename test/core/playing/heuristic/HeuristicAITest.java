package core.playing.heuristic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import core.GameState;
import core.Move;
import core.Piece;
import core.placing.Placer;

class HeuristicAITest {

	@Test
	void testCaptureFlag() {
		Piece[] red = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] blue = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(red, blue);
		HeuristicAI ai = new HeuristicAI(true, state);

		List<Move> moves = new ArrayList<Move>();
		Piece piece = ai.guesser.currentState.getRedPieces()[6];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 1, 7));
		piece = ai.guesser.currentState.getRedPieces()[5];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 7, 0));
		piece = ai.guesser.currentState.getRedPieces()[2];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 2, 7));
		piece = ai.guesser.currentState.getRedPieces()[1];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 5, 1));
		piece = ai.guesser.currentState.getRedPieces()[0];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 0, 6));

		piece = ai.guesser.currentState.getBluePieces()[1];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 3, 4));
		piece = ai.guesser.currentState.getBluePieces()[0];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 4, 2));

		ai.guesser.currentState.removePiece(ai.guesser.currentState.getRedPieces()[4]);
		ai.guesser.currentState.removePiece(ai.guesser.currentState.getRedPieces()[3]);

		ai.guesser.currentState.removePiece(ai.guesser.currentState.getBluePieces()[6]);
		ai.guesser.currentState.removePiece(ai.guesser.currentState.getBluePieces()[5]);
		ai.guesser.currentState.removePiece(ai.guesser.currentState.getBluePieces()[4]);
		ai.guesser.currentState.removePiece(ai.guesser.currentState.getBluePieces()[3]);
		ai.guesser.currentState.removePiece(ai.guesser.currentState.getBluePieces()[2]);

		ai.gameState = ai.guesser.currentState;

		for(Move move : moves)
			ai.guesser.currentState.move(move);

		Move move = ai.nextMove();
		assertEquals(ai.guesser.currentState.getBluePieces()[9].getPos(), move.getEnd());
	
	}

}
