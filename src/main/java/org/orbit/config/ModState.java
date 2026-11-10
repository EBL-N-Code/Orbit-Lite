package org.orbit.config;

public class ModState {
    private static boolean disabled = true;
    private static boolean underMaintenance = false;

    public static void disableMod() {
        disabled = true;
    }

    public static void enableMod() {
        disabled = false;
    }

    public static boolean isDisabled() {
        return disabled;
    }

    public static boolean isUnderMaintenance() {
        return underMaintenance;
    }

    public static void setUnderMaintenance(boolean underMaintenance) {
        ModState.underMaintenance = underMaintenance;
    }
}
