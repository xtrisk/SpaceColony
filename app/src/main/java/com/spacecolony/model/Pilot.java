package com.spacecolony.model;

/**
 * Pilot crew member specialization.
 * Special: Evasion – doubles own resilience for the next incoming attack.
 */
public class Pilot extends CrewMember {

    /**
     * Creates a Pilot with the fixed base stats used by this role.
     */
    public Pilot(String name) {
        super(name, 5, 4, 20);
    }

    /** Returns the role name shown in the UI and mission logs. */
    @Override
    public String getSpecialization() {
        return "Pilot";
    }

    /** Returns the theme color used to represent Pilots in the interface. */
    @Override
    public String getColorHex() {
        return "#2196F3"; // Blue
    }

    /** Describes the Pilot special ability in short form for the UI. */
    @Override
    public String getSpecialDescription() {
        return "Evasion: Doubles resilience for the next hit";
    }

    /**
     * Evasion – activates a defensive stance that doubles resilience for the
     * next incoming attack.
     */
    @Override
    public String useSpecial(CrewMember ally, Threat threat) {
        setDefending(true);
        return getName() + " activates Evasion Maneuvers!\n"
                + "  Resilience doubled for the next incoming attack ("
                + getResilience() + " → " + (getResilience() * 2) + ").";
    }
}
