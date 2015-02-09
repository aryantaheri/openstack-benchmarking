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

public class CompareStrategyTypes {

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

    public static void main(String[] args) {

        String dirPath = "/home/aryan/data/10min";
        boolean[] classExecTypes = {false, true};
        boolean[] instanceExecTypes = {false, true};
        for (boolean c : classExecTypes) {
            for (boolean i : instanceExecTypes) {
                initDataPointMaps();
                List<File> expFiles = loadReports(dirPath, c, i);
                processExecTypeFiles(expFiles, c, i);
                plotExecType(dirPath, c, i);
            }
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

    private static List<File> loadReports(String dirPath, boolean classConcurrency, boolean instanceConcurrency) {
        List<File> expFiles = new ArrayList<>();
        File[] rootDirs = new File[CompareExecTypes.strategies.length];
        for (int i = 0; i < CompareExecTypes.strategies.length; i++) {
            rootDirs[i] = CompareExecTypes.getStrategyRootDir(dirPath, CompareExecTypes.strategies[i]);
            File[] dirs = rootDirs[i].listFiles();
            FileFilter fileFilter = new WildcardFileFilter("classes*[con=" + classConcurrency + "]*[con=" + instanceConcurrency +"]*.obj");
            for (File expDir : dirs) {

                File[] files = expDir.listFiles(fileFilter);
                if (files == null) continue;

                List<File> fileList = Arrays.asList(files);
                Collections.sort(fileList);
                expFiles.addAll(fileList);

            }
        }
        System.out.println("[c=" + classConcurrency  + ", i=" + instanceConcurrency + "] expFiles -> " + expFiles);
        return expFiles;
    }

    private static void processExecTypeFiles(List<File> expFiles, boolean classConcurrency, boolean instanceConcurrency) {
        for (File bwExpReportFile : expFiles) {
            processFile(bwExpReportFile, classConcurrency, instanceConcurrency);
        }
    }

    private static void processFile(File bwExpReportsFile, boolean classConcurrency, boolean instanceConcurrency) {
        List<BwExpReport> bwExpReports = ReportManager.readReportObjects(bwExpReportsFile.getAbsolutePath());
        boolean classC = ClassBasedProcessor.areClassesConcurrent(bwExpReportsFile.getName());
        boolean instanceC = ClassBasedProcessor.areInstancesConcurrent(bwExpReportsFile.getName());;
        int[] classes = ClassBasedProcessor.getClasses(bwExpReportsFile.getName());
        int netNum = ClassBasedProcessor.getNetNum(bwExpReportsFile.getName());
        int insNum = ClassBasedProcessor.getInsNum(bwExpReportsFile.getName());
        String strategy = getStrategy(bwExpReportsFile);
        System.out.println(bwExpReportsFile.getName());
        System.out.println(classC);
        System.out.println(instanceC);
        System.out.println(Arrays.toString(classes));
        System.out.println(netNum);
        System.out.println(insNum);
        System.out.println("Strategy=" + strategy);
        if (classC != classConcurrency || instanceC != instanceConcurrency) {
            System.err.println("Inconsistency between classConcurrency and instanceConcurrency of inputs");
            System.exit(-1);
        }

        for (BwExpReport bwExpReport : bwExpReports) {
            setStrategyDataPoins(bwExpReport, strategy);
        }
    }

    private static void setStrategyDataPoins(BwExpReport bwExpReport, String strategy) {
        String dataSetName = getDataSetName(bwExpReport, strategy);
        double xOffset = calcXOffset(bwExpReport, strategy);

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

    private static void plotExecType(String dirPath, boolean classConcurrency, boolean instanceConcurrency) {
        File dir = new File(dirPath);
        String execType = "c:"+classConcurrency+",i:" + instanceConcurrency;
        String plotPrefix = "CompareStrategy-"+ execType +"-";
        List<JavaPlot> plots = new ArrayList<>();
        List<JavaPlot> summaryPlots = new ArrayList<>();
        JavaPlot plot = null;
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "rate.eps", "BW All (ExecType=" + execType + ")", "Class", "Rate (Mbps)", rateDataPointMap);
        plots.add(plot);
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "rateD.eps", "BW D (ExecType=" + execType + ")", "Class", "Rate (Mbps)", rateDiffDataPointMap);
        plots.add(plot);
        summaryPlots.add(plot);
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix,  "rateS.eps", "BW S (ExecType=" + execType + ")", "Class", "Rate (Mbps)", rateSameDataPointMap);
        plots.add(plot);

        plot = Plotter.plotInverseBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "cpu.eps", "CPU Utilization (ExecType=" + execType + ")", "Class", "Rx CPU", cpuRxDataPointMap, "Tx CPU", cpuTxDataPointMap);
        plots.add(plot);
        plot = Plotter.plotInverseBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "cpuD.eps", "CPU Utilization D (ExecType=" + execType + ")", "Class", "Rx CPU", cpuRxDiffDataPointMap, "Tx CPU", cpuTxDiffDataPointMap);
        plots.add(plot);
        summaryPlots.add(plot);
        plot = Plotter.plotInverseBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "cpuS.eps", "CPU Utilization S (ExecType=" + execType + ")", "Class", "Rx CPU", cpuRxSameDataPointMap, "Tx CPU", cpuTxSameDataPointMap);
        plots.add(plot);

        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "rtt.eps", "RTT All (ExecType=" + execType + ")", "Class", "RTT (ms)", rttDataPointMap);
        plots.add(plot);
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "rttD.eps", "RTT D (ExecType=" + execType + ")", "Class", "RTT (ms)", rttDiffDataPointMap);
        plots.add(plot);
        summaryPlots.add(plot);
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "rttS.eps", "RTT S (ExecType=" + execType + ")", "Class", "RTT (ms)", rttSameDataPointMap);
        plots.add(plot);

        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "retrans.eps", "Retrans All (ExecType=" + execType + ")", "Class", "# retrans", retransDataPointMap);
        plots.add(plot);
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "retransD.eps", "Retrans D (ExecType=" + execType + ")", "Class", "# retrans", retransDiffDataPointMap);
        plots.add(plot);
        summaryPlots.add(plot);
        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "retransS.eps", "Retrans S (ExecType=" + execType + ")", "Class", "#retrans", retransSameDataPointMap);
        plots.add(plot);

        plot = Plotter.plotBox(dir.getAbsolutePath(), plotPrefix, "reportError.eps", "reportError All (ExecType=" + execType + ")", "Class", "# error", reportErrorDataPointMap);
        plots.add(plot);
        summaryPlots.add(plot);

//        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "reportErrorD.eps", "reportError D (ExecType=" + execType + ")", "Class", "# error", reportErrorDiffDataPointMap);
//        plots.add(plot);
//        plot = Plotter.plotBoxWithMinMax(dir.getAbsolutePath(), plotPrefix, "reportErrorS.eps", "reportError S (ExecType=" + execType + ")", "Class", "# error", reportErrorSameDataPointMap);
//        plots.add(plot);

        plot = Plotter.plotBox(dir.getAbsolutePath(), plotPrefix, "missingValue.eps", "missingValue All (ExecType=" + execType + ")", "Class", "# missingValue", missingValueDataPointMap);
        plots.add(plot);
        summaryPlots.add(plot);
        summaryPlotMap.put(execType, summaryPlots);
        Plotter.plotMultipleBoxPlots(dir.getAbsolutePath(), plotPrefix, "all.eps", plots);
    }

    private static String getDataSetName(BwExpReport bwExpReport, String strategy) {
        return strategy;
    }

    private static String getStrategy(File bwExpReportsFile) {
        for (int i = 0; i < CompareExecTypes.strategies.length; i++) {
            if (bwExpReportsFile.getAbsolutePath().contains("strategy="+CompareExecTypes.strategies[i]+"/")){
                return CompareExecTypes.strategies[i];
            }
        }
        return null;
    }

    private static double calcXOffset(BwExpReport bwExpReport, String strategy) {
        double scale = BOX_WIDTH;
        for (int i = 0; i < CompareExecTypes.strategies.length; i++) {
            if (strategy.equalsIgnoreCase(CompareExecTypes.strategies[i])){
                return (i - CompareExecTypes.strategies.length/2) * scale;
            }
        }
        return (CompareExecTypes.strategies.length/2 + 0.5) * scale;
    }
}
