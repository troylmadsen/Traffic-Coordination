package madsen.driver;

import java.util.Random;

import aim4.driver.AutoDriver;
import aim4.map.BasicMap;
import aim4.vehicle.AutoVehicleDriverView;
import madsen.vehicle.SpeedControl;

/**
 * Functions identically to a {@link AutoDriver} with the added use of front
 * peripheral distance sensors to provide input.
 * 
 * @author Troy Madsen
 */
public class ForwardSensorAutoDriver extends AutoDriver {

	private static Random gaussian = new Random();

	//FIXME replace this with a proper time counter
	/**
	 * Number of iterations that should be waited before attempting another
	 * response to break an intersection deadlock
	 */
	private double responseWait = 1;

	//FIXME replace this with a proper time counter
	/**
	 * Number of iterations remaining before a non-critical response to sensor
	 * readings may be attempted again.
	 */
	private double responseCounter = 0;

	/**
	 * Mean of the speed adjustment curve of vehicles
	 */
	private double mean;
	
	/**
	 * Standard deviation of the speed adjustment curve of vehicles
	 */
	private double std;

	/**
	 * Minimum speed reduction of a vehicle
	 */
	private double minRed;

	/**
	 * Maximum speed reduction of a vehicle
	 */
	private double maxRed;

	/**
	 * Minimum speed increase of a vehicle
	 */
	private double minInc;
	
	/**
	 * Maximum speed increase of a vehicle
	 */
	private double maxInc;

	/**
	 * Minimum speed a vehicle may be reduced to
	 */
	private double speedMin;
	
	/**
	 * Maximum speed a vehicle may be increased to
	 */
	private double speedMax;
	
	//TODO Remove this
	/**
	 * Sets the speed adjustments to be made relative to the current speed of the vehicle
	 */
	private boolean speedRelative;
	
	/**
	 * Sets the shift amount for the speed adjustment curve
	 * of acceleration-tending operations
	 */
	 private double accelShift;
	 
	 /**
		 * Sets the shift amount for the speed adjustment curve
		 * of deceleration-tending operations
		 */
		 private double decelShift;

	/**
	 * Creates a driver controlling a vehicle with forward-facing peripheral sensors. Uses default speed adjustment behavior.
	 * 
	 * @param vehicle
	 *            The vehicle this driver will control
	 * @param basicMap
	 *            The map the vehicle exists in
	 */
	public ForwardSensorAutoDriver(AutoVehicleDriverView vehicle,
			BasicMap basicMap) {
		super(vehicle, basicMap);
		//TODO What was this previously?
	}
	
	/**
	 * Creates a driver controlling a vehicle with forward-facing peripheral sensors. Uses speed controls to create a gaussian curve for
	 * selecting a new speed to adjust to when attempting to avoid collisions.
	 * 
	 * @param vehicle The vehicle this driver will control
	 * @param basicMap The map the vehicle exists in
	 * @param speedControl Speed controls that the vehicle will use
	 */
	public ForwardSensorAutoDriver(AutoVehicleDriverView vehicle,
			BasicMap basicMap, SpeedControl speedControl) {
		super(vehicle, basicMap);
		
		// Assigning speed control values
		this.mean = speedControl.mean;
		this.std = speedControl.std;
		this.minRed = speedControl.minRed;
		this.maxRed = speedControl.maxRed;
		this.minInc = speedControl.minInc;
		this.maxInc = speedControl.maxInc;
		this.speedMin = speedControl.speedMin;
		this.speedMax = speedControl.speedMax;
		this.accelShift = speedControl.accelShift;
		this.decelShift = speedControl.decelShift;
	}

	/**
	 * Take control actions for driving the agent's Vehicle. This allows both
	 * the Coordinator and the Pilot to act (in that order).
	 */
	@Override
	public void act() {
		super.act();

		respondToSensors();
	}

	/**
	 * Uses all of the front peripheral sensor data to appropriately adjust it's
	 * speed to avoid collisions.
	 */
	private void respondToSensors() {
		// Distance sensor readings
		double front = getVehicle().getFrontVehicleDistanceSensor().read();
		double left30 = getVehicle().getFrontLeft30VehicleDistanceSensor()
				.read();
		double left45 = getVehicle().getFrontLeft45VehicleDistanceSensor()
				.read();
		double left60 = getVehicle().getFrontLeft60VehicleDistanceSensor()
				.read();
		double right30 = getVehicle().getFrontRight30VehicleDistanceSensor()
				.read();
		double right45 = getVehicle().getFrontRight45VehicleDistanceSensor()
				.read();
		double right60 = getVehicle().getFrontRight60VehicleDistanceSensor()
				.read();
		
		// Speed sensor readings
		double frontSpeed = getVehicle().getFrontVehicleSpeedSensor().read();
		
//		// Determine minimum speed
//		speedMin = Math.max(this.getCurrentLane().getSpeedLimit() - maxRed, 1.0);
		
//		// Determine maximum speed
//		speedMax = this.getCurrentLane().getSpeedLimit() + maxInc;

		// Get a speed adjustment within bounds
		double adjustment = gaussian.nextGaussian() * std + mean;
		if (adjustment > 0) {
			if (adjustment < minInc) {
				adjustment = minInc;
			} else if (adjustment > maxInc) {
				adjustment = maxInc;
			}
		} else {
			if (Math.abs(adjustment) < minRed) {
				adjustment = minRed * -1;
			} else if (Math.abs(adjustment) > maxRed) {
				adjustment = maxRed * -1;
			}
		}

		// Setting the new speed of the vehicle
		double velocity = this.getVehicle().gaugeVelocity() + adjustment;

		// Determining appropriate action to take
		if (front < 3) {
			velocity = Math.max(1, frontSpeed - 2);
		} else if (front < 10) {
			velocity = frontSpeed;
		} else if (left45 < 100 && right45 < 100) {
			// Critical response
			// Make sure vehicle speed is between speedMin and speedMax
			if (velocity < speedMin || velocity <= 0) {
				velocity = speedMin;
			} else if (velocity > speedMax) {
				velocity = speedMax;
			}
			//System.out.println("CLR45 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (left45 < 100) {
			// Critical response
			// Usually speed up
			velocity += accelShift;
			// Make sure vehicle speed is between speedMin and speedMax
			if (velocity < speedMin || velocity <= 0) {
				velocity = speedMin;
			} else if (velocity > speedMax) {
				velocity = speedMax;
			}
			//System.out.println("CL45 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (right45 < 100) {
			// Critical response
			// Usually slow down
			velocity += decelShift;
			// Make sure vehicle speed is between speedMin and speedMax
			if (velocity < speedMin || velocity <= 0) {
				velocity = speedMin;
			} else if (velocity > speedMax) {
				velocity = speedMax;
			}
			//System.out.println("CR45 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (left30 < 60 || right30 < 60) {
			// Critical response
			// Usually slow down
			velocity += decelShift;
			// Make sure vehicle speed is between speedMin and speedMax
			if (velocity < speedMin || velocity <= 0) {
				velocity = speedMin;
			} else if (velocity > speedMax) {
				velocity = speedMax;
			}
			//System.out.println("CLR30 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (left60 < 45 || right60 < 45) {
			// Critical response
			// Usually speed up
			velocity += accelShift;
			// Make sure vehicle speed is between speedMin and speedMax
			if (velocity < speedMin || velocity <= 0) {
				velocity = speedMin;
			} else if (velocity > speedMax) {
				velocity = speedMax;
			}
			//System.out.println("CLR60 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (left45 < 200 && right45 < 200) {
			// Make sure vehicle speed is between speedMin and speedMax
			if (velocity < speedMin || velocity <= 0) {
				velocity = speedMin;
			} else if (velocity > speedMax) {
				velocity = speedMax;
			}
			 //System.out.println("LR45 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (left45 < 200) {
			// Usually speed up
			velocity += accelShift;
			// Make sure vehicle speed is between speedMin and speedMax
			if (velocity < speedMin || velocity <= 0) {
				velocity = speedMin;
			} else if (velocity > speedMax) {
				velocity = speedMax;
			}
			//System.out.println("L45 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (right45 < 200) {
			// Usually slow down
			velocity += decelShift;
			// Make sure vehicle speed is between speedMin and speedMax
			if (velocity < speedMin || velocity <= 0) {
				velocity = speedMin;
			} else if (velocity > speedMax) {
				velocity = speedMax;
			}
			//System.out.println("R45 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if ((left30 < 120 || right30 < 120)) {
			// Usually slow down
			velocity += decelShift;
			// Make sure vehicle speed is between speedMin and speedMax
			if (velocity < speedMin || velocity <= 0) {
				velocity = speedMin;
			} else if (velocity > speedMax) {
				velocity = speedMax;
			}
			//System.out.println("LR30 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if ((left60 < 90 || right60 < 90)) {
			// Usually speed up
			velocity += accelShift;
			// Make sure vehicle speed is between speedMin and speedMax
			if (velocity < speedMin || velocity <= 0) {
				velocity = speedMin;
			} else if (velocity > speedMax) {
				velocity = speedMax;
			}
			//System.out.println("LR60 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else {
			// Reset speed
			velocity = this.getCurrentLane().getSpeedLimit();
		}
		
		// Adjust speed and reset wait timer
		if (responseCounter <= 0) {
			responseCounter = responseWait;
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		}

		responseCounter--;
	}

}
