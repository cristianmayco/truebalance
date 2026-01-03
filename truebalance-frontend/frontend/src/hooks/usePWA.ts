import { useEffect, useState } from 'react';
// @ts-ignore - virtual module
import { useRegisterSW } from 'virtual:pwa-register/react';

export function usePWA() {
  const [showReloadPrompt, setShowReloadPrompt] = useState(false);

  const {
    needRefresh: [needRefresh, setNeedRefresh],
    updateServiceWorker,
  } = useRegisterSW({
    onRegistered(registration: any) {
      console.log('Service Worker registered:', registration);
    },
    onRegisterError(error: any) {
      console.error('Service Worker registration error:', error);
    },
  });

  useEffect(() => {
    if (needRefresh) {
      setShowReloadPrompt(true);
    }
  }, [needRefresh]);

  const reloadPage = () => {
    updateServiceWorker(true);
    setShowReloadPrompt(false);
  };

  const dismissPrompt = () => {
    setNeedRefresh(false);
    setShowReloadPrompt(false);
  };

  return {
    showReloadPrompt,
    reloadPage,
    dismissPrompt,
  };
}
