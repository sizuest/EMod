/***********************************
 * $Id$
 *
 * $URL$
 * $Author$
 * $Date: 2014-10-30 16:24:44$
 * 
 *
 * Copyright (c) 2011 by Inspire AG, ETHZ
 * All rights reserved
 *
 ***********************************/
package ch.ethz.inspire.emod.gui.utils;

import java.io.File;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ch.ethz.inspire.emod.utils.PropertiesHandler;

public class MaterialHandler {

 	/**
	 * fill a tree element with the machine component from the DB
	 * @param aTree	tree element to fill
	 */ 	
	public static void fillTree(Tree aTree){
		
		//read material db folder from the current path
		String path = PropertiesHandler.getProperty("app.MaterialDBPathPrefix") + "/";
		File dir = new File(path);		
		File[] subDirsMaterials = dir.listFiles();
		Arrays.sort(subDirsMaterials);
		
		//iterate over existing Materials
		for (int i = 0; i < subDirsMaterials.length; i++){
			TreeItem child = new TreeItem(aTree, SWT.NONE);
			child.setText(subDirsMaterials[i].getName());
		}
	}
}
