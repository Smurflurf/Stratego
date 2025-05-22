package core.playing.mcts;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import core.GameState;
import core.Move;
import core.Piece;
import core.Utils;
import core.placing.Placer;
import ui.UI;

class MCTSTest {

	@Test
	void test() {
		Piece[] red = Placer.placePiecesWith(true, Placer.Type.PREBUILT);
		Piece[] blue = Placer.placePiecesWith(false, Placer.Type.PREBUILT);
		GameState state = new GameState(red, blue);
		MCTS mcts = new MCTS(true, state);

		List<Move> moves = new ArrayList<Move>();
		Piece piece = mcts.guesser.currentState.getRedPieces()[6];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 1, 7));
		piece = mcts.guesser.currentState.getRedPieces()[5];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 7, 0));
		piece = mcts.guesser.currentState.getRedPieces()[2];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 2, 7));
		piece = mcts.guesser.currentState.getRedPieces()[1];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 5, 1));
		piece = mcts.guesser.currentState.getRedPieces()[0];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 0, 6));
		
		piece = mcts.guesser.currentState.getBluePieces()[1];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 3, 4));
		piece = mcts.guesser.currentState.getBluePieces()[0];
		moves.add(new Move(piece, piece.getX(), piece.getY(), 4, 2));
		
		mcts.guesser.currentState.removePiece(mcts.guesser.currentState.getRedPieces()[4]);
		mcts.guesser.currentState.removePiece(mcts.guesser.currentState.getRedPieces()[3]);
		
		mcts.guesser.currentState.removePiece(mcts.guesser.currentState.getBluePieces()[6]);
		mcts.guesser.currentState.removePiece(mcts.guesser.currentState.getBluePieces()[5]);
		mcts.guesser.currentState.removePiece(mcts.guesser.currentState.getBluePieces()[4]);
		mcts.guesser.currentState.removePiece(mcts.guesser.currentState.getBluePieces()[3]);
		mcts.guesser.currentState.removePiece(mcts.guesser.currentState.getBluePieces()[2]);
		
		mcts.gameState = mcts.guesser.currentState;
		
//		red[4] = null;
//		red[3] = null;

//		blue[6] = null;
//		blue[5] = null;
//		blue[4] = null;
//		blue[3] = null;
//		blue[2] = null;
		
		for(Move move : moves)
			mcts.guesser.currentState.move(move);
		
//		mcts.ui.updateBoard(mcts.gameState, null);
//		mcts.nextMove();
//		Utils.sleep(10000);
	}

}
