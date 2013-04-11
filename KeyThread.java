import java.awt.Robot;
import java.awt.event.KeyEvent;

public class KeyThread extends Thread {
    int key;
    Robot robot;
    
    public KeyThread(int key, Robot robot){
        this.key = key;
        this.robot = robot;
    }
    
    @Override
    public void run(){
        robot.keyRelease(KeyEvent.VK_RIGHT);
        robot.keyRelease(KeyEvent.VK_LEFT);
        int k = (key > 0) ? KeyEvent.VK_RIGHT : KeyEvent.VK_LEFT;
        robot.keyPress(k);
        long timeLimit = System.currentTimeMillis() + Math.abs(key);
        while(System.currentTimeMillis() < timeLimit){}
        robot.keyRelease(k);
    }
    
}
