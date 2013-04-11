package main;

import static main.Utility.*;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameData {
    private static final int maxWallDistBetweenTwoFrames = 30;
    
    private Debug debug;
    
    private SuperHexagonWindow superHexagonWindow;
    public int verbose;
    
    public long timeToTakeScreenshot;
    public long timeToProceedImage;
    
    public int iFrame;
    public GameImage currentGameImage;
    public GameImage oldGameImage;
    private long timePosition;  // Time position of the previous frame
    public long currentTimeDif; // Time between previous & current frame
    
    public boolean freshUpdatedSpeeds; // No error in this & previous frame
    public int worldRotation;
    public double cursorSpeed;
    
    public int directionToGo;
    
    private List<Wall>[] currentWallSpeeds;

    public GameData(SuperHexagonWindow superHexagonWindow, int verbose) {
        
        this.debug = new Debug();
        
        this.superHexagonWindow = superHexagonWindow;
        this.verbose = verbose;
        this.iFrame = 0;
    }
    
    public void update(){
        if(verbose > 0) System.out.println("Starting frame [" + iFrame + "] processing");

        long startTime = System.currentTimeMillis();
        currentTimeDif = startTime - timePosition;
        timePosition = startTime;
        
        BufferedImage capture = superHexagonWindow.getJNACapture();
        
        long afterScreenShot = System.currentTimeMillis();
        timeToTakeScreenshot = (afterScreenShot - startTime);
        if(verbose > 0) System.out.println("Screenshot took in: " + timeToTakeScreenshot + "ms");
        
        this.currentGameImage = new GameImage(capture, verbose);

        timeToProceedImage = (System.currentTimeMillis() - afterScreenShot);
        if(verbose > 0) System.out.println("Image processed in: " + timeToProceedImage + "ms");

        
        freshUpdatedSpeeds = !currentGameImage.error && !oldGameImage.error;
        if(freshUpdatedSpeeds){
            worldRotation = getWorldRotation();
            if( verbose > 1 ) System.out.println("New world rotation: " + worldRotation);
            
            currentWallSpeeds = getWallSpeeds();
            if(verbose > 2 ){
                System.out.println("Walls: ");
                for(int i=0 ; i<6 ; i++)
                    for(Wall wall: currentWallSpeeds[i]){
                        System.out.print("[" + i + "] " + wall.startPos + " (" + wall.width + ")");
                        if( wall.isSpeedDefined )
                            System.out.println(" - Speed: " + wall.speed);
                        else
                            System.out.println();
                    }
            }
        }
        
        superHexagonWindow.robot.keyRelease(KeyEvent.VK_LEFT);
        superHexagonWindow.robot.keyRelease(KeyEvent.VK_RIGHT);

        directionToGo = 99;
        if(!currentGameImage.error){
            directionToGo = DecisionBot.getDirectionToGo(currentGameImage);
            if(directionToGo == -1)
                superHexagonWindow.robot.keyPress(KeyEvent.VK_LEFT);
            else if(directionToGo == 1)
                superHexagonWindow.robot.keyPress(KeyEvent.VK_RIGHT);
        }
        
        if(verbose > 9) debug.update(this);
        
        System.out.println();
        this.oldGameImage = this.currentGameImage;
        iFrame++;
    }
    
    
    private int getWorldRotation(){
        Point oldLefterPoint = oldGameImage.hexagonPoints[0];
        int closerPointPosition = getCloserPoint(oldLefterPoint, currentGameImage.hexagonPoints);
        
        if( closerPointPosition == 0 )
            return 0;
        else if( closerPointPosition == 1 )
            return 1;
        else if( closerPointPosition == 5 )
            return -1;
        else {
            if( verbose > 0 ) System.out.println("Error: World rotation probably wrong");
            return closerPointPosition;
        }
    }
    
    
    class Wall{
        public int startPos;
        public int width;
        public boolean isSpeedDefined;
        public int speed;

        public Wall(int startPos, int width, boolean isSpeedDefined, int speed) {
            this.startPos = startPos;
            this.width = width;
            this.isSpeedDefined = isSpeedDefined;
            this.speed = speed;
        }
    }
    private List<Wall>[] getWallSpeeds(){
        List<Wall>[] walls = (List<Wall>[]) new ArrayList[6];
        
        for(int i=0 ; i<6 ; i++){
            walls[i] = new ArrayList<>();
            
            List<Point> oldWalls = new ArrayList<>();
            int minimalWall = oldGameImage.walls[mod6(i-worldRotation)].iterator().next().x;
            for(Point wall : oldGameImage.walls[mod6(i-worldRotation)])
                oldWalls.add(new Point(wall.x - minimalWall, wall.y));

            List<Point> currentWalls = new ArrayList<>();
            minimalWall = currentGameImage.walls[i].iterator().next().x;
            for(Point wall : currentGameImage.walls[i])
                currentWalls.add(new Point(wall.x - minimalWall, wall.y));

            Iterator<Point> oldWallsIterator = oldWalls.iterator();

            for(Point currentWall : currentWalls){
                boolean wallFound = false;
                while( !wallFound && oldWallsIterator.hasNext() ){
                    Point oldWall = oldWallsIterator.next();

                    int diffBetweenWalls;
                    // If old wall is behind the limit, analyse with end of wall
                    if(currentWall.x <= 0 && currentWall.x + currentWall.y > 0)
                        diffBetweenWalls = (oldWall.x + oldWall.y) - (currentWall.x + currentWall.y);
                    else 
                        diffBetweenWalls = oldWall.x - currentWall.x;
                    
                    if( Math.abs(diffBetweenWalls) < maxWallDistBetweenTwoFrames ){
                        wallFound = true;
                        walls[i].add(new Wall(currentWall.x + currentGameImage.positionDistanceFromCenter,
                                currentWall.y, true, diffBetweenWalls));
                    }
                }
                
                if( !wallFound )
                    walls[i].add(new Wall(currentWall.x + currentGameImage.positionDistanceFromCenter,
                                currentWall.y, false, 0));
            }
            
        }
        
        return walls;
    }
    
    
}
