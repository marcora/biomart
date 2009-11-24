package org.biomart.transformation.oldXmlObjects;


import java.util.Comparator;


import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.CompareUtils;
import org.biomart.common.general.utils.XmlUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.transformation.helpers.TransformationUtils;
import org.jdom.Element;


public class OldNode extends Object implements Comparable<OldNode>, Comparator<OldNode> {

	public static void main(String[] args) {}

	protected Boolean valid = true;
	protected Element jdomElement = null;
	protected String text = null;
	protected Boolean hidden = null;

	public boolean isValid() {
		return valid;
	}
	
	/**
	 * For nodes with just a value but no properties (such as <MainTable> or <Key>)
	 * @param jdomDatasetConfig
	 * @throws FunctionalException
	 */
	public OldNode(Element jdomDatasetConfig, boolean basic) throws FunctionalException {
		this(jdomDatasetConfig);
		if (jdomDatasetConfig.getAttributes().size()>0 || jdomDatasetConfig.getChildren().size()>0) {
			throw new FunctionalException("Not a basic XML node, " +
					"jdomDatasetConfig = " + XmlUtils.displayJdomElement(jdomDatasetConfig));
		}
	}
	
	/**
	 * For nodes with just a value but no properties (such as <MainTable> or <Key>)
	 * @param jdomDatasetConfig
	 * @throws FunctionalException
	 */
	protected OldNode(Element jdomDatasetConfig) throws FunctionalException {
		this(jdomDatasetConfig, 
				jdomDatasetConfig.getValue(), 
				jdomDatasetConfig.getAttributeValue("hidden")
		);
	}
	private OldNode(Element jdomElement, String value, String hidden) throws FunctionalException {
		super();
		this.jdomElement = jdomElement;
		this.text = value;
		this.hidden = TransformationUtils.getBooleanValueFromString(hidden, "hidden");
	}

	public Boolean getHidden() {
		return hidden;
	}

	public boolean isNotHidden() {
		return !hidden;
	}

	public Element getJdomElement() {
		return jdomElement;
	}

	public String getText() {
		return text;
	}

	public void setJdomElement(Element jdomElement) {
		this.jdomElement = jdomElement;
	}

	public void setValue(String value) {
		this.text = value;
	}

	@Override
	public String toString() {
		return 
			"jdomElement = " + jdomElement + ", " +
			"text = " + text + ", " +
			"hidden = " + hidden;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		OldNode oldNode=(OldNode)object;
		return (
			//(this.jdomElement==oldNode.jdomElement || (this.jdomElement!=null && jdomElement.equals(oldNode.jdomElement))) &&
			(this.jdomElement==oldNode.jdomElement || (this.jdomElement!=null && oldNode.jdomElement!=null && jdomElement.getName().equals(oldNode.jdomElement.getName()))) &&
			(this.text==oldNode.text || (this.text!=null && text.equals(oldNode.text)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==jdomElement? 0 : jdomElement.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==text? 0 : text.hashCode());
		return hash;
	}

	public int compare(OldNode oldNode1, OldNode oldNode2) {
		if (oldNode1==null && oldNode2!=null) {
			return -1;
		} else if (oldNode1!=null && oldNode2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(oldNode1.jdomElement, oldNode2.jdomElement);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(oldNode1.text, oldNode2.text);
	}

	public int compareTo(OldNode oldNode) {
		return compare(this, oldNode);
	}

}