/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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
package org.matsim.signalsystems.data.ambertimes.v10;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;

/**
 * @author jbischoff
 * @author dgrether
 */
public class AmberTimesDataImpl implements AmberTimesData {

	private Map<Id, AmberTimeData> amberTimeData = new HashMap<Id, AmberTimeData>();
	private Double defaultAmberTimeGreen;
	private Integer globalDefaultAmberTime;
	private Integer globalDefaultRedAmberTime;

	
	
	@Override
	public void addAmberTimeData(AmberTimeData amberTimeData) {
		this.amberTimeData.put(amberTimeData.getSignalSystemId(), amberTimeData);
	}

	@Override
	public Map<Id, AmberTimeData> getAmberTimeDataBySystemId() {
		return amberTimeData;
	}

	@Override
	public Integer getDefaultAmber() {
		return this.globalDefaultAmberTime;
	}

	@Override
	public Double getDefaultAmberTimeGreen() {
		return this.defaultAmberTimeGreen;
	}

	@Override
	public Integer getDefaultRedAmber() {
		return this.globalDefaultRedAmberTime;
	}

	@Override
	public void setDefaultAmber(Integer seconds) {
		this.globalDefaultAmberTime=seconds;
	}

	@Override
	public void setDefaultAmberTimeGreen(Double proportion) {
		this.defaultAmberTimeGreen=proportion;
	}

	@Override
	public void setDefaultRedAmber(Integer seconds) {
		this.globalDefaultRedAmberTime=seconds;
	}

}
