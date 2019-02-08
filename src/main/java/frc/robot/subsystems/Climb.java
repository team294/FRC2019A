/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DigitalInput;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.BaseMotorController;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.ctre.phoenix.motorcontrol.SensorCollection;

import frc.robot.Robot;
import frc.robot.RobotMap;

/**
 * Add your docs here.
 */
public class Climb extends Subsystem {
  // Put methods for controlling this subsystem
  // here. Call these from Commands.
  private final Compressor compressor = new Compressor(0);
  private final WPI_TalonSRX climbMotor1 = new WPI_TalonSRX(RobotMap.climbMotor1);
  private final BaseMotorController climbMotor2 = new WPI_VictorSPX(RobotMap.climbMotor2);
  private final WPI_TalonSRX climbVacuum1 = new WPI_TalonSRX(RobotMap.climbVacuum1);
  private final WPI_TalonSRX climbVacuum2 = new WPI_TalonSRX(RobotMap.climbVacuum2);
  private final DigitalInput vacuumSwitch = new DigitalInput(RobotMap.vacuumSwitch);
  private final SensorCollection climbLimit;
  private int periodicCount = 0;
  public double climbStartingPoint = 0;

  public double rampRate = .005; 
  public double kP = 1;
  public double kI = 0;
  public double kD = 0;
  public double kFF = 0;
  public int kIz = 0;
  public double kMaxOutput = 1.0;	
  public double kMinOutput = -1.0;

  public Climb() {
    enableCompressor(true);

    climbMotor2.follow(climbMotor1);
    climbMotor2.setInverted(true);
    climbMotor1.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
    climbMotor1.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen);
    climbLimit = climbMotor1.getSensorCollection();

    zeroClimbEnc();

    climbMotor1.config_kP(0, kP);
    climbMotor1.config_kI(0, kI);
    climbMotor1.config_kD(0, kD);
    climbMotor1.config_kF(0, kFF);
    climbMotor1.config_IntegralZone(0, kIz);
    climbMotor1.configClosedloopRamp(rampRate);
    climbMotor1.configPeakOutputForward(kMaxOutput);
    climbMotor1.configPeakOutputReverse(kMinOutput);

    climbMotor1.clearStickyFaults(0);
    climbMotor2.clearStickyFaults(0);
    climbMotor1.setNeutralMode(NeutralMode.Brake);
    climbMotor2.setNeutralMode(NeutralMode.Brake);
  }
/*
  /**
   * Enables or disables the compressor
   * @param turnOn true = turn on compressor when pressure drops
   * 				false = keep compressor off
   */
  public void enableCompressor(boolean turnOn) {
	  compressor.setClosedLoopControl(turnOn);
  }

  /**
   * Sets percent power of motor
   * @param percentPower between -1.0 and 1.0
   */
  public void setClimbMotorPercentOutput(double percentOutput) {
    climbMotor1.set(percentOutput);
  }

  public void setClimbPos(double angle) {
    climbMotor1.set(ControlMode.Position, climbAngleToEncTicks(angle));
  }

  /**
   * Turns on or turns off the vacuum
   * @param turnOn true turns vacuum on, false turns vacuum off
   */
  public void enableVacuum(boolean turnOn) {
    if (turnOn) {
      climbVacuum1.set(0.5);
      climbVacuum2.set(0.5);
    }
    else {
      climbVacuum1.set(0.0);
      climbVacuum2.set(0.0);
    }
  }

  /**
   * Sets current value of the climbEncoder as the new "zero"
   */
  public void zeroClimbEnc() {
    climbStartingPoint = climbMotor1.getSelectedSensorPosition(0);
  }

  /**
   * 
   * @return raw encoder value
   */
  public double getClimbEncTicksRaw() {
    return climbMotor1.getSelectedSensorPosition(0);
  }

  public double getClimbEncoderTicks() {
    return climbMotor1.getSelectedSensorPosition(0) - climbStartingPoint;
  }

  /**
   * Convert encoder ticks to angle in degrees
   * @param encoderTicks use getClimbEncoderTicks()
   */
  public double climbEncTicksToAngle (double encoderTicks) {
    return ((encoderTicks * 360) / RobotMap.encoderTicksPerRevolution);
  }

  /**
   * Convert angle in degrees to encoder ticks
   * @param climbAngle in degrees
   */
  public double climbAngleToEncTicks (double climbAngle) {
    return ((climbAngle * RobotMap.encoderTicksPerRevolution) / 360);
  }

  /**
   * @return angle in degrees
   */
  public double getClimbAngle() {
    return climbEncTicksToAngle(getClimbEncoderTicks());
  }

  public void stopClimbMotor() {
    climbMotor1.set(0.0);
  }

  /**
   * @return true = vaccum is at the required pressure
   *          false = vacuum is not at the required pressure yet
   */
  public boolean isVacuumAchieved() {
    return vacuumSwitch.get();
  }

  /**
   * @return true = climb is at its calibration angle
   */
  public boolean getClimbReferenceLimit() {
    return climbLimit.isRevLimitSwitchClosed();
  }

  public void updateClimbLog() {
    Robot.log.writeLog("Climb", "Update Variables", 
    "Climb1 Volts" + climbMotor1.getMotorOutputVoltage() + ",Climb2 Volts," + climbMotor2.getMotorOutputVoltage() + 
    ",ClimbVac1 Volts," + climbVacuum1.getMotorOutputVoltage() + ",ClimbVac2 Volts," + climbVacuum2.getMotorOutputVoltage() +
    ",Climb1 Amps," + Robot.pdp.getCurrent(RobotMap.climbMotor1PDP) + ",Climb2 Amps," + Robot.pdp.getCurrent(RobotMap.climbMotor2PDP) + 
    ",ClimbVac1 Amps," + Robot.pdp.getCurrent(RobotMap.climbVacuum1PDP) + ",ClimbVac2 Amps," + Robot.pdp.getCurrent(RobotMap.climbVacuum2PDP) +
    ",ClimbEnc Ticks," + getClimbEncoderTicks());
  }
  
  @Override
  public void initDefaultCommand() {
    // Set the default command for a subsystem here.
    // setDefaultCommand(new MySpecialCommand());
  }
  @Override
  public void periodic() {
    if (!Robot.robotPrefs.climbCalibrated || Robot.beforeFirstEnable) {
      if (climbLimit.isRevLimitSwitchClosed()) {
        Robot.robotPrefs.setArmCalibration(getClimbEncTicksRaw() - climbAngleToEncTicks(RobotMap.climbStartingAngle), false);
      }
    }
    if (getClimbAngle() > RobotMap.vacuumTargetAngle || getClimbAngle() < RobotMap.climbStartingAngle) {
      Robot.robotPrefs.climbCalibrated = false;
    }
    if (DriverStation.getInstance().isEnabled()) {
      if ((++periodicCount) >= 25) {
        updateClimbLog();
        periodicCount=0;  
      }
    }
  }
}