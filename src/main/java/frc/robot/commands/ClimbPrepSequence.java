/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import frc.robot.Robot;
import edu.wpi.first.wpilibj.command.CommandGroup;

public class ClimbPrepSequence extends CommandGroup {
  /**
   * Climbing sequence!  Stows the wrist safely (if needed), moves arm to get 
   * vacuum, then lifts the robot.
   */
  public ClimbPrepSequence() {
    addSequential(new ElevatorWristStow());
    addSequential(new ClimbArmSetAngle(Robot.robotPrefs.climbPrep));
  }
}