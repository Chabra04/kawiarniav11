import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {

    private static final String FILE_NAME = "save_data.txt";

    public static void save(int level, int score, double money, int clientsServed,
                            int beans, int mCow, int mLactose, int mSoy,
                            List<Client> activeClients) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            writer.write(String.valueOf(level)); writer.newLine();
            writer.write(String.valueOf(score)); writer.newLine();
            writer.write(String.valueOf(money)); writer.newLine();
            writer.write(String.valueOf(clientsServed)); writer.newLine();

            // Kuchnia
            writer.write(String.valueOf(beans)); writer.newLine();
            writer.write(String.valueOf(mCow)); writer.newLine();
            writer.write(String.valueOf(mLactose)); writer.newLine();
            writer.write(String.valueOf(mSoy)); writer.newLine();

            // KLIENCI
            if (activeClients != null) {
                writer.write(String.valueOf(activeClients.size()));
                writer.newLine();
                for (Client c : activeClients) {
                    // Format: Imie;Zamowienie;Mleko;Cierpliwosc
                    String milkStr = (c.getMilkPreference() == null) ? "NULL" : c.getMilkPreference().name();
                    String line = c.getName() + ";" + c.getOrderName() + ";" + milkStr + ";" + c.getPatience();
                    writer.write(line);
                    writer.newLine();
                }
            } else {
                writer.write("0");
                writer.newLine();
            }

            System.out.println("Zapisano stan gry.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load(Game game) {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String levelStr = reader.readLine();
            String scoreStr = reader.readLine();
            String moneyStr = reader.readLine();
            String clientsStr = reader.readLine();

            String beansStr = reader.readLine();
            String mCowStr = reader.readLine();
            String mLacStr = reader.readLine();
            String mSoyStr = reader.readLine();

            if (levelStr != null) game.currentLevel = Integer.parseInt(levelStr);
            if (scoreStr != null) game.score = Integer.parseInt(scoreStr);
            if (moneyStr != null) game.money = Double.parseDouble(moneyStr);
            if (clientsStr != null) game.clientsServedFromSave = Integer.parseInt(clientsStr);

            if (beansStr != null) game.loadedBeans = Integer.parseInt(beansStr);
            if (mCowStr != null) game.loadedMilkCow = Integer.parseInt(mCowStr);
            if (mLacStr != null) game.loadedMilkLactose = Integer.parseInt(mLacStr);
            if (mSoyStr != null) game.loadedMilkSoy = Integer.parseInt(mSoyStr);

            // WCZYTYWANIE KLIENTÓW
            String countStr = reader.readLine();
            game.loadedClients = new ArrayList<>();

            if (countStr != null) {
                int count = Integer.parseInt(countStr);
                for (int i = 0; i < count; i++) {
                    String line = reader.readLine();
                    if (line != null) {
                        String[] parts = line.split(";");
                        if (parts.length == 4) {
                            String name = parts[0];
                            String order = parts[1];
                            String milkStr = parts[2];
                            int patience = Integer.parseInt(parts[3]);

                            KitchenProcess.MilkType milk = milkStr.equals("NULL") ? null : KitchenProcess.MilkType.valueOf(milkStr);
                            game.loadedClients.add(new Client(name, order, milk, 0, 0, patience));
                        }
                    }
                }
            }

            System.out.println("Wczytano grę (klientów w sali: " + game.loadedClients.size() + ")");

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            game.currentLevel = 1;
            game.money = 300.0;
            game.loadedClients = new ArrayList<>();
        }
    }

    public static boolean exists() {
        File f = new File(FILE_NAME);
        return f.exists() && !f.isDirectory();
    }
}
