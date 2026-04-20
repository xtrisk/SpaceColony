package com.spacecolony.game;

import com.spacecolony.model.CrewMember;
import com.spacecolony.model.Location;

/**
 * Manages the Simulator location.
 * Crew members here can be trained to gain experience points.
 */
public class Simulator {

    private static Simulator instance;

    /**
     * Keeps construction private so every screen uses the same Simulator
     * manager instead of creating separate copies.
     */
    private Simulator() {}

    /**
     * Returns the single shared Simulator manager.
     */
    public static Simulator getInstance() {
        if (instance == null) {
            instance = new Simulator();
        }
        return instance;
    }

    // ───────────────────────────── TRAINING ───────────────────────────────────

    /**
     * Trains a crew member, awarding 1 experience point.
     * Experience increases effective skill (skill + XP).
     *
     * @param cm The crew member to train
     * @return Log string describing the training result
     */
    public String train(CrewMember cm) {
        cm.addExperience(1);
        cm.incrementTrainingSessions();
        return cm.getSpecialization() + "(" + cm.getName() + ") completed a training session!\n"
                + "  Experience: " + cm.getExperience()
                + " | Effective Skill: " + cm.getEffectiveSkill();
    }

    /**
     * Moves a crew member to the Simulator.
     *
     * @param cm The crew member to move
     */
    public void moveToSimulator(CrewMember cm) {
        cm.setLocation(Location.SIMULATOR);
    }
}
