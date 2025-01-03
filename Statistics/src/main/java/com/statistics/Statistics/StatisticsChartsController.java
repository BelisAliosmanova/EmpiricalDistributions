package com.statistics.Statistics;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class StatisticsChartsController {

    @GetMapping("/")
    public String showInputForm() {
        return "input";
    }

    @PostMapping("/process")
    public String processData(@RequestParam("data") String inputData, Model model) {
        // Parse the input data
        List<Integer> dataList = parseInputData(inputData);

        // Mean
        double mean = dataList.stream().mapToInt(Integer::intValue).average().orElse(0);

        // Mode
        Map<Integer, Long> frequencyMap = dataList.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        long maxFrequency = Collections.max(frequencyMap.values());
        List<Integer> mode = frequencyMap.entrySet().stream()
                .filter(entry -> entry.getValue() == maxFrequency)
                .map(Map.Entry::getKey)
                .toList();

        // Median
        Collections.sort(dataList);
        double median;
        int size = dataList.size();
        if (size % 2 == 0) {
            median = (dataList.get(size / 2 - 1) + dataList.get(size / 2)) / 2.0;
        } else {
            median = dataList.get(size / 2);
        }

        // Quartiles
        double q1 = getPercentile(dataList, 25);
        double q2 = median; // Median is the 2nd quartile
        double q3 = getPercentile(dataList, 75);

        // Variance
        double variance = dataList.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .sum() / (dataList.size() - 1);

        // Standard Deviation
        double stdDev = Math.sqrt(variance);

        // Create bar chart
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        for (Map.Entry<Integer, Long> entry : frequencyMap.entrySet()) {
            barDataset.addValue(entry.getValue(), "Frequency", entry.getKey());
        }
        JFreeChart barChart = ChartFactory.createBarChart(
                "Data Frequencies",
                "Values",
                "Frequency",
                barDataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        // Create pie chart
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        for (Map.Entry<Integer, Long> entry : frequencyMap.entrySet()) {
            pieDataset.setValue("Value " + entry.getKey(), entry.getValue());
        }
        JFreeChart pieChart = ChartFactory.createPieChart(
                "Data Distribution",
                pieDataset,
                true,
                true,
                false
        );

        // Convert charts to Base64 images
        model.addAttribute("barChart", encodeChartToBase64(barChart, 800, 400));
        model.addAttribute("pieChart", encodeChartToBase64(pieChart, 800, 400));

        // Add statistics to the model
        model.addAttribute("mean", mean);
        model.addAttribute("mode", mode);
        model.addAttribute("median", median);
        model.addAttribute("q1", q1);
        model.addAttribute("q2", q2);
        model.addAttribute("q3", q3);
        model.addAttribute("variance", variance);
        model.addAttribute("stdDev", stdDev);

        return "charts";
    }

    private List<Integer> parseInputData(String inputData) {
        List<Integer> dataList = new ArrayList<>();
        String[] rows = inputData.split(";");
        for (String row : rows) {
            String[] values = row.split(",");
            for (String value : values) {
                dataList.add(Integer.parseInt(value.trim()));
            }
        }
        return dataList;
    }

    private double getPercentile(List<Integer> sortedList, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * sortedList.size()) - 1;
        return sortedList.get(index);
    }

    private String encodeChartToBase64(JFreeChart chart, int width, int height) {
        try {
            BufferedImage bufferedImage = chart.createBufferedImage(width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
