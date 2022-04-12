package com.marginallyclever.makelangelo.makeart.io;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.ResizeTurtleToPaperAction;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imageconverter.SelectImageConverterPanel;
import com.marginallyclever.makelangelo.makeart.io.vector.TurtleFactory;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.util.ArrayList;

public class LoadFilePanel extends JPanel implements PreviewListener {
	private static final Logger logger = LoggerFactory.getLogger(LoadFilePanel.class);
	
	private static final long serialVersionUID = 1L;
	private Paper myPaper;

	private JButton bChoose = new JButton(Translator.get("Open"));
	private final JTextArea jtaFilename = new JTextArea();

	private SelectImageConverterPanel myConvertImage;
	private PreviewListener mySubPreviewListener;
	private final JPanel mySubPanel = new JPanel();
	private OpenFileChooser openFileChooser;
	private JDialog parent;

	public LoadFilePanel(Paper paper,String filename) {
		super();
		myPaper = paper;
		setLayout(new BorderLayout());
		add(getFileSelectionPanel(filename),BorderLayout.NORTH);
		add(mySubPanel,BorderLayout.CENTER);

		this.jtaFilename.setEditable(false);
		this.jtaFilename.setLineWrap(true);

		openFileChooser = new OpenFileChooser(this);
		openFileChooser.setOpenListener(this::load);
	}
	
	private JPanel getFileSelectionPanel(String previousFile) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(bChoose,BorderLayout.WEST);
		panel.add(jtaFilename,BorderLayout.CENTER);
		
		jtaFilename.setText(previousFile);
		
		bChoose.addActionListener((e)-> openFileChooser.chooseFile());
		
		return panel;
	}

	public boolean load(String filename) {
		try {
			if (SelectImageConverterPanel.isFilenameForAnImage(filename)) {
				TransformedImage image = new TransformedImage( ImageIO.read(new FileInputStream(filename)) );

				myConvertImage = new SelectImageConverterPanel(myPaper, image);
				// TODO replace this border with something more appropriate
				myConvertImage.setBorder(BorderFactory.createTitledBorder(SelectImageConverterPanel.class.getSimpleName()));
				myConvertImage.addActionListener(this::notifyListeners);
				mySubPanel.removeAll();
				mySubPanel.add(myConvertImage);
				myConvertImage.run();
				mySubPreviewListener = myConvertImage;
				return true;
			} else {
				Turtle t = TurtleFactory.load(filename);
				// by popular demand, resize turtle to fit paper
				ResizeTurtleToPaperAction resize = new ResizeTurtleToPaperAction(myPaper,false,"");
				t = resize.run(t);
				
				notifyListeners(new ActionEvent(t,0,"turtle"));
				if (parent != null) {
					parent.dispatchEvent(new WindowEvent(parent, WindowEvent.WINDOW_CLOSING));
				}
			}
		} catch(Exception e) {
			logger.error("Failed to load {}", filename, e);
			JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), Translator.get("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}

	@Override
	public void render(GL2 gl2) {
		if(mySubPreviewListener!=null) mySubPreviewListener.render(gl2);
	}

	public void setParent(JDialog parent) {
		this.parent = parent;
	}

	public void loadingFinished() {
		logger.debug("loadingFinished()");
		if(myConvertImage!=null) {
			myConvertImage.loadingFinished();
		}
	}

	// OBSERVER PATTERN

	private ArrayList<ActionListener> listeners = new ArrayList<>();

	public void addActionListener(ActionListener a) {
		listeners.add(a);
	}
	
	public void removeActionListener(ActionListener a) {
		listeners.remove(a);
	}
	
	private void notifyListeners(ActionEvent e) {
		for( ActionListener a : listeners ) {
			a.actionPerformed(e);
		}
	}

	// TEST
	
	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		
		JFrame frame = new JFrame(LoadFilePanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new LoadFilePanel(new Paper(),""));
		frame.setPreferredSize(new Dimension(800,600));
		frame.pack();
		frame.setVisible(true);
	}
}
