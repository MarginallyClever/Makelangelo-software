package com.marginallyclever.makelangelo;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.ServiceLoader;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.marginallyclever.core.log.Log;
import com.marginallyclever.makelangelo.plotter.Plotter;
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
			Preferences topNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
			configsAvailable = topNode.childrenNames();
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

		Preferences topNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences iNode = topNode.node(configsAvailable[i]);
		if(iNode==null) {
			throw new InvalidParameterException("Plotter '"+configsAvailable[i]+"' could not be loaded.");
		}
		String typeName = iNode.get("hardwareVersion", "5");
		// search the services for the type of machine.
		ServiceLoader<Plotter> slp = ServiceLoader.load(Plotter.class);
		for( Plotter p : slp ) {
			if(p.getVersion().contentEquals(typeName)) {
				try {
					Plotter newP = (Plotter) p.getClass().getDeclaredConstructor().newInstance();
					newP.loadConfig(configsAvailable[i]);
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
		
		Preferences topNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		Preferences deadNode = topNode.node(Long.toString(p.getUID()));
		try {
			deadNode.removeNode();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		refreshPlotters();
	}

	public void add(Plotter p) {
		System.out.println("Adding "+(p.getName()+" "+p.getUID()));
		Preferences topNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
		try {
			String [] names = topNode.childrenNames();
			System.out.println(names.length+" names.");
			int biggestID=0;
			for( String s : names ) {
				System.out.println("\t"+s);
				int id = Integer.parseInt(s);
				if(biggestID<id) biggestID=id;
			}
			p.setNodeName(Integer.toString(biggestID+1));
			p.saveConfig();
			refreshPlotters();
		} catch (BackingStoreException e) {
			Log.error("Adding new robot failed.");
			e.printStackTrace();
		}
	}
}
