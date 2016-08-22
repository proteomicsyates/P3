/**
 * diego
 * Jun 18, 2013
 */
package edu.scripps.p3.io;

import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * @author diego
 *
 */
public class excelIO {

	public Workbook loadExcel(File f) {

		Workbook book = null;
		try {
			book = Workbook.getWorkbook(f);
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return book;

	}
	
	public WritableWorkbook createWorkbook(File out) {

		WritableWorkbook workbook = null;

		try {
			workbook = Workbook.createWorkbook(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return workbook;
	}
	
	public void writeLabel(WritableWorkbook book, WritableSheet sheet, int column, int row, String text) {
		
		Label label = new Label(column, row, text);
		try {
			sheet.addCell(label);
		} catch (RowsExceededException e) {

			e.printStackTrace();
		} catch (WriteException e) {

			e.printStackTrace();
		}
	}
	
	public void writeExcel(WritableWorkbook book) {
		try {
			book.write();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			book.close();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
