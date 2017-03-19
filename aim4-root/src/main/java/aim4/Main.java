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

import java.awt.Component;

import javax.swing.JButton;

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
	  // Final values
	  final int NUM_MODELS = 4;
	  
	  // Parameter variables
	  int runNumber = -1;
	  boolean headless = false;
	  double speedLimit = 25.0;
	  int lanes = 1;
	  double signalDuration = 30.0;
	  double trafficDensity = 0.28;
	  String logFile = "Research Log Default";
	  int executionDuration = 60;
	  int modelNum = 0;
	  
	  // Parsing command line for sim setup
	  /*
	   * Help				-h -help				Displays help
	   * Headless			-headless				Sets the simulator as
	   * 											headless
	   * Speed Limit		-s -speed-limit			Sets the speed limit
	   * Lane Count			-l -lanes				Sets the number of lanes
	   * Random seed		-r -random-seed			Sets the random seed
	   * Signal Duration	-s -signal-duration		Sets the duration of stop
	   * 											lights
	   * Traffic Density	-d -traffic-density		Sets the traffic density
	   * Log File			-f -log-file			Sets the name of the log
	   * 											file
	   * Execution Duration	-e -execution-duration	Sets the execution duration
	   * Model				-m -model				Sets the simulator model
	   * 											to run
	   */
	  try {
		  for (int i = 0; i < args.length; i++) {
			  args[i] = args[i].toLowerCase();
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
				  
			  } else if (args[i].equals("-model") || args[i].equals("-m")) {
				  modelNum = Integer.parseInt(args[++i]);
				  
				  if (NUM_MODELS <= modelNum || modelNum < 0) {
					  throw new IllegalArgumentException("Specified model "
							  + "must be between 0 and " + NUM_MODELS
							  + " exclusive.");
				  }
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
    Viewer v;
    if (headless) {
    	v = new Viewer(simSetup, false, true);
    }
    else {
    	new Viewer(simSetup);
    }
  }
}