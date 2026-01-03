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
