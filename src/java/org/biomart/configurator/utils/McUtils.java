package org.biomart.configurator.utils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.biomart.common.resources.Resources;

public class McUtils {
	
	public static String getCurrentTimeString() {
		Format formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss  z");
		Date date = new Date();
		return formatter.format(date);
	}
	
	public static String StrListToStr(List<String> list) {
		if(list==null || list.size()==0)
			return null;
		else {
			StringBuffer res = new StringBuffer();
			res.append(list.get(0));
			for(int i=1;i<list.size();i++) {
				res.append(Resources.get("colonseparator"));
				res.append(list.get(i));
			}
			return res.toString();
		}
	}

	public static long getCurrentTime() {
		Calendar cal = new GregorianCalendar();
		return cal.getTimeInMillis();
	}
}