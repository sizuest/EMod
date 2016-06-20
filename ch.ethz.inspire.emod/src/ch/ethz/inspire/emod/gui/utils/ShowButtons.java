package ch.ethz.inspire.emod.gui.utils;

public class ShowButtons {
	public static final int NONE         = 0; 
	public static final int OK           = 1;
	public static final int RESET        = 2;
	public static final int CANCEL       = 4;
	public static final int ALL          = 7;
	
	public static boolean ok(int button){
		return (1 & button)>0;
	}
	
	public static boolean reset(int button){
		return (2 & button)>0;
	}
	
	public static boolean cancel(int button){
		return (4 & button)>0;
	}
	
	public static int count(int button){
		int count = Integer.bitCount(button);
		return count;
	}
	
}
