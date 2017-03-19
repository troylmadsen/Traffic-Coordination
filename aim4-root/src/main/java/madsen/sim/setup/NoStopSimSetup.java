/*
 * Troy Madsen
 */
package madsen.sim.setup;

import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.driver.pilot.V2IPilot;
import aim4.im.v2i.reservation.ReservationGridManager;
import aim4.map.GridMap;
import aim4.map.GridMapUtil;
import aim4.sim.AutoDriverOnlySimulator;
import aim4.sim.Simulator;
import aim4.sim.setup.BasicSimSetup;
import aim4.sim.setup.SimSetup;

/**
 * The setup for the simulator in which there is no intersection control and
 * vehicles move freely.
 * 
 * @author Troy Madsen
 */
public class NoStopSimSetup extends BasicSimSetup implements SimSetup{

	/** The name of the file containing the traffic volume data */
	private String trafficVolumeFileName = null;

	////////////////////////////
	// CONSTRUCTORS
	////////////////////////////

	/**
	 * Create a setup for th esimulator in which there is no intersection
	 * control.
	 * 
	 * @param basicSimSetup  the basic simulator setup
	 */
	public NoStopSimSetup(BasicSimSetup basicSimSetup) {
		super(basicSimSetup);
	}

	/**
	 * Create a setup for th esimulator in which there is no intersection
	 * control.
	 * 
	 * @param columns                     the number of columns
	 * @param rows                        the number of rows
	 * @param laneWidth                   the width of lanes
	 * @param speedLimit                  the speed limit
	 * @param lanesPerRoad                the number of lanes per road
	 * @param medianSize                  the median size
	 * @param distanceBetween             the distance between intersections
	 * @param trafficLevel                the traffic level
	 * @param stopDistBeforeIntersection  the stopping distance before
	 */
	public NoStopSimSetup(int columns, int rows,
			double laneWidth, double speedLimit,
			int lanesPerRoad,
			double medianSize, double distanceBetween,
			double trafficLevel,
			double stopDistBeforeIntersection){
		super(columns, rows, laneWidth, speedLimit, lanesPerRoad,
				medianSize, distanceBetween, trafficLevel,
				stopDistBeforeIntersection);
	}

	////////////////////////////
	// PUBLIC METHODS
	////////////////////////////

	/**
	 * Set the name of the file containing the traffic volume data.
	 *
	 * @param trafficVolumeFileName  the name of the file containing the traffic
	 *                               volume data
	 */
	public void setTrafficVolume(String trafficVolumeFileName) {
		this.trafficVolumeFileName = trafficVolumeFileName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Simulator getSimulator() {
		double currentTime = 0.0;
		GridMap layout = new GridMap(currentTime,
				numOfColumns,
				numOfRows,
				laneWidth,
				speedLimit,
				lanesPerRoad,
				medianSize,
				distanceBetween);

//		/* Comment this */
//		//TODO: Needs some investigation still
//		ReservationGridManager.Config gridConfig =
//				new ReservationGridManager.Config(SimConfig.TIME_STEP,
//						SimConfig.GRID_TIME_STEP,
//						0.0,
//						0.0,
//						0.0,
//						true,
//						1.0);
//		/* End */

		SimConfig.MUST_STOP_BEFORE_INTERSECTION = false;
		Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = false;

//		/* Comment this */
//		GridMapUtil.setNoStopManagers(layout, currentTime,
//				gridConfig);
//		/* End */

		if (trafficVolumeFileName == null) {
			if (numOfColumns == 1 && numOfRows == 1) {
				GridMapUtil.setUniformTurnBasedSpawnPoints(layout, trafficLevel);
			} else {
				GridMapUtil.setUniformRandomSpawnPoints(layout, trafficLevel);
			}
		} else {
			GridMapUtil.setUniformRatioSpawnPoints(layout, trafficVolumeFileName);
		}

		V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION =
				stopDistBeforeIntersection;

		return new AutoDriverOnlySimulator(layout);
	}
}
