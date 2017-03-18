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
package ch.ethz.inspire.emod.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Implements basic functions for zip / un-zip
 * @author sizuest
 *
 */
public class ZipUtils {

	/**
	 * @param sourceFolder
	 * @param target
	 */
	public static void zipFolder(String sourceFolder, String target){
		
		List<String> fileList = generateFileList(sourceFolder, new File(sourceFolder));
		
		byte[] buffer = new byte[1024];
		String source = "";
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try {
			try {
				source = sourceFolder.substring(
						 sourceFolder.lastIndexOf("\\") + 1,
						 sourceFolder.length());
			} catch (Exception e) {
				source = sourceFolder;
			}
			fos = new FileOutputStream(target);
			zos = new ZipOutputStream(fos);

			FileInputStream in = null;

			for (String file : fileList) {
				ZipEntry ze = new ZipEntry(source + File.separator + file);
				zos.putNextEntry(ze);
				try {
					in = new FileInputStream(sourceFolder + File.separator + file);
					int len;
					while ((len = in.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				} finally {
					in.close();
				}
			}

			zos.closeEntry();

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				zos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param node
	 */
	private static List<String> generateFileList(String sourceFolder, File node) {
		
		List<String> fileList = new ArrayList<String>();

		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(sourceFolder, node.toString()));

		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				fileList.addAll(generateFileList(sourceFolder, new File(node, filename)));
			}
		}
		
		return fileList;
	}

	private static String generateZipEntry(String sourceFolder, String file) {
		return file.substring(sourceFolder.length() + 1, file.length());
	}
}
