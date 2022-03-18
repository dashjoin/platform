/*
 * inova8 2020
 */
package com.inova8.intelligentgraph.dashjoin;

import java.util.HashMap;
import java.util.Map;

import com.inova8.intelligentgraph.path.Edge;
import com.inova8.intelligentgraph.vocabulary.PATHQL;

/**
 * The Class Step.
 */
public class Step extends HashMap<String, Object>{
    

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    public Map<String, Object> edge;
	
	/**
	 * Instantiates a new step.
	 *
	 * @param edge the edge
	 */
	public Step(String database, Edge edge ) {
		//Step step = new Step();
		this.put("end",  new Node(database, edge.getTarget()));		
		this.edge  = new HashMap<String,Object>();
		this.edge.put(PATHQL.EDGE_PREDICATESTRING , new Predicate( database,edge));
		if(edge.getDirection()!=null)this.edge.put(PATHQL.EDGE_DIRECTIONSTRING , edge.getDirection());
		if(edge.getIsDereified()!=null)this.edge.put(PATHQL.EDGE_DEREIFIEDSTRING , edge.getIsDereified());
		if(edge.getReification()!=null)this.edge.put(PATHQL.EDGE_REIFICATIONSTRING , edge.getReification());
		
		this.put("edge", this.edge);
		
	}   
}
