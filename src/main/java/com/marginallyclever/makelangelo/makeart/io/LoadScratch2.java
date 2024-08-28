package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * LoadAndSaveSB2 loads limited set of Scratch commands into memory. 
 * @author Admin
 */
public class LoadScratch2 implements TurtleLoader {
	private static final Logger logger = LoggerFactory.getLogger(LoadScratch2.class);
	
	private final String PROJECT_JSON = "project.json";
	
	private static class Scratch2Variable {
		public String name;
		public double value;

		public Scratch2Variable(String arg0,float arg1) {
			name=arg0;
			value=arg1;
		}
	};
	
	private static class Scratch2List {
		public String name;
		public ArrayList<Double> contents;

		public Scratch2List(String _name) {
			name=_name;
			contents=new ArrayList<Double>();
		}
	};
	
	private static final FileNameExtensionFilter filter = new FileNameExtensionFilter("Scratch 2","SB2");
	private Turtle turtle;
	private LinkedList<Scratch2Variable> scratchVariables;
	private LinkedList<Scratch2List> scratchLists;
	private Map<String,JSONArray> subRoutines = new HashMap<String,JSONArray>();
	private int indent;
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad(String filename) {
		String filenameExtension = filename.substring(filename.lastIndexOf('.')).toUpperCase();;
		return filenameExtension.equalsIgnoreCase(".SB2");
	}
	
	@Override
	public Turtle load(InputStream in) throws Exception {
		logger.debug("Loading...");

		turtle = new Turtle();
		indent=0;
		
		JSONObject tree = getTreeFromInputStream(in);
		readScratchVariables(tree);
		readScratchLists(tree);

		// read the sketch(es)
		// look for the first child with a script
		JSONArray scripts = getScripts(tree);
		logger.debug("found {} top level event scripts", scripts.length());

		readAllSubroutines(scripts);
		findAndRunFromGreenFlag(scripts);
		
		logger.debug("finished scripts");

		return turtle;
	}

	private void findAndRunFromGreenFlag(JSONArray scripts) throws Exception {
		// extract known elements and convert them to turtle commands.
		Iterator<?> scriptIter = scripts.iterator();
		// find the first script with a green flag.
		while( scriptIter.hasNext() ) {
			JSONArray scripts0 = (JSONArray)scriptIter.next();
			if( scripts0==null ) continue;
			JSONArray scripts02 = (JSONArray)scripts0.get(2);
			if( scripts02==null || scripts02.length()==0 ) continue;
			// look inside the script at the first block, which gives the type.
			Object scriptContents = scripts02.get(0);
			if( ( scriptContents instanceof JSONArray ) ) {
				Object scriptHead = ((JSONArray)scriptContents).get(0);
				
    			String firstType = (String)scriptHead;
				if(firstType.equals("whenGreenFlag")) {
					logger.debug("Found green flag script");
					parseScratchCode(scripts02);
				}
			}
		}
	}

	private void readAllSubroutines(JSONArray scripts) {
		// extract known elements and convert them to turtle commands.
		Iterator<?> scriptIter = scripts.iterator();
		
		while( scriptIter.hasNext() ) {
			JSONArray scripts0 = (JSONArray)scriptIter.next();
			if( scripts0==null ) continue;
			JSONArray scripts02 = (JSONArray)scripts0.get(2);
			if( scripts02==null || scripts02.length()==0 ) continue;
			// look inside the script at the first block, which gives the type.
			Object scriptContents = scripts02.get(0);
			if( ( scriptContents instanceof JSONArray ) ) {
				String firstType = ((JSONArray)scriptContents).getString(0);
				if(firstType.equals("whenIReceive")) {
	    			String firstName = (String)((JSONArray)scriptContents).get(1);
					logger.debug("Found subroutine {}", firstName);
	    			subRoutines.put(firstName, ((JSONArray)scriptContents));
				}
			}
		}
	}

	private JSONArray getScripts(JSONObject tree) throws Exception {
		Iterator<?> childIter = tree.getJSONArray("children").iterator();
		while( childIter.hasNext() ) {
			JSONObject child = (JSONObject)childIter.next();
			if(child.has("scripts")) {
				return (JSONArray)child.get("scripts");
			}
		}
		throw new Exception("JSON node 'scripts' missing.");
	}

	private JSONObject getTreeFromInputStream(InputStream in) throws FileNotFoundException, IOException {
		File tempZipFile = extractProjectJSON(in);
		
        logger.debug("Parsing JSON file...");
        JSONTokener tokener = new JSONTokener(tempZipFile.toURI().toURL().openStream());
        JSONObject tree = new JSONObject(tokener);

		tempZipFile.delete();
		
		return tree;
	}

	private File extractProjectJSON(InputStream in) throws FileNotFoundException, IOException {
		logger.debug("Searching for project.json...");
		try (ZipInputStream zipInputStream = new ZipInputStream(in)) {
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				if (entry.getName().equals(PROJECT_JSON)) {
					logger.debug("Found.");

					// read buffered stream into temp file.
					File tempZipFile = File.createTempFile("project", "json");
					tempZipFile.setReadable(true);
					tempZipFile.setWritable(true);
					tempZipFile.deleteOnExit();
					try (FileOutputStream fos = new FileOutputStream(tempZipFile)) {
						byte[] buffer = new byte[2048];
						int len;
						while ((len = zipInputStream.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
						return tempZipFile;
					}
				}
			}
		}
		throw new FileNotFoundException("SB2 missing project.json");
	}

	/**
	 * read the list of Scratch variables
	 * @param tree the JSONObject tree read from the project.json/zip file.
	 * @throws Exception
	 */
	private void readScratchVariables(JSONObject tree) throws Exception {
		scratchVariables = new LinkedList<Scratch2Variable>();
		JSONArray variables = (JSONArray)tree.get("variables");
		// A scratch file without variables would crash before this test
		if (variables != null) {
			Iterator<?> varIter = variables.iterator();
			while( varIter.hasNext() ) {
				//logger.debug("var:"+elem.toString());
				JSONObject elem = (JSONObject)varIter.next();
				String varName = elem.getString("name");
				Object varValue = elem.get("value");
				float value;
				if(varValue instanceof Number) {
					Number num = (Number)varValue;
					value = (float)num.doubleValue();
					scratchVariables.add(new Scratch2Variable(varName, value));
				} else if(varValue instanceof String) {
					try {
						value = Float.parseFloat((String)varValue);
	    				scratchVariables.add(new Scratch2Variable(varName, value));
					} catch (Exception e) {
						throw new Exception("Variables must be numbers.", e);
					}
				} else throw new Exception("Variable "+varName+" is "+varValue.toString());
			}
		}
	}

	/**
	 * read the list of Scratch lists
	 * @param tree the JSONObject tree read from the project.json/zip file.
	 * @throws Exception
	 */
	private void readScratchLists(JSONObject tree) throws Exception {
		scratchLists = new LinkedList<Scratch2List>();
		JSONArray listOfLists = (JSONArray)tree.get("lists");
		if(listOfLists == null) return;
		logger.debug("Found {} lists.", listOfLists.length());
		Iterator<?> listIter = listOfLists.iterator();
		while( listIter.hasNext() ) {
			//logger.debug("var:"+elem.toString());
			JSONObject elem = (JSONObject)listIter.next();
			String listName = (String)elem.get("listName");
			logger.debug("  Found list {}", listName);
			Object contents = elem.get("contents");
			Scratch2List list = new Scratch2List(listName);
			// fill the list with any given contents
			if( contents != null && contents instanceof JSONArray ) {
				JSONArray arr = (JSONArray)contents;

				Iterator<?> scriptIter = arr.iterator();
				while(scriptIter.hasNext()) {
					Object varValue = scriptIter.next();
					double value;
					if(varValue instanceof Number) {
						Number num = (Number)varValue;
						value = num.doubleValue();
						list.contents.add(value);
					} else if(varValue instanceof String) {
						try {
							value = Float.parseFloat((String)varValue);
							list.contents.add(value);
						} catch (Exception e) {
							throw new Exception("List variables must be numbers.", e);
						}
					} else throw new Exception("List variable "+listName+"("+list.contents.size()+") is "+varValue.toString());
				}
			}
			// add the list to the list-of-lists.
			scratchLists.add(list);
		}
	}
	
	private int getListByID(Object obj) throws Exception {
		if(!(obj instanceof String)) throw new Exception("List name not a string.");
		String listName = obj.toString();
		ListIterator<Scratch2List> iter = scratchLists.listIterator();
		int index=0;
		while(iter.hasNext()) {
			Scratch2List i = iter.next();
			if(i.name.equals(listName)) return index;
			++index;
		}
		throw new Exception("List '"+listName+"' not found.");
	}
	
	private String getIndent() {
		String str="";
		for(int j=0;j<indent;++j) str+="  ";
		return str;
	}
	
	/**
	 * read the elements of a JSON array describing Scratch code and parse it into turtle commands.
	 * @param script valid JSONArray of Scratch commands.
	 * @throws Exception
	 */
	private void parseScratchCode(JSONArray script) throws Exception {
		if(script==null) return;
		
		//logger.debug(getIndent()+"{");  //"[size="+script.size()+"]"
		indent++;
		
		Iterator<?> scriptIter = script.iterator();
		// Find the script with the green flag.  Assumes only one green flag per script.
		while( scriptIter.hasNext() ) {
			Object o = scriptIter.next();
			if( o instanceof JSONArray ) {
				JSONArray arr = (JSONArray)o;
				parseScratchCode(arr);
				continue;
			}
			String name = o.toString();
			//logger.debug(getIndent()+name);
			switch(name) {
			case "whenIReceive"->		scriptIter.next();
			case "doBroadcastAndWait"-> doBroadcastAndWait(scriptIter);
			case "doRepeat"-> 			doRepeat(scriptIter);
			case "doUntil"-> 			doUntil(scriptIter);
			case "doIf"-> 				doIf(scriptIter);
			case "doIfElse"-> 			doIfElse(scriptIter);
			case "append:toList:"-> 	doAppend(scriptIter);
			case "deleteLine:ofList:"-> doDeleteLine(scriptIter);
			case "insert:at:ofList:"-> 	doInsertLine(scriptIter);
			case "setLine:ofList:to:"-> doSetLine(scriptIter);
			case "wait:elapsed:from:"-> doWait(scriptIter);
			case "putPenUp"-> 			turtle.penUp();
			case "putPenDown"-> 		turtle.penDown();
			case "gotoX:y:"->			doGotoXY(scriptIter);
			case "changeXposBy:"-> 		doChangeX(scriptIter);
			case "changeYposBy:"-> 		doChangeY(scriptIter);
			case "forward:"-> 			doForward(scriptIter);
			case "turnRight:"-> 		doTurnRight(scriptIter);
			case "turnLeft:"-> 			doTurnLeft(scriptIter);
			case "xpos:"-> 				doSetX(scriptIter);
			case "ypos:"-> 				doSetY(scriptIter);
			case "heading:"-> 			doSetHeading(scriptIter);
			case "setVar:to:"-> 		doSetVar(scriptIter);
			case "changeVar:by:"-> 		doChangeVar(scriptIter);
			default->					logger.debug("Ignored Scratch block "+name);
			}
		}
		indent--;
		//logger.debug(getIndent()+"}");
	}

	// relative change
	private void doChangeVar(Iterator<?> scriptIter) throws Exception {
		String varName = (String)scriptIter.next();
		float v = (float)resolveValue(scriptIter.next());

		for(Scratch2Variable sv : scratchVariables) {
			if(sv.name.equals(varName)) {
				sv.value += v;
				//logger.debug(getIndent()+"Change "+varName+" by "+v+" to "+sv.value);
			}
		}
		throw new Exception("Variable '"+varName+"' not found.");
	}

	// absolute change
	private void doSetVar(Iterator<?> scriptIter) throws Exception {
		String varName = (String)scriptIter.next();
		float v = (float)resolveValue(scriptIter.next());

		for(Scratch2Variable sv : scratchVariables) {
			if(sv.name.equals(varName)) {
				sv.value = v;
				//logger.debug(getIndent()+"Set "+varName+" to "+v);
				return;
			}
		}
		throw new Exception("Variable '"+varName+"' not found.");
	}

	private void doSetHeading(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		double degrees = resolveValue(o2);
		turtle.setAngle(degrees);
		//logger.debug(getIndent()+"Turn to "+degrees);
	}

	private void doSetY(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		double v = resolveValue(o2);
		turtle.moveTo(turtle.getX(),v);
		logger.debug("{} Move to ({},{})", getIndent(), turtle.getX(), turtle.getY());
	}

	private void doSetX(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		double v = resolveValue(o2);
		turtle.moveTo(v,turtle.getY());
		logger.debug("{} Move to ({},{})", getIndent(), turtle.getX(), turtle.getY());
	}

	private void doTurnLeft(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		double degrees = resolveValue(o2);
		turtle.turn(degrees);
		logger.debug("{} Left {} degrees.", getIndent(), degrees);
	}

	private void doTurnRight(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		double degrees = resolveValue(o2);
		turtle.turn(-degrees);
		logger.debug("{} Right {} degrees.", getIndent(), degrees);
	}

	private void doForward(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		double v = resolveValue(o2);
		turtle.forward(v);
		logger.debug("{} Move forward {} mm", getIndent(), v);
	}

	private void doChangeX(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		double v = resolveValue(o2);
		turtle.moveTo(turtle.getX()+v,turtle.getY());
		//logger.debug(getIndent()+"Move to ("+turtle.getX()+","+turtle.getY()+")");		
	}

	private void doChangeY(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		double v = resolveValue(o2);
		turtle.moveTo(turtle.getX(),turtle.getY()+v);
		//logger.debug(getIndent()+"Move to ("+turtle.getX()+","+turtle.getY()+")");		
	}

	private void doGotoXY(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		double x = resolveValue(o2);
		Object o3 = scriptIter.next();
		double y = resolveValue(o3);
		turtle.moveTo(x,y);
		logger.debug("{}Move to ({},{})", getIndent(), turtle.getX(), turtle.getY());
	}

	private void doWait(Iterator<?> scriptIter) throws Exception {
		// dwell - does nothing.
		Object o2 = scriptIter.next();
		double seconds = resolveValue(o2);
		logger.debug("{} dwell {} seconds.", getIndent(), seconds);
	}

	private void doSetLine(Iterator<?> scriptIter) throws Exception {
		// "setLine:ofList:to:", index, list name, new value
		Object o4 = scriptIter.next();
		Object o2 = scriptIter.next();
		Object o3 = scriptIter.next();
		double newValue = resolveValue(o4);
		int listIndex = (int)resolveListIndex(o2,o3);
		scratchLists.get(getListByID(o3)).contents.set(listIndex,newValue);
	}

	private void doInsertLine(Iterator<?> scriptIter) throws Exception {
		// "insert:at:ofList:", new value, index, list name 
		Object o4 = scriptIter.next();
		Object o2 = scriptIter.next();
		Object o3 = scriptIter.next();
		double newValue = resolveValue(o4);
		int listIndex = (int)resolveListIndex(o2,o3);
		scratchLists.get(getListByID(o3)).contents.add(listIndex,newValue);
	}

	private void doDeleteLine(Iterator<?> scriptIter) throws Exception {
		// "deleteLine:ofList:", index, list name 
		Object o2 = scriptIter.next();
		Object o3 = scriptIter.next();
		int listIndex = (int)resolveListIndex(o2,o3);
		scratchLists.get(getListByID(o3)).contents.remove(listIndex);
	}

	private void doAppend(Iterator<?> scriptIter) throws Exception {
		// "append:toList:", new value, list name 
		Object o2 = scriptIter.next();
		Object o3 = scriptIter.next();
		double value = resolveValue(o2);
		scratchLists.get(getListByID(o3)).contents.add(value);
	}

	private void doIfElse(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		Object o3 = scriptIter.next();
		Object o4 = scriptIter.next();
		if(resolveBoolean((JSONArray)o2)) {
			//logger.debug(getIndent()+"if(true)");
			parseScratchCode((JSONArray)o3);
		} else {
			//logger.debug(getIndent()+"if(false)");
			parseScratchCode((JSONArray)o4);
		}		
	}

	private void doIf(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		Object o3 = scriptIter.next();
		if(resolveBoolean((JSONArray)o2)) {
			//logger.debug(getIndent()+"if(true)");
			parseScratchCode((JSONArray)o3);
		}		
	}

	private void doUntil(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		Object o3 = scriptIter.next();
		//logger.debug(getIndent()+"Do Until");
		while(!resolveBoolean((JSONArray)o2)) {
			parseScratchCode((JSONArray)o3);
		}
	}

	private void doRepeat(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		Object o3 = scriptIter.next();
		int count = (int)resolveValue(o2);
		//logger.debug(getIndent()+"Repeat "+count+" times:");
		for(int i=0;i<count;++i) {
			parseScratchCode((JSONArray)o3);
		}		
	}

	private void doBroadcastAndWait(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		//logger.debug(getIndent()+"broadcast "+(String)o2);
		JSONArray arr = subRoutines.get(o2);
		parseScratchCode(arr);
	}

	/**
	 * Scratch block contains a boolean or boolean operator
	 * @param obj a String, Number, or JSONArray of elements to be calculated. 
	 * @return the calculated final value.
	 * @throws Exception
	 */
	private boolean resolveBoolean(Object obj) throws Exception {
		if(!(obj instanceof JSONArray)) {
			throw new Exception("Parse error (resolveBoolean not array)");
		}
		JSONArray arr=(JSONArray)obj;
		Iterator<?> scriptIter = arr.iterator();
		Object first = scriptIter.next();
		String name = first.toString();
		if(name.equals(">")) {
			Object o2 = scriptIter.next();
			Object o3 = scriptIter.next();
			double a = resolveValue(o2);
			double b = resolveValue(o3);
			return a > b;
		}
		if(name.equals("<")) {
			Object o2 = scriptIter.next();
			Object o3 = scriptIter.next();
			double a = resolveValue(o2);
			double b = resolveValue(o3);
			return a < b;
		}
		if(name.equals("=")) {
			Object o2 = scriptIter.next();
			Object o3 = scriptIter.next();
			double a = resolveValue(o2);
			double b = resolveValue(o3);
			return a == b; 
		}
		if(name.equals("not")) {
			Object o2 = scriptIter.next();
			return !resolveBoolean(o2);
		}
		if(name.equals("&")) {
			Object o2 = scriptIter.next();
			Object o3 = scriptIter.next();
			return resolveBoolean(o2) && resolveBoolean(o3);
		}
		if(name.equals("|")) {
			Object o2 = scriptIter.next();
			Object o3 = scriptIter.next();
			return resolveBoolean(o2) || resolveBoolean(o3);
		}
		
		throw new Exception("Parse error (resolveBoolean unsupported)");
	}
	
	/**
	 * Scratch block contains an Operator (variable, constant, or math combination of the two). 
	 * @param obj a String, Number, or JSONArray of elements to be calculated.
	 * @return the calculated final value.
	 * @throws Exception
	 */
	private double resolveValue(Object obj) throws Exception {
		if(obj instanceof String) {
			// probably a variable
			String firstName = obj.toString();
			if(firstName.equals("xpos")) return turtle.getX();
			if(firstName.equals("ypos")) return turtle.getY();
			if(firstName.equals("heading")) return turtle.getAngle();

			try {
				float v = Float.parseFloat(firstName);
				return v;
			} catch (Exception e) {
				throw new Exception("Unresolved string value '"+obj.toString()+"'", e);
			}
		}
		
		if(obj instanceof Number) {
			Number num = (Number)obj;
			return (double)num.doubleValue();
		}
		
		if(obj instanceof JSONArray) {
			JSONArray arr=(JSONArray)obj;
			Iterator<?> scriptIter = arr.iterator();
			Object first = scriptIter.next();
			if(!(first instanceof String)) {
				throw new Exception("Parse error (resolveValue array)");
			}
			String firstName = first.toString();
			switch(firstName) {
			case "/" : 						return doDivide(scriptIter);
			case "*" : 						return doMultiply(scriptIter);
			case "+" : 						return doAdd(scriptIter);
			case "-" : 						return doSubtract(scriptIter);
			case "%" : 						return doModulus(scriptIter);
			case "randomFrom:to:" : 		return doRandom(scriptIter);
			case "readVariable" : 			return doReadVariable(scriptIter);
			case "computeFunction:of:" : 	return doCompute(scriptIter);
			case "lineCountOfList:" : 		return doCountList(scriptIter);
			case "getLine:ofList:" : 		return doGetFromList(scriptIter);
			default :						logger.debug("unknown operation "+firstName);
			}
			
			return resolveValue(first);
		}

		throw new Exception("Parse error (resolveValue)");
	}

	private double doGetFromList(Iterator<?> scriptIter) throws Exception {
		Object o2 = scriptIter.next();
		Object o3 = scriptIter.next();
		int listIndex = resolveListIndex(o2,o3);
		String listName = (String)o3;
		Scratch2List list = scratchLists.get(getListByID(listName)); 

		return list.contents.get(listIndex);
	}

	private double doCountList(Iterator<?> scriptIter) throws Exception {
		String listName = (String)scriptIter.next();
		return scratchLists.get(getListByID(listName)).contents.size();
	}

	private double doCompute(Iterator<?> scriptIter) throws Exception {
		String functionName = (String)scriptIter.next();
		double a = (float)resolveValue(scriptIter.next());

		switch(functionName) {
		case "abs" :		return Math.abs(a);
		case "floor" :		return Math.floor(a);
		case "ceiling" :	return Math.ceil(a);
		case "sqrt" :		return Math.sqrt(a);
		case "sin" :		return Math.sin(Math.toRadians(a));
		case "cos" :		return Math.cos(Math.toRadians(a));
		case "tan" :		return Math.tan(Math.toRadians(a));
		case "asin" :		return Math.asin(Math.toRadians(a));
		case "acos" :		return Math.acos(Math.toRadians(a));
		case "atan" :		return Math.atan(Math.toRadians(a));
		case "ln" :			return Math.log(a);
		case "log" :		return Math.log10(a);
		case "e ^" :		return Math.pow(Math.E,a);
		case "10 ^" :		return Math.pow(10,a);
		default : 			throw new Exception("doCompute unknown operation "+functionName);
		}
	}

	private double doReadVariable(Iterator<?> scriptIter) throws Exception {
		String varName = (String)scriptIter.next();

		for(Scratch2Variable sv : scratchVariables) {
			if(sv.name.equals(varName)) return sv.value;
		}
		throw new Exception("Failed to find variable "+varName);
	}

	private double doRandom(Iterator<?> scriptIter) throws Exception {
		int a = (int)resolveValue(scriptIter.next());
		int b = (int)resolveValue(scriptIter.next());
		if(a>b) {
			int c = b;
			b=a;
			a=c;
		}
		Random r = new Random();
		return r.nextInt(b-a)+a;
	}
	
	private double doModulus(Iterator<?> scriptIter) throws Exception {
		double a = (double)resolveValue(scriptIter.next());
		double b = (double)resolveValue(scriptIter.next());
		return a%b;
	}

	private double doDivide(Iterator<?> scriptIter) throws Exception {
		double a = (double)resolveValue(scriptIter.next());
		double b = (double)resolveValue(scriptIter.next());
		return a/b;
	}
	
	private double doMultiply(Iterator<?> scriptIter) throws Exception {
		double a = (double)resolveValue(scriptIter.next());
		double b = (double)resolveValue(scriptIter.next());
		return a*b;
	}
	
	private double doAdd(Iterator<?> scriptIter) throws Exception {
		double a = (double)resolveValue(scriptIter.next());
		double b = (double)resolveValue(scriptIter.next());
		return a+b;
	}
	
	private double doSubtract(Iterator<?> scriptIter) throws Exception {
		double a = (double)resolveValue(scriptIter.next());
		double b = (double)resolveValue(scriptIter.next());
		return a-b;
	}

	/**
	 * Find the requested index in a list.
	 * @param o2 the index value.  could be "random", "last", or an index number
	 * @param o3 the list name.
	 * @return the resolved value as an integer.
	 * @throws Exception
	 */
	private int resolveListIndex(Object o2,Object o3) throws Exception {
		int listIndex;
		
		if(o2 instanceof String) {
			String index = (String)o2;
			String listName = (String)o3;
			Scratch2List list = scratchLists.get(getListByID(listName));
			if(index.equals("last")) {
				listIndex = list.contents.size()-1;
			} else if(index.equals("random")) {
				listIndex = (int) (Math.random() * list.contents.size());
			} else {
				listIndex = Integer.parseInt(index);
			}
		} else {
			listIndex = (int)resolveValue(o2);
		}

		return listIndex;
	}
}
