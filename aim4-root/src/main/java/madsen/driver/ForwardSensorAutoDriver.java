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
	private int responseWait = 10;
	
	/**
	 * Number of iteration remaining before a non-critical response to
	 * sensor readings may be attempted again.
	 */
	private int responseCounter = 0;

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

//		System.out.println("Gauge velocity " + this.getVehicle().gaugeVelocity());
//		this.getVehicle().setTargetVelocityWithMaxAccel(35);
		System.out.println(this.getCurrentLane().getSpeedLimit());
		
		// Determining appropriate action to take
		if (left45 < 100 && right45 < 100) {
			// Critical response
			responseCounter = responseWait;
			double s = this.getVehicle().gaugeVelocity() + gaussian.nextGaussian();
			System.out.println("LR45 " + this.getVehicle().getSpec().getMaxVelocity());
			this.getVehicle().setTargetVelocityWithMaxAccel(this.getVehicle().gaugeVelocity());
		} else if (left45 < 100) {
			// Critical response
			responseCounter = responseWait;
		} else if (right45 < 100) {
			// Critical response
			responseCounter = responseWait;
		} else if (left30 < 60 || right30 < 60) {
			// Critical response
			responseCounter = responseWait;
		} else if (left60 < 45 || right60 < 45) {
			// Critical response
			responseCounter = responseWait;
		} else if (left45 < 200 && right45 < 200 && responseCounter <= 0) {
			responseCounter = responseWait;
		} else if (left45 < 200 && responseCounter <= 0) {
			responseCounter = responseWait;
		} else if (right45 < 200 && responseCounter <= 0) {
			responseCounter = responseWait;
		} else if ((left30 < 120 || right30 < 120) && responseCounter <= 0) {
			responseCounter = responseWait;
		} else if ((left60 < 90 || right60 < 90) && responseCounter <= 0) {
			responseCounter = responseWait;
		} else {
			// Reset speed
		}

		responseCounter--;
	}

}
