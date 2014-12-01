package no.ux.uis.cipsi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.opendaylight.controller.samples.differentiatedforwarding.openstack.ReportManager;
import org.opendaylight.controller.samples.differentiatedforwarding.openstack.performance.BwExpReport;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.Point;
import com.panayotis.gnuplot.dataset.PointDataSet;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.GNUPlotTerminal;
import com.panayotis.gnuplot.terminal.PostscriptTerminal;

public class Processor {

    public static void main(String[] args) {
//        try {
////            GNUPlot gnuPlot = new GNUPlot();
////            Plot plot = new FunctionPlot("sin(x)");
////            gnuPlot.setPersist(true);
////            GNUPlotTerminal term = new FileTerminal("x11", "/tmp/term");
////            GNUPlotTerminal term = new ImageTerminal();
////            GNUPlotTerminal term = new SVGTerminal("/tmp/tmp.svg");
//            GNUPlotTerminal term = new PostscriptTerminal("/tmp/ps.eps");
////            gnuPlot.setTerminal(term);
////            gnuPlot.addPlot(plot);
////            gnuPlot.plot();
//
//            JavaPlot javaPlot = new JavaPlot();
//            javaPlot.addPlot("sin(x)");
//            javaPlot.setPersist(true);
//            javaPlot.setTerminal(term);
//            javaPlot.newGraph();
//            javaPlot.plot();
//            System.out.println(term.getOutputFile());
//            System.out.println("WTF");
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        plot(getData());
        processFiles();
    }

    static double[][] myds = new double[100][2];

    private static double[][][] getData(){
        Object obj = ToStringStyle.SIMPLE_STYLE;
        double[][] ds1 = {{1,1},{2,2},{4,3},{8,4},{16,5}};
        double[][] ds2 = {{1,6},{2,7},{4,8},{8,9},{16,10}};
        double[][] ds3 = {{1,11},{2,12},{4,13},{8,14},{16,15}};
        double[][][] ds = {ds1, ds2, ds3};
        return ds;
    }

    private static void processFiles() {
        String[] files = {"/tmp/classes[1][con=true]-nets1-instances1[con=true]-20141126-182108.obj",
                            "/tmp/classes[1][con=true]-nets1-instances2[con=true]-20141126-182532.obj",
                            "/tmp/classes[1][con=true]-nets1-instances4[con=true]-20141126-183042.obj",
                            "/tmp/classes[1][con=true]-nets1-instances8[con=true]-20141126-183754.obj",
                            "/tmp/classes[1][con=true]-nets1-instances16[con=true]-20141126-185314.obj",
                            "/tmp/classes[1][con=true]-nets1-instances32[con=true]-20141127-200709.obj"//,
//                            "/tmp/classes[1][con=true]-nets2-instances2[con=true]-20141126-192111.obj",
//                            "/tmp/classes[1][con=true]-nets2-instances4[con=true]-20141126-195245.obj",
//                            "/tmp/classes[1][con=true]-nets2-instances8[con=true]-20141126-202602.obj",
//                            "/tmp/classes[1][con=true]-nets2-instances16[con=true]-20141126-210233.obj",
//                            "/tmp/classes[1][con=true]-nets2-instances32[con=true]-20141126-215655.obj",
//                            "/tmp/classes[1][con=true]-nets4-instances4[con=true]-20141126-233826.obj",
//                            "/tmp/classes[1][con=true]-nets4-instances8[con=true]-20141127-001130.obj",
//                            "/tmp/classes[1][con=true]-nets4-instances16[con=true]-20141127-004819.obj",
//                            "/tmp/classes[1][con=true]-nets4-instances32[con=true]-20141127-013306.obj",
//                            "/tmp/classes[1][con=true]-nets8-instances8[con=true]-20141127-030103.obj",
//                            "/tmp/classes[1][con=true]-nets8-instances16[con=true]-20141127-033838.obj",
//                            "/tmp/classes[1][con=true]-nets8-instances32[con=true]-20141127-042343.obj"
                            };
        int dsIndex = 0;
        for (String file : files) {
            List<BwExpReport> reports = ReportManager.readReportObjects(file);
            plotBwExpReports(reports, dsIndex);
            dsIndex++;
        }
//        plot();
        plotDps();
    }

    static PointDataSet<Number> pds = new PointDataSet<>();
    private static void plotBwExpReports(List<BwExpReport> reports, int dsIndex) {

        DescriptiveStatistics rateStats = new DescriptiveStatistics();
        int reachableInstances = 0; // x axis
        int networks = 0;
        for (BwExpReport bwExpReport : reports) {
            reachableInstances += bwExpReport.getReachableInstances().size();
            networks++;
            for (double value : bwExpReport.getRateStatsDiffHyper().getValues()) {
                rateStats.addValue(value);
            }
        }
        System.out.println("dsIndex:" + dsIndex + " ins:" + reachableInstances + " value:" + rateStats.getMean());
        myds[dsIndex][0] = reachableInstances;
        myds[dsIndex][1] = rateStats.getMean();
        Point<Number> dataPoint = new Point(reachableInstances, rateStats.getMean());
        pds.add(dataPoint);
    }
    private static void plotDps() {
        JavaPlot plot = new JavaPlot();
        PlotStyle plotStyle = new PlotStyle();
        plotStyle.setStyle(Style.LINESPOINTS);
        for (int i = 0; i < pds.size(); i++) {
            System.out.println(pds.get(i));
        }

//        dataSetPlot.setPlotStyle(plotStyle);
//        dataSetPlot.setTitle("myds");
        DataSetPlot dataSetPlot = new DataSetPlot(pds);
        dataSetPlot.setPlotStyle(plotStyle);
        dataSetPlot.setTitle("myds");
        plot.addPlot(dataSetPlot);

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel("#Instances");
        plot.getAxis("y").setLabel("Rate (Mbps)");
        plot.setTitle("Bw Stats");
        GNUPlotTerminal term = new PostscriptTerminal("/tmp/ps.eps");
        plot.setTerminal(term);
        plot.plot();

    }
    private static void plot() {
        JavaPlot plot = new JavaPlot();
        PlotStyle plotStyle = new PlotStyle();
        plotStyle.setStyle(Style.LINESPOINTS);
        for (int i = 0; i < myds.length; i++) {
            System.out.println(Arrays.toString(myds[i]));
        }

        DataSetPlot dataSetPlot = new DataSetPlot(myds);
        dataSetPlot.setPlotStyle(plotStyle);
        dataSetPlot.setTitle("myds");

        plot.addPlot(dataSetPlot);

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel("#Instances");
        plot.getAxis("y").setLabel("Rate (Mbps)");
        plot.setTitle("Bw Stats");
        GNUPlotTerminal term = new PostscriptTerminal("/tmp/ps.eps");
        plot.setTerminal(term);
        plot.plot();

    }

    private static void plot(double[][][] ds) {
        JavaPlot plot = new JavaPlot();
//        DataSe
        PlotStyle plotStyle = new PlotStyle();
        plotStyle.setStyle(Style.LINESPOINTS);

        for (int i = 0; i < ds[0].length; i++) {
            System.out.println(Arrays.toString(ds[0][i]));
        }
        DataSetPlot dataSetPlot = new DataSetPlot(ds[0]);
        dataSetPlot.setPlotStyle(plotStyle);
        dataSetPlot.setTitle("ds0");

        DataSetPlot dataSetPlot2 = new DataSetPlot(ds[1]);
        dataSetPlot2.setPlotStyle(plotStyle);
        dataSetPlot2.setTitle("ds1");

        DataSetPlot dataSetPlot3 = new DataSetPlot(ds[2]);
        dataSetPlot3.setPlotStyle(plotStyle);
        dataSetPlot3.setTitle("ds2");

        plot.addPlot(dataSetPlot);
        plot.addPlot(dataSetPlot2);
        plot.addPlot(dataSetPlot3);

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel("#Instances");
        plot.getAxis("y").setLabel("Rate (Mbps)");
        plot.setTitle("Bw Stats");
        GNUPlotTerminal term = new PostscriptTerminal("/tmp/ps.eps");
        plot.setTerminal(term);
        plot.plot();
    }
}
