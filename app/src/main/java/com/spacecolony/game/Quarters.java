package com.spacecolony.game;

import com.spacecolony.model.CrewMember;
import com.spacecolony.model.Engineer;
import com.spacecolony.model.Location;
import com.spacecolony.model.Medic;
import com.spacecolony.model.Pilot;
import com.spacecolony.model.Scientist;
import com.spacecolony.model.Soldier;
import com.spacecolony.model.Storage;

/**
 * Manages the Quarters location.
 * Handles crew member recruitment and energy restoration.
 */
public class Quarters {

    private static Quarters instance;
    private final Storage storage;

    /**
     * Connects Quarters to the shared storage so recruited crew can be saved
     * and later found by the other screens.
     */
    private Quarters() {
        storage = Storage.getInstance();
    }

    /**
     * Returns the single shared Quarters manager used by the app.
     */
    public static Quarters getInstance() {
        if (instance == null) {
            instance = new Quarters();
        }
        return instance;
    }

    // ─────────────────────────── RECRUITMENT ──────────────────────────────────

    /**
     * Creates a new crew member of the given specialization, places them in
     * Quarters, and adds them to Storage.
     *
     * @param name           Name for the new crew member
     * @param specialization One of: Pilot, Engineer, Medic, Scientist, Soldier
     * @return The newly created CrewMember, or null if type is unknown
     */
    public CrewMember createCrewMember(String name, String specialization) {
        CrewMember cm;
        switch (specialization) {
            case "Pilot":
                cm = new Pilot(name);
                break;
            case "Engineer":
                cm = new Engineer(name);
                break;
            case "Medic":
                cm = new Medic(name);
                break;
            case "Scientist":
                cm = new Scientist(name);
                break;
            case "Soldier":
                cm = new Soldier(name);
                break;
            default:
                return null;
        }
        cm.setLocation(Location.QUARTERS);
        storage.addCrewMember(cm);
        return cm;
    }

    /**
     * Restores a crew member's energy to full when they return to Quarters.
     * Experience points are retained.
     *
     * @param cm The crew member returning to Quarters
     */
    public void restoreEnergy(CrewMember cm) {
        cm.restoreEnergy();
        cm.setLocation(Location.QUARTERS);
    }

    /**
     * Moves a crew member to Quarters (restoring energy).
     *
     * @param cm The crew member to move
     */
    public void moveToQuarters(CrewMember cm) {
        restoreEnergy(cm);
    }
}
