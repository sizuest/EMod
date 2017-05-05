package ch.ethz.inspire.emod.utils;

import org.junit.Test;

import ch.ethz.inspire.emod.model.parameters.PhysicalValue;
import static org.junit.Assert.assertEquals;

/**
 * 
 * @author simon
 *
 */
public class PhysicalValueTest {
	
	
	/**
	 * Test method for {@link ch.ethz.inspire.emod.model.parameters.PhysicalValue}
	 * @throws Exception
	 */
	@Test
	public void testPhysicalValue() throws Exception{
		
		PhysicalValue pv1 = new PhysicalValue();
		PhysicalValue pv2 = new PhysicalValue();
		PhysicalValue pv3 = new PhysicalValue();
		
		// Values
		pv1.set(2.0,"m^2");
		pv2.set(3.1, "N");
		pv3.set(1.0,"m^2");
		
		// Test
		PhysicalValue res1 = PhysicalValue.add(pv1, pv3);
		PhysicalValue res2 = PhysicalValue.subtract(pv1, pv3);
		PhysicalValue res3 = PhysicalValue.multiply(pv1, pv2);
		PhysicalValue res4 = PhysicalValue.divide(pv2, pv1);
		PhysicalValue res5 = PhysicalValue.pow(pv1, 0.5);
		
		// Check
		assertEquals("Add", 3, res1.getValue(),0);
		assertEquals("Sub", 1, res2.getValue(),0);
		assertEquals("Mul", 6.2, res3.getValue(),0);
		assertEquals("Div", 1.55, res4.getValue(),0);
		assertEquals("Pow", Math.pow(2, 0.5), res5.getValue(),0.00001);
		
		assertEquals("AddUnit", "m^2", res1.getUnit().toString());
		assertEquals("SubUnit", "m^2", res2.getUnit().toString());
		assertEquals("MulUnit", "m^3 kg s^-2", res3.getUnit().toString());
		assertEquals("DivUnit", "Pa", res4.getUnit().toString());
		assertEquals("PowUnit", "m", res5.getUnit().toString());
		
		System.out.println(res1.getUnit().toString());
		System.out.println(res2.getUnit().toString());
		System.out.println(res3.getUnit().toString());
		System.out.println(res4.getUnit().toString());
		System.out.println(res5.getUnit().toString());
		
	}
	
	/**
	 * Test method for the file handling in
	 * {@link ch.ethz.inspire.emod.model.parameters.PhysicalValue}
	 */
	@Test
	public void rwPhysicalUnit(){
		PhysicalValue pvF   = new PhysicalValue();
		PhysicalValue pvV   = new PhysicalValue();
		PhysicalValue pvQ   = new PhysicalValue();
		
		double[] Q = {.001, .002};
		
		pvF.set(1.0,  "N");
		pvV.set(5.0,  "Pa s");
		pvQ.set( Q, "m^3/s");
		
		System.out.println(pvF.getUnit().toString());
		System.out.println(pvV.getUnit().toString());
		System.out.println(pvQ.getUnit().toString());
		
		try {
			ComponentConfigReader param = new ComponentConfigReader("Test", "Test");
			// Save
			param.setValue("Force", pvF);
			param.setValue("Viscosity", pvV);
			param.setValue("FlowRate", pvQ);
			//Load
			pvF = param.getPhysicalValue("Force");
			pvV = param.getPhysicalValue("Viscosity");
			pvQ = param.getPhysicalValue("FlowRate");
			
			param.Close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals("Value", 1, pvF.getValue(), 0);
		assertEquals("Unit", "N", pvF.getUnit().toString());
		
		assertEquals("Value", 5, pvV.getValue(), 0);
		assertEquals("Unit", "Pas", pvV.getUnit().toString());
		
		
	}

}
