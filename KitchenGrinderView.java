import java.awt.*;

public class KitchenGrinderView {

    private final KitchenProcess process;
    private final Rectangle grinderArea;

    public KitchenGrinderView(KitchenProcess process, Rectangle grinderArea) {
        this.process = process;
        this.grinderArea = grinderArea;
    }

    public void update() {
        // Logika update jest w KitchenProcess
    }

    public void render(Graphics g) {
        // 1. OBUDOWA
        g.setColor(new Color(50, 50, 50)); // Ciemnoszary
        g.fillRect(grinderArea.x, grinderArea.y, grinderArea.width, grinderArea.height);

        g.setColor(Color.BLACK);
        g.drawRect(grinderArea.x, grinderArea.y, grinderArea.width, grinderArea.height);

        // Etykieta
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Młynek", grinderArea.x + 35, grinderArea.y + 190);

        // 2. STAN ZIAREN (Wyświetlacz na górze)
        g.setColor(Color.WHITE);
        g.fillRect(grinderArea.x + 10, grinderArea.y - 25, 80, 20);
        g.setColor(Color.BLACK);
        g.drawRect(grinderArea.x + 10, grinderArea.y - 25, 80, 20);

        int beans = process.getCoffeeBeansLevel();
        if (beans < 10) g.setColor(Color.RED);
        else g.setColor(Color.BLACK);

        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Kawa: " + beans + "g", grinderArea.x + 15, grinderArea.y - 10);


        // 3. PASEK POSTĘPU (Tylko gdy mielimy lub proces trwa)
        if (process.getCurrentGrindTime() > 0) {
            drawProgressBar(g);
        }
    }

    private void drawProgressBar(Graphics g) {
        int barX = grinderArea.x + 10;
        int barY = grinderArea.y + 50;
        int barW = 100;
        int barH = 15;

        // Tło paska
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barW, barH);

        // Obliczanie szerokości
        float progress = process.getGrindProgress();
        int currentW = (int) (progress * barW);
        if (currentW > barW) currentW = barW;

        // Kolor zależny od czasu (Under=Żółty, Perfect=Zielony, Over=Czerwony)
        // Logika kolorów zgodna z KitchenProcess (3000ms +/- 500ms)
        long time = process.getCurrentGrindTime();
        Color c;
        String statusText;

        if (time < 2500) {
            c = Color.YELLOW;       // Za krótko
            statusText = "Mielenie...";
        } else if (time <= 3500) {
            c = Color.GREEN;        // Idealnie
            statusText = "IDEALNIE";
        } else {
            c = Color.RED;          // Za długo
            statusText = "PRZEMIELONA";
        }

        // Rysowanie paska
        g.setColor(c);
        g.fillRect(barX, barY, currentW, barH);

        // Ramka
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barW, barH);

        // Znacznik idealnego momentu (Biała kreska)
        g.setColor(Color.WHITE);
        g.drawLine(barX + 90, barY - 2, barX + 90, barY + barH + 2);

        // Tekst statusu
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString(statusText, barX, barY - 5);
    }
}