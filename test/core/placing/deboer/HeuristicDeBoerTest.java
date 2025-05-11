package core.placing.deboer;

import static org.junit.jupiter.api.Assertions.*;

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
	void testPlacementOK() {
		Piece flag = new Piece(PieceType.FLAGGE, true);
		Piece spy = new Piece(PieceType.SPIONIN, true);
		Piece marschall = new Piece(PieceType.MARSCHALL, true);
		Piece general = new Piece(PieceType.GENERAL, true);
		Piece spaeher = new Piece(PieceType.SPAEHER, true);
		
		spy.setPos(ByteMapper.toByte(0, 2));
		flag.setPos(ByteMapper.toByte(0, 0));
		marschall.setPos(ByteMapper.toByte(0, 1));
		general.setPos(ByteMapper.toByte(1, 0));
		spaeher.setPos(ByteMapper.toByte(7, 0));
		
		for(int i=0; i<10; i++)
			heuristic.pieces[i] = spy;
		heuristic.pieces[9] = flag;
		heuristic.pieces[0] = marschall;
		heuristic.pieces[1] = general;
		assertTrue(heuristic.placementOK());
		System.out.println();

		heuristic.pieces[9] = flag;
		heuristic.pieces[0] = spy;
		heuristic.pieces[1] = general;
		assertFalse(heuristic.placementOK());
		System.out.println();

		heuristic.pieces[9] = flag;
		heuristic.pieces[0] = marschall;
		heuristic.pieces[1] = spy;
		assertFalse(heuristic.placementOK());
		System.out.println();
		
		heuristic.pieces[5] = spaeher;
		assertTrue(heuristic.placementOK());

		heuristic.pieces[5] = general;
		general.setPos(spaeher.getPos());
		assertFalse(heuristic.placementOK());
	}
	
	@Test
	void testLineUpBlocked() {
		Piece flag = new Piece(PieceType.FLAGGE, true);
		Piece spy = new Piece(PieceType.SPIONIN, true);
		Piece marschall = new Piece(PieceType.MARSCHALL, true);
		
		Piece[][] field = new Piece[8][3];
		field[0][0] = flag;
		assertFalse(heuristic.lineUpBlocked(field, 0, 0));
		
		field[0][0] = null;
		field[2][0] = flag;
		assertTrue(heuristic.lineUpBlocked(field, 2, 0));
		
		field[0][0] = flag;
		field[0][1] = marschall;
		field[2][0] = spy;
		assertTrue(heuristic.lineUpBlocked(field, 0, 0));

	}
	
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
