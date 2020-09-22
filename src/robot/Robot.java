package robot;

import simulator.Controller;

import static robot.ORIENTATION.*;

public class Robot {

    private Controller mainController;
    private ORIENTATION robotOrientation;
    private int[] robotPosition = new int[2];
    private boolean isRealBot;

    /*
        Sensor format: Direction_Row_Col_Sensor
     */

    private Sensor Front_Front_Left_Sensor;
    private Sensor Front_Front_Mid_Sensor;
    private Sensor Front_Front_Right_Sensor;
    private Sensor Left_Front_Left_Sensor;
    private Sensor Left_Bottom_Left_Sensor;
    private Sensor Right_Bottom_Right_Sensor; //Long range



    public Robot(int startX, int startY, ORIENTATION startOrientation, boolean isReal){
        mainController= Controller.getInstance();

        isRealBot=isReal;
        robotPosition[0] = startX;
        robotPosition[1] = startY;
        robotOrientation = startOrientation;

        Front_Front_Left_Sensor=new Sensor(NORTH, ROBOT_CONST.SHORT_RANGE_MAX_DISTANCE, ROBOT_CONST.SHORT_RANGE_MIN_DISTANCE, -1, -1, this);
        Front_Front_Mid_Sensor=new Sensor(NORTH, ROBOT_CONST.SHORT_RANGE_MAX_DISTANCE, ROBOT_CONST.SHORT_RANGE_MIN_DISTANCE, 0, -1, this);
        Front_Front_Right_Sensor=new Sensor(NORTH, ROBOT_CONST.SHORT_RANGE_MAX_DISTANCE, ROBOT_CONST.SHORT_RANGE_MIN_DISTANCE, 1, -1, this);
        Left_Front_Left_Sensor=new Sensor(WEST, ROBOT_CONST.SHORT_RANGE_MAX_DISTANCE, ROBOT_CONST.SHORT_RANGE_MIN_DISTANCE, -1, -1, this);
        Left_Bottom_Left_Sensor=new Sensor(WEST, ROBOT_CONST.SHORT_RANGE_MAX_DISTANCE, ROBOT_CONST.SHORT_RANGE_MIN_DISTANCE, -1, 1, this);
        Right_Bottom_Right_Sensor=new Sensor(EAST, ROBOT_CONST.LONG_RANGE_MAX_DISTANCE, ROBOT_CONST.LONG_RANGE_MIN_DISTANCE, 1, 1, this);
    }


    public int[] getRobotPosition(){
        return new int[]{robotPosition[0], robotPosition[1]};
    }

    private void updateSensorDirLeft(){
        Front_Front_Left_Sensor.robotTurnLeft();
        Front_Front_Mid_Sensor.robotTurnLeft();
        Front_Front_Right_Sensor.robotTurnLeft();
        Left_Bottom_Left_Sensor.robotTurnLeft();
        Left_Front_Left_Sensor.robotTurnLeft();
        Right_Bottom_Right_Sensor.robotTurnLeft();
    }

    private void updateSensorDirRight(){
        Front_Front_Left_Sensor.robotTurnRight();
        Front_Front_Mid_Sensor.robotTurnRight();
        Front_Front_Right_Sensor.robotTurnRight();
        Left_Bottom_Left_Sensor.robotTurnRight();
        Left_Front_Left_Sensor.robotTurnRight();
        Right_Bottom_Right_Sensor.robotTurnRight();
    }


    public ORIENTATION getRobotOrientation(){
        return robotOrientation;
    }

    public void Turn_Right(){
        System.out.println("turning right");
        switch (robotOrientation){
            case NORTH:
                robotOrientation= EAST;
                break;
            case EAST:
                robotOrientation= SOUTH;
                break;
            case SOUTH:
                robotOrientation= WEST;
                break;
            case WEST:
                robotOrientation= NORTH;
                break;
        }
        updateSensorDirRight();
    }

    public void Turn_Left(){
        switch (robotOrientation){
            case NORTH:
                robotOrientation= WEST;
                break;
            case WEST:
                robotOrientation= SOUTH;
                break;
            case SOUTH:
                robotOrientation= EAST;
                break;
            case EAST:
                robotOrientation= NORTH;
                break;
        }
        updateSensorDirLeft();
    }

    public void Move_Forward(){
        switch (robotOrientation){
            case NORTH:
                robotPosition[1]--;
                break;
            case SOUTH:
                robotPosition[1]++;
                break;
            case WEST:
                robotPosition[0]--;
                break;
            case EAST:
                robotPosition[0]++;
        }
    }

    public void Move_Forward(int steps){
        switch (robotOrientation){
            case NORTH:
                robotPosition[1]-=steps;
                break;
            case SOUTH:
                robotPosition[1]+=steps;
                break;
            case WEST:
                robotPosition[0]-=steps;
                break;
            case EAST:
                robotPosition[0]+=steps;
        }
    }

    public void Move_Back(){
        switch (robotOrientation){
            case NORTH:
                robotPosition[1]++;
                break;
            case SOUTH:
                robotPosition[1]--;
                break;
            case WEST:
                robotPosition[0]++;
                break;
            case EAST:
                robotPosition[0]--;
        }
    }

    public void SenseFront() {
        if(isRealBot){
            Front_Front_Left_Sensor.senseReal();
            Front_Front_Mid_Sensor.senseReal();
            Front_Front_Right_Sensor.senseReal();
        }else{
            Front_Front_Left_Sensor.sense();
            Front_Front_Mid_Sensor.sense();
            Front_Front_Right_Sensor.sense();
        }
    }

    public void SenseLeft(){
        if(isRealBot){
            Left_Front_Left_Sensor.senseReal();
            Left_Bottom_Left_Sensor.senseReal();
        }else{
            Left_Front_Left_Sensor.sense();
            Left_Bottom_Left_Sensor.sense();
        }
    }

    public void SenseRight(){
        if(isRealBot){
            Right_Bottom_Right_Sensor.senseReal();
        }else{
            Right_Bottom_Right_Sensor.sense();
        }
    }
}
