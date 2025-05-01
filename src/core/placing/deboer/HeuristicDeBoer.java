package core.placing.deboer;

import java.util.Map;
import java.util.stream.IntStream;

import core.ByteMapper;
import core.Piece;
import core.PieceType;
import core.placing.Placer;

public class HeuristicDeBoer extends Placer {
	Map<PieceType, int[][]> pieceDistributions;

	public HeuristicDeBoer(boolean team) {
		super(team);
		pieceDistributions = strados2.tools.CompressedMapIO.loadCompressedMaps("classic");
	}

	@Override
	public Piece[] place() {
		for(int p=pieces.length-1; p>=0; p--) {
			Piece piece = pieces[p];
			byte location = 0;
			do {
				location = getRandomLocation(getDistribution(piece));
			} while(fieldIsOccupied(location));
			piece.setPos(location);
		}
		mirrorPlacing();
		return pieces;
	}
	
	/**
	 * Modifies and returns the distribution array for Piece.
	 * Modifies it in a way to implement heuristics.
	 * Sets probabilities of already taken fields to 0.
	 * @param piece to get its position and type
	 * @return probability distribution for piece, with modified probabilities to implement heuristics
	 */
	int[][] getDistribution(Piece piece) {
		int[][] distribution = pieceDistributions.get(piece.getType());
		
		for(Piece p: pieces)
			if(p.getPos() != -1)
				distribution[p.getX()][p.getY()] = 0;
		
		return distribution;
	}

	/**
	 * Returns a random x,y byte based on pieceDistributions probabilities.
	 * The higher the content, the more likely it is to be picked.
	 * @param pieceDistributions
	 * @return random byte with probability based on pieceDistributions
	 */
	byte getRandomLocation(int[][] pieceDistributions){
		int distributionSum = 0;
		int count = 0;
		int rand;

		for(int x=0; x<pieceDistributions.length; x++) {
			distributionSum += IntStream.of(pieceDistributions[x]).sum();
		}
		rand = random.nextInt(distributionSum);

		for(int y=0; y<4; y++) {
			for(int x=0; x<8; x++) {
				count += pieceDistributions[x][y];
				if(count > rand) {
					return ByteMapper.toByte(x, y);
				}
			}
		}

		return -1;
	}
}
