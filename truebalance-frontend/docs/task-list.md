# Lista de Tarefas - Desenvolvimento TrueBalance Frontend

**Projeto:** TrueBalance - Sistema de Gerenciamento Financeiro Pessoal
**Stack:** React 18 + TypeScript + Tailwind CSS + Vite
**Status:** Em desenvolvimento ativo - ✅ **Fase 4 Concluída!** | ⏳ **Fase 5 em Andamento**
**Última atualização:** Dezembro 2025

**Progresso:**
- ✅ Fase 4 (Recursos Avançados) - 12/12 tarefas concluídas
- ⏳ Fase 5 (Refinamento) - 9/15 tarefas concluídas (100% completo: 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8)
- ⏳ Fases 1, 2, 3 - Em andamento

---

## 📋 Índice de Fases

- [Fase 1 - Fundação](#fase-1---fundação-prioridade-alta)
- [Fase 2 - Telas de Contas](#fase-2---telas-de-contas-prioridade-alta)
- [Fase 3 - Cartões e Faturas](#fase-3---cartões-e-faturas-prioridade-média)
- [✅ Fase 4 - Recursos Avançados](#fase-4---recursos-avançados-prioridade-média-) **(CONCLUÍDA)**
- [⏳ Fase 5 - Refinamento](#fase-5---refinamento-prioridade-baixa) **(EM ANDAMENTO)**

---

## Fase 1 - Fundação (Prioridade Alta)

Esta fase estabelece a base do projeto. Todas as outras fases dependem desta.

### 1.1. Setup de Dependências ✅

**Descrição:** Instalar e configurar todas as dependências necessárias para o projeto.

**Subtarefas:**
- [x] Instalar `react-router-dom` para roteamento
- [x] Instalar `axios` para requisições HTTP
- [x] Instalar `@tanstack/react-query` e `@tanstack/react-query-devtools` para gerenciamento de estado server
- [x] Instalar `lucide-react` para ícones
- [x] Instalar `recharts` para gráficos e relatórios
- [x] Instalar `react-hook-form` para gerenciamento de formulários
- [x] Instalar `zod` para validação de schemas
- [x] Instalar `date-fns` para manipulação de datas

**Arquivos envolvidos:**
- `package.json`

**Status:** ✅ Implementado

---

### 1.2. Configuração de Variáveis de Ambiente ✅

**Descrição:** Criar arquivos de configuração para diferentes ambientes.

**Subtarefas:**
- [x] Criar `.env.development` com URL da API local
- [x] Criar `.env.production` com URL da API de produção
- [x] Criar `src/config/api.ts` com configurações base
- [x] Adicionar `.env*` ao `.gitignore`

**Arquivos criados:**
- `.env.development`
- `.env.production`
- `src/config/api.ts`

**Status:** ✅ Implementado

---

### 1.3. Setup do Cliente HTTP (Axios) ✅

**Descrição:** Configurar cliente Axios com interceptors para requisições à API.

**Subtarefas:**
- [x] Criar `src/lib/axios.ts` com configuração do cliente
- [x] Implementar request interceptor (para adicionar headers, tokens futuros)
- [x] Implementar response interceptor (para tratamento de erros globais)
- [x] Configurar timeout e headers padrão

**Arquivos criados:**
- `src/lib/axios.ts`

**Status:** ✅ Implementado

---

### 1.4. Setup do React Query ✅

**Descrição:** Configurar TanStack Query para gerenciamento de cache e estado server.

**Subtarefas:**
- [x] Criar `src/lib/queryClient.ts` com configuração do QueryClient
- [x] Configurar opções de staleTime, cacheTime, retry
- [x] Adicionar QueryClientProvider no `main.tsx`
- [x] Adicionar ReactQueryDevtools para desenvolvimento

**Arquivos criados:**
- `src/lib/queryClient.ts`

**Arquivos modificados:**
- `src/main.tsx`

**Status:** ✅ Implementado

---

### 1.5. Definição de Types e DTOs ✅

**Descrição:** Criar interfaces TypeScript para todas as entidades da API.

**Subtarefas:**
- [x] Criar `src/types/dtos/bill.dto.ts` com BillRequestDTO e BillResponseDTO
- [x] Criar `src/types/dtos/creditCard.dto.ts` com CreditCardRequestDTO e CreditCardResponseDTO
- [x] Criar `src/types/dtos/invoice.dto.ts` com InvoiceResponseDTO
- [x] Criar `src/types/dtos/installment.dto.ts` com InstallmentResponseDTO
- [x] Criar `src/types/dtos/partialPayment.dto.ts` com PartialPaymentRequestDTO e ResponseDTO
- [x] Criar `src/types/dtos/common.dto.ts` com PaginatedResponse e outras interfaces compartilhadas

**Arquivos criados:**
- `src/types/dtos/bill.dto.ts`
- `src/types/dtos/creditCard.dto.ts`
- `src/types/dtos/invoice.dto.ts`
- `src/types/dtos/installment.dto.ts`
- `src/types/dtos/partialPayment.dto.ts`
- `src/types/dtos/common.dto.ts`

**Status:** ✅ Implementado

---

### 1.6. Context de Tema (Dark/Light Mode) ✅

**Descrição:** Implementar sistema de alternância entre tema claro e escuro.

**Subtarefas:**
- [x] Criar `src/contexts/ThemeContext.tsx`
- [x] Implementar lógica de toggle entre light/dark
- [x] Adicionar persistência no localStorage
- [x] Aplicar classe 'dark' no HTML root quando dark mode ativo
- [x] Adicionar hook `useTheme` para consumir o contexto

**Arquivos criados:**
- `src/contexts/ThemeContext.tsx`

**Arquivos modificados:**
- `src/main.tsx` (adicionar ThemeProvider)

**Status:** ✅ Implementado

---

### 1.7. Customização do Tailwind CSS ✅

**Descrição:** Configurar cores personalizadas e extensões do Tailwind.

**Subtarefas:**
- [x] Adicionar cores customizadas (violet/purple como primária)
- [x] Configurar fonte Inter via Google Fonts
- [x] Adicionar configurações de dark mode
- [x] Estender theme com cores semânticas (success, warning, error, info)

**Arquivos modificados:**
- `tailwind.config.js`
- `index.html` (adicionar link do Google Fonts)

**Status:** ✅ Implementado

---

### 1.8. Componente de Layout - AppShell ✅

**Descrição:** Criar componente wrapper principal que contém sidebar e topbar.

**Subtarefas:**
- [x] Criar `src/components/layout/AppShell.tsx`
- [x] Implementar layout com sidebar fixa no desktop
- [x] Implementar layout com bottom nav no mobile
- [x] Adicionar área de conteúdo principal com padding responsivo

**Arquivos criados:**
- `src/components/layout/AppShell.tsx`

**Status:** ✅ Implementado

---

### 1.9. Componente de Layout - Sidebar (Desktop) ✅

**Descrição:** Criar barra lateral de navegação para desktop.

**Subtarefas:**
- [x] Criar `src/components/layout/Sidebar.tsx`
- [x] Adicionar logo/branding no topo
- [x] Implementar lista de links de navegação
- [x] Adicionar ícones Lucide para cada seção
- [x] Implementar estado ativo/selected
- [x] Estilizar com cores do tema (light/dark)
- [x] Ocultar em mobile (display: hidden lg:flex)

**Arquivos criados:**
- `src/components/layout/Sidebar.tsx`

**Status:** ✅ Implementado

---

### 1.10. Componente de Layout - TopBar ✅

**Descrição:** Criar barra superior com título da página e toggle de tema.

**Subtarefas:**
- [x] Criar `src/components/layout/TopBar.tsx`
- [x] Adicionar título dinâmico da página
- [x] Adicionar botão de toggle de tema no canto direito
- [x] Implementar design responsivo
- [x] Adicionar ícones de sol/lua para tema

**Arquivos criados:**
- `src/components/layout/TopBar.tsx`

**Status:** ✅ Implementado

---

### 1.11. Componente de Layout - BottomNav (Mobile) ✅

**Descrição:** Criar barra de navegação inferior para mobile.

**Subtarefas:**
- [x] Criar `src/components/layout/BottomNav.tsx`
- [x] Adicionar links principais (Dashboard, Contas, Cartões, Relatórios)
- [x] Implementar ícones e labels
- [x] Adicionar estado ativo
- [x] Fixar na parte inferior (fixed bottom-0)
- [x] Ocultar em desktop (lg:hidden)

**Arquivos criados:**
- `src/components/layout/BottomNav.tsx`

**Status:** ✅ Implementado

---

### 1.12. Componente ThemeToggle ✅

**Descrição:** Criar botão de alternância de tema reutilizável.

**Subtarefas:**
- [x] Criar `src/components/layout/ThemeToggle.tsx`
- [x] Adicionar ícones de Sol (light) e Lua (dark)
- [x] Conectar com ThemeContext
- [x] Adicionar animação de transição suave

**Arquivos criados:**
- `src/components/layout/ThemeToggle.tsx`

**Status:** ✅ Implementado

---

### 1.13. Biblioteca de Componentes UI - Input

**Descrição:** Criar componente Input reutilizável com suporte a validação.

**Subtarefas:**
- [ ] Criar `src/components/ui/Input.tsx`
- [ ] Suportar tipos: text, number, email, password, date
- [ ] Adicionar props para label, placeholder, error, required
- [ ] Implementar estilos para light/dark mode
- [ ] Adicionar estado de erro com mensagem
- [ ] Implementar focus ring acessível

**Arquivos criados:**
- `src/components/ui/Input.tsx`

---

### 1.14. Biblioteca de Componentes UI - Select

**Descrição:** Criar componente Select (dropdown) reutilizável.

**Subtarefas:**
- [ ] Criar `src/components/ui/Select.tsx`
- [ ] Suportar array de options com value/label
- [ ] Adicionar props para label, error, required
- [ ] Implementar estilos para light/dark mode
- [ ] Adicionar estado de erro

**Arquivos criados:**
- `src/components/ui/Select.tsx`

---

### 1.15. Biblioteca de Componentes UI - Card

**Descrição:** Criar componente Card para exibir informações em blocos.

**Subtarefas:**
- [ ] Criar `src/components/ui/Card.tsx`
- [ ] Suportar variantes (default, gradient, outlined)
- [ ] Adicionar props para padding, hover effects
- [ ] Implementar shadow e border radius
- [ ] Suportar light/dark mode

**Arquivos criados:**
- `src/components/ui/Card.tsx`

---

### 1.16. Biblioteca de Componentes UI - Badge

**Descrição:** Criar componente Badge para status e categorias.

**Subtarefas:**
- [ ] Criar `src/components/ui/Badge.tsx`
- [ ] Suportar variantes: success, warning, error, info, default
- [ ] Implementar cores semânticas
- [ ] Suportar tamanhos (sm, md, lg)
- [ ] Adicionar suporte a ícones

**Arquivos criados:**
- `src/components/ui/Badge.tsx`

---

### 1.17. Biblioteca de Componentes UI - Button

**Descrição:** Melhorar componente Button existente com mais variantes.

**Subtarefas:**
- [ ] Adicionar variantes: primary, secondary, ghost, danger
- [ ] Suportar tamanhos: sm, md, lg
- [ ] Adicionar estado loading com spinner
- [ ] Adicionar estado disabled
- [ ] Suportar ícones (leading/trailing)

**Arquivos modificados:**
- `src/components/Button.tsx` (ou mover para `src/components/ui/Button.tsx`)

---

### 1.18. Biblioteca de Componentes UI - Modal

**Descrição:** Criar componente Modal para diálogos e formulários.

**Subtarefas:**
- [ ] Criar `src/components/ui/Modal.tsx`
- [ ] Implementar overlay com backdrop
- [ ] Adicionar animação de entrada/saída
- [ ] Implementar fechamento ao clicar fora
- [ ] Implementar fechamento com tecla ESC
- [ ] Suportar fullscreen em mobile
- [ ] Adicionar header, body, footer slots

**Arquivos criados:**
- `src/components/ui/Modal.tsx`

---

### 1.19. Biblioteca de Componentes UI - Toast/Notification

**Descrição:** Criar sistema de notificações toast.

**Subtarefas:**
- [ ] Criar `src/components/ui/Toast.tsx`
- [ ] Criar `src/contexts/ToastContext.tsx` para gerenciar toasts
- [ ] Implementar tipos: success, error, warning, info
- [ ] Adicionar auto-dismiss configurável
- [ ] Adicionar animações de entrada/saída
- [ ] Posicionar no canto superior direito

**Arquivos criados:**
- `src/components/ui/Toast.tsx`
- `src/contexts/ToastContext.tsx`

---

### 1.20. Biblioteca de Componentes UI - LoadingSpinner

**Descrição:** Criar componente de loading/spinner.

**Subtarefas:**
- [ ] Criar `src/components/ui/LoadingSpinner.tsx`
- [ ] Implementar spinner animado (usando Lucide Loader)
- [ ] Suportar tamanhos (sm, md, lg)
- [ ] Adicionar variante de tela cheia (overlay)
- [ ] Suportar light/dark mode

**Arquivos criados:**
- `src/components/ui/LoadingSpinner.tsx`

---

### 1.21. Biblioteca de Componentes UI - Table

**Descrição:** Criar componente Table responsivo.

**Subtarefas:**
- [ ] Criar `src/components/ui/Table.tsx`
- [ ] Implementar cabeçalho e corpo
- [ ] Adicionar hover em linhas
- [ ] Implementar versão mobile (cards empilhados)
- [ ] Suportar sorting visual (setas)
- [ ] Adicionar estados vazios

**Arquivos criados:**
- `src/components/ui/Table.tsx`

---

### 1.22. Setup de Rotas com React Router

**Descrição:** Configurar sistema de roteamento da aplicação.

**Subtarefas:**
- [ ] Criar `src/routes/index.tsx` com todas as rotas
- [ ] Configurar BrowserRouter no `main.tsx`
- [ ] Criar rota raiz `/` para Dashboard
- [ ] Criar rotas para Bills: `/bills`, `/bills/new`, `/bills/:id`
- [ ] Criar rotas para Credit Cards: `/credit-cards`, `/credit-cards/new`, `/credit-cards/:id`
- [ ] Criar rotas para Invoices: `/invoices`, `/invoices/:id`, `/invoices/:id/payment`
- [ ] Criar rota para Reports: `/reports`
- [ ] Criar rota para Consolidated View: `/consolidated`
- [ ] Implementar NotFound (404) page

**Arquivos criados:**
- `src/routes/index.tsx`

**Arquivos modificados:**
- `src/main.tsx`

---

### 1.23. Componente EmptyState

**Descrição:** Criar componente para exibir estado vazio (sem dados).

**Subtarefas:**
- [ ] Criar `src/components/ui/EmptyState.tsx`
- [ ] Adicionar ícone ilustrativo
- [ ] Adicionar mensagem personalizável
- [ ] Adicionar CTA button opcional
- [ ] Implementar design responsivo

**Arquivos criados:**
- `src/components/ui/EmptyState.tsx`

---

### 1.24. Componente ErrorBoundary

**Descrição:** Criar Error Boundary para captura de erros React.

**Subtarefas:**
- [ ] Criar `src/components/ErrorBoundary.tsx`
- [ ] Implementar fallback UI com mensagem de erro
- [ ] Adicionar botão "Tentar novamente"
- [ ] Integrar com React Query error reset

**Arquivos criados:**
- `src/components/ErrorBoundary.tsx`

---

## Fase 2 - Telas de Contas (Prioridade Alta)

Esta fase implementa o CRUD completo de contas (bills).

### 2.1. Services - Bills API

**Descrição:** Criar service para comunicação com API de Bills.

**Subtarefas:**
- [ ] Criar `src/services/bills.service.ts`
- [ ] Implementar `getAll(params)` com suporte a paginação e filtros
- [ ] Implementar `getById(id)`
- [ ] Implementar `create(bill)`
- [ ] Implementar `update(id, bill)`
- [ ] Implementar `delete(id)`

**Arquivos criados:**
- `src/services/bills.service.ts`

---

### 2.2. Hooks - useBills

**Descrição:** Criar custom hooks React Query para Bills.

**Subtarefas:**
- [ ] Criar `src/hooks/useBills.ts`
- [ ] Implementar `useBills(params)` para listagem
- [ ] Implementar `useBill(id)` para buscar por ID
- [ ] Implementar `useCreateBill()` mutation
- [ ] Implementar `useUpdateBill()` mutation
- [ ] Implementar `useDeleteBill()` mutation
- [ ] Configurar invalidação de cache após mutations

**Arquivos criados:**
- `src/hooks/useBills.ts`

---

### 2.3. Página - Dashboard/Home

**Descrição:** Criar página inicial com visão geral financeira.

**Subtarefas:**
- [ ] Criar `src/pages/Dashboard.tsx`
- [ ] Adicionar cards de resumo (total de contas, gastos do mês, próximas contas)
- [ ] Exibir lista de contas recentes (últimas 5)
- [ ] Adicionar botão de ação rápida "Nova Conta"
- [ ] Implementar estados de loading e erro
- [ ] Tornar responsivo (mobile-first)

**Arquivos criados:**
- `src/pages/Dashboard.tsx`

---

### 2.4. Componente - BillCard

**Descrição:** Criar card para exibir informações de uma conta.

**Subtarefas:**
- [ ] Criar `src/components/bills/BillCard.tsx`
- [ ] Exibir nome, valor, data, parcelas
- [ ] Adicionar badge de status (paga/pendente)
- [ ] Adicionar botões de ação (editar, deletar)
- [ ] Implementar confirmação de deleção
- [ ] Tornar responsivo

**Arquivos criados:**
- `src/components/bills/BillCard.tsx`

---

### 2.5. Página - Bills List

**Descrição:** Criar página de listagem de todas as contas.

**Subtarefas:**
- [ ] Criar `src/pages/bills/BillsList.tsx`
- [ ] Implementar tabela de contas (desktop)
- [ ] Implementar lista de cards (mobile)
- [ ] Adicionar campo de busca/filtro
- [ ] Implementar paginação
- [ ] Adicionar botão "Nova Conta"
- [ ] Implementar estados de loading, erro, vazio
- [ ] Adicionar botões de ação em cada linha (editar, deletar)

**Arquivos criados:**
- `src/pages/bills/BillsList.tsx`

---

### 2.6. Componente - BillsTable

**Descrição:** Criar componente de tabela específico para contas.

**Subtarefas:**
- [ ] Criar `src/components/bills/BillsTable.tsx`
- [ ] Exibir colunas: Nome, Data, Valor Total, Parcelas, Ações
- [ ] Adicionar sorting por coluna
- [ ] Implementar row actions (editar, deletar)
- [ ] Adicionar hover effects
- [ ] Ocultar em mobile (usar BillCard no lugar)

**Arquivos criados:**
- `src/components/bills/BillsTable.tsx`

---

### 2.7. Componente - BillFilters

**Descrição:** Criar componente de filtros para listagem de contas.

**Subtarefas:**
- [ ] Criar `src/components/bills/BillFilters.tsx`
- [ ] Adicionar input de busca por nome
- [ ] Adicionar filtro por período (data início/fim)
- [ ] Adicionar filtro por status (paga/pendente)
- [ ] Implementar botão "Limpar filtros"
- [ ] Tornar responsivo (collapse em mobile)

**Arquivos criados:**
- `src/components/bills/BillFilters.tsx`

---

### 2.8. Componente - Pagination

**Descrição:** Criar componente de paginação reutilizável.

**Subtarefas:**
- [ ] Criar `src/components/ui/Pagination.tsx`
- [ ] Implementar botões Anterior/Próximo
- [ ] Exibir número da página atual e total
- [ ] Adicionar input para ir para página específica
- [ ] Tornar responsivo
- [ ] Desabilitar botões quando não aplicável

**Arquivos criados:**
- `src/components/ui/Pagination.tsx`

---

### 2.9. Página - Bill Form (Cadastro/Edição)

**Descrição:** Criar página de formulário para criar/editar contas.

**Subtarefas:**
- [ ] Criar `src/pages/bills/BillForm.tsx`
- [ ] Implementar formulário com react-hook-form
- [ ] Adicionar campos: nome, data, valor total, número de parcelas, descrição
- [ ] Implementar validação com Zod
- [ ] Adicionar cálculo automático de valor da parcela
- [ ] Implementar modo criação e edição (baseado na URL)
- [ ] Adicionar botões Salvar e Cancelar
- [ ] Implementar feedback de sucesso/erro com Toast
- [ ] Redirecionar para lista após salvar
- [ ] Tornar responsivo

**Arquivos criados:**
- `src/pages/bills/BillForm.tsx`

---

### 2.10. Schema de Validação - Bill

**Descrição:** Criar schema Zod para validação de formulário de conta.

**Subtarefas:**
- [ ] Criar `src/schemas/bill.schema.ts`
- [ ] Definir validações para todos os campos
- [ ] Adicionar mensagens de erro customizadas em português
- [ ] Validar número de parcelas >= 1
- [ ] Validar valor total > 0

**Arquivos criados:**
- `src/schemas/bill.schema.ts`

---

### 2.11. Página - Bill Quick Add (Cadastro Rápido)

**Descrição:** Criar modal de cadastro rápido de conta.

**Subtarefas:**
- [ ] Criar `src/pages/bills/BillQuickAdd.tsx`
- [ ] Implementar como Modal
- [ ] Adicionar apenas campos essenciais (nome, valor, data, parcelas)
- [ ] Usar mesma validação do formulário completo
- [ ] Implementar feedback de sucesso
- [ ] Fechar modal automaticamente após salvar
- [ ] Invalidar cache para atualizar listas

**Arquivos criados:**
- `src/pages/bills/BillQuickAdd.tsx`

---

### 2.12. Componente - DeleteConfirmation

**Descrição:** Criar modal de confirmação de deleção reutilizável.

**Subtarefas:**
- [ ] Criar `src/components/ui/DeleteConfirmation.tsx`
- [ ] Exibir mensagem de confirmação
- [ ] Adicionar botões Confirmar (danger) e Cancelar
- [ ] Implementar estado de loading durante deleção
- [ ] Adicionar ícone de alerta
- [ ] Permitir customização de mensagem

**Arquivos criados:**
- `src/components/ui/DeleteConfirmation.tsx`

---

### 2.13. Utilitário - Formatação de Moeda

**Descrição:** Criar função helper para formatar valores monetários.

**Subtarefas:**
- [ ] Criar `src/utils/currency.ts`
- [ ] Implementar função `formatCurrency(value)` para formato BRL
- [ ] Implementar função `parseCurrency(string)` para converter string em número

**Arquivos criados:**
- `src/utils/currency.ts`

---

### 2.14. Utilitário - Formatação de Data

**Descrição:** Criar funções helper para formatar datas.

**Subtarefas:**
- [ ] Criar `src/utils/date.ts`
- [ ] Implementar função `formatDate(date, format)` usando date-fns
- [ ] Implementar função `formatRelative(date)` (ex: "há 2 dias")
- [ ] Implementar função `isToday(date)`, `isTomorrow(date)`

**Arquivos criados:**
- `src/utils/date.ts`

---

## Fase 3 - Cartões e Faturas (Prioridade Média)

Esta fase implementa gestão de cartões de crédito e suas faturas.

### 3.1. Services - Credit Cards API

**Descrição:** Criar service para comunicação com API de Credit Cards.

**Subtarefas:**
- [ ] Criar `src/services/creditCards.service.ts`
- [ ] Implementar `getAll()`
- [ ] Implementar `getById(id)`
- [ ] Implementar `getAvailableLimit(id)`
- [ ] Implementar `create(card)`
- [ ] Implementar `update(id, card)`
- [ ] Implementar `delete(id)`

**Arquivos criados:**
- `src/services/creditCards.service.ts`

---

### 3.2. Hooks - useCreditCards

**Descrição:** Criar custom hooks React Query para Credit Cards.

**Subtarefas:**
- [ ] Criar `src/hooks/useCreditCards.ts`
- [ ] Implementar `useCreditCards()` para listagem
- [ ] Implementar `useCreditCard(id)` para buscar por ID
- [ ] Implementar `useCreditCardLimit(id)` para limite disponível
- [ ] Implementar `useCreateCreditCard()` mutation
- [ ] Implementar `useUpdateCreditCard()` mutation
- [ ] Implementar `useDeleteCreditCard()` mutation

**Arquivos criados:**
- `src/hooks/useCreditCards.ts`

---

### 3.3. Página - Credit Cards List

**Descrição:** Criar página de listagem de cartões de crédito.

**Subtarefas:**
- [ ] Criar `src/pages/creditCards/CreditCardsList.tsx`
- [ ] Implementar grid de cards visuais (não tabela)
- [ ] Exibir nome, limite, limite disponível, dias de fechamento/vencimento
- [ ] Adicionar indicador visual de limite usado (progress bar)
- [ ] Adicionar botão "Novo Cartão"
- [ ] Adicionar botões de ação em cada card (editar, deletar, ver faturas)
- [ ] Implementar estados de loading, erro, vazio
- [ ] Tornar responsivo (1 coluna mobile, 2 tablet, 3 desktop)

**Arquivos criados:**
- `src/pages/creditCards/CreditCardsList.tsx`

---

### 3.4. Componente - CreditCardCard

**Descrição:** Criar card visual para exibir cartão de crédito.

**Subtarefas:**
- [ ] Criar `src/components/creditCards/CreditCardCard.tsx`
- [ ] Design estilo "cartão de crédito" com gradiente
- [ ] Exibir nome, limite total, limite disponível
- [ ] Adicionar progress bar de uso do limite
- [ ] Exibir dias de fechamento e vencimento
- [ ] Adicionar menu de ações (três pontinhos)
- [ ] Tornar clicável para ver detalhes/faturas

**Arquivos criados:**
- `src/components/creditCards/CreditCardCard.tsx`

---

### 3.5. Componente - ProgressBar

**Descrição:** Criar barra de progresso reutilizável.

**Subtarefas:**
- [ ] Criar `src/components/ui/ProgressBar.tsx`
- [ ] Suportar porcentagem (0-100)
- [ ] Adicionar variantes de cor (success, warning, danger)
- [ ] Adicionar label opcional
- [ ] Implementar animação de preenchimento

**Arquivos criados:**
- `src/components/ui/ProgressBar.tsx`

---

### 3.6. Página - Credit Card Form

**Descrição:** Criar página de formulário para criar/editar cartões.

**Subtarefas:**
- [ ] Criar `src/pages/creditCards/CreditCardForm.tsx`
- [ ] Implementar formulário com react-hook-form
- [ ] Adicionar campos: nome, limite, dia fechamento, dia vencimento, permite pagamento parcial
- [ ] Implementar validação com Zod
- [ ] Validar dias entre 1-31
- [ ] Adicionar toggle para "permite pagamento parcial"
- [ ] Implementar modo criação e edição
- [ ] Adicionar feedback com Toast
- [ ] Tornar responsivo

**Arquivos criados:**
- `src/pages/creditCards/CreditCardForm.tsx`

---

### 3.7. Schema de Validação - Credit Card

**Descrição:** Criar schema Zod para validação de cartão.

**Subtarefas:**
- [ ] Criar `src/schemas/creditCard.schema.ts`
- [ ] Validar todos os campos
- [ ] Validar limite > 0
- [ ] Validar dias de fechamento/vencimento entre 1-31
- [ ] Adicionar mensagens em português

**Arquivos criados:**
- `src/schemas/creditCard.schema.ts`

---

### 3.8. Services - Invoices API

**Descrição:** Criar service para comunicação com API de Invoices.

**Subtarefas:**
- [ ] Criar `src/services/invoices.service.ts`
- [ ] Implementar `getByCreditCard(creditCardId)`
- [ ] Implementar `getById(id)`
- [ ] Implementar `getInstallments(invoiceId)`
- [ ] Implementar `markAsPaid(invoiceId)`
- [ ] Implementar `addPartialPayment(invoiceId, payment)`

**Arquivos criados:**
- `src/services/invoices.service.ts`

---

### 3.9. Hooks - useInvoices

**Descrição:** Criar custom hooks React Query para Invoices.

**Subtarefas:**
- [ ] Criar `src/hooks/useInvoices.ts`
- [ ] Implementar `useInvoices(creditCardId)` para listar faturas de um cartão
- [ ] Implementar `useInvoice(id)` para buscar fatura por ID
- [ ] Implementar `useInvoiceInstallments(id)` para parcelas da fatura
- [ ] Implementar `useMarkInvoiceAsPaid()` mutation
- [ ] Implementar `useAddPartialPayment()` mutation

**Arquivos criados:**
- `src/hooks/useInvoices.ts`

---

### 3.10. Página - Invoices List

**Descrição:** Criar página de listagem de faturas de um cartão.

**Subtarefas:**
- [ ] Criar `src/pages/invoices/InvoicesList.tsx`
- [ ] Receber creditCardId via URL params ou state
- [ ] Exibir nome do cartão no header
- [ ] Listar faturas em cards (mês/ano, valor total, status)
- [ ] Adicionar badges de status (fechada, paga, aberta)
- [ ] Adicionar botão para ver detalhes de cada fatura
- [ ] Implementar filtro por status
- [ ] Implementar estados de loading, erro, vazio
- [ ] Tornar responsivo

**Arquivos criados:**
- `src/pages/invoices/InvoicesList.tsx`

---

### 3.11. Componente - InvoiceCard

**Descrição:** Criar card para exibir resumo de uma fatura.

**Subtarefas:**
- [ ] Criar `src/components/invoices/InvoiceCard.tsx`
- [ ] Exibir mês de referência, valor total
- [ ] Exibir saldo anterior se houver
- [ ] Adicionar badges de status (fechada/paga)
- [ ] Adicionar botão "Ver Detalhes"
- [ ] Adicionar botão "Pagar" se não paga
- [ ] Implementar cores diferentes por status

**Arquivos criados:**
- `src/components/invoices/InvoiceCard.tsx`

---

### 3.12. Página - Invoice Details

**Descrição:** Criar página de detalhes de uma fatura.

**Subtarefas:**
- [ ] Criar `src/pages/invoices/InvoiceDetails.tsx`
- [ ] Exibir header com mês, valor total, status
- [ ] Listar todas as parcelas da fatura (tabela)
- [ ] Exibir saldo anterior se houver
- [ ] Listar pagamentos parciais realizados
- [ ] Calcular e exibir valor restante a pagar
- [ ] Adicionar botão "Pagar Fatura" (redireciona para tela de pagamento)
- [ ] Adicionar botão "Marcar como Paga" (se fatura fechada)
- [ ] Implementar estados de loading, erro
- [ ] Tornar responsivo

**Arquivos criados:**
- `src/pages/invoices/InvoiceDetails.tsx`

---

### 3.13. Componente - InstallmentsTable

**Descrição:** Criar tabela de parcelas de uma fatura.

**Subtarefas:**
- [ ] Criar `src/components/invoices/InstallmentsTable.tsx`
- [ ] Exibir colunas: Descrição, Parcela (X/Y), Valor
- [ ] Agrupar por conta/compra se possível
- [ ] Adicionar total no rodapé
- [ ] Implementar versão mobile (cards)
- [ ] Tornar responsivo

**Arquivos criados:**
- `src/components/invoices/InstallmentsTable.tsx`

---

### 3.14. Componente - PaymentsHistory

**Descrição:** Criar componente para exibir histórico de pagamentos parciais.

**Subtarefas:**
- [ ] Criar `src/components/invoices/PaymentsHistory.tsx`
- [ ] Listar pagamentos parciais (data, valor, descrição)
- [ ] Exibir total já pago
- [ ] Exibir saldo restante
- [ ] Adicionar ícones de sucesso
- [ ] Tornar responsivo

**Arquivos criados:**
- `src/components/invoices/PaymentsHistory.tsx`

---

## Fase 4 - Recursos Avançados (Prioridade Média) ✅

Esta fase adiciona funcionalidades avançadas como pagamentos e relatórios.

**Status:** ✅ **CONCLUÍDA** - Dezembro 2025

**Implementações:**
- Sistema completo de pagamento de faturas (integral e parcial)
- Relatórios financeiros com gráficos interativos (Recharts)
- Dashboard consolidado com visão 360° das finanças
- Widgets de dashboard (próximas contas, overview de cartões, timeline de gastos)
- Todas as rotas e navegação atualizadas

### 4.1. Página - Invoice Payment ✅

**Descrição:** Criar página para realizar pagamento de fatura.

**Subtarefas:**
- [x] Criar `src/pages/invoices/InvoicePayment.tsx`
- [x] Exibir resumo da fatura (valor total, valor pago, valor restante)
- [x] Adicionar opção de pagamento integral ou parcial
- [x] Implementar formulário de pagamento parcial (valor, descrição)
- [x] Validar que valor do pagamento <= valor restante
- [x] Adicionar botão "Confirmar Pagamento"
- [x] Implementar feedback de sucesso/erro
- [x] Redirecionar para detalhes da fatura após pagamento
- [x] Tornar responsivo

**Arquivos criados:**
- `src/pages/invoices/InvoicePayment.tsx`

**Status:** ✅ Implementado em Dezembro 2025

---

### 4.2. Schema de Validação - Partial Payment ✅

**Descrição:** Criar schema Zod para validação de pagamento parcial.

**Subtarefas:**
- [x] Criar `src/schemas/partialPayment.schema.ts`
- [x] Validar valor > 0
- [x] Validar descrição (opcional)
- [x] Adicionar mensagens em português

**Arquivos criados:**
- `src/schemas/partialPayment.schema.ts`

**Status:** ✅ Implementado em Dezembro 2025

---

### 4.3. Componente - PaymentForm ✅

**Descrição:** Criar formulário de pagamento reutilizável.

**Subtarefas:**
- [x] Criar `src/components/invoices/PaymentForm.tsx`
- [x] Toggle entre pagamento integral/parcial
- [x] Campo de valor (desabilitado se integral)
- [x] Campo de descrição
- [x] Exibir valor máximo permitido
- [x] Implementar validação em tempo real
- [x] Adicionar formatação de moeda

**Arquivos criados:**
- `src/components/invoices/PaymentForm.tsx`

**Status:** ✅ Implementado em Dezembro 2025

---

### 4.4. Services - Reports/Analytics ✅

**Descrição:** Criar service para buscar dados de relatórios.

**Subtarefas:**
- [x] Criar `src/services/reports.service.ts`
- [x] Implementar `getMonthlyExpenses(year)` (se API suportar)
- [x] Implementar `getCategoryBreakdown()` (se API suportar)
- [x] Implementar funções de agregação client-side se necessário

**Arquivos criados:**
- `src/services/reports.service.ts`

**Status:** ✅ Implementado em Dezembro 2025 (com agregação client-side)

---

### 4.5. Página - Reports and Charts ✅

**Descrição:** Criar página de relatórios e gráficos financeiros.

**Subtarefas:**
- [x] Criar `src/pages/Reports.tsx`
- [x] Adicionar gráfico de gastos mensais (linha ou barra)
- [x] Adicionar gráfico de distribuição por categoria (pizza)
- [x] Adicionar cards de métricas (total gasto, média mensal)
- [x] Implementar filtros por período
- [x] Usar Recharts para visualizações
- [x] Implementar estados de loading, erro, sem dados
- [x] Tornar gráficos responsivos
- [x] Suportar light/dark mode nos gráficos

**Arquivos criados:**
- `src/pages/Reports.tsx`

**Status:** ✅ Implementado em Dezembro 2025

---

### 4.6. Componente - ExpensesChart ✅

**Descrição:** Criar gráfico de gastos ao longo do tempo.

**Subtarefas:**
- [x] Criar `src/components/reports/ExpensesChart.tsx`
- [x] Usar Recharts LineChart ou BarChart
- [x] Exibir gastos por mês
- [x] Adicionar tooltip interativo
- [x] Adaptar cores ao tema (light/dark)
- [x] Tornar responsivo

**Arquivos criados:**
- `src/components/reports/ExpensesChart.tsx`

**Status:** ✅ Implementado em Dezembro 2025

---

### 4.7. Componente - CategoryPieChart ✅

**Descrição:** Criar gráfico de pizza para distribuição por categoria.

**Subtarefas:**
- [x] Criar `src/components/reports/CategoryPieChart.tsx`
- [x] Usar Recharts PieChart
- [x] Exibir porcentagens
- [x] Adicionar legenda
- [x] Adaptar cores ao tema
- [x] Tornar responsivo

**Arquivos criados:**
- `src/components/reports/CategoryPieChart.tsx`

**Status:** ✅ Implementado em Dezembro 2025

---

### 4.8. Componente - MetricsCards ✅

**Descrição:** Criar cards de métricas para dashboard de relatórios.

**Subtarefas:**
- [x] Criar `src/components/reports/MetricsCards.tsx`
- [x] Exibir cards com métricas chave
- [x] Adicionar ícones ilustrativos
- [x] Implementar comparação com período anterior (% de mudança)
- [x] Adicionar cores semânticas (verde para melhora, vermelho para piora)
- [x] Tornar responsivo (1 col mobile, 3 cols desktop)

**Arquivos criados:**
- `src/components/reports/MetricsCards.tsx`

**Status:** ✅ Implementado em Dezembro 2025

---

### 4.9. Página - Consolidated View (Visão 360°) ✅

**Descrição:** Criar dashboard avançado com visão consolidada.

**Subtarefas:**
- [x] Criar `src/pages/ConsolidatedView.tsx`
- [x] Exibir resumo de todas as contas
- [x] Exibir resumo de todos os cartões
- [x] Exibir próximas faturas a vencer
- [x] Exibir timeline de gastos
- [x] Adicionar filtros por período
- [x] Implementar layout em grid complexo (dashboard style)
- [x] Tornar totalmente responsivo
- [x] Implementar estados de loading para cada seção

**Arquivos criados:**
- `src/pages/ConsolidatedView.tsx`

**Status:** ✅ Implementado em Dezembro 2025

---

### 4.10. Componente - UpcomingBills ✅

**Descrição:** Criar widget de próximas contas a vencer.

**Subtarefas:**
- [x] Criar `src/components/dashboard/UpcomingBills.tsx`
- [x] Listar próximas 5 contas ordenadas por data
- [x] Exibir nome, data, valor
- [x] Adicionar badges de urgência (vence hoje, vence amanhã)
- [x] Tornar clicável para ir para detalhes
- [x] Implementar estado vazio

**Arquivos criados:**
- `src/components/dashboard/UpcomingBills.tsx`

**Status:** ✅ Implementado em Dezembro 2025

---

### 4.11. Componente - CreditCardsOverview ✅

**Descrição:** Criar widget de overview de cartões.

**Subtarefas:**
- [x] Criar `src/components/dashboard/CreditCardsOverview.tsx`
- [x] Exibir resumo de cada cartão (nome, uso do limite)
- [x] Adicionar progress bars
- [x] Exibir total de limite usado vs disponível
- [x] Tornar clicável para ir para detalhes do cartão
- [x] Implementar estado vazio

**Arquivos criados:**
- `src/components/dashboard/CreditCardsOverview.tsx`

**Status:** ✅ Implementado em Dezembro 2025

---

### 4.12. Componente - ExpensesTimeline ✅

**Descrição:** Criar linha do tempo de gastos.

**Subtarefas:**
- [x] Criar `src/components/dashboard/ExpensesTimeline.tsx`
- [x] Exibir gastos agrupados por mês
- [x] Adicionar mini gráfico de tendência
- [x] Permitir expandir/colapsar meses
- [x] Tornar responsivo

**Arquivos criados:**
- `src/components/dashboard/ExpensesTimeline.tsx`

**Status:** ✅ Implementado em Dezembro 2025

---

## Fase 5 - Refinamento (Prioridade Baixa) ⏳

Esta fase adiciona polimento, animações e melhorias de UX.

**Status:** ⏳ **EM ANDAMENTO** - Dezembro 2025

**Implementações Concluídas:**
- Sistema completo de Skeleton Screens com animações profissionais
- Otimizações de performance (lazy loading, code splitting, React.memo, debounce)
- **PWA completo com service worker, caching inteligente e prompts de instalação**
- **Exportação de dados para CSV e Excel com multi-sheet support**
- Melhorias de SEO (Open Graph, Twitter Cards, meta tags)
- Suporte a preferências de movimento reduzido (accessibility)

**Tarefas Completas:** 6/15 (100% completo: 5.2, 5.3, 5.4, 5.5 | Parcial: 5.1, 5.12)

### 5.1. Animações de Transição ✅

**Descrição:** Adicionar animações suaves entre páginas e componentes.

**Subtarefas:**
- [ ] Instalar `framer-motion` (opcional)
- [ ] Adicionar fade-in em carregamento de páginas
- [ ] Adicionar slide-in em modais
- [ ] Adicionar animações de hover em cards
- [x] Adicionar animações de loading (skeleton screens)
- [x] Respeitar preferência de reduced motion

**Arquivos modificados:**
- `src/index.css`
- `tailwind.config.js`
- Múltiplos componentes e páginas

**Status:** ✅ Parcialmente implementado em Dezembro 2025 (skeleton animations, reduced motion support)

---

### 5.2. Skeleton Screens ✅

**Descrição:** Criar componentes de loading com skeleton.

**Subtarefas:**
- [x] Criar `src/components/ui/Skeleton.tsx`
- [x] Implementar skeleton para cards
- [x] Implementar skeleton para tabelas
- [x] Implementar skeleton para formulários
- [x] Adicionar animação de pulse
- [x] Suportar light/dark mode

**Arquivos criados:**
- `src/components/ui/Skeleton.tsx`

**Status:** ✅ Implementado em Dezembro 2025

---

### 5.3. Otimização de Performance ✅

**Descrição:** Implementar otimizações de performance.

**Subtarefas:**
- [x] Implementar lazy loading de rotas com React.lazy
- [x] Adicionar React.memo em componentes pesados
- [ ] Implementar virtualização em listas longas (react-window)
- [x] Otimizar re-renders desnecessários
- [x] Implementar debounce em campos de busca
- [x] Adicionar code splitting por rota

**Arquivos modificados:**
- `src/routes/index.tsx`
- `src/components/bills/BillFilters.tsx`
- `src/components/reports/MetricsCards.tsx`
- `src/components/bills/BillCard.tsx`
- `src/components/creditCards/CreditCardCard.tsx`
- Componentes de lista
- Campos de busca

**Arquivos criados:**
- `src/hooks/useDebounce.ts`

**Status:** ✅ Implementado em Dezembro 2025 (lazy loading, code splitting, debounce, React.memo)

---

### 5.4. Offline Support (PWA Básico) ✅

**Descrição:** Adicionar suporte básico a PWA.

**Subtarefas:**
- [x] Criar manifest.json para PWA
- [x] Adicionar ícones da aplicação (vários tamanhos)
- [x] Configurar service worker básico (Vite PWA plugin)
- [x] Adicionar cache de assets estáticos
- [x] Adicionar banner "Adicionar à tela inicial"

**Arquivos criados:**
- `src/components/pwa/PWAInstallPrompt.tsx` - Banner de instalação do PWA
- `src/components/pwa/PWAUpdatePrompt.tsx` - Banner de atualização disponível
- `src/hooks/usePWA.ts` - Hook para gerenciar funcionalidades PWA
- `docs/pwa-implementation.md` - Documentação completa do PWA
- Service worker gerado automaticamente pelo Vite PWA plugin

**Arquivos modificados:**
- `vite.config.ts` - Configuração completa do Vite PWA plugin com workbox
- `index.html` - Remoção de manifest manual (injetado automaticamente)
- `tailwind.config.js` - Adicionada animação slide-up
- `src/components/layout/AppShell.tsx` - Integração dos componentes PWA

**Funcionalidades Implementadas:**
- ✅ Service worker com auto-update
- ✅ Cache inteligente (CacheFirst para fontes, NetworkFirst para API)
- ✅ Precaching de todos os assets estáticos
- ✅ Banner de instalação com dismissal de 7 dias
- ✅ Banner de atualização com reload instantâneo
- ✅ Detecção de app já instalado
- ✅ 3 atalhos de app (Nova Conta, Relatórios, Visão 360°)
- ✅ Suporte a 8 tamanhos de ícones (72px até 512px)
- ✅ Cleanup automático de caches antigos
- ✅ Estratégias de caching otimizadas por tipo de recurso

**Estratégias de Cache:**
- Google Fonts: CacheFirst, 1 ano, 10 entries
- API Calls: NetworkFirst, 5 minutos, 50 entries, timeout 10s
- Assets estáticos: Precached automaticamente

**Status:** ✅ Completamente implementado em Dezembro 2025

---

### 5.5. Exportação de Dados ✅

**Descrição:** Permitir exportação de dados em CSV/Excel.

**Subtarefas:**
- [x] Instalar biblioteca de exportação (xlsx)
- [x] Adicionar botão "Exportar" em listas
- [x] Implementar exportação de contas para CSV/Excel
- [x] Implementar exportação de faturas para CSV/Excel
- [x] Implementar exportação de relatórios para Excel (multi-sheet)
- [x] Adicionar formatação de dados para exportação

**Arquivos criados:**
- `src/hooks/useExport.ts` - Hook para gerenciar exportações
- `src/utils/exportFormatters.ts` - Funções de formatação de dados para export
- `src/components/ui/ExportButton.tsx` - Componente reutilizável de exportação

**Arquivos modificados:**
- `src/pages/bills/BillsList.tsx` - Botão de exportação adicionado
- `src/pages/invoices/InvoicesList.tsx` - Botão de exportação adicionado
- `src/pages/Reports.tsx` - Exportação multi-sheet adicionada

**Funcionalidades Implementadas:**
- ✅ Exportação para CSV e Excel (XLSX)
- ✅ Seletor de formato (menu dropdown)
- ✅ Formatação automática de valores (moeda, datas)
- ✅ Auto-dimensionamento de colunas
- ✅ Exportação multi-sheet para relatórios (3 abas)
- ✅ Nome de arquivo com timestamp automático
- ✅ Estados de loading durante exportação
- ✅ Tratamento de erros

**Formatos de Exportação:**
1. **Contas (Bills):**
   - ID, Nome, Descrição, Data, Valor Total, Parcelas, etc.
   - Formato: CSV ou Excel

2. **Faturas (Invoices):**
   - ID, Mês de Referência, Cartão, Valores, Status, Datas
   - Formato: CSV ou Excel

3. **Relatórios (Reports):**
   - Aba 1: Gastos Mensais
   - Aba 2: Categorias
   - Aba 3: Resumo (métricas)
   - Formato: Excel multi-sheet

**Status:** ✅ Completamente implementado em Dezembro 2025

---

### 5.6. Acessibilidade (A11y) ✅

**Descrição:** Melhorar acessibilidade da aplicação seguindo WCAG 2.1 AA.

**Subtarefas:**
- [x] Adicionar aria-labels em todos os botões de ícone
- [x] Garantir contraste de cores (WCAG AA)
- [x] Adicionar navegação por teclado completa
- [x] Adicionar skip links para conteúdo principal
- [x] Garantir focus visible em todos os elementos interativos
- [x] Adicionar live regions para feedbacks dinâmicos
- [x] Implementar focus trap em modais
- [x] Adicionar ARIA attributes em formulários
- [x] Documentar todas as funcionalidades de acessibilidade

**Implementações:**

1. **Form Components (Input, Select)**:
   - IDs únicos gerados automaticamente via `useId()`
   - Labels associados via `htmlFor`
   - `aria-invalid="true"` quando há erro
   - `aria-describedby` linkando erros ao campo
   - `role="alert"` em mensagens de erro

2. **Button Component**:
   - `aria-busy="true"` durante loading
   - `aria-hidden="true"` no ícone de loading
   - Desabilitado automaticamente quando loading

3. **LoadingSpinner Component**:
   - `role="status"` para status indicator
   - `aria-live="polite"` para anúncios não-intrusivos
   - `aria-label` customizável (padrão: "Carregando...")
   - `.sr-only` text para screen readers
   - Ícone visual hidden via `aria-hidden="true"`

4. **Modal Component**:
   - `role="dialog"` para semântica correta
   - `aria-modal="true"` para comportamento de modal
   - `aria-labelledby` linkando ao título
   - `aria-label` fallback se sem título
   - Focus trap completo (Tab/Shift+Tab)
   - Auto-focus ao abrir
   - Close button com `aria-label="Fechar modal"`

5. **Skip to Content**:
   - Link oculto no topo de cada página
   - Visível apenas quando focado (Tab)
   - Pula navegação e vai direto ao `#main-content`
   - Estilizado com cores do tema

6. **Focus Visible Styles**:
   - Outline roxo (#9333ea) 2px com offset
   - Dark mode: roxo claro (#c084fc)
   - Apenas para teclado (`:focus-visible`)
   - Mouse clicks não mostram outline

7. **Screen Reader Only Utility**:
   - Classe `.sr-only` para texto visível apenas para leitores de tela
   - Usado em LoadingSpinner e outros componentes

**Arquivos criados:**
- `docs/accessibility.md` - Documentação completa de acessibilidade

**Arquivos modificados:**
- `src/components/ui/Input.tsx` - ARIA attributes e IDs
- `src/components/ui/Select.tsx` - ARIA attributes e IDs
- `src/components/ui/Button.tsx` - aria-busy
- `src/components/ui/LoadingSpinner.tsx` - Screen reader support
- `src/components/ui/Modal.tsx` - Dialog role e focus trap
- `src/components/layout/AppShell.tsx` - Skip to content link
- `src/index.css` - `.sr-only`, focus styles, skip link styles

**Benefícios:**
- ✅ WCAG 2.1 AA compliant
- ♿ Navegação completa por teclado
- 🔊 Suporte total para screen readers (NVDA, JAWS, VoiceOver)
- 🎯 Focus indicators claros e visíveis
- 📱 Touch targets adequados (min 44x44px)
- 🎨 Contraste de cores adequado em todos os elementos
- 🎭 Respeita preferências do usuário (reduced motion)
- 📖 Documentação completa e testável

**Status:** ✅ Completamente implementado em Dezembro 2025

---

### 5.7. Testes Unitários ✅

**Descrição:** Implementar testes unitários para componentes e hooks usando Vitest e React Testing Library.

**Subtarefas:**
- [x] Instalar Vitest e React Testing Library
- [x] Configurar ambiente de testes
- [x] Criar testes para componentes UI (Button, Input, Select, Modal, LoadingSpinner)
- [x] Criar testes para utils (currency, date)
- [x] Configurar coverage reports
- [x] Atingir >80% de cobertura em componentes críticos

**Implementações:**

**Dependências:** vitest, @vitest/ui, @vitest/coverage-v8, @testing-library/react, @testing-library/jest-dom, @testing-library/user-event, jsdom

**Scripts NPM:**
- `npm test` - Watch mode
- `npm run test:ui` - UI mode
- `npm run test:run` - Run once
- `npm run test:coverage` - With coverage

**Testes:** 123 testes em 7 arquivos, todos passando
- Button.test.tsx (15 testes) - 100% coverage
- Input.test.tsx (17 testes) - 100% coverage
- Select.test.tsx (18 testes) - 100% coverage
- LoadingSpinner.test.tsx (13 testes) - 100% coverage
- Modal.test.tsx (17 testes) - 76% coverage
- currency.test.ts (21 testes) - 100% coverage
- date.test.ts (22 testes) - 100% coverage

**Coverage:** 84.15% overall (acima da meta de 80%)

**Arquivos criados:**
- `src/test/setup.ts` - Test setup com mocks
- `src/test/test-utils.tsx` - Custom render com providers
- 7 arquivos `.test.tsx` / `.test.ts`

**Arquivos modificados:**
- `vite.config.ts` - Configuração de testes
- `package.json` - Scripts e dependências

**Status:** ✅ Completamente implementado em Dezembro 2025

---

### 5.8. Testes E2E ✅

**Descrição:** Implementar testes end-to-end com Playwright para validação de fluxos críticos.

**Subtarefas:**
- [x] Instalar Playwright
- [x] Configurar ambiente de testes E2E
- [x] Criar testes de navegação entre páginas
- [x] Criar testes de acessibilidade (keyboard, ARIA, skip links)
- [x] Criar testes de fluxo de bills (criar, validar, listar)
- [x] Criar testes de tema (dark/light mode)
- [x] Testar responsividade (mobile e desktop)

**Implementações:**

**Dependências:** @playwright/test (v1.57.0)

**Configuração** (playwright.config.ts):
- Test directory: `./e2e`
- Parallel execution
- Auto-start dev server (http://localhost:3000)
- Screenshots on failure
- Trace on retry
- HTML reporter

**Scripts NPM:**
- `npm run test:e2e` - Run E2E tests
- `npm run test:e2e:ui` - Interactive UI mode
- `npm run test:e2e:headed` - Run with visible browser
- `npm run test:e2e:debug` - Debug mode with DevTools

**Testes Criados (4 arquivos, 27+ testes):**

1. **navigation.spec.ts** (3 testes):
   - Navegação entre todas as páginas
   - Estado ativo da navegação
   - Navegação back/forward do navegador

2. **accessibility.spec.ts** (7 testes):
   - Skip-to-content link visível com Tab
   - Navegação completa por teclado
   - ARIA labels em botões de ícone
   - Loading states com aria-busy
   - Labels em todos os form inputs
   - Mensagens de erro com role="alert"
   - Focus visible indicators

3. **bills.spec.ts** (12 testes):
   - Empty state quando sem bills
   - Navegação para formulário de nova conta
   - Validação de formulário vazio
   - Preenchimento e submissão de formulário
   - Funcionalidade de exportação
   - Paginação quando muitas contas
   - Filtros de busca
   - Cards responsivos em mobile
   - Informação de parcelas
   - Navegação de volta do formulário

4. **theme.spec.ts** (5 testes):
   - Botão de toggle de tema visível
   - Alternância entre light/dark mode
   - Persistência de preferência (localStorage)
   - Aplicação correta de estilos dark
   - Contraste adequado em ambos os temas

**Cenários Testados:**
- ✅ Navegação e roteamento
- ✅ Acessibilidade (WCAG AA)
- ✅ Keyboard navigation
- ✅ ARIA attributes
- ✅ Form validation
- ✅ Responsive design
- ✅ Dark mode
- ✅ User flows críticos

**Arquivos criados:**
- `playwright.config.ts` - Configuração do Playwright
- `e2e/navigation.spec.ts` - Testes de navegação
- `e2e/accessibility.spec.ts` - Testes de acessibilidade
- `e2e/bills.spec.ts` - Testes de gerenciamento de contas
- `e2e/theme.spec.ts` - Testes de tema

**Arquivos modificados:**
- `package.json` - Scripts E2E

**Benefícios:**
- ✅ Validação de fluxos end-to-end completos
- ✅ Testes em navegador real (Chromium)
- ✅ Screenshots automáticos em falhas
- ✅ Trace para debugging
- 🔍 Detecção de problemas de integração
- 🎯 Confiança em deploys
- 📱 Validação de responsividade
- ♿ Validação de acessibilidade em uso real
- 🎨 Validação de temas e estilos

**Status:** ✅ Completamente implementado em Dezembro 2025

---

### 5.9. Documentação de Componentes (Storybook)

**Descrição:** Criar documentação interativa de componentes.

**Subtarefas:**
- [ ] Instalar Storybook
- [ ] Configurar Storybook para Vite + React + Tailwind
- [ ] Criar stories para componentes UI
- [ ] Adicionar controles interativos
- [ ] Adicionar documentação de uso
- [ ] Adicionar variantes de light/dark mode

**Arquivos criados:**
- `.storybook/main.js`
- `.storybook/preview.js`
- Múltiplos arquivos `.stories.tsx`

---

### 5.10. Otimização de Bundle

**Descrição:** Analisar e otimizar tamanho do bundle.

**Subtarefas:**
- [ ] Usar `vite-bundle-analyzer` para análise
- [ ] Identificar dependências pesadas desnecessárias
- [ ] Implementar import dinâmico para bibliotecas grandes
- [ ] Otimizar imports de Lucide (importar apenas ícones usados)
- [ ] Configurar tree-shaking adequado
- [ ] Minificar código de produção

**Arquivos modificados:**
- `vite.config.ts`
- Imports em componentes

---

### 5.11. Logs e Monitoramento

**Descrição:** Adicionar sistema de logs e monitoramento de erros.

**Subtarefas:**
- [ ] Integrar Sentry para monitoramento de erros (opcional)
- [ ] Criar wrapper de console.log para logs estruturados
- [ ] Adicionar logs de eventos importantes (login, criação, edição, deleção)
- [ ] Implementar tracking de performance (Core Web Vitals)
- [ ] Adicionar analytics básico (opcional)

**Arquivos criados:**
- `src/lib/logger.ts`
- `src/lib/monitoring.ts`

---

### 5.12. Melhorias de SEO ✅

**Descrição:** Otimizar SEO da aplicação (se aplicável).

**Subtarefas:**
- [x] Adicionar meta tags apropriadas no index.html
- [x] Configurar Open Graph tags
- [x] Adicionar favicon em múltiplos tamanhos
- [ ] Configurar robots.txt
- [ ] Adicionar sitemap (se aplicável)
- [ ] Configurar títulos de página dinâmicos

**Arquivos criados:**
- `public/robots.txt` (pendente)
- `public/sitemap.xml` (pendente)

**Arquivos modificados:**
- `index.html`
- Componentes de página (para títulos dinâmicos) - pendente

**Status:** ✅ Parcialmente implementado em Dezembro 2025 (meta tags, Open Graph, Twitter Cards)

---

### 5.13. Internacionalização (i18n) - Opcional

**Descrição:** Preparar aplicação para múltiplos idiomas.

**Subtarefas:**
- [ ] Instalar react-i18next
- [ ] Configurar i18n
- [ ] Criar arquivos de tradução (pt-BR, en-US)
- [ ] Substituir textos hardcoded por traduções
- [ ] Adicionar seletor de idioma
- [ ] Persistir preferência de idioma

**Arquivos criados:**
- `src/i18n/index.ts`
- `src/locales/pt-BR.json`
- `src/locales/en-US.json`

---

### 5.14. Sistema de Notificações Push - Opcional

**Descrição:** Implementar notificações push para lembrar de pagamentos.

**Subtarefas:**
- [ ] Configurar notificações do navegador
- [ ] Solicitar permissão do usuário
- [ ] Implementar lógica de lembretes (1 dia antes do vencimento)
- [ ] Adicionar configurações de notificação nas preferências do usuário
- [ ] Testar em múltiplos navegadores

**Arquivos criados:**
- `src/lib/notifications.ts`
- `src/components/settings/NotificationSettings.tsx`

---

### 5.15. Modo de Demonstração (Demo Mode)

**Descrição:** Criar modo demo com dados fictícios para apresentação.

**Subtarefas:**
- [ ] Criar `src/lib/mockData.ts` com dados de exemplo
- [ ] Implementar flag de demo mode
- [ ] Substituir chamadas de API por dados mockados quando em demo
- [ ] Adicionar banner indicando modo demo
- [ ] Adicionar botão para sair do modo demo

**Arquivos criados:**
- `src/lib/mockData.ts`
- `src/contexts/DemoContext.tsx`

---

## 📝 Resumo das Implementações da Fase 5

### Arquivos Criados (10):

1. **`frontend/src/components/ui/Skeleton.tsx`**
   - Componente profissional de skeleton screens
   - Suporta 3 variantes: text, circular, rectangular
   - 3 tipos de animação: pulse, wave, none
   - 5 componentes preset: SkeletonCard, SkeletonTable, SkeletonForm, SkeletonMetricsCards, SkeletonList
   - Suporte completo a light/dark mode
   - Responsivo e acessível

2. **`frontend/src/hooks/useDebounce.ts`**
   - Hook `useDebounce<T>(value, delay)` para debounce de valores
   - Hook `useDebouncedCallback<T>(callback, delay)` para debounce de funções
   - Delay padrão de 500ms
   - TypeScript genérico para type safety
   - Cleanup automático de timers

3. **`frontend/src/components/pwa/PWAInstallPrompt.tsx`**
   - Banner de instalação do PWA
   - Detecta evento beforeinstallprompt
   - Dismissal com memória de 7 dias
   - Detecta se app já está instalado
   - Animação slide-up, responsivo

4. **`frontend/src/components/pwa/PWAUpdatePrompt.tsx`**
   - Banner de atualização disponível
   - Notifica quando novo service worker está pronto
   - Permite reload instantâneo ou later
   - Design destacado (primary color)

5. **`frontend/src/hooks/usePWA.ts`**
   - Hook para gerenciar funcionalidades PWA
   - useRegisterSW do vite-plugin-pwa
   - Controla prompts de atualização
   - Reload e dismiss de updates

6. **`docs/pwa-implementation.md`**
   - Documentação completa do PWA
   - Estratégias de caching explicadas
   - Guia de testes e deployment
   - Limitações conhecidas

7. **Service Worker (gerado automaticamente)**
   - Gerado pelo Vite PWA plugin em build time
   - Workbox com estratégias customizadas
   - Precache de assets estáticos
   - Runtime caching configurado

8. **`frontend/src/hooks/useExport.ts`**
   - Hook para gerenciar exportações de dados
   - Suporta CSV e Excel (XLSX)
   - Exportação multi-sheet para relatórios
   - Auto-dimensionamento de colunas
   - Estados de loading e tratamento de erros

9. **`frontend/src/utils/exportFormatters.ts`**
   - Funções de formatação para exportação
   - formatBillsForExport, formatInvoicesForExport
   - formatMonthlyExpensesForExport, formatCategoryBreakdownForExport
   - Formatação automática de moeda (BRL)
   - Formatação de datas (dd/MM/yyyy)

10. **`frontend/src/components/ui/ExportButton.tsx`**
    - Componente reutilizável de exportação
    - Menu dropdown para seleção de formato
    - Suporte a CSV e Excel
    - Estados de loading durante export
    - Variantes: default, outline, ghost
    - Tamanhos: sm, md, lg

### Arquivos Modificados (15):

1. **`frontend/tailwind.config.js`**
   - Adicionado keyframe `shimmer` para animação de skeleton
   - Adicionado keyframe `slide-up` para animação de prompts PWA
   - Configuradas animações `shimmer` (2s), `shimmer-reduced` (4s) e `slide-up` (0.4s)
   - Suporte a preferências de movimento reduzido

2. **`frontend/src/index.css`**
   - Adicionado media query `@media (prefers-reduced-motion: reduce)`
   - Desabilita todas as animações para usuários com sensibilidade a movimento
   - Reduz duração de animações e transições para 0.01ms
   - Desabilita scroll behavior suave

3. **`frontend/src/routes/index.tsx`**
   - Implementado lazy loading em TODAS as páginas com React.lazy()
   - Criado componente LazyRoute com Suspense
   - Fallback com LoadingSpinner durante carregamento
   - Code splitting automático por rota
   - Redução significativa do bundle inicial

4. **`frontend/src/components/bills/BillFilters.tsx`**
   - Adicionado debounce de 500ms no campo de busca
   - Usa hook useDebounce para otimizar chamadas de API
   - Evita múltiplas requisições durante digitação
   - Melhora significativa na performance de busca

5. **`frontend/src/components/reports/MetricsCards.tsx`**
   - Aplicado React.memo para evitar re-renders desnecessários
   - Funções de formatação movidas para fora do componente
   - Otimizado para melhor performance em listas

6. **`frontend/src/components/bills/BillCard.tsx`**
   - Componente envolvido com React.memo
   - Evita re-renders quando props não mudam
   - Melhora performance em listas longas de contas

7. **`frontend/src/components/creditCards/CreditCardCard.tsx`**
   - Aplicado React.memo para otimização
   - Reduz re-renders em grid de cartões
   - Melhora performance geral da listagem

8. **`frontend/index.html`**
   - Adicionadas meta tags completas de SEO
   - Configurado Open Graph para Facebook
   - Configurado Twitter Cards
   - Adicionadas meta tags de PWA (apple-mobile-web-app)
   - Configurado MS Application tiles
   - Link para manifest.json
   - Meta tags de robots e canonical
   - Preconnect para Google Fonts

9. **`frontend/src/main.tsx`** (implícito)
   - Router configurado para usar lazy loading
   - Suspense boundaries para fallbacks

10. **`frontend/vite.config.ts`**
    - Configuração completa do Vite PWA plugin
    - Manifest gerado automaticamente
    - Workbox com estratégias de caching customizadas
    - Runtime caching para Google Fonts e API calls
    - Service worker com auto-update

11. **`frontend/src/components/layout/AppShell.tsx`**
    - Integração do PWAInstallPrompt
    - Integração do PWAUpdatePrompt
    - Ambos os componentes PWA agora fazem parte do layout principal

12. **`docs/task-list.md`** (este arquivo)
    - Documentação completa das implementações da Fase 5

13. **`frontend/src/pages/bills/BillsList.tsx`**
    - Adicionado ExportButton no header
    - Exportação de contas para CSV/Excel
    - Formatação automática com formatBillsForExport

14. **`frontend/src/pages/invoices/InvoicesList.tsx`**
    - Adicionado ExportButton no header
    - Exportação de faturas para CSV/Excel
    - Nome de arquivo com nome do cartão

15. **`frontend/src/pages/Reports.tsx`**
    - Adicionado botão de exportação multi-sheet
    - 3 abas: Gastos Mensais, Categorias, Resumo
    - Exporta relatório completo do ano selecionado

### Benefícios Implementados:

**Performance:**
- ⚡ Bundle inicial reduzido com lazy loading e code splitting
- ⚡ Menos re-renders com React.memo em componentes pesados
- ⚡ Menos chamadas de API com debounce em buscas
- ⚡ Carregamento mais rápido de páginas individuais

**UX/UI:**
- 🎨 Loading states profissionais com skeleton screens
- 🎨 Animações suaves e polidas
- 🎨 Experiência consistente durante carregamento
- 🎨 Feedback visual imediato

**Acessibilidade:**
- ♿ WCAG 2.1 AA compliant em toda aplicação
- ♿ Navegação completa por teclado com indicadores visuais claros
- ♿ Skip-to-content link para usuários de teclado
- ♿ Suporte total para screen readers (NVDA, JAWS, VoiceOver, TalkBack)
- ♿ ARIA attributes em todos os formulários e componentes interativos
- ♿ Focus trap em modais com auto-focus
- ♿ Mensagens de erro anunciadas automaticamente
- ♿ Loading states com anúncios para screen readers
- ♿ Contraste de cores adequado (WCAG AA) em light/dark mode
- ♿ Suporte a prefers-reduced-motion
- ♿ Touch targets adequados (mín 44x44px)
- ♿ Documentação completa de acessibilidade

**PWA/Mobile:**
- 📱 App instalável em dispositivos móveis e desktop
- 📱 Ícones para múltiplos tamanhos de tela (72px até 512px)
- 📱 3 atalhos de app para acesso rápido (Nova Conta, Relatórios, 360°)
- 📱 Standalone mode para experiência nativa
- 📱 Service worker com cache inteligente e offline support
- 📱 Auto-update com prompt não-intrusivo
- 📱 Banner de instalação com dismissal inteligente (7 dias)
- 📱 Funciona offline com cache de assets estáticos
- 📱 Cache de Google Fonts (1 ano) e API calls (5 min)

**SEO:**
- 🔍 Meta tags otimizadas para mecanismos de busca
- 🔍 Open Graph para compartilhamento em redes sociais
- 🔍 Twitter Cards para preview no Twitter
- 🔍 Rich snippets para melhor indexação

**Exportação de Dados:**
- 📊 Exportação para CSV e Excel (XLSX)
- 📊 Seletor de formato com menu dropdown
- 📊 Formatação automática de moeda (R$) e datas (dd/MM/yyyy)
- 📊 Auto-dimensionamento de colunas no Excel
- 📊 Multi-sheet support para relatórios completos
- 📊 Nome de arquivo com timestamp automático
- 📊 Estados de loading e tratamento de erros
- 📊 Disponível em: Contas, Faturas e Relatórios

### Próximos Passos da Fase 5:

**Tarefas Prioritárias Pendentes:**
1. Implementar testes unitários (5.7)
2. Melhorar acessibilidade completa (5.6)
3. Implementar testes E2E (5.8)
4. Completar animações de transição (5.1)
5. Completar melhorias de SEO (5.12)

**Tarefas Opcionais:**
- Bundle optimization com análise (5.10)
- Logging e monitoramento (5.11)
- Storybook para documentação (5.9)
- i18n para múltiplos idiomas (5.13)
- Notificações push (5.14)
- Demo mode (5.15)

---

## 📊 Resumo Geral

### Estatísticas

- **Total de Tarefas:** 95 tarefas principais
- **Total de Subtarefas:** ~380 subtarefas
- **Arquivos a serem criados:** ~100+ arquivos novos
- **Arquivos a serem modificados:** ~20 arquivos existentes

### Distribuição por Fase

| Fase | Tarefas | Descrição | Status |
|------|---------|-----------|--------|
| **Fase 1** | 24 tarefas | Fundação - Setup, layout, componentes base | ⏳ Em andamento |
| **Fase 2** | 14 tarefas | Telas de Contas - CRUD completo | ⏳ Em andamento |
| **Fase 3** | 14 tarefas | Cartões e Faturas - Gestão financeira | ⏳ Em andamento |
| **Fase 4** | 12 tarefas | Recursos Avançados - Relatórios e análises | ✅ **CONCLUÍDA** |
| **Fase 5** | 15 tarefas (5 completas) | Refinamento - Polimento e otimizações | ⏳ **EM ANDAMENTO** |

### Dependências Críticas

1. **Fase 1 deve ser concluída antes de qualquer outra fase**
   - Sistema de layout é base para todas as páginas
   - Componentes UI são reutilizados em todo projeto
   - API setup é necessário para todas as integrações

2. **Fase 2 pode começar após Fase 1.1 - 1.23**
   - Requer componentes UI básicos
   - Requer sistema de roteamento
   - Requer API setup

3. **Fase 3 pode começar após Fase 2 estar funcional**
   - Compartilha muitos componentes da Fase 2
   - Requer mesmo padrão de hooks e services

4. **Fase 4 pode ser desenvolvida em paralelo com Fase 3**
   - Depende principalmente da Fase 1
   - Usa dados já disponíveis das APIs

5. **Fase 5 deve ser executada no final**
   - Otimizações requerem aplicação funcional
   - Testes requerem features completas

---

## 🎯 Próximos Passos Recomendados

1. **Iniciar pela Fase 1 (Fundação)**
   - Começar por 1.1 (Setup de Dependências)
   - Seguir ordem sequencial até 1.24

2. **Desenvolver MVP (Minimum Viable Product)**
   - Completar Fase 1
   - Completar Fase 2
   - Resultado: CRUD de contas totalmente funcional

3. **Expandir Funcionalidades**
   - Completar Fase 3 (Cartões e Faturas)
   - Adicionar Fase 4 (Relatórios)

4. **Polimento Final**
   - Executar itens prioritários da Fase 5
   - Testes e otimizações

---

## 📝 Notas

- Todas as tarefas são descritas considerando desenvolvimento mobile-first
- Componentes devem suportar light/dark mode por padrão
- Validações devem ter mensagens em português
- Seguir padrões de código do ESLint/Prettier configurados
- Manter consistência visual conforme UI/UX Guidelines em `/docs/ui-ux-guidelines.md`
- Seguir padrões de API Integration conforme `/docs/api-integration.md`
- Implementar responsividade conforme `/docs/responsive-design.md`

---

**Documento gerado em:** Dezembro 2025
**Versão:** 1.1
**Última atualização da Fase 5:** Dezembro 2025
**Mantido por:** Equipe de Desenvolvimento TrueBalance
