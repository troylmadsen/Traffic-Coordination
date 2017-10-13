/*
Copyright (c) 2011 Tsz-Chiu Au, Peter Stone
University of Texas at Austin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

3. Neither the name of the University of Texas at Austin nor the names of its
contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package aim4;

import aim4.gui.SimSetupPanel;
import aim4.gui.Viewer;
import aim4.sim.setup.BasicSimSetup;
import aim4.util.Util;

/**
 * The default main class to show the GUI.
 */
public class Main {

  /////////////////////////////////
  // THE MAIN FUNCTION
  /////////////////////////////////

  /**
   * The main function of the simulator.
   * It starts the GUI.
   *
   * @param args  the command-line arguments; it should be empty since the GUI
   *              does not take any command-line arguments
   *
   */
  public static void main(String[] args) {
	  
	  /* Troy Madsen */
	  // Parameter variables
	  int runNumber = 0;
	  boolean headless = false;
	  double speedLimit = 25.0;
	  int lanes = 1;
	  double signalDuration = 30.0;
	  double trafficDensity = 0.28;
	  String logFile = "Research_Log_Default";
	  int executionDuration = 60;
	  int modelNum = 3;
	  double delayTime = 5.0; //TODO this needs to take a time value in milliseconds. Currently uses iterations
	  double std = 7.0;
	  double skewLeft = 9.0;
	  double skewRight = -9.0;
	  double minAdj = Double.MIN_VALUE;
	  double maxAdj = Double.MAX_VALUE;
	  double varMax = Double.MAX_VALUE;
	  
	  // Parsing command line for simulator setup
	  /*
	   * Help					-h	-help				Displays help
	   * Headless				-headless				Sets the simulator as headless
	   * Speed Limit			-s	-speed-limit		Sets the speed limit
	   * Lane Count				-l	-lanes				Sets the number of lanes
	   * Random seed			-r	-random-seed		Sets the random seed
	   * Signal Duration		-s	-signal-duration	Sets the duration of stop lights
	   * Traffic Density		-d	-traffic-density	Sets the traffic density
	   * Log File				-f	-log-file			Sets the name of the log file
	   * Execution Duration		-e	-execution-duration	Sets the execution duration
	   * Model					-m	-model				Sets the simulator model to run
	   * Delay Time				-T	-delay-time			Sets the required delay time before
	   * 												another sensor response may occur
	   * Standard Deviation		-D	-std				Sets the standard deviation of the
	   * 												speed adjustment curve
	   * Skew Left				-L	-skew-left			Sets the skew of a left-skewed curve
	   * Skew Right				-R	-skew-right			Sets the skew of a right-skewed curve
	   * Maximum Reduction		-M	-max-red			Sets the maximum amount a vehicle can reduce its speed by
	   * Maximum Increase		-X	-max-inc			Sets the maximum amount a vehicle can increase its speed by
	   * Variation Max			-V	-variation			Sets the maximum variation in speed from
	   * 												standard deviation
	   */
	  try {
		  for (int i = 0; i < args.length; i++) {
			  if (args[i].equals("-help") || args[i].equals("-h")) {
				  System.out.println();
				  System.out.println("Usage: aim4.jar [OPTION] [PARAMETER]...");
				  System.out.println("Runs the AIM4 simulator.");
				  System.out.println("  -h, -help\t\t\tUsage information");
				  System.out.println("      -headless\t\t\tRuns the simulator"
						  +" in headless mode");
				  System.out.println("  -s, -speed-limit\t\tSpeed limit of the"
						  + " simulator");
				  System.out.println("  -l, -lanes\t\t\tNumber of road lanes");
				  System.out.println("  -r, -random-seed\t\tSeed of the random"
						  + " number generator");
				  System.out.println("  -s, -signal-duration\t\tDuration of"
						  + " traffic signals if they exist in the simulator"
						  + " model");
				  System.out.println("  -d, -traffic-density\t\tDensity of"
						  + " traffic flow in simulator");
				  System.out.println("  -f, -log-file\t\t\tFile to log data"
						  + " to");
				  System.out.println("  -e, -execution-duration\tRun duration"
						  + " of the simulator");
				  System.out.println("  -m, -model\t\t\tSimulator model to"
						  + " execute");
				  System.out.println("  -T, -delay-time\t\tDelay between each"
						  + " vehicle response to sensor readings");
				  System.out.println("  -D, -std\t\t\tDeviation of vehicle"
						  + " sensor response degree");
				  System.out.println("  -L, -skew-left\t\tLeftward skew of"
						  + " vehicle sensor response");
				  System.out.println("  -R, -skew-right\t\tRightward skew of"
						  + " vehicle sensor response");
				  System.out.println("  -M, -max-red\t\t\tMaximum amount"
						  + " a vehicle can reduce its speed by");
				  System.out.println("  -X, -max-inc\t\t\tMaximum amount"
						  + " a vehicle can increase its speed by");
				  System.out.println("  -V, -variation\t\tMaximum variation"
						  + " from standard deviation");
				  System.out.println();
				  System.out.println("Report bugs to <madsentr@mail.gvsu.edu>");
				  System.exit(0);
			  } else if (args[i].equals("-headless")) {
				  headless = true;
			  } else if (args[i].equals("-speed-limit")
					  || args[i].equals("-s")) {
				  speedLimit = Double.parseDouble(args[++i]);
				  
				  if (80.0 < speedLimit || speedLimit < 0) {
					  throw new IllegalArgumentException("Speed limit may " +
							  "not be lower than 0 or greater then 80.0.");
				  }
			  } else if (args[i].equals("-lanes") || args[i].equals("-l")) {
				  lanes = Integer.parseInt(args[++i]);
				  
				  if (8 < lanes || lanes < 1) {
					  throw new IllegalArgumentException("Lanes may not be " +
							  "less than 1 or greater than 8.");
				  }
			  } else if (args[i].equals("-random-seed")
					  || args[i].equals("-r")) {
				  runNumber = Integer.parseInt(args[++i]);
				  
				  // Setting the random number generator
				  Util.random.setSeed(runNumber);
			  } else if (args[i].equals("-signal-duration")
					  || args[i].equals("-s")) {
				  signalDuration = Double.parseDouble(args[++i]);
				  
				  if (signalDuration < 5.0) {
					  throw new IllegalArgumentException("Signal duration may"
							  + " not be lower than 5.0 seconds.");
				  }
			  } else if (args[i].equals("-traffic-density")
					  || args[i].equals("-d")) {
				  // Mapping 0-2500 to 0-0.70
				  trafficDensity = Double.parseDouble(args[++i])
						  * (0.7 / 2500.0);
				  
				  if (0 > trafficDensity || trafficDensity > 0.7) {
					  throw new IllegalArgumentException("Traffic density must"
							  + " be between 0 and 2500.");
				  }
			  } else if(args[i].equals("-log-file") || args[i].equals("-f")) {
				  logFile = args[++i];
			  } else if (args[i].equals("-execution-duration")
					  || args[i].equals("-e")) {
				  executionDuration = Integer.parseInt(args[++i]);
				  
				  if (60 > executionDuration) {
					  throw new IllegalArgumentException("Execution duration"
					  		+ " must be longer than 60.");
				  }
			  } else if (args[i].equals("-model") || args[i].equals("-m")) {
				  modelNum = Integer.parseInt(args[++i]);
				  
				  if (SimSetupPanel.MODEL_COUNT <= modelNum || modelNum < 0) {
					  throw new IllegalArgumentException("Specified model "
							  + "must be between 0 and "
							  + SimSetupPanel.MODEL_COUNT + " exclusive.");
				  }
			  } else if (args[i].equals("-delay-time") || args[i].equals("-T")) {
				  delayTime = Double.parseDouble(args[++i]);

				  if (delayTime < 0) {
					  throw new IllegalArgumentException("Delay time must be "
							  + "greater than or equal to 0.");
				  }
			  } else if (args[i].equals("-std") || args[i].equals("-D")) {
				  std = Double.parseDouble(args[++i]);

				  if (std < 0) {
					  throw new IllegalArgumentException("Standard deviation "
							  + "may not be less than 0.");
				  }
			  } else if (args[i].equals("-skew-left") || args[i].equals("-L")) {
				  skewLeft = Double.parseDouble(args[++i]);
			  } else if (args[i].equals("-skew-right") || args[i].equals("-R")) {
				  skewRight = Double.parseDouble(args[++i]);
			  } else if (args[i].equals("-max-red") || args[i].equals("-M")) {
				  minAdj = Double.parseDouble(args[++i]);
			  } else if (args[i].equals("-max-inc") || args[i].equals("-X")) {
				  maxAdj = Double.parseDouble(args[++i]);
			  } else if (args[i].equals("-variation") || args[i].equals("-V")) {
				  varMax = Double.parseDouble(args[++i]);
			  } else {
				  throw new IllegalArgumentException("Parameter not known.");
			  }
		  }
	  }
	  catch (Exception e) {
		  System.out.println("\nParameter Entry Error! "
				  + e.getMessage());
		  System.exit(0);
	  }
	  

    // create the basic setup

    BasicSimSetup simSetup
      = new BasicSimSetup(1, // columns
                          1, // rows
                          4, // lane width
                          speedLimit, // speed limit
                          lanes, // lanes per road
                          1, // median size
                          150, // distance between
                          trafficDensity, // traffic level
                          1.0 // stop distance before intersection
                          );

    /* Troy Madsen */
    if (headless) {
    	new Viewer(simSetup, false, true, modelNum, executionDuration, logFile,
    			runNumber);
    }
    else {
    	new Viewer(simSetup);
    }
  }
}