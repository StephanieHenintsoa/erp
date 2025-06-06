// SalaryEvolutionResponse.java
package com.example.erp.entity.salary;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SalaryEvolutionResponse {
    @JsonProperty("monthly_data")
    private List<MonthlyData> monthlyData;
    
    public List<MonthlyData> getMonthlyData() {
        return monthlyData;
    }
    
    public void setMonthlyData(List<MonthlyData> monthlyData) {
        this.monthlyData = monthlyData;
    }
    
    public static class MonthlyData {
        private String month;
        
        @JsonProperty("month_short")
        private String monthShort;
        
        @JsonProperty("total_net")
        private Double totalNet;
        
        private Map<String, Double> earnings;
        private Map<String, Double> deductions;
        
        // Getters and Setters
        public String getMonth() {
            return month;
        }
        
        public void setMonth(String month) {
            this.month = month;
        }
        
        public String getMonthShort() {
            return monthShort;
        }
        
        public void setMonthShort(String monthShort) {
            this.monthShort = monthShort;
        }
        
        public Double getTotalNet() {
            return totalNet;
        }
        
        public void setTotalNet(Double totalNet) {
            this.totalNet = totalNet;
        }
        
        public Map<String, Double> getEarnings() {
            return earnings;
        }
        
        public void setEarnings(Map<String, Double> earnings) {
            this.earnings = earnings;
        }
        
        public Map<String, Double> getDeductions() {
            return deductions;
        }
        
        public void setDeductions(Map<String, Double> deductions) {
            this.deductions = deductions;
        }
    }
}