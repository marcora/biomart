package org.biomart.transformation.helpers;

import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.objects.Container;



public class ContainerPath /*implements Comparable<ContainerPath>, Comparator<ContainerPath> */{

	public static void main(String[] args) {}

	private Container pageContainer = null;
	private Container groupContainer = null;
	private Container collectionContainer = null;

	public ContainerPath(Container pageContainer, Container groupContainer, Container collectionContainer) {
		super();
		this.pageContainer = pageContainer;
		this.groupContainer = groupContainer;
		this.collectionContainer = collectionContainer;
	}

	public Container getPageContainer() {
		return pageContainer;
	}

	public Container getGroupContainer() {
		return groupContainer;
	}

	public Container getCollectionContainer() {
		return collectionContainer;
	}

	public void setPageContainer(Container pageContainer) {
		this.pageContainer = pageContainer;
	}

	public void setGroupContainer(Container groupContainer) {
		this.groupContainer = groupContainer;
	}

	public void setCollectionContainer(Container collectionContainer) {
		this.collectionContainer = collectionContainer;
	}

	@Override
	public String toString() {
		return 
			super.toString() + ", " + 
			"pageContainer = " + pageContainer==null ? null : pageContainer.getName() + ", " +
			"groupContainer = " + groupContainer==null ? null : groupContainer.getName() + ", " +
			"collectionContainer = " + collectionContainer==null ? null : collectionContainer.getName();
	}

	@Override
	public boolean equals(Object object) {
		if (this==object) {
			return true;
		}
		if((object==null) || (object.getClass()!= this.getClass())) {
			return false;
		}
		ContainerPath containerPath=(ContainerPath)object;
		return (
			(this.pageContainer==containerPath.pageContainer || (this.pageContainer!=null && containerPath.pageContainer!=null && pageContainer.getName().equals(containerPath.pageContainer.getName()))) &&
			(this.groupContainer==containerPath.groupContainer || (this.groupContainer!=null && containerPath.groupContainer!=null && groupContainer.getName().equals(containerPath.groupContainer.getName()))) &&
			(this.collectionContainer==containerPath.collectionContainer || (this.collectionContainer!=null && containerPath.collectionContainer!=null && collectionContainer.getName().equals(containerPath.collectionContainer.getName())))
		);
	}

	@Override
	public int hashCode() {
		int hash = MartConfiguratorConstants.HASH_SEED1;
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + super.hashCode();
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==pageContainer? 0 : pageContainer.getName().hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==groupContainer? 0 : groupContainer.getName().hashCode());
		hash = MartConfiguratorConstants.HASH_SEED2 * hash + (null==collectionContainer? 0 : collectionContainer.getName().hashCode());
		return hash;
	}

	/*@Override
	public int compare(ContainerPath containerPath1, ContainerPath containerPath2) {
		if (containerPath1==null && containerPath2!=null) {
			return -1;
		} else if (containerPath1!=null && containerPath2==null) {
			return 1;
		}
		int compare = CompareUtils.compareNull(containerPath1.pageContainer, containerPath2.pageContainer);
		if (compare!=0) {
			return compare;
		}
		compare = CompareUtils.compareNull(containerPath1.groupContainer, containerPath2.groupContainer);
		if (compare!=0) {
			return compare;
		}
		return CompareUtils.compareNull(containerPath1.collectionContainer, containerPath2.collectionContainer);
	}

	@Override
	public int compareTo(ContainerPath containerPath) {
		return compare(this, containerPath);
	}*/

}
