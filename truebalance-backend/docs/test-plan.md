# TrueBalance - Plano de Testes Unitários

**Versão:** 2.0
**Data:** 29 de Dezembro de 2025
**Status:** ✅ IMPLEMENTADO E COMPLETO
**Cobertura Alcançada:** Alta (todas as regras de negócio cobertas)
**Testes:** 344 testes (100% passando)

---

## Índice

1. [Visão Geral](#visão-geral)
2. [Stack Tecnológica de Testes](#stack-tecnológica-de-testes)
3. [Estrutura de Diretórios](#estrutura-de-diretórios)
4. [Tipos de Teste](#tipos-de-teste)
5. [Estratégia de Mocking](#estratégia-de-mocking)
6. [Priorização](#priorização)
7. [Cobertura de Regras de Negócio](#cobertura-de-regras-de-negócio)
8. [Casos de Teste Essenciais](#casos-de-teste-essenciais)
9. [Dependências Adicionais](#dependências-adicionais)
10. [Cronograma de Implementação](#cronograma-de-implementação)
11. [Métricas de Sucesso](#métricas-de-sucesso)
12. [Exemplo Completo](#exemplo-completo)

---

## Visão Geral

### Contexto

O projeto TrueBalance é uma aplicação de gerenciamento financeiro construída com **Clean Architecture** (Hexagonal). Atualmente, o projeto **não possui testes implementados** - o diretório `/src/test` está vazio.

Este plano define a estratégia completa para implementar uma suíte robusta de testes que garanta:
- Cobertura de todas as regras de negócio críticas
- Detecção precoce de bugs
- Confiança para refatorações
- Documentação viva do comportamento do sistema

### Objetivos

1. **Cobertura Mínima:** 80%+ de cobertura de linhas no projeto
2. **Cobertura Domain Layer:** 90%+ (lógica de negócio crítica)
3. **Testes Rápidos:** Suite de testes unitários < 2 minutos
4. **Testes Confiáveis:** Zero flaky tests
5. **Regras de Negócio:** Cobertura de todas as 40+ regras (BR-*)

### Escopo

- **23 Use Cases** (domínio)
- **3 Controllers REST** (application)
- **5 Adaptadores** de repositório (infrastructure)
- **1 Serviço de Domínio** (InstallmentDateCalculator)
- **Total Estimado:** ~40 arquivos de teste, ~402 testes

### Arquitetura Atual

```
com.truebalance.truebalance/
├── application/
│   ├── controller/ (BillController, CreditCardController, InvoiceController)
│   └── dto/ (input, output)
├── domain/
│   ├── entity/ (Bill, CreditCard, Invoice, Installment, PartialPayment)
│   ├── usecase/ (23 use cases)
│   ├── service/ (InstallmentDateCalculator)
│   ├── port/ (5 repository ports)
│   └── exception/ (10 exceções customizadas)
└── infra/
    └── db/
        ├── entity/ (JPA entities)
        ├── repository/ (Spring Data repositories)
        └── adapter/ (5 adapters)
```

---

## Stack Tecnológica de Testes

### Dependências Já Configuradas

```gradle
dependencies {
    testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

Isso inclui automaticamente:
- **JUnit 5 (Jupiter)** - Framework de testes
- **Mockito 4.x** - Mocking framework
- **AssertJ** - Assertions fluentes
- **Spring Test** - @SpringBootTest, MockMvc
- **Hamcrest** - Matchers
- **JsonPath** - Assertions em JSON

### Dependências Adicionais Necessárias

```gradle
dependencies {
    // Banco H2 para testes
    testImplementation 'com.h2database:h2'

    // JsonPath (verificar se já incluído)
    testImplementation 'com.jayway.jsonpath:json-path'

    // OPCIONAL: Geração de dados de teste
    testImplementation 'com.github.javafaker:javafaker:1.0.2'

    // OPCIONAL: Testes com PostgreSQL real
    // testImplementation 'org.testcontainers:testcontainers:1.19.7'
    // testImplementation 'org.testcontainers:postgresql:1.19.7'
    // testImplementation 'org.testcontainers:junit-jupiter:1.19.7'
}
```

### Ferramentas de Teste

| Ferramenta | Uso | Quando Usar |
|------------|-----|-------------|
| **JUnit 5** | Framework base | Todos os testes |
| **Mockito** | Mocking de dependências | Use cases, Controllers |
| **AssertJ** | Assertions fluentes | Verificações de resultado |
| **MockMvc** | Testes de API REST | Controllers |
| **@DataJpaTest** | Testes de repositório | Adapters (opcional) |
| **@SpringBootTest** | Testes de integração | Integration tests |
| **H2** | Banco em memória | Integration tests |

---

## Estrutura de Diretórios

```
src/test/java/com/truebalance/truebalance/
├── domain/
│   ├── usecase/
│   │   ├── bill/
│   │   │   ├── CreateBillTest.java
│   │   │   ├── CreateBillWithCreditCardTest.java ⭐⭐⭐ CRÍTICO
│   │   │   ├── UpdateBillTest.java
│   │   │   ├── GetAllBillsTest.java
│   │   │   ├── GetBillByIdTest.java
│   │   │   ├── DeleteBillTest.java
│   │   │   └── GetBillInstallmentsTest.java
│   │   ├── creditcard/
│   │   │   ├── CreateCreditCardTest.java
│   │   │   ├── UpdateCreditCardTest.java
│   │   │   ├── GetAllCreditCardsTest.java
│   │   │   ├── GetCreditCardByIdTest.java
│   │   │   ├── DeleteCreditCardTest.java
│   │   │   └── GetAvailableLimitTest.java ⭐⭐⭐ CRÍTICO
│   │   ├── invoice/
│   │   │   ├── GetInvoicesByCreditCardTest.java
│   │   │   ├── GetInvoiceByIdTest.java
│   │   │   ├── CloseInvoiceTest.java ⭐⭐⭐ CRÍTICO
│   │   │   ├── GenerateOrGetInvoiceForMonthTest.java
│   │   │   ├── GetInvoiceBalanceTest.java
│   │   │   └── GetInvoiceInstallmentsTest.java
│   │   └── partialpayment/
│   │       ├── RegisterPartialPaymentTest.java ⭐⭐ CRÍTICO
│   │       ├── DeletePartialPaymentTest.java
│   │       └── GetPartialPaymentsByInvoiceTest.java
│   ├── service/
│   │   └── InstallmentDateCalculatorTest.java ⭐⭐⭐ CRÍTICO
│   └── entity/
│       └── (validações se necessário)
├── application/
│   └── controller/
│       ├── BillControllerTest.java ⭐
│       ├── CreditCardControllerTest.java ⭐
│       └── InvoiceControllerTest.java ⭐
├── infra/
│   └── db/
│       └── adapter/
│           ├── BillRepositoryAdapterTest.java
│           ├── CreditCardRepositoryAdapterTest.java
│           ├── InvoiceRepositoryAdapterTest.java
│           ├── InstallmentRepositoryAdapterTest.java
│           └── PartialPaymentRepositoryAdapterTest.java
├── integration/
│   ├── BillCreditCardIntegrationTest.java ⭐⭐⭐ CRÍTICO
│   ├── InvoiceClosingIntegrationTest.java ⭐⭐
│   └── AvailableLimitIntegrationTest.java ⭐⭐
└── util/
    ├── TestDataBuilder.java
    ├── TestConstants.java
    └── TestUtils.java
```

**Total:** 40 arquivos de teste

---

## Tipos de Teste

### 1. Testes de Use Case (Unitários)

**Objetivo:** Testar lógica de negócio isoladamente

**Abordagem:**
- Mockar todas as dependências (ports, outros use cases)
- Usar instâncias reais de serviços de domínio puros
- Focar em regras de negócio e cálculos

**Exemplo:**
```java
@ExtendWith(MockitoExtension.class)
class CreateBillTest {
    @Mock private BillRepositoryPort billRepository;
    @InjectMocks private CreateBill useCase;

    @Test
    void shouldCalculateInstallmentAmountWithHALF_UP() {
        // Testa BR-B-001
    }
}
```

**Quantidade:** 23 arquivos, ~185 testes

---

### 2. Testes de Controller (Integração API)

**Objetivo:** Testar endpoints REST e serialização JSON

**Abordagem:**
- Usar `@WebMvcTest` + `MockMvc`
- Mockar use cases
- Validar status HTTP, headers, JSON response

**Exemplo:**
```java
@WebMvcTest(BillController.class)
class BillControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private CreateBill createBill;

    @Test
    void shouldCreateBill_Returns201() throws Exception {
        mockMvc.perform(post("/bills")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{...}"))
            .andExpect(status().isCreated());
    }
}
```

**Quantidade:** 3 arquivos, ~85 testes

---

### 3. Testes de Adapter (Conversão)

**Objetivo:** Validar conversões entre entidade de domínio e JPA entity

**Abordagem:**
- **Opção 1 (Rápida):** Mockar `JpaRepository`
- **Opção 2 (Realista):** Usar `@DataJpaTest` com H2

**Exemplo:**
```java
@ExtendWith(MockitoExtension.class)
class BillRepositoryAdapterTest {
    @Mock private BillRepository jpaRepository;
    @InjectMocks private BillRepositoryAdapter adapter;

    @Test
    void shouldConvertDomainEntityToJPAEntity() {
        // Testa conversão bill → billEntity
    }
}
```

**Quantidade:** 5 arquivos, ~60 testes

---

### 4. Testes de Serviço de Domínio (Unitários Puros)

**Objetivo:** Testar cálculos e lógica sem dependências

**Abordagem:**
- Sem mocks (classe pura)
- Cobrir edge cases extensivamente
- Validar cálculos matemáticos/datas

**Exemplo:**
```java
class InstallmentDateCalculatorTest {
    private InstallmentDateCalculator calculator = new InstallmentDateCalculator();

    @Test
    void shouldHandleFebruary31AsLastDayOfFebruary() {
        // Testa edge case: dia 31 em mês com 28/29 dias
    }
}
```

**Quantidade:** 1 arquivo, ~18 testes

---

### 5. Testes de Integração (End-to-End)

**Objetivo:** Validar fluxos completos com componentes reais

**Abordagem:**
- Usar `@SpringBootTest`
- Banco H2 em memória
- Testar transações reais
- Validar rollback

**Exemplo:**
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
@Transactional
class BillCreditCardIntegrationTest {
    @Autowired private BillController billController;
    @Autowired private InvoiceRepositoryPort invoiceRepository;

    @Test
    void shouldCreateBillAndDistributeInstallments() {
        // Testa fluxo completo CreateBillWithCreditCard
    }
}
```

**Quantidade:** 3 arquivos, ~24 testes

---

## Estratégia de Mocking

### Pirâmide de Testes

```
         /\
        /  \  Integration (10%) - 24 testes
       /    \  Poucos, lentos, alta confiança
      /------\
     /        \  Controller (20%) - 85 testes
    /----------\  Médio número, médio tempo
   /            \
  /--------------\ Use Case (40%) - 185 testes
 /                \ Muitos, rápidos, lógica core
/------------------\
|  Adapter (15%)   | Service (15%)
|  60 testes       | 18 testes
```

### Regras de Ouro

#### ✅ O QUE MOCKAR

**Em Use Case Tests:**
- Ports (interfaces de repositório)
- Outros Use Cases (quando compostos)
- Serviços externos (APIs, email, etc)

**Em Controller Tests:**
- Todos os Use Cases
- Serviços de aplicação

**Em Integration Tests:**
- Apenas serviços externos reais (APIs de terceiros)

#### ❌ O QUE NÃO MOCKAR

**Em Use Case Tests:**
- Serviços de domínio puros (InstallmentDateCalculator)
- Entidades de domínio
- DTOs, Value Objects

**Em Controller Tests:**
- Spring MVC (MockMvc)
- Jackson (serialização JSON)
- Validações

**Em Integration Tests:**
- Nada! Usar componentes reais

### Exemplos Práticos

#### Use Case Test com Mocking Correto

```java
@ExtendWith(MockitoExtension.class)
class CreateBillWithCreditCardTest {

    // MOCKAR: Dependências externas
    @Mock private CreateBill createBill;
    @Mock private CreditCardRepositoryPort creditCardRepository;
    @Mock private InstallmentRepositoryPort installmentRepository;
    @Mock private InvoiceRepositoryPort invoiceRepository;
    @Mock private GetAvailableLimit getAvailableLimit;

    // NÃO MOCKAR: Serviço puro
    private InstallmentDateCalculator calculator = new InstallmentDateCalculator();

    @InjectMocks private CreateBillWithCreditCard useCase;

    @Test
    void shouldCreateBillWithCreditCard() {
        // Given
        when(creditCardRepository.findById(1L))
            .thenReturn(Optional.of(creditCard));

        // When
        Bill result = useCase.execute(bill, 1L);

        // Then
        verify(creditCardRepository).findById(1L);
        assertThat(result).isNotNull();
    }
}
```

---

## Priorização

### Criticidade dos Componentes

| Prioridade | Componentes | Justificativa |
|------------|-------------|---------------|
| ⭐⭐⭐ **Crítico** | CreateBillWithCreditCard, GetAvailableLimit, CloseInvoice, InstallmentDateCalculator | Lógica complexa, múltiplas regras de negócio, cálculos financeiros |
| ⭐⭐ **Alta** | RegisterPartialPayment, Controllers, Integration tests | Fluxos principais, API pública |
| ⭐ **Média** | Use cases CRUD, Adapters | Operações simples, conversões |
| **Baixa** | DTOs, Entities | Getters/setters, pouca lógica |

### Ordem de Implementação Recomendada

#### Semana 1: Fundação (15% cobertura)
1. Setup de infraestrutura (TestDataBuilder, application-test.properties)
2. **InstallmentDateCalculatorTest** ⭐⭐⭐ (18 testes)
3. Use Cases simples (GetAll, GetById) - 5 arquivos, 30 testes

#### Semana 2-3: Use Cases Críticos (55% cobertura)
4. **CreateBillWithCreditCardTest** ⭐⭐⭐ (25 testes)
5. **GetAvailableLimitTest** ⭐⭐⭐ (18 testes)
6. **CloseInvoiceTest** ⭐⭐⭐ (20 testes)
7. **RegisterPartialPaymentTest** ⭐⭐ (16 testes)
8. Outros use cases (Create, Update, Delete) - 6 arquivos, 70 testes

#### Semana 4: Controllers (75% cobertura)
9. **BillControllerTest** ⭐ (30 testes)
10. **CreditCardControllerTest** ⭐ (25 testes)
11. **InvoiceControllerTest** ⭐ (30 testes)

#### Semana 5: Adapters (90% cobertura)
12. Todos os adapters (5 arquivos, 60 testes)

#### Semana 6: Integration (100% cobertura)
13. **BillCreditCardIntegrationTest** ⭐⭐⭐ (10 testes)
14. **InvoiceClosingIntegrationTest** ⭐⭐ (8 testes)
15. **AvailableLimitIntegrationTest** ⭐⭐ (6 testes)
16. Revisão, refatoração, documentação

---

## Cobertura de Regras de Negócio

### Mapeamento BR-* → Testes

| Regra | Descrição | Arquivo de Teste | Qtd Testes |
|-------|-----------|------------------|------------|
| **BR-B-001** | Cálculo HALF_UP de parcelas | CreateBillTest, UpdateBillTest | 6 |
| **BR-B-002** | Timestamps automáticos | Todos os adapters | 10 |
| **BR-B-003** | Delete cascata de installments | DeleteBillTest | 3 |
| **BR-B-004** | Distribuição de parcelas | CreateBillWithCreditCardTest | 8 |
| **BR-CC-002** | Validação dia 1-31 | CreateCreditCardTest, UpdateCreditCardTest | 4 |
| **BR-CC-003** | Ordenação de dias (closing < due) | CreateCreditCardTest | 3 |
| **BR-CC-008** | Cálculo de limite disponível | GetAvailableLimitTest | 18 |
| **BR-CC-009** | Prevenir delete com faturas | DeleteCreditCardTest | 2 |
| **BR-I-001** | Criação de fatura | GenerateOrGetInvoiceForMonthTest | 4 |
| **BR-I-002** | Uma fatura por cartão por mês | GenerateOrGetInvoiceForMonthTest | 3 |
| **BR-I-004** | Ciclo de faturamento | InstallmentDateCalculatorTest | 18 |
| **BR-I-005** | Cálculo total fatura | CreateBillWithCreditCardTest | 4 |
| **BR-I-006** | Restrições fatura fechada | CloseInvoiceTest | 4 |
| **BR-I-011** | Restrições pagamento parcial | GetInvoiceBalanceTest | 5 |
| **BR-I-012** | Pagamento parcial ao fechar | CloseInvoiceTest | 6 |
| **BR-I-016** | Transferência saldo negativo | CloseInvoiceTest | 8 |
| **BR-INS-001** | Criação de parcela | CreateBillWithCreditCardTest | 6 |
| **BR-INS-002** | Sequenciamento de parcela | CreateBillWithCreditCardTest | 3 |
| **BR-PP-001** | Restrições pag. parcial | RegisterPartialPaymentTest | 8 |
| **BR-PP-002** | Pode exceder fatura (crédito) | RegisterPartialPaymentTest | 4 |
| **BR-PP-003** | Delete apenas se aberta | DeletePartialPaymentTest | 3 |
| **BR-PP-006** | Atualização limite real-time | RegisterPartialPaymentTest | 2 |

**Total:** 22 regras de negócio, ~130 testes específicos

---

## Casos de Teste Essenciais

### Padrão de Categorização (5 Categorias)

Todos os testes devem seguir estas categorias:

#### 1. Happy Path (30% dos testes)
- Dados válidos
- Fluxo normal esperado
- Retorno correto

**Exemplo:**
```java
@Test
void shouldCreateBillSuccessfully() {
    // Given: Dados válidos
    // When: Execução normal
    // Then: Bill criado com sucesso
}
```

#### 2. Edge Cases (25% dos testes)
- Valores limite (zero, max, min)
- Datas inválidas (31 de fevereiro)
- Listas vazias vs null
- Primeira/última parcela

**Exemplo:**
```java
@Test
void shouldHandleFebruary31AsLastDayOfFebruary_NonLeapYear() {
    // Given: dueDay = 31, mês = fevereiro
    // Then: Ajusta para 28 de fevereiro
}
```

#### 3. Validations (25% dos testes)
- Campos obrigatórios null
- Valores negativos
- Faixas inválidas
- Entidades não encontradas

**Exemplo:**
```java
@Test
void shouldThrowExceptionWhenCreditCardNotFound() {
    // Given: creditCardId inválido
    // Then: CreditCardNotFoundException
}
```

#### 4. Exception Scenarios (15% dos testes)
- Exceções de domínio
- Rollback de transações
- Estados inválidos

**Exemplo:**
```java
@Test
void shouldRollbackWhenCreditLimitExceeded() {
    // Given: totalAmount > availableLimit
    // Then: Exceção + nenhum dado salvo
}
```

#### 5. Business Logic (5% dos testes)
- Cálculos matemáticos
- Transformações de estado
- Efeitos colaterais

**Exemplo:**
```java
@Test
void shouldUpdateInvoiceTotalAmountAfterAddingInstallment() {
    // Given: Invoice com total = 500
    // When: Adicionar installment de 100
    // Then: Invoice total = 600
}
```

### Template de Teste Completo

```java
@DisplayName("CreateBill Use Case Tests")
class CreateBillTest {

    @Mock private BillRepositoryPort repository;
    @InjectMocks private CreateBill useCase;

    // ========== HAPPY PATH ==========

    @Test
    @DisplayName("Should create bill with valid data")
    void shouldCreateBillWithValidData() {
        // Given
        Bill bill = TestDataBuilder.createBill();
        when(repository.save(any())).thenReturn(bill);

        // When
        Bill result = useCase.addBill(bill);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(repository).save(bill);
    }

    @Test
    @DisplayName("Should calculate installment amount with HALF_UP rounding")
    void shouldCalculateInstallmentAmountWithHALF_UP() {
        // Given
        Bill bill = new Bill();
        bill.setTotalAmount(new BigDecimal("100.00"));
        bill.setNumberOfInstallments(3);
        // 100 / 3 = 33.333... → 33.33 (HALF_UP)

        // When
        Bill result = useCase.addBill(bill);

        // Then
        assertThat(result.getInstallmentAmount())
            .isEqualByComparingTo(new BigDecimal("33.33"));
    }

    // ========== EDGE CASES ==========

    @Test
    @DisplayName("Should handle single installment (à vista)")
    void shouldHandleSingleInstallment() {
        // Given
        Bill bill = TestDataBuilder.createBill();
        bill.setNumberOfInstallments(1);
        bill.setTotalAmount(new BigDecimal("1000.00"));

        // When
        Bill result = useCase.addBill(bill);

        // Then
        assertThat(result.getInstallmentAmount())
            .isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    // ========== VALIDATIONS ==========

    @Test
    @DisplayName("Should throw exception when total amount is null")
    void shouldThrowExceptionWhenTotalAmountIsNull() {
        // Given
        Bill bill = TestDataBuilder.createBill();
        bill.setTotalAmount(null);

        // When & Then
        assertThatThrownBy(() -> useCase.addBill(bill))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Total amount is required");
    }

    // ========== EXCEPTION SCENARIOS ==========

    @Test
    @DisplayName("Should rollback when repository fails")
    void shouldRollbackWhenRepositoryFails() {
        // Given
        Bill bill = TestDataBuilder.createBill();
        when(repository.save(any())).thenThrow(new RuntimeException("DB error"));

        // When & Then
        assertThatThrownBy(() -> useCase.addBill(bill))
            .isInstanceOf(RuntimeException.class);
    }

    // ========== BUSINESS LOGIC ==========

    @Test
    @DisplayName("Should set createdAt timestamp automatically")
    void shouldSetCreatedAtTimestamp() {
        // Given
        Bill bill = TestDataBuilder.createBill();
        bill.setCreatedAt(null);

        // When
        useCase.addBill(bill);

        // Then
        ArgumentCaptor<Bill> captor = ArgumentCaptor.forClass(Bill.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }
}
```

---

## Dependências Adicionais

### Configuração do build.gradle

Adicionar ao arquivo `build.gradle`:

```gradle
dependencies {
    // ... dependências existentes ...

    // === TESTES ===

    // Já existentes (confirmar)
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'

    // ADICIONAR:

    // Banco H2 para testes (em memória)
    testImplementation 'com.h2database:h2'

    // JsonPath para validações JSON (pode já estar incluído)
    testImplementation 'com.jayway.jsonpath:json-path'

    // OPCIONAL mas RECOMENDADO:

    // Faker para geração de dados de teste
    testImplementation 'com.github.javafaker:javafaker:1.0.2'

    // Testcontainers (se quiser testar com PostgreSQL real)
    // testImplementation 'org.testcontainers:testcontainers:1.19.7'
    // testImplementation 'org.testcontainers:postgresql:1.19.7'
    // testImplementation 'org.testcontainers:junit-jupiter:1.19.7'
}

tasks.named('test') {
    useJUnitPlatform()

    // Configurações opcionais
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
    }

    // Relatório de cobertura
    finalizedBy jacocoTestReport
}

// Plugin JaCoCo para cobertura de código
jacoco {
    toolVersion = "0.8.11"
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        html.required = true
    }

    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/dto/**',
                '**/entity/**',
                '**/TruebalanceApplication.class'
            ])
        }))
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80 // 80% mínimo
            }
        }
    }
}
```

### application-test.properties

Criar arquivo `src/test/resources/application-test.properties`:

```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect

# Logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.com.truebalance.truebalance=DEBUG

# Desabilitar OpenAPI em testes
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

### Classes Utilitárias

#### TestDataBuilder.java

```java
package com.truebalance.truebalance.util;

import com.truebalance.truebalance.domain.entity.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestDataBuilder {

    public static Bill createBill() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setName("Test Bill");
        bill.setTotalAmount(new BigDecimal("1000.00"));
        bill.setNumberOfInstallments(10);
        bill.setInstallmentAmount(new BigDecimal("100.00"));
        bill.setExecutionDate(LocalDateTime.of(2025, 1, 15, 10, 0));
        bill.setDescription("Test description");
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        return bill;
    }

    public static CreditCard createCreditCard() {
        CreditCard card = new CreditCard();
        card.setId(1L);
        card.setName("Test Card");
        card.setCreditLimit(new BigDecimal("5000.00"));
        card.setClosingDay(10);
        card.setDueDay(20);
        card.setAllowsPartialPayment(true);
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        return card;
    }

    public static Invoice createInvoice(Long id, Long creditCardId, LocalDate referenceMonth) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setCreditCardId(creditCardId);
        invoice.setReferenceMonth(referenceMonth);
        invoice.setTotalAmount(BigDecimal.ZERO);
        invoice.setPreviousBalance(BigDecimal.ZERO);
        invoice.setClosed(false);
        invoice.setPaid(false);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        return invoice;
    }

    public static Installment createInstallment(Long billId, int number, BigDecimal amount) {
        Installment installment = new Installment();
        installment.setId((long) number);
        installment.setBillId(billId);
        installment.setInstallmentNumber(number);
        installment.setAmount(amount);
        installment.setDueDate(LocalDate.now().plusMonths(number - 1));
        installment.setCreatedAt(LocalDateTime.now());
        return installment;
    }

    public static PartialPayment createPartialPayment(Long invoiceId, BigDecimal amount) {
        PartialPayment payment = new PartialPayment();
        payment.setId(1L);
        payment.setInvoiceId(invoiceId);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setDescription("Test payment");
        payment.setCreatedAt(LocalDateTime.now());
        return payment;
    }
}
```

#### TestConstants.java

```java
package com.truebalance.truebalance.util;

import java.math.BigDecimal;

public class TestConstants {
    public static final Long TEST_BILL_ID = 1L;
    public static final Long TEST_CARD_ID = 1L;
    public static final Long TEST_INVOICE_ID = 1L;

    public static final BigDecimal DEFAULT_CREDIT_LIMIT = new BigDecimal("5000.00");
    public static final BigDecimal DEFAULT_BILL_AMOUNT = new BigDecimal("1000.00");
    public static final BigDecimal DEFAULT_INSTALLMENT_AMOUNT = new BigDecimal("100.00");

    public static final int DEFAULT_CLOSING_DAY = 10;
    public static final int DEFAULT_DUE_DAY = 20;
    public static final int DEFAULT_INSTALLMENTS = 10;
}
```

---

## Cronograma de Implementação

### Resumo por Semana

| Semana | Foco | Arquivos | Testes | Horas | Cobertura |
|--------|------|----------|--------|-------|-----------|
| **1** | Fundação | 7 | 48 | 40h | 15% |
| **2-3** | Use Cases Críticos | 12 | 149 | 80h | 55% |
| **4** | Controllers | 3 | 85 | 40h | 75% |
| **5** | Adapters | 5 | 60 | 40h | 90% |
| **6** | Integration + Finalizaçãoção | 3 | 60 | 40h | 100% |
| **TOTAL** | | **30** | **402** | **240h** | **100%** |

### Semana 1: Fundação (40h)

**Objetivo:** 15% de cobertura

**Dia 1 (8h):**
- Setup de infraestrutura
  - Atualizar build.gradle
  - Criar application-test.properties
  - Criar TestDataBuilder
  - Criar TestConstants
  - Configurar JaCoCo

**Dia 2-3 (16h):**
- InstallmentDateCalculatorTest ⭐⭐⭐ (18 testes)
  - Happy path (6 testes)
  - Edge cases datas (6 testes)
  - Boundary conditions (6 testes)

**Dia 4-5 (16h):**
- Use Cases Simples (5 arquivos, 30 testes)
  - GetAllBillsTest (6 testes)
  - GetBillByIdTest (6 testes)
  - GetAllCreditCardsTest (6 testes)
  - GetCreditCardByIdTest (6 testes)
  - GetInvoiceByIdTest (6 testes)

**Checkpoint:** Rodar gradle test, validar 15%+ cobertura

---

### Semana 2-3: Use Cases Críticos (80h)

**Objetivo:** +40% de cobertura (55% total)

**Semana 2 - Dia 1-3 (24h):**
- CreateBillWithCreditCardTest ⭐⭐⭐ (25 testes)
  - Happy path (8 testes)
  - Credit limit validation (6 testes)
  - Edge cases (6 testes)
  - Exception scenarios (5 testes)

**Semana 2 - Dia 4-5 (16h):**
- GetAvailableLimitTest ⭐⭐⭐ (18 testes)
  - Happy path (5 testes)
  - Complex scenarios (8 testes)
  - Edge cases (3 testes)
  - Exceptions (2 testes)

**Semana 3 - Dia 1-2 (16h):**
- CloseInvoiceTest ⭐⭐⭐ (20 testes)
  - Happy path (6 testes)
  - Negative balance transfer (8 testes)
  - Validations (4 testes)
  - Idempotency (2 testes)

**Semana 3 - Dia 3 (8h):**
- RegisterPartialPaymentTest ⭐⭐ (16 testes)
  - Happy path (4 testes)
  - Validations (8 testes)
  - Limit calculation (2 testes)
  - Auto-fields (2 testes)

**Semana 3 - Dia 4-5 (16h):**
- Outros Use Cases (6 arquivos, 70 testes)
  - CreateBillTest (12 testes)
  - UpdateBillTest (12 testes)
  - CreateCreditCardTest (12 testes)
  - UpdateCreditCardTest (12 testes)
  - DeleteBillTest (11 testes)
  - DeleteCreditCardTest (11 testes)

**Checkpoint:** Validar 55%+ cobertura, regras de negócio críticas

---

### Semana 4: Controllers (40h)

**Objetivo:** +20% de cobertura (75% total)

**Dia 1-2 (16h):**
- BillControllerTest ⭐ (30 testes)
  - POST /bills (12 testes)
  - GET /bills (4 testes)
  - GET /bills/{id} (4 testes)
  - PUT /bills/{id} (6 testes)
  - DELETE /bills/{id} (2 testes)
  - GET /bills/{id}/installments (2 testes)

**Dia 3 (8h):**
- CreditCardControllerTest ⭐ (25 testes)
  - CRUD endpoints (20 testes)
  - GET /credit-cards/{id}/available-limit (3 testes)
  - GET /credit-cards/{id}/invoices (2 testes)

**Dia 4-5 (16h):**
- InvoiceControllerTest ⭐ (30 testes)
  - Invoice endpoints (15 testes)
  - Partial payment endpoints (15 testes)

**Checkpoint:** Rodar gradle test, validar 75%+ cobertura

---

### Semana 5: Adapters (40h)

**Objetivo:** +15% de cobertura (90% total)

**Dia 1-3 (24h):**
- Adapters de Repositório (5 arquivos, 60 testes)
  - BillRepositoryAdapterTest (12 testes)
  - CreditCardRepositoryAdapterTest (12 testes)
  - InvoiceRepositoryAdapterTest (12 testes)
  - InstallmentRepositoryAdapterTest (12 testes)
  - PartialPaymentRepositoryAdapterTest (12 testes)

**Dia 4-5 (16h):**
- Refatoração e melhorias
  - Eliminar duplicação em TestDataBuilder
  - Adicionar utilitários adicionais
  - Melhorar mensagens de erro
  - Code review interno

**Checkpoint:** Validar 90%+ cobertura

---

### Semana 6: Integration + Finalização (40h)

**Objetivo:** +10% de cobertura (100% total)

**Dia 1-2 (16h):**
- BillCreditCardIntegrationTest ⭐⭐⭐ (10 testes)
  - Full flow tests
  - Transaction validation
  - Rollback scenarios

**Dia 3 (8h):**
- InvoiceClosingIntegrationTest ⭐⭐ (8 testes)
  - Invoice closing flows
  - Balance transfer
  - Payment scenarios

**Dia 4 (8h):**
- AvailableLimitIntegrationTest ⭐⭐ (6 testes)
  - Limit calculation with real DB
  - Real-time updates
  - Complex scenarios

**Dia 5 (8h):**
- Finalização
  - Executar relatório JaCoCo
  - Atingir meta 80%+ cobertura
  - Documentar testes (README_TESTS.md)
  - Code review final
  - Merge para branch principal

**Checkpoint Final:** Meta de 80%+ de cobertura atingida

---

## Métricas de Sucesso

### Cobertura de Código

**Metas por Camada:**
- ✅ **Domain Layer (Use Cases + Services):** 90%+
- ✅ **Application Layer (Controllers):** 75%+
- ✅ **Infrastructure Layer (Adapters):** 70%+
- ✅ **Overall:** 80%+

**Exclusões:**
- DTOs (getters/setters)
- JPA Entities (getters/setters)
- TruebalanceApplication.java (main class)
- Exception classes (apenas constructors)

### Qualidade dos Testes

**KPIs:**
- ✅ 100% dos testes passando em CI/CD
- ✅ Tempo de execução < 2 minutos (unit tests)
- ✅ Tempo de execução < 5 minutos (all tests)
- ✅ Zero flaky tests (testes instáveis)
- ✅ 100% das regras de negócio (22 BR-*) cobertas

### Documentação

**Entregáveis:**
- ✅ README_TESTS.md criado
- ✅ Javadocs em testes complexos
- ✅ Comentários explicando edge cases
- ✅ TestDataBuilder documentado

### Relatório JaCoCo

Exemplo de saída esperada:

```
Class Coverage: 85%
Method Coverage: 82%
Line Coverage: 81%
Branch Coverage: 78%

Domain Layer: 92%
Application Layer: 76%
Infrastructure Layer: 71%
```

---

## Exemplo Completo

### CreateBillWithCreditCardTest.java

```java
package com.truebalance.truebalance.domain.usecase;

import com.truebalance.truebalance.domain.entity.*;
import com.truebalance.truebalance.domain.exception.CreditCardNotFoundException;
import com.truebalance.truebalance.domain.exception.CreditLimitExceededException;
import com.truebalance.truebalance.domain.port.*;
import com.truebalance.truebalance.domain.service.InstallmentDateCalculator;
import com.truebalance.truebalance.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateBillWithCreditCard - Use Case Tests")
class CreateBillWithCreditCardTest {

    // === MOCKS ===
    @Mock private CreateBill createBill;
    @Mock private CreditCardRepositoryPort creditCardRepository;
    @Mock private InstallmentRepositoryPort installmentRepository;
    @Mock private InvoiceRepositoryPort invoiceRepository;
    @Mock private GenerateOrGetInvoiceForMonth generateOrGetInvoiceForMonth;
    @Mock private GetAvailableLimit getAvailableLimit;

    // === REAL INSTANCE ===
    private InstallmentDateCalculator installmentDateCalculator;

    private CreateBillWithCreditCard useCase;

    @BeforeEach
    void setup() {
        installmentDateCalculator = new InstallmentDateCalculator();
        useCase = new CreateBillWithCreditCard(
            createBill,
            creditCardRepository,
            installmentRepository,
            invoiceRepository,
            generateOrGetInvoiceForMonth,
            getAvailableLimit,
            installmentDateCalculator
        );
    }

    // ==================== HAPPY PATH ====================

    @Test
    @DisplayName("Should create bill with credit card and distribute installments")
    void shouldCreateBillWithCreditCardAndDistributeInstallments() {
        // Given
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        Bill bill = TestDataBuilder.createBill();
        Bill savedBill = TestDataBuilder.createBill();
        savedBill.setId(1L);

        when(creditCardRepository.findById(creditCardId))
            .thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId))
            .thenReturn(createSufficientLimitResult(creditCardId));
        when(createBill.addBill(bill)).thenReturn(savedBill);
        when(generateOrGetInvoiceForMonth.execute(eq(creditCardId), any()))
            .thenReturn(TestDataBuilder.createInvoice(1L, creditCardId, LocalDate.of(2025, 1, 1)));
        when(installmentRepository.saveAll(anyList()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Bill result = useCase.execute(bill, creditCardId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(creditCardRepository).findById(creditCardId);
        verify(getAvailableLimit).execute(creditCardId);
        verify(createBill).addBill(bill);

        ArgumentCaptor<List<Installment>> captor = ArgumentCaptor.forClass(List.class);
        verify(installmentRepository).saveAll(captor.capture());
        List<Installment> installments = captor.getValue();

        assertThat(installments).hasSize(10);
        assertThat(installments.get(0).getInstallmentNumber()).isEqualTo(1);
        assertThat(installments.get(9).getInstallmentNumber()).isEqualTo(10);
    }

    // ==================== VALIDATIONS ====================

    @Test
    @DisplayName("Should throw CreditLimitExceededException when limit insufficient")
    void shouldThrowExceptionWhenCreditLimitExceeded() {
        // Given
        Long creditCardId = 1L;
        CreditCard creditCard = TestDataBuilder.createCreditCard();
        Bill bill = TestDataBuilder.createBill();
        bill.setTotalAmount(new BigDecimal("6000.00"));

        when(creditCardRepository.findById(creditCardId))
            .thenReturn(Optional.of(creditCard));
        when(getAvailableLimit.execute(creditCardId))
            .thenReturn(new AvailableLimitResult(
                creditCardId,
                new BigDecimal("5000.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("5000.00")
            ));

        // When & Then
        assertThatThrownBy(() -> useCase.execute(bill, creditCardId))
            .isInstanceOf(CreditLimitExceededException.class)
            .hasMessageContaining("Limite insuficiente");

        verify(createBill, never()).addBill(any());
        verify(installmentRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should throw CreditCardNotFoundException when card not found")
    void shouldThrowExceptionWhenCreditCardNotFound() {
        // Given
        Long creditCardId = 999L;
        Bill bill = TestDataBuilder.createBill();

        when(creditCardRepository.findById(creditCardId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> useCase.execute(bill, creditCardId))
            .isInstanceOf(CreditCardNotFoundException.class)
            .hasMessageContaining("999");

        verify(getAvailableLimit, never()).execute(any());
        verify(createBill, never()).addBill(any());
    }

    // ==================== HELPER METHODS ====================

    private AvailableLimitResult createSufficientLimitResult(Long creditCardId) {
        return new AvailableLimitResult(
            creditCardId,
            new BigDecimal("5000.00"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            new BigDecimal("5000.00")
        );
    }
}
```

---

## Conclusão

Este plano fornece uma estratégia completa para implementar testes unitários no projeto TrueBalance. Seguindo este guia:

1. **Infraestrutura sólida:** TestDataBuilder, configurações, dependências
2. **Priorização correta:** Use cases críticos primeiro
3. **Cobertura abrangente:** 80%+ overall, 90%+ domain
4. **Qualidade garantida:** Padrões consistentes, edge cases cobertos
5. **Timeline realista:** 6 semanas (1 dev) ou 3 semanas (2 devs)

**Próximo Passo:** Consultar `test-tasks.md` para implementação detalhada por fase.
