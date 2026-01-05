public class KitchenProcess {

    // ===================== ETAPY I STANY =====================
    public enum Step {
        TAKE_PORTAFILTER,
        PORTAFILTER_IN_GRINDER,
        COFFEE_GROUND,
        PORTAFILTER_READY,
        COFFEE_TAMPED,
        PORTAFILTER_IN_MACHINE,
        CUP_IN_MACHINE,
        BREWING,
        DRINK_READY,
        CUP_TAKEN,
        SERVED
    }

    public enum GrindResult {
        UNDER,      // Za krótko
        PERFECT,    // Idealnie
        OVER        // Za długo
    }

    public enum BrewType {
        EMPTY,
        HOT_WATER,
        ESPRESSO,
        DOUBLE_ESPRESSO,
        AMERICANO,
        LATTE,
        CAPPUCCINO,
        DIRTY_WATER
    }

    public enum MilkState {
        COLD,
        WARM,
        PERFECT,
        FOAMY,
        BURNT
    }

    public enum MilkType {
        COW("Krowie"),
        LACTOSE_FREE("Bez laktozy"),
        SOY("Sojowe");

        public final String label;
        MilkType(String label) {
            this.label = label;
        }
    }

    // ===================== STAN OGÓLNY =====================
    private Step step = Step.TAKE_PORTAFILTER;

    // ===================== ZASOBY (EKONOMIA) =====================

    // --- MŁYNEK ---
    private static final int GRINDER_CAPACITY = 60;
    private static final int GRIND_COST = 10;
    private int coffeeBeansLevel = GRINDER_CAPACITY;

    // --- KARTONY MLEKA ---
    private static final int CARTON_CAPACITY = 600;
    private static final int POUR_AMOUNT = 150;

    private int cowMilkLevel = CARTON_CAPACITY;
    private int lactoseMilkLevel = CARTON_CAPACITY;
    private int soyMilkLevel = CARTON_CAPACITY;

    // ===================== TIMERY I CZASY =====================

    // 1. MIELENIE (Manualne)
    private static final long TARGET_GRIND_TIME = 3000;
    private long currentGrindTime = 0;

    // 2. PARZENIE (Automatyczne)
    private static final long BREW_TIME = 3000;
    private long brewStartTime = 0;

    // 3. SPIENIANIE (Manualne)
    private static final long TARGET_FROTH_TIME = 3000;
    private long currentFrothTime = 0;
    private long lastFrothInteraction = 0;
    private static final long TARGET_LATTE_TIME = 3000;
    private static final long TARGET_CAPPUCCINO_TIME = 4000;

    private static final long TOLERANCE = 400;

    // ===================== ZAWARTOŚĆ KUBKA (NAPÓJ) =====================
    private int espressoShots = 0;
    private boolean hasHotWater = false;
    private boolean hasMilk = false;

    // Jakość składników w kubku
    private GrindResult cupGrindResult = null;
    private MilkState cupMilkState = null;
    private MilkType cupMilkType = null;

    // ===================== STAN URZĄDZEŃ =====================
    // Kolba
    private boolean coffeeInPortafilter = false;
    private boolean coffeeTamped = false;
    private boolean coffeeUsed = false; // Fusy
    private GrindResult grindResult = null; // Jakość mielenia w kolbie

    // Ekspres
    private boolean portafilterInMachine = false;
    private boolean cupInMachine = false;
    private int pendingShots = 1;

    // Serwis
    private boolean served = false;

    // Dzbanek
    private boolean milkInJug = false;
    private boolean jugInFrother = false;
    private MilkType currentMilkType = null;
    private MilkState milkState = MilkState.COLD;

    // ===================== GETTERY =====================
    public Step getStep() { return step; }
    public GrindResult getGrindResult() { return grindResult; }
    public boolean isServed() { return served; }
    public boolean isCoffeeUsed() { return coffeeUsed; }

    public boolean hasMilkInJug() { return milkInJug; }
    public boolean isJugInFrother() { return jugInFrother; }
    public MilkState getMilkState() { return milkState; }
    public MilkType getCurrentMilkType() { return currentMilkType; }

    // Stan kubka (dla punktacji)
    public GrindResult getCupGrindResult() { return cupGrindResult; }
    public MilkState getCupMilkState() { return cupMilkState; }
    public MilkType getCupMilkType() { return cupMilkType; }

    // Zasoby
    public int getCoffeeBeansLevel() { return coffeeBeansLevel; }
    public int getMilkLevel(MilkType type) {
        return switch (type) {
            case COW -> cowMilkLevel;
            case LACTOSE_FREE -> lactoseMilkLevel;
            case SOY -> soyMilkLevel;
        };
    }

    // Paski postępu (0.0 - 1.0) dla UI
    public float getGrindProgress() { return (float) currentGrindTime / TARGET_GRIND_TIME; }
    public float getBrewProgress() {
        if (step != Step.BREWING) return 0;
        float p = (float)(System.currentTimeMillis() - brewStartTime) / BREW_TIME;
        return Math.min(p, 1.0f);
    }
    public float getFrothProgress() { return (float) currentFrothTime / TARGET_FROTH_TIME; }

    // Gettery czasów (dla kolorowania pasków)
    public long getCurrentGrindTime() { return currentGrindTime; }
    public long getCurrentFrothTime() { return currentFrothTime; }

    public void update() {
        if (step == Step.BREWING) {
            if (System.currentTimeMillis() - brewStartTime >= BREW_TIME) {
                finishBrewing();
            }
        }
    }

    // ===================== MŁYNEK (MIELENIE CZASOWE) =====================

    public boolean canStartGrinding() {
        return step == Step.PORTAFILTER_IN_GRINDER
                && !coffeeInPortafilter
                && coffeeBeansLevel >= GRIND_COST;
    }

    public void processGrinding() {
        if (step == Step.PORTAFILTER_IN_GRINDER) {
            if (currentGrindTime == 0) {
                if (coffeeBeansLevel >= GRIND_COST) {
                    coffeeBeansLevel -= GRIND_COST;
                } else {
                    return;
                }
            }
            currentGrindTime += 16;
        }
    }

    public void finishGrinding() {
        if (step == Step.PORTAFILTER_IN_GRINDER && currentGrindTime > 0) {
            coffeeInPortafilter = true;

            // Ocena jakości
            if (currentGrindTime < TARGET_GRIND_TIME - TOLERANCE) {
                grindResult = GrindResult.UNDER; // Za krótko
            } else if (currentGrindTime <= TARGET_GRIND_TIME + TOLERANCE) {
                grindResult = GrindResult.PERFECT; // Idealnie
            } else {
                grindResult = GrindResult.OVER; // Przemielona
            }

            step = Step.COFFEE_GROUND;
        }
    }

    public void putPortafilterInGrinder() {
        if (!coffeeInPortafilter) {
            step = Step.PORTAFILTER_IN_GRINDER;
            currentGrindTime = 0; // Reset paska przy włożeniu
            grindResult = null;
        }
    }

    public void refillGrinder() {
        coffeeBeansLevel = GRINDER_CAPACITY;
    }

    public boolean canTakePortafilterFromGrinder() {
        return step == Step.COFFEE_GROUND || step == Step.PORTAFILTER_IN_GRINDER;
    }

    public void takePortafilterFromGrinder() {
        if (canTakePortafilterFromGrinder()) {
            step = Step.PORTAFILTER_READY;
        }
    }

    // ===================== UBIJANIE =====================
    public boolean canStartTamping() {
        return coffeeInPortafilter && !coffeeTamped && !coffeeUsed && step == Step.PORTAFILTER_READY;
    }
    public void startTamping() {}
    public void finishTamping() {
        if (coffeeInPortafilter && !coffeeTamped) {
            coffeeTamped = true;
            step = Step.COFFEE_TAMPED;
        }
    }

    // ===================== EKSPRES - KOLBA =====================
    public boolean canPutPortafilterInMachine() {
        return coffeeInPortafilter && coffeeTamped && !coffeeUsed && !portafilterInMachine;
    }
    public void putPortafilterInMachine() {
        if (canPutPortafilterInMachine()) {
            portafilterInMachine = true;
            if (cupInMachine) step = Step.CUP_IN_MACHINE; else step = Step.PORTAFILTER_IN_MACHINE;
        }
    }
    public boolean canTakePortafilterFromMachine() { return portafilterInMachine && step != Step.BREWING; }
    public void takePortafilterFromMachine() { if (canTakePortafilterFromMachine()) portafilterInMachine = false; }

    // ===================== EKSPRES - KUBEK =====================
    public boolean canPutCupInMachine() { return !cupInMachine; }
    public void putCupInMachine() { if (canPutCupInMachine()) { cupInMachine = true; step = Step.CUP_IN_MACHINE; } }
    public boolean canTakeCup() { return cupInMachine && step != Step.BREWING; }
    public void takeCup() { if (canTakeCup()) { cupInMachine = false; if (getCurrentDrink() != BrewType.EMPTY) step = Step.CUP_TAKEN; } }

    // ===================== PARZENIE (CZASOWE) =====================
    public boolean canBrew() {
        return cupInMachine && step != Step.BREWING;
    }

    public void brew(int quantity) {
        if (!canBrew()) return;
        this.pendingShots = quantity;
        this.step = Step.BREWING;
        this.brewStartTime = System.currentTimeMillis(); // Start timera
    }

    public void finishBrewing() {
        if (step == Step.BREWING) {
            if (portafilterInMachine && coffeeInPortafilter && coffeeTamped) {
                espressoShots += pendingShots;
                cupGrindResult = grindResult; // Przekazanie jakości mielenia do kubka
                coffeeUsed = true; // Zamiana kawy w fusy
            } else {
                hasHotWater = true;
            }
            step = Step.DRINK_READY;
        }
    }

    // ===================== MLEKO (ZASOBY + SPIENIANIE) =====================
    public boolean canPourMilkIntoJug(MilkType type) {
        if (milkInJug) return false;
        return getMilkLevel(type) >= POUR_AMOUNT;
    }

    public void pourMilkIntoJug(MilkType type) {
        if (canPourMilkIntoJug(type)) {
            switch (type) {
                case COW -> cowMilkLevel -= POUR_AMOUNT;
                case LACTOSE_FREE -> lactoseMilkLevel -= POUR_AMOUNT;
                case SOY -> soyMilkLevel -= POUR_AMOUNT;
            }
            milkInJug = true;
            currentMilkType = type;
            currentFrothTime = 0;
            milkState = MilkState.COLD;
            lastFrothInteraction = 0;
        }
    }

    public void throwEmptyCarton(MilkType type) {
        // Reset kartonu (Kupno nowego)
        switch (type) {
            case COW -> cowMilkLevel = CARTON_CAPACITY;
            case LACTOSE_FREE -> lactoseMilkLevel = CARTON_CAPACITY;
            case SOY -> soyMilkLevel = CARTON_CAPACITY;
        }
    }

    public void putJugInFrother() { if (milkInJug) jugInFrother = true; }
    public void takeJugFromFrother() { jugInFrother = false; }

    public void processFrothing() {
        if (milkInJug && jugInFrother && milkState != MilkState.BURNT) {
            currentFrothTime += 16;
            lastFrothInteraction = System.currentTimeMillis();

            // 0 - 2.5s -> Ciepłe
            if (currentFrothTime < 2500) {
                milkState = MilkState.WARM;
            }
            // 2.5s - 3.5s -> IDEALNE (Latte)
            else if (currentFrothTime <= 3500) {
                milkState = MilkState.PERFECT;
            }
            // 3.5s - 4.5s -> MOCNA PIANKA (Cappuccino)
            else if (currentFrothTime <= 4500) {
                milkState = MilkState.FOAMY;
            }
            // > 4.5s -> SPALONE
            else {
                milkState = MilkState.BURNT;
            }
        }
    }

    public boolean canPourMilkIntoCup() {
        return (milkState == MilkState.PERFECT || milkState == MilkState.FOAMY)
                && espressoShots > 0
                && step == Step.CUP_TAKEN;
    }

    public void pourMilkIntoCup() {
        if (canPourMilkIntoCup()) {
            hasMilk = true;
            cupMilkState = milkState;
            cupMilkType = currentMilkType;

            milkInJug = false;
            currentMilkType = null;
            currentFrothTime = 0;
            milkState = MilkState.COLD;
            lastFrothInteraction = 0;
        }
    }

    // ===================== IDENTYFIKACJA NAPOJU =====================
    public BrewType getCurrentDrink() {
        if (espressoShots == 0 && !hasHotWater && !hasMilk) return BrewType.EMPTY;

        if (hasMilk) {
            if (espressoShots > 0) {
                if (cupMilkState == MilkState.FOAMY) return BrewType.CAPPUCCINO;
                return BrewType.LATTE;
            }
            return BrewType.DIRTY_WATER;
        }

        if (hasHotWater) { if (espressoShots > 0) return BrewType.AMERICANO; return BrewType.HOT_WATER; }
        if (espressoShots == 1) return BrewType.ESPRESSO;
        if (espressoShots >= 2) return BrewType.DOUBLE_ESPRESSO;
        return BrewType.EMPTY;
    }

    // ===================== RESETOWANIE / KOSZ =====================
    public void prepareNewCup() {
        espressoShots = 0; hasHotWater = false; hasMilk = false;
        cupGrindResult = null; cupMilkState = null; cupMilkType = null;
        served = false;

        if (portafilterInMachine) { if (cupInMachine) step = Step.CUP_IN_MACHINE; else step = Step.PORTAFILTER_IN_MACHINE; }
        else { step = Step.TAKE_PORTAFILTER; }
    }

    public boolean canServe() { return step == Step.CUP_TAKEN && getCurrentDrink() != BrewType.EMPTY; }
    public void serveDrink() { if (canServe()) { served = true; step = Step.SERVED; } }

    public void emptyPortafilter() {
        coffeeInPortafilter = false; coffeeTamped = false; coffeeUsed = false; grindResult = null; currentGrindTime = 0;
        if (step == Step.COFFEE_GROUND || step == Step.PORTAFILTER_READY || step == Step.COFFEE_TAMPED) step = Step.TAKE_PORTAFILTER;
    }

    public void emptyCup() {
        espressoShots = 0; hasHotWater = false; hasMilk = false;
        cupGrindResult = null; cupMilkState = null; cupMilkType = null;
        served = false;
        if (portafilterInMachine) {
            if (cupInMachine) step = Step.CUP_IN_MACHINE;
            else step = Step.PORTAFILTER_IN_MACHINE;
        } else {
            step = Step.TAKE_PORTAFILTER;
        }
    }
    public boolean isFrothingActive() {
        return System.currentTimeMillis() - lastFrothInteraction < 200;
    }
    public boolean canGrind() {
        return canStartGrinding();
    }
    public void emptyJug() {
        milkInJug = false; currentMilkType = null;
        currentFrothTime = 0; milkState = MilkState.COLD; lastFrothInteraction = 0;
        jugInFrother = false;
    }
    public void loadState(int beans, int mCow, int mLactose, int mSoy) {
        this.coffeeBeansLevel = beans;
        this.cowMilkLevel = mCow;
        this.lactoseMilkLevel = mLactose;
        this.soyMilkLevel = mSoy;
    }
}
