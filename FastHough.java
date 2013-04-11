
import java.awt.Color;

public class FastHough {

    public static boolean[][] fastEdgeDetector(int[][][] raster){
        long time = System.currentTimeMillis();
        boolean[][] canny = new boolean[raster.length][raster[0].length];
        for(int i=1 ; i<canny.length-1 ; i++)
            for(int j=1 ; j<canny[0].length-1 ; j++)
                if(isEdge(raster, i, j)){
                    canny[i][j] = true;
                    canny[i+1][j] = true;
                    canny[i-1][j] = true;
                    canny[i][j+1] = true;
                    canny[i][j-1] = true;
                }
        System.out.println("Edge detection done in: " + (System.currentTimeMillis()-time) + "ms");
        return canny;
    }
    
    private static boolean isEdge(int[][][] raster, int x, int y){
        if(colorDif(raster[x][y], raster[x+1][y]) > 10) return true;
        if(colorDif(raster[x][y], raster[x-1][y]) > 10) return true;
        if(colorDif(raster[x][y], raster[x][y+1]) > 10) return true;
        if(colorDif(raster[x][y], raster[x][y-1]) > 10) return true;
        return false;
    }
    
    public static int colorDif(int[] a, int[] b){
        return Math.abs(a[0]-b[0]) + Math.abs(a[1]-b[1]) + Math.abs(a[2]-b[2]);
    }
    
}
