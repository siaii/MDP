package robot;

import java.util.Arrays;

import simulator.Controller;
import simulator.PCClient;

import static robot.ORIENTATION.*;

public class Robot {

    private Controller mainController;
    private ORIENTATION robotOrientation;
    private int[] robotPosition = new int[2];
    private boolean isRealBot;
    private PCClient pcClient;

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
        pcClient = PCClient.getInstance();

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
        mainController.TakePicture();
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
        mainController.TakePicture();
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

    /*
        sensorResult[0] = front left
        sensorResult[1] = front mid
        sensorResult[2] = front right
        sensorResult[3] = left front
        sensorResult[4] = left bottom
        sensorResult[5] = right bottom
     */
    public void SenseAll(){
        int[] sensorResult = new int[6];
        if(isRealBot){
            sensorResult[0]=Front_Front_Left_Sensor.sense();
            sensorResult[1]=Front_Front_Mid_Sensor.sense();
            sensorResult[2]=Front_Front_Right_Sensor.sense();
            sensorResult[3]=Left_Front_Left_Sensor.sense();
            sensorResult[4]=Left_Bottom_Left_Sensor.sense();
            sensorResult[5]=Right_Bottom_Right_Sensor.sense();
        }else{
            //get real sensor data here
            String[] sensorData = pcClient.receivePacket().split(",");
            sensorResult = Arrays.asList(sensorData).stream().mapToInt(Integer::parseInt).toArray();
        }

        Front_Front_Left_Sensor.processSensorResult(sensorResult[0]);
        Front_Front_Mid_Sensor.processSensorResult(sensorResult[1]);
        Front_Front_Right_Sensor.processSensorResult(sensorResult[2]);
        Left_Front_Left_Sensor.processSensorResult(sensorResult[3]);
        Left_Bottom_Left_Sensor.processSensorResult(sensorResult[4]);
        Right_Bottom_Right_Sensor.processSensorResult(sensorResult[5]);

    }

    public String mdfString() {
        String mdf = mainController.getMdfString();
        //Convert from (0,0) on top left to bottom left to accommodate for android
        String robotPosString = robotPosition[0] + ","+ (19-robotPosition[1]);
        String robotDirection = convertDirToInt();
        String msg = mdf +","+robotPosString+","+robotDirection;
        //Send mdf string to android

        return msg;
    }

    //Number to change later
    private String convertDirToInt(){
        switch (robotOrientation){
            case NORTH:
                return "0";
            case EAST:
                return "1";
            case SOUTH:
                return "2";
            case WEST:
                return "3";
        }
        return "0";
    }

}
