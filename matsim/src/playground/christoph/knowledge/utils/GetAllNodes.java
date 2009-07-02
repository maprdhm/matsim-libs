/* *********************************************************************** *
 * project: org.matsim.*
 * GetAllNodes.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * @author Christoph Dobler
 * 
 * Liefert eine ArrayList von Nodes.
 * �bergabeparameter muss ein Netzwerk, eine Person oder ein Plan sein.
 * Wird eine Person �bergeben, so wird der jeweils aktuelle Plan verwendet.
 * Wird zus�tzlich noch eine ArrayList Nodes mit �bergeben, so wird diese
 * mit den neu gefundenen Nodes erweitert. Andernfalls wird eine neue erstellt.
 *
 */


package playground.christoph.knowledge.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.matsim.api.basic.v01.Id;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;

public class GetAllNodes {

	public Map<Id, NodeImpl> getAllNodes(NetworkLayer n)
	{
		return getNodes(n);
	}
	
	public void getAllNodes(NetworkLayer n, Map<Id, NodeImpl> nodesMap)
	{	
		getNodes(n, nodesMap);
	}
	
	public Map<Id, NodeImpl> getAllNodes(PersonImpl p)
	{
		return getNodes(new GetAllLinks().getAllLinks(p));
	}
	
	public void getAllNodes(PersonImpl p, Map<Id, NodeImpl> nodesMap)
	{
		getNodes(new GetAllLinks().getAllLinks(p), nodesMap);
	}
	
	public Map<Id, NodeImpl> getAllNodes(PlanImpl p)
	{
		return getNodes(new GetAllLinks().getAllLinks(p));
	}
	
	public void getAllNodes(PlanImpl p, Map<Id, NodeImpl> nodesMap)
	{
		getNodes(new GetAllLinks().getAllLinks(p), nodesMap);
	}
	
	
	
	// Liefert eine ArrayList aller Nodes, welche Teil der uebergebenen Links sind.
	// Da keine ArrayList mit bereits selektieren Nodes uebergeben wurde, wird diese neu erstellt. 
	protected Map<Id, NodeImpl> getNodes(ArrayList<LinkImpl> links)
	{
		Map <Id, NodeImpl> nodesMap = new TreeMap<Id, NodeImpl>();
		
		getNodes(links, nodesMap);
		
		return nodesMap; 
	} // getNodes(ArrayList<Link>)
	
	
	// Liefert eine ArrayList aller Nodes, welche Teil der uebergebenen Links sind.
	protected void getNodes(ArrayList<LinkImpl> links, Map<Id, NodeImpl> nodesMap)
	{
		Iterator<LinkImpl> linksIterator = links.iterator();
		
		while(linksIterator.hasNext())
		{
			LinkImpl link = linksIterator.next();
			
			NodeImpl fromNode = link.getFromNode();
			NodeImpl toNode = link.getToNode();
			
			if (!nodesMap.containsKey(fromNode.getId())) nodesMap.put(fromNode.getId(), fromNode);
			if (!nodesMap.containsKey(toNode.getId())) nodesMap.put(toNode.getId(), toNode);
		}

	}
	
	
	protected Map<Id, NodeImpl> getNodes(NetworkLayer n)
	{
		Map<Id, NodeImpl> nodesMap = new TreeMap<Id, NodeImpl>();

		getNodes(n, nodesMap);
		
		return nodesMap;
	} //getNodes(NetworkLayer n)
	
	
	protected void getNodes(NetworkLayer n, Map<Id, NodeImpl> nodesMap)
	{
		// get all nodes of the network
		Map<Id, NodeImpl> networkNodesMap = n.getNodes();
				
		// iterate over Array or Iteratable
		for (NodeImpl node : networkNodesMap.values()) 
		{
			// Which one is faster / better?
			if(!nodesMap.containsKey(node.getId())) nodesMap.put(node.getId(), node);
			
			//if(!nodesMap.containsValue(node)) nodesMap.put(node.getId(), node);
		}
	
	} //getNodes(NetworkLayer n, ArrayList<Node) nodes)
	
}