import java.awt.*;

public class KitchenServeView {

    private final KitchenProcess process;

    // Obszar, na który trzeba upuścić filiżankę
    // Nie jest final, bo obliczamy go dynamicznie w renderze
    private Rectangle serveArea;

    // Zmienne do efektu wizualnego po wydaniu (Feedback)
    private long lastServeTime = 0;
    private boolean showFeedback = false;
    private String feedbackText = "";
    private Color feedbackColor = Color.GREEN;

    public KitchenServeView(KitchenProcess process) {
        this.process = process;
        // Domyślna inicjalizacja, zaktualizuje się w pierwszej klatce render()
        this.serveArea = new Rectangle(800, 200, 150, 120);
    }

    // =====================================================
    // LOGIKA WYDAWANIA (Wywoływana z KitchenItemsView)
    // =====================================================
    public boolean tryServe(Rectangle draggedItemRect) {
        // 1. Sprawdzamy czy gracz upuścił kubek na obszar wydawania
        if (serveArea.intersects(draggedItemRect)) {

            // 2. Sprawdzamy w logice, czy można wydać (czy kubek nie jest pusty)
            if (process.canServe()) {
                process.serveDrink(); // Zmienia stan na SERVED -> Kitchen.java to wykryje i naliczy kasę
                triggerSuccessFeedback();
                return true; // Zwracamy true, żeby ItemsView usunęło kubek z ręki
            } else {
                triggerErrorFeedback(); // Próba wydania pustego kubka
                return false;
            }
        }
        return false;
    }

    // =====================================================
    // FEEDBACK WIZUALNY
    // =====================================================
    private void triggerSuccessFeedback() {
        lastServeTime = System.currentTimeMillis();
        showFeedback = true;
        feedbackText = "WYDANO!";
        feedbackColor = new Color(0, 150, 0); // Ciemny Zielony
    }

    private void triggerErrorFeedback() {
        lastServeTime = System.currentTimeMillis();
        showFeedback = true;
        feedbackText = "PUSTE!";
        feedbackColor = Color.RED;
    }

    // =====================================================
    // RENDER
    // =====================================================
    public void render(Graphics g, int w) {
        // Aktualizujemy pozycję obszaru (prawa strona ekranu)
        // Dzięki temu działa przy zmianie rozmiaru okna
        int areaW = 160;
        int areaH = 100;
        int x = w - areaW - 30; // 30px marginesu od prawej
        int y = 120; // Wysokość lady

        this.serveArea = new Rectangle(x, y, areaW, areaH);

        // --- Rysowanie Okna / Lady Wydawczej ---

        // Cień/Tło
        g.setColor(new Color(60, 40, 20));
        g.fillRect(x, y, areaW, areaH);

        // Wnętrze (Blat - jasne drewno)
        g.setColor(new Color(210, 180, 140));
        g.fillRect(x + 5, y + 5, areaW - 10, areaH - 10);

        // Ozdobna ramka
        g.setColor(new Color(160, 130, 90));
        g.drawRect(x + 10, y + 10, areaW - 20, areaH - 20);

        // Napis "WYDAJ"
        g.setColor(new Color(80, 50, 30));
        g.setFont(new Font("Arial", Font.BOLD, 20));

        FontMetrics fm = g.getFontMetrics();
        String label = "WYDAJ";
        int tx = x + (areaW - fm.stringWidth(label)) / 2;
        int ty = y + (areaH + fm.getAscent()) / 2 - 5;
        g.drawString(label, tx, ty);

        // Ikonka dzwonka (uproszczona)
        g.setColor(Color.ORANGE);
        g.fillOval(x + areaW - 30, y + 10, 20, 20);
        g.setColor(Color.BLACK);
        g.drawOval(x + areaW - 30, y + 10, 20, 20);

        // --- Rysowanie Feedbacku (po udanym/nieudanym wydaniu) ---
        if (showFeedback) {
            long timePassed = System.currentTimeMillis() - lastServeTime;

            if (timePassed < 1500) { // Pokazuj przez 1.5 sekundy
                g.setColor(feedbackColor);
                g.setFont(new Font("Arial", Font.BOLD, 28));

                // Efekt lekkiego unoszenia się napisu
                int floatOffset = (int) (timePassed / 50);
                g.drawString(feedbackText, x + 20, y - 10 - floatOffset);
            } else {
                showFeedback = false;
            }
        }
    }
}