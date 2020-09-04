package simulator;

import algorithm.ExplorationAlgorithm;
import algorithm.FastestPathAlgorithm;
import map.ACCESS;
import map.MAP_CONST;
import map.Map;
import map.True_Map;
import robot.ORIENTATION;
import robot.Robot;

import static robot.ORIENTATION.*;

public class Controller {

    private static Controller _instance = null;
    private ExplorationAlgorithm exploreAlgo;
    private FastestPathAlgorithm fastestPathAlgo;
    private UIController ui;
    private Robot virtualRobot;
    private Map arena;
    private True_Map trueArena;
    private static final boolean isRealRun=false;
    private boolean isArenaExplored=false;

    public static Controller getInstance(){
        if(_instance==null){
            _instance = new Controller();
        }
        return _instance;
    }

    private Controller(){
        _instance = this;
    }

    public Robot getRobotInstance(){
        return virtualRobot;
    }

    public Map getArenaInstance(){
        return arena;
    }

    public void Initialize(){
        ui = new UIController();
        arena = new Map();
        trueArena = new True_Map();
        virtualRobot = new Robot(MAP_CONST.ROBOT_START_ZONE_CENTER_X,MAP_CONST.ROBOT_START_ZONE_CENTER_Y, NORTH);
        exploreAlgo = new ExplorationAlgorithm();
        fastestPathAlgo = new FastestPathAlgorithm();
    }

    public void run() throws InterruptedException {
        ui.CreateUI();
        //ExploreArena();
    }

    public int[] getRobotPos(){
        return virtualRobot.getRobotPosition();
    }

    public void robotMoveForward() throws InterruptedException {
        if(checkRobotFront()){
            virtualRobot.Move_Forward();
            checkRobotRightSensor();
            checkRobotLeftSensor();
        }else{
            System.out.println("robot bumped to wall");
        }
    }

    public void startExploration() throws InterruptedException {
        exploreAlgo.exploreArena();
        isArenaExplored=true;
    }

    public void runFastestPath() throws InterruptedException {
        if(!isArenaExplored){
            System.out.println("Please explore the arena first");
            return;
        }
        fastestPathAlgo.runFastestPath(MAP_CONST.FINISH_ZONE_CENTER_X, MAP_CONST.FINISH_ZONE_CENTER_Y);
    }

    public void robotTurnRight() throws InterruptedException {
        virtualRobot.Turn_Right();
        //ui.repaint();
        //Thread.sleep(200);
    }

    public void robotTurnLeft() throws InterruptedException {
        virtualRobot.Turn_Left();
        //ui.repaint();
        //Thread.sleep(200);
    }

    private void updateVirtualArena(int coordX, int coordY, boolean isWall){
        arena.SetExplored(coordX, coordY);
        if(isWall){
            arena.SetTrueWallAt(coordX, coordY);
        }
    }


    //TODO CHANGE TO USE SENSOR, BOTH VIRTUAL AND PHYSICAL
    public boolean checkRobotFront(){
        int[] robotPos = virtualRobot.getRobotPosition();
        switch(virtualRobot.getRobotOrientation()){
            case NORTH:
                if(checkRobotFrontUnexplored(robotPos[0], robotPos[1])){
                    checkRobotFrontSensor();
                }
                if(arena.CheckIsAccessible(robotPos[0], robotPos[1]-1)== ACCESS.YES){
                    return true;
                }else {
                    return false;
                }
            case SOUTH:
                if(checkRobotFrontUnexplored(robotPos[0], robotPos[1])){
                    checkRobotFrontSensor();
                }
                if(arena.CheckIsAccessible(robotPos[0], robotPos[1]+1)==ACCESS.YES){
                    return true;
                }else {
                    return false;
                }
            case EAST:
                if(checkRobotFrontUnexplored(robotPos[0], robotPos[1])){
                    checkRobotFrontSensor();
                }
                if(arena.CheckIsAccessible(robotPos[0]+1, robotPos[1])==ACCESS.YES){
                    return true;
                }else {
                    return false;
                }
            case WEST:
                if(checkRobotFrontUnexplored(robotPos[0], robotPos[1])){
                    checkRobotFrontSensor();
                }
                if(arena.CheckIsAccessible(robotPos[0]-1, robotPos[1])==ACCESS.YES){
                    return true;
                }else {
                    return false;
                }
            default:
                return false;
        }
    }



    //Check for any unexplored tile in the 2 front tile of the robot, in all 3 tile wide robot path
    public boolean checkRobotFrontUnexplored(int posX, int posY){
        ORIENTATION robotOrientation = virtualRobot.getRobotOrientation();
        for(int i=-1; i<=1; i++){
            for(int j=1; j<=2; j++){
                switch (robotOrientation){
                    case NORTH:
                        if(arena.CheckIsAccessible(posX+i, posY-j)==ACCESS.UNEXPLORED){
                            return true;
                        }
                        break;
                    case WEST:
                        if(arena.CheckIsAccessible(posX-j, posY+i)==ACCESS.UNEXPLORED){
                            return true;
                        }
                        break;
                    case SOUTH:
                        if(arena.CheckIsAccessible(posX+i, posY+j)==ACCESS.UNEXPLORED){
                            return true;
                        }
                        break;
                    case EAST:
                        if(arena.CheckIsAccessible(posX+j, posY+i)==ACCESS.UNEXPLORED){
                            return true;
                        }
                        break;
                }
            }
        }
        return false;
    }

    public boolean CheckLeftIsAccessible(int posX, int posY){
        ORIENTATION robotOrientation = virtualRobot.getRobotOrientation();
        switch (robotOrientation){
            case NORTH:
                if(arena.CheckIsAccessible(posX-1, posY)==ACCESS.YES){
                    return true;
                }else{
                    return false;
                }
            case EAST:
                if(arena.CheckIsAccessible(posX, posY-1)==ACCESS.YES){
                    return true;
                }else{
                    return false;
                }
            case SOUTH:
                if(arena.CheckIsAccessible(posX+1, posY)==ACCESS.YES){
                    return true;
                }else{
                    return false;
                }
            case WEST:
                if(arena.CheckIsAccessible(posX, posY+1)==ACCESS.YES){
                    return true;
                }else {
                    return false;
                }
            default:
                return false;
        }
    }

    public void checkRobotFrontSensor(){
        int[] robotPos = virtualRobot.getRobotPosition();
        int robotX = robotPos[0];
        int robotY = robotPos[1];
        switch (virtualRobot.getRobotOrientation()){
            case NORTH:
                for(int i=-1; i<=1; ++i){
                    for(int j=2; j<=4; j++){
                        //xCoords = from robotCenter-1 to robotCenter+1
                        //yCoords = from robotCenter-2 to robotCenter-4 (to upper side, index towards 0)
                        if(robotX+i>=0 && robotX+i<MAP_CONST.MAP_GRID_WIDTH && robotY-j >=0 && robotY-j<MAP_CONST.MAP_GRID_HEIGHT){
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
        int[] robotPos = virtualRobot.getRobotPosition();
        int robotX = robotPos[0];
        int robotY = robotPos[1];
        switch (virtualRobot.getRobotOrientation()){
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
        int[] robotPos = virtualRobot.getRobotPosition();
        int robotX = robotPos[0];
        int robotY = robotPos[1];
        switch (virtualRobot.getRobotOrientation()){
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
    }

    public boolean checkIsAccessible(int posX, int posY){

        if(arena.CheckIsAccessible(posX,posY)==ACCESS.YES){
            return true;
        }else{
            return false;
        }
    }

    public ORIENTATION getRobotOrientation(){
        return virtualRobot.getRobotOrientation();
    }

}
