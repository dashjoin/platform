/*
 * inova8 2020
 */
package com.inova8.intelligentgraph.dashjoin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.rdf4j.model.Value;

/**
 * The Class Resource.
 */
public class Resource extends HashMap<String, Object>{
	 
 	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    public List<Object> pk = new ArrayList<>();	 
 	/**
	  * Instantiates a new resource.
	  *
	  * @param value the value
	  */
 	public Resource( String database ,org.eclipse.rdf4j.model.Value value ) {
 		this.put("database",  database);
 		this.put("table",  null);
 		List<Object> pk = new ArrayList<>();	 
		if(value instanceof org.eclipse.rdf4j.model.Value) {
			 pk.add(value.stringValue());		 
		 }else {
			 pk.add(value.stringValue());
		 }
	 	 this.put("pk",  pk);
	 }
 	public Resource(  String database, org.eclipse.rdf4j.model.Value... values ) {
 		this.put("database",  database);
 		this.put("table",  null);
 		List<Object> pk = new ArrayList<>();	
		for(Value value: values) {
			if(value instanceof org.eclipse.rdf4j.model.Value) {
				 pk.add(value.stringValue());		 
			 }else {
				 pk.add(value.stringValue());
			 }
 		}
	 	 this.put("pk",  pk);
	 }		
}
