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
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

            BarDataSet dataSet = new BarDataSet(entries, "Blocked sites");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            BarData barData = new BarData(dataSet);
            chart.setData(barData);
            chart.setDrawGridBackground(false);
            chart.setNoDataText("No data found");
            Description desc = new Description();
            desc.setText("");
            chart.setDescription(desc);
            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setValueFormatter(new InternalFormatter());
            xAxis.setLabelCount(0);

            chart.getAxisRight().setEnabled(false);
            chart.getAxisLeft().setEnabled(false);

            chart.invalidate(); // refresh
            chart.notifyDataSetChanged();
        });
        BarDataSet dataSet = new BarDataSet(entries, "Blocked sites");
        dataSet.setColor(R.color.black_a60);
        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        chart.setDrawGridBackground(false);
        chart.setNoDataText("No data found");
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(0);
        xAxis.setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);

        chart.invalidate(); // refresh
        chart.notifyDataSetChanged();

        dashboardViewModel.getText().observe(getViewLifecycleOwner(), s -> textView.setText(s));
        return root;
    }
}