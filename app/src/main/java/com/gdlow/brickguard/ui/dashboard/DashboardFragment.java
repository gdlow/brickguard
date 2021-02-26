package com.gdlow.brickguard.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.gdlow.brickguard.R;
import com.gdlow.brickguard.data.Count;
import com.gdlow.brickguard.data.DateAndCount;
import com.gdlow.brickguard.data.InteractionsViewModel;
import com.gdlow.brickguard.data.LocalResolveViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private LocalResolveViewModel localResolveViewModel;
    private InteractionsViewModel interactionsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        localResolveViewModel =
                new ViewModelProvider(this).get(LocalResolveViewModel.class);
        interactionsViewModel =
                new ViewModelProvider(this).get(InteractionsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        TextView adsBlockedText = root.findViewById(R.id.ad_sites_blocked_text);
        populateText(adsBlockedText,
                localResolveViewModel.getAllLocalResolvesCountFrom7dAgoWithNullRes());

        TextView adultBlockedText = root.findViewById(R.id.adult_sites_blocked_text);
        populateText(adultBlockedText,
                localResolveViewModel.getAllLocalResolvesCountFrom7dAgoWithOneRes());

        BarChart adChart = root.findViewById(R.id.ad_sites_chart);
        populateChart(adChart, localResolveViewModel.getDateAndCountFrom7dAgoWithNullRes(),
                "Blocked ad sites");

        BarChart adultChart = root.findViewById(R.id.adult_sites_chart);
        populateChart(adultChart, localResolveViewModel.getDateAndCountFrom7dAgoWithOneRes(),
                "Blocked adult sites");

        TextView configChangesText = root.findViewById(R.id.config_changes_text);
        populateText(configChangesText, interactionsViewModel.getCountFrom7dAgoWithConfigChanges());

        TextView switchedOffText = root.findViewById(R.id.switched_off_text);
        populateText(switchedOffText, interactionsViewModel.getCountFrom7dAgoWithSwitchedOff());

        return root;
    }

    private void populateText(TextView text, LiveData<Count> data) {
        data.observe(getViewLifecycleOwner(),
                res -> text.setText(String.format(Locale.getDefault(), "%d", res.getCount())));
    }

    private void populateChart(BarChart chart, LiveData<List<DateAndCount>> data, String label) {
        populateChartTemplate(chart);
        data.observe(getViewLifecycleOwner(), res -> {
            // Transform List<DateAndCount> into List<BarEntry>
            List<BarEntry> entries = new ArrayList<>();
            int i = 0;
            for (DateAndCount item : res) {
                entries.add(new BarEntry(i++, item.getCount()));
            }

            class InternalFormatter extends IndexAxisValueFormatter {

                @Override
                public String getFormattedValue(float value) {
                    if (Math.round(value) >= res.size()) {
                        // Required to prevent null pointer exception
                        return "01-01-1970";
                    }
                    return res.get(Math.round(value)).getDate();
                }
            }

            BarDataSet dataSet = new BarDataSet(entries, label);
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            BarData barData = new BarData(dataSet);
            chart.setData(barData);
            chart.getXAxis().setValueFormatter(new InternalFormatter());
            refreshChart(chart);
        });
    }

    private void populateChartTemplate(BarChart chart) {
        chart.setDrawGridBackground(false);
        chart.setNoDataText("No data found");
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(0);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setDrawLabels(true);
    }

    private void refreshChart(BarChart chart) {
        chart.invalidate();
    }
}