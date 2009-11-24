package org.biomart.test.cancelQueries;

import org.biomart.common.general.utils.MyUtils;

public class ThreadCommunication {
	public static synchronized void pressKey(String string) {
		print(string);
		MyUtils.wrappedReadInput();
	}
	public static synchronized void println(String string) {
		print(string + MyUtils.LINE_SEPARATOR);
	}
	public static synchronized void print(String string) {
		System.out.print("Thread " + Thread.currentThread().getId() + " - " + string);
	}
}
