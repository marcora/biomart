package org.biomart.common.general.utils;

public class WaitableBoolean {
	private Boolean value = null;
	public WaitableBoolean(Boolean value) {
		this.value = value;
	}
	public Boolean getValue() {
		return value;
	}
	public void setTrue() {
		this.value = true;
	}
	public void setFalse() {
		this.value = false;
	}
	public void setNull() {
		this.value = null;
	}
	public void setValue(Boolean value) {
		this.value = value;
	}
}
