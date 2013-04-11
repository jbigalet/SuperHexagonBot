
import javax.swing.JFrame;
import javax.swing.ImageIcon;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr;

public class JFrameDebug extends JFrame {

    Image imageToBeDraw;
    ImageIcon ii;
    double theta;

    public JFrameDebug() {
        super("Super Hexagon Bot Debug Window");

        imageToBeDraw = Toolkit.getDefaultToolkit().getImage("C:\\plop.jpg");

        ii = new ImageIcon(imageToBeDraw);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(ii.getIconWidth(), ii.getIconHeight());

        setVisible(true);
    }

    public void paint(Graphics g) {
//        g.drawImage(imageToBeDraw, 0, 0, this);
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(this.getWidth() / 2, this.getHeight() / 2);
        g2d.rotate(-theta);
        g2d.translate(-imageToBeDraw.getWidth(null) / 2, -imageToBeDraw.getHeight(null) / 2);
        g2d.drawImage(imageToBeDraw, 0, 0, null);
        g2d.dispose();
    }
    
    public void reloadImage(BufferedImage image, double rotation) throws IOException{
        theta = rotation;
        imageToBeDraw.flush();
        imageToBeDraw = image;
        repaint();
    }
}
