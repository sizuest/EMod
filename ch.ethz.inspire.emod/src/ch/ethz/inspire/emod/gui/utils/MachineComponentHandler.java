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
package ch.ethz.inspire.emod.gui.utils;

import java.io.File;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ch.ethz.inspire.emod.utils.PropertiesHandler;

/**
 * Handler class for machine component GUIs
 * @author sizuest
 *
 */
public class MachineComponentHandler {

 	/**
	 * fill a tree element with the machine component from the DB
	 * @param aTree	tree element to fill
	 */ 	
	public static void fillMachineComponentTree(Tree aTree){
		fillMachineComponentTree("", aTree);
	}
	
	/**
	 * fill a tree element with the machine component from the DB
	 * @param type 
	 * @param aTree	tree element to fill
	 */ 	
	public static void fillMachineComponentTree(String type, Tree aTree){
		
		//read machinecomponent db folder from the current path
		String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/";
		File dir = new File(path);		
		File[] subDirs = dir.listFiles();
		Arrays.sort(subDirs);
			
		//iterate twice, first over categories of machinecomponents, second over the parameter sets of each component
		for (int i = 0; i < subDirs.length; i++){
			//leave out the SimulationControl-Folder and SimulationControl-Folder
			if(subDirs[i].getName().contains("ThermalTest"))
				continue;
			else if (subDirs[i].getName().contains("SimulationControl"))
				continue;
			
			// If type is non empty
			if(!type.matches("") && !subDirs[i].getName().matches(type+"[a-zA-Z]*"))
				continue;
				
			//read the different parameter sets from subfolders
			String subpath = path + subDirs[i].getName() + "/";
			dir = new File(subpath);
			File[] subDirsComponents = dir.listFiles();
			Arrays.sort(subDirsComponents);			
			
			//write all categories of components as childs into the tree
			TreeItem child = new TreeItem(aTree, SWT.NONE);
			child.setText(subDirs[i].getName());
			
			//append parameter sets to their parent
			for(int j = 0; j < subDirsComponents.length; j++){
				TreeItem grandChild = new TreeItem(child, SWT.NONE);
				grandChild.setText(subDirsComponents[j].getName().replace(child.getText()+"_", "").replace(".xml", ""));
			}
		} 
	}
	
	/**
	 * fill a tree element with the simulation inputs from the DB
	 * @param aTree
	 */
	public static void fillInputsTree(Tree aTree){
		
		//read machinecomponent db folder from the current path
		String path = PropertiesHandler.getProperty("app.MachineComponentDBPathPrefix") + "/";
		File dir = new File(path);		
		File[] subDirs = dir.listFiles();
		Arrays.sort(subDirs);
			
		//iterate twice, first over categories of machinecomponents, second over the parameter sets of each component
		for (int i = 0; i < subDirs.length; i++){
			//leave out the SimulationControl-Folder and SimulationControl-Folder
			if(subDirs[i].getName().contains("SimulationControl")){
				//read the different parameter sets from subfolders
				String subpath = path + subDirs[i].getName() + "/";
				dir = new File(subpath);
				File[] subDirsComponents = dir.listFiles();
				Arrays.sort(subDirsComponents);			
				
				//write all categories of components as childs into the tree
				//TreeItem child = new TreeItem(aTree, SWT.NONE);
				//child.setText(subDirs[i].getName());
				
				//append parameter sets to their parent
				for(int j = 0; j < subDirsComponents.length; j++){
					TreeItem grandChild = new TreeItem(aTree, SWT.NONE);
					grandChild.setText(subDirsComponents[j].getName().replace("_Example.xml", ""));
				}
			}
		} 
	}
}
