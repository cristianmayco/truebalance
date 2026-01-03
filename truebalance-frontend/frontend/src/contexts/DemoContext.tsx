import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { isDemoMode, setDemoMode as saveDemoMode } from '../lib/mockData';

interface DemoContextType {
  isDemoMode: boolean;
  enableDemoMode: () => void;
  disableDemoMode: () => void;
  toggleDemoMode: () => void;
}

const DemoContext = createContext<DemoContextType | undefined>(undefined);

interface DemoProviderProps {
  children: ReactNode;
}

/**
 * DemoProvider Component
 *
 * Provides demo mode state and controls to the application.
 * Demo mode uses mock data instead of real API calls.
 *
 * @example
 * ```tsx
 * import { DemoProvider } from '@/contexts/DemoContext';
 *
 * function App() {
 *   return (
 *     <DemoProvider>
 *       <YourApp />
 *     </DemoProvider>
 *   );
 * }
 * ```
 */
export function DemoProvider({ children }: DemoProviderProps) {
  const [demoMode, setDemoModeState] = useState(() => isDemoMode());

  // Sync with localStorage when it changes in another tab
  useEffect(() => {
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === 'truebalance_demo_mode') {
        setDemoModeState(isDemoMode());
      }
    };

    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, []);

  const enableDemoMode = () => {
    saveDemoMode(true);
    setDemoModeState(true);
    // Reload to ensure all data switches to mock
    window.location.reload();
  };

  const disableDemoMode = () => {
    saveDemoMode(false);
    setDemoModeState(false);
    // Reload to switch back to real API
    window.location.reload();
  };

  const toggleDemoMode = () => {
    if (demoMode) {
      disableDemoMode();
    } else {
      enableDemoMode();
    }
  };

  const value = {
    isDemoMode: demoMode,
    enableDemoMode,
    disableDemoMode,
    toggleDemoMode,
  };

  return <DemoContext.Provider value={value}>{children}</DemoContext.Provider>;
}

/**
 * useDemo Hook
 *
 * Access demo mode state and controls.
 *
 * @example
 * ```tsx
 * function MyComponent() {
 *   const { isDemoMode, enableDemoMode, disableDemoMode } = useDemo();
 *
 *   return (
 *     <div>
 *       {isDemoMode && <p>Demo Mode Active</p>}
 *       <button onClick={enableDemoMode}>Enable Demo</button>
 *       <button onClick={disableDemoMode}>Disable Demo</button>
 *     </div>
 *   );
 * }
 * ```
 */
export function useDemo() {
  const context = useContext(DemoContext);

  if (context === undefined) {
    throw new Error('useDemo must be used within a DemoProvider');
  }

  return context;
}
