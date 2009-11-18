package org.biomart.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biomart.common.general.utils.XmlUtils;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.GroupFilter;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.objects.objects.PartitionTable;
import org.biomart.objects.objects.SimpleFilter;
import org.biomart.objects.objects.types.LocationType;
import org.jdom.Element;

public class DummyPortal {

	public static void main(String[] args) throws Exception {
		MartRegistry martRegistry = createDummyMartRegistry();
		Element xml = martRegistry.generateXml();
		System.out.println(XmlUtils.writeXmlFile(xml, "/home/anthony/Desktop/dummyPortal.xml"));
	}

	public static MartRegistry createDummyMartRegistry() {
		MartRegistry martRegistry = new MartRegistry();
		
		Location location = new Location("location0", "location0", "location0", true, "www.host0.com", "anonymous", LocationType.RDBMS);
		martRegistry.addLocation(location);
		
		Mart mart = new Mart("mart0", "mart0", "mart0", true, 1);
		location.addMart(mart);
		
		Dataset dataset = new Dataset("dataset0", "dataset0", "dataset0", true, true);
		mart.addDataset(dataset);
		
		Config config = new Config("config0", "dataset0");
		dataset.addConfig(config);
		
		List<List<String>> table = new ArrayList<List<String>>();
		table.add(new ArrayList<String>(Arrays.asList(new String[] {"row0"})));
		table.add(new ArrayList<String>(Arrays.asList(new String[] {"row1"})));
		table.add(new ArrayList<String>(Arrays.asList(new String[] {"row2"})));
		PartitionTable mainPartitionTable = new PartitionTable("0", 3, 1, table, true);
		
		Container rootContainer = config.getRootContainer();
		
		Container container0 = new Container("container0", "container0", "container0", true, null);
		rootContainer.addContainer(container0);
		
		Container container1 = new Container("container1", "container1", "container1", true, null);
		rootContainer.addContainer(container1);
		
		Attribute attribute = new Attribute(mainPartitionTable, "attribute");
		attribute.setPointer(false);
		container0.addAttribute(attribute);
		
		SimpleFilter simpleFilter0 = new SimpleFilter(mainPartitionTable, "simpleFilter0", false);
		SimpleFilter simpleFilter1 = new SimpleFilter(mainPartitionTable, "simpleFilter1", false);
		SimpleFilter simpleFilter2 = new SimpleFilter(mainPartitionTable, "simpleFilter2", false);
		simpleFilter0.setPointer(false);
		simpleFilter1.setPointer(false);
		simpleFilter2.setPointer(false);
		container0.addFilter(simpleFilter0);
		container0.addFilter(simpleFilter1);
		container0.addFilter(simpleFilter2);
		
		GroupFilter groupFilter = new GroupFilter(mainPartitionTable, "groupFilter");
		groupFilter.getElementList().addElement(simpleFilter0);
		groupFilter.getElementList().addElement(simpleFilter1);
		groupFilter.getElementList().addElement(simpleFilter2);
		groupFilter.setPointer(false);
		container1.addFilter(groupFilter);
		return martRegistry;
	}
}
