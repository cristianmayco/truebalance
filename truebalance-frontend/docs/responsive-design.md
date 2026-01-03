# Responsive Design - True Balance

Estratégia de design responsivo mobile-first para garantir experiência consistente em todos os dispositivos.

## 📱 Breakpoints

### Definição de Breakpoints

| Dispositivo | Breakpoint | Classe Tailwind | Uso |
|-------------|-----------|-----------------|-----|
| **Mobile**  | < 768px   | (default)       | Smartphones em portrait/landscape |
| **Tablet**  | 768px - 1023px | `md:` | Tablets, smartphones landscape |
| **Desktop** | >= 1024px | `lg:` | Laptops, desktops, monitores |
| **Large Desktop** | >= 1280px | `xl:` | Monitores grandes |

### Configuração Tailwind

```javascript
// tailwind.config.js
export default {
  darkMode: 'class',
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    screens: {
      'sm': '640px',
      'md': '768px',
      'lg': '1024px',
      'xl': '1280px',
      '2xl': '1536px',
    },
    extend: {
      // Cores customizadas aqui
    }
  }
}
```

---

## 🎯 Estratégia Mobile-First

### Princípios

1. **Desenvolva para mobile primeiro**
   - CSS base para telas pequenas
   - Use `md:` e `lg:` para adaptar para telas maiores
   - Mais fácil adicionar features do que remover

2. **Progressive Enhancement**
   - Funcionalidade core em todos os dispositivos
   - Features extras em telas maiores
   - Graceful degradation

3. **Touch-Friendly**
   - Botões >= 44x44px
   - Espaçamento generoso entre elementos clicáveis
   - Gestos nativos (swipe, long press)

### Exemplo de Código

```tsx
// ❌ Desktop-first (evitar)
<div className="flex lg:flex-col">

// ✅ Mobile-first (correto)
<div className="flex-col lg:flex-row">
```

---

## 📐 Adaptações por Breakpoint

### Mobile (< 768px)

**Layout:**
- Single column layout
- Full-width cards
- Stacked elements
- Bottom navigation (fixed)
- Hamburger menu (se necessário)

**Componentes:**
- Tabelas viram cards verticais
- Formulários em coluna única
- Modais em full-screen
- Date pickers nativos
- Number inputs com teclado numérico

**Exemplo:**
```tsx
<div className="
  w-full px-4 py-2        /* Mobile: full width, padding lateral */
  md:w-1/2 md:px-6        /* Tablet: 50% width, mais padding */
  lg:w-1/3 lg:px-8        /* Desktop: 33% width, ainda mais padding */
">
  Content
</div>
```

---

### Tablet (768px - 1023px)

**Layout:**
- 2 column layout quando apropriado
- Sidebar colapsável
- Hybrid navigation (sidebar + bottom nav opcional)
- Modais em tamanho médio (não full-screen)

**Componentes:**
- Tabelas simplificadas (menos colunas)
- Grid 2x2 para cards
- Formulários podem ter 2 colunas
- Dropdowns ao invés de drawers

**Exemplo:**
```tsx
<div className="
  grid grid-cols-1      /* Mobile: 1 coluna */
  md:grid-cols-2        /* Tablet: 2 colunas */
  lg:grid-cols-3        /* Desktop: 3 colunas */
  gap-4
">
  {cards.map(...)}
</div>
```

---

### Desktop (>= 1024px)

**Layout:**
- Sidebar fixa (não colapsável)
- Multi-column layouts (3-4 colunas)
- Tabelas completas com todas as colunas
- Modais centralizados (max-width)

**Componentes:**
- Hover states visíveis
- Tooltips em hover
- Keyboard shortcuts
- Context menus (right-click)
- Data tables com sorting/filtering

**Exemplo:**
```tsx
<div className="
  fixed bottom-0 left-0 right-0 z-50     /* Mobile: bottom nav */
  lg:static lg:w-64 lg:h-screen          /* Desktop: sidebar fixa */
">
  Navigation
</div>
```

---

## 🎨 Componentes Responsivos

### Navigation

**Mobile:**
```tsx
<nav className="
  lg:hidden fixed bottom-0 left-0 right-0
  bg-white dark:bg-slate-800 border-t
  flex justify-around items-center h-16
">
  {navItems.map(item => (
    <NavItem icon={item.icon} label={item.label} />
  ))}
</nav>
```

**Desktop:**
```tsx
<aside className="
  hidden lg:flex flex-col w-64 h-screen
  bg-white dark:bg-slate-800 border-r
  fixed left-0 top-0
">
  {navItems.map(item => (
    <NavItem icon={item.icon} label={item.label} />
  ))}
</aside>
```

---

### Data Table

**Mobile (Cards):**
```tsx
<div className="lg:hidden space-y-4">
  {data.map(item => (
    <div className="bg-white dark:bg-slate-800 rounded-lg p-4 shadow">
      <div className="flex justify-between mb-2">
        <span className="font-semibold">{item.name}</span>
        <span className="text-violet-600">{item.amount}</span>
      </div>
      <div className="text-sm text-gray-600 dark:text-slate-400">
        {item.date}
      </div>
    </div>
  ))}
</div>
```

**Desktop (Table):**
```tsx
<table className="hidden lg:table w-full">
  <thead>
    <tr>
      <th>Nome</th>
      <th>Data</th>
      <th>Valor</th>
      <th>Ações</th>
    </tr>
  </thead>
  <tbody>
    {data.map(item => (
      <tr>
        <td>{item.name}</td>
        <td>{item.date}</td>
        <td>{item.amount}</td>
        <td><Actions /></td>
      </tr>
    ))}
  </tbody>
</table>
```

---

### Forms

```tsx
<form className="space-y-4">
  {/* Mobile: 1 column, Desktop: 2 columns */}
  <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
    <div>
      <label>Nome</label>
      <input type="text" />
    </div>
    <div>
      <label>Valor</label>
      <input type="number" />
    </div>
  </div>

  {/* Botões: Mobile (stacked), Desktop (inline) */}
  <div className="flex flex-col lg:flex-row gap-2 lg:gap-4">
    <button className="w-full lg:w-auto">Salvar</button>
    <button className="w-full lg:w-auto">Cancelar</button>
  </div>
</form>
```

---

### Modal/Dialog

```tsx
/* Mobile: Full screen */
<div className="
  fixed inset-0 z-50
  lg:inset-auto lg:top-1/2 lg:left-1/2
  lg:-translate-x-1/2 lg:-translate-y-1/2
  lg:max-w-lg lg:rounded-xl
  bg-white dark:bg-slate-800
">
  Modal content
</div>
```

---

## 👆 Touch Targets

### Tamanhos Mínimos

**Regra de Ouro:** >= 44x44px (iOS Human Interface Guidelines)

```tsx
/* ❌ Muito pequeno para mobile */
<button className="px-2 py-1">
  Button
</button>

/* ✅ Touch-friendly */
<button className="px-4 py-3 min-h-[44px]">
  Button
</button>
```

### Espaçamento

```tsx
/* Botões adjacentes */
<div className="flex gap-2">  {/* Mínimo 8px entre botões */}
  <button>...</button>
  <button>...</button>
</div>

/* Links em lista */
<nav className="space-y-1">  {/* Mínimo 4px entre links */}
  <a className="block py-2">Link 1</a>
  <a className="block py-2">Link 2</a>
</nav>
```

---

## 📱 Gestos Mobile

### Swipe

```tsx
// Usar biblioteca como react-swipeable
import { useSwipeable } from 'react-swipeable'

const handlers = useSwipeable({
  onSwipedLeft: () => handleDelete(),
  onSwipedRight: () => handleEdit(),
})

<div {...handlers} className="touch-pan-y">
  Swipe me!
</div>
```

### Long Press

```tsx
// Mostrar menu de contexto
const [isLongPressing, setIsLongPressing] = useState(false)
let timeout: NodeJS.Timeout

<div
  onTouchStart={() => {
    timeout = setTimeout(() => setIsLongPressing(true), 500)
  }}
  onTouchEnd={() => {
    clearTimeout(timeout)
    setIsLongPressing(false)
  }}
>
  Long press me
</div>
```

### Pull to Refresh

```tsx
// Usar biblioteca como react-pull-to-refresh
import PullToRefresh from 'react-pull-to-refresh'

<PullToRefresh onRefresh={handleRefresh}>
  <div>Content</div>
</PullToRefresh>
```

---

## ⚡ Performance Mobile

### Otimizações

1. **Lazy Loading**
```tsx
const Dashboard = lazy(() => import('./pages/Dashboard'))
const Reports = lazy(() => import('./pages/Reports'))
```

2. **Image Optimization**
```tsx
<img
  src={image.url}
  srcSet={`${image.url}?w=400 400w, ${image.url}?w=800 800w`}
  sizes="(max-width: 768px) 100vw, 50vw"
  loading="lazy"
/>
```

3. **Reduce Bundle Size**
- Tree shaking automático (Vite)
- Import apenas componentes necessários
- Code splitting por rota

4. **Minimizar Reflows**
```css
/* Use transform ao invés de width/height para animações */
.animate {
  transform: scale(1.05);  /* ✅ Bom */
  width: 105%;              /* ❌ Ruim (causa reflow) */
}
```

---

## 🎨 Dark Mode em Responsive

### Toggle de Tema

```tsx
/* Desktop: No TopBar */
<button className="hidden lg:flex">
  {isDark ? <Sun /> : <Moon />}
</button>

/* Mobile: No menu ou bottom nav */
<button className="lg:hidden">
  {isDark ? <Sun /> : <Moon />}
</button>
```

### Imagens Responsivas com Tema

```tsx
<picture>
  {/* Dark mode mobile */}
  <source
    media="(max-width: 767px) and (prefers-color-scheme: dark)"
    srcSet="/logo-mobile-dark.svg"
  />
  {/* Light mode mobile */}
  <source
    media="(max-width: 767px)"
    srcSet="/logo-mobile-light.svg"
  />
  {/* Fallback */}
  <img src="/logo-light.svg" alt="Logo" />
</picture>
```

---

## 📊 Teste de Responsividade

### Checklist

- [ ] Funciona em iPhone SE (375px)
- [ ] Funciona em iPhone 12/13 Pro (390px)
- [ ] Funciona em iPad (768px)
- [ ] Funciona em iPad Pro (1024px)
- [ ] Funciona em desktop (1920px)
- [ ] Botões >= 44x44px
- [ ] Texto legível sem zoom
- [ ] Sem scroll horizontal
- [ ] Forms usáveis em mobile
- [ ] Modais não cortados
- [ ] Bottom nav não sobrepõe conteúdo

### Ferramentas

1. **Chrome DevTools**
   - Device toolbar (Cmd+Shift+M)
   - Responsive mode
   - Throttling de rede

2. **Firefox Responsive Design Mode**
   - Cmd+Opt+M
   - Touch simulation

3. **BrowserStack** (opcional)
   - Testes em dispositivos reais
   - Screenshots automáticos

---

## 📚 Recursos

- [Tailwind Responsive Design](https://tailwindcss.com/docs/responsive-design)
- [Mobile First Design](https://www.nngroup.com/articles/mobile-first-design/)
- [Touch Target Sizes](https://www.nngroup.com/articles/touch-target-size/)

---

**Última atualização**: Dezembro 2025
