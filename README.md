# Empirical Distributions: A Statistical Data Visualization Tool

This project is a school assignment focused on statistical analysis and visualization. The application allows users to input a set of numerical data and generates key statistical measures like mean, median, mode, variance, standard deviation, and quartiles. It also creates bar and pie charts for visualizing the frequency distribution of the data.

## Features

- **Data Input**: Users can input their data as comma-separated values (CSV), with different rows separated by semicolons.
- **Statistical Calculations**:
  - **Mean**
  - **Mode**
  - **Median**
  - **Quartiles (Q1, Q2, Q3)**
  - **Variance**
  - **Standard Deviation**
- **Data Visualization**:
  - **Bar Chart** showing the frequency of each value.
  - **Pie Chart** representing the data distribution.

## Technology Stack

- **Frontend**:
  - HTML, CSS
  - Thymeleaf for dynamic rendering
- **Backend**:
  - Java
  - Spring Boot Framework
  - JFreeChart for creating charts

## Project Structure

- **StatisticsChartsController.java**: Handles user input, processes data, and generates charts.
- **StatisticsChartsService.java**: Contains the logic for calculating statistics and creating the charts.
- **input.html**: A form where users input their data.
- **CSS Styles**: Custom styling to make the page visually appealing.

## Screenshots   
![image](https://github.com/user-attachments/assets/ce109abf-b888-474c-9e9e-d56d934f9d09)
![image](https://github.com/user-attachments/assets/3b8e392f-5674-4510-b9cf-9bae41b42bed)

