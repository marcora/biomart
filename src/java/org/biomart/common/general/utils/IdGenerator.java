package org.biomart.common.general.utils;

public class IdGenerator {
	private static int id = 0;
	private static int toSkip = 0;
	public static void initialize() {
		id=0;
		toSkip=0;
	}
	public static int getNextID() {
		if (toSkip!=0) {
			skip();
		}
		return id++;
	}
	public static void addToSkip() {
		toSkip++;
	}
	public static void skip() {
		id+=toSkip;
		toSkip=0;
	}
}
