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
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.GNUPlotTerminal;
import com.panayotis.gnuplot.terminal.PostscriptTerminal;

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
        String dirName = "/tmp/exp-20141128-200441";
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
            for (BwExpReport bwExpReport : classReports) {
                netInsNum += bwExpReport.getReachableInstances().size();

                for (BwReport report : bwExpReport.getBwReports()) {
                    rateStats.addValue(report.getRate());
                    cpuRxStats.addValue(report.getReceiverCpu());
                    cpuTxStats.addValue(report.getTransmitterCpu());
                    rttStats.addValue(report.getRtt());
                    retransStats.addValue(report.getRetrans());

                    if (report.getReceiverHost().equalsIgnoreCase(report.getTransmitterHost())){
                        rateStatsSameHyper.addValue(report.getRate());
                        cpuRxStatsSameHyper.addValue(report.getReceiverCpu());
                        cpuTxStatsSameHyper.addValue(report.getTransmitterCpu());
                        rttStatsSameHyper.addValue(report.getRtt());
                        retransStatsSameHyper.addValue(report.getRetrans());

                    } else {
                        rateStatsDiffHyper.addValue(report.getRate());
                        cpuRxStatsDiffHyper.addValue(report.getReceiverCpu());
                        cpuTxStatsDiffHyper.addValue(report.getTransmitterCpu());
                        rttStatsDiffHyper.addValue(report.getRtt());
                        retransStatsDiffHyper.addValue(report.getRetrans());
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

    private static PointDataSet<Number> getSortedPointDataSet(List<Point<Number>> pointListOrig, boolean inverse) {
        PointDataSet<Number> points = new PointDataSet<>();
        List<Point<Number>> pointList = new ArrayList<>();
//        if (inverse){
//            pointList = Lists.transform(pointListOrig, new Function<Point<Number>, Point<Number>>() {
//                @Override
//                public Point<Number> apply(Point<Number> point) {
//                    return new Point<Number>(point.get(0), (-1 * point.get(1).doubleValue()));
//                }
//            });
//        } else {
//            pointList = pointListOrig;
//        }
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

    private static void plot(String plotName, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMap) {
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
    }

    private static void plotInverse(String plotName, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMapY1, String y2Lable, Map<String, List<Point<Number>>> dataPointMapY2) {
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
    }

    private static void plot(String plotName, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMapY1, String y2Lable, Map<String, List<Point<Number>>> dataPointMapY2) {
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
    }

    private static String getDataSetName(int classValue, int classNetNum) {
        return "class:"+classValue+"-nets:"+classNetNum;
    }

    private static boolean areClassesConcurrent(String reportFileName){
        return reportFileName.matches(".*classes.*\\[con=true\\].*\\[con=.*");
    }

    private static boolean areInstancesConcurrent(String reportFileName){
        return reportFileName.matches(".*classes.*\\[con=.*\\].*\\[con=true\\].*");
    }

    private static int[] getClasses(String reportFileName){
        String[] classes = reportFileName.split("\\]\\[")[0].split("\\[")[1].split(",");
        int[] c = new int[classes.length];

        for (int i = 0; i < classes.length; i++) {
            c[i] = Integer.parseInt(classes[i].trim());
        }
        return c;
    }

    private static int getNetNum(String reportFileName) {
        return Integer.parseInt(reportFileName.split("nets")[1].split("-")[0]);
    }

    private static int getInsNum(String reportFileName) {
        return Integer.parseInt(reportFileName.split("instances")[1].split("\\[")[0]);
    }
}
