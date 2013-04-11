import static com.googlecode.javacv.cpp.opencv_core.*;

public class HoughLineCV {
    
    protected double theta; 
    protected double rho; 

    protected int pt1X;
    protected int pt1Y;
    protected int pt2X;
    protected int pt2Y;
    
    protected int A;
    protected int B;
    protected int C;
 
    public HoughLineCV(double theta, double rho) { 
        this.theta = theta; 
        this.rho = rho; 

        double a = Math.cos((double) theta), b = Math.sin((double) theta);
        double x0 = a * rho, y0 = b * rho;
        pt1X = (int) Math.round(x0 + 1000 * (-b));
        pt1Y = (int) Math.round(y0 + 1000 * (a));
        pt2X = (int) Math.round(x0 - 1000 * (-b));
        pt2Y = (int) Math.round(y0 - 1000 * (a));
//        System.out.println(pt1X + ":" + pt1Y + " ; " + pt2X + ":" + pt2Y);
        
        A = pt1Y-pt2Y;
        B = pt2X-pt1X;
        C = pt1Y*(pt1X-pt2X) + pt1X*(pt2Y-pt1Y);
//        pt1X=0;
//        pt1Y=(int)(-c/b);
//        pt2X=1000;
//        pt2Y=(int)((-a*1000-c)/b);
    } 
    
    public HoughLineCV(int x, int y, int x2, int y2){
        
    }
 
    public void draw(IplImage image) {
        cvLine(image, new CvPoint(pt1X, pt1Y), new CvPoint(pt2X, pt2Y), CV_RGB(255, 0, 0), 1, CV_AA, 0);
    }
    
    public double distanceToPt(int x, int y){
        double s = Math.abs(A*x+B*y+C);
        double t = Math.sqrt(A*A+B*B);
//        System.out.println(A + ":" + B + " -> " + s/t);
        return s/t;
    }
    
    public double realDistanceToPt(int x, int y){
        double s = A*x+B*y+C;
        double t = Math.sqrt(A*A+B*B);
//        System.out.println(A + ":" + B + " -> " + s/t);
        return s/t;
    }
}
