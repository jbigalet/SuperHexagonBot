
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class JFrameRectangleDebug extends JFrame {

    List<CRectangle>[] rects = (List<CRectangle>[]) new ArrayList[6];

    public JFrameRectangleDebug() {
        super("Super Hexagon Bot Rectangles Debug Window");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLocation(0, 530);

        setVisible(true);
    }

    public void paint(Graphics g) {
//        g.drawImage(imageToBeDraw, 0, 0, this);
        super.paintComponents(g);
        Graphics2D g2d = (Graphics2D) g;
        for(int i=0 ; i<6 ; i++)
            if(rects[i] != null)
                for(CRectangle rect : rects[i]){
                    g2d.setColor(new Color(rect.color));
//                    if(rect.color != 0xff0000)
//                        g2d.setColor(SuperHexagonBot.colorDif(rect.color, 0x000000)<200 ? Color.BLACK : Color.CYAN);
//                    else
//                        g2d.setColor(Color.RED);
                    g2d.fillRect(150*i, rect.start, 150, rect.end-rect.start);
                }
        g2d.dispose();
    }
    
    public void reloadRectangles(List<CRectangle>[] rects) {
        this.rects = rects;
        repaint();
    }
    
}