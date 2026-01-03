# PWA Implementation - TrueBalance Frontend

## Overview

O TrueBalance é uma Progressive Web App (PWA) completa, permitindo instalação em dispositivos móveis e desktop, além de funcionalidades offline.

## Tecnologias Utilizadas

- **vite-plugin-pwa**: Plugin Vite para geração automática de service worker e manifest
- **workbox**: Biblioteca do Google para estratégias de caching e service worker
- **workbox-window**: API para comunicação com service worker no client-side

## Configuração

### 1. Service Worker (vite.config.ts)

O service worker é gerado automaticamente pelo Vite PWA plugin com as seguintes estratégias:

#### Estratégias de Caching:

**Google Fonts (CacheFirst)**
- Cache: `google-fonts-cache`
- Expiration: 1 ano
- Max Entries: 10
- Estratégia: Cache First (usa cache, depois rede)

**Google Fonts Static Files (CacheFirst)**
- Cache: `gstatic-fonts-cache`
- Expiration: 1 ano
- Max Entries: 10
- Estratégia: Cache First

**API Calls (NetworkFirst)**
- Cache: `api-cache`
- Expiration: 5 minutos
- Max Entries: 50
- Network Timeout: 10 segundos
- Estratégia: Network First (tenta rede, fallback para cache)

#### Configurações Gerais:

- **registerType**: `autoUpdate` - Atualiza automaticamente quando nova versão está disponível
- **skipWaiting**: `true` - Ativa novo service worker imediatamente
- **clientsClaim**: `true` - Service worker assume controle imediatamente
- **cleanupOutdatedCaches**: `true` - Remove caches antigos automaticamente

### 2. Manifest (Auto-gerado)

O manifest é gerado automaticamente a partir da configuração em `vite.config.ts`:

```json
{
  "name": "TrueBalance - Gerenciamento Financeiro",
  "short_name": "TrueBalance",
  "theme_color": "#9333ea",
  "background_color": "#ffffff",
  "display": "standalone",
  "orientation": "portrait-primary",
  "start_url": "/",
  "scope": "/"
}
```

### 3. Ícones

O app suporta os seguintes tamanhos de ícones:
- 72x72
- 96x96
- 128x128
- 144x144
- 152x152
- 192x192
- 384x384
- 512x512

Todos com `purpose: "any maskable"` para suporte em diferentes plataformas.

### 4. Atalhos (App Shortcuts)

O PWA inclui 3 atalhos de app:
1. **Nova Conta** → `/bills/new`
2. **Relatórios** → `/reports`
3. **Visão 360°** → `/consolidated`

## Componentes PWA

### PWAInstallPrompt

Componente que exibe banner de instalação do app.

**Localização:** `src/components/pwa/PWAInstallPrompt.tsx`

**Funcionalidades:**
- Detecta evento `beforeinstallprompt`
- Exibe prompt após 3 segundos
- Permite instalação ou dismissal
- Memoriza dismissal por 7 dias
- Detecta se app já está instalado

**Design:**
- Posicionado no canto inferior (bottom-20 mobile, bottom-4 desktop)
- Animação slide-up
- Suporte a light/dark mode
- Responsivo

### PWAUpdatePrompt

Componente que notifica quando nova versão está disponível.

**Localização:** `src/components/pwa/PWAUpdatePrompt.tsx`

**Funcionalidades:**
- Detecta quando novo service worker está disponível
- Exibe banner no topo da tela
- Permite recarregar imediatamente ou mais tarde
- Atualiza app automaticamente ao recarregar

**Design:**
- Posicionado no topo (top-4)
- Background primary (destaque)
- Botão de reload proeminente

### usePWA Hook

Hook customizado para gerenciar funcionalidades PWA.

**Localização:** `src/hooks/usePWA.ts`

**Exporta:**
- `showReloadPrompt`: boolean indicando se há atualização
- `reloadPage()`: função para recarregar e aplicar atualização
- `dismissPrompt()`: função para descartar prompt de atualização

**Uso:**
```typescript
const { showReloadPrompt, reloadPage, dismissPrompt } = usePWA();
```

## Integração no App

Os componentes PWA são integrados no `AppShell`:

```typescript
<AppShell>
  {/* ... conteúdo ... */}

  <PWAUpdatePrompt />
  <PWAInstallPrompt />
</AppShell>
```

## Build e Deploy

### Build de Produção

```bash
npm run build
```

O build gera:
- Service worker em `/dist/sw.js`
- Manifest em `/dist/manifest.webmanifest`
- Workbox precache manifest

### Testes Locais

Para testar PWA localmente:

```bash
npm run build
npm run preview
```

**Importante:** PWA só funciona em produção ou com HTTPS. Use `npm run preview` para testar localmente.

### Verificação PWA

Use as seguintes ferramentas para verificar:

1. **Chrome DevTools**
   - Lighthouse → PWA audit
   - Application → Manifest
   - Application → Service Workers

2. **Online Tools**
   - [web.dev/measure](https://web.dev/measure)
   - [PWA Builder](https://www.pwabuilder.com/)

## Funcionalidades Offline

### Assets Estáticos
Todos os assets estáticos (JS, CSS, HTML, imagens, fontes) são automaticamente precached pelo service worker.

### Google Fonts
Fontes do Google são cacheadas permanentemente (1 ano) usando estratégia CacheFirst.

### API Calls
Chamadas de API usam estratégia NetworkFirst:
- Tenta buscar da rede primeiro
- Fallback para cache se rede falhar
- Timeout de rede de 10s
- Cache expira em 5 minutos

## Melhores Práticas Implementadas

✅ Auto-update de service worker
✅ Cleanup de caches antigos
✅ Estratégias de caching otimizadas por tipo de recurso
✅ Precaching de assets críticos
✅ Prompts de instalação e atualização não-intrusivos
✅ Suporte a múltiplas resoluções de ícones
✅ Atalhos de app para acesso rápido
✅ Suporte a light/dark mode nos prompts
✅ Persistência de dismissals do usuário
✅ Detecção de app já instalado

## Limitações Conhecidas

1. **Service Worker não funciona em desenvolvimento**
   - Por padrão, `devOptions.enabled: false` para não interferir com HMR
   - Para testar, use build de produção

2. **beforeinstallprompt não funciona em iOS**
   - iOS usa seu próprio fluxo de instalação
   - Prompt de instalação não será exibido em Safari iOS
   - Ícones e manifest ainda funcionam

3. **Cache de API é temporário**
   - 5 minutos de expiration para garantir dados atualizados
   - Para offline real, considerar implementar IndexedDB

## Próximos Passos (Opcional)

- [ ] Implementar sincronização em background (Background Sync API)
- [ ] Adicionar push notifications
- [ ] Implementar IndexedDB para cache persistente de dados
- [ ] Adicionar analytics de uso offline
- [ ] Implementar estratégia de cache mais agressiva para imagens

## Referências

- [Vite PWA Plugin](https://vite-pwa-org.netlify.app/)
- [Workbox](https://developers.google.com/web/tools/workbox)
- [MDN: Progressive Web Apps](https://developer.mozilla.org/en-US/docs/Web/Progressive_web_apps)
- [Web.dev: PWA](https://web.dev/progressive-web-apps/)

---

**Última atualização:** Dezembro 2025
**Versão:** 1.0
