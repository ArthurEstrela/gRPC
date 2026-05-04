package org.ifgoiano.grpc.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.ifgoiano.grpc.weather.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@GrpcService
public class WeatherServiceImpl extends WeatherServiceGrpc.WeatherServiceImplBase {

    private final Map<String, Double> cidadesTemp = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public WeatherServiceImpl() {
        cidadesTemp.put("Urutai", 28.5);
        cidadesTemp.put("Goiania", 31.0);
        cidadesTemp.put("Ceres", 33.2);
    }

    @Override
    public void obterTemperaturaAtual(CidadeRequest request, StreamObserver<TemperaturaResponse> responseObserver) {
        String nome = request.getNome();
        if (!cidadesTemp.containsKey(nome)) {
            responseObserver.onError(new RuntimeException("Cidade não encontrada: " + nome));
            return;
        }

        TemperaturaResponse response = TemperaturaResponse.newBuilder()
                .setCidade(nome)
                .setTemperatura(cidadesTemp.get(nome))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void previsaoCincoDias(CidadeRequest request, StreamObserver<PrevisaoResponse> responseObserver) {
        String nome = request.getNome();
        if (!cidadesTemp.containsKey(nome)) {
            responseObserver.onError(new RuntimeException("Cidade não encontrada: " + nome));
            return;
        }

        double tempAtual = cidadesTemp.get(nome);
        PrevisaoResponse.Builder responseBuilder = PrevisaoResponse.newBuilder().setCidade(nome);

        for (int i = 1; i <= 5; i++) {
            double variacao = (random.nextDouble() * 6) - 3; // -3 a +3 graus
            PrevisaoDia dia = PrevisaoDia.newBuilder()
                    .setData("Dia " + i)
                    .setTemperatura(Math.round((tempAtual + variacao) * 10.0) / 10.0)
                    .build();
            responseBuilder.addDias(dia);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void listarCidades(Empty request, StreamObserver<CidadesResponse> responseObserver) {
        CidadesResponse response = CidadesResponse.newBuilder()
                .addAllCidades(cidadesTemp.keySet())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void cadastrarCidade(CadastrarCidadeRequest request, StreamObserver<CadastrarCidadeResponse> responseObserver) {
        double tempSimulada = Math.round((20.0 + random.nextDouble() * 20.0) * 10.0) / 10.0;
        cidadesTemp.put(request.getNome(), tempSimulada);

        CadastrarCidadeResponse response = CadastrarCidadeResponse.newBuilder()
                .setSucesso(true)
                .setMensagem("Cidade " + request.getNome() + " cadastrada! Temperatura simulada: " + tempSimulada + "°C")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void estatisticasClimaticas(CidadeRequest request, StreamObserver<EstatisticasResponse> responseObserver) {
        String nome = request.getNome();
        if (!cidadesTemp.containsKey(nome)) {
            responseObserver.onError(new RuntimeException("Cidade não encontrada: " + nome));
            return;
        }

        double tempAtual = cidadesTemp.get(nome);
        double minima = tempAtual - (random.nextDouble() * 5); // Até 5 graus a menos
        double maxima = tempAtual + (random.nextDouble() * 5); // Até 5 graus a mais
        double media = (minima + maxima) / 2;

        EstatisticasResponse response = EstatisticasResponse.newBuilder()
                .setCidade(nome)
                .setMediaTemperatura(Math.round(media * 10.0) / 10.0)
                .setMinima(Math.round(minima * 10.0) / 10.0)
                .setMaxima(Math.round(maxima * 10.0) / 10.0)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
