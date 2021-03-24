package com.marginallyclever.makelangelo;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.marginallyclever.core.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;

public class AllPlottersGUI {
	// all plotters we care about
	private AllPlotters allPlotters;

	// The thing which draws the list of names 
	private JList<Object> listOfNames;
	// the backend database for the thing which draws the list of names.
	// edit this list, then tell {@link AllPlottersGUI#listOfNames} to update.
	private DefaultListModel<Object> listBackend;
	
	private Frame myParent;

	
	public AllPlottersGUI(AllPlotters plotters) {
		super();
		allPlotters = plotters;
	}
	
	public void run(Frame parent) {
		myParent = parent;
		
		// get all names
		listBackend = new DefaultListModel<Object>();

		int count = allPlotters.length();
		for(int i=0;i<count;++i) {
			Plotter p = allPlotters.get(i);
			String name = p.getName()+" "+p.getUID();
			listBackend.addElement(name);
		}

		// make list
		listOfNames = new JList<Object>();
		listOfNames.setModel(listBackend);
		listOfNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listOfNames.setLayoutOrientation(JList.VERTICAL);
        listOfNames.setVisibleRowCount(-1);
        
        // make list scrollable
        JScrollPane listScroller = new JScrollPane(listOfNames);
        listScroller.setPreferredSize(new Dimension(250, 250));
        //listScroller.setAlignmentX(LEFT_ALIGNMENT);
        
        //Create and initialize the buttons.
        JButton buttonEdit = new JButton("Edit");
        buttonEdit.setActionCommand("Edit");
        buttonEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editSelectedMachine();
			}
		});

        JButton buttonAdd = new JButton("+");
        buttonAdd.setActionCommand("Add");
        buttonAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addNewMachine();
			}
		});

        JButton buttonRemove = new JButton("-");
        buttonRemove.setActionCommand("Remove");
        buttonRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedMachine();
			}
        }); 
        
        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(buttonEdit);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(buttonAdd);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(buttonRemove);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(listPane);
		panel.add(buttonPane);
        
        listOfNames.setSelectedIndex(0);

		JDialog dialog = new JDialog(parent,Translator.get("Makelangelo.manageMachines"),true);
		dialog.setContentPane(panel);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}

	private void editSelectedMachine() {
		Plotter p = allPlotters.get(listOfNames.getSelectedIndex());

		EditPlotterGUI panel = new EditPlotterGUI(p);
		JPanel interior = panel.getInteriorPanel();
		
		int result = JOptionPane.showConfirmDialog(
				myParent, 
				interior,
				Translator.get("Makelangelo.robotSettings"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if(result==JOptionPane.OK_OPTION) {
			p.saveConfig();
		}
	}
	
	public void addNewMachine() {
		NewPlotterGUI panel = new NewPlotterGUI();
		int result = JOptionPane.showConfirmDialog(
				myParent, 
				panel, 
				Translator.get("addMachine"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			try {
				Plotter p = panel.getNewPlotter();
				p.saveConfig();
				allPlotters.add(p);
				
				listBackend.addElement(p.getName()+" "+p.getUID());
				listOfNames.setModel(listBackend);
				
				// TODO open edit panel immediately?
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
	
	private void deleteSelectedMachine() {
		int index = listOfNames.getSelectedIndex();
		if(index==-1) {
			// no selection.
			return;
		}
		
		Plotter p = allPlotters.get(index);
		int result = JOptionPane.showConfirmDialog(
				myParent,
				Translator.get("confirmDeleteMachine",(p.getName()+" "+p.getUID())),
				Translator.get("confirmDeleteMachine",(p.getName()+" "+p.getUID())),
				JOptionPane.YES_NO_OPTION);
		if(result == JOptionPane.YES_OPTION) {

			if(index!=-1) {
				listBackend.remove(index);
				listOfNames.setModel(listBackend);
				
				allPlotters.delete(index);
			}
		}
	}
}
