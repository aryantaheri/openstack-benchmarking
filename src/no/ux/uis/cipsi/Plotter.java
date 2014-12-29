package no.ux.uis.cipsi;

import java.util.List;
import java.util.Map;

import com.panayotis.gnuplot.GNUPlotParameters;
import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.Point;
import com.panayotis.gnuplot.layout.AutoGraphLayout;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.plot.Plot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.GNUPlotTerminal;
import com.panayotis.gnuplot.terminal.PostscriptTerminal;

public class Plotter {


    public static JavaPlot plotBoxWithMinMax(String plotDir, String plotPrefix, String plotSuffix, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMap) {
        String plotName = plotDir + "/" + plotPrefix + plotSuffix;
        JavaPlot plot = new JavaPlot();
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        PlotStyle plotStyle = new PlotStyle();
//        plotStyle.setStyle(Style.LINESPOINTS);
        plotStyle.setStyle(Style.BOXERRORBARS);

        for (String dataSetName : dataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(Utils.getSortedPointDataSetWithMinMaxForBox(dataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(dataSetName);
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel(xLable);
        plot.getAxis("y").setLabel(yLable);
        plot.set("xzeroaxis", "");
//        plot.set("boxwidth",  "0.1 relative ");
        plot.set("style", "data boxes");
        plot.set("style", "fill pattern 1 border");
        plot.set("xtics", "1");
        plot.set("xrange", "[0:5]");
//        plot.set("xtics", "border in scale 0 nomirror rotate by -45  autojustify");
        plot.setTitle(plotTitle);
        GNUPlotTerminal term = new PostscriptTerminal(plotName);
        plot.setTerminal(term);
        plot.plot();
        return plot;
    }

    public static JavaPlot plotInverseBoxWithMinMax(String plotDir, String plotPrefix, String plotSuffix, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMapY1, String y2Lable, Map<String, List<Point<Number>>> dataPointMapY2) {
        String plotName = plotDir + "/" + plotPrefix + plotSuffix;
        JavaPlot plot = new JavaPlot();
        PlotStyle plotStyle = new PlotStyle();
        plotStyle.setStyle(Style.BOXERRORBARS);

        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        for (String dataSetName : dataPointMapY1.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(Utils.getSortedPointDataSetWithMinMaxForBox(dataPointMapY1.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(yLable+"-"+dataSetName);
            plot.addPlot(dataSetPlot);
        }

        for (String dataSetName : dataPointMapY2.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(Utils.getSortedPointDataSetWithMinMaxForBox(dataPointMapY2.get(dataSetName), true));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle("-1 * " + y2Lable + "-" + dataSetName);
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel(xLable);
        plot.getAxis("y").setLabel(y2Lable + " - " + yLable);
        plot.set("xzeroaxis", "");
        plot.set("style", "data boxes");
        plot.set("style", "fill pattern 1 border");
        plot.set("xtics", "1");
        plot.set("xrange", "[0:5]");
        plot.setTitle(plotTitle);
        GNUPlotTerminal term = new PostscriptTerminal(plotName);
        plot.setTerminal(term);
        plot.plot();
        return plot;
    }

    public static JavaPlot plotBox(String plotDir, String plotPrefix, String plotSuffix, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMap) {
        String plotName = plotDir + "/" + plotPrefix + plotSuffix;
        JavaPlot plot = new JavaPlot();
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        PlotStyle plotStyle = new PlotStyle();
//        plotStyle.setStyle(Style.LINESPOINTS);
        plotStyle.setStyle(Style.BOXES);

        for (String dataSetName : dataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(Utils.getSortedPointDataSetForBox(dataPointMap.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(dataSetName);
            plot.addPlot(dataSetPlot);
        }

        plot.setKey(JavaPlot.Key.TOP_RIGHT);
        plot.getAxis("x").setLabel(xLable);
        plot.getAxis("y").setLabel(yLable);
        plot.set("xzeroaxis", "");
//        plot.set("boxwidth",  "0.1 relative ");
        plot.set("style", "data boxes");
        plot.set("style", "fill pattern 1 border");
        plot.set("xtics", "1");
        plot.set("xrange", "[0:5]");
//        plot.set("xtics", "border in scale 0 nomirror rotate by -45  autojustify");
        plot.setTitle(plotTitle);
        GNUPlotTerminal term = new PostscriptTerminal(plotName);
        plot.setTerminal(term);
        plot.plot();
        return plot;
    }

    public static JavaPlot plotErrorLines(String plotName, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMap) {
        return plot(plotName, plotTitle, xLable, yLable, dataPointMap, Style.ERRORLINES);
    }

    public static JavaPlot plot(String plotName, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMap, Style style) {
        JavaPlot plot = new JavaPlot();
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        PlotStyle plotStyle = new PlotStyle();
//        plotStyle.setStyle(Style.LINESPOINTS);
        plotStyle.setStyle(style);

        for (String dataSetName : dataPointMap.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(Utils.getSortedPointDataSetWithMinMaxForBox(dataPointMap.get(dataSetName), false));
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

    public static JavaPlot plotInverse(String plotName, String plotTitle, String xLable, String yLable, Map<String, List<Point<Number>>> dataPointMapY1, String y2Lable, Map<String, List<Point<Number>>> dataPointMapY2) {
        JavaPlot plot = new JavaPlot();
        PlotStyle plotStyle = new PlotStyle();
//        plotStyle.setStyle(Style.LINESPOINTS);
        plotStyle.setStyle(Style.ERRORLINES);

        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        for (String dataSetName : dataPointMapY1.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(Utils.getSortedPointDataSetWithMinMaxForBox(dataPointMapY1.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(yLable+"-"+dataSetName);
            plot.addPlot(dataSetPlot);
        }

        for (String dataSetName : dataPointMapY2.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(Utils.getSortedPointDataSetWithMinMaxForBox(dataPointMapY2.get(dataSetName), true));
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
            DataSetPlot dataSetPlot = new DataSetPlot(Utils.getSortedPointDataSetWithMinMaxForBox(dataPointMapY1.get(dataSetName), false));
            dataSetPlot.setPlotStyle(plotStyle);
            dataSetPlot.setTitle(yLable+"-"+dataSetName);
            dataSetPlot.set("axes", "x1y1");
            plot.addPlot(dataSetPlot);
        }

        for (String dataSetName : dataPointMapY2.keySet()) {
            DataSetPlot dataSetPlot = new DataSetPlot(Utils.getSortedPointDataSetWithMinMaxForBox(dataPointMapY2.get(dataSetName), false));
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

    public static void plotMultipleBoxPlots(String plotDir, String plotPrefix, String plotSuffix, List<JavaPlot> plots) {
        String plotName = plotDir + "/" + plotPrefix + plotSuffix;
        JavaPlot plot = new JavaPlot();
        plot.getDebugger().setLevel(plot.getDebugger().VERBOSE);

        PlotStyle plotStyle = new PlotStyle();
        plotStyle.setStyle(Style.ERRORLINES);

        JavaPlot allPlot = new JavaPlot();
        allPlot.set("term", "postscript eps size 11.7in,16.5in enhanced color");
        allPlot.set("output", "'" + plotName + "'");
        AutoGraphLayout layout = new AutoGraphLayout();
        layout.setColumns(3);
//        layout.setRows(5);
        allPlot.getPage().setLayout(layout);
        allPlot.set("xzeroaxis", "");
//        allPlot.set("style", "data boxes");
//        allPlot.set("style", "fill pattern 1 border");
        allPlot.set("xtics", "1");
        allPlot.set("xrange", "[0:5]");

        for (JavaPlot javaPlot : plots) {
            for (Plot dsp : javaPlot.getPlots()) {
                allPlot.addPlot(dsp);
            }
            allPlot.getAxis("x").setLabel(javaPlot.getAxis("x").get("xlabel").split("'")[1]);
            // Dirty hack to reset title
            allPlot.getAxis("y").setLabel(javaPlot.getAxis("y").get("ylabel").split("'")[1]  + "' \n set title '" + javaPlot.getParameters().get("title").split("'")[1]);
            allPlot.newGraph();
        }
        allPlot.setMultiTitle("All Plots");
        allPlot.plot();
    }
}
