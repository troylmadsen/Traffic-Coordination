package madsen.noise;

import aim4.noise.DoubleGauge;
import aim4.noise.NoiseFunction;
import aim4.util.Util;

/**
 * A gauge that holds doubles. This gauge also can apply noise
 * functions to incoming data to simulate a noisy gauge. Value can
 * only be adjusted proportionally where delta is the most that
 * the gauge can change from a single reading.
 * 
 * @author Troy Madsen
 */
public class SmoothDoubleGauge extends DoubleGauge {
	/**
	 * A rolling list of previous gauge readings to use for calculating
	 * the new value.
	 */
	private double values[] = new double[4];
	
	/**
	 * The maximum number of invalid record attempts before resetting value.
	 */
	private int resetLimit = 5;
	
	/**
	 * Number of invalid record attempts made in a row.
	 */
	private int invalidCount = 0;
	
	/**
	 * Maximum amount that the gauge reading can shift by from a single
	 * record call.
	 */
	private double delta = 10;

	// Constructors
	/**
	 * Class constructor for unlimited, uninitialized, noiseless gauge.
	 */
	public SmoothDoubleGauge() {}

	/**
	 * Class constructor for unlimited, uninitialized gauge with noise.
	 *
	 * @param noiseFunction the noise function this gauge will apply to values
	 * @param delta the amount the gauge can change by per record call
	 */
	public SmoothDoubleGauge(NoiseFunction noiseFunction, double delta) {
		super(noiseFunction);
		this.delta = delta;
		clear();
	}

	/**
	 * Class constructor for unlimited, noiseless gauge with initial value.
	 *
	 * @param value the initial value of the gauge
	 * @param delta the amount the gauge can change by per record call
	 */
	public SmoothDoubleGauge(double value, double delta) {
		super(value);
		this.delta = delta;
		clear();
	}

	/**
	 * Class constructor for unlimited, initialized gauge with noise.
	 *
	 * @param value the initial value of the gauge
	 * @param noiseFunction the noise function this gauge will apply to values
	 * @param delta the amount the gauge can change by per record call
	 */
	public SmoothDoubleGauge(double value, NoiseFunction noiseFunction,
			double delta) {
		super(value, noiseFunction);
		this.delta = delta;
		clear();
	}

	/**
	 * Class constructor for limited, initialized, noiseless gauge.
	 *
	 * @param value    the initial value of the gauge
	 * @param minValue the minimum value the gauge can store/read
	 * @param maxValue the maximum value the gauge can store/read
	 * @param delta the amount the gauge can change by per record call
	 */
	public SmoothDoubleGauge(double value, double minValue, double maxValue,
			double delta) {
		super(value, minValue, maxValue);
		this.delta = delta;
		clear();
	}

	/**
	 * Class Constructor for limited, initialized, noisy gauge.
	 *
	 * @param value         the initial value of the gauge
	 * @param minValue      the minimum value the gauge can store/read
	 * @param maxValue      the maximum value the gauge can store/read
	 * @param noiseFunction the noise function this gauge will apply to values
	 * @param delta the amount the gauge can change by per record call
	 */
	public SmoothDoubleGauge(double value, double minValue, double maxValue,
			NoiseFunction noiseFunction, double delta) {
		super(value, minValue, maxValue, noiseFunction);
		this.delta = delta;
		clear();
	}
	
	// Helper methods
	
	/**
	 * Clears all old values built up in the gauge for clean recording.
	 */
	public void clear() {
		for (int i = 0; i < values.length; i++) {
			values[i] = Double.MAX_VALUE;
		}
	}

	// Set

	/**
	   * Records a value to the gauge, with noise according to the
	   * gauge's {@link NoiseFunction}. Value will only be accepted if the
	   * change in value is less than or equal to delta.
	   *
	   * @param recValue the value to be written to the gauge
	   */
	@Override
	public void record(double recValue) {
		double v = noiseFunction.apply(recValue);
		if (Math.abs(values[0] - Double.MAX_VALUE) <= 0.0000001) {
			// Checking for blank gauge and setting gauge reading to recValue
			for (int i = 0; i < values.length; i++) {
				values[i] = v;
			}
			value = v;
		} else {
			double sum = 0;
			for (double d: values) {
				sum += d;
			}
			double dValue = (sum + v) / (values.length + 1);
			// If change is permissible, adjust gauge
			if (Math.abs(dValue - value) <= delta) {
				for (int i = 1; i < values.length; i++) {
					values[i-1] = values[i];
				}
				values[values.length - 1] = v;
				value = Util.constrain(dValue, minValue, maxValue);
				invalidCount = 0;
			}
			else {
				System.out.println("Invalid reading " + dValue + " " + value);
				invalidCount++;
				if (invalidCount >= resetLimit) {
					clear();
				}
			}
		}
	}
}










































