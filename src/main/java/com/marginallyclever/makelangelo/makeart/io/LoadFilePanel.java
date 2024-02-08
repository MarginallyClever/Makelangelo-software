package com.marginallyclever.makelangelo.makeart.io;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imageconverter.SelectImageConverterPanel;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;

public class LoadFilePanel extends JPanel implements PreviewListener {
	private static final Logger logger = LoggerFactory.getLogger(LoadFilePanel.class);
	public static final String COMMAND_TURTLE = "turtle";
	private final Paper myPaper;
	private final JButton bChoose = new JButton(Translator.get("Open"));
	private final OpenFileChooser openFileChooser = new OpenFileChooser(this);
	private final JLabel selectedFilename = new JLabel();
	private SelectImageConverterPanel myConvertImage;
	private PreviewListener mySubPreviewListener;
	private JDialog parent;

	public LoadFilePanel(Paper paper,String filename) {
		super(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		myPaper = paper;
		add(getFileSelectionPanel(filename),BorderLayout.NORTH);
		selectedFilename.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));

		openFileChooser.setOpenListener(this::onNewFilenameChosen);
	}
	
	private JPanel getFileSelectionPanel(String previousFile) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(bChoose,BorderLayout.WEST);
		panel.add(selectedFilename,BorderLayout.CENTER);
		
		selectedFilename.setText(previousFile);
		
		bChoose.addActionListener((e)-> openFileChooser.chooseFile());
		
		return panel;
	}

	private void stopExistingImageConverter() {
		if(myConvertImage!=null) {
			myConvertImage.loadingFinished();
			remove(myConvertImage);
		}
	}

	public boolean onNewFilenameChosen(String filename) {
		stopExistingImageConverter();
		selectedFilename.setText(filename);

		try {
			if (SelectImageConverterPanel.isFilenameForAnImage(filename)) {
				TransformedImage image = new TransformedImage( ImageIO.read(new FileInputStream(filename)) );
				myConvertImage = new SelectImageConverterPanel(myPaper, image);
				myConvertImage.addActionListener(this::notifyListeners);

				add(myConvertImage,BorderLayout.CENTER);

				myConvertImage.run();
				mySubPreviewListener = myConvertImage;
				return true;
			} else {
				Turtle t = TurtleFactory.load(filename);
				notifyListeners(new ActionEvent(t,0, COMMAND_TURTLE));
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

	private final EventListenerList listeners = new EventListenerList();

	public void addActionListener(ActionListener a) {
		listeners.add(ActionListener.class,a);
	}
	
	public void removeActionListener(ActionListener a) {
		listeners.remove(ActionListener.class,a);
	}
	
	private void notifyListeners(ActionEvent e) {
		for( ActionListener a : listeners.getListeners(ActionListener.class) ) {
			a.actionPerformed(e);
		}
	}
}
