package org.biomart.martRemote.objects.response;

import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.martRemote.Jsoml;
import org.biomart.martRemote.objects.request.MartRemoteRequest;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Element;
import org.biomart.objects.objects.MartRegistry;

public class GetAttributesResponse extends GetElementsResponse {

	private List<Attribute> attributeList = null;

	public GetAttributesResponse(MartRegistry martRegistry, MartRemoteRequest martServiceRequest) {
		super(martRegistry, martServiceRequest);
		this.attributeList = new ArrayList<Attribute>();
	}

	public List<Attribute> getAttributeList() {
		return attributeList;
	}

	public void populateObjects() throws FunctionalException {
		super.populateObjects(true);
		for (Element element : super.elementList) {
			this.attributeList.add((Attribute)element);
		}
	}

	@Override
	public Jsoml createOutputResponse(boolean xml, Jsoml root) throws FunctionalException {
		for (Attribute attribute : this.attributeList) {
			root.addContent(attribute.generateOutputForWebService(xml));
		}
		return root;
	}
	/*protected Document createXmlResponse(Document document) throws FunctionalException {
		org.jdom.Element root = document.getRootElement();
		for (Attribute attribute : this.attributeList) {
			root.addContent(attribute.generateXmlForWebService());
		}
		return document;
	}*/
}
