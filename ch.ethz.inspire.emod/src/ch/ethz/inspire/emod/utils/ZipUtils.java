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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Implements basic functions for zip / un-zip
 * @author sizuest
 *
 */
public class ZipUtils {
	
	/**
	 * Unzips the stated file to the given folder
	 * 
	 * @param source
	 * @param targetFolder
	 * @throws IOException 
	 */
	public static void unzipFolder(String source, String targetFolder) throws IOException{
		FileInputStream fis = null;
		ZipInputStream zis = null;
		
		try{
			fis = new FileInputStream(source);
			zis = new ZipInputStream(fis);
			
			ZipEntry ze = zis.getNextEntry();
			
			while(ze!=null){
				String path = targetFolder + File.separator + ze.getName();
				
				if(!ze.isDirectory()) {
					(new File(path)).mkdirs();
					(new File(path)).delete();
					
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
			        byte[] bytesIn = new byte[1024];
			        int read = 0;
			        while ((read = zis.read(bytesIn)) != -1) {
			            bos.write(bytesIn, 0, read);
			        }
			        bos.close();
				}
				else{
					File dir = new File(path);
					dir.mkdir();
				}
				
				ze = zis.getNextEntry();
			}
			zis.close();
			fis.close();
		}
		catch (IOException ex) {
			throw ex;
		} finally {
			try {
				zis.close();
			} catch (IOException e) {
				throw e;
			}
		}
	}

	/**
	 * @param sourceFolder
	 * @param target
	 * @throws IOException 
	 */
	public static void zipFolder(String sourceFolder, String target) throws IOException{
		
		List<String> fileList = generateFileList(sourceFolder, new File(sourceFolder));
		
		byte[] buffer = new byte[1024];
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try {
			fos = new FileOutputStream(target);
			zos = new ZipOutputStream(fos);

			FileInputStream in = null;

			for (String file : fileList) {
				ZipEntry ze = new ZipEntry(file);
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
			throw ex;
		} finally {
			try {
				zos.close();
			} catch (IOException e) {
				throw e;
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
