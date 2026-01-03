# Documentação da API – Gerenciamento Financeiro

## Visão Geral

Esta API REST é responsável pelo gerenciamento de despesas financeiras, incluindo:

- Contas (despesas)
- Cartões de crédito
- Faturas
- Parcelas de contas

O sistema suporta compras à vista e parceladas, garantindo o correto vínculo entre contas, parcelas e faturas futuras de cartões de crédito.

---

## Conceitos do Domínio

### Conta

Uma Conta representa uma despesa realizada pelo usuário. Ela pode ser:

- À vista (`numberOfInstallments = 1`)
- Parcelada (`numberOfInstallments > 1`)

A conta pode ou não estar associada a um cartão de crédito.

#### Modelo de Conta

```java
private Long id;
private String name;
private LocalDateTime executionDate;
private BigDecimal totalAmount;
private int numberOfInstallments;
private BigDecimal installmentAmount;
private String description;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
```

---

### Cartão de Crédito

O Cartão de Crédito agrupa faturas mensais e define as regras de fechamento e vencimento.

#### Regras

- Um cartão possui várias faturas
- Cada fatura pertence a um único cartão
- Compras parceladas geram parcelas em faturas futuras

#### Modelo de Cartão

```java
private Long id;
private String name;
private BigDecimal creditLimit;
private int closingDay;
private int dueDay;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
```

---

### Fatura

A Fatura representa o período de cobrança de um cartão de crédito.

#### Regras

- Cada fatura pertence a um cartão
- Uma fatura contém várias parcelas
- O valor total da fatura é a soma das parcelas vinculadas
- Faturas podem ser abertas ou fechadas

#### Modelo de Fatura

```java
private Long id;
private Long creditCardId;
private LocalDate referenceMonth;
private BigDecimal totalAmount;
private boolean closed;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
```

---

### Parcela de Conta

A Parcela representa uma parte de uma conta parcelada ou uma conta à vista registrada em uma fatura.

Mesmo compras à vista são tratadas como uma única parcela.

#### Modelo de Parcela

```java
private Long id;
private Long accountId;
private Long creditCardId;
private Long invoiceId;
private int installmentNumber;
private BigDecimal amount;
private LocalDate dueDate;
private LocalDateTime createdAt;
```

---

## Relacionamentos

- Uma Conta possui várias Parcelas
- Um Cartão possui várias Faturas
- Uma Fatura possui várias Parcelas

```
Conta 1 ---- * Parcela
Cartão 1 ---- * Fatura
Fatura 1 ---- * Parcela
```

---

## Regras de Negócio

### Compra à Vista

- `numberOfInstallments = 1`
- Gera uma parcela
- A parcela é vinculada à fatura correspondente ao período da compra

### Compra Parcelada

Exemplo:

- Valor total: 400.00
- Parcelamento: 4x
- Valor da parcela: 100.00

Resultado:

- São criadas 4 parcelas
- Cada parcela é vinculada a uma fatura futura
- Todas as parcelas possuem referência à conta original

---

## Endpoints da API

### Importação e Exportação Unificada

#### Exportar Todas as Entidades

`GET /unified/export`

Exporta todas as contas, cartões de crédito e faturas para um único arquivo Excel com múltiplas abas.

**Resposta:**
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Arquivo Excel com três abas:
  - **Contas**: Todas as contas cadastradas
  - **Cartões de Crédito**: Todos os cartões cadastrados
  - **Faturas**: Todas as faturas cadastradas

**Exemplo:**
```bash
curl -X GET http://localhost:8080/unified/export -o truebalance_export.xlsx
```

---

#### Importar Todas as Entidades

`POST /unified/import`

Importa contas, cartões de crédito e faturas de um único arquivo Excel com múltiplas abas.

**Parâmetros:**
- `file` (MultipartFile): Arquivo Excel (XLS ou XLSX) contendo as abas:
  - **Contas**: Aba com nome "Contas"
  - **Cartões de Crédito**: Aba com nome "Cartões de Crédito"
  - **Faturas**: Aba com nome "Faturas"
- `duplicateStrategy` (String): Estratégia para duplicatas
  - `SKIP`: Ignora registros duplicados
  - `CREATE_DUPLICATE`: Cria registros mesmo se duplicados

**Formato do Arquivo:**

**Aba "Contas":**
| ID | Nome | Descrição | Data | Valor Total | Número de Parcelas | Valor da Parcela | ID Cartão | Criado em | Atualizado em |
|---|---|---|---|---|---|---|---|---|---|
| 1 | Compra Mercado | ... | 10/01/2025 | R$ 400,00 | 4 | R$ 100,00 | 1 | ... | ... |

**Aba "Cartões de Crédito":**
| ID | Nome | Limite de Crédito | Limite Disponível | Dia de Fechamento | Dia de Vencimento | Permite Pagamento Parcial | Criado em | Atualizado em |
|---|---|---|---|---|---|---|---|---|
| 1 | Nubank | R$ 5.000,00 | R$ 4.500,00 | 10 | 17 | Sim | ... | ... |

**Aba "Faturas":**
| ID | ID Cartão | Mês de Referência | Valor Total | Saldo Anterior | Fechada | Paga | Criado em | Atualizado em |
|---|---|---|---|---|---|---|---|---|
| 1 | 1 | 01/2025 | R$ 1.200,00 | R$ 0,00 | Não | Não | ... | ... |

**Resposta:**
```json
{
  "bills": {
    "totalProcessed": 10,
    "totalCreated": 8,
    "totalSkipped": 2,
    "totalErrors": 0,
    "duplicatesFound": [...],
    "errors": [],
    "createdBills": [...]
  },
  "creditCards": {
    "totalProcessed": 5,
    "totalCreated": 4,
    "totalSkipped": 1,
    "totalErrors": 0,
    "duplicatesFound": [...],
    "errors": [],
    "createdCreditCards": [...]
  },
  "invoices": {
    "totalProcessed": 12,
    "totalCreated": 10,
    "totalSkipped": 2,
    "totalErrors": 0,
    "duplicatesFound": [...],
    "errors": [],
    "createdInvoices": [...]
  },
  "summary": {
    "totalCreated": 22,
    "totalSkipped": 5,
    "totalErrors": 0
  }
}
```

**Exemplo:**
```bash
curl -X POST http://localhost:8080/unified/import \
  -F "file=@dados.xlsx" \
  -F "duplicateStrategy=SKIP"
```

---

### Importação Individual por Tipo

#### Importar Contas de Arquivo

`POST /bills/bulk-import-file`

Importa contas de um arquivo CSV ou XLS/XLSX.

**Parâmetros:**
- `file` (MultipartFile): Arquivo CSV ou XLS/XLSX
- `duplicateStrategy` (String): SKIP ou CREATE_DUPLICATE

**Cabeçalhos esperados (CSV/Excel):**
- `Nome` (obrigatório)
- `Data` (obrigatório) - formato: dd/MM/yyyy
- `Valor Total` (obrigatório)
- `Número de Parcelas` (obrigatório)
- `Descrição` (opcional)
- `ID Cartão` (opcional)

---

#### Importar Faturas de Arquivo

`POST /invoices/bulk-import-file`

Importa faturas de um arquivo CSV ou XLS/XLSX.

**Parâmetros:**
- `file` (MultipartFile): Arquivo CSV ou XLS/XLSX
- `duplicateStrategy` (String): SKIP ou CREATE_DUPLICATE

**Cabeçalhos esperados (CSV/Excel):**
- `ID Cartão` (obrigatório)
- `Mês de Referência` (obrigatório) - formato: MM/yyyy ou yyyy-MM
- `Valor Total` (obrigatório)
- `Saldo Anterior` (opcional)
- `Fechada` (opcional) - true/false, sim/não
- `Paga` (opcional) - true/false, sim/não

---

#### Importar Cartões de Crédito de Arquivo

`POST /credit-cards/bulk-import-file`

Importa cartões de crédito de um arquivo CSV ou XLS/XLSX.

**Parâmetros:**
- `file` (MultipartFile): Arquivo CSV ou XLS/XLSX
- `duplicateStrategy` (String): SKIP ou CREATE_DUPLICATE

**Cabeçalhos esperados (CSV/Excel):**
- `Nome` (obrigatório)
- `Limite de Crédito` (obrigatório)
- `Dia de Fechamento` (obrigatório) - 1 a 31
- `Dia de Vencimento` (obrigatório) - 1 a 31
- `Permite Pagamento Parcial` (opcional) - true/false, sim/não

---

### Contas

#### Criar Conta

`POST /accounts`

```json
{
  "name": "Compra Mercado",
  "executionDate": "2025-01-10T10:00:00",
  "totalAmount": 400.00,
  "numberOfInstallments": 4,
  "description": "Compras do mês",
  "creditCardId": 1
}
```

Processamento interno:

- Calcula o valor da parcela
- Cria a conta
- Gera as parcelas
- Associa as parcelas às faturas corretas

---

#### Listar Contas

`GET /accounts`

---

#### Buscar Conta por ID

`GET /accounts/{id}`

---

#### Atualizar Conta

`PUT /accounts/{id}`

---

#### Excluir Conta

`DELETE /accounts/{id}`

Ao excluir uma conta, todas as parcelas associadas devem ser removidas.

---

### Cartões de Crédito

#### Criar Cartão

`POST /credit-cards`

```json
{
  "name": "Cartão Nubank",
  "creditLimit": 5000,
  "closingDay": 10,
  "dueDay": 17
}
```

---

#### Listar Cartões

`GET /credit-cards`

---

#### Buscar Cartão por ID

`GET /credit-cards/{id}`

---

#### Atualizar Cartão

`PUT /credit-cards/{id}`

---

#### Excluir Cartão

`DELETE /credit-cards/{id}`

---

### Faturas

#### Listar Faturas de um Cartão

`GET /credit-cards/{id}/invoices`

---

#### Buscar Fatura

`GET /invoices/{id}`

---

#### Fechar Fatura

`POST /invoices/{id}/close`

---

### Parcelas

#### Listar Parcelas de uma Fatura

`GET /invoices/{id}/installments`

---

#### Listar Parcelas de uma Conta

`GET /accounts/{id}/installments`

---

## Restrições e Comportamentos

- Parcelas não podem ser alteradas individualmente
- Contas associadas a faturas fechadas não podem ser alteradas
- Faturas fechadas não aceitam novas parcelas
- Exclusão de conta remove todas as parcelas associadas

---

## Considerações Finais

Esta API foi projetada para manter consistência financeira, rastreabilidade de parcelas e correto controle de faturas futuras, garantindo integridade dos dados e previsibilidade dos valores cobrados.

---

## Referências Adicionais

Para informações detalhadas sobre importação e exportação, consulte:
- **Guia Completo de Import/Export**: `import-export-guide.md`
- **Swagger UI**: http://localhost:8080/swagger-ui.html
