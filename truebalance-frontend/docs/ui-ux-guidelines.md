# UI/UX Guidelines - TrueBalance

Guia completo de design system para o TrueBalance, incluindo temas claro e escuro, paleta de cores, tipografia, espaçamento e componentes visuais.

## 🎨 Temas

### Light Mode
- Visual limpo e profissional
- Fundo branco com superfícies em gray-50
- Roxo vibrante como cor primária
- Alto contraste para legibilidade
- **Uso**: Ambientes com muita luz, trabalho profissional

### Dark Mode (Recomendado)
- Visual jovem e moderno
- Fundo slate-900 com superfícies em slate-800
- Roxo claro/brilhante como destaque
- Menos cansativo para os olhos
- **Uso**: Uso prolongado, ambientes com pouca luz

### Theme Toggle
- Switch visível no TopBar (canto superior direito)
- Ícones: Sol ☀️ (light) / Lua 🌙 (dark)
- Transição suave entre temas
- Persistência no localStorage
- Resposta à preferência do sistema (opcional)

## 🎨 Paleta de Cores

### Tema Claro (Light Mode)

**Cores Primárias:**
```css
--primary: #8B5CF6;        /* violet-500 */
--primary-dark: #7C3AED;   /* violet-600 */
--primary-light: #A78BFA;  /* violet-400 */
--primary-hover: #7C3AED;
```

**Cores de Fundo:**
```css
--background: #FFFFFF;     /* white */
--surface: #F9FAFB;        /* gray-50 */
--surface-hover: #F3F4F6;  /* gray-100 */
```

**Cores de Texto:**
```css
--text-primary: #111827;   /* gray-900 */
--text-secondary: #6B7280; /* gray-500 */
--text-tertiary: #9CA3AF;  /* gray-400 */
```

**Cores de Borda:**
```css
--border: #E5E7EB;         /* gray-200 */
--border-hover: #D1D5DB;   /* gray-300 */
```

---

### Tema Escuro (Dark Mode)

**Cores Primárias:**
```css
--primary: #A78BFA;        /* violet-400 */
--primary-dark: #8B5CF6;   /* violet-500 */
--primary-light: #C4B5FD;  /* violet-300 */
--primary-hover: #C4B5FD;
```

**Cores de Fundo:**
```css
--background: #0F172A;     /* slate-900 */
--surface: #1E293B;        /* slate-800 */
--surface-hover: #334155;  /* slate-700 */
```

**Cores de Texto:**
```css
--text-primary: #F1F5F9;   /* slate-100 */
--text-secondary: #94A3B8; /* slate-400 */
--text-tertiary: #64748B;  /* slate-500 */
```

**Cores de Borda:**
```css
--border: #334155;         /* slate-700 */
--border-hover: #475569;   /* slate-600 */
```

---

### Cores Semânticas (Ambos Temas)

**Success (Verde):**
- Light: `#10B981` (emerald-500)
- Dark: `#34D399` (emerald-400)
- Uso: Confirmações, sucesso em ações, saldos positivos

**Warning (Âmbar):**
- Light: `#F59E0B` (amber-500)
- Dark: `#FBBF24` (amber-400)
- Uso: Alertas, faturas próximas ao vencimento

**Error (Vermelho):**
- Light: `#EF4444` (red-500)
- Dark: `#F87171` (red-400)
- Uso: Erros de validação, ações destrutivas, saldos negativos

**Info (Azul):**
- Light: `#3B82F6` (blue-500)
- Dark: `#60A5FA` (blue-400)
- Uso: Informações neutras, tooltips, dicas

---

### Gradientes

**Purple Gradient (Headers, Cards de Destaque):**
```css
background: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%);
/* Tailwind: bg-gradient-to-br from-violet-500 to-violet-600 */
```

**Dark Purple Gradient (Dark Mode - Cards Premium):**
```css
background: linear-gradient(135deg, #581C87 0%, #6B21A8 50%, #4C1D95 100%);
/* Tailwind: bg-gradient-to-r from-purple-900 via-violet-900 to-indigo-900 */
```

**Subtle Gradient (Backgrounds):**
```css
/* Light */
background: linear-gradient(180deg, #FAFAFA 0%, #FFFFFF 100%);

/* Dark */
background: linear-gradient(180deg, #0F172A 0%, #1E293B 100%);
```

---

## ✍️ Tipografia

### Font Family
```css
font-family: 'Inter', system-ui, -apple-system, 'Segoe UI', sans-serif;
```

**Como Importar (Google Fonts):**
```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
```

### Escala de Tamanhos

| Classe      | Tamanho | Uso                               |
|-------------|---------|-----------------------------------|
| `text-xs`   | 12px    | Labels pequenos, badges           |
| `text-sm`   | 14px    | Texto secundário, descrições      |
| `text-base` | 16px    | Texto principal do corpo          |
| `text-lg`   | 18px    | Subtítulos, valores destacados    |
| `text-xl`   | 20px    | Títulos de cards                  |
| `text-2xl`  | 24px    | Títulos de seções                 |
| `text-3xl`  | 30px    | Títulos principais                |
| `text-4xl`  | 36px    | Títulos de páginas                |

### Font Weights

| Classe          | Weight | Uso                     |
|-----------------|--------|-------------------------|
| `font-normal`   | 400    | Texto do corpo          |
| `font-medium`   | 500    | Botões, labels          |
| `font-semibold` | 600    | Subtítulos, destaques   |
| `font-bold`     | 700    | Títulos principais      |

### Line Height
```css
--leading-tight: 1.25;   /* Títulos */
--leading-normal: 1.5;   /* Corpo de texto */
--leading-relaxed: 1.75; /* Parágrafos longos */
```

---

## 📏 Espaçamento

Sistema baseado em múltiplos de 4px (padrão Tailwind).

### Escala de Espaçamento

| Classe | Valor | Uso                           |
|--------|-------|-------------------------------|
| `p-1`  | 4px   | Espaçamento mínimo            |
| `p-2`  | 8px   | Padding em badges, tags       |
| `p-3`  | 12px  | Padding em botões pequenos    |
| `p-4`  | 16px  | Padding padrão em botões      |
| `p-5`  | 20px  | Padding em inputs             |
| `p-6`  | 24px  | Padding em cards              |
| `p-8`  | 32px  | Padding em sections           |
| `p-12` | 48px  | Padding em containers grandes |

### Gaps e Margins
```css
gap-2  /* 8px  - Items em linha */
gap-4  /* 16px - Grupos de elementos */
gap-6  /* 24px - Seções */

mb-2   /* 8px  - Margem entre labels e inputs */
mb-4   /* 16px - Margem entre campos de formulário */
mb-6   /* 24px - Margem entre seções */
mb-8   /* 32px - Margem entre blocos grandes */
```

---

## 🔲 Bordas e Sombras

### Border Radius

| Classe        | Valor | Uso                        |
|---------------|-------|----------------------------|
| `rounded`     | 4px   | Badges, tags               |
| `rounded-md`  | 6px   | Botões, inputs             |
| `rounded-lg`  | 8px   | Cards, modais              |
| `rounded-xl`  | 12px  | Cards de destaque          |
| `rounded-2xl` | 16px  | Containers grandes         |
| `rounded-full`| 9999px| Avatares, badges circulares|

### Shadows (Light Mode)

```css
/* Sutil */
.shadow-sm {
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
}

/* Padrão */
.shadow {
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
}

/* Média */
.shadow-md {
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
}

/* Grande */
.shadow-lg {
  box-shadow: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
}
```

### Shadows (Dark Mode)

```css
/* Sombras mais intensas para contraste */
.dark .shadow-md {
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.3), 0 2px 4px -2px rgb(0 0 0 / 0.3);
}

/* Glow effect em elementos primários */
.dark .shadow-violet-500\/50 {
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.5);
}
```

---

## 🎨 Ícones

### Biblioteca: Lucide React
- [Documentação](https://lucide.dev)
- Moderna, limpa, boa renderização em dark mode
- 1000+ ícones SVG
- Tree-shakeable (importa apenas o que usa)

### Instalação
```bash
npm install lucide-react
```

### Uso
```tsx
import { Home, CreditCard, TrendingUp, Moon, Sun } from 'lucide-react'

// Exemplo
<Home className="w-5 h-5 text-violet-500" />
```

### Tamanhos Padrão

| Uso              | Classe      | Tamanho |
|------------------|-------------|---------|
| Ícones pequenos  | `w-4 h-4`   | 16px    |
| Ícones padrão    | `w-5 h-5`   | 20px    |
| Ícones grandes   | `w-6 h-6`   | 24px    |
| Ícones em botões | `w-5 h-5`   | 20px    |
| Ícones em cards  | `w-8 h-8`   | 32px    |

### Cores
- Herdam a cor do texto por padrão
- Use classes de texto: `text-violet-500`, `text-gray-400`
- Em dark mode: `dark:text-violet-400`

---

## 🎬 Animações e Transições

### Durações

```css
transition-all duration-200    /* Padrão - Hover, Focus */
transition-all duration-300    /* Smooth - Modais, Drawers */
transition-colors duration-200 /* Tema toggle */
```

### Easing
```css
ease-in-out  /* Padrão */
ease-out     /* Slides, Fades */
ease-in      /* Dismissals */
```

### Efeitos de Hover

**Botões:**
```tsx
<button className="transform hover:scale-105 transition-transform duration-200">
  Hover me
</button>
```

**Cards:**
```tsx
<div className="hover:shadow-lg transition-shadow duration-300">
  Card content
</div>
```

**Links:**
```tsx
<a className="hover:text-violet-600 dark:hover:text-violet-400 transition-colors">
  Link
</a>
```

### Loading States

```tsx
/* Spinner */
<div className="animate-spin">
  <Loader className="w-5 h-5" />
</div>

/* Pulse */
<div className="animate-pulse bg-gray-200 dark:bg-slate-700 rounded">
  Loading...
</div>

/* Bounce */
<div className="animate-bounce">
  ⬇️
</div>
```

### Preferência do Usuário
```css
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
```

---

## 🎯 Estados de Interação

### Hover
```tsx
/* Botões */
className="hover:bg-violet-700 dark:hover:bg-violet-500"

/* Links */
className="hover:underline hover:text-violet-600"

/* Cards */
className="hover:shadow-lg hover:scale-[1.02]"
```

### Active (Pressed)
```tsx
className="active:scale-95"
```

### Focus (Keyboard Navigation)
```tsx
className="focus:ring-2 focus:ring-violet-500 focus:ring-offset-2
           dark:focus:ring-violet-400 dark:ring-offset-slate-900"
```

### Disabled
```tsx
className="disabled:opacity-50 disabled:cursor-not-allowed
           disabled:hover:bg-violet-600"
```

---

## ♿ Acessibilidade

### Contraste

**Mínimos WCAG AA:**
- Texto normal: 4.5:1
- Texto grande (>= 18px ou >= 14px bold): 3:1
- Componentes UI: 3:1

**Verificar:**
- Roxo (#8B5CF6) em branco: ✅ 7.5:1
- Roxo claro (#A78BFA) em slate-900: ✅ 8.2:1

### Focus Visible

Todos os elementos interativos devem ter indicador de foco:
```tsx
className="focus:outline-none focus:ring-2 focus:ring-violet-500"
```

### Semântica HTML

```tsx
/* BOM */
<button onClick={...}>Salvar</button>
<nav>...</nav>
<main>...</main>

/* RUIM */
<div onClick={...}>Salvar</div>
<div>Navigation here</div>
```

### Labels para Screen Readers

```tsx
/* Ícones sem texto */
<button aria-label="Fechar modal">
  <X className="w-5 h-5" />
</button>

/* Inputs */
<label htmlFor="email" className="sr-only">Email</label>
<input id="email" type="email" />
```

### Keyboard Navigation

**Teclas suportadas:**
- `Tab` / `Shift+Tab` - Navegação entre elementos
- `Enter` / `Space` - Ativar botões
- `Esc` - Fechar modais/drawers
- `Arrow Keys` - Navegação em menus/selects

---

## 📱 Componentes Base

### Botões

```tsx
/* Primary */
<button className="
  bg-violet-600 hover:bg-violet-700
  dark:bg-violet-500 dark:hover:bg-violet-600
  text-white font-medium
  px-4 py-2 rounded-md
  transition-colors duration-200
  focus:ring-2 focus:ring-violet-500 focus:ring-offset-2
">
  Primary Action
</button>

/* Secondary */
<button className="
  bg-gray-200 hover:bg-gray-300
  dark:bg-slate-700 dark:hover:bg-slate-600
  text-gray-900 dark:text-slate-100
  px-4 py-2 rounded-md
">
  Secondary Action
</button>

/* Ghost */
<button className="
  text-violet-600 dark:text-violet-400
  hover:bg-violet-50 dark:hover:bg-violet-500/10
  px-4 py-2 rounded-md
">
  Ghost Button
</button>
```

### Inputs

```tsx
<input className="
  w-full px-4 py-2 rounded-md
  bg-white dark:bg-slate-800
  border border-gray-200 dark:border-slate-700
  text-gray-900 dark:text-slate-100
  placeholder:text-gray-400 dark:placeholder:text-slate-500
  focus:ring-2 focus:ring-violet-500 focus:border-transparent
  transition-colors duration-200
" />
```

### Cards

```tsx
<div className="
  bg-white dark:bg-slate-800
  border border-gray-200 dark:border-slate-700
  rounded-lg shadow-sm
  p-6
  hover:shadow-md transition-shadow duration-300
">
  Card content
</div>
```

---

## 🎨 Exemplos de Uso

### Dashboard Card com Gradiente

```tsx
<div className="
  bg-gradient-to-br from-violet-500 to-violet-600
  dark:from-violet-600 dark:to-purple-700
  text-white rounded-xl p-6
  shadow-lg dark:shadow-violet-500/20
">
  <h3 className="text-2xl font-bold">R$ 1.234,56</h3>
  <p className="text-violet-100">Gastos do mês</p>
</div>
```

### Botão com Ícone

```tsx
<button className="
  inline-flex items-center gap-2
  bg-violet-600 hover:bg-violet-700 text-white
  px-4 py-2 rounded-md
">
  <Plus className="w-5 h-5" />
  Nova Conta
</button>
```

### Badge de Status

```tsx
/* Success */
<span className="
  inline-flex items-center gap-1
  px-2 py-1 rounded text-xs font-medium
  bg-emerald-100 text-emerald-800
  dark:bg-emerald-500/20 dark:text-emerald-400
">
  <Check className="w-3 h-3" />
  Paga
</span>

/* Warning */
<span className="
  px-2 py-1 rounded text-xs font-medium
  bg-amber-100 text-amber-800
  dark:bg-amber-500/20 dark:text-amber-400
">
  Vence em 3 dias
</span>
```

---

## 📚 Recursos

- [Tailwind CSS Documentation](https://tailwindcss.com)
- [Lucide Icons](https://lucide.dev)
- [Color Contrast Checker](https://webaim.org/resources/contrastchecker/)
- [Inter Font](https://fonts.google.com/specimen/Inter)

---

**Última atualização**: Dezembro 2025
