/* *********************************************************************** *
 * project: org.matsim.*
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

package playground.mmoyo.analysis.stopZoneOccupancyAnalysis;

import org.matsim.contrib.cadyts.pt.CadytsPtConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.IterationEndsListener;

/**
 * Controler listener for stop zone analysis
 */
public class CtrlListener4configurableOcuppAnalysis implements IterationEndsListener, BeforeMobsimListener {
	ConfigurableOccupancyAnalyzer configurableOccupAnalyzer;
	KMZPtCountSimComparisonWriter kmzPtCountSimComparisonWritter;
	boolean stopZoneConversion;
	
	public CtrlListener4configurableOcuppAnalysis(final Controler controler){
		
		//create occupancy analyzer based on CadytsPtConfigGroup();		
		if (!(controler.getConfig().getModule(CadytsPtConfigGroup.GROUP_NAME) instanceof org.matsim.contrib.cadyts.pt.CadytsPtConfigGroup)){
			CadytsPtConfigGroup ccc = new CadytsPtConfigGroup() ;
			controler.getConfig().addModule(CadytsPtConfigGroup.GROUP_NAME, ccc) ;
		}
		CadytsPtConfigGroup cptcg = (CadytsPtConfigGroup) controler.getConfig().getModule(CadytsPtConfigGroup.GROUP_NAME);
		configurableOccupAnalyzer = new ConfigurableOccupancyAnalyzer( cptcg.getCalibratedLines() ,  cptcg.getTimeBinSize());
		controler.getEvents().addHandler(configurableOccupAnalyzer);
		
		kmzPtCountSimComparisonWritter = new KMZPtCountSimComparisonWriter(controler);
	}
	
	@Override
	public void notifyBeforeMobsim(final BeforeMobsimEvent event) {
		configurableOccupAnalyzer.reset(event.getIteration());
		configurableOccupAnalyzer.setStopZoneConversion(stopZoneConversion);
	}

	@Override
	public void notifyIterationEnds(final IterationEndsEvent event) {
		int it = event.getIteration();
		Controler controler = event.getControler();
		if (isActiveInThisIteration(it,  controler)) {
			kmzPtCountSimComparisonWritter.write( configurableOccupAnalyzer.getOccuAnalyzer(), it, stopZoneConversion);
		}
	}
	
	private boolean isActiveInThisIteration(final int iter, final Controler controler) {
		return (iter % controler.getConfig().ptCounts().getPtCountsInterval() == 0);
	}
	
	public void setStopZoneConversion(boolean stopZoneConversion){
		this.stopZoneConversion = stopZoneConversion;
	}
}
