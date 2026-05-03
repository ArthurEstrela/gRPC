package org.ifgoiano.grpc.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.ifgoiano.grpc.lib.*;
import org.ifgoiano.grpc.model.CityData;
import org.ifgoiano.grpc.model.ForecastDayData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@GrpcService
public class WeatherServiceImpl extends WeatherServiceGrpc.WeatherServiceImplBase {

    private final Map<String, CityData> cityDatabase = new ConcurrentHashMap<>();

    public WeatherServiceImpl() {
        // Mock some data
        initMockData();
    }

    private void initMockData() {
        registerCityData("Urutaí", 25.5);
        registerCityData("Goiânia", 30.2);
        registerCityData("Brasília", 22.8);
    }

    private void registerCityData(String name, double temp) {
        List<ForecastDayData> forecast = new ArrayList<>();
        List<Double> history = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 1; i <= 5; i++) {
            double min = temp - random.nextDouble() * 5;
            double max = temp + random.nextDouble() * 5;
            forecast.add(ForecastDayData.builder()
                    .date("2026-05-0" + (i + 2))
                    .minTemp(min)
                    .maxTemp(max)
                    .condition(random.nextBoolean() ? "Ensolarado" : "Nublado")
                    .build());
            history.add(min);
            history.add(max);
        }
        history.add(temp);

        cityDatabase.put(name.toLowerCase(), CityData.builder()
                .name(name)
                .currentTemperature(temp)
                .forecast(forecast)
                .history(history)
                .build());
    }

    @Override
    public void getCurrentTemperature(CityRequest request, StreamObserver<TemperatureResponse> responseObserver) {
        CityData data = cityDatabase.get(request.getName().toLowerCase());
        if (data != null) {
            TemperatureResponse response = TemperatureResponse.newBuilder()
                    .setCityName(data.getName())
                    .setTemperature(data.getCurrentTemperature())
                    .setUnit("Celsius")
                    .build();
            responseObserver.onNext(response);
        } else {
            responseObserver.onError(new RuntimeException("Cidade não encontrada: " + request.getName()));
            return;
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getFiveDayForecast(CityRequest request, StreamObserver<ForecastResponse> responseObserver) {
        CityData data = cityDatabase.get(request.getName().toLowerCase());
        if (data != null) {
            List<ForecastDay> days = data.getForecast().stream()
                    .map(f -> ForecastDay.newBuilder()
                            .setDate(f.getDate())
                            .setMinTemp(f.getMinTemp())
                            .setMaxTemp(f.getMaxTemp())
                            .setCondition(f.getCondition())
                            .build())
                    .collect(Collectors.toList());

            ForecastResponse response = ForecastResponse.newBuilder()
                    .setCityName(data.getName())
                    .addAllDays(days)
                    .build();
            responseObserver.onNext(response);
        } else {
            responseObserver.onError(new RuntimeException("Cidade não encontrada"));
            return;
        }
        responseObserver.onCompleted();
    }

    @Override
    public void listCities(Empty request, StreamObserver<CityListResponse> responseObserver) {
        CityListResponse response = CityListResponse.newBuilder()
                .addAllCities(cityDatabase.values().stream().map(CityData::getName).collect(Collectors.toList()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void registerCity(RegisterCityRequest request, StreamObserver<CityResponse> responseObserver) {
        registerCityData(request.getName(), request.getCurrentTemperature());
        CityResponse response = CityResponse.newBuilder()
                .setMessage("Cidade " + request.getName() + " cadastrada com sucesso!")
                .setSuccess(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getClimateStats(CityRequest request, StreamObserver<StatsResponse> responseObserver) {
        CityData data = cityDatabase.get(request.getName().toLowerCase());
        if (data != null) {
            double avg = data.getHistory().stream().mapToDouble(d -> d).average().orElse(0.0);
            double min = data.getHistory().stream().mapToDouble(d -> d).min().orElse(0.0);
            double max = data.getHistory().stream().mapToDouble(d -> d).max().orElse(0.0);

            StatsResponse response = StatsResponse.newBuilder()
                    .setCityName(data.getName())
                    .setAverageTemperature(avg)
                    .setMinTemperature(min)
                    .setMaxTemperature(max)
                    .build();
            responseObserver.onNext(response);
        } else {
            responseObserver.onError(new RuntimeException("Cidade não encontrada"));
            return;
        }
        responseObserver.onCompleted();
    }
}
