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
import java.io.Writer;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * LoadAndSaveSB3 loads limited set of Scratch commands into memory. 
 * We ignore monitors, which are visual displays of variables, booleans, and lists.  They don't contain any real information we need.
 * 
 * @author Dan Royer
 *
 */
@SuppressWarnings(value = { "unused" }) // TODO until this is finished

public class LoadScratch3 implements TurtleLoader {
	private static final Logger logger = LoggerFactory.getLogger(LoadScratch3.class);
	private final String PROJECT_JSON = "project.json";
	
	private class ScratchVariable {
		public String name;
		public String uniqueID;
		public double value;

		public ScratchVariable(String _name,String _id,float _val) {
			name=_name;
			uniqueID=_id;
			value=_val;
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
	
	private FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeScratch3"),"SB3");
	private LinkedList<ScratchVariable> scratchVariables;
	private LinkedList<ScratchList> scratchLists;
	private JSONObject blocks;
	private Set<?> blockKeys;
	private Turtle myTurtle;

	//private int indent=0;
	private boolean penUp=false;
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad(String filename) {
		String filenameExtension = filename.substring(filename.lastIndexOf('.')).toUpperCase();
		return filenameExtension.equalsIgnoreCase(".SB3");
	}
	
	@Override
	public Turtle load(InputStream in) throws Exception {
		logger.debug("{}...", Translator.get("FileTypeSB3"));
		// reset the turtle object
		myTurtle = new Turtle();
		
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
			throw new Exception("SB3 missing project.json");
		}
		
		// parse JSON
        logger.debug("Parsing JSON file...");

        JSONTokener tokener = new JSONTokener(tempZipFile.toURI().toURL().openStream());
        JSONObject tree = new JSONObject(tokener);
		// we're done with the tempZipFile now that we have the JSON structure.
		tempZipFile.delete();
		
		
		if(confirmAtLeastVersion3(tree)==false) {
			throw new Exception("File must be at least version 3.0.0.");
		}
		if(confirmHasPenExtension(tree)==false) {
			throw new Exception("File must include pen extension.");
		}
		
		readScratchVariables(tree);
		readScratchLists(tree);
		readScratchInstructions(tree);

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

		logger.debug("found {} scripts", scripts.length());
		logger.debug("finished scripts");

		return myTurtle;
	}


	/**
	 * parse blocks in scratch
	 * @param tree the JSONObject tree read from the project.json/zip file.
	 * @throws Exception
	 */
	private void readScratchInstructions(JSONObject tree) throws Exception {
		scratchVariables = new LinkedList<ScratchVariable>();
		JSONArray targets = (JSONArray)tree.get("targets");
		Iterator<?> targetIter = targets.iterator();
		while(targetIter.hasNext()) {
			JSONObject targetN = (JSONObject)targetIter.next();
			if( (Boolean)targetN.get("isStage") == true ) continue;
			blocks = (JSONObject)targetN.get("blocks");
			// we found the blocks.
			logger.debug("found {} blocks", blocks.length());
			// get the keys, too.
			blockKeys = blocks.keySet();
			// find the first block, which should be the only toplevel block.
			for( Object k : blockKeys ) {
				JSONObject block = (JSONObject)blocks.get(k.toString());
				boolean topLevel = (Boolean)block.get("topLevel");
				String opcode = (String)block.get("opcode");
				if(topLevel && opcode.equals("event_whenflagclicked")) {
					// found!
					logger.debug("**START**");
					parseScratchCode(k);
					logger.debug("**END**");
				}
			}
		}
		
		//parseScratchCode(out,blocks,null,null);
	}
	
	
	JSONObject findNextBlock(JSONObject previous) {
		String key = (String)previous.get("next");
		return (JSONObject)blocks.get(key);
	}
	
	void parseScratchCode(Object currentKey) throws Exception {
		JSONObject currentBlock = (JSONObject)blocks.get(currentKey.toString());
		JSONObject inputs;
		JSONArray substack, condition;
		
		boolean stopCalled=false;
		
		while(currentBlock!=null) {
			String opcode = (String)currentBlock.get("opcode");
			if(opcode==null) throw new Exception("opcode null");
			
			switch(opcode) {
			// START OF SCRIPT
			case "event_whenflagclicked":
				// gcode preamble
				// reset the turtle object
				myTurtle = new Turtle();
				// make sure machine state is the default.
				break;
				
			// C BLOCKS START
			case "control_repeat":
				logger.debug("REPEAT");
				inputs = (JSONObject)currentBlock.get("inputs");
				condition =(JSONArray)inputs.get("TIMES");
				substack = (JSONArray)inputs.get("SUBSTACK");
				break;
			case "control_repeat_until":
				logger.debug("REPEAT UNTIL");
				inputs = (JSONObject)currentBlock.get("inputs");
				condition =(JSONArray)inputs.get("CONDITION");
				substack = (JSONArray)inputs.get("SUBSTACK");
				break;
			case "control_forever":
				logger.debug("FOREVER");
				inputs = (JSONObject)currentBlock.get("inputs");
				substack = (JSONArray)inputs.get("SUBSTACK");
				break;
			case "control_if":
				logger.debug("IF");
				inputs = (JSONObject)currentBlock.get("inputs");
				condition =(JSONArray)inputs.get("CONDITION");
				substack = (JSONArray)inputs.get("SUBSTACK");
				break;
			case "control_if_else":
				logger.debug("IF");
				inputs = (JSONObject)currentBlock.get("inputs");
				condition =(JSONArray)inputs.get("CONDITION");
				substack = (JSONArray)inputs.get("SUBSTACK");
				substack = (JSONArray)inputs.get("SUBSTACK2");
				logger.debug("IF ELSE");
				break;
			// C BLOCKS END
			
			case "control_stop":
				//throw new Exception("control_stop not supported.");
				return;
/*
			case "data_variable":			break;
			case "data_setvariableto":		break;
			case "data_changevariableby":	break;
			case "data_hidevariable":		break;
			case "data_showvariable":		break;
			case "data_listcontents":		break;
			case "data_addtolist":			break;
			case "data_deleteoflist":		break;
			case "data_deletealloflist":	break;
			case "data_insertatlist":		break;
			case "data_replaceitemoflist":	break;
			case "data_itemoflist":			break;
			case "data_itemnumoflist":		break;
			case "data_lengthoflist":		break;
			case "data_listcontainsitem":	break;
*/
			case "motion_gotoxy":
				break;
			case "pen_penDown":
				myTurtle.penDown();
				break;
			case "pen_penUp":
				myTurtle.penUp();
				break;
			default:
				logger.debug("Ignored {}", opcode);
			}

			currentBlock = findNextBlock(currentBlock);
		}
	}

	/**
	 * confirm this is version 3
	 * @param tree the JSONObject tree read from the project.json/zip file.
	 * @throws Exception
	 */
	private boolean confirmAtLeastVersion3(JSONObject tree) throws Exception {
		JSONObject meta = (JSONObject)tree.get("meta");
		if(meta==null) return false;
		
		String semver = (String)meta.get("semver");
		if(semver==null) return false;
		
		return ( semver.compareTo("3.0.0") <= 0 ); 
	}
	
	private boolean confirmHasPenExtension(JSONObject tree) throws Exception {
		JSONArray extensions = (JSONArray)tree.get("extensions");
		if(extensions==null) return false;
		Iterator<Object> i = extensions.iterator();
		while(i.hasNext()) {
			Object o = i.next();
			if(o instanceof String && o.equals("pen")) return true;
		}
		return false;
	}
	
	/**
	 * read the list of Scratch variables
	 * @param tree the JSONObject tree read from the project.json/zip file.
	 * @throws Exception
	 */
	private void readScratchVariables(JSONObject tree) throws Exception {
		scratchVariables = new LinkedList<ScratchVariable>();
		JSONArray targets = (JSONArray)tree.get("targets");
		Iterator<?> targetIter = targets.iterator();
		while(targetIter.hasNext()) {
			JSONObject targetN = (JSONObject)targetIter.next();
			if( (Boolean)targetN.get("isStage") == false ) continue;
			
			JSONObject variables = (JSONObject)targetN.get("variables");
			Iterator<?> keys = variables.keySet().iterator();
			while(keys.hasNext()) {
				String k=(String)keys.next();
				JSONArray details = (JSONArray)variables.get(k);
				String name = (String)details.get(0);
				Number value = (Number)details.get(1);
				try {
					logger.debug("Variable {} {} {}", name, k, value.floatValue());
					scratchVariables.add(new ScratchVariable(name,k,value.floatValue()));
				} catch (Exception e) {
					throw new Exception("Variables must be numbers.", e);
				}
			}
		}
		logger.debug(scratchVariables.toString());
	}

	/**
	 * read the list of Scratch lists
	 * @param tree the JSONObject tree read from the project.json/zip file.
	 * @throws Exception
	 */
	private void readScratchLists(JSONObject tree) throws Exception {
		scratchLists = new LinkedList<ScratchList>();
		JSONArray targets = (JSONArray)tree.get("targets");
		Iterator<?> targetIter = targets.iterator();
		while(targetIter.hasNext()) {
			JSONObject targetN = (JSONObject)targetIter.next();
			if( (Boolean)targetN.get("isStage") == false ) continue;
			JSONObject listOfLists = (JSONObject)targetN.get("lists");
			if(listOfLists == null) return;
			Set<?> keys = listOfLists.keySet();
			Iterator<?> keyIter = keys.iterator();
			while( keyIter.hasNext() ) {
				String key = (String)keyIter.next();
				logger.debug("list key:{}", key);
				JSONArray elem = (JSONArray)listOfLists.get(key);
				String listName = (String)elem.get(0);
				logger.debug("  list name:{}", listName);
				Object contents = elem.get(1);
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
							value = (float)num.doubleValue();
							logger.debug("  list float:{}", value);
							list.contents.add(value);
						} else if(varValue instanceof String) {
							try {
								value = Double.parseDouble((String)varValue);
								logger.debug("  list string:{}", value);
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
	}
	
	private int getListID(Object obj) throws Exception {
		if(!(obj instanceof String)) throw new Exception("List name not a string.");
		String listName = obj.toString();
		Iterator<ScratchList> iter = scratchLists.iterator();
		int index=0;
		while(iter.hasNext()) {
			ScratchList i = iter.next();
			if(i.name.equals(listName)) return index;
			++index;
		}
		throw new Exception("List '"+listName+"' not found.");
	}
	
	/**
	 * read the elements of a JSON array describing Scratch code and parse it into gcode.
	 * @param script valid JSONArray of Scratch commands.
	 * @param out where to put the gcode.
	 * @throws Exception
	 */
	private void parseScratchCode(JSONArray script,Writer out) throws Exception {
		if(script==null) return;
		
		//for(int j=0;j<indent;++j) logger.debug("  ");
		//logger.debug("size="+script.size());
		//indent++;
		
		Iterator<?> scriptIter = script.iterator();
		// find the script with the green flag
		while( scriptIter.hasNext() ) {
			Object o = scriptIter.next();
			if( o instanceof JSONArray ) {
				JSONArray arr = (JSONArray)o;
				parseScratchCode(arr,out);
			} else {
				String name = o.toString();
				//for(int j=0;j<indent;++j) logger.debug("  ");
				//logger.debug(i+"="+name);
				
				if(name.equals("whenGreenFlag")) {
					// gcode preamble
	    			// reset the turtle object
	    			myTurtle = new Turtle();
					logger.debug("**START**");
					continue;
				} else if(name.equals("doRepeat")) {
					Object o2 = scriptIter.next();
					Object o3 = scriptIter.next();
					int count = (int)resolveValue(o2);
					//logger.debug("Repeat "+count+" times:");
					for(int i=0;i<count;++i) {
						parseScratchCode((JSONArray)o3,out);
					}
				} else if(name.equals("doUntil")) {
					Object o2 = scriptIter.next();
					Object o3 = scriptIter.next();
					//logger.debug("Do Until {");
					while(!resolveBoolean((JSONArray)o2)) {
						parseScratchCode((JSONArray)o3,out);
					}
					//logger.debug("}");
				} else if(name.equals("doIf")) {
					Object o2 = scriptIter.next();
					Object o3 = scriptIter.next();
					if(resolveBoolean((JSONArray)o2)) {
						parseScratchCode((JSONArray)o3,out);
					}
				} else if(name.equals("doIfElse")) {
					Object o2 = scriptIter.next();
					Object o3 = scriptIter.next();
					Object o4 = scriptIter.next();
					if(resolveBoolean((JSONArray)o2)) {
						parseScratchCode((JSONArray)o3,out);
					} else {
						parseScratchCode((JSONArray)o4,out);
					}
				} else if(name.equals("append:toList:")) {
					// "append:toList:", new value, list name 
					Object o2 = scriptIter.next();
					Object o3 = scriptIter.next();
					double value = resolveValue(o2);
					scratchLists.get(getListID(o3)).contents.add(value);
				} else if(name.equals("deleteLine:ofList:")) {
					// "deleteLine:ofList:", index, list name 
					Object o2 = scriptIter.next();
					Object o3 = scriptIter.next();
					int listIndex = (int)resolveListIndex(o2,o3);
					scratchLists.get(getListID(o3)).contents.remove(listIndex);
				} else if(name.equals("insert:at:ofList:")) {
					// "insert:at:ofList:", new value, index, list name 
					Object o4 = scriptIter.next();
					Object o2 = scriptIter.next();
					Object o3 = scriptIter.next();
					double newValue = resolveValue(o4);
					int listIndex = (int)resolveListIndex(o2,o3);
					scratchLists.get(getListID(o3)).contents.add(listIndex,newValue);
				} else if(name.equals("setLine:ofList:to:")) {
					// "setLine:ofList:to:", index, list name, new value
					Object o4 = scriptIter.next();
					Object o2 = scriptIter.next();
					Object o3 = scriptIter.next();
					double newValue = resolveValue(o4);
					int listIndex = (int)resolveListIndex(o2,o3);
					scratchLists.get(getListID(o3)).contents.set(listIndex,newValue);
				} else if(name.equals("wait:elapsed:from:")) {
					// dwell - does nothing.
					Object o2 = scriptIter.next();
					double seconds = resolveValue(o2);
					logger.debug("dwell {} seconds.", seconds);
					continue;
				} else if(name.equals("putPenUp")) {
					myTurtle.penUp();
					logger.debug("pen up");
					continue;
				} else if(name.equals("putPenDown")) {
					myTurtle.penDown();
					logger.debug("pen down");
				} else if(name.equals("gotoX:y:")) {
					Object o2 = scriptIter.next();
					double x = resolveValue(o2);
					Object o3 = scriptIter.next();
					double y = resolveValue(o3);
					
					myTurtle.moveTo(x,y);
					logger.debug("Move to ({},{})", myTurtle.getX(), myTurtle.getY());
				} else if(name.equals("changeXposBy:")) {
					Object o2 = scriptIter.next();
					double v = resolveValue(o2);
					myTurtle.moveTo(myTurtle.getX()+v,myTurtle.getY());
					//logger.debug("Move to ("+turtle.getX()+","+turtle.getY()+")");
				} else if(name.equals("changeYposBy:")) {
					Object o2 = scriptIter.next();
					double v = resolveValue(o2);
					myTurtle.moveTo(myTurtle.getX(),myTurtle.getY()+v);
					//logger.debug("Move to ("+turtle.getX()+","+turtle.getY()+")");
				} else if(name.equals("forward:")) {
					Object o2 = scriptIter.next();
					double v = resolveValue(o2);
					myTurtle.forward(v);
					logger.debug("Move forward {} mm", v);
				} else if(name.equals("turnRight:")) {
					Object o2 = scriptIter.next();
					double degrees = resolveValue(o2);
					myTurtle.turn(-degrees);
					logger.debug("Right {} degrees.", degrees);
				} else if(name.equals("turnLeft:")) {
					Object o2 = scriptIter.next();
					double degrees = resolveValue(o2);
					myTurtle.turn(degrees);
					logger.debug("Left {} degrees.", degrees);
				} else if(name.equals("xpos:")) {
					Object o2 = scriptIter.next();
					double v = resolveValue(o2);
					myTurtle.moveTo(v,myTurtle.getY());
					//logger.debug("Move to ("+turtle.getX()+","+turtle.getY()+")");
				} else if(name.equals("ypos:")) {
					Object o2 = scriptIter.next();
					double v = resolveValue(o2);
					myTurtle.moveTo(myTurtle.getX(),v);
					//logger.debug("Move to ("+turtle.getX()+","+turtle.getY()+")");
				} else if(name.equals("heading:")) {
					Object o2 = scriptIter.next();
					double degrees = resolveValue(o2);
					myTurtle.setAngle(degrees);
					//logger.debug("Turn to "+degrees);
				} else if(name.equals("setVar:to:")) {
					// set variable
					String varName = (String)scriptIter.next();
					Object o3 = scriptIter.next();
					double v = (float)resolveValue(o3);

					boolean foundVar=false;
					Iterator<ScratchVariable> svi = scratchVariables.iterator();
					while(svi.hasNext()) {
						ScratchVariable sv = svi.next();
						if(sv.name.equals(varName)) {
							sv.value = v;
							logger.debug("Set {} to {}", varName, v);
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
					Iterator<ScratchVariable> svi = scratchVariables.iterator();
					while(svi.hasNext()) {
						ScratchVariable sv = svi.next();
						if(sv.name.equals(varName)) {
							sv.value += v;
							logger.debug("Change {} by {} to {}", varName, v, sv.value);
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
		}
		//indent--;
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
				return myTurtle.getX();
			}
			if(firstName.equals("ypos")) {
				return myTurtle.getY();
			}
			if(firstName.equals("heading")) {
				return myTurtle.getAngle();
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
			return (float)num.doubleValue();
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

				Iterator<ScratchVariable> svi = scratchVariables.iterator();
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
				return scratchLists.get(getListID(listName)).contents.size();
			}
			if(firstName.equals("getLine:ofList:")) {
				Object o2 = scriptIter.next();
				Object o3 = scriptIter.next();
				int listIndex = resolveListIndex(o2,o3);
				String listName = (String)o3;
				ScratchList list = scratchLists.get(getListID(listName)); 

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
		String index = (String)o2;
		String listName = (String)o3;
		ScratchList list = scratchLists.get(getListID(listName)); 
		int listIndex;
		if(index.equals("last")) {
			listIndex = list.contents.size()-1;
		} else if(index.equals("random")) {
			listIndex = (int) (Math.random() * list.contents.size());
		} else {
			listIndex = Integer.parseInt(index);
		}

		return listIndex;
	}
}
