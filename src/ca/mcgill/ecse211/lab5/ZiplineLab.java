package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.lab5.Driver;
import ca.mcgill.ecse211.lab5.LightLocalizer;
import ca.mcgill.ecse211.lab5.Odometer;
import ca.mcgill.ecse211.lab5.OdometryDisplay;
import ca.mcgill.ecse211.lab5.UltrasonicLocalizer;

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
  private static int X0 = 0, Y0 = 0, Xc = 0, Yc = 0, SC = 0;

  // Robot's physical constants
  public static final double WHEEL_RADIUS = 2.10;
  public static final double BASE_WIDTH = 12.5;
  
  // Square width of the arena
  public static final double SQUARE_WIDTH = 30.48;
  
  // Orientation of zipline
  public static final int ZIPLINE_ORIENTATION = 90;

  //initialize motors TODO: check new ports 
  private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A")); 

  private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

  private static final EV3LargeRegulatedMotor upMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));

  //initialize sensors TODO: check new ports
  private static final Port usPort = LocalEV3.get().getPort("S1");		
  private static final Port csPort = LocalEV3.get().getPort("S4");	


  public static void main(String[] args){
    int buttonChoice;
    boolean done = false;

    // Ultrasonic sensor initialization
    SensorModes usSensor = new EV3UltrasonicSensor(usPort);
    SampleProvider usDistance = usSensor.getMode("Distance");
    float[] usData = new float[usDistance.sampleSize()];

    // Color sensor initialization
    SensorModes csSensor = new EV3ColorSensor(csPort);
    SampleProvider csLight = csSensor.getMode("Red");
    float[] csData = new float[csLight.sampleSize()];
    
    final TextLCD t = LocalEV3.get().getTextLCD();
    final Odometer odometer = new Odometer(leftMotor, rightMotor, WHEEL_RADIUS, BASE_WIDTH);
    
    OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, t);
    
    Driver driver = new Driver(odometer, leftMotor, rightMotor, WHEEL_RADIUS, BASE_WIDTH);
    
    UltrasonicLocalizer ultrasonicLocalizer = new UltrasonicLocalizer(odometer, driver, usDistance, usData);
    LightLocalizer lightLocalizer = new LightLocalizer(odometer, driver, csLight, csData);
    
    // Start escape thread
    (new Thread() {
      public void run() {
        while (Button.waitForAnyPress() != Button.ID_ESCAPE);
        System.exit(0);
      }
    }).start();
    
    
    // Get required user inputs
    SC = getUserInput(0,3,"SC",t);
    X0 = getUserInput(1,7,"X0",t);
    Y0 = getUserInput(1,7,"Y0",t);
    Xc = getUserInput(1,7,"Xc",t);
    Yc = getUserInput(1,7,"Yc",t);
  
    t.clear();
    t.drawString("SC = " + SC, 0, 0);
    t.drawString("X0 = " + X0, 0, 1);
    t.drawString("Y0 = " + Y0, 0, 2);
    t.drawString("Xc = " + Xc, 0, 3);
    t.drawString("Yc = " + Yc, 0, 4);
  
    Button.waitForAnyPress();
    
    odometer.start();
    odometryDisplay.start();
    
    switch(SC){
      case 0:
        // Robot starts in bottom left corner
        ultrasonicLocalizer.fallingEdge(0);
        
        // Start light localizer
        lightLocalizer.localize(1*SQUARE_WIDTH,1*SQUARE_WIDTH);
        break;
      case 1:
        // Robot starts in bottom left corner
        ultrasonicLocalizer.fallingEdge(270);
        
        // Start light localizer
        lightLocalizer.localize(7*SQUARE_WIDTH,1*SQUARE_WIDTH);
        break;
      case 2:
        // Robot starts in bottom left corner
        ultrasonicLocalizer.fallingEdge(180);
        
        // Start light localizer
        lightLocalizer.localize(7*SQUARE_WIDTH,7*SQUARE_WIDTH);
        break;
      case 3:
        // Robot starts in bottom left corner
        ultrasonicLocalizer.fallingEdge(90);
        
        // Start light localizer
        lightLocalizer.localize(1*SQUARE_WIDTH,7*SQUARE_WIDTH);
        break;
      default:
        break;
    }
    
    
    // Travel to (X0,Y0) and then (Xc,Yc)
    driver.travelTo(X0*SQUARE_WIDTH, Y0*SQUARE_WIDTH);
    driver.travelTo(Xc*SQUARE_WIDTH, Yc*SQUARE_WIDTH);
    
    Button.waitForAnyPress();
    return;
  }

  // Helper method to get an integer user input named varName between minVal and maxVal
  public static int getUserInput(int minVal, int maxVal, String varName, TextLCD lcdDisplay){
    int buttonChoice = 0;
    int value = minVal;
    do{
      // clear the display
      lcdDisplay.clear();

      lcdDisplay.drawString("Enter "+ varName + ":", 0, 0);
      lcdDisplay.drawString("Up/down to set value", 0, 1);
      lcdDisplay.drawString("Enter to confirm", 0, 2);
      lcdDisplay.drawString(varName + " = " + value, 0, 3);

      // Wait for button press
      buttonChoice = Button.waitForAnyPress();

      // Check if value should be incremented or decreased
      if(buttonChoice == Button.ID_UP){
        value++;
        if(value > maxVal){
          value = minVal;
        }
      }else if(buttonChoice == Button.ID_DOWN){
        value--;
        if(value < minVal){
          value = maxVal;
        }
      }
    }while(buttonChoice != Button.ID_ENTER);

    return value;
  }
}
