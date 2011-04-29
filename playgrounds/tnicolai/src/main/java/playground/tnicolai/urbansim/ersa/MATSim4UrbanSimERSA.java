/* *********************************************************************** *
 * project: org.matsim.*
 * MATSim4UrbanSimERSA.java
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

/**
 * 
 */
package playground.tnicolai.urbansim.ersa;

import gnu.trove.TObjectDoubleHashMap;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.geotools.feature.Feature;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.sna.gis.Zone;
import org.matsim.contrib.sna.gis.ZoneLayer;
import org.matsim.core.controler.Controler;
import org.matsim.core.facilities.ActivityFacilitiesImpl;

import playground.johannes.socialnetworks.gis.SpatialGrid;
import playground.johannes.socialnetworks.gis.SpatialGridTableWriter;
import playground.johannes.socialnetworks.gis.io.FeatureKMLWriter;
import playground.johannes.socialnetworks.gis.io.FeatureSHP;
import playground.tnicolai.urbansim.MATSim4Urbansim;
import playground.tnicolai.urbansim.constants.Constants;
import playground.tnicolai.urbansim.gis.MyColorizer;
import playground.tnicolai.urbansim.utils.ProgressBar;
import playground.tnicolai.urbansim.utils.helperObjects.JobsObject;
import playground.tnicolai.urbansim.utils.helperObjects.WorkplaceObject;
import playground.tnicolai.urbansim.utils.helperObjects.ZoneObject;
import playground.tnicolai.urbansim.utils.io.ReadFromUrbansimParcelModel;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author thomas
 *
 */
public class MATSim4UrbanSimERSA extends MATSim4Urbansim{
	
	// Logger
	private static final Logger logger = Logger.getLogger(MATSim4UrbanSimERSA.class);
	
	private String shapeFile = null;
	private int gridSize = -1;
	private double jobSample = 1.;
	
	public MATSim4UrbanSimERSA(String args[]){
		super(args);
		
		// set the resolution, this is used for setting 
		// the starting points for accessibility measures
		checkAndSetShapeFile(args);
		checkAndSetGridSize(args);
		checkAnsSetJobSample(args);
	}

	/**
	 * Set the shape file path in order to determine 
	 * the starting points for accessibility computation
	 * 
	 * @param args
	 */
	private void checkAndSetShapeFile(String[] args) {

		if( args.length >= 2 ){
			shapeFile = args[1].trim();
			logger.info("The shape file path was set to " + shapeFile);
		}
		else{
			shapeFile = "/Users/thomas/Development/opus_home/data/psrc_parcel/shapefiles/boundary.shp";
			logger.warn("No path for the shape file was given. The path is set to " + shapeFile + " (default setting)!");
		}
		
		if(!pathExsits(shapeFile))
			throw new RuntimeException("Given path to shape file does not exist: " + shapeFile);
	}
	
	/**
	 * Set the grid size for the starting points
	 * 
	 * @param args
	 */
	private void checkAndSetGridSize(String[] args) {
		try{
			if(args.length >= 3){
				gridSize = Integer.parseInt( args[2].trim() );
				logger.info("The grid size was set to " + gridSize);
			} else{
				gridSize = 10000;
				logger.warn("No parameter for the grid size was given. The grid size is set to " + gridSize + " (default setting)!");
			}
		} catch (NumberFormatException nfe){
			nfe.printStackTrace();
			logger.error( "Please set a correct grid size. " + args[2] + " is not a valid value (integer).");
		}
	}
	
	/**
	 * Set the jobSample for the starting points
	 * 
	 * @param args
	 */
	private void checkAnsSetJobSample(String[] args) {
		try{
			if(args.length >= 4){
				jobSample = Double.parseDouble( args[3].trim() );
				logger.info("The jobSample was set to " + String.valueOf(jobSample) );
			} else{
				jobSample = 1.;
				logger.warn("No parameter for the job sample was given. The job sample is set to " + String.valueOf(jobSample) + " (default setting)!");
			}
		} catch(NumberFormatException nfe){
			nfe.printStackTrace();
			logger.error( "Please set a correct job sample . " + args[3] + " is not a valid value (double).");
		}
	}
	
	/**
	 * run simulation
	 * @param zones
	 * @throws IOException 
	 */
	@Override
	protected void runControler( ActivityFacilitiesImpl zones, Map<Id,WorkplaceObject> numberOfWorkplacesPerZone, ActivityFacilitiesImpl parcels, 
			ReadFromUrbansimParcelModel readFromUrbansim){
		
		Controler controler = new Controler(scenario);
		controler.setOverwriteFiles(true);	// sets, whether output files are overwritten
		controler.setCreateGraphs(false);	// sets, whether output Graphs are created
		
		ERSAControlerListener myListener = null;
		
		try {
			myListener = initAndAddControlerListener(parcels, readFromUrbansim, controler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int cID = benchmark.addMeasure("Running Contoler");
		controler.run();
		benchmark.stoppMeasurement(cID);
		
		logger.info("Finished computations ...");
		logger.info("Running contoler took " + benchmark.getDurationInSeconds(cID) + " seconds.");
		
		writeKMZFiles(myListener);
		writeSpatialGridTables(myListener);
		
		// dumping benchmark results
		benchmark.dumpResults(Constants.OPUS_MATSIM_TEMPORARY_DIRECTORY + "matsim4ersa_benchmark.txt");
	}

	/**
	 * @param myListener
	 */
	private void writeSpatialGridTables(ERSAControlerListener myListener) {
		logger.info("Writing spatial grid tables ...");
		SpatialGridTableWriter sgTableWriter = new SpatialGridTableWriter();
		try {
			int ttID = benchmark.addMeasure("Writing TravelTime SpatialGrid-Table" , Constants.OPUS_MATSIM_TEMPORARY_DIRECTORY + Constants.ERSA_TRAVEL_TIME_ACCESSIBILITY + gridSize + "x" + gridSize + Constants.FILE_TYPE_TXT, false);
			sgTableWriter.write(myListener.getTravelTimeAccessibilityGrid(), Constants.OPUS_MATSIM_TEMPORARY_DIRECTORY + Constants.ERSA_TRAVEL_TIME_ACCESSIBILITY + gridSize + "x" + gridSize + Constants.FILE_TYPE_TXT);
			benchmark.stoppMeasurement(ttID);
			logger.info("Writing TravelTime SpatialGrid-Table took " + benchmark.getDurationInSeconds(ttID) + " seconds.");
			
			int tcID = benchmark.addMeasure("Writing TravelCostSpatialGrid-Table", Constants.OPUS_MATSIM_TEMPORARY_DIRECTORY + Constants.ERSA_TRAVEL_COST_ACCESSIBILITY + gridSize + "x" + gridSize + Constants.FILE_TYPE_TXT, false);
			sgTableWriter.write(myListener.getTravelCostAccessibilityGrid(), Constants.OPUS_MATSIM_TEMPORARY_DIRECTORY + Constants.ERSA_TRAVEL_COST_ACCESSIBILITY + gridSize + "x" + gridSize + Constants.FILE_TYPE_TXT);
			benchmark.stoppMeasurement(tcID);
			logger.info("Writing TravelCost SpatialGrid-Table took " + benchmark.getDurationInSeconds(tcID) + " seconds.");
			
			int tdID = benchmark.addMeasure("Writing TravelDistanceSpatialGrid-Table", Constants.OPUS_MATSIM_TEMPORARY_DIRECTORY + Constants.ERSA_TRAVEL_DISTANCE_ACCESSIBILITY + gridSize + "x" + gridSize + Constants.FILE_TYPE_TXT, false);
			sgTableWriter.write(myListener.getTravelDistanceAccessibilityGrid(), Constants.OPUS_MATSIM_TEMPORARY_DIRECTORY + Constants.ERSA_TRAVEL_DISTANCE_ACCESSIBILITY + gridSize + "x" + gridSize + Constants.FILE_TYPE_TXT);
			benchmark.stoppMeasurement(tdID);
			logger.info("Writing TravelDistance SpatialGrid-Table took " + benchmark.getDurationInSeconds(tdID) + " seconds.");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("Done with writing spatial grid tables ...");
	}

	/**
	 * @param myListener
	 */
	private void writeKMZFiles(ERSAControlerListener myListener) {
		logger.info("Writing Google Erath files ...");
		
		ZoneLayer<ZoneObject> startZones = myListener.getStartZones();
		
		FeatureKMLWriter writer = new FeatureKMLWriter();
		Set<Geometry> geometries = new HashSet<Geometry>();
		
		TObjectDoubleHashMap<Geometry> travelTimeValues = new TObjectDoubleHashMap<Geometry>();
		TObjectDoubleHashMap<Geometry> travelCostValues = new TObjectDoubleHashMap<Geometry>();
		TObjectDoubleHashMap<Geometry> travelDistanceValues = new TObjectDoubleHashMap<Geometry>();
		
		for(Zone<ZoneObject> zone : startZones.getZones()) {
			geometries.add(zone.getGeometry());
			travelTimeValues.put(zone.getGeometry(), zone.getAttribute().getTravelTimeAccessibility());
			travelCostValues.put(zone.getGeometry(), zone.getAttribute().getTravelCostAccessibility());
			travelDistanceValues.put(zone.getGeometry(), zone.getAttribute().getTravelDistanceAccessibility());
		}
		
		// writing travel time accessibility kmz file
		int ttID = benchmark.addMeasure("Writing TravelTime KMZ-file", Constants.OPUS_MATSIM_TEMPORARY_DIRECTORY + Constants.ERSA_TRAVEL_TIME_ACCESSIBILITY + gridSize + "x" + gridSize + Constants.FILE_TYPE_KMZ, false);
		writer.setColorizable(new MyColorizer(travelTimeValues));
		writer.write(geometries, Constants.OPUS_MATSIM_TEMPORARY_DIRECTORY + Constants.ERSA_TRAVEL_TIME_ACCESSIBILITY + gridSize + "x" + gridSize + Constants.FILE_TYPE_KMZ);
		benchmark.stoppMeasurement(ttID);
		logger.info("Writing TravelTime KMZ-file took " + benchmark.getDurationInSeconds(ttID) + " seconds.");
		
		// writing travel cost accessibility kmz file
		int tcID = benchmark.addMeasure("Writing TravelCost KMZ-file", Constants.OPUS_MATSIM_TEMPORARY_DIRECTORY + Constants.ERSA_TRAVEL_COST_ACCESSIBILITY + gridSize + "x" + gridSize + Constants.FILE_TYPE_KMZ, false);
		writer.setColorizable(new MyColorizer(travelCostValues));
		writer.write(geometries, Constants.OPUS_MATSIM_TEMPORARY_DIRECTORY + Constants.ERSA_TRAVEL_COST_ACCESSIBILITY + gridSize + "x" + gridSize + Constants.FILE_TYPE_KMZ);
		benchmark.stoppMeasurement(tcID);
		logger.info("Writing TravelCost KMZ-file took " + benchmark.getDurationInSeconds(tcID) + " seconds.");
		
		// writing travel distance accessibility kmz file
		int tdID = benchmark.addMeasure("Writing TravelDistance KMZ-file", Constants.OPUS_MATSIM_TEMPORARY_DIRECTORY + Constants.ERSA_TRAVEL_DISTANCE_ACCESSIBILITY + gridSize + "x" + gridSize + Constants.FILE_TYPE_KMZ, false);
		writer.setColorizable(new MyColorizer(travelDistanceValues));
		writer.write(geometries, Constants.OPUS_MATSIM_TEMPORARY_DIRECTORY + Constants.ERSA_TRAVEL_DISTANCE_ACCESSIBILITY + gridSize + "x" + gridSize + Constants.FILE_TYPE_KMZ);
		benchmark.stoppMeasurement(tdID);
		logger.info("Writing TravelDistance KMZ-file took " + benchmark.getDurationInSeconds(tdID) + " seconds.");
		
		logger.info("Done with writing Google Erath files ...");
	}

	/**
	 * @param parcels
	 * @param readFromUrbansim
	 * @param controler
	 * @throws IOException 
	 */
	private ERSAControlerListener initAndAddControlerListener(ActivityFacilitiesImpl parcels,
			ReadFromUrbansimParcelModel readFromUrbansim, Controler controler) throws IOException {
		
		Geometry boundary = getBoundary(shapeFile);
		
		// set starting points to measure accessibility (KMZ and CSV output)
		int szID = benchmark.addMeasure("Creating Grid (startingZomes)");
		ZoneLayer<ZoneObject> startZones = getStratZones(boundary);
		benchmark.stoppMeasurement(szID);
		logger.info("Creating Grid took " + benchmark.getDurationInSeconds(szID) + "seconds.");
		
		SpatialGrid<Double> travelTimeAccessibilityGrid = createSpatialGrid(boundary);
		SpatialGrid<Double> travelCostAccessibilityGrid = createSpatialGrid(boundary);
		SpatialGrid<Double> travelDistanceAccessibilityGrid = createSpatialGrid(boundary);
		
		// gather all workplaces
		int jmID = benchmark.addMeasure("Creating Destinations (jobObjectMap)");
		Map<Id, JobsObject> jobObjectMap = getJobMap(parcels, readFromUrbansim);
		benchmark.stoppMeasurement(jmID);
		logger.info("Creating job destinations (jobObjectMap) took " + benchmark.getDurationInSeconds(jmID) + "seconds.");
		
		ERSAControlerListener myListener = new ERSAControlerListener(startZones, jobObjectMap, 
				travelTimeAccessibilityGrid, travelCostAccessibilityGrid, travelDistanceAccessibilityGrid, benchmark);
		
		// The following lines register what should be done _after_ the iterations were run:
		controler.addControlerListener( myListener );
		
		return myListener;
	}

	/**
	 * @param boundary
	 */
	private SpatialGrid<Double> createSpatialGrid(Geometry boundary) {
		Envelope env = boundary.getEnvelopeInternal();
		double xMin = env.getMinX();
		double xMax = env.getMaxX();
		double yMin = env.getMinY();
		double yMax = env.getMaxY();
		
		return new SpatialGrid<Double>(xMin, yMin, xMax, yMax, gridSize);
	}

	/**
	 * @param parcels
	 * @param readFromUrbansim
	 * @return
	 */
	private Map<Id, JobsObject> getJobMap(ActivityFacilitiesImpl parcels,
			ReadFromUrbansimParcelModel readFromUrbansim) {

		Map<Id, JobsObject> jobObjectMap = readFromUrbansim.readDisaggregatedJobs(parcels, jobSample);
		return jobObjectMap;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private ZoneLayer<ZoneObject> getStratZones(Geometry boundary) throws IOException {
		
		ZoneLayer<ZoneObject> startZones = createGridLayerByGridSize(gridSize, boundary);
		
		return startZones;
	}

	/**
	 * @param psrcSHPFile
	 * @return
	 * @throws IOException
	 */
	private Geometry getBoundary(String psrcSHPFile) throws IOException {
		// get boundaries of study area
		Set<Feature> featureSet = FeatureSHP.readFeatures(psrcSHPFile);
		logger.info("Extracting boundary of the shape file ...");
		Geometry boundary = featureSet.iterator().next().getDefaultGeometry();
		boundary.setSRID( Constants.SRID_WASHINGTON_NORTH ); // tnicolai: check if this is the correct id
		logger.info("Done extracting boundary ...");
		
		return boundary;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param gridSize
	 * @param boundary
	 * @return
	 */
	private static ZoneLayer<ZoneObject> createGridLayerByGridSize(double gridSize, Geometry boundary) {
		
		logger.info("Setting statring points for accessibility measure ...");

		int skippedPoints = 0;
		int setPoints = 0;
		
		GeometryFactory factory = new GeometryFactory();
		Set<Zone<ZoneObject>> zones = new HashSet<Zone<ZoneObject>>();
		Envelope env = boundary.getEnvelopeInternal();
		
		ProgressBar bar = new ProgressBar( (env.getMaxX()-env.getMinX())/gridSize );
		
		// goes step by step from the min x and y coordinate to max x and y coordinate
		for(double x = env.getMinX(); x < env.getMaxX(); x += gridSize) {
			
			bar.update();
						
			for(double y = env.getMinY(); y < env.getMaxY(); y += gridSize) {
				Point point = factory.createPoint(new Coordinate(x, y));
				if(boundary.contains(point)) {
					
					Coordinate[] coords = new Coordinate[5];
					coords[0] = point.getCoordinate();
					coords[1] = new Coordinate(x, y + gridSize);
					coords[2] = new Coordinate(x + gridSize, y + gridSize);
					coords[3] = new Coordinate(x + gridSize, y);
					coords[4] = point.getCoordinate();
					// Linear Ring defines an artificial zone
					LinearRing linearRing = factory.createLinearRing(coords);
					Polygon polygon = factory.createPolygon(linearRing, null);
					polygon.setSRID( Constants.SRID_WASHINGTON_NORTH ); // tnicolai: this should be the correct id for the Washington area
					
					Zone<ZoneObject> zone = new Zone<ZoneObject>(polygon);
					zone.setAttribute( new ZoneObject( setPoints ) );
					zones.add(zone);
					
					setPoints++;
				}
				else skippedPoints++;
			}
		}

		logger.info(setPoints + " starting points were set and " + skippedPoints + " points have been skipped (points were skipped when they lay outside the shape file boundary).");
		logger.info("Done with setting starting points!");
		
		ZoneLayer<ZoneObject> layer = new ZoneLayer<ZoneObject>(zones);
		return layer;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param gridSize
	 * @param boundary
	 * @return
	 */
	private static <Integer> ZoneLayer<Integer> createGridLayerByFixedNumberOfStartingPoints(double numberfStartingPoints, Geometry boundary) {
		
		logger.info("Setting statring points for accessibility measure ...");
		
		int skippedPoints = 0;
		int setPoints = 0;
		
//		GeometryFactory factory = new GeometryFactory();
		Set<Zone<Integer>> zones = new HashSet<Zone<Integer>>();
//		Envelope env = boundary.getEnvelopeInternal();
//		for(double x = env.getMinX(); x < env.getMaxX(); x += resolution) {
//			for(double y = env.getMinY(); y < env.getMaxY(); y += resolution) {
//				Point point = factory.createPoint(new Coordinate(x, y));
//				if(boundary.contains(point)) {
//					Coordinate[] coords = new Coordinate[5];
//					coords[0] = point.getCoordinate();
//					coords[1] = new Coordinate(x, y + resolution);
//					coords[2] = new Coordinate(x + resolution, y + resolution);
//					coords[3] = new Coordinate(x + resolution, y);
//					coords[4] = point.getCoordinate();
//					
//					LinearRing linearRing = factory.createLinearRing(coords);
//					Polygon polygon = factory.createPolygon(linearRing, null);
//					polygon.setSRID(21781);
//					Zone<T> zone = new Zone<T>(polygon);
//					zones.add(zone);
//					
//					setPoints++;
//				}
//				else skippedPoints++;
//			}
//		}
		
		logger.info(setPoints + " were set and " + skippedPoints + " have been skipped.");
		logger.info("Done with setting starting points!");
		
		ZoneLayer<Integer> layer = new ZoneLayer<Integer>(zones);
		return layer;
	}
	
	/**
	 * This is the program entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		long startTime;
		long endTime;
		long time;
		
		startTime = System.currentTimeMillis();
		
		MATSim4UrbanSimERSA m4uERSA = new MATSim4UrbanSimERSA(args);
		m4uERSA.runMATSim();
		
		endTime = System.currentTimeMillis();
		time = (endTime - startTime) / 60000;
		
		logger.info("Computation took " + time + "minutes. Computation done!");
	}

}

