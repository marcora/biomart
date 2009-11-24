package org.biomart.processors;

public class Fasta {

	/**
	 * @param args
	 * JONATHAN:
	 * you need to look into FASTA.pm formatter in biomart-perl
	 * see how the sequences are obtained/processed in GenomicSequence.pm
	 * the coordinates for the sequences are taken from respective exportables
	 * in gene_ensembl dataset of ensembl_mart_55
	 * 
	 * the example exportables, each representing a sequence type, are:
	 * peptide, coding, cdna etc
	 * 
	 * you would have to make certain assumptions here,
	 * 
	 * assume that you hvae the coordinates, from the database table 
	 * (you will find this by looking into the exportable attributeList)
	 * 
	 * you will also need the Sequence itself. The sequences live in
	 * sequence_mart_55. Hence you would need jdbc connection to this mart
	 * to retrieve the sequence.
	 * 
	 * please feel free to investigate how biomart-perl does it before 
	 * writing this 
	 * Good Luck!
	 * 
	 *
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
