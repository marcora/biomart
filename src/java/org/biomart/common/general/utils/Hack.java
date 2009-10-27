package org.biomart.common.general.utils;

public class Hack {
	public HackEnum hackEnum = null;
	public String[] strings = null;
	public Integer[] integers = null;
	public Double[] doubles = null;
	public Boolean[] booleans = null;
	public Hack subHack = null;
	public Hack(HackEnum hackEnum, String[] strings, Integer[] integers, Double[] doubles, Boolean[] booleans) {
		this(hackEnum, strings, integers, doubles, booleans, null);
	}
	public Hack(HackEnum hackEnum, String[] strings, Integer[] integers, Double[] doubles, Boolean[] booleans, Hack subHack) {
		this.hackEnum = hackEnum;
		this.strings = strings;
		this.integers = integers;
		this.doubles = doubles;
		this.booleans = booleans;
		this.subHack = subHack;
	}
	public static boolean contains(Hack[] hacks, HackEnum hackEnum) {
		for (int i = 0; i < hacks.length; i++) {
			if (hacks[i]!=null && hackEnum.equals(hacks[i].hackEnum)) {
				return true;
			}
		}
		return false;
	}
	public static Integer getIndex(Hack[] hacks, HackEnum hackEnum) {
		for (int i = 0; i < hacks.length; i++) {
			if (hacks[i]!=null && hackEnum.equals(hacks[i].hackEnum)) {
				return i;
			}
		}
		return null;
	}
	public static Hack getHack(Hack[] hacks, HackEnum hackEnum) {
		return hacks[getIndex(hacks, hackEnum)];		
	}                                       
}
