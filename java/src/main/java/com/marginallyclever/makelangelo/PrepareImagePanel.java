package com.marginallyclever.makelangelo;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.kabeja.dxf.Bounds;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFSpline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.DXFSplineConverter;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

import com.marginallyclever.drawingtools.DrawingTool;
import com.marginallyclever.filters.Filter;
import com.marginallyclever.filters.Filter_GeneratorBoxes;
import com.marginallyclever.filters.Filter_GeneratorColorBoxes;
import com.marginallyclever.filters.Filter_GeneratorColorFloodFill;
import com.marginallyclever.filters.Filter_GeneratorCrosshatch;
import com.marginallyclever.filters.Filter_GeneratorHilbertCurve;
import com.marginallyclever.filters.Filter_GeneratorPulse;
import com.marginallyclever.filters.Filter_GeneratorScanline;
import com.marginallyclever.filters.Filter_GeneratorSpiral;
import com.marginallyclever.filters.Filter_GeneratorVoronoiStippling;
import com.marginallyclever.filters.Filter_GeneratorYourMessageHere;
import com.marginallyclever.filters.Filter_GeneratorZigZag;


/**
 * Controls related to converting an image to gcode
 * @author danroyer
 * @since 7.1.4
 */
public class PrepareImagePanel
extends JPanel
implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4703402918904039337L;
	
	protected MultilingualSupport translator;
	protected MachineConfiguration machineConfiguration;
	protected MainGUI gui;
	
	protected String lastFileIn="";
	protected String lastFileOut="";
	
	private String[] machineConfigurations;
	private JComboBox<String> machineChoices;
	private JSlider input_paper_margin;
    private JButton buttonOpenFile, buttonHilbertCurve, buttonText2GCODE, buttonSaveFile;
	
	private Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MAKELANGELO_ROOT);
	
	// Image processing
	// TODO use a ServiceLoader for plugins
	private List<Filter> image_converters;
	
	// TODO use a ServiceLoader instead?
	protected void loadImageConverters() {
		image_converters = new ArrayList<Filter>();
		image_converters.add(new Filter_GeneratorZigZag(gui,machineConfiguration,translator));
		image_converters.add(new Filter_GeneratorSpiral(gui,machineConfiguration,translator));
		image_converters.add(new Filter_GeneratorCrosshatch(gui,machineConfiguration,translator));
		image_converters.add(new Filter_GeneratorScanline(gui,machineConfiguration,translator));
		image_converters.add(new Filter_GeneratorPulse(gui,machineConfiguration,translator));
		image_converters.add(new Filter_GeneratorBoxes(gui,machineConfiguration,translator));
		image_converters.add(new Filter_GeneratorColorBoxes(gui, machineConfiguration, translator));
		image_converters.add(new Filter_GeneratorColorFloodFill(gui, machineConfiguration, translator));  // not ready for public consumption
		image_converters.add(new Filter_GeneratorVoronoiStippling(gui, machineConfiguration, translator));
	}

    /**
     *
     * @return
     */
    private String[] getAnyMachineConfigurations() {
        String[] machineNames = machineConfiguration.getKnownMachineNames();
        if(machineNames.length == 1 && machineNames[0] == null) {
            machineNames = machineConfiguration.getAvailableConfigurations();
        }
        return machineNames;
    }
	

	public void createPanel(MainGUI _gui,MultilingualSupport _translator,MachineConfiguration _machineConfiguration) {
		translator=_translator;
		gui=_gui;
		machineConfiguration = _machineConfiguration;
		
		this.setLayout(new GridLayout(0,1));
		this.setPreferredSize(new Dimension(150,100));
		
		machineConfigurations = getAnyMachineConfigurations();
		machineChoices = new JComboBox<>(machineConfigurations);
		machineChoices.setSelectedIndex(machineConfiguration.getCurrentMachineIndex());
		
		input_paper_margin = new JSlider(JSlider.HORIZONTAL, 0, 50, 100-(int)(machineConfiguration.paperMargin*100));
		input_paper_margin.setMajorTickSpacing(10);
		input_paper_margin.setMinorTickSpacing(5);
		input_paper_margin.setPaintTicks(false);
		input_paper_margin.setPaintLabels(true);

		add(new JLabel(translator.get("MachineNumber")));
		add(machineChoices);
		add(new JLabel(translator.get("PaperMargin")));
		add(input_paper_margin);

        // File conversion menu
        buttonOpenFile = new JButton(translator.get("MenuOpenFile"));
        buttonOpenFile.addActionListener(this);
        add(buttonOpenFile);
        
        buttonHilbertCurve = new JButton(translator.get("MenuHilbertCurve"));
        buttonHilbertCurve.addActionListener(this);
        add(buttonHilbertCurve);
        
        buttonText2GCODE = new JButton(translator.get("MenuTextToGCODE"));
        buttonText2GCODE.addActionListener(this);
        add(buttonText2GCODE);

        buttonSaveFile = new JButton(translator.get("MenuSaveGCODEAs"));
        buttonSaveFile.addActionListener(this);
        add(buttonSaveFile);
	}
	
	// The user has done something.  respond to it.
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		final int machine_choiceSelectedIndex = machineChoices.getSelectedIndex();
		long new_uid = Long.parseLong(machineChoices.getItemAt(machine_choiceSelectedIndex));
		machineConfiguration.loadConfig(new_uid);
		machineConfiguration.paperMargin=(100-input_paper_margin.getValue())*0.01;
		
		if( subject == buttonOpenFile ) {
			openFileDialog();
			return;
		}
		if( subject == buttonHilbertCurve ) {
			hilbertCurve();
			return;
		}
		if( subject == buttonText2GCODE ) {
			textToGCODE();
			return;
		}
		
		if( subject == buttonSaveFile ) {
			saveFileDialog();
			return;
		}
	}
	
	void updateButtonAccess(boolean isRunning) {
		if(buttonHilbertCurve!=null) buttonHilbertCurve.setEnabled(!isRunning);
		if(buttonText2GCODE!=null) buttonText2GCODE.setEnabled(!isRunning);
	}

	// creates a file open dialog. If you don't cancel it opens that file.
	public void openFileDialog() {
	    // Note: source for ExampleFileFilter can be found in FileChooserDemo,
	    // under the demo/jfc directory in the Java 2 SDK, Standard Edition.

		String filename = lastFileIn;

		FileFilter filterGCODE = new FileNameExtensionFilter(translator.get("FileTypeGCode"), "ngc");
		FileFilter filterImage = new FileNameExtensionFilter(translator.get("FileTypeImage"), "jpg", "jpeg", "png", "wbmp", "bmp", "gif");
		FileFilter filterDXF   = new FileNameExtensionFilter(translator.get("FileTypeDXF"), "dxf");
		 
		JFileChooser fc = new JFileChooser(new File(filename));
		fc.addChoosableFileFilter(filterImage);
		fc.addChoosableFileFilter(filterDXF);
		fc.addChoosableFileFilter(filterGCODE);
	    if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	String selectedFile=fc.getSelectedFile().getAbsolutePath();

	    	// if machine is not yet calibrated
	    	if(machineConfiguration.isPaperConfigured() == false) {
	    		JOptionPane.showMessageDialog(null,translator.get("SetPaperSize"));
	    		return;
	    	}
	    	openFileOnDemand(selectedFile);
	    }
	}
	
	public void saveFileDialog() {
	    // Note: source for ExampleFileFilter can be found in FileChooserDemo,
	    // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
		String filename = lastFileOut;

		FileFilter filterGCODE = new FileNameExtensionFilter(translator.get("FileTypeGCode"), "ngc");
		
		JFileChooser fc = new JFileChooser(new File(filename));
		fc.addChoosableFileFilter(filterGCODE);
	    if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	String selectedFile=fc.getSelectedFile().getAbsolutePath();

			if(!selectedFile.toLowerCase().endsWith(".ngc")) {
				selectedFile+=".ngc";
			}

	    	try {
	    		gui.gCode.save(selectedFile);
	    	}
		    catch(IOException e) {
		    	gui.log("<span style='color:red'>"+translator.get("Failed")+e.getMessage()+"</span>\n");
		    	return;
		    }
	    }
	}
	
	public boolean isFileGcode(String filename) {
		String ext=filename.substring(filename.lastIndexOf('.'));
    	return (ext.equalsIgnoreCase(".ngc") || ext.equalsIgnoreCase(".gc"));
	}
	
	public boolean isFileDXF(String filename) {
		String ext=filename.substring(filename.lastIndexOf('.'));
    	return (ext.equalsIgnoreCase(".dxf"));
	}
	
	public boolean isFileImage(String filename) {
		String ext=filename.substring(filename.lastIndexOf('.'));
    	return ext.equalsIgnoreCase(".jpg")
    			|| ext.equalsIgnoreCase(".png")
    			|| ext.equalsIgnoreCase(".bmp")
    			|| ext.equalsIgnoreCase(".gif");
	}
	
	// User has asked that a file be opened.
	public void openFileOnDemand(String filename) {
 		gui.log("<font color='green'>" + translator.get("OpeningFile") + filename + "...</font>\n");
 		boolean file_loaded_ok=false;
 		
	   	if(isFileGcode(filename)) {
			file_loaded_ok = loadGCode(filename);
    	} else if(isFileDXF(filename)) {
    		file_loaded_ok = loadDXF(filename);
    	} else if(isFileImage(filename)) {
    		file_loaded_ok = loadImage(filename);
    	} else {
    		gui.log("<font color='red'>"+translator.get("UnknownFileType")+"</font>\n");
    	}

	   	if(file_loaded_ok==true) {
	   		lastFileIn = filename;
	   		gui.updateMenuBar();
	   	}
	   	
	   	gui.statusBar.clear();
	}
	
	
	protected boolean chooseImageConversionOptions(boolean isDXF) {
		final JPanel panel = new JPanel(new GridBagLayout());
		
		final JCheckBox reverse_h = new JCheckBox(translator.get("FlipForGlass"));
		reverse_h.setSelected(machineConfiguration.reverseForGlass);

		String [] filter_names = new String[image_converters.size()];
		Iterator<Filter> fit = image_converters.iterator();
		int i=0;
		while(fit.hasNext()) {
			Filter f = fit.next();
			filter_names[i++] = f.getName();
		}
		
		final JComboBox<String> input_draw_style = new JComboBox<String>(filter_names);
		input_draw_style.setSelectedIndex(getDrawStyle());
		
		GridBagConstraints c = new GridBagConstraints();

		int y=0;
		if(!isDXF) {
			c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=0;  c.gridy=y;  panel.add(new JLabel(translator.get("ConversionStyle")),c);
			c.anchor=GridBagConstraints.WEST;	c.gridwidth=3;	c.gridx=1;	c.gridy=y++;	panel.add(input_draw_style,c);
		}
		c.anchor=GridBagConstraints.WEST;	c.gridwidth=1;  c.gridx=1;  c.gridy=y++;  panel.add(reverse_h,c);
		
	    int result = JOptionPane.showConfirmDialog(null, panel, translator.get("ConversionOptions"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	    if (result == JOptionPane.OK_OPTION) {
			setDrawStyle(input_draw_style.getSelectedIndex());
			machineConfiguration.reverseForGlass=reverse_h.isSelected();
			machineConfiguration.saveConfig();
			
			// Force update of graphics layout.
			gui.updateMachineConfig();

			return true;	    	
	    }
		
		return false;
	}

	/**
	 * Opens a file.  If the file can be opened, get a drawing time estimate, update recent files list, and repaint the preview tab.
	 * @param filename what file to open
	 */
	public boolean loadGCode(String filename) {
		try {
			gui.gCode.load(filename);
		   	gui.log("<font color='green'>" + gui.gCode.estimate_count + translator.get("LineSegments")
					+ "\n" + gui.gCode.estimated_length + translator.get("Centimeters") + "\n"
					+ translator.get("EstimatedTime") + gui.statusBar.formatTime((long) (gui.gCode.estimated_time)) + "s.</font>\n");
	    }
	    catch(IOException e) {
	    	gui.log("<span style='color:red'>"+translator.get("FileNotOpened") + e.getLocalizedMessage()+"</span>\n");
	    	gui.updateMenuBar();
	    	return false;
	    }
	    
		gui.gCode.changed=true;
	    gui.halt();
	    return true;
	}
	
	
	protected boolean loadDXF(String filename) {
		if( chooseImageConversionOptions(true) == false ) return false;

        // where to save temp output file?
		final String destinationFile = gui.getTempDestinationFile();
		final String srcFile = filename;
		
		final ProgressMonitor pm = new ProgressMonitor(null, translator.get("Converting"), "", 0, 100);
		pm.setProgress(0);
		pm.setMillisToPopup(0);
		
		final SwingWorker<Void,Void> s = new SwingWorker<Void,Void>() {
			public boolean ok=false;
			
			@SuppressWarnings("unchecked")
			@Override
			public Void doInBackground() {
				gui.log("<font color='green'>"+translator.get("Converting")+" "+destinationFile+"</font>\n");

				Parser parser = ParserBuilder.createDefaultParser();

				double dxf_x2=0;
				double dxf_y2=0;
				
				try (
				FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
				Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)
				) {
					DrawingTool tool = machineConfiguration.getCurrentTool();
					out.write(machineConfiguration.getConfigLine()+";\n");
					out.write(machineConfiguration.getBobbinLine()+";\n");
					out.write("G00 G90;\n");
					tool.writeChangeTo(out);
					tool.writeOff(out);
					
					parser.parse(srcFile, DXFParser.DEFAULT_ENCODING);
					DXFDocument doc = parser.getDocument();
					Bounds b = doc.getBounds();
					double width = b.getMaximumX() - b.getMinimumX();
					double height = b.getMaximumY() - b.getMinimumY();
					double cx = ( b.getMaximumX() + b.getMinimumX() ) / 2.0f;
					double cy = ( b.getMaximumY() + b.getMinimumY() ) / 2.0f;
					double wh = width>height ?  width:height;
					double sy = machineConfiguration.getPaperHeight()*10.0/wh;
					double sx = machineConfiguration.getPaperWidth()*10.0/wh;
					double scale = (sx<sy? sx:sy );
					sx = scale * (machineConfiguration.reverseForGlass? -1 : 1);
					sx *= machineConfiguration.paperMargin;
					sy *= machineConfiguration.paperMargin;
					
					// count all entities in all layers
					Iterator<DXFLayer> layer_iter = (Iterator<DXFLayer>)doc.getDXFLayerIterator();
					int entity_total=0;
					int entity_count=0;
					while(layer_iter.hasNext()) {
						DXFLayer layer = (DXFLayer)layer_iter.next();
						gui.log("<font color='yellow'>Found layer "+layer.getName()+"</font>\n");
						Iterator<String> entity_iter = (Iterator<String>)layer.getDXFEntityTypeIterator();
						while(entity_iter.hasNext()) {
							String entity_type = (String)entity_iter.next();
							List<DXFEntity> entity_list = (List<DXFEntity>)layer.getDXFEntities(entity_type);
							gui.log("<font color='yellow'>+ Found "+entity_list.size()+" of type "+entity_type+"</font>\n");
							entity_total+=entity_list.size();
						}
					}
					// set the progress meter
					pm.setMinimum(0);
					pm.setMaximum(entity_total);
							
					// convert each entity
					layer_iter = doc.getDXFLayerIterator();
					while(layer_iter.hasNext()) {
						DXFLayer layer = (DXFLayer)layer_iter.next();

						Iterator<String> entity_type_iter = (Iterator<String>)layer.getDXFEntityTypeIterator();
						while(entity_type_iter.hasNext()) {
							String entity_type = (String)entity_type_iter.next();
							List<DXFEntity> entity_list = layer.getDXFEntities(entity_type);
							
							if(entity_type.equals(DXFConstants.ENTITY_TYPE_LINE)) {
								Iterator<DXFEntity> iter = entity_list.iterator();
								while(iter.hasNext()) {
									pm.setProgress(entity_count++);
									DXFLine entity = (DXFLine)iter.next();
									Point start = entity.getStartPoint();
									Point end = entity.getEndPoint();

									double x =(start.getX()-cx)*sx;
									double y =(start.getY()-cy)*sy;
									double x2=(end  .getX()-cx)*sx;
									double y2=(end  .getY()-cy)*sy;
									double dx,dy;
									//*
									// is it worth drawing this line?
									dx = x2-x;
									dy = y2-y;
									if(dx*dx+dy*dy < tool.getDiameter()/2.0) {
										continue;
									}
									//*/
									dx = dxf_x2 - x;
									dy = dxf_y2 - y;

									if(dx*dx+dy*dy > tool.getDiameter()/2.0) {
										if(tool.isDrawOn()) {
											tool.writeOff(out);
										}
										tool.writeMoveTo(out, (float)x,(float)y);
									}
									if(tool.isDrawOff()) {
										tool.writeOn(out);
									}
									tool.writeMoveTo(out, (float)x2,(float)y2);
									dxf_x2=x2;
									dxf_y2=y2;
								}
							} else if(entity_type.equals(DXFConstants.ENTITY_TYPE_SPLINE)) {
								Iterator<DXFEntity> iter = entity_list.iterator();
								while(iter.hasNext()) {
									pm.setProgress(entity_count++);
									DXFSpline entity = (DXFSpline)iter.next();
									entity.setLineWeight(30);
									DXFPolyline polyLine = DXFSplineConverter.toDXFPolyline(entity);
									boolean first=true;
									for(int j=0;j<polyLine.getVertexCount();++j) {
										DXFVertex v = polyLine.getVertex(j);
										double x = (v.getX()-cx)*sx;
										double y = (v.getY()-cy)*sy;
										double dx = dxf_x2 - x;
										double dy = dxf_y2 - y;
										
										if(first==true) {
											first=false;
											if(dx*dx+dy*dy > tool.getDiameter()/2.0) {
												// line does not start at last tool location, lift and move.
												if(tool.isDrawOn()) {
													tool.writeOff(out);
												}
												tool.writeMoveTo(out, (float)x,(float)y);
											}
											// else line starts right here, do nothing.
										} else {
											// not the first point, draw.
											if(tool.isDrawOff()) tool.writeOn(out);
											if(j<polyLine.getVertexCount()-1 && dx*dx+dy*dy<tool.getDiameter()/2.0) continue;  // less than 1mm movement?  Skip it. 
											tool.writeMoveTo(out, (float)x,(float)y);
										}
										dxf_x2=x;
										dxf_y2=y;
									}
								}
							} else if(entity_type.equals(DXFConstants.ENTITY_TYPE_POLYLINE)) {
								Iterator<DXFEntity> iter = entity_list.iterator();
								while(iter.hasNext()) {
									pm.setProgress(entity_count++);
									DXFPolyline entity = (DXFPolyline)iter.next();
									boolean first=true;
									for(int j=0;j<entity.getVertexCount();++j) {
										DXFVertex v = entity.getVertex(j);
										double x = (v.getX()-cx)*sx;
										double y = (v.getY()-cy)*sy;
										double dx = dxf_x2 - x;
										double dy = dxf_y2 - y;
										
										if(first==true) {
											first=false;
											if(dx*dx+dy*dy > tool.getDiameter()/2.0) {
												// line does not start at last tool location, lift and move.
												if(tool.isDrawOn()) {
													tool.writeOff(out);
												}
												tool.writeMoveTo(out, (float)x,(float)y);
											}
											// else line starts right here, do nothing.
										} else {
											// not the first point, draw.
											if(tool.isDrawOff()) tool.writeOn(out);
											if(j<entity.getVertexCount()-1 && dx*dx+dy*dy<tool.getDiameter()/2.0) continue;  // less than 1mm movement?  Skip it. 
											tool.writeMoveTo(out, (float)x,(float)y);
										}
										dxf_x2=x;
										dxf_y2=y;
									}
								}
							}
						}
					}

					// entities finished.  Close up file.
					tool.writeOff(out);
					tool.writeMoveTo(out, 0, 0);
					
					ok=true;
				} catch(IOException | ParseException e) {
					e.printStackTrace();
				}
				
				pm.setProgress(100);
			    return null;
			}
			
			@Override
			public void done() {
				pm.close();
				gui.log("<font color='green'>"+translator.get("Finished")+"</font>\n");
				gui.playConversionFinishedSound();
				if(ok) {
					loadGCode(destinationFile);
				}
				gui.halt();
			}
		};
		
		s.addPropertyChangeListener(new PropertyChangeListener() {
		    // Invoked when task's progress property changes.
		    public void propertyChange(PropertyChangeEvent evt) {
		        if ("progress" == evt.getPropertyName() ) {
		            int progress = (Integer) evt.getNewValue();
		            pm.setProgress(progress);
		            String message = String.format("%d%%\n", progress);
		            pm.setNote(message);
		            if(s.isDone()) {
	                	gui.log("<font color='green'>"+translator.get("Finished")+"</font>\n");
		            } else if (s.isCancelled() || pm.isCanceled()) {
		                if (pm.isCanceled()) {
		                    s.cancel(true);
		                }
	                    gui.log("<font color='green'>"+translator.get("Cancelled")+"</font>\n");
		            }
		        }
		    }
		});
		
		s.execute();
		
		return true;
	}
	
	
	public boolean loadImage(String filename) {
        // where to save temp output file?
		final String sourceFile = filename;
		final String destinationFile = gui.getTempDestinationFile();
		
		loadImageConverters();
		if( chooseImageConversionOptions(false) == false ) return false;

		final ProgressMonitor pm = new ProgressMonitor(null, translator.get("Converting"), "", 0, 100);
		pm.setProgress(0);
		pm.setMillisToPopup(0);
		
		final SwingWorker<Void,Void> s = new SwingWorker<Void,Void>() {
			@Override
			public Void doInBackground() {
				// read in image
				BufferedImage img;
				try {
					gui.log("<font color='green'>"+translator.get("Converting")+" "+destinationFile+"</font>\n");
					// convert with style
					img = ImageIO.read(new File(sourceFile));
					
					int style = getDrawStyle();
					Filter f = image_converters.get(style);
					f.setParent(this);
					f.setProgressMonitor(pm);
					f.setDestinationFile(destinationFile);
					f.convert(img);
					gui.updateMachineConfig();
				}
				catch(IOException e) {
					gui.log("<font color='red'>"+translator.get("Failed")+e.getLocalizedMessage()+"</font>\n");
					gui.updateMenuBar();
				}

				pm.setProgress(100);
			    return null;
			}
			
			@Override
			public void done() {
				pm.close();
				gui.log("<font color='green'>"+translator.get("Finished")+"</font>\n");
				loadGCode(destinationFile);
				gui.playConversionFinishedSound();
			}
		};
		
		s.addPropertyChangeListener(new PropertyChangeListener() {
		    // Invoked when task's progress property changes.
		    public void propertyChange(PropertyChangeEvent evt) {
		        if ("progress" == evt.getPropertyName() ) {
		            int progress = (Integer) evt.getNewValue();
		            pm.setProgress(progress);
		            String message = String.format("%d%%.\n", progress);
		            pm.setNote(message);
		            if(s.isDone()) {
		            	gui.log("<font color='green'>"+translator.get("Finished")+"</font>\n");
		            } else if (s.isCancelled() || pm.isCanceled()) {
		                if (pm.isCanceled()) {
		                    s.cancel(true);
		                }
		                gui.log("<font color='green'>"+translator.get("Cancelled")+"</font>\n");
		            }
		        }
		    }
		});
		
		s.execute();
		
		return true;
	}
	
	private void setDrawStyle(int style) {
		prefs.putInt("Draw Style", style);
	}
	
	private int getDrawStyle() {
		return prefs.getInt("Draw Style", 0);
	}
		
	
	public void hilbertCurve() {
		Filter_GeneratorHilbertCurve msg = new Filter_GeneratorHilbertCurve(gui,machineConfiguration,translator);
		msg.generate( gui.getTempDestinationFile() );

		loadGCode(gui.getTempDestinationFile());
		gui.playConversionFinishedSound();
	}
	
	
	public void textToGCODE() {
		Filter_GeneratorYourMessageHere msg = new Filter_GeneratorYourMessageHere(gui,machineConfiguration,translator);
		msg.generate( gui.getTempDestinationFile());

		loadGCode(gui.getTempDestinationFile());
		gui.playConversionFinishedSound();
	}
}
