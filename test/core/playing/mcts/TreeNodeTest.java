package core.playing.mcts;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import core.GameState;
import core.placing.Placer;

class TreeNodeTest {

	@Test
	void test() {
		GameState state = new GameState(
				Placer.placePiecesWith(true, Placer.Type.PREBUILT),
				Placer.placePiecesWith(false, Placer.Type.PREBUILT));
		TreeNode node = new TreeNode(state, null, null);
		
//		System.out.println(node.bestChild());
	}

}
