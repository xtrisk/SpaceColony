package com.spacecolony.model;

/**
 * Represents a system-generated threat that crew members fight cooperatively.
 * Created by MissionControl, scaled by mission count.
 */
public class Threat {

    private final String name;
    private int skill;
    private int resilience;
    private int energy;
    private final int maxEnergy;

    /**
     * @param name      Display name of the threat
     * @param skill     Attack power
     * @param resilience Damage reduction
     * @param maxEnergy Starting / max energy
     */
    public Threat(String name, int skill, int resilience, int maxEnergy) {
        this.name = name;
        this.skill = skill;
        this.resilience = resilience;
        this.maxEnergy = maxEnergy;
        this.energy = maxEnergy;
    }

    /**
     * Threat attacks a crew member.
     * Bonus: Randomness – adds 0–2 random bonus damage.
     *
     * @param target The crew member being attacked
     * @return Log string
     */
    public String attack(CrewMember target) {
        int randomBonus = (int) (Math.random() * 3);
        int rawDamage = skill + randomBonus;
        int netDamage = target.takeDamage(rawDamage);
        return name + " retaliates against " + target.getName()
                + "\n  Damage dealt: " + rawDamage + " - " + target.getResilience()
                + (target.isDefending() ? "(x2 defend)" : "")
                + " = " + netDamage
                + "\n  " + target.getName() + " energy: "
                + target.getEnergy() + "/" + target.getMaxEnergy();
    }

    /**
     * Crew member deals damage to the threat.
     *
     * @param rawDamage Damage from crew member's act()
     * @return Net damage dealt after resilience reduction
     */
    public int defend(int rawDamage) {
        int netDamage = Math.max(0, rawDamage - resilience);
        energy = Math.max(0, energy - netDamage);
        return netDamage;
    }

    /**
     * Reduces this threat's resilience (Scientist special ability).
     */
    public void reduceResilience(int amount) {
        resilience = Math.max(0, resilience - amount);
    }

    /** @return true if energy is 0 */
    public boolean isDefeated() {
        return energy <= 0;
    }

    // ─────────────────────────── GETTERS ──────────────────────────────────────

    /** Returns the threat name shown to the player. */
    public String getName()    { return name; }
    /** Returns the threat's current attack power. */
    public int getSkill()      { return skill; }
    /** Returns the threat's current damage reduction value. */
    public int getResilience() { return resilience; }
    /** Returns the threat's remaining energy. */
    public int getEnergy()     { return energy; }
    /** Returns the threat's starting maximum energy. */
    public int getMaxEnergy()  { return maxEnergy; }

    /**
     * Builds a short one-line summary of the threat for mission logs and
     * debugging output.
     */
    @Override
    public String toString() {
        return name + " (skill:" + skill + "; res:" + resilience
                + "; energy:" + energy + "/" + maxEnergy + ")";
    }
}
