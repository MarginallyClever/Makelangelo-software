package com.marginallyclever.makelangelo;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.ServiceLoader;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.marginallyclever.core.log.Log;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.PlotterModel;
import com.marginallyclever.util.PreferencesHelper;

/**
 * {@link Plotter} instances are stored in preference nodes.  This is the factory that restores them from disk as needed.
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class AllPlotters {
	private String [] configsAvailable; 
	
	public AllPlotters() {
		refreshPlotters();
	}
	
	public void refreshPlotters() {
		// which configurations are available?
		try {
			Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
			configsAvailable = topLevelMachinesPreferenceNode.childrenNames();
		} catch (Exception e) {
			Log.error( e.getMessage() );
		}
	}

	public int length() {
		return configsAvailable.length;
	}

	/**
	 * @param i
	 * @return the ith plotter in the list.
	 */
	public Plotter get(int i) {
		//if(i<0 || i>= configsAvailable.length) throw new InvalidParameterException("i ("+i+") >=0, <configsAvailable.length ("+configsAvailable.length+")");

		Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(configsAvailable[i]);
		if(uniqueMachinePreferencesNode==null) {
			throw new InvalidParameterException("Plotter '"+configsAvailable[i]+"' could not be loaded.");
		}
		String typeName = uniqueMachinePreferencesNode.get("hardwareVersion", "5");
		// search the services for the type of machine.
		ServiceLoader<Plotter> slp = ServiceLoader.load(Plotter.class);
		for( Plotter p : slp ) {
			if(p.getVersion().contentEquals(typeName)) {
				try {
					Plotter newP = (Plotter) p.getClass().getDeclaredConstructor().newInstance();
					newP.loadConfig(Long.parseLong(configsAvailable[i]));
					return newP;
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | SecurityException | NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
		}
		
		throw new InvalidParameterException("Plotter '"+configsAvailable[i]+"' of type '"+typeName+"' could not be constructed.");
	}

	public void delete(int index) {
		Plotter p = get(index);
		System.out.println("Deleting "+(p.getName()+" "+p.getUID()));
		
		Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences deadNode = topLevelMachinesPreferenceNode.node(Long.toString(p.getUID()));
		try {
			deadNode.removeNode();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		refreshPlotters();
	}

	public void add(PlotterModel plotter) {
		
		
	}
}
