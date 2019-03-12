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
import frc.robot.commands.ElevatorWithXBox;
import frc.robot.utilities.ElevatorProfileGenerator;
import frc.robot.utilities.FileLog;
import frc.robot.utilities.RobotPreferences;
import frc.robot.utilities.Wait;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.SensorCollection;

/**
 * Add your docs here.
 */
public class Elevator extends Subsystem {
	// Put methods for controlling this subsystem
	// here. Call these from Commands.

	private WPI_TalonSRX elevatorMotor1;
	private WPI_TalonSRX elevatorMotor2;
	private SensorCollection elevatorLimits;

	private ElevatorProfileGenerator elevatorProfile;

	// private int posMoveCount = 0; // increments every cycle the elevator moves up
	// private int negMoveCount = 0; // increments every cycle the elevator moves down
	// private double currEnc = 0.0; // current recorded encoder value
	// private double encSnapShot = 0.0; // snapshot of encoder value used to make sure encoder is working
	private int motorFaultCount = 0; // increments every cycle the motor detects an issue
	private boolean elevEncOK = true; // true is encoder working, false is encoder broken
	private boolean elevatorMode; // true is automated (encoder is working and calibrated), false is manual mode

	private long lastTime;
	private double lastVelocity, lastMPVelocity, dt;
	private double initPos, finalPos, lastPos;
	private double prevError, error, intError;

	private double rampRate = 0.3;
	private double kPu = 0.15;  // was 0.25, 0.05
	private double kIu = 0;
	private double kDu = 0;
	private double kPd = 0.05;
	private double kId = 0;
	private double kDd = 0;
	private double kFF = 0.05;  // was 0.1, 0.0
	private int kIz = 0;
	private double kMaxOutput = 0.8;
	private double kMinOutput = -0.6;  // -0.4

	public Elevator() {
		elevatorMotor1 = new WPI_TalonSRX(RobotMap.elevatorMotor1);
		elevatorMotor2 = new WPI_TalonSRX(RobotMap.elevatorMotor2);
		elevatorMotor2.follow(elevatorMotor1);
		elevatorMotor1.setInverted(false);
		elevatorMotor2.setInverted(true);
		elevatorMotor1.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
		elevatorMotor1.setSensorPhase(true);         // Flip direction of sensor reading
		elevatorMotor1.configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen);
		elevatorMotor1.configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen);

		elevatorLimits = elevatorMotor1.getSensorCollection();
		checkAndZeroElevatorEnc();

		// elevatorMotor1.config_kF(0, kFF);
		// elevatorMotor1.config_IntegralZone(0, kIz);
		elevatorMotor1.configClosedloopRamp(rampRate);
		elevatorMotor1.configPeakOutputForward(kMaxOutput);
		elevatorMotor1.configPeakOutputReverse(kMinOutput);

		elevatorMotor1.clearStickyFaults();
		elevatorMotor2.clearStickyFaults();
		elevatorMotor1.setNeutralMode(NeutralMode.Brake);
		elevatorMotor2.setNeutralMode(NeutralMode.Brake);

		// Wait 0.25 seconds before checking the encoder ticks.  The reason is that zeroing the encoder (above)
		// can be delayed up to 50ms for a round trip
		// from the Rio to the Talon and back to the Rio.  So, reading position could give the wrong value if
		// we don't wait (random weird behavior).
		// DO NOT GET RID OF THIS WITHOUT TALKING TO DON OR ROB.
		Wait.waitTime(250);

		if(getElevatorLowerLimit() && getElevatorEncTicks() == 0) {
			elevatorMode = true;
		}
		else {
			elevatorMode = false; // start the elevator in manual mode unless it is properly zeroed
		}

		lastTime = System.currentTimeMillis();
		if (elevatorMode) {
			lastPos = getElevatorPos();			
		} else {
			lastPos = 0;
		}

		lastVelocity = 0.0;
		lastMPVelocity = 0.0;
		finalPos = lastPos;
		elevatorProfile = new ElevatorProfileGenerator(finalPos, finalPos, 0, 0, 0);
		
	}

	/**
	 * @param percentOutput between -1.0 (down) and 1.0 (up)
	 */
	public void setElevatorMotorPercentOutput(double percentOutput) {
		elevatorMotor1.set(ControlMode.PercentOutput, percentOutput);
	}

	/**
	 * Resets the PID. Use this if the calibration changes or the elevator was running in manual mode 
	 * to prevent the elevator from jumping when the PID re-engages.	 
	 * @param gotDisabled whether robot was disabled or not
	 */
	public void resetPID(boolean gotDisabled) {
		if(gotDisabled) {
			lastPos = 0;
		} else {
			lastPos = getElevatorPos();
		}
		lastVelocity = 0.0;
		lastMPVelocity = 0.0;
		startPID(lastPos);
		elevatorProfile.newProfile(lastPos, lastPos, 0, 0, 0);
	}

	/**
	 * Sets target angle for elevator PID.
	 * @param pos in inches
	*/
	public void startPID(double pos) {
		// Prevent elevator from going outside of allowed range  WHY IS THIS COMMENTED OUT?
		// pos = (pos > Robot.robotPrefs.hatchHigh) ? Robot.robotPrefs.hatchHigh : pos;
		// pos = (pos < Robot.robotPrefs.elevatorWristStow) ? Robot.robotPrefs.elevatorWristSafeStow : pos;
		finalPos = pos;
		initPos = getElevatorPos();
		intError = 0;
		prevError = 0;
		error = 0;

		if (!DriverStation.getInstance().isEnabled() || elevatorMode) {
			// We are disabled or under joystick control, so just track the position/height.
			// I.e., we were called from periodic() using startPID(getElevatorPos());
			// TODO log elevator pos
		} else {
			// TODO logging statement
			// Robot.log.writeLog("Arm Start PID,cal Zero," + Robot.robotPrefs.armCalZero + ",initial raw encoder,"
			// 		+ getArmEncRaw() + ",initialAngle," + initAngle + ",Destination Angle," + angle);
		}

		Robot.log.writeLog("Elevator", "Trapezoid", "initPos," + initPos + ",targetPos," + finalPos);

		SmartDashboard.putNumber("ElevatorInitPos", initPos);
		SmartDashboard.putNumber("ElevatorTarget", finalPos);
		if(initPos < finalPos) {
			elevatorProfile.newProfile(finalPos, 50, 100); // was 150, 150; was 50, 100
			Robot.log.writeLog("Elevator", "Trapezoid", "initPos < pos");

		} else {
			elevatorProfile.newProfile(finalPos, 50, 100);  // was 150, 150; was 50, 100
			Robot.log.writeLog("Elevator", "Trapezoid", "initPos > pos");

		}

	}

	/**
	 * only works when encoder is working and elevatorMode is true (in automatic mode)
	 * @param inches target height in inches off the floor
	 */
	public void setElevatorPos(double inches) {
		if (elevEncOK && elevatorMode &&										// Elevator must be calibrated
			  Robot.wrist.getWristAngle() < Robot.robotPrefs.wristKeepOut &&  	// Wrist must not be stowed
			  Robot.wrist.getCurrentWristTarget() < Robot.robotPrefs.wristKeepOut && // Wrist must not be moving to stow
			  ( Robot.wrist.getWristAngle() >= Robot.robotPrefs.wristStraight - 5.0 &&	// wrist must be at least horizontal
				Robot.wrist.getCurrentWristTarget() >= Robot.robotPrefs.wristStraight - 5.0 ||
				inches >= Robot.robotPrefs.groundCargo &&						// Elevator is not going below groundCargo position
				Robot.wrist.getWristAngle() >= Robot.robotPrefs.wristDown - 3.0 &&	     // wrist must be at least wristDown
				Robot.wrist.getCurrentWristTarget() >= Robot.robotPrefs.wristDown - 3.0 )
		 ) {
			elevatorMotor1.set(ControlMode.Position, inchesToEncoderTicks(inches - Robot.robotPrefs.elevatorBottomToFloor));
			Robot.log.writeLog("Elevator", "Position set", "Target," + inches + ",Allowed,Yes,Wrist Angle," +
			   Robot.wrist.getWristAngle() + ",Wrist Target," + Robot.wrist.getCurrentWristTarget());
		} else {
			Robot.log.writeLog("Elevator", "Position set", "Target," + inches + ",Allowed,No,Wrist Angle,"  +
 			  Robot.wrist.getWristAngle() + ",Wrist Target," + Robot.wrist.getCurrentWristTarget());
		}
	}

	/**
	 * Returns the height that elevator is trying to move to in inches from the floor.
	 * Returns hatchHigh if the elevator is in manual mode (not calibrated), in order to engage interlocks.
	 * <p><b>NOTE:</b> This is the target height, not the current height.
	 * If the elevator is in manual control mode, returns the actual elevator position.
	 * @return desired inches of elevator height
	 */
	public double getCurrentElevatorTarget() {
		if (elevatorMode) {
			if (elevatorMotor1.getControlMode() == ControlMode.Position) {
				return encoderTicksToInches(elevatorMotor1.getClosedLoopTarget(0)) + Robot.robotPrefs.elevatorBottomToFloor;
			} else {
				return getElevatorPos();
			}
		} else {
			return Robot.robotPrefs.hatchHigh;   //This could be a problem on next time it is enable it goes to hatchHigh
		}
	}

	/**
	 * @return Current elevator position, in inches from floor.  Returns hatchHigh
	 * if the elevator is in manual mode (not calibrated), in order to engage interlocks.
	 */
	public double getElevatorPos() {
		if (elevatorMode) {
			return encoderTicksToInches(getElevatorEncTicks()) + Robot.robotPrefs.elevatorBottomToFloor;
		} else {
			return Robot.robotPrefs.hatchHigh;   //This could be a problem on next time it is enable it goes to hatchHigh
		}
	}

	/**
	 * stops elevator motors
	 */
	public void stopElevator() {
		setElevatorMotorPercentOutput(0.0);
	}

	/**
	 * only zeros elevator encoder when it is at the zero position (lower limit)
	 */
	public void checkAndZeroElevatorEnc() {
		if (getElevatorLowerLimit()) {
			stopElevator();			// Make sure Talon PID loop won't move the robot to the last set position when we reset the enocder position
			elevatorMotor1.setSelectedSensorPosition(0, 0, 0);
			Robot.log.writeLog("Elevator", "Zero Encoder", "");
		}
	}

	/**
	 * @return raw encoder ticks (based on encoder zero being at zero position)
	 */
	public double getElevatorEncTicks() {
		return elevatorMotor1.getSelectedSensorPosition(0);
	}

	/**
	 * @param encoderTicks in enocder Ticks
	 * @return parameter encoder ticks converted to equivalent inches
	 */
	public double encoderTicksToInches(double encoderTicks) {
		return (encoderTicks / Robot.robotPrefs.encoderTicksPerRevolution) * (Robot.robotPrefs.elevatorGearCircumference * 2);
	}

	/**
	 * @param inches in inches
	 * @return parameter inches converted to equivalent encoder ticks
	 */
	public double inchesToEncoderTicks(double inches) {
		return (inches / (Robot.robotPrefs.elevatorGearCircumference * 2)) * Robot.robotPrefs.encoderTicksPerRevolution;
	}

	/**
	 * reads whether the elevator is at the upper limit
	 */
	public boolean getElevatorUpperLimit() {
		return elevatorLimits.isFwdLimitSwitchClosed();
	}

	/**
	 * reads whether the elevator is at the lower limit
	 */
	public boolean getElevatorLowerLimit() {
		return elevatorLimits.isRevLimitSwitchClosed();
	}

	/**
	 * returns whether encoder is working or not
	 * @return true is encoder working
	 * 			false is encoder broken
	 */
	public boolean getEncOK() {
		return elevEncOK;
	}

	/**
	 * Checks elevator motor currents, records sticky faults if a motor is faulty for more than 5 cycles
	 */
	public void verifyMotors() {
		double amps1 = Robot.pdp.getCurrent(RobotMap.elevatorMotor1PDP);
		double amps2 = Robot.pdp.getCurrent(RobotMap.elevatorMotor2PDP);

		if(motorFaultCount >= 5) {
			Robot.robotPrefs.recordStickyFaults("Elevator");
			motorFaultCount = 0;
		}

		if(amps1 > 8 && amps2 < 3) {
			motorFaultCount++;
		}
		else if(amps2 > 8 && amps1 < 3) {
			motorFaultCount++;
		}
		else {
			motorFaultCount = 0;
		}
	}

	/**
    * Writes information about the subsystem to the filelog
    * @param logWhenDisabled true will log when disabled, false will discard the string
    */
	public void updateElevatorLog(boolean logWhenDisabled) {
		Robot.log.writeLog(logWhenDisabled, "Elevator", "Update Variables",
				"Volts1," + elevatorMotor1.getMotorOutputVoltage() + ",Volts2," + elevatorMotor2.getMotorOutputVoltage() + 
				",Amps1," + Robot.pdp.getCurrent(RobotMap.elevatorMotor1PDP) + ",Amps2," + Robot.pdp.getCurrent(RobotMap.elevatorMotor2PDP) + 
				",Enc Ticks," + getElevatorEncTicks() + ",Enc Inches," + getElevatorPos() + ",Elev Target," + getCurrentElevatorTarget() +
				",Upper Limit," + getElevatorUpperLimit() + ",Lower Limit," + getElevatorLowerLimit() + 
				",Enc OK," + elevEncOK + ",Elev Mode," + elevatorMode);
	}

	@Override
	public void initDefaultCommand() {
		// Set the default command for a subsystem here.
		if (!elevatorMode) {
			setDefaultCommand(new ElevatorWithXBox());
		}
	}

	
	@Override
	public void periodic() {
		
		// Can some of these be eliminated by competition?

		if (Robot.log.getLogRotation() == FileLog.ELEVATOR_CYCLE) {
			SmartDashboard.putBoolean("Elev encOK", elevEncOK);
			SmartDashboard.putBoolean("Elev Mode", elevatorMode); // See below for note on this
			// SmartDashboard.putNumber("EncSnap", encSnapShot);
			// SmartDashboard.putNumber("Enc Now", currEnc);
			SmartDashboard.putNumber("Elev Pos", getElevatorPos());
			SmartDashboard.putNumber("Elev Target", getCurrentElevatorTarget());
			SmartDashboard.putNumber("Elev Ticks", getElevatorEncTicks());
			// SmartDashboard.putNumber("Enc Tick", getElevatorEncTicks());
			SmartDashboard.putBoolean("Elev Lower Limit", getElevatorLowerLimit());
			SmartDashboard.putBoolean("Elev Upper Limit", getElevatorUpperLimit());
		}

		if (Robot.log.getLogRotation() == FileLog.ELEVATOR_CYCLE) {
			updateElevatorLog(false);
		}

		dt = ((double)(System.currentTimeMillis() - lastTime)) / 1000.0;  // Time since the last periodic run
		if (elevatorMode) {
			lastVelocity = (getElevatorPos() - lastPos)/dt;
		} else {
			lastVelocity = 0.0;
		}

		elevatorProfile.updateProfileCalcs();
		lastMPVelocity = elevatorProfile.getCurrentVelocity();
		error = elevatorProfile.getCurrentPosition() - getElevatorPos();
		intError = intError + error * dt;

		double percentPower = kFF;
		if (finalPos - initPos > 0.25) {
			percentPower += kPu * error + ((error - prevError) * kDu) + (kIu * intError);
		} else if(finalPos - initPos < -0.25) {
			percentPower += kPd * error + ((error - prevError) * kDd) + (kId * intError);
		} 
		prevError = error;

		setElevatorMotorPercentOutput(percentPower);

		lastTime = System.currentTimeMillis();
		lastPos = getElevatorPos();

		// Following code changes the frequency of variable logging depending
		// on the set logLevel, Motors are checked every cycle regardless
		if (DriverStation.getInstance().isEnabled()) {

			verifyMotors(); // What is the concrete use for this?  Move to a pit command, instead of live during match?

			
		
			// Following code checks whether the encoder is incrementing in the same direction as the 
			// motor is moving and changes control modes based on state of encoder

			/* All of the code below should be gotten rid of. It doesn't speed anything up in competition - the codriver still has to recognize that the encoders are broken
			and the elevator is stalled. This is just more code to run in periodic() */
			// TO DO: The code below is causing false triggers that causes the elevator to be uncalibrated.
			/*
			currEnc = getElevatorEncTicks();
			if (elevatorMotor1.getMotorOutputVoltage() > 5) {
				if (posMoveCount == 0) {
					encSnapShot = getElevatorEncTicks();
				}
				negMoveCount = 0;
				posMoveCount++;
				if (posMoveCount > 3) {
					elevEncOK = (currEnc - encSnapShot) > 100;
					if (!elevEncOK) {
						Robot.robotPrefs.recordStickyFaults("Elevator Enc");
						setDefaultCommand(new ElevatorWithXBox()); 
						// We can probably ignore the automatic switch to xbox. By the time the codriver realizes, they can just push the joystick button in anyways.
						elevatorMode = false;
					}
					posMoveCount = 0;
				}
			} else if (elevatorMotor1.getMotorOutputVoltage() < -5) {
				if (negMoveCount == 0) {
					encSnapShot = getElevatorEncTicks();
				}
				posMoveCount = 0;
				negMoveCount++;
				if (negMoveCount > 3) {
					elevEncOK = (currEnc - encSnapShot) < -100;
					if (!elevEncOK) {
						Robot.robotPrefs.recordStickyFaults("Elevator Enc");
						setDefaultCommand(new ElevatorWithXBox());
						// We can probably ignore the automatic switch to xbox. By the time the codriver realizes, they can just push the joystick button in anyways.
						elevatorMode = false;
					}
					negMoveCount = 0;
				}
			}
			*/

			// Autocalibrate in the encoder is OK and the elevator is at the lower limit switch
			if (!elevatorMode && elevEncOK && getElevatorLowerLimit()) {
				setDefaultCommand(null);
				elevatorMode = true;
				// posMoveCount = 0;
				// negMoveCount = 0;
			}

		}
		
	}
}