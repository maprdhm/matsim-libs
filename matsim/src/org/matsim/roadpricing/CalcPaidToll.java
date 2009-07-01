/* *********************************************************************** *
 * project: org.matsim.*
 * CalcPaidToll.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.roadpricing;

import java.util.Map;
import java.util.TreeMap;

import org.matsim.api.basic.v01.Id;
import org.matsim.api.basic.v01.events.BasicAgentWait2LinkEvent;
import org.matsim.api.basic.v01.events.BasicLinkEnterEvent;
import org.matsim.api.basic.v01.events.BasicPersonEvent;
import org.matsim.api.basic.v01.events.handler.BasicAgentWait2LinkEventHandler;
import org.matsim.api.basic.v01.events.handler.BasicLinkEnterEventHandler;
import org.matsim.core.events.AgentMoneyEvent;
import org.matsim.core.events.AgentWait2LinkEvent;
import org.matsim.core.events.Events;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.roadpricing.RoadPricingScheme.Cost;

/**
 * Calculates the toll agents pay during a simulation by analyzing events. To
 * fully function, add an instance of this class as EventHandler to your Events
 * object.
 *
 * @author mrieser
 */
public class CalcPaidToll implements BasicLinkEnterEventHandler, BasicAgentWait2LinkEventHandler {

	static class AgentInfo {
		public double toll = 0.0;
		public boolean insideCordonArea = true;
	}

	final RoadPricingScheme scheme;
	final TreeMap<Id, AgentInfo> agents = new TreeMap<Id, AgentInfo>();
	private final NetworkLayer network;

	private TollBehaviourI handler = null;

	public CalcPaidToll(final NetworkLayer network, final RoadPricingScheme scheme) {
		super();
		this.network = network;
		this.scheme = scheme;
		if (RoadPricingScheme.TOLL_TYPE_DISTANCE.equals(scheme.getType())) {
			this.handler = new DistanceTollBehaviour();
		} else if (RoadPricingScheme.TOLL_TYPE_AREA.equals(scheme.getType())) {
			this.handler = new AreaTollBehaviour();
		} else if (RoadPricingScheme.TOLL_TYPE_CORDON.equals(scheme.getType())) {
			this.handler = new CordonTollBehaviour();
		} else {
			throw new IllegalArgumentException("RoadPricingScheme of type \"" + scheme.getType() + "\" is not supported.");
		}
	}

	public void handleEvent(final BasicLinkEnterEvent event) {
		LinkImpl link = this.network.getLink(event.getLinkId());
		this.handler.handleEvent(event, link);
	}

	public void handleEvent(final BasicAgentWait2LinkEvent event) {
		LinkImpl link = this.network.getLink(event.getLinkId());
		this.handler.handleEvent(event, link);
	}

	/**
	 * Sends {@link AgentMoneyEvent}s for all agents that must pay a toll.
	 * This method should usually be called at the end before of an iteration.
	 *
	 * <strong>Important note: </strong>Do not call this method twice without
	 * calling {@link #reset(int)} in between. Otherwise the toll-disutility
	 * may be added twice to the agents' score!
	 *
	 * @param time the current time the generated events are associated with
	 * @param events the {@link Events} collection, the generated events are sent to for processing
	 */
	public void sendUtilityEvents(final double time, final Events events) {
		for (Map.Entry<Id, AgentInfo> entries : this.agents.entrySet()) {
			events.processEvent(new AgentMoneyEvent(time, entries.getKey(), -entries.getValue().toll));
		}
	}

	public void reset(final int iteration) {
		this.agents.clear();
	}

	/**
	 * Returns the toll the specified agent has paid in the course of the
	 * simulation so far.
	 *
	 * @param agentId
	 * @return The toll paid by the specified agent, 0.0 if no toll was paid.
	 */
	public double getAgentToll(final Id agentId) {
		AgentInfo info = this.agents.get(agentId);
		if (info == null) {
			return 0.0;
		}
		return info.toll;
	}

	/**
	 * @return The toll paid by all the agents.
	 */
	public double getAllAgentsToll() {
		double tolls = 0;
		for (AgentInfo ai : this.agents.values()) {
			tolls += (ai == null) ? 0.0 : ai.toll;
		}
		return tolls;
	}

	/**
	 * @return The Number of all the Drawees.
	 */
	public int getDraweesNr() {
		int dwCnt = 0;
		for (AgentInfo ai : this.agents.values()) {
			if ((ai != null) && (ai.toll > 0.0)) {
					dwCnt++;
			}
		}
		return dwCnt;
	}

	/**
	 * A simple interface to implement different toll schemes.
	 */
	private interface TollBehaviourI {
		public void handleEvent(BasicPersonEvent event, LinkImpl link);
	}

	/**
	 * Handles the calculation of the distance toll. If an agent enters a link at
	 * a time the toll has to be paid, the toll amount is added to the agent. The
	 * agent does not have to pay the toll for a link if it starts on the link,
	 * as it may have paid already when arriving on the link.
	 */
	class DistanceTollBehaviour implements TollBehaviourI {
		public void handleEvent(final BasicPersonEvent event, final LinkImpl link) {
			if (event instanceof AgentWait2LinkEvent) {
				/* we do not handle wait2link-events for distance toll, because the agent
				 * must not pay twice for this link, and he (likely) paid already when
				 * arriving at this link.  */
				return;
			}
			Cost cost = CalcPaidToll.this.scheme.getLinkCost(link.getId(),
					event.getTime());
			if (cost != null) {
				double newToll = link.getLength() * cost.amount;
				AgentInfo info = CalcPaidToll.this.agents.get(event.getPersonId());
				if (info == null) {
					info = new AgentInfo();
					CalcPaidToll.this.agents.put(event.getPersonId(), info);
				}
				info.toll += newToll;
			}
		}
	}

	/** Handles the calculation of the area toll. Whenever the agent is seen on
	 * one of the tolled link, the constant toll amount has to be paid once. */
	class AreaTollBehaviour implements TollBehaviourI {
		public void handleEvent(final BasicPersonEvent event, final LinkImpl link) {
			Cost cost = CalcPaidToll.this.scheme.getLinkCost(link.getId(), event.getTime());
			if (cost != null) {
				AgentInfo info = CalcPaidToll.this.agents.get(event.getPersonId());
				if (info == null) {
					info = new AgentInfo();
					CalcPaidToll.this.agents.put(event.getPersonId(), info);
					info.toll = cost.amount;
				}
			}
		}
	}

	/**
	 * Handles the calculation of the cordon toll. An agent has only to pay if he
	 * crosses the cordon from the outside to the inside.
	 */
	class CordonTollBehaviour implements TollBehaviourI {
		public void handleEvent(final BasicPersonEvent event, final LinkImpl link) {
			Cost cost = CalcPaidToll.this.scheme.getLinkCost(link.getId(), event.getTime());
			if (cost != null) {
				// this is a link inside the toll area.
				AgentInfo info = CalcPaidToll.this.agents.get(event.getPersonId());
				if (info == null) {
					// no information about this agent, so it did not yet pay the toll
					info = new AgentInfo();
					CalcPaidToll.this.agents.put(event.getPersonId(), info);
					info.toll = 0.0; // we start in the area, do not toll
				} else if (!info.insideCordonArea) {
					// agent was outside before, now inside the toll area --> agent has to pay
					info.insideCordonArea = true;
					info.toll += cost.amount;
				}
			} else {
				// this is a link outside the toll area.
				AgentInfo info = CalcPaidToll.this.agents.get(event.getPersonId());
				if (info == null) {
					info = new AgentInfo();
					CalcPaidToll.this.agents.put(event.getPersonId(), info);
				}
				info.insideCordonArea = false;
			}
		}
	}

}
