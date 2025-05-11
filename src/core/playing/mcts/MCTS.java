package core.playing.mcts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.SplittableRandom;

import core.GameState;
import core.Move;
import core.PieceType;
import core.Utils;
import core.playing.AI;
import core.playing.random.RandomAI;
import ui.UI;


public class MCTS extends AI {
	protected static final SplittableRandom rand = new SplittableRandom();
	public TreeNode root;

	public int simulationCounter;	
	public int heuristicCounter;
	public int expansionCounter;


	public MCTS(boolean team, GameState gameState) {
		super(team, gameState);
		root = new TreeNode(gameState.clone(), null, null);
		simulationCounter = 0;
		heuristicCounter = 0;
		expansionCounter = 0;
	}

	@Override
	public Move nextMove() {
		long start = System.currentTimeMillis();
		long end = start + Constants.TIME_IN_MS;


		root = new TreeNode(gameState.clone(), null, null);

		while(System.currentTimeMillis() < end){
			//Schritte des UCT abarbeiten
			TreeNode selected = selectAndExpand(root);
			backpropagate(selected, simulate(selected, 0));
		}

//		TreeNode bestChild = bestChildNoLoops(root);
		TreeNode bestChild = bestRootChild(root);

		if (bestChild == null || bestChild.getMoveThatLedToThisNode() == null) {
			System.err.println("MCTS Warning: No best child found or move is null. Picking random valid move.");
			return RandomAI.nextMove(gameState);
		}

		printResults(bestChild.getMoveThatLedToThisNode());

		return bestChild.getMoveThatLedToThisNode();
	}

	/**
	 * prints some interesting results to the console
	 * 
	 * @param bestChild
	 */
	void printResults(Move move) {
		System.out.println(
				"\t- - # # * *\t" + this.getClass().getCanonicalName() 
				+ "  :  "+ Math.round(root.getWinRate() * 100)
				+ "%\t* * # # - -\t"
				);
		System.out.println(
				"Knoten expandiert: " + expansionCounter +
				", Tiefe bester Pfad: " + /*bestPathDepth(root) +*/
				"\nSimulationen bis zum Ende: " + simulationCounter + 
				", Heuristik angewendet: " + heuristicCounter + 
				", Move: " + move
				);
		/*for(int i=0; i<root.children.length; i++) {
			if(root.children[i] != null)
				System.out.println(
						"\tchild "+ i + 
						" Gewinnchance: " + Math.round(root.children[i].getV()* 1000000)/10000. + 
						"% bei " + root.children[i].getNK() + " Spielen"
						);
		}*/

		System.out.println("\n");
	}

	/** @return the chosen paths depth (best childs best child, ... , ... best child = null) */
	int bestPathDepth(TreeNode node) {
		int depth = 0;
		while(node != null) {
			depth ++;
			node = node.bestChild();
			node = this.bestRootChild(node); 
		}
		return depth;
	}


	/**
	 * Selects a node to simulate on using the UCBk formula.
	 * expands a children if a node in the chain has unexpanded ones.
	 * = "TreePolicy" from the pseudo-code
	 * @param parent node, from it on the nodes will be checked for one to simulate on
	 * @return the node to simulate on
	 */
	TreeNode selectAndExpand(TreeNode node){
		while (!node.isTerminal()) {
			if (!node.isFullyExpanded()) {
				expansionCounter++;
				return node.expand();
				//TODO might cause null pointers, check later
			} else {
				// Node is fully expanded, move down via UCT
				node = node.bestChild();
				// Handle case where selection might fail (e.g., all children terminal?)
				//TODO if thats the case check this later. removed handling.
			}
		}
		return node;
	}


	/**
	 * simulates a game from a specific node to finish (or a maximum step value of Constants.MAX_STEPS simulation),
	 * first checks if a node is in a terminal state, if thats the case the simulation ends and the result is returned
	 * @param the node from which a game is going to be simulated
	 * @return true if team A wins the simulation (either by getting more beans or team B having no moves left), 
	 * 		   false if team B wins the simulation (either by getting more beans or team A having no moves left)
	 * 		   default case is a heuristic. if it returns value > 0, team A is winning
	 */
	// TODO isTerminal nur 3
	boolean simulate(TreeNode simulateOn, int step){
		boolean isTerminal = isTerminal(simulateOn, false);
		TreeNode clone = null;

		if(!isTerminal)
			clone = simulateOn.simClone();
		else
			clone = simulateOn;

		while(!isTerminal && step++ < Constants.MAX_STEPS) {
			clone = oneMove(clone, pickField(clone), false);
			isTerminal = isTerminal(clone, false);
		}

		if(isTerminal) {
			simulationCounter++;
			switch(Utils.getWinner(clone.getGameState())) {
			case 0:
				simulationCounter++;
				return true;
			case 1:
				simulationCounter++;
				return false;
			case 2:
				simulationCounter++;
				return !getTeam();
			default:
				heuristicCounter++;
				int score = terminalHeuristic(simulateOn);
				return score == 0 ? !root.getGameState().getTeam() : score > 0;			// TODO test behaviour

			}
		} else {
			heuristicCounter++;
			int score = terminalHeuristic(simulateOn);
			return score == 0 ? !root.getGameState().getTeam() : score > 0;			// TODO test behaviour
		}
	}

	/**
	 * a heuristic to evaluate the winner of a given nodes gameState.
	 * the heuristics choice depends on the games phase: start-, middle- or end-game
	 * @param a node which will be analyzed
	 * @return an Integer the describes the game,
	 * 			>0:	team A got a better position
	 * 			<0: team B got a better position
	 * 
	 * TODO anpassen an eigene Heuristiken
	 */
	int terminalHeuristic(TreeNode node) {
		return 0;
	}


	/**
	 * Picks a (random) Move
	 * @return a random Move
	 */	
	// TODO anderes als Random
	Move pickField(TreeNode simulateOn) {
		Move move = RandomAI.nextMove(simulateOn.getGameState());
		if(move == null)
		{
			UI ui = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
			ui.updateBoard(simulateOn.getGameState(), move);
			System.out.println("err");
//			Utils.sleep(50000);
			RandomAI.nextMove(simulateOn.getGameState());
		}
			return move;
	}

	/**
	 * propagates the simulation result up the tree until the root element is reached
	 * @param node on which the simulation was executed
	 * @param winner of the simulation, winner == true if team A won
	 */
	void backpropagate(TreeNode child, boolean winner){
		while(child != null) {
			child.updateStats(winner);
			child = child.getParent();
		}
	}

	/**
	 * TODO optimise later
	 * checks if a game is in a terminal state.
	 * @param a node to check if it is terminal
	 * @param isExpanding true is the method gets called from expand. false if its called from simulate
	 * @return true if terminal
	 */
	public boolean isTerminal(TreeNode node, boolean isExpanding) {
		return Utils.isGameOver(node.getGameState());
	}

	/**
	 * Simulates one move and returns a new node containing the new state.
	 * also adds the new node to the parent nodes children, its place in the Array
	 * is the move made to get from the parent to the child (= field %6)
	 * @param parent node
	 * @param the move in form of the selected element in the parents array
	 * @param isExpanding true if oneMove gets called from the expand method. false if its from simulate. depending on that less calculations are made for simulations
	 * @return a child node containing the simulation result
	 */
	TreeNode oneMove(TreeNode parent, Move move, boolean isExpanding) {
		GameState clone = parent.getGameState().clone();
		if(!Utils.execute(clone, move.normalize(clone))) {
			UI ui = new UI(AI.Type.HUMAN, AI.Type.HUMAN);
			ui.updateBoard(clone, move);
			System.out.println("err");
			Utils.sleep(50000);
		}
		return new TreeNode(clone, parent, move);
	}	

	/**
	 * Returns the {@link #root} best child.
	 * @param parent node
	 * @return roots child that got visited the most
	 */
	TreeNode bestRootChild(TreeNode parent) {
		return root.getChildren().values().stream()
                .max(Comparator.comparingInt(TreeNode::getVisitCount))
                .orElse(null);
	}

	/**
	 * Returns the root best child. 
	 * Uses improved algorithms to prohibit infinity loops.
	 * Looks for the best children by V value, if the best children's win chances are less than 0.1% apart, 
	 * the child with the least best path depth gets chosen (= faster win).
	 * @param parent node
	 * @return the child node with the highest V-value 
	 */
	TreeNode bestChildNoLoops(TreeNode parent) {
		ArrayList<TreeNode> goodChildren = new ArrayList<TreeNode>();
		goodChildren.add(bestRootChild(parent));

		for(TreeNode node : parent.getChildren().values())
			if(node != goodChildren.get(0) 
			&& goodChildren.get(0).getV() - node.getV() < 0.001)
				goodChildren.add(node);

		goodChildren.sort((e1, e2) -> bestPathDepth(e1) - bestPathDepth(e2));
		return goodChildren.get(0);
	}
	
	
	/**
	 * Sets .
	 * @param state new GameState
	 */
	@Override
	public void setArraysAndGameState(GameState state) {
		super.setArraysAndGameState(state);
		this.heuristicCounter = 0;
		this.expansionCounter = 0;
		this.simulationCounter = 0;
	}
}
