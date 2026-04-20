package com.spacecolony.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton storage class for all crew members.
 * Uses HashMap<Integer, CrewMember> to store crew members keyed by their ID.
 */
public class Storage {

    private static Storage instance;

    // Primary data structure: HashMap keyed by crew member ID
    private final HashMap<Integer, CrewMember> crewMap;

    /**
     * Starts the in-memory storage map that will hold every recruited crew
     * member for the lifetime of the app.
     */
    private Storage() {
        crewMap = new HashMap<>();
    }

    /** Returns the single Storage instance. */
    public static Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    // ─────────────────────────── CRUD OPERATIONS ──────────────────────────────

    /**
     * Adds a crew member to storage.
     *
     * @param cm The crew member to add
     */
    public void addCrewMember(CrewMember cm) {
        crewMap.put(cm.getId(), cm);
    }

    /**
     * Retrieves a crew member by ID.
     *
     * @param id The crew member's ID
     * @return The crew member, or null if not found
     */
    public CrewMember getCrewMember(int id) {
        return crewMap.get(id);
    }

    /**
     * Removes a crew member from storage (permanent removal on death,
     * before Medbay bonus).
     *
     * @param id The crew member's ID
     */
    public void removeCrewMember(int id) {
        crewMap.remove(id);
    }

    /**
     * Returns all crew members as a List.
     *
     * @return ArrayList of all crew members
     */
    public List<CrewMember> listCrewMembers() {
        return new ArrayList<>(crewMap.values());
    }

    /**
     * Returns crew members currently at a specific location.
     *
     * @param location The target location
     * @return List of crew members at that location
     */
    public List<CrewMember> getCrewByLocation(Location location) {
        List<CrewMember> result = new ArrayList<>();
        for (CrewMember cm : crewMap.values()) {
            if (cm.getLocation() == location) {
                result.add(cm);
            }
        }
        return result;
    }

    /**
     * Returns a count summary for each location.
     *
     * @return Map from Location to count
     */
    public Map<Location, Integer> getLocationCounts() {
        Map<Location, Integer> counts = new HashMap<>();
        for (Location loc : Location.values()) {
            counts.put(loc, 0);
        }
        for (CrewMember cm : crewMap.values()) {
            Location loc = cm.getLocation();
            counts.put(loc, counts.get(loc) + 1);
        }
        return counts;
    }

    /** @return Total number of crew members in storage */
    public int getTotalCrew() {
        return crewMap.size();
    }

    /** Checks if storage contains a crew member with the given ID. */
    public boolean contains(int id) {
        return crewMap.containsKey(id);
    }
}
