package main;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class GameImage {
    private final static int centerCircleRadius = 100;
    private final static int maxColorDif = 100;
    private final static double minDifBetweenCenteredHexagonEdges = Math.PI/4;
    public final static int maxDistOfWallsFromCenter = 235;
    private final static int minPixelsInPositionTriangle = 20;
    private final static int maxPixelsInPositionTriangle = 100;
    
    private final static int centerX = 392;
    private final static int centerY = 268;
    
    public boolean error = false;
    
    public int verbose;
    
    public BufferedImage capture;
    public int width;
    public int height;
    
    public int[][] centeredRaster;
    
    public int centerColor;
    public Point[] hexagonPoints;
    public Line[] hexagonLines;
    public Point[] cutingPoints;
    public Line[] cutingLines;
    
    public List<Point>[] walls; // .x = distance from center, .y = size
    public int[] minWalls; // First walls, as the distance between the triangle & the potential walls
    
    public Point positionPoint = null;
    public double position = -1d;
    public int positionDistanceFromCenter;
    
    public GameImage(BufferedImage capture, int verbose) {
        this.verbose = verbose;
        this.capture = capture;
        this.width = capture.getWidth();
        this.height = capture.getHeight();
        
        centeredRaster = getCenteredRaster(centerCircleRadius);
        
        centerColor = centeredRaster[centerX][centerY];
        
        if(verbose > 2)
            System.out.println("Center point color: " + centerColor);
        
        hexagonPoints = getHexagonPoints();
        if(!error) hexagonLines = getHexagonLines();
                
        if(!error && verbose > 2)
            for(Point p:hexagonPoints)
                System.out.println("Centered hexagon point: " + p.x + ":" + p.y);

        if(!error) walls = getWalls();
        
        if(!error) getTrianglePosition();
        if(!error && verbose > 1)
            System.out.println("Triangle found: " + position + " [" + positionDistanceFromCenter + "]");
        
        if(!error) minWalls = getMinWalls();
        
    }
    
    // Get the grey raster from the capture limited to the circle with 'limit' radius, from the center
    private int[][] getCenteredRaster(int limit){
        int[][] raster = new int[width][height];
        
        DataBufferInt pseudoRaster = (DataBufferInt) capture.getRaster().getDataBuffer();
        
        for(int x=0 ; x<width ; x++)
            for(int y=0 ; y<height ; y++)
                if( Utility.pointDistance(x, y, centerX, centerY) < limit ){
                    int rgb = pseudoRaster.getElem(x+y*width);
                    int[] color = Utility.intToRGB(rgb);
                    raster[x][y] = color[0] + color[1] + color[2];
                }
        
        return raster;
    }
    
    // Get the 6 edges points of the centered hexagon
    private Point[] getHexagonPoints(){
        int firstHexPointY = centerY;
        while( Math.abs(centerColor - centeredRaster[centerX][firstHexPointY]) < maxColorDif ){
            firstHexPointY++;
            if( firstHexPointY == height ){
                if(verbose > 0) System.out.println("Error: Impossible to find centered hexagon in frame");
                error = true;
                return new Point[]{};
            }
        }
        
        boolean[][] memoization = new boolean[width][height];
        List<Point> lPoints = new ArrayList<>();
        expandEdge(centerX, firstHexPointY, lPoints, memoization);
        
        Point[] points = new Point[6];
        for(int i=0 ; i<6 ; i++)
            points[i] = getNextMaxPoint(lPoints);
        
        return points;
    }
    
    // Browse the edge of the centered hexagon
    private void expandEdge(int x, int y, List<Point> points, boolean[][] memoization){
        if(!memoization[x][y]){
            memoization[x][y] = true;
            if( Math.abs(centeredRaster[x][y] - centerColor) > maxColorDif)
                if(        Math.abs(centeredRaster[x-1][y] - centerColor) < maxColorDif
                        || Math.abs(centeredRaster[x+1][y] - centerColor) < maxColorDif
                        || Math.abs(centeredRaster[x][y-1] - centerColor) < maxColorDif
                        || Math.abs(centeredRaster[x][y+1] - centerColor) < maxColorDif ){
                    
                    points.add(new Point(x, y));
                    
                    expandEdge(x-1, y-1, points, memoization);
                    expandEdge(x-1, y  , points, memoization);
                    expandEdge(x-1, y+1, points, memoization);

                    expandEdge(x  , y-1, points, memoization);
                    expandEdge(x  , y+1, points, memoization);

                    expandEdge(x+1, y-1, points, memoization);
                    expandEdge(x+1, y  , points, memoization);
                    expandEdge(x+1, y+1, points, memoization);
                }
        }
    }
    
    // Get the next maximal point of the centered hexagon from the center
    private Point getNextMaxPoint(List<Point> points){
        double maxDist = -1;
        Point maxPoint = new Point();
        for(Point p:points){
            double dist = Utility.pointDistance(p.x, p.y, centerX, centerY);
            if(dist > maxDist){
                maxDist = dist;
                maxPoint = p;
            }
        }
        
        Point[] pointsCopy = new Point[points.size()];
        points.toArray( pointsCopy );
        for(Point p : pointsCopy)
            if(Math.abs(Utility.angleBetween(new Point(centerX,centerY), p, maxPoint)) < minDifBetweenCenteredHexagonEdges)
                points.remove(p);
        
        return maxPoint;
    }
    
    // Get the 6 lines describing the centered hexagon in clockwise order while then sorting the hexagon points
    // At the same time, it calculates the 3 cuting lines (perpendicular and passing throw the opposite point)
    private Line[] getHexagonLines(){
        List<Point> points = new ArrayList<>(Arrays.asList(hexagonPoints));
        
        // In order to get it clockwise, we're taking the left-most point, and find the a upper one
        Point leftPoint = new Point(width, -1);
        for(Point p:points)
            if(p.x < leftPoint.x)
                leftPoint = p;
        points.remove(leftPoint);
        
        Point[] sortedPoints = new Point[6];
        
        Point upPoint = new Point();
        double minDist = Double.MAX_VALUE;
        for(Point p:points)
            if(p.y <= leftPoint.y){
                double dist = Utility.pointDistance(leftPoint.x, leftPoint.y, p.x, p.y);
                if(dist < minDist){
                    minDist = dist;
                    upPoint = p;
                }
            }
        
        Line[] lines = new Line[6];
        lines[0] = new Line(leftPoint.x, leftPoint.y, upPoint.x, upPoint.y);
        sortedPoints[0] = leftPoint;
        
        // Remove the old starting point from the list, checking for the min distance for the other point
        Point oldPoint = upPoint;
        for(int i=1 ; i<5 ; i++){
            points.remove(oldPoint);
            
            minDist = Double.MAX_VALUE;
            Point minPoint = new Point();
            for(Point p:points){
                double dist = Utility.pointDistance(oldPoint.x, oldPoint.y, p.x, p.y);
                if(dist < minDist){
                    minDist = dist;
                    minPoint = p;
                }
            }
            
            lines[i] = new Line(oldPoint.x, oldPoint.y, minPoint.x, minPoint.y);
            sortedPoints[i] = oldPoint;
            oldPoint = minPoint;
        }
        
        Point lastPoint = points.get(0);
        lines[5] = new Line(lastPoint.x, lastPoint.y, leftPoint.x, leftPoint.y);
        sortedPoints[5] = lastPoint;
        
        this.hexagonPoints = sortedPoints;

        // Generating cuting lines & cuting points
        cutingPoints = new Point[6];
        for(int i=0 ; i<5 ; i++)
            cutingPoints[i] = new Point( (sortedPoints[i].x+sortedPoints[i+1].x)/2, (sortedPoints[i].y+sortedPoints[i+1].y)/2 );
        cutingPoints[5] = new Point( (sortedPoints[0].x+sortedPoints[5].x)/2, (sortedPoints[0].y+sortedPoints[5].y)/2 );
        
        cutingLines = new Line[3];
        for(int i=0 ; i<3 ; i++)
            cutingLines[i] = new Line(cutingPoints[i].x, cutingPoints[i].y, cutingPoints[i+3].x, cutingPoints[i+3].y);
        
        return lines;
    }
    
    // Detect the walls and remove them from the centered raster
    private List<Point>[] getWalls(){
        List<Point>[] wallList = (List<Point>[]) new ArrayList[6];
        for(int i=0 ; i<6 ; i++)
            wallList[i] = new ArrayList<>();
        
        for(Line line : cutingLines){
            List<Point> points = line.getPointList(0, 0, width, height);

            boolean isWall = false;
            boolean isTriangle = false;
            int wallStart = 0;
            for(Point p:points){
                int pointColor = capture.getRGB(p.x, p.y);
                int[] pointRGB = Utility.intToRGB(pointColor);
                int pointBW = pointRGB[0] + pointRGB[1] + pointRGB[2];
                boolean isFromWall = ( Math.abs(pointBW - centerColor) > maxColorDif );
                
                if( isWall && !isFromWall ){
                    // End of a wall
                    isWall = false;
                    if( !isTriangle ){
                        int distFromCenter = (int)Utility.pointDistance(p.x, p.y, centerX, centerY);
                        int currentRectPos = Utility.getCloserPoint(p, cutingPoints);
                        wallList[currentRectPos].add(new Point( Math.min(wallStart, distFromCenter), Math.abs(wallStart - distFromCenter) ));

                    } else {
                        isTriangle = false;
                    }
                
                } else if( !isWall && isFromWall ){
                    // Start of wall
                    isWall = true;
                    wallStart = (int)Utility.pointDistance(p.x, p.y, centerX, centerY);
                    
                    ExploreParameters exploreValue = exploreAndRemove(p.x, p.y);
                    // The last boolean is to avoid conflict of walls cut by the center circle that contains a number of pixels to close to the triangle cursor
                    if( exploreValue.depth < maxPixelsInPositionTriangle && exploreValue.depth > minPixelsInPositionTriangle && exploreValue.maxDistance < centerCircleRadius - 5 ){
                        positionPoint = exploreValue.furthestPoint;
                        positionDistanceFromCenter = exploreValue.maxDistance;
                        isTriangle = true;
                    }
                }
            }
            
            //If currently browsing a wall, add it to the list
            if( isWall ){
                int maxDist = (int)Utility.pointDistance(width, height, centerX, centerY);
                int currentRectPos = Utility.getCloserPoint(points.get(points.size()-1), cutingPoints);
                wallList[currentRectPos].add(new Point( wallStart, maxDist - wallStart ));
            }
        }
        
        // Remove the walls above the limit (13.04 : 17h20)
        for(List<Point> cWalls : wallList){
            Point[] wallsCopy = new Point[cWalls.size()];
            cWalls.toArray(wallsCopy);
            
            for(Point wall : wallsCopy){
                if( wall.x > maxDistOfWallsFromCenter )
                    cWalls.remove(wall);
                else if( wall.x + wall.y > maxDistOfWallsFromCenter )
                    wall.y = maxDistOfWallsFromCenter - wall.x;
            }
        }
        
        // Sort walls
        for(List<Point> cWalls : wallList){
            Collections.sort(cWalls, new Comparator<Point>(){
                @Override
                public int compare(Point t, Point t1) {
                    return Integer.compare(t.x, t1.x);
                }
            });
        }
        
        // Bound walls if separated by less than 2 pixels
        for(int i=0 ; i<6 ; i++){
            List<Point> boundedWalls = new ArrayList<>();
            
            Iterator<Point> itWalls = wallList[i].iterator();
            if( itWalls.hasNext() ){        // if not: error & add the empty list
                Point oldWall = itWalls.next();
                boolean isLastAdded = false;
                while(itWalls.hasNext()){
                    Point currentWall = itWalls.next();

                    if( isLastAdded )
                        isLastAdded = false;
                    else {
                        if(currentWall.x - (oldWall.x + oldWall.y) < 3){
                            boundedWalls.add(new Point(oldWall.x, (currentWall.x - oldWall.x) + currentWall.y));
                            isLastAdded = true;
                        } else {
                            boundedWalls.add(oldWall);
                            isLastAdded = false;
                        }
                    }

                    oldWall = currentWall;
                }

                if( !isLastAdded )
                    boundedWalls.add(oldWall);
            } else {
                error = true;
            }
            
            wallList[i] = boundedWalls;
        }
        
        return wallList;
    }
    
    // Erase walls from the raster. Return Point: .x = depth ; .y = maxDistanceFromCenter
    class ExploreParameters {
        public int depth;
        public int maxDistance;
        public Point furthestPoint;

        public ExploreParameters(int depth, int maxDistance, Point furthestPoint) {
            this.depth = depth;
            this.maxDistance = maxDistance;
            this.furthestPoint = furthestPoint;
        }
        
        public void mergeParameters(ExploreParameters par){
            this.depth += par.depth;
            if(par.maxDistance > this.maxDistance){
                this.maxDistance = par.maxDistance;
                this.furthestPoint = par.furthestPoint;
            }
        }
    }
    private ExploreParameters exploreAndRemove(int x, int y){
        int distFromCenter = (int)Utility.pointDistance(x, y, centerX, centerY);
        
        // If in the center raster & is of wall color
        if( distFromCenter < centerCircleRadius 
                && Math.abs(centeredRaster[x][y] - centerColor) > maxColorDif ){
            
            centeredRaster[x][y] = centerColor;

            ExploreParameters parameters = new ExploreParameters(1, distFromCenter, new Point(x, y));
            
            parameters.mergeParameters( exploreAndRemove(x-1, y  ) );
            parameters.mergeParameters( exploreAndRemove(x+1, y  ) );
            parameters.mergeParameters( exploreAndRemove(x  , y-1) );
            parameters.mergeParameters( exploreAndRemove(x  , y+1) );
            
            return parameters;
        
        } else {
            return new ExploreParameters(0, 0, new Point());
        }
    }
    
    // Get the position of the triangle cursor
    private void getTrianglePosition(){
        // If the positionPoint wasn't spotted while exploring the walls, find it
        for(int x = centerX - centerCircleRadius ; (x < centerX + centerCircleRadius) && (positionPoint == null) ; x++ )
            for(int y = centerY - centerCircleRadius ; (y < centerY + centerCircleRadius) && (positionPoint == null); y++ ){
                ExploreParameters exploreValue = exploreAndRemove(x, y);
                if( exploreValue.depth < maxPixelsInPositionTriangle && exploreValue.depth > minPixelsInPositionTriangle
                      && exploreValue.maxDistance < centerCircleRadius - 5 ){
                    positionPoint = exploreValue.furthestPoint;
                    positionDistanceFromCenter = exploreValue.maxDistance;
                }
            }
        
        if(positionPoint == null){
            if(verbose > 0) System.out.println("Error: The triangle cursor wasn't found");
            error = true;
            return;
        }
        
        int closestPoint = Utility.getCloserPoint(positionPoint, cutingPoints);
        int nextPoint = (closestPoint+1)%6;
        double bigAngle = Utility.angleBetween(new Point(centerX, centerY), hexagonPoints[closestPoint], hexagonPoints[nextPoint]);
        double smallAngle = Utility.angleBetween(new Point(centerX, centerY), hexagonPoints[closestPoint], positionPoint);
        double positionDiff = smallAngle/bigAngle;
        if(positionDiff > 1) positionDiff = 0.99;
        
        position = closestPoint + positionDiff;
    }

    // Get, for every wall list, closest wall from the triangle cursor
    private int[] getMinWalls(){
        int[] mWalls = new int[6];
        
        for(int i=0 ; i<6 ; i++){
            int minDist = Integer.MAX_VALUE;
            for(Point wall : walls[i]){
                if(wall.x > positionDistanceFromCenter && wall.x < minDist)
                    minDist = wall.x;
                else if( wall.x + wall.y > positionDistanceFromCenter && wall.x < positionDistanceFromCenter)
                    minDist = positionDistanceFromCenter;
            }
            mWalls[i] = minDist - positionDistanceFromCenter;
        }
        
        return mWalls;
    }
}
