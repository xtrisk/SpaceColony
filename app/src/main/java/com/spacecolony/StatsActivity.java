package com.spacecolony;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spacecolony.adapter.CrewMemberAdapter;
import com.spacecolony.game.MissionControl;
import com.spacecolony.model.CrewMember;
import com.spacecolony.model.Location;
import com.spacecolony.model.Storage;

import java.util.List;
import java.util.Map;

/**
 * Statistics Screen. Bonus: Statistics feature (+1).
 * Shows colony-wide stats and per-crew-member performance.
 */
public class StatsActivity extends AppCompatActivity {

    /**
     * Builds the statistics screen by summarizing colony-wide numbers first
     * and then listing detailed stats for every recruited crew member.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        TextView tvColonyStats = findViewById(R.id.tvColonyStats);
        TextView tvCrewStats   = findViewById(R.id.tvCrewStats);

        // ── Colony-wide statistics ──
        Storage storage = Storage.getInstance();
        Map<Location, Integer> counts = storage.getLocationCounts();
        int totalMissions = MissionControl.getMissionCounter();
        int totalCrew = storage.getTotalCrew();

        StringBuilder colonyInfo = new StringBuilder();
        colonyInfo.append("Total Crew: ").append(totalCrew).append("\n");
        colonyInfo.append("Total Missions Completed: ").append(totalMissions).append("\n\n");
        colonyInfo.append("Current Locations:\n");
        for (Location loc : Location.values()) {
            colonyInfo.append("  • ").append(loc.getDisplayName())
                    .append(": ").append(counts.get(loc)).append("\n");
        }
        tvColonyStats.setText(colonyInfo.toString());

        // ── Per-crew-member statistics ──
        List<CrewMember> all = storage.listCrewMembers();
        if (all.isEmpty()) {
            tvCrewStats.setText("No crew members recruited yet.");
        } else {
            StringBuilder crewInfo = new StringBuilder();
            for (CrewMember cm : all) {
                crewInfo.append(cm.getSpecialization())
                        .append(" — ").append(cm.getName())
                        .append("  [ID:").append(cm.getId()).append("]\n");
                crewInfo.append("  Location   : ").append(cm.getLocation().getDisplayName()).append("\n");
                crewInfo.append("  Skill      : ").append(cm.getEffectiveSkill())
                        .append(" (base ").append(cm.getBaseSkill())
                        .append(" + xp ").append(cm.getExperience()).append(")\n");
                crewInfo.append("  Energy     : ").append(cm.getEnergy())
                        .append("/").append(cm.getMaxEnergy()).append("\n");
                crewInfo.append("  Training   : ").append(cm.getTrainingSessions()).append(" sessions\n");
                crewInfo.append("  Missions   : ").append(cm.getMissionsCompleted())
                        .append(" total | ").append(cm.getMissionsWon())
                        .append(" won | ").append(cm.getMissionsLost()).append(" lost\n");
                crewInfo.append("\n");
            }
            tvCrewStats.setText(crewInfo.toString());
        }
    }
}
