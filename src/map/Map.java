package map;

import java.util.ArrayList;

public class Map {
    public class Map_Cells{
        boolean isTrueWall=false;
        boolean isVirtualWall = false;
        boolean isExplored = false;

        private void SetTrueWall(boolean val){
            isTrueWall=val;
        }

        private void SetVirtualWall(boolean val){
            isVirtualWall=val;
        }

        public boolean GetVirtualWall(){
            return isVirtualWall;
        }

        public boolean GetTrueWall(){
            return isTrueWall;
        }
    }

    public Map_Cells[][] fullMap = new Map_Cells[MAP_CONST.MAP_GRID_HEIGHT][MAP_CONST.MAP_GRID_WIDTH];

    public Map(){
        for(int i = 0; i< MAP_CONST.MAP_GRID_HEIGHT; ++i){
            for(int j = 0; j< MAP_CONST.MAP_GRID_WIDTH; ++j){
                fullMap[i][j] = new Map_Cells();
                if(i==0 || i== MAP_CONST.MAP_GRID_HEIGHT-1){
                    fullMap[i][j].SetVirtualWall(true);
                }else if(j==0 || j== MAP_CONST.MAP_GRID_WIDTH-1){
                    fullMap[i][j].SetVirtualWall(true);
                }
            }
        }
        InitStartZone();
    }

    public void SetTrueWallAt(int xPos, int yPos){
        //The actual wall
        fullMap[yPos][xPos].SetTrueWall(true);

        //The virtual wall on the surrounding 8 cells
        if(yPos>0){
            fullMap[yPos-1][xPos].SetVirtualWall(true);
            if(xPos>0){
                fullMap[yPos-1][xPos-1].SetVirtualWall(true);
            }
            if(xPos<MAP_CONST.MAP_GRID_WIDTH-1){
                fullMap[yPos-1][xPos+1].SetVirtualWall(true);
            }
        }
        if(yPos<MAP_CONST.MAP_GRID_HEIGHT-1){
            fullMap[yPos+1][xPos].SetVirtualWall(true);
            if(xPos>0){
                fullMap[yPos+1][xPos-1].SetVirtualWall(true);
            }
            if(xPos<MAP_CONST.MAP_GRID_WIDTH-1){
                fullMap[yPos+1][xPos+1].SetVirtualWall(true);
            }
        }

        if(xPos>0){
            fullMap[yPos][xPos-1].SetVirtualWall(true);
        }
        if(xPos<MAP_CONST.MAP_GRID_WIDTH-1){
            fullMap[yPos][xPos+1].SetVirtualWall(true);

        }
    }


    public int GetMapHeight(){
        return fullMap.length;
    }

    public int GetMapWidth(){
        return fullMap[0].length;
    }

    public ACCESS CheckIsAccessible(int x, int y){
        if(x<0 || x>=MAP_CONST.MAP_GRID_WIDTH){
            return ACCESS.NO;
        }
        if(y<0 || y>=MAP_CONST.MAP_GRID_HEIGHT){
            return ACCESS.NO;
        }
        if(fullMap[y][x].isExplored){
            //Inaccessible by robot is virtual wall is true
            if(fullMap[y][x].GetVirtualWall()){
                return ACCESS.NO;
            }else{
                return ACCESS.YES;
            }
        }else{
            return ACCESS.UNEXPLORED;
        }
    }

    public void SetExplored(int xPos, int yPos){
        fullMap[yPos][xPos].isExplored=true;
    }

    private void InitStartZone(){
        for(int i=-1; i<=1; ++i){
            for(int j=-1; j<=1; j++){
                fullMap[MAP_CONST.ROBOT_START_ZONE_CENTER_Y+i][MAP_CONST.ROBOT_START_ZONE_CENTER_X+j].isExplored=true;
            }
        }
    }

    private ArrayList<int[]> getUnexploredCoords(){
        ArrayList<int[]> unexploredTiles = new ArrayList<int[]>();
        for(int i=0; i<MAP_CONST.MAP_GRID_HEIGHT-1; ++i){
            for(int j=0; j<MAP_CONST.MAP_GRID_WIDTH-1; ++j){
                if(!fullMap[i][j].isExplored){
                    unexploredTiles.add(new int[]{j,i});
                }
            }
        }

        return unexploredTiles;
    }

    private String binToHex(String bin){
        int dec = Integer.parseInt(bin, 2);

        return Integer.toHexString(dec);
    }

    //Part 1 of mdf string, is the grid explored or not
    public String GetMdfStringExplored(){
        StringBuilder part1 = new StringBuilder();
        StringBuffer binTemp = new StringBuffer(4);
        binTemp.append("11");
        for(int y=0; y<MAP_CONST.MAP_GRID_HEIGHT; ++y){
            for(int x=0; x<MAP_CONST.MAP_GRID_WIDTH; ++x){
                if(binTemp.length()==4){
                    part1.append(binToHex(binTemp.toString()));
                    binTemp.delete(0,binTemp.capacity());
                }
                if(fullMap[y][x].isExplored){
                    binTemp.append('1');
                }else{
                    binTemp.append('0');
                }
            }
        }
        if(binTemp.length()>0){
            binTemp.append("11");
            part1.append(binToHex(binTemp.toString()));
        }
        System.out.println(part1);

        return part1.toString();
    }

    //Part 2 of mdf string, is the grid a wall or not
    public String GetMdfStringIsWall(){
        StringBuilder part2 = new StringBuilder();
        StringBuffer binTemp = new StringBuffer(4);

        for(int y=0; y<MAP_CONST.MAP_GRID_HEIGHT; ++y){
            for(int x=0; x<MAP_CONST.MAP_GRID_WIDTH; ++x){
                if(binTemp.length()==4){
                    part2.append(binToHex(binTemp.toString()));
                    binTemp.delete(0, binTemp.capacity());
                }
                if(fullMap[y][x].isExplored){
                    if(fullMap[y][x].isTrueWall){
                        binTemp.append('1');
                    }else{
                        binTemp.append('0');
                    }
                }
            }
        }

        if(binTemp.length()>0){
            part2.append(binToHex(binTemp.toString()));
        }

        System.out.println(part2.toString());

        return part2.toString();
    }
}
