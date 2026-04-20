package com.spacecolony;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spacecolony.adapter.CrewMemberAdapter;
import com.spacecolony.game.MissionControl;
import com.spacecolony.game.Simulator;
import com.spacecolony.model.CrewMember;
import com.spacecolony.model.Location;
import com.spacecolony.model.Storage;

import java.util.List;
import java.util.Set;

/**
 * Quarters Screen.
 * Lists crew members currently in Quarters.
 * Allows moving them to Simulator or Mission Control.
 */
public class QuartersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CrewMemberAdapter adapter;
    private TextView tvEmpty;

    /**
     * Builds the Quarters screen and wires the movement buttons so selected
     * crew can be sent to training or to mission prep.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quarters);

        recyclerView = findViewById(R.id.recyclerViewCrew);
        tvEmpty      = findViewById(R.id.tvEmpty);
        Button btnMoveSimulator     = findViewById(R.id.btnMoveToSimulator);
        Button btnMoveMissionControl = findViewById(R.id.btnMoveToMissionControl);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnMoveSimulator.setOnClickListener(v -> moveSelected(Location.SIMULATOR));
        btnMoveMissionControl.setOnClickListener(v -> moveSelected(Location.MISSION_CONTROL));

        loadCrewList();
    }

    /**
     * Reloads the Quarters roster whenever the screen becomes active again so
     * the visible list matches the latest crew locations.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadCrewList();
    }

    /**
     * Displays all crew members currently resting in Quarters, or shows the
     * empty-state message when nobody is there.
     */
    private void loadCrewList() {
        List<CrewMember> crew = Storage.getInstance().getCrewByLocation(Location.QUARTERS);
        if (crew.isEmpty()) {
            tvEmpty.setVisibility(android.view.View.VISIBLE);
            recyclerView.setVisibility(android.view.View.GONE);
        } else {
            tvEmpty.setVisibility(android.view.View.GONE);
            recyclerView.setVisibility(android.view.View.VISIBLE);
            adapter = new CrewMemberAdapter(crew, true, 0);
            recyclerView.setAdapter(adapter);
        }
    }

    /**
     * Sends the selected crew to the requested destination and then refreshes
     * the list so the move is immediately reflected on screen.
     */
    private void moveSelected(Location target) {
        if (adapter == null) {
            Toast.makeText(this, "No crew in Quarters.", Toast.LENGTH_SHORT).show();
            return;
        }
        Set<Integer> selectedIds = adapter.getSelectedIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Select at least one crew member first.", Toast.LENGTH_SHORT).show();
            return;
        }

        Storage storage = Storage.getInstance();
        int count = 0;
        for (int id : selectedIds) {
            CrewMember cm = storage.getCrewMember(id);
            if (cm != null) {
                if (target == Location.SIMULATOR) {
                    Simulator.getInstance().moveToSimulator(cm);
                } else {
                    MissionControl.getInstance().moveToMissionControl(cm);
                }
                count++;
            }
        }

        Toast.makeText(this, count + " crew member(s) moved to " + target.getDisplayName() + ".",
                Toast.LENGTH_SHORT).show();
        loadCrewList();
    }
}
