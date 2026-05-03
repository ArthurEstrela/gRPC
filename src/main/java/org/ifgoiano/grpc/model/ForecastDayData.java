package org.ifgoiano.grpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForecastDayData {
    private String date;
    private double minTemp;
    private double maxTemp;
    private String condition;
}
