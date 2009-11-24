package org.biomart.old.bioMartPortalLinks;


import java.io.File;

import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.helpers.Rdbs;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.SuperTable2;
import org.biomart.test.linkIndicesTest.program.DatabaseSchema;
import org.biomart.test.linkIndicesTest.program.LinkIndexesParameters;
import org.biomart.test.linkIndicesTest.program.LinkIndicesTestEnvironmentResult;
import org.biomart.test.linkIndicesTest.program.MartServiceSchema;
import org.biomart.test.linkIndicesTest.program.Mode;
import org.biomart.test.linkIndicesTest.program.MultipleDatasetJoinCoordinator;


public class MultiBatchingLinkIndexCreation {

	public static void main(String[] args) {
		MultiBatchingLinkIndexCreation test = new MultiBatchingLinkIndexCreation();
		try {
			test.process();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void process() throws Exception {
		
		/*
name = uniprot_id,		biDirectional = true,
	left = 
			bioMartVersion = 0.5,	virtualSchemaName = pancreas_expression_db,	martName = Pancreatic_Expression,	datasetName = scerevisiae_gene_ensembl
		importable = {linkName = uniprot_id, linkVersion = , defaultValue = false, list = [uniprot_swissprot_accession]}
	right = 
			bioMartVersion = 0.5,	virtualSchemaName = pancreas_expression_db,	martName = Pancreatic_Expression,	datasetName = olatipes_gene_ensembl
		importable = null,	exportable = {linkName = uniprot_id, linkVersion = , defaultValue = false, list = [uniprot_swissprot_accession]}

name = uniprot_id,		biDirectional = true,
	left = 
			bioMartVersion = 0.5,	virtualSchemaName = pancreas_expression_db,	martName = Pancreatic_Expression,	datasetName = olatipes_gene_ensembl
		importable = {linkName = uniprot_id, linkVersion = , defaultValue = false, list = [uniprot_swissprot_accession]}
	right = 
			bioMartVersion = 0.5,	virtualSchemaName = pancreas_expression_db,	martName = Pancreatic_Expression,	datasetName = scerevisiae_gene_ensembl
		importable = null,	exportable = {linkName = uniprot_id, linkVersion = , defaultValue = false, list = [uniprot_swissprot_accession]}
		
		
		
		
		
4 @@@@@@@@@@@@@@@@@@@ 19499, 744, 744 tot, 4961ms, complex_db_id, complex, complex_db_id_key, interaction, id_complex_db_id__dm_value
name = complex_db_id,		biDirectional = true,
	left = 
			bioMartVersion = 0.6,	virtualSchemaName = default,	martName = REACTOME,	datasetName = interaction
		importable = {linkName = complex_db_id, linkVersion = , defaultValue = false, list = [complex_db_id_list]}
	right = 
			bioMartVersion = 0.6,	virtualSchemaName = default,	martName = REACTOME,	datasetName = complex
		importable = null,	exportable = {linkName = complex_db_id, linkVersion = , defaultValue = true, list = [complex_db_id_key]}

name = complex_db_id,		biDirectional = true,
	left = 
			bioMartVersion = 0.6,	virtualSchemaName = default,	martName = REACTOME,	datasetName = complex
		importable = {linkName = complex_db_id, linkVersion = , defaultValue = false, list = [complex_db_id_list]}
	right = 
			bioMartVersion = 0.6,	virtualSchemaName = default,	martName = REACTOME,	datasetName = interaction
		importable = null,	exportable = {linkName = complex_db_id, linkVersion = , defaultValue = true, list = [id_complex_db_id__dm_value]}
		
		
5 @@@@@@@@@@@@@@@@@@@ 19499, 19202, 19202 tot, 13860ms, complex_db_id, complex, complex_db_id_key, reaction, db_id
		name = complex_db_id,		biDirectional = true,
	left = 
			bioMartVersion = 0.6,	virtualSchemaName = default,	martName = REACTOME,	datasetName = complex
		importable = {linkName = complex_db_id, linkVersion = , defaultValue = false, list = [complex_db_id_list]}
	right = 
			bioMartVersion = 0.6,	virtualSchemaName = default,	martName = REACTOME,	datasetName = reaction
		importable = null,	exportable = {linkName = complex_db_id, linkVersion = , defaultValue = true, list = [db_id]}

name = complex_db_id,		biDirectional = true,
	left = 
			bioMartVersion = 0.6,	virtualSchemaName = default,	martName = REACTOME,	datasetName = reaction
		importable = {linkName = complex_db_id, linkVersion = , defaultValue = false, list = [complex_db_id_list]}
	right = 
			bioMartVersion = 0.6,	virtualSchemaName = default,	martName = REACTOME,	datasetName = complex
		importable = null,	exportable = {linkName = complex_db_id, linkVersion = , defaultValue = true, list = [complex_db_id_key]}
		
		
		*/
		
		
		DatabaseSchema localDatabase = new DatabaseSchema(Rdbs.MYSQL, MyConstants.BMTEST_SERVER, 3306, "martadmin", "biomart", "ac_link");
	
		/*SuperTable2 superTable2_1 = new SuperTable2(MART_SERVICE_SERVER, "default", "complex", "0.7", "complex_db_id_key", "complex_db_id_key");
		SuperTable2 superTable2_2 = new SuperTable2(MART_SERVICE_SERVER, "default", "pathway", "0.7", "db_id", "db_id");*/
		
		/*SuperTable2 superTable2_1 = new SuperTable2(MART_SERVICE_SERVER, "pancreas_expression_db", "scerevisiae_gene_ensembl", "0.5", "uniprot_swissprot_accession", "uniprot_swissprot_accession");
		SuperTable2 superTable2_2 = new SuperTable2(MART_SERVICE_SERVER, "pancreas_expression_db", "olatipes_gene_ensembl", "0.5", "uniprot_swissprot_accession", "uniprot_swissprot_accession");*/
		
		SuperTable2 superTable2_1 = new SuperTable2(MartServiceConstants.BMTEST_MART_SERVICE_STRING_URL, "default", "interaction", "0.6", "id_complex_db_id__dm_value", "complex_db_id_list");
		SuperTable2 superTable2_2 = new SuperTable2(MartServiceConstants.BMTEST_MART_SERVICE_STRING_URL, "default", "complex", "0.6", "complex_db_id_key", "complex_db_id_list");

		LinkIndexesParameters params = new LinkIndexesParameters(
				localDatabase, Mode.LINK_INDEX_CREATION, /*100000000*/100, 1.0, 1, 	// 100Mo No increasing size of batching possible anyway
				false, true,
				new MartServiceSchema(superTable2_1, false, null, null), 
				new MartServiceSchema(superTable2_2, false, null, null));
		
		String linkIndexTableName = "my_link_index_table";
		MultipleDatasetJoinCoordinator coordinator = new MultipleDatasetJoinCoordinator(params);
		File createIndextemporaryFile = File.createTempFile(linkIndexTableName + MyUtils.INFO_SEPARATOR, ".txt", new File(MyUtils.OUTPUT_FILES_PATH));
		LinkIndicesTestEnvironmentResult result = coordinator.joinMultipleDataset(false, false, true, createIndextemporaryFile);		
		//createIndextemporaryFile.deleteOnExit();
		System.out.println(result);
	}
}
