package ch.ethz.inspire.emod.utils;

import java.util.ArrayList;

/**
 * Generic implementation of a undo/redo function
 * 
 * The position pos points to the actual state in the states array. Each state
 * is assigned a comment.
 * 
 * @author sizuest
 * 
 * @param <T>
 */

public class Undo<T extends Cloneable> {
	private int maxSteps;
	private int pos;
	private ArrayList<T> lastStates = new ArrayList<T>();
	private ArrayList<String> comments = new ArrayList<String>();
	
	/**
	 * Constructor for Unmarshaller
	 */
	public Undo(){}

	/**
	 * New undo for object type T
	 * 
	 * @param maxSteps
	 * @param initValue
	 */
	public Undo(int maxSteps, T initValue) {
		this.maxSteps = maxSteps;
		clear(initValue);
	}

	/**
	 * Returns whether an undo is available / possible
	 * 
	 * @return
	 */
	public boolean undoPossible() {
		if (this.pos > 0)
			return true;
		else
			return false;
	}

	/**
	 * Returns whether an redo is available / possible
	 * 
	 * @return
	 */
	public boolean redoPossible() {
		if (this.pos < lastStates.size() - 1)
			return true;
		else
			return false;
	}

	/**
	 * Performs an undo step
	 * 
	 * @return
	 */
	public T undo() {
		if (this.pos > 0)
			this.pos--;

		return lastStates.get(this.pos);
	}

	/**
	 * Performs a redo step
	 * 
	 * @return
	 */
	public T redo() {
		if (this.pos < this.lastStates.size() - 1)
			this.pos++;

		return lastStates.get(this.pos);
	}

	/**
	 * Returns the comment connected to the undo step
	 * 
	 * @return
	 */
	public String getUndoComment() {
		if (!undoPossible())
			return "";
		return this.comments.get(pos);
	}

	/**
	 * Returns the comment connected to the redo step
	 * 
	 * @return
	 */
	public String getRedoComment() {
		if (!redoPossible())
			return "";

		return this.comments.get(pos + 1);
	}

	/**
	 * Add a new state (last state becomes undo state)
	 * 
	 * @param state
	 * @param comment
	 */
	public void add(T state, String comment) {
		if (null == this.lastStates) {
			this.lastStates = new ArrayList<T>();
			this.comments = new ArrayList<String>();
		}

		// If full: shift
		if (lastStates.size() == maxSteps) {
			// Shift
			for (int i = 1; i < maxSteps; i++) {
				lastStates.set(i - 1, lastStates.get(i));
				comments.set(i - 1, comments.get(i));
			}
			// Add
			lastStates.set(maxSteps - 1, state);
			comments.set(maxSteps - 1, comment);
		}
		// Append
		else {

			this.pos++;

			for (int i = lastStates.size() - 1; i >= this.pos; i--) {
				lastStates.remove(i);
				comments.remove(i);
			}

			lastStates.add(state);
			comments.add(comment);
		}
	}

	/**
	 * Returns the actual state
	 * 
	 * @return
	 */
	public T get() {
		return lastStates.get(pos);
	}

	/**
	 * Clear all non active states
	 * 
	 * @param initValue
	 */
	public void clear(T initValue) {
		this.lastStates = new ArrayList<T>();
		this.comments = new ArrayList<String>();

		this.lastStates.add(initValue);
		this.comments.add("");
		pos = 0;
	}
}
