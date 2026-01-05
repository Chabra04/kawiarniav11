import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Client {

    private final String name;
    private final String orderName;
    private final KitchenProcess.MilkType milkPreference;

    private boolean processed = false;
    private int x, y;
    private final int width = 120;
    private final int height = 150;

    // Cierpliwość
    private int patience;
    private final int maxPatience;

    // KONSTRUKTOR 1 Nowy klient
    public Client(String name, String orderName, KitchenProcess.MilkType milkPreference, int x, int y) {
        this(name, orderName, milkPreference, x, y, 7200);
    }

    // KONSTRUKTOR 2 Wczytany klient
    public Client(String name, String orderName, KitchenProcess.MilkType milkPreference, int x, int y, int patience) {
        this.name = name;
        this.orderName = orderName;
        this.milkPreference = milkPreference;
        this.x = x;
        this.y = y;
        this.maxPatience = 7800;
        this.patience = patience;
    }

    private static BufferedImage clientImage;

    static {
        try {
            clientImage = ImageIO.read(new File("resources/Klient.jpg"));
        } catch (IOException e) {
            clientImage = null;
        }
    }

    public String getOrderName() { return orderName; }
    public KitchenProcess.MilkType getMilkPreference() { return milkPreference; }
    public String getName() { return name; }
    public int getPatience() { return patience; } // Getter do zapisu

    public String getFullOrderText() {
        if (milkPreference == null) return orderName;
        return orderName + " (" + milkPreference.label + ")";
    }

    public void setPosition(int x, int y) { this.x = x; this.y = y; }

    public void update() {
        if (!processed && patience > 0) patience--;
    }

    public boolean isOutOfPatience() { return patience <= 0; }
    public boolean isProcessed() { return processed; }
    public void acceptOrder() { processed = true; }
    public boolean contains(int mx, int my) { return mx >= x && mx <= x + width && my >= y && my <= y + height; }

    public void render(Graphics g) {
        if (processed) return;

        if (clientImage != null) {
            g.drawImage(clientImage, x, y, width, height, null);
        } else {
            g.setColor(new Color(200, 170, 150));
            g.fillRect(x, y, width, height);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, height);
        }

        int infoX = x + width + 15;
        int infoY = y;

        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString(name, infoX, infoY + 20);

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString(getFullOrderText(), infoX, infoY + 45);

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
}
