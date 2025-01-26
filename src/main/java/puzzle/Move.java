package puzzle;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * A move consisting of a start and an end point, possibly capturing pieces.
 */
public class Move implements Serializable {
	// -------- attributes --------

	/** The start position. */
	protected Position start;

	/** The end position. */
	protected Position end;

	/** The list of captured positions (if any). */
	protected Position captured;

	/** Indicate if the move did a capture*/
	protected boolean jumpMove;

	// -------- constructors --------

	/**
	 * Create a move (no captures).
	 */
	public Move(Position start, Position end) {
		this.start = start;
		this.end = end;
		this.jumpMove = false;
		this.captured = null;
	}

	/**
	 * Create a move with start, end, and captured position
	 */
	public Move(Position start, Position end, Position captured) {
		this.start = start;
		this.end = end;
		this.jumpMove = true;
		this.captured = captured;
	}

	// -------- methods --------

	/**
	 * Get the start.
	 */
	public Position getStart() {
		return start;
	}

	/**
	 * Get the target.
	 */
	public Position getEnd() {
		return end;
	}

	/**
	 * Get the capture position (if any).
	 */
	public Position getCaptured() {
		if(this.jumpMove == false){
			return null;
		}
		return this.captured;
	}

	/**
	 * Show if it is a jump move.
	 */
	public boolean isJumpMove() {
		// Check if this move has any captures
		return this.jumpMove;
	}

	/**
	 * Test if two positions are equal (start and end of a move).
	 * 
	 * @return True, if equal.
	 */
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof Move) {
			Move tmp = (Move) o;
			if (tmp.getStart().equals(getStart()) && tmp.getEnd().equals(getEnd()))
				ret = true;
		}
		return ret;
	}

	/**
	 * Calculate the hash code.
	 * 
	 * @return The hash code.
	 */
	public int hashCode() {
		// todo: use xor?
		return getStart().hashCode() << 16 + getEnd().hashCode();
	}

	/**
	 * Get the string representation.
	 * 
	 * @return The string representation.
	 */
	public String toString() {
		if (isJumpMove()) {
			String capturedPositions = captured.toString();
			return "Jump from " + start + " to " + end + " capturing " + capturedPositions;
		} else {
			return "Move from " + start + " to " + end;
		}
	}
}
