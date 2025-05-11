package core.playing.guesser;

import org.junit.jupiter.api.Test;

import core.Direction;
import core.GameState;
import core.Move;
import core.PieceType;
import core.Utils;
import core.placing.Placer;
import core.playing.AI;
import core.playing.AI.AIInformer;
import ui.UI;

class GuesserTest {

//	@Test
	void testGuesserStartState() {
		var RANKS = new PieceType[] {
//			PieceType.GENERAL, PieceType.SPIONIN, PieceType.MARSCHALL, PieceType.MINEUR, PieceType.SPAEHER, PieceType.BOMBE, PieceType.FLAGGE // worst
			PieceType.SPIONIN, PieceType.FLAGGE, PieceType.BOMBE, PieceType.MARSCHALL, PieceType.MINEUR, PieceType.GENERAL, PieceType.SPAEHER // best
//				PieceType.FLAGGE, PieceType.MARSCHALL, PieceType.SPIONIN, PieceType.BOMBE, PieceType.MINEUR, PieceType.GENERAL, PieceType.SPAEHER //2nd best
	};
		GameState state = new GameState(
				Placer.placePiecesWith(true, Placer.Type.BARRAGE),
				Placer.placePiecesWith(false, Placer.Type.BARRAGE));

		UI ui = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
		ui.updateBoard(state, null);
		ui.setTitle("ground truth");
		Guesser guesser = new Guesser(state.obfuscateFor(true), RANKS);
		UI ui2 = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
		ui2.updateBoard(guesser.converge(true), null);
		ui2.setTitle("guessed blue pieces: " + Math.round((guesser.accuracy(state, true)) * 100) + "%");
		
		guesser.print();
		
		Utils.sleep(100000);
		System.out.println(guesser.guess(state.getRedPieces()[9], guesser.getInGameRankCounts(true)));
	}

//	@Test
	void testUpdate() {
		GameState state = new GameState(
				Placer.placePiecesWith(true, Placer.Type.PREBUILT),
				Placer.placePiecesWith(false, Placer.Type.PREBUILT));
		
		GameState start = state.clone();
		
		Guesser guesser = new Guesser(state.obfuscateFor(true), null);
		Move move = new Move(state.getRedPieces()[4], Direction.UP, 2);
		
		UI ui3 = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
		ui3.updateBoard(guesser.converge(), null);
		ui3.setTitle("startstate guessed " + Math.round((guesser.accuracy(start, true)) * 100) + "%");
		
		Utils.execute(state, move);
		AIInformer informer = new AIInformer(state, move, false, null, null);
		guesser.update(informer);
		
		UI ui = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
		ui.updateBoard(state, null);
		ui.setTitle("ground truth");
		
		
		UI ui2 = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
		ui2.updateBoard(guesser.converge(), null);
		ui2.setTitle("guessed " + Math.round((guesser.accuracy(start, true)) * 100) + "%");
		

		UI ui4 = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
		ui4.updateBoard(guesser.convergeToStartState(0), null);
		ui4.setTitle("startstate guessed after update" + Math.round((guesser.accuracy(start, true)) * 100) + "%");
		
		guesser.print();
		
		Utils.sleep(100000);
	}
	
//	@Test
	void testUpdateAttack() {
		GameState state = new GameState(
				Placer.placePiecesWith(true, Placer.Type.PREBUILT),
				Placer.placePiecesWith(false, Placer.Type.PREBUILT));
		
		GameState start = state.clone();
		
		Guesser guesser = new Guesser(state.obfuscateFor(true), null);
		
//		UI ui3 = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
//		ui3.updateBoard(guesser.converge(), null);
//		ui3.setTitle("startstate guessed " + Math.round((guesser.accuracy(start, true) + guesser.accuracy(start, false)) * 100) / 2. + "%");
		
		Move move = new Move(state.getRedPieces()[3], Direction.UP, 1);
		Utils.execute(state, move);
		AIInformer informer = new AIInformer(state, move, false, null, null);
		guesser.update(informer);
		
		move = new Move(state.getBluePieces()[3], Direction.DOWN, 1);
		Utils.execute(state, move);
		informer = new AIInformer(state, move, false, null, null);
		guesser.update(informer);
		
		move = new Move(state.getRedPieces()[3], Direction.UP, 1);
		Utils.execute(state, move);
		informer = new AIInformer(state, move, true, PieceType.MINEUR, PieceType.MINEUR);
		UI ui = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
		ui.updateBoard(state, null);
		ui.setTitle("ground truth");
		
		
		guesser.update(informer);
		
		
		
		UI ui2 = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
		ui2.updateBoard(guesser.converge(), null);
		ui2.setTitle("guessed " + Math.round((guesser.accuracy(start, true)) * 100) + "%");
		

		UI ui4 = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
		ui4.updateBoard(guesser.convergeToStartState(1), null);
		ui4.setTitle("startstate guessed after update" + Math.round(guesser.accuracy(start, true) * 100) + "%");
		
		guesser.print();
		
		Utils.sleep(100000);
		System.out.println(guesser.guess(state.getRedPieces()[9], guesser.getInGameRankCounts(true)));
	}
}
