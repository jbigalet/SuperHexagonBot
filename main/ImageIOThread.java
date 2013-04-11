package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import javax.imageio.ImageIO;

public class ImageIOThread extends Thread{
    
    private final BlockingQueue<ImageIOInfo> imageInfoList;
    private String directory;
    private String format;

    public ImageIOThread(BlockingQueue<ImageIOInfo> imageInfoList, String directory, String format) {
        this.imageInfoList = imageInfoList;
        this.directory = directory;
        this.format = format;
    }
    
    @Override
    public void run(){
        try {
            while(true){
                ImageIOInfo imageInfoToProcess = imageInfoList.take();
                long time = System.currentTimeMillis();
                ImageIO.write(imageInfoToProcess.image, format, new File(directory + imageInfoToProcess.name + "." + format));
//                System.out.println("Image [" + count + "] saved in: " + (System.currentTimeMillis()-time) + "ms");
            }
        } catch (InterruptedException | IOException e){
            e.printStackTrace();
        }
    }
    
}
