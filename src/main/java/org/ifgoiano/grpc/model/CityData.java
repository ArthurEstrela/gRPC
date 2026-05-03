package org.ifgoiano.grpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CityData {
    private String name;
    private double currentTemperature;
    private List<ForecastDayData> forecast;
    private List<Double> history; // For stats
}
