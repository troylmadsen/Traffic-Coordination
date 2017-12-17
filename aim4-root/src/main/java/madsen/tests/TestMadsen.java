package madsen.tests;

import java.awt.geom.Point2D;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import aim4.gui.Viewer;
import aim4.map.GridMap;
import aim4.sim.setup.BasicSimSetup;
import aim4.vehicle.AutoVehicleSimView;
import aim4.vehicle.BasicAutoVehicle;
import aim4.vehicle.VehicleSpecDatabase;
import junit.framework.TestCase;
import madsen.driver.ForwardSensorAutoDriver;
import madsen.vehicle.SpeedControl;

class TestMadsen extends TestCase {
	
	// Input parameters
	int runNumber = 0;
	boolean headless = false;
	double speedLimit = 25.0;
	int lanes = 1;
	double signalDuration = 30.0;
	double trafficDensity = 0.28;
	String logFile = "Unit Tests";
	int executionDuration = 60;
	int modelNum = 3;
	double delayTime = 5.0; //TODO this needs to take a time value in milliseconds. Currently uses iterations
	double mean = 0;
	double std = 10.0;
	double minRed = 0;
	double maxRed = Double.MAX_VALUE;
	double minInc = 0;
	double maxInc = Double.MAX_VALUE;
	double speedMin = 0;
	double speedMax = Double.MAX_VALUE;
	boolean speedRelative = false;
  	double accelShift = 5;
  	double decelShift = -5;

  	// Test simulator
  	BasicSimSetup simSetup;
  	
  	// Test vehicle
  	AutoVehicleSimView avsv;
  	
  	// Test map the vehicle exists in
  	GridMap map;
  	
  	// Test vehicle speed controls
  	SpeedControl sc;
  	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	protected void setUp() throws Exception {
		super.setUp();
		
		// create the basic setup
	    simSetup = new BasicSimSetup(1, // columns
	                          1, // rows
	                          4, // lane width
	                          speedLimit, // speed limit
	                          lanes, // lanes per road
	                          1, // median size
	                          150, // distance between
	                          trafficDensity, // traffic level
	                          1.0 // stop distance before intersection
	                          );
	    
	    // Create an AutoVehicleSimView and set the sensors
	    avsv = new BasicAutoVehicle(VehicleSpecDatabase.getVehicleSpecByName("SUV"), new Point2D.Double(0, 0), 0, 0, 0, 0, 0, 0);
		
		// Create a map for the vehicle
		map = new GridMap(0, 1, 1, 4, speedLimit, lanes, 1, 150);
		
		// Create a new SpeedControl for vehicles
		sc = new SpeedControl(mean, std, minRed, maxRed, minInc,
				maxInc, speedMin, speedMax, speedRelative, accelShift, decelShift);
	}

	@AfterEach
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	void create() {
		new Viewer(simSetup, false, true, modelNum, executionDuration, logFile,
    			runNumber, delayTime, mean, std, minRed, maxRed, minInc, maxInc,
    			speedMin, speedMax, speedRelative, accelShift, decelShift);
	}
	
	@Test
	void speedAdjustment1() {
		ForwardSensorAutoDriver d = new ForwardSensorAutoDriver(avsv, map, sc);
		
		//TODO Get the initial speed and the ending speed and assert whether they are equal
		//FIXME This does not get the proper speed as the vehicle may be accelerating still
		double initSpeed = avsv.getVelocity();
		
		System.out.println(d.getVehicle().getFrontVehicleDistanceSensor().read());
		d.getVehicle().getFrontVehicleDistanceSensor().record(100);
		System.out.println(d.getVehicle().getFrontVehicleDistanceSensor().read());
	}

}
