package com.marginallyclever.makelangelo.makeArt.io.image;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.TransformedImage;
import com.marginallyclever.makelangelo.makeArt.imageConverter.ImageConverter;
import com.marginallyclever.makelangelo.makeArt.imageConverter.ImageConverterFactory;
import com.marginallyclever.makelangelo.makeArt.imageConverter.ImageConverterPanel;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.select.SelectPanelChangeListener;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.SwingWorker.StateValue;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;


public class ConvertImagePanel extends JPanel implements PreviewListener, SelectPanelChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(ConvertImagePanel.class);
	private static final long serialVersionUID = 5574250944369730761L;
	// Set of image file extensions.
	public static final String [] IMAGE_FILE_EXTENSIONS = {"jpg","jpeg","png","wbmp","bmp","gif","qoi"};

	@SuppressWarnings("deprecation")
	private Preferences prefs = PreferencesHelper
			.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
	
	private Paper myPaper;
	private TransformedImage myImage;
	private static JComboBox<String> styleNames;
	private static JComboBox<String> fillNames;
	private JPanel cards = new JPanel(new CardLayout());
	
	private ImageConverterPanel myConverterPanel;
	private ImageConverterThread imageConverterThread; 
	private ArrayList<ImageConverterThread> workerList = new ArrayList<ImageConverterThread>();
	private int workerCount = 0;
	private ProgressMonitor pm;
	
	public ConvertImagePanel(Paper paper,TransformedImage image) {
		super();
		myPaper = paper;
		myImage = image;
		
		cards.setPreferredSize(new Dimension(450,300));
		
		fillNames = getFillSelection();
		styleNames = getStyleSelection();

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		int y = 0;

		y++;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = y;
		c.ipadx=5;
		this.add(new JLabel(Translator.get("ConversionFill")), c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.ipadx=0;
		this.add(fillNames, c);
		c.gridy = y;
		
		y++;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = y;
		c.ipadx=5;
		this.add(new JLabel(Translator.get("ConversionStyle")), c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.ipadx=0;
		this.add(styleNames, c);
	
		y++;
		c.anchor=GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth=4;
		c.gridx=0;
		c.gridy=y;
		c.insets = new Insets(10, 0, 0, 0);
		cards.setPreferredSize(new Dimension(449,325));
		cards.setBorder(BorderFactory.createLoweredBevelBorder());
		this.add(cards,c);

		int first = (styleNames!=null ? styleNames.getSelectedIndex() : 0);
		changeConverter(ImageConverterFactory.list[first]);
	}
	
	private JComboBox<String> getStyleSelection() {
		ArrayList<String> imageConverterNames = new ArrayList<String>();
		for( ImageConverterPanel i : ImageConverterFactory.list ) {
			imageConverterNames.add(i.getConverter().getName());
			cards.add(i,i.getConverter().getName());
		}
		
		JComboBox<String> box = new JComboBox<String>((String[])imageConverterNames.toArray(new String[0]));
		box.addItemListener((e) -> onConverterChanged(e));
		box.setSelectedIndex(getPreferredDrawStyle());

		return box;
	}

	private void onConverterChanged(ItemEvent e) {
		logger.debug("onConverterChanged");
				
	    CardLayout cl = (CardLayout)(cards.getLayout());
	    cl.show(cards, (String)e.getItem());
	    scaleLoader(fillNames.getSelectedIndex());

		int first = (styleNames!=null ? styleNames.getSelectedIndex() : 0);
		setPreferredDrawStyle(first);
		changeConverter(ImageConverterFactory.list[first]);
	}

	private JComboBox<String> getFillSelection() {
		String[] imageFillNames = {
			Translator.get("ConvertImagePaperFill"),
			Translator.get("ConvertImagePaperFit")
		};
		JComboBox<String> box = new JComboBox<String>(imageFillNames);
		
		int p=getPreferredFillStyle();
		if(p>=box.getItemCount()) p=0;
		box.addItemListener((e) ->{
			scaleLoader(box.getSelectedIndex());
			setPreferredFillStyle(box.getSelectedIndex());
		});
		box.setSelectedIndex(p);

		return box;
	}

	private void scaleLoader(int mode) {
		switch(mode) {
			case 0:  scaleToFillPaper();  break;
			case 1:  scaleToFitPaper();  break;
			default: break;
		}
		reconvert();
	}

	private void scaleToFillPaper() {
		double width  = myPaper.getMarginWidth();
		double height = myPaper.getMarginHeight();

		float f;
		if( width > height ) {
			f = (float)( width / (double)myImage.getSourceImage().getWidth() );
		} else {
			f = (float)( height / (double)myImage.getSourceImage().getHeight() );
		}
		myImage.setScale(f,-f);
	}
	
	private void scaleToFitPaper() {
		double width  = myPaper.getMarginWidth();
		double height = myPaper.getMarginHeight();
		
		float f;
		if( width < height ) {
			f = (float)( width / (double)myImage.getSourceImage().getWidth() );
		} else {
			f = (float)( height / (double)myImage.getSourceImage().getHeight() );
		}
		myImage.setScale(f,-f);
	}

	private void setPreferredDrawStyle(int style) {
		prefs.putInt("Draw Style", style);
	}
	
	private void setPreferredFillStyle(int style) {
		prefs.putInt("Fill Style", style);
	}

	private int getPreferredDrawStyle() {
		return prefs.getInt("Draw Style", 0);
	}

	private int getPreferredFillStyle() {
		return prefs.getInt("Fill Style", 0);
	}
	
	public static boolean isFilenameForAnImage(String filename) {
		final String filenameExtension = filename.substring(filename.lastIndexOf('.') + 1);
		List<String> valid = Arrays.asList(IMAGE_FILE_EXTENSIONS);
		return valid.contains(filenameExtension.toLowerCase());
	}

	private void stopConversion() {
		logger.debug("Stop conversion");
		if(imageConverterThread!=null) imageConverterThread.cancel(true);
		stopWorker();
	}
	
	private void startConversion() {
		if(myConverterPanel==null || myImage==null) return;
		logger.debug("startConversion() {}", myConverterPanel.getName());
		startWorker();
	}
	
	private void changeConverter(ImageConverterPanel chosenPanel) {
		if( chosenPanel == myConverterPanel ) return;
		logger.debug("changeConverter() {}", chosenPanel.getName());
		stopConversion();

		if(myConverterPanel!=null) {
			myConverterPanel.removeSelectPanelChangeListener(this);
		}
		if(chosenPanel!=null) {
			chosenPanel.addSelectPanelChangeListener(this);
		}
		myConverterPanel = chosenPanel;
		
		startConversion();
	}

	@Override
	public void selectPanelPropertyChange(PropertyChangeEvent evt) {
		reconvert();
	}
	
	private void reconvert() {
		logger.debug("reconvert()");
		stopConversion();
		startConversion();
	}
	
	private void stopWorker() {
		if(myConverterPanel!=null) {
			myConverterPanel.getConverter().stopIterating();
		}
		if(imageConverterThread!=null) {
			logger.debug("Stopping worker");
			if(imageConverterThread.cancel(true)) {
				logger.debug("stop OK");
			} else {
				logger.debug("stop FAILED");
			}
		}
	}

	private void startWorker() {
		if(myImage==null || myPaper==null) return;
		
		logger.debug("startWorker()");
		
		ProgressMonitor pm = new ProgressMonitor(null, Translator.get("Converting"), "", 0, 100);
		pm.setProgress(0);
		pm.setMillisToPopup(0);
		
		ImageConverter converter = myConverterPanel.getConverter();
		converter.setProgressMonitor(pm);
		converter.setPaper(myPaper);
		converter.setImage(myImage);
		
		imageConverterThread = getNewWorker(converter,workerCount);
		addWorker(imageConverterThread);
		imageConverterThread.execute();
	}
	
	private ImageConverterThread getNewWorker(ImageConverter converter, int workerCount2) {
		ImageConverterThread thread = new ImageConverterThread(converter,Integer.toString(workerCount2));
		
		thread.addPropertyChangeListener((evt) -> {
			String propertyName = evt.getPropertyName(); 
			if(propertyName.equals("progress")) {
				int progress = (Integer) evt.getNewValue();
				pm.setProgress(progress);
				String message = String.format("%d%%.\n", progress);
				pm.setNote(message);
			}
			if(propertyName.equals("state")) {
				if(evt.getNewValue()==StateValue.DONE) {
					if (imageConverterThread.isDone()) {
						logger.debug("Finished");
						notifyListeners(new ActionEvent(converter.turtle,0,"turtle"));
					} else if (imageConverterThread.isCancelled() || pm.isCanceled()) {
						logger.debug("Cancelled");
						if(pm.isCanceled()) imageConverterThread.cancel(true);
					}
					removeWorker(thread);
				}
			}
		});

		return thread;
	}
	
	private void addWorker(ImageConverterThread thread) {
		workerList.add(thread);
		workerCount++;
		logger.debug("Added worker. {} workers now.", workerList.size());
	}
	
	private void removeWorker(ImageConverterThread thread) {
		workerList.remove(thread);
		workerCount--;
		logger.debug("Removed worker. {} workers now.", workerList.size());
		if(imageConverterThread==thread)
			imageConverterThread=null;
	}

	@Override
	public void render(GL2 gl2) {
		if( myConverterPanel instanceof PreviewListener ) { 
			((PreviewListener)myConverterPanel).render(gl2);
		}
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
	
	// TEST
	
	public static void main(String[] args) throws Exception {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		TransformedImage image = new TransformedImage(ImageIO.read(new FileInputStream("C:/Users/aggra/Documents/drawbot art/grumpyCat.jpg")));
		JFrame frame = new JFrame(ConvertImagePanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ConvertImagePanel(new Paper(),image));
		frame.pack();
		frame.setVisible(true);
	}
}
