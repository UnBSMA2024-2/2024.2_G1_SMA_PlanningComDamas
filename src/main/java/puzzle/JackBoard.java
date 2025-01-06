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

	protected Piece white_piece = new Piece(true);
	protected Piece black_piece = new Piece(false);
	protected List<Move> moves = new ArrayList<Move>();
	public SimplePropertyChangeSupport pcs = new SimplePropertyChangeSupport(this);

	private boolean isWhiteTurn = true;

  public static JackBoard getInstance() {
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
		else
			return null;
	}

	/**
	 * Get possible moves.
	 * @return Get all possible move.
	 */
	public List<Move> getPossibleMoves(){
		return getPossibleMoves(-5);
	}

	/**
	 * Get possible moves.
	 * @return Get all possible move.
	 */
	public List<Move> getPossibleMoves(int playerColorPiece)
	{
		List<Move> ret = new ArrayList<Move>();
		List<List<Position>> allPieces = new ArrayList<>();
		ArrayList<Position> pieceOldPosition = new ArrayList<Position>();
		for(int y=0; y<8; y++)
		{
			for(int x=0; x<8; x++)
			{
				if(get(x,y)!=0 && get(x,y)!=4 && (playerColorPiece == -5 || get(x,y)==playerColorPiece)) {

					List<List<Position>> captures = moves(x, y,playerColorPiece).captures;
					List<Position> moves = moves(x, y,playerColorPiece).moves;
					while (captures.size() < moves.size()) {
						captures.add(new ArrayList<Position>());
					}
					for(int j = 0; j<moves.size(); j++) {					
						ret.add(new Move( new Position(x, y),moves.get(j), captures.get(j)));
					}
				}
			}
		}

		return ret;
	}

	/**
	 * Do a move.
	 * @param move The move.
	 */
	public boolean move(Move move)
	{
		// todo: check?
		int p = get(move.getStart());
		set(0, move.getStart()); // deixando um buraco no movimento inicial
		if (move.isJumpMove()) {
			for(Position captured: move.getCaptured()) {
				set(0, captured);
			}
		}
		// the_hole = move.getStart();
		set(p, move.getEnd());
		// moves.add(move);
		// pcs.firePropertyChange("solution", null, move);
		pcs.firePropertyChange(IBoard.MOVE, null, move);
		isWhiteTurn = !isWhiteTurn;
		return true;
	}

	/**
	 * Takeback a move.
	 */
	public boolean takeback()
	{
		if(moves.size()==0)
			return false;

		Move move = (Move)moves.get(moves.size()-1);
		int p = get(move.getEnd());
		set(p, move.getStart());
		set(0, move.getEnd());
		the_hole = move.getEnd();
		moves.remove(moves.size()-1);
		pcs.firePropertyChange("solution", null, move);
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
	 * The int [][] board represents the game board, with 0 marking
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
	// x, y, piece_col
	static int[][] move_check_table = {
		{ 1, 1, 0},
		{ -1, -1, 0},
		{ 1, -1, 0},
		{ -1, 1, 0},
		{ 2, 2, 0, 1, 1 },
		{ -2, 2, 0, -1, 1 },
		{ 2, -2, 0, 1, -1 },
		{ -2, -2, 0, -1, -1 }
	};

	/**
	 * The moves() method computes possible moves to <x:y>,
	 * represented by the Positiones of pieces to move.
	 */
	TmpDTO moves(int x, int y, int playerColorPiece)
	{
		List<List<Position>> captures = new ArrayList();
		List<Position> v = new ArrayList<Position>();
		for(int i = 0; (i<move_check_table.length); i++){

			if (playerColorPiece == 1 && (i == 0 || i == 2 || i == 4 || i == 6)) {
				continue;
			}
			if (playerColorPiece == -1 && (i == 1 || i == 3 || i == 5 || i == 7)) {
				continue;
			}

			boolean valid = check(v, move_check_table[i], x, y);
			if (valid && (i == 4 || i == 5 || i == 6 || i == 7)) {
				List<Position> tempList = new ArrayList<>();
				tempList.add(new Position(x, y));
				captures.add(tempList);
			}
		}
		return new TmpDTO(captures, v);

	}

	/**
	 * The check() method processes a move_check_table entry, and adds
	 * a Position to the Vector when the entry applies.
	 */
	boolean check(List<Position> v, int[] m, int x, int y)
	{
		int x1 = (x+m[0]);
		int y1 = (y+m[1]);
		int piece = get(x,y);
		if((get(x1, y1)==m[2]))
		{
			if(((m.length==3) || (get((x+m[3]), (y+m[4])) != piece)))
			{
				Position s = new Position(x1, y1);
				v.add(s);
				return true;
			}
		}
		return false;
	}

	int get(int x, int y)
	{
		if(((((x<0) || (x>= 8)) || (y<0)) || (y>= 8)))
			return 4;
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

	void set(int v, Position s)
	{
		set(v, s.x, s.y);
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

