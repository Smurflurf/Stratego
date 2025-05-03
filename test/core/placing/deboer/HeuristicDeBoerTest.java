package core.placing.deboer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import core.ByteMapper;
import core.Piece;
import core.PieceType;
import strados2.tools.HeatMapGenerator;

@SuppressWarnings("unused")
class HeuristicDeBoerTest {
	HeuristicDeBoer heuristic;
	
	@Test
	void testGetDistribution() {
		Piece flag = new Piece(PieceType.FLAGGE, true);
		int[][] flagDistrib = heuristic.getDistribution(flag);
		flag.setPos(ByteMapper.toByte(0, 0));
		heuristic.pieces[9] = flag;
		

		Piece bomb = new Piece(PieceType.BOMBE, true);
		int[][] bombDistrib = heuristic.getDistribution(bomb);
		bomb.setPos(ByteMapper.toByte(2, 1));
		heuristic.pieces[8] = bomb;
		
		Piece bomb2 = new Piece(PieceType.BOMBE, true);
		int[][] bombDistrib2 = heuristic.getDistribution(bomb2);
		
//		HeatMapGenerator.createHeatmapChart(bombDistrib2, "bombDistrib2", "bombDistrib2");
		
	}

	 @BeforeEach
	 void init() {
		 heuristic = new HeuristicDeBoer(true);
	 }
}
