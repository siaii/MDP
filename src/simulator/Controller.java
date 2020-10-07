package simulator;


import algorithm.ExplorationAlgorithm;
import algorithm.FastestPathAlgorithm;
import map.ACCESS;
import map.MAP_CONST;
import map.Map;
import map.True_Map;
import robot.Camera;
import robot.ORIENTATION;
import robot.Robot;

import java.util.ArrayList;

import static robot.ORIENTATION.*;

public class Controller {

    private static Controller _instance = null;
    private ExplorationAlgorithm exploreAlgo;
    private FastestPathAlgorithm fastestPathAlgo;
    private UIController ui;
    private Robot virtualRobot;
    private Map arena;
    private True_Map trueArena;
    private Camera camera;
    public static final boolean isRealBot=true;
    private boolean isArenaExplored=false;

    private PCClient pcClient;
    Boolean connect = false;

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

    public True_Map getTrueArena(){ return trueArena;}

    public void Initialize(){
        pcClient = PCClient.getInstance();

        ui = new UIController();
        arena = new Map();
        trueArena = new True_Map();
        virtualRobot = new Robot(MAP_CONST.ROBOT_START_ZONE_CENTER_X,MAP_CONST.ROBOT_START_ZONE_CENTER_Y, NORTH, isRealBot);
        exploreAlgo = new ExplorationAlgorithm();
        fastestPathAlgo = new FastestPathAlgorithm();
        camera = new Camera();
    }

    public void run() throws InterruptedException {
        ui.CreateUI();
        // _instance.startExploration();
        if (isRealBot) {
            String cmd;
            cmd = pcClient.receivePacket();
            if (cmd.equals("ex")){
                _instance.startExploration();
                CreateWaitForFPThread();
            }

        }
    }

    private void CreateWaitForFPThread(){
        Thread waitThread = new Thread(){
            @Override
            public void run() {
                while(!exploreAlgo.getIsExploreFinished());
                String cmd;
                cmd = pcClient.receivePacket();
                if (cmd.substring(0,2).equals("pf")){
                    int x = Integer.parseInt(cmd.split(",")[2]);
                    int y = 19 - Integer.parseInt(cmd.split(",")[3]);

                    try {
                        _instance.runFastestPath(x,y);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        waitThread.start();
    }

    public int[] getRobotPos(){
        return virtualRobot.getRobotPosition();
    }

    //Default move forward 1 step
    public void robotMoveForward() throws InterruptedException {
        robotMoveForward(1);
    }

    //Check if the "steps" steps in front is accessible or not (should be in checkRobotFront?)
    public void robotMoveForward(int steps) throws InterruptedException {
        if(checkRobotFront()){
            virtualRobot.Move_Forward(steps);
            String mdf = virtualRobot.mdfString();
            if(isRealBot){
                //Send command to rpi
                pcClient.sendPacket("1," + mdf);
            }
            virtualRobot.SenseAll();
        }else{
            System.out.println("robot bumped to wall");
        }
    }

    public void startExploration() throws InterruptedException {
        exploreAlgo.setMode(ui.getExploreMode());
//        virtualRobot.SenseAll();
        exploreAlgo.exploreArena();
        isArenaExplored=true;
    }

    //Default fastest path to Finish
    public void runFastestPath(int waypointX, int waypointY) throws InterruptedException {
        if(!isArenaExplored){
            System.out.println("Please explore the arena first");
            return;
        }
        fastestPathAlgo.setFinishedWaypoint(true);
        fastestPathAlgo.setExplorationMode(false);
        fastestPathAlgo.runFastestPath(waypointX, waypointY);
    }

    public void gotoFastestPath(int destX, int destY) throws InterruptedException {
        if(!isArenaExplored){
            System.out.println("Please explore the arena first");
            return;
        }
        fastestPathAlgo.runFastestPath(destX, destY);
    }

    //Called when ExploreTask in ExplorationAlgorithm has finished
    public void exploredUnexploredTiles() throws InterruptedException {
        ArrayList<int[]> unexplored = arena.getUnexploredCoords();
        //Go to unexplored part, or if there are none go to start(should already be at the start)
        if(!unexplored.isEmpty()){
            gotoFastestPath(unexplored.get(unexplored.size()-1)[0], unexplored.get(unexplored.size()-1)[1]);
        }else{
            int[] robotPos = getRobotPos();
            if(robotPos[0]==MAP_CONST.ROBOT_START_ZONE_CENTER_X && robotPos[1]==MAP_CONST.ROBOT_START_ZONE_CENTER_Y){
                //Send stop exploration here
                if (isRealBot) pcClient.sendPacket("se");
                System.out.println("send se1");
            }else{
                fastestPathAlgo.setGoingToStart();
                robotGoToStart();
            }
        }
    }

    public void robotGoToStart() throws InterruptedException {
        gotoFastestPath(MAP_CONST.ROBOT_START_ZONE_CENTER_X, MAP_CONST.ROBOT_START_ZONE_CENTER_Y);
    }

    public void robotTurnRight() throws InterruptedException {
        virtualRobot.Turn_Right();
        String mdf = virtualRobot.mdfString();
        if(isRealBot){
            //Send command to rpi
            pcClient.sendPacket("D," + mdf);
            virtualRobot.SenseAll();
        }
    }

    public void robotTurnLeft() throws InterruptedException {
        virtualRobot.Turn_Left();
        String mdf = virtualRobot.mdfString();
        if(isRealBot){
            //Send command to rpi
            pcClient.sendPacket("A," + mdf);
            virtualRobot.SenseAll();
        }
    }

    public boolean isVirtualArenaUpdateContinue(int coordX, int coordY){
        if(arena.GetExplored(coordX, coordY)){
            if(arena.GetTrueWallAt(coordX, coordY)){
                return false;
            }
        }
        return true;
    }

    public void updateVirtualArena(int coordX, int coordY, boolean isWall){
        arena.SetExplored(coordX, coordY);
        if(isWall){
            arena.SetTrueWallAt(coordX, coordY);
        }
    }

    public int[] checkTrueWall(int dist){
        int[] robotPos = virtualRobot.getRobotPosition();
        switch(virtualRobot.getRobotOrientation()){
            case NORTH:
                if(checkRobotFrontUnexplored(robotPos[0], robotPos[1])){
                    virtualRobot.SenseAll();
                }
                for(int i=2; i<dist; i++){
                    if(arena.GetTrueWallAt(robotPos[0], robotPos[1]-i)){
                        return new int[]{robotPos[0], robotPos[1]-i};
                    }
                }
                return null;
            case SOUTH:
                if(checkRobotFrontUnexplored(robotPos[0], robotPos[1])){
                    virtualRobot.SenseAll();
                }
                for(int i=2; i<dist; i++){
                    if(arena.GetTrueWallAt(robotPos[0], robotPos[1]+i)){
                        return new int[]{robotPos[0], robotPos[1]+1};
                    }
                }
                return null;

            case EAST:
                if(checkRobotFrontUnexplored(robotPos[0], robotPos[1])){
                    virtualRobot.SenseAll();
                }
                for(int i=2; i<dist; i++){
                    if(arena.GetTrueWallAt(robotPos[0]+i, robotPos[1])){
                        return new int[]{robotPos[0]+i, robotPos[1]};
                    }
                }
                    return null;

            case WEST:
                if(checkRobotFrontUnexplored(robotPos[0], robotPos[1])){
                    virtualRobot.SenseAll();
                }
                for(int i=2; i<dist; i++){
                    if(arena.GetTrueWallAt(robotPos[0]-i, robotPos[1])){
                        return new int[]{robotPos[0]-i, robotPos[1]};
                    }
                }
                    return null;

            default:
                return null;
        }
    }

    public boolean checkRobotFront(){
        int[] robotPos = virtualRobot.getRobotPosition();
        switch(virtualRobot.getRobotOrientation()){
            case NORTH:
                if(checkRobotFrontUnexplored(robotPos[0], robotPos[1])){
                    virtualRobot.SenseAll();
                }
                if(arena.CheckIsAccessible(robotPos[0], robotPos[1]-1)== ACCESS.YES){
                    return true;
                }else {
                    return false;
                }
            case SOUTH:
                if(checkRobotFrontUnexplored(robotPos[0], robotPos[1])){
                    virtualRobot.SenseAll();
                }
                if(arena.CheckIsAccessible(robotPos[0], robotPos[1]+1)==ACCESS.YES){
                    return true;
                }else {
                    return false;
                }
            case EAST:
                if(checkRobotFrontUnexplored(robotPos[0], robotPos[1])){
                    virtualRobot.SenseAll();
                }
                if(arena.CheckIsAccessible(robotPos[0]+1, robotPos[1])==ACCESS.YES){
                    return true;
                }else {
                    return false;
                }
            case WEST:
                if(checkRobotFrontUnexplored(robotPos[0], robotPos[1])){
                    virtualRobot.SenseAll();
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

    //Get unexplored coordinates
    public ArrayList<int[]> getMapUnexplored(){
        return arena.getUnexploredCoords();
    }

    //Reset robot orientation to north
    public void resetRobotOrientation() throws InterruptedException {
        switch (getRobotOrientation()){
            case NORTH:
                break;
            case SOUTH:
                //Maybe implement U-Turn?
                robotTurnRight();
                robotTurnRight();
                break;
            case EAST:
                robotTurnLeft();
                break;
            case WEST:
                robotTurnRight();
                break;
        }
        ui.repaint();
    }

    private float calculateMapCoverage(){
        float res=0;
        for(int y=0; y<MAP_CONST.MAP_GRID_HEIGHT; ++y){
            for(int x=0; x<MAP_CONST.MAP_GRID_WIDTH; ++x){
                if(arena.GetExplored(x,y)){
                    res+=1f;
                }
            }
        }

        return res/(MAP_CONST.MAP_GRID_HEIGHT*MAP_CONST.MAP_GRID_WIDTH);
    }

    public boolean hasFulfilledCoverage(){
        float cov=ui.getCoverage();
        if(calculateMapCoverage()>=cov){
            System.out.println("Coverage is "+cov*100+"%");
            return true;
        }else{
            return false;
        }
    }

    public void setFastestPathGoingToStart(){
        fastestPathAlgo.setGoingToStart();
    }

    public int getExploreDuration(){
        return ui.getExploreDuration();
    }

    public int getRobotMoveSpeed(){
        int stepPerSec = ui.getStepsPerSec();
        return 1000/stepPerSec;
    }


    public String getMdfString(){
        String part1 = arena.GetMdfStringExplored();
        String part2 = arena.GetMdfStringIsWall();

        return part1+","+part2;
    }
}
