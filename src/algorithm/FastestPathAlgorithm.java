package algorithm;

import map.MAP_CONST;
import robot.ORIENTATION;
import robot.ROBOT_CONST;
import simulator.Controller;

import java.util.ArrayList;

public class FastestPathAlgorithm {
    private Controller mainController;
    private ArrayList<ORIENTATION> pathToTake;

    public FastestPathAlgorithm(){
        mainController = Controller.getInstance();
    }

    private double costG(int[] fromCell, int[] toCell, ORIENTATION currentOrientation){
        double totalCost = 0;

        switch (currentOrientation){
            case NORTH:
            case SOUTH:
                if(fromCell[0]!=toCell[0]){
                    totalCost+=ROBOT_CONST.TURN_COST;
                }
                break;
            case WEST:
            case EAST:
                if(fromCell[1]!=toCell[1]){
                    totalCost+=ROBOT_CONST.TURN_COST;
                }
        }
        totalCost+=ROBOT_CONST.MOVE_COST;

        return totalCost;
    }

    private double costH(int[] cell){
        double totalCost = 0;
        int totalDifference =Math.abs(cell[0]-MAP_CONST.FINISH_ZONE_CENTER_X) + Math.abs(cell[1] - MAP_CONST.FINISH_ZONE_CENTER_Y);

        //Total cost of the robot moving
        totalCost+=totalDifference*ROBOT_CONST.MOVE_COST;

        if(cell[0]!=MAP_CONST.FINISH_ZONE_CENTER_X && cell[1]!=MAP_CONST.FINISH_ZONE_CENTER_Y){
            if(totalDifference>20){
                totalCost+=3*ROBOT_CONST.TURN_COST;
            }else{
                totalCost+=ROBOT_CONST.TURN_COST;
            }
        }
        return totalCost;
    }

    private String findFastestPath(){
        int[] currCellPos={MAP_CONST.ROBOT_START_ZONE_CENTER_X, MAP_CONST.ROBOT_START_ZONE_CENTER_Y};



        return "0";
    }

}
