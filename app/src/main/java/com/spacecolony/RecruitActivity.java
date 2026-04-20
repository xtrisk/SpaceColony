package com.spacecolony;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.spacecolony.game.Quarters;
import com.spacecolony.model.CrewMember;

/**
 * Recruit Crew Member Screen.
 * Allows the user to enter a name, pick a specialization, and create a new crew member.
 */
public class RecruitActivity extends AppCompatActivity {

    private EditText etName;
    private Spinner spinnerSpec;
    private TextView tvStatPreview;

    private static final String[] SPECIALIZATIONS = {
            "Pilot", "Engineer", "Medic", "Scientist", "Soldier"
    };

    // Default stats per spec for the preview
    private static final String[] STAT_PREVIEWS = {
            "Skill: 5 | Resilience: 4 | Max Energy: 20\nSpecial: Evasion – Doubles resilience for next hit",
            "Skill: 6 | Resilience: 3 | Max Energy: 19\nSpecial: Repair Systems – Restore 4 own energy",
            "Skill: 7 | Resilience: 2 | Max Energy: 18\nSpecial: Field Medicine – Heal ally for 6 energy",
            "Skill: 8 | Resilience: 1 | Max Energy: 17\nSpecial: Analyze Weakness – Reduce threat resilience by 2",
            "Skill: 9 | Resilience: 0 | Max Energy: 16\nSpecial: Assault Strike – 1.5× damage attack"
    };

    /**
     * Builds the recruit screen, fills the specialization dropdown, and wires
     * the create/cancel buttons.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recruit);

        etName        = findViewById(R.id.etCrewName);
        spinnerSpec   = findViewById(R.id.spinnerSpecialization);
        tvStatPreview = findViewById(R.id.tvStatPreview);
        Button btnCreate = findViewById(R.id.btnCreate);
        Button btnCancel = findViewById(R.id.btnCancel);

        // Set up specialization spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, SPECIALIZATIONS);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerSpec.setAdapter(adapter);

        // Update stat preview when selection changes
        spinnerSpec.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                tvStatPreview.setText(STAT_PREVIEWS[pos]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Show initial preview
        tvStatPreview.setText(STAT_PREVIEWS[0]);

        btnCreate.setOnClickListener(v -> createCrewMember());
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Validates the entered name, creates the selected crew type, and closes
     * the screen after showing a success message.
     */
    private void createCrewMember() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name for the crew member.", Toast.LENGTH_SHORT).show();
            return;
        }

        String spec = SPECIALIZATIONS[spinnerSpec.getSelectedItemPosition()];
        CrewMember cm = Quarters.getInstance().createCrewMember(name, spec);

        if (cm != null) {
            Toast.makeText(this,
                    spec + " " + name + " has joined the colony! (ID: " + cm.getId() + ")",
                    Toast.LENGTH_LONG).show();
            etName.setText("");
            finish();
        } else {
            Toast.makeText(this, "Failed to create crew member.", Toast.LENGTH_SHORT).show();
        }
    }
}
