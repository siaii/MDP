package map;

import robot.ORIENTATION;
import simulator.Controller;
import robot.Robot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MapCanvas extends JPanel {

    Map arena;
    Robot virtualRobot;

    public MapCanvas(){
        Controller c = Controller.getInstance();
        arena = c.getArenaInstance();
        virtualRobot = c.getRobotInstance();
    }


    @Override
    protected void paintComponent(Graphics g) {
        drawMapGrid(g);
        drawRobot(g, virtualRobot);
    }

    private void drawMapGrid(Graphics g){
        int xCoords = MAP_CONST.CELL_START_X;
        for(int x = 0; x < arena.GetMapWidth(); ++x){
            int yCoords = MAP_CONST.CELL_START_Y;
            for(int y = 0; y< arena.GetMapHeight(); ++y){
                Map.Map_Cells tempCell = arena.fullMap[y][x];
                if(!tempCell.isExplored){
                    g.setColor(Color.GRAY);
                }
                else if(tempCell.GetTrueWall()){
                    g.setColor(Color.BLACK);
                }else if(tempCell.GetVirtualWall()){
                    g.setColor(Color.red);
                }else{
                    g.setColor(Color.lightGray);
                }
                g.fillRect(xCoords, yCoords, MAP_CONST.MAP_CELL_SIZE, MAP_CONST.MAP_CELL_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(xCoords, yCoords, MAP_CONST.MAP_CELL_SIZE, MAP_CONST.MAP_CELL_SIZE);
                yCoords+= MAP_CONST.MAP_CELL_SIZE;
            }
            xCoords+= MAP_CONST.MAP_CELL_SIZE;
        }
    }

    private void drawRobot(Graphics g, Robot robot){
        int[] robotPos = robot.getRobotPosition();
        int[] robotPixelPos = convertRobotPosToPixel(robotPos);

        //Draw robot body
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawOval(robotPixelPos[0], robotPixelPos[1], MAP_CONST.ROBOT_DIAMETER, MAP_CONST.ROBOT_DIAMETER);
        g2.setColor(Color.YELLOW);
        //fillOval creates an oval 1 pixel smaller than drawOval, thus the +1
        g2.fillOval(robotPixelPos[0], robotPixelPos[1], MAP_CONST.ROBOT_DIAMETER+1, MAP_CONST.ROBOT_DIAMETER+1);


        //Draw robot orientation marker
        drawRobotOrientation(g, robotPixelPos, robot.getRobotOrientation());
    }

    private void drawRobotOrientation(Graphics g, int[] robotPixelPos, ORIENTATION orientation){
        g.setColor(Color.red);
        if(orientation== ORIENTATION.NORTH){
            g.fillOval(robotPixelPos[0]+MAP_CONST.ROBOT_DIAMETER/2 - MAP_CONST.ROBOT_ORIENTATION_DIAMETER/2 , robotPixelPos[1]+MAP_CONST.ROBOT_ORIENTATION_OFFSET_FROM_EDGE - MAP_CONST.ROBOT_ORIENTATION_DIAMETER/2, MAP_CONST.ROBOT_ORIENTATION_DIAMETER, MAP_CONST.ROBOT_ORIENTATION_DIAMETER);
        }
        if(orientation== ORIENTATION.SOUTH){
            g.fillOval(robotPixelPos[0]+MAP_CONST.ROBOT_DIAMETER/2 - MAP_CONST.ROBOT_ORIENTATION_DIAMETER/2 , robotPixelPos[1]+MAP_CONST.ROBOT_DIAMETER - MAP_CONST.ROBOT_ORIENTATION_OFFSET_FROM_EDGE - MAP_CONST.ROBOT_ORIENTATION_DIAMETER/2, MAP_CONST.ROBOT_ORIENTATION_DIAMETER, MAP_CONST.ROBOT_ORIENTATION_DIAMETER);
        }
        if(orientation== ORIENTATION.WEST){
            g.fillOval(robotPixelPos[0]+MAP_CONST.ROBOT_ORIENTATION_OFFSET_FROM_EDGE - MAP_CONST.ROBOT_ORIENTATION_DIAMETER/2 , robotPixelPos[1]+MAP_CONST.ROBOT_DIAMETER/2 - MAP_CONST.ROBOT_ORIENTATION_DIAMETER/2, MAP_CONST.ROBOT_ORIENTATION_DIAMETER, MAP_CONST.ROBOT_ORIENTATION_DIAMETER);
        }
        if(orientation== ORIENTATION.EAST){
            g.fillOval(robotPixelPos[0]+MAP_CONST.ROBOT_DIAMETER-MAP_CONST.ROBOT_ORIENTATION_OFFSET_FROM_EDGE - MAP_CONST.ROBOT_ORIENTATION_DIAMETER/2 , robotPixelPos[1]+MAP_CONST.ROBOT_DIAMETER/2 - MAP_CONST.ROBOT_ORIENTATION_DIAMETER/2, MAP_CONST.ROBOT_ORIENTATION_DIAMETER, MAP_CONST.ROBOT_ORIENTATION_DIAMETER);
        }
    }


    private int[] convertRobotPosToPixel(int[] robotPos){
        int robotPixelX = MAP_CONST.CELL_START_X + (robotPos[0]-1) * MAP_CONST.MAP_CELL_SIZE + MAP_CONST.MAP_CELL_SIZE/2;
        int robotPixelY = MAP_CONST.CELL_START_Y + (robotPos[1]-1) * MAP_CONST.MAP_CELL_SIZE + MAP_CONST.MAP_CELL_SIZE/2;
        return new int[]{robotPixelX, robotPixelY};
    }

    public void CreateButtons(JFrame frame){
        JButton turnRightBut = new JButton("Turn Right");
        turnRightBut.setBounds(250,550, 100, 50);
        turnRightBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                virtualRobot.Turn_Right();
            }
        });

        JButton turnLeftBut = new JButton("Turn Left");
        turnLeftBut.setBounds(50,550, 100, 50);

        JButton forwardBut = new JButton("Forward");
        forwardBut.setBounds(150, 475, 100, 50);

        frame.add(turnRightBut);
        frame.add(turnLeftBut);
        frame.add(forwardBut);
    }
}
