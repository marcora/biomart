package org.biomart.common.general.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Tmp implements Test {

	public static void main(String[] args) throws Exception {
		System.out.println(dd());
		byte[] byary={79, 65, 73};

		String s = new String(byary, "");
	}

	public static String dd() throws IOException {
		Element root = new org.jdom.Element("myRootElement");
		Document newDoc = new Document(root);
		root.setContent(new ArrayList<Element>(Arrays.asList(new Element[] {new Element("my_child")})));
		
		XMLOutputter fmt = new XMLOutputter(Format.getPrettyFormat());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fmt.output(newDoc, baos);
        return baos.toString();
	}
}
