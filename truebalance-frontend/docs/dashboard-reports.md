# Dashboard e Relatórios - TrueBalance

Documentação completa das funcionalidades de Dashboard e Relatórios do TrueBalance.

## 📊 Dashboard

### Visão Geral

O Dashboard é a página principal do sistema, oferecendo uma visão consolidada das finanças do usuário com métricas importantes e visualizações mensais.

### Funcionalidades Principais

#### 1. Filtros de Período

O Dashboard permite visualizar dados de diferentes períodos:

- **Últimos 12 meses** (padrão)
- **Últimos 2 anos** (24 meses)
- **Últimos 5 anos** (60 meses)
- **Últimos 10 anos** (120 meses)

**Implementação:**
```typescript
const [periodFilter, setPeriodFilter] = useState<PeriodFilter>('12')

const { data: monthlyExpenses } = useQuery({
  queryKey: ['monthlyExpenses', periodFilter],
  queryFn: () => reportsService.getMonthlyExpensesByPeriod(Number(periodFilter)),
})
```

#### 2. Cards de Resumo

Três cards principais exibem métricas importantes:

1. **Total no Período**
   - Soma de todos os gastos no período selecionado
   - Inclui contas e cartões de crédito

2. **Gastos do Mês**
   - Total gasto no mês atual
   - Calculado automaticamente baseado na data atual

3. **Média Mensal**
   - Média de gastos por mês no período selecionado
   - Calculado como: `totalPeríodo / númeroDeMeses`

#### 3. Cards Mensais

Cada mês é exibido em um card individual contendo:

- **Cabeçalho do Mês**
  - Nome do mês (em português)
  - Ano
  - Ícone de calendário

- **Total do Mês**
  - Valor total gasto no mês
  - Destaque visual em roxo/violeta

- **Breakdown Detalhado**
  - **Contas**: Valor gasto em contas
  - **Cartões**: Valor gasto em cartões de crédito
  - Percentuais de cada categoria

- **Barra de Progresso**
  - Visualização proporcional entre contas e cartões
  - Cores diferenciadas para cada categoria

**Estrutura do Card:**
```tsx
<Card className="hover:shadow-lg transition-shadow">
  <div className="p-4">
    {/* Header */}
    <div className="flex items-center justify-between mb-4">
      <div>
        <h3 className="text-lg font-semibold capitalize">{expense.month}</h3>
        <p className="text-sm text-gray-600">{expense.year}</p>
      </div>
    </div>

    {/* Total */}
    <p className="text-2xl font-bold text-violet-600">
      {formatCurrency(expense.total)}
    </p>

    {/* Breakdown */}
    <div className="space-y-2">
      <div className="flex justify-between">
        <span>Contas</span>
        <span>{formatCurrency(expense.bills)} ({percentage}%)</span>
      </div>
      {/* Similar para Cartões */}
    </div>

    {/* Progress Bar */}
    <div className="w-full bg-gray-200 rounded-full h-2">
      <div className="bg-violet-600 h-2 rounded-full" style={{ width: `${percentage}%` }} />
    </div>
  </div>
</Card>
```

### Ordenação

Os cards mensais são exibidos em ordem decrescente (mais recente primeiro):

```typescript
{expenses
  .slice()
  .reverse()
  .map((expense) => (
    <MonthlyCard key={monthKey} expense={expense} />
  ))}
```

### Estados da Interface

#### Loading
- Exibe spinner de carregamento enquanto busca dados
- Bloqueia interações durante o carregamento

#### Empty State
- Mensagem: "Nenhum gasto encontrado"
- Descrição: Informa o período selecionado
- Ação: Botão para criar nova conta

#### Error State
- Tratamento de erros via React Query
- Mensagens de erro apropriadas

### Integração com API

O Dashboard utiliza o serviço `reportsService`:

```typescript
import { reportsService } from '@/services/reports.service'

// Buscar gastos por período
const expenses = await reportsService.getMonthlyExpensesByPeriod(12)
```

**Estrutura de Dados:**
```typescript
interface MonthlyExpense {
  month: string      // "janeiro", "fevereiro", etc.
  year: number       // 2024
  bills: number      // Total em contas
  creditCards: number // Total em cartões
  total: number      // Total geral
}
```

---

## 📈 Relatórios

### Visão Geral

A página de Relatórios oferece análises detalhadas dos gastos financeiros com gráficos e métricas avançadas.

### Funcionalidades Principais

#### 1. Seletor de Ano

Permite visualizar relatórios de anos específicos:

- Ano atual (padrão)
- Últimos 5 anos disponíveis
- Dropdown para seleção rápida

**Implementação:**
```typescript
const [selectedYear, setSelectedYear] = useState(currentYear)

const { data: monthlyExpenses } = useQuery({
  queryKey: ['monthlyExpenses', selectedYear],
  queryFn: () => reportsService.getMonthlyExpenses(selectedYear),
})
```

#### 2. Cards de Métricas

Exibe quatro métricas principais:

1. **Total de Gastos**
   - Soma de todos os gastos no ano selecionado

2. **Média Mensal**
   - Média de gastos por mês no ano

3. **Mês com Maior Gasto**
   - Identifica o mês com maior despesa
   - Exibe valor e nome do mês

4. **Mês com Menor Gasto**
   - Identifica o mês com menor despesa
   - Exibe valor e nome do mês

#### 3. Gráfico de Gastos Mensais

Gráfico de linha ou barras mostrando:

- Evolução dos gastos ao longo do ano
- Diferenciação entre contas e cartões
- Valores mensais detalhados

**Componente:**
```tsx
<ExpensesChart data={monthlyExpenses} />
```

#### 4. Distribuição por Categoria

Gráfico de pizza (pie chart) mostrando:

- Percentual de gastos por categoria
- Valores absolutos
- Contagem de itens por categoria

**Categorias Principais:**
- Contas (diversas categorias)
- Cartão de Crédito

**Componente:**
```tsx
<CategoryPieChart data={categoryData} />
```

#### 5. Insights Automáticos

Seção com análises automáticas:

- Gasto médio mensal do ano
- Comparação com período anterior
- Categoria com maior gasto
- Percentuais e tendências

**Exemplo de Insight:**
```
Seu gasto médio mensal em 2024 foi de R$ 1.500,00.

Comparado com o período anterior, seus gastos aumentaram em 15.3%.

A maior parte dos seus gastos está em Cartão de Crédito, 
representando 65.2% do total.
```

#### 6. Exportação de Relatórios

Funcionalidade para exportar relatórios em Excel:

- Múltiplas planilhas:
  - Gastos Mensais
  - Categorias
  - Resumo de Métricas

**Implementação:**
```typescript
const handleExportReport = () => {
  const sheets = [
    {
      name: 'Gastos Mensais',
      data: formatMonthlyExpensesForExport(monthlyExpenses),
    },
    {
      name: 'Categorias',
      data: formatCategoryBreakdownForExport(categoryData),
    },
    {
      name: 'Resumo',
      data: [
        { Métrica: 'Total Gasto', Valor: `R$ ${metrics.totalExpenses}` },
        // ...
      ],
    },
  ]

  exportMultiSheet(sheets, `relatorio_financeiro_${selectedYear}`)
}
```

### Integração com API

A página de Relatórios utiliza múltiplos métodos do `reportsService`:

```typescript
// Gastos mensais
const monthlyExpenses = await reportsService.getMonthlyExpenses(year)

// Métricas
const metrics = await reportsService.getExpenseMetrics(year)

// Breakdown por categoria
const categoryData = await reportsService.getCategoryBreakdown(startDate, endDate)
```

**Filtro por Ano:**
```typescript
const startDate = new Date(selectedYear, 0, 1)
const endDate = new Date(selectedYear, 11, 31, 23, 59, 59)

const categoryData = await reportsService.getCategoryBreakdown(startDate, endDate)
```

---

## 🔄 Visão Consolidada (360°)

### Visão Geral

A Visão Consolidada oferece um panorama completo de todas as finanças em uma única tela.

### Funcionalidades

#### 1. Cards de Resumo

Quatro cards principais:

1. **Gastos Este Mês**
   - Total de contas do mês
   - Total de faturas do mês
   - Soma geral

2. **Limite Utilizado**
   - Percentual de uso do limite
   - Valor usado
   - Valor disponível

3. **Média Mensal**
   - Baseado no ano atual
   - Calculado automaticamente

4. **Itens Pendentes**
   - Contagem de contas a pagar
   - Próximas 30 dias

#### 2. Componentes Integrados

- **Próximas Contas** (`UpcomingBills`)
  - Lista de contas a vencer
  - Próximos 30 dias
  - Badges de urgência

- **Visão Geral de Cartões** (`CreditCardsOverview`)
  - Lista de cartões
  - Limites e disponibilidade
  - Status de uso

- **Timeline de Gastos** (`ExpensesTimeline`)
  - Linha do tempo mensal
  - Comparação entre meses
  - Tendências visuais

#### 3. Estatísticas Rápidas

Rodapé com estatísticas:

- Total de Contas
- Total de Cartões
- Total de Faturas
- Limite Disponível

### Integração com API

```typescript
// Resumo consolidado
const consolidatedData = await reportsService.getConsolidatedSummary()

// Métricas do ano atual
const metrics = await reportsService.getExpenseMetrics(currentYear)
```

---

## 🧪 Testes

### Testes Unitários

#### Reports Service

```typescript
describe('ReportsService', () => {
  it('should fetch and group monthly expenses correctly', async () => {
    // Testa agrupamento mensal
  })

  it('should calculate metrics correctly', async () => {
    // Testa cálculos de métricas
  })

  it('should handle empty data', async () => {
    // Testa estados vazios
  })
})
```

#### Dashboard Component

```typescript
describe('Dashboard', () => {
  it('should render period filter', () => {
    // Testa renderização do filtro
  })

  it('should change period when selecting option', async () => {
    // Testa mudança de período
  })

  it('should display monthly cards', async () => {
    // Testa exibição de cards mensais
  })
})
```

### Testes E2E

```typescript
test('should filter dashboard by period', async ({ page }) => {
  await page.goto('/')
  
  // Selecionar período de 2 anos
  await page.selectOption('select', '24')
  
  // Verificar que dados são atualizados
  await expect(page.locator('[data-testid="total-period"]')).toBeVisible()
})
```

---

## 📝 Notas de Implementação

### Estrutura Paginada

**Importante**: Todos os endpoints retornam dados paginados:

```typescript
interface PaginatedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}
```

Sempre acesse os dados através de `response.data.content`:

```typescript
const billsResponse = await axiosInstance.get<PaginatedResponse<BillResponseDTO>>('/bills')
const bills = billsResponse.data.content || []
```

### Processamento de Datas

**Bills:**
```typescript
const billDate = bill.billDate || bill.date || bill.executionDate
const date = new Date(billDate)
```

**Invoices:**
```typescript
const dateParts = invoice.referenceMonth.split('-')
const year = parseInt(dateParts[0])
const month = parseInt(dateParts[1])
const date = new Date(year, month - 1, 1)
```

### Cálculo de Parcelas

Sempre divida o valor total pelo número de parcelas:

```typescript
const installments = bill.numberOfInstallments || 1
const installmentValue = bill.totalAmount / installments
```

---

## 🎨 Design e UX

### Cores e Temas

- **Violeta/Purple**: Contas e métricas principais
- **Azul**: Cartões de crédito
- **Verde**: Valores positivos e disponibilidade
- **Laranja**: Alertas e pendências

### Responsividade

- **Mobile**: Cards em coluna única
- **Tablet**: Cards em 2 colunas
- **Desktop**: Cards em 3 colunas

### Animações

- Transições suaves ao mudar período
- Hover effects nos cards
- Loading states animados

---

## 🚀 Melhorias Futuras

1. **Filtros Avançados**
   - Filtro por categoria
   - Filtro por valor mínimo/máximo
   - Filtro por status (pago/pendente)

2. **Visualizações Adicionais**
   - Gráficos de tendência anual
   - Comparação entre períodos
   - Previsões baseadas em histórico

3. **Exportação Melhorada**
   - PDF com gráficos
   - CSV para análise externa
   - Compartilhamento de relatórios

4. **Notificações**
   - Alertas de gastos excessivos
   - Lembretes de contas a vencer
   - Metas de gastos mensais

---

**Última atualização**: Janeiro 2025
**Versão**: 2.0
