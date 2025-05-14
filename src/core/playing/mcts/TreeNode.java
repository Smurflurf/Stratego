package core.playing.mcts;

import core.GameState;
import core.Move;
import core.Utils;
import core.playing.AI;
import ui.UI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TreeNode {
	private int winsP1;
	private int winsP2;
	private int visitCount;
	private float uct;

	private GameState gameState;
	private TreeNode parent;
	private Map<Move, TreeNode> children;
	private Move moveThatLedToThisNode;
	private ArrayList<Move> untriedMoves;


	/**
	 * New Node constructor.
	 * @param gameState with moveThatLedToThisNode already executed
	 * @param parent parent Node
	 * @param moveThatLedToThisNode self explanatory
	 */
	public TreeNode(GameState gameState, TreeNode parent, Move moveThatLedToThisNode) {
		this.gameState = gameState;
		this.parent = parent;
		this.moveThatLedToThisNode = moveThatLedToThisNode;
		this.children = new HashMap<>();
		this.untriedMoves = new ArrayList<>(Arrays.asList(Utils.getAllPossibleMoves(gameState)));
		this.winsP1 = 1;
		this.winsP2 = 1;
		this.visitCount = 2;
		this.uct = Float.MAX_VALUE;
	}

	private TreeNode(GameState gameState) {
		this.gameState = gameState;
	}

	public TreeNode simClone() {
		return new TreeNode(gameState.clone());
	}

	/**
	 * Selects the best child node based on the UCT formula.
	 * @return The best child node to explore further.
	 */
	public TreeNode bestChild() {
		TreeNode bestChild = null;
		float bestValue = Float.NEGATIVE_INFINITY;
		for (TreeNode child : children.values()) {
			float uctValue = child.getUCT();
			if (uctValue > bestValue) {
				bestValue = uctValue;
				bestChild = child;
			}
		}
		
		return bestChild;
	}

	/**
	 * Expands the current node by creating one child node for a randomly chosen untried move.
	 * @return newly expanded child node
	 */
	public TreeNode expand() {
		GameState nextState = gameState.clone();
		Move move = untriedMoves.remove(MCTS.rand.nextInt(untriedMoves.size())).normalize(nextState);

		if(!Utils.execute(nextState, move)) {
			UI ui = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
			ui.updateBoard(nextState, move);
			System.out.println("Error in TreeNode.expand: " + move);
			Utils.sleep(50000);
		}

		TreeNode childNode = new TreeNode(nextState, this, move);
		children.put(move, childNode); 
		
		return childNode;
	}

	/**
	 * Updates this node's statistics during backpropagation.
	 * @param winnerTeam True if Red won the simulation, False if Blue won.
	 */
	public void updateStats(boolean winner) {
		if(winner) {
			winsP1++;
		} else {
			winsP2++;
		}
		visitCount++;
		updateUct();
	}

	public GameState getGameState() {
		return gameState;
	}

	public TreeNode getParent() {
		return parent;
	}

	public Map<Move, TreeNode> getChildren() {
		return children;
	}

	public Move getMoveThatLedToThisNode() {
		return moveThatLedToThisNode;
	}

	public ArrayList<Move> getUntriedMoves() {
		return untriedMoves;
	}

	public boolean isFullyExpanded() {
		return untriedMoves.isEmpty();
	}

	public boolean isTerminal() {
		return Utils.isGameOver(gameState);
	}

	public int getVisitCount() {
		return visitCount;
	}

	/** 
	 * @return total simulations played from this node
	 */
	public int getNK() {
//		return winsP1 + winsP2;
		return visitCount;
	}

	/** 
	 * returns V value (win rate) for UCT depending on the player.
	 * @return V value for UCT
	 */
	public float getV() {
		return !gameState.getTeam() ? (float)winsP1 / getNK() : (float)winsP2 / getNK();
	}

	/**
	 * @return returns the UCT value of the current node
	 */
	public float getUCT() {
		return this.uct;
	}

	/**
	 * prints the node and its important attributes to the console
	 */
	public void printMe(String s) {
		System.out.println("---*\t" + s + "\t*---");
		System.out.println("\n" + "wins Player A: " + getWinsP1() + ", wins Player B: " + getWinsP2());
		System.out.print("nk: " + getNK());
		if(parent != null && getNK() != 0) {
			System.out.println(", np " + parent.getNK()+ ", UCT: " + getUCT());
		}
		System.out.println("\n---*\t\t*---");
	}

	public int getWinsP1() {
		return winsP1;
	}

	public int getWinsP2() {
		return winsP2;
	}

	private void updateUct() {
		if(parent != null) {
			float nk = getNK();
			float v = !gameState.getTeam() ? winsP1 / nk : winsP2 / nk;
			uct = v + Constants.C * (float)Math.sqrt((float)Math.log(parent.getNK()) / nk);
		}
	}
	
	}