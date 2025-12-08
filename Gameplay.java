import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Gameplay {

    private Level level;
    private int currentLevel;
    private int score;

    private BufferedImage background; // tło gry

    public Gameplay(int currentLevel, int score) {
        this.currentLevel = currentLevel;
        this.score = score;

        // wczytanie tła gry z folderu resources (poprawna metoda w Javie)
        try {
            background = ImageIO.read(getClass().getResourceAsStream("/resources/tloGry.jpg"));
        } catch (Exception e) {
            System.out.println("Nie udało się wczytać tła gry!");
            background = null;
            e.printStackTrace();
        }

        level = new Level(currentLevel, 3000, this);
        level.start();
    }

    public void update() {
        level.update();
    }

    public void render(Graphics g) {
        // rysowanie tła
        if (background != null) {
            g.drawImage(background, 0, 0, Game.WIDTH, Game.HEIGHT, null);
        } else {
            g.setColor(new Color(230, 220, 200));
            g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);
        }

        // rysowanie poziomu
        level.render(g);
    }

    public void addScore(int points) {
        score += points;
    }

    public int getScore() {
        return score;
    }

    public void nextLevel() {
        currentLevel++;
        level = new Level(currentLevel, 3000, this);
        level.start();
    }

    public void mouseClicked(int mx, int my) {
        level.click(mx, my);
    }

    public Level getLevel() {
        return level;
    }
}
