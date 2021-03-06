package frc.robot.utilities;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Robot;

public class RobotPreferences {

	private final Preferences prefs;
	
	/*
	 * all of the robot preferences
	 */
	public String problemSubsystem;     // Records which subsystem(s) problem(s) exist in
	public boolean problemExists;       // Set true if there is an issue
	public boolean inBCRLab;			// Set true if in the BCR lab (with a big pole in the middle of the field)
	public boolean prototypeRobot;		// Set true if using code for prototype bots, false for practice and competition bots
	public boolean neoDrivetrain;		// Set true if using neos on the drive train (mutually exclusive with prototypeRobot)
	public double wheelCircumference;	// Wheel circumference, in inches
	public double elevatorGearCircumference; //circumference of the gear driving the elevator in inches
	public double elevatorBottomToFloor; //distance of elevator 0 value from the ground
	public double elevatorWristSafeStow; 	 // highest elevator position (from ground) where wrist can be stowed
	public double cameraDistanceFromFrontOfBumper;  // (default = 12 inches)
	public double cameraXOffset;		// Fudge factor to account for camera mount not being centered
	public double wristGearRatio; 		// wrist gear ratio, gear with encoder / gear driving wrist
	public double wristCalZero;   		// Wrist encoder position at O degrees, in encoder ticks (i.e. the calibration factor)
	public boolean wristCalibrated = false;     // Default to wrist being uncalibrated.  Calibrate from robot preferences or "Calibrate Wrist Zero" button on dashboard
	public double climbCalZero; // Climb encoder position at 0 degrees in encoder ticks
	public boolean climbCalibrated = false; // Default to climb being uncalibrated

	/*
	* Measurements
	*/
	// Wrist Angles (in degrees)
	public final double wristMax = 113.0;		// Location of upper limit switch for auto calibration
	public final double wristStowed = 110.0;
	public final double wristKeepOut = 28.0; // Max angle to avoid interference with elevator or climber
	public final double wristUp = 15.0;
	public final double wristStraight = -1.0;	//  needed to bias upward to account for sag and insure that hatch cover gripper engages first
	public final double wristVision = -5.0;    // wrist angle for optimal vision tracking
	public final double wristCargoShot = -30.0;	// Angle for wrist for cargo ship ball shot
	public final double wristLowerCrashWhenElevatorLow = -45.0;   // If the elevator is in the low position, don't move the wrist below this!
	public final double wristDown = -60.0;		// TODO Should be -59.0? // In this position, elevator must be able to go to groundCargo
	public final double wristMin = -61.0;			// Location of lower limit switch for auto calibration
	public enum WristAngle {stowed, up, straight, cargoShot, vision, down}

	// TODO Update with 2019 base
  	// Robot Pathfinder data
  	public final double encoderTicksPerRevolution = 4096.0;
  	public final double wheelbase_in = 26.5;       // wheelbase, in inches was 25
  	// public static final double wheel_diameter_in = 6.0;   // wheel diamater, in inches  -- DO NOT USE -- Use wheelCircumference preference instead
  	// public static final double wheel_distance_in_per_tick = wheel_diameter_in*Math.PI/encoderTicksPerRevolution;  // wheel distance traveled per encoder tick, in inches
  	public final double max_velocity_ips = 115.0;   // max robot velocity, in inches per second  // was 95.0
  	public final double max_acceleration_ipsps = 130.0;  // max robot acceleration, in inches per second per second
	public final double max_jerk_ipspsps = 2400.0;  // max robot jerk, in inches per second per second per second
	  
	// TurnGyro Data
	public enum TurnDirection {left, right, shortest} // for direction TurnWithGyro will actually turn (or take the shortest turn)

	// Hatch piston positions
	//public enum HatchPistonPositions { grab, release, moving, unknown }

	/*
	Measurement variables
	*/

	// Field level heights (for elevator targeting), in inches
	public final double hatchLow = 19.0;
  	public final double hatchMid = 48.5;
  	public final double hatchHigh = 69.7;		// was 72.8
  	public final double cargoShipCargo = 43.0;   // Was 34.75
	public final double rocketBallOffset = 2;  // Ball intake is higher than the disc grabber (low position only)
	public final double loadCargo = 44.125;
	public final double groundCargo = 16.5;  		// At this level, wrist must be able to go to wristDown  // TODO should this be the same as elevatorWristStow (at the hard stop)?

	public enum ElevatorPosition {bottom, vision, wristStow, hatchLow, hatchMid, hatchHigh, cargoShipCargo, loadCargo, groundCargo}

	//Climb Target Angles (in degrees)
	public final double climbLimitAngle = 137.7;		// Max angle for climber (limit switch), was 120
	public final double climbWristStowedSafe = 137.0;	// Max angle for climber when wrist is stowed (but wrist can't move if climber is here)
	public final double climbWristMovingSafe = 122.0;	// Max angle for climber if wrist is moving
	public final double climbLiftAngle = 130.0;			// Angle where robot scores climb points, was 128 in Match 1
	public final double climbStart = 110.0;				// Climber starting angle (must be safe for wrist to move, must be in frame perimeter)
	public final double climbPrep = 45.0;				// Prep climber for climb (part way down, to make climb faster)
	public final double climbVacuumAngle = -5.0;		// Climber angle to attach vacuum to platform
	public final double climbMinAngle = -10.0;			// Min angle for climber

	/**
	 * Creates a RobotPreferences object and reads the robot preferences.
	 */
	public RobotPreferences() {
		prefs = Preferences.getInstance();
		refresh();
	}
	
	/**
	 * Re-reads the robot preferences.
	 */
	public void refresh() {
		problemSubsystem = prefs.getString("problemSubsystem", "");
		problemExists = prefs.getBoolean("problemExists", false);
		inBCRLab = prefs.getBoolean("inBCRLab", false);
		prototypeRobot = prefs.getBoolean("prototypeRobot", false); // true if testing code on a prototype, default to false (competition bot w/ Victors)
		neoDrivetrain = prefs.getBoolean("neoDrivetrain", true); // Default to true (using Neos) on competition bot
		wheelCircumference = prefs.getDouble("wheelDiameter", 6) * Math.PI;	
		elevatorGearCircumference = prefs.getDouble("elevatorGearDiameter", 1.43) * Math.PI; // TODO Recheck that value is correct (at OC Regional) Conversion factor for makeshift elevator 18/32.3568952084);
		elevatorBottomToFloor = prefs.getDouble("elevatorBottomToFloor", 15.5); // inches from ground to elevator's lowest position
		elevatorWristSafeStow = prefs.getDouble("elevatorWristSafeStow", 18.5); // max elevator position from floor where wrist can be stowed
		cameraDistanceFromFrontOfBumper = prefs.getDouble("cameraDistanceFromFrontOfBumper", 12);
		cameraXOffset = prefs.getDouble("cameraXOffset", 0.0);
		wristGearRatio = prefs.getDouble("wristGearRatio", 1.0);
		wristCalZero = prefs.getDouble("wristCalZero", -9999);
		wristCalibrated = (wristCalZero != -9999);
		if(!wristCalibrated) {
			DriverStation.reportError("Error: Preferences missing from RoboRio for Wrist calibration.", false);
			recordStickyFaults("Preferences-wristCalZero");
			wristCalZero = 0;
		}
		climbCalZero = prefs.getDouble("climbCalZero", -9999);
		climbCalibrated = (climbCalZero != -9999);
		if(!climbCalibrated) {
			DriverStation.reportError("Error: Preferences missing from RoboRio for Climb calibration.", false);
			recordStickyFaults("Preferences-climbCalZero");
			climbCalZero = 0;
		}
	}

	/**
	 * Sets climb angle calibration factor and enables angle control modes for climb.
	 * 
	 * @param climbCalZero
	 *            Calibration factor for climb
	 * @param writeCalToPreferences
	 *            true = store calibration in RobotPrefs, false = don't change RobotPrefs
	 */
	public void setClimbCalibration(double climbCalZero, boolean writeCalToPreferences) {
		this.climbCalZero = climbCalZero;
		climbCalibrated = true;
		Robot.climb.stopClimb();  // Stop motor, so it doesn't jump to new value
		Robot.log.writeLog("Preferences", "Calibrate climber", "zero value," + climbCalZero + ",write to prefs," + writeCalToPreferences);
		if (writeCalToPreferences) {
			prefs.putDouble("climbCalZero", climbCalZero);
		}
	}

	/**
	 * Stops climb motor and sets climbCalibrated to false
	 */
	public void setClimbUncalibrated() {
		Robot.climb.stopClimb();
		climbCalibrated = false;
		Robot.log.writeLog("Preferences", "Uncalibrate climber", "");
	}

	/* Sets up Preferences if they haven't been set as when changing RoboRios or first start-up.
		The values are set to defaults, so if using the prototype robots set inBCRLab to true
	*/	
	public void doExist(){				 
		if (!prefs.containsKey("problemSubsystem")){
			prefs.putString("problemSubsystem", "");
		}
		if (!prefs.containsKey("problemExists")) {
			prefs.putBoolean("problemExists", false);
		}
		if (!prefs.containsKey("inBCRLab")){
			 prefs.putBoolean("inBCRLab", false);
		}	 
		if (!prefs.containsKey("prototypeRobot")){
			prefs.putBoolean("prototypeRobot", false);
		}
		if (!prefs.containsKey("neoDrivetrain")){
			prefs.putBoolean("neoDrivetrain", true);
		}
		if (!prefs.containsKey("driveDirection")){
			prefs.putBoolean("driveDirection", false);
		}
		if (!prefs.containsKey("wheelDiameter")){
			prefs.putDouble("wheelDiameter", 6);
		}
		if (!prefs.containsKey("elevatorGearDiameter")) {
			prefs.putDouble("elevatorGearDiameter", 1.43);
		}
		if (!prefs.containsKey("elevatorBottomToFloor")) {
			prefs.putDouble("elevatorBottomToFloor", 15.5);
		}
		if (!prefs.containsKey("elevatorWristSafeStow")) {
			prefs.putDouble("elevatorWristSafeStow", 18.5);
		}
		if (!prefs.containsKey("cameraDistanceFromFrontOfBumper")){
			prefs.putDouble("cameraDistanceFromFrontOfBumper", 12);
		}
		if (!prefs.containsKey("cameraXOffset")){
			prefs.putDouble("cameraXOffset", 0.0);
		}
		if (!prefs.containsKey("wristGearRatio")) {
			prefs.putDouble("wristGearRatio", 1.0);
		}
		if (!prefs.containsKey("wristCalZero")) {
			prefs.putDouble("wristCalZero", -9999);
		}
		if (!prefs.containsKey("climbCalZero")) {
			prefs.putDouble("climbCalZero", -9999);
		}
	}

	/**
	 * Sets wrist angle calibration factor and enables angle control modes for wrist
	 * 
	 * @param wristCalZero  Calibration factor for wrist
	 * @param writeCalToPreferences  true = store calibration in Robot Preferences, false = don't change Robot Preferences
	 */
	public void setWristCalibration(double wristCalZero, boolean writeCalToPreferences) {
		this.wristCalZero = wristCalZero;
		wristCalibrated = true;
		Robot.wrist.stopWrist();	// Stop motor, so it doesn't jump to new value
		Robot.log.writeLog("Preferences", "Calibrate wrist", "zero value," + wristCalZero + 
			",Enc Raw," + Robot.wrist.getWristEncoderTicksRaw() +
			",Wrist Angle," + Robot.wrist.getWristAngle() + ",Wrist Target," + Robot.wrist.getCurrentWristTarget());
		if (writeCalToPreferences) {
			prefs.putDouble("wristCalZero", wristCalZero);
		}
	}

	/**
	 * Stops wrist motor and sets wristCalibrated to false
	 */
	public void setWristUncalibrated() {
		Robot.wrist.stopWrist();
		Robot.log.writeLog("Preferences", "Uncalibrate wrist", "Enc Raw," + Robot.wrist.getWristEncoderTicksRaw() +
			",Wrist Angle," + Robot.wrist.getWristAngle() + ",Wrist Target," + Robot.wrist.getCurrentWristTarget());
		wristCalibrated = false;
	}

	/**
	 * Records in robotPreferences, fileLog, and Shuffleboard that a problem was found in a subsystem
	 * (only records if the subsystem wasn't already flagged)
	 * @param subsystem String name of subsystem in which a problem exists
	 */
	public void recordStickyFaults(String subsystem) {
		if (problemSubsystem.indexOf(subsystem) == -1) {
			if (problemSubsystem.length() != 0) {
				problemSubsystem = problemSubsystem + ", ";
			}
			problemSubsystem = problemSubsystem + subsystem;
			putString("problemSubsystem", problemSubsystem);
			Robot.log.writeLogEcho(subsystem, "Sticky Fault Logged", "");
		}
		if (!problemExists) {
			problemExists = true;
			putBoolean("problemExists", problemExists);
		}
		showStickyFaults();
	}

	/**
	 * Clears any sticky faults in the RobotPreferences and Shuffleboard
	 */
	public void clearStickyFaults() {
		problemSubsystem = "";
		problemExists = false;
		putString("problemSubsystem", problemSubsystem);
		putBoolean("problemExists", problemExists);
		showStickyFaults();
		Robot.log.writeLog("RobotPrefs", "Sticky Faults Cleared", "");
	}

	/**
	 * Show any sticky faults on Shuffleboard
	 */
	public void showStickyFaults() {
		SmartDashboard.putString("problemSubsystem", problemSubsystem);
		SmartDashboard.putBoolean("problemExists", problemExists);
	}

	public String getString(String k) {
		return getString(k, null);
	}
	public String getString(String k, String d) {
		return prefs.getString(k, d);
	}
	public int getInt(String k) {
		return getInt(k, 0);
	}
	public int getInt(String k, int d) {
		return prefs.getInt(k, d);
	}
	public double getDouble(String k, double d) {
		return prefs.getDouble(k, d);
	}
	public double getDouble(String k) {
		return getDouble(k, 0);
	}
	public boolean getBoolean(String k, boolean d) {
		return prefs.getBoolean(k, d);
	}
	public boolean getBoolean(String k) {
		return getBoolean(k, false);
	}
	public void putString(String key, String val) {
		prefs.putString(key, val);
	}
	public void putDouble(String key, double val) {
		prefs.putDouble(key, val);
	}	
	public void putInt(String key, int val) {
		prefs.putInt(key, val);
	}
	public void putBoolean(String key, boolean val) {
		prefs.putBoolean(key, val);
	}

}