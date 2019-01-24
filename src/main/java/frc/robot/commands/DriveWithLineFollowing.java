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

public class DriveWithLineFollowing extends Command {
  public DriveWithLineFollowing() {
    // Use requires() here to declare subsystem dependencies
    // eg. requires(chassis);
    requires(Robot.driveTrain);
  }

  // Called just before this Command runs the first time
  @Override
  protected void initialize() {
    Robot.driveTrain.clearEncoderList();  // clear the encoder list in preparation for checking if the wheels are moving or not
    System.out.println("---LINE TRACKING INITIATED---");
    Robot.driveTrain.driveOnLine();
  }

  // Called repeatedly when this Command is scheduled to run
  @Override
  protected void execute() {
    Robot.driveTrain.driveOnLine();
  }

  // Make this return true when this Command no longer needs to run execute()
  @Override
  protected boolean isFinished() {
    return Robot.driveTrain.areEncodersTurning(5.0); // Check if the encoders have changed
  }

  // Called once after isFinished returns true
  @Override
  protected void end() {
    // TODO: Add a call to robot log here to indicate end of line following, possibly with a data dump
    Robot.driveTrain.stop();
    System.out.println("---LINE FOLLOWING ENDED---");
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  @Override
  protected void interrupted() {
    end();
  }
}
