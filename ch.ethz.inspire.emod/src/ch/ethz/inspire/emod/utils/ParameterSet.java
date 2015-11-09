package ch.ethz.inspire.emod.utils;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.inspire.emod.model.units.PhysicalValue;
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
	
	public String getName(){
		return this.name;
	}
	
	public void setComment(String comment){
		this.comment = comment;
	}
	
	public String getComment(){
		return this.comment;
	}
	
	public void setParameter(String name, PhysicalValue value){
		map.put(name, value);
	}
	
	public void setParameter(String name, double[] value, SiUnit unit){
		setParameter(name, new PhysicalValue(value, unit));
	}
	
	public void setParameter(String name, double value, SiUnit unit){
		setParameter(name, new PhysicalValue(value, unit));
	}
	
	public PhysicalValue getParameter(String name){
		return map.get(name);
	}
	
	public Map<String, PhysicalValue> getParameterSet(){
		return this.map;
	}
}
