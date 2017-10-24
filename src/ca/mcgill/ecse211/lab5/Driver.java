package ca.mcgill.ecse211.lab5;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/*
 * This class is used to move the robot and includes various methods to make controlling the wheel motors easier
 * */

public class Driver {
  private Odometer odometer;
  private EV3LargeRegulatedMotor leftMotor;
  private EV3LargeRegulatedMotor rightMotor;
  private double wheelRadius;
  private double baseWidth;
  private int forwardSpeed;
  private int rotateSpeed;
  
  public Driver(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double wheelRadius, double baseWidth){
    this.odometer = odometer;
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
    this.wheelRadius = wheelRadius;
    this.baseWidth = baseWidth;
    this.forwardSpeed = 250;    // Default value
    this.rotateSpeed = 150;     // Default value
  }
  
  // Method to move the robot forward indefinitely
  public void forward(){
    // Set forward speed
    this.setSpeed(this.forwardSpeed);
    
    // Move forward
    this.leftMotor.forward();
    this.rightMotor.forward();
  }
  
  // Method to stop both motors
  public void stop(){
    // Stop motors
    this.leftMotor.stop(true);
    this.rightMotor.stop(false);
  }
  
  // Method to move the robot to the desired point (in cm)
  public void travelTo(double x, double y){
    double deltaX = x - this.odometer.getX();
    double deltaY = y - this.odometer.getY();
    double deltaD = Math.sqrt(Math.pow(deltaX,2) + Math.pow(deltaY,2));

//    System.out.println("X=" + odometer.getX() + " Y=" + odometer.getY());
//    System.out.println("deltaX=" + deltaX + " deltaY=" + deltaY + " deltaD=" + deltaD);
    
    // Determine target angle
    double theta = 0;
    if(deltaX >= 0 & deltaY >= 0){
        theta = (180/Math.PI) * Math.atan(deltaX/deltaY);
    }else if(deltaX >= 0 & deltaY < 0){
        theta = 90 + (180/Math.PI) * -Math.atan(deltaY/deltaX);
    }else if(deltaX < 0 & deltaY < 0){
        theta = 180 + (180/Math.PI) * Math.atan(deltaX/deltaY);
    }else{
        theta = 270 + (180/Math.PI) * -Math.atan(deltaY/deltaX);
    }
    
    // Turn theta orientation
    this.turnTo(theta);
    
    // Move forward by deltaD cm
    this.forward(deltaD,false);
  }
  
  // Method to move the robot forward by 'distance' cm
  public void forward(double distance, boolean immediateReturn){
    // Set forward speed
    this.setSpeed(this.forwardSpeed);
    
    // Move forward
    this.leftMotor.rotate(convertDistance(this.wheelRadius, distance), true);
    this.rightMotor.rotate(convertDistance(this.wheelRadius, distance), immediateReturn);
  }
  
  // Method to turn the robot to an orientation of theta degrees
  public void turnTo(double theta){
    double currentTheta = odometer.getTheta();
    double deltaT = 0;
    
    // Check which direction is the change in angle the smallest
    deltaT = theta - currentTheta;
    if(deltaT > 180){
      deltaT = deltaT-360;
    }
    
    this.turnBy(deltaT,false);
  }
  
  // Method to turn the robot by theta degrees
  public void turnBy(double theta, boolean immediateReturn){
    // Set rotate speed
    this.setSpeed(this.rotateSpeed);
    
    // Rotate the robot
    this.leftMotor.rotate(convertAngle(this.wheelRadius, this.baseWidth, theta), true);
    this.rightMotor.rotate(-convertAngle(this.wheelRadius, this.baseWidth, theta), immediateReturn);
  }
  
  public EV3LargeRegulatedMotor getLeftMotor(){
    return this.leftMotor;
  }
  
  public EV3LargeRegulatedMotor getRightMotor(){
    return this.rightMotor;
  }
  
  // Speed related quality of life helper methods
  public void setForwardSpeed(int forwardSpeed){
    this.forwardSpeed = forwardSpeed;
  }
  
  public void setRotateSpeed(int rotateSpeed){
    this.rotateSpeed = rotateSpeed;
  }
  
  public void setSpeed(int speed){
    this.leftMotor.setSpeed(speed);
    this.rightMotor.setSpeed(speed);
  }
  
  private static int convertDistance(double radius, double distance) {
    // Wheel rotation in degrees = 360 * distance/circumference
    return (int) ((180.0 * distance) / (Math.PI * radius));
  }

  private static int convertAngle(double radius, double width, double angle) {
    // Distance each wheel needs to travel = circumference * angle/360 
    // (ie: each wheel needs to move by arc length of robot rotation)
    return convertDistance(radius, Math.PI * width * angle / 360.0);
  }
}
