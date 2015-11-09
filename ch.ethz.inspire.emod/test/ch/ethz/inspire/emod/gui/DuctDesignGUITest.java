package ch.ethz.inspire.emod.gui;

import org.eclipse.swt.widgets.Display;
import org.junit.Test;

public class DuctDesignGUITest {
	@Test
	public void testGUI(){
		Display disp = new Display();

		(new DuctDesignGUI()).editDuctGUI("Test");

		//disp.dispose();
	}

}
