# Guia de SEO - TrueBalance Frontend

Este documento descreve as práticas e ferramentas de SEO implementadas no TrueBalance.

## 📋 Visão Geral

O TrueBalance implementa boas práticas de SEO para melhorar a visibilidade nos mecanismos de busca e a experiência de compartilhamento em redes sociais.

## 🔧 Ferramentas Implementadas

### 1. robots.txt

Localizado em `/public/robots.txt`, controla o comportamento dos crawlers de busca.

**Configuração atual:**
```txt
User-agent: *
Allow: /
Disallow: /api/
Disallow: /*.json$
Sitemap: https://truebalance.app/sitemap.xml
```

**Explicação:**
- ✅ Permite todos os bots em páginas públicas
- ❌ Bloqueia APIs e arquivos JSON
- 🗺️ Aponta para o sitemap

**Quando ajustar:**
- Ambiente de staging: adicionar `Disallow: /`
- Beta privado: bloquear crawlers
- Produção: liberar conforme necessário

---

### 2. sitemap.xml

Localizado em `/public/sitemap.xml`, lista todas as páginas importantes para indexação.

**Páginas incluídas:**
- `/` - Home/Dashboard (prioridade 1.0)
- `/bills` - Lista de contas (prioridade 0.9)
- `/bills/new` - Nova conta (prioridade 0.7)
- `/credit-cards` - Cartões (prioridade 0.9)
- `/credit-cards/new` - Novo cartão (prioridade 0.7)
- `/reports` - Relatórios (prioridade 0.8)
- `/consolidated` - Visão 360° (prioridade 0.8)

**Manutenção:**
- Atualizar `<lastmod>` quando fizer mudanças significativas
- Adicionar novas páginas públicas
- Ajustar prioridades conforme necessário
- Manter `<changefreq>` realista

**Geração automática (futuro):**
```bash
# Considerar ferramenta para gerar sitemap dinamicamente
npm run generate:sitemap
```

---

### 3. usePageTitle Hook

Hook para definir títulos dinâmicos de página.

**Uso básico:**
```tsx
import { usePageTitle } from '@/hooks/usePageTitle';

function BillsPage() {
  usePageTitle('Minhas Contas');
  // Resultado: "Minhas Contas | TrueBalance"

  return <div>...</div>;
}
```

**Uso avançado:**
```tsx
usePageTitle('Relatórios', {
  suffix: 'TrueBalance - Finanças Pessoais',
  restoreOnUnmount: false
});
// Resultado: "Relatórios | TrueBalance - Finanças Pessoais"
```

**Parâmetros:**
- `title` (string): Título da página
- `options.suffix` (string): Sufixo customizado (padrão: "TrueBalance")
- `options.restoreOnUnmount` (boolean): Restaurar título anterior (padrão: true)

**Quando usar:**
- Em todas as páginas principais
- Em modais importantes (opcional)
- Quando nome da conta/cartão for dinâmico

---

### 4. Componente SEO

Componente para gerenciar todas as meta tags de uma página.

**Uso completo:**
```tsx
import { SEO } from '@/components/SEO';

function ReportsPage() {
  return (
    <>
      <SEO
        title="Relatórios Financeiros"
        description="Visualize gráficos detalhados dos seus gastos mensais e anuais"
        keywords="relatórios, gráficos, gastos, finanças"
        ogImage="https://truebalance.app/og-reports.png"
        ogType="article"
        canonical="https://truebalance.app/reports"
      />

      <div>
        {/* Conteúdo da página */}
      </div>
    </>
  );
}
```

**Props disponíveis:**

| Prop | Tipo | Padrão | Descrição |
|------|------|--------|-----------|
| `title` | string | - | Título da página (obrigatório) |
| `description` | string | Descrição padrão | Meta description |
| `keywords` | string | Keywords padrão | Palavras-chave |
| `ogTitle` | string | `title` | Título para Open Graph |
| `ogDescription` | string | `description` | Descrição para Open Graph |
| `ogImage` | string | URL padrão | Imagem para compartilhamento |
| `ogType` | 'website' \| 'article' | 'website' | Tipo de conteúdo |
| `twitterCard` | 'summary' \| 'summary_large_image' | 'summary_large_image' | Tipo de Twitter Card |
| `canonical` | string | - | URL canônica |
| `noindex` | boolean | false | Bloquear indexação |
| `nofollow` | boolean | false | Bloquear seguir links |

---

## 🎯 Melhores Práticas

### Títulos de Página

**✅ BOM:**
```tsx
usePageTitle('Fatura de Janeiro - Nubank');
// "Fatura de Janeiro - Nubank | TrueBalance"
```

**❌ RUIM:**
```tsx
usePageTitle('TrueBalance - Página de Fatura');
// Redundante, SEO pobre
```

**Diretrizes:**
- Máximo 60 caracteres (incluindo sufixo)
- Palavras-chave no início
- Descritivo e único
- Sem repetir "TrueBalance" no título

---

### Meta Descriptions

**✅ BOM:**
```tsx
<SEO
  description="Gerencie suas contas parceladas, acompanhe pagamentos e receba lembretes de vencimento."
/>
```

**❌ RUIM:**
```tsx
<SEO
  description="Página de contas"
/>
// Muito curta, sem valor
```

**Diretrizes:**
- 150-160 caracteres
- Inclua call-to-action quando apropriado
- Descreva o valor da página
- Unique por página

---

### Keywords

**✅ BOM:**
```tsx
<SEO
  keywords="contas a pagar, parcelas, gestão financeira, controle de gastos"
/>
```

**❌ RUIM:**
```tsx
<SEO
  keywords="finanças, dinheiro, app, aplicativo, software, sistema"
/>
// Muito genérico
```

**Diretrizes:**
- 5-10 keywords relevantes
- Específicas para a página
- Ordem por relevância
- Evite keyword stuffing

---

### Open Graph Images

**Tamanhos recomendados:**
- **Facebook:** 1200x630px
- **Twitter:** 1200x675px (16:9)
- **LinkedIn:** 1200x627px

**Dicas:**
- Use imagens de alta qualidade
- Inclua branding (logo)
- Texto legível mesmo em thumbnail
- Formato: PNG ou JPG
- Tamanho máximo: 8MB (idealmente < 1MB)

**Exemplo:**
```tsx
<SEO
  ogImage="https://truebalance.app/og-images/reports.png"
  ogType="article"
/>
```

---

## 🌐 URLs Canônicas

URLs canônicas previnem conteúdo duplicado.

**Quando usar:**
```tsx
// Página acessível por múltiplas URLs
<SEO
  canonical="https://truebalance.app/bills"
/>
```

**Exemplos:**
- `/bills` e `/bills?page=1` → canonical: `/bills`
- `/reports?year=2025` → canonical: `/reports`
- Parâmetros de tracking/UTM → sempre use canonical

---

## 🤖 Controle de Indexação

### Páginas Públicas (indexar)

```tsx
<SEO
  title="Dashboard"
  // noindex e nofollow são false por padrão
/>
```

### Páginas Privadas (não indexar)

```tsx
<SEO
  title="Editar Fatura"
  noindex
  nofollow
/>
```

**Quando usar noindex:**
- Formulários de edição
- Páginas de confirmação
- Páginas internas/admin
- Páginas de erro customizadas
- Páginas de teste/staging

---

## 📊 Validação e Testes

### Ferramentas de Teste:

1. **Google Rich Results Test**
   - URL: https://search.google.com/test/rich-results
   - Valida markup e dados estruturados

2. **Facebook Sharing Debugger**
   - URL: https://developers.facebook.com/tools/debug/
   - Testa Open Graph tags

3. **Twitter Card Validator**
   - URL: https://cards-dev.twitter.com/validator
   - Valida Twitter Cards

4. **LinkedIn Post Inspector**
   - URL: https://www.linkedin.com/post-inspector/
   - Testa compartilhamento no LinkedIn

### Checklist de Validação:

- [ ] Title tag presente e único (< 60 caracteres)
- [ ] Meta description presente (150-160 caracteres)
- [ ] Meta keywords relevantes (opcional, baixa prioridade)
- [ ] Open Graph title, description, image
- [ ] Twitter Card tags
- [ ] URL canônica quando apropriado
- [ ] robots.txt acessível
- [ ] sitemap.xml acessível e válido
- [ ] Imagens OG têm dimensões corretas
- [ ] Páginas sensíveis têm noindex

---

## 🚀 Próximos Passos (Futuro)

### Structured Data (JSON-LD)

Adicionar dados estruturados para rich snippets:

```tsx
<script type="application/ld+json">
{
  "@context": "https://schema.org",
  "@type": "SoftwareApplication",
  "name": "TrueBalance",
  "applicationCategory": "FinanceApplication",
  "operatingSystem": "Web",
  "description": "Sistema de gerenciamento financeiro pessoal"
}
</script>
```

### Geração Dinâmica de Sitemap

Gerar sitemap.xml dinamicamente baseado nas rotas:

```ts
// scripts/generate-sitemap.ts
import { routes } from '../src/routes';

function generateSitemap() {
  const urls = routes.map(route => ({
    loc: `https://truebalance.app${route.path}`,
    lastmod: new Date().toISOString().split('T')[0],
    priority: calculatePriority(route),
  }));

  // Gerar XML...
}
```

### Internacionalização (i18n)

Adicionar hreflang tags para múltiplos idiomas:

```html
<link rel="alternate" hreflang="pt-BR" href="https://truebalance.app/pt-BR/bills" />
<link rel="alternate" hreflang="en-US" href="https://truebalance.app/en-US/bills" />
```

### Analytics e Core Web Vitals

Monitorar métricas de performance SEO:

```tsx
// Implementar tracking de Core Web Vitals
import { getCLS, getFID, getFCP, getLCP, getTTFB } from 'web-vitals';

getCLS(console.log);
getFID(console.log);
getLCP(console.log);
// ...
```

---

## 📚 Exemplos por Página

### Dashboard/Home

```tsx
function Dashboard() {
  return (
    <>
      <SEO
        title="Dashboard"
        description="Visão geral das suas finanças pessoais. Acompanhe contas, cartões e gastos em tempo real."
        keywords="dashboard financeiro, visão geral, finanças pessoais"
        ogImage="https://truebalance.app/og-dashboard.png"
      />

      <PageTransition>
        {/* Conteúdo */}
      </PageTransition>
    </>
  );
}
```

### Bills List

```tsx
function BillsList() {
  usePageTitle('Minhas Contas');

  return (
    <>
      <SEO
        title="Minhas Contas"
        description="Gerencie todas as suas contas e parcelas em um só lugar. Controle pagamentos e vencimentos."
        keywords="contas a pagar, parcelas, gestão de contas, controle de pagamentos"
        canonical="https://truebalance.app/bills"
      />

      {/* Conteúdo */}
    </>
  );
}
```

### Bill Details (Dinâmico)

```tsx
function BillDetails() {
  const { data: bill } = useBill(id);

  usePageTitle(bill?.name || 'Detalhes da Conta');

  return (
    <>
      <SEO
        title={bill?.name}
        description={`Detalhes da conta ${bill?.name}. Valor total: R$ ${bill?.totalAmount}. Parcelas: ${bill?.installments}.`}
        noindex // Página específica, não indexar
      />

      {/* Conteúdo */}
    </>
  );
}
```

### Reports

```tsx
function Reports() {
  return (
    <>
      <SEO
        title="Relatórios Financeiros"
        description="Visualize gráficos e relatórios detalhados dos seus gastos. Análise mensal, anual e por categoria."
        keywords="relatórios financeiros, gráficos de gastos, análise financeira"
        ogImage="https://truebalance.app/og-reports.png"
        ogType="article"
      />

      {/* Conteúdo */}
    </>
  );
}
```

---

## 🔍 Debugging

### Verificar meta tags no navegador:

```js
// Console do navegador
console.log(document.title);
console.log(document.querySelector('meta[name="description"]')?.content);
console.log(document.querySelector('meta[property="og:image"]')?.content);
```

### Inspecionar no DevTools:

1. Abrir DevTools (F12)
2. Elements tab
3. Procurar por `<head>`
4. Verificar meta tags

---

**Documento criado em:** Dezembro 2025
**Versão:** 1.0
**Mantido por:** Equipe de Desenvolvimento TrueBalance
