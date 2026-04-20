package com.spacecolony.model;

/**
 * Represents the possible locations of a crew member within the Space Colony.
 */
public enum Location {
    QUARTERS("Quarters"),
    SIMULATOR("Simulator"),
    MISSION_CONTROL("Mission Control"),
    MEDBAY("Medbay");

    private final String displayName;

    /**
     * Stores the nice human-readable label that should appear in the UI.
     */
    Location(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the readable location name instead of the enum constant name.
     */
    public String getDisplayName() {
        return displayName;
    }
}
