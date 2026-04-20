package com.spacecolony.game;

import com.spacecolony.model.CrewMember;
import com.spacecolony.model.Location;
import com.spacecolony.model.Storage;
import com.spacecolony.model.Threat;

/**
 * Manages Mission Control and the cooperative turn-based mission system.
 * Uses a mission counter for difficulty scaling.
 */
public class MissionControl {

    private static MissionControl instance;

    /** Static counter – scales threat difficulty with each completed mission. */
    private static int missionCounter = 0;

    // Threat name pool for variety
    private static final String[] THREAT_NAMES = {
            "Asteroid Storm",
            "Solar Flare",
            "Hull Breach",
            "Alien Boarding Party",
            "Reactor Meltdown",
            "Fuel Leak Explosion",
            "Oxygen System Failure",
            "Rogue AI Drone",
            "Meteor Shower",
            "Microorganism Outbreak"
    };

    private MissionControl() {}

    /**
     * Returns the one shared MissionControl object used by the whole app so
     * mission state and difficulty scaling stay consistent.
     */
    public static MissionControl getInstance() {
        if (instance == null) {
            instance = new MissionControl();
        }
        return instance;
    }

    // ────────────────────────── THREAT GENERATION ─────────────────────────────

    /**
     * Generates a scaled threat based on the current mission counter.
     * Formula: threatSkill = 4 + missionCounter
     *
     * @return A new Threat instance
     */
    public Threat generateThreat() {
        String name = THREAT_NAMES[missionCounter % THREAT_NAMES.length];
        int skill     = 4 + missionCounter;
        int resilience = Math.max(0, missionCounter / 2);
        int maxEnergy = 20 + (missionCounter * 3);
        return new Threat(name, skill, resilience, maxEnergy);
    }

    /**
     * Moves a crew member to Mission Control.
     *
     * @param cm The crew member to move
     */
    public void moveToMissionControl(CrewMember cm) {
        cm.setLocation(Location.MISSION_CONTROL);
    }

    // ──────────────────────── ACTION EXECUTION ────────────────────────────────

    /**
     * Executes a crew member's ATTACK action against a threat.
     *
     * @param actor  The attacking crew member
     * @param threat The target threat
     * @return Log string
     */
    public String performAttack(CrewMember actor, Threat threat) {
        int rawDamage = actor.act();
        int netDamage = threat.defend(rawDamage);
        return actor + " acts against " + threat.getName()
                + "\n  Damage dealt: " + rawDamage + " - " + threat.getResilience()
                + " = " + netDamage
                + "\n  " + threat.getName() + " energy: "
                + threat.getEnergy() + "/" + threat.getMaxEnergy();
    }

    /**
     * Executes a crew member's DEFEND action (prepare for incoming attack).
     *
     * @param actor The defending crew member
     * @return Log string
     */
    public String performDefend(CrewMember actor) {
        actor.setDefending(true);
        return actor.getName() + " takes a defensive stance!\n"
                + "  Resilience doubled for the next incoming attack.";
    }

    /**
     * Executes a crew member's SPECIAL action.
     *
     * @param actor  The crew member using the special
     * @param ally   The other crew member (may be null or defeated)
     * @param threat The current threat
     * @return Log string
     */
    public String performSpecial(CrewMember actor, CrewMember ally, Threat threat) {
        return actor.useSpecial(ally, threat);
    }

    /**
     * Executes the threat's retaliation against a crew member.
     *
     * @param threat The threat that retaliates
     * @param target The targeted crew member
     * @return Log string
     */
    public String threatRetaliate(Threat threat, CrewMember target) {
        return threat.attack(target);
    }

    // ───────────────────────── MISSION RESOLUTION ─────────────────────────────

    /**
     * Called when a mission is successfully completed.
     * Awards XP to survivors and increments mission counter.
     *
     * @param cmA First crew member (null if defeated)
     * @param cmB Second crew member (null if defeated)
     * @return Log string
     */
    public String completeMission(CrewMember cmA, CrewMember cmB) {
        missionCounter++;
        StringBuilder log = new StringBuilder();
        log.append("=== MISSION COMPLETE ===\n");

        if (cmA != null && !cmA.isDefeated()) {
            cmA.addExperience(1);
            cmA.incrementMissionsCompleted();
            cmA.incrementMissionsWon();
            cmA.setLocation(Location.MISSION_CONTROL);
            log.append(cmA.getName()).append(" gains 1 experience point. (exp: ")
               .append(cmA.getExperience()).append(")\n");
        }
        if (cmB != null && !cmB.isDefeated()) {
            cmB.addExperience(1);
            cmB.incrementMissionsCompleted();
            cmB.incrementMissionsWon();
            cmB.setLocation(Location.MISSION_CONTROL);
            log.append(cmB.getName()).append(" gains 1 experience point. (exp: ")
               .append(cmB.getExperience()).append(")\n");
        }
        return log.toString();
    }

    /**
     * Called when a mission fails (both crew defeated).
     * Bonus – No Death: sends defeated crew to Medbay instead of removing them.
     *
     * @param cmA First crew member
     * @param cmB Second crew member
     * @return Log string
     */
    public String failMission(CrewMember cmA, CrewMember cmB) {
        missionCounter++;
        StringBuilder log = new StringBuilder();
        log.append("=== MISSION FAILED ===\n");
        log.append("Mission failed. All crew members lost.\n");
        log.append("Survivors have been transported to the Medbay.\n");

        // Bonus: No Death – send to Medbay instead of removing
        if (cmA != null) {
            sendToMedbay(cmA);
            cmA.incrementMissionsCompleted();
            cmA.incrementMissionsLost();
            log.append(cmA.getName()).append(" has been sent to Medbay.\n");
        }
        if (cmB != null) {
            sendToMedbay(cmB);
            cmB.incrementMissionsCompleted();
            cmB.incrementMissionsLost();
            log.append(cmB.getName()).append(" has been sent to Medbay.\n");
        }
        return log.toString();
    }

    /**
     * Bonus – No Death: Sends a defeated crew member to Medbay.
     * Stats reset to initial values as a penalty.
     *
     * @param cm The defeated crew member
     */
    private void sendToMedbay(CrewMember cm) {
        cm.setLocation(Location.MEDBAY);
        // Partial energy restore (penalty: starts at half max)
        cm.restoreEnergy();
    }

    /**
     * Exposes the number of finished missions so other screens can show global
     * progress and use it for difficulty scaling.
     */
    public static int getMissionCounter() {
        return missionCounter;
    }
}
