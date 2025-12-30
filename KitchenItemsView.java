import java.awt.*;

public class KitchenItemsView {

    private final KitchenProcess process;
    private final Gameplay gameplay;
    private KitchenServeView serveView;

    // =====================================================
    // CENNIK I KARY
    // =====================================================
    private static final double COST_COFFEE_BAG = 20.0;     // Koszt worka kawy
    private static final double COST_MILK_CARTON = 8.0;     // Koszt nowego mleka
    private static final double FINE_BAD_SEGREGATION = 5.0; // Kara za złe wyrzucenie

    // =====================================================
    // ITEMY I LOKALIZACJE
    // =====================================================
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
    // Stany lokalne (wizualne)
    private boolean hasCoffee = false;
    private boolean tamped = false;

    // Lokalizacje przedmiotów
    private Location portafilterLoc = Location.BASE;
    private Location cupLoc = Location.BASE;
    private Location tamperLoc = Location.BASE;
    private Location jugLoc = Location.BASE;

    // =====================================================
    // POZYCJE (HITBOXY)
    // =====================================================
    // Baza (Blat)
    private final Rectangle portafilterBase = new Rectangle(50, 420, 140, 80);
    private final Rectangle cupBase         = new Rectangle(210, 420, 100, 100);
    private final Rectangle tamperBase      = new Rectangle(330, 420, 80, 100);
    private final Rectangle jugBase         = new Rectangle(430, 420, 100, 100);

    // Kartony mleka
    private final Rectangle milkCowBase     = new Rectangle(550, 400, 70, 120);
    private final Rectangle milkLactoseBase = new Rectangle(630, 400, 70, 120);
    private final Rectangle milkSoyBase     = new Rectangle(710, 400, 70, 120);

    // Worek kawy
    private final Rectangle coffeeBagBase   = new Rectangle(0, 320, 50, 80);

    // Obszary dynamiczne (ustawiane w configure)
    private Rectangle grinderArea;
    private Rectangle espressoMachine;
    private Rectangle frotherArea;

    // Kosze na śmieci
    private Rectangle trashBio;      // Brązowy (Fusy)
    private Rectangle trashPlastic;  // Żółty (Kartony)
    private Rectangle trashMixed;    // Szary/Czarny (Płyny/Zlew)

    // =====================================================
    // STEROWANIE
    // =====================================================
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

    // =====================================================
    // KONFIGURACJA
    // =====================================================
    public void configure(Rectangle grinder, Rectangle espresso, Rectangle frother, Rectangle trashArea) {
        this.grinderArea = grinder;
        this.espressoMachine = espresso;
        this.frotherArea = frother;

        // Dzielimy obszar kosza na 3 strefy
        int w = trashArea.width / 3;
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

    // =====================================================
    // INPUT
    // =====================================================
    public void mouseMoved(int x, int y) { mouseX = x; mouseY = y; }

    public boolean mousePressed(int x, int y) {

        // 1. Spieniacz
        if (jugLoc == Location.FROTHER && frotherArea.contains(x, y)) {
            if (process.getCurrentFrothTime() > 0) {
                frothingInput = false; process.takeJugFromFrother(); startDrag(Item.MILK_JUG, frotherArea, x, y); jugLoc = Location.HAND; return true;
            } else { frothingInput = true; return true; }
        }

        // 2. Wyjmowanie z maszyn
        if (cupLoc == Location.MACHINE && process.canTakeCup() && espressoMachine.contains(x, y)) {
            startDrag(Item.CUP, espressoMachine, x, y); cupLoc = Location.HAND; process.takeCup(); return true;
        }
        if (portafilterLoc == Location.MACHINE && process.canTakePortafilterFromMachine() && espressoMachine.contains(x, y)) {
            startDrag(Item.PORTAFILTER_TAMPED, espressoMachine, x, y); portafilterLoc = Location.HAND; process.takePortafilterFromMachine(); return true;
        }
        // --- MŁYNEK (NOWA OBSŁUGA) ---
        if (portafilterLoc == Location.GRINDER && grinderArea.contains(x, y)) {
            // Jeśli nie ma kawy i można mielić -> zacznij mielić
            if (!hasCoffee && process.canStartGrinding()) {
                grindingInput = true;
                return true;
            }
            // Jeśli już jest zmielona LUB nie można mielić (brak ziaren) -> wyjmij
            else if (!grindingInput) { // Blokujemy wyjmowanie w trakcie mielenia
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

        // 5. Zasoby (Zawsze dostępne)
        if (milkCowBase.contains(x, y)) { startDrag(Item.MILK_CARTON_COW, milkCowBase, x, y); return true; }
        if (milkLactoseBase.contains(x, y)) { startDrag(Item.MILK_CARTON_LACTOSE, milkLactoseBase, x, y); return true; }
        if (milkSoyBase.contains(x, y)) { startDrag(Item.MILK_CARTON_SOY, milkSoyBase, x, y); return true; }
        if (coffeeBagBase.contains(x, y)) { startDrag(Item.COFFEE_BAG, coffeeBagBase, x, y); return true; }

        return false;
    }

    public void mouseReleased(int x, int y) {
        frothingInput = false;

        // Zatrzymanie mielenia
        if (grindingInput) {
            grindingInput = false;
            process.finishGrinding();
            if (process.getGrindResult() != null) {
                // Mielenie zakończone, ustaw flagę
                onCoffeeGround();
            }
        }

        if (dragging) drop();
    }
    public void update() {
        if (tamping && System.currentTimeMillis() - tampStart >= TAMP_TIME) { tamping = false; tamped = true; process.finishTamping(); }
        if (frothingInput && jugLoc == Location.FROTHER) process.processFrothing();

        // Przetwarzanie mielenia
        if (grindingInput && portafilterLoc == Location.GRINDER) {
            process.processGrinding();
        }
    }
    private void startDrag(Item item, Rectangle src, int x, int y) { heldItem = item; dragging = true; dragOffsetX = x - src.x; dragOffsetY = y - src.y; }

    // =====================================================
    // DROP & LOGIKA EKONOMII/SEGREGACJI
    // =====================================================
    private void drop() {
        dragging = false;
        Rectangle dragged = new Rectangle(mouseX - dragOffsetX, mouseY - dragOffsetY, 90, 80);

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
                // SEGREGACJA: Fusy tylko do BIO
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
                // SEGREGACJA: Płyny do ZLEWU (MIX)
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
                // SEGREGACJA: Płyny do ZLEWU (MIX)
                else if (dragged.intersects(trashMixed)) { process.emptyJug(); jugLoc = Location.BASE; }
                else if (dragged.intersects(trashBio) || dragged.intersects(trashPlastic)) {
                    gameplay.spendMoney(FINE_BAD_SEGREGATION, "Kara: Mleko w złym koszu");
                    jugLoc = Location.BASE;
                } else jugLoc = Location.BASE;
            }

            // --- KARTONY (Nalewanie / Kupno / Wyrzucanie) ---
            case MILK_CARTON_COW -> handleCartonDrop(dragged, KitchenProcess.MilkType.COW);
            case MILK_CARTON_LACTOSE -> handleCartonDrop(dragged, KitchenProcess.MilkType.LACTOSE_FREE);
            case MILK_CARTON_SOY -> handleCartonDrop(dragged, KitchenProcess.MilkType.SOY);

            // --- WOREK KAWY (Zakup) ---
            case COFFEE_BAG -> {
                if (dragged.intersects(grinderArea)) {
                    if (gameplay.getMoney() >= COST_COFFEE_BAG) {
                        gameplay.spendMoney(COST_COFFEE_BAG, "Zakup kawy (Ziarna)");
                        process.refillGrinder();
                    } else {
                        System.out.println("Brak środków na kawę!");
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

    // Pomocnicza metoda do obsługi kartonów (DRY principle)
    private void handleCartonDrop(Rectangle dragged, KitchenProcess.MilkType type) {
        // 1. Nalewanie do dzbanka
        if (dragged.intersects(jugBase) && process.canPourMilkIntoJug(type)) {
            process.pourMilkIntoJug(type);
        }
        // 2. Wyrzucanie do PLASTIKU (Kupno nowego)
        else if (dragged.intersects(trashPlastic)) {
            if (process.getMilkLevel(type) < 600) { // Tylko jeśli zużyty
                if (gameplay.getMoney() >= COST_MILK_CARTON) {
                    gameplay.spendMoney(COST_MILK_CARTON, "Nowe mleko: " + type.label);
                    process.throwEmptyCarton(type);
                } else {
                    System.out.println("Brak środków na mleko!");
                }
            }
        }
        // 3. Kary za zły kosz
        else if (dragged.intersects(trashBio) || dragged.intersects(trashMixed)) {
            gameplay.spendMoney(FINE_BAD_SEGREGATION, "Kara: Karton w złym koszu");
        }
    }

    // =====================================================
    // RENDER
    // =====================================================
    public void render(Graphics g) {

        // --- KOSZE NA ŚMIECI ---
        drawTrashBin(g, trashBio, "BIO (Fusy)", new Color(139, 69, 19));
        drawTrashBin(g, trashPlastic, "PLASTIK", new Color(220, 220, 50));
        drawTrashBin(g, trashMixed, "ZLEW/MIX", new Color(60, 60, 60));

        // --- MŁYNEK (Stan ziaren) ---
        g.setColor(Color.WHITE);
        g.fillRect(grinderArea.x + 10, grinderArea.y - 25, 80, 20);
        g.setColor(Color.BLACK);
        g.drawRect(grinderArea.x + 10, grinderArea.y - 25, 80, 20);

        int beans = process.getCoffeeBeansLevel();
        if (beans < 10) g.setColor(Color.RED); else g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Kawa: " + beans + "g", grinderArea.x + 15, grinderArea.y - 10);

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

        // --- WOREK KAWY ---
        draw(g, coffeeBagBase, "Ziarna", new Color(80, 50, 30));

        // --- DZBANEK NA BAZIE ---
        if (jugLoc == Location.BASE) {
            String label = "Dzbanek"; Color c = new Color(200, 200, 255);
            if (process.hasMilkInJug()) {
                label = (process.getCurrentMilkType() != null ? process.getCurrentMilkType().label : "?") + " ";
                label += switch (process.getMilkState()) { case COLD -> "(Zimne)"; case WARM -> "(Letnie)"; case PERFECT -> "(OK)"; case BURNT -> "(Spalone)"; };
                if (process.getMilkState() == KitchenProcess.MilkState.BURNT) c = new Color(120, 80, 80);
            }
            draw(g, jugBase, label, c);
        }

        // --- DZBANEK W SPIENIACZU ---
        if (jugLoc == Location.FROTHER) {
            int jx = frotherArea.x + (frotherArea.width - 90) / 2; int jy = frotherArea.y + 45;
            Rectangle r = new Rectangle(jx, jy, 90, 80);
            String label = "Spienianie...";
            if (process.getCurrentFrothTime() > 0 && !frothingInput) label = "Wyjmij";
            else if (process.hasMilkInJug()) label = (process.getCurrentMilkType() != null ? process.getCurrentMilkType().label : "");
            Color c = (process.getMilkState() == KitchenProcess.MilkState.BURNT) ? new Color(120, 80, 80) : new Color(220, 220, 255);
            draw(g, r, label, c);
        }

        // --- PRZECIĄGANIE (DRAGGING) ---
        if (dragging) {
            Rectangle r = new Rectangle(mouseX - dragOffsetX, mouseY - dragOffsetY, 90, 80);

            if (heldItem == Item.CUP) drawCup(g, r);
            else if (heldItem == Item.MILK_CARTON_COW) drawCarton(g, new Rectangle(mouseX - dragOffsetX, mouseY - dragOffsetY, 70, 100), "Krowie", new Color(200,200,255), process.getMilkLevel(KitchenProcess.MilkType.COW));
            else if (heldItem == Item.MILK_CARTON_LACTOSE) drawCarton(g, new Rectangle(mouseX - dragOffsetX, mouseY - dragOffsetY, 70, 100), "Bez Lakt.", new Color(220,180,220), process.getMilkLevel(KitchenProcess.MilkType.LACTOSE_FREE));
            else if (heldItem == Item.MILK_CARTON_SOY) drawCarton(g, new Rectangle(mouseX - dragOffsetX, mouseY - dragOffsetY, 70, 100), "Sojowe", new Color(200,240,200), process.getMilkLevel(KitchenProcess.MilkType.SOY));
            else if (heldItem == Item.COFFEE_BAG) draw(g, new Rectangle(mouseX - dragOffsetX, mouseY - dragOffsetY, 60, 80), "Ziarna", new Color(80, 50, 30));
            else if (heldItem == Item.PORTAFILTER_TAMPED && process.isCoffeeUsed()) draw(g, r, "Fusy", new Color(50, 30, 20)); // Fusy w ręku
            else if (heldItem == Item.TAMPER || !tamping) {
                g.setColor(Color.ORANGE); g.fillRect(r.x, r.y, r.width, r.height); g.setColor(Color.BLACK); g.setFont(new Font("Arial", Font.PLAIN, 10)); g.drawString(heldItem.toString(), r.x, r.y - 5);
            }
        }
    }

    // ===================== METODY POMOCNICZE (DRY) =====================

    private void drawTrashBin(Graphics g, Rectangle r, String label, Color c) {
        g.setColor(c); g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.BLACK); g.drawRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(label, r.x + 5, r.y + 20);
    }

    private void drawCup(Graphics g, Rectangle r) {
        KitchenProcess.BrewType drink = process.getCurrentDrink();
        String label = switch (drink) {
            case EMPTY -> "filiżanka"; case HOT_WATER -> "Wrzątek"; case ESPRESSO -> "Espresso";
            case DOUBLE_ESPRESSO -> "Dbl Esp."; case AMERICANO -> "Americano";
            case LATTE -> "Latte " + (process.getCupMilkType() != null ? process.getCupMilkType().label : "");
            case DIRTY_WATER -> "Pomyje";
        };
        Color c = (drink == KitchenProcess.BrewType.ESPRESSO || drink == KitchenProcess.BrewType.DOUBLE_ESPRESSO) ? new Color(100, 60, 30) : Color.WHITE;
        if (drink == KitchenProcess.BrewType.LATTE) c = new Color(210, 180, 140);
        g.setColor(c); g.fillRect(r.x, r.y, r.width, r.height); g.setColor(Color.BLACK); g.drawRect(r.x, r.y, r.width, r.height);
        g.setFont(new Font("Arial", Font.BOLD, 10)); g.setColor(c == Color.WHITE ? Color.BLACK : Color.WHITE); g.drawString(label, r.x + 2, r.y + r.height / 2);
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