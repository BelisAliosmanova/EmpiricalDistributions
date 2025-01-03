package com.statistics.Statistics;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class StatisticsChartsController {

    private final StatisticsChartsService statisticsChartsService;

    public StatisticsChartsController(StatisticsChartsService statisticsChartsService) {
        this.statisticsChartsService = statisticsChartsService;
    }

    @GetMapping("/")
    public String showInputForm() {
        return "input"; // This will render the input form (input.html)
    }

    @PostMapping("/process")
    public String processData(@RequestParam("data") String inputData, Model model) {
        // Validate and parse the input data
        List<Integer> dataList = statisticsChartsService.parseInputData(inputData);

        if (dataList.isEmpty()) {
            model.addAttribute("error", "Невалидни или празни данни. Моля, въведете валидни числа, разделени със запетаи.");
            return "input";
        }

        // Calculate statistics
        double mean = dataList.stream().mapToInt(Integer::intValue).average().orElse(0);
        List<Integer> mode = statisticsChartsService.calculateMode(dataList);
        double median = statisticsChartsService.calculateMedian(dataList);
        double q1 = statisticsChartsService.getPercentile(dataList, 25);
        double q3 = statisticsChartsService.getPercentile(dataList, 75);
        double variance = statisticsChartsService.calculateVariance(dataList, mean); // Change to sample or population as needed
        double stdDev = Math.sqrt(variance);

        // Create bar chart
        DefaultCategoryDataset barDataset = statisticsChartsService.createBarDataset(dataList);
        JFreeChart barChart = statisticsChartsService.createBarChart(barDataset);

        // Create pie chart
        DefaultPieDataset pieDataset = statisticsChartsService.createPieDataset(dataList);
        JFreeChart pieChart = statisticsChartsService.createPieChart(pieDataset);

        // Convert charts to Base64
        String barChartBase64 = statisticsChartsService.encodeChartToBase64(barChart, 800, 400);
        String pieChartBase64 = statisticsChartsService.encodeChartToBase64(pieChart, 800, 400);

        // Add statistics and chart images to the model
        model.addAttribute("inputData", inputData);
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
}
