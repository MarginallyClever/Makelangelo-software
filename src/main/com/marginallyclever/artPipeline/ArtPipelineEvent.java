package com.marginallyclever.artPipeline;

public class ArtPipelineEvent {
	public static final int CANCELLED = 0; 
	public static final int FINISHED = 1;
	
	public int type;
	public ArtPipeline from;
	public Object extra;
	
	ArtPipelineEvent(int type, ArtPipeline from,Object extra) {
		this.type = type;
		this.from = from;
		this.extra = extra;
	}
}
