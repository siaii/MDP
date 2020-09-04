package simulator;

import map.MAP_CONST;
import map.MapCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UIController extends JFrame {
    private static UIController _instance;
    private Controller mainController;
    private JPanel mapPanel;
    private JPanel buttonPanel; //TODO not permanent (maybe?)

    public UIController(){
        super("Map Test");
        mainController = Controller.getInstance();
        //Create window frame, size to change
        setSize(400, 700);
        setVisible(true);
        setResizable(false);
        _instance = this;
    }

    public static UIController getInstance(){
        if(_instance == null){
            _instance=new UIController();
        }
        return _instance;
    }


    public void CreateUI(){

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        mapPanel = new MapCanvas();
        buttonPanel = new JPanel();

        mapPanel.setBounds(MAP_CONST.CELL_START_X, MAP_CONST.CELL_START_Y,MAP_CONST.MAP_GRID_WIDTH*MAP_CONST.MAP_CELL_SIZE, MAP_CONST.MAP_GRID_HEIGHT*MAP_CONST.MAP_CELL_SIZE);
        mapPanel.setPreferredSize(new Dimension(MAP_CONST.MAP_GRID_WIDTH*MAP_CONST.MAP_CELL_SIZE+MAP_CONST.CELL_START_X*2, MAP_CONST.MAP_GRID_HEIGHT*MAP_CONST.MAP_CELL_SIZE+MAP_CONST.CELL_START_Y*2));
        mapPanel.setBackground(Color.GRAY);
        buttonPanel.setBounds(400, 400, 400, 300);

        CreateButtons(buttonPanel);

        container.add(mapPanel);
        container.add(buttonPanel);
        add(container);
        pack();
    }


    public void CreateButtons(JPanel buttonPanel){

        buttonPanel.setLayout(new GridLayout(0,2));

        JButton exploreBut = new JButton("Explore");
        JButton fastestBut = new JButton("Fastest Path");


        exploreBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    mainController.startExploration();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                repaint();
            }
        });

        fastestBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    mainController.runFastestPath();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                repaint();
            }
        });

        buttonPanel.add(exploreBut);
        buttonPanel.add(fastestBut);
    }


}
