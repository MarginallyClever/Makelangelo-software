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

import java.awt.BorderLayout;
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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
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
import javax.vecmath.Vector3d;

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
					//myPaper.saveConfig();
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
					//myPen.saveConfig();
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
							myTurtles.clear();

							for(NodeConnector<?> nc : node.outputs ) {
								System.out.println("Node output "+nc.getClass().getSimpleName());
								if(nc instanceof NodeConnectorTurtle) {
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
							myTurtles.clear();
							
							for(NodeConnector<?> nc : node.outputs ) {
								System.out.println("Node output "+nc.getClass().getSimpleName());
								if(nc instanceof NodeConnectorTurtle) {
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
		JMenu menu = new JMenu(Translator.get("Makelangelo.menuRobot"));
		refreshRobotsList(menu);
		menuBar.add(menu);
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
				if( robotController != null ) {
					robotController.setShowPenUp(!robotController.getShowPenUp());
				}
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
	private void refreshRobotsList(JMenu menu) {
		menu.removeAll();
		int count = allPlotters.length();
		if(count>0) {
			ButtonGroup group = new ButtonGroup();
			JRadioButtonMenuItem [] favorites = new JRadioButtonMenuItem[10];
			int i;
			int limit = (int)Math.min(count, favorites.length);
			for(i=0;i<limit;++i) {
				Plotter p = allPlotters.get(i);
				String name = p.getNickname();
				favorites[i] = new JRadioButtonMenuItem(name);
				group.add(favorites[i]);
				menu.add(favorites[i]);
				favorites[i].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setActivePlotter(p);
					}
				});
				favorites[i].setSelected(p.getUID()==activePlotter.getUID());
			}
			menu.add(new JSeparator());
		}

		JMenuItem buttonManage = new JMenuItem(Translator.get("Makelangelo.manageMachines"));
		menu.add(buttonManage);
		final JMenu menu2 = menu;
		buttonManage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AllPlottersGUI mm = new AllPlottersGUI(allPlotters); 
				mm.run(mainFrame);
				refreshRobotsList(menu2);
			}
		});
		menu.add(buttonManage);
		/*
		JMenuItem buttonRobotSettings = new JMenuItem(Translator.get("Makelangelo.robotSettings"));
		menu.add(buttonRobotSettings);
		buttonRobotSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( robotController != null ) {
					menuBar.setEnabled(false);
					DialogMachineSettings m = new DialogMachineSettings(mainFrame,Translator.get("Makelangelo.robotSettings"));
					m.run(robotController);
					menuBar.setEnabled(true);
				}
			}
		});*/
		
		JMenuItem buttonDrive = new JMenuItem(Translator.get("Makelangelo.runRobot"));
		menu.add(buttonDrive);
		buttonDrive.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				drivePlotter();
			}
		});
	}
	
	protected void setActivePlotter(Plotter p) {
		if(previewPanel!=null) {
			if(activePlotter!=null) {
				previewPanel.removeListener(activePlotter);
			}
		}

		logPanel.setRobot(null);
		robotController = null;
		
		if(activePlotter != p) {
			activePlotter = p;
			// PropertyChangeEvent here?
			if(activePlotter!=null) {
				System.out.println("Changing active plotter to "+p.getNickname());
				robotController = new RobotController(activePlotter);
				
				// create a robot and listen to it for important news
				logPanel.setRobot(robotController);
			}
		}
		
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
		setActivePlotter(activePlotter);

		mainFrame.setContentPane(previewPanel);
		
		Log.message("  adding drag & drop support...");
		mainFrame.setTransferHandler(this);

		adjustWindowSize();
		
		mainFrame.setVisible(true);
	}

	private void drivePlotter() {
		menuBar.setEnabled(false);

		PanelDrivePlotter myRobotPanel = new PanelDrivePlotter(getMainFrame(),activePlotter);
		
		JDialog dialog = new JDialog(getMainFrame(),Translator.get("Makelangelo.menuRobot"), true);
        dialog.setLocation(getMainFrame().getLocation());
		dialog.setContentPane(myRobotPanel);
		dialog.pack();
		dialog.setVisible(true);
		
		menuBar.setEnabled(true);
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

	public JFrame getMainFrame() {
		return mainFrame;
	}

	@Deprecated
	public RobotController getRobot() {
		return robotController;
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
				return success;
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		Log.error(Translator.get("UnknownFileType"));
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
					success=saver.save(fileOutputStream,myTurtles,robotController);
				} catch(IOException e) {
					JOptionPane.showMessageDialog(getMainFrame(), "Save failed: "+e.getMessage());
					//e.printStackTrace();
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
			System.out.println("Node input "+nc.getClass().getSimpleName());
			if(nc instanceof NodeConnectorTransformedImage) {
				((NodeConnectorTransformedImage)nc).setValue(owl);
			};
		}
		
		for(int i=0;i<100;++i) {
			c.iterate();
			if(!c.getKeepIterating()) break;
		}
		
		myTurtles.clear();
		
		for(NodeConnector<?> nc : c.outputs ) {
			System.out.println("Node output "+nc.getClass().getSimpleName());
			if(nc instanceof NodeConnectorTurtle) {
				myTurtles.add(((NodeConnectorTurtle)nc).getValue());
			}
		}
		
		if(myTurtles.size()>0) {
			robotController.setTurtles(myTurtles);
		} else {
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
		for( Turtle t : myTurtles ) {
			optimizeOneTurtle(t);
		}
	}

	private class Polyline extends LinkedList<Integer> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	}

	private class Segment2D {
		public int a,b;
		
		public Segment2D(int aa,int bb) {
			a=aa;
			b=bb;
		}
	}
	private ArrayList<Point2D> points = new ArrayList<Point2D>();
	private ArrayList<Segment2D> segments = new ArrayList<Segment2D>();
	private ArrayList<Polyline> polyLines = new ArrayList<Polyline>();

	private int optimizeAddPointToPool(double x,double y,final double EPSILON) {
		int i=0;
		for( Point2D p1 : points ) {
			if(Math.abs(x-p1.x)<EPSILON && Math.abs(y-p1.y)<EPSILON) {
				// no
				return i;
			}
			++i;
		}
		// yes
		points.add(new Point2D(x,y));
		return i; // which is the same as points.size()-1;
	}
	
	private void reportTurtleTravel(Turtle turtle) {
		double travel=0;
		TurtleMove prev=null;
		for( TurtleMove mo : turtle.history ) {
			if(mo.isUp && prev!=null ) {
				double dx=prev.x-mo.x;
				double dy=prev.y-mo.y;
				travel += Math.sqrt(dx*dx+dy*dy);
			}
			prev=mo;
		}
		System.out.println("Travel (mm)="+travel);
	}

	private void optimizeOneTurtle(Turtle turtle) {
		reportTurtleTravel(turtle);
		
		points.clear();
		segments.clear();
		polyLines.clear();
		
		// build a list of unique points (further than EPSILON apart)
		// and line segments (two points connected by a line)
		final double EPSILON = 0.01;
		
		TurtleMove prev = null;
		int drawMoves=0;
		int travelMoves=0;

		for( TurtleMove m : turtle.history ) {
			// is this point unique in the pool?
			if( !m.isUp ) {
				drawMoves++;
				if(prev!=null) {
					int a = optimizeAddPointToPool(prev.x,prev.y,EPSILON);
					int b = optimizeAddPointToPool(m.x,m.y,EPSILON);
					if(a!=b) {
						// we keep only segments that are longer than EPSILON.
						segments.add(new Segment2D(a,b));
					}
				}
			} else {
				travelMoves++;
			}
			
			prev = m;
		}
		System.out.println("history = "+turtle.history.size());
		System.out.println("drawMoves = "+drawMoves);
		System.out.println("travelMoves = "+travelMoves);
		System.out.println("points = "+points.size());
		System.out.println("segments = "+segments.size());
		assert(segments.size()<=drawMoves);
		
		// greedy tours to build sequence of lines with no pen up or down.
		while( segments.size()>0 ) {
			Polyline line = new Polyline();
			polyLines.add(line);
			
			Segment2D s0 = segments.remove(0);
			int head = s0.a;
			int tail = s0.b;
			line.add(head);
			line.add(tail);

			// if a segment meets the head or tail of this snake, take it from the segment pool and add grow the snake. 
			ArrayList<Segment2D> segmentsToKeep = new ArrayList<Segment2D>();
			for( Segment2D s : segments ) {
					 if(s.a==head) {	line.addFirst(s.b);		head=s.b;	}
				else if(s.b==head) {	line.addFirst(s.a);		head=s.a;	}
				else if(s.a==tail) {	line.addLast(s.b);		tail=s.b;	}
				else if(s.b==tail) {	line.addLast(s.a);		tail=s.a;	}
				else segmentsToKeep.add(s);
			}
			segments = segmentsToKeep;
			//System.out.println("line size="+line.size());
		}
		System.out.println("polylines = "+polyLines.size());
		assert(polyLines.size()<=segments.size());
		assert(polyLines.size()<=travelMoves);

		// find the bounds of the points
		Point2D top = new Point2D(-Double.MAX_VALUE,-Double.MAX_VALUE);
		Point2D bottom = new Point2D(Double.MAX_VALUE,Double.MAX_VALUE);
		for( Point2D p : points ) {
			top.x = Math.max(p.x, top.x);
			top.y = Math.max(p.y, top.y);
			bottom.x = Math.min(p.x, bottom.x);
			bottom.y = Math.min(p.y, bottom.y);
		}
		top.x+=0.001;
		top.y+=0.001;
		double w = top.x-bottom.x;
		double h = top.y-bottom.y;

		// we have a box from top to bottom.  
		// let's make a grid bucketsPerSide*bucketsPerSide large.
		int numEnds = polyLines.size()*2;
		int bucketsPerSide = (int)Math.ceil(Math.sqrt(numEnds/2));
		if(bucketsPerSide<1) bucketsPerSide=1;
		// allocate buckets
		Polyline[] buckets = new Polyline[bucketsPerSide*bucketsPerSide];
		for( int b=0;b<buckets.length;b++) {
			buckets[b] = new Polyline();
		}
		
		// put the head and tail of each polyline into their buckets.
		for( Polyline line : polyLines ) {
			for( int index : new int[] { line.peekFirst(), line.peekLast() } ) {
				Point2D p = points.get(index);
				int ix = (int)(bucketsPerSide * (p.x-bottom.x) / w);
				int iy = (int)(bucketsPerSide * (p.y-bottom.y) / h);
				buckets[iy*bucketsPerSide+ix].add(index);
			}
		}

		{/*
			// some debug info
			int i=0;
			System.out.println("buckets=[");
			for(int y=0;y<bucketsPerSide;++y) {
				for(int x=0;x<bucketsPerSide;++x) {
					System.out.print(buckets[i].size()+"\t");
					i++;
				}
				System.out.println();
			}
			System.out.println("]");
		//*/
		}
		
		// sort the polylines by nearest neighbor into newOrder
		ArrayList<Polyline> newOrder = new ArrayList<Polyline>();
		ArrayList<Polyline> foundLines = new ArrayList<Polyline>(); 
		Polyline foundIndexes = new Polyline();
		Point2D lastPoint=null;
		int ix,iy;
		int bx=0;
		int by=0;
		
		while(polyLines.size()>0) {
			int radius=0;
			while(foundIndexes.size()==0) {
				if(radius==0) {
					foundIndexes.addAll(buckets[by*bucketsPerSide+bx]);
				} else {
					//System.out.println("radius="+radius);
					for(iy=by-radius;iy<=by+radius;++iy) {
						if(iy<0 || iy >= bucketsPerSide) continue;
						ix = bx-radius;  if(ix>=0            ) foundIndexes.addAll(buckets[iy*bucketsPerSide+ix]);
						ix = bx+radius;  if(ix<bucketsPerSide) foundIndexes.addAll(buckets[iy*bucketsPerSide+ix]);
					}
					for(ix=bx-radius;ix<=bx+radius;++ix) {
						if(ix<0 || ix >= bucketsPerSide) continue;
						iy = by-radius;  if(iy>=0            ) foundIndexes.addAll(buckets[iy*bucketsPerSide+ix]);
						iy = by+radius;  if(iy<bucketsPerSide) foundIndexes.addAll(buckets[iy*bucketsPerSide+ix]);
					}
				}
				radius++;
			}
			
			// find best line.
			Polyline bestLine;
			{
				//System.out.println("found "+foundIndexes.size()+" candidate point(s).");
				// we found at least one index, maybe more, and we don't know which bucket the index(es) came from.
				// figure out to which polyLine they belong.
				for( Polyline line : polyLines ) {
					int first=line.peekFirst();
					int last =line.peekLast();
					if(foundIndexes.contains(first) || foundIndexes.contains(last)) {
						// make sure found lines are unique.
						if(!foundLines.contains(line)) {
							foundLines.add(line);
						}
					}
				}
				//System.out.println("found "+foundLines.size()+" unique polyLine(s).");
				
				// we know which lines were found.
				// we know they are pretty close.
				// We prefer polylines that start and end in the same cell.
				//   That means we prefer closed polyloops first.
				// We prefer polylines with a head close to their tail
				// sort based on this preference.
				final Point2D testLastPoint = lastPoint;
				foundLines.sort(new Comparator<Polyline>() {
					@Override
					public int compare(Polyline o1, Polyline o2) {
						int a = o1.peekFirst();
						int b = o1.peekLast();
						int c=o2.peekFirst();
						int d=o2.peekLast();
						double d1=0;
						double d2=0;
						/*
						if(a==b) d1=0;  // closed
						else {
							Point2D h1=points.get(a);
							Point2D t1=points.get(b);
							double dx=h1.x-t1.x;
							double dy=h1.y-t1.y;
							d1 = dx*dx+dy*dy;
						}
						
						if(c==d) d2=0;  // closed
						else {
							Point2D h2=points.get(c);
							Point2D t2=points.get(d);					
							double dx=h2.x-t2.x;
							double dy=h2.y-t2.y;
							d2 = dx*dx+dy*dy;
						}*/
						
						if(d1==d2 && testLastPoint!=null) {
							Point2D h1=points.get(a);
							Point2D t1=points.get(b);
							Point2D h2=points.get(c);
							Point2D t2=points.get(d);					
							d1 = Math.min(testLastPoint.distance(h1), testLastPoint.distance(t1));
							d1 = Math.min(testLastPoint.distance(h2), testLastPoint.distance(t2));
						}
						
						return (int)((d2-d1)*1000);
					}
				});
				// the first line is the best line in the list
				bestLine = foundLines.get(0);
				foundLines.clear();
				
				// set bx/by 
				int first = bestLine.peekLast();
				int last = bestLine.peekLast();
				lastPoint = points.get(foundIndexes.contains(first) ? last : first);
				bx = (int)(bucketsPerSide * (lastPoint.x-bottom.x) / w);
				by = (int)(bucketsPerSide * (lastPoint.y-bottom.y) / h);

				foundIndexes.clear();
			}

			//System.out.println("cleanup...");
			polyLines.remove(bestLine);
			newOrder.add(bestLine);
			Integer bh = bestLine.peekFirst();
			Integer bt = bestLine.peekLast();
			for( Polyline b : buckets ) {
				b.remove(bh);
				b.remove(bt);
			}
			
			if((polyLines.size()%1000)==0) {
				//System.out.println(polyLines.size());
			}
		}
		
		// rebuild the new, more efficient turtle path
		System.out.println("Rebuilding...");
		ArrayList<TurtleMove> newHistory = new ArrayList<TurtleMove>();
		for( Polyline line : newOrder ) {
			boolean first=true;
			for( Integer index : line ) {
				Point2D p = points.get(index);
				newHistory.add(new TurtleMove(p.x,p.y,first));
				first=false;
			}
		}
		turtle.history = newHistory;

		reportTurtleTravel(turtle);
	}
	
	/**
	 * Reduce the total number of commands without altering the output.
	 */
	private void simplifyTurtles() {
		for( Turtle t : myTurtles ) {
			removeSequentialPenUpMoves(t);
		}
		
		for( Turtle t : myTurtles ) {
			removeSequentialLinearPenDownMoves(t);
		}
	}
	
	/**
	 * Any time there are two pen up moves in a row then the first is not needed.
	 * @param turtle to be simplified.
	 */
	private void removeSequentialPenUpMoves(Turtle turtle) {
		ArrayList<TurtleMove> toKeep = new ArrayList<TurtleMove>();
		
		int len = turtle.history.size();
		
		TurtleMove a=turtle.history.get(0);
		TurtleMove b=null;
		for(int i=1;i<len;++i) {
			b = turtle.history.get(i);
			// if abc are up then b is redundant.
			if(a.isUp && b.isUp) {
				// do nothing. lose a.
			} else {
				// a not redudant, keep it.
				toKeep.add(a);
			}
			a = b;
		}
		if(b!=null) {
			toKeep.add(b);
		}

		int len2 = toKeep.size();
		System.out.println("history start="+len+", end="+len2+", saved="+(len-len2));
		turtle.history.clear();
		turtle.history.addAll(toKeep);
	}
	
	/**
	 * Any time there are two pen down moves in ab and bc where abc is a straight line?  Lose b.
	 * @param turtle to be simplified.
	 */
	private void removeSequentialLinearPenDownMoves(Turtle turtle) {
		ArrayList<TurtleMove> toKeep = new ArrayList<TurtleMove>();
		
		int len = turtle.history.size();

		Vector3d v0 = new Vector3d();
		Vector3d v1 = new Vector3d();

		TurtleMove a;
		TurtleMove b;
		TurtleMove c=null;
		toKeep.add(turtle.history.get(0));
		for(int i=1;i<len-1;++i) {
			a = turtle.history.get(i-1);
			b = turtle.history.get(i);
			c = turtle.history.get(i+1);
			// if abc are up then b is redundant.
			if(!b.isUp && !c.isUp) {
				// are ABC in a straight line?
				v0.x = b.x-a.x;
				v0.y = b.y-a.y;
				v0.normalize();
				v1.x = c.x-b.x;
				v1.y = c.y-b.y;
				v1.normalize();
				// 1 degree = cos(PI/180) = 0.99984769515
				if(v1.dot(v0)>0.99984769515) {
					// Less than 1 degree.  Lose b.
				} else {
					// 1 degree or more.  b not redudant.  Keep it.
					toKeep.add(b);
				}
			} else {
				// b not redudant, keep it.
				toKeep.add(b);
			}
		}
		if(c!=null) {
			toKeep.add(c);
		}

		int len2 = toKeep.size();
		System.out.println("history start="+len+", end="+len2+", saved="+(len-len2));
		turtle.history.clear();
		turtle.history.addAll(toKeep);
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
		boolean showPenUp = false;
		
		if( robotController != null ) {
			showPenUp = robotController.getShowPenUp();
		}
		
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
