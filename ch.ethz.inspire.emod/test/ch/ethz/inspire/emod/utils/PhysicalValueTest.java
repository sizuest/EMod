package ch.ethz.inspire.emod.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class PhysicalValueTest {
	
	@Test
	public void testPhysicalValue() throws Exception{
		
		PhysicalValue<Double> pv1 = new PhysicalValue<Double>();
		PhysicalValue<Double> pv2 = new PhysicalValue<Double>();
		PhysicalValue<Double> pv3 = new PhysicalValue<Double>();
		
		// Values
		pv1.set(2.0,"m^2");
		pv2.set(3.1, "N");
		pv3.set(1.0,"m^2");
		
		// Test
		PhysicalValue<Double> res1 = PhysicalValue.add(pv1, pv3);
		PhysicalValue<Double> res2 = PhysicalValue.subtract(pv1, pv3);
		PhysicalValue<Double> res3 = PhysicalValue.multiply(pv1, pv2);
		PhysicalValue<Double> res4 = PhysicalValue.divide(pv2, pv1);
		PhysicalValue<Double> res5 = PhysicalValue.pow(pv1, 0.5);
		
		// Check
		assertEquals("Add", 3, res1.value,0);
		assertEquals("Sub", 1, res2.value,0);
		assertEquals("Mul", 6.2, res3.value,0);
		assertEquals("Div", 1.55, res4.value,0);
		assertEquals("Pow", Math.pow(2, 0.5), res5.value,0.00001);
		
		assertEquals("AddUnit", "m^2", res1.unit.toString());
		assertEquals("SubUnit", "m^2", res2.unit.toString());
		assertEquals("MulUnit", "m^3 kg s^2", res3.unit.toString());
		assertEquals("DivUnit", "m^-1 kg s^2", res4.unit.toString());
		assertEquals("PowUnit", "m", res5.unit.toString());
		
		System.out.println(res1.unit.toString());
		System.out.println(res2.unit.toString());
		System.out.println(res3.unit.toString());
		System.out.println(res4.unit.toString());
		System.out.println(res5.unit.toString());
		
	}

}
