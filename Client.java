import java.awt.*;

public class Client {

    private final String name;
    private final String orderName; // np. "Latte"

    // NOWE: Preferowane mleko (null, jeśli to czarna kawa)
    private final KitchenProcess.MilkType milkPreference;

    private boolean processed = false;
    private int x, y;
    private final int width = 120;
    private final int height = 150;
    private int patience;
    private final int maxPatience;

    public Client(String name, String orderName, KitchenProcess.MilkType milkPreference, int x, int y) {
        this.name = name;
        this.orderName = orderName;
        this.milkPreference = milkPreference; // Może być null
        this.x = x;
        this.y = y;
        this.maxPatience = 600;
        this.patience = maxPatience;
    }

    // =====================================================
    // GETTERY
    // =====================================================
    public String getOrderName() {
        return orderName;
    }

    public KitchenProcess.MilkType getMilkPreference() {
        return milkPreference;
    }

    // Metoda pomocnicza do wyświetlania pełnego zamówienia
    public String getFullOrderText() {
        if (milkPreference == null) return orderName;
        // np. "Latte (Sojowe)"
        return orderName + " (" + milkPreference.label + ")";
    }

    // =====================================================
    // POZYCJA & LOGIKA (Bez zmian)
    // =====================================================
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void update() { if (!processed && patience > 0) patience--; }
    public boolean isOutOfPatience() { return patience <= 0; }
    public boolean isProcessed() { return processed; }
    public void acceptOrder() { processed = true; }
    public boolean contains(int mx, int my) { return mx >= x && mx <= x + width && my >= y && my <= y + height; }

    // =====================================================
    // RENDER (Drobna zmiana w wyświetlaniu)
    // =====================================================
    public void render(Graphics g) {
        if (processed) return;

        // Ciało
        g.setColor(new Color(200, 170, 150));
        g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);

        // Info
        int infoX = x + width + 15;
        int infoY = y;

        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString(name, infoX, infoY + 20);

        g.setFont(new Font("Arial", Font.PLAIN, 16));

        // Wyświetlamy zamówienie z rodzajem mleka
        String txt = getFullOrderText();
        g.drawString(txt, infoX, infoY + 45);

        // Pasek cierpliwości
        int barWidth = 140;
        int barHeight = 12;
        int filled = (int) ((patience / (float) maxPatience) * barWidth);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(infoX, infoY + 60, barWidth, barHeight);

        float ratio = patience / (float) maxPatience;
        g.setColor(new Color((int) (255 * (1 - ratio)), (int) (255 * ratio), 0));
        g.fillRect(infoX, infoY + 60, filled, barHeight);

        g.setColor(Color.BLACK);
        g.drawRect(infoX, infoY + 60, barWidth, barHeight);
    }

    // Kompatybilność wsteczna dla starego kodu (zwraca pełny tekst jako 'order')
    public String getOrder() {
        return getFullOrderText();
    }
}
