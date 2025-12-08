import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Level {

    private int levelNumber;
    private int timeLimit;
    private int timeLeft;
    private boolean started;
    private boolean finished;

    private List<Client> clients;
    private List<String> acceptedOrders;
    private Random random = new Random();

    private Gameplay gameplay;

    private String[] names = {"Anna", "Piotr", "Julia", "Marek", "Krzysztof",
            "Natalia", "Kinga", "Oskar", "Bartek", "Zuzanna"};

    private String[] drinks = {"Latte", "Espresso", "Cappuccino", "Americano", "Mocha"};

    public Level(int number, int timeLimit, Gameplay gameplay) {
        this.levelNumber = number;
        this.timeLimit = timeLimit;
        this.timeLeft = timeLimit;
        this.gameplay = gameplay;

        this.started = false;
        this.finished = false;

        clients = new ArrayList<>();
        acceptedOrders = new ArrayList<>();
    }

    public void start() {
        started = true;
        finished = false;
        timeLeft = timeLimit;
        generateClients(3); // generujemy 3 klientów
    }

    private void generateClients(int count) {
        clients.clear();

        int screenWidth = 800; // tutaj można dynamicznie pobrać szerokość okna
        int startX = screenWidth / 2 - 60; // wyśrodkowanie klientów (szerokość klienta = 120)
        int startY = 200;
        int gap = 180;

        for (int i = 0; i < count; i++) {
            String randomName = names[random.nextInt(names.length)];
            String randomDrink = drinks[random.nextInt(drinks.length)];
            clients.add(new Client(randomName, randomDrink, startX, startY + i * gap));
        }
    }

    public void update() {
        if (!started || finished) return;

        if (timeLeft > 0) timeLeft--;
        else finish();
    }

    // obsługa kliknięcia myszy
    public void click(int mx, int my) {
        for (Client c : clients) {
            if (c.contains(mx, my)) {
                c.showOrder();
            }
            if (c.isAcceptButton(mx, my)) {
                accept(c);
            }
            if (c.isRejectButton(mx, my)) {
                reject(c);
            }
        }
    }

    private void accept(Client c) {
        if (!c.isProcessed()) {
            c.acceptOrder();
            acceptedOrders.add(c.getOrder());
            gameplay.addScore(10);
            checkLevelEnd();
        }
    }

    private void reject(Client c) {
        if (!c.isProcessed()) {
            c.rejectOrder();
            gameplay.addScore(-5);
            checkLevelEnd();
        }
    }

    private void checkLevelEnd() {
        boolean allProcessed = true;
        for (Client c : clients) {
            if (!c.isProcessed()) {
                allProcessed = false;
                break;
            }
        }
        if (allProcessed) finish();
    }

    private void finish() {
        finished = true;
        started = false;
        SaveManager.save(levelNumber, gameplay.getScore());
    }

    public void render(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("Poziom " + levelNumber, 50, 50);

        g.setFont(new Font("Arial", Font.PLAIN, 30));
        g.drawString("Pozostały czas: " + timeLeft, 50, 100);
        g.drawString("Punkty: " + gameplay.getScore(), 50, 150);

        // rysowanie klientów
        for (Client c : clients) {
            c.render(g);
        }

        // lista zaakceptowanych zamówień po prawej
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Zamówienia zaakceptowane:", 500, 50);
        int y = 80;
        for (String order : acceptedOrders) {
            g.drawString(order, 500, y);
            y += 25;
        }

        if (finished) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Koniec poziomu!", 200, 400);
        }
    }

    public List<Client> getClients() {
        return clients;
    }

    public boolean isFinished() {
        return finished;
    }
}
