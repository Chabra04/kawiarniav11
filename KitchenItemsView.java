import java.awt.*;

public class KitchenItemsView {

    private final KitchenProcess process;
    private final Gameplay gameplay;
    private KitchenServeView serveView;

    // CENNIK I KARY
    private static final double COST_COFFEE_BAG = 15.0;
    private static final double COST_MILK_CARTON = 5.0;
    private static final double FINE_BAD_SEGREGATION = 2.0;

    // ITEMY I LOKALIZACJE
    public enum Item {
        NONE,
        PORTAFILTER, PORTAFILTER_WITH_COFFEE, PORTAFILTER_TAMPED,
        CUP, TAMPER, MILK_JUG,
        MILK_CARTON_COW, MILK_CARTON_LACTOSE, MILK_CARTON_SOY,
        COFFEE_BAG
    }

    public enum Location { BASE, HAND, GRINDER, MACHINE, FROTHER }

    private Item heldItem = Item.NONE;
    private boolean dragging = false;
    private boolean grindingInput = false;
    // Stany lokalne
    private boolean hasCoffee = false;
    private boolean tamped = false;

    // Lokalizacje przedmiotów
    private Location portafilterLoc = Location.BASE;
    private Location cupLoc = Location.BASE;
    private Location tamperLoc = Location.BASE;
    private Location jugLoc = Location.BASE;

    // POZYCJE (HITBOXY)
    // Baza (Blat)
    private final Rectangle portafilterBase = new Rectangle(60, 440, 160, 100);
    private final Rectangle cupBase         = new Rectangle(390, 440, 160, 120);
    private final Rectangle tamperBase      = new Rectangle(240, 440, 100, 100);
    private final Rectangle jugBase         = new Rectangle(680, 440, 120, 100);

    // Kartony mleka
    private final Rectangle milkCowBase     = new Rectangle(850, 340, 80, 160);
    private final Rectangle milkLactoseBase = new Rectangle(940, 340, 80, 160);
    private final Rectangle milkSoyBase     = new Rectangle(1030, 340, 80, 160);

    private final Rectangle coffeeBagBase   = new Rectangle(30, 600, 90, 140);

    // Obszary dynamiczne (ustawiane w configure)
    private Rectangle grinderArea;
    private Rectangle espressoMachine;
    private Rectangle frotherArea;

    private Rectangle trashBio;
    private Rectangle trashPlastic;
    private Rectangle trashMixed;

    // STEROWANIE
    private int mouseX, mouseY;
    private int dragOffsetX, dragOffsetY;

    // Animacje
    private boolean tamping = false;
    private long tampStart;
    private static final long TAMP_TIME = 1000;
    private boolean frothingInput = false;

    public KitchenItemsView(KitchenProcess process, Gameplay gameplay) {
        this.process = process;
        this.gameplay = gameplay;
    }

    // KONFIGURACJA
    public void configure(Rectangle grinder, Rectangle espresso, Rectangle frother, Rectangle trashArea) {
        this.grinderArea = grinder;
        this.espressoMachine = espresso;
        this.frotherArea = frother;

        int w = trashArea.width / 2;
        int h = trashArea.height;
        int x = trashArea.x;
        int y = trashArea.y;

        this.trashBio     = new Rectangle(x, y, w, h);
        this.trashPlastic = new Rectangle(x + w, y, w, h);
        this.trashMixed   = new Rectangle(x + 2 * w, y, w, h);
    }

    public void setServeView(KitchenServeView serveView) { this.serveView = serveView; }

    public void resetCupPosition() { this.cupLoc = Location.BASE; }

    public void onCoffeeGround() {
        hasCoffee = true;
        tamped = false;
        portafilterLoc = Location.GRINDER;
        heldItem = Item.NONE;
        dragging = false;
    }

    // INPUT
    public void mouseMoved(int x, int y) { mouseX = x; mouseY = y; }

    public boolean mousePressed(int x, int y) {

        // 1. Spieniacz
        if (jugLoc == Location.FROTHER && frotherArea.contains(x, y)) {
            if (process.getCurrentFrothTime() > 0) {
                frothingInput = false;
                process.takeJugFromFrother();
                startDrag(Item.MILK_JUG, frotherArea, x, y);
                jugLoc = Location.HAND;
                return true;
            } else {
                frothingInput = true;
                return true;
            }
        }

        // 2. Wyjmowanie z maszyn
        if (cupLoc == Location.MACHINE && process.canTakeCup() && espressoMachine.contains(x, y)) {
            startDrag(Item.CUP, espressoMachine, x, y); cupLoc = Location.HAND; process.takeCup(); return true;
        }
        if (portafilterLoc == Location.MACHINE && process.canTakePortafilterFromMachine() && espressoMachine.contains(x, y)) {
            startDrag(Item.PORTAFILTER_TAMPED, espressoMachine, x, y); portafilterLoc = Location.HAND; process.takePortafilterFromMachine(); return true;
        }
        // --- MŁYNEK ---
        if (portafilterLoc == Location.GRINDER && grinderArea.contains(x, y)) {
            if (!hasCoffee && process.canStartGrinding()) {
                grindingInput = true;
                return true;
            }
            else if (!grindingInput) {
                Item itemToTake = hasCoffee ? Item.PORTAFILTER_WITH_COFFEE : Item.PORTAFILTER;
                startDrag(itemToTake, grinderArea, x, y);
                portafilterLoc = Location.HAND;
                process.takePortafilterFromGrinder();
                return true;
            }
        }

        // 3. Drop w locie
        if (dragging) { drop(); return true; }

        // 4. Podnoszenie z bazy
        if (portafilterLoc == Location.BASE && portafilterBase.contains(x, y)) { startDrag(!hasCoffee ? Item.PORTAFILTER : !tamped ? Item.PORTAFILTER_WITH_COFFEE : Item.PORTAFILTER_TAMPED, portafilterBase, x, y); portafilterLoc = Location.HAND; return true; }
        if (cupLoc == Location.BASE && cupBase.contains(x, y)) { startDrag(Item.CUP, cupBase, x, y); cupLoc = Location.HAND; return true; }
        if (tamperLoc == Location.BASE && tamperBase.contains(x, y)) { startDrag(Item.TAMPER, tamperBase, x, y); tamperLoc = Location.HAND; return true; }
        if (jugLoc == Location.BASE && jugBase.contains(x, y)) { startDrag(Item.MILK_JUG, jugBase, x, y); jugLoc = Location.HAND; return true; }

        // 5. Zasoby
        if (milkCowBase.contains(x, y)) {
            startDrag(Item.MILK_CARTON_COW, milkCowBase, x, y);
            return true; }
        if (milkLactoseBase.contains(x, y)) {
            startDrag(Item.MILK_CARTON_LACTOSE, milkLactoseBase, x, y);
            return true; }
        if (milkSoyBase.contains(x, y)) {
            startDrag(Item.MILK_CARTON_SOY, milkSoyBase, x, y);
            return true; }
        if (coffeeBagBase.contains(x, y)) {
            startDrag(Item.COFFEE_BAG, coffeeBagBase, x, y);
            return true; }

        return false;
    }

    public void mouseReleased(int x, int y) {
        frothingInput = false;

        // Zatrzymanie mielenia
        if (grindingInput) {
            grindingInput = false;
            process.finishGrinding();
            if (process.getGrindResult() != null) {
                onCoffeeGround();
            }
        }

        if (dragging) drop();
    }
    public void update() {
        if (tamping && System.currentTimeMillis() - tampStart >= TAMP_TIME) { tamping = false; tamped = true; process.finishTamping(); }
        if (frothingInput && jugLoc == Location.FROTHER) process.processFrothing();

        if (grindingInput && portafilterLoc == Location.GRINDER) {
            process.processGrinding();
        }
    }
    private void startDrag(Item item, Rectangle src, int x, int y) { heldItem = item; dragging = true; dragOffsetX = x - src.x; dragOffsetY = y - src.y; }

    // DROP & LOGIKA EKONOMII/SEGREGACJI
    private void drop() {
        dragging = false;
        Rectangle dragged = new Rectangle(mouseX - dragOffsetX, mouseY - dragOffsetY, 100, 90);

        switch (heldItem) {
            // --- KOLBA ---
            case PORTAFILTER, PORTAFILTER_WITH_COFFEE, PORTAFILTER_TAMPED -> {
                if (dragged.intersects(grinderArea)) {
                    if (!hasCoffee) { portafilterLoc = Location.GRINDER; process.putPortafilterInGrinder(); }
                    else portafilterLoc = Location.BASE;
                }
                else if (dragged.intersects(espressoMachine) && process.canPutPortafilterInMachine()) {
                    portafilterLoc = Location.MACHINE; process.putPortafilterInMachine();
                }
                else if (dragged.intersects(trashBio)) {
                    hasCoffee = false; tamped = false; process.emptyPortafilter(); portafilterLoc = Location.BASE;
                }
                else if (dragged.intersects(trashPlastic) || dragged.intersects(trashMixed)) {
                    gameplay.spendMoney(FINE_BAD_SEGREGATION, "Kara: Fusy w złym koszu");
                    portafilterLoc = Location.BASE;
                } else portafilterLoc = Location.BASE;
            }

            // --- KUBEK ---
            case CUP -> {
                if (dragged.intersects(espressoMachine)) { cupLoc = Location.MACHINE; process.putCupInMachine(); }
                else if (serveView != null && serveView.tryServe(dragged)) { cupLoc = Location.BASE; }
                else if (dragged.intersects(trashMixed)) { process.emptyCup(); cupLoc = Location.BASE; }
                else if (dragged.intersects(trashBio) || dragged.intersects(trashPlastic)) {
                    gameplay.spendMoney(FINE_BAD_SEGREGATION, "Kara: Płyn w złym koszu");
                    cupLoc = Location.BASE;
                } else cupLoc = Location.BASE;
            }

            // --- DZBANEK ---
            case MILK_JUG -> {
                if (dragged.intersects(frotherArea) && process.hasMilkInJug()) { process.putJugInFrother(); jugLoc = Location.FROTHER; }
                else if (dragged.intersects(cupBase) && process.canPourMilkIntoCup()) { process.pourMilkIntoCup(); jugLoc = Location.BASE; }
                else if (dragged.intersects(trashMixed)) { process.emptyJug(); jugLoc = Location.BASE; }
                else if (dragged.intersects(trashBio) || dragged.intersects(trashPlastic)) {
                    gameplay.spendMoney(FINE_BAD_SEGREGATION, "Kara: Mleko w złym koszu");
                    jugLoc = Location.BASE;
                } else jugLoc = Location.BASE;
            }

            // --- KARTONY ---
            case MILK_CARTON_COW -> handleCartonDrop(dragged, KitchenProcess.MilkType.COW);
            case MILK_CARTON_LACTOSE -> handleCartonDrop(dragged, KitchenProcess.MilkType.LACTOSE_FREE);
            case MILK_CARTON_SOY -> handleCartonDrop(dragged, KitchenProcess.MilkType.SOY);

            // --- WOREK KAWY ---
            case COFFEE_BAG -> {
                if (dragged.intersects(grinderArea)) {
                    if (gameplay.getMoney() >= COST_COFFEE_BAG) {
                        gameplay.spendMoney(COST_COFFEE_BAG, "Zakup kawy (Ziarna)");
                        process.refillGrinder();
                    }
                }
            }

            // --- UBIJAK ---
            case TAMPER -> {
                if (dragged.intersects(portafilterBase) && portafilterLoc == Location.BASE && process.canStartTamping()) {
                    tamping = true; tampStart = System.currentTimeMillis(); process.startTamping(); tamperLoc = Location.BASE;
                } else tamperLoc = Location.BASE;
            }

            default -> {}
        }
        heldItem = Item.NONE;
    }

    private void handleCartonDrop(Rectangle dragged, KitchenProcess.MilkType type) {
        if (dragged.intersects(jugBase) && process.canPourMilkIntoJug(type)) {
            process.pourMilkIntoJug(type);
        }
        else if (dragged.intersects(trashPlastic)) {
            if (process.getMilkLevel(type) < 600) {
                if (gameplay.getMoney() >= COST_MILK_CARTON) {
                    gameplay.spendMoney(COST_MILK_CARTON, "Nowe mleko: " + type.label);
                    process.throwEmptyCarton(type);
                }
            }
        }
        else if (dragged.intersects(trashBio) || dragged.intersects(trashMixed)) {
            gameplay.spendMoney(FINE_BAD_SEGREGATION, "Kara: Karton w złym koszu");
        }
    }

    // RENDER
    public void render(Graphics g) {

        // --- KOSZE NA ŚMIECI ---
        drawTrashBin(g, trashBio, "BIO (Fusy)", new Color(139, 69, 19));
        drawTrashBin(g, trashPlastic, "PLASTIK", new Color(220, 220, 50));
        drawTrashBin(g, trashMixed, "ZLEW/MIX", new Color(60, 60, 60));

        // --- KOLBA NA BAZIE ---
        if (portafilterLoc == Location.BASE) {
            String label; Color c;
            if (process.isCoffeeUsed()) { label = "Fusy (Bio)"; c = new Color(50, 30, 20); }
            else if (tamped) { label = "Ubita"; c = new Color(100, 60, 40); }
            else if (hasCoffee) { label = "Kawa"; c = new Color(160, 120, 60); }
            else { label = "Kolba"; c = new Color(180, 180, 180); }
            draw(g, portafilterBase, label, c);
        }

        // --- INNE ITEMY NA BAZIE ---
        if (cupLoc == Location.BASE) drawCup(g, cupBase);
        if (tamperLoc == Location.BASE && !tamping) draw(g, tamperBase, "Ubijak", new Color(120,120,120));
        if (tamping) draw(g, portafilterBase, "Ubijanie...", new Color(100, 100, 100));

        // --- KARTONY ---
        drawCarton(g, milkCowBase, "Krowie", new Color(200, 200, 255), process.getMilkLevel(KitchenProcess.MilkType.COW));
        drawCarton(g, milkLactoseBase, "Bez Lakt.", new Color(220, 180, 220), process.getMilkLevel(KitchenProcess.MilkType.LACTOSE_FREE));
        drawCarton(g, milkSoyBase, "Sojowe", new Color(200, 240, 200), process.getMilkLevel(KitchenProcess.MilkType.SOY));

        draw(g, coffeeBagBase, "Ziarna", new Color(80, 50, 30));

        // --- DZBANEK NA BAZIE ---
        if (jugLoc == Location.BASE) {
            String label = "Dzbanek"; Color c = new Color(200, 200, 255);
            if (process.hasMilkInJug()) {
                label = (process.getCurrentMilkType() != null ? process.getCurrentMilkType().label : "?") + " ";
                label += switch (process.getMilkState()) {
                    case COLD -> "(Zimne)";
                    case WARM -> "(Letnie)";
                    case PERFECT -> "(Latte)";
                    case FOAMY -> "(PIANKA)";
                    case BURNT -> "(Spalone)";
                };
                if (process.getMilkState() == KitchenProcess.MilkState.BURNT) c = new Color(120, 80, 80);
            }
            draw(g, jugBase, label, c);
        }

        // --- PRZECIĄGANIE (DRAGGING) ---
        if (dragging) {
            Rectangle r = new Rectangle(mouseX - dragOffsetX, mouseY - dragOffsetY, 90, 80);

            if (heldItem == Item.CUP) drawCup(g, r);
            else if (heldItem == Item.MILK_CARTON_COW) drawCarton(g, new Rectangle(mouseX - dragOffsetX, mouseY - dragOffsetY, 70, 100), "Krowie", new Color(200,200,255), process.getMilkLevel(KitchenProcess.MilkType.COW));
            else if (heldItem == Item.MILK_CARTON_LACTOSE) drawCarton(g, new Rectangle(mouseX - dragOffsetX, mouseY - dragOffsetY, 70, 100), "Bez Lakt.", new Color(220,180,220), process.getMilkLevel(KitchenProcess.MilkType.LACTOSE_FREE));
            else if (heldItem == Item.MILK_CARTON_SOY) drawCarton(g, new Rectangle(mouseX - dragOffsetX, mouseY - dragOffsetY, 70, 100), "Sojowe", new Color(200,240,200), process.getMilkLevel(KitchenProcess.MilkType.SOY));
            else if (heldItem == Item.COFFEE_BAG) draw(g, new Rectangle(mouseX - dragOffsetX, mouseY - dragOffsetY, 60, 80), "Ziarna", new Color(80, 50, 30));
            else if (heldItem == Item.PORTAFILTER_TAMPED && process.isCoffeeUsed()) draw(g, r, "Fusy", new Color(50, 30, 20));
            else if (heldItem == Item.TAMPER || !tamping) {
                g.setColor(Color.ORANGE); g.fillRect(r.x, r.y, r.width, r.height); g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.PLAIN, 10)); g.drawString(heldItem.toString(), r.x, r.y - 5);
            }
        }
    }

    // ===================== METODY POMOCNICZE =====================

    private void drawTrashBin(Graphics g, Rectangle r, String label, Color c) {
        g.setColor(c); g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.BLACK); g.drawRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(label, r.x + 5, r.y + 20);
    }

    // --- METODA RYSOWANIA KUBKA ---
    private void drawCup(Graphics g, Rectangle r) {
        KitchenProcess.BrewType drink = process.getCurrentDrink();

        // Etykieta na kubku
        String label = switch (drink) {
            case EMPTY -> "Filiżanka";
            case HOT_WATER -> "Wrzątek";
            case ESPRESSO -> "Espresso";
            case DOUBLE_ESPRESSO -> "Dbl Esp.";
            case AMERICANO -> "Americano";
            case LATTE -> "Latte " + (process.getCupMilkType() != null ? process.getCupMilkType().label : "");
            case CAPPUCCINO -> "Cappuccino " + (process.getCupMilkType() != null ? process.getCupMilkType().label : "");
            case DIRTY_WATER -> "Pomyje";
        };

        // Kolor napoju
        Color c = Color.WHITE;
        if (drink == KitchenProcess.BrewType.ESPRESSO || drink == KitchenProcess.BrewType.DOUBLE_ESPRESSO) c = new Color(100, 60, 30);
        else if (drink == KitchenProcess.BrewType.LATTE) c = new Color(210, 180, 140);
        else if (drink == KitchenProcess.BrewType.CAPPUCCINO) c = new Color(225, 200, 160); // Jaśniejsze dla Cappuccino

        g.setColor(c);
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.BLACK);
        g.drawRect(r.x, r.y, r.width, r.height);

        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.setColor(c == Color.WHITE ? Color.BLACK : Color.WHITE); // Kontrast
        g.drawString(label, r.x + 2, r.y + r.height / 2);
    }

    private void drawCarton(Graphics g, Rectangle r, String label, Color c, int level) {
        g.setColor(c); g.fillRect(r.x, r.y, r.width, r.height); g.setColor(Color.BLACK); g.drawRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.WHITE); g.fillRect(r.x + 10, r.y + 10, r.width - 20, 20); g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.BOLD, 11)); g.drawString(label, r.x + 5, r.y + 50);
        if (level == 0) { g.setColor(Color.RED); g.setFont(new Font("Arial", Font.BOLD, 12)); g.drawString("PUSTY", r.x + 10, r.y + 80); }
        else { g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.PLAIN, 10)); g.drawString(level + " ml", r.x + 10, r.y + 80); }
    }

    private void draw(Graphics g, Rectangle r, String label, Color c) {
        g.setColor(c); g.fillRect(r.x, r.y, r.width, r.height); g.setColor(Color.BLACK); g.drawRect(r.x, r.y, r.width, r.height);
        if (c.getRed() < 100 && c.getGreen() < 100 && c.getBlue() < 100) g.setColor(Color.WHITE); else g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12)); g.drawString(label, r.x + 5, r.y + r.height / 2);
    }
}
