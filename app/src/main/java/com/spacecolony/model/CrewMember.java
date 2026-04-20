package com.spacecolony.model;

/**
 * Abstract base class for all crew members.
 * Demonstrates OOP principles: encapsulation, inheritance, and polymorphism.
 */
public abstract class CrewMember {

    // Static ID counter shared across all crew members
    private static int idCounter = 0;

    private final int id;
    private final String name;
    protected int baseSkill;
    protected int resilience;
    private int experience;
    private int energy;
    private final int maxEnergy;
    private Location location;

    // Tactical: temporary defend boost for one incoming attack
    private boolean defending;

    // Statistics tracking (Bonus: Statistics feature)
    private int missionsCompleted;
    private int missionsWon;
    private int trainingSessions;
    private int missionsLost;

    /**
     * Constructor for CrewMember.
     *
     * @param name      Name of the crew member
     * @param baseSkill Base skill value
     * @param resilience Resilience (damage reduction)
     * @param maxEnergy Maximum energy (HP)
     */
    public CrewMember(String name, int baseSkill, int resilience, int maxEnergy) {
        this.id = ++idCounter;
        this.name = name;
        this.baseSkill = baseSkill;
        this.resilience = resilience;
        this.experience = 0;
        this.maxEnergy = maxEnergy;
        this.energy = maxEnergy;
        this.location = Location.QUARTERS;
        this.defending = false;
        this.missionsCompleted = 0;
        this.missionsWon = 0;
        this.trainingSessions = 0;
        this.missionsLost = 0;
    }

    // ───────────────────────────── COMBAT METHODS ─────────────────────────────

    /**
     * Performs a standard attack.
     * Bonus: Randomness – adds 0–2 random bonus damage.
     *
     * @return Total damage dealt before resilience reduction
     */
    public int act() {
        int randomBonus = (int) (Math.random() * 3); // 0, 1, or 2
        return getEffectiveSkill() + randomBonus;
    }

    /**
     * Takes incoming damage, applying resilience reduction.
     * If defending, resilience is doubled for this hit.
     *
     * @param incomingDamage Raw damage from attacker
     * @return Net damage actually taken
     */
    public int takeDamage(int incomingDamage) {
        int effectiveResilience = defending ? resilience * 2 : resilience;
        int netDamage = Math.max(0, incomingDamage - effectiveResilience);
        energy = Math.max(0, energy - netDamage);
        defending = false; // reset flag after taking hit
        return netDamage;
    }

    /**
     * Sets the defending flag (doubles resilience for next incoming attack).
     */
    public void setDefending(boolean defending) {
        this.defending = defending;
    }

    /**
     * Abstract special ability – each subclass defines its unique effect.
     *
     * @param ally   The other crew member on the mission (may be null)
     * @param threat The current threat
     * @return Log string describing what happened
     */
    public abstract String useSpecial(CrewMember ally, Threat threat);

    // ───────────────────────────── STAT METHODS ───────────────────────────────

    /**
     * Effective skill = baseSkill + experience (XP increases skill power).
     */
    public int getEffectiveSkill() {
        return baseSkill + experience;
    }

    /**
     * Adds earned experience so future attacks and specials become stronger.
     */
    public void addExperience(int xp) {
        experience += xp;
    }

    /** Fully restores energy (used when returning to Quarters). */
    public void restoreEnergy() {
        energy = maxEnergy;
    }

    /** Heals a fixed amount of energy, capped at maxEnergy. */
    public void healEnergy(int amount) {
        energy = Math.min(maxEnergy, energy + amount);
    }

    /** @return true if energy has reached 0 */
    public boolean isDefeated() {
        return energy <= 0;
    }

    // ───────────────────────── ABSTRACT IDENTITY ──────────────────────────────

    /** @return The specialization name (e.g., "Pilot") */
    public abstract String getSpecialization();

    /** @return Hex color string for the specialization (e.g., "#2196F3") */
    public abstract String getColorHex();

    /** @return Short description of the special ability */
    public abstract String getSpecialDescription();

    // ─────────────────────────── GETTERS / SETTERS ────────────────────────────

    /** Returns the unique ID used to track this crew member in storage. */
    public int getId()           { return id; }
    /** Returns the name the player gave to this crew member. */
    public String getName()      { return name; }
    /** Returns the original skill before any training bonuses are added. */
    public int getBaseSkill()    { return baseSkill; }
    /** Returns the flat damage reduction this crew member currently has. */
    public int getResilience()   { return resilience; }
    /** Returns how much experience this crew member has earned so far. */
    public int getExperience()   { return experience; }
    /** Returns the current remaining energy or health. */
    public int getEnergy()       { return energy; }
    /** Returns the maximum energy this crew member can hold. */
    public int getMaxEnergy()    { return maxEnergy; }
    /** Returns which room or station this crew member is currently in. */
    public Location getLocation(){ return location; }
    /** Tells whether this crew member has a defend boost ready for the next hit. */
    public boolean isDefending() { return defending; }

    /** Updates the crew member's current location in the colony. */
    public void setLocation(Location location) { this.location = location; }

    // Statistics
    /** Returns how many missions this crew member has participated in. */
    public int getMissionsCompleted() { return missionsCompleted; }
    /** Returns how many missions this crew member has won. */
    public int getMissionsWon()       { return missionsWon; }
    /** Returns how many training sessions this crew member has completed. */
    public int getTrainingSessions()  { return trainingSessions; }
    /** Returns how many missions this crew member has lost. */
    public int getMissionsLost()      { return missionsLost; }

    /** Adds one to the total mission count after a mission ends. */
    public void incrementMissionsCompleted() { missionsCompleted++; }
    /** Adds one win to the crew member's record. */
    public void incrementMissionsWon()       { missionsWon++; }
    /** Adds one completed training session to the stats. */
    public void incrementTrainingSessions()  { trainingSessions++; }
    /** Adds one loss to the crew member's mission record. */
    public void incrementMissionsLost()      { missionsLost++; }

    // ─────────────────────────────── toString ─────────────────────────────────

    /**
     * Builds a compact text summary of this crew member that is mainly used in
     * logs and debugging output.
     */
    @Override
    public String toString() {
        return getSpecialization() + "(" + name + ")"
                + " skill:" + getEffectiveSkill()
                + "; res:" + resilience
                + "; exp:" + experience
                + "; energy:" + energy + "/" + maxEnergy;
    }
}
