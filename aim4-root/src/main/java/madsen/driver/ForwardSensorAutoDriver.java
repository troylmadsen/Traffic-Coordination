package madsen.driver;

import java.util.Random;

import aim4.driver.AutoDriver;
import aim4.map.BasicMap;
import aim4.vehicle.AutoVehicleDriverView;

/**
 * Functions identically to a {@link AutoDriver} with the added use
 * of front peripheral distance sensors to provide input.
 * 
 * @author Troy Madsen
 */
public class ForwardSensorAutoDriver extends AutoDriver {
	
	private static Random gaussian = new Random();

	/**
	 * Number of iterations that should be waited before attempting another
	 * response to break an intersection deadlock.
	 */
	private static int responseWait = 10;
	
	/**
	 * Number of iteration remaining before a non-critical response to
	 * sensor readings may be attempted again.
	 */
	private int responseCounter = 0;
	
	/**
	 * The standard deviation of the speed adjustment distribution.
	 */
	private static double std = 7.0;
	
	/**
	 * Added to the mean to create a pseudo-left skew.
	 */
	private static double leftSkew = 5.0;
	
	/**
	 * Added to the mean to create a pseudo-right skew.
	 */
	private static double rightSkew = -5.0;
	
	/**
	 * The maximum amount the vehicle is allowed to slow down.
	 */
	private static double maxSpeedReduction = std * 2;

	/**
	 * A driver controlling a vehicle with forward peripheral sensors.
	 * 
	 * @param vehicle The vehicle this driver will control
	 * @param basicMap The map the vehicle exists in
	 */
	public ForwardSensorAutoDriver(AutoVehicleDriverView vehicle,
			BasicMap basicMap) {
		super(vehicle, basicMap);
	}

	/**
	 * Take control actions for driving the agent's Vehicle.  This allows
	 * both the Coordinator and the Pilot to act (in that order).
	 */
	@Override
	public void act() {
		super.act();

		respondToSensors();
	}

	/**
	 * Uses all of the front peripheral sensor data to appropriately adjust
	 * it's speed to avoid collisions.
	 */
	private void respondToSensors() {
		// Sensor readings
		double left30 = getVehicle().getFrontLeft30VehicleDistanceSensor().read();
		double left45 = getVehicle().getFrontLeft45VehicleDistanceSensor().read();
		double left60 = getVehicle().getFrontLeft60VehicleDistanceSensor().read();
		double right30 = getVehicle().getFrontRight30VehicleDistanceSensor().read();
		double right45 = getVehicle().getFrontRight45VehicleDistanceSensor().read();
		double right60 = getVehicle().getFrontRight60VehicleDistanceSensor().read();
		
		// Preemptively getting a random gaussian
		// Getting new speed based on current speed to allow for better deadlock resolution
//		double v = this.getCurrentLane().getSpeedLimit() + (std * gaussian.nextGaussian());
		double v = this.getVehicle().gaugeVelocity() + (std * gaussian.nextGaussian());
		
		// Determining appropriate action to take
		if (left45 < 100 && right45 < 100) {
			// Critical response
			responseCounter = responseWait;
			if (v < (this.getCurrentLane().getSpeedLimit() - maxSpeedReduction) || v < 0) {
				v = Math.max(this.getCurrentLane().getSpeedLimit() - maxSpeedReduction, 1.0);
			}
//			System.out.println("CLR45 -> " + v);
			this.getVehicle().setTargetVelocityWithMaxAccel(v);
		} else if (left45 < 100) {
			// Critical response
			// Usually speed up
			responseCounter = responseWait;
			v += leftSkew;
			if (v < (this.getCurrentLane().getSpeedLimit() - maxSpeedReduction) || v < 0) {
				v = Math.max(this.getCurrentLane().getSpeedLimit() - maxSpeedReduction, 1.0);
			}
//			System.out.println("CL45 -> " + v);
			this.getVehicle().setTargetVelocityWithMaxAccel(v);
		} else if (right45 < 100) {
			// Critical response
			// Usually slow down
			responseCounter = responseWait;
			v += rightSkew;
			if (v < (this.getCurrentLane().getSpeedLimit() - maxSpeedReduction) || v < 0) {
				v = Math.max(this.getCurrentLane().getSpeedLimit() - maxSpeedReduction, 1.0);
			}
//			System.out.println("CR45 -> " + v);
			this.getVehicle().setTargetVelocityWithMaxAccel(v);
		} else if (left30 < 60 || right30 < 60) {
			// Critical response
			// Usually slow down
			responseCounter = responseWait;
			v += rightSkew;
			if (v < (this.getCurrentLane().getSpeedLimit() - maxSpeedReduction) || v < 0) {
				v = Math.max(this.getCurrentLane().getSpeedLimit() - maxSpeedReduction, 1.0);
			}
//			System.out.println("CLR30 -> " + v);
			this.getVehicle().setTargetVelocityWithMaxAccel(v);
		} else if (left60 < 45 || right60 < 45) {
			// Critical response
			// Usually speed up
			responseCounter = responseWait;
			v += leftSkew;
			if (v < (this.getCurrentLane().getSpeedLimit() - maxSpeedReduction) || v < 0) {
				v = Math.max(this.getCurrentLane().getSpeedLimit() - maxSpeedReduction, 1.0);
			}
//			System.out.println("CLR60 -> " + v);
			this.getVehicle().setTargetVelocityWithMaxAccel(v);
		} else if (left45 < 200 && right45 < 200 && responseCounter <= 0) {
			responseCounter = responseWait;
			if (v < (this.getCurrentLane().getSpeedLimit() - maxSpeedReduction) || v < 0) {
				v = Math.max(this.getCurrentLane().getSpeedLimit() - maxSpeedReduction, 1.0);
			}
//			System.out.println("LR45 -> " + v);
			this.getVehicle().setTargetVelocityWithMaxAccel(v);
		} else if (left45 < 200 && responseCounter <= 0) {
			// Usually speed up
			responseCounter = responseWait;
			v += leftSkew;
			if (v < (this.getCurrentLane().getSpeedLimit() - maxSpeedReduction) || v < 0) {
				v = Math.max(this.getCurrentLane().getSpeedLimit() - maxSpeedReduction, 1.0);
			}
//			System.out.println("L45 -> " + v);
			this.getVehicle().setTargetVelocityWithMaxAccel(v);
		} else if (right45 < 200 && responseCounter <= 0) {
			// Usually slow down
			responseCounter = responseWait;
			v += rightSkew;
			if (v < (this.getCurrentLane().getSpeedLimit() - maxSpeedReduction) || v < 0) {
				v = Math.max(this.getCurrentLane().getSpeedLimit() - maxSpeedReduction, 1.0);
			}
//			System.out.println("R45 -> " + v);
			this.getVehicle().setTargetVelocityWithMaxAccel(v);
		} else if ((left30 < 120 || right30 < 120) && responseCounter <= 0) {
			// Usually slow down
			responseCounter = responseWait;
			v += rightSkew;
			if (v < (this.getCurrentLane().getSpeedLimit() - maxSpeedReduction) || v < 0) {
				v = Math.max(this.getCurrentLane().getSpeedLimit() - maxSpeedReduction, 1.0);
			}
//			System.out.println("LR30 -> " + v);
			this.getVehicle().setTargetVelocityWithMaxAccel(v);
		} else if ((left60 < 90 || right60 < 90) && responseCounter <= 0) {
			// Usually speed up
			responseCounter = responseWait;
			v += leftSkew;
			if (v < (this.getCurrentLane().getSpeedLimit() - maxSpeedReduction) || v < 0) {
				v = Math.max(this.getCurrentLane().getSpeedLimit() - maxSpeedReduction, 1.0);
			}
//			System.out.println("LR60 -> " + v);
			this.getVehicle().setTargetVelocityWithMaxAccel(v);
		} else if (responseCounter <= 0) {
			// Reset speed
			this.getVehicle().setTargetVelocityWithMaxAccel(this.getCurrentLane().getSpeedLimit());
		}

		responseCounter--;
	}

}
