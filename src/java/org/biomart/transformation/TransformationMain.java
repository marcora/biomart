package org.biomart.transformation;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.helpers.DatabaseParameter;
import org.biomart.objects.helpers.Rdbs;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.old.martService.Configuration;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.objects.DatasetInMart;
import org.biomart.old.martService.objects.MartInVirtualSchema;
import org.biomart.transformation.helpers.HostAndVirtualSchema;
import org.biomart.transformation.helpers.MartServiceIdentifier;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationGeneralVariable;
import org.biomart.transformation.helpers.TransformationParameter;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

public class TransformationMain {
	
	public static final boolean ENABLE_PROPERTIES_CHECK = true;
	
	public static String TRANSFORMATIONS_GENERAL_OUTPUT = "/home/anthony/Desktop/Transformation/";
	
	public static final String DEFAULT_DATA_FOLDER_NAME = "DataFolder";
	public static final String DATABASE_DESCRIPTION_OUTPUT_FILE_NAME = "DatabaseDescription";
	public static final String TABLE_LIST_SERIAL_FILE_NAME = "SerialTableList";
	public static final String TABLE_COLUMN_MAP_CONTENT_SERIAL_FILE_NAME = "SerialTableColumMap";
	public static final Rdbs RDBS = Rdbs.MYSQL;
	public static final String DATABASE_HOST = "martdb.ensembl.org";
	public static final Integer DATABASE_PORT = 5316;
	public static final String DATABASE_USER = "anonymous";
	public static final String DATABASE_PASSWORD = "";
	public static final DatabaseParameter DATABASE_PARAMETER = 
		new DatabaseParameter(RDBS, DATABASE_HOST, DATABASE_PORT, DATABASE_USER, DATABASE_PASSWORD);
	public static final String[] DATABASE_NAMES = new String[] {
		
		/*
		- ensembl_mart_53 to 55
		- vega_mart_53 to 55
		- snp_mart_53 to 55
		*/
		
		// gene_ensembl (55)
		"ensembl_mart_55", "vega_mart_55", "snp_mart_55", "sequence_mart_55", "ontology_mart_55", "genomic_features_mart_55", "functional_genomics_mart_55"
		
		// gene_ensembl (54)		
		//"ensembl_mart_54", "vega_mart_54", "snp_mart_54", "sequence_mart_54", "ontology_mart_54", "genomic_features_mart_54"

		// gene_ensembl (53)
		//"ensembl_mart_53", "vega_mart_53", "snp_mart_53", "sequence_mart_53", "ontology_mart_53", "genomic_features_mart_53"

		// gene_vega
		//"vega_mart_55", "genomic_features_mart_55", "sequence_mart_55"
		
		// ensembl bacteria
		//"bacterial_mart_54" ?
		
		// UNIPROT
		//"SPMART"
		
		// marker_feature & encode
		//"genomic_features_mart_55"
	};
	
	/*
		ensembl_mart_55={gene_ensembl=[aaegypti_gene_ensembl, agambiae_gene_ensembl, btaurus_gene_ensembl, celegans_gene_ensembl, cfamiliaris_gene_ensembl, cintestinalis_gene_ensembl, cporcellus_gene_ensembl, csavignyi_gene_ensembl, dmelanogaster_gene_ensembl, dnovemcinctus_gene_ensembl, drerio_gene_ensembl, ecaballus_gene_ensembl, eeuropaeus_gene_ensembl, etelfairi_gene_ensembl, fcatus_gene_ensembl, gaculeatus_gene_ensembl, ggallus_gene_ensembl, hsapiens_gene_ensembl, lafricana_gene_ensembl, mdomestica_gene_ensembl, mlucifugus_gene_ensembl, mmulatta_gene_ensembl, mmusculus_gene_ensembl, oanatinus_gene_ensembl, ocuniculus_gene_ensembl, ogarnettii_gene_ensembl, olatipes_gene_ensembl, ppygmaeus_gene_ensembl, rnorvegicus_gene_ensembl, saraneus_gene_ensembl, scerevisiae_gene_ensembl, stridecemlineatus_gene_ensembl, tbelangeri_gene_ensembl, tnigroviridis_gene_ensembl, trubripes_gene_ensembl, xtropicalis_gene_ensembl, ptroglodytes_gene_ensembl, oprinceps_gene_ensembl, mmurinus_gene_ensembl, ttruncatus_gene_ensembl, pvampyrus_gene_ensembl, tsyrichta_gene_ensembl, vpacos_gene_ensembl, dordii_gene_ensembl, pcapensis_gene_ensembl, ggorilla_gene_ensembl, acarolinensis_gene_ensembl, choffmanni_gene_ensembl, tguttata_gene_ensembl, meugenii_gene_ensembl]}, 
		functional_genomics_mart_55={regulatory_feature=[hsapiens_regulatory_feature, mmusculus_regulatory_feature], external_feature=[hsapiens_external_feature, dmelanogaster_external_feature, mmusculus_external_feature], feature_set=[hsapiens_feature_set, dmelanogaster_feature_set, mmusculus_feature_set], annotated_feature=[hsapiens_annotated_feature, mmusculus_annotated_feature]}, 
		vega_mart_55={gene_vega=[hsapiens_gene_vega, mmusculus_gene_vega]}, 
		sequence_mart_55={aaegypti_genomic_sequence=[aaegypti_genomic_sequence], mmulatta_genomic_sequence=[mmulatta_genomic_sequence], dordii_genomic_sequence=[dordii_genomic_sequence], xtropicalis_genomic_sequence=[xtropicalis_genomic_sequence], lafricana_genomic_sequence=[lafricana_genomic_sequence], tguttata_genomic_sequence=[tguttata_genomic_sequence], ggallus_genomic_sequence=[ggallus_genomic_sequence], cfamiliaris_genomic_sequence=[cfamiliaris_genomic_sequence], mlucifugus_genomic_sequence=[mlucifugus_genomic_sequence], mmurinus_genomic_sequence=[mmurinus_genomic_sequence], btaurus_genomic_sequence=[btaurus_genomic_sequence], acarolinensis_genomic_sequence=[acarolinensis_genomic_sequence], cporcellus_genomic_sequence=[cporcellus_genomic_sequence], ecaballus_genomic_sequence=[ecaballus_genomic_sequence], csavignyi_genomic_sequence=[csavignyi_genomic_sequence], pvampyrus_genomic_sequence=[pvampyrus_genomic_sequence], drerio_genomic_sequence=[drerio_genomic_sequence], etelfairi_genomic_sequence=[etelfairi_genomic_sequence], ttruncatus_genomic_sequence=[ttruncatus_genomic_sequence], ptroglodytes_genomic_sequence=[ptroglodytes_genomic_sequence], eeuropaeus_genomic_sequence=[eeuropaeus_genomic_sequence], tnigroviridis_genomic_sequence=[tnigroviridis_genomic_sequence], cintestinalis_genomic_sequence=[cintestinalis_genomic_sequence], ocuniculus_genomic_sequence=[ocuniculus_genomic_sequence], rnorvegicus_genomic_sequence=[rnorvegicus_genomic_sequence], tbelangeri_genomic_sequence=[tbelangeri_genomic_sequence], ppygmaeus_genomic_sequence=[ppygmaeus_genomic_sequence], vpacos_genomic_sequence=[vpacos_genomic_sequence], dmelanogaster_genomic_sequence=[dmelanogaster_genomic_sequence], celegans_genomic_sequence=[celegans_genomic_sequence], trubripes_genomic_sequence=[trubripes_genomic_sequence], ogarnettii_genomic_sequence=[ogarnettii_genomic_sequence], pcapensis_genomic_sequence=[pcapensis_genomic_sequence], oanatinus_genomic_sequence=[oanatinus_genomic_sequence], agambiae_genomic_sequence=[agambiae_genomic_sequence], olatipes_genomic_sequence=[olatipes_genomic_sequence], gaculeatus_genomic_sequence=[gaculeatus_genomic_sequence], oprinceps_genomic_sequence=[oprinceps_genomic_sequence], tsyrichta_genomic_sequence=[tsyrichta_genomic_sequence], ggorilla_genomic_sequence=[ggorilla_genomic_sequence], mdomestica_genomic_sequence=[mdomestica_genomic_sequence], fcatus_genomic_sequence=[fcatus_genomic_sequence], scerevisiae_genomic_sequence=[scerevisiae_genomic_sequence], stridecemlineatus_genomic_sequence=[stridecemlineatus_genomic_sequence], hsapiens_genomic_sequence=[hsapiens_genomic_sequence], meugenii_genomic_sequence=[meugenii_genomic_sequence], mmusculus_genomic_sequence=[mmusculus_genomic_sequence], choffmanni_genomic_sequence=[choffmanni_genomic_sequence], saraneus_genomic_sequence=[saraneus_genomic_sequence], dnovemcinctus_genomic_sequence=[dnovemcinctus_genomic_sequence]}, 
		ontology_mart_55={hsap_evoc_CellType=[hsap_evoc_CellType], closure_cellular_component=[closure_cellular_component], hsap_evoc_DevelopmentStage=[hsap_evoc_DevelopmentStage], drer_evoc_DevelopmentalStage=[drer_evoc_DevelopmentalStage], hsap_evoc_Pathology=[hsap_evoc_Pathology], drer_evoc_AnatomicalTerms=[drer_evoc_AnatomicalTerms], closure_biological_process=[closure_biological_process], closure_molecular_function=[closure_molecular_function], hsap_evoc_AnatomicalSystem=[hsap_evoc_AnatomicalSystem]}, 
		genomic_features_mart_55={encode=[hsap_encode], marker_feature_copy=[agam_marker_end, drer_marker_end, ggal_marker_end, hsap_marker_end, mmus_marker_end, rnor_marker_end, mmul_marker_end, gacu_marker_end, ecab_marker_end, cfam_marker_end, btau_marker_end], marker_feature=[agam_marker_start, drer_marker_start, ggal_marker_start, hsap_marker_start, mmus_marker_start, rnor_marker_start, mmul_marker_start, gacu_marker_start, ecab_marker_start, cfam_marker_start, btau_marker_start], misc_feature=[agam_misc_feature_copy, drer_misc_feature_copy, hsap_misc_feature_copy], karyotype=[agam_karyotype_start, dmel_karyotype_start, hsap_karyotype_start, mmus_karyotype_start, rnor_karyotype_start], karyotype_copy=[agam_karyotype_end, dmel_karyotype_end, hsap_karyotype_end, mmus_karyotype_end, rnor_karyotype_end]}, 
		snp_mart_55={variation=[agambiae_snp, btaurus_snp, cfamiliaris_snp, drerio_snp, ggallus_snp, hsapiens_snp, mmusculus_snp, oanatinus_snp, ppygmaeus_snp, ptroglodytes_snp, rnorvegicus_snp, tnigroviridis_snp, tguttata_snp]
	*/
	
	private static Map<String, Configuration> webServiceConfigurationMap = null;
	private static Configuration initialConfiguration = null;
	public static Map<String, Configuration> getWebServiceConfigurationMap() {
		return webServiceConfigurationMap;
	}
	public static Configuration getInitialConfiguration() {
		return initialConfiguration;
	}

	public static void main(String[] args) {
		rebuildCentralPortalRegistry();
		//run();
	}
	
	public static void rebuildCentralPortalRegistry() {
		
		MartRegistry martRegistry = new MartRegistry();
        
		List<MartRegistry> martRegistryList = run();
		
		List<Location> locationList = new ArrayList<Location>();
		Map<Location, List<Mart>> martMap = new HashMap<Location, List<Mart>>();
		for (MartRegistry martRegistryTmp : martRegistryList) {
			List<Location> locationListTmp = martRegistryTmp.getLocationList();
			for (Location location : locationListTmp) {
				if (!locationList.contains(location)) {
					martRegistry.addLocation(location);
					locationList.add(location);
					martMap.put(location, location.getMartList());
				} else {
					Location currentLocation = locationList.get(locationList.indexOf(location));
					List<Mart> martListTmp = location.getMartList();
					List<Mart> martList = martMap.get(location);
					for (Mart mart : martListTmp) {
						if (!martList.contains(mart)) {
							currentLocation.addMart(mart);
							martList.add(mart);
						} else {
							Mart currentMart = martList.get(martList.indexOf(mart));
							List<Dataset> datasetList = mart.getDatasetList();
							for (Dataset dataset : datasetList) {
								currentMart.addDataset(dataset);
							}
						}
					}
				}
			}
		}
		
		Element newRootElement = martRegistry.generateXml();
		try {
			MyUtils.writeSerializedObject(martRegistry, "./conf/files/" + 
					(webServiceTransformation ? "web" : "db") + "_portal.serial");
			MyUtils.writeXmlFile(newRootElement, "./conf/xml/" + 
					(webServiceTransformation ? "web" : "db") + "_portal.xml");
		} catch (TechnicalException e) {
			e.printStackTrace();
		}
	}
	
	static boolean webServiceTransformation = true;
	
	public static List<MartRegistry> run() {
		List<MartRegistry> martRegistryList = new ArrayList<MartRegistry>();
		try {
			
			MyUtils.CHECK = true;
			MyUtils.EXCEPTION = true;
			MyUtils.EXIT_PROGRAM = true;
			
			if (!webServiceTransformation) {

				
				//transform(false, "55", "default", "gene_ensembl");
				//transform(false, "55", "default", "gene_vega");
				//transform(false, "55", "default", "variation");
				
				martRegistryList.add(transform(false, "55", "default", TRANSFORMATIONS_GENERAL_OUTPUT, "gene_ensembl").getMartRegistry());
				martRegistryList.add(transform(false, "55", "default", TRANSFORMATIONS_GENERAL_OUTPUT, "gene_vega").getMartRegistry());
				martRegistryList.add(transform(false, "55", "default", TRANSFORMATIONS_GENERAL_OUTPUT, "variation").getMartRegistry());
				
			} else {
				MartServiceIdentifier initialHost = new MartServiceIdentifier(
						MyConstants.CENTRAL_PORTAL_SERVER, String.valueOf(80), MartServiceConstants.DEFAULT_PATH_TO_MART_SERVICE);
				
				String configurationMapSerialFilePathAndName = 
					fetchWebServiceConfigurationMap(initialHost, TRANSFORMATIONS_GENERAL_OUTPUT);
				MyUtils.checkStatusProgram(null!=webServiceConfigurationMap && webServiceConfigurationMap.size()>=1 && initialConfiguration!=null);
				
				String martName = "ensembl";
				String datasetName = "hsapiens_gene_ensembl";
				HostAndVirtualSchema hostAndVirtualSchema = computeHostAndVirtualSchema(martName);
				Transformation transformation = transform(true, "55", initialHost, hostAndVirtualSchema.getMartServiceIdentifier(), 
						TRANSFORMATIONS_GENERAL_OUTPUT, hostAndVirtualSchema.getVirtualSchema(), datasetName);
				MartRegistry martRegistry = transformation.getMartRegistry();
				martRegistryList.add(martRegistry);
				
				String martName2 = "ensembl";
				String datasetName2 = "mmusculus_gene_ensembl";
				HostAndVirtualSchema hostAndVirtualSchema2 = computeHostAndVirtualSchema(martName2);
				Transformation transformation2 = transform(true, "55", initialHost, hostAndVirtualSchema2.getMartServiceIdentifier(), 
						TRANSFORMATIONS_GENERAL_OUTPUT, hostAndVirtualSchema2.getVirtualSchema(), datasetName2);
				MartRegistry martRegistry2 = transformation2.getMartRegistry();
				martRegistryList.add(martRegistry2);
				
				String martName4 = "uniprot_mart";
				String datasetName4 = "UNIPROT";
				HostAndVirtualSchema hostAndVirtualSchema4 = computeHostAndVirtualSchema(martName4);
				Transformation transformation4 = transform(true, "55", initialHost, hostAndVirtualSchema4.getMartServiceIdentifier(), 
						TRANSFORMATIONS_GENERAL_OUTPUT, hostAndVirtualSchema4.getVirtualSchema(), datasetName4);
				MartRegistry martRegistry4 = transformation4.getMartRegistry();
				martRegistryList.add(martRegistry4);
				
				//martRegistryList.addAll(webTransformCentralPortal(initialHost));
				
				System.out.println("webServiceConfigurationMap = " + webServiceConfigurationMap.keySet());
				MyUtils.writeSerializedObject(webServiceConfigurationMap, configurationMapSerialFilePathAndName);
			}
		} catch (TechnicalException e) {
			e.printStackTrace();
		} catch (FunctionalException e) {
			e.printStackTrace();
		}
		return martRegistryList;
	}

	public static List<String> ignoreMart = new ArrayList<String>(Arrays.asList(new String[] {

			"ENSEMBL_MART_ENSEMBL", "ENSEMBL_MART_SNP", "GRAMENE_MAP_30", "GRAMENE_MARKER_30", "GRAMENE_ONTOLOGY_30",	// portal out of sync
"dicty",	// server down
			
/*"Eurexpress Biomart",	// ok now?
"htgt",
"pepseekerGOLD_mart06",*/
	}));
	public static List<String> ignoreDataset = new ArrayList<String>(Arrays.asList(new String[] {
	}));
	
	@SuppressWarnings("unused")
	private static List<MartRegistry> webTransformCentralPortal(MartServiceIdentifier martServiceIdentifier) throws TechnicalException, FunctionalException {

		List<MartRegistry> martRegistryList = new ArrayList<MartRegistry>();
		
		String formattedMartServiceStringUrl = martServiceIdentifier.formatMartServiceUrl();
	
		Configuration biomartPortalConfiguration = webServiceConfigurationMap.get(formattedMartServiceStringUrl);
boolean ok = true;			
			for (Iterator<String> it = biomartPortalConfiguration.virtualSchemaMartSetMap.keySet().iterator(); it.hasNext();) {
				String currentServerVirtualSchema = it.next();
				Set<MartInVirtualSchema> martSet = biomartPortalConfiguration.virtualSchemaMartSetMap.get(currentServerVirtualSchema);
				for (MartInVirtualSchema currentMart : martSet) {
					System.out.println("> " + currentMart);

				List<DatasetInMart> biomartPortalDatasetList = biomartPortalConfiguration.martDatasetListMap.get(currentMart.martName);
				for (DatasetInMart biomartPortalDataset : biomartPortalDatasetList) {
					System.out.println("\t> " + currentMart.martName + " - " + biomartPortalDataset);
					if (!biomartPortalDataset.getVisible()) {
						System.out.println("invisible...");
					} else if (TransformationUtils.isGenomicSequence(biomartPortalDataset.getDatasetType())) {
						System.out.println("GenomicSequence...");
					} else {

if (!ok && (!"Pancreatic_Expression".equals(currentMart.martName) || !"hsapiens_gene_ensembl".equals(biomartPortalDataset.datasetName))) {
	continue;
} else {
	ok = true;
}
							
if (ignoreDataset.contains(biomartPortalDataset.datasetName)) {
	continue;
}
if (ignoreMart.contains(currentMart.martName)) {
	continue;
}

if (currentMart.martName.equals("Pancreatic_Expression") && biomartPortalDataset.datasetName.equals("hsapiens_gene_ensembl")) continue; // broken config
	//invalid filter: container that isn't a container: <FilterDescription displayName="Chromosome name" field="chr_name" tableConstraint="main" multipleValues="" graph="" type="list" displayType="container" internalName="chromosome_name" qualifier="=" autoCompletion="" key="gene_id_key" style="" legal_qualifiers="=" otherFilters="hsapiens_band_start.chrom_name_band_start;hsapiens_band_end.chrom_name_band_end;hsapiens_marker_start.marker_start_chrom_name;hsapiens_marker_end.marker_end_chrom_name">
if (currentMart.martName.equals("SEQ") && biomartPortalDataset.datasetName.equals("rnorvegicus_expr_gene_ensembl")) continue; // pointed server unvailable
if (currentMart.martName.equals("ensembl_expressionmart_48") && biomartPortalDataset.datasetName.equals("rnorvegicus_expr_gene_ensembl")) continue; // pointed server unvailable
/*if (!(currentMart.martName.equals("ENSEMBL_MART_ENSEMBL") && biomartPortalDataset.datasetName.equals("ojaponica_gene_ensembl"))) continue;	// doesn't exist anymore*/ // mart discarded anyway

						String martName = currentMart.getMartName();
						HostAndVirtualSchema hostAndVirtualSchema = computeHostAndVirtualSchema(martName);
						Transformation transformation = transform(true, null, martServiceIdentifier, hostAndVirtualSchema.getMartServiceIdentifier(), 
							TRANSFORMATIONS_GENERAL_OUTPUT, hostAndVirtualSchema.getVirtualSchema(), biomartPortalDataset.datasetName);
						martRegistryList.add(transformation.getMartRegistry());

					}
				}
			}
		}
		return martRegistryList;
	}
	
	/**
	 * Will recursively fetch configs
	 * @param martName
	 * @return
	 * @throws TechnicalException
	 * @throws FunctionalException
	 */
	public static HostAndVirtualSchema computeHostAndVirtualSchema(String martName) throws TechnicalException, FunctionalException {
		return computeHostAndVirtualSchema(0, initialConfiguration, martName);
	}
	private static HostAndVirtualSchema computeHostAndVirtualSchema(int depth, Configuration configuration, String martName) throws TechnicalException, FunctionalException {
		MartInVirtualSchema mart = configuration.getMartByName(martName);	
		MyUtils.checkStatusProgram(null!=mart, martName + ", " + configuration.getMartServiceUrl());
		MartServiceIdentifier martServiceIdentifier = new MartServiceIdentifier(mart.getHost(), mart.getPort(), mart.getPath());
		
		if (!mart.getLocal()) {
			expandWebServiceConfigurationMap(martServiceIdentifier);
		}
		return new HostAndVirtualSchema(martServiceIdentifier, mart.getServerVirtualSchema());
		/*} else {
			Configuration newConfiguration = expandWebServiceConfigurationMap(
					martServiceIdentifier);
			MyUtils.checkStatusProgram(!configuration.equals(newConfiguration), 
					configuration.getMartServiceUrl() + ", " + newConfiguration.getMartServiceUrl());
			return computeHostAndVirtualSchema(depth+1, newConfiguration, mart.martName);
		}*/
	}
	
	public static Transformation transform(boolean webService, String version, String transformationsGeneralOutput,
			String virtualSchema, String templateName)
	throws TechnicalException, FunctionalException{
		return transform(webService, version, null, null, transformationsGeneralOutput, virtualSchema, null, templateName);
	}
	public static Transformation transform(boolean webService, String version, 
			MartServiceIdentifier portalIdentifier, MartServiceIdentifier hostLocal, String transformationsGeneralOutput,
			String virtualSchema, String datasetName)
	throws TechnicalException, FunctionalException{
		return transform(webService, version, portalIdentifier, hostLocal, transformationsGeneralOutput, virtualSchema, datasetName, null);
	}
	public static Transformation transform(boolean webService, String version, 
			MartServiceIdentifier portalIdentifier, MartServiceIdentifier trueHostIdentifier, String transformationsGeneralOutput,
			String virtualSchema, String datasetName, String templateName)
	throws TechnicalException, FunctionalException{
		System.out.println("start.");
		
		String transformationTypeFolderName = TransformationUtils.generateTransformationTypeFolderName(webService, version);
		String datasetOrTemplateName = templateName!=null ? templateName : datasetName;
		String datasetGeneralOutputFolderPathAndName = transformationsGeneralOutput + transformationTypeFolderName + MyUtils.FILE_SEPARATOR + 
		TransformationUtils.generateIdentifier(webService, version, trueHostIdentifier, virtualSchema, datasetOrTemplateName) + MyUtils.FILE_SEPARATOR;	
		
		//Properties properties = getPropertiesFile();
				
		TransformationGeneralVariable general = new TransformationGeneralVariable(version,
				(webService ? webServiceConfigurationMap : null), portalIdentifier,
				transformationsGeneralOutput, datasetGeneralOutputFolderPathAndName,
				DATABASE_PARAMETER, DATABASE_NAMES);
		
		// Fetch all database information if not using webservice 
		String xmlFilePathAndName = null;
		if (!webService) {
			general.getDatabaseCheck().fetchDatasetInformation();				
		} else {
			System.out.println("webServiceConfigurationMap = " + webServiceConfigurationMap.keySet());
			Configuration configuration = webServiceConfigurationMap.get(trueHostIdentifier.formatMartServiceUrl());
			MyUtils.checkStatusProgram(null!=configuration, trueHostIdentifier.formatMartServiceUrl());
				
			Document xmlDocument = configuration.getXml(virtualSchema, datasetName); 
			/*TransformationUtils.fetchMartServiceXmlDocument(
			general.getServer(), general.getPathToMartService(), virtualSchema, datasetName);*/
			
			general.getDatasetNameToWebServiceJdomDocument().put(datasetName, xmlDocument);
			MyUtils.checkStatusProgram(null!=xmlDocument);	
			xmlFilePathAndName = TransformationUtils.writeWebServiceXmlConfigurationFile(
					xmlDocument, general.getTransformationsGeneralOutput(), 
					webService, version, trueHostIdentifier, virtualSchema, datasetName);
			System.out.println("transforming " + xmlFilePathAndName);
		}

		TransformationParameter params = new TransformationParameter(webService, trueHostIdentifier, virtualSchema,
				datasetName, templateName, general, DEFAULT_DATA_FOLDER_NAME);

		Transformation transformation = new Transformation(general, params);
		transformation.transform();
		
		// Close warning/error file
		try {
			params.getErrorFileWriter().close();
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		
		System.out.println("done.");	// If we see that, we didn't get a fatal exception
		
		return transformation;
	}

	public static Configuration expandWebServiceConfigurationMap(MartServiceIdentifier martServiceIdentifier) throws FunctionalException, TechnicalException {
		String formattedMartServiceServer = martServiceIdentifier.formatMartServiceUrl();
		Configuration configuration = webServiceConfigurationMap.get(formattedMartServiceServer);
		if (configuration==null) {
			configuration = addConfiguration(formattedMartServiceServer);
		}
		return configuration;
	}
	public static String fetchWebServiceConfigurationMap(MartServiceIdentifier initialHost, Configuration configuration) throws FunctionalException, TechnicalException {
		return fetchWebServiceConfigurationMap(initialHost, null, configuration);
	}
	public static String fetchWebServiceConfigurationMap(MartServiceIdentifier initialHost, String transformationsGeneralOutput) throws FunctionalException, TechnicalException {
		return fetchWebServiceConfigurationMap(initialHost, transformationsGeneralOutput, null);
	}
	public static String fetchWebServiceConfigurationMap(MartServiceIdentifier initialHost) throws FunctionalException, TechnicalException {
		return fetchWebServiceConfigurationMap(initialHost, null, null);
	}
	private static String fetchWebServiceConfigurationMap(MartServiceIdentifier initialHost, 
			String transformationsGeneralOutput, Configuration configuration) throws FunctionalException, TechnicalException {
		
		String initialFormattedMartServiceServer = initialHost.formatMartServiceUrl();
		
		String configurationMapSerialFilePathAndName = 
			transformationsGeneralOutput + MyUtils.FILE_SEPARATOR + TransformationConstants.TRANSFORMATIONS_GENERAL_SERIAL_FOLDER_NAME + 
			MyUtils.FILE_SEPARATOR + TransformationConstants.WEB_SERVICE_CONFIGURATION_SERIAL_FILE_NAME;
		if (transformationsGeneralOutput!=null &&	// if we don't consider serialization 
				new File(configurationMapSerialFilePathAndName).exists()) {
			System.out.println("Using serial at " + configurationMapSerialFilePathAndName);
			webServiceConfigurationMap = (Map<String, Configuration>)MyUtils.readSerializedObject(configurationMapSerialFilePathAndName);
			System.out.println("quick check: " + 
					(webServiceConfigurationMap!=null ? webServiceConfigurationMap.size() : null));
			
			initialConfiguration = webServiceConfigurationMap.get(initialFormattedMartServiceServer);
			MyUtils.checkStatusProgram(null!=initialConfiguration);
		} else {
			if (webServiceConfigurationMap==null) {
				webServiceConfigurationMap = new HashMap<String, Configuration>();
			}
			if (initialConfiguration==null) {
				if (configuration==null) { 	// this means a configuration was provided already, no need to fetch anything
					System.out.println("No serial at " + configurationMapSerialFilePathAndName + " , fetching...");
					initialConfiguration = addConfiguration(initialFormattedMartServiceServer);					
				} else {
					MyUtils.checkStatusProgram(webServiceConfigurationMap.get(initialFormattedMartServiceServer)==null);
					webServiceConfigurationMap.put(initialFormattedMartServiceServer, configuration);
					initialConfiguration = configuration;
				}
			}
		}
		MyUtils.checkStatusProgram(webServiceConfigurationMap!=null && webServiceConfigurationMap.size()>=1 && initialConfiguration!=null);
		System.out.println("webServiceConfigurationMap = " + webServiceConfigurationMap.keySet());

		return configurationMapSerialFilePathAndName;
	}

	public static Configuration addConfiguration(String formattedMartServiceServer) throws TechnicalException, FunctionalException {
		Configuration configuration = fetchConfiguration(formattedMartServiceServer);
		MyUtils.checkStatusProgram(webServiceConfigurationMap.get(formattedMartServiceServer)==null);
		webServiceConfigurationMap.put(formattedMartServiceServer, configuration);
		return configuration;
	}
	public static Configuration fetchConfiguration(
			String formattedMartServiceServer) throws FunctionalException,
			TechnicalException {
		System.out.println("fetching " + formattedMartServiceServer);
		Configuration configuration = new Configuration(formattedMartServiceServer, false);
		try {
			configuration.fetchMartSet();
			configuration.fetchDatasets();
		} catch (MalformedURLException e) {
			throw new TechnicalException(e);
		} catch (JDOMException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		} catch (InterruptedException e) {
			throw new TechnicalException(e);
		}
		return configuration;
	}

	private static void closeErrorFileWriter(TransformationParameter params) throws TechnicalException {
		try {
			params.getErrorFileWriter().close();
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}
}
