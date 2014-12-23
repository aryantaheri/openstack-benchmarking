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

import com.panayotis.gnuplot.dataset.Point;

public class CompareExecTypes {

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

//    private static String[] strategies = {"none", "meter", "queue", "meter_queue"};
    private static String[] strategies = {"meter"};
    public static void main(String[] args) {
        for (String strategy : strategies) {
            List<File> expFiles = loadReports("/home/aryan/data/useful", strategy);
            System.out.println("[strategy=" + strategy  + "] expFiles -> " + expFiles);
            processStrategyFiles(expFiles, strategy);
        }
    }


    private static List<File> loadReports(String dirPath, String strategy) {
        File rootDir = new File(dirPath + "/strategy="+strategy);
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

        Map<Integer, List<BwExpReport>> classGroups = new HashMap<Integer, List<BwExpReport>>();

        for (BwExpReport bwExpReport : bwExpReports) {
            int classValue = bwExpReport.getClassValue();
            setExecTypeDataPoins(bwExpReport);
        }

//        setClassDataPoints(classGroups);
    }


    private static void setExecTypeDataPoins(BwExpReport bwExpReport) {
        String dataSetName = getDataSetName(bwExpReport);

        Utils.addDataPoints(dataSetName, bwExpReport.getClassValue(), bwExpReport.getRateStats(), rateDataPointMap);
        Utils.addDataPoints(dataSetName, bwExpReport.getClassValue(), bwExpReport.getRateStatsDiffHyper(), rateDiffDataPointMap);
        Utils.addDataPoints(dataSetName, bwExpReport.getClassValue(), bwExpReport.getRateStatsSameHyper(), rateSameDataPointMap);

        Utils.addDataPoints(dataSetName, bwExpReport.getClassValue(), bwExpReport.getCpuRxStats(), cpuRxDataPointMap);
        Utils.addDataPoints(dataSetName, bwExpReport.getClassValue(), bwExpReport.getCpuRxStatsDiffHyper(), cpuRxDiffDataPointMap);
        Utils.addDataPoints(dataSetName, bwExpReport.getClassValue(), bwExpReport.getCpuRxStatsSameHyper(), cpuRxSameDataPointMap);

        Utils.addDataPoints(dataSetName, bwExpReport.getClassValue(), bwExpReport.getCpuTxStats(), cpuTxDataPointMap);
        Utils.addDataPoints(dataSetName, bwExpReport.getClassValue(), bwExpReport.getCpuTxStatsDiffHyper(), cpuTxDiffDataPointMap);
        Utils.addDataPoints(dataSetName, bwExpReport.getClassValue(), bwExpReport.getCpuTxStatsSameHyper(), cpuTxSameDataPointMap);
    }

    private static String getDataSetName(BwExpReport bwExpReport) {
        return "c="+bwExpReport.isRunClassExpConcurrently() + ",i="+bwExpReport.isRunInstanceExpConcurrently();
    }


}
