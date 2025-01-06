package puzzle;

import java.util.Comparator;

/**
 *  Sort moves according to a strategy.
 */
public class MoveComparator implements Comparator<Move>
{
    //-------- constants --------

    /** No strategy: try moves in order of appearance. */
	public static final String STRATEGY_NONE	= "none";

    /** The strategy preferring moves with the highest number of captures. */
    public static final String STRATEGY_MAX_CAPTURES = "max_captures";

    /** The strategy preferring moves that involve queens over normal pieces then number of captures. */
    public static final String STRATEGY_PREFER_QUEENS = "prefer_queens";

    //-------- attributes --------

	/** The board (required for checking which piece is in a given position). */
	protected IBoard	board;
	
	/** The strategy. */
	protected String	strategy;

    //-------- constructors --------

	/**
	 *  Create a move comparator.
	 */
	public MoveComparator(IBoard board, String strategy)
	{
		this.strategy	= strategy;
		this.board	= board;
	}
	
	//-------- Coparator interface --------

    /**
     *  Compare two moves.
     *  @return A negative number when the first move should come before the second.
     */
    public int compare(Move move1, Move move2)
	{
        
        int compareCaptureCount = Integer.compare(move2.getCaptured().size(), move1.getCaptured().size());

        boolean isQueen1 = board.getPiece(move1.getStart()).isQueen();
        boolean isQueen2 = board.getPiece(move2.getStart()).isQueen();
        int compareQueen = Boolean.compare(isQueen2, isQueen1);

        int ret = 0;

        if (STRATEGY_MAX_CAPTURES.equals(strategy)) {
            ret = compareCaptureCount;
        } else if (STRATEGY_PREFER_QUEENS.equals(strategy)) {
            ret = compareQueen != 0 ? compareQueen : compareCaptureCount;
        }

        return ret;
    }
}
