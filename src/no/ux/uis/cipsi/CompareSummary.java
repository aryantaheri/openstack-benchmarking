package no.ux.uis.cipsi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.panayotis.gnuplot.JavaPlot;

public class CompareSummary {

    public static void main(String[] args) {
        CompareStrategyTypes.main(args);
        CompareExecTypes.main(args);
        Map<String, List<JavaPlot>> strategySummaryPlotMap = CompareStrategyTypes.summaryPlotMap;
        Map<String, List<JavaPlot>> execTypeSummaryPlotMap = CompareExecTypes.summaryPlotMap;

        List<JavaPlot> summaries1 = new ArrayList<>();
        List<JavaPlot> summaries2 = new ArrayList<>();
        for (String strategy : strategySummaryPlotMap.keySet()) {
            summaries1.addAll(strategySummaryPlotMap.get(strategy).subList(0, 3));
            summaries2.addAll(strategySummaryPlotMap.get(strategy).subList(3, 6));
        }
        for (String execType : execTypeSummaryPlotMap.keySet()) {
            summaries1.addAll(execTypeSummaryPlotMap.get(execType).subList(0, 3));
            summaries2.addAll(execTypeSummaryPlotMap.get(execType).subList(3, 6));
        }

        Plotter.plotMultipleBoxPlots("/tmp", "Summary", "1", summaries1);
        Plotter.plotMultipleBoxPlots("/tmp", "Summary", "2", summaries2);
    }
}
