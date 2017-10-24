package ca.mcgill.ecse211.lab5;

/**
 * Created by Allison Mejia on 20/10/2017
 */

import lejos.robotics.SampleProvider;

public class OdometryCorrection extends Thread{
	
	private static final long CORRECTION_PERIOD = 10;
	static Odometer odometer;
	SampleProvider ColorSensor;
	float[] colorData;
	static double tileSize;
	static double dist2wheel;
	
	public OdometryCorrection(Odometer odo,SampleProvider cs, float[] csData, double tileS, double dist2W){
		this.odometer = odo;
		this.ColorSensor = cs;
		this.colorData = csData;
		this.tileSize = tileS;
		this.dist2wheel = dist2W;
	}
	public void run(){
		while(true){
			
			// Wait to detect line
		   detectLine(this.ColorSensor,this.colorData,50,16,8); 
			 
		}
	}
	
	// Helper method that returns when a line is crossed, n indicates number of values to keep for moving average
	//Created by Christophe 
	
	  private static void detectLine(SampleProvider cs, float[] csData, int samplingFrequency, int threshold, int n){
	    float[] csValues = new float[n];
	    float movingAverage = 0;
	    float lastMovingAverage = 0;
	    float derivative = 0;
	    int counter = 0;
	    
	    double Xo, Yo; //values returned by the odometer
		double lineTreshold = (double) 5/tileSize; //in cm = ~ 4.88 cm
		
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

	        // Correct Odometer value if line is detected
	        if(derivative > lineTreshold){
	        	  //range [0,12]
				   Xo = (double) ( (odometer.getX()-dist2wheel)  / tileSize);
				   Yo = (double) ( (odometer.getY()-dist2wheel) / tileSize);
				   
				   if( Xo%1.0 <= lineTreshold && Yo%1.0 <=lineTreshold ){
					   //do nothing, we crossed a corner
					   
				   }
				   else if( Xo%1.0 <= lineTreshold){
					   //we crossed a vertical line
					   odometer.setX(((Xo - (Xo%1.0)) * tileSize) + dist2wheel );
				   }
				   else if( Yo%1.0 <=lineTreshold){
					   //we crossed a horizonal line
					   odometer.setY(((Yo- (Yo%1.0)) * tileSize) + dist2wheel );
				   }
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