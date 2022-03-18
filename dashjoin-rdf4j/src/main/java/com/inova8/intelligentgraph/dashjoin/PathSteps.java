/*
 * inova8 2020
 */
package com.inova8.intelligentgraph.dashjoin;

import java.util.HashMap;

import com.inova8.intelligentgraph.path.Edge;
import com.inova8.intelligentgraph.path.Path;

/**
 * The Class PathSteps.
 */
public class PathSteps extends HashMap<String, Object>{

	private static final long serialVersionUID = 1L;
	public  PathSteps(String database,Path path ){
		Steps steps = new Steps();
		Boolean first =true;
		for (Edge edge : path) {
			if(first)this.put("start", new Node(database,edge.getSource()));
			first = false;
			steps.add(new Step(database,edge));
		}
		this.put("steps", steps);

	}


}