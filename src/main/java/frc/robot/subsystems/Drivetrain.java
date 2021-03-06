package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import frc.robot.commands.TankDriveWithXbox;
import frc.robot.Robot;
import frc.robot.RobotMap;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableEntry;

public class Drivetrain extends Subsystem {

  private WPI_TalonSRX leftFront = new WPI_TalonSRX(RobotMap.FRONT_LEFT_MOTOR);
  private WPI_TalonSRX leftRear = new WPI_TalonSRX(RobotMap.REAR_LEFT_MOTOR);
  private SpeedControllerGroup leftSide = new SpeedControllerGroup(leftFront, leftRear);

  private WPI_TalonSRX rightFront = new WPI_TalonSRX(RobotMap.FRONT_RIGHT_MOTOR);
  private WPI_TalonSRX rightRear = new WPI_TalonSRX(RobotMap.REAR_RIGHT_MOTOR);
  private SpeedControllerGroup rightSide = new SpeedControllerGroup(rightFront, rightRear);

  private DifferentialDrive drivetrain = new DifferentialDrive(leftSide, rightSide);

  private AnalogGyro gyro = new AnalogGyro(0);
  private Encoder leftEncoder = new Encoder(RobotMap.LEFT_ENCODER_CHANNEL_A, RobotMap.LEFT_ENCODER_CHANNEL_B, false,
      Encoder.EncodingType.k4X);
  private Encoder rightEncoder = new Encoder(RobotMap.RIGHT_ENCODER_CHANNEL_A, RobotMap.RIGHT_ENCODER_CHANNEL_B, false,
      Encoder.EncodingType.k4X);

  private DoubleSolenoid shifter = new DoubleSolenoid(RobotMap.SHIFTER_FORWARD, RobotMap.SHIFTER_REVERSE);

  private static NetworkTable obiWan;
  private static NetworkTableEntry directionStateEntry;

  public Drivetrain() {
    super("Drivetrain");
    SmartDashboard.putString("Drivetrain", "enabled");
    // INITIALIZE
    obiWan = NetworkTableInstance.getDefault().getTable("ObiWan");
    directionStateEntry = obiWan.getEntry("DirectionState");
    leftRear.follow(leftFront);
    rightRear.follow(rightFront);
    shifterBackward();
    // normalCompressor.setClosedLoopControl(true);

    // ENCODERS
    // leftEncoder.setDistancePerPulse(findDistancePerPulse(RobotMap.COUNTS_PER_REVOLUTION));
    // rightEncoder.setDistancePerPulse(findDistancePerPulse(RobotMap.COUNTS_PER_REVOLUTION));
    // resetEncoders();

    //GYRO
    gyro.calibrate();
  }

  public double getGyroAngle() {
     return gyro.getAngle();
  }

  public void tankDrive(double leftSpeed, double rightSpeed) {
    drivetrain.tankDrive(leftSpeed, rightSpeed, true);
  }

  public void setLeftSide(double speed) {
    leftSide.set(speed);
  }

  public void setRightSide(double speed) {
    rightSide.set(speed);
  }

  // public void arcadeDrive(double speed, double rotation) {
  // drivetrain.arcadeDrive(speed, rotation);
  // }

  // ENCODERS
  public void resetEncoders() {
    leftEncoder.reset();
    rightEncoder.reset();
  }

  public double findDistancePerPulse(double coutsPerRevolution) {
    return (Math.PI * RobotMap.WHEEL_DIAMETER) / coutsPerRevolution;
  }

  // !
  public double getEncodersCount() {
    return (leftEncoder.get() + rightEncoder.get() / 2);
  }

  public double getLeftEncoderCount() {
    return leftEncoder.get();
  }

  public double getRightEncoderCount() {
    return rightEncoder.get();
  }

  // public <T extends Number> T average(T[] nums) {
  // T sum = 0;
  // for (T num : nums) {
  // sum = sum + num;
  // }
  // return sum;
  // }

  public double getLeftEncoderDistance() {
    return leftEncoder.getDistance();
  }

  public double getRightEncoderDistance() {
    return rightEncoder.getDistance();
  }

  // returns speed of drivetrain in cm/s
  public double getSpeed() {
    return (leftEncoder.getRate() + rightEncoder.getRate()) / 2;
  }

  // returns average of encoder distances
  public double getEncodersDistance() {
    return (getLeftEncoderDistance() + getRightEncoderDistance() / 2);
  }

  // SHIFTER
  public enum GearShiftState {
    LOW, HIGH;

    private static GearShiftState currentState = GearShiftState.LOW;

    public static GearShiftState get() {
      return currentState;
    }

    public static void set(GearShiftState state) {
      currentState = state;
      SmartDashboard.putString("Gear Shift State", currentState.toString());
    }

    public static boolean check(GearShiftState state) {
      if (GearShiftState.get() == state) {
        return true;
      }
      return false;
    }

  }

  public void shifterForward() {
    shifter.set(Value.kForward);
    GearShiftState.set(GearShiftState.HIGH);
  }

  public void shifterBackward() {
    shifter.set(Value.kReverse);
    GearShiftState.set(GearShiftState.LOW);
  }

  public GearShiftState getShifterValue() {
    return GearShiftState.get();
  }

  // FACE
  public enum DirectionState {
    FORWARD, BACKWARD, INVALID;

    private static DirectionState currentState = DirectionState.FORWARD;

    public static DirectionState get() {
      return currentState;
    }

    public static void set(DirectionState state) {
      currentState = state;
      SmartDashboard.putString("Direction State", currentState.toString());
      directionStateEntry.setString(state.toString());
    }

    public static DirectionState update() {
      DirectionState state;

      if (Robot.drivetrain.getLeftDirection() != Robot.drivetrain.getRightDirection()) {
        state = INVALID;
      } else {
        if (Robot.drivetrain.getLeftDirection() == true) {
          state = BACKWARD;
        } else {
          state = FORWARD;
        }
      }

      set(state);
      return state;
    }

    public static boolean check(DirectionState state) {
      if (DirectionState.get() == state) {
        return true;
      }
      return false;
    }
  }

  public void faceForwards() {
    leftSide.setInverted(false);
    rightSide.setInverted(false);
    DirectionState.update();
  }

  public void faceBackwards() {
    leftSide.setInverted(true);
    rightSide.setInverted(true);
    DirectionState.update();
  }

  public boolean getLeftDirection() {
    return leftSide.getInverted();
  }

  public boolean getRightDirection() {
    return rightSide.getInverted();
  }

  @Override
  protected void initDefaultCommand() {
    setDefaultCommand(new TankDriveWithXbox());
  }

}