package core.playing;

import org.junit.jupiter.api.Test;

import core.GameState;
import core.Utils;
import core.placing.Placer;
import ui.UI;

class AITest {

	@Test
	void testAI() {
		GameState state = new GameState(Placer.placePiecesWith(true, Placer.Type.PREBUILT), Placer.placePiecesWith(false, Placer.Type.PREBUILT));
		AI ai = AI.Type.RANDOM.createAI(true, state);
		
		UI ui = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
		ui.updateBoard(state, null);

		UI ui2 = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
		ui2.updateBoard(ai.gameState, null);
		ui2.setTitle("Guessed");
		
		Utils.sleep(10000);
	}

}
