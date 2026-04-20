package com.spacecolony.model;

/**
 * Scientist crew member specialization.
 * Special: Analyze Weakness – permanently reduces threat resilience by 2.
 */
public class Scientist extends CrewMember {

    /**
     * Creates a Scientist with the fixed base stats used by this role.
     */
    public Scientist(String name) {
        super(name, 8, 1, 17);
    }

    /** Returns the role name shown in the UI and mission logs. */
    @Override
    public String getSpecialization() {
        return "Scientist";
    }

    /** Returns the theme color used to represent Scientists in the interface. */
    @Override
    public String getColorHex() {
        return "#9C27B0"; // Purple
    }

    /** Describes the Scientist special ability in short form for the UI. */
    @Override
    public String getSpecialDescription() {
        return "Analyze Weakness: Reduce threat resilience by 2";
    }

    /**
     * Analyze Weakness – reduces the threat's resilience by 2 permanently.
     * Demonstrates polymorphism: Scientist interacts with Threat differently
     * than other crew members.
     */
    @Override
    public String useSpecial(CrewMember ally, Threat threat) {
        if (threat != null && !threat.isDefeated()) {
            int before = threat.getResilience();
            threat.reduceResilience(2);
            return getName() + " uses Analyze Weakness!\n"
                    + "  Threat resilience reduced: " + before + " → " + threat.getResilience();
        }
        return getName() + " uses Analyze Weakness! (No valid target)";
    }
}
