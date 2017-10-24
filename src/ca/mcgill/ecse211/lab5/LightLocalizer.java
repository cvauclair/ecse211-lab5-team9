package ca.mcgill.ecse211.lab5;

import lejos.robotics.SampleProvider;

public class LightLocalizer {
  private Odometer odometer;
  private Driver driver;
  private SampleProvider cs;
  private float[] csData;
  enum LineOrientation {Horizontal, Vertical};
  
  public LightLocalizer(Odometer odometer, Driver driver, SampleProvider cs, float[] csData){
    this.odometer = odometer;
    this.driver = driver;
    this.cs = cs;
    this.csData = csData;
  }
  
  // Localize the robot to (x0,y0) (in cm) using a "soft-hard-coded" technique
  public void localize(double x0, double y0){
    // Slow down robot so that it does not miss the line
    this.driver.setForwardSpeed(100);
    
    // Correct the robot's odometer's Y position by finding a horizontal line
    lineLocalization(LineOrientation.Horizontal);
    
    // Get away from the last line so the robot does not detect it again
    this.driver.forward(2,false);
    
    // Turn robot so that the next line the robot crosses will be a vertical line
    this.driver.turnBy(45,false);
    
    // Correct the robot's odometer's X position by finding a vertical line
    lineLocalization(LineOrientation.Vertical);
    
    // Once the odometer's position is correctly set, travel to (x0,y0) and orient the robot correctly
    this.driver.travelTo(0,0);
    this.driver.turnTo(0);
    this.odometer.setX(x0);
    this.odometer.setY(y0);
  }
  
  // Method that sets the odometer's Y position by detecting a horizontal line (horizontalLine = true) or
  // its X position by detecting a vertical line (horizontalLine = false). The odometer's Y value (or 
  // X value depending on horizaontalLine) is set to initialValue
  private void lineLocalization(LineOrientation lineOrientation){    
    // Set robot to driver forward 
    this.driver.forward();
    
    // Wait to detect line
    detectLine(this.cs,this.csData,50,16,8);
    
    // Stop robot
    this.driver.stop();
    
    // Correct the odometer's Y or X value depending on whether the crossed line is vertical or horizontal
    if(lineOrientation == LineOrientation.Horizontal){
      this.odometer.setY(0);
    }else if(lineOrientation == LineOrientation.Vertical){
      this.odometer.setX(0);
    }
  }
  
  // Helper method that returns when a line is crossed, n indicates number of values to keep for moving average
  private static void detectLine(SampleProvider cs, float[] csData, int samplingFrequency, int threshold, int n){
    float[] csValues = new float[n];
    float movingAverage = 0;
    float lastMovingAverage = 0;
    float derivative = 0;
    int counter = 0;
    
    while(true){
      cs.fetchSample(csData, 0);
      
      // Shift values and add new value
      for(int i = n-1; i > 0; i--){
          csValues[i] = csValues[i-1];
      }
      csValues[0] = csData[0] * 1000;
      
      // Increment counter
      counter++;
      
      // Compute moving average and derivative only if first n values have been measured
      if(counter >= n){ 
        // If first time moving average is computed
        if(lastMovingAverage == 0){
            lastMovingAverage = csValues[n-1];
        }

        // Calculate the moving average
        movingAverage = lastMovingAverage + (csValues[0] - csValues[n-1])/n;

        // Calculate poor man's derivative
        derivative = movingAverage - lastMovingAverage;

        // Exit loop and method if line is detected
        if(derivative > threshold){
          return;
        }
      }
      
      try {
        Thread.sleep(samplingFrequency);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
