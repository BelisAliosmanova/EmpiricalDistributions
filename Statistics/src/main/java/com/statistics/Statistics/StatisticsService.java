package com.statistics.Statistics;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    public Map<String, Object> processData(String inputData) {
        // Parse input data into a list of integers
        List<Integer> dataList = Arrays.stream(inputData.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        // Calculate statistics
        double mean = dataList.stream().mapToInt(Integer::intValue).average().orElse(0);
        Map<Integer, Long> frequencyMap = dataList.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        double median = calculateMedian(dataList);
        double q1 = getPercentile(dataList, 25);
        double q3 = getPercentile(dataList, 75);
        double variance = dataList.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .sum() / (dataList.size() - 1);
        double stdDev = Math.sqrt(variance);

        // Create charts
        String barChartPath = createBarChart(frequencyMap);
        String pieChartPath = createPieChart(frequencyMap);

        // Prepare results
        Map<String, Object> results = new HashMap<>();
        results.put("statistics", Map.of(
                "Mean", mean,
                "Median", median,
                "1st Quartile", q1,
                "3rd Quartile", q3,
                "Variance", variance,
                "Standard Deviation", stdDev
        ));
        results.put("barChartPath", barChartPath);
        results.put("pieChartPath", pieChartPath);

        return results;
    }

    private double calculateMedian(List<Integer> dataList) {
        Collections.sort(dataList);
        int size = dataList.size();
        if (size % 2 == 0) {
            return (dataList.get(size / 2 - 1) + dataList.get(size / 2)) / 2.0;
        } else {
            return dataList.get(size / 2);
        }
    }

    private double getPercentile(List<Integer> sortedList, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * sortedList.size()) - 1;
        return sortedList.get(index);
    }

    private String createBarChart(Map<Integer, Long> frequencyMap) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        frequencyMap.forEach((key, value) -> dataset.addValue(value, "Frequency", key));

        JFreeChart chart = ChartFactory.createBarChart("Data Frequencies", "Values", "Frequency", dataset);
        String filePath = "barChart.png";
        saveChartAsImage(chart, filePath);

        return filePath;
    }

    private String createPieChart(Map<Integer, Long> frequencyMap) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        frequencyMap.forEach((key, value) -> dataset.setValue("Value " + key, value));

        JFreeChart chart = ChartFactory.createPieChart("Data Distribution", dataset);
        String filePath = "pieChart.png";
        saveChartAsImage(chart, filePath);

        return filePath;
    }

    private void saveChartAsImage(JFreeChart chart, String filePath) {
        try {
            ChartUtils.saveChartAsPNG(new File(filePath), chart, 800, 400);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
