package com.spacecolony.model;

/**
 * Engineer crew member specialization.
 * Special: Repair Systems – restores 4 own energy.
 */
public class Engineer extends CrewMember {

    /**
     * Creates an Engineer with the fixed base stats used by this role.
     */
    public Engineer(String name) {
        super(name, 6, 3, 19);
    }

    /** Returns the role name shown in the UI and mission logs. */
    @Override
    public String getSpecialization() {
        return "Engineer";
    }

    /** Returns the theme color used to represent Engineers in the interface. */
    @Override
    public String getColorHex() {
        return "#FFC107"; // Amber
    }

    /** Describes the Engineer special ability in short form for the UI. */
    @Override
    public String getSpecialDescription() {
        return "Repair Systems: Restore 4 own energy";
    }

    /**
     * Repair Systems – restores 4 energy to self.
     */
    @Override
    public String useSpecial(CrewMember ally, Threat threat) {
        healEnergy(4);
        return getName() + " uses Repair Systems!\n"
                + "  Restored 4 energy. (" + getEnergy() + "/" + getMaxEnergy() + ")";
    }
}
