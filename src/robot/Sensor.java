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
    private final boolean isFrontSensor;
    /* direction : 1 for right sensor, -1 for left sensor, 0 for front sensor
    * offsetX, +1 for right, -1 for left
    * offsetY, +1 for up, -1 for bottom (maybe)
    */
    public Sensor(ORIENTATION direction, int maxDist, int minDist, int offsetX, int offsetY, Robot robot, boolean isFrontFacing){
        mController=Controller.getInstance();
        realMap=mController.getTrueArena();
        isFrontSensor=isFrontFacing;
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
        for(int i=minDistance; i<res; ++i){
            checkCoords[axis]+=dir;
            if(checkValidCoords(checkCoords[0], checkCoords[1])){
                mController.updateVirtualArena(checkCoords[0], checkCoords[1], false, isFrontSensor);
            }
        }
        if(res<maxDistance){
            checkCoords[axis]+=dir;
            if(checkValidCoords(checkCoords[0],checkCoords[1])){
                mController.updateVirtualArena(checkCoords[0], checkCoords[1], true, isFrontSensor);
            }
        }
    }

    private boolean checkValidCoords(int x, int y){
        if(x<0 || x>MAP_CONST.MAP_GRID_WIDTH-1) return false;
        if(y<0 || y>MAP_CONST.MAP_GRID_HEIGHT-1) return false;
        return true;
    }

}
