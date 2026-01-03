# Guia de Animações - TrueBalance Frontend

Este documento descreve o sistema de animações implementado no TrueBalance usando **Framer Motion**.

## 📦 Dependências

- `framer-motion` - Biblioteca principal de animações
- React 18+

## 🎨 Componentes de Animação

### 1. PageTransition

Adiciona animação de fade-in suave ao carregar páginas.

**Uso:**
```tsx
import { PageTransition } from '@/components/ui/PageTransition';

function MyPage() {
  return (
    <PageTransition>
      <div>Conteúdo da página</div>
    </PageTransition>
  );
}
```

**Comportamento:**
- Fade in com slight upward movement (20px)
- Duração: 0.4s
- Easing: easeOut customizado
- Respeita `prefers-reduced-motion`

---

### 2. SlideIn

Animação de slide-in a partir de qualquer direção.

**Uso:**
```tsx
import { SlideIn } from '@/components/ui/SlideIn';

// Slide from bottom (padrão)
<SlideIn>
  <div>Modal content</div>
</SlideIn>

// Slide from specific direction
<SlideIn direction="left" delay={0.2}>
  <div>Sidebar content</div>
</SlideIn>
```

**Props:**
- `direction`: 'top' | 'bottom' | 'left' | 'right' (padrão: 'bottom')
- `delay`: número em segundos (padrão: 0)
- Respeita `prefers-reduced-motion`

**Casos de uso:**
- Modais (bottom em mobile)
- Sidebars (left/right)
- Notificações (top)

---

### 3. Modal

Modal completo com animações integradas e acessibilidade.

**Uso:**
```tsx
import { Modal } from '@/components/ui/Modal';

<Modal
  isOpen={isOpen}
  onClose={() => setIsOpen(false)}
  title="Confirmar Ação"
  footer={<Button onClick={handleConfirm}>Confirmar</Button>}
>
  <p>Tem certeza que deseja continuar?</p>
</Modal>
```

**Recursos:**
- ✅ Slide-in from bottom com fade
- ✅ Backdrop com fade animation
- ✅ Focus trap (Tab/Shift+Tab)
- ✅ Auto-focus no primeiro elemento
- ✅ Fechar com ESC (opcional)
- ✅ Fechar ao clicar fora (opcional)
- ✅ Fullscreen em mobile
- ✅ ARIA attributes completos
- ✅ Respeita `prefers-reduced-motion`

**Props:**
- `isOpen`: boolean
- `onClose`: () => void
- `title`: string (opcional)
- `footer`: ReactNode (opcional)
- `size`: 'sm' | 'md' | 'lg' | 'xl' | 'full' (padrão: 'md')
- `closeOnOverlayClick`: boolean (padrão: true)
- `closeOnEsc`: boolean (padrão: true)
- `showCloseButton`: boolean (padrão: true)

---

### 4. Card

Card com animação de hover suave.

**Uso:**
```tsx
import Card from '@/components/ui/Card';

// Card padrão com hover
<Card>
  <div className="p-6">Card content</div>
</Card>

// Card com variante gradient
<Card variant="gradient" hover>
  <div className="p-6">Gradient card</div>
</Card>

// Card sem hover
<Card hover={false}>
  <div className="p-6">Static card</div>
</Card>
```

**Props:**
- `variant`: 'default' | 'gradient' | 'outlined' (padrão: 'default')
- `hover`: boolean (padrão: true)
- Respeita `prefers-reduced-motion`

**Animação de hover:**
- Scale: 1.0 → 1.02
- Shadow elevation aumenta
- Duração: 0.3s
- Easing: custom cubic-bezier

**Variantes:**
- `default`: Fundo branco/cinza escuro com shadow
- `gradient`: Gradiente violet/purple com shadow
- `outlined`: Apenas borda, sem fundo

---

### 5. StaggerContainer & StaggerItem

Anima listas de elementos com delay sequencial entre cada item.

**Uso:**
```tsx
import { StaggerContainer, StaggerItem } from '@/components/ui/StaggerContainer';

<StaggerContainer staggerDelay={0.1}>
  {items.map(item => (
    <StaggerItem key={item.id}>
      <Card>{item.name}</Card>
    </StaggerItem>
  ))}
</StaggerContainer>
```

**Props (StaggerContainer):**
- `staggerDelay`: número em segundos entre cada item (padrão: 0.1)
- Respeita `prefers-reduced-motion`

**Comportamento:**
- Cada item faz fade-in com slight upward movement
- Delay automático entre itens cria efeito "cascata"
- Perfeito para listas, grids de cards

**Exemplo real:**
```tsx
// Lista de contas
<StaggerContainer staggerDelay={0.08}>
  {bills.map(bill => (
    <StaggerItem key={bill.id}>
      <BillCard bill={bill} />
    </StaggerItem>
  ))}
</StaggerContainer>
```

---

## 🪝 Hooks

### usePrefersReducedMotion

Hook para detectar preferência de movimento reduzido do usuário.

**Uso:**
```tsx
import { usePrefersReducedMotion } from '@/hooks/usePrefersReducedMotion';

function MyComponent() {
  const prefersReducedMotion = usePrefersReducedMotion();

  const duration = prefersReducedMotion ? 0.01 : 0.4;

  return (
    <motion.div
      animate={{ opacity: 1 }}
      transition={{ duration }}
    />
  );
}
```

**Benefícios:**
- Reativo - atualiza se usuário mudar preferência
- Compatível com navegadores antigos
- Usado internamente em todos os componentes de animação

---

## ♿ Acessibilidade

Todos os componentes de animação respeitam a preferência do usuário `prefers-reduced-motion`:

**Quando ativo:**
- Durações reduzidas para 0.01ms (essencialmente instantâneo)
- Movimentos (translate, scale) desabilitados
- Opacidade mantida

**Como testar:**

**macOS:**
1. System Preferences → Accessibility → Display
2. Ativar "Reduce motion"

**Windows:**
1. Settings → Ease of Access → Display
2. Ativar "Show animations in Windows"

**Navegador (DevTools):**
```css
/* Chrome DevTools → Rendering → Emulate CSS media feature */
prefers-reduced-motion: reduce
```

---

## 🎯 Diretrizes de Uso

### Quando usar animações:

✅ **Use para:**
- Transições de página
- Aparecer/desaparecer modais
- Hover em elementos interativos
- Loading states
- Feedback de ações do usuário
- Listas e grids (stagger)

❌ **Evite para:**
- Animações contínuas/infinitas sem propósito
- Movimentos bruscos ou rápidos demais
- Animações que bloqueiam interação
- Elementos críticos (formulários, botões de ação)

### Performance:

**Boas práticas:**
1. Use `transform` e `opacity` (aceleração de GPU)
2. Evite animar `width`, `height`, `top`, `left`
3. Use `will-change` com cuidado
4. Limite animações simultâneas (máximo ~5-6)
5. Sempre respeite `prefers-reduced-motion`

**Exemplo otimizado:**
```tsx
// ✅ BOM - usa transform
<motion.div animate={{ scale: 1.1 }} />

// ❌ RUIM - força reflow
<motion.div animate={{ width: 200 }} />
```

---

## 📊 Timings Recomendados

```tsx
// Micro-interactions (hover, click)
duration: 0.2 - 0.3s

// Transições de página
duration: 0.4 - 0.5s

// Modais e overlays
duration: 0.3 - 0.4s

// Stagger delay (entre itens)
delay: 0.05 - 0.1s

// Reduced motion
duration: 0.01s (essencialmente desabilitado)
```

---

## 🎨 Easings Customizados

```tsx
// Padrão do projeto (smooth e profissional)
ease: [0.25, 0.1, 0.25, 1] // cubic-bezier

// Alternativas comuns:
easeOut: 'easeOut'      // Desaceleração suave
easeIn: 'easeIn'        // Aceleração suave
easeInOut: 'easeInOut'  // Aceleração e desaceleração
```

---

## 📚 Exemplos Práticos

### Página completa com animações

```tsx
import { PageTransition } from '@/components/ui/PageTransition';
import { StaggerContainer, StaggerItem } from '@/components/ui/StaggerContainer';
import Card from '@/components/ui/Card';

export function BillsListPage() {
  const { data: bills } = useBills();

  return (
    <PageTransition>
      <div className="container mx-auto p-6">
        <h1 className="text-3xl font-bold mb-8">Minhas Contas</h1>

        <StaggerContainer staggerDelay={0.08}>
          {bills.map(bill => (
            <StaggerItem key={bill.id}>
              <Card hover>
                <div className="p-6">
                  <h3>{bill.name}</h3>
                  <p>{formatCurrency(bill.amount)}</p>
                </div>
              </Card>
            </StaggerItem>
          ))}
        </StaggerContainer>
      </div>
    </PageTransition>
  );
}
```

### Modal com confirmação

```tsx
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';

export function DeleteConfirmModal({ isOpen, onClose, onConfirm }) {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Confirmar Exclusão"
      size="sm"
      footer={
        <div className="flex gap-3 justify-end">
          <Button variant="ghost" onClick={onClose}>
            Cancelar
          </Button>
          <Button variant="danger" onClick={onConfirm}>
            Excluir
          </Button>
        </div>
      }
    >
      <p className="text-gray-600 dark:text-gray-400">
        Tem certeza que deseja excluir esta conta? Esta ação não pode ser desfeita.
      </p>
    </Modal>
  );
}
```

---

## 🔧 Configuração do Tailwind

As animações CSS básicas já estão configuradas no `tailwind.config.js`:

```js
// Skeleton shimmer
animation: {
  shimmer: 'shimmer 2s infinite',
}

// PWA prompts
animation: {
  'slide-up': 'slide-up 0.4s ease-out',
}
```

---

## 📝 Checklist de Implementação

Ao adicionar animações a um novo componente:

- [ ] Importar framer-motion
- [ ] Usar componentes de animação existentes quando possível
- [ ] Implementar `prefers-reduced-motion`
- [ ] Testar em light e dark mode
- [ ] Testar em mobile e desktop
- [ ] Validar performance (60fps)
- [ ] Garantir que animação não bloqueia interação
- [ ] Adicionar documentação se criar nova animação

---

## 🐛 Troubleshooting

**Animação não aparece:**
- Verificar se `framer-motion` está instalado
- Confirmar que componente está envolvido em `<AnimatePresence>` (para exit animations)
- Checar console por erros

**Animação muito rápida/lenta:**
- Ajustar `duration` no objeto `transition`
- Verificar se `prefers-reduced-motion` não está ativo

**Performance ruim:**
- Limitar número de animações simultâneas
- Usar apenas `transform` e `opacity`
- Considerar `layout` animations com cuidado (custoso)

---

**Documento criado em:** Dezembro 2025
**Versão:** 1.0
**Mantido por:** Equipe de Desenvolvimento TrueBalance
