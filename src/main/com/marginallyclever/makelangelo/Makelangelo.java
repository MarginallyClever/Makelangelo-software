package com.marginallyclever.makelangelo;
/**
 * @(#)Makelangelo.java drawbot application with GUI
 * 
 * The Makelangelo app is a tool for programming CNC robots, typically plotters.  It converts lines (made of segments made of points)
 * into instructions in GCODE format, as described in https://github.com/MarginallyClever/Makelangelo-firmware/wiki/gcode-description.
 * 
 * In order to do this the app also provides convenient methods to load vectors (like DXF or SVG), create vectors (TurtleGenerators), or 
 * interpret bitmaps (like BMP,JPEG,PNG,GIF,TGA) into vectors (ImageConverters).
 * 
 * The app must also know some details about the machine, the surface onto which drawings will be made, and the drawing tool making
 * the mark on the paper.  This knowledge helps the app to create better gcode.  
 * 
 * @author Dan Royer (dan@marginallyclever.com)
 * @version 1.00 2012/2/28
 */

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;
import com.jogamp.opengl.GL2;
import com.marginallyclever.core.Clipper2D;
import com.marginallyclever.core.CommandLineOptions;
import com.marginallyclever.core.Point2D;
import com.marginallyclever.core.TransformedImage;
import com.marginallyclever.core.Translator;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.log.LogPanel;
import com.marginallyclever.core.node.Node;
import com.marginallyclever.core.node.NodeConnector;
import com.marginallyclever.core.node.NodeDialog;
import com.marginallyclever.core.turtle.DefaultTurtleRenderer;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.core.turtle.TurtleMove;
import com.marginallyclever.core.turtle.TurtleRenderer;
import com.marginallyclever.makelangelo.nodeConnector.NodeConnectorTransformedImage;
import com.marginallyclever.makelangelo.nodeConnector.NodeConnectorTurtle;
import com.marginallyclever.makelangelo.nodes.ImageConverter;
import com.marginallyclever.makelangelo.nodes.LoadFile;
import com.marginallyclever.makelangelo.nodes.SaveFile;
import com.marginallyclever.makelangelo.nodes.TurtleGenerator;
import com.marginallyclever.makelangelo.nodes.fractals.Generator_SierpinskiTriangle;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.pen.EditPenGUI;
import com.marginallyclever.makelangelo.pen.Pen;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.preferences.MakelangeloAppPreferences;
import com.marginallyclever.makelangelo.preferences.MetricsPreferences;
import com.marginallyclever.makelangelo.preview.Camera;
import com.marginallyclever.makelangelo.preview.OpenGLPanel;
import com.marginallyclever.makelangelo.preview.RendersInOpenGL;
import com.marginallyclever.util.PreferencesHelper;
import com.marginallyclever.util.PropertiesFileHelper;

/**
 * Main entry point into the Makelangelo application.
 * @author Dan Royer
 * @since 0.0.1
 */
public final class Makelangelo extends TransferHandler implements RendersInOpenGL {
	static final long serialVersionUID = 1L;

	/**
	 * Defined in src/resources/makelangelo.properties and uses Maven's resource filtering to update the VERSION based upon VERSION
	 * defined in POM.xml. In this way we only define the VERSION once and prevent violating DRY.
	 */
	public String VERSION;
	
	// only used on first run.
	private final static int DEFAULT_WINDOW_WIDTH = 1200;
	private final static int DEFAULT_WINDOW_HEIGHT = 1020;

	private MakelangeloAppPreferences appPreferences;
	private AllPlotters allPlotters;
	private Plotter activePlotter;

	// what drawing tool is loaded.
	private Pen myPen = new Pen();
	
	// what surface is under the tool.
	
	private Paper myPaper = new Paper();
	// The collection of lines to draw.
	private ArrayList<Turtle> myTurtles;
	
	private Camera camera;
	private RobotController robotController;

	
	protected String lastFileIn = "";
	protected FileFilter lastFilterIn = null;
	
	protected String lastFileOut = "";
	protected FileFilter lastFilterOut = null;
	
	// GUI elements
	private JFrame mainFrame = null;
	
	// only allow one log frame
	private JFrame logFrame = null;
	private LogPanel logPanel = null;
	
	// the menu along the top of the app
	private JMenuBar menuBar;
	
	// OpenGL window
	private OpenGLPanel previewPanel;
	
	private PiCaptureAction piCameraCaptureAction;
	
	private JMenu robotsMenu;
	JRadioButtonMenuItem [] favoriteRobots = new JRadioButtonMenuItem[10];
	
	private boolean showPenUp=false;
	
	
	public static void main(String[] argv) throws Exception {
		Log.start();
		CommandLineOptions.setFromMain(argv);
		Makelangelo makelangeloProgram = new Makelangelo();
		
		if(GraphicsEnvironment.isHeadless()) {
			// TODO a text-only interface?
		} else {
			// Schedule a job for the event-dispatching thread:
			// creating and showing this application's GUI.
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					makelangeloProgram.runHeadFirst();
				}
			});
		}
	}

	public Makelangelo() {
		super();

		myTurtles = new ArrayList<Turtle>();
		// by default start with one turtle.
		myTurtles.add(new Turtle());
		
		Translator.start();
		
		logPanel = new LogPanel();

		Log.message("Locale="+Locale.getDefault().toString());
		Log.message("Headless="+(GraphicsEnvironment.isHeadless()?"Y":"N"));
		
		Log.message("Starting preferences...");
		//Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
		VERSION = PropertiesFileHelper.getMakelangeloVersionPropertyValue();
		appPreferences = new MakelangeloAppPreferences(this);

		Log.message("Starting robot...");
		allPlotters = new AllPlotters();
		if(allPlotters.length()>0) {
			setActivePlotter(allPlotters.get(0));
		}
		
		//testGeneratorsAndConverters();
		
		Log.message("Starting camera...");
		camera = new Camera();
	}
	
	public void runHeadFirst() {
		try {
			piCameraCaptureAction = new PiCaptureAction(this, Translator.get("Makelangelo.capturePhoto"));	
		} catch (FailedToRunRaspistillException e) {
			Log.message("Raspistill unavailable.");
		}

		createAppWindow();
		
		checkSharingPermission();

		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
		if (preferences.getBoolean("Check for updates", false)) {
			ActionCheckForUpdate a = new ActionCheckForUpdate(mainFrame,VERSION);
			a.checkForUpdate(true);
		}
	}

	/**
	 * Check if we need to ask about sharing
	 */
	protected void checkSharingPermission() {
		Log.message("Checking sharing permissions...");
		
		final String SHARING_CHECK_STRING = "Last version sharing checked";
		
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.METRICS);
		String v = preferences.get(SHARING_CHECK_STRING,"0");
		int comparison = VERSION.compareTo(v);
		if(comparison!=0) {
			preferences.put(SHARING_CHECK_STRING,VERSION);
			int dialogResult = JOptionPane.showConfirmDialog(mainFrame, Translator.get("collectAnonymousMetricsOnUpdate"),"Sharing Is Caring",JOptionPane.YES_NO_OPTION);
			MetricsPreferences.setAllowedToShare(dialogResult == JOptionPane.YES_OPTION);
		}
	}

	/**
	 * If the menu bar exists, empty it. If it doesn't exist, create it.
	 */
	public void createMenuBar() {
		Log.message("Create menu bar");

		menuBar = new JMenuBar();
		
		addMenuFile();
		addMenuPaper();
		addMenuPen();
		addMenuGenerate();
		addMenuPiCapture();
		addMenuConvert();
		addMenuEdit();
		addMenuRobots();
		addMenuView();
		addMenuHelp();
		
		Log.message("  finish...");
		menuBar.updateUI();
		mainFrame.setJMenuBar(menuBar);
	}
	
	private void addMenuFile() {
		Log.message("  file...");
		JMenu menu = new JMenu(Translator.get("MenuMakelangelo"));
		menuBar.add(menu);

		JMenuItem buttonNew = new JMenuItem(Translator.get("Makelangelo.action.new"));
		buttonNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// confirm here, just to be safe.
				int result = JOptionPane.showConfirmDialog(
						mainFrame, 
						Translator.get("AreYouSure"),
						Translator.get("AreYouSure"),
						JOptionPane.YES_NO_OPTION); 
				if(result==JOptionPane.YES_OPTION) {
					newFile();
				}
			}
		});
		menu.add(buttonNew);
		
		JMenuItem buttonSave = new JMenuItem(Translator.get("Makelangelo.action.save"));
		buttonSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveFile();	
			}
		});
		menu.add(buttonSave);

		menu.addSeparator();
		
		JMenuItem buttonAdjustPreferences = new JMenuItem(Translator.get("MenuPreferences"));
		buttonAdjustPreferences.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				appPreferences.run(getMainFrame());
			}
		});
		menu.add(buttonAdjustPreferences);

		menu.addSeparator();

		JMenuItem buttonExit = new JMenuItem(Translator.get("MenuQuit"));
		buttonExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onClose();
			}
		});
		menu.add(buttonExit);
	}
	
	private void addMenuPaper() {
		Log.message("  paper...");
		JMenu menu = new JMenu(Translator.get("Makelangelo.editPaper"));

		JMenuItem buttonEditPaper = new JMenuItem(Translator.get("Makelangelo.editPaper"));
		menu.add(buttonEditPaper);
		buttonEditPaper.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EditPaperGUI panel = new EditPaperGUI(myPaper);
				JPanel interior = panel.getInteriorPanel();
				
				int result = JOptionPane.showConfirmDialog(
						mainFrame, 
						interior,
						Translator.get("Makelangelo.editPaper"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE);
				if(result==JOptionPane.OK_OPTION) {
					panel.save();
				}
			}
		});
		menu.add(buttonEditPaper);
		
		menuBar.add(menu);
	}
	
	private void addMenuPen() {
		Log.message("  pen...");
		JMenu menu = new JMenu(Translator.get("Makelangelo.editPen"));

		JMenuItem buttonEditPaper = new JMenuItem(Translator.get("Makelangelo.editPen"));
		menu.add(buttonEditPaper);
		buttonEditPaper.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EditPenGUI panel = new EditPenGUI(myPen);
				JPanel interior = panel.getInteriorPanel();
				
				int result = JOptionPane.showConfirmDialog(
						mainFrame, 
						interior,
						Translator.get("Makelangelo.editPen"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE);
				if(result==JOptionPane.OK_OPTION) {
					panel.save();
				}
			}
		});
		menu.add(buttonEditPaper);
		
		menuBar.add(menu);
	}
	
	private void addMenuGenerate() {
		Log.message("  generate...");
		JMenu menu = new JMenu(Translator.get("Makelangelo.menuGenerate"));
		menuBar.add(menu);
		
		ServiceLoader<TurtleGenerator> service = ServiceLoader.load(TurtleGenerator.class);
		for( TurtleGenerator node : service ) {
			JMenuItem item = new JMenuItem(node.getName());
			menu.add(item);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					menuBar.setEnabled(false);

					node.setWidth(myPaper.getWidth());
					node.setHeight(myPaper.getHeight());
					
					// Display the panel
					NodeDialog dialog = new NodeDialog(getMainFrame(),node);
			        dialog.setLocation(getMainFrame().getLocation());
					dialog.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for(NodeConnector<?> nc : node.outputs ) {
								if(nc instanceof NodeConnectorTurtle) {
									System.out.println("Node output "+nc.getClass().getSimpleName());
									myTurtles.add(((NodeConnectorTurtle)nc).getValue());
								}
							}
							
							if(myTurtles.size()>0) {
								if(robotController!=null) {
									robotController.setTurtles(myTurtles);
								}
							} else {
								System.out.println("No turtles found!");
							}
						}
					});
					dialog.run();
					// @see makelangeloApp.openFile();
					
					menuBar.setEnabled(true);
				}
			});
		}
	}

	private void addMenuConvert() {
		Log.message("  convert...");
		JMenu menu = new JMenu(Translator.get("Makelangelo.menuConvert"));
		menuBar.add(menu);
		
		ServiceLoader<ImageConverter> service = ServiceLoader.load(ImageConverter.class);
		for( ImageConverter node : service ) {
			JMenuItem item = new JMenuItem(node.getName());
			menu.add(item);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					menuBar.setEnabled(false);
					
					// add the converter to the pool
					// display the panel
					NodeDialog dialog = new NodeDialog(getMainFrame(),node);
			        dialog.setLocation(getMainFrame().getLocation());
					dialog.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for(NodeConnector<?> nc : node.outputs ) {
								if(nc instanceof NodeConnectorTurtle) {
									System.out.println("Node output "+nc.getClass().getSimpleName());
									myTurtles.add(((NodeConnectorTurtle)nc).getValue());
								}
							}
							
							if(myTurtles.size()>0) {
								if(robotController!=null) {
									robotController.setTurtles(myTurtles);
								}
							} else {
								System.out.println("No turtles found!");
							}
						}
					});
					dialog.run();
					
					menuBar.setEnabled(true);
				}
			});
		}
	}
	
	private void addMenuPiCapture() {
		if (piCameraCaptureAction != null) {
			JMenu item = new JMenu(piCameraCaptureAction);
            menuBar.add(item);
        }
	}
	
	private void addMenuEdit() {
		Log.message("  edit...");
		JMenu menu = new JMenu(Translator.get("Makelangelo.menuEdit"));
		menuBar.add(menu);
		
		JMenuItem buttonOptimize = new JMenuItem(Translator.get("Makelangelo.action.optimize"));
		buttonOptimize.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			optimizeTurtles();
		}
		});
		menu.add(buttonOptimize);
		
		JMenuItem buttonSimplify = new JMenuItem(Translator.get("Makelangelo.action.simplify"));
		buttonSimplify.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			simplifyTurtles();
		}
		});
		menu.add(buttonSimplify);

		JMenuItem buttonRotate90 = new JMenuItem(Translator.get("Makelangelo.action.rotate90"));
		buttonRotate90.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK));
		buttonRotate90.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rotateTurtles(90);
			}
		});
		menu.add(buttonRotate90);

		JMenuItem buttonRotate90cw = new JMenuItem(Translator.get("Makelangelo.action.rotate90cw"));
		buttonRotate90cw.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK));
		buttonRotate90cw.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rotateTurtles(-90);
			}
		});
		menu.add(buttonRotate90cw);

		
		JMenuItem buttonFlipV = new JMenuItem(Translator.get("Makelangelo.action.flipVertical"));
		buttonFlipV.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				flipTurtlesVertically();
			}
		});
		menu.add(buttonFlipV);
		
		JMenuItem buttonFlipH = new JMenuItem(Translator.get("Makelangelo.action.flipHorizontal"));
		buttonFlipH.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				flipTurtlesHorizontally();
			}
		});
		menu.add(buttonFlipH);

		JMenuItem buttonScaleToHeight = new JMenuItem(Translator.get("Makelangelo.action.scaleToHeight"));
		buttonScaleToHeight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scaleToFillHeight();
			}
		});
		menu.add(buttonScaleToHeight);
		
		JMenuItem buttonScaleToWidth = new JMenuItem(Translator.get("Makelangelo.action.scaleToWidth"));
		buttonScaleToWidth.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scaleToFillWidth();
			}
		});
		menu.add(buttonScaleToWidth);
		
		JMenuItem buttonCenter = new JMenuItem(Translator.get("Makelangelo.action.center"));
		buttonCenter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				centerToPaper();
			}
		});
		menu.add(buttonCenter);
		
		JMenuItem buttonCrop = new JMenuItem(Translator.get("Makelangelo.action.crop"));
		buttonCrop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cropTurtles();
			}
		});
		menu.add(buttonCrop);
	}
	
	private void addMenuRobots() {
		Log.message("  robot...");
		robotsMenu = new JMenu(Translator.get("Makelangelo.menuRobot"));
		refreshRobotsList();
		menuBar.add(robotsMenu);
	}
	
	private void addMenuView() {
		Log.message("  view...");
		JMenu menu = new JMenu(Translator.get("Makelangelo.menuView"));
		menuBar.add(menu);
		
		JMenuItem buttonShowUp = new JMenuItem(Translator.get("Makelangelo.viewPenUp"));
		buttonShowUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK));
		buttonShowUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showPenUp=!showPenUp;
			};
		});
		menu.add(buttonShowUp);
		
		JMenuItem buttonZoomOut = new JMenuItem(Translator.get("Makelangelo.ZoomOut"));
		buttonZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
		buttonZoomOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				camera.zoomOut();
			};
		});
		menu.add(buttonZoomOut);

		JMenuItem buttonZoomIn = new JMenuItem(Translator.get("Makelangelo.ZoomIn"));
		buttonZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK));
		buttonZoomIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				camera.zoomIn();
			};
		});
		menu.add(buttonZoomIn);
		
		JMenuItem buttonZoomToFit = new JMenuItem(Translator.get("Makelangelo.ZoomFit"));
		buttonZoomToFit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK));
		buttonZoomToFit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( activePlotter != null ) {
					camera.zoomToFit(
						activePlotter.getWidth()/2,
						activePlotter.getHeight()/2);
				} else if( myPaper != null ) {
					camera.zoomToFit(
						myPaper.getWidth()/2,
						myPaper.getHeight()/2);
				}
			};
		});
		menu.add(buttonZoomToFit);
		
		JMenuItem buttonViewLog = new JMenuItem(Translator.get("Makelangelo.ShowLog"));
		buttonViewLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(logFrame == null) {
					logFrame = new JFrame(Translator.get("Log"));
					logFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					logFrame.setPreferredSize(new Dimension(600,400));
					logFrame.add(logPanel);
					logFrame.pack();
					logFrame.addWindowListener(new WindowListener() {
						@Override
						public void windowOpened(WindowEvent e) {}
						@Override
						public void windowIconified(WindowEvent e) {}
						@Override
						public void windowDeiconified(WindowEvent e) {}
						@Override
						public void windowDeactivated(WindowEvent e) {}
						@Override
						public void windowClosing(WindowEvent e) {}
						@Override
						public void windowClosed(WindowEvent e) {
							logFrame=null;
						}
						@Override
						public void windowActivated(WindowEvent e) {}
					});
				}
				logFrame.setVisible(true);
			}
		});
		menu.add(buttonViewLog);
	}
	
	private void addMenuHelp() {
		Log.message("  help...");
		JMenu menu = new JMenu(Translator.get("Makelangelo.menuHelp"));
		menuBar.add(menu);

		menu.add(new JMenuItem(new ActionOpenForum()));
		menu.add(new JMenuItem(new ActionCheckForUpdate(mainFrame,VERSION)));
		menu.add(new JMenuItem(new ActionAbout(mainFrame,VERSION)));
	}

	// list 10 most recent robot profiles, select first.
	private void refreshRobotsList() {
		robotsMenu.removeAll();
		int count = allPlotters.length();
		if(count>0) {
			ButtonGroup group = new ButtonGroup();
			int i;
			int limit = (int)Math.min(count, favoriteRobots.length);
			for(i=0;i<limit;++i) {
				Plotter p = allPlotters.get(i);
				String name = p.getNickname();
				favoriteRobots[i] = new JRadioButtonMenuItem(name);
				group.add(favoriteRobots[i]);
				robotsMenu.add(favoriteRobots[i]);
				favoriteRobots[i].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setActivePlotter(p);
					}
				});
			}
			robotsMenu.add(new JSeparator());
		}

		JMenuItem buttonManage = new JMenuItem(Translator.get("Makelangelo.manageMachines"));
		robotsMenu.add(buttonManage);
		buttonManage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AllPlottersGUI mm = new AllPlottersGUI(allPlotters); 
				mm.run(mainFrame);
				refreshRobotsList();
			}
		});
		robotsMenu.add(buttonManage);
		
		JMenuItem buttonDrive = new JMenuItem(Translator.get("Makelangelo.runRobot"));
		robotsMenu.add(buttonDrive);
		buttonDrive.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DrivePlotterGUI mm = new DrivePlotterGUI(activePlotter); 
				mm.run(mainFrame);
				refreshRobotsList();
			}
		});
	}
	
	protected void setActivePlotter(Plotter p) {
		if(activePlotter == p) return;
		
		// if we were drawing a robot, forget about it.
		if(previewPanel!=null) {
			if(activePlotter!=null) {
				previewPanel.removeListener(activePlotter);
			}
		}

		logPanel.setRobot(null);
		robotController = null;
		
		// uncheck the favorites list
		int limit = (int)Math.min(allPlotters.length(), favoriteRobots.length);
		for(int i=0;i<limit;++i) {
			if(favoriteRobots[i]!=null) {
				favoriteRobots[i].setSelected(false);
			}
		}
		
		// set the new robot
		activePlotter = p;

		// PropertyChangeEvent here?
		if(activePlotter!=null) {
			System.out.println("Changing active plotter to "+p.getNickname());
			robotController = new RobotController(activePlotter);
			
			// create a robot and listen to it for important news
			logPanel.setRobot(activePlotter);

			String activeNodeName = activePlotter.getNodeName();
			//System.out.println("activeNodeName="+activeNodeName);
			for(int i=0;i<limit;++i) {
				Plotter p2 = allPlotters.get(i);
				//System.out.println("p2.getNodeName "+i+"="+p2.getNodeName());
				if(favoriteRobots[i]!=null) {
					favoriteRobots[i].setSelected(activeNodeName.contentEquals(p2.getNodeName()));
				}
			}
		}

		// remember to draw the new robot
		if(previewPanel!=null) {
			if(activePlotter !=null) {
				previewPanel.addListener(activePlotter);
			}
			
			// new plotter?  recenter and rezoom camera.
			if(activePlotter!=null) {
				camera.zoomToFit(
						activePlotter.getWidth()/2,
						activePlotter.getHeight()/2);
				
			}
		}
	}

	/**
	 *  For thread safety this method should be invoked from the event-dispatching thread.
	 */
	public void createAppWindow() {
		Log.message("Creating GUI...");

		mainFrame = new JFrame(Translator.get("TitlePrefix")+" "+this.VERSION);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
		});
		
		// overall look and feel
		
		//JFrame.setDefaultLookAndFeelDecorated(true);  // ugly!

        try {
        	// weird but less ugly.
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

		createMenuBar();

		Log.message("create PreviewPanel...");
		previewPanel = new OpenGLPanel();
		previewPanel.setCamera(camera);
		previewPanel.addListener(myPaper);
		previewPanel.addListener(this);

		setActivePlotter(allPlotters.get(0));

		mainFrame.setContentPane(previewPanel);
		
		Log.message("  adding drag & drop support...");
		mainFrame.setTransferHandler(this);

		adjustWindowSize();
		
		mainFrame.setVisible(true);
	}


	private void adjustWindowSize() {
		Log.message("adjust window size...");

		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);

		int width = preferences.getInt("Default window width", DEFAULT_WINDOW_WIDTH);
		int height = preferences.getInt("Default window height", DEFAULT_WINDOW_HEIGHT);

		// Get default screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		// Set window size
		if (width > screenSize.width || height > screenSize.height) {
			width = screenSize.width;
			height = screenSize.height;

			preferences.putInt("Default window width", width);
			preferences.putInt("Default window height", height);
		}

		mainFrame.setSize(width, height);

		// by default center the window. Later use preferences.
		int defaultLocationX = (screenSize.width - width) / 2;
		int defaultLocationY = (screenSize.height - height) / 2;
		mainFrame.setLocation(defaultLocationX, defaultLocationY);
		// int locationX = prefs.getInt("Default window location x",
		// defaultLocationX);
		// int locationY = prefs.getInt("Default window location y",
		// defaultLocationY);
		// mainFrame.setLocation(locationX,locationY);
	}

	private void onClose() {
		int result = JOptionPane.showConfirmDialog(mainFrame, Translator.get("ConfirmQuitQuestion"),
				Translator.get("ConfirmQuitTitle"), JOptionPane.YES_NO_OPTION);

		if (result == JOptionPane.YES_OPTION) {
			previewPanel.removeListener(activePlotter);
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			saveWindowRealEstate();

			// Log.end() should be the very last call.  mainFrame.dispose() kills the thread, so this is as close as I can get.
			Log.end();

			// Run this on another thread than the AWT event queue to make sure the 
			// call to Animator.stop() completes before exiting
			new Thread(new Runnable() {
				public void run() {
					// stop the animator
					previewPanel.stop();
					// throw out the window, which is set to EXIT_ON_CLOSE, which kills the original thread.
					mainFrame.dispose();
				}
			}).start();
		}
	}

	/**
	 * save window position and size
	 */
	private void saveWindowRealEstate() {
		Dimension size = this.mainFrame.getSize();
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);

		preferences.putInt("Default window width", size.width);
		preferences.putInt("Default window height", size.height);

		Point location = this.mainFrame.getLocation();
		preferences.putInt("Default window location x", location.x);
		preferences.putInt("Default window location y", location.y);
	}

	// transfer handler
	@Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        // we only import FileList
        if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            Log.message("Does not support files of type(s): "+info.getDataFlavors());
            return false;
        }
        return true;
    }

	// transfer handler
	@Override
    public boolean importData(TransferHandler.TransferSupport info) {
    	// only accept drops
        if (!info.isDrop()) return false;
        
        // recommended to explicitly call canImport from importData (see java documentation)
        if(!canImport(info)) return false;
        
        // Get the fileList that is being dropped.
        List<?> data = null;
        try {
        	data = (List<?>)info.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
        } 
        catch (Exception e) {
        	Log.error("Failed to import from drag and drop.");
        	return false;
        }
        if(data==null) return false;
        // accept only one file at a time.
        if(data.size()!=1) return false;
        
        String filename = ((File)data.get(0)).getAbsolutePath();

        return openFileOnDemand(filename);
    }

	/**
	 * User has asked that a file be opened.
	 * find the LoadAndSaveFileType plugin that can do it, and then try.  
	 * The loader might spawn a new thread and return before the load is actually finished.
	 * @param filename the file to be opened.
	 * @return true if file was loaded successfully.  false if it failed.
	 */
	public boolean openFileOnDemand(String filename) {
		Log.message(Translator.get("OpeningFile",filename));

		ServiceLoader<LoadFile> imageLoaders = ServiceLoader.load(LoadFile.class);
		for( LoadFile loader : imageLoaders ) {
			// Can you load this file?
			if(!loader.canLoad(filename)) continue;

			try (final InputStream fileInputStream = new FileInputStream(filename)) {
				boolean success=loader.load(fileInputStream);
				
				if( loader instanceof TurtleGenerator ) {
					TurtleGenerator g = (TurtleGenerator)loader;

					for(NodeConnector<?> nc : g.outputs ) {
						if(nc instanceof NodeConnectorTurtle) {
							System.out.println("Node output "+nc.getClass().getSimpleName());
							myTurtles.add(((NodeConnectorTurtle)nc).getValue());
						}
					}
				}
				
				return success;
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		JOptionPane.showMessageDialog(mainFrame, Translator.get("UnknownFileType"));
		return false;
	}

	private boolean isMatchingFileFilter(FileNameExtensionFilter a,FileNameExtensionFilter b) {
		if(!a.getDescription().equals(b.getDescription())) return false;
		String [] aa = a.getExtensions();
		String [] bb = b.getExtensions();
		if(aa.length!=bb.length) return false;
		for(int i=0;i<aa.length;++i) {
			if(!aa[i].equals(bb[i])) return false;
		}
		return true;
	}
	
	private void saveFile() {
		// list all the known file types that I can save.
		File lastDir = (lastFileOut==null?null : new File(lastFileOut));
		JFileChooser fc = new JFileChooser(lastDir);
		
		ServiceLoader<SaveFile> imageSavers = ServiceLoader.load(SaveFile.class);
		for( SaveFile saver : imageSavers ) {
			FileFilter filter = saver.getFileNameFilter();
			fc.addChoosableFileFilter(filter);
		}
		
		// do not allow wild card (*.*) file extensions
		fc.setAcceptAllFileFilterUsed(false);
		// remember the last path & filter used.
		if(lastFilterOut!=null) fc.setFileFilter(lastFilterOut);
		
		// run the dialog
		if (fc.showSaveDialog(getMainFrame()) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();
			FileNameExtensionFilter selectedFilter = (FileNameExtensionFilter)fc.getFileFilter();
			
			// figure out which of the savers was requested.
			for( SaveFile saver : imageSavers ) {
				FileNameExtensionFilter filter = (FileNameExtensionFilter)saver.getFileNameFilter();
				//if(!filter.accept(new File(selectedFile))) {
				if( !isMatchingFileFilter(selectedFilter,filter) ) {
					continue;
				}
				
				// make sure a valid extension is added to the file.
				String selectedFileLC = selectedFile.toLowerCase();
				String[] exts = ((FileNameExtensionFilter)filter).getExtensions();
				boolean foundExtension=false;
				for(String ext : exts) {
					if (selectedFileLC.endsWith('.'+ext.toLowerCase())) {
						foundExtension=true;
						break;
					}
				}
				if(!foundExtension) {
					selectedFile+='.'+exts[0];
				}

				// try to save now.
				boolean success = false;
				try (final OutputStream fileOutputStream = new FileOutputStream(selectedFile)) {
					success=saver.save(fileOutputStream,myTurtles,activePlotter);
				} catch(IOException e) {
					JOptionPane.showMessageDialog(getMainFrame(), "Save failed: "+e.getMessage());
				}
				if(success==true) {
					lastFileOut = selectedFile;
					lastFilterOut = selectedFilter;
					break;
				}					
			}
			// No file filter was found.  Wait, what?!
		}
	}
	
	@SuppressWarnings("unused")
	private void testGeneratorsAndConverters() {
		TransformedImage owl = TransformedImage.loadImage(".\\src\\test\\resources\\owl.jpg");
		owl.rotateAbsolute(-25);
		owl.setScale(0.5, 0.5);

		//Node c = new Generator_Border();
		//Node c = new Generator_Dragon();
		//Node c = new Generator_FibonacciSpiral();
		//Node c = new Generator_FillPage();
		//Node c = new Generator_GosperCurve();
		//Node c = new Generator_GraphPaper();
		//Node c = new Generator_HilbertCurve();
		//Node c = new Generator_KochCurve();
		//Node c = new Generator_Lissajous();
		//Node c = new Generator_LSystemTree();
		//Node c = new Generator_Maze();
		//Node c = new Generator_Package();
		//Node c = new Generator_Polyeder();
		Node c = new Generator_SierpinskiTriangle();
		//Node c = new Generator_Spirograph();
		//Generator_Text c = new Generator_Text(); c.setMessage("Hello, World!");
		
		//ImageConverter c = new Converter_Boxes();
		//ImageConverter c = new Converter_CMYK();
		//ImageConverter c = new Converter_Crosshatch();
		//ImageConverter c = new Converter_Moire();
		//ImageConverter c = new Converter_Multipass();
		//ImageConverter c = new Converter_Pulse();
		//ImageConverter c = new Converter_RandomLines();
		//ImageConverter c = new Converter_Sandy();
		//ImageConverter c = new Converter_Spiral_CMYK();
		//ImageConverter c = new Converter_Spiral();
		//ImageConverter c = new Converter_SpiralPulse();
		
		System.out.println("Node name "+c.getName());
		
		for(NodeConnector<?> nc : c.inputs ) {
			if(nc instanceof NodeConnectorTransformedImage) {
				System.out.println("Node input "+nc.getClass().getSimpleName());
				((NodeConnectorTransformedImage)nc).setValue(owl);
			};
		}
		
		for(int i=0;i<100;++i) {
			c.iterate();
			if(!c.getKeepIterating()) break;
		}
		
		for(NodeConnector<?> nc : c.outputs ) {
			if(nc instanceof NodeConnectorTurtle) {
				System.out.println("Node output "+nc.getClass().getSimpleName());
				myTurtles.add(((NodeConnectorTurtle)nc).getValue());
			}
		}
		
		robotController.setTurtles(myTurtles);
		
		if(myTurtles.size()>0) {
			System.out.println("No turtles found!");
		}
	}
	
	// DO NOT add confirm here, it's too late at this point.
	private void newFile() {
		myTurtles.clear();
		robotController.setTurtles(myTurtles);
	}

	private void rotateTurtles(double degrees) {
		for( Turtle t : myTurtles ) {
			t.rotate(degrees);
		}
	}
	
	private void flipTurtlesVertically() {
		for( Turtle t : myTurtles ) {
			t.scale(1, -1);
		}
	}

	private void flipTurtlesHorizontally() {
		for( Turtle t : myTurtles ) {
			t.scale(-1, 1);
		}
	}
	
	private void scaleToFillHeight() {
		Point2D top = new Point2D();
		Point2D bottom = new Point2D();
		Turtle.getBounds(myTurtles, top, bottom);
		double th=top.y-bottom.y;
		double ph=myPaper.getHeight();
		double n = ph/th;
		System.out.println("scale="+n);
		for( Turtle t : myTurtles ) {
			t.scale(n,n);
		}
	}
	
	private void scaleToFillWidth() {
		Point2D top = new Point2D();
		Point2D bottom = new Point2D();
		Turtle.getBounds(myTurtles, top, bottom);
		double tw=top.x-bottom.x;
		double pw=myPaper.getWidth();
		double n = pw/tw;
		System.out.println("scale="+n);
		for( Turtle t : myTurtles ) {
			t.scale(n,n);
		}
	}
	
	private void centerToPaper() {
		Point2D top = new Point2D();
		Point2D bottom = new Point2D();
		Turtle.getBounds(myTurtles, top, bottom);
		
		double tw=(top.x+bottom.x)/2.0;
		double th=(top.y+bottom.y)/2.0;
		
		for( Turtle t : myTurtles ) {
			t.translate(-tw,-th);
		}
	}
	
	// shorten the pen up travels.
	private void optimizeTurtles() {
		TurtleOptimizer opt = new TurtleOptimizer();
		
		for( Turtle t : myTurtles ) {
			opt.optimizeOneTurtle(t);
		}
	}
	
	/**
	 * Reduce the total number of commands without altering the output.
	 */
	private void simplifyTurtles() {
		TurtleOptimizer opt = new TurtleOptimizer();
		
		for( Turtle t : myTurtles ) {
			opt.removeSequentialPenUpMoves(t);
		}
		
		for( Turtle t : myTurtles ) {
			opt.removeSequentialLinearPenDownMoves(t);
		}
	}
	
	/**
	 * crop a set of {@link Turtle} to the page edges.
	 */
	private void cropTurtles() {
		double yTop    = myPaper.getTop();
		double yBottom = myPaper.getBottom();
		double xLeft   = myPaper.getLeft();
		double xRight  = myPaper.getRight();
		
		for( Turtle t : myTurtles ) {
			cropOneTurtle(t,xRight,yTop,xLeft,yBottom);
		}
	}
	
	/**
	 * Crop one {@link Turtle}.
	 * @param turtle
	 * @param tl top right corner
	 * @param bl bottom left corner
	 */
	private void cropOneTurtle(Turtle turtle,double xRight,double yTop,double xLeft,double yBottom) {	
		Point2D tr = new Point2D(xRight,yTop);
		Point2D bl = new Point2D(xLeft,yBottom);
		
		ArrayList<TurtleMove> toKeep = new ArrayList<TurtleMove>();
		
		Point2D p0 = new Point2D();
		Point2D p1 = new Point2D();
		
		int len = turtle.history.size();

		TurtleMove prev = turtle.history.get(0);
		for(int i=0;i<len-1;++i) {
			TurtleMove next = turtle.history.get(i+1);
			p0.set(prev.x,prev.y);
			p1.set(next.x,next.y);

			boolean prevIn = (Clipper2D.outCodes(p0, xLeft, xRight, yTop, yBottom)==0);
			if(Clipper2D.clipLineToRectangle(p0, p1, tr, bl)) {
				if(prevIn) {
					toKeep.add(new TurtleMove(p0.x,p0.y,prev.isUp));
				} else {
					toKeep.add(new TurtleMove(p0.x,p0.y,true));
				}
			} // else entire line clipped
			prev = next;
		}
		// last point
		if(len>1) {
			prev = turtle.history.get(len-2);
			TurtleMove next = turtle.history.get(len-1);
			p0.set(prev.x,prev.y);
			p1.set(next.x,next.y);
			boolean nextIn = (Clipper2D.outCodes(p1, xLeft, xRight, yTop, yBottom)==0);
			if(Clipper2D.clipLineToRectangle(p0, p1, tr, bl)) {
				if(nextIn) {
					toKeep.add(new TurtleMove(p1.x,p1.y,next.isUp));
				} else {
					toKeep.add(new TurtleMove(p1.x,p1.y,true));
				}
			} // else entire line clipped
		}
		
		turtle.history.clear();
		turtle.history.addAll(toKeep);
		toKeep.clear();
	}

	@Override
	public void render(GL2 gl2) {
		float size = (float)(myPen.getDiameter() * 200.0 / camera.getZoom());
		
		for( Turtle t : myTurtles ) {
			TurtleRenderer tr = new DefaultTurtleRenderer(gl2,showPenUp);
			tr.setPenDownColor(t.getColor());
			gl2.glLineWidth(size);
			t.render(tr);
		}
	}

	public Paper getPaper() {
		return myPaper;
	}
	
	public JFrame getMainFrame() {
		return mainFrame;
	}
}

/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Makelangelo. If not, see <http://www.gnu.org/licenses/>.
 */
