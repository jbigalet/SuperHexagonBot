
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.Point;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import javax.imageio.ImageIO;

public class SuperHexagonBot {

    public static Robot robot;
    public static boolean debug = true;
    public static void main(String[] args) throws AWTException, IOException, InterruptedException {

        JFrameDebug frame = new JFrameDebug();
        fRectangles = new JFrameRectangleDebug();

        long time = System.currentTimeMillis();

        if(!debug){
            WinDef.HWND hWnd = User32.INSTANCE.FindWindow("GLUT", "Super Hexagon");
            WinDef.RECT rect = new WinDef.RECT();
            User32.INSTANCE.GetWindowRect(hWnd, rect);
            User32.INSTANCE.SetForegroundWindow(hWnd);

            robot = new Robot();
            Rectangle rectangle = rect.toRectangle();

            robot.keyPress(KeyEvent.VK_ENTER);
            Thread.sleep(50);
            robot.keyRelease(KeyEvent.VK_ENTER);
            
            for(int i=0 ; i<200 ; i++){
                nProcess++;
                BufferedImage capture = robot.createScreenCapture(rectangle);
//                ImageIO.write(capture, "bmp", new File("C:/test/plop_00" + i + ".bmp"));
                processImage(frame, capture);
                capture.flush();
            }
        } else {
//            for(int i=12 ; i!=0 ; i=0){    //105
            for(int i=0 ; i<14 ; i++){
                nProcess++;
                BufferedImage capture = ImageIO.read(new File("C:/test/plop_00" + i + ".bmp"));
                processImage(frame, capture);
            }
        }
                
        long averageProcess = (System.currentTimeMillis()-time)/nProcess;
        System.out.println("Average FPS: " + (1000d/(double)averageProcess) + " (" + averageProcess + "ms)");
        
//        ImageIO.write(capture, "bmp", new File("C:/plop.bmp"));

    }
    
//    public static int timeProcessing = 0;
    public static int nProcess = 0;
    
    public static double oldTheta = 0d;
    public static double rotation = 0d;
    public static void processImage(JFrameDebug frame, BufferedImage oImage) throws IOException{
        clImage = imageToArray(oImage);
        
//        boolean[][] edges = FastHough.fastEdgeDetector(imageToFullArray(oImage, 200));
//        BufferedImage edgesImage = new BufferedImage(edges.length, edges[0].length, BufferedImage.TYPE_INT_RGB);
//        for(int i=0 ; i<edges.length ; i++)
//            for(int j=0 ; j<edges[0].length ; j++)
//                if(edges[i][j])
//                    edgesImage.setRGB(i, j, 0xffffff);
//        ImageIO.write(edgesImage, "bmp", new File("C:\\plouff.bmp"));
        
        Point[] maxPoints = centralHexagonDetection(imageToFullArray(oImage, 200));
        for(Point p:maxPoints){
            System.out.println(p);
            oImage.setRGB(p.x, p.y, 0xff00ff);
        }
        Vector<HoughLineCV> cutLines = getCutLines2(maxPoints);
        
        long time = System.currentTimeMillis();
        
        for(int x=0 ; x<oImage.getWidth() ; x++){
            for(int y=0 ; y<30 ; y++)
                oImage.setRGB(x, y, 0x000000ff);
            for(int y=510 ; y<518 ; y++)
                oImage.setRGB(x, y, 0x000000ff);
        }
        for(int x=0 ; x<210 ; x++)
            for(int y=30 ; y<60 ; y++)
                oImage.setRGB(x, y, 0x000000ff);
        for(int x=502 ; x<oImage.getWidth(); x++)
            for(int y=30 ; y<83 ; y++)
                oImage.setRGB(x, y, 0x000000ff);
        for(int x=542 ; x<oImage.getWidth(); x++)
            for(int y=30 ; y<83 ; y++)
                oImage.setRGB(x, y, 0x000000ff);
        for(int y=0 ; y<oImage.getHeight() ; y++){
            for(int x=0 ; x<8 ; x++)
                oImage.setRGB(x, y, 0x000000ff);
            for(int x=776 ; x<oImage.getWidth() ; x++)
                oImage.setRGB(x, y, 0x000000ff);
        }
        
        HoughCV houghCV = JavaCVTest.HoughLines(oImage); // 392:268
//        IplImage iplOImage = IplImage.createFrom(oImage);
//        BufferedImage eImage = houghCV.edgeImage.getBufferedImage(houghCV.edgeImage.getBufferedImageType() == BufferedImage.TYPE_CUSTOM ? 1.0 : 1.0, false);
        
//        Vector<HoughLineCV> cutLines = getCutLines(houghCV.lines);
        if(cutLines.size()!=3){
            System.out.println("ERROR [" + nProcess + "]: " + cutLines.size() + " found as cut lines. Skipping frame.\n");
            return;
        }
//        cutLines = sortByTheta(cutLines);
        List<CRectangle>[] rects = getPseudoHorizontalLines(oImage, cutLines);
        rects = bwRects(rects);
//        int[] walls = getWalls(rects, 100);
        int minWall = getMinWalls(rects)+10;
        int[] walls = getWalls(rects, minWall);
        for(int i = 0; i < walls.length; i++){
            rects[i].add(new CRectangle(walls[i]+minWall, walls[i]+2+minWall, 0xff00ff));
            System.out.println(walls[i]);
        }
        
//        int largestSpace = getLargestSpace(walls);
//        if(triangle - largestSpace > 0)
//            new KeyThread(-140, robot).start();
//        else if(triangle - largestSpace < 0)
//            new KeyThread(140, robot).start();
        
        if(triangle2 != -1){
            if(!debug){
                int diff = triangle - triangle2;
                if( Math.abs(diff) == 5 ) diff = -diff/5;
                if( walls[triangle] < walls[triangle2] ) diff = -diff;
                new KeyThread(40*diff, robot).start();
                System.out.println("Triangle on an intersection, going to the " + (diff==1?"right":"left"));
            }
        } else
            if(walls[triangle] < 150){
                int nWall = (triangle == 5) ? 0 : triangle+1;
                int pWall = (triangle == 0) ? 5 : triangle-1;
                if(walls[nWall] < walls[pWall]){
                    System.out.println("Go to the left");
                    if(!debug) new KeyThread(-60, robot).start();
                }
                else {
                    System.out.println("Go to the right");
                    if(!debug) new KeyThread(60, robot).start();
                }
            }
        
        rects[triangle].add(new CRectangle(0, 50, 0xff0000));
        
        Graphics2D g2d = oImage.createGraphics();

        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        for(HoughLineCV line : cutLines){
//        for(HoughLineCV line : houghCV.lines){
//            line.draw(iplOImage);
            g2d.drawLine(line.pt1X, line.pt1Y, line.pt2X, line.pt2Y);
        }
        
        g2d.setColor(Color.WHITE);
//        g2d.setStroke(new BasicStroke(1));
//        for(HoughLineCV line : horLines)
//            g2d.drawLine(line.pt1X, line.pt1Y, line.pt2X, line.pt2Y);
        
        double mTheta = Double.MAX_VALUE;
        HoughLineCV bestLine = null;
        for(HoughLineCV line : cutLines){
            double tmp = Math.abs(line.theta - oldTheta)%(Math.PI/2);
            if( tmp < mTheta ){
                mTheta = tmp;
                bestLine = line;
            }
            tmp = Math.abs(line.theta - oldTheta - Math.PI)%(Math.PI/2);
            if( tmp < mTheta ){
                mTheta = tmp;
                bestLine = line;
            }
        }
        rotation = (rotation + mTheta) % (2*Math.PI);
        oldTheta = mTheta;
//        System.out.println("Rotation: " + rotation);
//        g2d.drawLine(bestLine.pt1X, bestLine.pt1Y, bestLine.pt2X, bestLine.pt2Y);

//        BufferedImage image = iplOImage.getBufferedImage(iplOImage.getBufferedImageType() == BufferedImage.TYPE_CUSTOM ? 1.0 : 1.0, false);
        
        long time2 = System.currentTimeMillis();
        System.out.println("Image analysis [" + nProcess + "] done in " + (time2-time) + "ms\n");
        
        frame.reloadImage(oImage, 0);
//        frame.reloadImage(arrayToImage(clImage), 0);

        fRectangles.reloadRectangles(rects);

//        ImageIO.write(oImage, "bmp", new File("C:/test2/plop_00" + (plop++) +  ".bmp"));
//        ImageIO.write(image, "bmp", new File("C:/plop_00" + (plop++) +  "b.bmp"));
    }
    
    public static double oldprocessImage(JFrameDebug frame, BufferedImage oImage, double oldTheta) throws IOException{
//
//        long time = System.currentTimeMillis();
//        
////        CannyEdgeDetector detector = new CannyEdgeDetector();
////
////        detector.setLowThreshold(0.5f);
////        detector.setHighThreshold(1f);
////        detector.setSourceImage(oImage);
////        detector.process();
////
////        BufferedImage image = detector.getEdgesImage();
//        
//        for(int x=0 ; x<oImage.getWidth() ; x++){
//            for(int y=0 ; y<30 ; y++)
//                oImage.setRGB(x, y, 0x000000ff);
//            for(int y=510 ; y<518 ; y++)
//                oImage.setRGB(x, y, 0x000000ff);
//        }
//        for(int x=0 ; x<210 ; x++)
//            for(int y=30 ; y<60 ; y++)
//                oImage.setRGB(x, y, 0x000000ff);
//        for(int x=502 ; x<oImage.getWidth(); x++)
//            for(int y=30 ; y<83 ; y++)
//                oImage.setRGB(x, y, 0x000000ff);
//        for(int x=542 ; x<oImage.getWidth(); x++)
//            for(int y=30 ; y<83 ; y++)
//                oImage.setRGB(x, y, 0x000000ff);
//        for(int y=0 ; y<oImage.getHeight() ; y++){
//            for(int x=0 ; x<8 ; x++)
//                oImage.setRGB(x, y, 0x000000ff);
//            for(int x=776 ; x<oImage.getWidth() ; x++)
//                oImage.setRGB(x, y, 0x000000ff);
//        }
//        
//        
//        BufferedImage image = JavaCVTest.CannyEdgeDetector(oImage);
//        
//        long time2 = System.currentTimeMillis();
//        System.out.println("Canny Edge Detector done in " + (time2-time) + "ms.");
//
//        HoughTransform h = new HoughTransform(image.getWidth(), image.getHeight()); 
//        h.addPoints(image); 
//        Vector<HoughLine> lines = h.getLines(50);
//        Vector<HoughLine> cutLines = getCutLines(lines);
//        double rMin = Double.MAX_VALUE;
//        double theta = 0;
//        double mThetaDiff = Double.MAX_VALUE;
//        double newRot = 0;
//        for (int j = 0; j < cutLines.size(); j++) { 
//            HoughLine line = cutLines.elementAt(j);
//            line.draw(oImage, Color.RED.getRGB());
//
//            System.out.println("-> " + line.theta + "   []   " + line.r + "   - " + line.score);
//            if( Math.abs(554 - line.r) < rMin){
//                rMin = Math.abs(554 - line.r);
//                theta = line.theta;
//            }
//            double tmp = Math.abs( line.theta - oldTheta ); // ThetaDiff
//            double tmp2 = Math.abs(Math.PI - tmp);
//            double realDiff = Math.min(tmp, tmp2);
//            if( realDiff < mThetaDiff ){
//                mThetaDiff = realDiff;
//                newRot = line.theta - oldTheta;
//            }
//        }
//        
//        if(newRot > 2)
//            newRot -= Math.PI;
//        rotation += newRot;
//        System.out.println("Rotation: " + newRot + " [" + rotation + "]");
//
////        image = JavaCVTest.HoughLines(image); // 392:268
//        
//        long time3 = System.currentTimeMillis();
//        System.out.println("Hough Transform done in " + (time3-time2) + "ms [" + theta + " @ " + rMin + "\n");
//        
//        frame.reloadImage(oImage, rotation);
//        ImageIO.write(oImage, "bmp", new File("C:/plop_00" + plop +  ".bmp"));
//        ImageIO.write(image, "bmp", new File("C:/plop_00" + (plop++) +  "b.bmp"));
//
//        return theta;
        return 0;
    }
    public static int plop = 0;
    
    public static Vector<HoughLineCV> getCutLines(Vector<HoughLineCV> lines){
        
        Vector<HoughLineCV> lineHeap = new Vector<>();
        for(HoughLineCV line : lines)
            if( line.distanceToPt(392, 268) < 10 )
                lineHeap.add(line);

        Vector<HoughLineCV> cutLines = new Vector<>(3);
        while(!lineHeap.isEmpty()){
            double minR = Double.MAX_VALUE;
            HoughLineCV bestLine = lineHeap.firstElement();
            for(HoughLineCV line : lineHeap)
                if( line.distanceToPt(392, 268) < minR ){
                    minR = line.distanceToPt(392, 268);
                    bestLine = line;
                }
            for(int i=0 ; i<lineHeap.size() ; i++){
                HoughLineCV line = lineHeap.get(i);
                if( Math.abs(line.theta-bestLine.theta)%(Math.PI/2) < Math.PI/30
                        || Math.abs(line.theta-bestLine.theta-Math.PI)%(Math.PI/2) < Math.PI/30 ){
                    lineHeap.remove(line);
                    i--;
                }
            }
            cutLines.add(bestLine);
//            System.out.println(bestLine.theta);
        }
        
        return cutLines;
    }
    
    public static int triangle = -1;
    public static int triangle2 = -1;
    public static int[][] iCopy;
    public static JFrameRectangleDebug fRectangles;
    public static List<CRectangle>[] getPseudoHorizontalLines(BufferedImage image, Vector<HoughLineCV> cutLines){
        Vector<HoughLineCV> realHor = new Vector<>();
        
        HoughLineCV[] cLines = new HoughLineCV[4];
        for(int i=0 ; i<3 ; i++)
            cLines[i] = cutLines.get(i);
        cLines[3] = cLines[0];
        
        for(int i=0 ; i<3 ; i++){
            HoughLineCV cLine1 = cLines[i];
//            System.out.println(cLine1.theta);
            HoughLineCV cLine2 = cLines[i+1];
            double thetaDiff = Math.abs(cLine1.theta - cLine2.theta);
            double thetaMid = (cLine1.theta + cLine2.theta)/2d;
            if(thetaDiff > Math.PI/2)
                thetaMid = (cLine1.theta + cLine2.theta - Math.PI) / 2d + Math.PI;
            thetaMid %= Math.PI;
//            System.out.println("- " + cLine1.theta);
//            System.out.println(thetaMid);
            double rho = Math.cos(thetaMid)*392d + Math.sin(thetaMid)*268d;
            realHor.add(new HoughLineCV(thetaMid, rho));
        }
        
        realHor = sortByTheta(realHor);
        // Follow realHor lines and check the color
        List<CRectangle>[] rects = (List<CRectangle>[]) new ArrayList[6];
        int iRect = 0;
        triangle = -1;
//        System.out.println(realHor.size());
        for(HoughLineCV line : realHor){
//            System.out.println(line.theta);
            double theta = line.theta;
            double r = line.rho;
            
            int height = image.getHeight(); 
            int width = image.getWidth(); 
//            
//            float centerX = width / 2; 
//            float centerY = height / 2; 
            
            double tsin = Math.sin(theta);
            double tcos = Math.cos(theta);

            List<CRectangle> tmpRect = new ArrayList<>();
            List<CRectangle> tmpRect2 = new ArrayList<>();
            int oldPix = 0&000000;
            int start = 0;
            if (theta < Math.PI * 0.25 || theta > Math.PI * 0.75) {
                // Vertical-ish lines 
                for (int y = 0; y < height; y++) {
                    int x = (int) ((r - y * tsin) / tcos);
                    if (x < width && x >= 0) {
                        int pix = image.getRGB(x, y);
                        if( colorDif(pix, oldPix) > 50 ){
                            int dist = (int)Math.sqrt((x-392)*(x-392)+(y-268)*(y-268));
//                            System.out.println("-> " + start + "-" + dist + " @ " + oldPix);
                            iExp = 0;
                            expandCluster(x, y, pix);
                            if(iExp<100 && iExp>0 && dist<150)
//                                System.out.println("Triangle: " + x + "-" + y + "@" + iExp);
                                triangle = (x <= 392) ? iRect : iRect+3;
                            else
                                if (x <= 392)
                                    tmpRect.add(new CRectangle(start, dist, oldPix));
                                else
                                    tmpRect2.add(new CRectangle(start, dist, oldPix));
                            
                            oldPix = pix;
                            start = dist;
                        }
                    }
//                    int dist = (int)Math.sqrt((x-392)*(x-392)+(y-268)*(y-268));
//                    tmpRect.add(new CRectangle(start, dist, oldPix));
                }
            } else {
                // Horizontal-sh lines 
                for (int x = 0; x < width; x++) {
                    int y = (int) ((r - x * tcos) / tsin);
                    if (y < height && y >= 0) {
                        int pix = image.getRGB(x, y);
                        if( colorDif(pix, oldPix) > 50 ){
                            int dist = (int)Math.sqrt((x-392)*(x-392)+(y-268)*(y-268));
//                            System.out.println("-> " + start + "-" + dist + " @ " + oldPix);
                            iExp = 0;
                            expandCluster(x, y, pix);
                            if(iExp<100 && iExp>0 && dist<150)
//                                System.out.println("Triangle: " + x + "-" + y + "@" + iExp);
                                triangle = (x <= 392) ? iRect : iRect+3;
                            else
                                if(x <= 392)
                                    tmpRect.add(new CRectangle(start, dist, oldPix));
                                else
                                    tmpRect2.add(new CRectangle(start, dist, oldPix));

                            oldPix = pix;
                            start = dist;
                        }
                    }
//                    int dist = (int)Math.sqrt((x-392)*(x-392)+(y-268)*(y-268));
//                    tmpRect.add(new CRectangle(start, dist, oldPix));
                }
            }
            rects[iRect++] = tmpRect;
            rects[iRect+2] = tmpRect2;
        }
        
        triangle2 = -1;
        if(triangle != -1){
            System.out.println("Triangle found1: " + triangle);
        } else {
            boolean br = true;
            int X = -1, Y = -1;
            for(int x=292 ; x<492 && br ; x++)
                for(int y=168 ; y<368 && br ; y++)
                    if(colorDif(clImage[x][y], 0) > 20){
                        iExp = 0;
                        expandCluster(x, y, clImage[x][y]);
                        if(iExp>10){
//                            System.out.println(x + ":" + y);
                            X = x;
                            Y = y;
                            br = false;
                        }
                    }
            double minDist = Double.MAX_VALUE;
            int iLine = 0;
            Map<Double, Integer> dists = new TreeMap<>();
            for(HoughLineCV line : realHor){
                double dist = line.distanceToPt(X, Y);
                dists.put(dist, iLine);
//                System.out.println(line.theta);
                if(dist < minDist){
                    minDist = dist;
//                    double xOrt = (X+line.C*line.B-Y*line.A*line.B)/(1+(line.A*line.B)*(line.A*line.B));
//                    double xOrt = (X+line.C/line.B-Y*line.A/line.B)/(1+(line.A/line.B)*(line.A/line.B));

                    double L = Math.sqrt((line.pt2X-line.pt1X)*(line.pt2X-line.pt1X)+(line.pt2Y-line.pt1Y)*(line.pt2Y-line.pt1Y));
                    double r = ((line.pt1Y-Y)*(line.pt1Y-line.pt2Y)+(line.pt1X-X)*(line.pt1X-line.pt2X))/(L*L);
                    double xOrt = line.pt1X + r*(line.pt2X-line.pt1X);
//                    System.out.println(xOrt);
                    // TO REMAKE
                    if( xOrt < 392 || (xOrt < 396 && Y > 270) )
                        triangle = iLine;
                    else
                        triangle = iLine+3;
                }
                iLine++;
            }
            if(triangle != -1){
                System.out.println("Triangle found2: " + triangle);
                if(minDist > 15){
                    int i=0;
                    int t=-1;
                    for(Double d:dists.keySet()){
                        t = dists.get(d);
                        if(i==1) break;
                        i++;
                    }
                    triangle2 = (Math.abs(triangle-t) > 1) ? t+3 : t;
                    System.out.println("Triangle at intersection of: " + triangle2);
                }
            } else {
                System.out.println("ERROR: No triangle found");
            }
            
        }
        
//        fRectangles.reloadRectangles(rects);
        
//        Vector<HoughLineCV> realHor = new Vector<>();
//        for(HoughLineCV cLine : cutLines){
//            Vector<HoughLineCV> horLines = new Vector<>();
//            for(HoughLineCV line : lines)
//                if( Math.abs(line.theta-cLine.theta) % Math.PI < Math.PI/30
//                        && line.distanceToPt(392, 268) > 40 ){
//                    horLines.add(line);
////                    System.out.println(line.rho + " @ " + line.theta + " : " + line.realDistanceToPt(392, 268));
//                }
//            while(!horLines.isEmpty()){
//                double minTheta = Double.MAX_VALUE;
//                HoughLineCV bestLine = horLines.firstElement();
//                for(HoughLineCV line : horLines)
////                    if( Math.abs(line.theta-cLine.theta) % Math.PI < minTheta ){
////                        minTheta = Math.abs(line.theta-cLine.theta) % Math.PI;
////                        bestLine = line;
////                    }
//                    if( line.distanceToPt(392, 268) < minTheta ){
//                        minTheta = line.distanceToPt(392, 268);
//                        bestLine = line;
//                    }
//                double dist = bestLine.realDistanceToPt(392, 268);
//                for(int i=0 ; i<horLines.size() ; i++){
//                    HoughLineCV line = horLines.get(i);
//                    if( Math.abs(line.realDistanceToPt(392, 268) - dist) < 20 ){
//                        horLines.remove(line);
//                        i--;
//                    }
//                }
//                realHor.add(bestLine);
//            }
//        }
        
        
        
        return rects;
    }
    
    public static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    
    public static int[][] clImage;
    public static int plouf = 0;
    public static int iExp = 0;
    public static void expandCluster(int x, int y, int color){
//        System.out.println(plouf++);
        if(x < 0 || x >= clImage.length|| y < 0 || y >= clImage[0].length)
            return;
        if( clImage[x][y] == 0 || colorDif(clImage[x][y], color) > 20 )
            return;
//        System.out.println(image.getRGB(x, y));
//        if( clImage[x][y] == 0 ){
//            System.out.println("BLANC");
//            return;
//        }
        clImage[x][y]= 0;
        iExp++;
        expandCluster(x-1, y, color);
        expandCluster(x+1, y, color);
        expandCluster(x, y-1, color);
        expandCluster(x, y+1, color);
    }
    
    public static int colorDif(int c1, int c2){
        Color col1 = new Color(c1);
        Color col2 = new Color(c2);
        int dis = Math.abs(col1.getBlue()-col2.getBlue())+Math.abs(col1.getGreen()-col2.getGreen())+Math.abs(col1.getRed()-col2.getRed());
        if(c2 == 0x0000ff)
            System.out.println("BLUE SOPTED: " + dis);
        return dis;
    }
    
    public static int[][] imageToArray(BufferedImage image){
        int[][] a = new int[image.getWidth()][image.getHeight()];
        for(int i=0 ; i<a.length ; i++)
            for(int j=0 ; j<a[0].length ; j++)
                a[i][j] = image.getRGB(i, j);
        return a;
    }
    
    public static int[][][] imageToFullArray(BufferedImage image, int limit){
        int[][][] a = new int[image.getWidth()][image.getHeight()][3];
        for(int i=0 ; i<a.length ; i++)
            for(int j=0 ; j<a[0].length ; j++)
                if(pointDistance(i, j, 392, 268) < limit){
                    int rgb = image.getRGB(i,j);
                    a[i][j][0] = (rgb >> 16) & 0xFF;
                    a[i][j][1] = (rgb >> 8) & 0xFF;
                    a[i][j][2] = rgb & 0xFF;
            }
        return a;
    }
    
    public static BufferedImage arrayToImage(int[][] a){
        BufferedImage image = new BufferedImage(a.length, a[0].length, BufferedImage.TYPE_INT_RGB);
        for(int i=0 ; i<a.length ; i++)
            for(int j=0 ; j<a[0].length ; j++)
                image.setRGB(i, j, a[i][j]);
        return image;
    }
    
    public static Vector<HoughLineCV> sortByTheta(Vector<HoughLineCV> lines){
        List<Double> thetas = new ArrayList<>();   //TO REMAKE
        for(HoughLineCV line:lines)
            thetas.add(line.theta);
        Collections.sort(thetas);
//        System.out.println(thetas);
        Vector<HoughLineCV> sorted = new Vector<>();
        for(Double theta:thetas)
            for(HoughLineCV line:lines)
                if(line.theta==theta)
                    sorted.add(line);
        return sorted;
    }
    
    public static int getMinWalls(List<CRectangle>[] rects){
        int mWall = Integer.MAX_VALUE;
        for(List<CRectangle> rect : rects){
            int tmp = getMinWall(rect);
            if(tmp<mWall)
                mWall = tmp;
        }
        return mWall;
    }
    
    public static int getMinWall(List<CRectangle> rect){
        int min = Integer.MAX_VALUE;
        for(CRectangle r : rect)
            if(r.color == 0x000000){
                int tmp = Math.max(r.start, r.end);
                min = Math.min(min, tmp);
            }
        return min;
    }
    
    public static int[] getWalls(List<CRectangle>[] rects, int limit){
        int[] walls = new int[rects.length];
        for(int i = 0; i < walls.length; i++)
            walls[i] = getWall(rects[i], limit) - limit;
        return walls;
    }
    
    public static int getWall(List<CRectangle> rect, int limit){
//        System.out.println("plop");
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for(CRectangle r:rect){
//            System.out.println(r.color);
            max = Math.max(Math.max(r.start, r.end), max);
            if( r.color == 0x000000
                    && Math.max(r.start, r.end) > limit ){
//                System.out.println(r.color);
//                return Math.min(r.start, r.end);
                min = Math.min(min, Math.min(r.start, r.end));
            }
        }
        return Math.min(min, max);
//        CRectangle r = rect.get(rect.size()-1);
//        return Math.min(r.start, r.end);
    }
    
    public static List<CRectangle>[] bwRects(List<CRectangle>[] rects){
        List<CRectangle>[] bw = (List<CRectangle>[]) new ArrayList[6];
        for(int i=0 ; i<rects.length ; i++)
            bw[i] = bwRect(rects[i]);
        return bw;
    }
    
    public static List<CRectangle> bwRect(List<CRectangle> rect){
        List<CRectangle> bw = new ArrayList<>();
        for(CRectangle r:rect)
            if( Math.abs(r.start-r.end) > 2)
                bw.add(new CRectangle(r.start, r.end, 
                        colorDif(r.color, 0x000000)>200 ? 0x000000 : 0xffffff));
        return bw;
    }
    
    public static int getLargestSpace(int[] walls){
        int larg = -1;
        int thin = Integer.MIN_VALUE;
        for(int i=0 ; i<walls.length ; i++)
            if(walls[i] > thin){
                thin = walls[i];
                larg = i;
            }
        return larg;
    }
    
    public static Point[] centralHexagonDetection(int[][][] raster){
        int[] centerColor = raster[392][268];
        int firstHexPoint = 268;
        while( FastHough.colorDif(centerColor, raster[392][firstHexPoint]) < 100 )
            firstHexPoint++;
        boolean[][] alreadySaw = new boolean[raster.length][raster[0].length];
        List<Point> lPoints = new ArrayList<>();
        expandEdge(raster, 392, firstHexPoint, lPoints, alreadySaw);
        Point[] points = new Point[6];
        for(int i=0 ; i<6 ; i++)
            points[i] = getMaxPoint(lPoints);
        return points;
    }
    
    public static Point getMaxPoint(List<Point> lPoints){
        double maxDist = -1;
        Point maxPoint = new Point();
        for(Point p:lPoints){
            double tmp = pointDistance(p.x, p.y, 392, 268);
            if(tmp > maxDist){
                maxDist = tmp;
                maxPoint = p;
            }
        }
        Point[] bck = new Point[lPoints.size()];
        lPoints.toArray( bck );
        for(Point p : bck)
            if(pointDistance(p.x, p.y, maxPoint.x, maxPoint.y) < 20)
                lPoints.remove(p);
        return maxPoint;
    }
    
    public static void expandEdge(int[][][] raster, int x, int y, List<Point> points, boolean[][] alreadySaw){
        if(!alreadySaw[x][y]){
            alreadySaw[x][y] = true;
            if(raster[x][y][0]+raster[x][y][1]+raster[x][y][2] > 200)
                if(raster[x-1][y][0]+raster[x-1][y][1]+raster[x-1][y][2] < 200
                        || raster[x+1][y][0]+raster[x+1][y][1]+raster[x+1][y][2] < 200
                        || raster[x][y-1][0]+raster[x][y-1][1]+raster[x][y-1][2] < 200
                        || raster[x][y+1][0]+raster[x][y+1][1]+raster[x][y+1][2] < 200){
                    points.add(new Point(x, y));
                    expandEdge(raster, x-1, y, points, alreadySaw);
                    expandEdge(raster, x+1, y, points, alreadySaw);
                    expandEdge(raster, x, y-1, points, alreadySaw);
                    expandEdge(raster, x, y+1, points, alreadySaw);
                    expandEdge(raster, x-1, y-1, points, alreadySaw);
                    expandEdge(raster, x+1, y+1, points, alreadySaw);
                    expandEdge(raster, x-1, y+1, points, alreadySaw);
                    expandEdge(raster, x+1, y-1, points, alreadySaw);
                }
        }
    }
    
    public static double pointDistance(int x, int y, int x2, int y2){
        int X = x-x2;
        int Y = y-y2;
        return Math.sqrt(X*X+Y*Y);
    }
    
    public static Vector<HoughLineCV> getCutLines2(Point[] points){
        Vector<HoughLineCV> lines = new Vector<>();
        for(int i=0 ; i<3 ; i++)
            lines.add(new HoughLineCV(points[i].x, points[i].y, 392, 268));
        return lines;
    }
}
