package robot;

import simulator.Controller;

public class Camera {
    Controller mController;
    public Camera(){
        mController=Controller.getInstance();
    }

    private int[] CalculateImgCoords(float sensorDist, float horizontalDist){
        int[] robotPos = mController.getRobotPos();
        int imgPosX, imgPosY;
        int offsetX, offsetY;
        //Assume horizontal dist is positive for right side, negative for left side of image's center
        switch (mController.getRobotOrientation()){
            case NORTH:
                offsetX = Math.round(horizontalDist/10);
                offsetY = (int) Math.ceil(sensorDist/10);
                imgPosX = robotPos[0]+offsetX;
                imgPosY = robotPos[1]-offsetY-1;
                break;
            case SOUTH:
                offsetX = Math.round(horizontalDist/10);
                offsetY = (int) Math.ceil(sensorDist/10);
                imgPosX = robotPos[0]+offsetX;
                imgPosY = robotPos[1]+offsetY+1;
                break;
            case WEST:
                offsetX = (int) Math.ceil(sensorDist/10);
                offsetY = Math.round(horizontalDist/10);
                imgPosX = robotPos[0]-offsetX-1;
                imgPosY = robotPos[1]+offsetY;
                break;
            case EAST:
                offsetX = (int) Math.ceil(sensorDist/10);
                offsetY = Math.round(horizontalDist/10);
                imgPosX = robotPos[0]+offsetX+1;
                imgPosY = robotPos[1]+offsetY;
                break;
            default:
                imgPosX=robotPos[0];
                imgPosY=robotPos[1];
                break;
        }

        return new int[]{imgPosX, imgPosY};
    }

    public void TakePicture(){
        //Send command to rpi to take pic
        //Receive info from python
        //Send info to android
        //Update map here?
    }
}
