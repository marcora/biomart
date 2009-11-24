package org.biomart.common.general.utils;

public enum Trilean {
	PLUS, ZERO, MINUS;
	public int toInteger () {
		if (this==PLUS) {
			return 1;
		} else if (this==MINUS) {
			return -1;
		} else {
			return 0;
		}
	}
	public static Trilean toTrilean (int integer) {
		if (integer==1) {
			return PLUS;
		} else if (integer==-1) {
			return MINUS;
		} else if (integer==0) {
			return ZERO;
		} else {
			return null;
		}
	}
	public boolean isPlus() {
		return this.equals(PLUS);
	}
	public boolean isMinus() {
		return this.equals(MINUS);
	}
	public boolean isZero() {
		return this.equals(ZERO);
	}
	public boolean isNotMinus() {
		return this.equals(ZERO) || this.equals(PLUS);
	}
	public boolean isNotPlus() {
		return this.equals(ZERO) || this.equals(MINUS);
	}
	
	/**
	 * Because valueOf can't seem to work and I am not allowed to implement it again... (TODO: why?)
	 * @param trilean
	 * @return
	 */
	/*public static Trilean valueOf2(String trilean) {
		if (null==trilean) {
			return null;
		} else if (trilean.equals(PLUS.name())) {
			return PLUS;
		} else if (trilean.equals(MINUS.name())) {
			return MINUS;
		} else if (trilean.equals(ZERO.name())) {
			return ZERO;
		} else {
			return null;
		}
	}*/
}
