import java.awt.*;

public class KitchenServeView {

    private final KitchenProcess process;
    private Rectangle serveArea;

    // Zmienne do efektu wizualnego
    private long lastServeTime = 0;
    private boolean showFeedback = false;
    private String feedbackText = "";
    private Color feedbackColor = Color.GREEN;

    public KitchenServeView(KitchenProcess process) {
        this.process = process;
        this.serveArea = new Rectangle(800, 200, 150, 120);
    }

    // METODA DO WYWOŁANIA NAPISU
    public void triggerFeedback(String text, Color color) {
        this.feedbackText = text;
        this.feedbackColor = color;
        this.showFeedback = true;
        this.lastServeTime = System.currentTimeMillis();
    }

    public boolean isFeedbackActive() {
        return showFeedback;
    }

    // LOGIKA UPUSZCZANIA
    public boolean tryServe(Rectangle draggedItemRect) {
        if (serveArea.intersects(draggedItemRect)) {
            if (process.canServe()) {
                process.serveDrink();
                return true;
            } else {
                triggerFeedback("PUSTE!", Color.RED);
                return false;
            }
        }
        return false;
    }

    // RENDER
    public void render(Graphics g, int w) {
        int areaW = 160;
        int areaH = 100;
        int x = w - areaW - 30;
        int y = 120;

        this.serveArea = new Rectangle(x, y, areaW, areaH);

        // Rysowanie okna
        g.setColor(new Color(60, 40, 20));
        g.fillRect(x, y, areaW, areaH);
        g.setColor(new Color(210, 180, 140));
        g.fillRect(x + 5, y + 5, areaW - 10, areaH - 10);
        g.setColor(new Color(160, 130, 90));
        g.drawRect(x + 10, y + 10, areaW - 20, areaH - 20);

        g.setColor(new Color(80, 50, 30));
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String label = "WYDAJ";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, x + (areaW - fm.stringWidth(label)) / 2, y + (areaH + fm.getAscent()) / 2 - 5);

        // Rysowanie dzwonka
        g.setColor(Color.ORANGE);
        g.fillOval(x + areaW - 30, y + 10, 20, 20);
        g.setColor(Color.BLACK);
        g.drawOval(x + areaW - 30, y + 10, 20, 20);

        // --- RYSOWANIE NAPISU FEEDBACKU ---
        if (showFeedback) {
            long timePassed = System.currentTimeMillis() - lastServeTime;

            // Wyświetlaj przez 2 sekundy
            if (timePassed < 2000) {
                g.setColor(feedbackColor);
                // Duży, wyraźny napis
                g.setFont(new Font("Arial", Font.BOLD, 32));

                // Efekt cienia pod tekstem
                g.setColor(Color.BLACK);
                g.drawString(feedbackText, x - 50 + 2, y + 150 + 2);

                g.setColor(feedbackColor);
                g.drawString(feedbackText, x - 50, y + 150);
            } else {
                showFeedback = false;
            }
        }
    }
}
