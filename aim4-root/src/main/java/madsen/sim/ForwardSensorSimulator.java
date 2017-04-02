package madsen.sim;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import madsen.driver.ForwardSensorAutoDriver;
import aim4.config.Debug;
import aim4.config.DebugPoint;
import aim4.driver.DriverSimView;
import aim4.map.BasicMap;
import aim4.map.SpawnPoint;
import aim4.map.SpawnPoint.SpawnSpec;
import aim4.map.lane.Lane;
import aim4.sim.AutoDriverOnlySimulator;
import aim4.vehicle.AutoVehicleSimView;
import aim4.vehicle.BasicAutoVehicle;
import aim4.vehicle.VehicleSimView;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VinRegistry;

/**
 * A simulator with only autonomous drivers and forward-facing sensors to track
 * vehicles to the sides.
 * 
 * @author Troy Madsen
 */
public class ForwardSensorSimulator extends AutoDriverOnlySimulator {

	/////////////////////////////////
	// NESTED CLASSES
	/////////////////////////////////

	/**
	 * The result of a simulation step.
	 */
	public static class ForwardSensorSimStepResult implements SimStepResult {

		/** The VIN of the completed vehicles in this time step */
		List<Integer> completedVINs;

		/**
		 * Create a result of a simulation step
		 *
		 * @param completedVINs  the VINs of completed vehicles.
		 */
		public ForwardSensorSimStepResult(List<Integer> completedVINs) {
			this.completedVINs = completedVINs;
		}

		/**
		 * Get the list of VINs of completed vehicles.
		 *
		 * @return the list of VINs of completed vehicles.
		 */
		public List<Integer> getCompletedVINs() {
			return completedVINs;
		}
	}

	/////////////////////////////////
	// PRIVATE FIELDS
	/////////////////////////////////

	/* Troy Madsen */
	/** The distance that the forward sensors scan */
	private static double sensorDistance = 400;


	/////////////////////////////////
	// CLASS CONSTRUCTORS
	/////////////////////////////////

	/**
	 * Create an instance of the simulator.
	 *
	 * @param basicMap             the map of the simulation
	 */
	public ForwardSensorSimulator(BasicMap basicMap) {
		super(basicMap);
	}

	/////////////////////////////////
	// PUBLIC METHODS
	/////////////////////////////////

	// the main loop

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized AutoDriverOnlySimStepResult step(double timeStep) {
		return super.step(timeStep);
	}


	/////////////////////////////////
	// PRIVATE METHODS
	/////////////////////////////////

	/////////////////////////////////
	// STEP 1
	/////////////////////////////////

	/**
	 * Spawn vehicles.
	 *
	 * @param timeStep  the time step
	 */
	@Override
	protected void spawnVehicles(double timeStep) {
		for(SpawnPoint spawnPoint : basicMap.getSpawnPoints()) {
			List<SpawnSpec> spawnSpecs = spawnPoint.act(timeStep);
			if (!spawnSpecs.isEmpty()) {
				if (canSpawnVehicle(spawnPoint)) {
					for(SpawnSpec spawnSpec : spawnSpecs) {
						VehicleSimView vehicle = makeVehicle(spawnPoint, spawnSpec);
						VinRegistry.registerVehicle(vehicle); // Get vehicle a VIN number
						vinToVehicles.put(vehicle.getVIN(), vehicle);
						break; // only handle the first spawn vehicle
						// TODO: need to fix this
					}
				} // else ignore the spawnSpecs and do nothing
			}
		}
	}

	/**
	 * Create a vehicle at a spawn point.
	 *
	 * @param spawnPoint  the spawn point
	 * @param spawnSpec   the spawn specification
	 * @return the vehicle
	 */
	@Override
	protected VehicleSimView makeVehicle(SpawnPoint spawnPoint,
			SpawnSpec spawnSpec) {
		VehicleSpec spec = spawnSpec.getVehicleSpec();
		Lane lane = spawnPoint.getLane();
		// Now just take the minimum of the max velocity of the vehicle, and
		// the speed limit in the lane
		double initVelocity = Math.min(spec.getMaxVelocity(), lane.getSpeedLimit());
		// Obtain a Vehicle
		AutoVehicleSimView vehicle =
				new BasicAutoVehicle(spec,
						spawnPoint.getPosition(),
						spawnPoint.getHeading(),
						spawnPoint.getSteeringAngle(),
						initVelocity, // velocity
						initVelocity,  // target velocity
						spawnPoint.getAcceleration(),
						spawnSpec.getSpawnTime());
		// Set the driver
		ForwardSensorAutoDriver driver = new ForwardSensorAutoDriver(vehicle, basicMap);
		driver.setCurrentLane(lane);
		driver.setSpawnPoint(spawnPoint);

		/* Troy Madsen */
		driver.setDestination(basicMap.getRoad(lane));
		vehicle.setDriver(driver);
		vehicle.setTargetLaneForVehicleTracking(lane);
		vehicle.setVehicleTracking(true);

		return vehicle;
	}


	/////////////////////////////////
	// STEP 2
	/////////////////////////////////

	/**
	 * Provide tracking information to vehicles.
	 *
	 * @param vehicleLists  a mapping from lanes to lists of vehicles sorted by
	 *                      their distance on their lanes
	 */
	@Override
	protected void provideVehicleTrackingInfo(
			Map<Lane, SortedMap<Double, VehicleSimView>> vehicleLists) {
		// Vehicle Tracking
		for(VehicleSimView vehicle: vinToVehicles.values()) {
			// If the vehicle is autonomous
			if (vehicle instanceof AutoVehicleSimView) {
				AutoVehicleSimView autoVehicle = (AutoVehicleSimView)vehicle;

				if (autoVehicle.isVehicleTracking()) {
					DriverSimView driver = autoVehicle.getDriver();
					Lane targetLane = autoVehicle.getTargetLaneForVehicleTracking();
					Point2D pos = autoVehicle.getPosition();
					double dst = targetLane.distanceAlongLane(pos);

					// initialize the distances to infinity
					double frontDst = Double.MAX_VALUE;
					double rearDst = Double.MAX_VALUE;
					VehicleSimView frontVehicle = null ;
					VehicleSimView rearVehicle = null ;
					/* Troy Madsen */
					VehicleSimView onComingRight30Vehicle = null;
					VehicleSimView onComingRight45Vehicle = null;
					VehicleSimView onComingRight60Vehicle = null;
					VehicleSimView onComingLeft30Vehicle = null;
					VehicleSimView onComingLeft45Vehicle = null;
					VehicleSimView onComingLeft60Vehicle = null;

					// only consider the vehicles on the target lane
					SortedMap<Double,VehicleSimView> vehiclesOnTargetLane =
							vehicleLists.get(targetLane);

					/* Troy Madsen */
					// All vehicles in sim
					SortedMap<Double, VehicleSimView> vehiclesOnComingLane =
							(SortedMap<Double, VehicleSimView>) new TreeMap<Double, VehicleSimView>();
					for (Entry<Lane, SortedMap<Double, VehicleSimView>> e:
						vehicleLists.entrySet()) {
						for (Entry<Double, VehicleSimView> v: e.getValue().entrySet()) {
							if (v.getValue() != autoVehicle) {
								vehiclesOnComingLane.put(v.getKey(), v.getValue());
							}
						}
					}

					// compute the distances and the corresponding vehicles
					try {
						double d = vehiclesOnTargetLane.tailMap(dst).firstKey();
						frontVehicle = vehiclesOnTargetLane.get(d);
						frontDst = (d-dst)-frontVehicle.getSpec().getLength();
					} catch(NoSuchElementException e) {
						frontDst = Double.MAX_VALUE;
						frontVehicle = null;
					}
					try {
						double d = vehiclesOnTargetLane.headMap(dst).lastKey();
						rearVehicle = vehiclesOnTargetLane.get(d);
						rearDst = dst-d;
					} catch(NoSuchElementException e) {
						rearDst = Double.MAX_VALUE;
						rearVehicle = null;
					}
					/* Troy Madsen */
					double heading = autoVehicle.getHeading();
					double vehicleX = autoVehicle.getPointAtMiddleFront(0).getX();
					double vehicleY = autoVehicle.getPointAtMiddleFront(0).getY();
					// These must be cleared for new readings
					//          autoVehicle.getFrontRight30VehicleDistanceSensor().clear();
					//          autoVehicle.getFrontRight45VehicleDistanceSensor().clear();
					//          autoVehicle.getFrontRight60VehicleDistanceSensor().clear();
					//          autoVehicle.getFrontLeft30VehicleDistanceSensor().clear();
					//          autoVehicle.getFrontLeft45VehicleDistanceSensor().clear();
					//          autoVehicle.getFrontLeft60VehicleDistanceSensor().clear();
					// Sensor values
					double right30Value = Double.MAX_VALUE;
					double right45Value = Double.MAX_VALUE;
					double right60Value = Double.MAX_VALUE;
					double left30Value = Double.MAX_VALUE;
					double left45Value = Double.MAX_VALUE;
					double left60Value = Double.MAX_VALUE;
					// Sensor detection lines
					Line2D right30Sensor = new Line2D.Double(
							vehicleX,
							vehicleY,
							vehicleX + Math.cos(heading + Math.PI/6) * sensorDistance,
							vehicleY + Math.sin(heading + Math.PI/6) * sensorDistance);
					Line2D right45Sensor = new Line2D.Double(
							vehicleX,
							vehicleY,
							vehicleX + Math.cos(heading + Math.PI/4) * sensorDistance,
							vehicleY + Math.sin(heading + Math.PI/4) * sensorDistance);
					Line2D right60Sensor = new Line2D.Double(
							vehicleX,
							vehicleY,
							vehicleX + Math.cos(heading + Math.PI/3) * sensorDistance,
							vehicleY + Math.sin(heading + Math.PI/3) * sensorDistance);
					Line2D left30Sensor = new Line2D.Double(
							vehicleX,
							vehicleY,
							vehicleX + Math.cos(heading - Math.PI/6) * sensorDistance,
							vehicleY + Math.sin(heading - Math.PI/6) * sensorDistance);
					Line2D left45Sensor = new Line2D.Double(
							vehicleX,
							vehicleY,
							vehicleX + Math.cos(heading - Math.PI/4) * sensorDistance,
							vehicleY + Math.sin(heading - Math.PI/4) * sensorDistance);
					Line2D left60Sensor = new Line2D.Double(
							vehicleX,
							vehicleY,
							vehicleX + Math.cos(heading - Math.PI/3) * sensorDistance,
							vehicleY + Math.sin(heading - Math.PI/3) * sensorDistance);
					for (Entry<Double, VehicleSimView> e: vehiclesOnComingLane.entrySet()) {
						// Check for vehicle detection on right side
						try {
							if (right30Sensor.intersects(e.getValue().getShape().getBounds2D())) {
								double distance = Math.sqrt(Math.pow(vehicleX - e.getValue().getCenterPoint().getX(), 2) + Math.pow(vehicleY - e.getValue().getCenterPoint().getY(), 2));
								if (distance < right30Value) {
//									System.out.println("Right 30 " + autoVehicle.getVIN() + " " + e.getValue().getVIN());
									onComingRight30Vehicle = e.getValue();
									right30Value = distance;
//									System.out.println(distance);
								}
							} else if (right45Sensor.intersects(e.getValue().getShape().getBounds2D())) {
								double distance = Math.sqrt(Math.pow(vehicleX - e.getValue().getCenterPoint().getX(), 2) + Math.pow(vehicleY - e.getValue().getCenterPoint().getY(), 2));
								if (distance < right45Value) {
//									System.out.println("Right 45 " + autoVehicle.getVIN() + " " + e.getValue().getVIN());
									onComingRight45Vehicle = e.getValue();
									right45Value = distance;
//									System.out.println(distance);
								}
							} else if (right60Sensor.intersects(e.getValue().getShape().getBounds2D())) {
								double distance = Math.sqrt(Math.pow(vehicleX - e.getValue().getCenterPoint().getX(), 2) + Math.pow(vehicleY - e.getValue().getCenterPoint().getY(), 2));
								if (distance < right60Value) {
//									System.out.println("Right 60 " + autoVehicle.getVIN() + " " + e.getValue().getVIN());
									onComingRight60Vehicle = e.getValue();
									right60Value = distance;
//									System.out.println(distance);
								}
							}
						} catch (NoSuchElementException ex) {
//							System.out.println("Nothing right");
						}
						// Checks for vehicle detection on left side
						try {
							if (left30Sensor.intersects(e.getValue().getShape().getBounds2D())) {
								double distance = Math.sqrt(Math.pow(vehicleX - e.getValue().getCenterPoint().getX(), 2) + Math.pow(vehicleY - e.getValue().getCenterPoint().getY(), 2));
								if (distance < left30Value) {
//									System.out.println("Left 30 " + autoVehicle.getVIN() + " " + e.getValue().getVIN());
									onComingLeft30Vehicle = e.getValue();
									left30Value = distance;
//									System.out.println(distance);
								}
							} else if (left45Sensor.intersects(e.getValue().getShape().getBounds2D())) {
								double distance = Math.sqrt(Math.pow(vehicleX - e.getValue().getCenterPoint().getX(), 2) + Math.pow(vehicleY - e.getValue().getCenterPoint().getY(), 2));
								if (distance < left45Value) {
//									System.out.println("Left 45 " + autoVehicle.getVIN() + " " + e.getValue().getVIN());
									onComingLeft45Vehicle = e.getValue();
									left45Value = distance;
//									System.out.println(distance);
								}
							} else if (left60Sensor.intersects(e.getValue().getShape().getBounds2D())) {
								double distance = Math.sqrt(Math.pow(vehicleX - e.getValue().getCenterPoint().getX(), 2) + Math.pow(vehicleY - e.getValue().getCenterPoint().getY(), 2));
								if (distance < left60Value) {
//									System.out.println("Left 60 " + autoVehicle.getVIN() + " " + e.getValue().getVIN());
									onComingLeft60Vehicle = e.getValue();
									left60Value = distance;
//									System.out.println(distance);
								}
							}
						} catch (NoSuchElementException ex) {
//							System.out.println("Nothing left");
						}
					}

					// assign the sensor readings

					autoVehicle.getFrontVehicleDistanceSensor().record(frontDst);
					autoVehicle.getRearVehicleDistanceSensor().record(rearDst);
					/* Troy Madsen */
					if (right30Value <= sensorDistance) {
			        	  autoVehicle.getFrontRight30VehicleDistanceSensor().record(right30Value);
			          }
			          if (right45Value <= sensorDistance) {
			        	  autoVehicle.getFrontRight45VehicleDistanceSensor().record(right45Value);
			          }
			          if (right60Value <= sensorDistance) {
			        	  autoVehicle.getFrontRight60VehicleDistanceSensor().record(right60Value);
			          }
			          if (left30Value <= sensorDistance) {
			        	  autoVehicle.getFrontLeft30VehicleDistanceSensor().record(left30Value);
			          }
			          if (left45Value <= sensorDistance) {
			        	  autoVehicle.getFrontLeft45VehicleDistanceSensor().record(left45Value);
			          }
			          if (left60Value <= sensorDistance) {
			        	  autoVehicle.getFrontLeft60VehicleDistanceSensor().record(left60Value);
			          }

					/* Troy Madsen */
					// Announce detections
					//FIXME remove this
//					if (right30Value <= sensorDistance) {
//						System.out.println("Right 30 detection " + autoVehicle.getVIN() + " " + onComingRight30Vehicle.getVIN());
//						System.out.println(autoVehicle.getFrontRight30VehicleDistanceSensor().read());
//					}
//					if (right45Value <= sensorDistance) {
//						System.out.println("Right 45 detection " + autoVehicle.getVIN() + " " + onComingRight45Vehicle.getVIN());
//						System.out.println(autoVehicle.getFrontRight45VehicleDistanceSensor().read());
//					}
//					if (right60Value <= sensorDistance) {
//						System.out.println("Right 60 detection " + autoVehicle.getVIN() + " " + onComingRight60Vehicle.getVIN());
//						System.out.println(autoVehicle.getFrontRight60VehicleDistanceSensor().read());
//					}
//					if (left30Value <= sensorDistance) {
//						System.out.println("Left 30 detection " + autoVehicle.getVIN() + " " + onComingLeft30Vehicle.getVIN());
//						System.out.println(autoVehicle.getFrontLeft30VehicleDistanceSensor().read());
//					}
//					if (left45Value <= sensorDistance) {
//						System.out.println("Left 45 detection " + autoVehicle.getVIN() + " " + onComingLeft45Vehicle.getVIN());
//						System.out.println(autoVehicle.getFrontLeft45VehicleDistanceSensor().read());
//					}
//					if (left60Value <= sensorDistance) {
//						System.out.println("Left 60 detection " + autoVehicle.getVIN() + " " + onComingLeft60Vehicle.getVIN());
//						System.out.println(autoVehicle.getFrontLeft60VehicleDistanceSensor().read());
//					}

					// assign the vehicles' velocities

					if(frontVehicle!=null) {
						autoVehicle.getFrontVehicleSpeedSensor().record(
								frontVehicle.getVelocity());
					} else {
						autoVehicle.getFrontVehicleSpeedSensor().record(Double.MAX_VALUE);
					}
					if(rearVehicle!=null) {
						autoVehicle.getRearVehicleSpeedSensor().record(
								rearVehicle.getVelocity());
					} else {
						autoVehicle.getRearVehicleSpeedSensor().record(Double.MAX_VALUE);
					}

					// show the section on the viewer
					if (Debug.isTargetVIN(driver.getVehicle().getVIN())) {
						Point2D p1 = targetLane.getPointAtNormalizedDistance(
								Math.max((dst-rearDst)/targetLane.getLength(),0.0));
						Point2D p2 = targetLane.getPointAtNormalizedDistance(
								Math.min((frontDst+dst)/targetLane.getLength(),1.0));
						Debug.addLongTermDebugPoint(
								new DebugPoint(p2, p1, "cl", Color.RED.brighter()));
					}
				}
			}
		}

	}


}
