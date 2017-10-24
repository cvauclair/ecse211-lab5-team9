package ca.mcgill.ecse211.lab5;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class ZiplineControl {
  private EV3LargeRegulatedMotor pulleyMotor;
  private Driver driver;
  
  public ZiplineControl(EV3LargeRegulatedMotor pulleyMotor, Driver driver){
    this.pulleyMotor = pulleyMotor;
    this.driver = driver;
  }
  
  public void traverseZipline(){
    // Driver a bit forward to help the pulley get on zipline
    this.driver.setForwardSpeed(150);
    this.driver.forward();
    
    // Note: rotations are negative because motor is built the wrong way
    // Rotate slowly for the first third of the rotations 
    this.pulleyMotor.setSpeed(80);
    this.pulleyMotor.rotate(-2160);
    
    // Rotate quicker for the second third of the rotations
    this.pulleyMotor.setSpeed(120);
    this.pulleyMotor.rotate(-2160);
    
    // Rotate slowly for the last third of the rotations
    this.pulleyMotor.setSpeed(80);
    this.pulleyMotor.rotate(-2160);

    // Move a bit forward to move away from zipline
    this.driver.forward(2,false);
    this.driver.setForwardSpeed(120);  
  }
}
