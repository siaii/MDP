package map;

public class MAP_CONST {
    public static final int MAP_GRID_WIDTH = 15;
    public static final int MAP_GRID_HEIGHT = 20;
    public static final int MAP_CELL_SIZE = 20; //In pixels
    public static final int CELL_START_X = 50;
    public static final int CELL_START_Y = 50;

    public static final int ROBOT_DIAMETER  = MAP_CONST.MAP_CELL_SIZE*2;
    public static final int ROBOT_ORIENTATION_DIAMETER = 5;
    public static final int ROBOT_ORIENTATION_OFFSET_FROM_EDGE = 5;

    public static final int ROBOT_START_ZONE_CENTER_X = 1;
    public static final int ROBOT_START_ZONE_CENTER_Y = MAP_GRID_HEIGHT-2;

    public static final int FINISH_ZONE_CENTER_X = MAP_GRID_WIDTH-2;
    public static final int FINISH_ZONE_CENTER_Y = 1;

}
