package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Line {

    // ax + by = c
    public int a;
    public int b;
    public int c;
    
    public Line(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
    
    public Line(int x1, int y1, int x2, int y2){
        this.a = y2-y1;
        this.b = x1-x2;
        this.c = x1*y2 - x2*y1;
        
        if( a == 0 && b == 0 ) System.out.println("Error: Can't create a line with two mingled points");
    }
    
    
    public int getY(int x){
        if( b == 0 )
            return Integer.MAX_VALUE;
        
        return (c-a*x)/b;
    }
    
    public int getX(int y){
        if( a == 0 )
            return Integer.MAX_VALUE;
        
        return (c-b*y)/a;
    }
    

    public void draw(BufferedImage image, Color color, float stroke){
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.setStroke(new BasicStroke(stroke));
        
        // Vertical line
        if( b == 0 && a != 0 ){
            g.drawLine(c/a, 0, c/a, image.getHeight());
        } else
            g.drawLine(0, getY(0), image.getWidth(), getY(image.getWidth()));
    }
    
    
    public List<Point> getPointList(int minX, int minY, int maxX, int maxY){
        List<Point> points = new ArrayList<>();
        
        if ( b == 0 || Math.abs(getY(100)-getY(0)) > 100 ) {
            // Vertical-ish lines 
            for (int y = minY; y < maxY; y++) {
                int x = getX(y);
                if (x < maxX && x >= minX)
                    points.add(new Point(x, y));
            }

        } else {
            // Horizontal-sh lines 
            for (int x = minX; x < maxX; x++) {
                int y = getY(x);
                if (y < maxY && y >= minY)
                    points.add(new Point(x, y));
            }
        }
        
        return points;
    }
}
