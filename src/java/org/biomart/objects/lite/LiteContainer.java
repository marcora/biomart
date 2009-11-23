package org.biomart.objects.lite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.GroupFilter;
import org.biomart.objects.objects.MartConfiguratorObject;
import org.biomart.objects.objects.Part;
import org.biomart.objects.objects.Range;
import org.biomart.objects.objects.SimpleFilter;

public class LiteContainer extends LiteMartConfiguratorObject implements Serializable {

	private static final long serialVersionUID = 5317562972868527144L;

	private static final String XML_ELEMENT_NAME = "container";

	private Integer queryRestriction = null;

	private List<LiteMartConfiguratorObject> liteContaineeList = null;	// Ordered references to above lists of containers, 
																				// filters & attributes

	private List<LiteContainer> liteContainerList = null;
	private List<LiteFilter> liteFilterList = null;
	private List<LiteAttribute> liteAttributeList = null;

	public LiteContainer(Container container, List<Integer> mainRowNumbersWanted) throws FunctionalException {
		this(null, container, mainRowNumbersWanted);
	}
	public LiteContainer(MartRemoteRequest martRemoteRequest,
			Container container, List<Integer> mainRowNumbersWanted) throws FunctionalException {

		super(XML_ELEMENT_NAME, container.getName(),
				container.getDisplayName(), container.getDescription(),
				container.getVisible());

		this.queryRestriction = container.getQueryRestriction();

		this.liteContaineeList = new ArrayList<LiteMartConfiguratorObject>();

		this.liteContainerList = new ArrayList<LiteContainer>();
		this.liteFilterList = new ArrayList<LiteFilter>();
		this.liteAttributeList = new ArrayList<LiteAttribute>();
		populateContent(container.getContaineeList(), mainRowNumbersWanted);
	}

	private void populateContent(List<MartConfiguratorObject> containeeList, List<Integer> mainRowNumbersWanted) throws FunctionalException {
		for (MartConfiguratorObject containee : containeeList) {
			if (containee instanceof Container) {
				if (containee.getVisible()) { // Only the visible ones
					LiteContainer liteContainer = new LiteContainer((Container)containee, mainRowNumbersWanted);
					addLiteContainer(liteContainer); // also handles containeeList
				}
			} else if (containee instanceof org.biomart.objects.objects.Element) {

				org.biomart.objects.objects.Element element = (org.biomart.objects.objects.Element) containee;
				if (element instanceof SimpleFilter
						&& ((SimpleFilter) element).getPartition()) {
					LiteFilter liteSimpleFilterPartition = new LiteFilter((SimpleFilter) element);
					addLiteFilter(liteSimpleFilterPartition);
				} else {
					Range targetRange = element.getTargetRange();
					Set<Part> partSet = targetRange.getPartSet();
					for (Part part : partSet) {
						if (part.getVisible()) { // Only the visible ones
							if (element.getPointer() && null == element.getPointedElement())
								continue; // broken pointers (from transformation for instance)
							int mainRowNumber = part.getMainRowNumber(); // could be taken from part too
							if (mainRowNumbersWanted.contains(mainRowNumber)) {
								if (element.getPointer()) { // FIXME not
															// adequate if
															// pointers of
															// pointers... ->
															// but will be
															// solved by light
															// objects anyway
									org.biomart.objects.objects.Element pointedElement = element.getPointedElement();
									if (pointedElement instanceof Attribute) {
										LiteAttribute pointedLiteAttribute = new LiteAttribute((Attribute) pointedElement, part);
										pointedLiteAttribute.updatePointerClone(element);
										if (!this.liteAttributeList.contains(pointedLiteAttribute)) { // No repetitions
											addLiteAttribute(pointedLiteAttribute); // also handles containeeList
										}
									} else if (pointedElement instanceof Filter) {
										LiteFilter pointedLiteFilter = null;
										if (pointedElement instanceof SimpleFilter) {
											LiteFilter pointedLiteSimpleFilter = 
												new LiteFilter((SimpleFilter) pointedElement, part);
											pointedLiteSimpleFilter.updatePointerClone(element);
											pointedLiteFilter = pointedLiteSimpleFilter;
										} else if (pointedElement instanceof GroupFilter) {
											LiteFilter pointedLiteGroupFilter = 
												new LiteFilter((GroupFilter) pointedElement, part);
											pointedLiteGroupFilter.updatePointerClone(element);
											pointedLiteFilter = pointedLiteGroupFilter;
										}
										if (!this.liteFilterList.contains(pointedLiteFilter)) { // No repetitions
											addLiteFilter(pointedLiteFilter); // also handles containeeList
										}
									}
								} else {
									if (element instanceof Attribute) {
										LiteAttribute liteAttribute = new LiteAttribute((Attribute) element, part);
										if (!this.liteAttributeList.contains(liteAttribute)) { // No repetitions
											addLiteAttribute(liteAttribute); // also handles containeeList
										}
									} else if (element instanceof Filter) {
										LiteFilter liteFilter = null;
										if (element instanceof SimpleFilter) {
											liteFilter = new LiteFilter((SimpleFilter) element, part);
										} else if (element instanceof GroupFilter) {
											liteFilter = new LiteFilter((GroupFilter) element, part);
										}
										if (!this.liteFilterList.contains(liteFilter)) { // No repetitions
											addLiteFilter(liteFilter); // also handles containeeList
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public List<LiteMartConfiguratorObject> getLiteContaineeList() {
		return new ArrayList<LiteMartConfiguratorObject>(liteContaineeList);
	}
	public List<LiteContainer> getLiteContainerList() {
		return new ArrayList<LiteContainer>(liteContainerList);
	}
	public List<LiteFilter> getLiteFilterList() {
		return new ArrayList<LiteFilter>(liteFilterList);
	}
	public List<LiteAttribute> getLiteAttributeList() {
		return new ArrayList<LiteAttribute>(liteAttributeList);
	}

	private void addLiteContainer(LiteContainer liteContainer) {
		this.liteContainerList.add(liteContainer);
		this.liteContaineeList.add(liteContainer);
	}

	private void addLiteFilter(LiteFilter liteFilter) {
		this.liteFilterList.add(liteFilter);
		this.liteContaineeList.add(liteFilter);
	}

	private void addLiteAttribute(LiteAttribute liteAttribute) {
		this.liteAttributeList.add(liteAttribute);
		this.liteContaineeList.add(liteAttribute);
	}
	
	// Properties in super class available for this light object
	public String getDisplayName() {
		return super.displayName;
	}
	public String getDescription() {
		return super.description;
	}
	public Boolean getVisible() {
		return super.visible;
	}

	public Integer getQueryRestriction() {
		return queryRestriction;
	}

	@Override
	public String toString() {
		StringBuffer liteContaineeListSb = new StringBuffer();
		for (int i = 0; i < liteContaineeList.size(); i++) {
			liteContaineeListSb.append((i==0 ? "" : ", ") + liteContaineeList.get(i).getName());
		}
		return super.toString() + ", " + 
			"queryRestriction = " + queryRestriction + ", " + 
			"liteContaineeList = " + liteContaineeListSb.toString();
	}

	@Override
	protected Jsoml generateExchangeFormat(boolean xml)
			throws FunctionalException {
		Jsoml jsoml = new Jsoml(xml, super.xmlElementName);

		jsoml.setAttribute("name", super.name);
		jsoml.setAttribute("displayName", super.displayName);
		jsoml.setAttribute("description", super.description);

		jsoml.setAttribute("queryRestriction", this.queryRestriction);

		for (LiteMartConfiguratorObject liteContainee : this.liteContaineeList) {
			if (liteContainee instanceof LiteContainer) {
				Jsoml containeeJsoml = liteContainee.generateExchangeFormat(xml);
				jsoml.addContent(containeeJsoml);
			} else if (liteContainee instanceof LiteAttribute) {
				LiteAttribute liteAttribute = (LiteAttribute) liteContainee;
				Jsoml elementJsonml = liteAttribute.generateExchangeFormat(xml);
				jsoml.addContent(elementJsonml);
			} else if (liteContainee instanceof LiteFilter) {
				LiteFilter liteFilter = (LiteFilter) liteContainee;
				Jsoml elementJsonml = liteFilter.generateExchangeFormat(xml);
				jsoml.addContent(elementJsonml);
			}
		}

		return jsoml;
	}
}
