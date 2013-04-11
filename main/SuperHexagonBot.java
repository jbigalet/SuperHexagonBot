package main;

import java.io.IOException;

public class SuperHexagonBot {

    public static boolean debug = false;
    public static int verbose = 10;
    
    public static void main(String[] args) throws IOException, InterruptedException {
        SuperHexagonWindow superHexagonWindow = new SuperHexagonWindow();
        GameData gameData = new GameData( superHexagonWindow, verbose );

        while(true){
            gameData.update();
        }

    }
    
//    public static double getCursorSpeed(SuperHexagonWindow superHexagonWindow){
//        superHexagonWindow.robot.keyPress(KeyEvent.VK_RIGHT);
//        long startTime = System.currentTimeMillis();
//        
//        boolean isPositionFound = false;
//        long oldTime = System.currentTimeMillis();
//        double lastPosition = 0;
//        
//        long totalTime = 0;
//        double totalPosDif = 0;
//        
//        while( System.currentTimeMillis() - startTime < 1000 ){
//            BufferedImage capture = superHexagonWindow.getJNACapture();
//            GameImage gameImage = new GameImage(capture, );
//            
//            if( gameImage.error )
//                isPositionFound = false;
//            else {
//                
//                if( !isPositionFound ){
//                    isPositionFound = true;
//                    lastPosition = gameImage.position % (1d);
//                    oldTime = System.currentTimeMillis();
//                } else {
//                    
//                    long tmpTime = System.currentTimeMillis();
//                    long timeDif = tmpTime - oldTime;
//                    totalTime += timeDif;
//                    oldTime = tmpTime;
//                    
//                    double tmpPosition = gameImage.position % (1d);
//                    double posDif = ( tmpPosition - lastPosition );
//                    if( posDif < 0 ) posDif += 1d;
//                    totalPosDif += posDif;
//                    lastPosition = tmpPosition;
//                    
//                    System.out.println("Current speed: " + (posDif/timeDif) + " (" + posDif + " in " + timeDif + "ms)");
//                }
//            }
//        }
//        
//        superHexagonWindow.robot.keyRelease(KeyEvent.VK_RIGHT);
//        
//        return totalPosDif/totalTime;
//    }
}
