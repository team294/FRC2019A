/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import edu.wpi.first.wpilibj.command.Command;
import frc.robot.Robot;

public class ClimbEncoderCalibrateAtLimit extends Command {
  /**
   * Drives climb motor up slowly, calibrates encoder when climb reaches the limit switch
   */
  public ClimbEncoderCalibrateAtLimit() {
    // Use requires() here to declare subsystem dependencies
    // eg. requires(chassis);
    requires(Robot.climb);
  }

  // Called just before this Command runs the first time
  @Override
  protected void initialize() {
  }

  // Called repeatedly when this Command is scheduled to run
  @Override
  protected void execute() {
    Robot.climb.setClimbMotorPercentOutput(0.1);
  }

  // Make this return true when this Command no longer needs to run execute()
  @Override
  protected boolean isFinished() {
    return Robot.climb.isClimbAtLimitSwitch();
  }

  // Called once after isFinished returns true
  @Override
  protected void end() {
    Robot.climb.stopClimbMotor();
    Robot.climb.calibrateClimbEnc(Robot.robotPrefs.climbStartingAngle, false);
    // TODO Something to set climb reference angle
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  @Override
  protected void interrupted() {
    Robot.climb.stopClimbMotor();
  }
}