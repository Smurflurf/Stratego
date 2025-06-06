package core.playing.mcts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.SplittableRandom;

import core.GameState;
import core.Move;
import core.Utils;
import core.playing.AI;
import core.playing.heuristic.MoveHeuristic;
import core.playing.heuristic.TerminalHeuristic;
import core.playing.random.RandomAI;


public class MCTS extends AI {
	protected static final SplittableRandom rand = new SplittableRandom();
	public TreeNode root;
	public TerminalHeuristic terminalHeuristic;
	public MoveHeuristic moveHeuristic;
	//	UI ui;
	public int simulationCounter;	
	public int heuristicCounter;
	public int expansionCounter;
	
	public static boolean printResultsToConsole = false;

	boolean enableBestChildNoLoops = false;
	boolean useExpandHeuristic = false;
	boolean useHeavyPlayout = false;
	boolean useHybridPlayout = false;
	boolean useCommonPlayout = true;
	float C = Constants.C;
	int maxSteps = Constants.MAX_STEPS;

	public MCTS(boolean team, GameState gameState, String ... guesserProbs) {
		super(team, gameState, guesserProbs);
		//		ui = new UI(null, null);
		root = new TreeNode(gameState.clone(), null, null);
		simulationCounter = 0;
		heuristicCounter = 0;
		expansionCounter = 0;

		terminalHeuristic = new TerminalHeuristic();
		//		terminalHeuristic.disableEnhancedKnownDeadMultipliers();
		moveHeuristic = new MoveHeuristic(guesser);
	}

	@Override
	public Move nextMove() {
		//		if(Mediator.stateGame != null)
		//		gameState = Mediator.stateGame;
		//		ui.updateBoard(gameState, lastMove);
		//		ui.setTitle("MCTS perspective " + (getTeam() ? " Red" : " Blue"));

		this.heuristicCounter = 0;
		this.expansionCounter = 0;
		this.simulationCounter = 0;

		long start = System.currentTimeMillis();
		long end = start + Constants.TIME_IN_MS;


		root = new TreeNode(gameState.clone(), null, null);

		while(System.currentTimeMillis() < end && simulationCounter + heuristicCounter < Constants.MAX_SIMULATIONS){
			//Schritte des UCT abarbeiten
			TreeNode selected = selectAndExpand(root);
			selected.backpropagate(simulate(selected, 0), C);
		}

		TreeNode bestChild;
		if(enableBestChildNoLoops)
			bestChild = bestChildNoLoops(root);
		else
			bestChild = bestRootChild(root);

		if (bestChild == null || bestChild.getMoveThatLedToThisNode() == null) {
			System.err.println("MCTS Warning: No best child found or move is null. Picking random valid move.");
			return RandomAI.nextMove(gameState);
		}

		if(printResultsToConsole)
			printResults(bestChild);
		System.gc();
		// TODO: -XX:+UseParallelGC, funktioniert am besten in dieser Umgebung

		return bestChild.getMoveThatLedToThisNode();
	}

	/**
	 * prints some interesting results to the console
	 * 
	 * @param bestChild
	 */
	void printResults(TreeNode bestChild) {
		System.out.println(
				"\t- - # # * *\t" + this.getClass().getCanonicalName() + (getTeam() ? " Red" : " Blue")
				+ "  :  "+ Math.round(bestChild.getV() * 100)
				+ "%\t* * # # - -\t"
				);
		System.out.println(
				"Knoten expandiert: " + expansionCounter +
				", Tiefe bester Pfad: " + bestPathDepth(root) +
				"\nSimulationen bis zum Ende: " + simulationCounter + 
				", Heuristik angewendet: " + heuristicCounter + 
				", Move: " + bestChild.getMoveThatLedToThisNode()
				);

		//		TreeNode[] children = root.getChildren().values().toArray(TreeNode[]::new);
		//		Arrays.sort(children, Comparator.comparingDouble(c -> ((TreeNode) c).getV()).reversed());
		//		for(int i=0; i<(children.length > 5 ? 5 : children.length); i++) {
		//				System.out.println(
		//						"\tchild "+ i + 
		//						" Gewinnchance: " + Math.round(children[i].getV()* 1000000)/10000. + 
		//						"% bei " + children[i].getNK() + " Spielen"
		//						);
		//		}

		System.out.println("\n");
	}

	/** 
	 * @return the chosen paths depth (best childs best child, ... , ... best child = null) 
	 */
	int bestPathDepth(TreeNode node) {
		int depth = 0;
		while(node != null) {
			depth ++;
			node = node.bestChild();
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
				return node.expand(useExpandHeuristic, moveHeuristic);
			} else {
				// Node is fully expanded, move down via UCT
				node = node.bestChild();
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
	boolean simulate(TreeNode simulateOn, int step){
		boolean isTerminal = simulateOn.isTerminal();
		TreeNode clone = null;

		if(!isTerminal)
			clone = simulateOn.simClone();
		else
			clone = simulateOn;

		while(!isTerminal && step++ < maxSteps) {
			oneMove(clone, pickField(clone));
			isTerminal = clone.isTerminal();
		}


		if(isTerminal) {
			switch(Utils.getWinner(clone.getGameState())) {

			case 0:	// red wins
				simulationCounter++;
				return true;
			case 1:	// blue wins
				simulationCounter++;
				return false;
			case 2:	// draw, win for my Team
				simulationCounter++;
				return !getTeam();
			default:
				heuristicCounter++;
				int score = terminalHeuristic.evaluate(simulateOn.getGameState());
				return score == 0 ? !root.getGameState().getTeam() : score > 0;			// TODO test behaviour

			}
		} else {
			heuristicCounter++;
			int score = terminalHeuristic.evaluate(simulateOn.getGameState());
			return score == 0 ? !root.getGameState().getTeam() : score > 0;			// TODO test behaviour
		}
	}

	/**
	 * Picks a Move while simulating.
	 * Configure heavy(heuristic) or light(random) move choosing with {@link #useHeavyPlayout}.
	 * @return a random Move
	 */	
	Move pickField(TreeNode simulateOn) {
		Move move;

		if(useCommonPlayout) {
			double evaluationNeighbors;
			double evaluationTarget;
			do {
				move = RandomAI.nextMove(simulateOn.getGameState());
				evaluationNeighbors = moveHeuristic.moveNeighborSafety(move, simulateOn.getGameState());
				evaluationTarget = moveHeuristic.evaluate(move, gameState);
			} while ((evaluationTarget == 0 &&
					((evaluationNeighbors) > 0.7 && evaluationNeighbors < 1))
					&& rand.nextInt(5) > 1);
		} else if(useHybridPlayout) {
			if(rand.nextInt(5) < 1)
				move = moveHeuristic.getBestMove(
						Utils.getAllPossibleMoves(
								simulateOn.getGameState()), 
						simulateOn.getGameState());
			else
				move = RandomAI.nextMove(simulateOn.getGameState());
		} else if(useHeavyPlayout)
			move = moveHeuristic.getBestMove(
					Utils.getAllPossibleMoves(
							simulateOn.getGameState()), 
					simulateOn.getGameState());
		else
			move = RandomAI.nextMove(simulateOn.getGameState());

	return move;
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
void oneMove(TreeNode parent, Move move) {
	Utils.execute(parent.getGameState(), move);
}	

/**
 * Returns the {@link #root} best child.
 * @param parent node
 * @return roots child that got visited the most
 */
TreeNode bestRootChild(TreeNode parent) {
	return root.getChildren().values().stream()
			.max(Comparator.comparingInt(TreeNode::getNK))
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

public void setMaxSteps(int maxSteps) {
	this.maxSteps = maxSteps;
}
public void setC(float C) {
	this.C = C;
}
public void useExpandHeuristic() {
	useExpandHeuristic = true;
}
public void enableBestChildNoLoops() {
	enableBestChildNoLoops = true;
}
public void useHeavyPlayout() {
	useHeavyPlayout = true;
}
public void useHybridPlayout() {
	useHeavyPlayout = true;
}
public void useCommonPlayout() {
	useCommonPlayout = true;
}
}
