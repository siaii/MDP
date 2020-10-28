package algorithm;

import map.MAP_CONST;
import simulator.Controller;
import simulator.MODE;
import simulator.UIController;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ExplorationAlgorithm {
    private Controller mController;
    private UIController ui;
    private MODE explorationMode = MODE.DEFAULT;
    private long durationMillis;
    private long endTime;
    private int stepCD;
    private ExploreTask explore;

    public ExplorationAlgorithm(){
        mController = Controller.getInstance();
        ui = UIController.getInstance();
        explore = new ExploreTask();
    }

    private void GetControllerInstance(){
        if(mController==null){
            mController= Controller.getInstance();
        }
        if(ui==null){
            ui = UIController.getInstance();
        }
    }

    public boolean getIsExploreFinished(){
        return explore.isDone();
    }
    public void exploreArena() throws InterruptedException {
        durationMillis=mController.getExploreDuration();
        stepCD=mController.getRobotMoveSpeed();
        endTime=System.currentTimeMillis()+durationMillis;
        explore.execute();

    }

    public void setMode(MODE mode){
        explorationMode=mode;
    }

    //Exploration class multithreading
    private class ExploreTask extends SwingWorker<Void, int[]> {
        @Override
        protected Void doInBackground() throws Exception {
            int[] startPos = {MAP_CONST.ROBOT_START_ZONE_CENTER_X, MAP_CONST.ROBOT_START_ZONE_CENTER_Y};
            int[] currRobotPos = mController.getRobotPos();
            do {
                if(explorationMode==MODE.TIMELIMITED){
                    if(System.currentTimeMillis()>endTime){
                        System.out.println("Time's Up!");
                        break;
                    }
                }else if(explorationMode==MODE.COVERAGELIMITED){
                    if(mController.hasFulfilledCoverage()){
                        break;
                    }
                }
                //If the left side of robot is accessible, then the robot will go there
                if (mController.CheckLeftIsAccessible(currRobotPos[0], currRobotPos[1])) {
                    mController.robotTurnLeft();
                    if(mController.checkRobotFront()){
                        mController.robotMoveForward();
                    }else{
                        //If after left turn front is inaccessible, then the front sensor just updated something
                        //TODO test this tmr
                        mController.robotTurnRight();
                    }
                }else {
                    //If the robot's front is inaccessible, turn right
                    if(mController.checkRobotFront()){
                        mController.robotMoveForward();
                    }else{
                        mController.robotTurnRight();
                    }
                }

                //Sleep is for simulator only
                if(!mController.isRealBot){
                    Thread.sleep(stepCD);
                }
                currRobotPos=mController.getRobotPos();
                publish(currRobotPos);

            } while (currRobotPos[0] != startPos[0] || currRobotPos[1] !=startPos[1]);

            //Turn north when arrived back at starting position
            mController.resetRobotOrientation();
            return null;
        }

        @Override
        protected void process(List<int[]> robotCoords) {
            ui.repaint();
        }

        @Override
        protected void done() {
            super.done();
            try {
                if(explorationMode==MODE.DEFAULT){
                    mController.SendSE();
                    mController.setArenaExplored();
//TODO decide this part                    mController.exploredUnexploredTiles();
                }else{
                    System.out.println("~~~~~Start Fastest Path to explore~~~~~");
                    mController.setFastestPathGoingToStart();
                    mController.robotGoToStart();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
