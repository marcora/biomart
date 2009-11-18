package org.biomart.transformation;


import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.XmlUtils;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.ElementList;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.Range;
import org.biomart.objects.objects.types.ElementListType;
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
	
	public void transformPortables(Config config, List<? extends OldPortable> oldPortableList, boolean isImportable) throws TechnicalException, FunctionalException {
		for (OldPortable oldPortable : oldPortableList) {
			ElementList portable = transformPortable(oldPortable);
			if (null!=portable) {
				if (isImportable) {
					config.addImportable(portable);
				} else {	// then exportable
					config.addExportable(portable);
				}
			}
		}
	}

	/*
	 
	  <Portable attributes="ensembl_gene_id,ensembl_transcript_id,chromosome_name,exon_chrom_start,exon_chrom_end,strand,transcript_count" internalName="gene_exon_intron" linkName="gene_exon_intron" linkVersion="*link_version*" name="gene_exon_intron" orderBy="ensembl_gene_id" type="link" />
	 
	  <Portable filters="ensembl_gene_id" internalName="ensembl_das_gene" linkName="ensembl_das_gene" name="ensembl_das_gene" type="dasGene" />
	  <Portable filters="link_ensembl_gene_id" internalName="gene_stable_id" linkName="*species3*_gene_stable_id" name="*species3*_gene_stable_id" type="link" />
	  <Portable filters="link_development_stage" internalName="development_stage_term" linkName="development_stage_term" name="development_stage_term" type="link" />
	  
	  <importable name="imp_ensembl_gene_id" filters="ensembl_gene_id" range="[P1R1][P1R2][P1R3]"/>
	  
	  
	 	Portable that would need better intersection
		"(PmC0).gene_exon_intron", "(PmC0).transcript_exon_intron", "(PmC0).gene_flank", "(PmC0).transcript_flank", 
		"(PmC0).coding_gene_flank", "(PmC0).coding_transcript_flank", "(PmC0).3utr", "(PmC0).5utr", "(PmC0).cdna", "(PmC0).gene_exon",
		"(PmC0).peptide", "(PmC0).coding"
	*/

	public ElementList transformPortable(OldPortable oldPortable) throws TechnicalException, FunctionalException {
		
		MyUtils.checkStatusProgram(!help.containsAliases(oldPortable.getInternalName()));
		PartitionTable mainPartitionTable = vars.getMainPartitionTable();	
		/*PartitionReference mainPartitionReference = new PartitionReference(mainPartitionTable);*/
		String portableName = /*mainPartitionReference.toXmlString() + TransformationConstants.DOT + */oldPortable.getInternalName();
		
		boolean isPortable = oldPortable instanceof OldImportable;
		ElementList portable = new ElementList(isPortable ? 
				ElementListType.IMPORTABLE : ElementListType.EXPORTABLE, portableName, mainPartitionTable);
	
		OldImportable oldImportable = null;
		OldExportable oldExportable = null;
		if (isPortable) {
			MyUtils.checkStatusProgram(oldPortable instanceof OldImportable);
			oldImportable = (OldImportable)oldPortable;
		} else if (oldPortable instanceof OldExportable) {
			MyUtils.checkStatusProgram(oldPortable instanceof OldExportable);
			oldExportable = (OldExportable)oldPortable;
		}
		
		// For backward compatibility: have to keep them	TODO think about it
		String formerLinkName = help.replaceAliases(oldPortable.getLinkName());
		String formerLinkVersion = oldPortable.getLinkVersion();	// linkVersion is actually never unaliased by the system (cf link creation)
																	// cf xml for gene_vega for instance, *link_version2* appears but is never even defined
		portable.setFormerLinkName(formerLinkName);
		portable.setFormerLinkVersion(formerLinkVersion);
		if (!isPortable) {
			MyUtils.checkStatusProgram(!help.containsAliases(String.valueOf(oldExportable.getDefault_())));
			Boolean formerDefault = oldExportable.getDefault_();
			portable.setFormerDefault(formerDefault);
		}
		
		// Fill the list of Element
		if (isPortable) {
			List<String> filtersList = oldImportable.getFilters();
			for (String filterName : filtersList) {
				Filter filter = vars.getFilterFromFilterMap(filterName);
				if (!TransformationUtils.checkForWarning(null==filter, vars.getPortableReferencesAnInvalidElementList(),
						"importable " + portableName + " references an invalid filter: " + filterName)) {
					return null;
				}
				portable.addElement(filter);
			}
		} else {
			List<String> attributesList = oldExportable.getAttributes();
			for (String attributeName : attributesList) {
				Attribute attribute = vars.getAttributeFromAttributeMap(attributeName);
				if (!TransformationUtils.checkForWarning(null==attribute, vars.getPortableReferencesAnInvalidElementList(),
						"exportable " + portableName + " references an invalid attribute: " + attributeName)) {
					return null;
				}
				portable.addElement(attribute);
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
					"oldPortable = " + XmlUtils.displayJdomElement(oldPortable.getJdomElement()));
			MyUtils.checkStatusProgram(!oldPortable.getPointer());
		
			// Compute intersection of the main table rows only for now (latest agreement)
			List<Range> rangeList = portable.computeRangeList();
			Range range = Range.mainRangesIntersection(mainPartitionTable, false, rangeList);
		
			portable.setRange(range);
			if (help.containsAliases(oldPortable.getLinkName())) {	// We know that linkName=name (checked when populating oldObjects)
						
			} else {
				
			}
		}	
		
		return portable;	
	}
}
