package org.biomart.objects.helpers;


import java.util.Comparator;

import org.biomart.common.general.utils.CompareUtils;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;



public class CurrentPath implements Comparable<CurrentPath>, Comparator<CurrentPath> {

	public static void main(String[] args) {}

	private Location location = null;
	private Mart mart = null;
	private Dataset dataset = null;
	private Config config = null;

	public CurrentPath(Location location, Mart mart) {
		super();
		this.location = location;
		this.mart = mart;
	}

	public void setDatasetAndConfig(Dataset dataset, Config config) {
		this.dataset = dataset;
		this.config = config;
	}
	
	/*public CurrentPath(Location location, Mart mart, Dataset dataset, Config config) {
		super();
		this.location = location;
		this.mart = mart;
		this.dataset = dataset;
		this.config = config;
	}*/

	public Location getLocation() {
		return location;
	}

	public Mart getMart() {
		return mart;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public Config getConfig() {
		return config;
	}

	public String getLocationName() {
		return location.getName();
	}

	public String getMartName() {
		return mart.getName();
	}

	public Integer getMartVersion() {
		return mart.getVersion();
	}

	public String getDatasetName() {
		return dataset.getName();
	}

	public String getConfigName() {
		return config.getName();
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"location = " + location + ", " +
			"mart = " + mart + ", " +
			"dataset = " + dataset + ", " +
			"config = " + config;
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		CurrentPath currentPath=(CurrentPath)object;
		return (
			(this.location==currentPath.location || (this.location!=null && location.equals(currentPath.location))) &&
			(this.mart==currentPath.mart || (this.mart!=null && mart.equals(currentPath.mart))) &&
			(this.dataset==currentPath.dataset || (this.dataset!=null && dataset.equals(currentPath.dataset))) &&
			(this.config==currentPath.config || (this.config!=null && config.equals(currentPath.config)))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==location? 0 : location.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==mart? 0 : mart.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==dataset? 0 : dataset.hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==config? 0 : config.hashCode());
		return hash;
	}

	public int compare(CurrentPath currentPath1, CurrentPath currentPath2) {
		if (currentPath1==null && currentPath2!=null) {
			return -1;
		} else if (currentPath1!=null && currentPath2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(currentPath1.location, currentPath2.location);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(currentPath1.mart, currentPath2.mart);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(currentPath1.dataset, currentPath2.dataset);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(currentPath1.config, currentPath2.config);
	}

	public int compareTo(CurrentPath currentPath) {
		return compare(this, currentPath);
	}

}
