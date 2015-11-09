package ch.ethz.inspire.emod.model.fluid;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.ethz.inspire.emod.model.material.Material;

public class DuctTest {
	
	@Test
	public void testInitSaveLoad(){
		Duct duct1 = new Duct("Test");
		Duct duct2;
		
		ADuctElement ductE = new DuctDrilling("Sinkleitung",.005, .2, 2);
		ductE.setIsolation(new Isolation("PUR", .005));
		duct1.addElement(new DuctDrilling("Sinkleitung",.005, .05, 2));
		duct1.addElement(ductE);
		duct1.addElement(new DuctHelix("Statorkühlung", .1, .2, .03, new HPRectangular(.02, .003)));
		duct1.addElement(new DuctDrilling("Steigleitung",.005, .05, 2));
		
		assertEquals("Number of elements", 6, duct1.getElements().size(), 0);
		
		duct1.save();
		
		duct2 = Duct.buildFromFile("Test");
	}
	
	@Test
	public void testRemove(){
		Duct duct = new Duct("Test");

		duct.addElement(new DuctDrilling("Sinkleitung1",.005, .05, 2));
		duct.addElement(new DuctDrilling("Sinkleitung2",.005, .2, 2));
		duct.addElement(new DuctHelix("Statorkühlung", .1, .2, .03, new HPRectangular(.02, .003)));
		duct.addElement(new DuctDrilling("Steigleitung",.005, .05, 2));
		
		assertEquals("Number of elements", 6, duct.getElements().size(), 0);
		
		duct.removeElement("Sinkleitung1");
		
		assertEquals("Number of elements", 5, duct.getElements().size(), 0);
		
		duct.removeElement("Statorkühlung");
		
		assertEquals("Number of elements", 2, duct.getElements().size(), 0);
	}
	
	@Test
	public void testSetMaterial(){
		Duct duct = new Duct("Test");
		
		duct.addElement(new DuctDrilling("Sinkleitung",.005, .25, 2));
		duct.addElement(new DuctHelix("Statorkühlung", .1, .2, .03, new HPRectangular(.02, .003)));
		
		duct.setMaterial(new Material("Water"));
		
		
		assertTrue("Material", duct.getElement(0).getMaterial().equals(duct.getElement(1).getMaterial()));
	}
	
	@Test
	public void testHTC(){
		Duct duct1 = new Duct("Test");
		duct1.setMaterial(new Material("Water"));
		
		ADuctElement ductE = new DuctDrilling("Steigleitung",.005, .05, 2);
		ductE.setIsolation(new Isolation("PUR", .005));
		
		duct1.addElement(new DuctDrilling("Sinkleitung",.005, .25, 2));
		duct1.addElement(new DuctHelix("Statorkühlung", .1, .2, .03, new HPRectangular(.02, .003)));
		duct1.addElement(ductE);
		
		double htc1, htc2;
		
		htc1 = duct1.getThermalResistance(0, 1E5, 293.15, 293.15);
		htc2 = duct1.getThermalResistance(1.0/60000, 1E5, 293.15, 293.15);
		
		assertEquals("No Flow", 0, htc1, 0);
		assertEquals("1 l/min", 2600, htc2, 100);
	}
	
	@Test
	public void testPressureLoss(){
		
		Duct duct1 = new Duct("Test");
		duct1.setMaterial(new Material("Water"));
		
		ADuctElement ductE = new DuctDrilling("Steigleitung",.005, .05, 2);
		ductE.setIsolation(new Isolation("PUR", .005));
		
		duct1.addElement(new DuctDrilling("Sinkleitung",.005, .25, 2));
		duct1.addElement(new DuctHelix("Statorkühlung", .1, .2, .03, new HPRectangular(.02, .003)));
		duct1.addElement(ductE);
		
		double dp1, dp2;
		
		dp1 = duct1.getPressureDrop(0, 1E5, 293.15, 293.15);
		dp2 = duct1.getPressureDrop(1.0/60000, 1E5, 293.15, 293.15);
		
		assertEquals("No Flow", 0, dp1, 0);
		assertEquals("1 l/min", 65E3, dp2, 1E3);
		
	}

}
