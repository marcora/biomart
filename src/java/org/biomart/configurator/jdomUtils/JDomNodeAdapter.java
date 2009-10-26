

package org.biomart.configurator.jdomUtils;

import general.exceptions.FunctionalException;
import general.exceptions.TechnicalException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import java.util.HashMap;

import java.util.Iterator;

import java.util.List;

import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import javax.swing.tree.TreeNode;
import martConfigurator.transformation.TransformationMain;
import martConfigurator.transformation.helpers.MartServiceIdentifier;
import org.biomart.builder.model.Column;
import org.biomart.builder.model.DataSetColumn;
import org.biomart.builder.model.DataSetTable;
import org.biomart.builder.model.InheritedColumn;
import org.biomart.builder.model.WrappedColumn;
import org.biomart.builder.model.DataSet;

import org.biomart.builder.model.Mart;
import org.biomart.builder.model.Relation;

import org.biomart.builder.model.Schema;

import org.biomart.builder.model.Table;
import org.biomart.builder.model.Key.ForeignKey;

import org.biomart.builder.model.Key.PrimaryKey;
import org.biomart.builder.model.JDBCSchema;

import org.biomart.common.resources.*;

import org.biomart.common.view.gui.SwingWorker;
import org.biomart.common.view.gui.dialogs.ProgressDialog2;
import org.biomart.common.view.gui.dialogs.StackTrace;

import org.biomart.configurator.model.Location;

import org.biomart.configurator.model.object.Processor;

import org.biomart.configurator.utils.DsConnectionObject;
import org.biomart.configurator.utils.TransformationYongPrototype;

import org.biomart.configurator.utils.McEventObject;


import org.biomart.configurator.utils.McGuiUtils;

import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.utils.type.DataSetTableType;
import org.biomart.configurator.utils.type.EventType;
import org.biomart.configurator.utils.type.IdwViewType;
import org.biomart.configurator.utils.type.McGuiType;

import org.biomart.configurator.view.idwViews.McViewSchema;

import org.biomart.configurator.view.idwViews.McViews;

import org.biomart.configurator.view.gui.dialogs.AddLinkedDataSetsDialog;

import org.biomart.configurator.view.gui.dialogs.LocationConnectionDialog;

import org.jdom.Document;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;





/**

 * Wraps an XML element. Will return find children of this element if any.

 * 

 * @see http://java.sun.com/webservices/jaxp/dist/1.1/docs/tutorial/index.html

 */

public class JDomNodeAdapter extends DefaultMutableTreeNode {

    
	private static final long serialVersionUID = 1L;
	/** the Element encapsulated by this node */

    private Element node;

    

    /** used for toString() */

    private final static String tab = "  ";

    private final static String lf = "\n";



    /**

     * Creates a new instance of the JDOMAdapterNode class

     * @param Element node

     */

    public JDomNodeAdapter(Element node) {

    	super(node);

        this.node = node;

    }

    

    public JDomNodeAdapter(Object userObject) {

    	super(userObject);

    	if(userObject instanceof Element)

    		node = (Element)userObject;

    }

    

    public Element getNode() {

    	return node;

    }

    

    public List<JDomNodeAdapter> addSourceSchemas(Mart mart, Element parent) {
    	Element currentElement;
    	if(parent==null)
    		currentElement = this.node;
    	else
    		currentElement = parent;


    	List<JDomNodeAdapter> newNodes = new ArrayList<JDomNodeAdapter>();
		for (final Iterator<Schema> i = mart.getSchemas().values().iterator(); i.hasNext();) {
			final Schema schema = i.next();
			if (schema instanceof JDBCSchema) {
				Log.debug("Writing JDBC schema");
				// It's a JDBC schema.
				final JDBCSchema jdbcSchema = (JDBCSchema) schema;
				final Element schemaElement = new Element(Resources.get("SOURCESCHEMA"));
				currentElement.addContent(schemaElement);

				schemaElement.setAttribute("name",jdbcSchema.getName());
				schemaElement.setAttribute("uniqueId",""+jdbcSchema.getUniqueId());
				schemaElement.setAttribute("driverClassName",jdbcSchema.getConnectionObject().getDriverClassString());
				schemaElement.setAttribute("url",jdbcSchema.getConnectionObject().getJdbcUrl());
				schemaElement.setAttribute("databaseName",jdbcSchema.getDataLinkDatabase());
				schemaElement.setAttribute("schemaName",jdbcSchema.getDataLinkSchema());
				schemaElement.setAttribute("username",jdbcSchema.getConnectionObject().getUserName());
				schemaElement.setAttribute("keyguessing",""+jdbcSchema.isKeyGuessing());
				schemaElement.setAttribute("masked",""+jdbcSchema.isMasked());
				schemaElement.setAttribute("hideMasked",""+jdbcSchema.isHideMasked());
				schemaElement.setAttribute("password",jdbcSchema.getConnectionObject().getPassword());


				for (final Iterator<Table> ti = schema.getTables().values().iterator(); ti.hasNext();) {
					final Table table = ti.next();
					Log.debug("Writing table: " + table);
					final Element tableElement = new Element(Resources.get("TABLE"));
					schemaElement.addContent(tableElement);
					tableElement.setAttribute("uniqueId", "" + table.getUniqueId());

					tableElement.setAttribute("name", table.getName());

					tableElement.setAttribute("ignore", Boolean.toString(table.isMasked()));

					tableElement.setAttribute("inSchemaPartition", table.getSchemaPartitions().toString());

					

					for (final Iterator<Column> ci = table.getColumns().values().iterator(); ci.hasNext();) {

						final Column col = ci.next();

						Log.debug("Writing column: " + col);

						final Element colElement = new Element(Resources.get("COLUMN"));

						tableElement.addContent(colElement);

						

						colElement.setAttribute("name",col.getName());

						colElement.setAttribute("visibleModified",Boolean.toString(col.isVisibleModified()));

						colElement.setAttribute("inSchemaPartition",col.getSchemaPartitions().toString());

					}

					//foreign key

					for (Iterator fi = table.getForeignKeys().iterator(); fi.hasNext();) {

						ForeignKey candidateFK = (ForeignKey) fi.next();

						Element fkElement = new Element(Resources.get("ForeignKey"));

						Column[] colArray = candidateFK.getColumns();

						String tmpColumns = "";

						for(int colIndex = 1; colIndex<colArray.length; colIndex++) {

							tmpColumns = tmpColumns+","+colArray[colIndex].getName();

						}

						tmpColumns = colArray[0].getName()+tmpColumns;

						fkElement.setAttribute(Resources.get("inColumns"),tmpColumns);

						fkElement.setAttribute(Resources.get("Status"),candidateFK.getStatus().toString());

						tableElement.addContent(fkElement);

					}

					//PK

					PrimaryKey pk = table.getPrimaryKey();

					if(pk!=null) {

						Element pkElement = new Element(Resources.get("PrimaryKey"));

						Column[] colArray = pk.getColumns();

						String tmpColumns = "";

						for(int colIndex=1; colIndex<colArray.length; colIndex++) {

							tmpColumns = tmpColumns+","+colArray[colIndex].getName();

						}

						tmpColumns = colArray[0].getName()+tmpColumns;						

						pkElement.setAttribute(Resources.get("inColumns"),tmpColumns);

						pkElement.setAttribute(Resources.get("Status"),pk.getStatus().toString());

						tableElement.addContent(pkElement);

					}

				}

				//Relation

				for(Iterator si = jdbcSchema.getRelations().iterator(); si.hasNext(); ) {

					Element relationElement = new Element(Resources.get("RELATION"));

					final Relation r = (Relation) si.next();

					relationElement.setAttribute("cardinality",r.getCardinality().getName());

					relationElement.setAttribute("originalCardinality",r.getOriginalCardinality().getName());

					relationElement.setAttribute(Resources.get("Status"),r.getStatus().toString());



					Column[] colArray = r.getFirstKey().getColumns();

					String tmpColumns = "";

					for(int colIndex=1; colIndex<colArray.length; colIndex++) {

						tmpColumns = tmpColumns+","+colArray[colIndex].getTable().getName()+"."+colArray[colIndex].getName();

					}

					tmpColumns = colArray[0].getTable().getName()+"."+colArray[0].getName()+tmpColumns;						

					

					relationElement.setAttribute(Resources.get("FirstKey"),tmpColumns);

					Column[] colArray2 = r.getSecondKey().getColumns();

					

					tmpColumns = "";

					for(int colIndex=1; colIndex<colArray2.length; colIndex++) {

						tmpColumns = tmpColumns+","+colArray2[colIndex].getTable().getName()+"."+colArray2[colIndex].getName();

					}

					tmpColumns = colArray2[0].getTable().getName()+"."+colArray2[0].getName()+tmpColumns;						



					relationElement.setAttribute(Resources.get("SecondKey"),tmpColumns);	

					schemaElement.addContent(relationElement);

				}			

				newNodes.add(new JDomNodeAdapter(schemaElement));

			}

		}

		return newNodes;

		

    }



    public List<JDomNodeAdapter> addDataSet(Mart mart, Element parent) {
       	Element currentElement;
    	if(parent==null)
    		currentElement = this.node;
    	else
    		currentElement = parent;

    	List<JDomNodeAdapter> newNodes = new ArrayList<JDomNodeAdapter>();
		for (final Iterator<DataSet> dsi = mart.getDataSets().values().iterator(); dsi.hasNext();) {
			final DataSet ds = dsi.next();
			// Get schema and dataset mods.

			Log.debug("Writing dataset: " + ds);
	    	//check if current dataset already exists
	    	Element oldDsElement = JDomUtils.searchElement(currentElement, Resources.get("DATASET"), ds.getName());

	    	if(oldDsElement == null) {
				final Element dsElement = new Element(Resources.get("DATASET"));
				currentElement.addContent(dsElement);
				dsElement.setAttribute(Resources.get("NAME"),ds.getName());
				dsElement.setAttribute("optimiser",ds.getDataSetOptimiserType().getName());
				dsElement.setAttribute("invisible",Boolean.toString(ds.isInvisible()));
				dsElement.setAttribute("masked",Boolean.toString(ds.isMasked()));
				dsElement.setAttribute("hideMasked",Boolean.toString(ds.isHideMasked()));
				dsElement.setAttribute("indexOptimiser",Boolean.toString(ds.isIndexOptimiser()));
				dsElement.setAttribute("centralTable",ds.getCentralTable().getName());

				//last update time
				dsElement.setAttribute(Resources.get("TIME"),McUtils.getCurrentTimeString());
				//config name is user_type_dataset
				String user = McGuiUtils.INSTANCE.getCurrentUser().getUserName();
				String guiType = McGuiUtils.INSTANCE.getGuiType().toString();


				for (final Iterator<DataSetTable> i = ds.getTables().values().iterator(); i.hasNext();) {

					final DataSetTable dsTable = i.next();
					Log.debug("Writing modifications for " + dsTable);
					final Element dstElement = new Element(Resources.get("DSTABLE"));

					dsElement.addContent(dstElement);

					dstElement.setAttribute("name",dsTable.getName());

					DataSetTableType type = dsTable.getType();

					if(type.equals(DataSetTableType.MAIN))

						dstElement.setAttribute("type","0");

					else if(type.equals(DataSetTableType.MAIN_SUBCLASS))

						dstElement.setAttribute("type","1");

					else

						dstElement.setAttribute("type","2");

						

					//attribute

					for (final Iterator<Column> ci = dsTable.getColumns().values().iterator(); ci.hasNext();) {

						final Column col = ci.next();

						final Element colElement = new Element(Resources.get("COLUMN"));

						dstElement.addContent(colElement);

						colElement.setAttribute("name",col.getName());

						//default no masked column

						colElement.setAttribute(Resources.get("maskColumnTitle"),"0");

					}

				}


					Element e = this.findChildElement(currentElement, Resources.get("DATASET"), ds.getName());

					this.doNaive(e, mart, ds.getName(),user);

			

				newNodes.add(new JDomNodeAdapter(dsElement));

	    	}

		}

		return newNodes;
    }

    

    public void addGUIs(List<JDomNodeAdapter>configs) {;

    	String name = Resources.get("NAME");

    	List<Element> guiList = this.node.getChildren();

    	for(JDomNodeAdapter dataset:configs) {

    		for(Element gui:guiList) {

	    		String datasetName = dataset.getAttributeValue(name);	    		 

		    	Element configPointer = new Element(Resources.get("CONFIGPOINTER"));

		    	configPointer.setAttribute(name,

		    			this.node.getAttributeValue(name)+"_"+gui.getAttributeValue(name)+"_"+datasetName);

		    	gui.addContent(configPointer);	    		

    		}

    	}

    	

    	

    }

    

    private void doNaive(Element e, Mart mart, String datasetName, String userName) {

    	//e is dataset

    	String rName=Resources.get("NAME");

    	final DataSet ds = this.findDataSet(mart, datasetName);

    	if(ds==null) 

    		return;

    	

    	final Element filterContainer = new Element(Resources.get("CONTAINER"));

    	filterContainer.setAttribute(Resources.get("CONFIG"),userName+"_"+McGuiType.MARTVIEW+"_"+datasetName);

//    	filterContainer.setAttribute(Resources.get("USER"),userName);

//    	filterContainer.setAttribute(Resources.get("GUI"),McGuiType.MARTVIEW.toString());

       	filterContainer.setAttribute(rName,Resources.get("FILTER"));

    	e.addContent(filterContainer);

    	

     	

    	final Element attContainer = new Element(Resources.get("CONTAINER"));

    	attContainer.setAttribute(Resources.get("CONFIG"),userName+"_"+McGuiType.MARTVIEW+"_"+datasetName);

//    	attContainer.setAttribute(Resources.get("USER"),userName);

//    	attContainer.setAttribute(Resources.get("GUI"),McGuiType.MARTVIEW.toString());

    	attContainer.setAttribute(rName,Resources.get("ATTRIBUTE"));

    	e.addContent(attContainer);

    	

    	//need to order the DataSetTable 

    	List<DataSetTable> mainList = new ArrayList<DataSetTable>();

    	List<DataSetTable> subList = new ArrayList<DataSetTable>();

    	List<DataSetTable> dmList = new ArrayList<DataSetTable>();

    	for(Iterator<DataSetTable> it = ds.getTables().values().iterator(); it.hasNext(); ) {

    		DataSetTable dsTable = it.next();

    		if(dsTable.getType().equals(DataSetTableType.MAIN))

    			mainList.add(dsTable);

    		else if(dsTable.getType().equals(DataSetTableType.MAIN_SUBCLASS))

    			subList.add(dsTable);

    		else

    			dmList.add(dsTable);

    	}

    	//order sublist

    	while(subList.size()>0) {

    		DataSetTable lastDst = mainList.get(mainList.size()-1);

    		for(DataSetTable dst:subList) {

    			if(dst.getParent().equals(lastDst)) {

    				mainList.add(dst);

    				subList.remove(dst);

    				break;

    			}

    		}

    	}



    	mainList.addAll(dmList);

    	

    	for (DataSetTable dsTable:mainList) {

    		Element ftContainer = new Element(Resources.get("CONTAINER"));

    		ftContainer.setAttribute(rName,dsTable.getName());

    		filterContainer.addContent(ftContainer);

    		

    		Element atContainer = new Element(Resources.get("CONTAINER"));

    		atContainer.setAttribute(rName,dsTable.getName());

    		attContainer.addContent(atContainer);

    		

    		String martName = e.getParentElement().getAttributeValue(Resources.get("NAME"));

    		String locName = e.getParentElement().getParentElement().getAttributeValue(Resources.get("NAME"));

    		//columns

    		for(final Iterator<DataSetColumn> ci=dsTable.getColumns().values().iterator(); ci.hasNext();) {

    			DataSetColumn col = ci.next();

    			Element attributeElement = new Element(Resources.get("ATTRIBUTEPOINTER"));

    			atContainer.addContent(attributeElement);    			

    			attributeElement.setAttribute(rName,col.getName());

    			attributeElement.setAttribute(Resources.get("POINTER"),"false");

    			attributeElement.setAttribute(Resources.get("TARGETFIELD"),col.getName());

    			if(col instanceof WrappedColumn) {

    				attributeElement.setAttribute(Resources.get("SOURCEFIELD"),

    						((WrappedColumn)col).getWrappedColumn().getName());

    				attributeElement.setAttribute(Resources.get("SOURCETABLE"),

    						((WrappedColumn)col).getWrappedColumn().getTable().getName());

    			}else if(col instanceof InheritedColumn) {

    				attributeElement.setAttribute(Resources.get("SOURCEFIELD"),

    						((InheritedColumn)col).getInheritedColumn().getName());

    				attributeElement.setAttribute(Resources.get("SOURCETABLE"),

    						((InheritedColumn)col).getInheritedColumn().getTable().getName());    				

    			}

    			attributeElement.setAttribute(Resources.get("LOCATION"),locName);

    			attributeElement.setAttribute(Resources.get("MART"),martName);

    			attributeElement.setAttribute(Resources.get("VERSION"),"");

    			attributeElement.setAttribute(Resources.get("DATASET"),ds.getName());

    			attributeElement.setAttribute(Resources.get("CONFIG"),"naive");

    			attributeElement.setAttribute(Resources.get("DSTABLE"),dsTable.getName());

    			attributeElement.setAttribute(Resources.get("SOURCERANGE"),"");

    			attributeElement.setAttribute(Resources.get("TARGETRANGE"),"");

    			attributeElement.setAttribute(Resources.get("REPORT"),"true");

    			attributeElement.setAttribute(Resources.get("DISPLAYNAME"),col.getName());    	

    			

    			Element filterElement = new Element(Resources.get("FILTER"));

    			filterElement.setAttribute(rName, col.getName());

    			filterElement.setAttribute(Resources.get("POINTER"),"false");

    			filterElement.setAttribute(Resources.get("TARGETFIELD"),col.getName());

    			if(col instanceof WrappedColumn) {

    				filterElement.setAttribute(Resources.get("SOURCEFIELD"),

    						((WrappedColumn)col).getWrappedColumn().getName());

    				filterElement.setAttribute(Resources.get("SOURCETABLE"),

    						((WrappedColumn)col).getWrappedColumn().getTable().getName());

    			}

    			filterElement.setAttribute(Resources.get("LOCATION"),locName);

    			filterElement.setAttribute(Resources.get("MART"),martName);

    			filterElement.setAttribute(Resources.get("VERSION"),"");

    			filterElement.setAttribute(Resources.get("DATASET"),ds.getName());

    			filterElement.setAttribute(Resources.get("CONFIG"),"naive");

    			filterElement.setAttribute(Resources.get("DSTABLE"),dsTable.getName());

    			filterElement.setAttribute(Resources.get("SOURCERANGE"),"");

    			filterElement.setAttribute(Resources.get("TARGETRANGE"),"");

    			filterElement.setAttribute(Resources.get("REPORT"),"true");

    			filterElement.setAttribute(Resources.get("DISPLAYNAME"),col.getName());    	

    			ftContainer.addContent(filterElement);

    			

    		}

    		

    	}

    }

    

    private DataSet findDataSet(Mart mart, String datasetName) {

		for (final Iterator<DataSet> dsi = mart.getDataSets().values().iterator(); dsi

		.hasNext();) {

			final DataSet ds = dsi.next();

			if(ds.getName().equals(datasetName))

				return ds;

		}

		return null;    	

    }

    

        

    public Element addMart(String name, Element parent, boolean source) {
    	Element location = (parent == null)? this.node: parent; 
    	//check if this mart already exist
    	Element martElement = JDomUtils.searchElement(location, Resources.get("MART"), name);
    	if(martElement == null) {
    		martElement = new Element(Resources.get("MART"));
    		martElement.setAttribute(Resources.get("NAME"),name);
    		if(source)
    			martElement.setAttribute(Resources.get("TYPE"),Resources.get("MARTTYPESOURCE"));
    		location.addContent(martElement);
    	}	
    	return martElement;
    }

    

    

    

    /**

     * Finds index of child in this node.

     * 

     * @param child The child to look for

     * @return index of child, -1 if not present (error)

     */

    public int index(JDomNodeAdapter child) {



        int count = this.getChildCount();

        for (int i = 0; i < count; i++) {

            JDomNodeAdapter n = this.child(i);

            if (child.node == n.node) {

                return i;

            }

        }

        return -1; // Should never get here.

    }

    

    public Object getUserObject() {

    	return this.node;

    }



    /**

     * Returns an adapter node given a valid index found through

     * the method: public int index(JDOMAdapterNode child)

     * 

     * @param searchIndex find this by calling index(JDOMAdapterNode)

     * @return the desired child

     */

    public JDomNodeAdapter child(int searchIndex) {

        Element child = (Element)node.getChildren().get(searchIndex);

        return new JDomNodeAdapter(child);

    }

    

    /**

     * should consider the filter later

     */

    public TreeNode getChildAt(int searchIndex) {

        Element child = (Element)node.getChildren().get(searchIndex);

        return new JDomNodeAdapter(child);

    }





    /**

     * Return the number of children for this element/node

     * 

     * @return int number of children

     */

    public int childCount() {

        return node.getChildren().size();

    }

    

    public int getChildCount() {

    	return this.childCount();

    }

    

    

    /**

     * Tricky toString which allows for copying entire elements and their children

     * from the xml viewer.

     * 

     * @return String

     */

    public String toString() {

        StringBuilder sb = new StringBuilder();

        if(this.getChildCount() > 0) {

            sb.append(node.getName() + lf);

            for (int i = 0; i < this.getChildCount(); i++) {

                JDomNodeAdapter child = child(i);

                sb.append(child.toString(1));

            }



        } else {

            sb.append(tab + node.getName() +"["+node.getTextTrim()+"]"+lf);

        }

        

        return sb.toString();

    }

    

    /** used recursively to space the xml */

    public String toString(int r) {

        //tab to appropriate level

        StringBuilder tabs = new StringBuilder();

        for (int i = 0; i < r; i++) {

            tabs.append(tab);

        }

        String space = tabs.toString();

        

        StringBuilder sb = new StringBuilder();

        if(this.getChildCount() > 0) {

            sb.append(space + node.getName() + lf);

            for (int i = 0; i < this.getChildCount(); i++) {

                JDomNodeAdapter child = child(i);

                sb.append(child.toString(r+1));

            }



        } else {

            sb.append(space + node.getName() +"["+node.getTextTrim()+"]"+lf);

        }

        

        return sb.toString();

    }

    

    public String getAttributeValue(String att) {

    	

    	return this.node.getAttributeValue(att);

    }



    private Element findChildElement(Element parent, String name, String value) {

    	List<Element> eList = parent.getChildren(name);

    	for(Element e:eList) {

    		if(e.getAttributeValue(Resources.get("NAME")).equals(value))

    			return e;

    	}

    	return null;

    }

       

    public TreeNode getParent() {

    	Element e = this.node.getParentElement();

    	if(e==null)

    	{

    		this.parent=null;

    		return null;

    	}

    	this.parent = new JDomNodeAdapter(e);

    	return (JDomNodeAdapter)this.parent;

    }

        

    private void initLocations(Object locObject) {
    	if(locObject instanceof Location) {
    		//TODO should change to DsConnectionObject
	    	Location loc = (Location)locObject;
	    	loc.requestCreateLocationFromDB();
	    	//if finished, add the content to tree
	    	//this is martRegistry
	    	//find if this location already exist
	    	String locName = loc.getName();
	    	//find the location of the user
	    	Element locElement = JDomUtils.searchElementInUser(this.getNode(), 
	    			McGuiUtils.INSTANCE.getCurrentUser().getUserName(),
	    			Resources.get("LOCATION"), locName);

	    	if(locElement == null) {
	    		Element locE = new Element(Resources.get("LOCATION"));
		    	locE.setAttribute(Resources.get("NAME"),loc.getName());
		    	locE.setAttribute(Resources.get("TYPE"),Resources.get("RDBMSTYPE"));
		    	//add all synchronized users
		    	List<String> userList = McGuiUtils.INSTANCE.getSynchronizedUserList(McGuiUtils.INSTANCE.getCurrentUser().getUserName());
		    	if(userList==null || userList.size()==0)
		    		locE.setAttribute(Resources.get("USER"),McGuiUtils.INSTANCE.getCurrentUser().getUserName());
		    	else
		    		locE.setAttribute(Resources.get("USER"),McUtils.StrListToStr(userList));

		    	this.node.addContent(locE);
		    	locElement = locE;
	    	}    	

	    	this.addLocation(loc, locElement);
			McEventObject mcObject = new McEventObject(EventType.Request_NewLocation,loc);
			McViews.getInstance().getView(IdwViewType.MCTREE).getController().processV2Cupdate(mcObject); 

    	}else if(locObject instanceof DsConnectionObject) {
    		DsConnectionObject conObj = (DsConnectionObject) locObject;
    		Map<String, List<String>> dsInfoMap = conObj.getDsInfoMap();
			for(String name:dsInfoMap.keySet()) {
				for(String dsName:dsInfoMap.get(name)) {
					try {
						//get ride of http:
						String host = conObj.getHost();
						if(conObj.getPort()!=null && !conObj.getPort().equals(""))
							host = host+":"+conObj.getPort();
						MartServiceIdentifier initialHost = new MartServiceIdentifier(conObj.getHost(),conObj.getPort(),conObj.getPath());
						TransformationMain.fetchWebServiceConfigurationMap(initialHost, conObj.getConfigMap().get(host+conObj.getPath()));
						Document urlDoc = TransformationYongPrototype.wrappedTransform(initialHost, name, dsName);
/*						System.out.println(initialHost);
						System.out.println(name);
						System.out.println(dsName);
				    	XMLOutputter outputter = new XMLOutputter();
				     	try {
				     		File file = new File("/Users/yliang/output.xml");
				    		FileOutputStream fos = new FileOutputStream(file);
				    		outputter.output(urlDoc, fos);
				    		fos.close();
				    	}
				    	catch(Exception e) {
				    		e.printStackTrace();
				    	}
*/
						//change the location name
						Element locElement = urlDoc.getRootElement().getChild(Resources.get("LOCATION"));
						boolean isNameEmpty = false;
						if(conObj.getName()==null || conObj.getName().equals("")) 
							isNameEmpty = true;
						if(isNameEmpty) 
							locElement.setAttribute(Resources.get("NAME"),host);
						else
							locElement.setAttribute(Resources.get("NAME"),conObj.getName());
						//change the mart name
						Element martElement = locElement.getChild(Resources.get("MART"));
						martElement.setAttribute(Resources.get("NAME"),name);
						this.addLocationFromDocument(urlDoc);

						//McEvent needs a not null object, host
						McEventObject mcObject = new McEventObject(EventType.Request_NewLocation,host);
						McViews.getInstance().getView(IdwViewType.MCTREE).getController().processV2Cupdate(mcObject); 
						//storeInHistory hardcode for now
					} catch (TechnicalException e) {
						e.printStackTrace();
					} catch (FunctionalException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
    	}

    }
    

    /**

     * hardcode for mysql:jdbc

     */

    
    public void addLocations() {

    	//pass in current location set
    	final Object locObject = LocationConnectionDialog.showDialog(((McViewSchema)McViews.getInstance().getView(IdwViewType.SCHEMA)).getLocations());
    	if(locObject==null)
    		return;

		final ProgressDialog2 progressMonitor = ProgressDialog2.getInstance();				

		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				try {
					initLocations(locObject);
				} catch (final Throwable t) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							StackTrace.showStackTrace(t);
						}
					});
				}finally {
					progressMonitor.setVisible(false);
				//	progressMonitor.dispose();					
				}
				return null;
			}

			public void finished() {
				// Close the progress dialog.
				progressMonitor.setVisible(false);
				//progressMonitor.dispose();
			}
		};
		
		worker.start();
		progressMonitor.start("processing ...");
    }



    public void addImportable(String importableName) {

    	Element dsElement = this.findAncestorElement(this.node, Resources.get("DATASET"));

    	if(dsElement==null)

    		return; //should not go here

    	

    	Element importableE = new Element(Resources.get("IMPORTABLE"));

    	importableE.setAttribute(Resources.get("NAME"), importableName);

    	importableE.setAttribute(Resources.get("GUI"),McGuiUtils.INSTANCE.getGuiType().toString());

    	dsElement.addContent(importableE);

    	

    }

    

    public void addExportable(String exportableName) {

    	Element dsElement = this.findAncestorElement(this.node, Resources.get("DATASET"));

    	if(dsElement==null)

    		return; //should not go here

   	

    	Element exportableE = new Element(Resources.get("EXPORTABLE"));

    	exportableE.setAttribute(Resources.get("NAME"), exportableName);

    	exportableE.setAttribute(Resources.get("GUI"),McGuiUtils.INSTANCE.getGuiType().toString());

    	dsElement.addContent(exportableE);

    	

    }



    /**

     * ptValue is hardcoded for now

     */

    public void addPartition(String dsName, String dsTableName, String colName, List<String> ptValues) {

    	//current node is dataset

    	//find the datasettable type : main, submain, dm

    	Element dsTableElement = JDomUtils.searchElement(this.getNode(), Resources.get("DSTABLE"), dsTableName);

    	Element partElement = new Element(Resources.get("PARTITIONTABLE"));

    	List<Element> ptList = this.node.getChildren(Resources.get("PARTITIONTABLE"));

    	String ptName = Resources.get("PTPREFIX")+(ptList.size()+1);

    	

    	partElement.setAttribute(Resources.get("NAME"), ptName);

    	partElement.setAttribute(Resources.get("DATASET"),dsName);

    	partElement.setAttribute(Resources.get("DSTABLE"),dsTableName);

    	partElement.setAttribute(Resources.get("COLUMN"),colName);

    	partElement.setAttribute("cols","1");

    	String dstType = dsTableElement.getAttributeValue(Resources.get("TYPE"));

    	partElement.setAttribute(Resources.get("TYPE"),dstType);

    	//default not flatten on dm

    	if(dstType.equals("2"))

    		partElement.setAttribute(Resources.get("FLATTEN"),"0");

    	this.node.addContent(partElement);

    	

    	int row = 1;

    	for(String item:ptValues) {

    		Element cellElement = new Element("cell");

        	cellElement.setAttribute("row", ""+row);

        	cellElement.setAttribute("col","1");

        	cellElement.setAttribute("value", item);

        	row++;

        	partElement.addContent(cellElement);

    	}

    	partElement.setAttribute("rows",""+(row-1));

    	//update filters and attributes

    	Element filterElement = JDomUtils.searchElement(this.getNode(), Resources.get("FILTER"), colName);

    	Element attElement = JDomUtils.searchElement(this.getNode(), Resources.get("ATTRIBUTEPOINTER"), colName);

    	filterElement.setAttribute(Resources.get("PARTITIONTABLE"),ptName);

    	attElement.setAttribute(Resources.get("PARTITIONTABLE"),ptName);

     }

    

    public void addContainer() {

    	Element containerElement = new Element(Resources.get("CONTAINER"));

    	this.node.addContent(containerElement);

    	containerElement.setAttribute(Resources.get("NAME"),"");

    }

    

    public void addPartition(Element ptElement, List<String> ptValues) {

    	//change col count;

    	String colCount = ptElement.getAttributeValue("cols");

    	int col = 0;

    	try {

    		col = Integer.parseInt(colCount)+1;

    	}catch(Exception e) {

    		System.err.println(e.getStackTrace());

    		return;

    	}

    	ptElement.setAttribute("cols",""+col);

    	int row = 1;

    	for(String item:ptValues) {

    		Element cellElement = new Element("cell");

        	cellElement.setAttribute("row", ""+row);

        	cellElement.setAttribute("col",""+col);

        	cellElement.setAttribute("value", item);

        	row++;

        	ptElement.addContent(cellElement);

    	}

    }

        

    public void addLocation(Location loc, Element locElement) {
    	//current node is Location
    	Element martElement;
    	for(Iterator<Mart> i = loc.getMarts().values().iterator(); i.hasNext();) {
    		Mart mart = i.next();
    		martElement = this.addMart(mart.getMartName(), locElement,loc.isFromSourceSchema());
     		List<JDomNodeAdapter> datasets = this.addDataSet(mart,martElement);
     		for(JDomNodeAdapter dataset:datasets) {
     			this.addSourceSchemas(mart, dataset.getNode());
     		}
    	}
    }



    public void removePartition(String ptName) {

    	//this is a dataset

    	Element ptElement = JDomUtils.searchElement(this.node, Resources.get("PARTITIONTABLE"), ptName);

    	this.node.removeContent(ptElement);

    	//update filters and attributes

    	Map<String, String> conditions = new HashMap<String, String>();

    	conditions.put(Resources.get("PARTITIONTABLE"), ptName);

    	Element filterElement = JDomUtils.searchElementWithCondition(

    			this.getNode(), Resources.get("FILTER"), Resources.get("PARTITIONTABLE"),ptName);

    	Element attElement = JDomUtils.searchElementWithCondition(this.getNode(), 

    			Resources.get("ATTRIBUTEPOINTER"), Resources.get("PARTITIONTABLE"), ptName);

    	filterElement.removeAttribute(Resources.get("PARTITIONTABLE"));

    	attElement.removeAttribute(Resources.get("PARTITIONTABLE"));    	

    }

    public boolean isMart() {

    	return false;

    }

    

    public boolean isLocation() {

    	return this.node.getName().equals(Resources.get("LOCATION"));

    }

    

    public boolean isNodeType(String type) {

    	return this.node.getName().equals(type);

    }



    public void removePortable() {

    	this.getNode().detach();

    }



    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = PRIME + this.node.hashCode();
        return result;
    }

    

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
 
        if (obj == null)
            return false;
 
        if (getClass() != obj.getClass())
            return false;

        if(! (obj instanceof JDomNodeAdapter))
        	return false;

        JDomNodeAdapter jdomNode = (JDomNodeAdapter)obj;
        return jdomNode.getNode().equals(this.node);
    }



    public void updateMaskedDSCol(DataSetColumn dsCol) {
    	//this is dataset node
    	String dsTableName = dsCol.getDataSetTable().getName();
    	Element dsTableElement = JDomUtils.searchElement(this.node, Resources.get("DSTABLE"), dsTableName);
    	Element attributeElement = JDomUtils.searchElement(dsTableElement, 
    			Resources.get("COLUMN"), dsCol.getName());
    	attributeElement.setAttribute(Resources.get("maskColumnTitle"),dsCol.isColumnMasked() ? "1":"0");
    }



    public Element findAncestorElement(Element currentE, String ancestor) {

    	if(currentE.getName().equals(ancestor))

    		return currentE;

    	boolean found = false;

    	while(!found) {

    		currentE = currentE.getParentElement();

    		if(currentE == null)

    			return null;

    		if(currentE.getName().equals(ancestor))

    			found = true;

    	}

    	return currentE;

    }



    private void addLocationFromDocument(Document doc) {

    	//find location name

    	String nameStr = Resources.get("NAME");

    	Element sourceLoc = doc.getRootElement().getChild(Resources.get("LOCATION"));

    	Element sourceMart = sourceLoc.getChild(Resources.get("MART"));

    	String locName = sourceLoc.getAttributeValue(nameStr);

    	String martName = sourceMart.getAttributeValue(nameStr);

    	Element locElement = JDomUtils.searchElementInUser(((JDomNodeAdapter)this.getRoot()).getNode(), 

    			McGuiUtils.INSTANCE.getCurrentUser().getUserName(),

    			Resources.get("LOCATION"), 

    			locName);

    	Element martElement = null;

    	if(locElement==null) {

    		locElement = new Element(Resources.get("LOCATION"));

    		locElement.setAttribute(nameStr,locName);

	    	locElement.setAttribute(Resources.get("TYPE"),Resources.get("URLTYPE"));
	    	//add all users in synchronize list
	    	List<String> userList = McGuiUtils.INSTANCE.getSynchronizedUserList(McGuiUtils.INSTANCE.getCurrentUser().getUserName());

	    	if(userList==null || userList.size()==0)
	    		locElement.setAttribute(Resources.get("USER"),McGuiUtils.INSTANCE.getCurrentUser().getUserName()); 
	    	else
	    		locElement.setAttribute(Resources.get("USER"),McUtils.StrListToStr(userList));
    		((JDomNodeAdapter)this.getRoot()).getNode().addContent(locElement);
    		martElement = new Element(Resources.get("MART"));
    		martElement.setAttribute(nameStr,martName);
    		locElement.addContent(martElement);
    	} else {

    		//check if the mart exist

    		Map<String,String> conditions = new HashMap<String,String>();
    		conditions.put(nameStr, martName);
    		martElement = JDomUtils.findChildElement(locElement, Resources.get("MART"), conditions);

    		if(martElement==null) {

        		martElement = new Element(Resources.get("MART"));

        		martElement.setAttribute(nameStr,martName);    			

        	   	locElement.addContent(martElement);

        	}

    	}

    	//assume no duplicate datast for now

    	Element sourceDs = sourceMart.getChild(Resources.get("DATASET"));

    	Element targetDs = new Element(Resources.get("DATASET"));

    	martElement.addContent(targetDs);

    	

    	targetDs.setAttribute(nameStr,sourceDs.getAttributeValue(nameStr));

    	targetDs.setAttribute(Resources.get("TIME"),McUtils.getCurrentTimeString());

    	//jdbcSchema

    	Element jdbcSchema = new Element(Resources.get("SOURCESCHEMA"));

    	jdbcSchema.setAttribute(nameStr,martName+"_"+sourceDs.getAttributeValue(nameStr));
    	targetDs.addContent(jdbcSchema);
    	//get all tables
    	List<Element> sTableList = JDomUtils.searchElementList(sourceDs, Resources.get("TABLE"), null);
    	String centralTableName = sourceDs.getAttributeValue("centralTable");
    	targetDs.setAttribute("centralTable",centralTableName);
    	List<String> mtList = new ArrayList<String>();
    	for(Element sTable:sTableList) {
    		boolean isMain = sTable.getAttributeValue("main").equals("true")? true: false;
    		String keyStr = sTable.getAttributeValue("key");
    		String tableNameStr = sTable.getAttributeValue(nameStr);
    		if(isMain) 
    			mtList.add(tableNameStr);
    		
    		Element dstElement = new Element(Resources.get("DSTABLE"));
    		dstElement.setAttribute(nameStr,tableNameStr);
    		targetDs.addContent(dstElement);

    			
    		Element stElement = new Element(Resources.get("TABLE"));
    		stElement.setAttribute(nameStr,tableNameStr);
    		jdbcSchema.addContent(stElement);
    		
    		if(isMain) {
    			Element pkElement = new Element(Resources.get("PrimaryKey"));
    			pkElement.setAttribute(Resources.get("inColumns"),keyStr);
    			pkElement.setAttribute(Resources.get("Status"),"INFERRED");
    			stElement.addContent(pkElement);    				
    		}else {
    			Element fkElement = new Element(Resources.get("ForeignKey"));
    			fkElement.setAttribute(Resources.get("inColumns"),keyStr);
    			fkElement.setAttribute(Resources.get("Status"),"INFERRED");
    			stElement.addContent(fkElement);
    		}

    		List<Element> fieldEList = sTable.getChildren();

    		for(Element field:fieldEList) {
    			Element colElement = new Element(Resources.get("COLUMN"));
    			String colName = field.getAttributeValue(Resources.get("NAME"));
    			colElement.setAttribute(nameStr,colName);
    			stElement.addContent(colElement);

    			Element attrElement = new Element(Resources.get("COLUMN"));
    			attrElement.setAttribute(nameStr,colName);
    			dstElement.addContent(attrElement); 
    			if(isMain && !tableNameStr.equals(centralTableName)) {
    				if(colName.indexOf(Resources.get("keySuffix"))>=0 && !colName.equals(keyStr)) {
    	    			Element fkElement = new Element(Resources.get("ForeignKey"));
    	    			fkElement.setAttribute(Resources.get("inColumns"),colName);
    	    			fkElement.setAttribute(Resources.get("Status"),"INFERRED");
    	    			stElement.addContent(fkElement);    					
    				}    					
    			}
    		}

    	}
    	//relation
    	List<Element> relationList = sourceDs.getChildren(Resources.get("RELATION"));
    	for(int i=0; i<relationList.size(); i++) {
    		Element relation = relationList.get(i);
    		//relation.detach();
    		Element relElement = (Element)relation.clone();
    		relElement.detach();
    		//check if it is subclass
    		String firstTable = relation.getAttributeValue("firstTable");
    		String secondTable = relation.getAttributeValue("secondTable");

    		if(mtList.contains(firstTable)&&mtList.contains(secondTable))
    			relElement.setAttribute("subclass","true");
    		jdbcSchema.addContent(relElement);
    	}
    	

    	//find configs

    	List<Element> configList = sourceDs.getChildren(Resources.get("CONFIG"));
    	Map<String,String> conditions = new HashMap<String,String>();
    	for(Element config:configList) {
        	List<Element> containerList = JDomUtils.findChildElements(config, Resources.get("CONTAINER"), conditions);
        	for(Element container:containerList) {
        		Element childElement = JDomUtils.searchElement(container, Resources.get("FILTER"), null);
        		if(childElement == null) {
        			childElement = JDomUtils.searchElement(container, Resources.get("ATTRIBUTE"), null);
        			if(childElement == null) {
        				continue;
        			}
        		}

        		container.detach();
        		String userName = McGuiUtils.INSTANCE.getCurrentUser().getUserName();
            	container.setAttribute(Resources.get("CONFIG"),userName+"_"+container.getAttributeValue(nameStr));
            	targetDs.addContent(container);      	
        	}   

        	//add exportable; importable
        	List<Element> expList = JDomUtils.searchElementList(config, Resources.get("EXPORTABLE"), null);
        	for(Element exp:expList) {
        		exp.detach();
            	exp.setAttribute(Resources.get("GUI"),McGuiUtils.INSTANCE.getGuiType().toString());
        		targetDs.addContent(exp);
        	}

        	List<Element> impList = JDomUtils.searchElementList(config, Resources.get("IMPORTABLE"), null);

        	for(Element imp:impList) {

        		imp.detach();

            	imp.setAttribute(Resources.get("GUI"),McGuiUtils.INSTANCE.getGuiType().toString());

        		targetDs.addContent(imp);        		

        	}

    	}
   	doc = null;

    }



    public void addProcessor(Processor processor) {

    	//node is dataset

    	Element processorElement = new Element(Resources.get("PROCESSOR"));

    	processorElement.setAttribute(Resources.get("NAME"),processor.getName());

    	StringBuffer sb = new StringBuffer();

    	List<String> plist = processor.getFilterList();

    	for(int i=0;i< plist.size()-1; i++) {

    		sb.append(plist.get(i));

    		sb.append(";");

    	}

    	sb.append(plist.get(plist.size()-1));

    	processorElement.setAttribute(Resources.get("FILTER"),sb.toString());

    	processorElement.setAttribute(Resources.get("USER"),McGuiUtils.INSTANCE.getCurrentUser().getUserName());

    	processorElement.setAttribute(Resources.get("GUI"),McGuiUtils.INSTANCE.getGuiType().toString());

    	this.node.addContent(processorElement);

    }

    

    public void addProcessorElement(String processorName) {

    	//node is dataset

    	Element processorElement = new Element(Resources.get("PROCESSOR"));

    	processorElement.setAttribute(Resources.get("NAME"),processorName);

    	processorElement.setAttribute(Resources.get("USER"),McGuiUtils.INSTANCE.getCurrentUser().getUserName());

    	processorElement.setAttribute(Resources.get("GUI"),McGuiUtils.INSTANCE.getGuiType().toString());

    	this.node.addContent(processorElement);

    }



    public void addLinkedDataSet() {

    	//dataset node    

    	AddLinkedDataSetsDialog adsd = new AddLinkedDataSetsDialog(this);

    	List<String> linkedDs = adsd.getSelectedList();

    	if(linkedDs == null || linkedDs.size()==0)

    		return;

    	String linkedDsStr = linkedDs.get(0);

    	for(int i=1; i<linkedDs.size(); i++) {

    		linkedDsStr = linkedDsStr+linkedDs.get(i);

    	}

    	this.node.setAttribute("linkedDS",linkedDsStr);

    }

    

    public void hide() {

    	this.node.setAttribute(Resources.get("HIDE"),"1");

    }

    

    public void hideForCurrentUser() {

    	String hideStr = this.node.getAttributeValue(Resources.get("HIDE"));

    	String currentUser = McGuiUtils.INSTANCE.getCurrentUser().getUserName();

    	if(null==hideStr)

    		this.node.setAttribute(Resources.get("HIDE"),currentUser);

    	else {

    		if(hideStr.equals(currentUser))

    			return;

    		else {

    			hideStr = hideStr+","+currentUser;

    			this.node.setAttribute(Resources.get("HIDE"),hideStr);

    		}

    	}

    		

    }

}