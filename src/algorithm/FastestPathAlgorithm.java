package algorithm;

import map.MAP_CONST;
import robot.ORIENTATION;
import robot.ROBOT_CONST;
import simulator.Controller;

import java.util.*;

public class FastestPathAlgorithm {
    private Controller mainController;
    private ArrayList<ORIENTATION> pathToTake;
    private int searchCountTimeout = 1000;

    private gridNode[][] gridNodeArray = new gridNode[MAP_CONST.MAP_GRID_HEIGHT][MAP_CONST.MAP_GRID_WIDTH];

    public FastestPathAlgorithm(){
        mainController = Controller.getInstance();
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


    //Using A* Algorithm
    public void findFastestPath(int destPosX, int destPosY){
        initCostArray();
        ArrayList<gridNode> openNodes = new ArrayList<gridNode>();
        ArrayList<gridNode> closedNodes = new ArrayList<gridNode>();

        //The start node
        gridNode currNode;
        gridNode startNode = gridNodeArray[MAP_CONST.ROBOT_START_ZONE_CENTER_Y][MAP_CONST.ROBOT_START_ZONE_CENTER_X];
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

    public void getFastestPath(){
        ArrayList<gridNode> pathToTake = new ArrayList<>();

        gridNode currNode = gridNodeArray[MAP_CONST.FINISH_ZONE_CENTER_Y][MAP_CONST.FINISH_ZONE_CENTER_X];
        do{
            pathToTake.add(currNode);
            currNode = currNode.fromNode;
        }while(currNode!=gridNodeArray[MAP_CONST.ROBOT_START_ZONE_CENTER_Y][MAP_CONST.ROBOT_START_ZONE_CENTER_X]);

        Collections.reverse(pathToTake);
        for(int i=0; i<pathToTake.size(); ++i){
            gridNode temp = pathToTake.get(i);
            System.out.printf("%d, %d\n", temp.nodePos[0], temp.nodePos[1]);
        }
    }

    //TODO implement this
    private void turnPathToDirection(ArrayList<gridNode> path){
        String robotMovement="";
        for(int i=1; i<path.size(); ++i){

        }
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

}
