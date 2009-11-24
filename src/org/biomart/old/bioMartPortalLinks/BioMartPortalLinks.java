package org.biomart.old.bioMartPortalLinks;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.Timer;
import org.biomart.common.general.utils.Trilean;
import org.biomart.old.martService.Configuration;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.objects.DatasetInMart;
import org.biomart.old.martService.objects.Exportable;
import org.biomart.old.martService.objects.Importable;
import org.biomart.old.martService.objects.MartInVirtualSchema;
import org.biomart.old.martService.objects.Portable;
import org.biomart.old.martService.restFulQueries.RestFulQuery;
import org.jdom.JDOMException;

//anthony@anthony-desktop:~/biomart_releases/martj-0.7_new_release/bin$ ./martregistrydbtool.sh -f ~/Desktop/new_cp.xml -H martdb.ebi.ac.uk -P 3306 -I central_registry -S central_registry -U anonymous



//            http://www.biomart.org/biomart/martservice?type=configuration&dataset=pathway&virtualSchema=default
//http://bm-test.res.oicr.on.ca:9061/biomart/martservice?type=configuration&dataset=pathway&virtualSchema=default

//null	default	REACTOME	wormbase_gene	interaction	wormbase_cds_id
	//            http://www.biomart.org/biomart/martservice/type=configuration&dataset=wormbase_gene&virtualSchema=default
	//http://bm-test.res.oicr.on.ca:9061/biomart/martservice/type=configuration&dataset=wormbase_gene&virtualSchema=default

//http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_Insertion" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
//http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="pancreas_expression_db" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.5" ><Dataset name="cfamiliaris_gene_ensembl" interface="default" ><Attribute name="uniprot_swissprot_accession" /></Dataset></Query>	
//http://www.biomart.org/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="pancreas_expression_db" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.5" ><Dataset name="cfamiliaris_gene_ensembl" interface="default" ><Attribute name="uniprot_swissprot_accession" /></Dataset></Query>	
//http://www.biomart.org/biomart/martservice/type=configuration&dataset=cfamiliaris_gene_ensembl&virtualSchema=pancreas_expression_db
//http://www.biomart.org/biomart/martservice?type=configuration&dataset=cfamiliaris_gene_ensembl&virtualSchema=pancreas_expression_db
//<AttributeDescription displayName="UniProt/Swiss-Prot Accession" field="dbprimary_id" tableConstraint="cfamiliaris_gene_ensembl__xref_uniprot_swissprot__dm" maxLength="6" internalName="uniprot_swissprot_accession" key="transcript_id_key" linkoutURL="exturl|http://www.ebi.uniprot.org/entry/%s"/>

//default_#_complex_#_referencedatabase_ensembl_homo_sapiens_gene_#_

//251	default	REACTOME	reaction	pathway	uniprot_id

/*	
Bug hidden:
http://www.biomart.org/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_Insertion" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
http://www.biomart.org/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query  virtualSchemaName = "default" formatter = "TSV" header = "0" uniqueRows = "0" count = "" datasetConfigVersion = "0.6" ><Dataset name = "marker_RAPD" interface = "default" ><Attribute name = "species" /><Attribute name = "marker_name" /></Dataset><Dataset name = "marker_Insertion" interface = "default" ><Attribute name = "species" /><Attribute name = "marker_name" /></Dataset></Query>
http://www.biomart.org/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query  virtualSchemaName = "default" formatter = "TSV" header = "0" uniqueRows = "0" count = "" datasetConfigVersion = "0.6" ><Dataset name = "marker_RAPD" interface = "default" ><Attribute name = "species" /><Attribute name = "marker_name" /></Dataset><Dataset name = "marker_Insertion" interface = "default" ><Attribute name = "species" /><Attribute name = "marker_name" /><Attribute name="acorr_marker_id" /></Dataset></Query>
http://www.biomart.org/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query  virtualSchemaName = "default" formatter = "TSV" header = "0" uniqueRows = "0" count = "" datasetConfigVersion = "0.6" ><Dataset name = "marker_RAPD" interface = "default" ><Attribute name = "species" /><Attribute name = "marker_name" /></Dataset><Dataset name = "marker_Insertion" interface = "default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
*/	

/*
<Importable filters="link_ensembl_gene_id" internalName="gene_stable_id" linkName="hsapiens_gene_stable_id" name="hsapiens_gene_stable_id" type="link"/>
<Exportable attributes="mouse_ensembl_gene" default="1" internalName="mmusculus_gene_stable_id" linkName="mmusculus_gene_stable_id" name="mmusculus_gene_stable_id" type="link"/>
<FilterDescription description="Filter to include genes with supplied list of Ensembl Gene IDs" displayName="Ensembl Gene ID(s)" displayType="text" field="stable_id_1023" hideDisplay="true" internalName="link_ensembl_gene_id" key="gene_id_1020_key" legal_qualifiers="=,in" qualifier="=" tableConstraint="main" type="text"/>
<AttributeDescription displayName="Mouse Ensembl Gene ID" field="stable_id_4016_r2" hidden="false" internalName="mouse_ensembl_gene" key="gene_id_1020_key" linkoutURL="exturl|http://www.ensembl.org/Mus_musculus/geneview?gene=%s" maxLength="20" tableConstraint="hsapiens_gene_ensembl__homolog_Mmus__dm"/>
*/
/*
dataset name: complex_db_id	=>	exportable name: interaction
dataset name: ensembl_gene_id	=>	exportable name: complex
dataset name: ensembl_gene_id	=>	exportable name: interaction
dataset name: ensembl_gene_id	=>	exportable name: htgt_trap
dataset name: ensembl_gene_id	=>	exportable name: htgt_targ
dataset name: ensembl_gene_id	=>	exportable name: pathway
dataset name: ensembl_gene_id	=>	exportable name: mmusculus_gene_ensembl
dataset name: ensembl_gene_id	=>	exportable name: reaction
dataset name: reaction_db_id	=>	exportable name: pathway
dataset name: uniprot_id	=>	exportable name: etelfairi_gene_ensembl
dataset name: uniprot_id	=>	exportable name: interaction
dataset name: uniprot_id	=>	exportable name: lafricana_gene_ensembl
dataset name: uniprot_id	=>	exportable name: olatipes_gene_ensembl
dataset name: uniprot_id	=>	exportable name: pathway
dataset name: wormbase_cds_id	=>	exportable name: interaction

case where 2 same exportable are defined (see config): http://www.biomart.org/biomart/martservice?type=configuration&dataset=htgt_trap
name = marker_symbol,	virtualSchema = default,	bioMartVersion = 0.6,	biDirectional = false,	otherDirection = null,
left = 
	bioMartVersion = 0.6,	virtualSchemaName = default,	martName = htgt,	datasetName = htgt_targ,	dataset.datasetType = TableSet,	super.toString() = bioMartPortalLinks.LinkSide@8841670e,
		importable = {linkName = marker_symbol, linkType = link, linkVersion = , defaultValue = false, list = [marker_symbol]}	portable = null
		exportableData = null
right = 
	bioMartVersion = 0.6,	virtualSchemaName = default,	martName = htgt,	datasetName = htgt_trap,	dataset.datasetType = TableSet,	super.toString() = bioMartPortalLinks.LinkSide@8841a4d9,
		importable = null	portable = {linkName = marker_symbol, linkType = link, linkVersion = , defaultValue = false, list = [marker_symbol]}
		exportableData = null
totalRows = null, fileSize = null, fileName = null, timer = null

ERROR something wrong with your registry: Can't use string ("30719") as a SCALAR ref while "strict refs" in use at /home/acros/biomart-perl4/bin/../lib/BioMart/Registry.pm line 1367.


-------------------------------------------------------------------------
default_#_wormbase_gene_#_expr_pattern_#_	null	499432	2696977	20090629-171156055	20090629-175653032	Read timed out	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="wormbase_gene" interface="default" ><Attribute name="expr_pattern" /></Dataset></Query>
default_#_marker_GenomicDNA_#_acorr_marker_id_#_	null	24613	300184	20090629-153151709	20090629-153651893	Read timed out	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_GenomicDNA" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_htgt_trap_#_escell_clone_name_#_	null	1260766	426163	20090629-164124433	20090629-164830596	Read timed out	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="htgt_trap" interface="default" ><Attribute name="escell_clone_name" /></Dataset></Query>
default_#_wormbase_gene_#_go_term_#_	null	2872627	294839	20090629-175654033	20090629-180148872	Read timed out	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="wormbase_gene" interface="default" ><Attribute name="go_term" /></Dataset></Query>
default_#_wormbase_go_term_#_gene_#_	null	3202333	263367	20090629-170514281	20090629-170937648	Read timed out	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="wormbase_go_term" interface="default" ><Attribute name="gene" /></Dataset></Query>
default_#_pathway_#_referencedatabase_uniprot_#_	null	1097503	1557392	20090629-160101192	20090629-162658585	Read timed out	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="pathway" interface="default" ><Attribute name="referencedatabase_uniprot" /></Dataset></Query>
default_#_wormbase_gene_#_rnai_#_	null	2285053	893766	20090629-164937335	20090629-170431101	Read timed out	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="wormbase_gene" interface="default" ><Attribute name="rnai" /></Dataset></Query>
	--> just very long
	
default_#_marker_Centromere_#_acorr_marker_id_#_	null	194	6157	20090629-154032854	20090629-154039011	Query ERROR: caught BioMart::Exception::Database: Error during query execution: Table 'marker_mart_29.marker_Centromere__analytical_correspondence__dm' doesn't exist	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_Centromere" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_Insertion_#_acorr_marker_id_#_	null	193	2912	20090629-153858683	20090629-153901595	Query ERROR: caught BioMart::Exception::Database: Error during query execution: Table 'marker_mart_29.marker_Insertion__analytical_correspondence__dm' doesn't exist	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_Insertion" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
	--> hide display but part of link + linking bug hides the fact that it fails:
		http://www.biomart.org/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query  virtualSchemaName = "default" formatter = "TSV" header = "0" uniqueRows = "0" count = "" datasetConfigVersion = "0.6" ><Dataset name = "marker_RAPD" interface = "default" ><Attribute name = "species" /><Attribute name = "marker_name" /></Dataset><Dataset name = "marker_Insertion" interface = "default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
			works

default_#_complex_#_referencedatabase_ensembl_homo_sapiens_gene_#_	null	139	3845	20090629-153107727	20090629-153111572	Query ERROR: caught BioMart::Exception::Usage: Attribute referencedatabase_ensembl_homo_sapiens_gene NOT FOUND	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="complex" interface="default" ><Attribute name="referencedatabase_ensembl_homo_sapiens_gene" /></Dataset></Query>
default_#_reaction_#_referencedatabase_ensembl_homo_sapiens_gene_#_	null	139	3878	20090629-153112573	20090629-153116451	Query ERROR: caught BioMart::Exception::Usage: Attribute referencedatabase_ensembl_homo_sapiens_gene NOT FOUND	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="reaction" interface="default" ><Attribute name="referencedatabase_ensembl_homo_sapiens_gene" /></Dataset></Query>
default_#_pathway_#_referencedatabase_ensembl_homo_sapiens_gene_#_	null	139	3779	20090629-153117452	20090629-153121231	Query ERROR: caught BioMart::Exception::Usage: Attribute referencedatabase_ensembl_homo_sapiens_gene NOT FOUND	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="pathway" interface="default" ><Attribute name="referencedatabase_ensembl_homo_sapiens_gene" /></Dataset></Query>
	--> broken in MartView too, if you REACTOME, complex, and choose only attribute "Gene ENSEMBL ID", you get the same error message

default_#_marker_GenePrimer_#_acorr_marker_id_#_	null	334	1890	20090629-154024172	20090629-154026062	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_GenePrimer" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_RFLP_#_acorr_marker_id_#_	null	334	1827	20090629-154007005	20090629-154008832	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_RFLP" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_EST_#_acorr_marker_id_#_	null	334	1876	20090629-154029976	20090629-154031852	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_EST" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_STS_#_acorr_marker_id_#_	null	334	1847	20090629-154009833	20090629-154011680	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_STS" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_Undefined_#_acorr_marker_id_#_	null	334	1802	20090629-154015567	20090629-154017369	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_Undefined" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_MicroarrayProbe_#_acorr_marker_id_#_	null	334	1855	20090629-153958303	20090629-154000159	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_MicroarrayProbe" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_SSR_#_acorr_marker_id_#_	null	334	1830	20090629-154001159	20090629-154002989	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_SSR" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_Deletion_#_acorr_marker_id_#_	null	334	1912	20090629-154027063	20090629-154028975	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_Deletion" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_AFLP_#_acorr_marker_id_#_	null	334	1886	20090629-154012680	20090629-154014567	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_AFLP" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_QTL_#_acorr_marker_id_#_	null	334	1591	20090629-153955711	20090629-153957303	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_QTL" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_MaizeBin_#_acorr_marker_id_#_	null	334	1905	20090629-154021266	20090629-154023171	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_MaizeBin" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_Clone_#_acorr_marker_id_#_	null	3275755	52114	20090629-153902596	20090629-153954710	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_Clone" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_GenePrediction_#_acorr_marker_id_#_	null	334	1895	20090629-154018370	20090629-154020265	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_GenePrediction" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
default_#_marker_Oligo_#_acorr_marker_id_#_	null	334	2014	20090629-154003990	20090629-154006004	Query ERROR: caught BioMart::Exception::Database: Could not connect to mysql database marker_mart_29: DBI connect('database=marker_mart_29;host=cabot.cshl.edu;port=3306','gramene_web',...) failed: Too many connections at /usr/local/ensembl-live/biomart-perl/lib/BioMart/Configuration/DBLocation.pm line 98	http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE Query><Query virtualSchemaName="default" formatter="TSV" header="0" uniqueRows="0" count="0" datasetConfigVersion="0.6" ><Dataset name="marker_Oligo" interface="default" ><Attribute name="acorr_marker_id" /></Dataset></Query>
	--> hide display but part of link + linking bug hides the fact that it fails:
-------------------------------------------------------------------------


get error in the middle of the process: default_#_marker_Clone_#_acorr_marker_id_#_

ds with pointer: str_93_gene
	contains pointerDataset="93_karyotype_start" but 93_karyotype_start doesn't exist...
	<FilterDescription internalName="biol_process" pointerDataset="go_biological_process" pointerFilter="biological_process_term_go" pointerInterface="default" />
		go_biological_process	2009-04-08 15:46:04	TableSet	false (ontology mart)
	<FilterDescription internalName="cell_component" pointerDataset="go_cellular_component" pointerFilter="cellular_component_term_go" pointerInterface="default" />
	<AttributeDescription internalName="structure_gene_stable_id" pointerAttribute="ensembl_gene_id" pointerDataset="str_93_gene" pointerInterface="default" />
*/

/**
 * 
 * anthony@anthony-desktop:~/workspace/00LinkIndex/bin$ java6 -cp /home/anthony/workspace/librairies/jdom-1.1/build/jdom.jar:. bioMartPortalLinks.BioMartPortalLinks
 * 
 * cd ~/workspace/00LinkIndex/bin/
 * java6 -cp "~/workspace/librairies/jdom-1.1/build/jdom.jar" bioMartPortalLinks.BioMartPortalLinks
 * grep -i -n ERROR *
 * grep -i -n 125457 *
 * anthony@anthony-desktop:~/anthony1/ShellScript$ sh ./find_errors.sh ExportableData | grep ERROR
 * 
 * 
 * TODO if DBLocation, create link index using direct DB connection
 * 
 * 
 */
public class BioMartPortalLinks implements Serializable {

	private static final long serialVersionUID = 4430582944906920702L;
	
	// -------------------------------------------------------------------------------------------------
	// Constants
	/*private static final Boolean CONSIDER_ONLY_1_ELEMENT_FOR_LINK = Boolean.FALSE;*/	
	private static final Boolean FETCH_ALL_COMBINATORY = Boolean.TRUE;
	private static final Boolean CHEAT_FOR_EXPORTABLE_NOT_PICKED_UP = Boolean.FALSE;

	public static final Boolean DEBUG = Boolean.TRUE;
	private static final Boolean RE_USE_FILES = Boolean.TRUE;
	private static final Boolean USE_SERIALIZE = Boolean.TRUE;
	private static final Boolean SERIALIZE = 
		!USE_SERIALIZE;
		//Boolean.TRUE;
		//Boolean.FALSE;
	public static final String STATISTICS_FOLDER_NAME = "Statistics";
	public static final String FINE = "fine";
	public static final String BROKEN = "broken";
	
	public static final String EXPORTABLE_DATA_STATISTICS_FILE_NAME = "ExportableDataStatistics";
	public static final String LINK_INDEXES_STATISTICS_FILE_NAME = "LinkIndexesStatistics";
	public static final String LINKS_FILE_NAME = "Links";
	public static final String BROKEN_EXPORTABLE_DATA_DATA_NAME = "BrokenExportableData";
		
	public static final String LINK_INDEX_CREATION_FOLDER = MyUtils.OUTPUT_FILES_PATH + "LinkIndexCreation" + MyUtils.FILE_SEPARATOR;
	public static final String STATISTICS_FOLDER = LINK_INDEX_CREATION_FOLDER + STATISTICS_FOLDER_NAME + MyUtils.FILE_SEPARATOR;
	
	public static final String SERIAL_FOLDER_NAME = "Serial";
	public static final String SERIAL_FOLDER_PATH_AND_NAME = LINK_INDEX_CREATION_FOLDER + SERIAL_FOLDER_NAME + MyUtils.FILE_SEPARATOR;
	public static final String SERIAL1_NAME = "Serial1";
	public static final String SERIAL2_NAME = "Serial2";
	public static final String SERIAL1_FILE_PATH_AND_NAME = SERIAL_FOLDER_PATH_AND_NAME + SERIAL1_NAME;
	public static final String SERIAL2_FILE_PATH_AND_NAME = SERIAL_FOLDER_PATH_AND_NAME + SERIAL2_NAME;
	
	public static final String EXPORTABLE_DATA_DATA_FOLDER = LINK_INDEX_CREATION_FOLDER + "ExportableData" + MyUtils.FILE_SEPARATOR;
	public static final String EXPORTABLE_DATA_DATA_STATISTICS_FILE_PATH_AND_NAME = STATISTICS_FOLDER + EXPORTABLE_DATA_STATISTICS_FILE_NAME;
	
	public static final String LINK_INDEXES_FOLDER = LINK_INDEX_CREATION_FOLDER + "LinkIndexes" + MyUtils.FILE_SEPARATOR;
	public static final String LINK_INDEXES_STATISTICS_FILE_PATH_AND_NAME = STATISTICS_FOLDER + LINK_INDEXES_STATISTICS_FILE_NAME;
	
	public static final String LINKS_FILE_PATH_AND_NAME = STATISTICS_FOLDER + LINKS_FILE_NAME;
	public static final String BROKEN_EXPORTABLE_DATA_DATA_NAME_FILE_PATH_AND_NAME = STATISTICS_FOLDER + BROKEN_EXPORTABLE_DATA_DATA_NAME;
	
	public static final String MART_SERVICE_URL = 
		//MartServiceConstants.CENTRAL_PORTAL_MART_SERVICE_STRING_URL;
		MartServiceConstants.BMTEST_MART_SERVICE_STRING_URL;
	public static final int SLEEP_LENGTH_FETCHING_VALUES = 1000;	// in ms
	public static final int MART_SERVICE_TIMEOUT = 180000;	// in ms
	public static final long MAX_DISK_SPACE = Long.valueOf("5000000000");
	
	// -------------------------------------------------------------------------------------------------
	// Variables
	public Configuration configuration = null;
	public List<Link> listLinks1 = null;
	public List<Link> listLinks2 = null;
	public List<Link> listLinks3 = null;
	public List<LinkSide> listLinkSides = null;
	public Map<PortableData, PortableData> validExportableData = null;		// Map because Set doesn't have a .get() method
	public Map<PortableData, PortableData> brokenExportableData = null;		// Map because Set doesn't have a .get() method
	public Integer totalIndexableImportableLinks = null;
	public Integer totalValidLinks = null;
	public Integer totalIndexableLinks = null;
	
	public Integer martServiceTimeOut = null;
		
	public Timer linkFindingTimer = null;
	public Timer dataDownloadingTimer = null;
	public Timer linkIndexCreationTimer = null;
	
	public Boolean debug = null;
    
	public static void main(String[] args) {
		
		boolean test = false;
		if (null!=args && args.length>=1) {
			test = true;
		}
		
		MyUtils.alterConsoleOutput(LINK_INDEX_CREATION_FOLDER + "MartServiceConsole", Trilean.MINUS);
        MyUtils.println("start.");
        
		populatePerlLinkCollections();

		BioMartPortalLinks biomartPortalLinks = null;
		try {
			if (!USE_SERIALIZE && !test) {
				biomartPortalLinks = new BioMartPortalLinks();
				biomartPortalLinks.fetchConfiguration();
				biomartPortalLinks.fetchLinkList();
				biomartPortalLinks.compareWithPerl();
				biomartPortalLinks.writeStatisticFile();
				biomartPortalLinks.writeLinkIndexTestingPlanStatistics();
				biomartPortalLinks.getStatisticsOnHashProblem();
				biomartPortalLinks.linkIndexCreation(MART_SERVICE_TIMEOUT);
				serialize(biomartPortalLinks);
			} else {
				biomartPortalLinks = deserialize();
			}
			
			if (!test) {	
				biomartPortalLinks.testLinkIndexesCouple(true);
				/*biomartPortalLinks.testLinkIndexesTriple();*/
			} if (test) {	
				TestLinks testLinks = new TestLinks(biomartPortalLinks);
				testLinks.test();
			}/* else if (USE_SERIALIZE || (!USE_SERIALIZE && FETCH_ALL_COMBINATORY)) {
				
			}*/
        } catch (JDOMException e) {
        	System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (TechnicalException e) {
        	System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (MalformedURLException e) {
        	System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
        	System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
        	System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (FunctionalException e) {
        	System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
        	System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
        	System.err.println(e.getMessage());
			e.printStackTrace();
		}
System.out.println("c1 = " + Importable.c1);
System.out.println("c2 = " + Importable.c2);
System.out.println("c3 = " + Importable.c3);
System.out.println("c4 = " + Importable.c4);
System.out.println("c5 = " + Importable.c5);
System.out.println("c6 = " + Importable.c6);
System.out.println("c7 = " + Importable.c7);
System.out.println("c10 = " + c10);
System.out.println("c11 = " + c11);
        MyUtils.println("done.");
		MyUtils.closeConsoleOutput();
	}
	
	public BioMartPortalLinks() {
		this.configuration = new Configuration(MART_SERVICE_URL);
		
		this.linkFindingTimer = new Timer();
		this.dataDownloadingTimer = new Timer();
		this.linkIndexCreationTimer = new Timer();
	}
	
	public void fetchConfiguration() throws FunctionalException, TechnicalException, InterruptedException, JDOMException, IOException {
		this.configuration.fetchMartSet();
		this.configuration.fetchDatasets();
		this.configuration.fetchLinkableDatasets();
		this.configuration.displayInitialStats();
		this.configuration.displayStatistics();
	}
	
	private LinkableDataset getLinkableDatasetByNameAndVirtualSchema(String datasetName, String virtualSchema) {		
		boolean found = false;
		LinkableDataset linkableDataset = null;
		for (LinkableDataset ld : this.configuration.datasetList) {
			if (datasetName.equals(ld.datasetName) && virtualSchema.equals(ld.serverVirtualSchema)) {
				if (found) {
					MyUtils.errorProgram("found", true);
				}
				linkableDataset = ld;
				found=true;
			}
		}
		return linkableDataset;
	}
	
	@SuppressWarnings("unchecked")
	public static BioMartPortalLinks deserialize() throws TechnicalException {
		BioMartPortalLinks bioMartPortalLinks = (BioMartPortalLinks)MyUtils.readSerializedObject(SERIAL1_FILE_PATH_AND_NAME);
		//this.listLinks1 = (List<Link>)MyUtils.readSerializedObject(SERIAL1_FILE_PATH_AND_NAME);
		System.out.println("Quick check: " + bioMartPortalLinks.listLinks3.size());
		//this.configuration.deserialize(SERIAL2_FILE_PATH_AND_NAME);
		return bioMartPortalLinks;
	}
	
	public static void serialize(BioMartPortalLinks bioMartPortalLinks) {
		try {
			MyUtils.writeSerializedObject(bioMartPortalLinks, SERIAL1_FILE_PATH_AND_NAME);
			//this.configuration.serialize(SERIAL2_FILE_PATH_AND_NAME);
		} catch (TechnicalException e) {
			e.printStackTrace();
		}
		MyUtils.println("serial1 = " + SERIAL1_FILE_PATH_AND_NAME + ", serial2 = " + SERIAL2_FILE_PATH_AND_NAME);
		System.out.println("serial1 = " + SERIAL2_FILE_PATH_AND_NAME + ", serial2 = " + SERIAL2_FILE_PATH_AND_NAME);		
	}

	public void fetchLinkList() throws FunctionalException, IOException {
		
		this.linkFindingTimer.startTimer();
		
		/**
		 * Contains for each dataset the list of (other) datasets for which we've done the link searching already
		 */
		Map<LinkableDataset, List<LinkableDataset>> mapDone = new HashMap<LinkableDataset, List<LinkableDataset>>();
		this.listLinks1 = new ArrayList<Link>();
		MyUtils.showProgress(this.configuration.getTotalLinkableDatasets());
        for (LinkableDataset sourceLinkableDataset : configuration.datasetList) {

        	// Show progress
 	    	System.out.print(".");
 	    	
 	    	// Fetch
 	    	for (LinkableDataset targetLinkableDataset : configuration.datasetList) {
 	    		
 	    		// Only consider links between 2 different datasets of the same virtual schema
 	    		if (targetLinkableDataset.sameVirtualSchema(sourceLinkableDataset) && 
 	    				!sourceLinkableDataset.equals(targetLinkableDataset)) {
 	    			
	 	    		List<LinkableDataset> doneForTheSource = mapDone.get(sourceLinkableDataset);
	 	        	if (null==doneForTheSource) {
	 	        		doneForTheSource = new ArrayList<LinkableDataset>();
	 	        	}
	 	    		if (doneForTheSource.contains(targetLinkableDataset)) {		// If already done, continue (it may have from the other way around)
	 	        		continue;
	 	        	}
	 	    		
	 	    		this.listLinks1.addAll(fetchBothWayLinks(sourceLinkableDataset, targetLinkableDataset));

	 	    		List<LinkableDataset> doneForTheTarget = mapDone.get(targetLinkableDataset);
	 	    		if (null==doneForTheTarget) {
	 	        		doneForTheTarget = new ArrayList<LinkableDataset>();
	 	        	}
	 	    		doneForTheTarget.add(sourceLinkableDataset);	// Because we do both ways
	 	    		mapDone.put(targetLinkableDataset, doneForTheTarget);	// optional
	 	   		}
 	    	}
        }
        System.out.println();

        // Write file
        writeLinks(listLinks1, 1);
        
        this.linkFindingTimer.stopTimer();
        System.out.println("this.linkFindingTimer = " + this.linkFindingTimer);		
		
        this.totalIndexableImportableLinks = 0;
		for (Link link : this.listLinks1) {
			MyUtils.println(link.toNiceStringPreCreation() + MyUtils.LINE_SEPARATOR);
			if (link.getIndexableImportable()) {
				totalIndexableImportableLinks++;
			}
		}
		System.out.println("totalIndexableImportableLinks = " + totalIndexableImportableLinks);
		
		diagnoseConfiguration();
		exportableProblemProcess();		
	}

	private void diagnoseConfiguration() {
		int importableOk = 0;
		int importableIncomplete = 0;
		int exportableOk = 0;
		int exportableIncomplete = 0;
		int importableOk2 = 0;
		int importableIncomplete2 = 0;
		for (Link link : this.listLinks1) {
			if (link.left.importable.getCompleteElementList()) {
				importableOk++;
			} else {
				importableIncomplete++;
			}
			if (link.right.exportable.getCompleteElementList()) {
				exportableOk++;
			} else {
				exportableIncomplete++;
			}
			if (link.left.importable.getCompleteAttributesList()) {
				importableOk2++;
			} else {
				importableIncomplete2++;
			}
		}
		System.out.println("importableOk = " + importableOk);
		System.out.println("importableIncomplete = " + importableIncomplete);
		System.out.println("exportableOk = " + exportableOk);
		System.out.println("exportableIncomplete = " + exportableIncomplete);
		System.out.println("importableOk2 = " + importableOk2);
		System.out.println("importableIncomplete2 = " + importableIncomplete2);
	}

	private void exportableProblemProcess() {
		System.out.println("exportableProblemLinkList.size() = " + exportableProblemLinkList.size());
		//System.out.println("exportableProblemLinkSideCoupleList.size() = " + exportableProblemLinkSideCoupleList.size());
		System.out.println("exportableProblemSet.size() = " + exportableProblemSet.size());
		for (List<String> list : exportableProblemSet) {
			System.out.println(MyUtils.TAB_SEPARATOR + list);
		}
		
		if (!CHEAT_FOR_EXPORTABLE_NOT_PICKED_UP) {
			int exportableProblemAffectedLinks = 0;
			for (Link link : this.listLinks1) {
				for (List<String> list : exportableProblemSet) {
					if (link.right.datasetName.equals(list.get(1)) && link.name.equals(list.get(2))) {
						exportableProblemAffectedLinks++;
					}
				}
			}
			System.out.println("exportableProblemAffectedLinks = " + exportableProblemAffectedLinks);
		}
	}

	private List<Link> fetchBothWayLinks(LinkableDataset linkableDataset1, LinkableDataset linkableDataset2) {	
		List<Link> linkList1 = findLink(linkableDataset1, linkableDataset2);
		List<Link> linkList2 = findLink(linkableDataset2, linkableDataset1);

populateProgramLinkSetForComparisonWithPerl(linkableDataset1, linkableDataset2, linkList1, linkList2);
		
		List<Link> allLinksList = new ArrayList<Link>();
		allLinksList.addAll(linkList1);
		allLinksList.addAll(linkList2);
		boolean hasBothWays = false;
		/*for (Link link1 : linkList1) {
			for (Link link2 : linkList2) {
			//for (int i = linkList2.size()-1; i >= 0; i--) {
				//Link link2 = linkList2.get(i);
				hasBothWays=true;
				c10++;
				if (link1.isOtherDirectionOf2(link2)) {
					link1.setBiDirectional(link2);
					link2.setBiDirectional(link1);
					tmp.remove(link2);
				}
			}
		}*/
		if (!hasBothWays) {
			c11++;
		}
		return allLinksList;
	}
public static int c10 = 0;
public static int c11 = 0;
	private List<Link> findLink (LinkableDataset linkableDataset1, LinkableDataset linkableDataset2) {
		
		boolean debug = false;				
		if (linkableDataset1.datasetName.equals(Configuration.DEBUG_DATASET1) && linkableDataset2.datasetName.equals(Configuration.DEBUG_DATASET2) ) {
			debug = true;
		}
boolean perlDebug = false
	|| debug;
		
		List<Link> validLinkList = new ArrayList<Link>();
		for (Importable importable1 : linkableDataset1.importableList) {
			for (Exportable exportable2 : linkableDataset2.exportableList) {
				/* Perl's code:
					if ($importable->linkName eq $exportable->linkName) {
		            # do not link on DAS or GFF type exp/imp pairs
						next if ($importable->type() ne 'link' || $exportable->type ne 'link');
						# versions must be compatible as well if exists for both
						next if (($importable->linkVersion && $exportable->linkVersion)
		                    && ($importable->linkVersion ne $exportable->linkVersion))
		        */
				if (importable1.linkName.equals(exportable2.linkName)) {		// should check length as well!!
					
					if (!importable1.linkType.equals(MartServiceConstants.XML_ATTRIBUTE_VALUE_LINK) || 
							!exportable2.linkType.equals(MartServiceConstants.XML_ATTRIBUTE_VALUE_LINK)) {
						continue;
					}
					if (!MyUtils.isEmpty(importable1.linkVersion) && !MyUtils.isEmpty(exportable2.linkVersion) &&		// they're never null (garanteed by constructor)
							!importable1.linkVersion.equals(exportable2.linkVersion)) {
						continue;
					}
					
					/*LinkSide newLinkSide = new LinkSide(linkableDataset1, importable1, TEMPORARY_FILE_FOLDER);
					if (!listLinkSide.contains(newLinkSide)) {
						listLinkSide.add(newLinkSide);
					}
					LinkSide left = listLinkSide.get(listLinkSide.indexOf(newLinkSide));
					
					LinkSide newLinkSide2 = new LinkSide(linkableDataset2, exportable2, TEMPORARY_FILE_FOLDER);
					if (!listLinkSide.contains(newLinkSide2)) {
						listLinkSide.add(newLinkSide2);
					}
					LinkSide right = listLinkSide.get(listLinkSide.indexOf(newLinkSide2));*/
					
					LinkSide left = new LinkSide(linkableDataset1, importable1);
					LinkSide right = new LinkSide(linkableDataset2, exportable2);
					Link link = new Link(importable1.linkName, linkableDataset1.serverVirtualSchema, linkableDataset1.bioMartVersion, left, right);
					if (linkableDataset1.datasetName.equals(Configuration.DEBUG_DATASET1) && linkableDataset2.datasetName.equals(Configuration.DEBUG_DATASET2)) {
						System.out.println("link = " + link);
					}
					validLinkList.add(link);
				}
			}
		}
		
		Link defaultLink = null;
		if (!validLinkList.isEmpty()) {
			
			/* Perl's code:
			    # make the first link the default or if AttList set as default
			    # make this link the default
			    if ((!defined $self->defaultLink()) || $attList->defaultList){
			       $self->defaultLink($linkName);
			    }
			*/
			
			boolean onlyOneAndNoDefault = validLinkList.size()==1 && countDefault(validLinkList)==0;
			boolean ManyButonlyOneDefault = countDefault(validLinkList)==1;
			
			if (debug) {
				System.out.println("countDefault(validLinkList) = " + countDefault(validLinkList) + ", validLinkList = " + validLinkList);
				System.out.println(onlyOneAndNoDefault);
				System.out.println(ManyButonlyOneDefault);
			}
			
			if (onlyOneAndNoDefault) {
				defaultLink = validLinkList.get(0);	
				
				if (debug) {
					System.out.println("onlyOneAndNoDefault");
				}				
			} else if (ManyButonlyOneDefault) {
				defaultLink = firstDefault(validLinkList);	// Only one anyway
				
				if (debug) {
					System.out.println("ManyButonlyOneDefault");
				}
				
			} else {	// need perl
				
				needPerl++;
				
				List<Link> potentialCandidatePerlHashProblem = null;
				List<String> perlHashLinkNames = null;
				
				boolean noDefault = countDefault(validLinkList)==0;
				boolean manyDefault = countDefault(validLinkList)>1;
				
				if (debug) {
					System.out.println(noDefault);
					System.out.println(manyDefault);
				}
				
				potentialCandidatePerlHashProblem = new ArrayList<Link>();
				if (noDefault) {
					for (int i = 0; i < validLinkList.size(); i++) {
						Link link = validLinkList.get(i);
						link.setHashProblem(1);
						potentialCandidatePerlHashProblem.add(link);
					}
				} else if (manyDefault) {
					for (int i = 0; i < validLinkList.size(); i++) {
						Link link = validLinkList.get(i);
						if (link.right.exportable.defaultValue) {
							link.setHashProblem(2);
							potentialCandidatePerlHashProblem.add(link);
						}
					}
				} else {
					MyUtils.errorProgram("ERROR", true);
				}
				
				perlHashLinkNames = new ArrayList<String>();
				for (Link l : potentialCandidatePerlHashProblem) {
					perlHashLinkNames.add(l.name);
				}
				
				MyUtils.checkStatusProgram(potentialCandidatePerlHashProblem.size()>1, "perlHash.size()>1, perlHash.size() = " + potentialCandidatePerlHashProblem.size(), true);

if (perlDebug) {				
	System.out.println(potentialCandidatePerlHashProblem);
	System.out.println(perlHashLinkNames);
	System.out.println(linkableDataset1.toImpExpOrientedString());
	System.out.println(linkableDataset2.toImpExpOrientedString());		
}
				
				// Need perl to tell us which link to choose
				PerlLink linkData = null;
				int occurences = 0;
				String[] nameOccurences = new String[6];

if (perlDebug) {				
	System.out.println(new PerlLink("999", linkableDataset1.serverVirtualSchema, linkableDataset1.mart, linkableDataset1.datasetName, linkableDataset2.datasetName, "?").toPerlOutputString());
	System.out.println(new PerlLink("999", linkableDataset1.serverVirtualSchema, linkableDataset2.mart, linkableDataset1.datasetName, linkableDataset2.datasetName, "?").toPerlOutputString());
}

				for (PerlLink ld : perlLinkList/*perlLinkSet*/) {
					if (ld.virtualSchema.equals(linkableDataset1.serverVirtualSchema) && ld.virtualSchema.equals(linkableDataset2.serverVirtualSchema) &&
							ld.sourceDatasetName.equals(linkableDataset1.datasetName) && ld.targetDatasetName.equals(linkableDataset2.datasetName)) {
						nameOccurences[occurences] = ld.linkName;
						occurences++;
						if (occurences>2) {
							MyUtils.errorProgram("occurences>2", true);
						}
						linkData = ld;
if (perlDebug) {													
	System.out.println(occurences + " - \t" + ld.toPerlOutputString());
}
					}
				}
				
				MyUtils.checkStatusProgram(occurences==0 || occurences==2, "occurences = " + occurences, true);
				
				if (occurences==0) {
if (perlDebug) {
	System.out.println("occurence=0");
}

					ArrayList<String> potentialExportableNames = new ArrayList<String>(Arrays.asList(new String[] {
							linkableDataset1.serverVirtualSchema, /*defaultLink.left.datasetName, */linkableDataset2.datasetName, "1"}));
					potentialExportableNames.addAll(perlHashLinkNames);
					exportableProblemSet.add(potentialExportableNames);
				
	
					needPerlNotInPerl++;
				} else {
					MyUtils.checkStatusProgram(linkData!=null, "linkData!=null", true);									
					MyUtils.checkStatusProgram(perlHashLinkNames.contains(linkData.linkName), "perlHashLinkNames.contains(linkData.linkName), " +
							"linkData.linkName = " + linkData.linkName + ", perlHashLinkNames = " + perlHashLinkNames, true);
					
					int occ = 0;
					for (Link l : potentialCandidatePerlHashProblem) {
						if (l.name.equals(linkData.linkName)) {	// if more than 1 version with same name then will take first one... TODO?

							MyUtils.checkStatusProgram(occ==0 || 
									(l.right.datasetName.equals("htgt_trap") && l.name.equals("marker_symbol")), 	// The exception (2 exportables identical)
									"occ = " + occ, true);
							
							defaultLink = l;
							occ++;
						}
					}
if (perlDebug) {							
	System.out.println(defaultLink);
	System.out.println((perlHashLinkNames.indexOf(defaultLink.name)+1) + "/" + perlHashLinkNames.size());
	//MyUtils.pressKeyToContinue();
}
					needPerlInPerl++;
				}
			}
		}
		
		if (
				//false && 
				debug
				){
			System.out.println("-----------------------------------------------------------");
			System.out.println(linkableDataset1.toImpExpOrientedString());
			System.out.println(linkableDataset2.toImpExpOrientedString());
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println(validLinkList.size());
			for (Link l : validLinkList) {
				System.out.println(l.toNiceStringPreCreation());
				System.out.println();
				System.out.println();
			}
			System.out.println();
			System.out.println();
			System.out.println();
			if (defaultLink!=null) {
				System.out.println("defaultLink = " + defaultLink.name);
			} else {
				System.out.println("defaultLink = " + null);
			}
			System.out.println();
		}
		
		List<Link> linkList = new ArrayList<Link>();	// The first one is the default one (only for now anyway)
		if (null!=defaultLink) {
			linkList.add(defaultLink);
		}
		
		// Make sure Perl finds it too
		boolean different = false;
		boolean exportableProblem = false;
		String mine = null;
		String perls = null;
		if (null!=defaultLink) {
			
			
			PerlLink perlLink = new PerlLink(null, defaultLink.left.virtualSchemaName, defaultLink.left.martName,
					defaultLink.left.datasetName, defaultLink.right.datasetName, defaultLink.name);
			
			mine = defaultLink.name;
			
			for (PerlLink pl : perlLinkSet) {
				if (pl.similar(perlLink) && !pl.equals(perlLink)) {
					exportableProblem = true;
					different = true;
					System.out.println(perlLink.toPerlOutputString() + " different in perl!!!!!!!!!!!!!!!! " + pl.toPerlOutputString());
					
					perls = pl.linkName;
					if (CHEAT_FOR_EXPORTABLE_NOT_PICKED_UP) {
						defaultLink.name = pl.linkName;
						perlLink = new PerlLink(null, defaultLink.left.virtualSchemaName, defaultLink.left.martName,
								defaultLink.left.datasetName, defaultLink.right.datasetName, defaultLink.name);
						System.out.println(defaultLink.toNiceString());
					}
					//MyUtils.pressKeyToContinue();//null	default	REACTOME	reaction	pathway	reaction_db_id different in perl!!!!!!!!!!!!!!!! 251	default	REACTOME	reaction	pathway	uniprot_id
				}
			}
			
			if (!perlLinkSet.contains(perlLink)) {
				System.out.println(perlLink.toPerlOutputString() + " not in perl!!!!!!!!!!!!!!!!");
				exportableProblem = true;
				if (CHEAT_FOR_EXPORTABLE_NOT_PICKED_UP) {
					linkList.clear();
				}
			}
		}
		
		if (different) {
			System.out.println(linkList);
		}
		if (exportableProblem) {
			exportableProblemLinkList.add(defaultLink);
			ArrayList<String> descriptiveList = new ArrayList<String>(Arrays.asList(new String[] {
								defaultLink.left.dataset.serverVirtualSchema, /*defaultLink.left.datasetName, */defaultLink.right.datasetName, "0", mine, perls}));
			if (null!=perls) {
				descriptiveList.add(perls);
			}
			exportableProblemSet.add(descriptiveList);
		}
		
		return linkList;
	}
	
	private void writeStatisticFile() {
		
		System.out.println("needPerl = " + needPerl);
		System.out.println(MyUtils.TAB_SEPARATOR + "needPerlNotInPerl = " + needPerlNotInPerl);
		System.out.println(MyUtils.TAB_SEPARATOR + "needPerlInPerl = " + needPerlInPerl);
		
		boolean includeHash = false;
		
		TreeSet<PerlLink2> myLink2TreeSet = new TreeSet<PerlLink2>(); 
		for (int i = 0; i < this.listLinks1.size(); i++) {
			Link link = this.listLinks1.get(i);
			myLink2TreeSet.add(new PerlLink2(link.virtualSchema, link.left.datasetName, link.right.datasetName, link.name, 
					(includeHash ? link.getHashProblem() : 0)));
		}
		StringBuffer stringBuffer = new StringBuffer(PerlLink2.getHeader() + MyUtils.LINE_SEPARATOR);
		for (PerlLink2 perlLink2 : myLink2TreeSet) {
			stringBuffer.append(perlLink2.toShortString2() + MyUtils.LINE_SEPARATOR);
		}
		MyUtils.writeFile(STATISTICS_FOLDER + (includeHash ? "MyLinkListWithHash" : "MyLinkListNoHash"), stringBuffer.toString());
		

		TreeSet<PerlLink2> perlLink2TreeSet = new TreeSet<PerlLink2>();
		for (PerlLink perlLink : perlLinkSet) {	
			perlLink2TreeSet.add(new PerlLink2(perlLink.virtualSchema, perlLink.sourceDatasetName, perlLink.targetDatasetName, perlLink.linkName, 0));
		}
		stringBuffer = new StringBuffer(PerlLink2.getHeader() + MyUtils.LINE_SEPARATOR);
		for (PerlLink2 perlLink2 : perlLink2TreeSet) {	
			stringBuffer.append(perlLink2.toShortString2() + MyUtils.LINE_SEPARATOR);
		}
		MyUtils.writeFile(STATISTICS_FOLDER + (includeHash ? "PerlLinkListWithHash" : "PerlLinkListNoHash"), stringBuffer.toString());
		
		TreeSet<PerlLink2> intersection = new TreeSet<PerlLink2>(myLink2TreeSet);
		intersection.retainAll(perlLink2TreeSet);
		TreeSet<PerlLink2> complement1 = new TreeSet<PerlLink2>(myLink2TreeSet);
		complement1.removeAll(perlLink2TreeSet);
		TreeSet<PerlLink2> complement2 = new TreeSet<PerlLink2>(perlLink2TreeSet);
		complement2.removeAll(myLink2TreeSet);
		System.out.println("myLink2TreeSet.size() = " + myLink2TreeSet.size());
		System.out.println("perlLink2TreeSet.size() = " + perlLink2TreeSet.size());
		System.out.println("intersection = " + intersection.size());
		System.out.println("complement1 = " + complement1.size());
		System.out.println("complement2 = " + complement2.size());
				
		stringBuffer = new StringBuffer();
		for (Link link : this.listLinks1) {
			
			if (!link.getValidVisibility()) continue;
			
			String type = null;
			Portable portable = null;
			if (link.left.importable.getMissingElement()) {
				type = "importable";
				portable = link.left.importable;
			}
			if (link.right.exportable.getMissingElement()) {
				type = "exportable";
				portable = link.right.exportable;
			}
			if (type!=null) {
				stringBuffer.append(type + MyUtils.TAB_SEPARATOR + link.toQuickDescription() + MyUtils.TAB_SEPARATOR + 
					portable.toShortString() + MyUtils.LINE_SEPARATOR);
			}
		}
		MyUtils.writeFile(STATISTICS_FOLDER + "NoMatchingElement", stringBuffer.toString());
		
		stringBuffer = new StringBuffer();
		for (Link link : this.listLinks1) {
			if (!link.getValidVisibility()) continue;
			if (link.left.importable.elementNamesList.size()!=link.right.exportable.elementNamesList.size()) {
				stringBuffer.append(link.toShortString());
			}
		}
		MyUtils.writeFile(STATISTICS_FOLDER + "ElementListSizeNotMatching", stringBuffer.toString());
	}
	
	private void linkIndexCreation(int martServiceTimeOut) throws MalformedURLException, UnsupportedEncodingException, IOException, InterruptedException, FunctionalException, TechnicalException, Exception {
		
		this.martServiceTimeOut = martServiceTimeOut;
		createListValidLinks();
		createListValidLinkSides();
		createMapTemporaryFiles();
		createLinkIndexes();
	}

	private void getStatisticsOnHashProblem() {
		int noHashProblemValidVisibilityCount =0;
		int noHashProblemInvalidVisibilityCount =0;
		int hashProblemValidVisibilityCount =0;
		int hashProblemInvalidVisibilityCount =0;
		for (Link link : this.listLinks1) {
			if (link.getHashProblem()==0) {
				if (link.getValidVisibility()) {
					noHashProblemValidVisibilityCount++;
				} else {
					noHashProblemInvalidVisibilityCount++;
				}
			} else {
				if (link.getValidVisibility()) {
					hashProblemValidVisibilityCount++;
				} else {
					hashProblemInvalidVisibilityCount++;
				}
			}
		}
		System.out.println("noHashProblemValidVisibilityCount = " + noHashProblemValidVisibilityCount);
		System.out.println("noHashProblemInvalidVisibilityCount = " + noHashProblemInvalidVisibilityCount);
		System.out.println("hashProblemValidVisibilityCount = " + hashProblemValidVisibilityCount);
		System.out.println("hashProblemInvalidVisibilityCount = " + hashProblemInvalidVisibilityCount);
	}

	private List<Link> createListValidLinks() throws IOException {			
		
		this.totalValidLinks = 0;
		this.listLinks2 = new ArrayList<Link>();
		for (Link link : this.listLinks1) {

			// Skip unnecessary ones (already done or not qualifying)
			if (link.getValidVisibility() && link.getNoGenomicSequence()) {
				totalValidLinks++;
				listLinks2.add(link);
			}
		}	
		System.out.println("totalValidLinks = " + totalValidLinks);
			
        // Write file
        writeLinks(listLinks2, 2);
        
		return listLinks2;
	}
	
	private void createMapTemporaryFiles() 
	throws MalformedURLException, UnsupportedEncodingException, IOException, TechnicalException, FunctionalException, InterruptedException {	
		
		this.dataDownloadingTimer.startTimer();
		
		System.out.println("Creating link sides files");
		long totalDiskSpace = 0;
		
		String message = null;
		FileWriter fileWriter = new FileWriter(new File(EXPORTABLE_DATA_DATA_STATISTICS_FILE_PATH_AND_NAME));
		BufferedWriter exportableDataStatisticsWriter = new BufferedWriter(fileWriter);
		
		int potentialCount = 0;
		int fineCount = 0;
		int differentCount = 0;
		int createdProperlyCount = 0;
		this.validExportableData = new HashMap<PortableData, PortableData>();
		this.brokenExportableData = new HashMap<PortableData, PortableData>();
		boolean created = true;
		for (LinkSide linkSide : this.listLinkSides) {

/*if (linkSide.getExportableData()==null || this.brokenExportableData.keySet().contains(linkSide.getExportableData())) {
	continue;
}*/
//if (exportableProblemLinkSideCoupleList.contains(linkSide)) continue;
			
			PortableData exportableDataTmp = createExportableData(linkSide);
			


/*if (!((linkSide.virtualSchemaName.equals("default") && linkSide.datasetName.equals("interaction") && linkSide.exportable.list.get(0).equals("complex_db_id_key")) ||
(linkSide.virtualSchemaName.equals("default") && linkSide.datasetName.equals("complex") && linkSide.exportable.list.get(0).equals("complex_db_id_key")))) continue;*/
//if (!(linkSide.getLinkSideFileName().equals("default_#_ojaponica_gene_ensembl_#_ensembl_gene_id_#_")/* || linkSide.getLinkSideFileName().equals("default_#_pathway_#_ensembl_gene_id_#_")*/)) continue;
//if (skipFile.contains(linkSide.getLinkSideExportableFileName())) continue;
//if (!linkSide.martName.equals("htgt") || linkSide.getLinkSideFileName().equals("default_#_htgt_trap_#_escell_clone_name_#_")) continue;			
//if (!(linkSide.getLinkSideFileName().equals("default_#_complex_#_complex_db_id_key_#_") || linkSide.getLinkSideFileName().equals("default_#_interaction_#_id_complex_db_id__dm_value_#_"))) continue;
//if (!(linkSide.martName.equals("ENSEMBL_MART_ENSEMBL") || linkSide.martName.equals("REACTOME") && (linkSide.datasetName.equals("ojaponica_gene_ensembl") || (linkSide.datasetName.equals("interaction") || linkSide.datasetName.equals("pathway"))))) continue;			
/*if (linkSide.virtualSchemaName.equals("pancreas_expression_db")) continue;	//20 of them
if (linkSide.martName.equals("GRAMENE_MARKER_29")) continue;
if (linkSide.datasetName.equals("wormbase_gene")) continue;	// takes too long
if (exportableDataTmp.getFileName().equals("default_#_complex_#_referencedatabase_ensembl_homo_sapiens_gene_#_")) continue;	// doesn't work
if (exportableDataTmp.getFileName().equals("default_#_reaction_#_referencedatabase_ensembl_homo_sapiens_gene_#_")) continue;	// doesn't work
if (exportableDataTmp.getFileName().equals("default_#_pathway_#_referencedatabase_ensembl_homo_sapiens_gene_#_")) continue;	// doesn't work*/
//if (linkSide.datasetName.startsWith("wormbase_")) continue;

//if (!(linkSide.martName.equals("ensembl") && linkSide.datasetName.compareTo("b")<0)) continue;
//if (!(linkSide.martName.equals("htgt"))) continue;

/*if ((linkSide.martName.equals("GRAMENE_MARKER_29") && 
		(linkSide.datasetName.equals("marker_Deletion")) ||
		(linkSide.datasetName.equals("marker_Centromere")) ||
		(linkSide.datasetName.equals("marker_Insertion")))) continue;*/
			
			System.out.print("# " + exportableDataTmp.getFileName() + MyUtils.TAB_SEPARATOR);
			
			boolean newOne = false;
			PortableData exportableData = null;
			String status = BROKEN;
			created = false;
			if (!validExportableData.keySet().contains(exportableDataTmp) && !this.brokenExportableData.keySet().contains(exportableDataTmp)) {
				newOne = true;
				System.out.println("new one" + MyUtils.TAB_SEPARATOR + "===========================");
				exportableData = exportableDataTmp;
				RestFulQuery query = linkSide.createMartServiceRestFulQuery(MART_SERVICE_URL);
				LinkIndexCreation linkIndexCreation = new LinkIndexCreation(query);
				try {
					created = linkIndexCreation.createDataFile(potentialCount, exportableData, 
							EXPORTABLE_DATA_DATA_FOLDER, RE_USE_FILES, SLEEP_LENGTH_FETCHING_VALUES, this.martServiceTimeOut);
					totalDiskSpace+=exportableData.getFileSize();
					this.validExportableData.put(exportableData, exportableData);
					status = FINE;
					fineCount++;
					differentCount++;
				} catch (TechnicalException e) {
					System.out.println("TechnicalException caught: " + e.getMessage());
					this.brokenExportableData.put(exportableData, exportableData);
				}
			} else if (validExportableData.keySet().contains(exportableDataTmp)) {
				System.out.println("already done");
				exportableData = validExportableData.get(exportableDataTmp);
				status = FINE;
				fineCount++;
			} else if (this.brokenExportableData.keySet().contains(exportableDataTmp)) {
				System.out.println("broken one");
				exportableData = brokenExportableData.get(exportableDataTmp);
			} else {
				MyUtils.errorProgram("impossible", true);
			}
			potentialCount++;
			if (created) {
				createdProperlyCount++;
				
				// Sleep so we don't overload servers
				Thread.sleep(1000);
			}
			linkSide.setPortableData(exportableData);
			
			message = status + MyUtils.TAB_SEPARATOR + linkSide.toStatisticsString() + MyUtils.LINE_SEPARATOR;
			System.out.print(message);
			if (newOne) {
				exportableDataStatisticsWriter.write(message);				
			}
			
			// To make sure we don't explode memory
			if (totalDiskSpace>=MAX_DISK_SPACE) {
				MyUtils.errorProgram("MAX_DISK_SPACE threshold crossed, totalSize = " + totalDiskSpace, true);
			}
			
			/*if (linkSide.getPortableData().buildFileName().contains("default_#_marker_Clone_#_acorr_marker_id_#_")) {
				System.out.println("ok");
			}*/
		}
		
		exportableDataStatisticsWriter.close();
		fileWriter.close();
		
		writeBrokenExportableDataFile();
		
		this.dataDownloadingTimer.stopTimer();
		System.out.println("this.dataDownloadingTimer = " + this.dataDownloadingTimer);
		
		System.out.println("validExportableData.size = " + validExportableData.size());
		System.out.println("brokenExportableData.size = " + brokenExportableData.size());
		System.out.println("potentialCount = " + potentialCount);
		System.out.println("createdCount = " + differentCount);
		System.out.println("fineCount = " + fineCount);
		System.out.println("differentCount = " + differentCount);
		System.out.println("totalDiskSpace = " + totalDiskSpace);

		displayBrokenExportableData();
	}

	private void createLinkIndexes() 
	throws IOException, FunctionalException, MalformedURLException, UnsupportedEncodingException, InterruptedException {
		
		FileWriter fileWriter = new FileWriter(new File(LINK_INDEXES_STATISTICS_FILE_PATH_AND_NAME));
		BufferedWriter linkIndexesStatisticsWriter = new BufferedWriter(fileWriter);
		
		this.linkIndexCreationTimer.startTimer();
		
		int linkIndexesCount = 0;
		Boolean created = null;
		LinkSide previousLeft = null;
		LinkIndexCreation linkIndexCreation = new LinkIndexCreation();
		//Set<List<PortableData>> setDone = new HashSet<List<PortableData>>();
		for (Link link : this.listLinks3) {


			
			
			/*if (!(link.left.martName.equals("htgt") && link.right.martName.equals("htgt")) || 
					link.left.getLinkSideFileName().equals("default_#_htgt_trap_#_escell_clone_name_#_") ||
					link.right.getLinkSideFileName().equals("default_#_htgt_trap_#_escell_clone_name_#_")) continue;*/
//				if (!(linkSideCouple.contains("default_#_ojaponica_gene_ensembl_#_ensembl_gene_id_#_")/* && linkSideCouple.contains("default_#_pathway_#_ensembl_gene_id_#_")*/)) continue;			
//				if (skipFile.contains(link.left.getLinkSideExportableFileName()) || skipFile.contains(link.right.getLinkSideExportableFileName())) continue;			
//				if (!(linkSideCouple.contains("default_#_complex_#_complex_db_id_key_#_") && linkSideCouple.contains("default_#_interaction_#_id_complex_db_id__dm_value_#_"))) continue;

			/*if (link.left.virtualSchemaName.equals("pancreas_expression_db") || link.right.virtualSchemaName.equals("pancreas_expression_db")) continue;
			if (link.left.martName.equals("GRAMENE_MARKER_29") || link.right.martName.equals("GRAMENE_MARKER_29")) continue;
			if (link.left.datasetName.equals("wormbase_gene") || link.right.datasetName.equals("wormbase_gene")) continue;	// takes too long
			List<String> linkSideCouple2 = getLinkIdentifierTmp(link);if (linkSideCouple2.contains("default_#_complex_#_referencedatabase_ensembl_homo_sapiens_gene_#_")) continue;	// doesn't work
			if (linkSideCouple2.contains("default_#_reaction_#_referencedatabase_ensembl_homo_sapiens_gene_#_")) continue;	// doesn't work
			if (linkSideCouple2.contains("default_#_pathway_#_referencedatabase_ensembl_homo_sapiens_gene_#_")) continue;	// doesn't work*/
//				if (link.left.datasetName.startsWith("wormbase_") || link.right.datasetName.startsWith("wormbase_")) continue;

//				if (!(link.left.martName.equals("ensembl") && link.right.martName.equals("ensembl") && link.left.datasetName.compareTo("b")<0 && link.right.datasetName.compareTo("b")<0)) continue;
//				if (!(link.left.martName.equals("htgt") && link.right.martName.equals("htgt"))) continue;

//				System.out.println(link.toShortString());

			/*if ((link.left.martName.equals("GRAMENE_MARKER_29") && link.right.martName.equals("GRAMENE_MARKER_29") && 
					(link.right.datasetName.equals("marker_Deletion") ||  link.right.datasetName.equals("marker_Centromere") ||  link.right.datasetName.equals("marker_Insertion") ||
					link.left.datasetName.equals("marker_Deletion") ||  link.left.datasetName.equals("marker_Centromere") ||  link.left.datasetName.equals("marker_Insertion")))) continue;*/

			/*(link.left.datasetName.equals("marker_Deletion") &&  link.right.datasetName.equals("marker_Centromere")) ||  
			(link.left.datasetName.equals("marker_Deletion") &&  link.right.datasetName.equals("marker_Insertion")) ||
			(link.left.datasetName.equals("marker_Centromere") &&  link.right.datasetName.equals("marker_Deletion")) ||
			(link.left.datasetName.equals("marker_Centromere") &&  link.right.datasetName.equals("marker_Insertion")) ||
			(link.left.datasetName.equals("marker_Insertion") &&  link.right.datasetName.equals("marker_Deletion")) ||
			(link.left.datasetName.equals("marker_Insertion") &&  link.right.datasetName.equals("marker_Centromere")))) continue;*/
//				if (!(link.left.martName.equals("ENSEMBL_MART_ENSEMBL") && link.right.martName.equals("REACTOME") && link.left.datasetName.equals("ojaponica_gene_ensembl") && (link.right.datasetName.equals("interaction") || link.right.datasetName.equals("pathway")))) continue;
//				if (!(link.left.martName.equals("GRAMENE_MARKER_29") && link.right.martName.equals("GRAMENE_MARKER_29") && link.left.datasetName.equals("marker_Oligo") && (link.right.datasetName.equals("marker_RFLP") || link.right.datasetName.equals("marker_STS") || link.right.datasetName.equals("marker_AFLP")))) continue;
//				if (link.left.martName.compareTo("GRAMENE_MARKER_29")<0) continue;
//				if (!(link.left.martName.equals("GRAMENE_MARKER_29") && link.right.martName.equals("GRAMENE_MARKER_29") && link.left.datasetName.equals("marker_EST") && link.right.datasetName.equals("marker_Deletion"))) continue;
						
			
			MyUtils.checkStatusProgram(link.isIndexableLink(), "link.isIndexableLink() = " + link.isIndexableLink(), true);
						
			/*List<PortableData> linkSideCouple = getLinkIdentifier(link);
			if (setDone.contains(linkSideCouple)) {	// Not redo it (bidirectional links use the same link index)
				continue;
			}*/
			
			System.out.println(MyUtils.LINE_SEPARATOR + MyUtils.DASH_LINE + MyUtils.LINE_SEPARATOR);
			System.out.println("link #" + linkIndexesCount); 
			System.out.println("pre " + link.toNiceStringPreCreation());
			
			boolean reUseLeft = previousLeft!=null && previousLeft.equals(link.left) && created!=null && created==true;
			
			String status = null;
			/*if (exportableProblemLinkList.contains(link)) continue;*/
			if (link.left.getPortableData()==null || link.right.getPortableData()==null || 
					this.brokenExportableData.keySet().contains(link.left.getPortableData()) || 
					this.brokenExportableData.keySet().contains(link.right.getPortableData())) {
				created = false;
				status = BROKEN;
			} else {
				created = linkIndexCreation.createLinkIndexUsingFileSystemAndJavaMemory(
						link, EXPORTABLE_DATA_DATA_FOLDER, LINK_INDEXES_FOLDER, RE_USE_FILES, !reUseLeft, null);
				status = FINE;
			}
			System.out.print(status + MyUtils.TAB_SEPARATOR + "post " + link.toNiceStringPostCreation());
			linkIndexesStatisticsWriter.write(status + MyUtils.TAB_SEPARATOR + link.toStatisticString() + MyUtils.LINE_SEPARATOR);

			previousLeft = link.left;
			linkIndexesCount++;
			
			
			/*setDone.add(linkSideCouple);*/
		}
/*MyUtils.checkStatusProgram(doneCount==this.totalIndexableLinks/2, "doneCount = " + doneCount + 
		", this.totalFullyValidLinks = " + this.totalIndexableLinks/2, true);*/
		
		this.linkIndexCreationTimer.stopTimer();
		
		System.out.println("this.linkIndexCreationTimer = " + this.linkIndexCreationTimer);
		
		System.out.println("linkIndexesCount = " + linkIndexesCount);

		linkIndexesStatisticsWriter.close();
		fileWriter.close();
	}
	
	@Deprecated
	public void createFinalValidListLink() {
		this.listLinks3 = new ArrayList<Link>();
		for (Link link : this.listLinks1) {
			if (link.isIndexableLink()) {
				this.listLinks3.add(link);
			}
		}
	}
	
	public void testLinkIndexesCouple(boolean unique) throws Exception {
		boolean countOnly = false;
		int count = 0;
		for (Link link : this.listLinks3) {
			MyUtils.checkStatusProgram(link.isIndexableLink(), "", true);
			
			System.out.println(link.left.datasetName + ", " + link.right.datasetName + ", " + link.right.exportable.getCompleteFiltersList());
			
			if (!link.left.datasetName.equals("wormbase_rnai") || !link.right.datasetName.equals("wormbase_variation")) continue;
			
			/*if (!(link.left.martName.equals("REACTOME") || link.right.martName.equals("REACTOME"))) continue;
			if (link.left.datasetName.equals("template") || link.right.datasetName.equals("template")) continue;
			if (link.left.datasetName.startsWith("marker") || link.right.datasetName.startsWith("marker")) continue;
			if (link.left.datasetName.startsWith("rnorvegicus") || link.right.datasetName.startsWith("rnorvegicus")) continue;*/
			
			//if (!link.left.martName.equals("ensembl") || !link.right.martName.equals("ensembl")) continue;
			/*MartInVirtualSchema mart1 = this.configuration.getMartByName(link.left.martName);
			MartInVirtualSchema mart2 = this.configuration.getMartByName(link.right.martName);
			if (!mart1.type.equals(MartServiceConstants.MART_TYPE_DB_LOCATION) || 
					!mart2.type.equals(MartServiceConstants.MART_TYPE_DB_LOCATION)) {
				continue;
			}*/
			
			// Need filter on exportable side too
			if (!link.right.exportable.getCompleteFiltersList()) continue;
			
			/*if (link.left.getPortableData()==null || link.left.getPortableData().getTotalRows()==null ||
					link.right.getPortableData()==null || link.right.getPortableData().getTotalRows()==null) continue;*/
			
			if (link.getTotalRows()!=null && link.getTotalRows()>0/* && 
					link.getTotalRows()<10 &&
					link.left.getPortableData().getTotalRows()<100 &&
					link.right.getPortableData().getTotalRows()<100*/
			) {
				if (!countOnly) {
					System.out.println(MyUtils.EQUAL_LINE);
					LinkIndexTestCouple linkIndexTestCouple = new LinkIndexTestCouple(link, MART_SERVICE_URL, unique);
					try {
						linkIndexTestCouple.process();
					} catch (TechnicalException e) {
						e.printStackTrace();
						System.out.println("ERROR with");
					} catch (FunctionalException e) {
						e.printStackTrace();
						System.out.println("ERROR with");
					}
					System.out.println();
					Thread.sleep(1000);
				}
				count++;
			}
		}
		System.out.println("count = " + count);
	}
	
	public void testLinkIndexesTriple() throws Exception {
		HashSet<List<Link>> setDone = new HashSet<List<Link>>();
		for (Link link1 : this.listLinks3) {
			MyUtils.checkStatusProgram(link1.isIndexableLink(), "", true);
			
			for (Link link2 : this.listLinks3) {
				MyUtils.checkStatusProgram(link2.isIndexableLink(), "", true);
				
				List<Link> list = new ArrayList<Link>();
				list.add(link1);
				list.add(link2);
				Collections.sort(list);
				if (setDone.contains(list)) continue;
				
				if (!link2.right.isSameSide(link1.left) && link1.right.isSameSide(link2.left)
						
						&& link1.getTotalRows()!=null && link1.getTotalRows()>0 && link2.getTotalRows()!=null && link2.getTotalRows()>0  
						
						) {
					
					System.out.println(MyUtils.EQUAL_LINE);
					LinkIndexTestTriple linkIndexTest = new LinkIndexTestTriple(link1, link2, MART_SERVICE_URL);
					try {
						linkIndexTest.initialize();
						linkIndexTest.process();
					} catch (TechnicalException e) {
						e.printStackTrace();
						System.out.println("ERROR with");
					} catch (FunctionalException e) {
						e.printStackTrace();
						System.out.println("ERROR with");
					}
					System.out.println();
					Thread.sleep(1000);
					
					setDone.add(list);
				}
			}
		}
	}

	private void writeLinkIndexTestingPlanStatistics() {
		
		StringBuffer stringBuffer;
		stringBuffer = new StringBuffer();
		HashSet<List<Link>> setDone = new HashSet<List<Link>>();
		for (Link link1 : this.listLinks1) {
			if (!link1.isIndexableLink()) continue;
			
			for (Link link2 : this.listLinks1) {
				if (!link2.isIndexableLink()) continue;
				
				List<Link> list = new ArrayList<Link>();
				list.add(link1);
				list.add(link2);
				// order matters now
				//Collections.sort(list);
				if (setDone.contains(list)) continue;
				
				if (!link2.right.isSameSide(link1.left) && link1.right.isSameSide(link2.left)) {
					
					// Keep track of list
					stringBuffer.append(link1.toShortString2() + "\t/\t" + link2.toShortString2() + MyUtils.LINE_SEPARATOR);
					
					setDone.add(list);
				}
			}
		}
		MyUtils.writeFile(STATISTICS_FOLDER + "Tmp", stringBuffer.toString());
	}

	private void writeLinks(List<Link> listLinks, int n) throws IOException {
		System.out.println("listLinks" + n + ".size() = " + listLinks.size());
		FileWriter fileWriter = new FileWriter(new File(LINKS_FILE_PATH_AND_NAME + n));
		BufferedWriter bw = new BufferedWriter(fileWriter);
		int count = 0;
		for (Link link : listLinks) {
			bw.write(count + MyUtils.TAB_SEPARATOR + link.virtualSchema + MyUtils.TAB_SEPARATOR + link.name + MyUtils.TAB_SEPARATOR + 
					link.left.datasetName  + MyUtils.TAB_SEPARATOR + link.right.datasetName + MyUtils.TAB_SEPARATOR + link.isIndexableLink() + MyUtils.TAB_SEPARATOR + link.getIndexableImportable() + MyUtils.TAB_SEPARATOR + link.getValidVisibility() + MyUtils.LINE_SEPARATOR);
			count++;
		}
		bw.close();
		fileWriter.close();
	}
	
	private PortableData createExportableData(LinkSide linkSide) {
		PortableData exportableDataTmp = new PortableData(linkSide.virtualSchemaName, linkSide.datasetName, 
				linkSide.isLeft() ? linkSide.importable.getAttributeNamesList() : linkSide.exportable.elementNamesList, 
						EXPORTABLE_DATA_DATA_FOLDER);
		return exportableDataTmp;
	}

	private void writeBrokenExportableDataFile() {
		StringBuffer brokenSb = new StringBuffer();
		brokenSb.append("this.brokenExportableData.size() = " + this.brokenExportableData.size() + MyUtils.LINE_SEPARATOR);
		for (Iterator<PortableData> it = brokenExportableData.keySet().iterator(); it.hasNext();) {
			PortableData exportableData = it.next();
			brokenSb.append(MyUtils.TAB_SEPARATOR + exportableData.toStatisticString() + MyUtils.LINE_SEPARATOR);
		}
		MyUtils.writeFile(BROKEN_EXPORTABLE_DATA_DATA_NAME_FILE_PATH_AND_NAME, brokenSb.toString());
	}
	
	@SuppressWarnings("unused")
	private void displayValidLinkList(List<Link> validLinkList) {
		System.out.println();
		System.out.println(validLinkList.size());
		for (Link l : validLinkList) {
			System.out.println("\t" + l.toNiceStringPreCreation());
		}
		System.out.println();
	}

	private int countDefault(List<Link> validLinkList) {
		int count = 0;
		for (int i = 0; i < validLinkList.size(); i++) {
			Link link = validLinkList.get(i);
			if (link.right.exportable.defaultValue) {
				count++;
			}
		}
		return count;
	}
	private Link firstDefault(List<Link> validLinkList) {
		Link firstDefault = null;
		for (int i = 0; i < validLinkList.size(); i++) {
			Link link = validLinkList.get(i);
			if (link.right.exportable.defaultValue) {
				firstDefault = link;
				break;
			}
		}
		return firstDefault;
	}

	private void createListValidLinkSides() throws IOException{	// Not a Set, we need to associate ExportableData to each LinkSide
		this.listLinkSides = new ArrayList<LinkSide>();
		this.listLinks3 = new ArrayList<Link>();
		for (Link link : this.listLinks1) {
			if (link.isIndexableLink()) {
				listLinkSides.add(link.left);
				listLinkSides.add(link.right);
				listLinks3.add(link);
			}
		}
		System.out.println("listLinkSides.size() = " + listLinkSides.size());
		this.totalIndexableLinks = listLinkSides.size();
		
        // Write file
        writeLinks(listLinks3, 3);
	}

	private List<PortableData> getLinkIdentifier(Link link) {
		MyUtils.checkStatusProgram(link.left.getPortableData()!=null && link.right.getPortableData()!=null, 
				"link.left.getExportableData()!=null && link.right.getExportableData()!=null, " +
				"link.left.getExportableData() = " + link.left.getPortableData() + ", link.right.getExportableData() = " + link.right.getPortableData(), true);
		List<PortableData> linkSideCouple = new ArrayList<PortableData>();
		linkSideCouple.add(link.left.getPortableData());
		linkSideCouple.add(link.right.getPortableData());
		Collections.sort(linkSideCouple);	// Sort by alphabetical order so we can recognize the other way too
		return linkSideCouple;
	}
	
	public void displayBrokenExportableData () {
		System.out.println("this.brokenExportableData.size() = " + this.brokenExportableData.size());
		for (Iterator<PortableData> it = brokenExportableData.keySet().iterator(); it.hasNext();) {
			PortableData exportableData = it.next();
			System.out.println(MyUtils.TAB_SEPARATOR + exportableData.toStatisticString());
		}
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//=================================================================================

public static int needPerl = 0;
public static int needPerlNotInPerl = 0;
public static int needPerlInPerl = 0;

List<String> skipVS = new ArrayList<String>(Arrays.asList(new String[] {"pancreas_expression_db"}));
List<String> skipFile = new ArrayList<String>(Arrays.asList(new String[] {
"default_#_marker_EST_#_acorr_marker_id_#_",
"default_#_marker_GenomicDNA_#_acorr_marker_id_#_",
"default_#_marker_MicroarrayProbe_#_acorr_marker_id_#_",
"default_#_marker_MaizeBin_#_acorr_marker_id_#_",	//2
"default_#_marker_Deletion_#_acorr_marker_id_#_",
"default_#_htgt_trap_#_escell_clone_name_#_",	//7
"default_#_complex_#_referencedatabase_ensembl_homo_sapiens_gene_#_",
"default_#_marker_Insertion_#_acorr_marker_id_#_",
"default_#_rgd_genes_#_uniprot_acc_attr_#_",
"default_#_ipi_rat_#_uniprot_acc_#_",
"default_#_pride_#_uniprot_ac_#_",
"default_#_pathway_#_referencedatabase_uniprot_#_",
"default_#_marker_Centromere_#_acorr_marker_id_#_",
"default_#_marker_GenePrediction_#_acorr_marker_id_#_",
"default_#_marker_Clone_#_acorr_marker_id_#_",
"default_#_wormbase_gene_#_rnai_#_",	//581
"default_#_wormbase_gene_#_expr_pattern_#_",
"default_#_wormbase_rnai_#_phenotype_#_",
"default_#_reaction_#_referencedatabase_ensembl_homo_sapiens_gene_#_"
}));

private static void populatePerlLinkCollections() {
	String content = MyUtils.readFile("/home/anthony/Desktop/perlLinks");
	String[] lines = content.split("\n");
	for (int i = 0; i < lines.length; i++) {
		String[] s = lines[i].split("\t");
		PerlLink linkData = new PerlLink(s[0].trim(), s[1].trim(), s[2].trim(), s[3].trim(), s[4].trim(), s[5].trim());
		
		
		
		perlLinkSet.add(linkData);
		perlLinkList.add(linkData);
	}
	
/*	for (LinkData linkData : perlLinkSet) {
		if (linkData.toPerlOutputString().endsWith("default	REACTOME	interaction	reaction	reaction_db_id")) {
			System.out.println("@@@" + linkData.toPerlOutputString());
		}
	}
	
System.exit(0);	*/
	
	// Check that only 1 link between 2 datasets
	if (false) {
		for (PerlLink ld : perlLinkSet) {
			String vs = ld.virtualSchema;
			String src = ld.sourceDatasetName;
			String trg = ld.targetDatasetName;
			
			int count = 0;
			for (PerlLink ld2 : perlLinkSet) {
				String vs2 = ld2.virtualSchema;
				String src2 = ld2.sourceDatasetName;
				String trg2 = ld2.targetDatasetName;
				if (vs.equals(vs2) && src.equals(src2) && trg.equals(trg2)) {
					count++;
					if (count>2) {
						
						System.out.println(vs);
						System.out.println(vs2);
						System.out.println(src);
						System.out.println(src2);
						System.out.println(trg);
						System.out.println(trg2);
						
						System.out.println("PBLM!!!!!!!!!!!!!!!!");
						System.exit(-1);
					}
				}
			}
		}
	}
}


private void populateProgramLinkSetForComparisonWithPerl(LinkableDataset linkableDataset1, LinkableDataset linkableDataset2, List<Link> linkList1, List<Link> linkList2) {
	if (!linkList1.isEmpty() && linkableDataset1.datasetName.equals(Configuration.DEBUG_DATASET1) && linkableDataset2.datasetName.equals(Configuration.DEBUG_DATASET2)) {
		System.out.println("&&&&&&&&&&&&&&&&&&&&");
		System.out.println("link1 = " + linkList1);
	}
	if (!linkList2.isEmpty() && linkableDataset1.datasetName.equals(Configuration.DEBUG_DATASET1) && linkableDataset2.datasetName.equals(Configuration.DEBUG_DATASET2)) {
		System.out.println("@@@@@@@@@@@@@@@@@@@@");
		System.out.println("link2 = " + linkList2);
	}
	for (Link link1 : linkList1) {
		PerlLink linkData = new PerlLink(null, link1.left.virtualSchemaName, link1.left.martName,
				link1.left.datasetName, link1.right.datasetName, link1.name);
		
		if (linkableDataset1.datasetName.equals(Configuration.DEBUG_DATASET1) && linkableDataset2.datasetName.equals(Configuration.DEBUG_DATASET2)) {
			System.out.println(linkData.toString());
		}
		
		tonyLinkSet.add(linkData);
		tonyLinkList.add(linkData);
		
		PerlLink linkData2 = new PerlLink(null, link1.left.virtualSchemaName, link1.right.martName,
				link1.left.datasetName, link1.right.datasetName, link1.name);
		if (linkableDataset1.datasetName.equals(Configuration.DEBUG_DATASET1) && linkableDataset2.datasetName.equals(Configuration.DEBUG_DATASET2)) {
			System.out.println(linkData2.toString());
		}
		tonyLinkSet.add(linkData2);
		tonyLinkList.add(linkData2);
	}
	
	for (Link link2 : linkList2) {
		PerlLink linkData = new PerlLink(null, link2.left.virtualSchemaName, link2.left.martName,
				link2.left.datasetName, link2.right.datasetName, link2.name);
		
		if (linkableDataset1.datasetName.equals(Configuration.DEBUG_DATASET1) && linkableDataset2.datasetName.equals(Configuration.DEBUG_DATASET2)) {
			System.out.println(linkData.toString());
		}
		
		tonyLinkSet.add(linkData);
		tonyLinkList.add(linkData);
		
		PerlLink linkData2 = new PerlLink(null, link2.left.virtualSchemaName, link2.right.martName,
				link2.left.datasetName, link2.right.datasetName, link2.name);
		if (linkableDataset1.datasetName.equals(Configuration.DEBUG_DATASET1) && linkableDataset2.datasetName.equals(Configuration.DEBUG_DATASET2)) {
			System.out.println(linkData2.toString());
		}
		tonyLinkSet.add(linkData2);
		tonyLinkList.add(linkData2);
	}
}

@SuppressWarnings("unused")
private void foo() {
		
	Map<PerlLink, Integer> m = new TreeMap<PerlLink, Integer>();
	
	for (Iterator<String> it = configuration.virtualSchemaMartSetMap.keySet().iterator(); it.hasNext();) {
		String serverVirtualSchema = it.next();
		Set<MartInVirtualSchema> martSet = configuration.virtualSchemaMartSetMap.get(serverVirtualSchema);
		
		for (MartInVirtualSchema mart : martSet) {
			String martName = mart.martName;
        	List<DatasetInMart> datasetInMartList = configuration.martDatasetListMap.get(martName);
			for (DatasetInMart datasetInMart : datasetInMartList) {
//String datasetName = datasetInMart.datasetName;
for (PerlLink l : perlLinkSet) {								// seems useless!!! doesn't use datasetName
	Integer c = m.get(l);
	if (c==null) {
		c=0;
	}
	c++;
	m.put(l, c);
}
        	}
 		}
		}
	
	Set<PerlLink> set = new TreeSet<PerlLink>(m.keySet());
	MyUtils.checkStatusProgram(set.equals(perlLinkSet), set.size() + ", " + perlLinkSet.size());
	
}

static Set<List<String>> exportableProblemSet = new HashSet<List<String>>();
static Set<Link> exportableProblemLinkList = new HashSet<Link>();
//static Set<LinkSide> exportableProblemLinkSideCoupleList = new HashSet<LinkSide>();


static Set<PerlLink> perlLinkSet = new HashSet<PerlLink>();
static Set<PerlLink> tonyLinkSet = new HashSet<PerlLink>();
static List<PerlLink> perlLinkList = new ArrayList<PerlLink>();
static List<PerlLink> tonyLinkList = new ArrayList<PerlLink>();
private void compareWithPerl() {
	
	StringBuffer stringBuffer = new StringBuffer();
	StringBuffer stringBuffer2 = new StringBuffer();
	for (PerlLink linkData : perlLinkSet) {
		//if (!set1.contains(linkData)) {
			//System.out.println(linkData);
			//if (c>1000) break;
			//c++;
		//}
		stringBuffer.append(linkData + "\n");
	}
	for (PerlLink linkData : perlLinkList) {
		stringBuffer2.append(linkData + "\n");
	}
	MyUtils.writeFile("/home/anthony/Desktop/set0", stringBuffer.toString());
	MyUtils.writeFile("/home/anthony/Desktop/list0", stringBuffer2.toString());
	stringBuffer = new StringBuffer();
	stringBuffer2 = new StringBuffer();
	for (PerlLink linkData : tonyLinkSet) {
		//if (!set1.contains(linkData)) {
			//System.out.println(linkData);
			//if (c>1000) break;
			//c++;
			stringBuffer.append(linkData.toPerlOutputString() + "\n");
		//}
	}
	for (PerlLink linkData : tonyLinkList) {
		stringBuffer2.append(linkData.toPerlOutputString() + "\n");
	}
	MyUtils.writeFile("/home/anthony/Desktop/set1", stringBuffer.toString());
	MyUtils.writeFile("/home/anthony/Desktop/list1", stringBuffer2.toString());
	

	
	int perl_size = perlLinkSet.size();
	int tony_size = tonyLinkSet.size();
	Set<PerlLink> perlLinkSetCopy1 = new HashSet<PerlLink>(perlLinkSet);
	Set<PerlLink> tonyLinkSetCopy1 = new HashSet<PerlLink>(tonyLinkSet);
	Set<PerlLink> perlLinkSetCopy2 = new HashSet<PerlLink>(perlLinkSet);
	Set<PerlLink> tonyLinkSetCopy2 = new HashSet<PerlLink>(tonyLinkSet);
	perlLinkSetCopy1.removeAll(tonyLinkSetCopy2);
	tonyLinkSetCopy1.removeAll(perlLinkSetCopy2);
	int new_perl_size = perlLinkSetCopy1.size();
	int new_tony_size = tonyLinkSetCopy1.size();
	
	System.out.println("perl has " + perl_size + " with\t" + new_perl_size + " that are not in tony.");
	System.out.println("tony has " + tony_size + " with\t" + new_tony_size + " that are not in perl.");
	
	System.out.println((new_perl_size/(double)perl_size)*100 + " %");
	System.out.println((new_tony_size/(double)tony_size)*100 + " %");
	
	
	
	
	
	System.out.println("============================================");
	List<DatasetToLink> listInPerlNotTony = new ArrayList<DatasetToLink>();
	List<DatasetToLink2> listInPerlNotTony2 = new ArrayList<DatasetToLink2>();
	StringBuffer sbInPerlNotTony1 = new StringBuffer();
	createLists2(perlLinkSetCopy1, listInPerlNotTony, listInPerlNotTony2, sbInPerlNotTony1);
	MyUtils.writeFile(LINK_INDEX_CREATION_FOLDER + "inPerlNotTony", sbInPerlNotTony1.toString());

	System.out.println("============================================");
	List<DatasetToLink> listInTonyNotPerl = new ArrayList<DatasetToLink>();
	List<DatasetToLink2> listInTonyNotPerl2 = new ArrayList<DatasetToLink2>();
	StringBuffer sbInTonyNotPerl1 = new StringBuffer();
	createLists2(tonyLinkSetCopy1, listInTonyNotPerl, listInTonyNotPerl2, sbInTonyNotPerl1);
	MyUtils.writeFile(LINK_INDEX_CREATION_FOLDER + "inTonyNotPerl", sbInTonyNotPerl1.toString());
}

private void createLists2(Set<PerlLink> linkSet, List<DatasetToLink> listInPerlNotTony, List<DatasetToLink2> listInPerlNotTony2, StringBuffer sbInPerlNotTony1) {
	int count =0;
	for (PerlLink l : linkSet) {
//if (l.sourceDatasetName.equals("template") || l.targetDatasetName.equals("template")) continue;		
		sbInPerlNotTony1.append(l.toPerlOutputString() + MyUtils.LINE_SEPARATOR);
//		System.out.println(l.toPerlOutputString());
		
		DatasetToLink dataset1 = new DatasetToLink(l.virtualSchema, l.location, l.sourceDatasetName);
		DatasetToLink dataset2 = new DatasetToLink(l.virtualSchema, l.location, l.targetDatasetName);
		
		String sourceMart = getLinkableDatasetByNameAndVirtualSchema(l.sourceDatasetName, l.virtualSchema).mart;
		String targetMart = getLinkableDatasetByNameAndVirtualSchema(l.targetDatasetName, l.virtualSchema).mart;
		if (!l.location.equalsIgnoreCase(sourceMart) && !l.location.equalsIgnoreCase(targetMart)) {
			System.out.println("error: |" + l.location + "|, |" + sourceMart + "|" + "|, |" + targetMart + "|");
			System.exit(0);
		}
		if (!listInPerlNotTony.contains(dataset1)) {
			listInPerlNotTony.add(dataset1);					
		}
		if (!listInPerlNotTony.contains(dataset2)) {
			listInPerlNotTony.add(dataset2);					
		}
		
		
		
		DatasetToLink2 dataset2_2 = new DatasetToLink2(l.linkName, l.sourceDatasetName, l.targetDatasetName, sourceMart, targetMart);
		if (!listInPerlNotTony2.contains(dataset2_2)) {
			listInPerlNotTony2.add(dataset2_2);					
		}
		
		count++;
		if (count>10) {
			//break;
		}
		//continue;
	}
}
@SuppressWarnings("unused")
private void createLists(Set<PerlLink> linkSet1, Set<PerlLink> linkSet2, List<DatasetToLink> listInPerlNotTony, List<DatasetToLink2> listInPerlNotTony2, StringBuffer sbInPerlNotTony1) {
	int count =0;
	for (PerlLink l : linkSet1) {
		
		boolean contain=false;
		for (PerlLink l2 : linkSet2) {
			if (l.equals(l2)) {
				contain=true;
				break;
			}
		}
		if (!contain) {
			sbInPerlNotTony1.append(l.toPerlOutputString() + MyUtils.LINE_SEPARATOR);
			System.out.println(l.toPerlOutputString());
			
			DatasetToLink dataset1 = new DatasetToLink(l.virtualSchema, l.location, l.sourceDatasetName);
			DatasetToLink dataset2 = new DatasetToLink(l.virtualSchema, l.location, l.targetDatasetName);
			
			String sourceMart = getLinkableDatasetByNameAndVirtualSchema(l.sourceDatasetName, l.virtualSchema).mart;
			String targetMart = getLinkableDatasetByNameAndVirtualSchema(l.targetDatasetName, l.virtualSchema).mart;
			if (!l.location.equalsIgnoreCase(sourceMart) && !l.location.equalsIgnoreCase(targetMart)) {
				System.out.println("error: |" + l.location + "|, |" + sourceMart + "|" + "|, |" + targetMart + "|");
				System.exit(0);
			}
			if (!listInPerlNotTony.contains(dataset1)) {
				listInPerlNotTony.add(dataset1);					
			}
			if (!listInPerlNotTony.contains(dataset2)) {
				listInPerlNotTony.add(dataset2);					
			}
			
			
			
			DatasetToLink2 dataset2_2 = new DatasetToLink2(l.linkName, l.sourceDatasetName, l.targetDatasetName, sourceMart, targetMart);
			if (!listInPerlNotTony2.contains(dataset2_2)) {
				listInPerlNotTony2.add(dataset2_2);					
			}
			
			count++;
			if (count>10) {
				//break;
			}
			//continue;
		}
	}
}
}



/*int linkIndexesCount = 0;
LinkIndexCreation linkIndexCreation = null;
Link previousLink = null;
for (Link link : this.listLink) {
	if (previousLink!=null && previousLink.isOtherDirectionOf(link)) {
		continue;
	}

//if (!(link.left.virtualSchemaName.equals("default") && link.left.datasetName.equals("interaction") && link.right.virtualSchemaName.equals("default") && link.right.datasetName.equals("complex"))) continue;
	
	
if (link.left.virtualSchemaName.equals("pancreas_expression_db")) continue;		
	if (link.biDirectional) {//if (link.left.datasetName.equals("interaction") && link.left.exportable.list.get(0).equals("gene__dm_value")) {System.out.println("@@ skip");continue;}
		
		Timer timer = new utils.Timer();
		timer.startTimer();		
System.out.println(MyUtils.LINE_SEPARATOR + MyUtils.DASH_LINE + MyUtils.LINE_SEPARATOR + linkIndexesCount + "\n" + link.toNiceString() + MyUtils.LINE_SEPARATOR);

		boolean sameLeft = previousLink!=null && previousLink.left.equals(link.left);
		System.out.println("sameLeft = " + sameLeft);
		
		Integer reuse = 0;
		if (previousLink!=null) {
			boolean leftIsPreviousLeft = link.left.equals(previousLink.left);
			boolean leftIsPreviousRight = link.left.equals(previousLink.right);
			boolean rightIsPreviousRight = link.right.equals(previousLink.right);
			boolean rightIsPreviousLeft = link.right.equals(previousLink.left);
			reuse = leftIsPreviousLeft ? 1 : (leftIsPreviousRight ? 2 : (rightIsPreviousRight ? 3 : (rightIsPreviousLeft ? 4 : 0)));
			
			System.out.println("leftIsPreviousLeft = " + leftIsPreviousLeft + ", leftIsPreviousRight = " + leftIsPreviousRight +
					", rightIsPreviousRight = " + rightIsPreviousRight + ", rightIsPreviousLeft = " + rightIsPreviousLeft);
		}
		System.out.println("reuse = " + reuse);


		MartServiceRestFulQuery restFulQuery1 = createMartServiceRestFulQuery(link.left);
		MartServiceRestFulQuery restFulQuery2 = createMartServiceRestFulQuery(link.right);
		
		Trilean method = Trilean.ZERO;
		Integer size = null;
		MartServiceRestFul martServiceRestFul1 = new MartServiceRestFul(restFulQuery1);
		MartServiceRestFul martServiceRestFul2 = new MartServiceRestFul(restFulQuery2);
		
		
		
		if (null==linkIndexCreation) {
			linkIndexCreation = new LinkIndexCreation(martServiceRestFul1, martServiceRestFul2);
		}
		if (sameLeft) {
			linkIndexCreation.setMartServiceRestFul2(martServiceRestFul2);
		} else {
			linkIndexCreation.setMartServiceRestFul(martServiceRestFul1, martServiceRestFul2);
		}
		
		if (sameLeft==Boolean.FALSE || (sameLeft==Boolean.TRUE && linkIndexCreation.size1>0)) {
			// Using java (potential RAM problem)
			if (method.isZero()) {
				size = linkIndexCreation.createLinkIndexUsingJavaMemory(!sameLeft, null);
			}
			
			// Using linux (disk space problem possible, platform-dependant)
			else if (method.isMinus()) {
				linkIndexCreation = new LinkIndexCreation(martServiceRestFul1, martServiceRestFul2);	// Does not support keeping left side yet (could)
				size = linkIndexCreation.createLinkIndexUsingLinuxUniq();
			}
			
			// Using multi-batching mechanism (no potential memory problem but longer)
			else if (method.isPlus()) {
				//MultipleDatasetJoinCoordinator.buildIndex();
				//TODO
			}
		} else {
			size = 0;
		}
		
		timer.stopTimer();
		System.out.println("link index size = " + size + ", index creation time = " + timer.getTimeEllapsedMs() + "ms" + MyUtils.LINE_SEPARATOR);
		
		previousLink = link;
		linkIndexesCount++;
	}
//if (linkIndexesCount>=3) {break;}
}*/

/*if (debug) {System.out.println("t0 "); displayValidLinkList(validLinkList);}

if (validLinkList.size()==0) {
	t0++;
	resultLinkList.clear();	// No links between these 2 datasets
} else if (validLinkList.size()==1) {
	t1++;
	resultLinkList.add(validLinkList.get(0));	// Only 1 unambiguous link
} else {	// More than 1 with same name

	List<Link> bothDefaultValueTrueList = new ArrayList<Link>();
	for (Link link : validLinkList) {
		if (link.left.importable.bothDefaultValueTrue(link.right.exportable)) {
			bothDefaultValueTrueList.add(link);
		}
	}

	if (debug) {displayValidLinkList(bothDefaultValueTrueList);}
	
	if (bothDefaultValueTrueList.size()==1) {
		t5++;
		resultLinkList.add(bothDefaultValueTrueList.get(0));	// Only 1 unambiguous link left
	} else {
		if (bothDefaultValueTrueList.size()>1) {
truth=true;					
			validLinkList = bothDefaultValueTrueList;	// Narrow down the list of links of interest
		}
		List<Link> oneAndOnlyOneDefaultValueTrueList = new ArrayList<Link>();
		for (Link link : validLinkList) {
			if (link.left.importable.oneAndOnlyOneDefaultValueTrue(link.right.exportable)) {
				oneAndOnlyOneDefaultValueTrueList.add(link);
			}
		}

		if (debug) {displayValidLinkList(oneAndOnlyOneDefaultValueTrueList);}
		
		if (oneAndOnlyOneDefaultValueTrueList.size()==1) {
			t6++;
			resultLinkList.add(oneAndOnlyOneDefaultValueTrueList.get(0));	// Only 1 unambiguous link left
		} else {
			if (oneAndOnlyOneDefaultValueTrueList.size()>1) {
truth=true;							
				validLinkList = oneAndOnlyOneDefaultValueTrueList;	// Narrow down the list of links of interest
			}
			List<Link> bothDefaultValueFalseList = new ArrayList<Link>();
			for (Link link : validLinkList) {
				if (link.left.importable.bothDefaultValueFalse(link.right.exportable)) {
					bothDefaultValueFalseList.add(link);
				}
			}							

			if (debug) {displayValidLinkList(bothDefaultValueFalseList);}
			
			if (bothDefaultValueFalseList.size()==1) {
				t7++;
				resultLinkList.add(bothDefaultValueFalseList.get(0));	// Only 1 unambiguous link left
			} else {
				if (bothDefaultValueFalseList.size()>1) {
					validLinkList = bothDefaultValueFalseList;	// Narrow down the list of links of interest
				}
				
				if (debug) if (true) {System.out.println("##########");displayValidLinkList(validLinkList);System.out.println(resultLinkList);MyUtils.wrappedReadInput();}

				t8++;
				
				Collections.sort(resultLinkList);
				resultLinkList.add(truth ? validLinkList.get(0) : validLinkList.get(validLinkList.size()-1));	// Arbitrarily take the last one of the latest valid list (alphabetically based on link name)
				
//System.out.println("##########");ddd(validLinkList);System.out.println(resultLinkList);						
			}
		}
	}
}

if (debug) {
	System.out.println();
}
	
return resultLinkList;*/