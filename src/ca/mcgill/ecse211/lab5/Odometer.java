package ca.mcgill.ecse211.lab5;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {
  // robot position
  private double x;
  private double newX;
  private double y;
  private double newY;
  private double theta;
  private double newTheta;
  private int leftMotorTachoCount;
  private int rightMotorTachoCount;
  private int lastLeftMotorTachoCount;
  private int lastRightMotorTachoCount;
  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;
  private double wheelRadius;	// Wheel radius in cm
  private double wheelBase;	// Wheel base in cm

  private static final long ODOMETER_PERIOD = 25; /*odometer update period, in ms*/

  private Object lock; /*lock object for mutual exclusion*/

  // default constructor
  public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double wheelRadius, double wheelBase) {
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
    this.x = 0;
    this.y = 0;
    this.theta = 0.0;
    this.leftMotorTachoCount = 0;
    this.rightMotorTachoCount = 0;
    this.lastLeftMotorTachoCount = 0;
    this.lastRightMotorTachoCount = 0;
    this.wheelRadius = wheelRadius;
    this.wheelBase = wheelBase;
    
    lock = new Object();
  }

  // run method (required for Thread)
  public void run() {
    long updateStart, updateEnd;

    this.leftMotor.resetTachoCount();
    this.rightMotor.resetTachoCount();
    this.lastLeftMotorTachoCount = this.leftMotor.getTachoCount();
    this.lastRightMotorTachoCount = this.rightMotor.getTachoCount();
    
    double distL = 0;	// Distance travelled by left wheel
    double distR = 0;	// Distance travelled by right wheel
    double deltaD = 0;	// Total displacement of robot
    double newX = 0;
    double newY = 0;
    double deltaT = 0; // Change in heading
    double newTheta = 0;
    
    while (true) {
      // Update the odometer as shown in the lecture slides
      updateStart = System.currentTimeMillis();

      // Update motor tacho counts
      this.leftMotorTachoCount = this.leftMotor.getTachoCount();
      this.rightMotorTachoCount = this.rightMotor.getTachoCount();
      
      // Calculate distances travelled by right and left wheels
      distL = 3.14159 * wheelRadius * (this.leftMotorTachoCount - this.lastLeftMotorTachoCount)/180;
      distR = 3.14159 * wheelRadius * (this.rightMotorTachoCount - this.lastRightMotorTachoCount)/180;
      
      // Update last moto tacho counts
      this.lastLeftMotorTachoCount = this.leftMotorTachoCount;
      this.lastRightMotorTachoCount = this.rightMotorTachoCount;
      
      // Calculate distance travelled by robot (average of distance travelled by both wheels)
      deltaD = 0.5 * (distL + distR);
            
      // Calculate orientation of robot and convert it to degrees
      deltaT = (180/Math.PI) * (distL-distR)/wheelBase;	// Calculate variation of theta and convert it to degrees

      // Calculate the new value of theta
      newTheta = this.getTheta() + deltaT;
      
      // Adjust the new value of theta so it stays between 0 and 359.9
      if(newTheta > 359.9){
        newTheta -= 359.9;
      }else if(newTheta < 0){
        newTheta += 359.9;
      }
      
      // Calculate the new values of x and y
      newX = this.getX() + deltaD * Math.sin(newTheta * (Math.PI/180));
      newY = this.getY() + deltaD * Math.cos(newTheta * (Math.PI/180));

      synchronized (lock) {
        theta = newTheta;
        x = newX;
        y = newY;
      }

      // this ensures that the odometer only runs once every period
      updateEnd = System.currentTimeMillis();
      if (updateEnd - updateStart < ODOMETER_PERIOD) {
        try {
          Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here because it is not
          // expected that the odometer will be interrupted by
          // another thread
        }
      }
    }
  }

  public void getPosition(double[] position, boolean[] update) {
    // ensure that the values don't change while the odometer is running
    synchronized (lock) {
      if (update[0])
        position[0] = x;
      if (update[1])
        position[1] = y;
      if (update[2])
        position[2] = theta;
    }
  }

  public double getX() {
    double result;

    synchronized (lock) {
      result = x;
    }

    return result;
  }

  public double getY() {
    double result;

    synchronized (lock) {
      result = y;
    }

    return result;
  }

  public double getTheta() {
    double result;

    synchronized (lock) {
      result = theta;
    }

    return result;
  }

  // mutators
  public void setPosition(double[] position, boolean[] update) {
    // ensure that the values don't change while the odometer is running
    synchronized (lock) {
      if (update[0])
        x = position[0];
      if (update[1])
        y = position[1];
      if (update[2])
        theta = position[2];
    }
  }

  public void setX(double x) {
    synchronized (lock) {
      this.x = x;
    }
  }

  public void setY(double y) {
    synchronized (lock) {
      this.y = y;
    }
  }

  public void setTheta(double theta) {
    synchronized (lock) {
      this.theta = theta;
    }
  }

  /**
   * @return the leftMotorTachoCount
   */
  public int getLeftMotorTachoCount() {
    return leftMotorTachoCount;
  }

  /**
   * @param leftMotorTachoCount the leftMotorTachoCount to set
   */
  public void setLeftMotorTachoCount(int leftMotorTachoCount) {
    synchronized (lock) {
      this.leftMotorTachoCount = leftMotorTachoCount;
    }
  }

  /**
   * @return the rightMotorTachoCount
   */
  public int getRightMotorTachoCount() {
    return rightMotorTachoCount;
  }

  /**
   * @param rightMotorTachoCount the rightMotorTachoCount to set
   */
  public void setRightMotorTachoCount(int rightMotorTachoCount) {
    synchronized (lock) {
      this.rightMotorTachoCount = rightMotorTachoCount;
    }
  }
}
