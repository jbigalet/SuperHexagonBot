package main;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class SuperHexagonWindow {

    public WinDef.HWND hWnd;
    public Rectangle rectangle;
    public Robot robot;
    
    public SuperHexagonWindow() {
        hWnd = User32.INSTANCE.FindWindow("GLUT", "Super Hexagon");
        if(hWnd == null){
            System.out.println("Critical Error: Impossible to find Super Hexagon window.");
            System.exit(1);
        }
        
        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(hWnd, rect);
        rectangle = rect.toRectangle();
        
        User32.INSTANCE.SetForegroundWindow(hWnd);
        
        try {
            robot = new Robot();
        } catch(AWTException ex) {
            System.out.println("Critical Error: Impossible to create the robot.");
            System.exit(1);
        }
        
        robot.keyPress(KeyEvent.VK_ENTER);
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            System.out.println("Critical Error: Impossible put the thread asleep.");
            System.exit(1);
        }
        robot.keyRelease(KeyEvent.VK_ENTER);
    }
    
    public BufferedImage getCapture(){
        return robot.createScreenCapture(rectangle);
    }
    
    public BufferedImage getJNACapture(){
        return JNAScreenShot.getScreenshot(rectangle);
    }
    
}
