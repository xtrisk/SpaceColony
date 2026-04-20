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
import com.spacecolony.model.CrewMember;
import com.spacecolony.model.Location;
import com.spacecolony.model.Storage;

import java.util.List;
import java.util.Set;

/**
 * Medbay Screen. Bonus: No Death feature.
 * Crew members who were defeated in battle come here.
 * They can be discharged back to Quarters once recovered.
 */
public class MedbayActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CrewMemberAdapter adapter;
    private TextView tvEmpty;

    /**
     * Prepares the Medbay screen and connects the discharge button to the
     * logic that sends recovered crew back to Quarters.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medbay);

        recyclerView = findViewById(R.id.recyclerViewCrew);
        tvEmpty      = findViewById(R.id.tvEmpty);
        Button btnDischarge = findViewById(R.id.btnDischarge);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnDischarge.setOnClickListener(v -> dischargeSelected());

        loadCrewList();
    }

    /**
     * Reloads the Medbay list whenever the user returns so the screen never
     * shows outdated recovery information.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadCrewList();
    }

    /**
     * Shows everyone currently in Medbay or displays the empty-state message
     * if nobody is recovering there.
     */
    private void loadCrewList() {
        List<CrewMember> crew = Storage.getInstance().getCrewByLocation(Location.MEDBAY);
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
     * Sends the selected recovering crew members back to Quarters, where their
     * energy is restored and they become active again.
     */
    private void dischargeSelected() {
        if (adapter == null) {
            Toast.makeText(this, "Medbay is empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        Set<Integer> selectedIds = adapter.getSelectedIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Select crew members to discharge.", Toast.LENGTH_SHORT).show();
            return;
        }

        Storage storage = Storage.getInstance();
        int count = 0;
        for (int id : selectedIds) {
            CrewMember cm = storage.getCrewMember(id);
            if (cm != null) {
                Quarters.getInstance().moveToQuarters(cm); // restores energy
                count++;
            }
        }
        Toast.makeText(this, count + " crew member(s) discharged to Quarters.", Toast.LENGTH_SHORT).show();
        loadCrewList();
    }
}
