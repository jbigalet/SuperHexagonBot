package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Debug {
    private final BlockingQueue<ImageIOInfo> imageInfoQueue;

    public Debug(){
        imageInfoQueue = new ArrayBlockingQueue<>(50);
        ImageIOThread imageIOThread = new ImageIOThread(imageInfoQueue, "C:/test/", "png");
        imageIOThread.start();

    }
    
    public void update(GameData gameData){
        GameImage gameImage = gameData.currentGameImage;
        
        BufferedImage capture = gameImage.capture;
        int captureWidth = capture.getWidth();
        int captureHeight = capture.getHeight();
        
        BufferedImage finalImage = new BufferedImage(captureWidth, captureHeight+GameImage.maxDistOfWallsFromCenter, BufferedImage.TYPE_INT_RGB);
        Graphics2D gFinalImage = finalImage.createGraphics();
        gFinalImage.drawImage(capture, 0, 0, null);
        
        if(gameImage.cutingLines != null)
            for(Line l:gameImage.cutingLines)
                l.draw(capture, Color.RED, 2f);
        
        gFinalImage.setColor(Color.MAGENTA);
        if(gameImage.positionPoint != null)
            gFinalImage.fillRect(gameImage.positionPoint.x-1, gameImage.positionPoint.y-1, 3, 3);

        int offset = captureWidth/6;
        if(gameImage.walls != null){
            gFinalImage.setColor(Color.WHITE);
            for(int i=0 ; i<6 ; i++)
                for(Point wall:gameImage.walls[i])
                    gFinalImage.fillRect(offset*i, wall.x+captureHeight, offset, wall.y);
        }

        if(gameImage.minWalls != null){
            gFinalImage.setColor(Color.BLUE);
            for(int i=0 ; i<6 ; i++)
                gFinalImage.fillRect(offset*i, gameImage.minWalls[i]+gameImage.positionDistanceFromCenter+captureHeight, offset, 3);
        }
        
        if(gameImage.position != -1d){
            gFinalImage.setColor(Color.MAGENTA);
            gFinalImage.fillRect((int)(offset*gameImage.position)-1, gameImage.positionDistanceFromCenter+captureHeight-1, 3, 3);
        } 

        
        int currentFPS = (int) (1000 / gameData.currentTimeDif);
        double scaling = (currentFPS-10d)/30d;
        if(scaling < 0) scaling = 0;
        else if(scaling > 1) scaling = 1;
        
        if(scaling < 0.5)
            gFinalImage.setColor(new Color(255, (int)(255*scaling), 0));
        else
            gFinalImage.setColor(new Color((int)(255*(2d-2*scaling)), (int)(255*scaling), 0));
        
        
        gFinalImage.setFont(new Font("Arial", Font.BOLD, 15));
        int fontSize = 20;
        int currentTextPosition = 130;

        gFinalImage.drawString("Current FPS: " + currentFPS, fontSize, (currentTextPosition+=fontSize+2));

        gFinalImage.setColor(Color.MAGENTA);
        gFinalImage.drawString("Time lapsed: " + (gameData.currentTimeDif), fontSize, (currentTextPosition+=fontSize+2));
        gFinalImage.drawString("Time to take the screenshot: " + (gameData.timeToTakeScreenshot), fontSize, (currentTextPosition+=fontSize+2));
        gFinalImage.drawString("Time to proceed the frame: " + (gameData.timeToProceedImage), fontSize, (currentTextPosition+=fontSize+2));

        currentTextPosition += fontSize+2;
        
        if(gameData.freshUpdatedSpeeds){
            gFinalImage.setColor(Color.GREEN);
            gFinalImage.drawString("Speeds updated", fontSize, (currentTextPosition+=fontSize+2));
            
            if(gameData.worldRotation != 0){
                gFinalImage.setColor(Color.RED);
                int posX = captureWidth/2;
                int posY = captureHeight-15;
                gFinalImage.fillRect(posX-20, posY-2, 40, 5);
                if(gameData.worldRotation == 1)
                    gFinalImage.fillPolygon(new int[] {posX+20, posX+30, posX+20},
                                            new int[] {posY-10, posY,    posY+10 }, 3);
                else 
                    gFinalImage.fillPolygon(new int[] {posX-20, posX-30, posX-20},
                                            new int[] {posY-10, posY,    posY+10 }, 3);
            }
            
//            gFinalImage.setColor(Color.MAGENTA);
//            gFinalImage.drawString("Average wall speed: " + gameData.averageWallSpeed, fontSize, (currentTextPosition+=fontSize+2));
            
        } else {
            gFinalImage.setColor(Color.RED);
            gFinalImage.drawString("Speeds not updated", fontSize, (currentTextPosition+=fontSize+2));
        }
        
        currentTextPosition += fontSize+2;
        
        gFinalImage.setColor(Color.RED);
        
        if(gameImage.cutingLines == null)
            gFinalImage.drawString("Cuting lines not found", fontSize, (currentTextPosition+=fontSize+2));
        
        if(gameImage.positionPoint == null )
            gFinalImage.drawString("Position point not found", fontSize, (currentTextPosition+=fontSize+2));
        else if(gameImage.position == -1d )
            gFinalImage.drawString("Position not found", fontSize, (currentTextPosition+=fontSize+2));

        if(gameImage.walls == null)
            gFinalImage.drawString("Walls not found", fontSize, (currentTextPosition+=fontSize+2));
        else if(gameImage.minWalls == null )
            gFinalImage.drawString("Minimum walls not found", fontSize, (currentTextPosition+=fontSize+2));

        gFinalImage.setColor(Color.MAGENTA);
        if(gameData.directionToGo != 99)
            gFinalImage.drawString("Direction: "
                    + (gameData.directionToGo < 0 ? "LEFT" :
                       gameData.directionToGo == 0 ? "NONE" : "RIGHT"),
                    fontSize, (currentTextPosition+=fontSize+2));
        
        try {
            imageInfoQueue.put(new ImageIOInfo(finalImage, "frame_" + gameData.iFrame));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
}
