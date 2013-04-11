
import static com.googlecode.javacv.cpp.opencv_core.*;
import java.util.Vector;

public class HoughCV {
    IplImage edgeImage;
    Vector<HoughLineCV> lines;
    
    public HoughCV(IplImage edgeImage, Vector<HoughLineCV> lines){
        this.edgeImage = edgeImage;
        this.lines = lines;
    }
}
