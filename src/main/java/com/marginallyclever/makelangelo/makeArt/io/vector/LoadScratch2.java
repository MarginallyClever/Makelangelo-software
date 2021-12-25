package com.marginallyclever.makelangelo.makeArt.io.vector;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileOutputStream;
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
	
	private FileNameExtensionFilter filter = new FileNameExtensionFilter("Scratch 2","SB2");
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
		String filenameExtension = filename.substring(filename.lastIndexOf('.')).toUpperCase();;
		return filenameExtension.equalsIgnoreCase(".SB2");
	}
	
	@Override
	public Turtle load(InputStream in) throws Exception {
		logger.debug("{}...", Translator.get("FileTypeSB2"));

		turtle = new Turtle();
		
	    indent=0;
		
		// open zip file
    	logger.debug("Searching for project.json...");
    	
		ZipInputStream zipInputStream = new ZipInputStream(in);
		
		// locate project.json
		ZipEntry entry;
		File tempZipFile=null;
		boolean found=false;
		while((entry = zipInputStream.getNextEntry())!=null) {
	        if( entry.getName().equals(PROJECT_JSON) ) {
	        	logger.debug("Found project.json...");
	        	
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
        logger.debug("Parsing JSON file...");
        
        JSONTokener tokener = new JSONTokener(tempZipFile.toURI().toURL().openStream());
        JSONObject tree = new JSONObject(tokener);
		// we're done with the tempZipFile now that we have the JSON structure.
		tempZipFile.delete();
		
		readScratchVariables(tree);
		readScratchLists(tree);

		// read the sketch(es)
		JSONArray children = (JSONArray)tree.get("children");
		if(children==null) throw new Exception("JSON node 'children' missing.");
		//logger.debug("found children");
		
		// look for the first child with a script

		Iterator<?> childIter = children.iterator();
		JSONArray scripts = null;
		while( childIter.hasNext() ) {
			JSONObject child = (JSONObject)childIter.next();
			scripts = (JSONArray)child.get("scripts");
			if (scripts != null)
				break;
		}
		
		if(scripts==null) throw new Exception("JSON node 'scripts' missing.");

		logger.debug("found  {} top-level event scripts", scripts.length());
		
		JSONArray greenFlagScript = null;
		
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
					if( greenFlagScript != null ) {
						throw new Exception("More than one green flag script");
					}
					greenFlagScript = scripts02;
				} else if(firstType.equals("whenIReceive")) {
	    			String firstName = (String)((JSONArray)scriptContents).get(1);
					logger.debug("Found subroutine {}", firstName);
	    			subRoutines.put(firstName, ((JSONArray)scriptContents));
				}
			}
		}

		// actual code begins here.
		parseScratchCode(greenFlagScript);
		
		logger.debug("finished scripts");

		return turtle;
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
			Iterator<?> varIter = variables.iterator();
			while( varIter.hasNext() ) {
				//logger.debug("var:"+elem.toString());
				JSONObject elem = (JSONObject)varIter.next();
				String varName = (String)elem.get("name");
				Object varValue = elem.get("value");
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
		scratchLists = new LinkedList<ScratchList>();
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
			ScratchList list = new ScratchList(listName);
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
			
			if(name.equals("whenGreenFlag")) {
				continue;
			} else if(name.equals("whenIReceive")) {
				// skip the name of the whenIReceive script.
				scriptIter.next();
				continue;
			} else if(name.equals("doBroadcastAndWait")) {
				Object o2 = scriptIter.next();
				//logger.debug(getIndent()+"broadcast "+(String)o2);
				JSONArray arr = subRoutines.get(o2);
				parseScratchCode(arr);
			} else if(name.equals("stopScripts")) {
				//logger.debug(getIndent()+"stop");
				break;
			} else if(name.equals("doRepeat")) {
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				int count = (int)resolveValue(o2);
				//logger.debug(getIndent()+"Repeat "+count+" times:");
				for(int i=0;i<count;++i) {
					parseScratchCode((JSONArray)o3);
				}
			} else if(name.equals("doUntil")) {
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				//logger.debug(getIndent()+"Do Until");
				while(!resolveBoolean((JSONArray)o2)) {
					parseScratchCode((JSONArray)o3);
				}
			} else if(name.equals("doIf")) {
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				if(resolveBoolean((JSONArray)o2)) {
					//logger.debug(getIndent()+"if(true)");
					parseScratchCode((JSONArray)o3);
				}
			} else if(name.equals("doIfElse")) {
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
			} else if(name.equals("append:toList:")) {
				// "append:toList:", new value, list name 
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				double value = resolveValue(o2);
				scratchLists.get(getListByID(o3)).contents.add(value);
			} else if(name.equals("deleteLine:ofList:")) {
				// "deleteLine:ofList:", index, list name 
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				int listIndex = (int)resolveListIndex(o2,o3);
				scratchLists.get(getListByID(o3)).contents.remove(listIndex);
			} else if(name.equals("insert:at:ofList:")) {
				// "insert:at:ofList:", new value, index, list name 
				Object o4 = scriptIter.next();
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				double newValue = resolveValue(o4);
				int listIndex = (int)resolveListIndex(o2,o3);
				scratchLists.get(getListByID(o3)).contents.add(listIndex,newValue);
			} else if(name.equals("setLine:ofList:to:")) {
				// "setLine:ofList:to:", index, list name, new value
				Object o4 = scriptIter.next();
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				double newValue = resolveValue(o4);
				int listIndex = (int)resolveListIndex(o2,o3);
				scratchLists.get(getListByID(o3)).contents.set(listIndex,newValue);
			} else if(name.equals("wait:elapsed:from:")) {
				// dwell - does nothing.
				Object o2 = scriptIter.next();
				double seconds = resolveValue(o2);
				logger.debug("{} dwell {} seconds.", getIndent(), seconds);
				continue;
			} else if(name.equals("putPenUp")) {
				turtle.penUp();
				logger.debug("{} pen up", getIndent());
				continue;
			} else if(name.equals("putPenDown")) {
				turtle.penDown();
				logger.debug("{} pen down", getIndent());
			} else if(name.equals("gotoX:y:")) {
				Object o2 = scriptIter.next();
				double x = resolveValue(o2);
				Object o3 = scriptIter.next();
				double y = resolveValue(o3);
				turtle.moveTo(x,y);
				logger.debug("{}Move to ({},{})", getIndent(), turtle.getX(), turtle.getY());
			} else if(name.equals("changeXposBy:")) {
				Object o2 = scriptIter.next();
				double v = resolveValue(o2);
				turtle.moveTo(turtle.getX()+v,turtle.getY());
				//logger.debug(getIndent()+"Move to ("+turtle.getX()+","+turtle.getY()+")");
			} else if(name.equals("changeYposBy:")) {
				Object o2 = scriptIter.next();
				double v = resolveValue(o2);
				turtle.moveTo(turtle.getX(),turtle.getY()+v);
				//logger.debug(getIndent()+"Move to ("+turtle.getX()+","+turtle.getY()+")");
			} else if(name.equals("forward:")) {
				Object o2 = scriptIter.next();
				double v = resolveValue(o2);
				turtle.forward(v);
				logger.debug("{} Move forward {} mm", getIndent(), v);
			} else if(name.equals("turnRight:")) {
				Object o2 = scriptIter.next();
				double degrees = resolveValue(o2);
				turtle.turn(-degrees);
				logger.debug("{} Right {} degrees.", getIndent(), degrees);
			} else if(name.equals("turnLeft:")) {
				Object o2 = scriptIter.next();
				double degrees = resolveValue(o2);
				turtle.turn(degrees);
				logger.debug("{} Left {} degrees.", getIndent(), degrees);
			} else if(name.equals("xpos:")) {
				Object o2 = scriptIter.next();
				double v = resolveValue(o2);
				turtle.moveTo(v,turtle.getY());
				logger.debug("{} Move to ({},{})", getIndent(), turtle.getX(), turtle.getY());
			} else if(name.equals("ypos:")) {
				Object o2 = scriptIter.next();
				double v = resolveValue(o2);
				turtle.moveTo(turtle.getX(),v);
				//logger.debug(getIndent()+"Move to ("+turtle.getX()+","+turtle.getY()+")");
			} else if(name.equals("heading:")) {
				Object o2 = scriptIter.next();
				double degrees = resolveValue(o2);
				turtle.setAngle(degrees);
				//logger.debug(getIndent()+"Turn to "+degrees);
			} else if(name.equals("setVar:to:")) {
				// set variable
				String varName = (String)scriptIter.next();
				Object o3 = scriptIter.next();
				float v = (float)resolveValue(o3);

				boolean foundVar=false;
				ListIterator<ScratchVariable> svi = scratchVariables.listIterator();
				while(svi.hasNext()) {
					ScratchVariable sv = svi.next();
					if(sv.name.equals(varName)) {
						sv.value = v;
						//logger.debug(getIndent()+"Set "+varName+" to "+v);
						foundVar=true;
					}
				}
				if(foundVar==false) {
					throw new Exception("Variable '"+varName+"' not found.");
				}
			} else if(name.equals("changeVar:by:")) {
				// set variable
				String varName = (String)scriptIter.next();
				Object o3 = scriptIter.next();
				float v = (float)resolveValue(o3);

				boolean foundVar=false;
				ListIterator<ScratchVariable> svi = scratchVariables.listIterator();
				while(svi.hasNext()) {
					ScratchVariable sv = svi.next();
					if(sv.name.equals(varName)) {
						sv.value += v;
						//logger.debug(getIndent()+"Change "+varName+" by "+v+" to "+sv.value);
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
		//logger.debug(getIndent()+"}");
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
			if(firstName.equals("/")) {
				// divide
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a/b;
			}
			if(firstName.equals("*")) {
				// multiply
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a*b;
			}
			if(firstName.equals("+")) {
				// add
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a+b;
			}
			if(firstName.equals("-")) {
				// subtract
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a-b;
			}
			if(firstName.equals("%")) {
				// modulus
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a%b;
			}
			if(firstName.equals("randomFrom:to:")) {
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
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
				Object o2 = scriptIter.next();
				
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
}
