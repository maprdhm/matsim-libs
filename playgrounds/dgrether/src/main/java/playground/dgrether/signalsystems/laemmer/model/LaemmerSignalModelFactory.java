/* *********************************************************************** *
 * project: org.matsim.*
 * DgTaSignalModelFactory
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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
package playground.dgrether.signalsystems.laemmer.model;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.signals.builder.DefaultSignalModelFactory;
import org.matsim.contrib.signals.builder.SignalModelFactory;
import org.matsim.contrib.signals.data.SignalsData;
import org.matsim.contrib.signals.data.signalgroups.v20.SignalPlanData;
import org.matsim.contrib.signals.model.SignalController;
import org.matsim.contrib.signals.model.SignalPlan;
import org.matsim.contrib.signals.model.SignalSystem;

import com.google.inject.Inject;

import playground.dgrether.signalsystems.LinkSensorManager;
import playground.dgrether.signalsystems.laemmer.model.LaemmerSignalController.Builder;


/**
 * @author dgrether
 *
 */
public final class LaemmerSignalModelFactory implements SignalModelFactory {

	private static final Logger log = Logger.getLogger(LaemmerSignalModelFactory.class);
	
	private DefaultSignalModelFactory delegate = new DefaultSignalModelFactory();

	private Builder builder;
	
	@Inject
	public LaemmerSignalModelFactory(LinkSensorManager sensorManager, Scenario scenario) {
		SignalsData signalsData = (SignalsData) scenario.getScenarioElement(SignalsData.ELEMENT_NAME);
		Network network = scenario.getNetwork();
		this.builder = new LaemmerSignalController.Builder(sensorManager, signalsData, network);
	}

	@Override
	public SignalSystem createSignalSystem(Id<SignalSystem> id) {
		return this.delegate.createSignalSystem(id);
	}

	@Override
	public SignalController createSignalSystemController(String controllerIdentifier, SignalSystem signalSystem) {
		if (LaemmerSignalController.IDENTIFIER.equals(controllerIdentifier)){
			log.info("Creating " + LaemmerSignalController.IDENTIFIER);
			return builder.build(signalSystem) ;
		}
		return this.delegate.createSignalSystemController(controllerIdentifier, signalSystem);
	}

	@Override
	public SignalPlan createSignalPlan(SignalPlanData planData) {
		return this.delegate.createSignalPlan(planData);
	}

}
