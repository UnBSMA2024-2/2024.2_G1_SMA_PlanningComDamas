package puzzle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jadex.commons.SimplePropertyChangeSupport;
import jadex.commons.beans.PropertyChangeListener;

/**
 * The View Board represents the puzzle board and the pegs.
 */
public class JackBoard implements IBoard, Serializable
{
  	private static volatile JackBoard instance;
	private final int invalidPosition = 4;

	protected List<Move> moves = new ArrayList<Move>();
	private boolean isWhiteTurn = true;

	protected Piece white_piece = new Piece(true);
	protected Piece queen_white_piece = new Piece(true,true);
	protected Piece black_piece = new Piece(false);
	protected Piece queen_black_piece = new Piece(false, true);

	public SimplePropertyChangeSupport pcs = new SimplePropertyChangeSupport(this);

	public static JackBoard getInstance(){
		JackBoard result = instance;
		if (result != null) {
			return result;
		}

		synchronized (JackBoard.class) {
			if (instance == null) {
				instance = new JackBoard();
			}
			return instance;
		}
	}

	public int getPlayerColorPiece() {
		if(isWhiteTurn) {
			isWhiteTurn = false;
			return 1;
		} else {
			isWhiteTurn = true;
			return -1;
		}
	}

	public boolean isWhitePlayerTurn() {
		return isWhiteTurn;
	}

	/**
	 * Get a piece for a location.
	 */
	public Piece getPiece(Position pos)
	{
		int piece = get(pos.getX(), pos.getY());
		if(piece==1)
			return white_piece;
		else if(piece==-1)
			return black_piece;
		else if(piece==2)
			return queen_white_piece;
		else if(piece==-2)
			return queen_black_piece;
		else
			return null;
	}

	/**
	 * Get possible moves.
	 * @return Get all possible move.
	 */
	public List<Move> getPossibleMoves(){
		List<Move> possibleMoves = new ArrayList<Move>();
		return possibleMoves;
	}

	/**
	 * Get possible moves.
	 * @return Get all possible move.
	 */
	public List<Move> getPossibleMoves(int playerColorPiece)
	{

		List<Move> possibleMoves = new ArrayList<Move>();
		for(int y=0; y<8; y++)
		{
			for(int x=0; x<8; x++)
			{
				int piece = get(x,y);
				
				if(piece != 0 && (piece==playerColorPiece || piece == playerColorPiece+1 
					|| piece == playerColorPiece-1)) {
					
					boolean isQueen = (piece == -2 || piece == 2);
					possibleMoves.addAll(moves(x, y, piece, isQueen));
				}
			}
		}

        if (possibleMoves.stream().anyMatch(Move::isJumpMove))
        {
            possibleMoves.removeIf(move -> !move.isJumpMove());
        }

		return possibleMoves;
	}

	/**
	 * Do a move.
	 * @param move The move.
	 */
	public boolean move(Move move)
	{
		int p = get(move.getStart());
		int f = get(move.getEnd());
		if (f != 0) {
			return false;
		}
		
		set(0, move.getStart()); // deixando um buraco no movimento inicial
		if (move.isJumpMove()) {
			set(0, move.getCaptured());
		}

		// the_hole = move.getStart();
		set(p, move.getEnd());
		
		// Promote piece to queen
		if(move.getEnd().getY() == 7 && p == -1){
			set(-2, move.getEnd());
		}

		if(move.getEnd().getY() == 0 && p == 1){
			set(2, move.getEnd());
		}

		pcs.firePropertyChange("solution", null, move);
		// pcs.firePropertyChange(IBoard.MOVE, null, move);
		isWhiteTurn = !isWhiteTurn;
		
		return true;
	}

	/**
	 * Takeback a move.
	 */
	public boolean takeback()
	{
		// if(moves.size()==0)
		// 	return false;

		// Move move = (Move)moves.get(moves.size()-1);
		// int p = get(move.getEnd());
		// set(p, move.getStart());
		// set(0, move.getEnd());
		// the_hole = move.getEnd();
		// moves.remove(moves.size()-1);
		// pcs.firePropertyChange("solution", null, move);
//		pcs.firePropertyChange(IBoard.TAKEBACK, null, move);
		return true;
	}

	/**
	 * Test if it is a solution.
	 * @return True, if solution.
	 */
	public boolean isSolution()
	{
		return solution();
	}

	/**
	 * Get all moves made so far.
	 */
	public List<Move> getMoves()
	{
		return moves;
	}

	/**
	 * Get all moves made so far.
	 */
	public Move getLastMove()
	{
		return moves.size()>0? (Move)moves.get(moves.size()-1): null;
	}

	/**
	 * Test if the last move was with a white piece.
	 * When no move was made, it return true.
	 * @return True, is last move was with white piece.
	 */
	public boolean wasLastMoveWhite()
	{
		Move move = getLastMove();
		return move==null || get(move.getEnd())==1;
	}

	/**
	 *  Get the board size.
	 */
	public int getSize()
	{
		return 8;
	}

	/**
	 *  Get the current board position.
	 */
	public List<Piece> getCurrentPosition()
	{
		List<Piece> ret = new ArrayList<Piece>();
		for(int y=0; y<8; y++)
		{
			for(int x=0; x<8; x++)
			{
				ret.add(getPiece(new Position(x, y)));
			}
		}
		return ret;
	}

	/**
	 *  Test if a position is free.
	 */
	public boolean isFreePosition(Position pos)
	{
		int x = pos.getX();
		int y = pos.getY();
		return get(x,y)==0;
	}

	/**
	 * The int [x][y] board represents the game board, with 0 marking
	 * the hole, and 4 marking out-of-limit coordinates. Otherwise
	 * there are "1" pieces and "-1" pieces. Note that we don't
	 * distinguish pieces individually in this representation.
	 */
	int[][] board = {
	    {  0, -1,  0,  0,  0,  1,  0,  1},
	    { -1,  0, -1,  0,  0,  0,  1,  0},
	    {  0, -1,  0,  0,  0,  1,  0,  1},
	    { -1,  0, -1,  0,  0,  0,  1,  0},
	    {  0, -1,  0,  0,  0,  1,  0,  1},
	    { -1,  0, -1,  0,  0,  0,  1,  0},
	    {  0, -1,  0,  0,  0,  1,  0,  1},
	    { -1,  0, -1,  0,  0,  0,  1,  0}
	};
	
	int last = 4;
	Position the_hole = new Position(2, 2);
	/**
	 * The static int [][] move_check_table represents available
	 * moves, as coordinate offsets and piece colour. E.g., with the
	 * hole in <x:y>, then a "-1" piece in <x+1:y> is eligible, as is
	 * a "-1" piece in <x+2,y> if there also is a "1" piece in
	 * <x+1:y>.
	 */

	// y, x, piece_col
	static int[][] move_check_table_white = {
		{ 1, -1, 0},         // Diagonal para cima e para a direita (movimento simples).
		{ -1, -1, 0},        // Diagonal para cima e para a esquerda (movimento simples).
		{ 2, -2, 0, 1, -1 }, // Salto para cima e para a direita (captura, passando por uma peça adversária em 1, -1).
		{ -2, -2, 0, -1, -1 } // Salto para cima e para a esquerda (captura, passando por uma peça adversária em -1, -1).	
	};

	static int[][] move_check_table_black = {
		{ -1, 1, 0},         // Diagonal para baixo e para a esquerda (movimento simples).
		{ 1, 1, 0},          // Diagonal para baixo e para a direita (movimento simples).
		{ -2, 2, 0, -1, 1 }, // Salto para baixo e para a esquerda (captura, passando por uma peça adversária em -1, 1).
		{ 2, 2, 0, 1, 1 }   // Salto para baixo e para a direita (captura, passando por uma peça adversária em 1, 1).
	};

	static int[][] move_check_table_black_queen = {
		{ 1, -1, 0},         // Diagonal para cima e para a direita (movimento simples).
		{ -1, -1, 0},        // Diagonal para cima e para a esquerda (movimento simples).
		{ 2, -2, 0, 1, -1 }, // Salto para cima e para a direita (captura, passando por uma peça adversária em 1, -1).
		{ -2, -2, 0, -1, -1 }, // Salto para cima e para a esquerda (captura, passando por uma peça adversária em -1, -1).	
		{ -1, 1, 0},         // Diagonal para baixo e para a esquerda (movimento simples).
		{ 1, 1, 0},          // Diagonal para baixo e para a direita (movimento simples).
		{ -2, 2, 0, -1, 1 }, // Salto para baixo e para a esquerda (captura, passando por uma peça adversária em -1, 1).
		{ 2, 2, 0, 1, 1 }   // Salto para baixo e para a direita (captura, passando por uma peça adversária em 1, 1).
	};

	static int[][] move_check_table_white_queen = {
		{ 1, -1, 0},         // Diagonal para cima e para a direita (movimento simples).
		{ -1, -1, 0},        // Diagonal para cima e para a esquerda (movimento simples).
		{ 2, -2, 0, 1, -1 }, // Salto para cima e para a direita (captura, passando por uma peça adversária em 1, -1).
		{ -2, -2, 0, -1, -1 }, // Salto para cima e para a esquerda (captura, passando por uma peça adversária em -1, -1).	
		{ -1, 1, 0},         // Diagonal para baixo e para a esquerda (movimento simples).
		{ 1, 1, 0},          // Diagonal para baixo e para a direita (movimento simples).
		{ -2, 2, 0, -1, 1 }, // Salto para baixo e para a esquerda (captura, passando por uma peça adversária em -1, 1).
		{ 2, 2, 0, 1, 1 }   // Salto para baixo e para a direita (captura, passando por uma peça adversária em 1, 1).
	};

	/**
	 * The moves() method computes possible moves to <x:y>,
	 * represented by the Positiones of pieces to move.
	 */
	List<Move> moves(int x, int y, int pieceType, boolean isQueen)
	{
		int[][] check_table = pieceType == 1 ? move_check_table_white : move_check_table_black;
		if (isQueen) {
			System.out.println("is queen"+ x+"-"+y);
			check_table = pieceType == 2 ? move_check_table_white_queen : move_check_table_black_queen;
		}

		List<Move> possibleMoves = new ArrayList<Move>();
		for(int i = 0; i < check_table.length; i++){
			check(possibleMoves, check_table[i], x, y);
		}
		return possibleMoves;
	}

	/**
	 * The check() method processes a move_check_table entry, and adds
	 * a Move to the Vector when the entry applies.
	 */
	boolean check(List<Move> possibleMoves, int[] m, int x, int y)
	{
		int x1 = (x+m[0]);
		int y1 = (y+m[1]);
		int piece = get(x,y);

		// System.out.println("Current piece: " + piece +" x: " + x + " y: " + y +"\n"+"x1: " + x1 + " y1: " + y1+" Future space piece: " + get(x1, y1));

		if((get(x1, y1) == m[2]))
		{	
			
			if(m.length == 3)
			{
				Position startPosition = new Position (x, y);
				Position endPosition = new Position (x1, y1);
				Move newMove = new Move(startPosition, endPosition);
				possibleMoves.add(newMove);
				// System.out.println("Adding move: " + newMove);
				return true;
			}

			int peaceToCapture = get((x+m[3]), (y+m[4]));
			if(m.length == 5 &&  peaceToCapture != piece && peaceToCapture != piece-1 && peaceToCapture != piece+1 
			&&  peaceToCapture != 0  &&  peaceToCapture != invalidPosition ){
				Position startPosition = new Position (x, y);
				Position endPosition = new Position (x1, y1);
				Position captured = new Position ((x+m[3]), (y+m[4]));
				Move newMove = new Move(startPosition, endPosition, captured);
				possibleMoves.add(newMove);
				// System.out.println("Adding move: " + newMove);
				return true;
			}
		}
		
		return false;
	}

	int get(int x, int y)
	{	
		if(((((x<0) || (x>= 8)) || (y<0)) || (y>= 8)))
			return invalidPosition;
		return board[x][y];
	}

	int get(Position s)
	{
		return get(s.x, s.y);
	}

	void set(int v, int x, int y)
	{
		board[x][y] = v;
	}

	void set(int pieceNumber, Position s)
	{
		if(s == null){
			return; 
		}

		set(pieceNumber, s.x, s.y);
	}

	boolean solution()
	{
		int onlyPiecesOf = 0; // 0: none, 1: white, -1: black 
		for(int i = 0; (i<board.length); i++)
		{
			for(int j = 0; (j<board[i].length); j++)
			{
				if((board[i][j]!=0)){
					if((onlyPiecesOf==0)){
						onlyPiecesOf = board[i][j];
					}
					else if((onlyPiecesOf!=board[i][j])){
						return false;
					}
				}
			}
		}
		return true;
	}

	// List<Position> moves(Position hole)
	// {
	// 	return moves(hole.x, hole.y);
	// }

	boolean isJumpMove(int x, int y)
	{
		int dx = Math.abs((the_hole.x-x));
		int dy = Math.abs((the_hole.y-y));
		return ((dx==2) || (dy==2));
	}

	boolean isJumpMove(Position s)
	{
		return isJumpMove(s.x, s.y);
	}

	//-------- property methods --------

	/**
     *  Add a PropertyChangeListener to the listener list.
     *  The listener is registered for all properties.
     *  @param listener  The PropertyChangeListener to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(listener);
    }

    /**
     *  Remove a PropertyChangeListener from the listener list.
     *  This removes a PropertyChangeListener that was registered
     *  for all properties.
     *  @param listener  The PropertyChangeListener to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(listener);
    }

	/** 
	 *  Get the string representation.
	 */
	public String toString()
	{
		return "JackBoard(moves=" + moves + ", the_hole=" + the_hole + ")";
	}
}

