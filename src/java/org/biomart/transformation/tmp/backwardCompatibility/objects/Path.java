package org.biomart.transformation.tmp.backwardCompatibility.objects;

public class Path {

	public static void main(String[] args) {}

	public String location = null;
	public String mart = null;
	public String version = null;
	public String dataset = null;
	public String config = null;
	public String element = null;

	public Path(String location, String mart, String version, String dataset, String config) {
		this(location, mart, version, dataset, config, null);
	}
	public Path(String location, String mart, String version, String dataset, String config, String element) {
		super();
		this.location = location;
		this.mart = mart;
		this.version = version;
		this.dataset = dataset;
		this.config = config;
		this.element = element;
	}

	public String getLocation() {
		return location;
	}

	public String getMart() {
		return mart;
	}

	public String getVersion() {
		return version;
	}

	public String getDataset() {
		return dataset;
	}

	public String getConfig() {
		return config;
	}

	public String getElement() {
		return element;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setMart(String mart) {
		this.mart = mart;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public void setElement(String element) {
		this.element = element;
	}

	@Override
	public String toString() {
		return 
			"location = " + location + ", " +
			"mart = " + mart + ", " +
			"version = " + version + ", " +
			"dataset = " + dataset + ", " +
			"config = " + config + ", " +
			"element = " + element;
	}
}
