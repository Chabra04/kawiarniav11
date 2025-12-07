import java.awt.*;

public class Gameplay {
    private Level level;

    public Gameplay(){
        level=new Level(1,3000);
        level.start();
    }

    public void update() {
    level.update();
    }

    public void render(Graphics g) {
        level.render(g);
    }
}
