package org.biomart.configurator.jdomUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biomart.common.resources.Resources;
import org.biomart.configurator.utils.McGuiUtils;
import org.jdom.Element;

public class JDomUtils {
	
	public static List<Element> getCurrentConfigElements(List<Element> list) {
		List<Element> result = new ArrayList<Element>();
//		String userName = McGuiUtils.INSTANCE.getCurrentUser().getUserName();
		String guiName = McGuiUtils.INSTANCE.getGuiType().toString();
		for(Element e:list) {
			if( e.getAttributeValue(Resources.get("GUI")).equals(guiName)) 
				result.add(e);
		}
		return result;
	}

	public static List<JDomNodeAdapter> getCurrentConfigNode(List<JDomNodeAdapter> list) {
		List<JDomNodeAdapter> result = new ArrayList<JDomNodeAdapter>();
		String userName = McGuiUtils.INSTANCE.getCurrentUser().getUserName();
		String guiName = McGuiUtils.INSTANCE.getGuiType().toString();
		for(JDomNodeAdapter node:list) {
			if(node.getNode().getAttributeValue(Resources.get("USER")).equals(userName) && 
					node.getNode().getAttributeValue(Resources.get("GUI")).equals(guiName)) 
				result.add(node);
		}
		return result;
	}
	
	public static Element findChildElement(Element e, String name, Map<String, String> conditions) {
		boolean found = true;
		List<Element> childEList = e.getChildren(name);
		for(Element child: childEList) {
			Iterator<Entry<String, String>> i = conditions.entrySet().iterator();
			found = true;
			while(i.hasNext() && found) {
				Entry<String, String> entry = i.next();
				String attribute = entry.getKey();
				String value = entry.getValue();
				if(!child.getAttributeValue(attribute).equals(value))
					found = false;
			}
			if(found)
				return child;
		}
		return null;				
	}

	public static Element findDescendentElement(Element e, String name, Map<String, String> conditions) {
		boolean found = true;
		Iterator it = e.getDescendants();
		while(it.hasNext()) {
			Object o = it.next();
			if(o instanceof Element) {
				Element element = (Element)o;
				if(element.getName().equals(name)) {
					Iterator<Entry<String, String>> i = conditions.entrySet().iterator();
					found = true;
					while(i.hasNext() && found) {
						Entry<String, String> entry = i.next();
						String attribute = entry.getKey();
						String value = entry.getValue();
						if(!element.getAttributeValue(attribute).equals(value))
							found = false;						
					}
					if(found)
						return element;
				}
			}
		}
		return null;
	}

	public static List<Element> findDescendentElements(Element e, String name, Map<String, String> conditions) {
		boolean found = true;
		List<Element> resList = new ArrayList<Element>();
		Iterator it = e.getDescendants();
		while(it.hasNext()) {
			Object o = it.next();
			if(o instanceof Element) {
				Element element = (Element)o;
				if(element.getName().equals(name)) {
					Iterator<Entry<String, String>> i = conditions.entrySet().iterator();
					found = true;
					while(i.hasNext() && found) {
						Entry<String, String> entry = i.next();
						String attribute = entry.getKey();
						String value = entry.getValue();
						if(!value.equals(element.getAttributeValue(attribute)))
							found = false;						
					}
					if(found)
						resList.add(element);
				}
			}
		}
		return resList;
	}

	public static List<Element> findChildElements(Element e, String name, Map<String, String> conditions) {
		boolean found = true;
		List<Element> result = new ArrayList<Element>();
		List<Element> childEList = e.getChildren(name);
		for(Element child: childEList) {
			Iterator<Entry<String, String>> i = conditions.entrySet().iterator();
			found = true;
			while(i.hasNext() && found) {
				Entry<String, String> entry = i.next();
				String attribute = entry.getKey();
				String value = entry.getValue();
				if(!child.getAttributeValue(attribute).equals(value))
					found = false;
			}
			if(found)
				result.add(child);
		}
		return result;				
	}
	
    /**
     * if value == null, return when the name matches
     */
    @SuppressWarnings("unchecked") //legacy code from jdom
	public static Element searchElement(Element startElement, String name, String value) {
    	Iterator it = startElement.getDescendants();
    	while(it.hasNext()) {
    		Object o = it.next();
    		if(o instanceof Element) {
    			Element e = (Element) o;
    			if(e.getName().equals(name)) {
    				if(value==null || e.getAttributeValue(Resources.get("NAME")).equals(value))   			
    					return e;
    			}
    		}else
    			continue;
    	}
    	return null;    	
    }

    @SuppressWarnings("unchecked") //legacy code from jdom
	public static Element searchElementInUser(Element startElement, String userName, String name, String value) {
    	Iterator it = startElement.getDescendants();
    	while(it.hasNext()) {
    		Object o = it.next();
    		if(o instanceof Element) {
    			Element e = (Element) o;
    			if(e.getName().equals(name)) {
    				String userStr = e.getAttributeValue(Resources.get("USER"));
    				if(JDomUtils.isUserMatched(userName, userStr))
    					if(value==null || e.getAttributeValue(Resources.get("NAME")).equals(value))   			
    						return e;
    			}
    		}else
    			continue;
    	}
    	return null;    	
    }

    /**
     * special case of findDescendentElement, since most of the time, there is only one condition
     * @param startElement
     * @param name
     * @param attName
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked") //legacy code from jdom
	public static Element searchElementWithCondition(Element startElement, String name, String attName, String value) {
    	Iterator it = startElement.getDescendants();
    	while(it.hasNext()) {
    		Object o = it.next();
    		if(o instanceof Element) {
    			Element e = (Element) o;
    			if(e.getName().equals(name)) {
    				if(value==null) 			
    					return e;
    				else {
	    				String att = e.getAttributeValue(attName);
	    				if(att!=null && att.equals(value))
	    					return e;
    				}
    			}
    		}else
    			continue;
    	}
    	return null;    	
    }
    
    public static List<JDomNodeAdapter> searchElementS(Element startElement, String name, String value) {
        List<JDomNodeAdapter> resultList = new ArrayList<JDomNodeAdapter>();  	
    	Iterator it = startElement.getDescendants();
    	while(it.hasNext()) {
    		Object o = it.next();
    		if(o instanceof Element) {
    			Element e = (Element) o;
    			if(e.getName().equals(name)) {
    				if(value==null||e.getAttributeValue(Resources.get("NAME")).equals(value))
    					resultList.add(new JDomNodeAdapter(e));
    			}
    		}else
    			continue;
    	}
    	return resultList;
    }
    
    public static List<JDomNodeAdapter> searchElementSInUser(Element startElement, String name, String value) {
        List<JDomNodeAdapter> resultList = new ArrayList<JDomNodeAdapter>();  	
    	Iterator it = startElement.getDescendants();
    	while(it.hasNext()) {
    		Object o = it.next();
    		if(o instanceof Element) {
    			Element e = (Element) o;
    			if(e.getName().equals(name)) {
    				String user = JDomUtils.getUserForElement(e);
    				if(user == null || user.equals(""))
    					continue;
    				if(JDomUtils.isUserMatched(McGuiUtils.INSTANCE.getCurrentUser().getUserName(), user))
    					if(value==null||e.getAttributeValue(Resources.get("NAME")).equals(value))
    						resultList.add(new JDomNodeAdapter(e));
    			}
    		}else
    			continue;
    	}
    	return resultList;
    }

    public static List<JDomNodeAdapter> searchElementSWithCondition(Element startElement, String name, String attName,
    		String value) {
        List<JDomNodeAdapter> resultList = new ArrayList<JDomNodeAdapter>();  	
    	Iterator it = startElement.getDescendants();
    	while(it.hasNext()) {
    		Object o = it.next();
    		if(o instanceof Element) {
    			Element e = (Element) o;
    			if(e.getName().equals(name)) {
    				if(value==null)
    					resultList.add(new JDomNodeAdapter(e));
    				else {
    					String att = e.getAttributeValue(attName);
    					if(att!=null && att.equals(value))
    					resultList.add(new JDomNodeAdapter(e));
    				}
    			}
    		}else
    			continue;
    	}
    	return resultList;
    }


    public static List<Element> searchElementList(Element startElement, String name, String value) {
        List<Element> resultList = new ArrayList<Element>();  	
    	Iterator it = startElement.getDescendants();
    	while(it.hasNext()) {
    		Object o = it.next();
    		if(o instanceof Element) {
    			Element e = (Element) o;
    			if(e.getName().equals(name)) {
    				if(value==null||e.getAttributeValue(Resources.get("NAME")).equals(value))
    					resultList.add(e);
    			}
    		}else
    			continue;
    	}
    	return resultList;
    }

    public static List<Element> searchElementListInUser(Element startElement, String name, String value, String userName) {
        List<Element> resultList = new ArrayList<Element>();  	
    	Iterator it = startElement.getDescendants();
    	while(it.hasNext()) {
    		Object o = it.next();
    		if(o instanceof Element) {
    			Element e = (Element) o;
    			if(e.getName().equals(name)) {
    				String user = JDomUtils.getUserForElement(e);
    				if(user == null || user.equals(""))
    					continue;
    				if(JDomUtils.isUserMatched(userName, user))
    					if(value==null||e.getAttributeValue(Resources.get("NAME")).equals(value))
    						resultList.add(e);
    			}
    		}else
    			continue;
    	}
    	return resultList;
    }

    public static List<Element> findLinkedDataSets(JDomNodeAdapter currentDS, boolean all) {
    	JDomNodeAdapter root = (JDomNodeAdapter)currentDS.getRoot();
    	List<Element> dsList = new ArrayList<Element>();
    	String linkedDsStr = currentDS.getNode().getAttributeValue(Resources.get("LINKEDDS"));
    	if(all && (null==linkedDsStr || linkedDsStr.equals("")))
    		return dsList;

		List<Element>impList = JDomUtils.searchElementList(currentDS.getNode(), Resources.get("IMPORTABLE"), null);
		List<Element>proList = JDomUtils.searchElementList(currentDS.getNode(), Resources.get("PROCESSOR"), null);
		List<Element>expList = JDomUtils.searchElementList(currentDS.getNode(), Resources.get("EXPORTABLE"), null);		
		List<Element> currentImpNodes = JDomUtils.getCurrentConfigElements(impList);
		List<Element> currentProNodes = JDomUtils.getCurrentConfigElements(proList);
		List<Element> currentExpNodes = JDomUtils.getCurrentConfigElements(expList);
		currentImpNodes.addAll(currentProNodes);
		
		List<Element> allDsList = JDomUtils.searchElementList(root.getNode(), Resources.get("DATASET"),null);
		
		for(Element datasetE: allDsList) {
			//don't show itself again
			if(datasetE.equals(currentDS.getNode()))
				continue;
			//check if the dataset is selected
			String locStr = datasetE.getParentElement().getParentElement().getAttributeValue(Resources.get("NAME"));
			String martStr = datasetE.getParentElement().getAttributeValue(Resources.get("NAME"));
			String dataStr= datasetE.getAttributeValue(Resources.get("NAME"));
			String str = locStr+"->"+martStr+"->"+dataStr;
			if(all && linkedDsStr.indexOf(str)<0)
				continue;
			
			List<Element>linkedImpNodes = JDomUtils.searchElementList(datasetE, Resources.get("IMPORTABLE"), null);
			List<Element>linkedExpNodes = JDomUtils.searchElementList(datasetE, Resources.get("EXPORTABLE"), null);
			List<Element>linkedProNodes = JDomUtils.searchElementList(datasetE, Resources.get("PROCESSOR"), null);
			List<Element> currentLinkedImps = JDomUtils.getCurrentConfigElements(linkedImpNodes);
			List<Element> currentLinkedExps = JDomUtils.getCurrentConfigElements(linkedExpNodes);
			List<Element> currentLinkedPros = JDomUtils.getCurrentConfigElements(linkedProNodes);
			currentLinkedImps.addAll(currentLinkedPros);
			
			boolean linked = false;
			
			for(Element imp: currentLinkedImps) {
				for(Element currentExp: currentExpNodes) {
					if(imp.getAttributeValue(Resources.get("NAME")).equalsIgnoreCase(
							currentExp.getAttributeValue(Resources.get("NAME")))) {
						linked = true;
						break;
					}
				}
			}
			
			if(linked) {
				dsList.add(datasetE);
			} else {			
				for(Element exp: currentLinkedExps) {
					for (Element currentImp: currentImpNodes) {
						if(exp.getAttributeValue(Resources.get("NAME")).equalsIgnoreCase(
								currentImp.getAttributeValue(Resources.get("NAME")))) {
							linked = true;
							break;
						}
					}
				}
				if(linked) {
					dsList.add(datasetE);					
				}
			}
		}
    	return dsList;
    }
    
    public static void updatePartitionTable(Element ptElement, int row, int col, String value) {
    	List<Element> cellList = ptElement.getChildren();
    	String rowStr = ""+(row+1);
    	String colStr = ""+(col+1);
    	for(Element cell:cellList) {
    		if(cell.getAttributeValue("col").equals(colStr) && 
    				cell.getAttributeValue("row").equals(rowStr)) {
    			cell.setAttribute("value",value);
    			break;
    		}
    	}
    }
    
    /**
     * Always gives a list that starts with a string (empty or not) and
	 *	then alternates partition reference / string (empty or not)
     * @param pattern
     * @param value
     * @return
     */
    public static List<String> extractPartitionReferences(String value, boolean ptOnly) {
        List<String> list = new ArrayList<String>();
        if (null!=value) {
        	String pat = "\\(\\w*\\)";
            Pattern pattern = Pattern.compile(pat);
            Matcher m = pattern.matcher(value);
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                list.add(value.substring(0, start));
                list.add(value.substring(start, end));
                value = value.substring(end);
                m = pattern.matcher(value);
            }
            list.add(value);
        } 
        
        if(ptOnly) {
        	List<String> ptList = new ArrayList<String>();
        	if(list.size()>=2) {	        	
	        	for(int i=1; i<list.size(); i=i+2) {
	        		//remove the ()
	        		String tmp = list.get(i);
	        		ptList.add(tmp.substring(1, tmp.length()-1));       		
	        	}
        	}
        	return ptList;
        } else
        	return list;
    }

	@SuppressWarnings("unchecked")
	public static List<ArrayList<String>> ptElement2Table(Element partitionTable) {
		List<Element> cells = partitionTable.getChildren();
		String rowString = partitionTable.getAttributeValue("rows");
		String colString = partitionTable.getAttributeValue("cols");
		int rows = 0, cols = 0;
		try {
			rows = Integer.parseInt(rowString);
			cols = Integer.parseInt(colString);
		}catch(Exception e) {
			System.err.println(e.getStackTrace());
			return null;
		}
		String[][] dataArray = new String[rows][cols];
		List<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		for(Element cell:cells) {
			int currentRow = 0;
			int currentCol = 0;
			String curRowS = cell.getAttributeValue("row");
			String curColS = cell.getAttributeValue("col");
			try{
				currentRow = Integer.parseInt(curRowS);
				currentCol = Integer.parseInt(curColS);
			}catch(Exception e) {
				System.err.println(e.getStackTrace());
				return null;				
			}
			dataArray[currentRow-1][currentCol-1]=cell.getAttributeValue("value");
		}
		for(int i=0; i<rows; i++) {
			ArrayList<String> rowList = new ArrayList<String>(Arrays.asList(dataArray[i]));
			data.add(rowList);
		}
		return data;
	}

	public static boolean hasPartitionReference(String value) {
		if(value.indexOf("(")>=0)
			return true;
		else 
			return false;
	}

	/**
	 * for elements under location
	 * @return
	 */
	public static String getUserForElement(Element e) {
		if(e.getName().equals(Resources.get("LOCATION")))
			return e.getAttributeValue(Resources.get("USER"));
		while(e.getParentElement()!=null) {
			e = e.getParentElement();
			if(e.getName().equals(Resources.get("LOCATION"))) {
				String user = e.getAttributeValue(Resources.get("USER"));
				return user;
			}
		}
		return null;
	}
	
	public static boolean isUserMatched(String user, String userList) {
		if(null==userList || userList.equals(""))
			return true;
		String[] list = userList.split(Resources.get("colonseparator"));
		if(Arrays.asList(list).contains(user)) 
			return true;
		else
			return false;
	}

	public static boolean isElementHiden(Element e) {
		String hideStr = e.getAttributeValue(Resources.get("HIDE"));
		if(hideStr==null)
			return false;
		else if(hideStr.equals("1"))
			return true;
		else {
			String currentUser = McGuiUtils.INSTANCE.getCurrentUser().getUserName();
			String[] userArr = hideStr.split(",");
			if(Arrays.asList(userArr).contains(currentUser))
				return true;
			else
				return false;
		}
	}
}