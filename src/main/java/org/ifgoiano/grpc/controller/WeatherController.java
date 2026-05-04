package org.ifgoiano.grpc.controller;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.ifgoiano.grpc.weather.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class WeatherController {

    @GrpcClient("weatherService")
    private WeatherServiceGrpc.WeatherServiceBlockingStub weatherServiceStub;

    @PostMapping("/cidade")
    public Map<String, Object> cadastrarCidade(@RequestBody CidadeDto dto) {
        CadastrarCidadeRequest request = CadastrarCidadeRequest.newBuilder()
                .setNome(dto.nome())
                .build();

        CadastrarCidadeResponse response = weatherServiceStub.cadastrarCidade(request);

        return Map.of(
                "sucesso", response.getSucesso(),
                "mensagem", response.getMensagem()
        );
    }

    @GetMapping("/cidades")
    public List<String> listarCidades() {
        CidadesResponse response = weatherServiceStub.listarCidades(Empty.newBuilder().build());
        return response.getCidadesList();
    }

    @GetMapping("/temperatura")
    public Map<String, Object> obterTemperatura(@RequestParam String cidade) {
        CidadeRequest request = CidadeRequest.newBuilder().setNome(cidade).build();
        TemperaturaResponse response = weatherServiceStub.obterTemperaturaAtual(request);
        
        return Map.of(
                "cidade", response.getCidade(),
                "temperatura", response.getTemperatura()
        );
    }

    @GetMapping("/previsao")
    public Map<String, Object> previsaoCincoDias(@RequestParam String cidade) {
        CidadeRequest request = CidadeRequest.newBuilder().setNome(cidade).build();
        PrevisaoResponse response = weatherServiceStub.previsaoCincoDias(request);
        
        List<Map<String, Object>> dias = response.getDiasList().stream()
                .map(dia -> Map.of(
                        "data", dia.getData(),
                        "temperatura", (Object) dia.getTemperatura()
                ))
                .collect(Collectors.toList());

        return Map.of(
                "cidade", response.getCidade(),
                "previsao", dias
        );
    }

    @GetMapping("/estatisticas")
    public Map<String, Object> estatisticasClimaticas(@RequestParam String cidade) {
        CidadeRequest request = CidadeRequest.newBuilder().setNome(cidade).build();
        EstatisticasResponse response = weatherServiceStub.estatisticasClimaticas(request);
        
        return Map.of(
                "cidade", response.getCidade(),
                "media_temperatura", response.getMediaTemperatura(),
                "minima", response.getMinima(),
                "maxima", response.getMaxima()
        );
    }

    public record CidadeDto(String nome) {}
}
