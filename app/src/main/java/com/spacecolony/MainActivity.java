package com.spacecolony;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.spacecolony.model.Location;
import com.spacecolony.model.Storage;

import java.util.Map;

/**
 * Main Activity – Colony Overview / Home Screen.
 * Shows summary counts for each location and navigation buttons.
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvQuartersCount;
    private TextView tvSimulatorCount;
    private TextView tvMissionControlCount;
    private TextView tvMedbayCount;
    private TextView tvTotalCrew;
    private TextView tvMissionCount;

    /**
     * Sets up the home screen, connects the summary labels, and wires each
     * button to the screen it should open.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvQuartersCount      = findViewById(R.id.tvQuartersCount);
        tvSimulatorCount     = findViewById(R.id.tvSimulatorCount);
        tvMissionControlCount = findViewById(R.id.tvMissionControlCount);
        tvMedbayCount        = findViewById(R.id.tvMedbayCount);
        tvTotalCrew          = findViewById(R.id.tvTotalCrew);
        tvMissionCount       = findViewById(R.id.tvMissionCount);

        // Navigation buttons
        findViewById(R.id.btnRecruit).setOnClickListener(v ->
                startActivity(new Intent(this, RecruitActivity.class)));

        findViewById(R.id.btnQuarters).setOnClickListener(v ->
                startActivity(new Intent(this, QuartersActivity.class)));

        findViewById(R.id.btnSimulator).setOnClickListener(v ->
                startActivity(new Intent(this, SimulatorActivity.class)));

        findViewById(R.id.btnMissionControl).setOnClickListener(v ->
                startActivity(new Intent(this, MissionControlActivity.class)));

        findViewById(R.id.btnMedbay).setOnClickListener(v ->
                startActivity(new Intent(this, MedbayActivity.class)));

        findViewById(R.id.btnStats).setOnClickListener(v ->
                startActivity(new Intent(this, StatsActivity.class)));
    }

    /**
     * Refreshes the dashboard whenever the user comes back here so the counts
     * always reflect the latest crew movements and mission progress.
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateCounts();
    }

    /**
     * Reads the latest data from storage and prints the current totals for each
     * room, the total crew size, and the finished mission count.
     */
    private void updateCounts() {
        Storage storage = Storage.getInstance();
        Map<Location, Integer> counts = storage.getLocationCounts();

        tvQuartersCount.setText("Quarters: " + counts.get(Location.QUARTERS));
        tvSimulatorCount.setText("Simulator: " + counts.get(Location.SIMULATOR));
        tvMissionControlCount.setText("Mission Control: " + counts.get(Location.MISSION_CONTROL));
        tvMedbayCount.setText("Medbay: " + counts.get(Location.MEDBAY));
        tvTotalCrew.setText("Total Crew: " + storage.getTotalCrew());
        tvMissionCount.setText("Missions Completed: " + com.spacecolony.game.MissionControl.getMissionCounter());
    }
}
