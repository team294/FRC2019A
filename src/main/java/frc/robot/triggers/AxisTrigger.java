/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.triggers;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.Trigger;

/**
 * LT and RT on xbox controller
 */
public class AxisTrigger extends Trigger {
  Joystick joystick;
  int axis;
  double min;
  
  /**
   * Creates a trigger that is active when the given joystick
   * axis' value is gerater than "min".  
   * @param joystick Joystick to monitor
   * @param axis Joystick axis number to montior
   * @param min Triggering minimum threshold
   */
  public AxisTrigger(Joystick joystick, int axis, double min) {
      this.joystick = joystick;
      this.axis = axis;
      this.min = min;
  }
  
  /**
   * Returns true (trigger active) if the joystick axis value is greater than min. 
   */
  @Override
  public boolean get() {
      return joystick.getRawAxis(axis) > min;
  }
}
