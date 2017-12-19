/**
 * diego
 * Jun 11, 2013
 */
package edu.scripps.p3.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JFileChooser;

/**
 * @author diego
 * 
 */
public class MyFileChooser {

	private File curdir=null;
	
	public MyFileChooser(){ 
		
	}
	
	public MyFileChooser(File curdir) {
		this.curdir = curdir;
	}
	
	/**
	 * 
	 * @param title require dialog title
	 * @return output directory, error code -2 if not 
	 */
	public File getOutdir(String title) {

		JFileChooser fc = new JFileChooser();
		File f = null;
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle(title);
		fc.setCurrentDirectory(curdir);
		int returnval = fc.showSaveDialog(null);
		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		} else {
			System.exit(-2);
		}
		return f;

	}

	public File openFile(String t) {

		File f = null;

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle(t);
		fc.setCurrentDirectory(curdir);
		int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
			curdir = f.getAbsoluteFile();
		}

		return f;
	}

	public File openDir(String t) {

		File f = null;

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle(t);
		fc.setCurrentDirectory(curdir);
		int returnval = fc.showOpenDialog(null);

		if (returnval == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
			curdir = f.getAbsoluteFile();
		} else {
			f = null;
		}

		return f;

	}

	public void writeOut(File outdir, String name, String output) {

		File fout = new File(outdir, name);
		try {
			Writer out = new BufferedWriter(new FileWriter(fout));
			out.write(output);
			out.close();

		} catch (IOException e) {
			System.err.println("probelm writing log");
			e.printStackTrace();
		}

	}
	
	public File getCurDir() {
		return curdir;
	}
	
	public void setCurDir(File curdir) {
		this.curdir = curdir;
	}
	
	public void writeLog(File outdir, StringBuffer log, String title) {

		File fout = new File(outdir, title + ".txt");

		try {
			Writer out = new BufferedWriter(new FileWriter(fout));
			out.write(log.toString());
			out.close();

		} catch (IOException e) {
			System.err.println("probelm writing log");
			e.printStackTrace();
		}

	}
	
	public void writeLog(File outdir, StringBuilder log, String title) {

		File fout = new File(outdir, title + ".txt");

		try {
			Writer out = new BufferedWriter(new FileWriter(fout));
			out.write(log.toString());
			out.close();

		} catch (IOException e) {
			System.err.println("probelm writing log");
			e.printStackTrace();
		}

	}
	
	public void saveTopology(File outdir, String map, String title) {
		
		File fout = new File(outdir, title + ".dat");

		try {
			Writer out = new BufferedWriter(new FileWriter(fout));
			out.write(map);
			out.close();

		} catch (IOException e) {
			System.err.println("probelm writing log");
			e.printStackTrace();
		}
	}

}
