package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Save the {@link ProgramInterface} instruction buffer to a gcode file of the user's choosing.
 * Relies on {@link MarlinPlotterInterface} to translate the instructions into gcode.
 * @author Dan Royer
 * @since 7.28.0
 */
public class SaveGCode {
	private static final Logger logger = LoggerFactory.getLogger(SaveGCode.class);
	
	private final JFileChooser fc = new JFileChooser();

	public SaveGCode() {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("GCode", "gcode");
		fc.addChoosableFileFilter(filter);
		// do not allow wild card (*.*) file extensions
		fc.setAcceptAllFileFilterUsed(false);
	}

	public SaveGCode(String lastDir) {
		// remember the last path used, if any
		fc.setCurrentDirectory((lastDir==null?null : new File(lastDir)));
	}

	public void run(Turtle turtle, Plotter robot, JFrame parent) throws Exception {
		fc.setSelectedFile(new File(turtle.getName()));// TODO only get the name to avoid changing the save path if the name start with a path.
		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();
			String fileWithExtension = addExtension(selectedFile,((FileNameExtensionFilter)fc.getFileFilter()).getExtensions());
			logger.debug("File selected by user: {}", fileWithExtension);
			save(fileWithExtension,turtle,robot);
		}
	}

	private String addExtension(String name, String [] extensions) {
		for( String e : extensions ) {
			if(FilenameUtils.getExtension(name).equalsIgnoreCase(e)) return name;
		}
		
		return name + "." + extensions[0];
	}
	
	private void save(String filename, Turtle turtle, Plotter robot) throws Exception {
		logger.debug("saving...");
		
		try (Writer out = new OutputStreamWriter(new FileOutputStream(filename))) {

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
			// TODO as comment engine generator ( "Makelangelo VERSION DETAILDVERSION" )
			/* TODO like Ultimaker cura ??? pre
			;FLAVOR:Marlin
;TIME:8627
;Filament used: 8.33533m
;Layer height: 0.2
;MINX:9.366
;MINY:109.8
;MINZ:0.2
;MAXX:96.724
;MAXY:197.158
;MAXZ:45
;ARCWELDERPROCESSED
; Postprocessed by [ArcWelder](https://github.com/FormerLurker/ArcWelderLib)
; Copyright(C) 2020 - Brad Hochgesang
; resolution=0.05mm
; path_tolerance=5%
; max_radius=1000000.00mm
; default_xyz_precision=3
; default_e_precision=5

;Generated with Cura_SteamEngine 4.10.0

			*/
			out.write(";Generated with  " + Makelangelo.staticgetFullVersionString() + "\n");
			Date date = new Date(System.currentTimeMillis());
			out.write("; " + formatter.format(date) + "\n");
			out.write("; " + turtle.getName() + "\n");
			// TODO as comment env states ( Paper Size , plotter type,  plotter config , ...)
			// TODO MarlinPlotterInterface.getFindHomeString()?
			out.write("; " + robot.toString() + "\n");
			// TODO start gcode ( Like M201 ... M92 ...
			// https://marlinfw.org/docs/gcode/M092.html Set Axis Steps-per-unit
			// https://marlinfw.org/docs/gcode/M117.html Set LCD Message
			// https://marlinfw.org/docs/gcode/M220.html Set Feedrate Percentage
			// ...
			out.write("G28\n");  // go home 

			/* ???
			
;LAYER_COUNT:225
;LAYER:0
M107
;MESH:spheres_v0001_3m5m4_.stl
G0 F3600 X53.011 Y180.863 Z0.2
;TYPE:WALL-INNER
			  
			 */
			boolean isUp = true;

			TurtleMove previousMovement = null;
			for (int i = 0; i < turtle.history.size(); ++i) {
				TurtleMove m = turtle.history.get(i);

				switch (m.type) {
					case TurtleMove.TRAVEL -> {
						if (!isUp) {
							// lift pen up
							out.write(MarlinPlotterInterface.getPenUpString(robot) + "\n");
							isUp = true;
						}
						previousMovement = m;
					}
					case TurtleMove.DRAW_LINE -> {
						if (isUp) {
							// go to m and put pen down
							if (previousMovement == null) previousMovement = m;
							out.write(MarlinPlotterInterface.getTravelToString(robot,previousMovement.x, previousMovement.y) + "\n");
							out.write(MarlinPlotterInterface.getPenDownString(robot) + "\n");
							isUp = false;
						}
						out.write(MarlinPlotterInterface.getDrawToString(robot,m.x, m.y) + "\n");
						previousMovement = m;
					}
					case TurtleMove.TOOL_CHANGE -> {
						// ?? ;TIME_ELAPSED:8627.004434
						// TODO tool change start gcode ( like i whant a bip M300 and/or somthing else like a park position ...)
						out.write(MarlinPlotterInterface.getPenUpString(robot) + "\n");
						out.write(MarlinPlotterInterface.getToolChangeString(m.getColor().toInt()) + "\n");
						// todo tool change end gcode
					}
				}
			}
			if (!isUp) out.write(MarlinPlotterInterface.getPenUpString(robot) + "\n");
		}
		//TODO endgcode

		/* TODO like Ultimaker Cura post
		;End of Gcode
;SETTING_3 {"global_quality": "[general]\\nversion = 4\\nname = Draft #2 Voxel e
;SETTING_3 leph t01 #2\\ndefinition = vertex_k8400\\n\\n[metadata]\\ntype = qual
;SETTING_3 ity_changes\\nquality_type = draft\\nsetting_version = 17\\n\\n[value
;SETTING_3 s]\\nacceleration_enabled = False\\nadhesion_type = none\\narcwelder_
;SETTING_3 enable = True\\nlayer_height_0 = 0.2\\nspeed_slowdown_layers = 1\\nsu
;SETTING_3 pport_enable = False\\nsupport_structure = tree\\n\\n", "extruder_qua
;SETTING_3 lity": ["[general]\\nversion = 4\\nname = Draft #2 Voxel eleph t01 #2
;SETTING_3 \\ndefinition = vertex_k8400\\n\\n[metadata]\\ntype = quality_changes
;SETTING_3 \\nquality_type = draft\\nintent_category = default\\nposition = 0\\n
;SETTING_3 setting_version = 17\\n\\n[values]\\nbrim_line_count = 12\\ncool_min_
;SETTING_3 layer_time = 2\\ncool_min_speed = 40\\nfill_outline_gaps = True\\nfil
;SETTING_3 ter_out_tiny_gaps = False\\ninfill_pattern = grid\\ninfill_sparse_den
;SETTING_3 sity = 25\\nlayer_start_x = 200\\nlayer_start_y = 200\\nmaterial_fina
;SETTING_3 l_print_temperature = 185.0\\nmaterial_flow = 94.0\\nmaterial_flow_la
;SETTING_3 yer_0 = 94.0\\nmaterial_initial_print_temperature = 190.0\\nmaterial_
;SETTING_3 print_temperature = 190.0\\nretraction_amount = 2.0\\nretraction_enab
;SETTING_3 le = True\\nskin_material_flow = 94.0\\nspeed_print = 80\\nspeed_z_ho
;SETTING_3 p = 5\\ntravel_avoid_supports = True\\nzig_zaggify_infill = True\\n\\
;SETTING_3 n"]}
		*/
		logger.debug("done.");
	}
}
