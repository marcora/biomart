package org.biomart.old.martService.restFulQueries;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.UniqueId;
import org.biomart.old.martService.restFulQueries.objects.Attribute;
import org.biomart.old.martService.restFulQueries.objects.Filter;



@Deprecated
public class RestFul {
	
	public static void main(String[] args) {
	}

	public RestFulQuery martServiceRestFulQuery = null;
	
	public RestFul(RestFulQuery restFulQuery) throws MalformedURLException, UnsupportedEncodingException {
		this.martServiceRestFulQuery = restFulQuery;
		this.martServiceRestFulQuery.buildQuery();
	}
	public RestFul() {
	}
	
	public List<List<String>> getListData (boolean display) throws MalformedURLException, IOException {
		if (display) {
			System.out.println(martServiceRestFulQuery.getReadableUrl());
		}
		URL url = this.martServiceRestFulQuery.getUrlGet();
		List<List<String>> listData = MyUtils.copyUrlContentToListStringList(url, MyUtils.TAB_SEPARATOR);
		return listData;
	}
	
	public Set<List<String>> getSetData () throws MalformedURLException, IOException {
		return getSetData(true);
	}
	public Set<List<String>> getSetData (boolean display) throws MalformedURLException, IOException {
		if (display) {
			System.out.println(martServiceRestFulQuery.getReadableUrl());
		}
		URL url = this.martServiceRestFulQuery.getUrlGet();
		Set<List<String>> setData = MyUtils.copyUrlContentToHashSetStringList(url, MyUtils.TAB_SEPARATOR);
		return setData;
	}
	
	@Override
	public String toString() {
		return "restFulQuery = {" + martServiceRestFulQuery.toShortString() + "}";
	}
	
	public void process() {

		try {
			RestFulQuery restFulQuery = null;
			
			/*restFulQuery = new RestFulQuery("hsapiens_gene_ensembl", 0, 5,
					new Attribute[] {
						new Attribute("ensembl_gene_id"), 
						new Attribute("ensembl_transcript_id")
					}, new Filter[] {
						new Filter("chromosome_name", "22")
					});*/
			@SuppressWarnings("unused")
			RestFulQuery restFulQuery1 = new RestFulQuery("0.7", false,/*0, 10,*/
					new RestFulQueryDataset("complex", 
					new ArrayList<Attribute>(Arrays.asList(new Attribute[] {
						new Attribute("complex_db_id_key"), 
					})), 
					new ArrayList<Filter>(Arrays.asList(new Filter[] {
						//new Filter("gene_id_list", "")
					}))));

			@SuppressWarnings("unused")
			RestFulQuery restFulQuery2 = new RestFulQuery("0.7", false,
					new RestFulQueryDataset("pathway", 
					new ArrayList<Attribute>(Arrays.asList(new Attribute[] {
						new Attribute("db_id"),
					})), 
					new ArrayList<Filter>(Arrays.asList(new Filter[] {
						//new Filter("ensembl_gene_id", "")
					}))));
			
			/*
			
6 @@@@@@@@@@@@@@@@@@@ 19499, 19147, 19147 tot, 26321ms, complex_for_pathway_db_id, complex, complex_db_id_key, pathway, db_id

				slow:
					RestFulQuery restFulQuery2 = new RestFulQuery("pride", "0.7", false,
					new Attribute[] {
						new Attribute("uniprot_ac"), 
					}, new Filter[] {
						//new Filter("ensembl_gene_id", "")
					});
			*/
			
			restFulQuery = restFulQuery2;
			restFulQuery.buildQuery();
			System.out.println(restFulQuery.query + MyUtils.LINE_SEPARATOR + restFulQuery.queryHtmlGet);
				
		
			System.out.println("1");
			URL url = new URL(restFulQuery.queryHtmlGet);
			System.out.println("2");
			List<List<String>> data = null;
			
			if (true) {
				File file = File.createTempFile(UniqueId.getUniqueID(), "", new File("." + MyUtils.FILE_SEPARATOR));
				System.out.println("3");
				RestFulQuery.urlContentToFile(url, file, true);
				System.out.println("4");
				data = MyUtils.getDataFromFile(file, MyUtils.TAB_SEPARATOR/*, 5, 3*/);
				file.delete();//System.out.println(file.getAbsolutePath());
			} else {
				data = MyUtils.copyUrlContentToListStringList(url, MyUtils.TAB_SEPARATOR);
			}
			
			System.out.println("5");

			System.out.println();
			System.out.println(data.size());
			for (List<String> list : data) {
				System.out.println(list);
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/*

name = ensembl_gene_id,	biDirectional = true,
	leftMartName = REACTOME,	leftDatasetName = interaction	leftImportable = {name = ensembl_gene_id, list = [gene_id_list]}, rightImportable = {name = ensembl_gene_id, list = [gene_id_list]}
	rightMartName = ensembl,	rightDatasetName = mmusculus_gene_ensembl, leftExportable = {name = ensembl_gene_id, list = [ensembl_gene_id]}	rightExportable = {name = ensembl_gene_id, list = [ensembl_gene_id]}


name = ensembl_gene_id,	biDirectional = true,
	leftMartName = REACTOME,	leftDatasetName = interaction	leftImportable = {name = ensembl_gene_id, list = [gene_id_list]}, 
	rightMartName = ensembl,	rightDatasetName = mmusculus_gene_ensembl	rightExportable = {name = ensembl_gene_id, list = [ensembl_gene_id]}

wget -O /home/anthony/Desktop/zyzy 'http://www.biomart.org/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query  virtualSchemaName = "default" formatter = "TSV" header = "0" uniqueRows = "0" count = "" datasetConfigVersion = "0.6" >				<Dataset name = "hsapiens_gene_ensembl" interface = "default" >		<Attribute name = "ensembl_gene_id" />		<Attribute name = "ensembl_transcript_id" />	</Dataset></Query>'
tring urlStringPart2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE Query><Query  virtualSchemaName = \"default\" formatter = \"TSV\" header = \"0\" uniqueRows = \"0\" count = \"\" limitStart = \"5\" limitSize = \"6\" datasetConfigVersion = \"0.6\" >				<Dataset name = \"hsapiens_gene_ensembl\" interface = \"default\" >		<Attribute name = \"ensembl_gene_id\" />		<Attribute name = \"ensembl_transcript_id\" />	</Dataset></Query>";


<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE Query>
<Query
       virtualSchemaName     = "default"
       uniqueRows            = ""
       count                 = ""
       datasetConfigVersion = "0.7" >
           <Dataset name = "mytest" interface = "default" >
                 <Attribute name = "ensembl_transcript_id" />
                 <Attribute name = "chromosome_name" />
                 <Filter name = "chromosome_name" value= "22"/>
           </Dataset>
</Query>

//<?xml version="1.0" encoding="UTF-8"?>	<!DOCTYPE Query>	<Query	       virtualSchemaName     = "default"	       uniqueRows            = ""	       count                 = ""	       datasetConfigVersion = "0.7" >	           <Dataset name = "mytest" interface = "default" >	                 <Attribute name = "ensembl_transcript_id" />	                 <Attribute name = "chromosome_name" />	                 <Filter name = "chromosome_name" value= "22"/>	           </Dataset>	</Query>

0 @@@@@@@@@@@@@@@@@@@ 744, 19499, 744 tot, 5176ms, complex_db_id, interaction, id_complex_db_id__dm_value, complex, complex_db_id_key
1 @@@@@@@@@@@@@@@@@@@ 1744, 24449, 1707 tot, 4140ms, reaction_db_id, interaction, id_reaction_db_id__dm_value, reaction, reaction_db_id
2 @@@@@@@@@@@@@@@@@@@ 451, 11284, 451 tot, 2932ms, pathway_db_id, interaction, id_pathway_db_id__dm_value, pathway, pathway_db_id
4 @@@@@@@@@@@@@@@@@@@ 19499, 744, 744 tot, 4961ms, complex_db_id, complex, complex_db_id_key, interaction, id_complex_db_id__dm_value
5 @@@@@@@@@@@@@@@@@@@ 19499, 19202, 19202 tot, 13860ms, complex_db_id, complex, complex_db_id_key, reaction, db_id
6 @@@@@@@@@@@@@@@@@@@ 19499, 19147, 19147 tot, 26321ms, complex_for_pathway_db_id, complex, complex_db_id_key, pathway, db_id
128 @@@@@@@@@@@@@@@@@@@ 2591, 37435, 2561 tot, 117432ms, ensembl_gene_id, complex, referencedatabase_ensembl_homo_sapiens_gene, hsapiens_gene_ensembl, ensembl_gene_id
170 @@@@@@@@@@@@@@@@@@@ 2591, 37435, 2561 tot, 117295ms, ensembl_gene_id, complex, referencedatabase_ensembl_homo_sapiens_gene, hsapiens_gene_ensembl, ensembl_gene_id
214 @@@@@@@@@@@@@@@@@@@ 24449, 1744, 1707 tot, 4569ms, reaction_db_id, reaction, reaction_db_id, interaction, id_reaction_db_id__dm_value
215 @@@@@@@@@@@@@@@@@@@ 19202, 19499, 19202 tot, 13569ms, complex_db_id, reaction, db_id, complex, complex_db_id_key
216 @@@@@@@@@@@@@@@@@@@ 24449, 23553, 23553 tot, 17684ms, reaction_db_id, reaction, reaction_db_id, pathway, reaction__dm_db_id
338 @@@@@@@@@@@@@@@@@@@ 3039, 37435, 3009 tot, 136725ms, ensembl_gene_id, reaction, referencedatabase_ensembl_homo_sapiens_gene, hsapiens_gene_ensembl, ensembl_gene_id
380 @@@@@@@@@@@@@@@@@@@ 3039, 37435, 3009 tot, 135280ms, ensembl_gene_id, reaction, referencedatabase_ensembl_homo_sapiens_gene, hsapiens_gene_ensembl, ensembl_gene_id

*/