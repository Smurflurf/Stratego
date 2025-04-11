package ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import core.ByteMapper;
import core.GameState;
import core.Move;
import core.Piece;
import core.PieceType;
import core.Utils;

/**
 * A simple graphical UI to display a GameState
 */
public class UI {
	JFrame frame;
	private JPanel boardPanel;
	private JLabel[][] squares;

	private static final Color RED_COLOR = new Color(255, 0, 0);
	private static final Color RED_COLOR_BG1 = new Color(255, 225, 225);
	private static final Color RED_COLOR_BG2 = new Color(255, 185, 185);
	private static final Color RED_COLOR_BG3 = new Color(255, 150, 150);
	private static final Color RED_DEAD_COLOR = new Color(60, 65, 81, 50);
	private static final Color BLUE_COLOR = new Color(0, 0, 255);
	private static final Color BLUE_COLOR_BG1 = new Color(225, 225, 255);
	private static final Color BLUE_COLOR_BG2 = new Color(185, 185, 255);
	private static final Color BLUE_COLOR_BG3 = new Color(150, 150, 255);
	private static final Color BLUE_DEAD_COLOR = new Color(60, 65, 81, 50);
	private static final Color LAKE_COLOR = new Color(109, 181, 205);
	private static final Color EMPTY_COLOR = new Color(200, 200, 200);
	private static final Color PIECE_BG_COLOR = new Color(230, 230, 230);

	public UI() {
		frame = new JFrame("Stratego Quick Battle");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setSize(500, 550);
		initializeUI();
	}

	/**
	 * Updates the visual representation of the board based on the GameState.
	 * 
	 * @param gameState current state of the game
	 * @param lastMove last move to display it visually, or null
	 */
	public void updateBoard(GameState gameState, Move lastMove) {
		SwingUtilities.invokeLater(() -> {
			for (int y=0; y<8; y++) {
				for (int x=0; x<8; x++) {
					JLabel currentLabel = squares[x][y];
					currentLabel.setForeground(EMPTY_COLOR);
					currentLabel.setBackground(EMPTY_COLOR);
					Piece piece = gameState.inspect(x, y);

					if (Utils.blockedByLake(x, y)) {
						currentLabel.setBackground(LAKE_COLOR);
						currentLabel.setText("X");
						currentLabel.setForeground(Color.BLACK);
					} else if (piece == null) {
						currentLabel.setBackground(EMPTY_COLOR);
						currentLabel.setText("");
					} else {
						currentLabel.setBackground(PIECE_BG_COLOR);
						currentLabel.setForeground(piece.getTeam() ? RED_COLOR : BLUE_COLOR);
						currentLabel.setText(getPieceSymbol(piece));
					}
				}
			}
			if(lastMove != null) {
//				if(lastMove.getFirstMove() != null) {
//					squares[lastMove.getFirstMove().getStartX()][lastMove.getFirstMove().getStartY()]
//							.setBackground(lastMove.getPiece().getTeam() ? RED_COLOR_BG1 : BLUE_COLOR_BG1);
//					squares[lastMove.getFirstMove().getStartX()][lastMove.getFirstMove().getStartY()]
//							.setForeground(lastMove.getPiece().getTeam() ? RED_DEAD_COLOR : BLUE_DEAD_COLOR);
//					squares[lastMove.getFirstMove().getStartX()][lastMove.getFirstMove().getStartY()]
//							.setText(getPieceSymbol(lastMove.getPiece()));
//				}

				for(Byte b : lastMove.getRelevantFields()) {
					squares[ByteMapper.getX(b)][ByteMapper.getY(b)]
							.setBackground(lastMove.getPiece().getTeam() ? RED_COLOR_BG1 : BLUE_COLOR_BG1);
				}
				
				squares[lastMove.getStartX()][lastMove.getStartY()]
						.setBackground(lastMove.getPiece().getTeam() ? RED_COLOR_BG2 : BLUE_COLOR_BG2);
				squares[lastMove.getStartX()][lastMove.getStartY()]
						.setForeground(lastMove.getPiece().getTeam() ? RED_DEAD_COLOR : BLUE_DEAD_COLOR);
				squares[lastMove.getStartX()][lastMove.getStartY()]
						.setText(getPieceSymbol(lastMove.getPiece()));
				squares[lastMove.getEndX()][lastMove.getEndY()]
						.setBackground(lastMove.getPiece().getTeam() ? RED_COLOR_BG3 : BLUE_COLOR_BG3);
				if(gameState.inspect(lastMove.getEndX(), lastMove.getEndY()) == null) {
					squares[lastMove.getEndX()][lastMove.getEndY()]
							.setText(getPieceSymbol(lastMove.getPiece()));
					squares[lastMove.getEndX()][lastMove.getEndY()]
							.setBackground(!lastMove.getPiece().getTeam() ? RED_COLOR_BG2 : BLUE_COLOR_BG2);
					squares[lastMove.getEndX()][lastMove.getEndY()]
							.setForeground(!lastMove.getPiece().getTeam() ? RED_DEAD_COLOR : BLUE_DEAD_COLOR);
				}
			}
			frame.setTitle("Stratego Quick Battle - Turn: " + (gameState.getTeam() ? "Red" : "Blue"));
		});
	}
	
	/**
	 * Changes the background color of all Pieces to the winners color
	 * @param winner true if red
	 */
	public void showGameOver(boolean winner) {
		for (int y=0; y<8; y++) {
			for (int x=0; x<8; x++) {
				JLabel currentLabel = squares[x][y];
				if(!currentLabel.getBackground().equals(EMPTY_COLOR) && !currentLabel.getBackground().equals(LAKE_COLOR))
					currentLabel.setBackground(winner ? RED_COLOR_BG2 : BLUE_COLOR_BG2);
			}
		}
		frame.setTitle("Stratego Quick Battle - Game Over. Winner: " + (winner ? "Red" : "Blue"));
	}

	private void initializeUI() {
		squares = new JLabel[8][8];
		boardPanel = new JPanel(new GridLayout(8, 8));
		initPanels();

		JPanel topLabels = new JPanel(new GridLayout(1, 8));
		JPanel leftLabels = new JPanel(new GridLayout(8, 1));
		for (int i=0; i<8; i++) {
			topLabels.add(new JLabel("" + i, SwingConstants.CENTER));
			leftLabels.add(new JLabel("" + i, SwingConstants.CENTER));
		}

		frame.add(topLabels, BorderLayout.NORTH);
		frame.add(leftLabels, BorderLayout.WEST);
		frame.add(boardPanel, BorderLayout.CENTER);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	/**
	 * Returns a short symbol for a piece type
	 * @param piece The piece to represent
	 * @return a string symbol
	 */
	private String getPieceSymbol(Piece piece) {
		if (piece == null) return "";
		PieceType type = piece.getType();
		switch (type) {
		case MARSCHALL: return "10";
		case GENERAL:   return "9";
		case MINEUR:    return "3";
		case SPAEHER:   return "2";
		case SPIONIN:   return "1";
		case BOMBE:     return "B";
		case FLAGGE:    return "F";
		case UNKNOWN:   return "?";
		default:        return "?";
		}
	}

	private void initPanels() {
		for (int y=0; y<8; y++) {
			for (int x=0; x<8; x++) {
				squares[x][y] = new JLabel("", SwingConstants.CENTER);
				squares[x][y].setOpaque(true);
				squares[x][y].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
				squares[x][y].setFont(new Font("SansSerif", Font.BOLD, 14));
				boardPanel.add(squares[x][y]);
			}
		}
	}

	public void close() {
		SwingUtilities.invokeLater(() -> {
			if (frame != null) {
				frame.dispose();
			}
		});
	}
}