package org.biomart.common.general.utils;

public class UniqueId {
	private static String latestTimeStamp = MyUtils.getTimeStamp();
	private static int timeStampCounter = 0;
	private static synchronized void setLatestTimeStamp(String timeStamp) {
		if (!UniqueId.latestTimeStamp.equals(timeStamp)) {
			UniqueId.latestTimeStamp = timeStamp;
		}
	}
	private static synchronized int getTimeStampCounter() {
		return UniqueId.timeStampCounter;
	}
	private static synchronized void updateTimeStampCounter(String timeStamp) {		
		if (timeStamp.equals(latestTimeStamp)) {
			timeStampCounter++;
		} else {
			timeStampCounter=0;
			setLatestTimeStamp(timeStamp);
		}
	}
	public static synchronized String getUniqueID() {
		String timeStamp = MyUtils.getTimeStamp();
		String id = getTimeStampCounter() + timeStamp;
		updateTimeStampCounter(timeStamp);
		return id;
	}
}
