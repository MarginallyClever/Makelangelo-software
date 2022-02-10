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
 * {@link LoadScratch3} loads limited set of Scratch commands into memory. 
 * We ignore monitors, which are visual displays of variables, booleans, and lists
 * They don't contain any real information we need.
 * 
 * @author Dan Royer
 *
 */
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
	private Set<String> blockKeys;
	private Turtle myTurtle;
	
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
		logger.debug("Loading...");
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

		return myTurtle;
	}


	/**
	 * parse blocks in scratch
	 * @param tree the JSONObject tree read from the project.json/zip file.
	 * @throws Exception
	 */
	private void readScratchInstructions(JSONObject tree) throws Exception {
		logger.debug("readScratchInstructions");
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
			break;
		}
		
		if(blocks == null) throw new Exception("targets > blocks missing");
		
		// find the first block with opcode=event_whenflagclicked.
		for( String k : blockKeys ) {
			JSONObject block = (JSONObject)blocks.get(k.toString());
			String opcode = (String)block.get("opcode");
			if(opcode.equals("event_whenflagclicked")) {
				parseScratchCode(k);
				return;
			}
		}
		throw new Exception("WhenFlagClicked block not found.");
	}
	
	private JSONObject getBlock(String key) {
		return (JSONObject)blocks.get(key);
	}
	
	private JSONObject findNextBlock(JSONObject currentBlock) {
		Object key = currentBlock.opt("next");
		if(key==null || key == JSONObject.NULL) return null;
		return getBlock((String)key);
	}
	
	private void parseScratchCode(String currentKey) throws Exception {
		logger.debug("parseScratchCode {}",currentKey);
		JSONObject currentBlock = getBlock(currentKey);
		
		boolean stopCalled=false;
		
		while(currentBlock!=null) {
			String opcode = (String)currentBlock.get("opcode");			
			switch(opcode) {
			// control blocks start
			case "event_whenflagclicked":	doStart(currentBlock);			break;
			case "control_repeat":			doRepeat(currentBlock);  		break;
			case "control_repeat_until":	doRepeatUntil(currentBlock);	break;
			case "control_forever":			doRepeatForever(currentBlock);	break;
			case "control_if":				doIf(currentBlock);				break;
			case "control_if_else":			doIfElse(currentBlock);			break;
			case "control_stop":
				//throw new Exception("control_stop not supported.");
				return;
			// control blocks end

//			case "data_variable":			break;
			case "data_setvariableto":		setVariableTo(currentBlock);	break;
			case "data_changevariableby":	changeVariableBy(currentBlock);	break;
/*			case "data_hidevariable":										break;
			case "data_showvariable":										break;
			case "data_listcontents":										break;
			case "data_addtolist":											break;
			case "data_deleteoflist":										break;
			case "data_deletealloflist":									break;
			case "data_insertatlist":										break;
			case "data_replaceitemoflist":									break;
			case "data_itemoflist":											break;
			case "data_itemnumoflist":										break;
			case "data_lengthoflist":										break;
			case "data_listcontainsitem":									break;
*/
			case "motion_gotoxy": 			doGotoXY(currentBlock);			break;
			case "pen_penDown":
				myTurtle.penDown();
				break;
			case "pen_penUp":
				myTurtle.penUp();
				break;
				
			case "motion_movesteps":		doMovesSteps(currentBlock);		break;
			case "motion_turnright":		doTurnRight(currentBlock);		break;
			case "motion_turnleft":			doTurnLeft(currentBlock);		break;
			case "motion_pointindirection":	doPointInDirection(currentBlock);break;
			
			default:
				logger.debug("Ignored {}", opcode);
			}

			currentBlock = findNextBlock(currentBlock);
		}
	}

	private void doStart(JSONObject currentBlock) {
		logger.debug("START");
		// reset the turtle object
		myTurtle = new Turtle();
	}

	private void doGotoXY(JSONObject currentBlock) throws Exception {
		logger.debug("GOTO XY");
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray x =(JSONArray)inputs.get("X");
		JSONArray y =(JSONArray)inputs.get("Y");
		double px = resolveValue(x.get(1));
		double py = resolveValue(y.get(1));
		myTurtle.moveTo(px, py);
	}

	/**
	 * opcode motion_movesteps.
	 * <br>N.B. : In scratch it seems that if we try to move out of the
	 * "screen", we find ourselves "block by"/"returned to" the edge ... (this therefore
	 * risks creating different renderings because turtle does not have this.)
	 *
	 * @param currentBlock
	 * @throws Exception
	 */
	private void doMovesSteps(JSONObject currentBlock) throws Exception {
		logger.debug("forward steps");
		JSONObject inputs = (JSONObject) currentBlock.get("inputs");
		JSONArray steps = (JSONArray) inputs.get("STEPS");
		double dSteps = resolveValue(steps.get(1));
		myTurtle.forward(dSteps);
	}

	/**
	 * motion_turnright.
	 * TODO as this is the same as TurnLeft but DEGREES * -1.0 ... a sub fonction to factorise.
	 * @param currentBlock
	 * @throws Exception
	 */
	private void doTurnRight(JSONObject currentBlock) throws Exception {
		logger.debug("Turn -DEGREES");
		JSONObject inputs = (JSONObject) currentBlock.get("inputs");
		JSONArray degrees = (JSONArray) inputs.get("DEGREES");
		double dDegrees = resolveValue(degrees.get(1));
		myTurtle.turn(-dDegrees);
	}

	/**
	 * motion_turnleft.
	 *
	 * @param currentBlock
	 * @throws Exception
	 */
	private void doTurnLeft(JSONObject currentBlock) throws Exception {
		logger.debug("Turn +DEGREES");
		JSONObject inputs = (JSONObject) currentBlock.get("inputs");
		JSONArray degrees = (JSONArray) inputs.get("DEGREES");
		double dDegrees = resolveValue(degrees.get(1));
		myTurtle.turn(dDegrees);
	}

	/**
	 * motion_pointindirection.
	 * <br>
	 * N.B. : turtel setAngle(0) = scratch motion_pointindirection (-90)
	 *
	 * @param currentBlock
	 * @throws Exception
	 */
	private void doPointInDirection(JSONObject currentBlock) throws Exception {
		logger.debug("pointindirection DIRECTION");
		JSONObject inputs = (JSONObject) currentBlock.get("inputs");
		JSONArray degrees = (JSONArray) inputs.get("DIRECTION");
		double dDegrees = resolveValue(degrees.get(1));
		myTurtle.setAngle(dDegrees + 90.0);
	}

	private void doIfElse(JSONObject currentBlock) throws Exception {
		logger.debug("IF ELSE");
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray condition =(JSONArray)inputs.get("CONDITION");
		JSONArray substack = (JSONArray)inputs.get("SUBSTACK");
		JSONArray substack2 = (JSONArray)inputs.get("SUBSTACK2");
		if(resolveBoolean(getBlock(condition.getString(1)))) {
			parseScratchCode(substack.getString(1));
		} else {
			parseScratchCode(substack2.getString(1));
		}
	}

	private void doIf(JSONObject currentBlock) throws Exception {
		logger.debug("IF");
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray condition =(JSONArray)inputs.get("CONDITION");
		JSONArray substack = (JSONArray)inputs.get("SUBSTACK");
		if(resolveBoolean(getBlock(condition.getString(1)))) {
			parseScratchCode(substack.getString(1));
		}
	}

	private void doRepeatForever(JSONObject currentBlock) throws Exception {
		logger.debug("FOREVER");
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray substack = (JSONArray)inputs.get("SUBSTACK");
		while(true) {
			parseScratchCode(substack.getString(1));
		}
	}

	private void doRepeatUntil(JSONObject currentBlock) throws Exception {
		logger.debug("REPEAT UNTIL");
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray CONDITION = (JSONArray)inputs.get("CONDITION");
		JSONArray SUBSTACK = (JSONArray)inputs.get("SUBSTACK");
		JSONObject condition = getBlock(CONDITION.getString(1));
		String substack = SUBSTACK.getString(1);
		
		while(!resolveBoolean(condition)) {
			parseScratchCode(substack);
		}
	}

	private void doRepeat(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray condition =(JSONArray)inputs.get("TIMES");
		JSONArray substack = (JSONArray)inputs.get("SUBSTACK");
		int count = (int)resolveValue(getBlock(condition.getString(1)));
		logger.debug("REPEAT {}",count);
		for(int i=0;i<count;++i) {
			parseScratchCode(substack.getString(1));
		}		
	}

	// relative change
	private void changeVariableBy(JSONObject currentBlock) throws Exception {
		ScratchVariable v = findFieldVariableInBlock(currentBlock);
		double newValue = findInputValueInBlock(currentBlock);
		// set and report
		v.value += newValue;
		logger.debug("Set {} to {}", v.name, v.value);
	}

	// absolute change
	private void setVariableTo(JSONObject currentBlock) throws Exception {
		ScratchVariable v = findFieldVariableInBlock(currentBlock);
		double newValue = findInputValueInBlock(currentBlock);
		// set and report
		v.value = newValue;
		logger.debug("Set {} to {}", v.name, v.value);
	}

	private double findInputValueInBlock(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray VALUE = (JSONArray)inputs.get("VALUE");
		JSONArray VALUE1 = (JSONArray)VALUE.get(1);
		return Double.valueOf(VALUE1.getString(1));
	}

	private ScratchVariable findFieldVariableInBlock(JSONObject currentBlock) throws Exception {
		JSONObject fields = (JSONObject)currentBlock.get("fields");
		JSONArray VARIABLE = (JSONArray)fields.get("VARIABLE");
		String uniqueID = VARIABLE.getString(1);
		return getScratchVariable(uniqueID);
	}

	private ScratchVariable getScratchVariable(String uniqueID) throws Exception {
		Iterator<ScratchVariable> svi = scratchVariables.iterator();
		while(svi.hasNext()) {
			ScratchVariable sv = svi.next();
			if(sv.uniqueID.equals(uniqueID)) {
				return sv;
			}
		}
		throw new Exception("Variable '"+uniqueID+"' not found.");
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
		logger.debug("readScratchVariables");
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
				Number value = (Number)Double.parseDouble(details.get(1).toString());
				try {
					logger.debug("Variable {} {} {}", k, name, value.floatValue());
					scratchVariables.add(new ScratchVariable(name,k,value.floatValue()));
				} catch (Exception e) {
					throw new Exception("Variables must be numbers.", e);
				}
			}
		}
	}

	/**
	 * read the list of Scratch lists
	 * @param tree the JSONObject tree read from the project.json/zip file.
	 * @throws Exception
	 */
	private void readScratchLists(JSONObject tree) throws Exception {
		logger.debug("readScratchLists");
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
	
	/**
	 * Scratch block contains a boolean or boolean operator
	 * @param obj a String, Number, or JSONArray of elements to be calculated. 
	 * @return the calculated final value.
	 * @throws Exception
	 */
	private boolean resolveBoolean(JSONObject currentBlock) throws Exception {
		if(currentBlock.has("opcode")) {
			// is equation
			String opcode = currentBlock.getString("opcode");
			switch(opcode) {
			case "operator_lt":		return doLessThan(currentBlock);
			case "operator_gt":		return doGreaterThan(currentBlock);
			case "operator_equals": return doEquals(currentBlock);
			case "operator_and":	return doAnd(currentBlock);
			case "operator_or":		return doOr(currentBlock);
			case "operator_not":	return doNot(currentBlock);
			default: throw new Exception("resolveBoolean unsupported opcode "+opcode);
			}
		}
		throw new Exception("Parse error (resolveBoolean missing opcode)");
	}
	
	private boolean doNot(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray OPERAND = (JSONArray)inputs.getJSONArray("OPERAND");
		boolean a = resolveBoolean(getBlock(OPERAND.getString(1)));
		return !a;
	}

	private boolean doOr(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray OPERAND1 = (JSONArray)inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = (JSONArray)inputs.getJSONArray("OPERAND2");
		boolean a = resolveBoolean(getBlock(OPERAND1.getString(1)));
		boolean b = resolveBoolean(getBlock(OPERAND2.getString(1)));
		return a || b;
	}

	private boolean doAnd(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray OPERAND1 = (JSONArray)inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = (JSONArray)inputs.getJSONArray("OPERAND2");
		boolean a = resolveBoolean(getBlock(OPERAND1.getString(1)));
		boolean b = resolveBoolean(getBlock(OPERAND2.getString(1)));
		return a && b;
	}

	private boolean doEquals(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray OPERAND1 = (JSONArray)inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = (JSONArray)inputs.getJSONArray("OPERAND2");
		double a = resolveValue(OPERAND1.get(1));
		double b = resolveValue(OPERAND2.get(1)); 
		return a == b;
	}

	private boolean doGreaterThan(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray OPERAND1 = (JSONArray)inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = (JSONArray)inputs.getJSONArray("OPERAND2");
		double a = resolveValue(OPERAND1.get(1));
		double b = resolveValue(OPERAND2.get(1)); 
		return a > b;
	}

	private boolean doLessThan(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray OPERAND1 = (JSONArray)inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = (JSONArray)inputs.getJSONArray("OPERAND2");
		double a = resolveValue(OPERAND1.get(1));
		double b = resolveValue(OPERAND2.get(1)); 
		return a < b;
	}

	/**
	 * Scratch block contains an Operator (variable, constant, or math combination of the two). 
	 * @param obj a String, Number, or JSONArray of elements to be calculated.
	 * @return the calculated final value.
	 * @throws Exception
	 */
	private double resolveValue(Object currentObject) throws Exception {
		if(currentObject instanceof JSONArray) {
			JSONArray currentArray = (JSONArray)currentObject;
			switch(currentArray.getInt(0)) {
			case 12:  return getScratchVariable(currentArray.getString(2)).value;  // variable
			case 10:  return Double.parseDouble(currentArray.getString(1));  // constant
			case 4:  return Double.parseDouble(currentArray.getString(1));  // ??? local block varialbe definition ( repeat n times )
			case 8:  return Double.parseDouble(currentArray.getString(1));  // ??? constante angle value  ( motion_pointindirection inputs DIRECTION )
			default: throw new Exception("resolveValue unknown value type "+currentArray.getInt(0));
			}
		} else if(currentObject instanceof String) {
			JSONObject currentBlock = (JSONObject)getBlock((String)currentObject);
			// is equation
			String opcode = currentBlock.getString("opcode");
			switch(opcode) {
			case "operator_add":		return doAdd(currentBlock);
			case "operator_subtract":	return doSubstract(currentBlock);
			case "operator_multiply":	return doMultiply(currentBlock);
			case "operator_divide":		return doDivide(currentBlock);
			case "operator_random":		return doRandom(currentBlock);
			case "operator_mathop":		return doMathOp(currentBlock);
			default: throw new Exception("resolveValue unsupported opcode "+opcode);
			}
		}
		throw new Exception("resolveValue unknown object type "+currentObject.getClass().getSimpleName());
	}
	
	private double doAdd(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray NUM1 = inputs.getJSONArray("NUM1");
		JSONArray NUM2 = inputs.getJSONArray("NUM2");
		double a = resolveValue(NUM1.get(1));
		double b = resolveValue(NUM2.get(1));
		return a + b;
	}

	private double doSubstract(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray NUM1 = inputs.getJSONArray("NUM1");
		JSONArray NUM2 = inputs.getJSONArray("NUM2");
		double a = resolveValue(NUM1.get(1));
		double b = resolveValue(NUM2.get(1));
		return a - b;
	}

	private double doMultiply(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray NUM1 = inputs.getJSONArray("NUM1");
		JSONArray NUM2 = inputs.getJSONArray("NUM2");
		double a = resolveValue(NUM1.get(1));
		double b = resolveValue(NUM2.get(1));
		return a * b;
	}

	private double doDivide(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray NUM1 = inputs.getJSONArray("NUM1");
		JSONArray NUM2 = inputs.getJSONArray("NUM2");
		double a = resolveValue(NUM1.get(1));
		double b = resolveValue(NUM2.get(1));
		return a / b;
	}

	private double doRandom(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray FROM = inputs.getJSONArray("FROM");
		JSONArray TO = inputs.getJSONArray("TO");
		double a = resolveValue(FROM.get(1));
		double b = resolveValue(TO.get(1));
		return Math.random() * (b-a) + a;
	}
	
	private double doMathOp(JSONObject currentBlock) throws Exception {
		JSONObject inputs = (JSONObject)currentBlock.get("inputs");
		JSONArray NUM = inputs.getJSONArray("NUM");
		double v = resolveValue(NUM.get(1));

		JSONObject fields = (JSONObject)currentBlock.get("fields");
		JSONArray OPERATOR = fields.getJSONArray("OPERATOR");
		switch(OPERATOR.getString(0)) {
		case "abs": 	return Math.abs(v);
		case "floor":   return Math.floor(v);
		case "ceiling": return Math.ceil(v);
		case "sqrt":    return Math.sqrt(v);
		case "sin":		return Math.sin(Math.toRadians(v));
		case "cos": 	return Math.cos(Math.toRadians(v));
		case "tan": 	return Math.tan(Math.toRadians(v));
		case "asin":    return Math.asin(Math.toRadians(v));
		case "acos":    return Math.acos(Math.toRadians(v));
		case "atan":    return Math.atan(Math.toRadians(v));
		case "ln":  	return Math.log(v);
		case "log": 	return Math.log10(v);
		case "e ^": 	return Math.exp(v);
		case "10 ^": 	return Math.pow(10,v);
		default: throw new Exception("doMathOp unknown operator "+OPERATOR.getString(1)); 
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
