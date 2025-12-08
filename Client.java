import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class Client {

    private String name;
    private String order;
    private boolean showOrder;
    private boolean processed;

    private int x, y;
    private int width = 120;
    private int height = 150;

    private Rectangle acceptButton;
    private Rectangle rejectButton;

    private BufferedImage image; // grafika klienta

    public Client(String name, String order, int x, int y) {
        this.name = name;
        this.order = order;
        this.showOrder = false;
        this.processed = false;

        this.x = x;
        this.y = y;

        // przyciski rysowane pod zamówieniem (po prawej stronie klienta)
        int orderX = x + width + 20;
        int orderY = y + 20; // tekst zamówienia
        acceptButton = new Rectangle(orderX, orderY + 40, 100, 30);
        rejectButton = new Rectangle(orderX, orderY + 80, 100, 30);

        // wczytanie grafiki klienta
        try {
            image = ImageIO.read(new File("resources/Klient.jpg"));
        } catch (Exception e) {
            System.out.println("Nie udało się wczytać grafiki klienta!");
            image = null;
        }
    }

    public void update() {}

    public void render(Graphics g) {
        if (processed) return;

        // rysowanie grafiki klienta
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            g.setColor(new Color(200, 170, 150));
            g.fillRect(x, y, width, height);
        }

        // imię klienta nad obrazkiem
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString(name, x + 10, y + 20);

        // jeśli pokazujemy zamówienie
        if (showOrder) {
            int textX = x + width + 20; // obok klienta
            int textY = y + 20;

            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.setColor(Color.BLACK);
            g.drawString("Zamówienie:", textX, textY);
            g.drawString(order, textX, textY + 20);

            // przyciski pod zamówieniem
            g.setColor(Color.GREEN);
            g.fillRect(acceptButton.x, acceptButton.y, acceptButton.width, acceptButton.height);
            g.setColor(Color.WHITE);
            g.drawString("Akceptuj", acceptButton.x + 10, acceptButton.y + 20);

            g.setColor(Color.RED);
            g.fillRect(rejectButton.x, rejectButton.y, rejectButton.width, rejectButton.height);
            g.setColor(Color.WHITE);
            g.drawString("Odrzuć", rejectButton.x + 10, rejectButton.y + 20);
        }
    }

    public boolean contains(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height && !processed;
    }

    public boolean isAcceptButton(int mx, int my) {
        return acceptButton.contains(mx, my) && showOrder && !processed;
    }

    public boolean isRejectButton(int mx, int my) {
        return rejectButton.contains(mx, my) && showOrder && !processed;
    }

    public void showOrder() { showOrder = true; }

    public void acceptOrder() { processed = true; }

    public void rejectOrder() { processed = true; }

    public boolean isProcessed() { return processed; }

    public String getOrder() { return order; }

    public String getName() { return name; }
}
