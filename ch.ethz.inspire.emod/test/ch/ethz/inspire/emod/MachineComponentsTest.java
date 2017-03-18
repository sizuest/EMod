/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date$
 * $Rev$
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import ch.ethz.inspire.emod.model.MachineComponent;

/**
 * @author dhampl
 *
 */
public class MachineComponentsTest {

	/**
	 * Test method for {@link Machine#getMachineComponent(String)}.
	 */
	@Test
	public void testGetComponent() {
		MachineComponent mc = new MachineComponent("test");
		MachineComponent mc1 = new MachineComponent("test1");
		MachineComponent mc2 = new MachineComponent("test2");
		ArrayList<MachineComponent> list = new ArrayList<MachineComponent>();
		list.add(mc);
		list.add(mc2);
		list.add(mc1);
		Machine.dummyBuildMachine();
		Machine.getInstance().setMachineComponentList(list);
		try {
			assertEquals("get component by name", mc2, Machine.getMachineComponent("test2"));
			assertSame("get component by name", mc2, Machine.getMachineComponent("test2"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
