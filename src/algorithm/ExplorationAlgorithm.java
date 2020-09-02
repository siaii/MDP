package algorithm;

import map.MAP_CONST;
import simulator.Controller;
import simulator.UIController;

import javax.swing.*;
import java.util.List;

public class ExplorationAlgorithm {
    private Controller mController;
    private UIController ui;

    public ExplorationAlgorithm(){
        mController = Controller.getInstance();
        ui = UIController.getInstance();
    }

    private void GetControllerInstance(){
        if(mController==null){
            mController= Controller.getInstance();
        }
        if(ui==null){
            ui = UIController.getInstance();
        }
    }
    public void exploreArena() throws InterruptedException {
        //May need this part when using the real robot, it's the same as the one below
        /*int[] startPos = {map.MAP_CONST.ROBOT_START_ZONE_CENTER_X, map.MAP_CONST.ROBOT_START_ZONE_CENTER_Y};
        int[] currRobotPos = virtualRobot.getRobotPosition();
        do {
            if (CheckLeftIsAccessible(currRobotPos[0], currRobotPos[1])) {
                robotTurnLeft();
                robotMoveForward();
            }else {
                if(!checkRobotFront()){
                    robotTurnRight();
                }
                robotMoveForward();
            }
            Thread.sleep(200);
            ui.repaint();
            currRobotPos=virtualRobot.getRobotPosition();

            /*if(currRobotPos[0]==startPos[0] && currRobotPos[1] == startPos[1]){
                System.out.println("robot returned");
            }else{
                System.out.println("robot at : "+currRobotPos[0] + ", " + currRobotPos[1]);
                System.out.println("starting point at: "+startPos[0]+ ", " + startPos[1]);
            }
        } while (currRobotPos[0] != startPos[0] || currRobotPos[1] !=startPos[1]);*/


        ExploreTask explore = new ExploreTask();
        explore.execute();
    }

    //Exploration class multithreading
    private class ExploreTask extends SwingWorker<Void, int[]> {
        @Override
        protected Void doInBackground() throws Exception {
            int[] startPos = {MAP_CONST.ROBOT_START_ZONE_CENTER_X, MAP_CONST.ROBOT_START_ZONE_CENTER_Y};
            int[] currRobotPos = mController.getRobotPos();
            do {
                //If the left side of robot is accessible, then the robot will go there
                if (mController.CheckLeftIsAccessible(currRobotPos[0], currRobotPos[1])) {
                    mController.robotTurnLeft();
                    mController.robotMoveForward();
                }else {
                    //If the robot's front is inaccessible, turn right
                    if(!mController.checkRobotFront()){
                        mController.robotTurnRight();
                    }

                    mController.robotMoveForward();
                }

                //Sleep is for simulator only

                Thread.sleep(10); //TODO change back to 200, this is only for fastest path testing
                currRobotPos=mController.getRobotPos();
                publish(currRobotPos);

            } while (currRobotPos[0] != startPos[0] || currRobotPos[1] !=startPos[1]);
            //Turn north when arrived back at starting position
            mController.robotTurnRight();
            return null;
        }

        @Override
        protected void process(List<int[]> robotCoords) {
            int[] coords = robotCoords.get(robotCoords.size()-1);
            ui.repaint();
        }
    }
}
