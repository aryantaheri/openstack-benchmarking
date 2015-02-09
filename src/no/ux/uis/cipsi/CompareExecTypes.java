package no.ux.uis.cipsi;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.opendaylight.controller.samples.differentiatedforwarding.openstack.ReportManager;
import org.opendaylight.controller.samples.differentiatedforwarding.openstack.performance.BwExpReport;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.Point;

public class CompareExecTypes {

    public static Map<String, List<JavaPlot>> summaryPlotMap = new HashMap<>();

    private static Map<String, List<Point<Number>>> rateDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> rateDiffDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> rateSameDataPointMap = new HashMap<>();

    private static Map<String, List<Point<Number>>> cpuRxDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> cpuRxDiffDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> cpuRxSameDataPointMap = new HashMap<>();

    private static Map<String, List<Point<Number>>> cpuTxDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> cpuTxDiffDataPointMap = new HashMap<>();
    private static Map<String, List<Point<Number>>> cpuTxSameDataPointMap = new HashMap<>();

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

    private static final double BOX_WIDTH = 0.15;
    public static String[] strategies = {"none", "meter", "queue", "meter_queue"};
//    private static String[] strategies = {"meter"};
    public static void main(String[] args) {

        String dirPath = "/home/aryan/data/10min";

        for (String strategy : strategies) {
            initDataPointMaps();
            List<File> expFiles = loadReports(dirPath, strategy);
            System.out.println("[strategy=" + strategy  + "] expFiles -> " + expFiles);
            processStrategyFiles(expFiles, strategy);
            plotStrategy(dirPath, strategy);
        }
    }



    private static void initDataPointMaps() {
        rateDataPointMap = new HashMap<>();
        rateDiffDataPointMap = new HashMap<>();
        rateSameDataPointMap = new HashMap<>();

        cpuRxDataPointMap = new HashMap<>();
        cpuRxDiffDataPointMap = new HashMap<>();
        cpuRxSameDataPointMap = new HashMap<>();

        cpuTxDataPointMap = new HashMap<>();
        cpuTxDiffDataPointMap = new HashMap<>();
        cpuTxSameDataPointMap = new HashMap<>();

        rttDataPointMap = new HashMap<>();
        rttDiffDataPointMap = new HashMap<>();
        rttSameDataPointMap = new HashMap<>();

        retransDataPointMap = new HashMap<>();
        retransDiffDataPointMap = new HashMap<>();
        retransSameDataPointMap = new HashMap<>();

        reportErrorDataPointMap = new HashMap<>();
        reportErrorDiffDataPointMap = new HashMap<>();
        reportErrorSameDataPointMap = new HashMap<>();

        missingValueDataPointMap = new HashMap<>();

    }

    private static List<File> loadReports(String dirPath, String strategy) {
        File rootDir = getStrategyRootDir(dirPath, strategy);
        System.out.println("[strategy=" + strategy  + "] rootDir: " + rootDir);
        List<File> expFiles = new ArrayList<>();

        File[] dirs = rootDir.listFiles();
        System.out.println("[strategy=" + strategy  + "] expDirs: " + Arrays.toString(dirs));

        FileFilter fileFilter = new WildcardFileFilter("*.obj");
        for (File expDir : dirs) {

            File[] files = expDir.listFiles(fileFilter);
            if (files == null) continue;

            List<File> fileList = Arrays.asList(files);
            Collections.sort(fileList);
            expFiles.addAll(fileList);

        }
        return expFiles;
    }

    private static void processStrategyFiles(List<File> expFiles, String strategy) {
        for (File bwExpReportFile : expFiles) {
            processFile(bwExpReportFile, strategy);
        }
    }

    private static void processFile(File bwExpReportsFile, String strategy){
        List<BwExpReport> bwExpReports = ReportManager.readReportObjects(bwExpReportsFile.getAbsolutePath());
        boolean classConsurrency = ClassBasedProcessor.areClassesConcurrent(bwExpReportsFile.getName());
        boolean instanceConsurrency = ClassBasedProcessor.areInstancesConcurrent(bwExpReportsFile.getName());;
        int[] classes = ClassBasedProcessor.getClasses(bwExpReportsFile.getName());
        int netNum = ClassBasedProcessor.getNetNum(bwExpReportsFile.getName());
        int insNum = ClassBasedProcessor.getInsNum(bwExpReportsFile.getName());
        System.out.println(bwExpReportsFile.getName());
        System.out.println(classConsurrency);
        System.out.println(instanceConsurrency);
        System.out.println(Arrays.toString(classes));
        System.out.println(netNum);
        System.out.println(insNum);


        for (BwExpReport bwExpReport : bwExpReports) {
            setExecTypeDataPoins(bwExpReport);
        }

    }


    private static void setExecTypeDataPoins(BwExpReport bwExpReport) {
        String dataSetName = getDataSetName(bwExpReport);
        double xOffset = calcXOffset(bwExpReport);

        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getRateStats(), rateDataPointMap);
        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getRateStatsDiffHyper(), rateDiffDataPointMap);
        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getRateStatsSameHyper(), rateSameDataPointMap);

        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getCpuRxStats(), cpuRxDataPointMap);
        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getCpuRxStatsDiffHyper(), cpuRxDiffDataPointMap);
        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getCpuRxStatsSameHyper(), cpuRxSameDataPointMap);

        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getCpuTxStats(), cpuTxDataPointMap);
        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getCpuTxStatsDiffHyper(), cpuTxDiffDataPointMap);
        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getCpuTxStatsSameHyper(), cpuTxSameDataPointMap);

        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getRttStats(), rttDataPointMap);
        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getRttStatsDiffHyper(), rttDiffDataPointMap);
        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getRttStatsSameHyper(), rttSameDataPointMap);

        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getRetransStats(), retransDataPointMap);
        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getRetransStatsDiffHyper(), retransDiffDataPointMap);
        Utils.addDataPointsWithMinMax(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getRetransStatsSameHyper(), retransSameDataPointMap);

        Utils.addBoxDataPoint(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getReportErrorCount(), reportErrorDataPointMap);
        Utils.addBoxDataPoint(dataSetName, bwExpReport.getClassValue(), xOffset, BOX_WIDTH, bwExpReport.getMissingValueCount(), missingValueDataPointMap);

    }

    private static void plotStrategy(String dirPath, String strategy) {
        File dir = getStrategyRootDir(dirPath, strategy);
        String plotPrefix = "CompareExec-"+strategy+"-";
        List<JavaPlot> plots = new ArrayList<>();
        List<JavaPlot> summaryPlots = new ArrayList<>();

        JavaPlot plot = null;
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "rate.eps", "BW All (Strategy=" + strategy + ")", "Class", "Rate (Mbps)", rateDataPointMap);
        plots.add(plot);
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "rateD.eps", "BW D (Strategy=" + strategy + ")", "Class", "Rate (Mbps)", rateDiffDataPointMap);
        plots.add(plot);
        summaryPlots.add(plot);
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix,  "rateS.eps", "BW S (Strategy=" + strategy + ")", "Class", "Rate (Mbps)", rateSameDataPointMap);
        plots.add(plot);

        plot = Plotter.plotInverseBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "cpu.eps", "CPU Utilization (Strategy=" + strategy + ")", "Class", "Rx CPU", cpuRxDataPointMap, "Tx CPU", cpuTxDataPointMap);
        plots.add(plot);
        plot = Plotter.plotInverseBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "cpuD.eps", "CPU Utilization D (Strategy=" + strategy + ")", "Class", "Rx CPU", cpuRxDiffDataPointMap, "Tx CPU", cpuTxDiffDataPointMap);
        plots.add(plot);
        summaryPlots.add(plot);
        plot = Plotter.plotInverseBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "cpuS.eps", "CPU Utilization S (Strategy=" + strategy + ")", "Class", "Rx CPU", cpuRxSameDataPointMap, "Tx CPU", cpuTxSameDataPointMap);
        plots.add(plot);

        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "rtt.eps", "RTT All (Strategy=" + strategy + ")", "Class", "RTT (ms)", rttDataPointMap);
        plots.add(plot);
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "rttD.eps", "RTT D (Strategy=" + strategy + ")", "Class", "RTT (ms)", rttDiffDataPointMap);
        plots.add(plot);
        summaryPlots.add(plot);
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "rttS.eps", "RTT S (Strategy=" + strategy + ")", "Class", "RTT (ms)", rttSameDataPointMap);
        plots.add(plot);

        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "retrans.eps", "Retrans All (Strategy=" + strategy + ")", "Class", "# retrans", retransDataPointMap);
        plots.add(plot);
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "retransD.eps", "Retrans D (Strategy=" + strategy + ")", "Class", "# retrans", retransDiffDataPointMap);
        plots.add(plot);
        summaryPlots.add(plot);
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "retransS.eps", "Retrans S (Strategy=" + strategy + ")", "Class", "#retrans", retransSameDataPointMap);
        plots.add(plot);

        plot = Plotter.plotBox(dir.getAbsolutePath(), plotPrefix, "reportError.eps", "reportError All (Strategy=" + strategy + ")", "Class", "# error", reportErrorDataPointMap);
        plots.add(plot);
        summaryPlots.add(plot);

//        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "reportErrorD.eps", "reportError D (Strategy=" + strategy + ")", "Class", "# error", reportErrorDiffDataPointMap);
//        plots.add(plot);
//        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "reportErrorS.eps", "reportError S (Strategy=" + strategy + ")", "Class", "# error", reportErrorSameDataPointMap);
//        plots.add(plot);

        plot = Plotter.plotBox(dir.getAbsolutePath(), plotPrefix, "missingValue.eps", "missingValue All (Strategy=" + strategy + ")", "Class", "# missingValue", missingValueDataPointMap);
        plots.add(plot);
        summaryPlots.add(plot);

        summaryPlotMap.put(strategy, summaryPlots);
        Plotter.plotMultipleBoxPlots(dir.getAbsolutePath(), plotPrefix, "all.eps", plots);
    }




    private static String getDataSetName(BwExpReport bwExpReport) {
        return "c="+bwExpReport.isRunClassExpConcurrently() + ",i="+bwExpReport.isRunInstanceExpConcurrently();
    }

    private static double calcXOffset(BwExpReport bwExpReport) {
        int offset = 0;
        double scale = BOX_WIDTH;
        if (bwExpReport.isRunClassExpConcurrently()){
            offset -= 2;
        } else {
            offset = 0;
        }
        if (bwExpReport.isRunInstanceExpConcurrently()){
            offset++;
        } else {
//            offset--;
        }
        return offset * scale;
    }

    public static File getStrategyRootDir(String dirPath, String strategy){
        File rootDir = new File(dirPath + "/strategy="+strategy);
        return rootDir;
    }
}
