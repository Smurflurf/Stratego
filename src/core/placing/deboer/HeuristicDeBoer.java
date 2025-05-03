package core.placing.deboer;

import java.util.Map;
import java.util.stream.IntStream;

import core.ByteMapper;
import core.Piece;
import core.PieceType;
import core.Utils;
import core.placing.Placer;
import strados2.tools.GeneralTools.RelativePosition;
import strados2.tools.NeighborIO;

public class HeuristicDeBoer extends Placer {
	public Map<PieceType, int[][]> pieceDistributions;
	public Map<PieceType, Map<RelativePosition, Map<PieceType, Double>>> neighborCounts;
	int[][] lastDistrib;
	PieceType lastType;
	
	public boolean useNeighbors;

	public HeuristicDeBoer(boolean team) {
		super(team);
		pieceDistributions = strados2.tools.CompressedMapIO.loadCompressedMaps("classic");
		neighborCounts = NeighborIO.loadNeighborCounts("classic");

		useNeighbors = true;
	}
	
	public HeuristicDeBoer(boolean team, Map<PieceType, 
			int[][]> pieceDistributions, Map<PieceType, 
			Map<RelativePosition, Map<PieceType, Double>>> neighborCounts) {
		super(team);
		this.pieceDistributions = pieceDistributions;
		this.neighborCounts = neighborCounts;
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
	 * @param toPlace to get its position and type
	 * @return probability distribution for piece, with modified probabilities to implement heuristics
	 */
	public int[][] getDistribution(Piece toPlace) {
		int[][] distribution = lastType == toPlace.getType() ? lastDistrib :
			deepCloneTH(pieceDistributions.get(toPlace.getType()));

		//remove all already occupied fields
		removeOccupiedFields(distribution);

		if(useNeighbors)
			for(Piece standing: pieces) {
				if(standing.getPos() != -1) {
					//lowers all not connected distributions 
					nerf(distribution, standing, toPlace);

					//magnifies all connected distributions
					buff(distribution, standing, toPlace);
				}
			}

		lastDistrib = distribution;
		lastType = toPlace.getType();
		return distribution;
	}

	/**
	 * Sets probabilities to 0 where a Piece occupies a field
	 * @param distrib probability distribution to modify
	 */
	private void removeOccupiedFields(int[][] distribution) {
		for(Piece standing: pieces)
			if(standing.getPos() != -1)
				distribution[standing.getX()][standing.getY()] = 0;
	}

	/**
	 * Magnifies all probabilities directly connected to standing based on the {@link #neighborCounts} relation
	 * @param distrib probability distribution to modify
	 * @param standing Piece already standing
	 * @param toPlace Piece to be placed
	 */
	private void buff(int[][] distrib, Piece standing, Piece toPlace) {
		for(RelativePosition pos : RelativePosition.values()) {
			if(pos.qbValid(standing.getX(), standing.getY())) {
				if(neighborCounts.get(standing.getType()) != null
						&& neighborCounts.get(standing.getType()).get(pos) != null
						&& neighborCounts.get(standing.getType()).get(pos).get(toPlace.getType()) != null) {
					int originalDistrib = distrib[pos.transformX(standing.getX())][pos.transformY(standing.getY())];
					originalDistrib += originalDistrib * neighborCounts.get(standing.getType()).get(pos).get(toPlace.getType());
					distrib[pos.transformX(standing.getX())][pos.transformY(standing.getY())] = originalDistrib;
				}
			}
		}
	}

	/**
	 * Calculates the euclidean distance between piece location and all places in distrib.
	 * If the distance is smaller than 1.5, meaning the fields right around piece, nothing happens.
	 * Otherwise distrib[x][y] gets lowered by p percent, where p is the combined probability of toPlace being standing's neighbor.
	 * @param distrib probability distribution to modify
	 * @param standing Piece already standing
	 * @param toPlace Piece to be placed
	 */
	public void nerf(int[][] distrib, Piece standing, Piece toPlace) {
		for(int x=0; x<distrib.length; x++) {
			for(int y=0; y<distrib[0].length; y++) {
				if(Math.sqrt((standing.getY() - y) * (standing.getY() - y) + (standing.getX() - x) * (standing.getX() - x)) > 1.5) {
					double combinedProb = 0;
					for(RelativePosition pos : RelativePosition.values())
						if(!Utils.outOfBounds(pos.transformX(x)) && pos.transformY(y)<4 && pos.transformY(y)>=0
						&& neighborCounts.get(standing.getType()) != null
						&& neighborCounts.get(standing.getType()).get(pos) != null
						&& neighborCounts.get(standing.getType()).get(pos).get(toPlace.getType()) != null)
							combinedProb += neighborCounts.get(standing.getType()).get(pos).get(toPlace.getType());
					distrib[x][y] = (int) Math.round(distrib[x][y] * (1 - combinedProb));
				}
			}
		}
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
