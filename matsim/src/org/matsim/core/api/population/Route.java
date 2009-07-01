/* *********************************************************************** *
 * project: org.matsim.*
 * Route.java
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

package org.matsim.core.api.population;

import org.matsim.api.basic.v01.population.BasicRoute;
import org.matsim.core.network.LinkImpl;

/**
 * Defines the minimum amount of information a route in MATSim must provide.
 *
 * @author mrieser
 */
public interface Route extends BasicRoute {

	public double getTravelTime();
	public void setTravelTime(final double travelTime);

	public LinkImpl getStartLink();
	public void setStartLink(final LinkImpl link);

	public LinkImpl getEndLink();
	public void setEndLink(final LinkImpl link);
	
}
