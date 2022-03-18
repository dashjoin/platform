package com.inova8.intelligentgraph.dashjoin;

import java.util.HashMap;

import com.inova8.intelligentgraph.path.Edge;

public class Predicate extends HashMap<String, Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public  Predicate(String database,Edge edge){

		if(edge.getReification()!=null) {
			this.put("_dj_resource", new Resource(database, edge.getReified(),edge.getReification(),edge.getPredicate(),edge.getReification()));
		}else {
			this.put("_dj_resource", new Resource(database,edge.getPredicate()));	
		}
	}
}
