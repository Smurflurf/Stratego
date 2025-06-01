package core.placing.deboer;

import java.util.Map;
import java.util.stream.IntStream;

import core.ByteMapper;
import core.Direction;
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
	public boolean useControlHeuristic;

	public HeuristicDeBoer(boolean team) {
		super(team);
		pieceDistributions = strados2.tools.CompressedMapIO.loadCompressedMaps("classic");
		neighborCounts = NeighborIO.loadNeighborCounts("classic");

		useNeighbors = true;
		useControlHeuristic = true;
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
		do {
			resetPieces();
			for(int p=pieces.length-1; p>=0; p--) {
				setSemiRandomLocation(pieces[p]);
			}
		} while (!placementOK());
		mirrorPlacing();
		return pieces;
	}

	/**
	 * Checks if the pieces are not placed badly.
	 * Returns false if there are obvious flaws.
	 * @return true if the pieces are placed without obvious flaws.
	 */
	public boolean placementOK() {
		if(!useControlHeuristic)
			return true;
		return testFlagAccessibility();
	}

	/**
	 * Sets a Pieces location to a with {@link #getSemiRandomLocation(int[][])} generated location.
	 * Takes into account {@link #pieceDistributions} and {@link #neighborCounts}.
	 * @param piece to set location
	 */
	private void setSemiRandomLocation(Piece piece) {
		byte location = 0;
		do {
			location = getRandomLocation(getDistribution(piece));
		} while(fieldIsOccupied(location));
		piece.setStartPos(location);
	}

	/**
	 * Resets all Piece positions to -1
	 */
	private void resetPieces() {
		for(Piece piece : pieces)
			piece.setStartPos((byte)-1);
	}

	/**
	 * Modifies and returns the distribution array for Piece.
	 * Modifies it in a way to implement heuristics.
	 * Sets probabilities of already taken fields to 0.
	 * @param toPlace to get its position and type
	 * @return probability distribution for piece, with modified probabilities to implement heuristics
	 */
	public int[][] getDistribution(Piece toPlace) {
		double buffFactor = 1;
		double nerfFactor = 1;
		
		int[][] distribution = lastType == toPlace.getType() ? lastDistrib :
			deepCloneTH(pieceDistributions.get(toPlace.getType()));

		//remove all already occupied fields
		removeOccupiedFields(distribution);

		if(useNeighbors)
			for(Piece standing: pieces) {
				if(standing.getPos() != -1) {
					//lowers all not connected distributions 
					nerf(distribution, standing, toPlace, nerfFactor);

					//magnifies all connected distributions
					buff(distribution, standing, toPlace, buffFactor);
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
	 * @param buffFactor weight of the neighbor probabilities
	 */
	private void buff(int[][] distrib, Piece standing, Piece toPlace, double buffFactor) {
		for(RelativePosition pos : RelativePosition.values()) {
			if(pos.qbValid(standing.getX(), standing.getY())) {
				if(neighborCounts.get(standing.getType()) != null
						&& neighborCounts.get(standing.getType()).get(pos) != null
						&& neighborCounts.get(standing.getType()).get(pos).get(toPlace.getType()) != null) {
					int originalDistrib = distrib[pos.transformX(standing.getX())][pos.transformY(standing.getY())];
					originalDistrib *= (int) Math.round(1 + buffFactor * neighborCounts.get(standing.getType()).get(pos).get(toPlace.getType()));
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
	 * @param nerfFactor weight of the neighbor probabilities
	 */
	public void nerf(int[][] distrib, Piece standing, Piece toPlace, double nerfFactor) {
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
					distrib[x][y] *= (int) Math.round(1 - nerfFactor * combinedProb);
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
	
	/**
	 * Checks if the flag is accessible by an enemy scout within two moves.
	 * @return true if the flag is save
	 */
	private boolean testFlagAccessibility() {
		Piece[][] field = new Piece[8][3];
		for(Piece piece : pieces)
			field[piece.getX()][piece.getY()] = piece;

		return lineUpBlocked(field, pieces[9].getX(), pieces[9].getY()) 
				&& lineSideBlocked(field);
	}

	/**
	 * Checks if position x,y in field is defended by any Piece other than a spy (and flag).
	 * If not, checks if the position is defended by a scout.
	 * @param field
	 * @param x
	 * @param y
	 * @return false if there is no defender for x,y in field
	 */
	boolean defenderNearby(Piece[][] field, int x, int y) {
		for(Direction dir : Direction.values()) {
			byte translated = dir.translate(ByteMapper.toByte(x, y), 1);
			int tX = ByteMapper.getX(translated);
			int tY = ByteMapper.getY(translated);
			if(
					(tX>=0 && tX<field.length) 			// test if direct neighbor exists
					&& (tY>=0 && tY<field[0].length) 	
					&& field[tX][tY] != null
					&& field[tX][tY].getType() != PieceType.SPIONIN
					&& field[tX][tY].getType() != PieceType.FLAGGE)
				return true;
		}

		// Scout check
		for(Direction dir : Direction.values()) {
			if(dir == Direction.UP || dir == Direction.DOWN) continue;
			for(int reach=1; reach<8; reach++) {
				int tX=0, tY = y;
				if(dir == Direction.LEFT || dir == Direction.RIGHT)	// translate x and y
					tX = dir.getOneDimTranslation() * reach + x;
//				else
//					tY = dir.getOneDimTranslation() * reach + x;
				
				if(!(tX>=0 && tX<field.length) 						// out of bounds check
						|| !(tY>=0 && tY<field[0].length))
					break;
				if(field[tX][tY] != null) {
					if(field[tX][tY].getType() == PieceType.SPAEHER) {
						return true;
					} else {
						break;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the flag is not reachable from its left or right side.
	 * @param field
	 * @return true if the flag is save from side attacks
	 */
	public boolean lineSideBlocked(Piece[][] field) {
		for(int x=pieces[9].getX() + 1; x<field.length; x++)	// test to the right
			if(field[x][pieces[9].getY()] != null && field[x][pieces[9].getY()].getType() != PieceType.SPIONIN)
				break;
			else if(lineUpBlocked(field, x, pieces[9].getY()))
				continue;
			else
				if(!defenderNearby(field, x, pieces[9].getY()))
					return false;

		for(int x=pieces[9].getX() - 1; x>=0; x--)				// test to the left
			if(field[x][pieces[9].getY()] != null && field[x][pieces[9].getY()].getType() != PieceType.SPIONIN)
				break;
			else if(lineUpBlocked(field, x, pieces[9].getY()))
				continue;
			else
				if(!defenderNearby(field, x, pieces[9].getY()))
					return false;
		
		// Criss cross, criss cross
		return true;
	}

	/**
	 * Checks if the flag has a direct sight line upwards.
	 * Spies don't count as defenders.
	 * @param field temporary int field created by {@link #testFlagAccessibility()}
	 * @return true if the flag is blocked (thats good)
	 */
	public boolean lineUpBlocked(Piece[][]field, int x, int y) {
		if(x == 2 || x == 5)
			return true;

		for(int y1=y+1; y1<field[0].length; y1++)
			if(field[x][y1] != null && field[x][y1].getType() != PieceType.SPIONIN)
				return true;

		return false;
	}
}
