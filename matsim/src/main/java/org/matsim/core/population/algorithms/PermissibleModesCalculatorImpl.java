/* *********************************************************************** *
 * project: org.matsim.*
 * PermissibleModesCalculatorImpl.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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
package org.matsim.core.population.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.config.Config;
import org.matsim.core.population.PersonUtils;

public final class PermissibleModesCalculatorImpl implements PermissibleModesCalculator {

	private final List<String> availableModes;
	private final List<String> availableModesWithoutCar;
	private final boolean considerCarAvailability;
	private final List<String> availableModesWithoutBike;
	private List<String> availableModesWithoutBikeandCar;
	private final List<String> availableModeswithoutDRT;
	private final List<String> availableModeswithoutDRTandCar;
	private final List<String> availableModeswithoutDRTandBike;
	private final List<String> availableModeswithoutDRTandCarandBike;

	@Inject
	public PermissibleModesCalculatorImpl(Config config) {
		this.availableModes = Arrays.asList(config.subtourModeChoice().getModes());

		if (this.availableModes.contains("drt_av")) {
			final List<String> l = new ArrayList<String>(this.availableModes);
			while (l.remove("drt_av")) {
			}
			this.availableModeswithoutDRT = Collections.unmodifiableList(l);
		} else {
			this.availableModeswithoutDRT = this.availableModes;
		}

		if (this.availableModes.contains(TransportMode.car)) {
			final List<String> l = new ArrayList<String>(this.availableModes);
			while (l.remove(TransportMode.car)) {
			}
			this.availableModesWithoutCar = Collections.unmodifiableList(l);
		} else {
			this.availableModesWithoutCar = this.availableModes;
		}
		if (this.availableModes.contains(TransportMode.bike)) {
			final List<String> l = new ArrayList<String>(this.availableModes);
			while (l.remove(TransportMode.bike)) {
			}
			this.availableModesWithoutBike = Collections.unmodifiableList(l);
		} else {
			this.availableModesWithoutBike = this.availableModes;
		}
		if (this.availableModes.contains(TransportMode.bike) || this.availableModes.contains(TransportMode.car)) {
			final List<String> l = new ArrayList<String>(this.availableModes);
			while (l.remove(TransportMode.car)) {
			}
			while (l.remove(TransportMode.bike)) {
			}
			this.availableModesWithoutBikeandCar = Collections.unmodifiableList(l);
		} else {
			this.availableModesWithoutBikeandCar = this.availableModes;
		}
		if (this.availableModes.contains("drt_av") || this.availableModes.contains(TransportMode.car)) {
			final List<String> l = new ArrayList<String>(this.availableModes);
			while (l.remove(TransportMode.car)) {
			}
			while (l.remove("drt_av")) {
			}
			this.availableModeswithoutDRTandCar = Collections.unmodifiableList(l);
		} else {
			this.availableModeswithoutDRTandCar = this.availableModes;
		}
		if (this.availableModes.contains("drt_av") || this.availableModes.contains(TransportMode.bike)) {
			final List<String> l = new ArrayList<String>(this.availableModes);
			while (l.remove(TransportMode.bike)) {
			}
			while (l.remove("drt_av")) {
			}
			this.availableModeswithoutDRTandBike = Collections.unmodifiableList(l);
		} else {
			this.availableModeswithoutDRTandBike = this.availableModes;
		}
		if (this.availableModes.contains("drt_av") || this.availableModes.contains(TransportMode.bike)|| this.availableModes.contains(TransportMode.car)) {
			final List<String> l = new ArrayList<String>(this.availableModes);
			while (l.remove(TransportMode.car)) {
			}
			while (l.remove(TransportMode.bike)) {
			}
			while (l.remove("drt_av")) {
			}
			this.availableModeswithoutDRTandCarandBike = Collections.unmodifiableList(l);
		} else {
			this.availableModeswithoutDRTandCarandBike = this.availableModes;
		}
		
		this.considerCarAvailability = config.subtourModeChoice().considerCarAvailability();
	}

	@Override
	public Collection<String> getPermissibleModes(final Plan plan) {
		if (!considerCarAvailability) return availableModes; 

		final Person person;
		try {
			person = plan.getPerson();
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException( "I need a PersonImpl to get car availability" );
		}

		final boolean carAvail =
			!"no".equals( PersonUtils.getLicense(person) ) &&
			!"never".equals( PersonUtils.getCarAvail(person) );
		
		final boolean bikeAvail = !"never".equals( PersonUtils.getBikeAvail(person) );

		boolean drtAvail = false;
		Object subpop = plan.getPerson().getAttributes().getAttribute("subpopulation");
		if(subpop != null)
			if(subpop.toString().equals("senior"))
				drtAvail = true;

		if(drtAvail) {
			if (carAvail && bikeAvail)
				return availableModes;
			else if (carAvail && !bikeAvail)
				return availableModesWithoutBike;
			else if (!carAvail && bikeAvail)
				return availableModesWithoutCar;
			return availableModesWithoutBikeandCar;
		}
		else {
			if (carAvail && bikeAvail)
				return availableModeswithoutDRT;
			else if (carAvail && !bikeAvail)
				return availableModeswithoutDRTandBike;
			else if (!carAvail && bikeAvail)
				return availableModeswithoutDRTandCar;
			return availableModeswithoutDRTandCarandBike;
		}
	}
}
