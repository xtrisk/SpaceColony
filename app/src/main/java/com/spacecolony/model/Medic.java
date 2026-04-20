package com.spacecolony.model;

/**
 * Medic crew member specialization.
 * Special: Field Medicine – heals ally for 6 energy (self for 3 if no ally).
 */
public class Medic extends CrewMember {

    /**
     * Creates a Medic with the fixed base stats used by this role.
     */
    public Medic(String name) {
        super(name, 7, 2, 18);
    }

    /** Returns the role name shown in the UI and mission logs. */
    @Override
    public String getSpecialization() {
        return "Medic";
    }

    /** Returns the theme color used to represent Medics in the interface. */
    @Override
    public String getColorHex() {
        return "#4CAF50"; // Green
    }

    /** Describes the Medic special ability in short form for the UI. */
    @Override
    public String getSpecialDescription() {
        return "Field Medicine: Heal ally for 6 energy";
    }

    /**
     * Field Medicine – heals the ally for 6 energy.
     * If no alive ally, heals self for 3 energy instead.
     */
    @Override
    public String useSpecial(CrewMember ally, Threat threat) {
        if (ally != null && !ally.isDefeated()) {
            ally.healEnergy(6);
            return getName() + " uses Field Medicine on " + ally.getName() + "!\n"
                    + "  Restored 6 energy. ("
                    + ally.getEnergy() + "/" + ally.getMaxEnergy() + ")";
        } else {
            healEnergy(3);
            return getName() + " uses Field Medicine on self (no ally available)!\n"
                    + "  Restored 3 energy. (" + getEnergy() + "/" + getMaxEnergy() + ")";
        }
    }
}
