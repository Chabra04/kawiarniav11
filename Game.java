import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;

public class Game extends Canvas implements Runnable{

    private JFrame frame;
    private boolean running = false;
    private Thread gameThread;

    public enum State{
        MENU,
        GAME
    }

    public static State gameState = State.MENU;

    private Menu menu;
    private Gameplay gameplay;

    public Game(){
        frame = new JFrame("Kawiarnia Telekomunikacyjna");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.add(this);
        frame.setVisible(true);

        menu = new Menu();
        gameplay = new Gameplay();

        addMouseListener(menu);
        addKeyListener(new java.awt.event.KeyAdapter(){
            @Override
            public void keyPressed(java.awt.event.KeyEvent e){
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
                    Game.gameState = State.MENU;
                }
            }
        });
        setFocusable(true);
        requestFocus();
    }

    public static void main(String[] args) {
        new Game().start();
    }

    public synchronized void start(){
        if (running) return;
        running =   true;
        gameThread =    new Thread(this);
        gameThread.start();
    }
    public synchronized void stop(){
        if (!running)return;
        running=   false;
        try{
            gameThread.join();
        }catch (Exception ignored){}
    }

    @Override
    public void run() {
        while (running){
            update();
            render();
            try{Thread.sleep(10);}catch (Exception ignored){}

        }
    }

    private void update() {
        if (gameState == State.MENU) {
            menu.update();
        } else if (gameState == State.GAME) {
            gameplay.update();
        }
    }

    private void render() {
        BufferStrategy bs =getBufferStrategy();
        if (bs==null){
            createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        g.setColor(new Color(230,220,200));
        g.fillRect(0,0,getWidth(),getHeight());

        if (gameState == State.MENU) {
            menu.render(g);
        } else if (gameState == State.GAME){
            gameplay.render(g);
        }

        g.dispose();
        bs.show();
    }



}
