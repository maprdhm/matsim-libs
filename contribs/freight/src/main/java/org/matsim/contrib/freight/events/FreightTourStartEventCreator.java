/*
 *   *********************************************************************** *
 *   project: org.matsim.*
 *   *********************************************************************** *
 *                                                                           *
 *   copyright       : (C)  by the members listed in the COPYING,        *
 *                     LICENSE and WARRANTY file.                            *
 *   email           : info at matsim dot org                                *
 *                                                                           *
 *   *********************************************************************** *
 *                                                                           *
 *     This program is free software; you can redistribute it and/or modify  *
 *     it under the terms of the GNU General Public License as published by  *
 *     the Free Software Foundation; either version 2 of the License, or     *
 *     (at your option) any later version.                                   *
 *     See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                           *
 *   ***********************************************************************
 *
 */

package org.matsim.contrib.freight.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.FreightConstants;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.vehicles.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/*package-private*/  final class FreightTourStartEventCreator implements FreightEventCreator{

	TreeMap<Id<Person>, ActivityEndEvent> endEventMap = new TreeMap<>();
	TreeMap<Id<Person>, PersonEntersVehicleEvent> personEntersVehicleEventMap = new TreeMap<>();


	@Override
	public Event createEvent(Event event, Carrier carrier, Activity activity, ScheduledTour scheduledTour, int activityCounter, Id<Vehicle> vehicleId) {
		//temporarily store some information, because ActivityEndEvent does not have any information about the person's vehicle.
		if((event instanceof ActivityEndEvent endEvent) && FreightConstants.START.equals(endEvent.getActType()) ) {
			final Id<Person> personId = endEvent.getPersonId();
			endEventMap.put(personId, endEvent);

			//it is unclear in which order the events arrive in the events stream, when they have the same time step ->Check if TourStartsEvent should be thrown now.
			if (personEntersVehicleEventMap.containsKey(personId) && endEventMap.containsKey(personId)) {
				return createFreightTourStartsEvent(personId, carrier, scheduledTour);
			}
		}

		if (event instanceof PersonEntersVehicleEvent personEntersVehicleEvent) { //now we have the persons vehicle
			final Id<Person> personId = personEntersVehicleEvent.getPersonId();
			personEntersVehicleEventMap.put(personId, personEntersVehicleEvent);

			//it is unclear in which order the events arrive in the events stream, when they have the same time step ->Check if TourStartsEvent should be thrown now.
			if (personEntersVehicleEventMap.containsKey(personId) && endEventMap.containsKey(personId)) {
				return createFreightTourStartsEvent(personId, carrier, scheduledTour);
			}
		}
		return null;
	}


	/**
	 * Creating the FreightTourStartsEvent
	 *
	 * @param personId
	 * @param carrier
	 * @param scheduledTour
	 * @return FreightTourStartEvent
	 */
	private FreightTourStartEvent createFreightTourStartsEvent(Id<Person> personId, Carrier carrier, ScheduledTour scheduledTour) {
		assert endEventMap.containsKey(personId);
		final var endEvent = endEventMap.get(personId);

		assert personEntersVehicleEventMap.containsKey(personId);
		final var entersVehicleEvent = personEntersVehicleEventMap.get(personId);

		//See if the events are corresponding by check if they are thrown at the same time
		if (endEvent.getTime() == entersVehicleEvent.getTime()) {
			//remove them from the maps, so we have a clean state for the next iteration
			endEventMap.remove(personId);
			personEntersVehicleEventMap.remove(personId);
			// TODO: If we have the tourId, we do not need to store the link here, kmt sep 22
			return new FreightTourStartEvent(endEvent.getTime(), carrier.getId(), scheduledTour.getTour().getStartLinkId(), entersVehicleEvent.getVehicleId(), scheduledTour.getTour().getId());
		}
		return null;
	}

}
