package madsen.sim;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import madsen.driver.ForwardSensorAutoDriver;

import aim4.config.Debug;
import aim4.config.DebugPoint;
import aim4.driver.AutoDriver;
import aim4.driver.DriverSimView;
import aim4.map.BasicMap;
import aim4.map.SpawnPoint;
import aim4.map.SpawnPoint.SpawnSpec;
import aim4.map.lane.Lane;
import aim4.noise.DoubleGauge;
import aim4.sim.AutoDriverOnlySimulator;
import aim4.vehicle.AutoVehicleDriverView;
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
	        	  if (v.getValue() != vehicle) {
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
          double heading = vehicle.getHeading();
          double vehicleX = vehicle.getPointAtMiddleFront(0).getX();
          double vehicleY = vehicle.getPointAtMiddleFront(0).getY();
          // These must be cleared for new readings
          ((AutoVehicleDriverView) vehicle).getFrontRight30VehicleDistanceSensor().record(Double.MAX_VALUE);
          ((AutoVehicleDriverView) vehicle).getFrontRight45VehicleDistanceSensor().record(Double.MAX_VALUE);
          ((AutoVehicleDriverView) vehicle).getFrontRight60VehicleDistanceSensor().record(Double.MAX_VALUE);
          ((AutoVehicleDriverView) vehicle).getFrontLeft30VehicleDistanceSensor().record(Double.MAX_VALUE);
          ((AutoVehicleDriverView) vehicle).getFrontLeft45VehicleDistanceSensor().record(Double.MAX_VALUE);
          ((AutoVehicleDriverView) vehicle).getFrontLeft60VehicleDistanceSensor().record(Double.MAX_VALUE);
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
        	  DoubleGauge g;
        	  // Check for vehicle detection on right side
        	  try {
		          if (right30Sensor.intersects(e.getValue().getShape().getBounds2D())) {
		        	  g = ((AutoVehicleDriverView) vehicle).getFrontRight30VehicleDistanceSensor();
		        	  double distance = Math.sqrt(Math.pow(vehicleX - e.getValue().getCenterPoint().getX(), 2) + Math.pow(vehicleY - e.getValue().getCenterPoint().getY(), 2));
		        	  if (distance < g.read()) {
		        		  g.record(distance);
		        		  //TODO Are these needed?
		        		  onComingRight30Vehicle = e.getValue();
		        	  }
		          } else if (right45Sensor.intersects(e.getValue().getShape().getBounds2D())) {
		        	  g = ((AutoVehicleDriverView) vehicle).getFrontRight45VehicleDistanceSensor();
		        	  double distance = Math.sqrt(Math.pow(vehicleX - e.getValue().getCenterPoint().getX(), 2) + Math.pow(vehicleY - e.getValue().getCenterPoint().getY(), 2));
		        	  if (distance < g.read()) {
		        		  g.record(distance);
		        		  onComingRight45Vehicle = e.getValue();
		        	  }
		          } else if (right60Sensor.intersects(e.getValue().getShape().getBounds2D())) {
		        	  g = ((AutoVehicleDriverView) vehicle).getFrontRight60VehicleDistanceSensor();
		        	  double distance = Math.sqrt(Math.pow(vehicleX - e.getValue().getCenterPoint().getX(), 2) + Math.pow(vehicleY - e.getValue().getCenterPoint().getY(), 2));
		        	  if (distance < g.read()) {
		        		  g.record(distance);
		        		  onComingRight60Vehicle = e.getValue();
		        	  }
		          }
        	  } catch (NoSuchElementException ex) {
        		  System.out.println("Nothing right");
        	  }
        	  // Checks for vehicle detection on left side
        	  try {
        		  if (left30Sensor.intersects(e.getValue().getShape().getBounds2D())) {
        			  g = ((AutoVehicleDriverView) vehicle).getFrontLeft30VehicleDistanceSensor();
		        	  double distance = Math.sqrt(Math.pow(vehicleX - e.getValue().getCenterPoint().getX(), 2) + Math.pow(vehicleY - e.getValue().getCenterPoint().getY(), 2));
		        	  if (distance < g.read()) {
		        		  g.record(distance);
		        		  onComingLeft30Vehicle = e.getValue();
		        	  }
	              } else if (left45Sensor.intersects(e.getValue().getShape().getBounds2D())) {
	            	  g = ((AutoVehicleDriverView) vehicle).getFrontLeft45VehicleDistanceSensor();
		        	  double distance = Math.sqrt(Math.pow(vehicleX - e.getValue().getCenterPoint().getX(), 2) + Math.pow(vehicleY - e.getValue().getCenterPoint().getY(), 2));
		        	  if (distance < g.read()) {
		        		  g.record(distance);
		        		  onComingLeft45Vehicle = e.getValue();
		        	  }
	              } else if (left60Sensor.intersects(e.getValue().getShape().getBounds2D())) {
	            	  g = ((AutoVehicleDriverView) vehicle).getFrontLeft60VehicleDistanceSensor();
		        	  double distance = Math.sqrt(Math.pow(vehicleX - e.getValue().getCenterPoint().getX(), 2) + Math.pow(vehicleY - e.getValue().getCenterPoint().getY(), 2));
		        	  if (distance < g.read()) {
		        		  g.record(distance);
		        		  onComingLeft60Vehicle = e.getValue();
		        	  }
	              }
        	  } catch (NoSuchElementException ex) {
        		  System.out.println("Nothing left");
        	  }
          }
          
          /* Troy Madsen */
          // Announce detections
          if (onComingRight30Vehicle != null) {
        	  System.out.println("Right 30 detection " + vehicle.getVIN() + " " + onComingRight30Vehicle.getVIN());
          }
          if (onComingRight45Vehicle != null) {
        	  System.out.println("Right 45 detection " + vehicle.getVIN() + " " + onComingRight45Vehicle.getVIN());
          }
          if (onComingRight60Vehicle != null) {
        	  System.out.println("Right 60 detection " + vehicle.getVIN() + " " + onComingRight60Vehicle.getVIN());
          }
          if (onComingLeft30Vehicle != null) {
        	  System.out.println("Left 30 detection " + vehicle.getVIN() + " " + onComingLeft30Vehicle.getVIN());
          }
          if (onComingLeft45Vehicle != null) {
        	  System.out.println("Left 45 detection " + vehicle.getVIN() + " " + onComingLeft45Vehicle.getVIN());
          }
          if (onComingLeft60Vehicle != null) {
        	  System.out.println("Left 60 detection " + vehicle.getVIN() + " " + onComingLeft60Vehicle.getVIN());
          }
          
          // assign the sensor readings

          autoVehicle.getFrontVehicleDistanceSensor().record(frontDst);
          autoVehicle.getRearVehicleDistanceSensor().record(rearDst);

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
