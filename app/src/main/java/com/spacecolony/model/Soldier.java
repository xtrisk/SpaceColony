package com.spacecolony.model;

/**
 * Soldier crew member specialization.
 * Special: Assault Strike – deals 1.5× skill damage (no defense reduction from self).
 */
public class Soldier extends CrewMember {

    /**
     * Creates a Soldier with the fixed base stats used by this role.
     */
    public Soldier(String name) {
        super(name, 9, 0, 16);
    }

    /** Returns the role name shown in the UI and mission logs. */
    @Override
    public String getSpecialization() {
        return "Soldier";
    }

    /** Returns the theme color used to represent Soldiers in the interface. */
    @Override
    public String getColorHex() {
        return "#F44336"; // Red
    }

    /** Describes the Soldier special ability in short form for the UI. */
    @Override
    public String getSpecialDescription() {
        return "Assault Strike: 1.5× damage attack";
    }

    /**
     * Assault Strike – deals 1.5× effective skill as damage to the threat,
     * plus the usual 0–2 random bonus.
     */
    @Override
    public String useSpecial(CrewMember ally, Threat threat) {
        if (threat == null || threat.isDefeated()) {
            return getName() + " prepares to strike, but there is no target!";
        }
        int rawDamage = (int) (getEffectiveSkill() * 1.5) + (int) (Math.random() * 3);
        int netDamage = threat.defend(rawDamage);
        return getName() + " launches an Assault Strike!\n"
                + "  Heavy damage dealt: " + rawDamage + " - " + threat.getResilience()
                + " = " + netDamage
                + "\n  " + threat.getName() + " energy: "
                + threat.getEnergy() + "/" + threat.getMaxEnergy();
    }
}
