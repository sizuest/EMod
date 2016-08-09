package ch.ethz.inspire.emod.model.parameters;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.inspire.emod.model.units.SiUnit;

public class ParameterSet {
	private String name;
	private String comment;
	private Map<String, PhysicalValue> map = new HashMap<String, PhysicalValue>();
	
	/**
	 * Constructor for marshaller
	 */
	public ParameterSet() {}
	
	
	public ParameterSet(String name){
		this.name = name;
	}
	
	/**
	 * Returns the parameter sets name
	 * @return name
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Sets the parameter set comment
	 * @param comment
	 */
	public void setComment(String comment){
		this.comment = comment;
	}
	
	/**
	 * Returns the parameter set comment
	 * @return
	 */
	public String getComment(){
		return this.comment;
	}
	
	/**
	 * Sets the physical value with the stated name
	 * @param name
	 * @param value
	 */
	public void setPhysicalValue(String name, PhysicalValue value){
		map.put(name, value);
	}
	/**
	 * Sets the physical value with the stated name
	 * @param name
	 * @param value
	 * @param unit
	 */
	public void setPhysicalValue(String name, double[] value, SiUnit unit){
		setPhysicalValue(name, new PhysicalValue(value, unit));
	}
	/**
	 * Sets the physical value with the stated name
	 * @param name
	 * @param value
	 * @param unit
	 */
	public void setPhysicalValue(String name, double value, SiUnit unit){
		setPhysicalValue(name, new PhysicalValue(value, unit));
	}
	/**
	 * Sets the physical value with the stated name
	 * @param name
	 * @return
	 */
	public PhysicalValue getPhysicalValue(String name){
		return map.get(name);
	}
	
	public Map<String, PhysicalValue> getParameterSet(){
		return this.map;
	}
}
