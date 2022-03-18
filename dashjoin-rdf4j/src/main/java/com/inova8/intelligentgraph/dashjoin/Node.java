package com.inova8.intelligentgraph.dashjoin;

import java.util.HashMap;

import org.eclipse.rdf4j.model.Value;

public class Node extends HashMap<String, Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public  Node( String database, Value node){
		this.put("_dj_resource", new Resource(database,node));	
	}
	
}
