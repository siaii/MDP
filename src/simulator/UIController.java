package simulator;

import map.MAP_CONST;
import map.MapCanvas;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Time;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class UIController extends JFrame {
    private static UIController _instance;
    private Controller mainController;
    private JPanel mapPanel;
    private JPanel buttonPanel; //TODO not permanent (maybe?)
    private JPanel settingPanel;
    private MODE exploreMode=MODE.DEFAULT;
    private int minDuration;
    private int secDuration;
    private float coverage=1f;
    private int stepsPerSec=5;

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
        settingPanel = new JPanel();

        mapPanel.setBounds(MAP_CONST.CELL_START_X, MAP_CONST.CELL_START_Y,MAP_CONST.MAP_GRID_WIDTH*MAP_CONST.MAP_CELL_SIZE, MAP_CONST.MAP_GRID_HEIGHT*MAP_CONST.MAP_CELL_SIZE);
        mapPanel.setPreferredSize(new Dimension(MAP_CONST.MAP_GRID_WIDTH*MAP_CONST.MAP_CELL_SIZE+MAP_CONST.CELL_START_X*2, MAP_CONST.MAP_GRID_HEIGHT*MAP_CONST.MAP_CELL_SIZE+MAP_CONST.CELL_START_Y*2));
        mapPanel.setBackground(Color.GRAY);
        buttonPanel.setBounds(400, 400, 400, 300);

        CreateButtons(buttonPanel);
        CreateSettings(settingPanel);

        container.add(mapPanel);
        container.add(buttonPanel);
        container.add(settingPanel);
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
                    //TODO  change the parameter to waypoint
                    mainController.runFastestPath(7, 2);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                repaint();
            }
        });

        buttonPanel.add(exploreBut);
        buttonPanel.add(fastestBut);
    }

    public void CreateSettings(JPanel settingPanel){
        JTabbedPane tabbedPane = new JTabbedPane();
        JComponent defaultPanel = makeMainSettingPanel();
        JComponent timePanel = makeTimeLimitedPanel();
        JComponent coveragePanel = makeCoverageLimitedPanel();

        tabbedPane.addTab("Main Settings", defaultPanel);
        tabbedPane.addTab("Time-Limited Settings", timePanel);
        tabbedPane.addTab("Coverage-Limited Settings", coveragePanel);

        settingPanel.add(tabbedPane);
    }

    private JPanel makeMainSettingPanel(){
        JPanel mainPanel = new JPanel(new GridLayout(2,0));
        JPanel panel1 = new JPanel();
        JLabel modeLabel = new JLabel("Mode:");
        JRadioButton defaultButton = new JRadioButton("Default");
        defaultButton.setSelected(true);
        defaultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exploreMode=MODE.DEFAULT;
            }
        });
        JRadioButton timeLimitedButton = new JRadioButton("Time-Limited");
        timeLimitedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exploreMode=MODE.TIMELIMITED;
            }
        });
        JRadioButton coverageLimitedButton = new JRadioButton("Coverage-Limited");
        coverageLimitedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exploreMode=MODE.COVERAGELIMITED;
            }
        });
        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(defaultButton);
        radioGroup.add(timeLimitedButton);
        radioGroup.add(coverageLimitedButton);

        panel1.add(modeLabel);
        panel1.add(defaultButton);
        panel1.add(timeLimitedButton);
        panel1.add(coverageLimitedButton);

        JPanel stepSpeedPanel = new JPanel(new GridLayout(0,2));
        JLabel speedLabel = new JLabel("Steps per second: ");
        JSpinner speedInput = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        speedInput.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                stepsPerSec=(Integer) speedInput.getValue();
            }
        });
        stepSpeedPanel.add(speedLabel);
        stepSpeedPanel.add(speedInput);
        mainPanel.add(panel1);
        mainPanel.add(stepSpeedPanel);
        return mainPanel;
    }

    private JPanel makeTimeLimitedPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,4));
        JSpinner minSpinner = new JSpinner(new SpinnerNumberModel(6, 0, 100, 1));
        minSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                minDuration = (int) minSpinner.getValue();
            }
        });
        JSpinner secSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 60, 1));
        secSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                secDuration = (int) secSpinner.getValue();
            }
        });
        JLabel minLabel = new JLabel("Minutes");
        JLabel secLabel = new JLabel("Seconds");
        panel.add(minLabel);
        panel.add(minSpinner);
        panel.add(secLabel);
        panel.add(secSpinner);

        return panel;
    }

    private JPanel makeCoverageLimitedPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0,2));
        JSpinner percSpinner = new JSpinner(new SpinnerNumberModel(100, 0, 100, 1));
        percSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                coverage= (Integer)percSpinner.getValue();
            }
        });
        JLabel percLabel = new JLabel("Coverage Limit (%)");
        panel.add(percLabel);
        panel.add(percSpinner);

        return panel;
    }

    public MODE getExploreMode(){
        return exploreMode;
    }

    /*
    * in milis
    * */
    public int getExploreDuration(){
        int dur = 0;
        dur+= TimeUnit.MINUTES.toMillis(minDuration);
        dur+= TimeUnit.SECONDS.toMillis(secDuration);
        return dur;
    }

    public float getCoverage(){
        return coverage/100f;
    }

    public int getStepsPerSec(){
        return stepsPerSec;
    }
}
