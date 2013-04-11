package main;

import java.awt.Color;
import java.awt.Point;

public class Utility {

    public static double pointDistance(int x1, int y1, int x2, int y2){
        int xDiff = x1-x2;
        int yDiff = y1-y2;
        return Math.sqrt(xDiff*xDiff + yDiff*yDiff);
    }
    
    public static int[] intToRGB(int rgb){
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return new int[] {red, green, blue};
    }
    
    public static double angleBetween(Point center, Point current, Point previous) {
        double tmp = Math.atan2(current.x - center.x, current.y - center.y)
                   - Math.atan2(previous.x - center.x, previous.y - center.y);
        tmp = Math.abs(tmp)%(2*Math.PI);
        return Math.min(tmp, Math.abs(2*Math.PI - tmp));
    }
    
    public static int colorDif(int c1, int c2){
        Color col1 = new Color(c1);
        Color col2 = new Color(c2);
        int dis = Math.abs(col1.getBlue()-col2.getBlue())+Math.abs(col1.getGreen()-col2.getGreen())+Math.abs(col1.getRed()-col2.getRed());
        return dis;
    }
    
    public static int getCloserPoint(Point p, Point[] points){
        double minDist = Double.MAX_VALUE;
        int iCloserPoint = -1;
        
        for(int i=0 ; i<points.length ; i++){
            double dist = pointDistance(p.x, p.y, points[i].x, points[i].y);
            if( dist < minDist ){
                minDist = dist;
                iCloserPoint = i;
            }
        }
            
        return iCloserPoint;
    }
    
    public static int maxPos(int[] array){
        int max = Integer.MIN_VALUE;
        int maxPos = -1;
        
        for(int i=0 ; i<array.length ; i++)
            if(array[i] > max){
                max = array[i];
                maxPos = i;
            }
        
        return maxPos;
    }
    
    public static int mod6( int i ){
        if( i < 0 ) return i+6;
        if( i > 5 ) return i-6;
        return i;
    }
}
