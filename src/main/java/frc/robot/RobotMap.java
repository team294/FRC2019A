
package frc.robot;

/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into
 * to a variable name. This provides flexibility changing wiring, makes checking
 * the wiring easier and significantly reduces the number of magic numbers
 * floating around.
 */
public class RobotMap {
  
  // CAN bus addresses
  public static final int powerDistributionPanel = 0;

  public static final int leftMotor1 = 10;
  public static final int leftMotor2 = 11;       // Talon
  public static final int leftMotor3 = 12;
  public static final int rightMotor1 = 20;
  public static final int rightMotor2 = 21;      // Talon
  public static final int rightMotor3 = 22;
  
  public static final int elevatorMotor1 = 30;   // Talon
  public static final int elevatorMotor2 = 31;   // Talon

  public static final int cargoMotor1 = 40;
  public static final int cargoMotor2 = 41;

  public static final int climbMotor1 = 50;     // Talon
  public static final int climbMotor2 = 51;     // Talon
  public static final int climbVacuum1 = 52;
  public static final int climbVacuum2 = 53;

  public static final int wristMotor = 60;      // Talon

  // Pneumatic Addresses
  public static final int pnuematicShifterLow = 0;
  public static final int pnuematicShifterHigh = 1;
  public static final int pneumaticHatchIn = 2;
  public static final int pneumaticHatchOut = 3;
 
  //RoboRIO digital I/O Addresses
  public static final int lineFollowerLeft = 2;
  public static final int lineFollowerCenter = 1;
  public static final int lineFollowerRight = 0;
  public static final int vacuumSwitch = 3;
  public static int photoSwitchCargo = 4;
   
  // TODO Update with 2019 base
  // Robot Pathfinder data
  public static final double encoderTicksPerRevolution = 4096.0;
  public static final double wheelbase_in = 25.0;       // wheelbase, in inches
  // public static final double wheel_diameter_in = 6.0;   // wheel diamater, in inches  -- DO NOT USE -- USE VALUE IN PREFERENCES INSTEAD
  // public static final double wheel_distance_in_per_tick = wheel_diameter_in*Math.PI/encoderTicksPerRevolution;  // wheel distance traveled per encoder tick, in inches
  public static final double max_velocity_ips = 200.0;   // max robot velocity, in inches per second
  public static final double max_acceleration_ipsps = 130.0;  // max robot acceleration, in inches per second per second
  public static final double max_jerk_ipspsps = 2400.0;  // max robot jerk, in inches per second per second per second
  
  // PDP Addresses
  public static final int leftMotor1PDP = 0; 
  public static final int leftMotor2PDP = 1; 
  public static final int leftMotor3PDP = 2; 
  public static final int rightMotor1PDP = 15; 
  public static final int rightMotor2PDP = 14; 
  public static final int rightMotor3PDP = 13; 
  public static final int climbMotor1PDP = 3; 
  public static final int climbMotor2PDP = 12;  
  public static final int climbVacuum1PDP = 10; 
  public static final int climbVacuum2PDP = 9; 
  public static final int elevatorMotor1PDP = 11; 
  public static final int elevatorMotor2PDP = 4; 
  public static final int cargoMotor1PDP = 6;  
  public static final int cargoMotor2PDP = 7;  
  public static final int wristMotorPDP = 5;  
}
