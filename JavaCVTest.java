
import com.googlecode.javacpp.Pointer;
import com.googlecode.javacv.CanvasFrame;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import java.awt.image.BufferedImage;
import java.util.Vector;

public class JavaCVTest {
    
    public static BufferedImage CannyEdgeDetector(BufferedImage oldImage){
        IplImage image = IplImage.createFrom(oldImage);

	//create grayscale IplImage of the same dimensions, 8-bit and 1 channel
        IplImage imageGray = cvCreateImage(cvSize(image.width(), image.height()), IPL_DEPTH_8U, 1);
        
        //convert image to grayscale
        cvCvtColor(image, imageGray, CV_BGR2GRAY );
 		
        IplImage gray = new IplImage(imageGray);
        IplImage edge = cvCreateImage(cvSize(gray.width(), gray.height()), IPL_DEPTH_8U, 1);
        
        //run Canny edge detection..
        cvCanny(gray, edge, 0.5f, 1f, 7);
        
        BufferedImage bImage = edge.getBufferedImage(edge.getBufferedImageType() == BufferedImage.TYPE_CUSTOM ? 1.0 : 1.0, false);
        
        return bImage;
    }
    
    public static HoughCV HoughLines(BufferedImage oldImage) {
        long time = System.currentTimeMillis();
        
        IplImage src = IplImage.createFrom(oldImage);
        IplImage dst = cvCreateImage(cvGetSize(src), src.depth(), 1);
        IplImage colorDst = cvCreateImage(cvGetSize(src), src.depth(), 3);

        cvCanny(src, dst, 0.5f, 1f, 7);
        cvCvtColor(dst, colorDst, CV_GRAY2BGR);

        CvMemStorage storage = cvCreateMemStorage(0);
        CvSeq lines = cvHoughLines2(dst, storage, CV_HOUGH_STANDARD, 1, Math.PI / 180, 50, 0, 0);
        Vector<HoughLineCV> houghLines = new Vector<>();
        for (int i = 0; i < lines.total(); i++) {
            CvPoint2D32f point = new CvPoint2D32f(cvGetSeqElem(lines, i));
            float rho=point.x();
            float theta=point.y();
            houghLines.add(new HoughLineCV(theta, rho));
        }
        
        System.out.println("HoughLines done in " + (System.currentTimeMillis()-time) + "ms");
        return new HoughCV(colorDst, houghLines);

    }
    
    public static BufferedImage HoughLinesProba(BufferedImage oldImage) {
        IplImage src = IplImage.createFrom(oldImage);
        IplImage dst;
        IplImage colorDst;
        CvMemStorage storage = cvCreateMemStorage(0);
        CvSeq lines = new CvSeq();

        dst = cvCreateImage(cvGetSize(src), src.depth(), 1);
        colorDst = cvCreateImage(cvGetSize(src), src.depth(), 3);

        cvCanny(src, dst, 50, 200, 3);
        cvCvtColor(dst, colorDst, CV_GRAY2BGR);

        lines = cvHoughLines2(dst, storage, CV_HOUGH_PROBABILISTIC, 1, Math.PI / 180, 40, 50, 10);
        for (int i = 0; i < lines.total(); i++) {
            Pointer line = cvGetSeqElem(lines, i);
            CvPoint pt1  = new CvPoint(line).position(0);
            CvPoint pt2  = new CvPoint(line).position(1);

//            System.out.println("Line spotted: ");
//            System.out.println("\t pt1: " + pt1);
//            System.out.println("\t pt2: " + pt2);
            cvLine(colorDst, pt1, pt2, CV_RGB(255, 0, 0), 3, CV_AA, 0);
        }

        BufferedImage bImage = colorDst.getBufferedImage(colorDst.getBufferedImageType() == BufferedImage.TYPE_CUSTOM ? 1.0 : 1.0, false);
        
        return bImage;

    }

}
