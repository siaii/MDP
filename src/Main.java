import simulator.Controller;

public class Main {
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new Main();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        //new Main();
    }

    public Main() throws InterruptedException {
        Controller con = Controller.getInstance();
        con.Initialize();
        con.run();

    }
}
