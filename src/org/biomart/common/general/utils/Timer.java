package org.biomart.common.general.utils;


import java.io.Serializable;
import java.util.GregorianCalendar;

import org.biomart.common.general.exceptions.FunctionalException;


public class Timer implements Serializable {
	
	private static final long serialVersionUID = -8446683008489107955L;
	
	private String comment = null;
	private Long startTime = null;
	private Long stopTime = null;
	private Long timeEllapsedMs = null;
	private Double timeEllapsedMin = null;
	private String start = null;
	private String stop = null;
	private boolean started = false;
	private boolean stopped = false;
	
	public Timer() {
		super();
	}
	public Timer(String comment) {
		this.comment = comment;
	}
	public String getComment() {
		return comment;
	}
	public String getStart() {
		return start;
	}
	public Long getStartTime() {
		return startTime;
	}
	public String getStop() {
		return stop;
	}
	public Long getStopTime() {
		return stopTime;
	}
	public Double getTimeEllapsedMin() {
		return timeEllapsedMin;
	}
	public Long getTimeEllapsedMs() {
		return timeEllapsedMs;
	}
	public void startTimer() throws FunctionalException {
		if (started) {
			throw new FunctionalException("already started");
		} else if (stopped) {
			throw new FunctionalException("already stopped");
		}
		this.startTime = new GregorianCalendar().getTimeInMillis();
		this.start = getTimeStamp();
		this.started = true;
	}
	public void stopTimer() throws FunctionalException {
		if (!started) {
			throw new FunctionalException("not started already");
		}
		this.stopTime = new GregorianCalendar().getTimeInMillis();
		this.stop = getTimeStamp();
		this.timeEllapsedMs = this.stopTime-this.startTime;
		this.timeEllapsedMin = ((long)((double)this.timeEllapsedMs/6000))/(double)10;	// To have like X.X
		this.stopped = true;
		this.started = false;
	}
	private String getTimeStamp() {
		return MyUtils.getCurrentDateAsString() + "-" + MyUtils.getCurrentTimeOfDayToMillisecondAsString();
	}
	@Override
	public String toString() {
		return "time ellapsed = " + timeEllapsedMin + " min" +
		" = " + timeEllapsedMs + " ms " + MyUtils.TAB_SEPARATOR +
		"(startTime = " + startTime +
		", stopTime = " + stopTime +
		", start = " + start +
		", stop = " + stop +
		", comment = " + comment + ")";
	}
	public String toStatisticString() {
		return timeEllapsedMs + MyUtils.TAB_SEPARATOR + start + MyUtils.TAB_SEPARATOR + stop;
	}
}
