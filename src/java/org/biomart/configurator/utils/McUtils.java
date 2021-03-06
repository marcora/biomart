package org.biomart.configurator.utils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.biomart.common.resources.Resources;

public class McUtils {
	
	//private static Calendar calendar = new GregorianCalendar();
	
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
		Calendar calendar = new GregorianCalendar();
		return calendar.getTimeInMillis();
	}

	public static String getRegValue(String reg, String expression, String input) {
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(input);
		return m.replaceAll(expression);
	}
    /** Returns an ImageIcon, or null if the path was invalid. */
    public static ImageIcon createImageIcon(String path) {
         return new ImageIcon(path);
    }

}