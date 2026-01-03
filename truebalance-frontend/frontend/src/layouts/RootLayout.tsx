import { Outlet } from 'react-router-dom';
import { AppShell } from '@/components/layout/AppShell';
import { DemoBanner } from '@/components/demo/DemoBanner';

/**
 * RootLayout Component
 *
 * Main layout wrapper for all application routes.
 * Includes AppShell (sidebar, topbar, bottom nav) and DemoBanner.
 *
 * Uses <Outlet /> to render child routes.
 */
export function RootLayout() {
  return (
    <>
      <DemoBanner />
      <AppShell>
        <Outlet />
      </AppShell>
    </>
  );
}
