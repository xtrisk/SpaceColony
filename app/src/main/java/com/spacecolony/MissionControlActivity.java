package com.spacecolony;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spacecolony.adapter.CrewMemberAdapter;
import com.spacecolony.game.MissionControl;
import com.spacecolony.model.CrewMember;
import com.spacecolony.model.Location;
import com.spacecolony.model.Storage;
import com.spacecolony.model.Threat;

import java.util.List;
import java.util.Set;

/**
 * Mission Control Screen.
 * Allows selecting two crew members, then runs a tactical turn-based mission.
 *
 * Bonus Features implemented here:
 *  - Tactical Combat: player chooses Attack / Defend / Special each turn (+2)
 *  - Mission Visualization: health bars update live, log scrolls (+2)
 *  - Randomness: see MissionControl.performAttack (+1)
 *  - No Death / Medbay: see MissionControl.failMission (+1)
 */
public class MissionControlActivity extends AppCompatActivity {

    // ─── Mission State Machine ───
    private enum MissionPhase {
        SELECTING,      // Choosing crew before mission
        TURN_A,         // Player action for crew member A
        TURN_B,         // Player action for crew member B
        VICTORY,        // Threat defeated
        DEFEAT          // Both crew defeated
    }

    private MissionPhase phase = MissionPhase.SELECTING;

    // ─── UI References ───
    // Selection panel
    private View panelSelection;
    private RecyclerView recyclerView;
    private CrewMemberAdapter adapter;
    private Button btnLaunch;
    private TextView tvSelectionHint;

    // Mission panel
    private View panelMission;
    private TextView tvTurnIndicator;
    private TextView tvMissionLog;
    private ScrollView scrollLog;

    // Crew A widgets
    private TextView tvCrewAName;
    private ProgressBar barCrewA;
    private TextView tvCrewAEnergy;

    // Crew B widgets
    private TextView tvCrewBName;
    private ProgressBar barCrewB;
    private TextView tvCrewBEnergy;

    // Threat widgets
    private TextView tvThreatName;
    private ProgressBar barThreat;
    private TextView tvThreatEnergy;

    // Action buttons
    private Button btnAttack;
    private Button btnDefend;
    private Button btnSpecial;
    private Button btnDone;

    // ─── State ───
    private CrewMember crewA;
    private CrewMember crewB;
    private Threat threat;
    private final StringBuilder missionLog = new StringBuilder();
    private int round = 1;
    private boolean crewAAlive = true;
    private boolean crewBAlive = true;

    private final MissionControl mc = MissionControl.getInstance();

    /**
     * Builds both Mission Control panels, grabs all HUD widgets, and wires the
     * buttons for launching and playing through a mission.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_control);

        // Selection panel
        panelSelection  = findViewById(R.id.panelSelection);
        recyclerView    = findViewById(R.id.recyclerViewCrew);
        btnLaunch       = findViewById(R.id.btnLaunch);
        tvSelectionHint = findViewById(R.id.tvSelectionHint);

        // Mission panel
        panelMission    = findViewById(R.id.panelMission);
        tvTurnIndicator = findViewById(R.id.tvTurnIndicator);
        tvMissionLog    = findViewById(R.id.tvMissionLog);
        scrollLog       = findViewById(R.id.scrollLog);

        tvCrewAName   = findViewById(R.id.tvCrewAName);
        barCrewA      = findViewById(R.id.barCrewA);
        tvCrewAEnergy = findViewById(R.id.tvCrewAEnergy);

        tvCrewBName   = findViewById(R.id.tvCrewBName);
        barCrewB      = findViewById(R.id.barCrewB);
        tvCrewBEnergy = findViewById(R.id.tvCrewBEnergy);

        tvThreatName   = findViewById(R.id.tvThreatName);
        barThreat      = findViewById(R.id.barThreat);
        tvThreatEnergy = findViewById(R.id.tvThreatEnergy);

        btnAttack  = findViewById(R.id.btnAttack);
        btnDefend  = findViewById(R.id.btnDefend);
        btnSpecial = findViewById(R.id.btnSpecial);
        btnDone    = findViewById(R.id.btnDone);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnLaunch.setOnClickListener(v -> launchMission());
        btnAttack.setOnClickListener(v -> handleAction("ATTACK"));
        btnDefend.setOnClickListener(v -> handleAction("DEFEND"));
        btnSpecial.setOnClickListener(v -> handleAction("SPECIAL"));
        btnDone.setOnClickListener(v -> {
            panelMission.setVisibility(View.GONE);
            panelSelection.setVisibility(View.VISIBLE);
            phase = MissionPhase.SELECTING;
            missionLog.setLength(0);
            round = 1;
            loadCrewList();
        });

        showSelectionPanel();
    }

    /**
     * Reloads the crew selection list when the user returns here, but only if
     * a mission is not already in progress.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (phase == MissionPhase.SELECTING) loadCrewList();
    }

    // ─────────────────────────── SELECTION ────────────────────────────────────

    /**
     * Switches back to the pre-mission selection screen and refreshes the crew
     * list that can be sent on the next mission.
     */
    private void showSelectionPanel() {
        panelSelection.setVisibility(View.VISIBLE);
        panelMission.setVisibility(View.GONE);
        loadCrewList();
    }

    /**
     * Loads all crew members currently waiting in Mission Control and updates
     * the hint text so the player knows whether a mission can start.
     */
    private void loadCrewList() {
        List<CrewMember> crew = Storage.getInstance().getCrewByLocation(Location.MISSION_CONTROL);
        tvSelectionHint.setText("Crew in Mission Control: " + crew.size() + "\nSelect exactly 2 for a mission.");
        if (crew.isEmpty()) {
            tvSelectionHint.setText("No crew in Mission Control.\nMove crew here from Quarters.");
            btnLaunch.setEnabled(false);
        } else {
            btnLaunch.setEnabled(true);
            adapter = new CrewMemberAdapter(crew, true, 2);
            recyclerView.setAdapter(adapter);
        }
    }

    /**
     * Starts a new mission after exactly two crew members are selected, then
     * builds the threat, resets mission state, and opens the combat view.
     */
    private void launchMission() {
        if (adapter == null) {
            Toast.makeText(this, "No crew available.", Toast.LENGTH_SHORT).show();
            return;
        }
        Set<Integer> ids = adapter.getSelectedIds();
        if (ids.size() != 2) {
            Toast.makeText(this, "Select exactly 2 crew members.", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer[] idArray = ids.toArray(new Integer[0]);
        crewA = Storage.getInstance().getCrewMember(idArray[0]);
        crewB = Storage.getInstance().getCrewMember(idArray[1]);

        if (crewA == null || crewB == null) {
            Toast.makeText(this, "Error: crew member not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Restore energy for the mission (they come from MC, not Quarters, so
        // energy was last set when they moved here — don't auto-restore, keep current)
        threat = mc.generateThreat();
        crewAAlive = true;
        crewBAlive = true;
        round = 1;
        missionLog.setLength(0);

        // Setup HUD
        setupMissionHUD();
        appendLog("=== MISSION: " + threat.getName().toUpperCase() + " ===\n");
        appendLog("Threat: " + threat + "\n");
        appendLog("Crew A: " + crewA + "\n");
        appendLog("Crew B: " + crewB + "\n");
        appendLog("-------------------------\n");

        // Start mission panel
        panelSelection.setVisibility(View.GONE);
        panelMission.setVisibility(View.VISIBLE);

        startRound();
    }

    // ─────────────────────────── MISSION HUD SETUP ───────────────────────────

    /**
     * Fills the combat HUD with crew names, colors, health bars, and the new
     * threat information before the first round begins.
     */
    private void setupMissionHUD() {
        // Crew A
        tvCrewAName.setText(crewA.getSpecialization() + "\n" + crewA.getName());
        try { tvCrewAName.setTextColor(Color.parseColor(crewA.getColorHex())); } catch (Exception ignored) {}
        barCrewA.setMax(crewA.getMaxEnergy());
        updateCrewAHUD();

        // Crew B
        tvCrewBName.setText(crewB.getSpecialization() + "\n" + crewB.getName());
        try { tvCrewBName.setTextColor(Color.parseColor(crewB.getColorHex())); } catch (Exception ignored) {}
        barCrewB.setMax(crewB.getMaxEnergy());
        updateCrewBHUD();

        // Threat
        tvThreatName.setText(threat.getName());
        barThreat.setMax(threat.getMaxEnergy());
        updateThreatHUD();

        btnDone.setVisibility(View.GONE);
    }

    /**
     * Refreshes crew member A's health bar and text after taking damage,
     * healing, or starting a new mission.
     */
    private void updateCrewAHUD() {
        barCrewA.setProgress(crewA.getEnergy());
        tvCrewAEnergy.setText(crewA.getEnergy() + "/" + crewA.getMaxEnergy());
    }

    /**
     * Refreshes crew member B's health bar and text after taking damage,
     * healing, or starting a new mission.
     */
    private void updateCrewBHUD() {
        barCrewB.setProgress(crewB.getEnergy());
        tvCrewBEnergy.setText(crewB.getEnergy() + "/" + crewB.getMaxEnergy());
    }

    /**
     * Updates the threat health display so the player can see how close the
     * mission is to being won.
     */
    private void updateThreatHUD() {
        barThreat.setProgress(threat.getEnergy());
        tvThreatEnergy.setText(threat.getEnergy() + "/" + threat.getMaxEnergy());
    }

    // ─────────────────────────── ROUND MANAGEMENT ─────────────────────────────

    /**
     * Begins a fresh round, writes a divider into the mission log, and decides
     * which surviving crew member gets the next turn.
     */
    private void startRound() {
        appendLog("\n--- Round " + round + " ---\n");

        // Find who acts first (A if alive, else B)
        if (crewAAlive) {
            setPhase(MissionPhase.TURN_A);
        } else if (crewBAlive) {
            setPhase(MissionPhase.TURN_B);
        }
    }

    /**
     * Changes the mission state and updates the top indicator plus action
     * buttons so the player always knows whose turn it is.
     */
    private void setPhase(MissionPhase newPhase) {
        phase = newPhase;
        switch (newPhase) {
            case TURN_A:
                tvTurnIndicator.setText(crewA.getName() + "'s Turn");
                tvTurnIndicator.setTextColor(Color.parseColor(crewA.getColorHex()));
                btnSpecial.setText("Special\n(" + crewA.getSpecialization() + ")");
                setActionButtonsEnabled(true);
                break;
            case TURN_B:
                tvTurnIndicator.setText(crewB.getName() + "'s Turn");
                tvTurnIndicator.setTextColor(Color.parseColor(crewB.getColorHex()));
                btnSpecial.setText("Special\n(" + crewB.getSpecialization() + ")");
                setActionButtonsEnabled(true);
                break;
            case VICTORY:
            case DEFEAT:
                setActionButtonsEnabled(false);
                btnDone.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    /**
     * Turns the action buttons on or off depending on whether the player is
     * allowed to choose an action right now.
     */
    private void setActionButtonsEnabled(boolean enabled) {
        btnAttack.setEnabled(enabled);
        btnDefend.setEnabled(enabled);
        btnSpecial.setEnabled(enabled);
    }

    // ─────────────────────────── ACTION HANDLING ──────────────────────────────

    /**
     * Handles a player-chosen action for the current crew member's turn.
     * Bonus: Tactical Combat – player controls Attack / Defend / Special.
     */
    private void handleAction(String actionType) {
        setActionButtonsEnabled(false);

        CrewMember actor = (phase == MissionPhase.TURN_A) ? crewA : crewB;
        CrewMember ally  = (phase == MissionPhase.TURN_A) ? crewB : crewA;

        // Execute chosen action
        String actionLog;
        switch (actionType) {
            case "ATTACK":
                actionLog = mc.performAttack(actor, threat);
                break;
            case "DEFEND":
                actionLog = mc.performDefend(actor);
                break;
            case "SPECIAL":
                actionLog = mc.performSpecial(actor, ally, threat);
                break;
            default:
                actionLog = "";
        }

        appendLog(actionLog + "\n");
        updateThreatHUD();

        // Check if threat is defeated
        if (threat.isDefeated()) {
            String result = mc.completeMission(
                    crewAAlive ? crewA : null,
                    crewBAlive ? crewB : null);
            appendLog("\n" + result);
            appendLog("The " + threat.getName() + " has been neutralized!\n");
            setPhase(MissionPhase.VICTORY);
            tvTurnIndicator.setText("Mission Complete");
            tvTurnIndicator.setTextColor(Color.parseColor("#4CAF50"));
            return;
        }

        // Threat retaliates against the actor
        if (!actionType.equals("DEFEND") || true) { // threat always retaliates
            String retaliateLog = mc.threatRetaliate(threat, actor);
            appendLog(retaliateLog + "\n");
        }

        // Update HUD for the actor
        if (phase == MissionPhase.TURN_A) {
            updateCrewAHUD();
            crewAAlive = !crewA.isDefeated();
            if (!crewAAlive) {
                appendLog("\n" + crewA.getName() + " has been defeated and sent to Medbay.\n");
            }
        } else {
            updateCrewBHUD();
            crewBAlive = !crewB.isDefeated();
            if (!crewBAlive) {
                appendLog("\n" + crewB.getName() + " has been defeated and sent to Medbay.\n");
            }
        }

        // Check defeat condition
        if (!crewAAlive && !crewBAlive) {
            String result = mc.failMission(crewA, crewB);
            appendLog("\n" + result);
            setPhase(MissionPhase.DEFEAT);
            tvTurnIndicator.setText("Mission Failed");
            tvTurnIndicator.setTextColor(Color.parseColor("#F44336"));
            return;
        }

        // Advance turn
        advanceTurn();
    }

    /**
     * Moves play to the next living crew member or starts the next round once
     * both available turns for the current round have been used.
     */
    private void advanceTurn() {
        if (phase == MissionPhase.TURN_A) {
            // A just acted → B's turn (if alive)
            if (crewBAlive) {
                setPhase(MissionPhase.TURN_B);
            } else {
                // B is dead, A goes again next round
                round++;
                startRound();
            }
        } else {
            // B just acted → end of round, start next round
            round++;
            startRound();
        }
    }

    // ─────────────────────────── LOGGING ──────────────────────────────────────

    /**
     * Adds new text to the mission log and automatically scrolls the log view
     * to the newest line so recent events stay visible.
     */
    private void appendLog(String text) {
        missionLog.append(text);
        tvMissionLog.setText(missionLog.toString());
        // Auto-scroll to bottom
        scrollLog.post(() -> scrollLog.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
