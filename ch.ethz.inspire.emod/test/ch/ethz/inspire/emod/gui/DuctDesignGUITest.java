package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.widgets.Display;
import org.junit.Test;

import ch.ethz.inspire.emod.gui.dd.DuctDesignGUI;

public class DuctDesignGUITest {
	@Test
	public void testGUI(){
		Display disp = new Display();

		(new DuctDesignGUI()).editDuctGUI("Test");

		//disp.dispose();
	}

}
