package com.spacecolony;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spacecolony.adapter.CrewMemberAdapter;
import com.spacecolony.game.Quarters;
import com.spacecolony.game.Simulator;
import com.spacecolony.model.CrewMember;
import com.spacecolony.model.Location;
import com.spacecolony.model.Storage;

import java.util.List;
import java.util.Set;

/**
 * Simulator Screen.
 * Lists crew members in the Simulator.
 * Allows training (gaining XP) or returning them to Quarters.
 */
public class SimulatorActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CrewMemberAdapter adapter;
    private TextView tvEmpty;
    private TextView tvTrainingLog;

    /**
     * Sets up the Simulator screen and connects the training and return
     * buttons to their matching actions.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulator);

        recyclerView  = findViewById(R.id.recyclerViewCrew);
        tvEmpty       = findViewById(R.id.tvEmpty);
        tvTrainingLog = findViewById(R.id.tvTrainingLog);

        Button btnTrain       = findViewById(R.id.btnTrain);
        Button btnToQuarters  = findViewById(R.id.btnMoveToQuarters);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnTrain.setOnClickListener(v -> trainSelected());
        btnToQuarters.setOnClickListener(v -> moveToQuarters());

        loadCrewList();
    }

    /**
     * Reloads the Simulator list whenever the user returns so the training
     * screen always shows the current roster.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadCrewList();
    }

    /**
     * Shows the crew currently in the Simulator or switches to the empty-state
     * message if no one is available for training.
     */
    private void loadCrewList() {
        List<CrewMember> crew = Storage.getInstance().getCrewByLocation(Location.SIMULATOR);
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
     * Runs a training session for every selected crew member, writes the
     * results into the training log, and refreshes the list afterward.
     */
    private void trainSelected() {
        if (adapter == null) {
            Toast.makeText(this, "No crew in Simulator.", Toast.LENGTH_SHORT).show();
            return;
        }
        Set<Integer> selectedIds = adapter.getSelectedIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Select crew members to train.", Toast.LENGTH_SHORT).show();
            return;
        }

        Storage storage = Storage.getInstance();
        StringBuilder log = new StringBuilder();
        for (int id : selectedIds) {
            CrewMember cm = storage.getCrewMember(id);
            if (cm != null) {
                String result = Simulator.getInstance().train(cm);
                log.append(result).append("\n\n");
            }
        }

        tvTrainingLog.setText(log.toString().trim());
        loadCrewList();
    }

    /**
     * Moves the selected crew back to Quarters, clears the training log, and
     * refreshes the screen so their new location is reflected immediately.
     */
    private void moveToQuarters() {
        if (adapter == null) {
            Toast.makeText(this, "No crew in Simulator.", Toast.LENGTH_SHORT).show();
            return;
        }
        Set<Integer> selectedIds = adapter.getSelectedIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Select crew members to send to Quarters.", Toast.LENGTH_SHORT).show();
            return;
        }

        Storage storage = Storage.getInstance();
        int count = 0;
        for (int id : selectedIds) {
            CrewMember cm = storage.getCrewMember(id);
            if (cm != null) {
                Quarters.getInstance().moveToQuarters(cm);
                count++;
            }
        }

        Toast.makeText(this, count + " crew member(s) returned to Quarters (energy restored).",
                Toast.LENGTH_SHORT).show();
        tvTrainingLog.setText("");
        loadCrewList();
    }
}
