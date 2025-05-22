package ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;

import core.ByteMapper;
import core.Direction;
import core.GameState;
import core.Move;
import core.Piece;
import core.PieceType;
import core.Utils;
import core.playing.AI;

/**
 * A simple graphical UI to display a GameState
 */
public class UI {
	JFrame frame;
	private JPanel boardPanel;
	private JLabel[][] squares;
	AI.Type red;
	AI.Type blue;

	private GameState currentGameState;
	private Piece selectedPiece = null;
	private byte startPos = -1;

	private static final Color DRAW_COLOR = new Color(204, 255, 204);
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
	private static final Color ERROR_COLOR = new Color(150, 0, 0);
	private static final Border SELECTED_BORDER_RED = BorderFactory.createLineBorder(RED_COLOR_BG2, 3);
	private static final Border SELECTED_BORDER_BLUE = BorderFactory.createLineBorder(BLUE_COLOR_BG2, 3);
	private static final Border POSSIBLE_MOVE_BORDER_RED = BorderFactory.createLineBorder(RED_COLOR_BG3, 2);
	private static final Border POSSIBLE_MOVE_BORDER_BLUE = BorderFactory.createLineBorder(BLUE_COLOR_BG3, 2);
	private static final Border DEFAULT_BORDER = BorderFactory.createLineBorder(Color.DARK_GRAY);	
	private static final Border ERROR_BORDER = BorderFactory.createLineBorder(ERROR_COLOR, 2);		

	// Static queue to pass the move from UI thread to game thread
	private static final BlockingQueue<Move> moveInputQueue = new LinkedBlockingQueue<>(1);


	public UI(AI.Type redPlayer, AI.Type bluePlayer) {
		red = redPlayer;
		blue = bluePlayer;
		frame = new JFrame("Stratego Quick Battle");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setSize(500, 550);
		initializeUI();
	}

	/**
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public static Move getHumanMove() throws InterruptedException {
//		System.out.println("HumanInput waiting for move from UI...");
		Move move = moveInputQueue.take();
//		System.out.println("HumanInput received move: " + move);
		return move;
	}

	public void setTitle(String title) {
		SwingUtilities.invokeLater(() -> {
			frame.setTitle(title);
		});
	}
	
	/**
	 * Updates the visual representation of the board based on the GameState.
	 * 
	 * @param gameState current state of the game
	 * @param lastMove last move to display it visually, or null
	 */
	public void updateBoard(GameState gameState, Move lastMove) {
		currentGameState = gameState;

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
				short s = lastMove.getRelevantFields();
				for(int x=0; x<7; x++)
					for(int y=0; y<7; y++)
						if(ByteMapper.contains(s, ByteMapper.toByte(x, y)))
							squares[x][y].setBackground(lastMove.getPiece().getTeam() ? RED_COLOR_BG1 : BLUE_COLOR_BG1);

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

	private void initializeUI() {
		squares = new JLabel[8][8];
		boardPanel = new JPanel(new GridLayout(8, 8));
		initPanelsWithListeners();

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
	 * Changes the background color of all Pieces to the winners color
	 * @param winner 0 = red, 1 = blue, >=2 = draw
	 */
	public void showGameOver(int winner) {
		for (int y=0; y<8; y++) {
			for (int x=0; x<8; x++) {
				JLabel currentLabel = squares[x][y];
				if(!currentLabel.getBackground().equals(EMPTY_COLOR) && !currentLabel.getBackground().equals(LAKE_COLOR))
					currentLabel.setBackground(winner == 0 ? RED_COLOR_BG2 : 
						winner == 1 ? BLUE_COLOR_BG2 : DRAW_COLOR);
			}
		}
		frame.setTitle("Stratego Quick Battle - Game Over. Winner: " + (winner == 0 ? "Red" : winner == 1 ? "Blue" : "nobody, it's a draw"));
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
		default:        return "?";
		}
	}

	private void initPanelsWithListeners() {
		for (int y=0; y<8; y++) {
			for (int x=0; x<8; x++) {
				squares[x][y] = new JLabel("", SwingConstants.CENTER);
				squares[x][y].setOpaque(true);
				squares[x][y].setBorder(DEFAULT_BORDER);
				squares[x][y].setFont(new Font("SansSerif", Font.BOLD, 14));
				final int xFinal = x, yFinal = y;
				squares[x][y].addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						handleSquareClick(xFinal, yFinal);
					}
				});
				boardPanel.add(squares[x][y]);
			}
		}
	}

	/**
	 * Handels clicking anywhere on the UI.
	 * @param x
	 * @param y
	 */
	private void handleSquareClick(int x, int y) {
		if (currentGameState == null || Utils.isGameOver(currentGameState)) return;

		AI.Type currentAIType = currentGameState.getTeam() ? red : blue;
		if (currentAIType != AI.Type.HUMAN) return;

		Piece clickedPiece = currentGameState.inspect(x, y);
		byte clickedPos = ByteMapper.toByte(x, y);

		if (selectedPiece == null) {
			if (clickedPiece != null 
					&& clickedPiece.getTeam() == currentGameState.getTeam()
					&& clickedPiece.getType().getMoves() > 0) {
				selectedPiece = clickedPiece;
				startPos = clickedPos;

				resetSelectionVisuals();
				squares[x][y].setBorder(selectedPiece.getTeam() ? SELECTED_BORDER_RED : SELECTED_BORDER_BLUE);
				highlightPossibleMoves();
			} else {
				resetSelectionState();
				flashErrorBorder(x, y);
			}
		} else {
			if (clickedPos == startPos) {
				resetSelectionState();
			} else {
				int dx = x - selectedPiece.getX();
				int dy = y - selectedPiece.getY();
				Direction direction = null;
				int fields = 0;

				if (dx == 0 && dy != 0) { 
					direction = dy > 0 ? Direction.DOWN : Direction.UP;
					fields = Math.abs(dy);
				} else if (dy == 0 && dx != 0) { 
					direction = dx > 0 ? Direction.RIGHT : Direction.LEFT;
					fields = Math.abs(dx);
				} else {	// Diagonal
					if(clickedPiece != null && clickedPiece.getTeam() == selectedPiece.getTeam()) {
						if(clickedPiece.getType() != PieceType.BOMBE && clickedPiece.getType() != PieceType.FLAGGE) {
							selectedPiece = clickedPiece;
							startPos = clickedPos;

							resetSelectionVisuals();
							squares[x][y].setBorder(selectedPiece.getTeam() ? SELECTED_BORDER_RED : SELECTED_BORDER_BLUE);
							highlightPossibleMoves();
							return;
						} else {
							resetSelectionState();
							flashErrorBorder(x, y);
							return;
						}
					} else {
						resetSelectionState();
						flashErrorBorder(x, y);
						return;
					}
				}

				Move move = new Move(selectedPiece, direction, fields);

				if (Utils.isMovePossible(currentGameState, move.getPiece(), move.getEndX(), move.getEndY(), move.getDirection(), move.getFields())) {
					try {
						moveInputQueue.clear();
						moveInputQueue.put(move);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt(); // Restore interrupt status
						System.err.println("UI thread interrupted while putting move.");
					}
					resetSelectionState();
				} else {
					if (clickedPiece != null 
							&& clickedPiece.getTeam() == currentGameState.getTeam() 
							&& clickedPiece.getType().getMoves() > 0) {
						selectedPiece = clickedPiece;
						startPos = clickedPos;
						resetSelectionVisuals();
						squares[x][y].setBorder(selectedPiece.getTeam() ? SELECTED_BORDER_RED : SELECTED_BORDER_BLUE);
						highlightPossibleMoves();
					} else {
						resetSelectionState();
						flashErrorBorder(x, y);
					}
				}
			}
		}
	}

	/**
	 * Colors the border around possible Move squares
	 */
	private void highlightPossibleMoves() {
		for(Move move : Utils.getPiecePossibleMoves(currentGameState, selectedPiece)) {
			squares[move.getEndX()][move.getEndY()].setBorder(selectedPiece.getTeam() ? POSSIBLE_MOVE_BORDER_RED : POSSIBLE_MOVE_BORDER_BLUE);
		}

	}

	/**
	 * Shortly flashes the selected border as {@link #errorBorder}.
	 * @param x
	 * @param y
	 */
	private void flashErrorBorder(int x, int y) {
		if (x<0 || x>=squares.length 
				|| y<0 || y>=squares[0].length 
				|| squares[x][y] == null) {
			return;
		}
		final JLabel targetLabel = squares[x][y];
		targetLabel.setBorder(ERROR_BORDER);
		//        Color oldColor = targetLabel.getBackground();
		//        targetLabel.setBackground(ERROR_COLOR);

		Timer resetTimer = new Timer(250, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (targetLabel.getBorder() == ERROR_BORDER) {
					targetLabel.setBorder(DEFAULT_BORDER);
				}
//				if (targetLabel.getBackground() == ERROR_COLOR) {
//					targetLabel.setBackground(oldColor);
//				}
			}
		});
		resetTimer.setRepeats(false);
		resetTimer.start();
	}

	/**
	 * Resets Borders and sets the selected Piece to null.
	 */
	private void resetSelectionState() {
		resetSelectionVisuals();
		selectedPiece = null;
		startPos = -1;
	}

	/**
	 * Resets Borders.
	 */
	private void resetSelectionVisuals() {
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				if (squares[x][y] != null) {
					squares[x][y].setBorder(DEFAULT_BORDER);
				}
			}
		}
	}

	/**
	 * Closes the UI.
	 */
	public void close() {
		SwingUtilities.invokeLater(() -> {
			if (frame != null) {
				frame.dispose();
			}
		});
	}
}