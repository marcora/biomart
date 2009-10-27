package org.biomart.transformation.oldXmlObjects;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.transformation.helpers.FilterOldDisplayType;
import org.biomart.transformation.helpers.FilterOldStyle;
import org.biomart.transformation.helpers.FilterOldType;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldFilter extends OldElement /*implements Comparable<OldFilter>, Comparator<OldFilter>*/ {

	public static void main(String[] args) {}
	
	public static final List<String> propertyList = new ArrayList<String>(Arrays.asList(new String[] {
			"type", "displayType", "qualifier", "legal_qualifiers", "isSelectable", "multipleValues", "defaultValue", 
			"regexp", "style", "filterList", "otherFilters", "graph", "autoCompletion", "buttonURL",
			"pointerFilter"
	}));

	protected FilterOldType type = null;
	protected FilterOldDisplayType displayType = null;
	protected FilterOldStyle style = null;
		
	protected String qualifier = null;
	protected String legal_qualifiers = null;
	protected Boolean multipleValues = null;
	protected String defaultValue = null;
	protected String regexp = null;
	protected List<String> filterList = null;
	protected List<String> otherFilters = null;
	protected String graph = null;
	protected String autoCompletion = null;
	protected String buttonURL = null;

	protected Boolean hasFilterList = null;
	
	protected OldFilter(Element jdomElement) throws FunctionalException {
		this(jdomElement, 
				jdomElement.getAttributeValue("type"),
				jdomElement.getAttributeValue("displayType"),
				jdomElement.getAttributeValue("qualifier"),
				jdomElement.getAttributeValue("legal_qualifiers"),
				jdomElement.getAttributeValue("multipleValues"),
				jdomElement.getAttributeValue("defaultValue"),
				jdomElement.getAttributeValue("regexp"),
				jdomElement.getAttributeValue("style"),
				jdomElement.getAttributeValue("filterList"),
				jdomElement.getAttributeValue("otherFilters"),
				jdomElement.getAttributeValue("graph"),
				jdomElement.getAttributeValue("autoCompletion"),
				jdomElement.getAttributeValue("buttonURL"),
				jdomElement.getAttributeValue("pointerFilter")
		);
	}
	
	private OldFilter(Element jdomElement, String type, String displayType, String qualifier, String legal_qualifiers, 
			String multipleValues, String defaultValue, String regexp, String style, String filterList, 
			String otherFilters, String graph, String autoCompletion, String buttonURL, String pointerFilter) throws FunctionalException {
		super(false, jdomElement, pointerFilter);
				
		this.type = null!=type ? FilterOldType.getFilterOldType(type) : FilterOldType.EMPTY;
		this.displayType = null!=displayType ? FilterOldDisplayType.getFilterOldDisplayType(displayType) : FilterOldDisplayType.TEXT;
		this.qualifier = qualifier;
		this.legal_qualifiers = legal_qualifiers;
		this.multipleValues = multipleValues!=null ? TransformationUtils.getBooleanValueFromString(multipleValues, "multipleValues") : null; 
		this.defaultValue = defaultValue;
		this.regexp = regexp;
		this.style = null!=style ? FilterOldStyle.getFilterOldStyle(style) : FilterOldStyle.EMPTY;
		this.filterList = new ArrayList<String>();
		if (filterList!=null) {
			String[] filterListSplit = filterList.split(MartServiceConstants.ELEMENT_SEPARATOR);
			this.filterList.addAll(Arrays.asList(filterListSplit));
		}
		this.hasFilterList = filterList!=null && !MyUtils.isEmpty(filterList);
		this.otherFilters = new ArrayList<String>();
		if (otherFilters!=null) {
			String[] otherFiltersSplit = otherFilters.split(MartServiceConstants.ELEMENT_SEPARATOR);
			this.otherFilters.addAll(Arrays.asList(otherFiltersSplit));
		}
		this.graph = graph;
		this.autoCompletion = autoCompletion;
		this.buttonURL = buttonURL;
	}

	public String getQualifier() {
		return qualifier;
	}

	public String getLegal_qualifiers() {
		return legal_qualifiers;
	}

	public Boolean getMultipleValues() {
		return multipleValues;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getRegexp() {
		return regexp;
	}

	public FilterOldStyle getStyle() {
		return style;
	}

	public List<String> getFilterList() {
		return filterList;
	}

	public List<String> getOtherFilters() {
		return otherFilters;
	}

	public String getGraph() {
		return graph;
	}

	public String getAutoCompletion() {
		return autoCompletion;
	}

	public String getButtonURL() {
		return buttonURL;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	public void setLegal_qualifiers(String legal_qualifiers) {
		this.legal_qualifiers = legal_qualifiers;
	}

	public void setMultipleValues(Boolean multipleValues) {
		this.multipleValues = multipleValues;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	public void setFilterList(List<String> filterList) {
		this.filterList = filterList;
	}

	public void setOtherFilters(List<String> otherFilters) {
		this.otherFilters = otherFilters;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

	public void setAutoCompletion(String autoCompletion) {
		this.autoCompletion = autoCompletion;
	}

	public void setButtonURL(String buttonURL) {
		this.buttonURL = buttonURL;
	}

	@Override
	public String toString() {
		return
			super.toString() + ", " +
			"type = " + type + ", " +
			"displayType = " + displayType + ", " +
			"qualifier = " + qualifier + ", " +
			"legal_qualifiers = " + legal_qualifiers + ", " +
			"multipleValues = " + multipleValues + ", " +
			"defaultValue = " + defaultValue + ", " +
			"regexp = " + regexp + ", " +
			"style = " + style + ", " +
			"filterList = " + filterList + ", " +
			"otherFilters = " + otherFilters + ", " +
			"graph = " + graph + ", " +
			"autoCompletion = " + autoCompletion + ", " +
			"buttonURL = " + buttonURL;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldFilter oldFilter=(OldFilter)object;
		return (
			(this.type==oldFilter.type || (this.type!=null && type.equals(oldFilter.type))) &&
			(this.displayType==oldFilter.displayType || (this.displayType!=null && displayType.equals(oldFilter.displayType))) &&
			(this.qualifier==oldFilter.qualifier || (this.qualifier!=null && qualifier.equals(oldFilter.qualifier))) &&
			(this.legal_qualifiers==oldFilter.legal_qualifiers || (this.legal_qualifiers!=null && legal_qualifiers.equals(oldFilter.legal_qualifiers))) &&
			(this.multipleValues==oldFilter.multipleValues || (this.multipleValues!=null && multipleValues.equals(oldFilter.multipleValues))) &&
			(this.defaultValue==oldFilter.defaultValue || (this.defaultValue!=null && defaultValue.equals(oldFilter.defaultValue))) &&
			(this.regexp==oldFilter.regexp || (this.regexp!=null && regexp.equals(oldFilter.regexp))) &&
			(this.style==oldFilter.style || (this.style!=null && style.equals(oldFilter.style))) &&
			(this.filterList==oldFilter.filterList || (this.filterList!=null && filterList.equals(oldFilter.filterList))) &&
			(this.otherFilters==oldFilter.otherFilters || (this.otherFilters!=null && otherFilters.equals(oldFilter.otherFilters))) &&
			(this.graph==oldFilter.graph || (this.graph!=null && graph.equals(oldFilter.graph))) &&
			(this.autoCompletion==oldFilter.autoCompletion || (this.autoCompletion!=null && autoCompletion.equals(oldFilter.autoCompletion))) &&
			(this.buttonURL==oldFilter.buttonURL || (this.buttonURL!=null && buttonURL.equals(oldFilter.buttonURL)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==type? 0 : type.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==displayType? 0 : displayType.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==qualifier? 0 : qualifier.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==legal_qualifiers? 0 : legal_qualifiers.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==multipleValues? 0 : multipleValues.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==defaultValue? 0 : defaultValue.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==regexp? 0 : regexp.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==style? 0 : style.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==filterList? 0 : filterList.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==otherFilters? 0 : otherFilters.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==graph? 0 : graph.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==autoCompletion? 0 : autoCompletion.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==buttonURL? 0 : buttonURL.hashCode());
		return hash;
	}

	public FilterOldDisplayType getDisplayType() {
		return displayType;
	}

	public void setDisplayType(FilterOldDisplayType displayType) {
		this.displayType = displayType;
	}

	public FilterOldType getType() {
		return type;
	}

	public void setType(FilterOldType type) {
		this.type = type;
	}

	public Boolean getHasFilterList() {
		return hasFilterList;
	}

	/*@Override
	public int compare(OldFilter oldFilter1, OldFilter oldFilter2) {
		if (oldFilter1==null && oldFilter2!=null) {
			return -1;
		} else if (oldFilter1!=null && oldFilter2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(oldFilter1.type, oldFilter2.type);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldFilter1.displayType, oldFilter2.displayType);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldFilter1.qualifier, oldFilter2.qualifier);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldFilter1.legal_qualifiers, oldFilter2.legal_qualifiers);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldFilter1.isSelectable, oldFilter2.isSelectable);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldFilter1.multipleValues, oldFilter2.multipleValues);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldFilter1.defaultValue, oldFilter2.defaultValue);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldFilter1.regexp, oldFilter2.regexp);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldFilter1.style, oldFilter2.style);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldFilter1.filterList, oldFilter2.filterList);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldFilter1.otherFilters, oldFilter2.otherFilters);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldFilter1.graph, oldFilter2.graph);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(oldFilter1.autoCompletion, oldFilter2.autoCompletion);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(oldFilter1.buttonURL, oldFilter2.buttonURL);
	}

	@Override
	public int compareTo(OldFilter oldFilter) {
		return compare(this, oldFilter);
	}*/

}