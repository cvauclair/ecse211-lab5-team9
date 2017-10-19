package ca.mcgill.ecse211.lab5;

/**
 * Lab 5: traversing zip line
 * 
 * This is the main class that displays the initial 
 * user interface for users to input coordinates 
 * and that executes the traversing activity 
 */

//Imports
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;


public class ZiplineLab {
	
	  //User inputs
	  private static int X0 = 0, Y0 = 0, Xc = 0, Yc = 0;
	
	  //robot measurements TODO: measure 
	  public static final double WHEEL_RADIUS = 2.1;
	  public static final double TRACK = 10.15;
	  
	  //initialize motors TODO: check new ports 
	  private static final EV3LargeRegulatedMotor leftMotor =
		      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A")); 
		  
	  private static final EV3LargeRegulatedMotor rightMotor =
		      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	  
	  private static final EV3LargeRegulatedMotor upMotor = 
			  new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	  
	  //initialize sensors TODO: check new ports
	  private static final Port usPort = LocalEV3.get().getPort("S1");		
	  private static final Port colorPort = LocalEV3.get().getPort("S2");	
	  
	  
	public void main(String[] args){
		
		int buttonChoice;
		boolean done = false;
		
		final TextLCD t = LocalEV3.get().getTextLCD();
		
		//Ultrasonic sensor initialization
	    @SuppressWarnings("resource")	
	    SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			
		float[] usData = new float[usValue.sampleSize()];
		
		//Color sensor initialization
		@SuppressWarnings("resource")	
		SensorModes colorSensor = new EV3UltrasonicSensor(colorPort);
		SampleProvider colorValue = usSensor.getMode("Red");			
		float[] colorData = new float[usValue.sampleSize()];
		
		//set up user interface to enter X0
		do{
			// clear the display
		      t.clear();
		      
		      t.drawString("Enter X0 ", 0, 0);
		      t.drawString("         ", 0, 1);
		      t.drawString("press up button to start", 0, 1);
		      
		      
			 buttonChoice = Button.waitForAnyPress();
		}while(buttonChoice != Button.ID_UP);
		
		if (buttonChoice == Button.ID_UP) {
			X0 = setUserInputs(X0, t, buttonChoice);
			//for debug purposes REMOVE LATER
			System.out.println(X0);
		}
		
		//Enter Y0
		do{
			// clear the display
		    t.clear();
				      
		    t.drawString("Enter Y0 ", 0, 0);
		    t.drawString("         ", 0, 1);
			t.drawString("press up button to start", 0, 1);
				      
				      
		    buttonChoice = Button.waitForAnyPress();
			}while(buttonChoice != Button.ID_UP);
				
		if (buttonChoice == Button.ID_UP) {
			Y0 = setUserInputs(Y0, t, buttonChoice);
			//for debug purposes REMOVE LATER
			System.out.println(Y0);
		}
		
		//Enter Xc
		do{
			// clear the display
		    t.clear();
						      
			t.drawString("Enter Xc ", 0, 0);
			t.drawString("         ", 0, 1);
			t.drawString("press left button to start", 0, 1);
						      
						      
			buttonChoice = Button.waitForAnyPress();
			}while(buttonChoice != Button.ID_LEFT);
						
		if (buttonChoice == Button.ID_LEFT) {
			Xc = setUserInputs(Xc, t, buttonChoice);
			//for debug purposes REMOVE LATER
			System.out.println(Xc);
		}
		
		//Enter Yc
		do{
			// clear the display
			t.clear();
								      
			t.drawString("Enter Yc ", 0, 0);
			t.drawString("         ", 0, 1);
			t.drawString("press left button to start", 0, 1);
								      
								      
			buttonChoice = Button.waitForAnyPress();
			}while(buttonChoice != Button.ID_LEFT);
								
		if (buttonChoice == Button.ID_LEFT) {
			Yc = setUserInputs(Yc, t, buttonChoice);
			//for debug purposes REMOVE LATER
			System.out.println(Yc);
		}
					
				
		
		
	}
	
	//helper display function for user input
	
	public static int setUserInputs(int variable, TextLCD t, int buttonChoice){
		
		do{
			// clear the display
		      t.clear();
		      
		      t.drawString("Enter input:              ", 0, 0);
		      t.drawString("To increment, press up    ", 0, 1);
		      t.drawString("To decrement, press down  ", 0, 1);
		      t.drawString("When finished, press right", 0, 1);
		      
			 buttonChoice = Button.waitForAnyPress();
		}while(buttonChoice != Button.ID_UP && buttonChoice != Button.ID_DOWN);
		
		//exit if button pressed is the right button
		while( buttonChoice != Button.ID_RIGHT){
			if (buttonChoice == Button.ID_UP) {
				variable++;
				t.clear();
				t.drawString("Value: "+ variable, 0, 0);
				buttonChoice = Button.waitForAnyPress();
			}
			else if (buttonChoice == Button.ID_DOWN) {
				variable--;
				t.clear();
				t.drawString("Value: "+ variable, 0, 0);
				buttonChoice = Button.waitForAnyPress();
			}	
		}
		return variable; //button right was pressed, set final value	
	}
	
	
}
