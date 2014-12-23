package no.ux.uis.cipsi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.panayotis.gnuplot.dataset.Point;

public class Utils {

    public static Map<String, List<Point<Number>>> addDataPoints(String dataSetName, int x, DescriptiveStatistics stats, Map<String, List<Point<Number>>> dataPointMap) {
        List<Point<Number>> dataPointList = dataPointMap.get(dataSetName);
        if (dataPointList == null){
            dataPointList = new ArrayList<>();
            dataPointMap.put(dataSetName, dataPointList);
        }

        if (Double.compare(stats.getMean(), Double.NaN) == 0) {
            return dataPointMap;
        }
        Point<Number> mean = new Point<Number>(x, stats.getMean(), stats.getMin(), stats.getMax());
//        System.out.println("mean:" + stats.getMean());
        dataPointList.add(mean);
        return dataPointMap;
    }
}
