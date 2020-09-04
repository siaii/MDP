package algorithm;

import map.MAP_CONST;
import robot.ORIENTATION;
import robot.ROBOT_CONST;
import simulator.Controller;
import simulator.UIController;

import javax.swing.*;
import java.util.*;

public class FastestPathAlgorithm {
    private Controller mainController;
    private UIController ui;
    private ArrayList<ORIENTATION> pathToTake;
    private int searchCountTimeout = 1000;
    private String pathString;

    private gridNode[][] gridNodeArray = new gridNode[MAP_CONST.MAP_GRID_HEIGHT][MAP_CONST.MAP_GRID_WIDTH];

    public FastestPathAlgorithm(){
        mainController = Controller.getInstance();
        ui = UIController.getInstance();
    }

    private class gridNode{
        public int gCost = MAP_CONST.INFINITE_COST;
        public int totalCost = MAP_CONST.INFINITE_COST;
        public boolean isAccessible;
        public boolean isExplored = false;
        public int[] nodePos;
        public gridNode fromNode;

        public gridNode(boolean _isAccessible, int posX, int posY){
            isAccessible = _isAccessible;
            nodePos = new int[]{posX,posY};
        }
    }

    public void printGridNodeCost(){
        for(int y=0; y<MAP_CONST.MAP_GRID_HEIGHT; ++y){
            for (int x=0; x<MAP_CONST.MAP_GRID_WIDTH; ++x){
                if(gridNodeArray[y][x].isAccessible){
                    System.out.printf("%3d | ",gridNodeArray[y][x].totalCost);
                }else{
                    System.out.print(" X  | ");
                }
            }
            System.out.println();
        }
    }

    private void initCostArray(){
        for(int x=0; x<MAP_CONST.MAP_GRID_WIDTH; ++x){
            for(int y=0; y<MAP_CONST.MAP_GRID_HEIGHT; ++y){
                gridNodeArray[y][x] = new gridNode(mainController.checkIsAccessible(x,y), x, y);
            }
        }
    }

    private int costG(gridNode fromCell, gridNode toCell, ORIENTATION currentOrientation){
        int totalCost = 0;

        if(fromCell.fromNode!=null){
            //Moving eastward/westward
            if(fromCell.nodePos[0]!=fromCell.fromNode.nodePos[0]){
                if(fromCell.nodePos[1]!=toCell.nodePos[1]){
                    totalCost+=ROBOT_CONST.TURN_COST;
                }
            }

            //Moving northward/southward
            if(fromCell.nodePos[1]!=fromCell.fromNode.nodePos[1]){
                if(fromCell.nodePos[0]!=toCell.nodePos[0]){
                    totalCost+=ROBOT_CONST.TURN_COST;
                }
            }
        }


        totalCost+=ROBOT_CONST.MOVE_COST;

        return totalCost;
    }

    private int costH(gridNode node, int destPosX, int destPosY){
        int totalCost = 0;
        int totalDifference =Math.abs(node.nodePos[0] - destPosX) + Math.abs(node.nodePos[1] - destPosY);

        //Total cost of the robot moving
        totalCost+=totalDifference*ROBOT_CONST.MOVE_COST;

        if(node.nodePos[0]!=destPosX && node.nodePos[1]!=destPosY){
            if(totalDifference>20){
                totalCost+=ROBOT_CONST.TURN_COST*3;
            }else {
                totalCost+=ROBOT_CONST.TURN_COST;
            }

        }
        return totalCost;
    }

    public void runFastestPath(int destPosX, int destPosY) throws InterruptedException {
        int[] robotInitialPos = mainController.getRobotPos();
        if(robotInitialPos[0]==destPosX && robotInitialPos[1]==destPosY){
            System.out.println("Robot already at destination");
            return;
        }
        findFastestPath(destPosX, destPosY);
        getFastestPath();
        FastestPathTask fastestPathTask = new FastestPathTask();
        fastestPathTask.execute();
    }

    //Using A* Algorithm
    public void findFastestPath(int destPosX, int destPosY){
        initCostArray();
        ArrayList<gridNode> openNodes = new ArrayList<gridNode>();
        ArrayList<gridNode> closedNodes = new ArrayList<gridNode>();
        int[] robotInitialPos = mainController.getRobotPos();
        //The start node
        gridNode currNode;
        gridNode startNode = gridNodeArray[robotInitialPos[1]][robotInitialPos[0]];
        startNode.totalCost=costH(startNode, destPosX, destPosY);
        startNode.gCost=0;

        openNodes.add(startNode);
        System.out.println("Starting fastest path search");
        System.out.println("Destination: "+destPosX+" "+destPosY);

        //Safety timeout
        int count=0;
        do{
            count++;
            if(count>searchCountTimeout){
                System.out.println("Path not found!");
                break;
            }

            currNode = getLowestFCost(openNodes);
            if(currNode==null){
                System.out.println("Path not found");
                break;
            }
            System.out.println("Exploring cell : "+currNode.nodePos[0]+", "+currNode.nodePos[1]);

            openNodes.remove(currNode);
            currNode.isExplored=true;

            //Add cell's neighbour to the arraylist
            ArrayList<gridNode> neighbours = new ArrayList<gridNode>();
            if(currNode.nodePos[0]!=0){
                neighbours.add(gridNodeArray[currNode.nodePos[1]][currNode.nodePos[0]-1]);
            }
            if(currNode.nodePos[0]!=MAP_CONST.MAP_GRID_WIDTH-1){
                neighbours.add(gridNodeArray[currNode.nodePos[1]][currNode.nodePos[0]+1]);
            }
            if(currNode.nodePos[1]!=0){
                neighbours.add(gridNodeArray[currNode.nodePos[1]-1][currNode.nodePos[0]]);
            }
            if(currNode.nodePos[1]!=MAP_CONST.MAP_GRID_HEIGHT-1){
                neighbours.add(gridNodeArray[currNode.nodePos[1]+1][currNode.nodePos[0]]);
            }

            //Loop through all neighbours
            for (gridNode neighbourNode:neighbours) {
                //If explored or inaccessible, skip
                if(!neighbourNode.isAccessible || neighbourNode.isExplored){
                    continue;
                }

                //g cost from current node to this neighbour node
                int newGCost = costG(currNode, neighbourNode, mainController.getRobotOrientation());
                //new g + h
                int newTotalCost = currNode.gCost + newGCost + costH(neighbourNode, destPosX, destPosY);

                if(newTotalCost<neighbourNode.totalCost || !openNodes.contains(neighbourNode)){
                    neighbourNode.totalCost = newTotalCost;
                    neighbourNode.gCost = currNode.gCost + newGCost;
                    neighbourNode.fromNode=currNode;
                    if(!openNodes.contains(neighbourNode)){
                        openNodes.add(neighbourNode);
                    }
                }
            }
        //Loop until it reaches
        }while(currNode.nodePos[0] != destPosX || currNode.nodePos[1] !=destPosY);
        System.out.println("Path found!");
    }

    public void getFastestPath() throws InterruptedException {
        ArrayList<gridNode> pathToTake = new ArrayList<>();

        gridNode currNode = gridNodeArray[MAP_CONST.FINISH_ZONE_CENTER_Y][MAP_CONST.FINISH_ZONE_CENTER_X];
        do{
            pathToTake.add(currNode);
            currNode = currNode.fromNode;
        }while(currNode!=gridNodeArray[MAP_CONST.ROBOT_START_ZONE_CENTER_Y][MAP_CONST.ROBOT_START_ZONE_CENTER_X]);

        //Add the starting node to arraylist
        pathToTake.add(currNode);

        Collections.reverse(pathToTake);
        for(int i=0; i<pathToTake.size(); ++i){
            gridNode temp = pathToTake.get(i);
            System.out.printf("%d, %d\n", temp.nodePos[0], temp.nodePos[1]);
        }

        pathString = turnPathToDirection(pathToTake);
    }

    private String turnPathToDirection(ArrayList<gridNode> path){
        StringBuilder robotMovement= new StringBuilder();
        ORIENTATION currOrientation = ORIENTATION.NORTH;
        for(int i=1; i<path.size(); ++i){
            //Robot going northward
            if(path.get(i).nodePos[1]-path.get(i-1).nodePos[1]<0){
                switch (currOrientation){
                    case NORTH:
                        robotMovement.append("F");
                        currOrientation=ORIENTATION.NORTH;
                        break;
                    case EAST:
                        robotMovement.append("LF");
                        currOrientation=ORIENTATION.NORTH;
                        break;
                    case WEST:
                        robotMovement.append("RF");
                        currOrientation=ORIENTATION.NORTH;
                        break;
                    case SOUTH:
                        robotMovement.append("U"); //Should not happen
                        currOrientation=ORIENTATION.NORTH;
                        break;
                }
            }
            //Going southward
            else if(path.get(i).nodePos[1]-path.get(i-1).nodePos[1]>0){
                switch (currOrientation){
                    case NORTH:
                        robotMovement.append("U"); //Should not happen
                        currOrientation=ORIENTATION.SOUTH;
                        break;
                    case EAST:
                        robotMovement.append("RF");
                        currOrientation=ORIENTATION.SOUTH;
                        break;
                    case WEST:
                        robotMovement.append("LF");
                        currOrientation=ORIENTATION.SOUTH;
                        break;
                    case SOUTH:
                        robotMovement.append("F");
                        currOrientation=ORIENTATION.SOUTH;
                        break;
                }
            }
            //Going westward
            else if(path.get(i).nodePos[0]-path.get(i-1).nodePos[0]<0){
                switch (currOrientation){
                    case NORTH:
                        robotMovement.append("LF");
                        currOrientation=ORIENTATION.WEST;
                        break;
                    case EAST:
                        robotMovement.append("U"); //Should not happen
                        currOrientation=ORIENTATION.WEST;
                        break;
                    case WEST:
                        robotMovement.append("F");
                        currOrientation=ORIENTATION.WEST;
                        break;
                    case SOUTH:
                        robotMovement.append("RF");
                        currOrientation=ORIENTATION.WEST;
                        break;
                }
            }
            //Going eastward
            else if(path.get(i).nodePos[0]-path.get(i-1).nodePos[0]>0){
                switch (currOrientation){
                    case NORTH:
                        robotMovement.append("RF");
                        currOrientation=ORIENTATION.EAST;
                        break;
                    case EAST:
                        robotMovement.append("F");
                        currOrientation=ORIENTATION.EAST;
                        break;
                    case WEST:
                        robotMovement.append("U"); //Should not happen
                        currOrientation=ORIENTATION.EAST;
                        break;
                    case SOUTH:
                        robotMovement.append("LF");
                        currOrientation=ORIENTATION.EAST;
                        break;
                }
            }
        }

        System.out.println(robotMovement.toString());
        return robotMovement.toString();
    }
    
    private gridNode getLowestFCost(ArrayList<gridNode> nodes){
        if(nodes.size()>0){
            gridNode result = nodes.get(nodes.size()-1);
            for (gridNode node: nodes) {
                if(node.totalCost<result.totalCost){
                    result=node;
                }
            }

            return result;
        }else{
            return null;
        }
    }

    private void executeFastestPath(String pathString) throws InterruptedException {
        for(int i=0; i<pathString.length(); ++i){
            switch (pathString.charAt(i)){
                case 'F':
                    mainController.robotMoveForward();
                    break;
                case 'R':
                    mainController.robotTurnRight();
                    break;
                case 'L':
                    mainController.robotTurnLeft();
                    break;
                default:
                    System.out.println("Something is wrong in the path");
                    break;
            }
            Thread.sleep(200);
        }
    }

    private class FastestPathTask extends SwingWorker<Void, int[]> {

        @Override
        protected Void doInBackground() throws Exception {
            for(int i=0; i<pathString.length(); ++i){
                switch (pathString.charAt(i)){
                    case 'F':
                        mainController.robotMoveForward();
                        break;
                    case 'R':
                        mainController.robotTurnRight();
                        break;
                    case 'L':
                        mainController.robotTurnLeft();
                        break;
                    default:
                        System.out.println("Something is wrong in the path");
                        break;
                }
                publish();
                Thread.sleep(200);
            }
            System.out.println("Reached target destination");
            return null;
        }

        @Override
        protected void process(List<int[]> chunks) {
            super.process(chunks);
            ui.repaint();
        }
    }

}
