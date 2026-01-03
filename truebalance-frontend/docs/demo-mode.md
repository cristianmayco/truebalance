# Modo de Demonstração - TrueBalance

Este documento descreve o modo de demonstração (Demo Mode) implementado no TrueBalance.

## 📋 Visão Geral

O modo de demonstração permite que usuários explorem o sistema com dados fictícios realistas, sem precisar criar uma conta ou conectar-se a uma API backend.

**Benefícios:**
- 🎯 Testar funcionalidades sem comprometer dados reais
- 🚀 Demonstrações de vendas e apresentações
- 👀 Preview da aplicação antes de cadastro
- 🧪 Ambiente de testes isolado
- 📚 Tutoriais e documentação

---

## 🏗️ Arquitetura

### Componentes Principais

```
frontend/src/
├── lib/
│   └── mockData.ts          # Dados fictícios
├── contexts/
│   └── DemoContext.tsx      # Estado global do modo demo
└── components/demo/
    ├── DemoBanner.tsx       # Banner de aviso
    └── DemoModeToggle.tsx   # Toggle on/off
```

---

## 📦 Mock Data

### Dados Disponíveis

**Bills (Contas):**
- 6 contas de exemplo
- Variedade de valores e parcelas
- Mix de status (paga/pendente)
- Datas realistas

```ts
import { mockBills } from '@/lib/mockData';

// Exemplo de conta
{
  id: '1',
  name: 'Aluguel',
  description: 'Aluguel mensal do apartamento',
  totalAmount: 2500.00,
  numberOfInstallments: 12,
  isPaid: false,
  // ...
}
```

**Credit Cards (Cartões):**
- 3 cartões de crédito
- Limites variados
- Diferentes bancos (Nubank, Inter, C6)
- Configurações distintas

**Invoices (Faturas):**
- Múltiplas faturas
- Estados diversos (paga, pendente, fechada, aberta)
- Saldos e datas realistas

**Reports Data:**
- Gastos mensais gerados dinamicamente
- Breakdown por categorias
- Métricas calculadas

---

## 🔧 Implementação

### 1. DemoContext

Provider global que gerencia o estado do modo demo.

**Uso:**
```tsx
// main.tsx ou App.tsx
import { DemoProvider } from '@/contexts/DemoContext';

function App() {
  return (
    <DemoProvider>
      <YourApp />
    </DemoProvider>
  );
}
```

**Hook useDemo:**
```tsx
import { useDemo } from '@/contexts/DemoContext';

function MyComponent() {
  const { isDemoMode, enableDemoMode, disableDemoMode } = useDemo();

  return (
    <div>
      {isDemoMode && <p>Demo Mode Active</p>}
      <button onClick={enableDemoMode}>Enable Demo</button>
    </div>
  );
}
```

**API do Hook:**
- `isDemoMode: boolean` - Estado atual
- `enableDemoMode()` - Ativar modo demo (reload page)
- `disableDemoMode()` - Desativar modo demo (reload page)
- `toggleDemoMode()` - Alternar entre ativo/inativo

---

### 2. DemoBanner

Banner no topo da página quando demo mode está ativo.

**Características:**
- Aviso claro e visível
- Botão "Sair do Demo"
- Botão para dispensar temporariamente
- Animação suave de slide-down
- Responsivo (mobile e desktop)
- Acessível (ARIA labels)

**Uso:**
```tsx
import { DemoBanner } from '@/components/demo/DemoBanner';

function AppShell() {
  return (
    <>
      <DemoBanner />
      <MainContent />
    </>
  );
}
```

---

### 3. DemoModeToggle

Componente de toggle para ativar/desativar demo mode.

**Uso:**
```tsx
import { DemoModeToggle } from '@/components/demo/DemoModeToggle';

function SettingsPage() {
  return (
    <div>
      <h1>Configurações</h1>
      <DemoModeToggle />
    </div>
  );
}
```

**Onde usar:**
- Página de configurações
- Footer do site
- Página de landing/marketing
- Sidebar (opcional)

---

## 🎯 Integração com Services

### Padrão de Implementação

Nos services, verificar se está em modo demo e retornar mock data ao invés de chamar API.

**Exemplo:**
```tsx
// src/services/bills.service.ts
import { isDemoMode, mockBills, filterMockBills } from '@/lib/mockData';
import axios from '@/lib/axios';

export async function getAllBills(params?: BillQueryParams) {
  // Check demo mode
  if (isDemoMode()) {
    // Return mock data
    return Promise.resolve({
      data: filterMockBills(params),
      total: mockBills.length,
      page: params?.page || 1,
    });
  }

  // Normal API call
  const response = await axios.get('/bills', { params });
  return response.data;
}

export async function getBillById(id: string) {
  if (isDemoMode()) {
    const bill = mockBills.find(b => b.id === id);
    if (!bill) throw new Error('Bill not found');
    return Promise.resolve(bill);
  }

  const response = await axios.get(`/bills/${id}`);
  return response.data;
}
```

---

### Exemplo Completo com React Query

```tsx
// src/hooks/useBills.ts
import { useQuery } from '@tanstack/react-query';
import { getAllBills } from '@/services/bills.service';

export function useBills(params?: BillQueryParams) {
  return useQuery({
    queryKey: ['bills', params],
    queryFn: () => getAllBills(params),
    // React Query funciona normalmente
    // Service decide se usa mock ou API real
  });
}
```

**No componente:**
```tsx
function BillsList() {
  const { isDemoMode } = useDemo();
  const { data: bills, isLoading } = useBills();

  return (
    <div>
      {isDemoMode && <DemoBanner />}
      {isLoading ? <Loading /> : <BillsTable bills={bills} />}
    </div>
  );
}
```

---

## 🛠️ Helpers Disponíveis

### mockData.ts

**Funções de filtro:**
```ts
// Filtrar bills com parâmetros
filterMockBills({
  search: 'aluguel',
  isPaid: false,
  page: 1,
  limit: 10
})

// Obter limite disponível de cartão
getMockAvailableLimit('cc1') // Retorna número

// Obter fatura atual
getMockCurrentInvoice('cc1') // Retorna InvoiceResponseDTO | null

// Gerar dados de relatório
getMockMonthlyExpenses(2025)   // Gastos mensais
getMockCategoryBreakdown()     // Breakdown por categorias
```

**Funções de persistência:**
```ts
// Verificar modo demo
isDemoMode() // boolean

// Setar modo demo
setDemoMode(true)  // ativar
setDemoMode(false) // desativar

// Alternar modo demo
toggleDemoMode() // retorna novo estado
```

---

## 📱 Fluxo de Usuário

### Ativar Demo Mode

1. Usuário clica em "Ativar Modo Demo"
2. `enableDemoMode()` é chamado
3. Flag salva no localStorage
4. Página recarrega
5. Services detectam demo mode
6. Mock data é retornado
7. DemoBanner aparece no topo

### Desativar Demo Mode

1. Usuário clica em "Sair do Demo" ou "Desativar Modo Demo"
2. `disableDemoMode()` é chamado
3. Flag removida do localStorage
4. Página recarrega
5. Services voltam a chamar API real
6. DemoBanner desaparece

---

## 🎨 Customização

### Adicionar Mais Dados Mock

**Editar mockData.ts:**
```ts
export const mockBills: BillResponseDTO[] = [
  // ... dados existentes
  {
    id: '7',
    name: 'Nova Conta',
    description: 'Descrição',
    totalAmount: 199.90,
    // ... outros campos
  },
];
```

### Criar Helpers Customizados

```ts
// mockData.ts
export function getMockUpcomingBills(days: number = 7) {
  const today = new Date();
  const futureDate = addDays(today, days);

  return mockBills.filter(bill => {
    const billDate = new Date(bill.date);
    return billDate >= today && billDate <= futureDate && !bill.isPaid;
  });
}
```

---

## 🚨 Limitações Conhecidas

### O que NÃO funciona em demo mode:

1. **Persistência de dados**
   - Criar, editar, deletar não salva
   - Pode simular com estado local temporário

2. **Autenticação**
   - Não há usuários ou login

3. **Upload de arquivos**
   - Não aplicável

4. **Integração externa**
   - Não conecta com APIs externas

5. **Notificações push**
   - Não funciona (requer backend)

### Workarounds:

**Simular criação de item:**
```ts
// Hook customizado para demo
export function useCreateBillDemo() {
  const { isDemoMode } = useDemo();
  const mutation = useCreateBill(); // Mutation real

  if (isDemoMode) {
    return {
      mutate: (data: BillRequestDTO) => {
        // Simular sucesso
        console.log('Demo mode: Bill created', data);
        // Adicionar ao estado local temporário
      },
      isLoading: false,
      isSuccess: true,
    };
  }

  return mutation;
}
```

---

## ♿ Acessibilidade

**DemoBanner:**
- `role="alert"` - Anunciado por screen readers
- `aria-live="polite"` - Não interrompe leitura
- Botões com `aria-label` descritivos
- Contraste adequado (WCAG AA)

**DemoModeToggle:**
- Labels descritivos
- Estado visual claro (badge "Ativo")
- Focus indicators
- Keyboard accessible

---

## 🧪 Testes

### Testar Demo Mode

**Manual:**
1. Abrir aplicação
2. Ativar demo mode
3. Navegar por todas as páginas
4. Verificar dados fictícios
5. Testar filtros, busca, paginação
6. Desativar demo mode
7. Confirmar volta ao normal

**Automatizado:**
```tsx
// __tests__/demo.test.tsx
import { render, screen } from '@testing-library/react';
import { DemoProvider, useDemo } from '@/contexts/DemoContext';

describe('Demo Mode', () => {
  it('should toggle demo mode', () => {
    const { result } = renderHook(() => useDemo(), {
      wrapper: DemoProvider,
    });

    expect(result.current.isDemoMode).toBe(false);

    act(() => {
      result.current.enableDemoMode();
    });

    expect(result.current.isDemoMode).toBe(true);
  });

  it('should show demo banner when active', () => {
    setDemoMode(true);

    render(
      <DemoProvider>
        <DemoBanner />
      </DemoProvider>
    );

    expect(screen.getByText(/Modo de Demonstração Ativo/i)).toBeInTheDocument();
  });
});
```

---

## 📚 Casos de Uso

### 1. Landing Page

Permitir visitantes explorarem antes de cadastro.

```tsx
function LandingPage() {
  const { enableDemoMode } = useDemo();

  return (
    <div>
      <h1>Conheça o TrueBalance</h1>
      <button onClick={enableDemoMode}>
        Explorar Demo
      </button>
    </div>
  );
}
```

### 2. Onboarding

Guiar novos usuários com dados de exemplo.

```tsx
function Onboarding() {
  const { isDemoMode, enableDemoMode } = useDemo();

  useEffect(() => {
    // Auto-ativar demo para onboarding
    if (!isDemoMode) {
      enableDemoMode();
    }
  }, []);

  return <Tutorial />;
}
```

### 3. Apresentações

Modo apresentação para vendas/demos.

```tsx
function PresentationMode() {
  const { isDemoMode } = useDemo();

  // Garantir demo mode ativo
  if (!isDemoMode) {
    setDemoMode(true);
    window.location.reload();
  }

  return <FullScreenDemo />;
}
```

---

## 🚀 Próximos Passos

### Melhorias Futuras:

1. **Múltiplos Perfis Demo**
   ```ts
   // Diferentes cenários
   setDemoProfile('student')   // Baixa renda
   setDemoProfile('professional') // Média renda
   setDemoProfile('executive')  // Alta renda
   ```

2. **Guided Tour**
   - Integrar com biblioteca de tours (driver.js, intro.js)
   - Destacar features importantes
   - Tooltips interativos

3. **Reset Demo Data**
   ```ts
   resetDemoData() // Voltar ao estado inicial
   ```

4. **Exportar Configuração Demo**
   - Permitir salvar estado atual
   - Compartilhar configuração
   - Importar cenários customizados

5. **Analytics de Demo**
   - Track quais features foram exploradas
   - Tempo gasto em cada seção
   - Conversão demo → cadastro

---

**Documento criado em:** Dezembro 2025
**Versão:** 1.0
**Mantido por:** Equipe de Desenvolvimento TrueBalance
