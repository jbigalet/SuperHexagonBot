package main;

import java.util.Map;
import java.util.TreeMap;

public class DecisionBot {

    final static int maxWall = 120;
    
    public static int getDirectionToGo(GameImage gameImage){
        int[] minWalls = gameImage.minWalls;
        double position = gameImage.position;
        int iPosition = (int) position;
        
        int toRet;
        if(minWalls[iPosition] > maxWall)
            toRet = 0;
        else {
            int diffWalls = minWalls[mod6(iPosition+1)] - minWalls[mod6(iPosition-1)];
            int maxWall = Math.max(minWalls[mod6(iPosition+1)], minWalls[mod6(iPosition-1)]);
            if( maxWall > maxWall )
                toRet = ( diffWalls > 0 ) ? 1 : -1;
            else {
                TreeMap<Integer,Integer> reachableWallPos = new TreeMap<>();
                getReachableWallPos(minWalls, 0, 0, reachableWallPos);
                int[] reachableWall = new int[reachableWallPos.size()];
                int i = 0;
                for(int wallPos : reachableWallPos.keySet())
                    reachableWall[i++] = minWalls[wallPos];
                
                int maxWallPosInReachableArray = Utility.maxPos(reachableWall);
                if(maxWallPosInReachableArray != -1){
                    int iToGet = 0;
                    while( reachableWall[maxWallPosInReachableArray] != minWalls[iToGet++] ) ;
                    
                    if( reachableWallPos.get(iToGet-1) != null){
                        if( reachableWallPos.get(iToGet-1) < 0 )
                            toRet = -1;
                        else
                            toRet = 1;
                    } else {
                        System.out.println("Little problem in my reachable shit");
                        return 0;
                    }
                } else {
                    System.out.println("Little problem in my reachable shit");
                    return 0;
                }
            }
        }
        
        if( position % (1d) < 0.1) {
            System.out.println("edge");
            if( minWalls[mod6(iPosition - 1)] > maxWall ){
                return -1;
            } else if( minWalls[iPosition] > maxWall ){
                return 1;
            } else {
                return toRet;
            }
        } else if( position % (1d) > 0.9) {
            System.out.println("edge");
            if( minWalls[mod6(iPosition + 1)] > maxWall ){
                return 1;
            } else if( minWalls[iPosition] > maxWall ){
                return -1;
            } else {
                return toRet;
            }
        }
        
        return toRet;
    }
    
    public static int mod6( int i ){
        if( i < 0 ) return i+6;
        if( i > 5 ) return i-6;
        return i;
    }
    
    public static void getReachableWallPos(int[] walls, int pos, int depth, Map<Integer,Integer> reachableWalls){
        if( !reachableWalls.containsKey(pos) )
            if( walls[pos] > 40 ){
                reachableWalls.put( pos, depth );
                getReachableWallPos(walls, mod6(pos-1), depth-1, reachableWalls);
                getReachableWallPos(walls, mod6(pos+1), depth+1, reachableWalls);
            }
            
    }
    
}
