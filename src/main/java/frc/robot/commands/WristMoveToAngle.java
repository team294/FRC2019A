/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import edu.wpi.first.wpilibj.command.Command;
import frc.robot.Robot;
import frc.robot.RobotMap;

public class WristMoveToAngle extends Command {

  private double target;
  private boolean targetAngle; // true if target is angle, false if target is position
  private RobotMap.WristAngle angle;

  /**
   * Moves wrist to target angle
   * @param angle target angle in degrees
   */
  public WristMoveToAngle(double angle) {
    // Use requires() here to declare subsystem dependencies
    // eg. requires(chassis);
    requires (Robot.wrist);
    target = angle;
    targetAngle = true;
  }

  /**
   * Moves wrist to target angle
   * @param angle target angle based on the angle called from RobotMap
   */
  public WristMoveToAngle(RobotMap.WristAngle angle) {
    requires(Robot.elevator);
    this.angle = angle;
    targetAngle = false;
  }

  // Called just before this Command runs the first time
  @Override
  protected void initialize() {
    if(targetAngle) {
      Robot.wrist.setWristAngle(target);
    } else {
      switch (angle) {
        case stowed:
          Robot.wrist.setWristAngle(RobotMap.WristStowed);
          break;
        case up:
          Robot.wrist.setWristAngle(RobotMap.WristUp);
          break;
        case straight:
          Robot.wrist.setWristAngle(RobotMap.WristStraight);
          break;
        case down:
          Robot.wrist.setWristAngle(RobotMap.WristDown);
          break;
      }
    }
  }

  // Called repeatedly when this Command is scheduled to run
  @Override
  protected void execute() {
  }

  // Make this return true when this Command no longer needs to run execute()
  @Override
  protected boolean isFinished() {
    return !Robot.wrist.getEncOK() || Math.abs(Robot.wrist.getWristAngle() - Robot.wrist.getCurrentWristTarget()) < 5.0; // tolerance of 5 degrees
  }

  // Called once after isFinished returns true
  @Override
  protected void end() {
  }

  // Called when another command which requires one or more of the same
  // subsystems is scheduled to run
  @Override
  protected void interrupted() {
    Robot.wrist.stopWrist();
  }
}