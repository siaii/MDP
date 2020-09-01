package robot;

import simulator.Controller;

import static robot.ORIENTATION.*;

public class Robot {

    private Controller mainController;
    private ORIENTATION robotOrientation;
    private int[] robotPosition = new int[2];

    public Robot(int startX, int startY, ORIENTATION startOrientation){
        mainController= Controller.getInstance();

        robotPosition[0] = startX;
        robotPosition[1] = startY;
        robotOrientation = startOrientation;
    }

    public Robot(){}

    public int[] getRobotPosition(){
        return robotPosition;
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
        int forwardSensorDistance = 3; //This is assumed, confirm with other later
        mainController.checkRobotFrontSensor();
    }

    public void SenseLeft(){
        mainController.checkRobotLeftSensor();
    }

    public void SenseRight(){
        mainController.checkRobotRightSensor();
    }
}
