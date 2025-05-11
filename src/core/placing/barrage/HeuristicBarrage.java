package core.placing.barrage;

import core.placing.deboer.HeuristicDeBoer;
import strados2.tools.NeighborIO;

public class HeuristicBarrage extends HeuristicDeBoer {

	public HeuristicBarrage(boolean team) {
		super(team,
				strados2.tools.CompressedMapIO.loadCompressedMaps("barrage"),
				NeighborIO.loadNeighborCounts("barrage"));
		
		useNeighbors = true;
		useControlHeuristic = true;
	}
}
