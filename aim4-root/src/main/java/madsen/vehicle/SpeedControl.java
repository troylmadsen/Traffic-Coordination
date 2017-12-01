package madsen.vehicle;

/**
 * Contains all of the controls for {@link ForwardSensorAutoDriver} speed controlling.
 * 
 * @author Troy Madsen
 */
public class SpeedControl {
	public final double mean;
	public final double std;
	public final double minRed;
	public final double maxRed;
	public final double minInc;
	public final double maxInc;
	public final double speedMin;
	public final double speedMax;
	public final boolean speedRelative;
	public final double accelShift;
	public final double decelShift;
	
	/**
	 * Creates a new SpeedControl with the provided controls
	 * 
	 * @param mean			Mean of the speed adjustment curve of vehicles
	 * @param std			Standard deviation of the speed adjustment curve of
	 * 						vehicles
	 * @param minRed		Minimum speed reduction of a vehicle
	 * @param maxRed		Maximum speed reduction of a vehicle
	 * @param minInc		Minimum speed increase of a vehicle
	 * @param maxInc		Maximum speed increase of a vehicle
	 * @param speedMin		Minimum speed a vehicle may be reduced to
	 * @param speedMax		Maximum speed a vehicle may be increased to
	 * @param speedRelative	Sets the speed adjustments to be made relative to
	 * 						the current speed of the vehicle
	 * @param accelShift	Sets the shift amount for the speed adjustment curve
	 * 						of acceleration-tending operations
	 * @param decelShift	Sets the shift amount for the speed adjustment curve
	 * 						of deceleration-tending operations
	 */
	public SpeedControl(final double mean, final double std,
			final double minRed, final double maxRed, final double minInc,
			final double maxInc, final double speedMin, final double speedMax,
			final boolean speedRelative, final double accelShift,
			final double decelShift) {
		this.mean = mean;
		this.std = std;
		this.minRed = minRed;
		this.maxRed = maxRed;
		this.minInc = minInc;
		this.maxInc = maxInc;
		this.speedMin = speedMin;
		this.speedMax = speedMax;
		this.speedRelative = speedRelative;
		this.accelShift = accelShift;
		this.decelShift = decelShift;
	}
	
}
