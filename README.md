# Sistema de Previsão Meteorológica Distribuído (gRPC + Spring Boot)

Este projeto implementa um sistema distribuído para o fornecimento de informações meteorológicas de diferentes regiões. Uma aplicação cliente recebe requisições HTTP (API REST) e atua como ponte para um servidor gRPC, que é o responsável por processar e retornar os dados meteorológicos. Tudo é executado dentro do ecossistema do **Spring Boot**.

---

## 🚀 Como rodar o projeto

### Pré-requisitos:

- Java 17+
- Maven 3.8+
- (Opcional) Postman, Insomnia ou `curl` para testes.

### Passos de Execução:

1. **Clone o repositório e acesse a pasta:**
   ```bash
   cd grpc
   ```
2. **Compile o projeto** (isso é fundamental para que o plugin do Protobuf gere as classes Java automáticas a partir do `.proto`):
   ```bash
   ./mvnw clean install -DskipTests
   ```
3. **Execute a aplicação Spring Boot:**
   ```bash
   ./mvnw spring-boot:run
   ```
   > A aplicação será iniciada contendo dois servidores simultâneos:
   >
   > - **Servidor Web (REST):** porta `8080`
   > - **Servidor gRPC:** porta `9090`

---

## 📖 Entendendo o arquivo `weather.proto`

O arquivo `.proto` define as estruturas de dados e os serviços usados pelo gRPC. Este contrato (interface) é independente da linguagem de programação.

```protobuf
syntax = "proto3";

// ... opções do java package ...

service WeatherService {
    rpc ObterTemperaturaAtual (CidadeRequest) returns (TemperaturaResponse);
    rpc PrevisaoCincoDias (CidadeRequest) returns (PrevisaoResponse);
    rpc ListarCidades (Empty) returns (CidadesResponse);
    rpc CadastrarCidade (CadastrarCidadeRequest) returns (CadastrarCidadeResponse);
    rpc EstatisticasClimaticas (CidadeRequest) returns (EstatisticasResponse);
}
```

- **`service`:** Define as operações remotas (RPCs). Como um Controller, mas para comunicação direta.
- **`message`:** Define os tipos de dados usados como Parâmetros (Requests) e Retornos (Responses). Exemplo: `CidadeRequest` encapsula a "String nome" e trafega binariamente pelo protocolo gRPC.
- **`rpc`:** Representa os 5 serviços implementados:
  - `ObterTemperaturaAtual`: Recebe uma cidade e retorna a temperatura real atual.
  - `PrevisaoCincoDias`: Recebe uma cidade e envia previsões aleatórias simulando um período de 5 dias.
  - `ListarCidades`: Usa a mensagem `Empty` como parâmetro e devolve a lista de cidades catalogadas em memória.
  - `CadastrarCidade`: Manda um DTO com cidade e temperatura, que é indexada no mapa de memória.
  - `EstatisticasClimaticas`: Calcula a média, mínima e máxima a partir da variação estocástica atual.

### Como o `.proto` gera código (stubs)?

Quando executamos o `mvn clean install` ou `mvn compile`, o Maven utiliza o plugin `protobuf-maven-plugin`. Esse plugin lê o arquivo `.proto`, invoca o compilador do Protobuf (`protoc`) e gera dinamicamente as classes Java correspondentes dentro de `target/generated-sources/protobuf`.
Essas classes contêm:

1. Os **Builders e Padrões de Mensagens** (como `CidadeRequest.newBuilder().setNome(...).build()`).
2. Os **Stubs** (`WeatherServiceBlockingStub` no cliente e `WeatherServiceImplBase` no Servidor).

---

## 🔄 Fluxo Completo: De HTTP até gRPC

Quando o usuário realiza uma requisição na API via Postman/cURL, o fluxo obedece a seguinte ordem:

1. **Requisição HTTP (Rest API):** O cliente envia um JSON ou parâmetro via HTTP para um endpoint, como `GET /temperatura?cidade=Urutai`.
2. **Controller (`WeatherController.java`):** A classe do Spring intercepta a chamada na porta 8080.
3. **Mapeamento e Construção (Protobuf):** O Controller empacota os dados recebidos (ex: o parâmetro `cidade`) em uma mensagem do gRPC (usando o `CidadeRequest.newBuilder().build()`).
4. **Chamada gRPC (Stub):** O Controller invoca o servidor usando o stub `weatherServiceStub.obterTemperaturaAtual(...)` injetado pelo `@GrpcClient`. Isso converte a mensagem Java em bytes eficientes e transmite via HTTP/2 (porta 9090).
5. **Servidor gRPC (`WeatherServiceImpl.java`):** O servidor processa a chamada, lendo as cidades salvas na memória (`Map` do Java).
6. **Resposta e Desempacotamento:** O servidor empacota a resposta num `TemperaturaResponse`, devolve ao stub do Controller, e este traduz tudo para um Map (JSON) de volta para o cliente (Postman).

---

## 🧪 Como Testar a API (e Tirar o Print!)

Com a aplicação iniciada (`mvn spring-boot:run`), você pode abrir um terminal novo, ou usar o **Postman** (crie Requests na porta `8080`).

### 1. Listar Cidades Disponíveis (GET)

**Via cURL (Terminal/PowerShell):**

```bash
curl http://localhost:8080/cidades
```

### 2. Obter Temperatura Atual (GET)

```bash
curl http://localhost:8080/temperatura?cidade=Urutai
```

### 3. Cadastrar uma Nova Cidade (POST)

**No Postman:**

- URL: `http://localhost:8080/cidade`
- Method: `POST`
- Body (RAW -> JSON):
  ```json
  {
    "nome": "Morrinhos"
  }
  ```

**No cURL:**

```bash
curl -X POST http://localhost:8080/cidade -H "Content-Type: application/json" -d "{\"nome\":\"Morrinhos\"}"
```

### 4. Obter Previsão de 5 Dias (GET)

```bash
curl http://localhost:8080/previsao?cidade=Goiania
```

### 5. Obter Estatísticas Climáticas (GET)

```bash
curl http://localhost:8080/estatisticas?cidade=Ceres
```

### Prints

![Sistema funcionando](docs/print(1).png)
![Sistema funcionando](docs/print(2).png)
![Sistema funcionando](docs/print(3).png)
![Sistema funcionando](docs/print(4).png)
