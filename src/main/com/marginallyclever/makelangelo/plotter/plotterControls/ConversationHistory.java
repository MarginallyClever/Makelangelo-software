package com.marginallyclever.makelangelo.plotter.plotterControls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionListener;

import com.marginallyclever.convenience.log.Log;

public class ConversationHistory extends JPanel {
	private static final long serialVersionUID = 6287436679006933618L;
	private DefaultListModel<ConversationEvent> listModel = new DefaultListModel<ConversationEvent>();
	private JList<ConversationEvent> listView = new JList<ConversationEvent>(listModel);
	private ConcurrentLinkedQueue<ConversationEvent> inBoundQueue = new ConcurrentLinkedQueue<ConversationEvent>();
	private JFileChooser chooser = new JFileChooser();

	private JButton bClear = new JButton("Clear");
	private JButton bSave = new JButton("Save");

	
	public ConversationHistory() {
		super();
		createCellRenderingSystem();

		JScrollPane scrollPane = new JScrollPane(listView);
		scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		this.setBorder(BorderFactory.createTitledBorder(ConversationHistory.class.getSimpleName()));
		this.setLayout(new BorderLayout());
		this.add(getToolBar(), BorderLayout.PAGE_START);
		this.add(scrollPane, BorderLayout.CENTER);
	}
	
	private JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		bar.setRollover(true);

		bar.add(bClear);
		bar.add(bSave);
		
		bClear.addActionListener( (e) -> runNewAction() );
		bSave.addActionListener( (e) -> runSaveAction() );
		
		return bar;
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
					if(!value.whoSpoke.contentEquals("You")) {
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
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, e1.getLocalizedMessage(),"runSaveAction error",JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
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
		while(!inBoundQueue.isEmpty()) {
			ConversationEvent msg = inBoundQueue.poll();
			if(msg!=null) listModel.addElement(msg);
		}
	}
		
	private void jumpToEnd() {
		listView.ensureIndexIsVisible(listModel.getSize()-1);
	}
	
	// TEST
	
	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame(ConversationHistory.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ConversationHistory());
		frame.pack();
		frame.setVisible(true);
	}
}
