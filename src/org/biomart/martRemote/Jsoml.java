package org.biomart.martRemote;

import java.util.Collection;
import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.jdom.Element;

public class Jsoml {

	public static final String XML_TEXT = "#text";
	public static final String JSON_EXCEPTION_MESSAGE = "Unhandled JSON object structure :";
	
	private Boolean xml = null;
	private String name = null;
	
	private Element xmlElement = null;
	
	private JSONObject jsonObject = null;
	private JSONArray jsonArray = null;
	
	public Jsoml(Element xmlElement) {
		this.xml = true;
		this.xmlElement = xmlElement;
		this.name = xmlElement.getName();
	}
	public Jsoml(JSONObject jsonObject) throws FunctionalException {
		this.xml = false;
		assignJsonObject(jsonObject);
	}
	public Jsoml(boolean xml, String name) {
		this.xml = xml;
		this.name = name;
		if (xml) {
			this.xmlElement = new Element(name);
		} else {
			this.jsonObject = new JSONObject();
			this.jsonArray = new JSONArray();
		}
	}
	public Jsoml(Object object) throws FunctionalException {
		this(object instanceof Element, object);
	}
	private Jsoml(boolean xml, Object object) throws FunctionalException {
		if (xml) {
			this.xmlElement = (Element)object;
			this.name = this.xmlElement.getName();
		} else {
			assignJsonObject((JSONObject)object);
		}
	}
	@SuppressWarnings("unchecked")
	private void assignJsonObject(JSONObject jsonObject) throws FunctionalException {
		this.jsonObject = jsonObject;
		Iterator<String> keys = (Iterator<String>)jsonObject.keys();		//TODO use keySet instead! much easier
		if (!keys.hasNext()) {
			throw new FunctionalException(JSON_EXCEPTION_MESSAGE + keys);
		}
		this.name = keys.next();
		if (keys.hasNext()) {
			throw new FunctionalException(JSON_EXCEPTION_MESSAGE + keys);
		}
		try {
			this.jsonArray = jsonObject.getJSONArray(this.name);
		} catch (JSONException e) {
			throw new FunctionalException("JSONObject is not a JSONArray : " + 
					jsonObject.get(this.name).getClass() + " (" + this.name + ")");
		}
	}
	

	public Object getXmlOrJson() {
		if (this.xml) {
			return getXmlElement();
		} else {
			loadArray(name);		// Must load it to account for changes (can't be added first like with JDOM element)
			return getJsonObject();
		}
	}
	
	public Element getXmlElement() {
		return this.xmlElement;
	}
	public JSONObject getJsonObject() {
		loadArray(name);		// Must load it to account for changes (can't be added first like with JDOM element)
		return this.jsonObject;
	}
	public String getName() {
		return name;
	}
	
	// JDOM like mehods
	public void setText(String text) {
		if (null!=text) {
			if (this.xml) {
				this.xmlElement.setText(text);
			} else {
				this.jsonArray.add(createJsonText(text));
			}
		}
	}
	public void setAttribute(String propertyName, Collection<String> propertyValues) {
		if (this.xml) {
			MartConfiguratorUtils.addAttribute(this.xmlElement, propertyName, propertyValues);		//TODO put in XmlUtils
		} else {
			addAttribute(this.jsonArray, propertyName, propertyValues);
		}
	}
	public void setAttribute(String propertyName, Object propertyValue) {
		if (this.xml) {
			MartConfiguratorUtils.addAttribute(this.xmlElement, propertyName, propertyValue);		//TODO put in XmlUtils
		} else {
			addAttribute(this.jsonArray, propertyName, propertyValue);
		}
	}
	private void addAttribute(JSONArray jsonArray, String propertyName, Object propertyValue) {
		if (propertyValue!=null) {
			if (!(propertyValue instanceof String) || !MyUtils.isEmpty((String)propertyValue)) {
				jsonArray.add(createJsonAttribute(propertyName, propertyValue));			
			}
		}
	}
	private void addAttribute(JSONArray jsonArray, String propertyName, Collection<String> propertyValues) {
		if (propertyValues!=null && !propertyValues.isEmpty()) {
			jsonArray.add(
					createJsonAttribute(propertyName, 
							MartConfiguratorUtils.collectionToString(propertyValues, MartConfiguratorConstants.LIST_ELEMENT_SEPARATOR)));
		}
	}
	public void removeAttribute(String propertyName) {
		if (this.xml) {
			this.xmlElement.removeAttribute(propertyName);
		} else {
			this.jsonArray.remove(propertyName);
		}
	}
	public void addContent(Jsoml jsoml) {
		if (this.xml) {
			this.xmlElement.addContent(jsoml.getXmlElement());
		} else {
			this.jsonArray.add(jsoml.getJsonObject());
		}
	}
	
	// Specific to JSON
	private JSONObject createJsonText(String text) {
		return createJsonAttribute(XML_TEXT, text);
	}
	private JSONObject createJsonAttribute(String propertyName, Object propertyValue) {
		JSONObject textJsonObject = new JSONObject();
		textJsonObject.put(propertyName, propertyValue);
		return textJsonObject;
	}
	private void loadArray(String name) {
		this.jsonObject.put(name, this.jsonArray);
	}
	
	@Override
	public String toString() {
		if (this.xml) {
			return this.xmlElement.toString();
		} else {
			loadArray(name);		// Must load it to account for changes (can't be added first like with JDOM element)
			return this.jsonObject.toString();
		}
	}
}

/*
<?xml version="1.0" ?> 
<myelement 
        myattribute1="1" 
        myattribute2="2">
        <mysubelement1 myattribute3="a" />
        <mysubelement1 myattribute3="b" />
        <mysubelement2 myattribute4="z" />
        <mysubelement1 myattribute3="c" />
        myvalue2
</myelement>

http://www.w3schools.com/xml/xml_validator.asp
http://www.xmlvalidation.com/

<?xml version="1.0" ?> 
<myelement 
        myattribute1="1" 
        myattribute2="2">
        <mysubelement1 myattribute3="a" />
	myvalue1
        <mysubelement1 myattribute3="b" />
        <mysubelement2 myattribute4="z" />
        <mysubelement1 myattribute3="c" />
        myvalue2
</myelement>
        
	{"myelement":[
                {"myattribute1":"1"},
                {"myattribute2":"2"},
                {"mysubelement1":[{"myattribute3":"a"}]},
                {"#value":"myvalue1"}
                {"mysubelement1":[{"myattribute3":"b"}]},
                {"mysubelement2":[{"myattribute4":"z"}]},
                {"mysubelement1":[{"myattribute3":"c"}]},
                {"#value":"myvalue2"}
        ]}
*/
