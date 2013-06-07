/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package playground.mmoyo.taste_variations;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.pt.transitSchedule.api.TransitSchedule;

public class SVDScoringfunctionFactory implements ScoringFunctionFactory {
	private final Map <Id, SVDvalues> svdValuesMap;
	private final Network net;
	private final TransitSchedule schedule;
	
	public SVDScoringfunctionFactory(final Map <Id, SVDvalues> svdValuesMap, final Network net, final TransitSchedule schedule) {
		this.svdValuesMap = svdValuesMap;
		this.net = net; 
		this.schedule = schedule;
	}
	
	@Override
	public ScoringFunction createNewScoringFunction(final Plan plan) {
		final SVDvalues svdValues = svdValuesMap.get(plan.getPerson().getId());
		ScoringFunction svdScoringFunction = new SVDscoring(plan, svdValues, net, schedule);
		return svdScoringFunction;
	}
}