package org.ifgoiano.grpc.controller;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.ifgoiano.grpc.lib.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class WeatherController {

    @GrpcClient("weatherService")
    private WeatherServiceGrpc.WeatherServiceBlockingStub weatherStub;

    @PostMapping("/cidade")
    public String registerCity(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("nome");
        double temp = Double.parseDouble(body.get("temperatura").toString());
        
        RegisterCityRequest request = RegisterCityRequest.newBuilder()
                .setName(name)
                .setCurrentTemperature(temp)
                .build();
        
        CityResponse response = weatherStub.registerCity(request);
        return response.getMessage();
    }

    @GetMapping("/cidades")
    public List<String> listCities() {
        CityListResponse response = weatherStub.listCities(Empty.newBuilder().build());
        return response.getCitiesList();
    }

    @GetMapping("/temperatura")
    public Map<String, Object> getTemperature(@RequestParam("cidade") String cidade) {
        CityRequest request = CityRequest.newBuilder().setName(cidade).build();
        TemperatureResponse response = weatherStub.getCurrentTemperature(request);
        Map<String, Object> res = new java.util.HashMap<>();
        res.put("cidade", response.getCityName());
        res.put("temperatura", response.getTemperature());
        res.put("unidade", response.getUnit());
        return res;
    }

    @GetMapping("/previsao")
    public Map<String, Object> getForecast(@RequestParam("cidade") String cidade) {
        CityRequest request = CityRequest.newBuilder().setName(cidade).build();
        ForecastResponse response = weatherStub.getFiveDayForecast(request);
        
        List<Map<String, Object>> days = response.getDaysList().stream()
                .map(d -> {
                    Map<String, Object> day = new java.util.HashMap<>();
                    day.put("data", d.getDate());
                    day.put("min", d.getMinTemp());
                    day.put("max", d.getMaxTemp());
                    day.put("condicao", d.getCondition());
                    return day;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> res = new java.util.HashMap<>();
        res.put("cidade", response.getCityName());
        res.put("previsao", days);
        return res;
    }

    @GetMapping("/estatisticas")
    public Map<String, Object> getStats(@RequestParam("cidade") String cidade) {
        CityRequest request = CityRequest.newBuilder().setName(cidade).build();
        StatsResponse response = weatherStub.getClimateStats(request);
        Map<String, Object> res = new java.util.HashMap<>();
        res.put("cidade", response.getCityName());
        res.put("media", response.getAverageTemperature());
        res.put("minima", response.getMinTemperature());
        res.put("maxima", response.getMaxTemperature());
        return res;
    }
}
