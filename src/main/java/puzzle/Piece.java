package puzzle;

import java.io.Serializable;

/**
 *  A piece for playing.
 */
public class Piece	implements Serializable
{
	//-------- attributes --------

	/** The piece color (white or black). */
	protected boolean is_white;
	
	/** The piece type (normal or queen). */
    protected boolean is_queen;

	//-------- constructors --------

	/**
	 *  Create a new piece.
	 */
	public Piece(boolean is_white)
	{
		this.is_white = is_white;
		this.is_queen = false;
	}

	//-------- methods --------

	/**
	 *  Test, if it is a white piece.
	 *  @return True, if it a white piece.
	 */
	public boolean isWhite()
	{
		return is_white;
	}

	/**
	 *  Test, if it is a queen piece.
	 *  @return True, if it a queen piece.
	 */
	public boolean isQueen()
	{
		return is_queen;
	}

	/**
     * Promote the piece to a queen.
     */
    public void promoteToQueen() {
        this.is_queen = true;
    }

	/**
	 *  Get the string representation.
	 *  @return The string representation.
	 */
	public String toString()
	{
		String color = isWhite() ? "white" : "black";
		String type = isQueen() ? "queen" : "normal";
		return color + " " + type;
	}
}
