# TrueBalance

Sistema de gerenciamento financeiro desenvolvido com Spring Boot.

## Tecnologias

- Java 21
- Spring Boot 4.0.1
- Spring Data JPA
- PostgreSQL 16
- Gradle 8.11.1
- Docker & Docker Compose
- Swagger/OpenAPI 3

## Requisitos

- Docker e Docker Compose
- OU Java 21 e PostgreSQL (para execução local)

## Executando com Docker (Recomendado)

### Iniciar a aplicação

```bash
docker-compose up -d
```

A aplicação estará disponível em `http://localhost:8080`

### Parar a aplicação

```bash
docker-compose down
```

### Rebuild após alterações no código

```bash
docker-compose up -d --build
```

### Ver logs

```bash
# Logs da aplicação
docker-compose logs -f app

# Logs do banco de dados
docker-compose logs -f postgres
```

### Remover dados do banco

```bash
docker-compose down -v
```

**Nota:** Os dados do banco são persistidos em um volume Docker. Rebuilds da aplicação não afetam os dados armazenados.

## Executando Localmente (sem Docker)

### Pré-requisitos

1. Instalar Java 21
2. Instalar PostgreSQL 16
3. Criar banco de dados:

```sql
CREATE DATABASE truebalance;
```

### Configurar variáveis de ambiente (opcional)

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/truebalance
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
```

### Executar aplicação

```bash
./gradlew bootRun
```

## Documentação da API

Após iniciar a aplicação, a documentação interativa estará disponível em:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Estrutura do Projeto

```
truebalance/
├── src/
│   ├── main/
│   │   ├── java/com/truebalance/truebalance/
│   │   │   ├── application/        # Camada de Aplicação
│   │   │   │   ├── controller/     # REST Controllers
│   │   │   │   ├── dto/            # Data Transfer Objects
│   │   │   │   └── exception/      # Exception Handlers
│   │   │   ├── domain/             # Camada de Domínio (Lógica de Negócio)
│   │   │   │   ├── entity/         # Entidades de Domínio
│   │   │   │   ├── usecase/        # Casos de Uso
│   │   │   │   ├── service/        # Serviços de Domínio
│   │   │   │   ├── port/           # Interfaces (Portas)
│   │   │   │   └── exception/      # Exceções de Domínio
│   │   │   ├── infra/              # Camada de Infraestrutura
│   │   │   │   └── db/
│   │   │   │       ├── entity/     # Entidades JPA
│   │   │   │       ├── repository/ # Spring Data Repositories
│   │   │   │       └── adapter/    # Adaptadores (Portas → JPA)
│   │   │   └── config/             # Configurações do Spring
│   │   └── resources/
│   │       └── application.yml
│   └── test/                       # 344 testes implementados
│       └── java/com/truebalance/truebalance/
│           ├── domain/usecase/     # Testes de Use Cases
│           ├── domain/service/     # Testes de Serviços
│           ├── application/controller/ # Testes de Controllers
│           ├── infra/db/adapter/   # Testes de Adapters
│           └── integration/        # Testes de Integração E2E
├── docs/                           # Documentação do Projeto
│   ├── tasks.md                    # Tarefas de Implementação
│   ├── test-tasks.md               # Tarefas de Testes
│   └── test-plan.md                # Plano de Testes
├── build.gradle
├── settings.gradle
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## Funcionalidades

O TrueBalance implementa um sistema completo de gerenciamento financeiro com as seguintes funcionalidades:

### 1. Gerenciamento de Contas (Bills)
- Criar, atualizar e listar contas
- Cálculo automático de parcelas com arredondamento HALF_UP
- Integração com cartões de crédito

### 2. Cartões de Crédito
- Gerenciamento completo de cartões (CRUD)
- Configuração de dia de fechamento e vencimento
- Cálculo de limite disponível em tempo real
- Validação de limite antes de criar contas

### 3. Sistema de Faturas
- Geração automática de faturas mensais
- Fechamento de faturas com cálculo de saldo
- Transferência de saldo positivo (débito) entre meses
- Transferência de crédito (saldo negativo) entre meses
- Uma fatura por cartão por mês (garantido por constraint)

### 4. Parcelas (Installments)
- Distribuição automática de parcelas entre faturas
- Cálculo inteligente de datas baseado no ciclo de faturamento
- Tratamento de edge cases (meses com dias inválidos)
- Rastreamento de parcelas por conta e fatura

### 5. Pagamentos Parciais
- Registro de pagamentos parciais em faturas abertas
- Atualização automática do limite disponível
- Permite pagamentos que excedem o valor da fatura (gerando crédito)
- Validação de permissão de pagamento parcial por cartão

## Arquitetura

O projeto segue a **Arquitetura Hexagonal (Ports & Adapters)** com Clean Architecture:

- **Domain Layer**: Lógica de negócio pura, sem dependências externas
- **Application Layer**: Controllers REST e DTOs
- **Infrastructure Layer**: Implementações JPA e adaptadores

**Benefícios:**
- ✅ Testabilidade: 344 testes unitários e de integração
- ✅ Independência de frameworks
- ✅ Fácil manutenção e evolução
- ✅ Separação clara de responsabilidades

## Configuração

As principais configurações podem ser alteradas através de variáveis de ambiente:

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `SPRING_DATASOURCE_URL` | URL de conexão do banco | `jdbc:postgresql://localhost:5432/truebalance` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `postgres` |

## Build

### Gerar JAR

```bash
./gradlew build
```

O arquivo JAR será gerado em `build/libs/`

### Executar testes

```bash
# Rodar todos os testes
./gradlew test

# Rodar com relatório de cobertura
./gradlew test jacocoTestReport

# Rodar apenas testes de integração
./gradlew test --tests "*IntegrationTest"

# Rodar apenas testes unitários
./gradlew test --tests "*Test" --exclude-task integrationTest
```

**Estatísticas de Testes:**
- ✅ 344 testes implementados (100% passando)
- ✅ Testes unitários de use cases
- ✅ Testes de controllers (MockMvc)
- ✅ Testes de adapters
- ✅ Testes de integração end-to-end
- ✅ Cobertura completa de regras de negócio

### Limpar build

```bash
./gradlew clean
```

## Portas Utilizadas

- `8080` - Aplicação Spring Boot
- `5432` - PostgreSQL

## Desenvolvimento

### Hibernate DDL

O projeto está configurado com `ddl-auto: update`, que atualiza automaticamente o schema do banco de dados baseado nas entidades JPA.

### SQL Logging

Os SQLs executados são exibidos no console com formatação habilitada para facilitar o debug.

## Licença

Este projeto é um projeto de demonstração.