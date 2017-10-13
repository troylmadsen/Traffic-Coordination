package madsen.driver;

import java.util.Random;

import aim4.driver.AutoDriver;
import aim4.gui.Viewer;
import aim4.map.BasicMap;
import aim4.vehicle.AutoVehicleDriverView;

/**
 * Functions identically to a {@link AutoDriver} with the added use of front
 * peripheral distance sensors to provide input.
 * 
 * @author Troy Madsen
 */
public class ForwardSensorAutoDriver extends AutoDriver {

	private static Random gaussian = new Random();

	/**
	 * Number of iterations that should be waited before attempting another
	 * response to break an intersection deadlock.
	 */
	private double responseWait = Viewer.delayTime;

	/**
	 * Number of iteration remaining before a non-critical response to sensor
	 * readings may be attempted again.
	 */
	private double responseCounter = 0;

	/**
	 * The standard deviation of the speed adjustment distribution.
	 */
	private double std = Viewer.std;

	/**
	 * Added to the mean to create a pseudo-left skew.
	 */
	private double leftSkew = Viewer.skewLeft;

	/**
	 * Added to the mean to create a pseudo-right skew.
	 */
	private double rightSkew = Viewer.skewRight;

	/**
	 * The maximum amount the vehicle is allowed to slow down.
	 */
	private double maxSpeedReduction = std * 2;

	/**
	 * Maximum amount a vehicle can reduce its speed by.
	 */
	private double maxRed = Viewer.maxRed;

	/**
	 * Maximum amount a vehicle can increase its speed by.
	 */
	private double maxInc = Viewer.maxInc;

	/**
	 * Maximum variation from the std that a vehicle may adjust by.
	 */
	private double varMax = Viewer.varMax;
	
	/**
	 * Minimum speed of this vehicle.
	 */
	private double minSpeed;
	
	/**
	 * Maximum speed of this vehicle.
	 */
	private double maxSpeed;

	/**
	 * A driver controlling a vehicle with forward peripheral sensors.
	 * 
	 * @param vehicle
	 *            The vehicle this driver will control
	 * @param basicMap
	 *            The map the vehicle exists in
	 */
	public ForwardSensorAutoDriver(AutoVehicleDriverView vehicle,
			BasicMap basicMap) {
		super(vehicle, basicMap);
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
		// Sensor readings
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
		
		// Determine minimum speed
		minSpeed = Math.max(this.getCurrentLane().getSpeedLimit() - maxRed, 1.0);
		
		// Determine maximum speed
		maxSpeed = this.getCurrentLane().getSpeedLimit() + maxInc;

		// Get a velocity variation within bounds
		double random = std * gaussian.nextGaussian();
		while (Math.abs(std - random) > varMax) {
			random = std * gaussian.nextGaussian();
		}

		// Preemptively getting a random gaussian speed adjustment
		// Getting new speed based on current speed to allow for better deadlock
		// resolution
		// double v = this.getCurrentLane().getSpeedLimit() + (std *
		// gaussian.nextGaussian());
		double velocity = this.getVehicle().gaugeVelocity() + random;

		// Determining appropriate action to take
		// TODO Add one for front collisions
		if (left45 < 100 && right45 < 100 && responseCounter <= 0) {
			// Critical response
			responseCounter = responseWait;
			// Make sure vehicle speed is between minAdj and maxAdj
			if (velocity < minSpeed || velocity <= 0) {
				velocity = minSpeed;
			} else if (velocity > maxSpeed) {
				velocity = maxSpeed;
			}
			//System.out.println("CLR45 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (left45 < 100 && responseCounter <= 0) {
			// Critical response
			// Usually speed up
			responseCounter = responseWait;
			velocity += leftSkew;
			// Make sure vehicle speed is between minAdj and maxAdj
			if (velocity < minSpeed || velocity <= 0) {
				velocity = minSpeed;
			} else if (velocity > maxSpeed) {
				velocity = maxSpeed;
			}
			//System.out.println("CL45 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (right45 < 100 && responseCounter <= 0) {
			// Critical response
			// Usually slow down
			responseCounter = responseWait;
			velocity += rightSkew;
			// Make sure vehicle speed is between minAdj and maxAdj
			if (velocity < minSpeed || velocity <= 0) {
				velocity = minSpeed;
			} else if (velocity > maxSpeed) {
				velocity = maxSpeed;
			}
			//System.out.println("CR45 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (left30 < 60 || right30 < 60 && responseCounter <= 0) {
			// Critical response
			// Usually slow down
			responseCounter = responseWait;
			velocity += rightSkew;
			// Make sure vehicle speed is between minAdj and maxAdj
			if (velocity < minSpeed || velocity <= 0) {
				velocity = minSpeed;
			} else if (velocity > maxSpeed) {
				velocity = maxSpeed;
			}
			//System.out.println("CLR30 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (left60 < 45 || right60 < 45 && responseCounter <= 0) {
			// Critical response
			// Usually speed up
			responseCounter = responseWait;
			velocity += leftSkew;
			// Make sure vehicle speed is between minAdj and maxAdj
			if (velocity < minSpeed || velocity <= 0) {
				velocity = minSpeed;
			} else if (velocity > maxSpeed) {
				velocity = maxSpeed;
			}
			//System.out.println("CLR60 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (left45 < 200 && right45 < 200 && responseCounter <= 0) {
			responseCounter = responseWait;
			// Make sure vehicle speed is between minAdj and maxAdj
			if (velocity < minSpeed || velocity <= 0) {
				velocity = minSpeed;
			} else if (velocity > maxSpeed) {
				velocity = maxSpeed;
			}
			 //System.out.println("LR45 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (left45 < 200 && responseCounter <= 0) {
			// Usually speed up
			responseCounter = responseWait;
			velocity += leftSkew;
			// Make sure vehicle speed is between minAdj and maxAdj
			if (velocity < minSpeed || velocity <= 0) {
				velocity = minSpeed;
			} else if (velocity > maxSpeed) {
				velocity = maxSpeed;
			}
			//System.out.println("L45 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (right45 < 200 && responseCounter <= 0) {
			// Usually slow down
			responseCounter = responseWait;
			velocity += rightSkew;
			// Make sure vehicle speed is between minAdj and maxAdj
			if (velocity < minSpeed || velocity <= 0) {
				velocity = minSpeed;
			} else if (velocity > maxSpeed) {
				velocity = maxSpeed;
			}
			//System.out.println("R45 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if ((left30 < 120 || right30 < 120) && responseCounter <= 0) {
			// Usually slow down
			responseCounter = responseWait;
			velocity += rightSkew;
			// Make sure vehicle speed is between minAdj and maxAdj
			if (velocity < minSpeed || velocity <= 0) {
				velocity = minSpeed;
			} else if (velocity > maxSpeed) {
				velocity = maxSpeed;
			}
			//System.out.println("LR30 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if ((left60 < 90 || right60 < 90) && responseCounter <= 0) {
			// Usually speed up
			responseCounter = responseWait;
			velocity += leftSkew;
			// Make sure vehicle speed is between minAdj and maxAdj
			if (velocity < minSpeed || velocity <= 0) {
				velocity = minSpeed;
			} else if (velocity > maxSpeed) {
				velocity = maxSpeed;
			}
			//System.out.println("LR60 -> " + velocity);
			this.getVehicle().setTargetVelocityWithMaxAccel(velocity);
		} else if (responseCounter <= 0) {
			// Reset speed
			this.getVehicle().setTargetVelocityWithMaxAccel(
					this.getCurrentLane().getSpeedLimit());
		}

		responseCounter--;
	}

}
