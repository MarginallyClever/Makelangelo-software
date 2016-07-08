package com.marginallyclever.loaders;

import java.util.LinkedList;

/**
 * Used by LoadDXF2.  A collection of DXFBucketEntities
 * @author Dan Royer
 *
 */
public class DXFGroup {
	public LinkedList<DXFBucketEntity> entities;
	
	public DXFGroup() {
		entities = new LinkedList<DXFBucketEntity>();
	}
	
	public void addLast(DXFBucketEntity e) {
		entities.add(e);
	}

	public void addFirst(DXFBucketEntity e) {
		entities.addFirst(e);
	}
}
