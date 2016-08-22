package edu.scripps.p3.inputgenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class GetUniprotAccessionNumberAndGeneNames {
	public static void main(String[] args){
		
		try{
			BufferedReader in = new BufferedReader(new FileReader(new File("../uniprot-mouse-SALVA.fasta")));
			String s = in.readLine();
			
			BufferedWriter out = new BufferedWriter(new FileWriter(new File("../UniProt_Mouse_Accession_And_GeneName_SALVA.txt")));
					
			
			while(s!=null){
				String[]  split = s.split("\\|");
				String[] split2 = split[2].split("=");
				String GeneName = split2[2].split(" PE")[0];
				
				out.write(split[1]+"\t" + GeneName + "\n");
				out.flush();
				
				s = in.readLine();
				while(s.charAt(0)!='>'){
					s = in.readLine();
					if(s==null){
						break;
					}
				}
			}
			
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
}
