import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;

public class Game extends Canvas implements Runnable {

    private JFrame frame;
    private boolean running = false;
    private Thread gameThread;

    public enum State {
        MENU, GAME
    }

    public static State gameState = State.MENU;

    private Menu menu;
    private Gameplay gameplay;

    public int currentLevel = 1;
    public int score = 0;

    public Game() {
        frame = new JFrame("Kawiarnia Telekomunikacyjna");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.add(this);
        frame.setVisible(true);

        menu = new Menu(this); // menu ma dostęp do Game
        gameplay = new Gameplay(currentLevel, score);

        // obsługa myszy dla menu i gry
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameState == State.GAME) {
                    gameplay.mouseClicked(e.getX(), e.getY());
                } else if (gameState == State.MENU) {
                    menu.mousePressed(e);
                }
            }
        });

        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    gameState = State.MENU;
                }
            }
        });

        setFocusable(true);
        requestFocus();
    }

    public void newGame() {
        SaveManager.delete();
        currentLevel = 1;
        score = 0;
        gameplay = new Gameplay(currentLevel, score);
        gameState = State.GAME;
    }

    public void continueGame() {
        if (SaveManager.exists()) {
            int[] data = SaveManager.load();
            currentLevel = data[0];
            score = data[1];
            gameplay = new Gameplay(currentLevel, score);
            gameState = State.GAME;
        }
    }

    public static void main(String[] args) {
        new Game().start();
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        try {
            gameThread.join();
        } catch (Exception ignored) {}
    }

    @Override
    public void run() {
        while (running) {
            update();
            render();
            try {
                Thread.sleep(10);
            } catch (Exception ignored) {}
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
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        g.setColor(new Color(230, 220, 200));
        g.fillRect(0, 0, getWidth(), getHeight());

        if (gameState == State.MENU) {
            menu.render(g);
        } else if (gameState == State.GAME) {
            gameplay.render(g);
        }

        g.dispose();
        bs.show();
    }
}
