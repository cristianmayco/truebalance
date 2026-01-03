# TrueBalance - Documentação de Implementação

**Versão:** 1.0
**Última Atualização:** 27 de Dezembro de 2025
**Status:** Fase 1 (MVP) Concluída, Fase 2 (Recursos Avançados) Planejada

---

## Visão Geral

TrueBalance é um sistema abrangente de gerenciamento financeiro projetado para rastrear despesas, gerenciar cartões de crédito e organizar ciclos de faturamento. A aplicação suporta compras à vista e parceladas, com cálculos automatizados e vinculação adequada entre contas, parcelas e futuras faturas de cartão de crédito.

Este documento fornece documentação técnica detalhada tanto para a **implementação atual** (Fase 1 - MVP) quanto para os **recursos planejados** (Fases 2-4) com base no design arquitetural.

**Legenda:**
- ✅ **Implementado** - Recurso está atualmente disponível no código
- 🔨 **Planejado** - Recurso está projetado mas ainda não implementado

---

## Índice

1. [Stack Tecnológica](#stack-tecnológica)
2. [Visão Geral da Arquitetura](#visão-geral-da-arquitetura)
3. [Modelo de Domínio & Entidades](#modelo-de-domínio--entidades)
4. [Data Transfer Objects (DTOs)](#data-transfer-objects-dtos)
5. [Endpoints da API REST](#endpoints-da-api-rest)
6. [Casos de Uso & Lógica de Negócio](#casos-de-uso--lógica-de-negócio)
7. [Camada de Repositório](#camada-de-repositório)
8. [Schema do Banco de Dados](#schema-do-banco-de-dados)
9. [Regras de Negócio](#regras-de-negócio)
10. [Configuração](#configuração)
11. [Roadmap de Desenvolvimento](#roadmap-de-desenvolvimento)
12. [Resumo dos Endpoints da API](#resumo-dos-endpoints-da-api)
13. [Padrões de Código & Convenções](#padrões-de-código--convenções)

---

## Stack Tecnológica

### Framework Principal
- **Linguagem:** Java 21
- **Framework:** Spring Boot 4.0.1
- **Arquitetura:** Hexagonal Architecture (Ports & Adapters)

### Persistência
- **ORM:** Spring Data JPA com Hibernate
- **Banco de Dados:** PostgreSQL 16
- **Driver JDBC:** org.postgresql

### Build & Deployment
- **Ferramenta de Build:** Gradle 8.11.1
- **Containerização:** Docker com builds multi-estágio
- **Orquestração de Containers:** Docker Compose

### Documentação da API
- **OpenAPI:** SpringDoc OpenAPI 3 (v2.7.0)
- **UI Interativa:** Swagger UI
- **Especificação:** OpenAPI 3.0

### Testes
- **Framework:** JUnit 5 (Jupiter)
- **Spring Testing:** Spring Boot Test

### Dependências Adicionais
- **Lombok:** Geração de código para redução de boilerplate
- **Jackson:** Serialização/desserialização JSON
- **Validação:** Jakarta Bean Validation

---

## Visão Geral da Arquitetura

TrueBalance segue a **Arquitetura Hexagonal** (também conhecida como padrão Ports and Adapters) com uma clara separação de responsabilidades entre camadas.

### Camadas Arquiteturais

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│  (Controllers, DTOs, REST API Interfaces)                   │
│  Package: com.truebalance.truebalance.application           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                           │
│  (Entities, Use Cases, Business Logic, Port Interfaces)     │
│  Package: com.truebalance.truebalance.domain                │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  Infrastructure Layer                       │
│  (JPA Entities, Repositories, Adapters)                     │
│  Package: com.truebalance.truebalance.infra                 │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                     Database Layer                          │
│                   PostgreSQL 16                             │
└─────────────────────────────────────────────────────────────┘
```

### Descrição das Camadas

#### 1. Application Layer (`application/`)
**Responsabilidade:** Interfaces voltadas ao usuário e transformação de dados

**Componentes:**
- `controller/` - Controllers REST que lidam com requisições HTTP
- `dto/input/` - DTOs de requisição para dados de entrada
- `dto/output/` - DTOs de resposta para dados de saída

**Exemplo:**
```
application/
├── controller/
│   └── BillController.java
└── dto/
    ├── input/
    │   └── BillRequestDTO.java
    └── output/
        └── BillResponseDTO.java
```

#### 2. Domain Layer (`domain/`)
**Responsabilidade:** Lógica de negócio e regras principais (independente de framework)

**Componentes:**
- `entity/` - Modelos de domínio (objetos Java puros, sem anotações JPA)
- `usecase/` - Operações de negócio e casos de uso
- `port/` - Interfaces de porta definindo contratos para dependências externas

**Exemplo:**
```
domain/
├── entity/
│   └── Bill.java
├── usecase/
│   ├── CreateBill.java
│   ├── UpdateBill.java
│   └── GetAllBills.java
└── port/
    └── BillRepositoryPort.java
```

**Princípio Chave:** A camada de domínio tem **zero dependências** em frameworks ou infraestrutura. Ela só depende da biblioteca padrão do Java.

#### 3. Infrastructure Layer (`infra/`)
**Responsabilidade:** Implementações técnicas e integrações externas

**Componentes:**
- `db/entity/` - Entidades JPA com anotações de banco de dados
- `db/repository/` - Repositórios Spring Data JPA
- `db/adapter/` - Implementações de adaptadores das interfaces de porta

**Exemplo:**
```
infra/db/
├── entity/
│   └── BillEntity.java
├── repository/
│   └── BillRepository.java
└── adapter/
    └── BillRepositoryAdapter.java
```

#### 4. Configuration Layer (`config/`)
**Responsabilidade:** Configuração Spring e definições de beans

**Componentes:**
- `UseCaseConfig.java` - Configuração de beans dos casos de uso
- `OpenApiConfig.java` - Configuração da documentação da API

### Fluxo de Dependências

```
Controller → Use Case → Repository Port ← Repository Adapter → JPA Repository → Database
   ↓            ↓              ↑                   ↑
  DTO    Domain Entity    Interface         Implementation
```

**Principais Benefícios:**
- **Testabilidade:** Lógica de negócio pode ser testada independentemente
- **Flexibilidade:** Fácil trocar implementações de infraestrutura
- **Manutenibilidade:** Fronteiras claras entre camadas
- **Proteção do Domínio:** Lógica de negócio principal isolada de detalhes técnicos

---

## Modelo de Domínio & Entidades

### 1. Bill (Conta/Despesa) ✅ Implementado

**Propósito:** Representa uma despesa financeira ou conta, seja como pagamento único ou dividida em múltiplas parcelas.

#### Modelo de Domínio
**Arquivo:** `src/main/java/com/truebalance/truebalance/domain/entity/Bill.java`

```java
public class Bill {
    private Long id;                        // Identificador único
    private String name;                    // Nome/descrição da conta
    private LocalDateTime executionDate;    // Quando a conta foi executada
    private BigDecimal totalAmount;         // Valor total da conta
    private int numberOfInstallments;       // Número de parcelas
    private BigDecimal installmentAmount;   // Valor por parcela (calculado)
    private String description;             // Notas adicionais
    private LocalDateTime createdAt;        // Timestamp de criação
    private LocalDateTime updatedAt;        // Timestamp da última atualização
}
```

#### Entidade JPA
**Arquivo:** `src/main/java/com/truebalance/truebalance/infra/db/entity/BillEntity.java`

**Tabela:** `bills`

| Coluna | Tipo | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PRIMARY KEY, AUTO_INCREMENT |
| name | VARCHAR(255) | NOT NULL |
| execution_date | TIMESTAMP | NOT NULL |
| total_amount | NUMERIC(10,2) | NOT NULL |
| number_of_installments | INTEGER | NOT NULL, DEFAULT 1 |
| installment_amount | NUMERIC(10,2) | NOT NULL |
| description | TEXT | NULLABLE |
| created_at | TIMESTAMP | NOT NULL, AUTO |
| updated_at | TIMESTAMP | NOT NULL, AUTO |

#### Regras de Negócio
- Se `numberOfInstallments = 1` → pagamento único (à vista)
- Se `numberOfInstallments > 1` → pagamento parcelado
- `installmentAmount` é calculado automaticamente: `totalAmount / numberOfInstallments` (arredondamento HALF_UP)
- Timestamps são gerenciados automaticamente via hooks de ciclo de vida JPA (`@PrePersist`, `@PreUpdate`)

---

### 2. CreditCard 🔨 Planejado

**Propósito:** Representa um cartão de crédito com configuração de ciclo de faturamento e limite de crédito.

#### Modelo de Domínio Planejado

```java
public class CreditCard {
    private Long id;                        // Identificador único
    private String name;                    // Nome do cartão (ex: "Nubank Gold")
    private BigDecimal creditLimit;         // Limite máximo de crédito
    private int closingDay;                 // Dia do mês quando a fatura fecha (1-31)
    private int dueDay;                     // Dia do mês quando o pagamento vence (1-31)
    private boolean allowsPartialPayment;   // Se permite pagamento parcial antes do fechamento
    private LocalDateTime createdAt;        // Timestamp de criação
    private LocalDateTime updatedAt;        // Timestamp da última atualização
}
```

#### Schema de Banco de Dados Planejado

**Tabela:** `credit_cards`

| Coluna | Tipo | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PRIMARY KEY |
| name | VARCHAR(255) | NOT NULL |
| credit_limit | NUMERIC(10,2) | NOT NULL |
| closing_day | INTEGER | NOT NULL, CHECK (1-31) |
| due_day | INTEGER | NOT NULL, CHECK (1-31) |
| allows_partial_payment | BOOLEAN | NOT NULL, DEFAULT FALSE |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

#### Regras de Negócio Planejadas
- Um cartão de crédito pode ter múltiplas faturas (uma por ciclo de faturamento)
- O dia de fechamento determina quando compras param de ser incluídas na fatura atual
- O dia de vencimento é quando o pagamento deve ser feito
- **Limite de Crédito:**
  - `creditLimit`: Limite total fixo do cartão (armazenado no banco)
  - `availableLimit`: **Campo calculado dinamicamente** (não armazenado no banco)
  - Fórmula: `availableLimit = creditLimit - SUM(installments de faturas abertas) + SUM(partial_payments)`
  - O limite disponível pode **exceder** o `creditLimit` se houver pagamentos parciais que gerem crédito
- Validação de compras: `newBill.totalAmount <= creditCard.availableLimit`
- **Pagamentos Parciais:** Se `allowsPartialPayment = true`, permite registrar pagamentos antecipados na fatura atual (aberta) antes do fechamento
- Pagamentos parciais **podem exceder** o valor da fatura, criando saldo negativo (crédito a favor)
- Saldo negativo de fatura fechada é **transferido automaticamente** para a próxima fatura

---

### 3. Invoice 🔨 Planejado

**Propósito:** Representa um período de faturamento de um cartão de crédito, agregando todas as parcelas daquele mês.

#### Modelo de Domínio Planejado

```java
public class Invoice {
    private Long id;                    // Identificador único
    private Long creditCardId;          // Referência ao cartão de crédito
    private LocalDate referenceMonth;   // Mês/ano da fatura (ex: 2025-01)
    private BigDecimal totalAmount;     // Soma de todas as parcelas + saldo anterior
    private BigDecimal previousBalance; // Saldo transferido da fatura anterior (pode ser negativo)
    private boolean closed;             // Se a fatura está fechada para edição
    private boolean paid;               // Se a fatura foi paga (total ≤ 0 ou pagamento confirmado)
    private LocalDateTime createdAt;    // Timestamp de criação
    private LocalDateTime updatedAt;    // Timestamp da última atualização
}
```

#### Schema de Banco de Dados Planejado

**Tabela:** `invoices`

| Coluna | Tipo | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PRIMARY KEY |
| credit_card_id | BIGINT | NOT NULL, FOREIGN KEY → credit_cards(id) |
| reference_month | DATE | NOT NULL |
| total_amount | NUMERIC(10,2) | NOT NULL, DEFAULT 0.00 |
| previous_balance | NUMERIC(10,2) | NOT NULL, DEFAULT 0.00 |
| closed | BOOLEAN | NOT NULL, DEFAULT FALSE |
| paid | BOOLEAN | NOT NULL, DEFAULT FALSE |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

**Índices:**
- `idx_invoice_card_month` em (`credit_card_id`, `reference_month`) - UNIQUE

**Observações:**
- `previousBalance` pode ser **negativo** (crédito) ou **positivo** (débito)
- `paid = true` quando a fatura foi quitada ou tem saldo ≤ 0

#### Regras de Negócio Planejadas
- Uma fatura por cartão de crédito por mês
- O valor total é calculado automaticamente a partir da soma das parcelas
- Faturas fechadas não podem aceitar novas parcelas
- Faturas são criadas automaticamente quando a primeira parcela é atribuída

---

### 4. Installment 🔨 Planejado

**Propósito:** Representa uma única porção de pagamento de uma conta, vinculada a uma fatura específica.

#### Modelo de Domínio Planejado

```java
public class Installment {
    private Long id;                    // Identificador único
    private Long billId;                // Referência à conta pai
    private Long creditCardId;          // Referência ao cartão de crédito (nullable)
    private Long invoiceId;             // Referência à fatura (nullable)
    private int installmentNumber;      // Número da parcela (ex: 1 de 4)
    private BigDecimal amount;          // Valor desta parcela
    private LocalDate dueDate;          // Quando esta parcela vence
    private LocalDateTime createdAt;    // Timestamp de criação
}
```

#### Schema de Banco de Dados Planejado

**Tabela:** `installments`

| Coluna | Tipo | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PRIMARY KEY |
| bill_id | BIGINT | NOT NULL, FOREIGN KEY → bills(id) ON DELETE CASCADE |
| credit_card_id | BIGINT | NULLABLE, FOREIGN KEY → credit_cards(id) |
| invoice_id | BIGINT | NULLABLE, FOREIGN KEY → invoices(id) |
| installment_number | INTEGER | NOT NULL |
| amount | NUMERIC(10,2) | NOT NULL |
| due_date | DATE | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |

**Índices:**
- `idx_installment_bill` em (`bill_id`)
- `idx_installment_invoice` em (`invoice_id`)
- `idx_installment_due_date` em (`due_date`)

#### Regras de Negócio Planejadas
- Cada parcela pertence a exatamente uma conta
- Parcelas podem ser vinculadas a uma fatura de cartão de crédito (se a conta usar cartão de crédito)
- Contas de pagamento único criam uma parcela
- Contas com múltiplas parcelas criam N parcelas, distribuídas entre futuras faturas
- O número da parcela indica a posição (ex: "3/10" = 3ª de 10 parcelas)

---

### 5. PartialPayment (Pagamento Parcial) 🔨 Planejado

**Propósito:** Registra pagamentos antecipados feitos em uma fatura antes do seu fechamento, reduzindo o valor final a pagar.

#### Modelo de Domínio Planejado

```java
public class PartialPayment {
    private Long id;                    // Identificador único
    private Long invoiceId;             // Referência à fatura
    private BigDecimal amount;          // Valor pago
    private LocalDateTime paymentDate;  // Data/hora do pagamento
    private String description;         // Descrição/observação do pagamento
    private LocalDateTime createdAt;    // Timestamp de criação
}
```

#### Schema de Banco de Dados Planejado

**Tabela:** `partial_payments`

| Coluna | Tipo | Constraints |
|--------|------|-------------|
| id | BIGSERIAL | PRIMARY KEY |
| invoice_id | BIGINT | NOT NULL, FOREIGN KEY → invoices(id) ON DELETE CASCADE |
| amount | NUMERIC(10,2) | NOT NULL, CHECK (amount > 0) |
| payment_date | TIMESTAMP | NOT NULL |
| description | VARCHAR(500) | NULLABLE |
| created_at | TIMESTAMP | NOT NULL |

**Índices:**
- `idx_partial_payment_invoice` em (`invoice_id`)
- `idx_partial_payment_date` em (`payment_date`)

#### Regras de Negócio Planejadas
- Pagamentos parciais só podem ser registrados em faturas **abertas** (`closed = false`)
- Só podem ser registrados se o cartão de crédito tiver `allowsPartialPayment = true`
- O valor do pagamento deve ser positivo e menor ou igual ao total atual da fatura
- Múltiplos pagamentos parciais podem ser feitos na mesma fatura
- Ao fechar a fatura, o valor final será: `totalAmount - SUM(partial_payments.amount)`
- Pagamentos parciais não podem ser editados, apenas deletados (se a fatura ainda estiver aberta)
- Se a fatura for deletada, todos os pagamentos parciais são deletados em cascata

---

### Relacionamentos entre Entidades

```
┌──────────────┐
│  CreditCard  │
│  (Planejado) │
└──────┬───────┘
       │ 1
       │ possui
       │ muitas
       ↓ *
┌──────────────┐
│   Invoice    │◄─────────────────┐
│  (Planejado) │                  │
└──────┬───────┘                  │
       │ 1                        │ 1
       │ contém                   │ possui
       │ muitas                   │ muitos
       ↓ *                        │ *
┌──────────────┐      * pertence a 1  ┌──────────────┐    ┌──────────────────┐
│ Installment  │─────────────────────────│     Bill     │    │ PartialPayment   │
│  (Planejado) │                         │(Implementado)│    │   (Planejado)    │
└──────────────┘                         └──────────────┘    └──────────────────┘
```

**Cardinalidades:**
- `Bill 1 ──── * Installment` (uma conta tem muitas parcelas)
- `CreditCard 1 ──── * Invoice` (um cartão de crédito tem muitas faturas)
- `Invoice 1 ──── * Installment` (uma fatura contém muitas parcelas)
- `Invoice 1 ──── * PartialPayment` (uma fatura pode ter muitos pagamentos parciais)

---

## Data Transfer Objects (DTOs)

### DTOs de Bill ✅ Implementado

#### BillRequestDTO (Entrada)
**Arquivo:** `src/main/java/com/truebalance/truebalance/application/dto/input/BillRequestDTO.java`

**Propósito:** Recebe dados do cliente ao criar ou atualizar uma conta.

```java
public class BillRequestDTO {
    private String name;
    private LocalDateTime executionDate;
    private BigDecimal totalAmount;
    private int numberOfInstallments;
    private String description;

    // Método de conversão
    public Bill toBill() {
        // Converte DTO para entidade de domínio
    }
}
```

**Regras de Validação (Atual):**
- Todos os campos são obrigatórios exceto `description`
- `totalAmount` deve ser positivo
- `numberOfInstallments` deve ser >= 1

**Exemplo JSON:**
```json
{
  "name": "Grocery Shopping",
  "executionDate": "2025-01-10T10:00:00",
  "totalAmount": 400.00,
  "numberOfInstallments": 4,
  "description": "Monthly groceries"
}
```

---

#### BillResponseDTO (Saída)
**Arquivo:** `src/main/java/com/truebalance/truebalance/application/dto/output/BillRespondeDTO.java`

**Propósito:** Retorna dados da conta para o cliente.

```java
public class BillRespondeDTO {
    private Long id;
    private String name;
    private LocalDateTime executionDate;
    private BigDecimal totalAmount;
    private int numberOfInstallments;
    private BigDecimal installmentAmount;  // Campo calculado
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Método factory
    public static BillRespondeDTO fromBill(Bill bill) {
        // Converte entidade de domínio para DTO
    }
}
```

**Exemplo JSON:**
```json
{
  "id": 1,
  "name": "Grocery Shopping",
  "executionDate": "2025-01-10T10:00:00",
  "totalAmount": 400.00,
  "numberOfInstallments": 4,
  "installmentAmount": 100.00,
  "description": "Monthly groceries",
  "createdAt": "2025-01-10T10:05:23",
  "updatedAt": "2025-01-10T10:05:23"
}
```

---

### DTOs de Credit Card 🔨 Planejado

#### CreditCardRequestDTO (Entrada)

```java
public class CreditCardRequestDTO {
    private String name;
    private BigDecimal creditLimit;
    private int closingDay;
    private int dueDay;
}
```

**Exemplo JSON:**
```json
{
  "name": "Nubank Gold",
  "creditLimit": 5000.00,
  "closingDay": 10,
  "dueDay": 17
}
```

---

#### CreditCardResponseDTO (Saída)

```java
public class CreditCardResponseDTO {
    private Long id;
    private String name;
    private BigDecimal creditLimit;
    private int closingDay;
    private int dueDay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

### DTOs de Invoice 🔨 Planejado

#### InvoiceResponseDTO (Saída)

```java
public class InvoiceResponseDTO {
    private Long id;
    private Long creditCardId;
    private LocalDate referenceMonth;
    private BigDecimal totalAmount;
    private BigDecimal previousBalance;    // Saldo da fatura anterior (pode ser negativo)
    private boolean closed;
    private boolean paid;                   // Se a fatura foi paga
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Exemplo JSON (Fatura Normal):**
```json
{
  "id": 1,
  "creditCardId": 1,
  "referenceMonth": "2025-01-01",
  "totalAmount": 1250.00,
  "previousBalance": 0.00,
  "closed": false,
  "paid": false,
  "createdAt": "2025-01-01T00:00:00",
  "updatedAt": "2025-01-15T14:30:00"
}
```

**Exemplo JSON (Fatura com Crédito Anterior):**
```json
{
  "id": 2,
  "creditCardId": 1,
  "referenceMonth": "2025-02-01",
  "totalAmount": 60.00,
  "previousBalance": -40.00,
  "closed": false,
  "paid": false,
  "createdAt": "2025-02-01T00:00:00",
  "updatedAt": "2025-02-01T00:00:00"
}
```

---

### DTOs de Installment 🔨 Planejado

#### InstallmentResponseDTO (Saída)

```java
public class InstallmentResponseDTO {
    private Long id;
    private Long billId;
    private Long creditCardId;
    private Long invoiceId;
    private int installmentNumber;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
}
```

**Exemplo JSON:**
```json
{
  "id": 1,
  "billId": 5,
  "creditCardId": 1,
  "invoiceId": 3,
  "installmentNumber": 1,
  "amount": 100.00,
  "dueDate": "2025-02-17",
  "createdAt": "2025-01-10T10:05:23"
}
```

---

### DTOs de PartialPayment 🔨 Planejado

#### PartialPaymentRequestDTO (Entrada)

```java
public class PartialPaymentRequestDTO {
    private BigDecimal amount;
    private String description;
}
```

**Exemplo JSON:**
```json
{
  "amount": 300.00,
  "description": "Pagamento antecipado parcial"
}
```

---

#### PartialPaymentResponseDTO (Saída)

```java
public class PartialPaymentResponseDTO {
    private Long id;
    private Long invoiceId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String description;
    private LocalDateTime createdAt;
    private BigDecimal creditCardAvailableLimit;  // Novo limite disponível após o pagamento
}
```

**Exemplo JSON:**
```json
{
  "id": 1,
  "invoiceId": 5,
  "amount": 300.00,
  "paymentDate": "2025-01-20T14:30:00",
  "description": "Pagamento antecipado parcial",
  "createdAt": "2025-01-20T14:30:15",
  "creditCardAvailableLimit": 520.00
}
```

**Observação:** O campo `creditCardAvailableLimit` é calculado **em tempo real** e retornado imediatamente após registrar o pagamento, mostrando o novo limite disponível do cartão.

---

## Endpoints da API REST

### Endpoints de Bill

#### 1. Criar Bill ✅ Implementado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | POST |
| **Path** | `/bills` |
| **Descrição** | Cria uma nova conta com cálculo automático de parcelas |
| **Request Body** | `BillRequestDTO` |
| **Response** | `BillResponseDTO` |
| **Status Codes** | 201 Created, 400 Bad Request |
| **Content-Type** | application/json |

**Exemplo de Request:**
```json
{
  "name": "Laptop Purchase",
  "executionDate": "2025-01-15T14:30:00",
  "totalAmount": 3600.00,
  "numberOfInstallments": 12,
  "description": "New MacBook Pro"
}
```

**Exemplo de Response (201 Created):**
```json
{
  "id": 42,
  "name": "Laptop Purchase",
  "executionDate": "2025-01-15T14:30:00",
  "totalAmount": 3600.00,
  "numberOfInstallments": 12,
  "installmentAmount": 300.00,
  "description": "New MacBook Pro",
  "createdAt": "2025-01-15T14:31:05",
  "updatedAt": "2025-01-15T14:31:05"
}
```

**Lógica de Negócio:**
1. Controller recebe `BillRequestDTO`
2. DTO é convertido para entidade de domínio `Bill` via `toBill()`
3. Caso de uso `CreateBill` é invocado com a conta
4. Caso de uso calcula `installmentAmount = totalAmount / numberOfInstallments`
5. Modo de arredondamento: `RoundingMode.HALF_UP` com 2 casas decimais
6. Bill é salva via `BillRepositoryPort.save()`
7. Entidade de domínio é convertida para `BillResponseDTO`
8. Response é retornado com HTTP 201

**Validação:**
- Todos os campos obrigatórios exceto `description`
- `totalAmount` deve ser > 0
- `numberOfInstallments` deve ser >= 1

**Referências de Arquivo:**
- Controller: `src/main/java/com/truebalance/truebalance/application/controller/BillController.java:42`
- Caso de Uso: `src/main/java/com/truebalance/truebalance/domain/usecase/CreateBill.java`

---

#### 2. Listar Todas as Bills ✅ Implementado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | GET |
| **Path** | `/bills` |
| **Descrição** | Recupera todas as contas do sistema |
| **Request Body** | None |
| **Response** | `List<BillResponseDTO>` |
| **Status Codes** | 200 OK |
| **Content-Type** | application/json |

**Exemplo de Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Grocery Shopping",
    "executionDate": "2025-01-10T10:00:00",
    "totalAmount": 400.00,
    "numberOfInstallments": 4,
    "installmentAmount": 100.00,
    "description": "Monthly groceries",
    "createdAt": "2025-01-10T10:05:23",
    "updatedAt": "2025-01-10T10:05:23"
  },
  {
    "id": 2,
    "name": "Netflix Subscription",
    "executionDate": "2025-01-01T00:00:00",
    "totalAmount": 49.90,
    "numberOfInstallments": 1,
    "installmentAmount": 49.90,
    "description": "Monthly subscription",
    "createdAt": "2025-01-01T08:15:00",
    "updatedAt": "2025-01-01T08:15:00"
  }
]
```

**Lógica de Negócio:**
1. Controller invoca o caso de uso `GetAllBills`
2. Caso de uso chama `BillRepositoryPort.findAll()`
3. Todas as contas são recuperadas do banco de dados
4. Cada entidade de domínio `Bill` é convertida para `BillResponseDTO`
5. Lista de DTOs é retornada com HTTP 200

**Referências de Arquivo:**
- Controller: `src/main/java/com/truebalance/truebalance/application/controller/BillController.java:36`
- Caso de Uso: `src/main/java/com/truebalance/truebalance/domain/usecase/GetAllBills.java`

---

#### 3. Atualizar Bill ✅ Implementado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | PUT |
| **Path** | `/bills/{id}` |
| **Descrição** | Atualiza uma conta existente e recalcula o valor da parcela |
| **Path Parameter** | `id` (Long) - Identificador da conta |
| **Request Body** | `BillRequestDTO` |
| **Response** | `BillResponseDTO` |
| **Status Codes** | 200 OK, 404 Not Found, 400 Bad Request |
| **Content-Type** | application/json |

**Exemplo de Request:**
```json
{
  "name": "Laptop Purchase (Updated)",
  "executionDate": "2025-01-15T14:30:00",
  "totalAmount": 4800.00,
  "numberOfInstallments": 12,
  "description": "New MacBook Pro 16-inch"
}
```

**Exemplo de Response (200 OK):**
```json
{
  "id": 42,
  "name": "Laptop Purchase (Updated)",
  "executionDate": "2025-01-15T14:30:00",
  "totalAmount": 4800.00,
  "numberOfInstallments": 12,
  "installmentAmount": 400.00,
  "description": "New MacBook Pro 16-inch",
  "createdAt": "2025-01-15T14:31:05",
  "updatedAt": "2025-01-15T15:22:18"
}
```

**Exemplo de Response (404 Not Found):**
```json
{
  "timestamp": "2025-01-15T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Bill not found with id: 999",
  "path": "/bills/999"
}
```

**Lógica de Negócio:**
1. Controller recebe o ID da conta do path e `BillRequestDTO` do body
2. DTO é convertido para entidade de domínio `Bill`
3. ID da Bill é definido na entidade
4. Caso de uso `UpdateBill` é invocado
5. Caso de uso verifica se a conta existe via `BillRepositoryPort.findById()`
6. Se não encontrada, retorna `Optional.empty()` → Controller retorna 404
7. Se encontrada, atualiza todos os campos e recalcula `installmentAmount`
8. Bill atualizada é salva via `BillRepositoryPort.save()`
9. Timestamp `updatedAt` é atualizado automaticamente via hook `@PreUpdate`
10. Entidade de domínio é convertida para `BillResponseDTO`
11. Response é retornado com HTTP 200

**Referências de Arquivo:**
- Controller: `src/main/java/com/truebalance/truebalance/application/controller/BillController.java:52`
- Caso de Uso: `src/main/java/com/truebalance/truebalance/domain/usecase/UpdateBill.java`

---

#### 4. Buscar Bill por ID 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | GET |
| **Path** | `/bills/{id}` |
| **Descrição** | Recupera uma conta específica por seu ID |
| **Path Parameter** | `id` (Long) - Identificador da conta |
| **Response** | `BillResponseDTO` |
| **Status Codes** | 200 OK, 404 Not Found |

**Exemplo de Response (200 OK):**
```json
{
  "id": 42,
  "name": "Laptop Purchase",
  "executionDate": "2025-01-15T14:30:00",
  "totalAmount": 3600.00,
  "numberOfInstallments": 12,
  "installmentAmount": 300.00,
  "description": "New MacBook Pro",
  "createdAt": "2025-01-15T14:31:05",
  "updatedAt": "2025-01-15T14:31:05"
}
```

**Lógica de Negócio Planejada:**
1. Controller recebe o ID da conta do path
2. Caso de uso `GetBillById` é invocado
3. Caso de uso chama `BillRepositoryPort.findById(id)`
4. Se encontrada, converte para DTO e retorna com HTTP 200
5. Se não encontrada, retorna HTTP 404

---

#### 5. Deletar Bill 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | DELETE |
| **Path** | `/bills/{id}` |
| **Descrição** | Deleta uma conta e todas as parcelas associadas |
| **Path Parameter** | `id` (Long) - Identificador da conta |
| **Response** | None (HTTP 204) ou mensagem de erro |
| **Status Codes** | 204 No Content, 404 Not Found, 409 Conflict |

**Lógica de Negócio Planejada:**
1. Controller recebe o ID da conta do path
2. Caso de uso `DeleteBill` é invocado
3. Caso de uso verifica se a conta existe
4. Se a conta está associada a faturas fechadas, retorna HTTP 409 Conflict
5. Deleta em cascata todas as parcelas via `ON DELETE CASCADE`
6. Deleta a conta
7. Retorna HTTP 204 No Content

**Restrições:**
- Não pode deletar contas associadas a faturas fechadas
- Todas as parcelas são deletadas automaticamente (cascata)

---

#### 6. Listar Parcelas da Bill 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | GET |
| **Path** | `/bills/{id}/installments` |
| **Descrição** | Lista todas as parcelas de uma conta específica |
| **Path Parameter** | `id` (Long) - Identificador da conta |
| **Response** | `List<InstallmentResponseDTO>` |
| **Status Codes** | 200 OK, 404 Not Found |

**Exemplo de Response (200 OK):**
```json
[
  {
    "id": 101,
    "billId": 42,
    "creditCardId": 1,
    "invoiceId": 5,
    "installmentNumber": 1,
    "amount": 300.00,
    "dueDate": "2025-02-17",
    "createdAt": "2025-01-15T14:31:05"
  },
  {
    "id": 102,
    "billId": 42,
    "creditCardId": 1,
    "invoiceId": 6,
    "installmentNumber": 2,
    "amount": 300.00,
    "dueDate": "2025-03-17",
    "createdAt": "2025-01-15T14:31:05"
  }
]
```

**Lógica de Negócio Planejada:**
1. Verifica se a conta existe
2. Consulta todas as parcelas onde `bill_id = {id}`
3. Converte para DTOs com referências de fatura e cartão de crédito
4. Retorna ordenado por número da parcela

---

### Endpoints de Credit Card 🔨 Planejado

#### 7. Criar Credit Card 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | POST |
| **Path** | `/credit-cards` |
| **Descrição** | Cria um novo cartão de crédito |
| **Request Body** | `CreditCardRequestDTO` |
| **Response** | `CreditCardResponseDTO` |
| **Status Codes** | 201 Created, 400 Bad Request |

**Exemplo de Request:**
```json
{
  "name": "Nubank Gold",
  "creditLimit": 5000.00,
  "closingDay": 10,
  "dueDay": 17
}
```

**Exemplo de Response (201 Created):**
```json
{
  "id": 1,
  "name": "Nubank Gold",
  "creditLimit": 5000.00,
  "closingDay": 10,
  "dueDay": 17,
  "createdAt": "2025-01-20T09:00:00",
  "updatedAt": "2025-01-20T09:00:00"
}
```

**Lógica de Negócio Planejada:**
1. Valida os dias de fechamento e vencimento (1-31)
2. Garante que dia de vencimento > dia de fechamento (ou no próximo mês)
3. Cria a entidade de cartão de crédito
4. Salva no banco de dados
5. Retorna a entidade criada

---

#### 8. Listar Credit Cards 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | GET |
| **Path** | `/credit-cards` |
| **Descrição** | Lista todos os cartões de crédito |
| **Response** | `List<CreditCardResponseDTO>` |
| **Status Codes** | 200 OK |

---

#### 9. Buscar Credit Card por ID 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | GET |
| **Path** | `/credit-cards/{id}` |
| **Descrição** | Recupera um cartão de crédito específico |
| **Path Parameter** | `id` (Long) - Identificador do cartão de crédito |
| **Response** | `CreditCardResponseDTO` |
| **Status Codes** | 200 OK, 404 Not Found |

---

#### 10. Atualizar Credit Card 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | PUT |
| **Path** | `/credit-cards/{id}` |
| **Descrição** | Atualiza informações do cartão de crédito |
| **Request Body** | `CreditCardRequestDTO` |
| **Response** | `CreditCardResponseDTO` |
| **Status Codes** | 200 OK, 404 Not Found |

---

#### 11. Deletar Credit Card 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | DELETE |
| **Path** | `/credit-cards/{id}` |
| **Descrição** | Deleta um cartão de crédito |
| **Status Codes** | 204 No Content, 404 Not Found, 409 Conflict |

**Restrição:** Não pode deletar cartão de crédito com faturas ou contas existentes.

---

#### 11.1. Consultar Limite Disponível do Credit Card 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | GET |
| **Path** | `/credit-cards/{id}/available-limit` |
| **Descrição** | Calcula e retorna o limite disponível considerando faturas e pagamentos parciais |
| **Path Parameter** | `id` (Long) - Identificador do cartão de crédito |
| **Response** | JSON com informações de limite |
| **Status Codes** | 200 OK, 404 Not Found |

**Exemplo de Response 1 - Sem Pagamentos Parciais:**
```json
{
  "creditCardId": 1,
  "creditLimit": 1000.00,
  "usedLimit": 800.00,
  "partialPaymentsTotal": 0.00,
  "availableLimit": 200.00
}
```

**Exemplo de Response 2 - Com Pagamentos Parciais (Limite Expandido):**
```json
{
  "creditCardId": 1,
  "creditLimit": 100.00,
  "usedLimit": 80.00,
  "partialPaymentsTotal": 120.00,
  "availableLimit": 140.00
}
```

**Lógica de Negócio Planejada:**
1. Verifica se o cartão de crédito existe (404 se não encontrado)
2. Calcula `usedLimit = SUM(installments.amount WHERE invoices.credit_card_id = {id} AND invoices.closed = false)`
3. Calcula `partialPaymentsTotal = SUM(partial_payments.amount WHERE invoices.credit_card_id = {id} AND invoices.closed = false)`
4. Calcula `availableLimit = creditLimit - usedLimit + partialPaymentsTotal`
5. Retorna objeto com:
   - `creditCardId`: ID do cartão
   - `creditLimit`: Limite total fixo do cartão
   - `usedLimit`: Valor usado em faturas abertas
   - `partialPaymentsTotal`: Soma de pagamentos parciais em faturas abertas
   - `availableLimit`: Limite disponível (pode exceder creditLimit)

**Observações:**
- O `availableLimit` pode ser **maior** que `creditLimit` se houver pagamentos parciais
- Apenas faturas **abertas** (`closed = false`) são consideradas no cálculo
- Faturas fechadas e pagas não afetam o limite disponível

---

### Endpoints de Invoice 🔨 Planejado

#### 12. Listar Faturas do Credit Card 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | GET |
| **Path** | `/credit-cards/{id}/invoices` |
| **Descrição** | Lista todas as faturas de um cartão de crédito específico |
| **Path Parameter** | `id` (Long) - Identificador do cartão de crédito |
| **Query Parameters** | `year` (opcional), `status` (open/closed, opcional) |
| **Response** | `List<InvoiceResponseDTO>` |
| **Status Codes** | 200 OK, 404 Not Found |

**Exemplo de Response:**
```json
[
  {
    "id": 1,
    "creditCardId": 1,
    "referenceMonth": "2025-01-01",
    "totalAmount": 1250.00,
    "closed": false,
    "createdAt": "2025-01-01T00:00:00",
    "updatedAt": "2025-01-15T14:30:00"
  },
  {
    "id": 2,
    "creditCardId": 1,
    "referenceMonth": "2024-12-01",
    "totalAmount": 3456.78,
    "closed": true,
    "createdAt": "2024-12-01T00:00:00",
    "updatedAt": "2024-12-10T23:59:59"
  }
]
```

---

#### 13. Buscar Invoice por ID 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | GET |
| **Path** | `/invoices/{id}` |
| **Descrição** | Recupera detalhes da fatura incluindo todas as parcelas |
| **Path Parameter** | `id` (Long) - Identificador da fatura |
| **Response** | `InvoiceResponseDTO` com parcelas embutidas |
| **Status Codes** | 200 OK, 404 Not Found |

---

#### 14. Fechar Invoice 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | POST |
| **Path** | `/invoices/{id}/close` |
| **Descrição** | Fecha uma fatura, calcula saldo final e transfere crédito se necessário |
| **Path Parameter** | `id` (Long) - Identificador da fatura |
| **Response** | `InvoiceResponseDTO` |
| **Status Codes** | 200 OK, 404 Not Found, 400 Bad Request |

**Request Body:** None

**Exemplo 1 - Response com Saldo Positivo (200 OK):**
```json
{
  "id": 1,
  "creditCardId": 1,
  "referenceMonth": "2025-01-01",
  "totalAmount": 150.00,
  "previousBalance": 0.00,
  "closed": true,
  "paid": false,
  "createdAt": "2025-01-01T00:00:00",
  "updatedAt": "2025-01-31T23:59:59"
}
```

**Exemplo 2 - Response com Saldo Negativo (Crédito Transferido):**
```json
{
  "id": 2,
  "creditCardId": 1,
  "referenceMonth": "2025-01-01",
  "totalAmount": -40.00,
  "previousBalance": 0.00,
  "closed": true,
  "paid": true,
  "createdAt": "2025-01-01T00:00:00",
  "updatedAt": "2025-01-31T23:59:59"
}
```

**Lógica de Negócio Planejada:**
1. Verifica se a fatura existe e não está fechada (404 se não existe, 400 se já fechada)
2. Calcula pagamentos parciais: `partialPaymentsTotal = SUM(partial_payments.amount)`
3. Calcula saldo final: `finalAmount = totalAmount - partialPaymentsTotal`
4. **Se `finalAmount < 0`** (crédito a favor):
   - Atualiza `totalAmount` para o valor final (negativo)
   - Define `paid = true` (fatura já está paga)
   - Busca ou cria a próxima fatura do mesmo cartão
   - Atualiza `nextInvoice.previousBalance = finalAmount` (negativo)
   - Recalcula `nextInvoice.totalAmount = SUM(installments) + previousBalance`
5. **Se `finalAmount == 0`**:
   - Atualiza `totalAmount` para 0
   - Define `paid = true` (totalmente paga)
6. **Se `finalAmount > 0`**:
   - Atualiza `totalAmount` para o valor final
   - Define `paid = false` (ainda há saldo a pagar)
7. Define `closed = true`
8. Atualiza timestamp `updatedAt`
9. Salva a fatura e próxima fatura (se aplicável)
10. Retorna a fatura atualizada

**Restrições:**
- Fatura deve existir
- Fatura não deve estar fechada
- Após fechar, novas parcelas não podem ser adicionadas
- Pagamentos parciais não podem mais ser adicionados ou deletados

---

#### 15. Listar Parcelas da Invoice 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | GET |
| **Path** | `/invoices/{id}/installments` |
| **Descrição** | Lista todas as parcelas dentro de uma fatura específica |
| **Path Parameter** | `id` (Long) - Identificador da fatura |
| **Response** | `List<InstallmentResponseDTO>` |
| **Status Codes** | 200 OK, 404 Not Found |

**Exemplo de Response:**
```json
[
  {
    "id": 101,
    "billId": 42,
    "creditCardId": 1,
    "invoiceId": 5,
    "installmentNumber": 1,
    "amount": 300.00,
    "dueDate": "2025-02-17",
    "createdAt": "2025-01-15T14:31:05"
  },
  {
    "id": 105,
    "billId": 43,
    "creditCardId": 1,
    "invoiceId": 5,
    "installmentNumber": 1,
    "amount": 150.00,
    "dueDate": "2025-02-17",
    "createdAt": "2025-01-18T10:22:33"
  }
]
```

---

### Endpoints de PartialPayment (Pagamento Parcial) 🔨 Planejado

#### 16. Registrar Pagamento Parcial 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | POST |
| **Path** | `/invoices/{id}/partial-payments` |
| **Descrição** | Registra um pagamento antecipado parcial em uma fatura aberta |
| **Path Parameter** | `id` (Long) - Identificador da fatura |
| **Request Body** | `PartialPaymentRequestDTO` |
| **Response** | `PartialPaymentResponseDTO` |
| **Status Codes** | 201 Created, 400 Bad Request, 404 Not Found, 409 Conflict |

**Request Example:**
```json
{
  "amount": 300.00,
  "description": "Pagamento antecipado parcial da fatura de janeiro"
}
```

**Response Example (201 Created):**
```json
{
  "id": 1,
  "invoiceId": 5,
  "amount": 300.00,
  "paymentDate": "2025-01-20T14:30:00",
  "description": "Pagamento antecipado parcial da fatura de janeiro",
  "createdAt": "2025-01-20T14:30:15",
  "creditCardAvailableLimit": 520.00
}
```

**Observação:** O campo `creditCardAvailableLimit` mostra o **novo limite disponível** do cartão **IMEDIATAMENTE após** o registro do pagamento, refletindo o aumento em tempo real.

**Response Example (400 Bad Request):**
```json
{
  "timestamp": "2025-01-20T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Valor do pagamento (600.00) excede o saldo da fatura (500.00)",
  "path": "/invoices/5/partial-payments"
}
```

**Response Example (409 Conflict):**
```json
{
  "timestamp": "2025-01-20T14:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Fatura está fechada. Não é possível registrar pagamentos parciais.",
  "path": "/invoices/5/partial-payments"
}
```

**Lógica de Negócio Planejada:**
1. Verifica se a fatura existe (404 se não encontrada)
2. Busca o cartão de crédito associado à fatura
3. Valida se o cartão permite pagamentos parciais (`allowsPartialPayment = true`)
   - Se não permitir, retorna HTTP 409 Conflict
4. Verifica se a fatura está aberta (`closed = false`)
   - Se fechada, retorna HTTP 409 Conflict
5. Valida se o valor do pagamento é positivo (`amount > 0`)
   - Se não for, retorna HTTP 400 Bad Request
6. Cria o registro de pagamento parcial com `paymentDate = LocalDateTime.now()`
7. Salva no banco de dados via `PartialPaymentRepositoryPort`
8. **Calcula o novo limite disponível do cartão IMEDIATAMENTE:**
   ```
   availableLimit = creditLimit - SUM(installments de faturas abertas) + SUM(partial_payments de faturas abertas)
   ```
9. Retorna o pagamento criado com HTTP 201 **incluindo o novo limite disponível**

**Validações:**
- Cartão de crédito deve ter `allowsPartialPayment = true`
- Fatura deve estar aberta (`closed = false`)
- Valor deve ser positivo (`amount > 0`)
- ✅ **Valor PODE exceder o saldo da fatura** (cria saldo negativo/crédito)

**Impacto Imediato:**
- 💰 Limite disponível do cartão aumenta **IMEDIATAMENTE** pelo valor do pagamento
- 📊 Saldo da fatura diminui (pode ficar negativo)
- 🚀 Usuário pode fazer novas compras com o limite aumentado **SEM esperar fechamento**

---

#### 17. Listar Pagamentos Parciais da Invoice 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | GET |
| **Path** | `/invoices/{id}/partial-payments` |
| **Descrição** | Lista todos os pagamentos parciais de uma fatura |
| **Path Parameter** | `id` (Long) - Identificador da fatura |
| **Response** | `List<PartialPaymentResponseDTO>` |
| **Status Codes** | 200 OK, 404 Not Found |

**Response Example (200 OK):**
```json
[
  {
    "id": 1,
    "invoiceId": 5,
    "amount": 300.00,
    "paymentDate": "2025-01-20T14:30:00",
    "description": "Primeiro pagamento parcial",
    "createdAt": "2025-01-20T14:30:15"
  },
  {
    "id": 2,
    "invoiceId": 5,
    "amount": 150.00,
    "paymentDate": "2025-01-22T10:15:00",
    "description": "Segundo pagamento parcial",
    "createdAt": "2025-01-22T10:15:10"
  }
]
```

**Lógica de Negócio Planejada:**
1. Verifica se a fatura existe
2. Busca todos os pagamentos parciais onde `invoice_id = {id}`
3. Ordena por data de pagamento (mais recente primeiro)
4. Converte para DTOs
5. Retorna lista com HTTP 200

---

#### 18. Deletar Pagamento Parcial 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | DELETE |
| **Path** | `/partial-payments/{id}` |
| **Descrição** | Remove um pagamento parcial de uma fatura aberta |
| **Path Parameter** | `id` (Long) - Identificador do pagamento parcial |
| **Response** | None (HTTP 204) |
| **Status Codes** | 204 No Content, 404 Not Found, 409 Conflict |

**Lógica de Negócio Planejada:**
1. Verifica se o pagamento parcial existe
2. Busca a fatura associada
3. Verifica se a fatura está aberta (`closed = false`)
4. Se fechada, retorna HTTP 409 Conflict com mensagem "Não é possível deletar pagamento de fatura fechada"
5. Deleta o pagamento parcial
6. Retorna HTTP 204 No Content

**Restrições:**
- Só pode deletar pagamento de fatura aberta
- Não pode editar pagamento parcial, apenas deletar e criar novo

---

#### 18.1. Pagar Fatura Integral 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | POST |
| **Path** | `/invoices/{id}/pay` |
| **Descrição** | Registra o pagamento integral de uma fatura (geralmente fechada) |
| **Path Parameter** | `id` (Long) - Identificador da fatura |
| **Response** | JSON com confirmação e novo limite disponível |
| **Status Codes** | 200 OK, 404 Not Found, 400 Bad Request |

**Request Example (Pagamento Exato):**
```json
{
  "amount": 100.00,
  "paymentDate": "2025-01-25T10:00:00",
  "description": "Pagamento via PIX"
}
```

**Request Example (Pagamento com Valor Diferente - Opcional):**
```json
{
  "amount": 120.00,
  "paymentDate": "2025-01-25T10:00:00",
  "description": "Pagamento com juros/desconto"
}
```

**Response Example (200 OK):**
```json
{
  "invoiceId": 5,
  "previousBalance": 100.00,
  "amountPaid": 100.00,
  "newBalance": 0.00,
  "paid": true,
  "creditCardAvailableLimit": 1000.00,
  "paymentDate": "2025-01-25T10:00:00"
}
```

**Lógica de Negócio Planejada:**
1. Verifica se a fatura existe (404 se não encontrada)
2. Busca o cartão de crédito associado à fatura
3. Calcula saldo atual da fatura: `currentBalance = totalAmount - SUM(partial_payments)`
4. Valida se o valor do pagamento é positivo (`amount > 0`)

**5. Validações Específicas por Tipo de Cartão:**

**Se `allowsPartialPayment = false` (Cartão SEM Pagamento Parcial):**
- ✅ Verifica se a fatura está **fechada** (`closed = true`)
  - Se fatura aberta, retorna HTTP 409 Conflict: "Pagamento só permitido após fechamento"
- ✅ Verifica se o valor é **exatamente igual** ao saldo atual
  - Se `amount != currentBalance`, retorna HTTP 400 Bad Request: "Valor deve ser exatamente R$ X,XX"
- ✅ Registra o pagamento integral
- ✅ Marca fatura como `paid = true`
- ❌ **Não permite** saldo negativo (sempre `newBalance = 0`)

**Se `allowsPartialPayment = true` (Cartão COM Pagamento Parcial):**
- ✅ Permite pagamento a **qualquer momento** (fatura aberta ou fechada)
- ✅ Permite **qualquer valor** (pode ser maior, menor ou igual ao saldo)
- ✅ Calcula novo saldo: `newBalance = currentBalance - amount`
- ✅ Se `newBalance <= 0`:
  - Marca fatura como `paid = true`
  - Se `newBalance < 0`, registra crédito para próxima fatura
- ✅ **Permite** saldo negativo (crédito a favor)

6. **Calcula o novo limite disponível do cartão IMEDIATAMENTE:**
   ```
   availableLimit = creditLimit - SUM(installments de faturas não pagas) + amount_paid
   ```

7. Retorna confirmação com novo limite disponível

**Impacto Imediato:**
- 💰 Limite disponível aumenta **IMEDIATAMENTE** pelo valor pago
- 📊 Fatura é marcada como `paid = true` se totalmente quitada
- 🚀 Usuário pode fazer novas compras com o limite aumentado **SEM esperar**

**Observações - Cartão COM Pagamento Parcial:**
- Pagamento pode ser de **qualquer valor** (não precisa ser exatamente o total da fatura)
- Se pagar **menos** que o total, fatura não é marcada como paga (saldo devedor)
- Se pagar **mais** que o total, excedente vira crédito para próxima fatura
- Limite disponível aumenta **pelo valor efetivamente pago**

**Observações - Cartão SEM Pagamento Parcial:**
- Pagamento **deve ser exatamente** o valor da fatura fechada
- **Não permite** crédito a favor ou saldo negativo
- Limite disponível **nunca excede** o creditLimit fixo
- Fatura **sempre** é marcada como `paid = true` após pagamento

---

#### 19. Obter Saldo Atual da Invoice 🔨 Planejado

| Propriedade | Valor |
|----------|-------|
| **Método HTTP** | GET |
| **Path** | `/invoices/{id}/balance` |
| **Descrição** | Calcula e retorna o saldo atual da fatura incluindo saldo anterior e pagamentos parciais |
| **Path Parameter** | `id` (Long) - Identificador da fatura |
| **Response** | JSON com informações de saldo |
| **Status Codes** | 200 OK, 404 Not Found |

**Response Example 1 - Saldo Positivo (a pagar):**
```json
{
  "invoiceId": 5,
  "totalAmount": 500.00,
  "previousBalance": 0.00,
  "partialPaymentsTotal": 450.00,
  "currentBalance": 50.00,
  "paid": false,
  "closed": false,
  "partialPaymentsCount": 3
}
```

**Response Example 2 - Saldo Negativo (crédito):**
```json
{
  "invoiceId": 6,
  "totalAmount": 80.00,
  "previousBalance": 0.00,
  "partialPaymentsTotal": 120.00,
  "currentBalance": -40.00,
  "paid": false,
  "closed": false,
  "partialPaymentsCount": 1
}
```

**Response Example 3 - Com Saldo Anterior Negativo:**
```json
{
  "invoiceId": 7,
  "totalAmount": 60.00,
  "previousBalance": -40.00,
  "partialPaymentsTotal": 0.00,
  "currentBalance": 60.00,
  "paid": false,
  "closed": false,
  "partialPaymentsCount": 0
}
```

**Lógica de Negócio Planejada:**
1. Verifica se a fatura existe (404 se não encontrada)
2. Busca `previousBalance` da fatura
3. Calcula `partialPaymentsTotal = SUM(partial_payments.amount WHERE invoice_id = {id})`
4. Calcula `currentBalance = totalAmount - partialPaymentsTotal`
5. Conta número de pagamentos parciais
6. Retorna objeto com:
   - `totalAmount`: Valor das parcelas + saldo anterior
   - `previousBalance`: Crédito/débito da fatura anterior
   - `partialPaymentsTotal`: Soma dos pagamentos parciais
   - `currentBalance`: Saldo atual (pode ser negativo)
   - `paid`: Se a fatura está paga
   - `closed`: Se a fatura está fechada
   - `partialPaymentsCount`: Quantidade de pagamentos realizados

---

## Casos de Uso & Lógica de Negócio

### Casos de Uso Implementados ✅

#### 1. CreateBill ✅
**Arquivo:** `src/main/java/com/truebalance/truebalance/domain/usecase/CreateBill.java`

**Propósito:** Cria uma nova conta com cálculo automático de parcelas.

**Entrada:** `Bill` (entidade de domínio)

**Saída:** `Bill` (entidade salva com ID gerado)

**Regras de Negócio:**
1. Calcula o valor da parcela baseado no número de parcelas
2. Arredonda para 2 casas decimais usando modo de arredondamento `HALF_UP`
3. Define timestamps (gerenciado pelo ciclo de vida JPA)
4. Persiste no banco de dados

**Algoritmo:**
```java
if (numberOfInstallments == 1) {
    installmentAmount = totalAmount
} else {
    installmentAmount = totalAmount / numberOfInstallments
    // Arredonda usando HALF_UP para 2 casas decimais
}
```

**Exemplo:**
- Total: $400.00, Parcelas: 4
- Cálculo: 400.00 ÷ 4 = 100.00
- Resultado: installmentAmount = $100.00

**Dependências:**
- `BillRepositoryPort` - para persistência

**Tratamento de Erro:**
- Atualmente sem validação explícita (depende de constraints do banco de dados)

---

#### 2. UpdateBill ✅
**Arquivo:** `src/main/java/com/truebalance/truebalance/domain/usecase/UpdateBill.java`

**Propósito:** Atualiza uma conta existente e recalcula o valor da parcela.

**Entrada:** `Bill` (entidade de domínio com ID)

**Saída:** `Optional<Bill>` (entidade atualizada, ou vazio se não encontrada)

**Regras de Negócio:**
1. Verifica se a conta existe
2. Atualiza todos os campos
3. Recalcula o valor da parcela (mesma lógica do CreateBill)
4. Preserva o timestamp de criação
5. Atualiza o timestamp de modificação

**Algoritmo:**
```java
Optional<Bill> existingBill = repository.findById(bill.getId())
if (existingBill.isEmpty()) {
    return Optional.empty()
}

// Atualiza campos
existingBill.setName(bill.getName())
existingBill.setTotalAmount(bill.getTotalAmount())
// ... outros campos

// Recalcula installmentAmount
existingBill.setInstallmentAmount(
    totalAmount.divide(
        BigDecimal.valueOf(numberOfInstallments),
        2,
        RoundingMode.HALF_UP
    )
)

return Optional.of(repository.save(existingBill))
```

**Dependências:**
- `BillRepositoryPort` - para buscar e atualizar

**Tratamento de Erro:**
- Retorna `Optional.empty()` se a conta não for encontrada
- Controller traduz para HTTP 404

---

#### 3. GetAllBills ✅
**Arquivo:** `src/main/java/com/truebalance/truebalance/domain/usecase/GetAllBills.java`

**Propósito:** Recupera todas as contas do sistema.

**Entrada:** Nenhuma

**Saída:** `List<Bill>` (todas as contas no banco de dados)

**Regras de Negócio:**
- Sem filtragem
- Sem paginação (a ser adicionada no futuro)
- Retorna lista vazia se não existirem contas

**Algoritmo:**
```java
return repository.findAll()
```

**Dependências:**
- `BillRepositoryPort` - para recuperação

---

### Casos de Uso Planejados 🔨

#### 4. GetBillById 🔨
**Propósito:** Recuperar uma conta específica por ID.

**Entrada:** `Long id`

**Saída:** `Optional<Bill>`

---

#### 5. DeleteBill 🔨
**Propósito:** Deletar uma conta e deletar em cascata todas as parcelas.

**Regras de Negócio:**
- Não pode deletar se associada a faturas fechadas
- Deleta parcelas em cascata
- Ajusta totais das faturas se parcelas forem removidas

---

#### 6. CreateCreditCard 🔨
**Propósito:** Criar um novo cartão de crédito.

**Regras de Negócio:**
- Valida dia de fechamento (1-31)
- Valida dia de vencimento (1-31)
- Garante que dia de vencimento é após dia de fechamento no ciclo de faturamento

---

#### 7. CreateBillWithCreditCard 🔨
**Propósito:** Criar uma conta vinculada a um cartão de crédito e gerar parcelas.

**Entrada:** `Bill`, `CreditCard ID`

**Saída:** `Bill` com entidades `Installment` geradas

**Regras de Negócio Complexas:**
1. Calcula valor da parcela (mesma lógica do CreateBill atual)
2. Determina ciclo de faturamento para cada parcela
3. Cria ou encontra fatura para cada período de faturamento
4. Cria entidades de parcela e vincula às faturas
5. Atualiza valores totais das faturas

**Algoritmo (Planejado):**
```
Para cada parcela (1 até N):
  1. Calcula data de vencimento baseada no dia de vencimento do cartão de crédito
  2. Determina em qual período de fatura isso se encaixa
  3. Encontra ou cria fatura para aquele período
  4. Cria entidade de parcela:
     - billId = bill.id
     - creditCardId = creditCard.id
     - invoiceId = invoice.id
     - installmentNumber = parcela atual
     - amount = bill.installmentAmount
     - dueDate = data de vencimento calculada
  5. Adiciona parcela à fatura
  6. Atualiza valor total da fatura
```

**Exemplo:**
- Bill: $1,200.00 em 12 parcelas = $100.00/mês
- Credit Card: dia de fechamento 10, dia de vencimento 17
- Data da compra: 15 de Jan, 2025
- Resultado: Cria 12 parcelas de Fev 2025 a Jan 2026

---

#### 8. CloseInvoice 🔨
**Propósito:** Fechar uma fatura e prevenir modificações futuras.

**Regras de Negócio:**
- Calcula total final das parcelas
- Define flag de fechada
- Previne adição de novas parcelas
- Bloqueia modificações de contas em faturas fechadas

---

#### 9. GenerateInvoiceForPeriod 🔨
**Propósito:** Gerar automaticamente fatura para um período de faturamento.

**Entrada:** `CreditCard ID`, `Reference Month`

**Saída:** `Invoice` (criada ou existente)

**Regras de Negócio:**
- Uma fatura por cartão por mês
- Período da fatura determinado pelo dia de fechamento
- Auto-calcula total inicial como $0.00

---

#### 10. RegisterPartialPayment 🔨
**Propósito:** Registrar um pagamento antecipado parcial em uma fatura aberta.

**Entrada:** `Invoice ID`, `PartialPayment` (amount, description)

**Saída:** `PartialPayment` (entidade salva)

**Regras de Negócio Complexas:**
1. Verifica se a fatura existe
2. Busca o cartão de crédito da fatura
3. Valida se `allowsPartialPayment = true`
4. Valida se fatura está aberta (`closed = false`)
5. Calcula saldo atual da fatura
6. Valida se valor do pagamento não excede o saldo
7. Cria registro de pagamento com data/hora atual
8. Persiste no banco de dados

**Validações:**
- Cartão deve permitir pagamentos parciais
- Fatura deve estar aberta
- Valor > 0
- Valor <= saldo atual da fatura

**Exceções:**
- `InvoiceNotFoundException` - Fatura não encontrada
- `InvoiceClosedException` - Fatura já fechada
- `PartialPaymentNotAllowedException` - Cartão não permite pagamento parcial
- `InvalidPaymentAmountException` - Valor inválido ou excede saldo

---

#### 11. GetInvoiceBalance 🔨
**Propósito:** Calcular e retornar o saldo atual de uma fatura considerando pagamentos parciais.

**Entrada:** `Invoice ID`

**Saída:** Objeto com informações de saldo (`totalAmount`, `partialPaymentsTotal`, `remainingBalance`)

**Algoritmo:**
```java
Invoice invoice = findById(invoiceId);
BigDecimal partialPaymentsTotal = sumPartialPayments(invoiceId);
BigDecimal remainingBalance = invoice.getTotalAmount().subtract(partialPaymentsTotal);

return InvoiceBalance.builder()
    .totalAmount(invoice.getTotalAmount())
    .partialPaymentsTotal(partialPaymentsTotal)
    .remainingBalance(remainingBalance)
    .closed(invoice.isClosed())
    .partialPaymentsCount(countPartialPayments(invoiceId))
    .build();
```

---

#### 12. DeletePartialPayment 🔨
**Propósito:** Remover um pagamento parcial de uma fatura aberta.

**Entrada:** `PartialPayment ID`

**Saída:** Void

**Regras de Negócio:**
- Busca o pagamento parcial
- Verifica se a fatura associada está aberta
- Se fechada, lança exceção
- Deleta o registro

**Exceções:**
- `PartialPaymentNotFoundException` - Pagamento não encontrado
- `InvoiceClosedException` - Não pode deletar pagamento de fatura fechada

---

#### 13. CloseInvoiceWithPartialPayments 🔨
**Propósito:** Atualização do caso de uso CloseInvoice para considerar pagamentos parciais.

**Regras Adicionais:**
- Ao fechar a fatura, o valor final a pagar será: `totalAmount - SUM(partial_payments.amount)`
- O campo `totalAmount` da fatura mantém o valor original das parcelas
- Pagamentos parciais reduzem apenas o valor efetivo a ser pago no fechamento
- Após fechamento, nenhum pagamento parcial pode ser adicionado ou removido

**Algoritmo Atualizado:**
```
1. Verifica se fatura existe e não está fechada
2. Calcula total de parcelas
3. Calcula total de pagamentos parciais
4. Calcula valor final: totalAmount - partialPaymentsTotal
5. Se valor final <= 0, marca fatura como totalmente paga
6. Define closed = true
7. Salva fatura
```

---

## Camada de Repositório

### Interface de Porta (Camada de Domínio)

#### BillRepositoryPort ✅
**Arquivo:** `src/main/java/com/truebalance/truebalance/domain/port/BillRepositoryPort.java`

**Propósito:** Define contrato para persistência de conta, isolando domínio da infraestrutura.

```java
public interface BillRepositoryPort {
    Bill save(Bill bill);
    Optional<Bill> findById(Long id);
    List<Bill> findAll();
    // Planejado: void deleteById(Long id);
}
```

**Princípio Chave:** Esta interface vive na **camada de domínio** mas é implementada na **camada de infraestrutura**, seguindo o Princípio da Inversão de Dependência.

---

### Implementação do Adaptador (Camada de Infraestrutura)

#### BillRepositoryAdapter ✅
**Arquivo:** `src/main/java/com/truebalance/truebalance/infra/db/adapter/BillRepositoryAdapter.java`

**Propósito:** Implementa `BillRepositoryPort` delegando ao repositório Spring Data JPA e convertendo entre entidades de domínio e JPA.

```java
@Component
public class BillRepositoryAdapter implements BillRepositoryPort {

    private final BillRepository billRepository;

    @Override
    public Bill save(Bill bill) {
        BillEntity entity = toEntity(bill);
        BillEntity saved = billRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Bill> findById(Long id) {
        return billRepository.findById(id)
            .map(this::toDomain);
    }

    @Override
    public List<Bill> findAll() {
        return billRepository.findAll()
            .stream()
            .map(this::toDomain)
            .toList();
    }

    // Métodos de conversão
    private BillEntity toEntity(Bill bill) { /* ... */ }
    private Bill toDomain(BillEntity entity) { /* ... */ }
}
```

**Responsabilidades:**
- Converte domínio `Bill` → JPA `BillEntity`
- Converte JPA `BillEntity` → domínio `Bill`
- Delega operações de persistência ao repositório Spring Data

---

### Repositório Spring Data (Camada de Infraestrutura)

#### BillRepository ✅
**Arquivo:** `src/main/java/com/truebalance/truebalance/infra/db/repository/BillRepository.java`

```java
@Repository
public interface BillRepository extends JpaRepository<BillEntity, Long> {
    // Herda:
    // - save(BillEntity)
    // - findById(Long)
    // - findAll()
    // - deleteById(Long)
    // - count()
    // - existsById(Long)
    // ... e mais
}
```

**Fornecido pelo Spring Data JPA:**
- Nenhuma implementação necessária
- Operações CRUD automáticas
- Geração de métodos de consulta
- Gerenciamento de transações

---

### Repositórios Planejados 🔨

#### CreditCardRepositoryPort 🔨
```java
public interface CreditCardRepositoryPort {
    CreditCard save(CreditCard creditCard);
    Optional<CreditCard> findById(Long id);
    List<CreditCard> findAll();
    void deleteById(Long id);
}
```

---

#### InvoiceRepositoryPort 🔨
```java
public interface InvoiceRepositoryPort {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(Long id);
    Optional<Invoice> findByCreditCardIdAndReferenceMonth(Long creditCardId, LocalDate month);
    List<Invoice> findByCreditCardId(Long creditCardId);
    List<Invoice> findByCreditCardIdAndClosed(Long creditCardId, boolean closed);
}
```

---

#### InstallmentRepositoryPort 🔨
```java
public interface InstallmentRepositoryPort {
    Installment save(Installment installment);
    List<Installment> saveAll(List<Installment> installments);
    List<Installment> findByBillId(Long billId);
    List<Installment> findByInvoiceId(Long invoiceId);
    void deleteByBillId(Long billId);
}
```

---

#### PartialPaymentRepositoryPort 🔨
```java
public interface PartialPaymentRepositoryPort {
    PartialPayment save(PartialPayment partialPayment);
    Optional<PartialPayment> findById(Long id);
    List<PartialPayment> findByInvoiceId(Long invoiceId);
    BigDecimal sumByInvoiceId(Long invoiceId);
    int countByInvoiceId(Long invoiceId);
    void deleteById(Long id);
}
```

---

## Schema do Banco de Dados

### Schema Atual (Fase 1) ✅

#### Tabela: `bills`

```sql
CREATE TABLE bills (
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    execution_date          TIMESTAMP NOT NULL,
    total_amount            NUMERIC(10,2) NOT NULL,
    number_of_installments  INTEGER NOT NULL DEFAULT 1,
    installment_amount      NUMERIC(10,2) NOT NULL,
    description             TEXT,
    created_at              TIMESTAMP NOT NULL,
    updated_at              TIMESTAMP NOT NULL
);

CREATE INDEX idx_bills_execution_date ON bills(execution_date);
CREATE INDEX idx_bills_created_at ON bills(created_at);
```

**Constraints:**
- `id` - Chave primária auto-incrementada
- `total_amount` - Não pode ser negativo (imposto pela aplicação)
- `number_of_installments` - Deve ser >= 1 (imposto pela aplicação)

**Modo DDL:** `hibernate.ddl-auto=update` (gera schema automaticamente)

---

### Schema Planejado (Fases 2-3) 🔨

#### Tabela: `credit_cards` 🔨

```sql
CREATE TABLE credit_cards (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    credit_limit    NUMERIC(10,2) NOT NULL,
    closing_day     INTEGER NOT NULL CHECK (closing_day BETWEEN 1 AND 31),
    due_day         INTEGER NOT NULL CHECK (due_day BETWEEN 1 AND 31),
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);
```

---

#### Tabela: `invoices` 🔨

```sql
CREATE TABLE invoices (
    id                BIGSERIAL PRIMARY KEY,
    credit_card_id    BIGINT NOT NULL,
    reference_month   DATE NOT NULL,
    total_amount      NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    closed            BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP NOT NULL,

    CONSTRAINT fk_invoice_credit_card
        FOREIGN KEY (credit_card_id)
        REFERENCES credit_cards(id)
        ON DELETE RESTRICT,

    CONSTRAINT uk_invoice_card_month
        UNIQUE (credit_card_id, reference_month)
);

CREATE INDEX idx_invoice_card ON invoices(credit_card_id);
CREATE INDEX idx_invoice_reference_month ON invoices(reference_month);
CREATE INDEX idx_invoice_closed ON invoices(closed);
```

**Constraints:**
- Uma fatura por cartão por mês (constraint única)
- Não pode deletar cartão de crédito se faturas existirem (RESTRICT)

---

#### Tabela: `installments` 🔨

```sql
CREATE TABLE installments (
    id                   BIGSERIAL PRIMARY KEY,
    bill_id              BIGINT NOT NULL,
    credit_card_id       BIGINT,
    invoice_id           BIGINT,
    installment_number   INTEGER NOT NULL,
    amount               NUMERIC(10,2) NOT NULL,
    due_date             DATE NOT NULL,
    created_at           TIMESTAMP NOT NULL,

    CONSTRAINT fk_installment_bill
        FOREIGN KEY (bill_id)
        REFERENCES bills(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_installment_credit_card
        FOREIGN KEY (credit_card_id)
        REFERENCES credit_cards(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_installment_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_installment_bill ON installments(bill_id);
CREATE INDEX idx_installment_invoice ON installments(invoice_id);
CREATE INDEX idx_installment_credit_card ON installments(credit_card_id);
CREATE INDEX idx_installment_due_date ON installments(due_date);
```

**Constraints:**
- Deleta em cascata quando a conta é deletada
- Anula referências se cartão de crédito ou fatura for deletada (SET NULL)

---

#### Tabela: `partial_payments` 🔨

```sql
CREATE TABLE partial_payments (
    id                BIGSERIAL PRIMARY KEY,
    invoice_id        BIGINT NOT NULL,
    amount            NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    payment_date      TIMESTAMP NOT NULL,
    description       VARCHAR(500),
    created_at        TIMESTAMP NOT NULL,

    CONSTRAINT fk_partial_payment_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_partial_payment_invoice ON partial_payments(invoice_id);
CREATE INDEX idx_partial_payment_date ON partial_payments(payment_date);
```

**Constraints:**
- Deleta em cascata quando a fatura é deletada
- Valor deve ser positivo
- Não pode ser editado, apenas deletado (se fatura aberta)

---

### Diagrama de Relacionamento de Entidades (ERD)

```
┌─────────────────────┐
│   credit_cards      │
│─────────────────────│
│ PK  id              │
│     name            │
│     credit_limit    │
│     closing_day     │
│     due_day         │
│     allows_partial  │
└──────────┬──────────┘
           │ 1
           │
           │ *
┌──────────▼──────────┐
│     invoices        │◄──────────────────┐
│─────────────────────│                   │
│ PK  id              │                   │ *
│ FK  credit_card_id  │                   │
│     reference_month │            ┌──────┴──────────────┐
│     total_amount    │            │  partial_payments   │
│     closed          │            │─────────────────────│
└──────────┬──────────┘            │ PK  id              │
           │ 1                     │ FK  invoice_id      │
           │                       │     amount          │
           │ *                     │     payment_date    │
┌──────────▼──────────┐            │     description     │
│   installments      │            └─────────────────────┘
│─────────────────────│◄──────┐
│ PK  id              │       │ *
│ FK  bill_id         │       │
│ FK  credit_card_id  │       │ 1
│ FK  invoice_id      │    ┌──┴──────────────────┐
│     installment_num │    │       bills         │
│     amount          │    │─────────────────────│
│     due_date        │    │ PK  id              │
└─────────────────────┘    │     name            │
                           │     execution_date  │
                           │     total_amount    │
                           │     num_installments│
                           │     installment_amt │
                           │     description     │
                           └─────────────────────┘
```

---

## Regras de Negócio

### Implementação Atual ✅

#### Regra 1: Cálculo de Parcelas
**Fórmula:** `installmentAmount = totalAmount / numberOfInstallments`

**Arredondamento:** `RoundingMode.HALF_UP` com 2 casas decimais

**Exemplos:**
- $400.00 ÷ 4 = $100.00
- $100.00 ÷ 3 = $33.33 (33.33 + 33.33 + 33.34)
- $10.00 ÷ 1 = $10.00

**Localização da Implementação:**
- `CreateBill.java:18-25`
- `UpdateBill.java:22-29`

---

#### Regra 2: Pagamento Único
Quando `numberOfInstallments = 1`:
- Conta é considerada pagamento único (à vista)
- `installmentAmount = totalAmount`
- Não há necessidade de distribuição de parcelas

---

#### Regra 3: Timestamps Automáticos
- `createdAt` é definido automaticamente na criação da entidade (`@PrePersist`)
- `updatedAt` é atualizado automaticamente na modificação da entidade (`@PreUpdate`)

---

### Regras de Negócio Planejadas 🔨

#### Regra 4: Ciclo de Faturamento do Cartão de Crédito
**Dia de Fechamento:** Dia do mês quando a fatura fecha (ex: dia 10)
**Dia de Vencimento:** Dia do mês quando o pagamento vence (ex: dia 17)

**Cenários de Compra:**
- Compra antes do dia de fechamento → Fatura atual
- Compra depois do dia de fechamento → Próxima fatura

**Exemplo:**
- Cartão: dia de fechamento 10, dia de vencimento 17
- Compra em 5 de Jan → Fatura de Jan (vence em 17 de Jan)
- Compra em 15 de Jan → Fatura de Fev (vence em 17 de Fev)

---

#### Regra 5: Distribuição de Parcelas para Faturas
Ao criar uma conta com N parcelas em um cartão de crédito:

1. Calcula valor da parcela (regra atual)
2. Para cada parcela (1 até N):
   - Determina período da fatura baseado na data de compra e dia de fechamento
   - Cria entidade de parcela com data de vencimento
   - Vincula à fatura apropriada
   - Adiciona valor da parcela ao total da fatura

**Exemplo:**
- Conta: $1,200 em 12 parcelas = $100/mês
- Compra: 15 de Jan, 2025
- Cartão: fechamento 10, vencimento 17
- Resultado:
  - Parcela 1 → Fatura de Fev 2025 (vence 17 de Fev)
  - Parcela 2 → Fatura de Mar 2025 (vence 17 de Mar)
  - ...
  - Parcela 12 → Fatura de Jan 2026 (vence 17 de Jan)

---

#### Regra 6: Cálculo do Total da Fatura
`invoice.totalAmount = SUM(installments.amount WHERE invoice_id = invoice.id)`

- Recalculado quando parcelas são adicionadas
- Recalculado quando parcelas são removidas
- Cálculo final quando a fatura é fechada

---

#### Regra 7: Restrições de Fatura Fechada
Uma vez que uma fatura é fechada (`closed = true`):
- Novas parcelas não podem ser adicionadas
- Parcelas existentes não podem ser modificadas
- Contas vinculadas a faturas fechadas não podem ser deletadas
- Valor total é bloqueado

---

#### Regra 8: Validação de Limite de Crédito
Ao criar uma conta com cartão de crédito:
- Verifica totais das faturas abertas atuais
- Verifica: `SUM(open_invoices.total) + new_bill.total <= credit_card.limit`
- Rejeita se o limite for excedido

---

#### Regra 9: Deleção em Cascata
Ao deletar uma conta:
- Todas as parcelas associadas são deletadas (`ON DELETE CASCADE`)
- Totais das faturas são recalculados (subtrai valores das parcelas deletadas)
- Não pode deletar se alguma parcela estiver em uma fatura fechada

---

#### Regra 10: Tipos de Pagamento - Permissões e Restrições

O comportamento de pagamentos varia conforme a configuração do cartão:

### Cartões COM Pagamento Parcial (`allowsPartialPayment = true`)

**Permissões:**
- ✅ Pagamentos **antes** do fechamento da fatura (parciais)
- ✅ Pagamentos **após** o fechamento da fatura (integrais)
- ✅ Valor pode **exceder** o total da fatura
- ✅ Fatura **pode ficar negativa** (crédito a favor)
- ✅ Limite disponível **pode exceder** o creditLimit fixo

**Comportamento:**
- Pagamentos parciais aumentam o limite imediatamente
- Saldo negativo é transferido para próxima fatura
- Máxima flexibilidade de pagamento

### Cartões SEM Pagamento Parcial (`allowsPartialPayment = false`)

**Restrições:**
- ❌ Pagamentos **antes** do fechamento da fatura **NÃO são permitidos**
- ✅ Pagamentos **apenas após** o fechamento da fatura
- ✅ Valor do pagamento = **valor do fechamento da fatura**
- ❌ Fatura **NUNCA fica negativa**
- ❌ Limite disponível **NUNCA excede** o creditLimit fixo

**Comportamento:**
- Pagamento só pode ser feito quando `closed = true`
- Valor pago sempre será o `totalAmount` da fatura fechada
- Limite aumenta exatamente pelo valor da fatura paga
- Não há possibilidade de crédito a favor

**Configuração Padrão:**
- Por padrão, novos cartões terão `allowsPartialPayment = false`
- Esta configuração pode ser alterada a qualquer momento pelo usuário

---

#### Regra 11: Pagamentos Parciais - Restrições de Fatura

**⚠️ APLICA-SE APENAS A:** Cartões com `allowsPartialPayment = true`

Pagamentos parciais só podem ser registrados se:
- A fatura está **aberta** (`closed = false`)
- O cartão de crédito permite pagamentos parciais (`allowsPartialPayment = true`)
- O valor do pagamento é **positivo** (`amount > 0`)

**IMPORTANTE:** Em cartões com pagamento parcial, pagamentos **PODEM EXCEDER** o valor atual da fatura, criando um **saldo negativo** (crédito a favor do usuário).

**⚠️ CARTÕES SEM PAGAMENTO PARCIAL:** Não aplicável - pagamento só ocorre após fechamento com valor exato da fatura.

**Cálculo do Saldo Atual da Fatura:**
```
saldoAtual = totalAmount - SUM(partial_payments.amount)
```

**Exemplos:**

**Cenário 1: Pagamento Parcial Normal**
- Fatura com total de R$ 500,00
- Pagamentos parciais já feitos: R$ 200,00 + R$ 150,00 = R$ 350,00
- Saldo atual: R$ 500,00 - R$ 350,00 = R$ 150,00 (a pagar)

**Cenário 2: Pagamento Excede Valor da Fatura (Saldo Negativo)**
- Fatura com total de R$ 80,00
- Pagamento parcial de R$ 120,00
- Saldo atual: R$ 80,00 - R$ 120,00 = **-R$ 40,00** (crédito a favor)
- Limite disponível aumenta em R$ 120,00 (ver Regra 15)

---

#### Regra 12: Pagamentos Parciais - Impacto no Fechamento
Ao fechar uma fatura que possui pagamentos parciais:

**Valor Final a Pagar:**
```
valorFinal = totalAmount - SUM(partial_payments.amount)
```

**Comportamentos:**

1. **Se `valorFinal > 0`**: Fatura fecha com saldo devedor
   - Cliente deve pagar o valor restante
   - Fatura fica com status normal (pendente de pagamento)

2. **Se `valorFinal = 0`**: Fatura totalmente paga
   - Sistema marca a fatura como `paid = true`
   - Não há cobrança para o cliente

3. **Se `valorFinal < 0`**: Fatura com crédito (saldo negativo)
   - Sistema marca a fatura como `paid = true` (não há nada a pagar)
   - O saldo negativo é **transferido para a próxima fatura** (ver Regra 16)
   - Cliente tem crédito a favor que será usado na próxima fatura

**Exemplo 1: Saldo Devedor**
1. Fatura criada com 4 parcelas de R$ 100,00 cada = R$ 400,00
2. Usuário faz pagamento parcial de R$ 150,00 em 10/01
3. Usuário faz pagamento parcial de R$ 100,00 em 15/01
4. Total de pagamentos parciais: R$ 250,00
5. Ao fechar a fatura em 20/01:
   - `totalAmount` = R$ 400,00
   - `partialPaymentsTotal` = R$ 250,00
   - `valorFinal` = R$ 150,00 ✅ (cliente deve pagar)
   - `paid` = false

**Exemplo 2: Saldo Negativo (Crédito)**
1. Fatura criada com parcelas totalizando R$ 80,00
2. Usuário faz pagamento parcial de R$ 120,00
3. Ao fechar a fatura:
   - `totalAmount` = R$ 80,00
   - `partialPaymentsTotal` = R$ 120,00
   - `valorFinal` = **-R$ 40,00** ✅ (crédito a favor)
   - `paid` = true
   - Saldo de -R$ 40,00 é transferido para a próxima fatura

---

#### Regra 13: Pagamentos Parciais - Deleção
Pagamentos parciais podem ser deletados apenas se:
- A fatura ainda está **aberta** (`closed = false`)
- Não há validação de valor mínimo para deletar
- Após deleção, o saldo da fatura é automaticamente recalculado

**Restrição:**
- Pagamentos parciais de faturas **fechadas** não podem ser deletados
- Se tentar deletar, retorna erro HTTP 409 Conflict

---

#### Regra 14: Pagamentos Parciais - Imutabilidade
Pagamentos parciais **não podem ser editados**, apenas:
- **Criados** (via POST `/invoices/{id}/partial-payments`)
- **Deletados** (via DELETE `/partial-payments/{id}`, se fatura aberta)

**Motivo:** Para manter histórico fidedigno de pagamentos e auditoria

Se o usuário quiser "corrigir" um pagamento:
1. Deletar o pagamento incorreto
2. Criar um novo pagamento com o valor correto

---

#### Regra 15: Cálculo do Limite Disponível (Available Limit)
O limite disponível de um cartão de crédito é calculado dinamicamente e **atualizado em tempo real** considerando **todas as faturas** (abertas e futuras) e os **pagamentos realizados**.

**Fórmula:**
```
limiteDisponivel = creditLimit - SUM(installments de faturas abertas) + SUM(partial_payments de faturas abertas)
```

**Componentes:**
- `creditLimit`: Limite total fixo do cartão (ex: R$ 1.000,00)
- `SUM(installments)`: Soma de todas as parcelas de faturas abertas (não pagas/não fechadas)
- `SUM(partial_payments)`: Soma de todos os pagamentos parciais em faturas abertas

**Comportamento - Atualização em Tempo Real:**
- ⬇️ O limite disponível **diminui IMEDIATAMENTE** quando novas compras são criadas (parcelas adicionadas)
- ⬆️ O limite disponível **aumenta IMEDIATAMENTE** quando qualquer pagamento é realizado
- 🚀 **NÃO É NECESSÁRIO** esperar o fechamento da fatura para o limite ser ajustado

**⚠️ IMPORTANTE - Diferença por Tipo de Cartão:**

**Cartões COM Pagamento Parcial (`allowsPartialPayment = true`):**
- 💰 Limite disponível **PODE EXCEDER** o `creditLimit` fixo
- Exemplo: `creditLimit = R$ 100`, pagamentos parciais = R$ 120 → `availableLimit = R$ 140`
- Saldo negativo permite limite maior que o fixo

**Cartões SEM Pagamento Parcial (`allowsPartialPayment = false`):**
- 🔒 Limite disponível **NUNCA EXCEDE** o `creditLimit` fixo
- Máximo: `availableLimit = creditLimit`
- Fatura nunca fica negativa, logo sem crédito extra
- Pagamento só ocorre após fechamento com valor exato

**Tipos de Pagamento:**

1. **Pagamento Parcial (Fatura Aberta):**
   - Fatura aberta = R$ 200,00
   - Pagamento parcial = R$ 100,00
   - **Limite disponível aumenta em R$ 100,00 IMEDIATAMENTE** ✅
   - Não precisa esperar fechamento

2. **Pagamento Integral (Fatura Fechada):**
   - Fatura fechada = R$ 100,00
   - Pagamento integral = R$ 100,00
   - **Limite disponível aumenta em R$ 100,00 IMEDIATAMENTE** ✅
   - Fatura é marcada como paga

**Exemplo Completo - Ajuste em Tempo Real (Cartão COM Pagamento Parcial):**

**⚠️ Este exemplo aplica-se a:** `allowsPartialPayment = true`

**Estado Inicial:**
- `creditLimit` = R$ 100,00
- Fatura Atual: R$ 80,00 (soma das parcelas)
- Pagamentos Parciais: R$ 0,00
- **Limite Disponível** = 100 - 80 + 0 = **R$ 20,00**

**1º Pagamento Parcial: R$ 50,00 (Instantâneo)**
- `creditLimit` = R$ 100,00 (não muda)
- Fatura Atual: R$ 80,00 (valor das parcelas não muda)
- Pagamentos Parciais: R$ 50,00
- **Limite Disponível** = 100 - 80 + 50 = **R$ 70,00** ⬆️ (+50)
- ✅ Limite aumentou IMEDIATAMENTE ao registrar o pagamento

**2º Pagamento Parcial: R$ 70,00 (Instantâneo)**
- `creditLimit` = R$ 100,00
- Fatura Atual: R$ 80,00
- Pagamentos Parciais: R$ 50,00 + R$ 70,00 = R$ 120,00
- **Limite Disponível** = 100 - 80 + 120 = **R$ 140,00** ⬆️ (+70)
- ✅ Limite aumentou IMEDIATAMENTE novamente
- 💰 Limite disponível agora EXCEDE o creditLimit (140 > 100)

**Saldo da Fatura:**
- Total: R$ 80,00
- Pago: R$ 120,00
- Saldo: **-R$ 40,00** (crédito a favor)

**Observações (Cartão COM Pagamento Parcial):**
- O limite prefixado (`creditLimit`) **permanece R$ 100,00** (fixo)
- O limite disponível **é R$ 140,00** (maior que o limite prefixado) ✅
- Cada pagamento ajustou o limite **IMEDIATAMENTE**, sem esperar fechamento
- Quando a fatura fechar, o saldo negativo será transferido para a próxima fatura

---

**Exemplo Completo - Cartão SEM Pagamento Parcial:**

**⚠️ Este exemplo aplica-se a:** `allowsPartialPayment = false`

**Estado Inicial:**
- `creditLimit` = R$ 100,00
- Fatura Atual (Aberta): R$ 80,00
- **Limite Disponível** = 100 - 80 = **R$ 20,00**

**Tentativa de Pagamento Parcial (Fatura Aberta):**
- ❌ **REJEITADO** - Cartão não permite pagamento antes do fechamento
- HTTP 409 Conflict: "Cartão não permite pagamentos parciais"
- Limite permanece: **R$ 20,00** (não muda)

**Fatura Fecha:**
- Fatura é marcada como `closed = true`
- Valor a pagar: **R$ 80,00** (exato, sem desconto)
- Limite disponível: **R$ 20,00** (ainda não mudou)

**Pagamento Integral Após Fechamento:**
- Valor pago: **R$ 80,00** (deve ser exato)
- Fatura marcada como `paid = true`
- **Limite Disponível** = 100 - 0 = **R$ 100,00** ⬆️ (+80)
- ✅ Limite aumentou IMEDIATAMENTE ao registrar o pagamento

**Tentativa de Pagar Mais que o Valor da Fatura:**
- Valor da fatura: R$ 80,00
- Tentativa de pagar: R$ 100,00
- ❌ **REJEITADO** - Deve pagar exatamente R$ 80,00
- HTTP 400 Bad Request: "Valor deve ser exatamente R$ 80,00"

**Observações (Cartão SEM Pagamento Parcial):**
- O limite prefixado (`creditLimit`) **é R$ 100,00**
- O limite disponível **máximo é R$ 100,00** (NUNCA excede) 🔒
- Pagamento só permitido **após** fechamento da fatura
- Valor do pagamento **deve ser exatamente** o valor da fatura
- **Não há possibilidade** de saldo negativo ou crédito a favor

**Validação de Compras:**
```
novaCompra.totalAmount <= creditCard.limiteDisponivel
```

---

#### Regra 16: Transferência de Saldo Negativo Entre Faturas
Quando uma fatura é **fechada** com saldo negativo (crédito a favor do cliente), esse saldo deve ser **transferido para a próxima fatura**.

**Processo de Transferência:**

1. **Fatura Atual Fecha com Saldo Negativo:**
   - `valorFinal` = totalAmount - SUM(partial_payments) < 0
   - Sistema marca a fatura como `paid = true`
   - Saldo negativo é armazenado temporariamente

2. **Próxima Fatura é Criada/Atualizada:**
   - Sistema busca o saldo negativo da fatura anterior
   - Calcula o valor inicial da nova fatura considerando o crédito:
   ```
   proximaFatura.totalAmount = SUM(installments da nova fatura) + saldoAnterior
   ```
   - Como o saldo anterior é negativo, o total da próxima fatura é **reduzido**

**Exemplo Completo:**

**Mês 1: Janeiro**
- Fatura com parcelas totalizando: R$ 80,00
- Pagamento parcial realizado: R$ 120,00
- Ao fechar em 31/Jan:
  - `valorFinal` = 80 - 120 = **-R$ 40,00**
  - `paid` = true ✅
  - Saldo a transferir: **-R$ 40,00**

**Mês 2: Fevereiro**
- Novas parcelas somam: R$ 100,00
- Sistema aplica o crédito da fatura anterior:
  - `totalAmount` inicial = 100 + (-40) = **R$ 60,00** ✅
- Cliente pagará apenas R$ 60,00 em vez de R$ 100,00

**Cenário Alternativo: Saldo Negativo Maior que Próxima Fatura**

**Mês 1:**
- Fatura: R$ 50,00
- Pagamento parcial: R$ 200,00
- Saldo final: 50 - 200 = **-R$ 150,00**

**Mês 2:**
- Novas parcelas: R$ 100,00
- Aplicando crédito: 100 + (-150) = **-R$ 50,00** ✅
- Fatura de Fevereiro já nasce com saldo negativo (cliente ainda tem crédito)
- `paid` = true (já paga)
- Saldo de **-R$ 50,00** é transferido para Março

**Implementação Técnica:**
- Campo na entidade Invoice: `previousBalance` (BigDecimal, pode ser negativo)
- Ao fechar fatura com saldo < 0:
  ```java
  BigDecimal carryOverBalance = valorFinal; // negativo
  Invoice nextInvoice = getOrCreateNextInvoice(creditCardId, nextMonth);
  nextInvoice.setPreviousBalance(carryOverBalance);
  ```
- Ao calcular total da fatura:
  ```java
  BigDecimal finalTotal = SUM(installments) + invoice.previousBalance;
  invoice.setTotalAmount(finalTotal);
  ```

**Regras de Validação:**
- Transferência só ocorre quando fatura é **fechada** (`closed = true`)
- Saldo negativo **não pode ser editado manualmente**
- Se próxima fatura já tiver parcelas, o saldo é **somado** ao total existente
- Saldo pode ser transferido através de **múltiplas faturas** se necessário

---

## Configuração

### Configuração de Casos de Uso ✅
**Arquivo:** `src/main/java/com/truebalance/truebalance/config/UseCaseConfig.java`

**Propósito:** Registra manualmente casos de uso como beans Spring (já que eles estão na camada de domínio sem anotações Spring).

```java
@Configuration
public class UseCaseConfig {

    @Bean
    public CreateBill createBill(BillRepositoryPort billRepositoryPort) {
        return new CreateBill(billRepositoryPort);
    }

    @Bean
    public UpdateBill updateBill(BillRepositoryPort billRepositoryPort) {
        return new UpdateBill(billRepositoryPort);
    }

    @Bean
    public GetAllBills getAllBills(BillRepositoryPort billRepositoryPort) {
        return new GetAllBills(billRepositoryPort);
    }
}
```

**Por que Configuração Manual?**
- Camada de domínio deve permanecer agnóstica a frameworks
- Casos de uso não usam anotações `@Component` ou `@Service`
- Definição explícita de beans mantém arquitetura limpa

---

### Configuração OpenAPI ✅
**Arquivo:** `src/main/java/com/truebalance/truebalance/config/OpenApiConfig.java`

**Propósito:** Customiza documentação Swagger/OpenAPI.

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("TrueBalance API")
                .version("1.0.0")
                .description("Financial Management System API")
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0"))
                .contact(new Contact()
                    .name("TrueBalance Team")
                    .email("contato@truebalance.com")));
    }
}
```

**URL Swagger UI:** `http://localhost:8080/swagger-ui.html`
**Spec OpenAPI:** `http://localhost:8080/api-docs`

---

### Configuração de Banco de Dados ✅
**Arquivo:** `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/truebalance
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update  # Cria/atualiza schema automaticamente
    show-sql: true      # Loga comandos SQL
    properties:
      hibernate:
        format_sql: true
        jdbc:
          time_zone: UTC

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
```

**Configurações Principais:**
- **DDL Auto:** `update` - Hibernate cria/atualiza tabelas automaticamente
- **Log SQL:** Habilitado para debug
- **Timezone:** UTC para timestamps consistentes
- **Swagger:** Habilitado com caminhos customizados

---

### Configuração Docker ✅

#### docker-compose.yml
**Arquivo:** `docker-compose.yml`

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: truebalance-db
    environment:
      POSTGRES_DB: truebalance
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - truebalance-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: truebalance-app
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/truebalance
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
    networks:
      - truebalance-network

volumes:
  postgres-data:

networks:
  truebalance-network:
    driver: bridge
```

---

#### Dockerfile
**Arquivo:** `Dockerfile`

```dockerfile
# Build stage
FROM gradle:8.11.1-jdk21-alpine AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Benefícios Multi-stage:**
- Imagem final menor (apenas JRE, não JDK completo + Gradle)
- Artefatos de build não incluídos na imagem de runtime
- Deploys mais rápidos

---

## Roadmap de Desenvolvimento

### Fase 1: MVP (✅ Concluída)

**Objetivo:** Gerenciamento básico de contas com cálculo de parcelas

**Recursos Implementados:**
- ✅ Entidade Bill (domínio + JPA)
- ✅ Endpoints CRUD de Bill (Criar, Listar todos, Atualizar)
- ✅ Cálculo automático de parcelas
- ✅ Fundação de arquitetura hexagonal
- ✅ Persistência PostgreSQL
- ✅ Documentação OpenAPI/Swagger
- ✅ Containerização Docker
- ✅ Padrão Port/Adapter

**Endpoints:**
- `POST /bills` - Criar conta
- `GET /bills` - Listar todas as contas
- `PUT /bills/{id}` - Atualizar conta

---

### Fase 2: Gerenciamento de Cartão de Crédito & Fatura (🔨 Planejado)

**Objetivo:** Implementar ciclos de faturamento de cartão de crédito e gerenciamento de faturas

**Recursos Planejados:**
- 🔨 Entidade CreditCard (domínio + JPA + DTOs)
- 🔨 Entidade Invoice (domínio + JPA + DTOs)
- 🔨 Endpoints CRUD de cartão de crédito
- 🔨 Endpoints de listagem e recuperação de faturas
- 🔨 Funcionalidade de fechamento de fatura
- 🔨 Lógica de ciclo de faturamento (dia de fechamento, dia de vencimento)
- 🔨 Geração automática de faturas

**Novos Endpoints:**
- `POST /credit-cards` - Criar cartão de crédito
- `GET /credit-cards` - Listar cartões de crédito
- `GET /credit-cards/{id}` - Buscar cartão de crédito
- `PUT /credit-cards/{id}` - Atualizar cartão de crédito
- `DELETE /credit-cards/{id}` - Deletar cartão de crédito
- `GET /credit-cards/{id}/invoices` - Listar faturas do cartão
- `GET /invoices/{id}` - Buscar detalhes da fatura
- `POST /invoices/{id}/close` - Fechar fatura

**Dependências:**
- Fase 1 deve estar completa

---

### Fase 3: Gerenciamento Avançado de Parcelas (🔨 Planejado)

**Objetivo:** Implementar entidade de parcela e distribuição automática para faturas

**Recursos Planejados:**
- 🔨 Entidade Installment (separada de Bill)
- 🔨 Criação automática de parcelas quando conta é criada
- 🔨 Roteamento de parcelas para faturas futuras baseado no ciclo de faturamento
- 🔨 Cálculo de data de vencimento por parcela
- 🔨 Endpoints de listagem de parcelas
- 🔨 Cenários complexos de parcelamento
- 🔨 Validação de limite de crédito

**Novos Endpoints:**
- `GET /bills/{id}/installments` - Listar parcelas da conta
- `GET /invoices/{id}/installments` - Listar parcelas da fatura
- `GET /bills/{id}` - Buscar conta única (com parcelas)
- `DELETE /bills/{id}` - Deletar conta (cascata para parcelas)

**Lógica de Negócio:**
- Algoritmo de distribuição de parcelas
- Cálculo de período de fatura
- Lógica de dia de fechamento do cartão de crédito
- Roteamento de parcelas para múltiplos meses

**Dependências:**
- Fase 2 deve estar completa

---

### Fase 4: Recursos Avançados (🔨 Futuro)

**Objetivo:** Adicionar recursos avançados para gerenciamento financeiro abrangente

**Recursos Planejados:**
- 🔨 Rastreamento de pagamento (marcar parcelas/faturas como pagas)
- 🔨 Categorias de conta (mercado, utilidades, entretenimento, etc.)
- 🔨 Alertas e notificações de limite de crédito
- 🔨 Análises e relatórios de gastos
- 🔨 Recorrência de conta (assinaturas mensais)
- 🔨 Suporte multi-usuário com autenticação
- 🔨 Gerenciamento de orçamento
- 🔨 Exportação de dados (CSV, PDF)
- 🔨 Dashboard com gráficos e grafos

**Possíveis Endpoints:**
- `POST /invoices/{id}/pay` - Marcar fatura como paga
- `GET /bills/categories` - Listar categorias de conta
- `GET /reports/spending?start=...&end=...` - Relatório de gastos
- `GET /analytics/monthly-summary` - Análises mensais
- `POST /bills/recurring` - Criar conta recorrente

**Dependências:**
- Fase 3 deve estar completa

---

## Resumo dos Endpoints da API

Tabela completa de todos os endpoints (atual + planejado):

### Endpoints de Bill

| Status | Método | Path | Descrição |
|--------|--------|------|-------------|
| ✅ | POST | `/bills` | Criar nova conta com cálculo de parcelas |
| ✅ | GET | `/bills` | Listar todas as contas |
| ✅ | PUT | `/bills/{id}` | Atualizar conta e recalcular parcelas |
| ✅ | GET | `/bills/hello` | Endpoint de health check |
| 🔨 | GET | `/bills/{id}` | Buscar conta específica por ID |
| 🔨 | DELETE | `/bills/{id}` | Deletar conta e cascata para parcelas |
| 🔨 | GET | `/bills/{id}/installments` | Listar todas as parcelas de uma conta |

### Endpoints de Credit Card

| Status | Método | Path | Descrição |
|--------|--------|------|-------------|
| 🔨 | POST | `/credit-cards` | Criar novo cartão de crédito |
| 🔨 | GET | `/credit-cards` | Listar todos os cartões de crédito |
| 🔨 | GET | `/credit-cards/{id}` | Buscar cartão de crédito específico |
| 🔨 | PUT | `/credit-cards/{id}` | Atualizar cartão de crédito |
| 🔨 | DELETE | `/credit-cards/{id}` | Deletar cartão de crédito |
| 🔨 | GET | `/credit-cards/{id}/invoices` | Listar todas as faturas de um cartão |

### Endpoints de Invoice

| Status | Método | Path | Descrição |
|--------|--------|------|-------------|
| 🔨 | GET | `/invoices/{id}` | Buscar fatura com detalhes |
| 🔨 | POST | `/invoices/{id}/close` | Fechar fatura para período de faturamento |
| 🔨 | GET | `/invoices/{id}/installments` | Listar parcelas na fatura |

**Total de Endpoints:**
- ✅ Implementados: 4
- 🔨 Planejados: 13
- **Total Geral:** 17 endpoints

---

## Padrões de Código & Convenções

### Convenções de Nomenclatura

| Componente | Padrão | Exemplo |
|-----------|---------|---------|
| Controllers | `{Entity}Controller` | `BillController` |
| Use Cases | `{Verb}{Entity}` | `CreateBill`, `UpdateBill`, `GetAllBills` |
| Request DTOs | `{Entity}RequestDTO` | `BillRequestDTO` |
| Response DTOs | `{Entity}ResponseDTO` | `BillResponseDTO` |
| Domain Entities | `{Entity}` | `Bill`, `CreditCard`, `Invoice` |
| JPA Entities | `{Entity}Entity` | `BillEntity`, `CreditCardEntity` |
| Repositories | `{Entity}Repository` | `BillRepository` |
| Port Interfaces | `{Entity}RepositoryPort` | `BillRepositoryPort` |
| Adapters | `{Entity}RepositoryAdapter` | `BillRepositoryAdapter` |

---

### Estrutura de Pacotes

```
com.truebalance.truebalance
│
├── application/                 # Application Layer (Interfaces do Usuário)
│   ├── controller/              # Controllers REST
│   │   ├── BillController.java
│   │   ├── CreditCardController.java (planejado)
│   │   └── InvoiceController.java (planejado)
│   └── dto/                     # Data Transfer Objects
│       ├── input/               # Request DTOs
│       │   ├── BillRequestDTO.java
│       │   └── CreditCardRequestDTO.java (planejado)
│       └── output/              # Response DTOs
│           ├── BillResponseDTO.java
│           ├── CreditCardResponseDTO.java (planejado)
│           ├── InvoiceResponseDTO.java (planejado)
│           └── InstallmentResponseDTO.java (planejado)
│
├── domain/                      # Domain Layer (Lógica de Negócio)
│   ├── entity/                  # Modelos de domínio (Java puro)
│   │   ├── Bill.java
│   │   ├── CreditCard.java (planejado)
│   │   ├── Invoice.java (planejado)
│   │   └── Installment.java (planejado)
│   ├── usecase/                 # Operações de negócio
│   │   ├── CreateBill.java
│   │   ├── UpdateBill.java
│   │   ├── GetAllBills.java
│   │   ├── CreateCreditCard.java (planejado)
│   │   ├── CloseInvoice.java (planejado)
│   │   └── GenerateInstallments.java (planejado)
│   └── port/                    # Interfaces de porta (abstrações)
│       ├── BillRepositoryPort.java
│       ├── CreditCardRepositoryPort.java (planejado)
│       ├── InvoiceRepositoryPort.java (planejado)
│       └── InstallmentRepositoryPort.java (planejado)
│
├── infra/                       # Infrastructure Layer (Técnico)
│   └── db/                      # Implementação de banco de dados
│       ├── entity/              # Entidades JPA
│       │   ├── BillEntity.java
│       │   ├── CreditCardEntity.java (planejado)
│       │   ├── InvoiceEntity.java (planejado)
│       │   └── InstallmentEntity.java (planejado)
│       ├── repository/          # Repositórios Spring Data
│       │   ├── BillRepository.java
│       │   ├── CreditCardRepository.java (planejado)
│       │   ├── InvoiceRepository.java (planejado)
│       │   └── InstallmentRepository.java (planejado)
│       └── adapter/             # Implementações de porta
│           ├── BillRepositoryAdapter.java
│           ├── CreditCardRepositoryAdapter.java (planejado)
│           ├── InvoiceRepositoryAdapter.java (planejado)
│           └── InstallmentRepositoryAdapter.java (planejado)
│
├── config/                      # Configuration Layer
│   ├── UseCaseConfig.java       # Definições de bean dos casos de uso
│   └── OpenApiConfig.java       # Config da documentação da API
│
└── TruebalanceApplication.java  # Classe principal Spring Boot
```

---

### Padrões Arquiteturais

#### 1. Hexagonal Architecture (Ports & Adapters)

**Princípio:** Camada de domínio é isolada de dependências externas.

**Implementação:**
- **Ports:** Interfaces na camada de domínio (`BillRepositoryPort`)
- **Adapters:** Implementações na camada de infraestrutura (`BillRepositoryAdapter`)
- **Direção:** Infraestrutura depende do domínio, não vice-versa

**Benefícios:**
- Lógica de domínio é testável sem banco de dados
- Fácil trocar implementações de infraestrutura
- Fronteiras e responsabilidades claras

---

#### 2. Dependency Inversion Principle

**Princípio:** Módulos de alto nível não devem depender de módulos de baixo nível. Ambos devem depender de abstrações.

**Exemplo:**
```
CreateBill (domain) → BillRepositoryPort (interface) ← BillRepositoryAdapter (infra)
```

- `CreateBill` depende de **interface** (`BillRepositoryPort`)
- `BillRepositoryAdapter` implementa essa interface
- Dependência aponta para dentro (infraestrutura → domínio)

---

#### 3. Separation of Concerns

**Domain Entity ≠ JPA Entity:**
- **Domínio:** `Bill.java` - Java puro, sem anotações de framework
- **JPA:** `BillEntity.java` - Anotado com JPA, específico de banco de dados

**Por quê?**
- Lógica de domínio não se importa com detalhes de persistência
- Pode mudar estratégia de banco de dados sem tocar na lógica de negócio
- Testável sem banco de dados

---

#### 4. DTO Pattern

**Princípio:** Separar modelos de domínio internos de contratos de API externos.

**Fluxo:**
1. Cliente envia `BillRequestDTO`
2. Controller converte para entidade de domínio `Bill`
3. Caso de uso processa `Bill`
4. Controller converte `Bill` para `BillResponseDTO`
5. Cliente recebe `BillResponseDTO`

**Por quê?**
- Estabilidade da API (mudanças internas não quebram a API)
- Validação na fronteira
- Segurança (não expor IDs internos, relacionamentos)

---

#### 5. Use Case-Driven Design

**Princípio:** Cada operação de negócio é uma classe de caso de uso separada.

**Exemplos:**
- `CreateBill` - Uma responsabilidade: criar uma conta
- `UpdateBill` - Uma responsabilidade: atualizar uma conta
- `GetAllBills` - Uma responsabilidade: recuperar todas as contas

**Benefícios:**
- Princípio da Responsabilidade Única
- Fácil de testar
- Operações de negócio claras
- Composição flexível

---

### Diretrizes de Estilo de Código

#### Linguagem
- **Código:** Nomes de variáveis/métodos/classes em inglês
- **Documentação:** Português em anotações OpenAPI, inglês em comentários de código
- **Mensagens de Commit:** Português (convenção atual)

#### Formatação
- **Indentação:** 4 espaços (padrão Java)
- **Chaves:** Estilo egípcio (chave de abertura na mesma linha)
- **Comprimento da Linha:** 120 caracteres máximo

#### Melhores Práticas
- **Imutabilidade:** Preferir `final` para variáveis quando possível
- **Null Safety:** Usar `Optional<T>` para retornos anuláveis
- **Valores Monetários:** Sempre usar `BigDecimal` para dinheiro (nunca `double` ou `float`)
- **Arredondamento:** Usar `RoundingMode.HALF_UP` para moeda
- **Timestamps:** Usar `LocalDateTime` para datas, armazenar em UTC
- **Validação:** Validar na fronteira da API (nível controller/DTO)

---

## Apêndice

### Referência de Localização de Arquivos

#### Arquivos Atuais (Fase 1)

**Domain Layer:**
- `src/main/java/com/truebalance/truebalance/domain/entity/Bill.java`
- `src/main/java/com/truebalance/truebalance/domain/usecase/CreateBill.java`
- `src/main/java/com/truebalance/truebalance/domain/usecase/UpdateBill.java`
- `src/main/java/com/truebalance/truebalance/domain/usecase/GetAllBills.java`
- `src/main/java/com/truebalance/truebalance/domain/port/BillRepositoryPort.java`

**Application Layer:**
- `src/main/java/com/truebalance/truebalance/application/controller/BillController.java`
- `src/main/java/com/truebalance/truebalance/application/dto/input/BillRequestDTO.java`
- `src/main/java/com/truebalance/truebalance/application/dto/output/BillRespondeDTO.java`

**Infrastructure Layer:**
- `src/main/java/com/truebalance/truebalance/infra/db/entity/BillEntity.java`
- `src/main/java/com/truebalance/truebalance/infra/db/repository/BillRepository.java`
- `src/main/java/com/truebalance/truebalance/infra/db/adapter/BillRepositoryAdapter.java`

**Configuração:**
- `src/main/java/com/truebalance/truebalance/config/UseCaseConfig.java`
- `src/main/java/com/truebalance/truebalance/config/OpenApiConfig.java`
- `src/main/resources/application.yml`

**Build & Deployment:**
- `build.gradle`
- `Dockerfile`
- `docker-compose.yml`
- `README.md`

---

### Comandos de Início Rápido

```bash
# Build do projeto
./gradlew build

# Executar testes
./gradlew test

# Iniciar com Docker Compose
docker-compose up -d

# Parar containers
docker-compose down

# Ver logs
docker-compose logs -f app

# Acessar Swagger UI
open http://localhost:8080/swagger-ui.html

# Acessar banco de dados
psql -h localhost -U postgres -d truebalance
```

---

### Exemplos de Testes de API

#### Criar Bill
```bash
curl -X POST http://localhost:8080/bills \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Purchase",
    "executionDate": "2025-01-20T10:00:00",
    "totalAmount": 600.00,
    "numberOfInstallments": 6,
    "description": "Test description"
  }'
```

#### Listar Bills
```bash
curl http://localhost:8080/bills
```

#### Atualizar Bill
```bash
curl -X PUT http://localhost:8080/bills/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Purchase",
    "executionDate": "2025-01-20T10:00:00",
    "totalAmount": 800.00,
    "numberOfInstallments": 8,
    "description": "Updated description"
  }'
```

---

## Histórico de Versões do Documento

| Versão | Data | Mudanças |
|---------|------|---------|
| 1.0 | 2025-12-27 | Documentação abrangente inicial com implementação atual e roadmap planejado |

---

**Fim da Documentação de Implementação**
