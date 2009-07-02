/* *********************************************************************** *
 * project: org.matsim.*
 * OnlyTimeDependentTravelCostCalculator.java
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

package playground.christoph.router.costcalculators;

import org.matsim.core.network.LinkImpl;
import org.matsim.core.router.util.TravelMinCost;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.misc.Time;

public class OnlyTimeDependentTravelCostCalculator implements TravelMinCost {
	protected final TravelTime timeCalculator;

	public OnlyTimeDependentTravelCostCalculator(final TravelTime timeCalculator)
	{
		this.timeCalculator = timeCalculator;
	}

	public double getLinkTravelCost(final LinkImpl link, final double time) 
	{
		if (timeCalculator != null)
		{
			double travelTime = this.timeCalculator.getLinkTravelTime(link, time);
			return travelTime;
		}
		else
		{
			return link.getFreespeedTravelTime(time);
		}
	}

	public double getLinkMinimumTravelCost(final LinkImpl link) 
	{
		double TravelTime = this.timeCalculator.getLinkTravelTime(link, Time.UNDEFINED_TIME);
		return TravelTime;
	}
}