package com.marginallyclever.artPipeline.loadAndSave;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.marginallyclever.artPipeline.ImageManipulator;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

/**
 * LoadAndSaveSB2 loads limited set of Scratch commands into memory. 
 * @author Admin
 *
 */
public class LoadAndSaveScratch2 extends ImageManipulator implements LoadAndSaveFileType {
	private final String PROJECT_JSON = "project.json";
	
	private class ScratchVariable {
		public String name;
		public double value;

		public ScratchVariable(String arg0,float arg1) {
			name=arg0;
			value=arg1;
		}
	};
	private class ScratchList {
		public String name;
		public ArrayList<Double> contents;

		public ScratchList(String _name) {
			name=_name;
			contents=new ArrayList<Double>();
		}
	};

	private static final Set<String> IMAGE_FILE_EXTENSIONS;
	static {
		IMAGE_FILE_EXTENSIONS = new HashSet<>();
		IMAGE_FILE_EXTENSIONS.add("SB2");
	}
	
	private FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeScratch2"),
			IMAGE_FILE_EXTENSIONS.toArray(new String[IMAGE_FILE_EXTENSIONS.size()]));
	private Turtle turtle;
	private LinkedList<ScratchVariable> scratchVariables;
	private LinkedList<ScratchList> scratchLists;
	private Map<String,JSONArray> subRoutines = new HashMap<String,JSONArray>();
	private int indent;
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad(String filename) {
		String filenameExtension = filename.substring(filename.lastIndexOf('.'));
		return IMAGE_FILE_EXTENSIONS.contains(filenameExtension.toLowerCase());
	}

	@Override
	public boolean canSave(String filename) {
		return false;
	}

	
	@Override
	public boolean load(InputStream in,MakelangeloRobot robot) {
		Log.message(Translator.get("FileTypeSB2")+"...");

		machine = robot.getSettings();
		turtle = new Turtle();
	    turtle.setX(machine.getHomeX());
	    turtle.setY(machine.getHomeX());
	    indent=0;
		
		try {
			// open zip file
        	Log.message("Searching for project.json...");
        	
			ZipInputStream zipInputStream = new ZipInputStream(in);
			
			// locate project.json
			ZipEntry entry;
			File tempZipFile=null;
			boolean found=false;
			while((entry = zipInputStream.getNextEntry())!=null) {
		        if( entry.getName().equals(PROJECT_JSON) ) {
		        	Log.message("Found project.json...");
		        	
			        // read buffered stream into temp file.
		        	tempZipFile = File.createTempFile("project", "json");
		        	tempZipFile.setReadable(true);
		        	tempZipFile.setWritable(true);
		        	tempZipFile.deleteOnExit();
			        FileOutputStream fos = new FileOutputStream(tempZipFile);
		    		byte[] buffer = new byte[2048];
		    		int len;
	                while ((len = zipInputStream.read(buffer)) > 0) {
	                    fos.write(buffer, 0, len);
	                }
	                fos.close();
	                found=true;
	                break;
		        }
			}

			if(found==false) {
				throw new Exception("SB2 missing project.json");
			}
			
			// parse JSON
            Log.message("Parsing JSON file...");
            
        	JSONParser parser = new JSONParser();
			JSONObject tree = (JSONObject)parser.parse(new FileReader(tempZipFile));
			// we're done with the tempZipFile now that we have the JSON structure.
			tempZipFile.delete();
			
			readScratchVariables(tree);
			readScratchLists(tree);

			// read the sketch(es)
			JSONArray children = (JSONArray)tree.get("children");
			if(children==null) throw new Exception("JSON node 'children' missing.");
			//Log.message("found children");
			
			// look for the first child with a script

			ListIterator<?> childIter = children.listIterator();
			JSONArray scripts = null;
			while( childIter.hasNext() ) {
				JSONObject child = (JSONObject)childIter.next();
				scripts = (JSONArray)child.get("scripts");
				if (scripts != null)
					break;
			}
			
			if(scripts==null) throw new Exception("JSON node 'scripts' missing.");

			Log.message("found  " +scripts.size() + " top-level event scripts");
			
			JSONArray greenFlagScript = null;
			
			// extract known elements and convert them to turtle commands.
			ListIterator<?> scriptIter = scripts.listIterator();
			// find the first script with a green flag.
			while( scriptIter.hasNext() ) {
    			JSONArray scripts0 = (JSONArray)scriptIter.next();
    			if( scripts0==null ) continue;
    			JSONArray scripts02 = (JSONArray)scripts0.get(2);
    			if( scripts02==null || scripts02.size()==0 ) continue;
    			// look inside the script at the first block, which gives the type.
    			Object scriptContents = scripts02.get(0);
    			if( ( scriptContents instanceof JSONArray ) ) {
    				Object scriptHead = ((JSONArray)scriptContents).get(0);
    				
	    			String firstType = (String)scriptHead;
					if(firstType.equals("whenGreenFlag")) {
						Log.message("Found green flag script");
						if( greenFlagScript != null ) {
							throw new Exception("More than one green flag script");
						}
						greenFlagScript = scripts02;
					} else if(firstType.equals("whenIReceive")) {
		    			String firstName = (String)((JSONArray)scriptContents).get(1);
						Log.message("Found subroutine "+firstName);
		    			subRoutines.put(firstName, ((JSONArray)scriptContents));
					}
    			}
			}

			// actual code begins here.
			parseScratchCode(greenFlagScript);
			
			Log.message("finished scripts");
			robot.setTurtle(turtle);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * read the list of Scratch variables
	 * @param tree the JSONObject tree read from the project.json/zip file.
	 * @throws Exception
	 */
	private void readScratchVariables(JSONObject tree) throws Exception {
		scratchVariables = new LinkedList<ScratchVariable>();
		JSONArray variables = (JSONArray)tree.get("variables");
		// A scratch file without variables would crash before this test
		if (variables != null) {
			ListIterator<?> varIter = variables.listIterator();
			while( varIter.hasNext() ) {
				//Log.message("var:"+elem.toString());
				JSONObject elem = (JSONObject)varIter.next();
				String varName = (String)elem.get("name");
				Object varValue = (Object)elem.get("value");
				float value;
				if(varValue instanceof Number) {
					Number num = (Number)varValue;
					value = (float)num.doubleValue();
					scratchVariables.add(new ScratchVariable(varName,value));
				} else if(varValue instanceof String) {
					try {
						value = Float.parseFloat((String)varValue);
	    				scratchVariables.add(new ScratchVariable(varName,value));
					} catch (Exception e) {
						throw new Exception("Variables must be numbers.");
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
		scratchLists = new LinkedList<ScratchList>();
		JSONArray listOfLists = (JSONArray)tree.get("lists");
		if(listOfLists == null) return;
		Log.message("Found "+listOfLists.size()+" lists.");
		ListIterator<?> listIter = listOfLists.listIterator();
		while( listIter.hasNext() ) {
			//Log.message("var:"+elem.toString());
			JSONObject elem = (JSONObject)listIter.next();
			String listName = (String)elem.get("listName");
			Log.message("  Found list "+listName);
			Object contents = (Object)elem.get("contents");
			ScratchList list = new ScratchList(listName);
			// fill the list with any given contents
			if( contents != null && contents instanceof JSONArray ) {
				JSONArray arr = (JSONArray)contents;

				ListIterator<?> scriptIter = arr.listIterator();
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
							throw new Exception("List variables must be numbers.");
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
		ListIterator<ScratchList> iter = scratchLists.listIterator();
		int index=0;
		while(iter.hasNext()) {
			ScratchList i = iter.next();
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
		
		//Log.message(getIndent()+"{");  //"[size="+script.size()+"]"
		indent++;
		
		ListIterator<?> scriptIter = script.listIterator();
		// Find the script with the green flag.  Assumes only one green flag per script.
		while( scriptIter.hasNext() ) {
			Object o = (Object)scriptIter.next();
			if( o instanceof JSONArray ) {
				JSONArray arr = (JSONArray)o;
				parseScratchCode(arr);
				continue;
			}
			
			String name = o.toString();
			//Log.message(getIndent()+name);
			
			if(name.equals("whenGreenFlag")) {
				continue;
			} else if(name.equals("whenIReceive")) {
				// skip the name of the whenIReceive script.
				scriptIter.next();
				continue;
			} else if(name.equals("doBroadcastAndWait")) {
				Object o2 = (Object)scriptIter.next();
				//Log.message(getIndent()+"broadcast "+(String)o2);
				JSONArray arr = subRoutines.get(o2);
				parseScratchCode(arr);
			} else if(name.equals("stopScripts")) {
				//Log.message(getIndent()+"stop");
				break;
			} else if(name.equals("doRepeat")) {
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				int count = (int)resolveValue(o2);
				//Log.message(getIndent()+"Repeat "+count+" times:");
				for(int i=0;i<count;++i) {
					parseScratchCode((JSONArray)o3);
				}
			} else if(name.equals("doUntil")) {
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				//Log.message(getIndent()+"Do Until");
				while(!resolveBoolean((JSONArray)o2)) {
					parseScratchCode((JSONArray)o3);
				}
			} else if(name.equals("doIf")) {
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				if(resolveBoolean((JSONArray)o2)) {
					//Log.message(getIndent()+"if(true)");
					parseScratchCode((JSONArray)o3);
				}
			} else if(name.equals("doIfElse")) {
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				Object o4 = (Object)scriptIter.next();
				if(resolveBoolean((JSONArray)o2)) {
					//Log.message(getIndent()+"if(true)");
					parseScratchCode((JSONArray)o3);
				} else {
					//Log.message(getIndent()+"if(false)");
					parseScratchCode((JSONArray)o4);
				}
			} else if(name.equals("append:toList:")) {
				// "append:toList:", new value, list name 
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				double value = resolveValue(o2);
				scratchLists.get(getListByID(o3)).contents.add(value);
			} else if(name.equals("deleteLine:ofList:")) {
				// "deleteLine:ofList:", index, list name 
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				int listIndex = (int)resolveListIndex(o2,o3);
				scratchLists.get(getListByID(o3)).contents.remove(listIndex);
			} else if(name.equals("insert:at:ofList:")) {
				// "insert:at:ofList:", new value, index, list name 
				Object o4 = (Object)scriptIter.next();
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				double newValue = resolveValue(o4);
				int listIndex = (int)resolveListIndex(o2,o3);
				scratchLists.get(getListByID(o3)).contents.add(listIndex,newValue);
			} else if(name.equals("setLine:ofList:to:")) {
				// "setLine:ofList:to:", index, list name, new value
				Object o4 = (Object)scriptIter.next();
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				double newValue = resolveValue(o4);
				int listIndex = (int)resolveListIndex(o2,o3);
				scratchLists.get(getListByID(o3)).contents.set(listIndex,newValue);
			} else if(name.equals("wait:elapsed:from:")) {
				// dwell - does nothing.
				Object o2 = (Object)scriptIter.next();
				double seconds = resolveValue(o2);
				Log.message(getIndent()+"dwell "+seconds+" seconds.");
				continue;
			} else if(name.equals("putPenUp")) {
				turtle.penUp();
				Log.message(getIndent()+"pen up");
				continue;
			} else if(name.equals("putPenDown")) {
				turtle.penDown();
				Log.message(getIndent()+"pen down");
			} else if(name.equals("gotoX:y:")) {
				Object o2 = (Object)scriptIter.next();
				double x = resolveValue(o2);
				Object o3 = (Object)scriptIter.next();
				double y = resolveValue(o3);
				
				turtle.moveTo(x,y);
				Log.message(getIndent()+"Move to ("+turtle.getX()+","+turtle.getY()+")");
			} else if(name.equals("changeXposBy:")) {
				Object o2 = (Object)scriptIter.next();
				double v = resolveValue(o2);
				turtle.moveTo(turtle.getX()+v,turtle.getY());
				//Log.message(getIndent()+"Move to ("+turtle.getX()+","+turtle.getY()+")");
			} else if(name.equals("changeYposBy:")) {
				Object o2 = (Object)scriptIter.next();
				double v = resolveValue(o2);
				turtle.moveTo(turtle.getX(),turtle.getY()+v);
				//Log.message(getIndent()+"Move to ("+turtle.getX()+","+turtle.getY()+")");
			} else if(name.equals("forward:")) {
				Object o2 = (Object)scriptIter.next();
				double v = resolveValue(o2);
				turtle.forward(v);
				Log.message(getIndent()+"Move forward "+v+" mm");
			} else if(name.equals("turnRight:")) {
				Object o2 = (Object)scriptIter.next();
				double degrees = resolveValue(o2);
				turtle.turn(-degrees);
				Log.message(getIndent()+"Right "+degrees+" degrees.");
			} else if(name.equals("turnLeft:")) {
				Object o2 = (Object)scriptIter.next();
				double degrees = resolveValue(o2);
				turtle.turn(degrees);
				Log.message(getIndent()+"Left "+degrees+" degrees.");
			} else if(name.equals("xpos:")) {
				Object o2 = (Object)scriptIter.next();
				double v = resolveValue(o2);
				turtle.moveTo(v,turtle.getY());
				Log.message(getIndent()+"Move to ("+turtle.getX()+","+turtle.getY()+")");
			} else if(name.equals("ypos:")) {
				Object o2 = (Object)scriptIter.next();
				double v = resolveValue(o2);
				turtle.moveTo(turtle.getX(),v);
				//Log.message(getIndent()+"Move to ("+turtle.getX()+","+turtle.getY()+")");
			} else if(name.equals("heading:")) {
				Object o2 = (Object)scriptIter.next();
				double degrees = resolveValue(o2);
				turtle.setAngle(degrees);
				//Log.message(getIndent()+"Turn to "+degrees);
			} else if(name.equals("setVar:to:")) {
				// set variable
				String varName = (String)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				float v = (float)resolveValue(o3);

				boolean foundVar=false;
				ListIterator<ScratchVariable> svi = scratchVariables.listIterator();
				while(svi.hasNext()) {
					ScratchVariable sv = svi.next();
					if(sv.name.equals(varName)) {
						sv.value = v;
						//Log.message(getIndent()+"Set "+varName+" to "+v);
						foundVar=true;
					}
				}
				if(foundVar==false) {
					throw new Exception("Variable '"+varName+"' not found.");
				}
			} else if(name.equals("changeVar:by:")) {
				// set variable
				String varName = (String)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				float v = (float)resolveValue(o3);

				boolean foundVar=false;
				ListIterator<ScratchVariable> svi = scratchVariables.listIterator();
				while(svi.hasNext()) {
					ScratchVariable sv = svi.next();
					if(sv.name.equals(varName)) {
						sv.value += v;
						//Log.message(getIndent()+"Change "+varName+" by "+v+" to "+sv.value);
						foundVar=true;
					}
				}
				if(foundVar==false) {
					throw new Exception("Variable '"+varName+"' not found.");
				}
			} else if(name.equals("clearPenTrails")) {
				// Ignore this Scratch command
			} else if(name.equals("hide")) {
				// Ignore this Scratch command
			} else if(name.equals("show")) {
				// Ignore this Scratch command
			} else {
				throw new Exception("Unsupported Scratch block '"+name+"'");
			}
		}
		indent--;
		//Log.message(getIndent()+"}");
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
		ListIterator<?> scriptIter = arr.listIterator();
		Object first = scriptIter.next();
		String name = first.toString();
		if(name.equals(">")) {
			Object o2 = (Object)scriptIter.next();
			Object o3 = (Object)scriptIter.next();
			double a = resolveValue(o2);
			double b = resolveValue(o3);
			return a > b;
		}
		if(name.equals("<")) {
			Object o2 = (Object)scriptIter.next();
			Object o3 = (Object)scriptIter.next();
			double a = resolveValue(o2);
			double b = resolveValue(o3);
			return a < b;
		}
		if(name.equals("=")) {
			Object o2 = (Object)scriptIter.next();
			Object o3 = (Object)scriptIter.next();
			double a = resolveValue(o2);
			double b = resolveValue(o3);
			return a == b; 
		}
		if(name.equals("not")) {
			Object o2 = (Object)scriptIter.next();
			return !resolveBoolean(o2);
		}
		if(name.equals("&")) {
			Object o2 = (Object)scriptIter.next();
			Object o3 = (Object)scriptIter.next();
			return resolveBoolean(o2) && resolveBoolean(o3);
		}
		if(name.equals("|")) {
			Object o2 = (Object)scriptIter.next();
			Object o3 = (Object)scriptIter.next();
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
			
			if(firstName.equals("xpos")) {
				return turtle.getX();
			}
			if(firstName.equals("ypos")) {
				return turtle.getY();
			}
			if(firstName.equals("heading")) {
				return turtle.getAngle();
			}

			try {
				float v = Float.parseFloat(firstName);
				return v;
			} catch (Exception e) {
				throw new Exception("Unresolved string value '"+obj.toString()+"'");
			}
		}
		
		if(obj instanceof Number) {
			Number num = (Number)obj;
			return (double)num.doubleValue();
		}
		
		if(obj instanceof JSONArray) {
			JSONArray arr=(JSONArray)obj;
			ListIterator<?> scriptIter = arr.listIterator();
			Object first = scriptIter.next();
			if(!(first instanceof String)) {
				throw new Exception("Parse error (resolveValue array)");
			}
			String firstName = first.toString();
			if(firstName.equals("/")) {
				// divide
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a/b;
			}
			if(firstName.equals("*")) {
				// multiply
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a*b;
			}
			if(firstName.equals("+")) {
				// add
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a+b;
			}
			if(firstName.equals("-")) {
				// subtract
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a-b;
			}
			if(firstName.equals("%")) {
				// modulus
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a%b;
			}
			if(firstName.equals("randomFrom:to:")) {
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				int a = (int)resolveValue(o2);
				int b = (int)resolveValue(o3);
				if(a>b) {
					int c = b;
					b=a;
					a=c;
				}
				Random r = new Random();
				return r.nextInt(b-a)+a;
			}
			if(firstName.equals("readVariable")) {
				String varName = (String)scriptIter.next();

				ListIterator<ScratchVariable> svi = scratchVariables.listIterator();
				while(svi.hasNext()) {
					ScratchVariable sv = svi.next();
					if(sv.name.equals(varName)) {
						return sv.value;
					}
				}
			}
			if(firstName.equals("computeFunction:of:")) {
				String functionName = (String)scriptIter.next();
				Object o2 = (Object)scriptIter.next();
				
				float a = (float)resolveValue(o2);

				if(functionName.equals("abs")) return (float)Math.abs(a);
				if(functionName.equals("floor")) return (float)Math.floor(a);
				if(functionName.equals("ceiling")) return (float)Math.ceil(a);
				if(functionName.equals("sqrt")) return (float)Math.sqrt(a);
				if(functionName.equals("sin")) return (float)Math.sin(Math.toRadians(a));
				if(functionName.equals("cos")) return (float)Math.cos(Math.toRadians(a));
				if(functionName.equals("tan")) return (float)Math.tan(Math.toRadians(a));

				if(functionName.equals("asin")) return (float)Math.asin(Math.toRadians(a));
				if(functionName.equals("acos")) return (float)Math.acos(Math.toRadians(a));
				if(functionName.equals("atan")) return (float)Math.atan(Math.toRadians(a));
				if(functionName.equals("ln")) return (float)Math.log(a);
				if(functionName.equals("log")) return (float)Math.log10(a);
				if(functionName.equals("e ^")) return (float)Math.pow(Math.E,a);
				if(functionName.equals("10 ^")) return (float)Math.pow(10,a);
				throw new Exception("Parse error (resolveValue computeFunction)");
			}
			if(firstName.equals("lineCountOfList:")) {
				String listName = (String)scriptIter.next();
				return scratchLists.get(getListByID(listName)).contents.size();
			}
			if(firstName.equals("getLine:ofList:")) {
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				int listIndex = resolveListIndex(o2,o3);
				String listName = (String)o3;
				ScratchList list = scratchLists.get(getListByID(listName)); 

				return list.contents.get(listIndex);
			}
			
			return resolveValue(first);
		}

		throw new Exception("Parse error (resolveValue)");
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
			ScratchList list = scratchLists.get(getListByID(listName));
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
	
	@Override
	public boolean save(OutputStream outputStream,MakelangeloRobot robot) {
		return true;
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public boolean canSave() {
		return false;
	}
}
