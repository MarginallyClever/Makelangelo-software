package com.marginallyclever.makelangeloRobot.loadAndSave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.ImageManipulator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.generators.Turtle;

/**
 * LoadAndSaveSB2 loads limited set of Scratch commands into memory. 
 * @author Admin
 *
 */
public class LoadAndSaveSB2 extends ImageManipulator implements LoadAndSaveFileType {
	private final String PROJECT_JSON = "project.json";
	
	class ScratchVariable {
		public String name;
		public float value;

		ScratchVariable() {
			name="";
			value=0;
		}
		ScratchVariable(String arg0,float arg1) {
			name=arg0;
			value=arg1;
		}
	};
	
	private FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeSB2"), "SB2");
	private Turtle turtle;
	LinkedList<ScratchVariable> scratchVariables;
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".sb2"));
	}

	@Override
	public boolean canSave(String filename) {
		return false;
	}

	
	@Override
	public boolean load(InputStream in,MakelangeloRobot robot) {
		Log.info(Translator.get("FileTypeSB2")+"...");
		// set up a temporary file
		File tempGCodeFile;
		try {
			tempGCodeFile = File.createTempFile("temp", ".ngc");
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		tempGCodeFile.deleteOnExit();
		Log.info(Translator.get("Converting") + " " + tempGCodeFile.getName());

		try (FileOutputStream fileOutputStream = new FileOutputStream(tempGCodeFile);
				Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
			
		

			if(robot.getSettings().isReverseForGlass()) {
				Log.info("Flipping for glass...");
			}

			machine = robot.getSettings();
			
			try {
				// open zip file
	        	System.out.println("Searching for project.json...");
	        	
				ZipInputStream zipInputStream = new ZipInputStream(in);
				
				// locate project.json
				ZipEntry entry;
				boolean found=false;
				while((entry = zipInputStream.getNextEntry())!=null) {
			        if( entry.getName().equals(PROJECT_JSON) ) {
			        	System.out.println("Found project.json...");
			        	
				        // read buffered stream into temp file.
			        	File tempZipFile = File.createTempFile("project", "json");
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

		    			// parse JSON
		                System.out.println("Parsing file...");
		                
			        	JSONParser parser = new JSONParser();
		    			JSONObject tree = (JSONObject)parser.parse(new FileReader(tempZipFile));
		    			// we're done with the tempZipFile now that we have the JSON structure.
		    			tempZipFile.delete();
		    			
		    			// reset the turtle object
		    			turtle = new Turtle();
		    			// make sure machine state is the default.
		    			setAbsoluteMode(out);
		    			
		    			// read the list of Scratch variables
		    			scratchVariables = new LinkedList<ScratchVariable>();
		    			JSONArray variables = (JSONArray)tree.get("variables");
		    			Iterator<?> varIter = variables.iterator();
		    			while( varIter.hasNext() ) {
			    			//System.out.println("var:"+elem.toString());
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

		    			// read the sketch(es)
		    			JSONArray children = (JSONArray)tree.get("children");
		    			if(children==null) throw new Exception("JSON node 'children' missing.");
		    			//System.out.println("found children");
		    			
		    			JSONObject children0 = (JSONObject)children.get(0);
		    			JSONArray scripts = (JSONArray)children0.get("scripts");
		    			if(scripts==null) throw new Exception("JSON node 'scripts' missing.");

		    			System.out.println("found  " +scripts.size() + " scripts");
		    			
		    			// extract known elements and convert them to gcode.
		    			Iterator<?> scriptIter = scripts.iterator();
		    			// find the script with the green flag
		    			while( scriptIter.hasNext() ) {
			    			JSONArray scripts0 = (JSONArray)scriptIter.next();
			    			if( scripts0==null ) continue;
			    			//System.out.println("scripts0");
			    			JSONArray scripts02 = (JSONArray)scripts0.get(2);
			    			if( scripts02==null || scripts02.size()==0 ) continue;
			    			//System.out.println("scripts02");
//			    			JSONObject o = (JSONObject)scripts02.get(0);
			    			//if( o!=null && o. )
			    			// actual code begins here.
			    			
			    			parseScratchArray(scripts02,out);
		    			}
		    			
		    			// load gcode
		    			
			        }
				}
				
				if(found==false) throw new Exception("SB2 missing project.json");
				
				
			} catch (Exception e) {
				Log.error(Translator.get("LoadError") +" "+ e.getLocalizedMessage());
				e.printStackTrace();
				return false;
			}
			
			
			
			// finished. Close up file.
			liftPen(out);
		    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
			out.flush();
			out.close();
	
			Log.info("Done!");
			LoadAndSaveGCode loader = new LoadAndSaveGCode();
			InputStream fileInputStream = new FileInputStream(tempGCodeFile);
			loader.load(fileInputStream,robot);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		tempGCodeFile.delete();
		
		return true;
	}
	
	int indent=0;
	boolean penUp=false;
	
	private void parseScratchArray(JSONArray script,Writer out) throws Exception {
		if(script==null) return;
		
		//for(int j=0;j<indent;++j) System.out.print("  ");
		//System.out.println("size="+script.size());
		indent++;
		
		Iterator<?> scriptIter = script.iterator();
		// find the script with the green flag
		while( scriptIter.hasNext() ) {
			Object o = (Object)scriptIter.next();
			if( o instanceof JSONArray ) {
				JSONArray arr = (JSONArray)o;
				parseScratchArray(arr,out);
			} else {
				String name = o.toString();
				//for(int j=0;j<indent;++j) System.out.print("  ");
				//System.out.println(i+"="+name);
				
				if(name.compareTo("whenGreenFlag")==0) {
					// gcode preamble
	    			// reset the turtle object
	    			turtle = new Turtle();
	    			// make sure machine state is the default.
	    			setAbsoluteMode(out);
					System.out.println("**START**");
				} else if(name.compareTo("wait:elapsed:from:")==0) {
					// dwell - does nothing.
					Object o2 = (Object)scriptIter.next();
					@SuppressWarnings("unused")
					float seconds = resolveValue(o2);
					System.out.println("dwell "+seconds+" seconds.");
				} else if(name.compareTo("putPenUp")==0) {
					penUp=true;
					this.liftPen(out);
					System.out.println("pen up");
				} else if(name.compareTo("putPenDown")==0) {
					penUp=false;
					this.lowerPen(out);
					System.out.println("pen down");
				} else if(name.compareTo("gotoX:y:")==0) {
					Object o2 = (Object)scriptIter.next();
					float x = resolveValue(o2);
					Object o3 = (Object)scriptIter.next();
					float y = resolveValue(o3);
					
					turtle.setX(x);
					turtle.setY(y);
					this.moveTo(out, turtle.getX(), turtle.getY(), penUp);
					System.out.println("Move to ("+turtle.getX()+","+turtle.getY()+")");
				} else if(name.compareTo("changeXposBy:")==0) {
					Object o2 = (Object)scriptIter.next();
					float v = resolveValue(o2);
					turtle.setX(turtle.getX()+v);
					this.moveTo(out, turtle.getX(), turtle.getY(), penUp);
					//System.out.println("Move to ("+turtle.getX()+","+turtle.getY()+")");
				} else if(name.compareTo("changeYposBy:")==0) {
					Object o2 = (Object)scriptIter.next();
					float v = resolveValue(o2);
					turtle.setY(turtle.getY()+v);
					this.moveTo(out, turtle.getX(), turtle.getY(), penUp);
					//System.out.println("Move to ("+turtle.getX()+","+turtle.getY()+")");
				} else if(name.compareTo("forward:")==0) {
					Object o2 = (Object)scriptIter.next();
					float v = resolveValue(o2);
					turtle.move(v);
					this.moveTo(out, turtle.getX(), turtle.getY(), penUp);
					System.out.println("Move forward "+v+" mm");
				} else if(name.compareTo("turnRight:")==0) {
					Object o2 = (Object)scriptIter.next();
					float degrees = resolveValue(o2);
					turtle.turn(-degrees);
					System.out.println("Right "+degrees+" degrees.");
				} else if(name.compareTo("turnLeft:")==0) {
					Object o2 = (Object)scriptIter.next();
					float degrees = resolveValue(o2);
					turtle.turn(degrees);
					System.out.println("Left "+degrees+" degrees.");
				} else if(name.compareTo("doRepeat")==0) {
					Object o2 = (Object)scriptIter.next();
					Object o3 = (Object)scriptIter.next();
					int count = (int)resolveValue(o2);
					System.out.println("Repeat "+count+" times:");
					for(int i=0;i<count;++i) {
						parseScratchArray((JSONArray)o3,out);
					}
				} else if(name.compareTo("xpos:")==0) {
					Object o2 = (Object)scriptIter.next();
					float v = resolveValue(o2);
					turtle.setX(v);
					this.moveTo(out, turtle.getX(), turtle.getY(), penUp);
					//System.out.println("Move to ("+turtle.getX()+","+turtle.getY()+")");
				} else if(name.compareTo("ypos:")==0) {
					Object o2 = (Object)scriptIter.next();
					float v = resolveValue(o2);
					turtle.setY(v);
					this.moveTo(out, turtle.getX(), turtle.getY(), penUp);
					//System.out.println("Move to ("+turtle.getX()+","+turtle.getY()+")");
				} else if(name.compareTo("heading:")==0) {
					Object o2 = (Object)scriptIter.next();
					float degrees = resolveValue(o2);
					turtle.setAngle(degrees);
					//System.out.println("Turn to "+degrees);
				} else if(name.compareTo("setVar:to:")==0) {
					// set variable
					String varName = (String)scriptIter.next();
					Object o3 = (Object)scriptIter.next();
					float v = (float)resolveValue(o3);

					boolean foundVar=false;
					Iterator<ScratchVariable> svi = scratchVariables.iterator();
					while(svi.hasNext()) {
						ScratchVariable sv = svi.next();
						if(sv.name.compareTo(varName)==0) {
							//System.out.println("Set "+varName+" to "+v);
							sv.value = v;
							foundVar=true;
						}
					}
					if(foundVar==false) {
						throw new Exception("Variable '"+varName+"' not found.");
					}
				} else if(name.compareTo("changeVar:by:")==0) {
					// set variable
					String varName = (String)scriptIter.next();
					Object o3 = (Object)scriptIter.next();
					float v = (float)resolveValue(o3);

					boolean foundVar=false;
					Iterator<ScratchVariable> svi = scratchVariables.iterator();
					while(svi.hasNext()) {
						ScratchVariable sv = svi.next();
						if(sv.name.compareTo(varName)==0) {
							sv.value += v;
							System.out.println("Change "+varName+" by "+v+" to "+sv.value);
							foundVar=true;
						}
					}
					if(foundVar==false) {
						throw new Exception("Variable '"+varName+"' not found.");
					}
				} else if(name.compareTo("clearPenTrails")==0) {
					// Ignore this Scratch command
				} else if(name.compareTo("hide")==0) {
					// Ignore this Scratch command
				} else if(name.compareTo("show")==0) {
					// Ignore this Scratch command
				} else {
					throw new Exception("Unsupported Scratch block '"+name+"'");
				}
			}
		}
		indent--;
	}
	
	/**
	 * Scratch block contains an Operator (variable, constant, or math combination of the two). 
	 * @param obj a String, Number, or JSONArray of elements to be calculated.
	 * @return the calculated final value.
	 * @throws Exception
	 */
	private float resolveValue(Object obj) throws Exception {
		if(obj instanceof String) {
			// probably a variable
			String firstName = obj.toString();
			
			if(firstName.compareTo("xpos")==0) {
				return turtle.getX();
			}
			if(firstName.compareTo("ypos")==0) {
				return turtle.getY();
			}
			if(firstName.compareTo("heading")==0) {
				return turtle.getAngle();
			}

			try {
				float v = Float.parseFloat(firstName);
				return v;
			} catch (Exception e) {
				throw new Exception("Unresolved string value");
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
				throw new Exception("Parse error (2)");
			}
			String firstName = first.toString();
			if(firstName.compareTo("/")==0) {
				// divide
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a/b;
			}
			if(firstName.compareTo("*")==0) {
				// multiply
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a*b;
			}
			if(firstName.compareTo("+")==0) {
				// add
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a+b;
			}
			if(firstName.compareTo("-")==0) {
				// subtract
				Object o2 = (Object)scriptIter.next();
				Object o3 = (Object)scriptIter.next();
				float a = (float)resolveValue(o2);
				float b = (float)resolveValue(o3);
				return a-b;
			}
			if(firstName.compareTo("randomFrom:to:")==0) {
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
			if(firstName.compareTo("readVariable")==0) {
				String varName = (String)scriptIter.next();
				
				Iterator<ScratchVariable> svi = scratchVariables.iterator();
				while(svi.hasNext()) {
					ScratchVariable sv = svi.next();
					if(sv.name.compareTo(varName)==0) {
						return sv.value;
					}
				}
			}
			
			return resolveValue(first);
		}

		throw new Exception("Parse error (3)");
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
