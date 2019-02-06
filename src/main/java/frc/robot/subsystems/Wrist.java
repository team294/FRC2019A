/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Robot;
import frc.robot.RobotMap;
import frc.robot.commands.*;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.SensorCollection;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class Wrist extends Subsystem {
  private WPI_TalonSRX wristMotor = new WPI_TalonSRX(RobotMap.wristMotor);
  private SensorCollection wristLimits;

  private int periodicCount = 0; // increments every cycle of periodic
	private int posMoveCount = 0; // increments every cycle the wrist moves up
	private int negMoveCount = 0; // increments every cycle the wrist moves down
	private int idleCount = 0; // increments every cycle the wrist isn't moving
	private double prevEnc = 0.0; // last recorded encoder value
	private double currEnc = 0.0; // current recorded encoder value
	private double encSnapShot = 0.0; // snapshot of encoder value used to make sure encoder is working
  private boolean encOK = true; // true if encoder works, false if encoder broke
  private boolean wristMode = true; // true if automated, false if manual mode

  // TODO test PID terms with actual wrist
  private double kP = 0;
	private double kI = 0;
	private double kD = 0;
  private double kFF = 0;
  private int kIz = 0;
  private double kMaxOutput = 1.0; // up max output
  private double kMinOutput = -1.0; // down max output
  private double rampRate = 0.005;

  public Wrist() {
    wristMotor.set(ControlMode.PercentOutput, 0);
    wristMotor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute, 0, 0);
    wristMotor.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen);
    wristMotor.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen);
    wristMotor.setNeutralMode(NeutralMode.Brake);
    wristMotor.clearStickyFaults();

    wristMotor.config_kP(0, kP);
		wristMotor.config_kI(0, kI);
		wristMotor.config_kD(0, kD);
		wristMotor.config_kF(0, kFF);
		wristMotor.config_IntegralZone(0, kIz);
		wristMotor.configClosedloopRamp(rampRate);
		wristMotor.configPeakOutputForward(kMaxOutput);
    wristMotor.configPeakOutputReverse(kMinOutput);
    
    wristLimits = wristMotor.getSensorCollection();
    zeroWristEncoder();
  }

  /**
   * 
   * @param percentOutput between -1.0 and 1.0
   */
  public void setWristMotorPercentOutput(double percentOutput) {
    wristMotor.set(ControlMode.PercentOutput, percentOutput);
  }

  /**
   * Only works when encoder is working, wristMode is true (in automatic mode)
   * If setting to greater than 60 degrees, elevator position and target
   * must be less than 3 inches above starting position
   * @param degrees target degrees
   */
  public void setWristAngle(double degrees) {
    if (encOK && wristMode && Robot.robotPrefs.wristCalibrated) {
      // TODO determine max degrees and elevator height tolerance
      if (degrees < 60 || (Robot.elevator.getElevatorEncInches() < 3 && Robot.elevator.getCurrentElevatorTarget() < 3)) {
        wristMotor.set(ControlMode.Position, degreesToEncoderTicks(degrees) + Robot.robotPrefs.wristCalZero);
        Robot.log.writeLog("Wrist", "Degrees set", "Target" + degrees);
      }
    }
  }

  /**
   * Stops wrist motor
   */
  public void stopWrist() {
    setWristMotorPercentOutput(0.0);
  }

  /**
   * Only zeros wrist encoder when it is at zero position (lower limit)
   */
  public void zeroWristEncoder() {
    if (getWristLowerLimit()) {
      wristMotor.setSelectedSensorPosition(0, 0, 0);
      Robot.log.writeLog("Wrist", "Zero Encoder", "");
    }
  }

  /**
   * Reads whether wrist is at lower limit
   * @return true if wrist is at lower limit, false if not
   */
  public boolean getWristLowerLimit() {
    return wristLimits.isRevLimitSwitchClosed();
  }

  /**
   * Reads whether wrist is at upper limit
   * @return true if wrist is at upper limit, false if not
   */
  public boolean getWristUpperLimit() {
    return wristLimits.isFwdLimitSwitchClosed();
  }

  /**
   * 
   * @return raw encoder ticks (based on encoder zero being at zero position)
   */
  public double getWristEncoderTicks() {
    return wristMotor.getSelectedSensorPosition(0) - Robot.robotPrefs.wristCalZero;
  }

  /**
   * 
   * @param encoderTicks encoder ticks
   * @return parameter encoder ticks converted to equivalent degrees
   */
  public double encoderTicksToDegrees(double encoderTicks) {
    return encoderTicks * RobotMap.encoderDegreesPerTicks;
  }

  /**
   * 
   * @param degrees angle in degrees
   * @return parameter degrees converted to equivalent encoder ticks
   */
  public double degreesToEncoderTicks(double degrees) {
    return degrees * RobotMap.encoderTicksPerDegrees;
  }

  /**
   * 
   * @return current encoder ticks converted to degrees
   */
  public double getWristEncoderDegrees() {
    return encoderTicksToDegrees(getWristEncoderTicks());
  }

  /**
	 * Returns the angle that wrist is currently positioned at in degrees
	 * 
	 * @return current degree of wrist angle
	 */
  public double getWristAngle() {
    double wristAngle = getWristEncoderDegrees();
    wristAngle = wristAngle % 360; // If encoder wraps around 360 degrees
    wristAngle = (wristAngle > 180) ? wristAngle - 360 : wristAngle; // Change range to -180 to +180
		return wristAngle;
  }

  /**
	 * Returns the angle that wrist is trying to move to in degrees
	 * 
	 * @return desired degree of wrist angle
	 */
  public double getCurrentWristTarget() {
    double currentTarget = wristMotor.getClosedLoopTarget(0) - Robot.robotPrefs.wristCalZero;
		return encoderTicksToDegrees(currentTarget);
  }

  /**
	 * returns whether encoder is working or not
	 * @return true if encoder works, false if encoder broke
	 */
	public boolean getEncOK() {
		return encOK;
  }

  /**
   * Writes information about the subsystem to the filelog
   */
  public void updateWristLog() {
    Robot.log.writeLog("Wrist", "Update Variables",
        ",Wrist Enc Ticks," + getWristEncoderTicks() + ",Wrist Enc Degrees," + getWristEncoderDegrees()
        + ",Upper Limit," + getWristUpperLimit() + ",Lower Limit," + getWristLowerLimit()
        + ",Enc OK," + encOK + ",Wrist Mode," + wristMode);
  }

  @Override
  public void initDefaultCommand() {
    // Set the default command for a subsystem here.
    // setDefaultCommand(new MySpecialCommand());
  }

  @Override
  public void periodic() {
    if (DriverStation.getInstance().isEnabled()) {
			prevEnc = currEnc;
			currEnc = getWristEncoderTicks();
			if (currEnc == prevEnc) {
				idleCount++;
			} else {
				idleCount = 0;
			}
			if (idleCount >= 50) {
				if ((++periodicCount) >= 25) {
					updateWristLog();
					periodicCount = 0;
				}
			} else {
				updateWristLog();
			}
		}

		// Following code checks whether the encoder is incrementing in the same direction as the 
    // motor is moving and changes control modes based on state of encoder
    
		if (wristMotor.getMotorOutputVoltage() > 5) {
			if (posMoveCount == 0) {
				encSnapShot = getWristEncoderTicks();
			}
			negMoveCount = 0;
			posMoveCount++;
			if (posMoveCount > 3) {
				encOK = (currEnc - encSnapShot) > 100;
				if (!encOK) {
					setDefaultCommand(new WristWithXBox());
					wristMode = false;
				}
				posMoveCount = 0;
			}
		}
		if (wristMotor.getMotorOutputVoltage() < -5) {
			if (negMoveCount == 0) {
				encSnapShot = getWristEncoderTicks();
			}
			posMoveCount = 0;
			negMoveCount++;
			if (negMoveCount > 3) {
				encOK = (currEnc - encSnapShot) < -100;
				if (!encOK) {
					setDefaultCommand(new WristWithXBox());
					wristMode = false;
				}
				negMoveCount = 0;
			}
		}
		if (!wristMode && encOK) {
			if (getWristLowerLimit() && getWristEncoderTicks() == 0) {
				setDefaultCommand(null);
				wristMode = true;
				posMoveCount = 0;
				negMoveCount = 0;
			}
		}
  }
}