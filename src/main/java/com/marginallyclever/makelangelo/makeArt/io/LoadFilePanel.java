package com.marginallyclever.makelangelo.makeArt.io;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.TransformedImage;
import com.marginallyclever.makelangelo.makeArt.io.image.ConvertImagePanel;
import com.marginallyclever.makelangelo.makeArt.io.vector.TurtleFactory;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.util.ArrayList;

public class LoadFilePanel extends JPanel implements PreviewListener {
	private static final Logger logger = LoggerFactory.getLogger(LoadFilePanel.class);
	
	private static final long serialVersionUID = 1L;
	private Paper myPaper;

	private JFileChooser fc = new JFileChooser();
	private JButton bChoose = new JButton(Translator.get("Open"));
	private JLabel filename = new JLabel();

	private ConvertImagePanel myConvertImage;
	private PreviewListener mySubPreviewListener;
	private JPanel mySubPanel = new JPanel();

	private String previousFile="";

	public LoadFilePanel(Paper paper,String filename) {
		super();
		myPaper = paper;
		setLayout(new BorderLayout());
		add(getFileSelectionPanel(filename),BorderLayout.NORTH);
		add(mySubPanel,BorderLayout.CENTER);
	}
	
	private JPanel getFileSelectionPanel(String previousFile) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(bChoose,BorderLayout.WEST);
		panel.add(filename,BorderLayout.CENTER);
		
		filename.setText(previousFile);
		
		bChoose.addActionListener((e)-> chooseFile());
		
		setupFilefilters();
		
		return panel;
	}
	
	public void chooseFile() {
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                        mySubPanel.removeAll();
                    
			String selectedFile = fc.getSelectedFile().getAbsolutePath();
			logger.debug("File selected by user: {}", selectedFile);
			filename.setText(selectedFile);
			
			load(selectedFile);
		}
	}
	
	public void load(String filename) {
		try {
			if(ConvertImagePanel.isFilenameForAnImage(filename)) {
				TransformedImage image = new TransformedImage( ImageIO.read(new FileInputStream(filename)) );

				myConvertImage = new ConvertImagePanel(myPaper,image);
				myConvertImage.setBorder(BorderFactory.createTitledBorder(ConvertImagePanel.class.getSimpleName()));
				myConvertImage.addActionListener(this::notifyListeners);
				
				mySubPanel.add(myConvertImage);
				mySubPreviewListener = myConvertImage;
			} else {
				Turtle t = TurtleFactory.load(filename);
				notifyListeners(new ActionEvent(t,0,"turtle"));
			}
			previousFile = filename;
		} catch(Exception e) {
			logger.error("Failed to load {}", filename, e);
			JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), Translator.get("Error"), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void setupFilefilters() {
		// add vector formats
		for( FileNameExtensionFilter ff : TurtleFactory.getLoadExtensions() ) {
			fc.addChoosableFileFilter(ff);
		}
		
		// add image formats
		FileNameExtensionFilter images = new FileNameExtensionFilter(Translator.get("FileTypeImage"),ConvertImagePanel.IMAGE_FILE_EXTENSIONS);
		fc.addChoosableFileFilter(images);
		
		// no wild card filter, please.
		fc.setAcceptAllFileFilterUsed(false);
	}

	@Override
	public void render(GL2 gl2) {
		if(mySubPreviewListener!=null) mySubPreviewListener.render(gl2);
	}

	// OBSERVER PATTERN

	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
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

	public String getLastFileIn() {
		return previousFile;
	}

	// TEST
	
	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		
		JFrame frame = new JFrame(LoadFilePanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new LoadFilePanel(new Paper(),""));
		frame.pack();
		frame.setVisible(true);
	}
}
