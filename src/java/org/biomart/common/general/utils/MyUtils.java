package org.biomart.common.general.utils;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.martRemote.MartRemoteConstants;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 * Methods copied from my personal library to save time (temporarily)
 * @author anthony
 *
 */
public class MyUtils {

	/* -------------------------------------- Constants -------------------------------------- */

	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String FILE_SEPARATOR_OPPOSITE = "\\".equals(FILE_SEPARATOR) ? "/" : "\\";
	public static final String INFO_SEPARATOR = "_";	
	public static final String TAB_SEPARATOR = "\t";
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	public static final String PARENT_FOLDER_PATH = "/home/anthony/javaIO" + FILE_SEPARATOR;
	public static final String FILES_PATH = PARENT_FOLDER_PATH + "_Files" + FILE_SEPARATOR;
	public static final String INPUT_FILES_PATH = PARENT_FOLDER_PATH + "_InputFiles" + FILE_SEPARATOR;
	public static final String OUTPUT_FILES_PATH = PARENT_FOLDER_PATH + "_OutputFiles" + FILE_SEPARATOR;
	
	public static final String CONSOLE_OUTPUT_REDIRECT = OUTPUT_FILES_PATH + "Console";
	
	public static final String TEXT_FILE_EXTENSION = ".txt";
	public static final String MIDI_FILE_EXTENSION = ".mid";
	
	public static final String DASH_LINE  = "-------------------------------------------------------------------------------------";
	public static final String EQUAL_LINE = "=====================================================================================";
	public static final String STAR_LINE  = "*************************************************************************************";
	public static final String ERROR_LINE = "#####################################################################################";
	
	public static void main(String[] args) {

		
		
		
/*		String commandProcessor = "cmd"; // win NT, XP, 2000
		  // String commandProcessor = "command"; // win 95, 98, Me
		  // String commandProcessor = "sh"; // UNIX

		  String cOption = "/C"; // win
		  // String cOption = "-c"; // UNIX

		  String[] command = {commandProcessor, cOption,
		   "command_name --argumentname=\"value with space\""};

		  rt.exec(command);*/
		  
		  
		/*String[] strings = new String[]{
				"mysql", "-h", CancelQueriesTest.databaseHost, "-P", String.valueOf(CancelQueriesTest.databasePort), "-u", 
				CancelQueriesTest.databaseUser, "-p", CancelQueriesTest.databasePassword, "-e", "'show process;'"
		};*/
	
		
	}
	
	/* -------------------------------------- CheckUtils -------------------------------------- */
//	public static boolean CHECK = true; 
//	//TODO better handling of the difference between program and thread
//	public static void checkStatusProgram ( boolean state, String errorMessage, boolean exit ) {
//		if (CHECK && !state) {
//			errorProgram(errorMessage, exit);
//		}		
//	}
//	public static void checkStatusProgram ( boolean state) {
//		checkStatusProgram(state, "", true);	
//	}
//	/**
//	 * Defaulted to true as it is a program and it is usually critical
//	 * @param state
//	 * @param errorMessage
//	 */
//	public static void checkStatusProgram ( boolean state, String errorMessage) {
//		checkStatusProgram(state, errorMessage, true);		
//	}
//	
//	/* -------------------------------------- ErrorUtils -------------------------------------- */
//	public static void errorProgram () {
//		errorProgram("", true);
//	}
//	public static void errorProgram (String errorMessage) {
//		errorProgram(errorMessage, true);
//	}
//	public static void errorProgram ( String errorMessage, boolean exitProgram ) {
//		System.out.println(ERROR_LINE);
//		message ("ERROR", errorMessage);
//		if (exitProgram) {
//			logBasicMessage(stackTraceToString(new Throwable()));			
//			System.exit(-1);
//		}
//	}
	
	public static boolean CHECK = true;
	public static boolean EXCEPTION = true;
	public static boolean EXIT_PROGRAM = false; 
	//TODO better handling of the difference between program and thread
	public static void checkStatusProgram ( boolean state, boolean exit ) {
		checkStatusProgram(state, "", exit);
	}
	public static void checkStatusProgram ( boolean state, String errorMessage, boolean exit ) {
		if (CHECK && !state) {
			errorProgram(errorMessage, exit);
		}		
	}
	public static void checkStatusProgram ( boolean state) {
		checkStatusProgram(state, "", EXIT_PROGRAM);	
	}
	/**
	 * Defaulted to true as it is a program and it is usually critical
	 * @param state
	 * @param errorMessage
	 */
	public static void checkStatusProgram ( boolean state, String errorMessage) {
		checkStatusProgram(state, errorMessage, EXIT_PROGRAM);		
	}
	
	/* -------------------------------------- ErrorUtils -------------------------------------- */
	public static void errorProgram () {
		errorProgram("", EXIT_PROGRAM);
	}
	public static void errorProgram (String errorMessage) {
		errorProgram(errorMessage, EXIT_PROGRAM);
	}
	public static void errorProgram ( String errorMessage, boolean exitProgram ) {
		if (EXCEPTION) {
			try {
				throw new FunctionalException(errorMessage);	//TODO catch
			} catch (FunctionalException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println(ERROR_LINE);
			message ("ERROR", errorMessage);
		}
		if (exitProgram) {
			if (!EXCEPTION) {
				logBasicMessage(stackTraceToString(new Throwable()));	
			}
			System.exit(-1);
		}
	}
	
	public static String stackTraceToString (Throwable e) {
		
		StringBuilder stringBuilder = new StringBuilder ();
		
		stringBuilder.append(" - [" + 
				e.getClass().getSimpleName() + "] Strack Trace:\n");
		
		StackTraceElement[] stackTrace = e.getStackTrace();
		for (int i = 0; i < stackTrace.length; i++) {
			stringBuilder.append("\t" + stackTrace[i].toString() + "\n");
		}
		Throwable parentException = e.getCause();
		if (parentException != null) {
			stringBuilder.append("Parent Exception: [" + parentException.getClass().getName()
					+ "]: " + parentException.getLocalizedMessage() + "\n");
			
			StackTraceElement[] parentStackTrace = parentException.getStackTrace();
			stringBuilder.append("Parent Exception Strack Trace:\n");
			for (int i = 0; i < parentStackTrace.length; i++) {
				stringBuilder.append("\t" + parentStackTrace[i].toString() + "\n");
			}
		}
		
		return stringBuilder.toString();
	}

	/* -------------------------------------- LogUtils -------------------------------------- */

	private static PrintStream printStream = System.out;
	
	public static void message ( String prefix, String message ) {
		//if (showLog) {
			StringBuilder stringBuilder = new StringBuilder (prefix + ": ");
			
			stringBuilder.append("[");
			/*if (showThreadInfo) {
				stringBuilder.append("(" + Thread.currentThread().getId() + ") " + Thread.currentThread().getName() + " - ");
			}*/
			/*if (showTime) {
				if (showMillisecondsInTime) {
					stringBuilder.append(TimeUtils.getCurrentTimeOfDayToMillisecondAsString());
				} else {
					stringBuilder.append(TimeUtils.getCurrentTimeOfDayAsString());
				}
			}*/
			//if (showCallerMethod) {
				stringBuilder.append(" - " + getCurrentMethodName(3));	// 2 car appel juste avant logMessage
				//}
			stringBuilder.append("] ");
			
			stringBuilder.append(message);
			logBasicMessage(stringBuilder.toString());
			//}
	}
	
	public static void logMessage ( String message ) {
		message ("LOG", message);
	}
	
	public static void logBasicMessage ( String message ) {
		logBasicMessageNoNewLine(message + LINE_SEPARATOR);
	}
	public static void logBasicMessageNoNewLine ( String message ) {
		printStream.print(message);
	}

	/* -------------------------------------- IntrospectionUtils -------------------------------------- */

	/**
	 * http://www.developpez.net/forums/showthread.php?t=416395
	 * @return
	 */
	public static String getCurrentMethodName () {
		return getCurrentMethodName(2);	// 2 because TODO explication
	}
	
	public static String getCurrentMethodName (int depth) {
		Throwable throwable = new Throwable();
		throwable.fillInStackTrace();
	    StackTraceElement e = throwable.getStackTrace()[depth];	// 1 because appel juste avant getCurrentMethodName
		return e.getClassName() + "." + e.getMethodName() + "()";
	}

	/* -------------------------------------- WriteFileClass -------------------------------------- */
	public static class WriteFileClass {
		
		public String DEFAULT_DEBUG_FILE = "./debug.txt";
		public void writeFile (String filePath, String s) {		
			
			createParentFolders(filePath);
			
			try {
				File file = new File (filePath);
				FileWriter fileWriter = new FileWriter (file);		
				BufferedWriter bufferedWriter = new BufferedWriter (fileWriter);
				bufferedWriter.write(s);
				bufferedWriter.close();
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Debug file written ok at: \"" + filePath + "\"");
		}
		/**
		 * Appels:
		 * new WriteFileClass ().writeFile("coucou");
		 * OU
		 * new WriteFileClass ().writeFile("C:/debug.txt", "coucou2"); 
		 * @param s
		 */
		public void writeFile (String s) {
			writeFile(DEFAULT_DEBUG_FILE, s);
		}
	}
	
	/* -------------------------------------- FileUtils -------------------------------------- */

	public static void createParentFolders (String filePath) {
		createParentFolders(filePath, true);
	}
	
	public static void createParentFolders (String filePath, boolean display) {
		
		if (!filePath.contains(FILE_SEPARATOR) && 
				!filePath.contains (FILE_SEPARATOR_OPPOSITE)) {
			System.out.println("No parent folder specified" );//DO NOT USE logUtils here!! cycle...	
			return;
		}
		
		String parentFoldersPath = null;
		int fileSeparatorIndex = filePath.lastIndexOf(FILE_SEPARATOR);
		int fileSeparatorOppositeIndex = filePath.lastIndexOf(FILE_SEPARATOR_OPPOSITE);
		
		if (-1==fileSeparatorIndex && -1==fileSeparatorOppositeIndex) {
			//TODO
			System.out.println("ERROR - -1==separator1 && -1==separator2");
		}
		
		int separatorIndex = fileSeparatorIndex!=-1 ? fileSeparatorIndex : fileSeparatorOppositeIndex;
		parentFoldersPath = filePath.substring(0, separatorIndex+1);
		
		File parentFolder = new File (parentFoldersPath);
		if (!parentFolder.exists()) {
			parentFolder.mkdirs();
			if (display) {
				System.out.println("Parent folder created: " + parentFoldersPath);//DO NOT USE logUtils here!! cycle...
			}
		} else {
			if (display) {
				System.out.println("Parent folder already exists" );//DO NOT USE logUtils here!! cycle...
			}
		}
	}
	
	/* -------------------------------------- Serialization Utils -------------------------------------- */
	
	public static void writeSerializedObject ( Object object, String fileName ) throws TechnicalException {

		// Write serialization
		try {
			// Write to disk with FileOutputStream
			FileOutputStream f_out = new FileOutputStream( fileName );

			// Write object with ObjectOutputStream
			ObjectOutputStream obj_out = new ObjectOutputStream (f_out);

			// Write object out to disk
			obj_out.writeObject ( object );
			
			obj_out.close();
			f_out.close();
		} catch (FileNotFoundException e) {
			throw new TechnicalException (e.getClass() + ", " + e.getMessage());
		} catch (IOException e) {
			throw new TechnicalException (e.getClass() + ", " + e.getMessage());
		}
	}

	public static Object readSerializedObject (URL url) throws TechnicalException {
		InputStream inputStream = null;
		try {
			// Read from url
			inputStream = url.openStream();
		} catch (IOException e) {
			throw new TechnicalException (e.getClass() + ", " + e.getMessage());
		}
		return readSerializedObject(inputStream);
	}
	public static Object readSerializedObject ( String fileName ) throws TechnicalException {
		FileInputStream f_in = null;
		try {
			// Read from disk using FileInputStream
			f_in = new FileInputStream( fileName );
		} catch (FileNotFoundException e) {
			throw new TechnicalException (e.getClass() + ", " + e.getMessage());
		}
		return readSerializedObject(f_in);
	}
	public static Object readSerializedObject (InputStream f_in) throws TechnicalException {

		// Read an object
		Object object = null;
		try {
			// Read object using ObjectInputStream
			ObjectInputStream obj_in = new ObjectInputStream (f_in);

			object = obj_in.readObject();
			
			obj_in.close();
			f_in.close();
		} catch (FileNotFoundException e) {
			throw new TechnicalException (e.getClass() + ", " + e.getMessage());
		} catch (IOException e) {
			throw new TechnicalException (e.getClass() + ", " + e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new TechnicalException (e.getClass() + ", " + e.getMessage());
		}

		return object;
	}
	
	/* -------------------------------------- Time Utils -------------------------------------- */
	public static String getCurrentDateAsString () {
		Calendar calendar = new GregorianCalendar();
		
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;	// +1 to shift from 0-11 to 1-12
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		return year + (month<10 ? "0" : "") + month + (day<10 ? "0" : "") + day;		
	}
	
	public static String getCurrentTimeOfDayToMillisecondAsString () {
		Calendar calendar = new GregorianCalendar();
		
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		int millisecond = calendar.get(Calendar.MILLISECOND);
		return (hour<10 ? "0" : "") + hour + (minute<10 ? "0" : "") + 
		minute + (second<10 ? "0" : "") + second + 
		(millisecond<10 ? "00" : (millisecond<100 ? "0" : "")) + millisecond;
	}
	
	public static String getDateAndTime() {
		return getCurrentDateAsString() + INFO_SEPARATOR + 
		getCurrentTimeOfDayToMillisecondAsString();
	}
	
	/* --------------------------------------  -------------------------------------- */
	/**
	 * n has to be like 1[00000]
	 * @param n
	 * @return
	 */
	public static String getTenToThe(int n) {
		return "10^" + (String.valueOf(n).length()-1);
	}
	
	private static int READ_INPUT_BUFFER_SIZE = 1000;

	public static void pressKeyToContinue() {
		try {
			readInput ("Press key to continue...");	//TODO actually make it just one key (not +Enter)
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String wrappedReadInput () {	
		String s = null;
		try {
			s = readInput ("");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
	
	public static String readInput () throws Exception {
		return readInput ("");
	}
		
	public static String readInput (String description) throws Exception {
		byte [] bytes = new byte [READ_INPUT_BUFFER_SIZE];	
		int length;
		try {
			length = System.in.read(bytes);
		} catch (IOException e) {
			throw new Exception ("readInput");
		}
		return new String (bytes, 0, length-1);	// -2 for the \n		
	}
	
	/* -------------------------------------- New ones -------------------------------------- */

	public static String getTimeStamp() {
		return getCurrentDateAsString() + getCurrentTimeOfDayToMillisecondAsString();
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * time saver but not very clean
	 * @param fileName
	 * @return
	 */
	public static void writePackageLocalFile (Class clazz, String fileName, String content){	//TODO nicer
		new WriteFileClass().writeFile(getPath(clazz, fileName), content);
	}
	/*public static String readPackageLocalFile (Class clazz, String fileName){	//TODO nicer
		return new ReadFileBaseImpl().read(getPath(clazz, fileName));
	}*/
	@SuppressWarnings("unchecked")
	private static String getPath(Class clazz, String fileName) {
		return "."+ FILE_SEPARATOR + "src" + FILE_SEPARATOR + 
		(clazz.getPackage()!=null ? clazz.getPackage().toString().replace("package ", "").replace(".", "/") + FILE_SEPARATOR : "") + 
		fileName;
	}
	
	public static StringBuffer stringBufferReplace (StringBuffer stringBuffer, String string1, String string2) {
		Integer index = null; 
		while ((index = stringBuffer.indexOf(string1))!=-1) {
			stringBuffer = stringBuffer.replace(index, index+string1.length(), string2);			
		}
		return stringBuffer;
	}
	
	public static StringBuffer arrayToStringBuffer(Object[] objectTab) {
		return arrayToStringBuffer(objectTab, ", ");
	}
	public static StringBuffer arrayToStringBuffer(Object[] objectTab, String separator) {
		StringBuffer stringBuffer = new StringBuffer();
		if (objectTab!=null) {
			for (int i = 0; i < objectTab.length; i++) {
				stringBuffer.append(objectTab[i].toString() + (i==objectTab.length-1 ? "" : separator));
			}
		}
		return stringBuffer;
	}
	
	public static boolean contains(Object[] objectTab, Object object) {
		for (int i = 0; i < objectTab.length; i++) {
			if (objectTab[i].equals(object)) {
				return true;
			}
		}
		return false;
	}
	
	private static PrintStream out = System.out;
	private static Trilean outputType = Trilean.PLUS;
	private static BufferedWriter fileWriter = null;
	public static void closeConsoleOutput() {
		if (null!=fileWriter) {
			try {
				MyUtils.fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void alterConsoleOutput() {
		alterConsoleOutput(CONSOLE_OUTPUT_REDIRECT, Trilean.ZERO);
	}
	public static void alterConsoleOutput(String filePathAndName, Trilean outputType) {
		MyUtils.outputType = outputType;
		try {
			MyUtils.fileWriter = new BufferedWriter(new FileWriter(filePathAndName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void println(Object object) {
		println(null!=object ? object.toString() : null);
	}
	public static void println() {
		println("");
	}
	public static void println(String message) {
		print(message + LINE_SEPARATOR);
	}
	public static void print(Object object) {
		print(null!=object ? object.toString() : null);
	}
	public static void print() {
		print("");
	}
	/**
	 * eclipse template:
	 * outln
	 * Use console
	 * MyUtils.println();
	 * @param message
	 */
	public static void print(String message) {
		if (Trilean.ZERO.equals(outputType) || Trilean.PLUS.equals(outputType)) {
			MyUtils.out.print(message);
		} 
		if (Trilean.ZERO.equals(outputType) || Trilean.MINUS.equals(outputType)) {
			try {
				MyUtils.fileWriter.write(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static StringBuffer copyUrlContentToStringBuffer(URL url) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuffer stringBuffer = new StringBuffer();
		String line = null;
		while ((line = in.readLine()) != null) {
			stringBuffer.append(line + MyUtils.LINE_SEPARATOR);
		}
		in.close();
		return stringBuffer;
	}
	
	public static List<List<String>> copyUrlContentToListStringList(URL url, String separator) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		List<List<String>> listList = new ArrayList<List<String>>();
		String line = null;
		while ((line = in.readLine()) != null) {
			if (!MyUtils.isEmpty(line)) {
				List<String> list = new ArrayList<String>(Arrays.asList(line.split(separator)));
				listList.add(list);
			}
		}
		in.close();
		return listList;
	}
	
	public static Set<List<String>> copyUrlContentToTreeSetStringList(URL url, String separator) throws IOException {
		return copyUrlContentToSetStringList(url, separator, true);
	}
	public static Set<List<String>> copyUrlContentToHashSetStringList(URL url, String separator) throws IOException {
		return copyUrlContentToSetStringList(url, separator, false);
	}
	private static Set<List<String>> copyUrlContentToSetStringList(URL url, String separator, boolean tree) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		Set<List<String>> setList = !tree ? 
				new HashSet<List<String>>() : 
				new TreeSet<List<String>>(new Comparator<List<String>>() {
					public int compare(List<String> list1, List<String> list2) {	// Assuming not null and same size already
				        int compare = 0;
				    	for (int i = 0; i < list1.size(); i++) {
				        	if ((compare=list1.get(i).compareTo(list2.get(i)))!=0) {
				        		return compare;
				        	}
						}
				        return 0;
					}
				});
		String line = null;
		while ((line = in.readLine()) != null) {
			if (!MyUtils.isEmpty(line)) {
				List<String> list = new ArrayList<String>(Arrays.asList(line.split(separator)));
				setList.add(list);
			}
		}
		in.close();
		return setList;
	}
	
	public static List<String> copyUrlContentToStringList(URL url) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		List<String> list = new ArrayList<String>();
		String line = null;
		while ((line = in.readLine()) != null) {
			list.add(line);
		}
		in.close();
		return list;
	}
	
	public static List<List<String>> getDataFromFile(File file, String separator) throws IOException {
		return getDataFromFile(file, separator, null, null);
	}
	public static List<List<String>> getDataFromFile(File file, String separator, Integer limitStart, Integer limitSize) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		List<List<String>> listList = new ArrayList<List<String>>();
		String line = null;
		int rowCount = 0;
		Integer limitEnd = (limitStart==null && limitSize==null) ? null : limitStart+limitSize;
		while ((line = in.readLine()) != null) {
			if (!MyUtils.isEmpty(line)) {
				if (limitEnd!=null && rowCount>=limitEnd) {
					break;
				}
				if ((limitStart==null && limitSize==null) || (limitStart!=null && rowCount>=limitStart)) {
					List<String> list = new ArrayList<String>(Arrays.asList(line.split(separator)));
					listList.add(list);
				}
				rowCount++;
			}
		}
		in.close();
		return listList;
	}

	public static TreeSet<String> getDataFromFileToTreeSet(File file, String separator) throws IOException {
		FileReader fileReader = new FileReader(file);
		BufferedReader in = new BufferedReader(fileReader);
		TreeSet<String> treeSetList = new TreeSet<String>();
		String line = null;
		while ((line = in.readLine()) != null) {
			if (!MyUtils.isEmpty(line)) {
				treeSetList.add(line);
			}
		}
		in.close();
		fileReader.close();
		return treeSetList;
	}

	public static void writeCollectionToFile(Collection<?> collection, File file) throws IOException {
		BufferedWriter in = new BufferedWriter(new FileWriter(file));
		for (Object object : collection) {
			in.write(object.toString() + MyUtils.LINE_SEPARATOR);
		}
		in.close();
	}
	
	public static void showProgress(int size) {
		System.out.println();
		System.out.println();
        for (int i = 0; i < size; i++) {
        	System.out.print("=");
        }
        System.out.println();
	}
	
	public static String readFile(String filePathAndName) {
		return readFile(new File(filePathAndName));
	}	
	public static String readFile(File file) {
		return readFileStringBuffer(file).toString();
	}
	public static StringBuffer readFileStringBuffer(String filePathAndName) {
		return readFileStringBuffer(new File(filePathAndName));
	}	
	public static StringBuffer readFileStringBuffer(File file) {
		StringBuffer stringBuffer = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			stringBuffer = new StringBuffer();
			String line = null;
			while ((line=br.readLine())!=null) {
				stringBuffer.append(line + LINE_SEPARATOR);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringBuffer;
	}
	
	public static void writeFile(String filePathAndName, String content) {
		
		int index = filePathAndName.lastIndexOf(MyUtils.FILE_SEPARATOR);
		String folderPath = filePathAndName.substring(0, index);
		File folder = new File(folderPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(new File(filePathAndName)));
			br.write(content);
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static StringBuffer runCommand(String command) throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
		StringBuffer input = readStream(process.getInputStream());  
		StringBuffer error = readStream(process.getErrorStream());
		return process.exitValue()!=0 ? error : input;
	}
	public static StringBuffer runShCommand(String command) throws IOException, InterruptedException {
		return runCommand(new String[] {"sh", "-c", command});
	}
	public static StringBuffer runCommand(String[] commands) throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec(commands);
		process.waitFor();
		StringBuffer input = readStream(process.getInputStream());  
		StringBuffer error = readStream(process.getErrorStream());
		return process.exitValue()!=0 ? error : input;
	}
	private static StringBuffer readStream(InputStream inputStream) throws IOException {
		StringBuffer stringBuffer = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String line = null;
		while ((line = br.readLine()) != null) {
			stringBuffer.append(line).append(MyUtils.LINE_SEPARATOR);
		}
		return stringBuffer;
	}

	public static void debugLine() {
		System.out.println(MyUtils.DASH_LINE);
	}
	
	public static void pressKey(String string) {
		System.out.print(string);
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*public static void error() {
		System.out.println("ERROR");
		System.exit(-1);
	}*/
	
	public static String firstLetterToUpperCase(String string) {
		return string!=null && string.length()>0 ? string.substring(0, 1).toUpperCase() + string.substring(1) : string;
	}
	public static String firstLetterToLowerCase(String string) {
		return string!=null && string.length()>0 ? string.substring(0, 1).toLowerCase() + string.substring(1) : string;
	}

	public static void writeXmlFile(Element root) throws TechnicalException {
		writeXmlFile(root, null);
	}
	public static Document writeXmlFile(Element root, String outputXmlFilePathAndName) throws TechnicalException {
		Document newDocument = new Document(root);
		writeXmlFile(newDocument, outputXmlFilePathAndName);
		return newDocument;
	}
	public static void writeXmlFile(Document document, String outputXmlFilePathAndName) throws TechnicalException {
		try {
			XMLOutputter fmt = new XMLOutputter(Format.getPrettyFormat());
			if (null!=outputXmlFilePathAndName) {
				FileOutputStream fos = new FileOutputStream(outputXmlFilePathAndName);
				fmt.output(document, fos);
			} else {
				fmt.output(document, System.out);
			}
		} catch (FileNotFoundException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
	}
	
	public static Document loadDocument(String stringUrl) throws TechnicalException {
		URL url = getUrlFromString(stringUrl);
		return loadDocument(url);
	}
	public static Document loadDocument(String stringUrl, SAXBuilder builder) throws TechnicalException {
		URL url = getUrlFromString(stringUrl);
		return loadDocument(url, builder);
	}
	public static Document loadDocument(URL url) throws TechnicalException {
		SAXBuilder builder = new SAXBuilder();
		return loadDocument(url, builder);
	}
	private static URL getUrlFromString(String stringUrl) throws TechnicalException {
		URL url = null;
		try {
			url = new URL(stringUrl);
		} catch (MalformedURLException e) {
			throw new TechnicalException(e);
		}
		return url;
	}
	public static Document loadDocument(URL url, SAXBuilder builder) throws TechnicalException {
		Document document = null;
		try {
			document = builder.build(url);
		} catch (JDOMException e) {
			throw new TechnicalException(e);
		} catch (IOException e) {
			throw new TechnicalException(e);
		}
		return document;
	}

	public static boolean nullOrEmpty(List<?> oldSpecificFilterContentList) {
		return oldSpecificFilterContentList==null || oldSpecificFilterContentList.isEmpty();
	}
	public static boolean isEmpty(String string) {
		return string.length()==0;	// so it throws NullPointerException if string is null (just like string.isEmpty() would)
	}
	public static String randomString() {
		return randomString(5);
	}
	public static String randomString(int length) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int offset = (int)(Math.random()*26);
			char c = (char)((int)(Math.random()<0.5 ? 'a' : 'A') + offset);
			stringBuffer.append(c);
		}
		return stringBuffer.toString();
	}
	
	/**
	 * Split that accounts for empty strings in any places (including at the beginning or at the end, String.split() doesn't)
	 * @param s
	 * @param c
	 * @return
	 */
	public static String[] split(String s, char c) {
		char[] charArray = s.toCharArray();
		List<Integer> indexList = new ArrayList<Integer>();
		for (int i = 0; i < charArray.length; i++) {
			if (charArray[i]==c) {
				indexList.add(i);
			}
		}
		String[] split = new String[indexList.size()+1];
		for (int i = 0; i < indexList.size()+1; i++) {
			split[i] = s.substring((i==0) ? 0 : indexList.get(i-1)+1, (i==indexList.size() ? s.length() : indexList.get(i)));
		}
		return split;
	}
	
	public static String trimHost(String host) {
		String trimmedHost = null;
		
		// Remove protocol
		trimmedHost = host.startsWith(MyConstants.HTTP_PROTOCOL) ?
				host.substring(MyConstants.HTTP_PROTOCOL.length()) : host;
		
		// Remove port
		trimmedHost = trimmedHost.split(":")[0];
		
		// Remove any extra /
		while (trimmedHost.endsWith("/")) {
			trimmedHost = trimmedHost.substring(0, trimmedHost.length()-1);
		}
		
		return trimmedHost;
	}
	public static String getProperty(String propertyName) throws FileNotFoundException, IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(MartRemoteConstants.QUERY_TEST_PROPERTIES_FILE_PATH_AND_NAME));
		return properties.getProperty(propertyName);
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * To debug a get
	 */
	public static Object mapGet(Map map, Object object) {
		Iterator it = map.keySet().iterator();
		Iterator it2 = map.values().iterator();
		while (it.hasNext()) {
			Object object1 = it.next();
			Object object2 = it2.next();
			if (object1.equals(object)) {
				return object2;
			}
		}
		return null;
	}
	
	public static boolean anthony() {	// TODO remove: to help debug
		return new File(".").getAbsolutePath().startsWith("/home/anthony");
	}
}
