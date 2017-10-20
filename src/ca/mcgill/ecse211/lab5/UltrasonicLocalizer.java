package ca.mcgill.ecse211.lab5;

import lejos.robotics.SampleProvider;

public class UltrasonicLocalizer {
  private Odometer odometer;
  private Driver driver;
  private SampleProvider us;
  private float[] usData;
  
  private static final int ANGLE_INTERVAL = 8;    // Angle intervals at which samples should be taken
  private static final int EDGE_VALUE = 30;
  private static final int NOISE_MARGIN = 2;
  
  public UltrasonicLocalizer(Odometer odometer, Driver driver, SampleProvider us, float[] usData){
    this.odometer = odometer;
    this.driver = driver;
    this.us = us;
    this.usData = usData;
  }
  
  public void fallingEdge(int initialOrientation){
    double backWallAngle = 0;
    double leftWallAngle = 0;
    double theta = 0;
    
    // Reduce rotating speed of robot to minimize wheel sliding when turning
    driver.setRotateSpeed(100);
    
    // Find back wall falling edge
    backWallAngle = findFallingEdge(true);
    
    // Return to starting orientation
    driver.turnTo(0);

    // Find left wall falling edge
    leftWallAngle = findFallingEdge(false);

    // Return to starting position
    driver.turnTo(0);
    
    // Correct the odometer's theta value depending on angle results (adjustment values of 215 and 205 gotten through experimentation)
    if(360-leftWallAngle > backWallAngle){
      this.odometer.setTheta(200-(backWallAngle+leftWallAngle)/2);
    }else{
      this.odometer.setTheta(220-(backWallAngle+leftWallAngle)/2);
    }
    // Orient the robot correctly
    driver.turnTo(0);
    
    // Set the actual theta passed by argument by the caller
    this.odometer.setTheta(initialOrientation);
  }
  
  public void risingEdge(int initialOrientation){
    double backWallAngle = 0;
    double leftWallAngle = 0;
    
    // Reduce rotating speed of robot to minimize wheel sliding when turning
    driver.setRotateSpeed(100);
    
    // Find left wall falling edge
    leftWallAngle = findRisingEdge(true);
    
    // Return to starting orientation
    driver.turnTo(0);

    // Find back wall falling edge
    backWallAngle = findRisingEdge(false);
    
    // Return to starting orientation
    driver.turnBy(360 - this.odometer.getTheta(), false);
    
    // Correct the odometer's theta value depending on angle results (adjustment values of 35 and 55 gotten through experimentation)
    if(360-backWallAngle > leftWallAngle){
      this.odometer.setTheta(55-(backWallAngle+leftWallAngle)/2);
    }else{
      this.odometer.setTheta(35-(backWallAngle+leftWallAngle)/2);
    }
    
    // Orient the robot correctly
    driver.turnTo(0);

    // Set the actual theta passed by argument by the caller
    this.odometer.setTheta(initialOrientation);
  }
  
  //Return the angle at which the falling edge was located
  private double findFallingEdge(boolean clockwise){
    boolean inNoiseMargin = false;  // Flag indicating if data values are in the noise margin
    double enteringAngle = 0;  // Angle at which data values enter the noise margin
    double exitingAngle = 0;   // Angle at which data values leave the noise margin
    float currentValue = 0;
    
    // Start turning, do not wait for the motors to finish
    if(clockwise){
      this.driver.turnBy(360,true);  
    }else{
      this.driver.turnBy(-360,true);
    }
    
    while(true){
      // Take the average of four measurements to reduce uncertainty
      currentValue = averageSensorValue(us,usData,4);
      if(currentValue < EDGE_VALUE + NOISE_MARGIN){
        if(!inNoiseMargin){
          // If the current data value was previously outside the noise margin, set the entering angle
          enteringAngle = this.odometer.getTheta();
        }
        inNoiseMargin = true;
      }else{
        // If the values leave the noise margin without going all the way through, set flag to false
        inNoiseMargin = false;
      }
      
      if(inNoiseMargin && currentValue < EDGE_VALUE - NOISE_MARGIN){
        // If current data has passed through the noise margin, set the exiting point and exit the loop
        exitingAngle = this.odometer.getTheta();
        break;
      }
    }
    
    // Stop rotating
    this.driver.stop();
    
    return (enteringAngle + exitingAngle)/2.0;
  }
  
  //Return the angle at which the falling edge was located
  private double findRisingEdge(boolean clockwise){
    boolean inNoiseMargin = false;  // Flag indicating if data values are in the noise margin
    double enteringAngle = 0;  // Angle at which data values enter the noise margin
    double exitingAngle = 0;   // Angle at which data values leave the noise margin
    float currentValue = 0;
    
    // Start turning, do not wait for the motors to finish
    if(clockwise){
      this.driver.turnBy(360,true);  
    }else{
      this.driver.turnBy(-360,true);
    }
    
    // Detect the rising edge
    while(true){
      // Take the average of four measurements to reduce uncertainty
      currentValue = averageSensorValue(us,usData,4);
      if(currentValue > EDGE_VALUE - NOISE_MARGIN){
        if(!inNoiseMargin){
          // If the current data value was previously outside the noise margin, set the entering angle
          enteringAngle = this.odometer.getTheta();
        }
        inNoiseMargin = true;
      }else{
        // If the values leave the noise margin without going all the way through, set flag to false
        inNoiseMargin = false;
      }
      
      if(inNoiseMargin && currentValue > EDGE_VALUE + NOISE_MARGIN){
        // If current data has passed through the noise margin, set the exiting point and exit the loop
        exitingAngle = this.odometer.getTheta();
        break;
      }
    }
    
    // Stop rotating
    this.driver.stop();
    
    return (enteringAngle + exitingAngle)/2.0;
  }
  
  // Helper method that reads the values of a sensor n times and computes the average
  private static float averageSensorValue(SampleProvider sensor, float[] data, int n){
    float total = 0;
    for(int i = 0; i < n; i++){
      sensor.fetchSample(data,0);
      // Cap the values at 200 to not get ridiculously large values
      total += (data[0]*100 > 200 ? 200 : data[0]*100);
    }
    return total/n;
  }
}
