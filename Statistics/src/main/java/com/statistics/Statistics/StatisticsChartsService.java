package com.statistics.Statistics;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsChartsService {

    public List<Integer> parseInputData(String inputData) {
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

    public List<Integer> calculateMode(List<Integer> dataList) {
        Map<Integer, Long> frequencyMap = dataList.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        long maxFrequency = Collections.max(frequencyMap.values());

        return frequencyMap.entrySet().stream()
                .filter(entry -> entry.getValue() == maxFrequency)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public double calculateMedian(List<Integer> dataList) {
        List<Integer> sortedList = new ArrayList<>(dataList);
        Collections.sort(sortedList);
        int size = sortedList.size();

        if (size % 2 == 0) {
            return (sortedList.get(size / 2 - 1) + sortedList.get(size / 2)) / 2.0;
        } else {
            return sortedList.get(size / 2);
        }
    }

    public double getPercentile(List<Integer> dataList, double percentile) {
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

    public double calculateVariance(List<Integer> dataList, double mean) {
        return dataList.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .sum() / dataList.size(); // Change to (dataList.size() - 1) for sample variance
    }

    public DefaultCategoryDataset createBarDataset(List<Integer> dataList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<Integer, Long> frequencyMap = dataList.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        for (Map.Entry<Integer, Long> entry : frequencyMap.entrySet()) {
            dataset.addValue(entry.getValue(), "Frequency", entry.getKey());
        }

        return dataset;
    }

    public JFreeChart createBarChart(DefaultCategoryDataset dataset) {
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

    public DefaultPieDataset createPieDataset(List<Integer> dataList) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<Integer, Long> frequencyMap = dataList.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        for (Map.Entry<Integer, Long> entry : frequencyMap.entrySet()) {
            dataset.setValue("Value " + entry.getKey(), entry.getValue());
        }

        return dataset;
    }

    public JFreeChart createPieChart(DefaultPieDataset dataset) {
        return ChartFactory.createPieChart(
                "Data Distribution",
                dataset,
                true,
                true,
                false
        );
    }

    public String encodeChartToBase64(JFreeChart chart, int width, int height) {
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
