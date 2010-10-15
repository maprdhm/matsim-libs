/* *********************************************************************** *
 * project: org.matsim.*
 * Sim2DEngine.java
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
package playground.gregor.sim2_v2.simulation;

import java.util.List;

import playground.gregor.sim2_v2.simulation.floor.Floor;

/**
 * @author laemmel
 * 
 */
public class Sim2DEngine {

	private List<Floor> floors;
	private PhantomManager phantomMgr;

	/**
	 * @param time
	 */
	public void move(double time) {

		if (this.phantomMgr != null) {
			this.phantomMgr.update(time);
		}

		for (Floor floor : this.floors) {
			floor.move(time);
		}

	}

}
