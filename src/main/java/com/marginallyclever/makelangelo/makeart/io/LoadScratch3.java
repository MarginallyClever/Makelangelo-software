package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.List;
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
	private static int seed=0;
	private static final Random random = new Random();
	
	
	private static class Scratch3Variable {
		public String name;
		
		public String uniqueID;
		public Object value;

		public Scratch3Variable(String name,String uniqueID,Object defaultValue) {
			this.name=name;
			this.uniqueID=uniqueID;
			this.value=defaultValue;
		}
		
		@Override
		public String toString() {
			return //uniqueID+" "+
					name+"="+value;
		}
	};
	
	private static class Scratch3List {
		public String name;
		public ArrayList<Double> contents;

		public Scratch3List(String _name) {
			name=_name;
			contents=new ArrayList<Double>();
		}
	};

	private static class Scratch3Variables extends ArrayList<Scratch3Variable> {
		public Scratch3Variables deepCopy() {
			Scratch3Variables copy = new Scratch3Variables();
			for(Scratch3Variable v : this) {
				copy.add(new Scratch3Variable(v.name,v.uniqueID,null));
			}
			return copy;
		}
	}

	private static class Scratch3Procedure {
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
	
	private FileNameExtensionFilter filter = new FileNameExtensionFilter("Scratch 3","SB3");
	
	private Scratch3Variables scratchGlobalVariables;
	private final Stack<Scratch3Variables> myStack = new Stack<>();
	private final List<Scratch3List> scratchLists = new ArrayList<>();
	private final List<Scratch3Procedure> scratchProcedures = new ArrayList<>();
	private JSONObject blocks;
	private Set<String> blockKeys;
	private Turtle myTurtle;

	// used in doRepeatForever
	private final int loopNbCountInsteadOfForever = 1;// resilient : we can draw something, but it may be incomplete ...
	// used in doRepeatForever
	private final boolean foreverThrowAnException = false;// To be resilient and user-friendly this is not an error...


	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.')+1);
		return Arrays.stream(filter.getExtensions()).anyMatch(ext::equalsIgnoreCase);
	}
	
	@Override
	public Turtle load(InputStream in) throws Exception {
		if (in == null) {
			throw new NullPointerException("Input stream is null");
		}
		logger.debug("Loading...");
		JSONObject tree = getTreeFromInputStream(in);
		random.setSeed(0);
		if(!confirmAtLeastVersion3(tree)) throw new Exception("File must be at least version 3.0.0.");
		if(!confirmHasPenExtension(tree)) throw new Exception("File must include pen extension.");
		
		readScratchVariables(tree);
		readScratchLists(tree);
		findBlocks(tree);
		readScratchProcedures();
		readScratchInstructions();

		return myTurtle;
	}

	private JSONObject getTreeFromInputStream(InputStream in) throws IOException {
		File tempZipFile = extractProjectJSON(in);
		
        logger.trace("Parsing JSON file...");
        JSONTokener tokener = new JSONTokener(tempZipFile.toURI().toURL().openStream());
        JSONObject tree = new JSONObject(tokener);

		tempZipFile.delete();
		
		return tree;
	}

	private File extractProjectJSON(InputStream in) throws IOException {
		logger.trace("Searching for project.json...");
		try (ZipInputStream zipInputStream = new ZipInputStream(in)) {
			ZipEntry entry;
			File tempZipFile = null;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				if (entry.getName().equals(PROJECT_JSON)) {
					logger.trace("Found project.json...");

					// read buffered stream into temp file.
					tempZipFile = File.createTempFile("project", "json");
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
		throw new FileNotFoundException("SB3 missing project.json");
	}

	private void findBlocks(JSONObject tree) throws Exception {
		JSONArray targets = (JSONArray)tree.get("targets");
		for (Object target : targets) {
			JSONObject targetN = (JSONObject) target;
			if ((Boolean) targetN.get("isStage") ) continue;
			blocks = targetN.getJSONObject("blocks");
			// we found the blocks.
			logger.debug("found {} blocks", blocks.length());
			// get the keys, too.
			blockKeys = blocks.keySet();

			return;
		}
		throw new Exception("targets > blocks missing");
	}

	private final int nbClicOnTheGreenFlag = 1;  // Let's be basic - one click for now.

	/**
	 * parse blocks in scratch3 format
	 * @throws Exception if no program was run because no green flag was found.
	 */
	private void readScratchInstructions() throws Exception {
		logger.trace("readScratchInstructions ( and do a flagclicked {} times ) ",nbClicOnTheGreenFlag);
		myTurtle = new Turtle();// needed to be init here in case multiple "event_whenflagclicked"
		// TODO some myTurtle init to be like Scratch initial state. ( like initial color... )
		
		int flagsClickedTotal = 0;
		for (int i = 0; i < nbClicOnTheGreenFlag; i++) {
			// find the first block with opcode=event_whenflagclicked.
			int flagsClickedInThisBlock = 0;

			for (String k : blockKeys) {
				Object getTmp = blocks.get(k);
				if (getTmp instanceof JSONObject) {
					JSONObject block = (JSONObject) getTmp;
					final String key_scratch_block_opcode = "opcode";
					if (block.has(key_scratch_block_opcode)) {
						String opcode = block.getString(key_scratch_block_opcode);
						if (opcode.equals("event_whenflagclicked")) {
							parseScratchCode(k);// TODO a stack (LIFO) with k to parseScratchCode(k) later (if multiple event_whenflagclicked this is in the Scratch3 interpretor reverse order ... and for scratch timing (wait, ...) if we want to implement it.)							
							flagsClickedInThisBlock++;
						}
					} else {
						// starge no opcode ... but to be resilient no exception .
						logger.debug("no {} for block {} : {} // instanceof {}",key_scratch_block_opcode, k, getTmp, getTmp.getClass().toString());
					}
				} else {					
					if ( getTmp != null ){
						// somethinge is not what expected ... maybe a lost variable in the scratch projet ...
						logger.debug("not expected for block {} : {} // instanceof {}", k, getTmp, getTmp.getClass().toString());
					}else{
						// normaly should not happend but juste to be sure ...
						logger.debug("not expected for block {} : {} // null", k, getTmp);
					}
				}
			}
			flagsClickedTotal += flagsClickedInThisBlock;
		}
		if (flagsClickedTotal == 0) {
			throw new Exception("WhenFlagClicked block not found.");
		}
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
		logger.trace("parseScratchCode {}",currentKey);
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
			case "pen_setPenColorToColor":	doSetPenColor(currentBlock);			break;
			default: logger.debug("Ignored {}", opcode);
			}

			currentKey = findNextBlockKey(currentBlock);
			if(currentKey==null) break;
			
			logger.trace("next block {}",currentKey);
			currentBlock = getBlock(currentKey);
		}
	}

	private void doStart(JSONObject currentBlock) {
		logger.trace("START a block opcode event_whenflagclicked ...");
	}

	private void doIfElse(JSONObject currentBlock) throws Exception {
		logger.trace("IF ELSE");
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
		logger.trace("CALL {}({})",proccode,args.toString());
		
		Scratch3Procedure p = findProcedureWithProccode(proccode);
		pushStack(p,args);
		parseScratchCode(getBlock(p.uniqueID).getString("next"));
		myStack.pop();
	}
	
	private ArrayList<Object> resolveArgumentsForProcedure(JSONObject currentBlock) throws Exception {
		ArrayList<Object> args = new ArrayList<>();
		
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray argumentids = new JSONArray((String)findMutationInBlock(currentBlock,"argumentids"));
		for (Object argumentid : argumentids) {
			JSONArray key = (JSONArray) inputs.get((String) argumentid);
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
		for (Scratch3Procedure p : scratchProcedures) {
			if (p.proccode.equals(proccode)) return p;
		}
		return null;
	}

	private void doIf(JSONObject currentBlock) throws Exception {
		logger.trace("IF");
		String condition = (String)findInputInBlock(currentBlock,"CONDITION");
		String substack = (String)findInputInBlock(currentBlock,"SUBSTACK");
		if(resolveBoolean(getBlock(condition))) {
			parseScratchCode(substack);
		}
	}

	/**
	 * dummy doReapeatForever. N.B. : For the current Makelangelo implementation
	 * we need to assert the Scratch program has an end. (is not infinite ...
	 * This is the easy case.) RepeatForever has to be altered (loop only n
	 * time) or seen as an error.
	 * @param currentBlock the block to parse
	 * @throws Exception if the block tries to repeat forever
	 */
	private void doRepeatForever(JSONObject currentBlock) throws Exception {
		if( foreverThrowAnException ) {
			throw new Exception(Translator.get("LoadScratch3.foreverNotAllowed"));
		}		
		logger.trace("REPEAT FOREVER ( will only repeat {} times. )", loopNbCountInsteadOfForever);
		String substack = (String)findInputInBlock(currentBlock,"SUBSTACK");		
		for (int i = 0; i < loopNbCountInsteadOfForever; i++){
			//while(true) { // technically this would work and the program would never end.  It is here for reference.
				parseScratchCode(substack);
			//}
		}		
	}

	private void doRepeatUntil(JSONObject currentBlock) throws Exception {
		logger.trace("REPEAT UNTIL");
		String condition = (String)findInputInBlock(currentBlock,"CONDITION");
		String substack = (String)findInputInBlock(currentBlock,"SUBSTACK");
		
		while(!resolveBoolean(getBlock(condition))) {
			parseScratchCode(substack);
		}
	}

	private void doRepeat(JSONObject currentBlock) throws Exception {
		int count = (int)resolveValue(findInputInBlock(currentBlock,"TIMES"));
		String substack = (String)findInputInBlock(currentBlock,"SUBSTACK");
		logger.trace("REPEAT {}",count);
		for(int i=0;i<count;++i) {
			parseScratchCode(substack);
		}		
	}

	// relative change
	private void changeVariableBy(JSONObject currentBlock) throws Exception {
		Scratch3Variable v = getScratchVariable((String)findFieldsInBlock(currentBlock));
		double newValue = resolveValue(findInputInBlock(currentBlock,"VALUE"));
		// set and report
		v.value = (double)v.value + newValue;
		logger.trace("Set {} to {}", v.name, v.value);
	}

	// absolute change
	private void setVariableTo(JSONObject currentBlock) throws Exception {
		Scratch3Variable v = getScratchVariable((String)findFieldsInBlock(currentBlock));
		// set and report
		v.value = resolveValue(findInputInBlock(currentBlock,"VALUE"));
		logger.trace("Set {} to {}", v.name, v.value);
	}

	private void doMotionGotoXY(JSONObject currentBlock) throws Exception {
		double px = resolveValue(findInputInBlock(currentBlock,"X"));
		double py = resolveValue(findInputInBlock(currentBlock,"Y"));
		logger.trace("GOTO {} {}",px,py);
		myTurtle.moveTo(px, py);
	}

	private void doMotionPointInDirection(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"DIRECTION"));
		logger.trace("POINT AT {}",v);
		myTurtle.setAngle(90.0-v);// axial symmetry of an axis having an angle of 45° see https://github.com/MarginallyClever/Makelangelo-software/issues/564#issuecomment-1046217070
	}
	
	private void doMotionTurnLeft(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"DEGREES"));
		logger.trace("LEFT {}",v);
		myTurtle.setAngle(myTurtle.getAngle()+v);//myTurtle.turn(v);
	}
	
	private void doMotionTurnRight(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"DEGREES"));
		logger.trace("RIGHT {}",v);
		myTurtle.setAngle(myTurtle.getAngle()-v);//myTurtle.turn(-v);
	}

	private void doMotionMoveSteps(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"STEPS"));
		logger.trace("MOVE {}",v);
		myTurtle.forward(v);
	}
	
	private void doMotionChangeX(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"DX"));
		logger.trace("MOVE X {}",v);
		myTurtle.moveTo(myTurtle.getX()+v,myTurtle.getY());
	}

	private void doMotionChangeY(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"DY"));
		logger.trace("MOVE Y {}",v);
		myTurtle.moveTo(myTurtle.getX(),myTurtle.getY()+v);
		
	}

	private void doMotionSetX(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"X"));
		logger.trace("SET X {}",v);
		myTurtle.moveTo(v,myTurtle.getY());
	}

	private void doMotionSetY(JSONObject currentBlock) throws Exception {
		double v = resolveValue(findInputInBlock(currentBlock,"Y"));
		logger.trace("SET Y {}",v);
		myTurtle.moveTo(myTurtle.getX(),v);
	}
	
	boolean ignoreDoSetPenColor = false; // As setColor can bug the Makelangelo Render a quick/bad hack to enable/disable this implementation.
	private void doSetPenColor(JSONObject currentBlock) throws Exception {
		Color c = new Color((int)resolveValue(findInputInBlock(currentBlock,"COLOR")));
		if ( !ignoreDoSetPenColor ){
			logger.trace("SET COLOR {}",c);
			myTurtle.setStroke(c);
		}else{
			logger.trace("SET COLOR {} ignored",c);
		}
	}
	
	/**
	 * Find and return currentBlock/fields/subKey/(first element).
	 *
	 * @param currentBlock the block to search.
	 * @return the first element of currentBlock/inputs/subKey
	 */
	private Object findFieldsInBlock(JSONObject currentBlock) {
		JSONObject inputs = currentBlock.getJSONObject("fields");
		JSONArray subKeyArray = (JSONArray)inputs.get("VARIABLE");
		return subKeyArray.get(1);
	}
		
	/**
	 * Find and return currentBlock/inputs/subKey/(first element). 
	 * @param currentBlock the block to search.
	 * @param subKey the key name inside currentBlock.
	 * @return the first element of currentBlock/inputs/subKey
	 */
	private Object findInputInBlock(JSONObject currentBlock,String subKey) {
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
			}
		}
		
		for(Scratch3Variable sv : scratchGlobalVariables) {
			if(sv.uniqueID.equals(uniqueID)) return sv;
		}
		
		throw new Exception("Variable '"+uniqueID+"' not found.");
	}

	/**
	 * Confirm this is version 3
	 * @param tree the JSONObject tree read from the project.json/zip file.
	 */
	private boolean confirmAtLeastVersion3(JSONObject tree) {
		JSONObject meta = (JSONObject)tree.get("meta");  // this cannot be getJSONObject because it changes the exception response.
		if(meta==null) return false;
		
		String semver = (String)meta.get("semver");  // this cannot be getJSONObject because it changes the exception response.
		if(semver==null) return false;
		
		return ( semver.compareTo("3.0.0") <= 0 ); 
	}
	
	private boolean confirmHasPenExtension(JSONObject tree) {
		JSONArray extensions = (JSONArray)tree.get("extensions");  // this cannot be getJSONObject because it changes the exception response.
		if(extensions==null) return false;

		for (Object o : extensions) {
			if (o instanceof String && o.equals("pen")) return true;
		}
		return false;
	}
	
	/**
	 * read the list of Scratch variables
	 * @param tree the JSONObject tree read from the project.json/zip file.
	 * @throws Exception
	 */
	private void readScratchVariables(JSONObject tree) throws Exception {
		logger.trace("readScratchVariables");
		scratchGlobalVariables = new Scratch3Variables();
		JSONArray targets = tree.getJSONArray("targets");
		for (Object target : targets) {
			JSONObject targetN = (JSONObject) target;
			if (!((Boolean) targetN.get("isStage"))) continue;

			JSONObject variables = targetN.getJSONObject("variables");
			for (String k : variables.keySet()) {
				JSONArray details = variables.getJSONArray(k);
				String name = details.getString(0);
				Object valueUnknown = details.get(1);
				Number value;
				if (valueUnknown instanceof String) value = Double.parseDouble((String) valueUnknown);
				else value = (Number) valueUnknown;
				try {
					double d = value.doubleValue();
					logger.debug("Variable {} {}", name, d);
					scratchGlobalVariables.add(new Scratch3Variable(name, k, d));
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
		logger.trace("readScratchLists");
		JSONArray targets = tree.getJSONArray("targets");
		for (Object target : targets) {
			JSONObject targetN = (JSONObject) target;
			if (!((Boolean) targetN.get("isStage"))) continue;
			JSONObject listOfLists = targetN.getJSONObject("lists");
			if (listOfLists == null) return;
			Set<?> keys = listOfLists.keySet();
			for (Object o : keys) {
				String key = (String) o;
				logger.trace("list key:{}", key);
				JSONArray elem = listOfLists.getJSONArray(key);
				String listName = elem.getString(0);
				logger.trace("  list name:{}", listName);
				Object contents = elem.get(1);
				Scratch3List list = new Scratch3List(listName);
				// fill the list with any given contents
				if ( contents instanceof JSONArray) {
					JSONArray arr = (JSONArray) contents;

					for (Object varValue : arr) {
						double value;
						if (varValue instanceof Number) {
							Number num = (Number) varValue;
							value = (float) num.doubleValue();
							logger.trace("  list float:{}", value);
							list.contents.add(value);
						} else if (varValue instanceof String) {
							try {
								value = Double.parseDouble((String) varValue);
								logger.trace("  list string:{}", value);
								list.contents.add(value);
							} catch (Exception e) {
								throw new Exception("List variables must be numbers.", e);
							}
						} else
							throw new Exception("List variable " + listName + "(" + list.contents.size() + ") is " + varValue.toString());
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
		logger.trace("readScratchProcedures");

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
				
				Scratch3Procedure p = new Scratch3Procedure(uniqueID, proccode);
				scratchProcedures.add(p);
				buildParameterListForProcedure(prototypeBlock,p);
				logger.trace("procedure found: {}",p.toString());
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
	 * @param currentBlock a String, Number, or JSONArray of elements to be calculated.
	 * @return the calculated final value.
	 * @throws Exception when an upsupported opcode is found.
	 */
	private boolean resolveBoolean(JSONObject currentBlock) throws Exception {
		if(currentBlock.has("opcode")) {
			// is equation
			String opcode = currentBlock.getString("opcode");
			return switch (opcode) {
				case "operator_lt" -> doLessThan(currentBlock);
				case "operator_gt" -> doGreaterThan(currentBlock);
				case "operator_equals" -> doEquals(currentBlock);
				case "operator_and" -> doAnd(currentBlock);
				case "operator_or" -> doOr(currentBlock);
				case "operator_not" -> doNot(currentBlock);
				default -> throw new Exception("resolveBoolean unsupported opcode " + opcode);
			};
		}
		throw new Exception("Parse error (resolveBoolean missing opcode)");
	}
	
	private boolean doNot(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray OPERAND = inputs.getJSONArray("OPERAND");
		boolean a = resolveBoolean(getBlock(OPERAND.getString(1)));
		return !a;
	}

	private boolean doOr(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray OPERAND1 = inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = inputs.getJSONArray("OPERAND2");
		boolean a = resolveBoolean(getBlock(OPERAND1.getString(1)));
		boolean b = resolveBoolean(getBlock(OPERAND2.getString(1)));
		return a || b;
	}

	private boolean doAnd(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray OPERAND1 = inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = inputs.getJSONArray("OPERAND2");
		boolean a = resolveBoolean(getBlock(OPERAND1.getString(1)));
		boolean b = resolveBoolean(getBlock(OPERAND2.getString(1)));
		return a && b;
	}

	private boolean doEquals(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray OPERAND1 = inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = inputs.getJSONArray("OPERAND2");
		double a = resolveValue(OPERAND1.get(1));
		double b = resolveValue(OPERAND2.get(1)); 
		return a == b;
	}

	private boolean doGreaterThan(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray OPERAND1 = inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = inputs.getJSONArray("OPERAND2");
		double a = resolveValue(OPERAND1.get(1));
		double b = resolveValue(OPERAND2.get(1)); 
		return a > b;
	}

	private boolean doLessThan(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray OPERAND1 = inputs.getJSONArray("OPERAND1");
		JSONArray OPERAND2 = inputs.getJSONArray("OPERAND2");
		double a = resolveValue(OPERAND1.get(1));
		double b = resolveValue(OPERAND2.get(1)); 
		return a < b;
	}

	/**
	 * Scratch block contains an Operator (variable, constant, or math combination of the two). 
	 * @param currentObject a String, Number, or JSONArray of elements to be calculated.
	 * @return the calculated final value.
	 * @throws Exception when an unknown value type is found.
	 */
	private double resolveValue(Object currentObject) throws Exception {
		if(currentObject instanceof JSONArray) {
			JSONArray currentArray = (JSONArray)currentObject;
			return switch (currentArray.getInt(0)) {
				case 4, 5, 6, 7, 8, 9, 10 ->
					// 4 number
					// 5 positive number
					// 6 positive integer
					// 7 integer
					// 8 angle
					// 9 color (#rrggbbaa)
					// 10 string, try to parse as number
						parseNumber(currentArray.get(1));
				case 12 ->  // 12 variable
						(double) getScratchVariable(currentArray.getString(2)).value;
				// 13 is list [name,id,x,y]
				default -> throw new Exception("resolveValue unknown value type " + currentArray.getInt(0));
			};
		} else if(currentObject instanceof String) {
			JSONObject currentBlock = getBlock((String)currentObject);
			// is equation
			String opcode = currentBlock.getString("opcode");
			return switch (opcode) {
				case "operator_add" -> doAdd(currentBlock);
				case "operator_subtract" -> doSubstract(currentBlock);
				case "operator_multiply" -> doMultiply(currentBlock);
				case "operator_divide" -> doDivide(currentBlock);
				case "operator_mod" -> doModulus(currentBlock);
				case "operator_random" -> doRandom(currentBlock);
				case "operator_mathop" -> doMathOp(currentBlock);
				case "operator_round" -> doRound(currentBlock);
				case "motion_direction" -> doMotionDirection();
				case "motion_xposition" -> doMotionXPosition();
				case "motion_yposition" -> doMotionYPosition();
				case "argument_reporter_string_number", "argument_reporter_boolean" -> (double) doReporterStringValue(currentBlock);
				default -> throw new Exception("resolveValue unsupported opcode " + opcode);
			};
		}
		throw new Exception("resolveValue unknown object type "+currentObject.getClass().getSimpleName());
	}
	
	private double parseNumber(Object object) {
		if(object instanceof String) {
			String str = (String)object;
			if(str.startsWith("#")) return (double)Integer.parseInt(str.substring(1),16);
			return Double.parseDouble(str);
		} else if(object instanceof Double) 
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

	private double doModulus(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray NUM1 = inputs.getJSONArray("NUM1");
		JSONArray NUM2 = inputs.getJSONArray("NUM2");
		double a = resolveValue(NUM1.get(1));
		double b = resolveValue(NUM2.get(1));
		return a % b;
	}
	
	private double doRandom(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray FROM = inputs.getJSONArray("FROM");
		JSONArray TO = inputs.getJSONArray("TO");
		double a = resolveValue(FROM.get(1));
		double b = resolveValue(TO.get(1));
		return random.nextDouble() * (b-a) + a;
	}
	
	private double doRound(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray NUM = inputs.getJSONArray("NUM");
		double a = resolveValue(NUM.get(1));
		return Math.round(a);
	}
	
	private double doMathOp(JSONObject currentBlock) throws Exception {
		JSONObject inputs = currentBlock.getJSONObject("inputs");
		JSONArray NUM = inputs.getJSONArray("NUM");
		double v = resolveValue(NUM.get(1));

		JSONObject fields = currentBlock.getJSONObject("fields");
		JSONArray OPERATOR = fields.getJSONArray("OPERATOR");
		return switch (OPERATOR.getString(0)) {
			case "abs" -> Math.abs(v);
			case "floor" -> Math.floor(v);
			case "ceiling" -> Math.ceil(v);
			case "sqrt" -> Math.sqrt(v);
			case "sin" -> Math.sin(Math.toRadians(v));
			case "cos" -> Math.cos(Math.toRadians(v));
			case "tan" -> Math.tan(Math.toRadians(v));
			case "asin" -> Math.asin(Math.toRadians(v));
			case "acos" -> Math.acos(Math.toRadians(v));
			case "atan" -> Math.atan(Math.toRadians(v));
			case "ln" -> Math.log(v);
			case "log" -> Math.log10(v);
			case "e ^" -> Math.exp(v);
			case "10 ^" -> Math.pow(10, v);
			default -> throw new Exception("doMathOp unknown operator " + OPERATOR.getString(1));
		};
	}

	private double doMotionDirection() {
		return myTurtle.getAngle();
	}
	
	private double doMotionXPosition() {
		return myTurtle.getX();
	}
	
	private double doMotionYPosition() {
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
		int index=0;
		for (Scratch3List i : scratchLists) {
			if (i.name.equals(listName)) return index;
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
			listIndex = (int) (random.nextDouble() * list.contents.size());
		} else {
			listIndex = Integer.parseInt(index);
		}

		return listIndex;
	}
}
