package robot;

import map.MAP_CONST;
import map.True_Map;
import simulator.Controller;
import simulator.UIController;

import static robot.ORIENTATION.*;

public class Sensor {
    private Controller mController;
    private Robot robotInstance;
    private True_Map realMap;
    private final int maxDistance;
    private final int minDistance;
    private final int sensorOffsetX;
    private final int sensorOffsetY;
    private ORIENTATION sensorDirection;
    /* direction : 1 for right sensor, -1 for left sensor, 0 for front sensor
    * offsetX, +1 for right, -1 for left
    * offsetY, +1 for up, -1 for bottom (maybe)
    */
    public Sensor(ORIENTATION direction, int maxDist, int minDist, int offsetX, int offsetY, Robot robot){
        mController=Controller.getInstance();
        realMap=mController.getTrueArena();
        robotInstance=robot;
        maxDistance=maxDist;
        minDistance=minDist;
        sensorOffsetX=offsetX;
        sensorOffsetY=offsetY;
        sensorDirection=direction;
    }


    public void robotTurnRight(){
        switch (sensorDirection){
            case NORTH:
                sensorDirection=EAST;
                break;
            case EAST:
                sensorDirection=SOUTH;
                break;
            case SOUTH:
                sensorDirection=WEST;
                break;
            case WEST:
                sensorDirection=NORTH;
                break;
        }
    }

    public void robotTurnLeft(){
        switch (sensorDirection){
            case NORTH:
                sensorDirection=WEST;
                break;
            case EAST:
                sensorDirection=NORTH;
                break;
            case SOUTH:
                sensorDirection=EAST;
                break;
            case WEST:
                sensorDirection=SOUTH;
                break;
        }
    }

    public int[] calculateSensorPos(){
        int[] sensorPos = robotInstance.getRobotPosition();
        switch (robotInstance.getRobotOrientation()){
            case NORTH:
                sensorPos[0]+=sensorOffsetX;
                sensorPos[1]+=sensorOffsetY;
                break;
            case EAST:
                sensorPos[0]-=sensorOffsetY;
                sensorPos[1]+=sensorOffsetX;
                break;
            case SOUTH:
                sensorPos[0]-=sensorOffsetX;
                sensorPos[1]-=sensorOffsetY;
                break;
            case WEST:
                sensorPos[0]+=sensorOffsetY;
                sensorPos[1]-=sensorOffsetX;
        }
        //System.out.println(sensorPos[0] + " "+ sensorPos[1]);
        return sensorPos;
    }

    public int sense(){
        switch (sensorDirection){
            case NORTH:
                return getSensorResult(1, -1);
            case EAST:
                return getSensorResult(0, 1);
            case SOUTH:
                return getSensorResult(1, 1);
            case WEST:
                return getSensorResult(0, -1);
            default:
                return -1;
        }
    }


    //0 for x axis, 1 for y axis, +1 for positive dir, -1 for negative dir
    private int getSensorResult(int axis, int dir){
        int[] sensorPos = calculateSensorPos();
        int[] checkCoords = new int[]{sensorPos[0], sensorPos[1]};
        for(int i=minDistance; i<maxDistance; ++i){
            checkCoords[axis]+=dir;
            if(checkValidCoords(checkCoords[0], checkCoords[1])){
                boolean isWall = realMap.IsWallAtCoords(checkCoords[0], checkCoords[1]);
                //mController.updateVirtualArena(checkCoords[0], checkCoords[1], isWall);
                //UIController.getInstance().repaint();
                if (isWall){
                    return i;
                }
            }
        }

        return maxDistance;
    }

    public void processSensorResult(int res){
        switch (sensorDirection){
            case NORTH:
                 processSensorResult(1, -1, res);
                 break;
            case EAST:
                 processSensorResult(0, 1, res);
                 break;
            case SOUTH:
                 processSensorResult(1, 1, res);
                 break;
            case WEST:
                 processSensorResult(0, -1, res);
                 break;
        }
    }

    private void processSensorResult(int axis, int dir, int res){
        int[] sensorPos = calculateSensorPos();
        int[] checkCoords = new int[]{sensorPos[0], sensorPos[1]};
        for(int i=minDistance; i<maxDistance; ++i){
            checkCoords[axis]+=dir;
            if(checkValidCoords(checkCoords[0], checkCoords[1])){
                if(mController.isVirtualArenaUpdateContinue(checkCoords[0], checkCoords[1])){
                    //Either i<res or i!=res, both works in simulator, need to check with the real robot
                    if(i<res){
                        mController.updateVirtualArena(checkCoords[0], checkCoords[1], false);
                    }else{
                        mController.updateVirtualArena(checkCoords[0], checkCoords[1], true);
                        break;
                    }
                }else{
                    break;
                }
            }
        }
    }

    private boolean checkValidCoords(int x, int y){
        if(x<0 || x>MAP_CONST.MAP_GRID_WIDTH-1) return false;
        if(y<0 || y>MAP_CONST.MAP_GRID_HEIGHT-1) return false;
        return true;
    }

    /*
    public void checkRobotFrontSensor(){
        int[] robotPos = robotInstance.getRobotPosition();
        int robotX = robotPos[0];
        int robotY = robotPos[1];
        switch (robotInstance.getRobotOrientation()){
            case NORTH:
                for(int i=-1; i<=1; ++i){
                    for(int j=2; j<=4; j++){
                        //xCoords = from robotCenter-1 to robotCenter+1
                        //yCoords = from robotCenter-2 to robotCenter-4 (to upper side, index towards 0)
                        if(robotX+i>=0 && robotX+i< MAP_CONST.MAP_GRID_WIDTH && robotY-j >=0 && robotY-j<MAP_CONST.MAP_GRID_HEIGHT){
                            boolean isWall = trueArena.IsWallAtCoords(robotX+i, robotY-j);
                            updateVirtualArena(robotX+i, robotY-j, isWall);
                            if(isWall){
                                break;
                            }
                        }
                    }
                }

                break;

            case SOUTH:
                for(int i=-1; i<=1; ++i){
                    for(int j=2; j<=4; j++){
                        //xCoords = from robotCenter-1 to robotCenter+1
                        //yCoords = from robotCenter+2 to robotCenter+4 (to lower side, index towards map height-1)
                        if(robotX+i>=0 && robotX+i< MAP_CONST.MAP_GRID_WIDTH && robotY+j >=0 && robotY+j<MAP_CONST.MAP_GRID_HEIGHT) {
                            boolean isWall = trueArena.IsWallAtCoords(robotX + i, robotY + j);
                            updateVirtualArena(robotX + i, robotY + j, isWall);
                            if(isWall){
                                break;
                            }
                        }
                    }
                }

                break;

            case EAST:
                for(int i=-1; i<=1; ++i){
                    for(int j=2; j<=4; j++){
                        //xCoords = from robotCenter+2 to robotCenter+4 (to the right)
                        //yCoords = from robotCenter-1 to robotCenter+1
                        if(robotX+j>=0 && robotX+j<MAP_CONST.MAP_GRID_WIDTH && robotY+i >=0 && robotY+i<MAP_CONST.MAP_GRID_HEIGHT) {
                            boolean isWall = trueArena.IsWallAtCoords(robotX + j, robotY + i);
                            updateVirtualArena(robotX + j, robotY + i, isWall);
                            if(isWall){
                                break;
                            }
                        }
                    }
                }
                break;

            case WEST:
                for(int i=-1; i<=1; ++i){
                    for(int j=2; j<=4; j++){
                        //xCoords = from robotCenter-2 to robotCenter-4 (to the left)
                        //yCoords = from robotCenter-1 to robotCenter+1
                        if(robotX-j>=0 && robotX-j<MAP_CONST.MAP_GRID_WIDTH && robotY+i >=0 && robotY+i<MAP_CONST.MAP_GRID_HEIGHT) {
                            boolean isWall = trueArena.IsWallAtCoords(robotX - j, robotY + i);
                            updateVirtualArena(robotX - j, robotY + i, isWall);
                            if(isWall){
                                break;
                            }
                        }
                    }
                }
                break;
        }
    }

    public void checkRobotRightSensor(){
        int[] robotPos = robotInstance.getRobotPosition();
        int robotX = robotPos[0];
        int robotY = robotPos[1];
        switch (robotInstance.getRobotOrientation()){
            case NORTH:
                for(int i=-1; i<=1; ++i){
                    for(int j=2; j<=4; j++){
                        //xCoords = from robotCenter-1 to robotCenter+1
                        //yCoords = from robotCenter-2 to robotCenter-4 (to upper side, index towards 0)
                        if(robotX+j>=0 && robotX+j<MAP_CONST.MAP_GRID_WIDTH && robotY+i >=0 && robotY+i<MAP_CONST.MAP_GRID_HEIGHT){
                            boolean isWall = trueArena.IsWallAtCoords(robotX+j, robotY+i);
                            updateVirtualArena(robotX+j, robotY+i, isWall);
                            if(isWall){
                                break;
                            }
                        }
                    }
                }

                break;

            case SOUTH:
                for(int i=-1; i<=1; ++i){
                    for(int j=2; j<=4; j++){
                        //xCoords = from robotCenter-1 to robotCenter+1
                        //yCoords = from robotCenter+2 to robotCenter+4 (to lower side, index towards map height-1)
                        if(robotX-j>=0 && robotX-j<MAP_CONST.MAP_GRID_WIDTH && robotY+i >=0 && robotY+i<MAP_CONST.MAP_GRID_HEIGHT) {
                            boolean isWall = trueArena.IsWallAtCoords(robotX - j, robotY + i);
                            updateVirtualArena(robotX - j, robotY + i, isWall);
                            if(isWall){
                                break;
                            }
                        }
                    }
                }

                break;

            case EAST:
                for(int i=-1; i<=1; ++i){
                    for(int j=2; j<=4; j++){
                        //xCoords = from robotCenter+2 to robotCenter+4 (to the right)
                        //yCoords = from robotCenter-1 to robotCenter+1
                        if(robotX+i>=0 && robotX+i<MAP_CONST.MAP_GRID_WIDTH && robotY+j >=0 && robotY+j<MAP_CONST.MAP_GRID_HEIGHT) {
                            boolean isWall = trueArena.IsWallAtCoords(robotX + i, robotY + j);
                            updateVirtualArena(robotX + i, robotY + j, isWall);
                            if (isWall){
                                break;
                            }
                        }
                    }
                }
                break;

            case WEST:
                for(int i=-1; i<=1; ++i){
                    for(int j=2; j<=4; j++){
                        //xCoords = from robotCenter-2 to robotCenter-4 (to the left)
                        //yCoords = from robotCenter-1 to robotCenter+1
                        if(robotX+i>=0 && robotX+i<MAP_CONST.MAP_GRID_WIDTH && robotY-j >=0 && robotY-j<MAP_CONST.MAP_GRID_HEIGHT) {
                            boolean isWall = trueArena.IsWallAtCoords(robotX + i, robotY - j);
                            updateVirtualArena(robotX + i, robotY - j, isWall);
                            if(isWall){
                                break;
                            }
                        }
                    }
                }
                break;
        }
    }

    public void checkRobotLeftSensor(){
        int[] robotPos = robotInstance.getRobotPosition();
        int robotX = robotPos[0];
        int robotY = robotPos[1];
        switch (robotInstance.getRobotOrientation()){
            case NORTH:
                for(int i=-1; i<=1; ++i){
                    for(int j=2; j<=4; j++){
                        //xCoords = from robotCenter-1 to robotCenter+1
                        //yCoords = from robotCenter-2 to robotCenter-4 (to upper side, index towards 0)
                        if(robotX-j>=0 && robotX-j<MAP_CONST.MAP_GRID_WIDTH && robotY+i >=0 && robotY+i<MAP_CONST.MAP_GRID_HEIGHT) {
                            boolean isWall = trueArena.IsWallAtCoords(robotX - j, robotY + i);
                            updateVirtualArena(robotX - j, robotY + i, isWall);
                            if(isWall){
                                break;
                            }
                        }
                    }
                }

                break;

            case SOUTH:
                for(int i=-1; i<=1; ++i){
                    for(int j=2; j<=4; j++){
                        //xCoords = from robotCenter-1 to robotCenter+1
                        //yCoords = from robotCenter+2 to robotCenter+4 (to lower side, index towards map height-1)
                        if(robotX+j>=0 && robotX+j<MAP_CONST.MAP_GRID_WIDTH && robotY+i >=0 && robotY+i<MAP_CONST.MAP_GRID_HEIGHT) {
                            boolean isWall = trueArena.IsWallAtCoords(robotX + j, robotY + i);
                            updateVirtualArena(robotX + j, robotY + i, isWall);
                            if (isWall){
                                break;
                            }
                        }
                    }
                }

                break;

            case EAST:
                for(int i=-1; i<=1; ++i){
                    for(int j=2; j<=4; j++){
                        //xCoords = from robotCenter+2 to robotCenter+4 (to the right)
                        //yCoords = from robotCenter-1 to robotCenter+1
                        if(robotX+i>=0 && robotX+i<MAP_CONST.MAP_GRID_WIDTH && robotY-j >=0 && robotY-j<MAP_CONST.MAP_GRID_HEIGHT) {
                            boolean isWall = trueArena.IsWallAtCoords(robotX + i, robotY - j);
                            updateVirtualArena(robotX + i, robotY - j, isWall);
                            if(isWall){
                                break;
                            }
                        }
                    }
                }
                break;

            case WEST:
                for(int i=-1; i<=1; ++i){
                    for(int j=2; j<=4; j++){
                        //xCoords = from robotCenter-2 to robotCenter-4 (to the left)
                        //yCoords = from robotCenter-1 to robotCenter+1
                        if(robotX+i>=0 && robotX+i<MAP_CONST.MAP_GRID_WIDTH && robotY+j >=0 && robotY+j<MAP_CONST.MAP_GRID_HEIGHT) {
                            boolean isWall = trueArena.IsWallAtCoords(robotX + i, robotY + j);
                            updateVirtualArena(robotX + i, robotY + j, isWall);
                            if (isWall){
                                break;
                            }
                        }
                    }
                }
                break;
        }
    }*/

}
