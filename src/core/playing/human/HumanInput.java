package core.playing.human;

import core.GameState;
import core.Move;
import core.playing.AI;
import ui.UI;

/**
 * Uses the UI to make human inputs
 */
public class HumanInput extends AI {

    public HumanInput(boolean player, GameState gameState) {
        super(player, gameState);
    }

    @Override
    public Move nextMove() {
        try {
            return UI.getHumanMove();
        } catch (InterruptedException e) {
            System.err.println("HumanInput interrupted while waiting for move. Returning null.");
            return null;
        }
    }
}