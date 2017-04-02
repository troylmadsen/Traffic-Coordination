package madsen.driver;

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
		
	}

}
