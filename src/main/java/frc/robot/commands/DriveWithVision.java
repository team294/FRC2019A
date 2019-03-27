/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Robot;

public class DriveWithVision extends Command {

  private boolean endOnLine = false;
  private boolean gyro = false;
  private double targetQuad = 0; // The quadrant of the target we want to drive to
  public double stopDistance = 26.0;    // Was 21  The camera is 21.7 inches from the front of the back of the hatch cover

  /**
   * Vision assisted driving without gyro, keep going and never end on the line
   */
  public DriveWithVision() {
    // Use requires() here to declare subsystem dependencies
    // eg. requires(chassis);
    this(false, false);
  }

  /**
   * Drive towards the vision target
   * @param endOnLine specify whether or not to end on the line target.
   * @param gyro specify whether or not to use gyro curve correction
   *  </br> true means end on line, false means continue to wall (will not exit with false)
   */
  public DriveWithVision(boolean endOnLine, boolean gyro) {
    requires(Robot.driveTrain);
    this.endOnLine = endOnLine;
    this.gyro = gyro;

    Robot.vision.setPipe(0); // On vision pipeline
    Robot.vision.setLedMode(3); // TODO Change back to 3 to turn on LEDs.  Make sure the LEDs are on before driving
  }

  // Called just before this Command runs the first time
  @Override
  protected void initialize() {
    Robot.vision.setPipe(0);
    Robot.vision.setLedMode(3);
    // Robot.driveTrain.setDriveMode(true);
    SmartDashboard.putBoolean("Ready to Score", false);
    Robot.driveTrain.clearEncoderList(); // May not be necessary to clear
    //Robot.driveTrain.driveToCrosshair();
    if (gyro) targetQuad = Robot.driveTrain.checkScoringQuadrant();
    System.out.println("Target Quadrant:" + targetQuad);
    Robot.log.writeLog("DriveTrain", "Vision Tracking Init", "Gyro," + gyro + ",Quadrant,"+targetQuad);
  }

  // Called repeatedly when this Command is scheduled to run
  @Override
  protected void execute() {
    Robot.driveTrain.driveToCrosshair(targetQuad, stopDistance);
  }

  // Make this return true when this Command no longer needs to run execute()
  @Override
  protected boolean isFinished() {
    // Robot.driveTrain.areEncodersStopped(5.0);
    return Robot.vision.distance < stopDistance;
    // return endOnLine && Robot.lineFollowing.isLinePresent() && Robot.vision.distanceFromTarget() < 40; // Stops when a line is detected by the line followers within a reasonable expected distance
    // TODO with an accurate distance measurement, we can stop automatically when close enough
  }

  // Called once after isFinished returns true
  @Override
  protected void end() {
    Robot.driveTrain.stop();
    Robot.vision.setPipe(2);
    Robot.vision.setLedMode(1);
    Robot.log.writeLog("DriveTrain", "Vision Tracking Ended", "");
    // Robot.leds.setColor(LedHandler.Color.OFF);   // Robot Periodic will turn off LEDs
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  @Override
  protected void interrupted() {
    end();
  }
}
