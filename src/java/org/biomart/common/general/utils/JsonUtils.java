package org.biomart.common.general.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.biomart.common.general.exceptions.TechnicalException;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

public class JsonUtils {
	
	private static final String XML_TO_JASON_VALUE_TAG = "#value";
	private static final String XML_TO_JASON_ATTRIBUTE_TAG = "$";
	private static final String XML_TO_JASON_ARRAY_TAG = "@";
	
	private static final String TAB = "   ";

	public static void main(String[] args) throws Exception {
		Element root = new org.jdom.Element("myRootElement");
		Document newDoc = new Document(root);
		Element element = new Element("my_child");
		element.setAttribute("test", "ok");
		Element element21 = new Element("my_child2");
		element21.setAttribute("test21", "ok21");
		Element element22 = new Element("my_child2");
		element21.setAttribute("test22", "ok22");
		element.addContent(element21);
		element.addContent(element22);
		
		Element element2 = new Element("my_child");
		
		root.setContent(new ArrayList<Element>(Arrays.asList(new Element[] {element, element2})));
		System.out.println(getJSONObjectNiceString(getJSONObjectFromDocument(newDoc)));
		System.out.println(getJSONObjectFromDocument(newDoc));
		System.out.println(XmlUtils.getXmlDocumentString(newDoc));
	}
	
	// Conversion
	public static JSONObject getJSONObjectFromDocument(Document document) throws TechnicalException {
		JSONObject jsonObject = new JSONObject();
		try {
			Element root = document.getRootElement();
			jsonObject.put("$" + root.getName(), convertElement(root));
		} catch (JSONException e) {
			throw new TechnicalException(e);
		}
		return jsonObject;
	}
	@SuppressWarnings("unchecked")
	private static JSONObject convertElement(Element element) throws JSONException {
		JSONObject jSONObject = new JSONObject();
	
		String value = element.getValue();
		if (null!=value && value.length()>0) {
			jSONObject.put(XML_TO_JASON_VALUE_TAG, value);
		}
		
		List<Attribute> attributes = (List<Attribute>)element.getAttributes();
		for (Attribute attribute : attributes) {
			jSONObject.put(attribute.getName(), attribute.getValue());
		}
		
		List<Element> children = (List<Element>)element.getChildren();
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		//String previousChildName = null;
		for (Element child : children) {
			String childName = child.getName();
			Boolean b = map.get(childName);
			map.put(childName, null!=b);
			//previousChildName = childName;
		}
		
		HashMap<String, JSON> jsonMap = new HashMap<String, JSON>();
		for (Element child : children) {
			String childName = child.getName();
			Boolean b = map.get(childName);
			if (b) {
				JSON json = jsonMap.get(childName);
				JSONArray jSONArrayChild = null;
				if (null==json) {
					jSONArrayChild = new JSONArray();
				} else {					
					jSONArrayChild = (JSONArray)json;
				}
				jSONArrayChild.add(convertElement(child));
				jsonMap.put(childName, jSONArrayChild);
			} else {
				jsonMap.put(childName, convertElement(child));
			}
		}
		
		for (Iterator<String> it = jsonMap.keySet().iterator(); it.hasNext();) {
			String childName = it.next();
			JSON json = jsonMap.get(childName);
			String prefix = json instanceof JSONObject ? XML_TO_JASON_ATTRIBUTE_TAG : XML_TO_JASON_ARRAY_TAG;
			jSONObject.put(prefix + childName, json);
		}
		
		return jSONObject;
	}
	
	// Display
	public static String getJSONObjectNiceString(JSONObject jSONObject) throws TechnicalException {
		return getJSONObjectNiceString(jSONObject, 0, false, false);
	}
	@SuppressWarnings("unchecked")
	private static String getJSONObjectNiceString(JSONObject jSONObject, int depth, boolean arrayItem, boolean debug) throws TechnicalException {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(computeTabs(depth) + "{");
		Integer depthLevel = null;
		try {
			int size = jSONObject.size();
			int index = 0;
			for (Iterator<String> it = (Iterator<String>)jSONObject.keys(); it.hasNext();) {
				String key = it.next();
				Object value = jSONObject.get(key);
				
				if (value instanceof JSONObject || value instanceof JSONArray) {
					stringBuffer.append(MyUtils.LINE_SEPARATOR + computeTabs(depth+1) + computeKey(key));
					if (value instanceof JSONObject) {
						depthLevel = depth+2;
						stringBuffer.append(MyUtils.LINE_SEPARATOR + getJSONObjectNiceString((JSONObject)value, depthLevel, false, debug));
					} else if (value instanceof JSONArray) {
						JSONArray jSONArray = (JSONArray)value;
						int size2 = jSONArray.size();
						int index2 = 0;
						stringBuffer.append(MyUtils.LINE_SEPARATOR + computeTabs(depth+2) + "[");
						for (Iterator<Object> it2 = (Iterator<Object>)jSONArray.listIterator(); it2.hasNext();) {
							
							Object value2 = it2.next();
							if (value2 instanceof JSONObject) {
								depthLevel = depth+3;
								JSONObject jsonObject2 = (JSONObject)value2;
								
								// To save space
								boolean simpleObject = false;
								String onlyKey = null;
								Object onlyValue = null;
								Set<String> keySet = (Set<String>)jsonObject2.keySet();
								if (keySet.size()==1) {
									onlyKey = keySet.iterator().next();
									onlyValue = jsonObject2.get(onlyKey);
									simpleObject = !(onlyValue instanceof JSONObject || onlyValue instanceof JSONArray);
								}
								
								// Treat a simple objects differently (no new lines)
								if (index2==0 || !simpleObject) {
									stringBuffer.append(MyUtils.LINE_SEPARATOR);
								}
								
								if (simpleObject) {
									if (index2==0) {
										stringBuffer.append(computeTabs(depthLevel));
									}
									stringBuffer.append("{" + computeKey(onlyKey) + ":" + computeValue(onlyValue) + "}");
														// both onlyKey and onlyValue are not null if found to be a simple object
								} else {
									stringBuffer.append(getJSONObjectNiceString(jsonObject2, depthLevel, true, debug));
								}
								stringBuffer.append(index2==size2-1 ? "" : ",");
							} else if (value2 instanceof JSONArray) {
								throw new TechnicalException("Unhandled"); // for now, to be written if it becomes neccessary
							} else if (value2 instanceof String) {
								throw new TechnicalException("Unhandled"); // for now, to be written if it becomes neccessary
								/*MyUtils.checkStatusProgram(value2 instanceof String);
								stringBuffer.append(
										MyUtils.LINE_SEPARATOR + "#################" + value2 + (index2==size2-1 ? "" : ","));*/
							} else {
								throw new TechnicalException("Unhandled"); // TODO better
							}
							index2++;
						}
						stringBuffer.append(MyUtils.LINE_SEPARATOR + computeTabs(depth+2) + "]");
					}
				} else {
					depthLevel = depth+1;
					stringBuffer.append((index==0 ? MyUtils.LINE_SEPARATOR + computeTabs(depthLevel) : " ") + computeKey(key) + ":" + computeValue(value));
				}
				stringBuffer.append((index==size-1 ? "" : ","));
				index++;
			}
		} catch (JSONException e) {
			throw new TechnicalException(e);
		}
		stringBuffer.append(MyUtils.LINE_SEPARATOR + computeTabs(depth) + "}");
		return stringBuffer.toString();
	}
	private static String computeKey(String key) {
		return "\"" + key + "\"";
	}
	private static String computeValue(Object value) {
		return "\"" + value + "\"";
	}
	private static StringBuffer computeTabs(int depth) {
		return computeTabs(depth, TAB);
	}
	private static StringBuffer computeTabs(int depth, String tab) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < depth; i++) {
			stringBuffer.append("   ");
		}
		return stringBuffer;
	}
}
