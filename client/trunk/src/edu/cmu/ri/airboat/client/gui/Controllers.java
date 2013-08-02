
//shantanu vyas
// this class is for controlling the thrust and rudder in 2 ways (so far)
// the first way will be using the arrow keys (up and down for the thrust and left and right for the rudder)
// the second way will be using a controller (ps3)
package edu.cmu.ri.airboat.client.gui;
import net.java.games.input.*;


/**
 *
 * @author shantanu
 */
public class Controllers {
    static int keyCode; //var for the current key code
    static DrivePanel _panel; //which frame to add the object too
  
    // keyboard codes for the arrow keys 
    static final int UP = 38;
    static final int DOWN = 40;
    static final int LEFT = 37;
    static final int RIGHT = 39;
    
    static boolean inBox;
    
     private static Controller Joystick;
	static Component triangle; 
	static Component circle;  
	static Component xButton; 
	static Component square; 

	static Component xAxis; 
	static Component yAxis; 
	static Component ZAxis; 
	static Component rzAxis; 
	
	static Component rightTrigger;
	static Component leftTrigger;
        
        static Component leftBumper;
        static Component rightBumper;
        
        static Component dUP;
        static Component dDOWN;
        static Component dLEFT;
        static Component dRIGHT;
        
        static Component select;
        static Component start;
        static Component ps3Button;

        
    public Controllers(DrivePanel panel){
        _panel = panel;
	
    }
    
    //keyboard stuff
    public static boolean inBox() {
        if (inBox == true) {
            return true;
        }
        else{
            return false;
        }
    }
    public static boolean UP_KEY() {
	if (keyCode == UP) {
	    return true;
	}
	else {
	    return false;
	}
    }
    public static boolean DOWN_KEY() {
	if (keyCode == DOWN) {
	    return true;
	}
	else{
	    return false;
	}
    }
    public static boolean LEFT_KEY() {
	if (keyCode == LEFT) {
	    return true;
	}
	else{
	    return false;
	}
    }
    public static boolean RIGHT_KEY() {
	if (keyCode == RIGHT) {
	    return true;
	}
	else{
	    return false;
	}
    }
    
    public static boolean isControllerConnected()
	{
		try {
			return Joystick.poll();
	        	} catch (Exception e) {
	        		return false;
	        }	
	    }	
	public static void main(String[] args)
        {
            
           
		Joystick = null;
		for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
			if (c.getType()== Controller.Type.STICK)
			{
                            System.out.println("controller connected");
				Joystick = c;
				//System.out.println(Joystick.getName());
			}
		}
		if (Joystick == null)
		{
			System.err.println("No Joystick Found");
		}
		if (Joystick != null)
		{
			for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
				System.out.println(c.getName());
			}

			triangle = Joystick.getComponent(Component.Identifier.Button._12);
			circle = Joystick.getComponent(Component.Identifier.Button._13);
			xButton = Joystick.getComponent(Component.Identifier.Button._14);
			square = Joystick.getComponent(Component.Identifier.Button._15);
		
			xAxis = Joystick.getComponent(Component.Identifier.Axis.X); //x axis for first joy stick (left)
			yAxis = Joystick.getComponent(Component.Identifier.Axis.Y); //y axis for second joy stick (left)
			ZAxis = Joystick.getComponent(Component.Identifier.Axis.Z); //x axis for second joystick (right)
			rzAxis = Joystick.getComponent(Component.Identifier.Axis.RZ); //y axis for second joystick (right)
			
			rightTrigger = Joystick.getComponent(Component.Identifier.Button._9); //right trigger
			leftTrigger = Joystick.getComponent(Component.Identifier.Button._8);
                        
                        while(true)
			{
				if (isControllerConnected())
				{
					System.out.println(isLeftTriggerPressed());
					//	System.out.println("Triangle: " + isTrianglePressed() + " Circle: " + isCirclePressed() + " xButton: " + isxButtonPressed() + " Square: " + isSquarePressed());
			//		System.out.println("1X: " + returnJ1X() + " 1Y: " + returnJ1Y() + " 2X: " + returnJ2X() + " 2Y: " + returnJ2Y());
				Joystick.poll();
				}
				else {
					System.out.println("Controller Disconnected");
					break;
				}
			}	
		}
	}	
	public static void init() {
		Joystick = null;
		for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
			if (c.getType()== Controller.Type.STICK)
			{
				Joystick = c;
			//	System.out.println(Joystick.getName());
			}
		}
		if (Joystick == null)
		{
			System.err.println("No Joystick Found");
		}
		if (Joystick != null)
		{
//                    if ("PLAYSTATION(R)3 Controller".equals(Joystick.getType().toString()))
                    {
                
			triangle = Joystick.getComponent(Component.Identifier.Button._12);
			circle = Joystick.getComponent(Component.Identifier.Button._13);
			xButton = Joystick.getComponent(Component.Identifier.Button._14);
			square = Joystick.getComponent(Component.Identifier.Button._15);
		
			xAxis = Joystick.getComponent(Component.Identifier.Axis.X); //x axis for first joy stick (left)
			yAxis = Joystick.getComponent(Component.Identifier.Axis.Y); //y axis for second joy stick (left)
			ZAxis = Joystick.getComponent(Component.Identifier.Axis.Z); //x axis for second joystick (right)
			rzAxis = Joystick.getComponent(Component.Identifier.Axis.RZ); //y axis for second joystick (right)
                        
			rightTrigger = Joystick.getComponent(Component.Identifier.Button._9); //right trigger
			leftTrigger = Joystick.getComponent(Component.Identifier.Button._8); //left trigger
                        
                        leftBumper = Joystick.getComponent(Component.Identifier.Button._10);
                        rightBumper = Joystick.getComponent(Component.Identifier.Button._11);
                        
                        dUP = Joystick.getComponent(Component.Identifier.Button._4);
                        dRIGHT = Joystick.getComponent(Component.Identifier.Button._5);
                        dDOWN = Joystick.getComponent(Component.Identifier.Button._6);
                        dLEFT = Joystick.getComponent(Component.Identifier.Button._7);
                        
                        select = Joystick.getComponent(Component.Identifier.Button._0);
                        start = Joystick.getComponent(Component.Identifier.Button._3);
                        ps3Button = Joystick.getComponent(Component.Identifier.Button._16);
                }
                    
                    // else
                    //{
                    //      System.out.println("Unsupported Controller Type, contact us for request to make your controller type work");
                    //} 
		}
	}
		public static void loop() {
		if (isControllerConnected())
		{
                    //	System.out.println("Triangle: " + isTrianglePressed() + " Circle: " + isCirclePressed() + " xButton: " + isxButtonPressed() + " Square: " + isSquarePressed());
                    //		System.out.println("1X: " + returnJ1X() + " 1Y: " + returnJ1Y() + " 2X: " + returnJ2X() + " 2Y: " + returnJ2Y());
                    Joystick.poll(); 
                }
		else 
		{
			System.out.println("Controller Disconnected");
		}
	}
                
	public static boolean isTrianglePressed() //checks to see if triangle is pressed
	{
		if (triangle.getPollData() != 0.0){return true;}
		else {return false;}
	}
	public static boolean isCirclePressed() //checks to see if circle is pressed
	{
		if (circle.getPollData() != 0.0){return true;}
		else {return false;}
	}
	public static boolean isxButtonPressed() //checks to see if x is pressed
	{
		if (xButton.getPollData() != 0.0){return true;}
		else {return false;}
	}
	public static boolean isSquarePressed() //checks to see if square is pressed
	{
		if (square.getPollData() != 0.0){return true;}
		else {return false;}
        }
        
        //triggers
        public static boolean isRightTriggerPressed()
        {
            if (rightTrigger.getPollData() != 0.0) {return true;}
                else {return false;}
        }
        public static boolean isLeftTriggerPressed()
        {
            if (leftTrigger.getPollData() != 0.0) {return true;}
                else {return false;}
        }
        
        
        //dpad controls
        public static boolean isDupPressed()
                {
            if (dUP.getPollData() != 0.0) {return true;}
            else {return false;}
	}
                
    	public static boolean isDrightPressed()
        {
            if (dRIGHT.getPollData() != 0.0) {return true;}
            else {return false;}
        }
        public static boolean isDdownPressed()
        {
            if (dDOWN.getPollData() != 0.0) {return true;}
            else {return false;}
        }
        public static boolean isDleftPressed()
	{
            if (dLEFT.getPollData() != 0.0) {return true;}
            else {return false;}
        }
        
        //center buttons
        public static boolean isSelectPressed()
        {
            if(select.getPollData() != 0.0) {return true;}
            else {return false;}
        }
        
        public static boolean isStartPressed()
        {
            if(start.getPollData() != 0.0) {return true;}
            else {return false;}
        }
        
        public static boolean isPS3ButtonPressed()
        {
            if(ps3Button.getPollData() != 0.0) {return true;}
            else {return false;}
        }
        
        //bumpers
        public static boolean isLeftBumperPressed()
        {
            if(leftBumper.getPollData() != 0.0) {return true;}
            else {return false;}
        }
        public static boolean isRightBumperPressed()
        {
     if(rightBumper.getPollData() != 0.0) {return true;}
     
            else {return false;}
        }
        
        
        // joystick
	public static double returnJ1X() //returns a double with the value of the X-Axis on the left joystick
	{
		return xAxis.getPollData();
	}
	public static double returnJ1Y() //returns a double with the value of the Y-Axis on the left joystick
	{
		return yAxis.getPollData();
	}
	public static double returnJ2X() //returns a double with the value of the X-Axis on the right joystick
	{
		return ZAxis.getPollData();
	}
	public static double returnJ2Y() //returns a double with the value of the Y-Axis the on right joystick
	{
		return rzAxis.getPollData();
	}
   }
