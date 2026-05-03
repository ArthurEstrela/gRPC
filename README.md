# Sistema de Previsão Meteorológica Distribuído (gRPC + Spring Boot)

Este projeto implementa um sistema distribuído para fornecimento de informações meteorológicas. Ele utiliza **gRPC** para a comunicação entre o cliente e o servidor, e **Spring Boot** para gerenciar a aplicação e expor uma API REST.

## Estrutura do Projeto

O sistema é composto por dois componentes principais que rodam na mesma aplicação Spring Boot (para fins de simplificação):

1.  **Servidor gRPC**: Responsável pelo processamento e armazenamento (em memória) dos dados meteorológicos.
2.  **Cliente gRPC / API REST**: Atua como uma ponte, recebendo requisições HTTP e convertendo-as em chamadas gRPC para o servidor.

## Requisitos

*   Java 21
*   Maven

## Como rodar o projeto

1.  Clone o repositório.
2.  Na raiz do projeto, execute o comando para compilar e gerar os stubs do gRPC:
    ```bash
    ./mvnw clean compile
    ```
3.  Execute a aplicação Spring Boot:
    ```bash
    ./mvnw spring-boot:run
    ```
4.  A API REST estará disponível em `http://localhost:8080`.

## Explicação do Arquivo `.proto`

O arquivo `src/main/proto/weather.proto` define a interface de comunicação entre o cliente e o servidor.

### Definição do Serviço (`service`)

```proto
service WeatherService {
    rpc GetCurrentTemperature(CityRequest) returns (TemperatureResponse);
    rpc GetFiveDayForecast(CityRequest) returns (ForecastResponse);
    rpc ListCities(Empty) returns (CityListResponse);
    rpc RegisterCity(RegisterCityRequest) returns (CityResponse);
    rpc GetClimateStats(CityRequest) returns (StatsResponse);
}
```

### Mensagens (`message`)

*   `CityRequest`: Contém o nome da cidade para consulta.
*   `TemperatureResponse`: Retorna a temperatura atual, nome da cidade e unidade.
*   `ForecastResponse`: Retorna uma lista de previsões para os próximos 5 dias.
*   `CityListResponse`: Lista os nomes de todas as cidades cadastradas.
*   `RegisterCityRequest`: Dados necessários para cadastrar uma nova cidade (nome e temperatura inicial).
*   `StatsResponse`: Estatísticas climáticas (média, mínima e máxima).

### RPCs Implementados

1.  **GetCurrentTemperature**: Retorna a temperatura atual de uma cidade específica.
2.  **GetFiveDayForecast**: Retorna a previsão detalhada para os próximos 5 dias.
3.  **ListCities**: Retorna a lista de todas as cidades disponíveis no sistema.
4.  **RegisterCity**: Permite adicionar uma nova cidade ao sistema.
5.  **GetClimateStats**: Calcula e retorna estatísticas baseadas no histórico de temperaturas da cidade.

### Geração de Código (Stubs)

O plugin `protobuf-maven-plugin` no `pom.xml` automatiza a geração dos stubs. Quando executamos `mvn compile`, o compilador `protoc` lê o arquivo `.proto` e gera classes Java que facilitam a implementação do serviço no servidor e a chamada do serviço no cliente.

## Fluxo Completo

1.  O usuário faz uma requisição HTTP GET para `http://localhost:8080/temperatura?cidade=Urutai`.
2.  O `WeatherController` (Spring MVC) recebe a requisição.
3.  O Controller utiliza o `WeatherServiceBlockingStub` (gerado pelo gRPC) para fazer uma chamada RPC ao servidor gRPC.
4.  O `WeatherServiceImpl` no servidor gRPC processa a requisição, busca os dados na memória e retorna uma mensagem gRPC.
5.  O Controller recebe a resposta gRPC, converte para JSON e retorna ao usuário.

## Exemplos de Uso (Endpoints REST)

### Listar Cidades
**GET** `/cidades`

### Obter Temperatura Atual
**GET** `/temperatura?cidade=Urutaí`

### Previsão de 5 Dias
**GET** `/previsao?cidade=Urutaí`

### Estatísticas Climáticas
**GET** `/estatisticas?cidade=Urutaí`

### Cadastrar Nova Cidade
**POST** `/cidade`
```json
{
    "nome": "São Paulo",
    "temperatura": 22.5
}
```
