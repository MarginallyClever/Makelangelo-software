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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * {@link LoadScratch3} loads a limited set of Scratch 3.0 commands into memory. 
 * We ignore monitors, which are visual displays of variables, booleans, and lists
 * They don't contain any real information we need.
 * 
 * See https://en.scratch-wiki.info/wiki/Scratch_File_Format
 * See https://github.com/LLK/scratch-blocks/tree/develop/blocks_vertical
 * 
 * @author Dan Royer
 * @since 7.31.0
 */
public class LoadScratch3 implements TurtleLoader {
	private static final Logger logger = LoggerFactory.getLogger(LoadScratch3.class);
	private final String PROJECT_JSON = "project.json";
	
	private class Scratch3Variable implements Cloneable {
		public String name;
		
		public String uniqueID;
		public Object value;

		public Scratch3Variable(String name,String uniqueID,Object defaultValue) {
			this.name=name;
			this.uniqueID=uniqueID;
			this.value=defaultValue;
		}
		
		public String toString() {
			return //uniqueID+" "+
					name+"="+value;
		}
	};
	
	private class Scratch3List {
		public String name;
		public ArrayList<Double> contents;

		public Scratch3List(String _name) {
			name=_name;
			contents=new ArrayList<Double>();
		}
	};
	
	@SuppressWarnings("serial")
	private class Scratch3Variables extends ArrayList<Scratch3Variable> {
		public Scratch3Variables deepCopy() {
			Scratch3Variables copy = new Scratch3Variables();
			for(Scratch3Variable v : this) {
				copy.add(new Scratch3Variable(v.name,v.uniqueID,null));
			}
			return copy;
		}
	}

	private class Scratch3Procedure {
		public String proccode;  // name of procedure
		public String uniqueID;
		public Scratch3Variables parameters = new Scratch3Variables();
		
		public Scratch3Procedure(String uniqueID,String proccode) {
			this.uniqueID = uniqueID;
			this.proccode = proccode;
		}
		
		public String toString() {
			return //uniqueID+" "+
					proccode+parameters.toString();
		}
	}
	
	private FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeScratch3"),"SB3");
	
	private Scratch3Variables scratchGlobalVariables;
	private Stack<Scratch3Variables> myStack = new Stack<>();
	
	private List<Scratch3List> scratchLists = new ArrayList<>();
	private List<Scratch3Procedure> scratchProcedures = new ArrayList<>();
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
		JSONObject tree = getTreeFromInputStream(in);
		
		if(!confirmAtLeastVersion3(tree)) throw new Exception("File must be at least version 3.0.0.");
		if(!confirmHasPenExtension(tree)) throw new Exception("File must include pen extension.");
		
		readScratchVariables(tree);
		readScratchLists(tree);
		findBlocks(tree);
		readScratchProcedures();
		readScratchInstructions();

		return myTurtle;
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
		ZipInputStream zipInputStream = new ZipInputStream(in);
		ZipEntry entry;
		File tempZipFile=null;
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
                return tempZipFile;
	        }
		}
		throw new FileNotFoundException("SB3 missing project.json");
	}

	private void findBlocks(JSONObject tree) throws Exception {
		JSONArray targets = (JSONArray)tree.get("targets");
		Iterator<?> targetIter = targets.iterator();
		while(targetIter.hasNext()) {
			JSONObject targetN = (JSONObject)targetIter.next();
			if( (Boolean)targetN.get("isStage") == true ) continue;
			blocks = targetN.getJSONObject("blocks");
			// we found the blocks.
			logger.debug("found {} blocks", blocks.length());
			// get the keys, too.
			blockKeys = blocks.keySet();
			
			return;
		}
		throw new Exception("targets > blocks missing");
	}
	
	/**
	 * parse blocks in scratch
	 * @param tree the JSONObject tree read from the project.json/zip file.
	 * @throws Exception
	 */
	private void readScratchInstructions() throws Exception {
		logger.debug("readScratchInstructions");
		
		// find the first block with opcode=event_whenflagclicked.
		for( String k : blockKeys ) {
			JSONObject block = blocks.getJSONObject(k.toString());
			String opcode = block.getString("opcode");
			if(opcode.equals("event_whenflagclicked")) {
				parseScratchCode(k);
				return;
			}
		}
		throw new Exception("WhenFlagClicked block not found.");
	}
	
	private JSONObject getBlock(String key) {
		return blocks.getJSONObject(key);
	}
	
	private String findNextBlockKey(JSONObject currentBlock) {
		Object key = currentBlock.opt("next");
		if(key==null || key == JSONObject.NULL) return null;
		return (String)key;
	}
	
	private void parseScratchCode(String currentKey) throws Exception {
		logger.debug("parseScratchCode {}",currentKey);
		JSONObject currentBlock = getBlock(currentKey);
				
		while(currentBlock!=null) {
			String opcode = (String)currentBlock.get("opcode");			
			switch(opcode) {
			// control blocks start
			case "event_whenflagclicked":	doStart(currentBlock);					break;
			case "control_repeat":			doRepeat(currentBlock);  				break;
			case "control_repeat_until":	doRepeatUntil(currentBlock);			break;
			case "control_forever":			doRepeatForever(currentBlock);			break;
			case "control_if":				doIf(currentBlock);						break;
			case "control_if_else":			doIfElse(currentBlock);					break;
			case "control_stop":
				//throw new Exception("control_stop not supported.");
				return;
			case "procedures_call":			doCall(currentBlock);					break;
			// control blocks end

			case "data_setvariableto":		setVariableTo(currentBlock);			break;
			case "data_changevariableby":	changeVariableBy(currentBlock);			break;
/*			case "data_variable":													break;
			case "data_hidevariable":												break;
			case "data_showvariable":												break;
			case "data_listcontents":												break;
			case "data_addtolist":													break;
			case "data_deleteoflist":												break;
			case "data_deletealloflist":											break;
			case "data_insertatlist":												break;
			case "data_replaceitemoflist":											break;
			case "data_itemoflist":													break;
			case "data_itemnumoflist":												break;
			case "data_lengthoflist":												break;
			case "data_listcontainsitem":											break;
*/
			case "motion_gotoxy": 			doMotionGotoXY(currentBlock);  			break;
			case "motion_pointindirection": doMotionPointInDirection(currentBlock);	break;
			case "motion_turnleft":			doMotionTurnLeft(currentBlock);  		break;
			case "motion_turnright":		doMotionTurnRight(currentBlock);  		break;
			case "motion_movesteps":		doMotionMoveSteps(currentBlock);		break;
			//case "motion_pointtowards": 	doMotionPointTowards(currentBlock);  break;
			case "motion_changexby": 		doMotionChangeX(currentBlock);  		break;
			case "motion_changeyby": 		doMotionChangeY(currentBlock);  		break;
			case "motion_setx": 			doMotionSetX(currentBlock);  			break;
			case "motion_sety": 			doMotionSetY(currentBlock);  			break;
			case "pen_penDown":				myTurtle.penDown();						break;
			case "pen_penUp":				myTurtle.penUp();						break;
			default: logger.debug("Ignored {}", opcode);
			}

			currentKey = findNextBlockKey(currentBlock);
			if(currentKey==null) break;
			
			logger.debug("next block {}",currentKey);
			currentBlock = getBlock(currentKey);
		}
	}

	private void doStart(JSONObject currentBlock) {
		logger.debug("START");
		// reset the turtle object
		myTurtle = new Turtle();
	}

	private void doIfElse(JSONObject currentBlock) throws Exception {
		logger.debug("IF ELSE");
		String condition = (String)findInputInBlock(currentBlock,"CONDITION");
		String substack = (String)findInputInBlock(currentBlock,"SUBSTACK");
		String substack2 = (String)findInputInBlock(currentBlock,"SUBSTACK2");
		if(resolveBoolean(getBlock(condition))) {
			parseScratchCode(substack);
		} else {
			parseScratchCode(substack2);
		}
	}
	
	private void doCall(JSONObject currentBlock) throws Exception {
		String proccode = (String)findMutationInBlock(currentBlock,"proccode");
		ArrayList<Object> args = resolveArgumentsForProcedure(currentBlock);
		logger.debug("CALL {}({})",proccode,args.toString());
		
		Scratch3Procedure p = findProcedureWithProccode(proccode);
		pushStack(p,args);
		parseScratchCode(getBlock(p.uniqueID).getString("next"));
		myStack.pop();
	}
	
	private ArrayList<Object> resolveArgumentsForProcedure(JSONObject currentBlock) throws Exception {
		ArrayList<Object> args = new ArrayList<>();
		
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray argumentids = new JSONArray((String)findMutationInBlock(currentBlock,"argumentids"));
		Iterator<Object> iter = argumentids.iterator();
		while(iter.hasNext()) {
			JSONArray key = (JSONArray)inputs.get((String)iter.next());
			args.add(resolveValue(key.get(1)));
		}

		return args;
	}

	// copy the parameters, set the values based on what was passed into the procedure, and then push that onto the stack.
	private void pushStack(Scratch3Procedure p, ArrayList<Object> args) {
		Scratch3Variables list = p.parameters.deepCopy();
		for(int i=0;i<list.size();++i) {
			list.get(i).value = args.get(i);
		}
		
		myStack.push(list);
	}

	private Scratch3Procedure findProcedureWithProccode(String proccode) {
		Iterator<Scratch3Procedure> iter = scratchProcedures.iterator();
		while(iter.hasNext()) {
			Scratch3Procedure p = iter.next();
			if(p.proccode.equals(proccode)) return p;
		}
		return null;
	}

	private void doIf(JSONObject currentBlock) throws Exception {
		logger.debug("IF");
		String condition = (String)findInputInBlock(currentBlock,"CONDITION");
		String substack = (String)findInputInBlock(currentBlock,"SUBSTACK");
		if(resolveBoolean(getBlock(condition))) {
			parseScratchCode(substack);
		}
	}

	private void doRepeatForever(JSONObject currentBlock) throws Exception {
		throw new Exception(Translator.get("LoadScratch3.foreverNotAllowed"));
		// technically this would work and the program would never end.  It is here for reference.
		//logger.debug("REPEAT FOREVER");
		//String substack = (String)findInputInBlock(currentBlock,"SUBSTACK");
		//while(true) {
		//	parseScratchCode(substack);
		//}
	}

	private void doRepeatUntil(JSONObject currentBlock) throws Exception {
		logger.debug("REPEAT UNTIL");
		String condition = (String)findInputInBlock(currentBlock,"CONDITION");
		String substack = (String)findInputInBlock(currentBlock,"SUBSTACK");
		
		while(!resolveBoolean(getBlock(condition))) {
			parseScratchCode(substack);
		}
	}

	private void doRepeat(JSONObject currentBlock) throws Exception {
		int count = (int)resolveValue(findInputInBlock(currentBlock,"TIMES"));
		String substack = (String)findInputInBlock(currentBlock,"SUBSTACK");
		logger.debug("REPEAT {}",count);
		for(int i=0;i<count;++i) {
			parseScratchCode(substack);
		}		
	}

	// relative change
	private void changeVariableBy(JSONObject currentBlock) throws Exception {
		Scratch3Variable v = getScratchVariable((String)findFieldsInBlock(currentBlock,"VARIABLE"));
		double newValue = resolveValue(findInputInBlock(currentBlock,"VALUE"));
		// set and report
		v.value = (double)v.value + newValue;
		logger.debug("Set {} to {}", v.name, v.value);
	}

	// absolute change
	private void setVariableTo(JSONObject currentBlock) throws Exception {
		Scratch3Variable v = getScratchVariable((String)findFieldsInBlock(currentBlock,"VARIABLE"));
		double newValue = resolveValue(findInputInBlock(currentBlock,"VALUE"));
		// set and report
		v.value = newValue;
		logger.debug("Set {} to {}", v.name, v.value);
	}

	private void doMotionGotoXY(JSONObject currentBlock) throws Exception {
		double px = resolveValue(findInputInBlock(currentBlock,"X"));
		double py = resolveValue(findInputInBlock(currentBlock,"Y"));
		logger.debug("GOTO {} {}",px,py);
		myTurtle.moveTo(px, py);
	}

	private void doMotionPointInDirection(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"DIRECTION"));
		logger.debug("POINT AT {}",v);
		myTurtle.setAngle(v);
	}
	
	private void doMotionTurnLeft(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"DEGREES"));
		logger.debug("LEFT {}",v);
		myTurtle.setAngle(myTurtle.getAngle()+v);
	}
	
	private void doMotionTurnRight(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"DEGREES"));
		logger.debug("RIGHT {}",v);
		myTurtle.setAngle(myTurtle.getAngle()-v);
	}

	private void doMotionMoveSteps(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"STEPS"));
		logger.debug("MOVE {}",v);
		myTurtle.forward(v);
	}
	
	private void doMotionChangeX(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"DX"));
		logger.debug("MOVE X {}",v);
		myTurtle.moveTo(myTurtle.getX()+v,myTurtle.getY());
	}

	private void doMotionChangeY(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"DY"));
		logger.debug("MOVE Y {}",v);
		myTurtle.moveTo(myTurtle.getX(),myTurtle.getY()+v);
		
	}

	private void doMotionSetX(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"X"));
		logger.debug("SET X {}",v);
		myTurtle.moveTo(v,myTurtle.getY());
	}

	private void doMotionSetY(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"Y"));
		logger.debug("SET Y {}",v);
		myTurtle.moveTo(myTurtle.getX(),v);
	}
	
	/**
	 * Find and return currentBlock/fields/subKey/(first element). 
	 * @param currentBlock the block to search.
	 * @param subKey the key name inside currentBlock.
	 * @return the first element of currentBlock/inputs/subKey
	 * @throws Exception if any part of the operation fails, usually because of non-existent key.
	 */
	private Object findFieldsInBlock(JSONObject currentBlock,String subKey) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("fields");
		JSONArray subKeyArray = (JSONArray)inputs.get(subKey);
		return subKeyArray.get(1);
	}
	
	/**
	 * Find and return currentBlock/inputs/subKey/(first element). 
	 * @param currentBlock the block to search.
	 * @param subKey the key name inside currentBlock.
	 * @return the first element of currentBlock/inputs/subKey
	 * @throws Exception if any part of the operation fails, usually because of non-existent key.
	 */
	private Object findInputInBlock(JSONObject currentBlock,String subKey) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray subKeyArray = (JSONArray)inputs.get(subKey);
		return subKeyArray.get(1);
	}

	/**
	 * Find and return currentBlock/mutation/subKey. 
	 * @param currentBlock the block to search.
	 * @param subKey the key name inside currentBlock.
	 * @return the element currentBlock/mutation/subKey
	 * @throws Exception if any part of the operation fails, usually because of non-existent key.
	 */
	private Object findMutationInBlock(JSONObject currentBlock,String subKey) throws Exception {
		JSONObject mutation = currentBlock.getJSONObject("mutation");
		return mutation.get(subKey);
	}

	/**
	 * Find and return the variable with uniqueID.  Search the top of myStack first, then the globals. 
	 * @param uniqueID the id to match.
	 * @return the variable found.
	 * @throws Exception if variable not found.
	 */
	private Scratch3Variable getScratchVariable(String uniqueID) throws Exception {
		if(!myStack.isEmpty()) {
			for(Scratch3Variable sv : myStack.peek()) {
				if(sv.uniqueID.equals(uniqueID)) return sv;
				if(sv.name.equals(uniqueID)) return sv;
			}
		}
		
		for(Scratch3Variable sv : scratchGlobalVariables) {
			if(sv.uniqueID.equals(uniqueID)) return sv;
			if(sv.name.equals(uniqueID)) return sv;
		}
		
		throw new Exception("Variable '"+uniqueID+"' not found.");
	}

	/**
	 * Confirm this is version 3
	 * @param tree the JSONObject tree read from the project.json/zip file.
	 * @throws Exception
	 */
	private boolean confirmAtLeastVersion3(JSONObject tree) throws Exception {
		JSONObject meta = (JSONObject)tree.get("meta");  // this cannot be getJSONObject because it changes the exception response.
		if(meta==null) return false;
		
		String semver = (String)meta.get("semver");  // this cannot be getJSONObject because it changes the exception response.
		if(semver==null) return false;
		
		return ( semver.compareTo("3.0.0") <= 0 ); 
	}
	
	private boolean confirmHasPenExtension(JSONObject tree) throws Exception {
		JSONArray extensions = (JSONArray)tree.get("extensions");  // this cannot be getJSONObject because it changes the exception response.
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
		scratchGlobalVariables = new Scratch3Variables();
		JSONArray targets = tree.getJSONArray("targets");
		Iterator<?> targetIter = targets.iterator();
		while(targetIter.hasNext()) {
			JSONObject targetN = (JSONObject)targetIter.next();
			if( (Boolean)targetN.get("isStage") == false ) continue;
			
			JSONObject variables = targetN.getJSONObject("variables");
			Iterator<?> keys = variables.keySet().iterator();
			while(keys.hasNext()) {
				String k=(String)keys.next();
				JSONArray details = variables.getJSONArray(k);
				String name = details.getString(0);
				Object valueUnknown = details.get(1);
				Number value;
				if(valueUnknown instanceof String) value = Double.parseDouble((String)valueUnknown); 
				else value = (Number)valueUnknown;
				try {
					double d = value.doubleValue();
					logger.debug("Variable {} {} {}", k, name, d);
					scratchGlobalVariables.add(new Scratch3Variable(name,k,d));
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
		JSONArray targets = tree.getJSONArray("targets");
		Iterator<?> targetIter = targets.iterator();
		while(targetIter.hasNext()) {
			JSONObject targetN = (JSONObject)targetIter.next();
			if( (Boolean)targetN.get("isStage") == false ) continue;
			JSONObject listOfLists = targetN.getJSONObject("lists");
			if(listOfLists == null) return;
			Set<?> keys = listOfLists.keySet();
			Iterator<?> keyIter = keys.iterator();
			while( keyIter.hasNext() ) {
				String key = (String)keyIter.next();
				logger.debug("list key:{}", key);
				JSONArray elem = listOfLists.getJSONArray(key);
				String listName = elem.getString(0);
				logger.debug("  list name:{}", listName);
				Object contents = elem.get(1);
				Scratch3List list = new Scratch3List(listName);
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
	 * Read in and store the description of procedures (methods)
	 * @throws Exception
	 */
	private void readScratchProcedures() throws Exception {
		logger.debug("readScratchProcedures");

		// find the blocks with opcode=procedures_definition.
		for( String k : blockKeys ) {
			String uniqueID = k.toString();
			Object obj = blocks.get(uniqueID);
			if(!(obj instanceof JSONObject)) continue;
			
			JSONObject currentBlock = blocks.getJSONObject(uniqueID);
			String opcode = currentBlock.getString("opcode");
			if(opcode.equals("procedures_definition")) {
				// the procedures_definition block points to the procedures_prototype block
				JSONObject prototypeBlock = getBlock((String)findInputInBlock(currentBlock,"custom_block"));
				// which contains the human-readable name of the procedure
				String proccode = (String)findMutationInBlock(prototypeBlock,"proccode");
				
				Scratch3Procedure p = new Scratch3Procedure(uniqueID,proccode);
				scratchProcedures.add(p);
				buildParameterListForProcedure(prototypeBlock,p);
				logger.debug("procedure found: {}",p.toString());
			}
		}
	}
	
	private void buildParameterListForProcedure(JSONObject prototypeBlock, Scratch3Procedure p) throws Exception {
		JSONArray argumentIDs = new JSONArray((String)findMutationInBlock(prototypeBlock,"argumentids"));
		JSONArray argumentNames = new JSONArray((String)findMutationInBlock(prototypeBlock,"argumentnames"));
		//JSONArray argumentDefaults = new JSONArray((String)findMutationInBlock(prototypeBlock,"argumentdefaults"));
		for(int i=0;i<argumentIDs.length();++i) {
			String uniqueID = argumentIDs.getString(i);
			//String defaultValue = argumentDefaults.getString(i);
			String name = argumentNames.getString(i);
			p.parameters.add(new Scratch3Variable(name,uniqueID,0/*defaultValue*/));
			// TODO set defaults?
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
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray OPERAND = (JSONArray)inputs.getJSONArray("OPERAND");
		boolean a = resolveBoolean(getBlock(OPERAND.getString(1)));
		return !a;
	}

	private boolean doOr(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray OPERAND1 = (JSONArray)inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = (JSONArray)inputs.getJSONArray("OPERAND2");
		boolean a = resolveBoolean(getBlock(OPERAND1.getString(1)));
		boolean b = resolveBoolean(getBlock(OPERAND2.getString(1)));
		return a || b;
	}

	private boolean doAnd(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray OPERAND1 = (JSONArray)inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = (JSONArray)inputs.getJSONArray("OPERAND2");
		boolean a = resolveBoolean(getBlock(OPERAND1.getString(1)));
		boolean b = resolveBoolean(getBlock(OPERAND2.getString(1)));
		return a && b;
	}

	private boolean doEquals(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray OPERAND1 = (JSONArray)inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = (JSONArray)inputs.getJSONArray("OPERAND2");
		double a = resolveValue(OPERAND1.get(1));
		double b = resolveValue(OPERAND2.get(1)); 
		return a == b;
	}

	private boolean doGreaterThan(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray OPERAND1 = (JSONArray)inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = (JSONArray)inputs.getJSONArray("OPERAND2");
		double a = resolveValue(OPERAND1.get(1));
		double b = resolveValue(OPERAND2.get(1)); 
		return a > b;
	}

	private boolean doLessThan(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
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
			case 4:  // number
			case 5:  // positive number
			case 6:  // positive integer
			case 7:  // integer
			case 8:  // angle
			// 9 is color (#rrggbbaa)
			case 10:  // string, try to parse as number
				return parseNumber(currentArray.get(1));
			case 12:  // variable
				return (double)getScratchVariable(currentArray.getString(2)).value; 
			// 13 is list [name,id,x,y]
			default: throw new Exception("resolveValue unknown value type "+currentArray.getInt(0));
			}
		} else if(currentObject instanceof String) {
			JSONObject currentBlock = getBlock((String)currentObject);
			// is equation
			String opcode = currentBlock.getString("opcode");
			switch(opcode) {
			case "operator_add":					return doAdd(currentBlock);
			case "operator_subtract":				return doSubstract(currentBlock);
			case "operator_multiply":				return doMultiply(currentBlock);
			case "operator_divide":					return doDivide(currentBlock);
			case "operator_random":					return doRandom(currentBlock);
			case "operator_mathop":					return doMathOp(currentBlock);
			case "motion_direction":				return doMotionDirection(currentBlock);
			case "motion_xposition":				return doMotionXPosition(currentBlock);
			case "motion_yposition":				return doMotionYPosition(currentBlock);
			case "argument_reporter_string_number":	return (double)doReporterStringValue(currentBlock);
			case "argument_reporter_boolean":		return (double)doReporterStringValue(currentBlock);
			default: throw new Exception("resolveValue unsupported opcode "+opcode);
			}
		}
		throw new Exception("resolveValue unknown object type "+currentObject.getClass().getSimpleName());
	}

	private double parseNumber(Object object) {
		if(object instanceof String)
			return Double.parseDouble((String)object);
		else if(object instanceof Double) 
			return (double)object;
		else //if(object instanceof Integer)
			return (double)(int)object;
	}

	private double doAdd(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray NUM1 = inputs.getJSONArray("NUM1");
		JSONArray NUM2 = inputs.getJSONArray("NUM2");
		double a = resolveValue(NUM1.get(1));
		double b = resolveValue(NUM2.get(1));
		return a + b;
	}

	private double doSubstract(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray NUM1 = inputs.getJSONArray("NUM1");
		JSONArray NUM2 = inputs.getJSONArray("NUM2");
		double a = resolveValue(NUM1.get(1));
		double b = resolveValue(NUM2.get(1));
		return a - b;
	}

	private double doMultiply(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray NUM1 = inputs.getJSONArray("NUM1");
		JSONArray NUM2 = inputs.getJSONArray("NUM2");
		double a = resolveValue(NUM1.get(1));
		double b = resolveValue(NUM2.get(1));
		return a * b;
	}

	private double doDivide(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray NUM1 = inputs.getJSONArray("NUM1");
		JSONArray NUM2 = inputs.getJSONArray("NUM2");
		double a = resolveValue(NUM1.get(1));
		double b = resolveValue(NUM2.get(1));
		return a / b;
	}

	private double doRandom(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray FROM = inputs.getJSONArray("FROM");
		JSONArray TO = inputs.getJSONArray("TO");
		double a = resolveValue(FROM.get(1));
		double b = resolveValue(TO.get(1));
		return Math.random() * (b-a) + a;
	}
	
	private double doMathOp(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray NUM = inputs.getJSONArray("NUM");
		double v = resolveValue(NUM.get(1));

		JSONObject fields = currentBlock.getJSONObject("fields");
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

	private double doMotionDirection(JSONObject currentBlock) throws Exception {
		return myTurtle.getAngle();
	}
	
	private double doMotionXPosition(JSONObject currentBlock) throws Exception {
		return myTurtle.getX();
	}
	
	private double doMotionYPosition(JSONObject currentBlock) throws Exception {
		return myTurtle.getY();
	}
	
	private Object doReporterStringValue(JSONObject currentBlock) throws Exception {
		String name = currentBlock.getJSONObject("fields").getJSONArray("VALUE").getString(0);
		
		if(!myStack.isEmpty()) {
			for(Scratch3Variable sv : myStack.peek()) {
				if(sv.name.equals(name)) return sv.value;
			}
		}
		throw new Exception("Variable '"+name+"' not found.");
	}

	private int getListID(Object obj) throws Exception {
		if(!(obj instanceof String)) throw new Exception("List name not a string.");
		String listName = obj.toString();
		Iterator<Scratch3List> iter = scratchLists.iterator();
		int index=0;
		while(iter.hasNext()) {
			Scratch3List i = iter.next();
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
	@SuppressWarnings("unused")
	private int resolveListIndex(Object o2,Object o3) throws Exception {
		String index = (String)o2;
		String listName = (String)o3;
		Scratch3List list = scratchLists.get(getListID(listName)); 
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
