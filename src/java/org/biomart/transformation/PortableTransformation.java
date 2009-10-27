package org.biomart.transformation;


import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Element;
import org.biomart.objects.objects.Exportable;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.Importable;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.Portable;
import org.biomart.objects.objects.Range;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.transformation.helpers.TransformationConstants;
import org.biomart.transformation.helpers.TransformationHelper;
import org.biomart.transformation.helpers.TransformationParameter;
import org.biomart.transformation.helpers.TransformationUtils;
import org.biomart.transformation.helpers.TransformationVariable;
import org.biomart.transformation.oldXmlObjects.OldExportable;
import org.biomart.transformation.oldXmlObjects.OldImportable;
import org.biomart.transformation.oldXmlObjects.OldPortable;


public class PortableTransformation {
	
	protected TransformationParameter params = null;
	protected TransformationVariable vars = null;
	protected TransformationHelper help = null;
		
	public PortableTransformation (TransformationParameter params, TransformationVariable vars, TransformationHelper help) {	
		this.params = params;
		this.vars = vars;
		this.help = help;
	}
	
	public void transformImportables(Config config, List<OldImportable> oldImportableList) throws TechnicalException, FunctionalException {
		for (OldImportable oldImportable : oldImportableList) {
			Importable importable = (Importable)transformPortable((OldPortable)oldImportable, true);
			if (null!=importable) {
				config.addImportable(importable);
			}
		}
	}
	
	public void transformExportables(Config config, List<OldExportable> oldExportableList) throws TechnicalException, FunctionalException {
		for (OldExportable oldExportable : oldExportableList) {
			Exportable exportable = (Exportable)transformPortable((OldPortable)oldExportable, false);
			if (null!=exportable) {
				config.addExportable(exportable);
			}
		}
	}

	/*
	 
	  <Exportable attributes="ensembl_gene_id,ensembl_transcript_id,chromosome_name,exon_chrom_start,exon_chrom_end,strand,transcript_count" internalName="gene_exon_intron" linkName="gene_exon_intron" linkVersion="*link_version*" name="gene_exon_intron" orderBy="ensembl_gene_id" type="link" />
	 
	  <Importable filters="ensembl_gene_id" internalName="ensembl_das_gene" linkName="ensembl_das_gene" name="ensembl_das_gene" type="dasGene" />
	  <Importable filters="link_ensembl_gene_id" internalName="gene_stable_id" linkName="*species3*_gene_stable_id" name="*species3*_gene_stable_id" type="link" />
	  <Importable filters="link_development_stage" internalName="development_stage_term" linkName="development_stage_term" name="development_stage_term" type="link" />
	  
	  <importable name="imp_ensembl_gene_id" filters="ensembl_gene_id" range="[P1R1][P1R2][P1R3]"/>
	  
	  
	 	Exportable that would need better intersection
		"(PmC0).gene_exon_intron", "(PmC0).transcript_exon_intron", "(PmC0).gene_flank", "(PmC0).transcript_flank", 
		"(PmC0).coding_gene_flank", "(PmC0).coding_transcript_flank", "(PmC0).3utr", "(PmC0).5utr", "(PmC0).cdna", "(PmC0).gene_exon",
		"(PmC0).peptide", "(PmC0).coding"
	*/

	public Portable transformPortable(OldPortable oldPortable, boolean isImportable) throws TechnicalException, FunctionalException {
		
		MyUtils.checkStatusProgram(!help.containsAliases(oldPortable.getInternalName()));
		PartitionTable mainPartitionTable = vars.getMainPartitionTable();	
		/*PartitionReference mainPartitionReference = new PartitionReference(mainPartitionTable);*/
		String portableName = /*mainPartitionReference.toXmlString() + TransformationConstants.DOT + */oldPortable.getInternalName();
		
		Portable portable = isImportable ? 
				new Importable(mainPartitionTable, portableName) : 
				new Exportable(mainPartitionTable, portableName);
	
		OldImportable oldImportable = null;
		OldExportable oldExportable = null;
		Importable importable = null;
		Exportable exportable = null;
		if (isImportable) {
			MyUtils.checkStatusProgram(oldPortable instanceof OldImportable);
			oldImportable = (OldImportable)oldPortable;
			importable = (Importable)portable;
		} else if (oldPortable instanceof OldExportable) {
			MyUtils.checkStatusProgram(oldPortable instanceof OldExportable);
			oldExportable = (OldExportable)oldPortable;
			exportable = (Exportable)portable;
		}
		
		// For backward compatibility: have to keep them	TODO think about it
		String formerLinkName = help.replaceAliases(oldPortable.getLinkName());
		String formerLinkVersion = oldPortable.getLinkVersion();	// linkVersion is actually never unaliased by the system (cf link creation)
																	// cf xml for gene_vega for instance, *link_version2* appears but is never even defined
		portable.setFormerLinkName(formerLinkName);
		portable.setFormerLinkVersion(formerLinkVersion);
		if (!isImportable) {
			MyUtils.checkStatusProgram(!help.containsAliases(String.valueOf(oldExportable.getDefault_())));
			Boolean formerDefault = oldExportable.getDefault_();
			exportable.setFormerDefault(formerDefault);
		}
		
		// Fill the list of Element
		if (isImportable) {
			List<String> filtersList = oldImportable.getFilters();
			for (String filterName : filtersList) {
				Filter filter = vars.getFilterMap().get(filterName);
				if (!TransformationUtils.checkForWarning(null==filter, vars.getPortableReferencesAnInvalidElementList(),
						"importable " + portableName + " references an invalid filter: " + filterName)) {
					return null;
				}
				importable.addFilter(filter);
			}
		} else {
			List<String> attributesList = oldExportable.getAttributes();
			for (String attributeName : attributesList) {
				Attribute attribute = vars.getAttributeMap().get(attributeName);
				if (!TransformationUtils.checkForWarning(null==attribute, vars.getPortableReferencesAnInvalidElementList(),
						"exportable " + portableName + " references an invalid attribute: " + attributeName)) {
					return null;
				}
				exportable.addAttribute(attribute);
			}
		}
		
		// Handle ranges
		if (TransformationConstants.PORTABLE_TYPE_FORMATTER.equals(oldPortable.getType())) {
			MyUtils.checkStatusProgram(!help.containsAliases(oldPortable.getLinkName()));
			//TODO ?
		} else if (TransformationConstants.PORTABLE_TYPE_DAS_GENE.equals(oldPortable.getType()) || 
				TransformationConstants.PORTABLE_TYPE_DAS_CHR.equals(oldPortable.getType())) {
			MyUtils.checkStatusProgram(!help.containsAliases(oldPortable.getLinkName()));
			//TODO ?
		} else {
			MyUtils.checkStatusProgram(MartServiceConstants.XML_ATTRIBUTE_VALUE_LINK.equals(oldPortable.getType()), 
					"oldPortable = " + MartConfiguratorUtils.displayJdomElement(oldPortable.getJdomElement()));
			MyUtils.checkStatusProgram(!oldPortable.getPointer());
				
			List<Element> elementList = new ArrayList<Element>(isImportable ? importable.getFilters() : exportable.getAttributes());
			List<Range> rangeList = new ArrayList<Range>();
	boolean needIntersection = false;	//TODO
			for (Element element : elementList) {
				Range targetRange = element.getTargetRange();
				rangeList.add(targetRange);
	if (rangeList.size()>1 && targetRange.getPartitionTableSet().size()>1) {
		System.out.println(targetRange.getXmlValue());
		needIntersection = true;
	}
			}
	if (needIntersection) {
		for (Element element : elementList) {
			System.out.println(MartConfiguratorUtils.displayJdomElement(element.generateXml()));
		}
	}
			
			// Compute intersection of the main table rows only for now (latest agreement)
			Range range = Range.mainRangesIntersection(mainPartitionTable, false, rangeList);
		
			portable.setRange(range);
			if (help.containsAliases(oldPortable.getLinkName())) {	// We know that linkName=name (checked when populating oldObjects)
						
			} else {
				
			}
		}	
		
		return portable;	
	}
}
