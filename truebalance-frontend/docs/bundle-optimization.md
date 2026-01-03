# Guia de Otimização de Bundle - TrueBalance Frontend

Este documento descreve as técnicas e ferramentas de otimização de bundle implementadas no TrueBalance.

## 📦 Visão Geral

Um bundle otimizado resulta em:
- ⚡ Carregamento inicial mais rápido
- 📉 Menor uso de largura de banda
- 🚀 Melhor performance geral
- 💾 Melhor cache do navegador

## 🔧 Ferramentas Implementadas

### 1. Rollup Plugin Visualizer

Ferramenta de análise visual do bundle.

**Instalação:**
```bash
npm install -D rollup-plugin-visualizer
```

**Uso:**
```bash
# Build e abrir análise automaticamente
npm run build:analyze

# Ou build normal (stats.html estará em dist/)
npm run build
```

**Análise:**
- Arquivo gerado: `dist/stats.html`
- Mostra tamanho de cada dependência
- Visualização em treemap interativo
- Mostra tamanhos gzip e brotli

**O que procurar:**
- ❌ Dependências grandes não utilizadas
- ❌ Duplicação de código
- ❌ Bibliotecas importadas múltiplas vezes
- ✅ Code splitting eficiente
- ✅ Vendor chunks bem organizados

---

## 🎯 Configurações de Build

### vite.config.ts

```ts
export default defineConfig({
  build: {
    // Target ES2015 para suporte amplo mas moderno
    target: 'es2015',

    // Minificação com esbuild (mais rápido que terser)
    minify: 'esbuild',

    // Desabilitar source maps em produção
    sourcemap: false,

    // Limite de aviso de chunk (500kb)
    chunkSizeWarningLimit: 500,

    // Code splitting de CSS
    cssCodeSplit: true,

    rollupOptions: {
      output: {
        // Chunk splitting manual
        manualChunks: {
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],
          'framer-motion': ['framer-motion'],
          'charts': ['recharts'],
          'react-query': ['@tanstack/react-query'],
          'forms': ['react-hook-form', 'zod'],
          'utils': ['date-fns', 'axios'],
        },

        // Naming patterns
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
      },
    },
  },
});
```

**Benefícios:**
- Vendor chunks permitem cache de longo prazo
- Bibliotecas grandes isoladas em chunks próprios
- Hash-based naming para cache busting eficiente

---

## 🌳 Tree-Shaking

### Lucide Icons (Centralized Imports)

**❌ Problema:**
```tsx
// Importa TODA a biblioteca lucide-react (~1MB)
import { Home, User, Settings } from 'lucide-react';
```

**✅ Solução:**
```tsx
// Imports centralizados em src/lib/icons.ts
import { HomeIcon, UserIcon, SettingsIcon } from '@/lib/icons';
```

**Como funciona:**
1. Todos os ícones são re-exportados de `src/lib/icons.ts`
2. Apenas ícones efetivamente usados são incluídos no bundle
3. Tree-shaking remove o resto automaticamente

**Adicionar novo ícone:**
```ts
// src/lib/icons.ts
export {
  NewIcon as NewIconName,
} from 'lucide-react';
```

**Medição:**
- Antes: ~1MB (todos os ícones)
- Depois: ~50-100KB (apenas ícones usados)
- **Economia: ~90%**

---

## 📊 Code Splitting

### Automático (React.lazy)

Já implementado em `src/routes/index.tsx`:

```tsx
const Dashboard = lazy(() => import('@/pages/Dashboard'));
const BillsList = lazy(() => import('@/pages/bills/BillsList'));
// ...

function LazyRoute({ component: Component }: LazyRouteProps) {
  return (
    <Suspense fallback={<LoadingSpinner fullScreen />}>
      <Component />
    </Suspense>
  );
}
```

**Benefícios:**
- Cada rota é um chunk separado
- Carregado apenas quando acessado
- Bundle inicial ~50-70% menor

---

### Manual (manualChunks)

Bibliotecas grandes são isoladas:

**Chunks criados:**
1. **react-vendor** (~150KB)
   - react, react-dom, react-router-dom
   - Carregado em todas as páginas
   - Cache de longo prazo

2. **framer-motion** (~100KB)
   - Biblioteca de animações
   - Apenas em páginas com animações

3. **charts** (~200KB)
   - recharts
   - Apenas na página de Relatórios

4. **react-query** (~50KB)
   - TanStack Query
   - Carregado cedo mas cacheable

5. **forms** (~80KB)
   - react-hook-form, zod
   - Apenas em páginas com formulários

6. **utils** (~50KB)
   - date-fns, axios
   - Utilities comuns

**Vantagens:**
- Cache granular
- Paralelização de downloads
- Atualização independente de vendors

---

## 🗜️ Minificação e Compressão

### Minificação (esbuild)

```ts
build: {
  minify: 'esbuild', // Mais rápido que 'terser'
}
```

**Comparação:**
- **esbuild:** ~10x mais rápido, 95-98% do tamanho do terser
- **terser:** Mais lento, mas ~2-5% menor

**Escolhemos esbuild por:**
- Builds muito mais rápidos (importante para CI/CD)
- Diferença de tamanho negligível
- Mesma funcionalidade

---

### Compressão (Servidor)

Configurar no servidor web (nginx, apache, etc.):

**Gzip (suporte universal):**
```nginx
gzip on;
gzip_types text/plain text/css application/json application/javascript text/xml;
gzip_min_length 1000;
```

**Brotli (melhor compressão, navegadores modernos):**
```nginx
brotli on;
brotli_types text/plain text/css application/json application/javascript text/xml;
```

**Economia típica:**
- Gzip: 70-80% redução
- Brotli: 75-85% redução

**Exemplo:**
- Bundle original: 500KB
- Gzip: ~125KB (75% menor)
- Brotli: ~100KB (80% menor)

---

## 📏 Métricas de Referência

### Tamanhos Esperados (após otimização)

**Bundles JavaScript:**
```
vendor.js (react-vendor)    : ~40KB gzip
index.js (main app)          : ~30KB gzip
framer-motion.js             : ~25KB gzip
charts.js                    : ~50KB gzip
forms.js                     : ~20KB gzip
```

**Total inicial (First Load):**
- **~100-150KB** (gzip)
- **~300-400KB** (não comprimido)

**Por rota (lazy loaded):**
- **~20-50KB** por página adicional

---

### Core Web Vitals Targets

**LCP (Largest Contentful Paint):**
- ✅ Bom: < 2.5s
- ⚠️ Precisa melhorar: 2.5-4s
- ❌ Ruim: > 4s

**FID (First Input Delay):**
- ✅ Bom: < 100ms
- ⚠️ Precisa melhorar: 100-300ms
- ❌ Ruim: > 300ms

**CLS (Cumulative Layout Shift):**
- ✅ Bom: < 0.1
- ⚠️ Precisa melhorar: 0.1-0.25
- ❌ Ruim: > 0.25

---

## ✅ Checklist de Otimização

### Antes de Fazer Deploy:

- [ ] Executar `npm run build:analyze`
- [ ] Verificar tamanho de cada chunk
- [ ] Confirmar que chunks grandes (>500KB) estão justificados
- [ ] Verificar duplicação de dependências
- [ ] Confirmar tree-shaking eficiente
- [ ] Testar performance com Lighthouse
- [ ] Validar Core Web Vitals
- [ ] Confirmar compressão ativada no servidor

---

## 🐛 Problemas Comuns

### Chunk muito grande

**Problema:**
```
(!) Some chunks are larger than 500 KiB
```

**Soluções:**
1. Identificar dependência grande no stats.html
2. Criar chunk separado no manualChunks
3. Considerar lazy loading
4. Verificar se dependência é necessária

---

### Dependências duplicadas

**Problema:**
Mesma biblioteca importada em múltiplos chunks.

**Solução:**
```ts
// vite.config.ts
manualChunks: {
  'shared-vendor': ['library-name'],
}
```

---

### Import side effects

**Problema:**
Biblioteca não tree-shakeable.

**Como identificar:**
```json
// package.json da biblioteca
{
  "sideEffects": true // ❌ Não permite tree-shaking
}
```

**Soluções:**
1. Procurar alternativa tree-shakeable
2. Importar apenas módulos necessários
3. Usar dynamic import

**Exemplo:**
```tsx
// ❌ Importa tudo
import _ from 'lodash';

// ✅ Importa apenas o necessário
import debounce from 'lodash/debounce';
```

---

## 📈 Monitoramento Contínuo

### Automação

Adicionar ao CI/CD:

```bash
# .github/workflows/bundle-size.yml
- name: Build and analyze
  run: npm run build:analyze

- name: Upload bundle analysis
  uses: actions/upload-artifact@v3
  with:
    name: bundle-analysis
    path: dist/stats.html
```

---

### Ferramentas Externas

**Bundle Analyzer (GitHub Action):**
```yaml
- uses: preactjs/compressed-size-action@v2
  with:
    build-script: "build"
```

**Lighthouse CI:**
```bash
npm install -g @lhci/cli
lhci autorun
```

---

## 🚀 Próximos Passos

### Futuras Otimizações:

1. **Prefetch crítico:**
   ```html
   <link rel="prefetch" href="/assets/charts-[hash].js">
   ```

2. **Preload de fontes:**
   ```html
   <link rel="preload" href="/fonts/inter.woff2" as="font">
   ```

3. **Service Worker caching:**
   - Já implementado via Vite PWA
   - Cache de assets estáticos
   - Runtime caching de API calls

4. **HTTP/2 Server Push:**
   - Configurar no servidor
   - Push de critical chunks

5. **Resource Hints:**
   - dns-prefetch
   - preconnect
   - prefetch

---

## 📚 Recursos

**Ferramentas:**
- [Bundle Phobia](https://bundlephobia.com/) - Checar tamanho de pacotes npm
- [Webpack Bundle Analyzer](https://github.com/webpack-contrib/webpack-bundle-analyzer)
- [Lighthouse](https://developers.google.com/web/tools/lighthouse)

**Documentação:**
- [Vite Build Optimizations](https://vitejs.dev/guide/build.html)
- [Rollup Code Splitting](https://rollupjs.org/guide/en/#code-splitting)
- [Web.dev Performance](https://web.dev/performance/)

---

**Documento criado em:** Dezembro 2025
**Versão:** 1.0
**Mantido por:** Equipe de Desenvolvimento TrueBalance
