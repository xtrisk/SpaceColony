package com.spacecolony.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.spacecolony.R;
import com.spacecolony.model.CrewMember;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RecyclerView Adapter for displaying crew members.
 * Bonus: RecyclerView feature implemented here.
 */
public class CrewMemberAdapter extends RecyclerView.Adapter<CrewMemberAdapter.ViewHolder> {

    /**
     * Simple callback used when the list should react to a tapped crew member.
     */
    public interface OnCrewMemberClickListener {
        void onCrewMemberClick(CrewMember cm);
    }

    private final List<CrewMember> crewList;
    private final Set<Integer> selectedIds = new HashSet<>();
    private final boolean selectionMode;
    private final int maxSelections;
    private OnCrewMemberClickListener clickListener;

    /**
     * @param crewList       List of crew members to display
     * @param selectionMode  If true, checkboxes are shown for multi-selection
     * @param maxSelections  Maximum number of items that can be selected (0 = unlimited)
     */
    public CrewMemberAdapter(List<CrewMember> crewList, boolean selectionMode, int maxSelections) {
        this.crewList = new ArrayList<>(crewList);
        this.selectionMode = selectionMode;
        this.maxSelections = maxSelections;
    }

    /**
     * Lets a screen register a custom click handler for cases where the list
     * is being used for taps instead of checkbox selection.
     */
    public void setOnCrewMemberClickListener(OnCrewMemberClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * Creates one visual row for the RecyclerView by inflating the crew item
     * layout from XML.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_crew_member, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Fills a row with the current crew member's name, stats, health, color,
     * and selection state.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CrewMember cm = crewList.get(position);

        // Set color indicator
        try {
            holder.colorBar.setBackgroundColor(Color.parseColor(cm.getColorHex()));
        } catch (Exception e) {
            holder.colorBar.setBackgroundColor(Color.GRAY);
        }

        // Display name and specialization
        holder.tvName.setText(cm.getName());
        holder.tvSpecialization.setText(cm.getSpecialization());

        // Stats
        holder.tvStats.setText("SKL:" + cm.getEffectiveSkill()
                + "  RES:" + cm.getResilience()
                + "  XP:" + cm.getExperience()
                + "  LOC:" + cm.getLocation().getDisplayName());

        // Energy progress bar
        holder.energyBar.setMax(cm.getMaxEnergy());
        holder.energyBar.setProgress(cm.getEnergy());
        holder.tvEnergy.setText(cm.getEnergy() + "/" + cm.getMaxEnergy());

        boolean isSelected = selectedIds.contains(cm.getId());
        int normalColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.card_bg);
        int selectedColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.card_bg_highlight);
        holder.cardView.setCardBackgroundColor(isSelected ? selectedColor : normalColor);

        // Checkbox visibility
        if (selectionMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(isSelected);
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(cm.getId(), position);
            } else if (clickListener != null) {
                clickListener.onCrewMemberClick(cm);
            }
        });

        holder.checkBox.setOnClickListener(v -> toggleSelection(cm.getId(), position));
    }

    /**
     * Adds or removes a crew member from the selected set and enforces the
     * maximum selection count when a screen has a limit.
     */
    private void toggleSelection(int id, int position) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id);
        } else {
            if (maxSelections > 0 && selectedIds.size() >= maxSelections) {
                // Remove the first selected if at max
                Integer first = selectedIds.iterator().next();
                selectedIds.remove(first);
                notifyDataSetChanged();
            }
            selectedIds.add(id);
        }
        notifyItemChanged(position);
    }

    /**
     * Reports how many rows the RecyclerView should display.
     */
    @Override
    public int getItemCount() {
        return crewList.size();
    }

    /** Returns IDs of currently selected crew members. */
    public Set<Integer> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }

    /** Replaces the data set and refreshes. */
    public void updateData(List<CrewMember> newList) {
        crewList.clear();
        crewList.addAll(newList);
        selectedIds.clear();
        notifyDataSetChanged();
    }

    /** Returns the crew member at a given position. */
    public CrewMember getItem(int position) {
        return crewList.get(position);
    }

    // ─────────────────────────── VIEW HOLDER ──────────────────────────────────

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardView;
        final View colorBar;
        final TextView tvName;
        final TextView tvSpecialization;
        final TextView tvStats;
        final ProgressBar energyBar;
        final TextView tvEnergy;
        final CheckBox checkBox;

        /**
         * Caches the child views inside one list row so binding data is fast
         * and the adapter does not keep calling findViewById repeatedly.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView         = (MaterialCardView) itemView;
            colorBar         = itemView.findViewById(R.id.viewColorBar);
            tvName           = itemView.findViewById(R.id.tvCrewName);
            tvSpecialization = itemView.findViewById(R.id.tvSpecialization);
            tvStats          = itemView.findViewById(R.id.tvStats);
            energyBar        = itemView.findViewById(R.id.progressEnergy);
            tvEnergy         = itemView.findViewById(R.id.tvEnergy);
            checkBox         = itemView.findViewById(R.id.checkBoxSelect);
        }
    }
}
