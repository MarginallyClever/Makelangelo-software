package com.marginallyclever.makelangelo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.NotImplementedException;

import com.marginallyclever.core.Translator;
import com.marginallyclever.makelangelo.plotter.Makelangelo5;
import com.marginallyclever.makelangelo.plotter.Plotter;

public class PanelManageMachines extends javax.swing.JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2037125086872692857L;

	private AllPlotters allPlotters;
	private JList<String> list;
	private DefaultListModel<Object> backend;
	

	public PanelManageMachines(AllPlotters plotters) {
		super();
		allPlotters = plotters;
				
		// get all names
		backend = new DefaultListModel<Object>();

		int count = allPlotters.length();
		for(int i=0;i<count;++i) {
			Plotter p = allPlotters.get(i);
			String name = p.getName()+" "+p.getUID();
			backend.addElement(name);
		}

		// make list
		JList<Object> list = new JList<Object>();
		list.setModel(backend);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        
        // make list scrollable
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 250));
        //listScroller.setAlignmentX(LEFT_ALIGNMENT);
        
        //Create and initialize the buttons.
        JButton buttonEdit = new JButton("Edit");
        buttonEdit.setActionCommand("Edit");
        buttonEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editSelectedMachine(list);
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
				deleteSelectedMachine(list);
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

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(listPane);
        add(buttonPane);
        
        list.setSelectedIndex(0);
	}

	private void editSelectedMachine(JList<Object> list) {
		Plotter p = allPlotters.get(list.getSelectedIndex());
		
		DialogMachineSettings m = new DialogMachineSettings(,Translator.get("Makelangelo.robotSettings"),true);
		m.run(robotController);
	}
	
	public void addNewMachine() {
		//Plotter p = new Makelangelo5();
		//allPlotters.add(p);
	}
	
	private void deleteSelectedMachine(JList<Object> list) {
		Plotter p = allPlotters.get(list.getSelectedIndex());
		int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete "+(p.getName()+" "+p.getUID())+"?");
		if(result == JOptionPane.YES_OPTION) {
			int index=list.getSelectedIndex();
			if(index!=-1) {
				backend.remove(index);
				list.setModel(backend);
				
				allPlotters.delete(index);
			}
		}
	}
	
	/**
	 * Refresh the list of available known machines. If we are connected to a
	 * machine, select that machine number and disable the ability to change
	 * selection.
	 */
	private void updateMachineNumber() {
		JPanel machineNumberPanel = new JPanel();
		GridBagConstraints cMachine = new GridBagConstraints();
		cMachine.fill = GridBagConstraints.HORIZONTAL;
		cMachine.anchor = GridBagConstraints.CENTER;
		cMachine.gridx = 0;
		cMachine.gridy = 0;

		if (allPlotters.length() == 0)
			return;

		ArrayList<String> names = new ArrayList<String>();
		for (int i = 0; i < allPlotters.length(); ++i) {
			names.add(allPlotters.get(i).getName());
		}
		JComboBox<String> machineChoices = new JComboBox<String>((String[]) names.toArray());

		JLabel label = new JLabel(Translator.get("MachineNumber"));
		cMachine.insets = new Insets(0, 0, 0, 5);
		machineNumberPanel.add(label, cMachine);
		cMachine.insets = new Insets(0, 0, 0, 0);

		cMachine.gridx++;
		machineNumberPanel.add(machineChoices, cMachine);
		cMachine.gridx++;

		machineChoices.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// TODO ?
				}
			}
		});
	}
}
