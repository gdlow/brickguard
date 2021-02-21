package com.example.beskar.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.beskar.R;
import com.example.beskar.data.DateAndCount;
import com.example.beskar.data.LocalResolveViewModel;
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

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private LocalResolveViewModel localResolveViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        localResolveViewModel =
                new ViewModelProvider(this).get(LocalResolveViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);

        BarChart chart = root.findViewById(R.id.chart);
        List<BarEntry> entries = new ArrayList<>();
        populateChartTemplate(chart, entries, "Blocked sites");
        refreshChart(chart);
        localResolveViewModel.getDateAndCountFrom7dAgoWithNullRes().observe(getViewLifecycleOwner(),res -> {
            int i = 0;
            for (DateAndCount item : res) {
                entries.add(new BarEntry(i++, item.getCount()));
            }

            class InternalFormatter extends IndexAxisValueFormatter {

                @Override
                public String getFormattedValue(float value) {
                    return res.get((int) value).getDate();
                }
            }

            populateChartTemplate(chart, entries, "Blocked sites");
            chart.getXAxis().setValueFormatter(new InternalFormatter());
            refreshChart(chart);
        });

        dashboardViewModel.getText().observe(getViewLifecycleOwner(), s -> textView.setText(s));
        return root;
    }

    private void populateChartTemplate(BarChart chart, List<BarEntry> entries, String label) {
        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData barData = new BarData(dataSet);
        chart.setData(barData);
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
    }

    private void refreshChart(BarChart chart) {
        chart.invalidate();
        chart.notifyDataSetChanged();
    }
}