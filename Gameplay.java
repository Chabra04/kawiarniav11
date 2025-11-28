import java.awt.*;

public class Gameplay {

    public void update() {
        // Tutaj później dodam logikę gry, klientów itd.
    }

    public void render(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Arial", Font.BOLD, 60));
        g.drawString("Rozpoczęto poziom 1", 200, 200);

        g.setFont(new Font("Arial", Font.PLAIN, 30));
        g.drawString("Kliknij ESC aby wyjść do menu", 200, 250);
    }
}
