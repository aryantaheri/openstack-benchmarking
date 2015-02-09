package no.ux.uis.cipsi;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.opendaylight.controller.samples.differentiatedforwarding.openstack.ReportManager;
import org.opendaylight.controller.samples.differentiatedforwarding.openstack.performance.BwExpReport;
import org.opendaylight.controller.samples.differentiatedforwarding.openstack.performance.BwReport;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.Point;
import com.panayotis.gnuplot.dataset.PointDataSet;
import com.panayotis.gnuplot.layout.AutoGraphLayout;
import com.panayotis.gnuplot.layout.GraphLayout;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.plot.Plot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.DefaultTerminal;
import com.panayotis.gnuplot.terminal.GNUPlotTerminal;
import com.panayotis.gnuplot.terminal.PostscriptTerminal;
import com.panayotis.gnuplot.terminal.SVGTerminal;

public class ClassBasedProcessor {

    private static Map<String, List<Point<Number>>> rateDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> rateDiffDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> rateSameDataPointMap = new HashMap<>();

    private static Map<String, List<Point<Number>>> rxCpuDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> rxCpuDiffDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> rxCpuSameDataPointMap = new HashMap<>();

    private static Map<String, List<Point<Number>>> txCpuDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> txCpuDiffDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> txCpuSameDataPointMap = new HashMap<>();

    private static Map<String, List<Point<Number>>> rttDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> rttDiffDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> rttSameDataPointMap = new HashMap<>();

    private static Map<String, List<Point<Number>>> retransDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> retransDiffDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> retransSameDataPointMap = new HashMap<>();

    private static Map<String, List<Point<Number>>> reportErrorDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> reportErrorDiffDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> reportErrorSameDataPointMap = new HashMap<>();

    private static Map<String, List<Point<Number>>> missingValueDataPointMap = new HashMap<>();

    private static void findReports(String dirPath){
        File dir = new File(dirPath);
        FileFilter fileFilter = new WildcardFileFilter("*.obj");
        File[] files = dir.listFiles(fileFilter);

        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList);
        for (File file : fileList) {
            System.out.println(file);
            processFile(file);
        }
    }

    public static void main(String[] args) {
        plotAll("/home/aryan/data/exp-20141205-095731");
    }
    public static void main2(String[] args) {
        String dirName = "/home/aryan/data/exp-20141205-095731";
        findReports(dirName);
        plot(dirName + "/rate.eps", "BW All", "#Instances", "Rate (Mbps)", rateDataPointMap);
        plot(dirName + "/rateD.eps", "BW D", "#Instances", "Rate (Mbps)", rateDiffDataPointMap);
        plot(dirName + "/rateS.eps", "BW S", "#Instances", "Rate (Mbps)", rateSameDataPointMap);

        plotInverse(dirName + "/cpu.eps", "CPU Utilization", "#Instances", "Rx CPU", rxCpuDataPointMap, "Tx CPU", txCpuDataPointMap);
        plotInverse(dirName + "/cpuD.eps", "CPU Utilization D", "#Instances", "Rx CPU", rxCpuDiffDataPointMap, "Tx CPU", txCpuDiffDataPointMap);
        plotInverse(dirName + "/cpuS.eps", "CPU Utilization S", "#Instances", "Rx CPU", rxCpuSameDataPointMap, "Tx CPU", txCpuSameDataPointMap);

        plot(dirName + "/rtt.eps", "RTT All", "#Instances", "RTT (ms)", rttDataPointMap);
        plot(dirName + "/rttD.eps", "RTT D", "#Instances", "RTT (ms)", rttDiffDataPointMap);
        plot(dirName + "/rttS.eps", "RTT S", "#Instances", "RTT (ms)", rttSameDataPointMap);

        plot(dirName + "/retrans.eps", "Retrans All", "#Instances", "# retrans", retransDataPointMap);
        plot(dirName + "/retransD.eps", "Retrans D", "#Instances", "# retrans", retransDiffDataPointMap);
        plot(dirName + "/retransS.eps", "Retrans S", "#Instances", "#retrans", retransSameDataPointMap);

        plot(dirName + "/reportError.eps", "reportError All", "#Instances", "# error", reportErrorDataPointMap);
        plot(dirName + "/reportErrorD.eps", "reportError D", "#Instances", "# error", reportErrorDiffDataPointMap);
        plot(dirName + "/reportErrorS.eps", "reportError S", "#Instances", "# error", reportErrorSameDataPointMap);

        plot(dirName + "/missingValue.eps", "missingValue All", "#Instances", "# missingValue", missingValueDataPointMap);
    }

    private static void processFile(File bwExpReportsFile){
        List<BwExpReport> bwExpReports = ReportManager.readReportObjects(bwExpReportsFile.getAbsolutePath());
        boolean classConsurrency = areClassesConcurrent(bwExpReportsFile.getName());
        boolean instanceConsurrency = areInstancesConcurrent(bwExpReportsFile.getName());;
        int[] classes = getClasses(bwExpReportsFile.getName());
        int netNum = getNetNum(bwExpReportsFile.getName());
        int insNum = getInsNum(bwExpReportsFile.getName());
        System.out.println(bwExpReportsFile.getName());
        System.out.println(classConsurrency);
        System.out.println(instanceConsurrency);
        System.out.println(Arrays.toString(classes));
        System.out.println(netNum);
        System.out.println(insNum);

        Map<Integer, List<BwExpReport>> classGroups = new HashMap<Integer, List<BwExpReport>>();

        for (BwExpReport bwExpReport : bwExpReports) {
            int classValue = bwExpReport.getClassValue();
            List<BwExpReport> classReports = classGroups.get(classValue);
            if (classReports == null) {
                classReports = new ArrayList<>();
                classGroups.put(classValue, classReports);
            }
            classReports.add(bwExpReport);
        }

        setClassDataPoints(classGroups);
    }

    private static void setClassDataPoints(Map<Integer, List<BwExpReport>> classGroups) {

        for (Integer classValue : classGroups.keySet()){
            DescriptiveStatistics rateStats = new DescriptiveStatistics();
            DescriptiveStatistics rateStatsDiffHyper = new DescriptiveStatistics();
            DescriptiveStatistics rateStatsSameHyper = new DescriptiveStatistics();

            DescriptiveStatistics cpuRxStats = new DescriptiveStatistics();
            DescriptiveStatistics cpuRxStatsDiffHyper = new DescriptiveStatistics();
            DescriptiveStatistics cpuRxStatsSameHyper = new DescriptiveStatistics();

            DescriptiveStatistics cpuTxStats = new DescriptiveStatistics();
            DescriptiveStatistics cpuTxStatsDiffHyper = new DescriptiveStatistics();
            DescriptiveStatistics cpuTxStatsSameHyper = new DescriptiveStatistics();

            DescriptiveStatistics rttStats = new DescriptiveStatistics();
            DescriptiveStatistics rttStatsDiffHyper = new DescriptiveStatistics();
            DescriptiveStatistics rttStatsSameHyper = new DescriptiveStatistics();

            DescriptiveStatistics retransStats = new DescriptiveStatistics();
            DescriptiveStatistics retransStatsDiffHyper = new DescriptiveStatistics();
            DescriptiveStatistics retransStatsSameHyper = new DescriptiveStatistics();

            List<BwExpReport> classReports = classGroups.get(classValue);
            int classNetNum = classReports.size();
            int netInsNum = 0;
            int missingValueCount = 0;

            int reportErrorCount = 0;
            int reportErrorCountDiffHyper = 0;
            int reportErrorCountSameHyper = 0;

            for (BwExpReport bwExpReport : classReports) {
                netInsNum += bwExpReport.getReachableInstances().size();
                missingValueCount += bwExpReport.getMissingValueCount();
                reportErrorCount += bwExpReport.getReportErrorCount();

                for (BwReport report : bwExpReport.getBwReports()) {

                    addValue(rateStats, report.getRate());
                    addValue(cpuRxStats, report.getReceiverCpu());
                    addValue(cpuTxStats, report.getTransmitterCpu());
                    addValue(rttStats, report.getRtt());
                    addValue(retransStats, report.getRetrans());

                    if (report.getReceiverHost().equalsIgnoreCase(report.getTransmitterHost())){

                        addValue(rateStatsSameHyper, report.getRate());
                        addValue(cpuRxStatsSameHyper, report.getReceiverCpu());
                        addValue(cpuTxStatsSameHyper, report.getTransmitterCpu());
                        addValue(rttStatsSameHyper, report.getRtt());
                        addValue(retransStatsSameHyper, report.getRetrans());

                        reportErrorCountSameHyper = updateReportErrorCount(report, reportErrorCountSameHyper);
                    } else {

                        addValue(rateStatsDiffHyper, report.getRate());
                        addValue(cpuRxStatsDiffHyper, report.getReceiverCpu());
                        addValue(cpuTxStatsDiffHyper, report.getTransmitterCpu());
                        addValue(rttStatsDiffHyper, report.getRtt());
                        addValue(retransStatsDiffHyper, report.getRetrans());

                        reportErrorCountDiffHyper = updateReportErrorCount(report, reportErrorCountDiffHyper);
                    }
                }
            }

            String dataSetName = getDataSetName(classValue, classNetNum);

            addDataPoints(dataSetName, netInsNum, rateStats, rateDataPointMap);
            addDataPoints(dataSetName, netInsNum, rateStatsDiffHyper, rateDiffDataPointMap);
            addDataPoints(dataSetName, netInsNum, rateStatsSameHyper, rateSameDataPointMap);

            addDataPoints(dataSetName, netInsNum, cpuRxStats, rxCpuDataPointMap);
            addDataPoints(dataSetName, netInsNum, cpuRxStatsDiffHyper, rxCpuDiffDataPointMap);
            addDataPoints(dataSetName, netInsNum, cpuRxStatsSameHyper, rxCpuSameDataPointMap);

            addDataPoints(dataSetName, netInsNum, cpuTxStats, txCpuDataPointMap);
            addDataPoints(dataSetName, netInsNum, cpuTxStatsDiffHyper, txCpuDiffDataPointMap);
            addDataPoints(dataSetName, netInsNum, cpuTxStatsSameHyper, txCpuSameDataPointMap);

            addDataPoints(dataSetName, netInsNum, rttStats, rttDataPointMap);
            addDataPoints(dataSetName, netInsNum, rttStatsDiffHyper, rttDiffDataPointMap);
            addDataPoints(dataSetName, netInsNum, rttStatsSameHyper, rttSameDataPointMap);

            addDataPoints(dataSetName, netInsNum, retransStats, retransDataPointMap);
            addDataPoints(dataSetName, netInsNum, retransStatsDiffHyper, retransDiffDataPointMap);
            addDataPoints(dataSetName, netInsNum, retransStatsSameHyper, retransSameDataPointMap);

            addDataPoints(dataSetName, netInsNum, reportErrorCount, reportErrorDataPointMap);
            addDataPoints(dataSetName, netInsNum, reportErrorCountDiffHyper, reportErrorDiffDataPointMap);
            addDataPoints(dataSetName, netInsNum, reportErrorCountSameHyper, reportErrorSameDataPointMap);

            addDataPoints(dataSetName, netInsNum, missingValueCount, missingValueDataPointMap);


        }

    }

    private static void addValue(DescriptiveStatistics stats, Float value) {
        if (value == null){
            return;
        } else {
            stats.addValue(value);
        }
    }

    private static int updateReportErrorCount(BwReport report, int reportErrorCount) {
        if (report.hasError()){
            reportErrorCount++;
            return reportErrorCount;
        } else {
            return reportErrorCount;
        }
    }

    private static void addDataPoints(String dataSetName, int netInsNum, DescriptiveStatistics stats, Map<String, List<Point<Number>>> dataPointMap) {
        List<Point<Number>> dataPointList = dataPointMap.get(dataSetName);
        if (dataPointList == null){
            dataPointList = new ArrayList<>();
            dataPointMap.put(dataSetName, dataPointList);
        }

        if (Double.compare(stats.getMean(), Double.NaN) == 0) {
            System.out.println("WTF");
            return;
        }
        Point<Number> mean = new Point<Number>(netInsNum, stats.getMean(), stats.getMin(), stats.getMax());
        System.out.println("mean:" + stats.getMean());
        dataPointList.add(mean);
    }

    private static void addDataPoints(String dataSetName, int netInsNum, int value, Map<String, List<Point<Number>>> dataPointMap) {
        List<Point<Number>> dataPointList = dataPointMap.get(dataSetName);
        if (dataPointList == null){
            dataPointList = new ArrayList<>();
            dataPointMap.put(dataSetName, dataPointList);
        }

        Point<Number> mean = new Point<Number>(netInsNum, value, value, value);
        System.out.println("value:" + value);
        dataPointList.add(mean);
    }

    private static PointDataSet<Number> getSortedPointDataSet(List<Point<Number>> pointListOrig, boolean inverse) {
        PointDataSet<Number> points = new PointDataSet<>();
        List<Point<Number>> pointList = new ArrayList<>();
        int scale = ((inverse == true) ? -1 : 1);

        for (Point<Number> point : pointListOrig) {
            pointList.add(new Point<Number>(point.get(0),
                                            (scale * point.get(1).doubleValue()),
                                            (scale * point.get(2).doubleValue()),
                                            (scale * point.get(3).doubleValue())));
        }
        Collections.sort(pointList, new Comparator<Point<Number>>() {

            @Override
            public int compare(Point<Number> o1, Point<Number> o2) {
                if (o1.get(0).doubleValue() < o2.get(0).doubleValue()){
                    return -1;
                } else if (o1.get(0).doubleValue() == o2.get(0).doubleValue()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        points.addAll(pointList);
        return points;
    }

    private static JavaPlot plot(String plotName, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMap) {
        JavaPlot plot = new JavaPlot();
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        PlotStyle plotStyle = new PlotStyle();
//        plotStyle.setStyle(Style.LINESPOINTS);
        plotStyle.setStyle(Style.ERRORLINES);

        for (String dataSetName : dataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(getSortedPointDataSet(dataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(dataSetName);
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel(xLable);
        plot.getAxis("y").setLabel(yLable);
        plot.set("xzeroaxis", "");
        plot.setTitle(plotTitle);
        GNUPlotTerminal term = new PostscriptTerminal(plotName);
        plot.setTerminal(term);
        plot.plot();
        return plot;
    }

    private static JavaPlot plotInverse(String plotName, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMapY1, String y2Lable, Map<String, List<Point<Number>>> dataPointMapY2) {
        JavaPlot plot = new JavaPlot();
        PlotStyle plotStyle = new PlotStyle();
//        plotStyle.setStyle(Style.LINESPOINTS);
        plotStyle.setStyle(Style.ERRORLINES);

        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        for (String dataSetName : dataPointMapY1.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(getSortedPointDataSet(dataPointMapY1.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(yLable+"-"+dataSetName);
            plot.addPlot(dataSetPlot);
        }

        for (String dataSetName : dataPointMapY2.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(getSortedPointDataSet(dataPointMapY2.get(dataSetName), true));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle("-1 * " + y2Lable + "-" + dataSetName);
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel(xLable);
        plot.getAxis("y").setLabel(y2Lable + " - " + yLable);
        plot.set("xzeroaxis", "");
        plot.setTitle(plotTitle);
        GNUPlotTerminal term = new PostscriptTerminal(plotName);
        plot.setTerminal(term);
        plot.plot();
        return plot;
    }

    private static JavaPlot plotY1Y2(String plotName, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMapY1, String y2Lable, Map<String, List<Point<Number>>> dataPointMapY2) {
        JavaPlot plot = new JavaPlot();
        PlotStyle plotStyle = new PlotStyle();
//        plotStyle.setStyle(Style.LINESPOINTS);
        plotStyle.setStyle(Style.ERRORLINES);

        PlotStyle plotStyleY2 = new PlotStyle();
        plotStyleY2.setStyle(Style.LINESPOINTS);
        plotStyleY2.setLineWidth(5);
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        for (String dataSetName : dataPointMapY1.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(getSortedPointDataSet(dataPointMapY1.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(yLable+"-"+dataSetName);
            dataSetPlot.set("axes", "x1y1");
            plot.addPlot(dataSetPlot);
        }

        for (String dataSetName : dataPointMapY2.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(getSortedPointDataSet(dataPointMapY2.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyleY2);
            dataSetPlot.setTitle(y2Lable + "-" + dataSetName);
            dataSetPlot.set("axes", "x1y2");
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel(xLable);
        plot.getAxis("y").setLabel(yLable);
        plot.set("y2label", "'"+y2Lable+"'");

        plot.set("y2tics", "");
        plot.set("ytics", "nomirror");
//        plot.set("autoscale", "y2");
//        plot.set("autoscale", "y");
//        plot.set("xrange", "[0:32]");
//        plot.set("y2range", "[0:100]");
//        plot.set("yrange", "[0:100]");
//        plot.set("grid", "x y y2");
        plot.setTitle(plotTitle);
        GNUPlotTerminal term = new PostscriptTerminal(plotName);
        plot.setTerminal(term);
        plot.plot();
        return plot;
    }

    private static void plotAll(String dirName) {

        findReports(dirName);
        List<JavaPlot> plots = new ArrayList<>();
        JavaPlot plot = null;
        plot = plot(dirName + "/rate.eps", "BW All", "#Instances", "Rate (Mbps)", rateDataPointMap);
        plots.add(plot);
        plot = plot(dirName + "/rateD.eps", "BW D", "#Instances", "Rate (Mbps)", rateDiffDataPointMap);
        plots.add(plot);
        plot = plot(dirName + "/rateS.eps", "BW S", "#Instances", "Rate (Mbps)", rateSameDataPointMap);
        plots.add(plot);

        plot = plotInverse(dirName + "/cpu.eps", "CPU Utilization", "#Instances", "Rx CPU", rxCpuDataPointMap, "Tx CPU", txCpuDataPointMap);
        plots.add(plot);
        plot = plotInverse(dirName + "/cpuD.eps", "CPU Utilization D", "#Instances", "Rx CPU", rxCpuDiffDataPointMap, "Tx CPU", txCpuDiffDataPointMap);
        plots.add(plot);
        plot = plotInverse(dirName + "/cpuS.eps", "CPU Utilization S", "#Instances", "Rx CPU", rxCpuSameDataPointMap, "Tx CPU", txCpuSameDataPointMap);
        plots.add(plot);

        plot = plot(dirName + "/rtt.eps", "RTT All", "#Instances", "RTT (ms)", rttDataPointMap);
        plots.add(plot);
        plot = plot(dirName + "/rttD.eps", "RTT D", "#Instances", "RTT (ms)", rttDiffDataPointMap);
        plots.add(plot);
        plot = plot(dirName + "/rttS.eps", "RTT S", "#Instances", "RTT (ms)", rttSameDataPointMap);
        plots.add(plot);

        plot = plot(dirName + "/retrans.eps", "Retrans All", "#Instances", "# retrans", retransDataPointMap);
        plots.add(plot);
        plot = plot(dirName + "/retransD.eps", "Retrans D", "#Instances", "# retrans", retransDiffDataPointMap);
        plots.add(plot);
        plot = plot(dirName + "/retransS.eps", "Retrans S", "#Instances", "#retrans", retransSameDataPointMap);
        plots.add(plot);

        plot = plot(dirName + "/reportError.eps", "reportError All", "#Instances", "# error", reportErrorDataPointMap);
        plots.add(plot);
        plot = plot(dirName + "/reportErrorD.eps", "reportError D", "#Instances", "# error", reportErrorDiffDataPointMap);
        plots.add(plot);
        plot = plot(dirName + "/reportErrorS.eps", "reportError S", "#Instances", "# error", reportErrorSameDataPointMap);
        plots.add(plot);

        plot = plot(dirName + "/missingValue.eps", "missingValue All", "#Instances", "# missingValue", missingValueDataPointMap);
        plots.add(plot);

        plotMultiplePlots(plots);
    }

    private static void plotMultiplePlots(List<JavaPlot> plots) {
        JavaPlot plot = new JavaPlot();
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        PlotStyle plotStyle = new PlotStyle();
        plotStyle.setStyle(Style.ERRORLINES);

        JavaPlot allPlot = new JavaPlot();
//        GNUPlotTerminal term = new PostscriptTerminal("/tmp/all.eps");
//        GNUPlotTerminal term = new DefaultTerminal();
//        allPlot.setTerminal(term);
        allPlot.set("term", "postscript eps size 12,20 enhanced color");
        allPlot.set("output", "'/tmp/all.eps'");
        AutoGraphLayout layout = new AutoGraphLayout();
        layout.setColumns(3);
//        layout.setRows(5);
        allPlot.getPage().setLayout(layout);

        for (JavaPlot javaPlot : plots) {
            for (Plot dsp : javaPlot.getPlots()) {
                allPlot.addPlot(dsp);
            }
            allPlot.newGraph();
        }
        allPlot.plot();
//        GNUPlotTerminal term = new SVGTerminal("/tmp/all.svg");
//        allPlot.plot();

        //
//      for (String dataSetName : dataPointMap.keySet()) {
//          DataSetPlot dataSetPlot = new DataSetPlot(getSortedPointDataSet(dataPointMap.get(dataSetName), false));
//          dataSetPlot.setPlotStyle(plotStyle);
//          dataSetPlot.setTitle(dataSetName);
//          plot.addPlot(dataSetPlot);
//      }
//
//      plot.setKey(JavaPlot.Key.TOP_RIGHT);
//      plot.getAxis("x").setLabel(xLable);
//      plot.getAxis("y").setLabel(yLable);
//      plot.set("xzeroaxis", "");
//      plot.setTitle(plotTitle);
//      GNUPlotTerminal term = new PostscriptTerminal(plotName);
//      plot.setTerminal(term);
//      plot.plot();

//      double[][] original1 = {{2,3},{4,5},{6,7}};
//      double[][] original2 = {{8,9},{12,13},{14,15}};
//      AbstractPlot originalPlot = new DataSetPlot(original1);
//      originalPlot.setTitle("original1");
//      AbstractPlot originalPlot2 = new DataSetPlot(original2);
//      originalPlot2.setTitle("original2");
//
//
//      JavaPlot p = new JavaPlot();
//
//      p.addPlot(originalPlot);
//      p.newGraph();
//      p.addPlot(originalPlot2);
//      p.setTerminal(new PostscriptTerminal("/tmp/all.eps"));
//      p.plot();

    }

    public static String getDataSetName(int classValue, int classNetNum) {
        return "class:"+classValue+"-nets:"+classNetNum;
    }

    public static boolean areClassesConcurrent(String reportFileName){
        return reportFileName.matches(".*classes.*\\[con=true\\].*\\[con=.*");
    }

    public static boolean areInstancesConcurrent(String reportFileName){
        return reportFileName.matches(".*classes.*\\[con=.*\\].*\\[con=true\\].*");
    }

    public static int[] getClasses(String reportFileName){
        String[] classes = reportFileName.split("\\]\\[")[0].split("\\[")[1].split(",");
        int[] c = new int[classes.length];

        for (int i = 0; i < classes.length; i++) {
            c[i] = Integer.parseInt(classes[i].trim());
        }
        return c;
    }

    public static int getNetNum(String reportFileName) {
        return Integer.parseInt(reportFileName.split("nets")[1].split("-")[0]);
    }

    public static int getInsNum(String reportFileName) {
        return Integer.parseInt(reportFileName.split("instances")[1].split("\\[")[0]);
    }
}
