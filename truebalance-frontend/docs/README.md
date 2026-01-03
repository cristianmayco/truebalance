npm # TrueBalance - Documentação de Telas Frontend

Documentação completa das telas da SPA (Single Page Application) do TrueBalance, um sistema de gerenciamento financeiro pessoal.

## 📋 Índice

### 🖥️ Telas Principais

#### Dashboard e Visões Gerais
- [01. Dashboard/Home](screens/01-dashboard.md) - Página inicial com visão consolidada
- [11. Visão Consolidada Avançada](screens/11-consolidated-view.md) - Dashboard completo 360°

#### Gestão de Contas
- [02. Listagem de Contas](screens/02-bills-list.md) - Lista, busca e paginação
- [03. Cadastro/Edição de Conta](screens/03-bill-form.md) - Formulário completo
- [04. Cadastro Rápido de Conta](screens/04-bill-quick-add.md) - Modal simplificado

#### Cartões de Crédito
- [05. Listagem de Cartões](screens/05-credit-cards-list.md) - Cards visuais
- [06. Cadastro/Edição de Cartão](screens/06-credit-card-form.md) - Formulário

#### Faturas
- [07. Listagem de Faturas](screens/07-invoices-list.md) - Faturas por cartão
- [08. Detalhes da Fatura](screens/08-invoice-details.md) - Parcelas e pagamentos
- [09. Pagamento de Fatura](screens/09-invoice-payment.md) - Pagamento parcial/integral

#### Relatórios
- [10. Relatórios e Gráficos](screens/10-reports-charts.md) - Análises financeiras

### 🧩 Componentes

- [Layout](components/layout.md) - AppShell, Sidebar, TopBar, ThemeToggle
- [Navigation](components/navigation.md) - Sistema de navegação
- [Forms](components/forms.md) - Inputs, validações, formulários
- [Data Display](components/data-display.md) - Tabelas, cards, badges
- [Feedback](components/feedback.md) - Toasts, modals, loading states

### 📐 Diretrizes

- [UI/UX Guidelines](ui-ux-guidelines.md) - Design system, cores, tipografia
- [Responsive Design](responsive-design.md) - Mobile-first, breakpoints
- [API Integration](api-integration.md) - Padrões de integração com backend

## 🎨 Design System

### Temas
- **Light Mode**: Visual limpo e profissional
- **Dark Mode**: Visual jovem e moderno (tema padrão sugerido)
- Cor primária: **Roxo/Purple** (#8B5CF6)

### Tecnologias
- React 18 + TypeScript
- Tailwind CSS 3 com dark mode
- Lucide React (ícones)
- Recharts ou Chart.js (gráficos)

## 📱 Responsividade

Todas as telas são projetadas com abordagem mobile-first:
- **Mobile**: < 768px (bottom navigation, cards verticais)
- **Tablet**: 768px - 1023px (sidebar colapsável)
- **Desktop**: >= 1024px (sidebar fixa, tabelas)

## 🔗 Backend API

Sistema backend em Spring Boot com as seguintes entidades:
- **Bill (Conta)** ✅ Implementado
- **CreditCard** 🔨 Planejado
- **Invoice** 🔨 Planejado
- **Installment** 🔨 Planejado
- **PartialPayment** 🔨 Planejado

Base URL: `http://localhost:8080`

Documentação detalhada da API em: `truebalance/docs/api-doc.md`

## 🚀 Roadmap de Implementação

### Fase 1 - Fundação (Prioridade Alta)
1. Componentes de layout e navegação
2. Sistema de temas (light/dark)
3. Dashboard básico
4. Listagem de contas
5. Cadastro de conta

### Fase 2 - CRUD Completo (Prioridade Alta)
6. Edição de conta
7. Deleção de conta
8. Cadastro rápido
9. Busca e paginação

### Fase 3 - Cartões e Faturas (Prioridade Média)
10. Listagem de cartões
11. Cadastro de cartão
12. Listagem de faturas
13. Detalhes da fatura

### Fase 4 - Recursos Avançados (Prioridade Média)
14. Pagamentos de fatura
15. Relatórios e gráficos
16. Visão consolidada avançada

### Fase 5 - Refinamento (Prioridade Baixa)
17. Animações e transições
18. Exportação de dados
19. PWA features
20. Testes E2E

## 📖 Como Usar Esta Documentação

### Para Designers
1. Revise [UI/UX Guidelines](ui-ux-guidelines.md) para entender o design system
2. Veja as telas individuais para entender layout e fluxos
3. Use as descrições de wireframes para criar mockups visuais

### Para Desenvolvedores
1. Comece pelos [Componentes](components/) para entender estruturas reutilizáveis
2. Consulte [API Integration](api-integration.md) para padrões de chamadas
3. Siga a ordem do roadmap para implementação
4. Use [Responsive Design](responsive-design.md) para garantir mobile-first

### Para Product Owners
1. Revise as telas para validar funcionalidades
2. Priorize features baseado no roadmap
3. Use os wireframes textuais para discussões de UX

## 🎯 Convenções

**Ícones e Emojis:**
- ✅ Implementado no backend
- 🔨 Planejado no backend
- 📱 Funcionalidade mobile
- 🖥️ Funcionalidade desktop
- ⚠️ Atenção/Warning
- 💡 Dica/Sugestão

**Estados de Tela:**
- **Loading**: Dados sendo carregados
- **Empty**: Sem dados para exibir
- **Error**: Erro na requisição ou validação
- **Success**: Dados carregados com sucesso

**Prioridades:**
- **Alta**: Essencial para MVP
- **Média**: Importante mas não bloqueante
- **Baixa**: Nice to have

## 📚 Referências

- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [React Documentation](https://react.dev)
- [Lucide Icons](https://lucide.dev)
- [Recharts](https://recharts.org)

## 🤝 Contribuindo

Ao adicionar ou modificar documentação:
1. Siga o template padrão de cada tipo de documento
2. Mantenha wireframes textuais claros e detalhados
3. Especifique todos os estados possíveis da tela
4. Documente integração com API
5. Inclua requisitos de responsividade

## 📞 Contato

Para dúvidas sobre a documentação, consulte a equipe de desenvolvimento.

---

**Última atualização**: Dezembro 2025
**Versão**: 1.0
