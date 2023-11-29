package com.marginallyclever.makelangelo.plotter.plottercontrols;

import com.marginallyclever.convenience.ButtonIcon;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * {@link ConversationHistory} maintains a history of the dialog between two or more parties.
 * New entries can be added with {@code addElement(senderName,message)}.
 * {@link ListSelectionListener}s will be notified if the user selects a line entry in the history.
 * @author Dan Royer
 * @since 7.28.0
 */
public class ConversationHistory extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(ConversationHistory.class);
	private final DefaultListModel<ConversationEvent> listModel = new DefaultListModel<>();
	private final JList<ConversationEvent> listView = new JList<>(listModel);
	private final ConcurrentLinkedQueue<ConversationEvent> inBoundQueue = new ConcurrentLinkedQueue<>();
	private final JFileChooser chooser = new JFileChooser();
	private final ButtonIcon bClear = new ButtonIcon(Translator.get("ConversationHistory.Clear"), "/images/application.png");
	private final ButtonIcon bSave = new ButtonIcon(Translator.get("ConversationHistory.Save"), "/images/disk.png");
	private final ButtonIcon bCopy = new ButtonIcon(Translator.get("ConversationHistory.Copy"), "/images/copy2clipboard.png");

	
	public ConversationHistory() {
		super(new BorderLayout());
		createCellRenderingSystem();

		JScrollPane scrollPane = new JScrollPane(listView);
		scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listView.setPrototypeCellValue(new ConversationEvent(" "," "));
		listView.setVisibleRowCount(20);

		this.setBorder(BorderFactory.createTitledBorder(ConversationHistory.class.getSimpleName()));
		this.add(getToolBar(), BorderLayout.PAGE_START);
		this.add(scrollPane, BorderLayout.CENTER);
	}
	
	private JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);

		bar.add(bCopy);
		bar.add(bSave);
		bar.add(bClear);

		bCopy.addActionListener(e -> copyToClipboard());
		bClear.addActionListener( (e) -> runNewAction() );
		bSave.addActionListener( (e) -> runSaveAction() );
		
		return bar;
	}

	private void copyToClipboard() {
		StringBuilder sb = new StringBuilder();

		int size=listModel.getSize();
		for(int i=0;i<size;++i) {
			String str = listModel.get(i).toString();
			sb.append(str).append("\n");
		}

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(sb.toString()), null);
	}

	private void createCellRenderingSystem() {
		listView.setCellRenderer(new ListCellRenderer<ConversationEvent>() {
			private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer(); 
			
			@Override
			public Component getListCellRendererComponent(JList<? extends ConversationEvent> list,
					ConversationEvent value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
				if(c instanceof JLabel) {
					JLabel jc = (JLabel)c;
					jc.setText(value.toString());
					if (value.whoSpoke.contentEquals("Error")) {
						jc.setForeground(Color.RED);
					} else if (!value.whoSpoke.contentEquals("You")) {
						jc.setForeground(Color.BLUE);
					}
				}
				return c;
			}
			
		});
	}

	private void runNewAction() {
		listModel.clear();
	}
	
	private void runSaveAction() {
		if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				saveFile(chooser.getSelectedFile());
			} catch (IOException e) {
				logger.error("Failed to save file", e);
				JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),"runSaveAction error",JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void saveFile(File file) throws IOException {
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
		int size=listModel.getSize();
		for(int i=0;i<size;++i) {
			String str = listModel.get(i).toString();
			if(!str.endsWith("\n")) str+="\n";
			fileWriter.write(str);
		}
		fileWriter.close();
	}
	
	public void clear() {
		runNewAction();
	}
	
	public void addListSelectionListener(ListSelectionListener listener) {
		listView.addListSelectionListener(listener);
	}

	public int getSelectedIndex() {
		return listView.getSelectedIndex();
	}

	public String getSelectedValue() {
		return listView.getSelectedValue().toString();
	}

	public void addElement(String src,String str) { 
		inBoundQueue.add(new ConversationEvent(src, str));
		repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		boolean isLast = (listView.getLastVisibleIndex() == listModel.getSize()-1);
		
		addQueuedMessages();
		
		if(isLast) jumpToEnd();
		
		super.paint(g);
	}

	private void addQueuedMessages() {
		listModel.addAll(inBoundQueue);
		inBoundQueue.clear();
	}
		
	private void jumpToEnd() {
		listView.ensureIndexIsVisible(listModel.getSize()-1);
	}
	
	// TEST
	
	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(ConversationHistory.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ConversationHistory ch = new ConversationHistory();
		frame.add(ch);
		frame.pack();
		frame.setVisible(true);

		ch.addElement("You", "N2 G28 XY*48");
		ch.addElement("/dev/cu.usbserial-1410", "X:0.00 Y:-186.00 Z:200.00 Count X:72290 Y:72290 Z:32000");
		ch.addElement("/dev/cu.usbserial-1410", "echo:; Advanced (B<min_segment_time_us> S<min_feedrate> T<min_travel_feedrate> X<max_x_jerk> Y<max_y_jerk> Z<max_z_jerk>):");
	}
}
