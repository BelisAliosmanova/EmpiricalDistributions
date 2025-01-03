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
        return "input"; // This will render the input form (input.html)
    }

    @PostMapping("/process")
    public String processData(@RequestParam("data") String inputData, Model model) {
        // Validate and parse the input data
        List<Integer> dataList = parseInputData(inputData);

        if (dataList.isEmpty()) {
            model.addAttribute("error", "Невалидни или празни данни. Моля, въведете валидни числа, разделени със запетаи.");
            return "input";
        }

        // Calculate statistics
        double mean = dataList.stream().mapToInt(Integer::intValue).average().orElse(0);
        List<Integer> mode = calculateMode(dataList);
        double median = calculateMedian(dataList);
        double q1 = getPercentile(dataList, 25);
        double q3 = getPercentile(dataList, 75);
        double variance = calculateVariance(dataList, mean); // Change to sample or population as needed
        double stdDev = Math.sqrt(variance);

        // Create bar chart
        DefaultCategoryDataset barDataset = createBarDataset(dataList);
        JFreeChart barChart = createBarChart(barDataset);

        // Create pie chart
        DefaultPieDataset pieDataset = createPieDataset(dataList);
        JFreeChart pieChart = createPieChart(pieDataset);

        // Convert charts to Base64
        String barChartBase64 = encodeChartToBase64(barChart, 800, 400);
        String pieChartBase64 = encodeChartToBase64(pieChart, 800, 400);

        // Add statistics and chart images to the model
        model.addAttribute("mean", mean);
        model.addAttribute("mode", mode);
        model.addAttribute("median", median);
        model.addAttribute("q1", q1);
        model.addAttribute("q3", q3);
        model.addAttribute("variance", variance);
        model.addAttribute("stdDev", stdDev);
        model.addAttribute("barChart", barChartBase64);
        model.addAttribute("pieChart", pieChartBase64);

        return "input";
    }

    private List<Integer> parseInputData(String inputData) {
        List<Integer> dataList = new ArrayList<>();

        try {
            String[] rows = inputData.split(";");

            for (String row : rows) {
                String[] values = row.split(",");
                for (String value : values) {
                    dataList.add(Integer.parseInt(value.trim()));
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Невалиден вход: " + e.getMessage());
        }

        return dataList;
    }

    private List<Integer> calculateMode(List<Integer> dataList) {
        Map<Integer, Long> frequencyMap = dataList.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        long maxFrequency = Collections.max(frequencyMap.values());

        return frequencyMap.entrySet().stream()
                .filter(entry -> entry.getValue() == maxFrequency)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double calculateMedian(List<Integer> dataList) {
        List<Integer> sortedList = new ArrayList<>(dataList);
        Collections.sort(sortedList);
        int size = sortedList.size();

        if (size % 2 == 0) {
            return (sortedList.get(size / 2 - 1) + sortedList.get(size / 2)) / 2.0;
        } else {
            return sortedList.get(size / 2);
        }
    }

    private double getPercentile(List<Integer> dataList, double percentile) {
        List<Integer> sortedList = new ArrayList<>(dataList);
        Collections.sort(sortedList);

        double position = percentile / 100.0 * (sortedList.size() - 1);
        int lowerIndex = (int) Math.floor(position);
        int upperIndex = (int) Math.ceil(position);

        if (lowerIndex == upperIndex) {
            return sortedList.get(lowerIndex);
        }

        double fraction = position - lowerIndex;

        return sortedList.get(lowerIndex) + fraction * (sortedList.get(upperIndex) - sortedList.get(lowerIndex));
    }

    private double calculateVariance(List<Integer> dataList, double mean) {
        return dataList.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .sum() / dataList.size(); // Change to (dataList.size() - 1) for sample variance
    }

    private DefaultCategoryDataset createBarDataset(List<Integer> dataList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<Integer, Long> frequencyMap = dataList.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        for (Map.Entry<Integer, Long> entry : frequencyMap.entrySet()) {
            dataset.addValue(entry.getValue(), "Frequency", entry.getKey());
        }

        return dataset;
    }

    private JFreeChart createBarChart(DefaultCategoryDataset dataset) {
        return ChartFactory.createBarChart(
                "Data Frequencies",
                "Values",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
    }

    private DefaultPieDataset createPieDataset(List<Integer> dataList) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<Integer, Long> frequencyMap = dataList.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        for (Map.Entry<Integer, Long> entry : frequencyMap.entrySet()) {
            dataset.setValue("Value " + entry.getKey(), entry.getValue());
        }

        return dataset;
    }

    private JFreeChart createPieChart(DefaultPieDataset dataset) {
        return ChartFactory.createPieChart(
                "Data Distribution",
                dataset,
                true,
                true,
                false
        );
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
