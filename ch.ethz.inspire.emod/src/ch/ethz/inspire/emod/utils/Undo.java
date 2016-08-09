package ch.ethz.inspire.emod.utils;

import java.util.ArrayList;
/**
 * Generic implementation of a undo/redo function
 * 
 * 
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
	
	public Undo(int maxSteps, T initValue){
		this.maxSteps = maxSteps;
		clear(initValue);
	}
	
	public Undo(){
		this.maxSteps = 10;
	}
	
	public boolean undoPossible(){
		if(this.pos>0)
			return true;
		else
			return false;
	}
	
	public boolean redoPossible(){
		if(this.pos<lastStates.size()-1)
			return true;
		else
			return false;
	}
	
	public T undo(){
		if(this.pos>0)
			this.pos--;
		
		return lastStates.get(this.pos);
	}
	
	public T redo(){
		if(this.pos<this.lastStates.size()-1)
			this.pos++;
		
		return lastStates.get(this.pos);
	}
	
	public String getUndoComment(){
		if(!undoPossible())
			return "";
		return this.comments.get(pos);
	}
	
	public String getRedoComment(){
		if(!redoPossible())
			return "";
		
		return this.comments.get(pos+1);
	}
	
	public void add(T state, String comment){	
		if(null == this.lastStates){
			this.lastStates = new ArrayList<T>();
			this.comments   = new ArrayList<String>();
		}
		
		// If full: shift
		if(lastStates.size()==maxSteps){
			// Shift
			for(int i=1; i<maxSteps; i++) {
				lastStates.set(i-1, lastStates.get(i));
				comments.set(i-1, comments.get(i));
			}
			// Add
			lastStates.set(maxSteps-1, state);
			comments.set(maxSteps-1, comment);
		}
		// Append
		else{
			
			this.pos++;
			
			for(int i=lastStates.size()-1; i>=this.pos; i--){
				lastStates.remove(i);
				comments.remove(i);
			}
			
			lastStates.add(state);
			comments.add(comment);
		}
	}
	
	public T get(){
		return lastStates.get(pos);
	}	

	public void clear(T initValue){
		this.lastStates = new ArrayList<T>();
		this.comments   = new ArrayList<String>();
		
		this.lastStates.add(initValue);
		this.comments.add("");
		pos = 0;
	}
}
