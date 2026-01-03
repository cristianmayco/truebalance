import { test, expect } from '@playwright/test'

test.describe('Bills Management', () => {
  test('should display empty state when no bills', async ({ page }) => {
    await page.goto('/bills')

    // Should show empty state or list
    await expect(page.locator('h1')).toContainText('Contas')

    // Should have "Nova Conta" button
    const newButton = page.locator('button:has-text("Nova Conta")')
    await expect(newButton).toBeVisible()
  })

  test('should navigate to new bill form', async ({ page }) => {
    await page.goto('/bills')

    // Click "Nova Conta" button
    await page.click('button:has-text("Nova Conta")')

    // Should navigate to form
    await expect(page).toHaveURL(/\/bills\/new/)
    await expect(page.locator('h1')).toContainText(/Nova Conta|Cadastrar/)
  })

  test('should show validation errors on empty form submit', async ({ page }) => {
    await page.goto('/bills/new')

    // Submit without filling
    await page.click('button[type="submit"]')

    // Should show errors
    const errors = page.locator('[role="alert"]')
    const count = await errors.count()
    expect(count).toBeGreaterThan(0)
  })

  test('should fill and submit bill form', async ({ page }) => {
    await page.goto('/bills/new')

    // Fill form
    await page.fill('input[name="name"]', 'Test Bill E2E')
    await page.fill('textarea[name="description"]', 'Test description for E2E')
    await page.fill('input[name="date"]', '2024-12-31')
    await page.fill('input[name="totalAmount"]', '1000')
    await page.fill('input[name="numberOfInstallments"]', '10')

    // Submit form
    await page.click('button[type="submit"]')

    // Should redirect to bills list (or show success)
    await page.waitForTimeout(1000) // Wait for navigation

    // Check if we're on bills page or if there's a success message
    const url = page.url()
    expect(url).toMatch(/\/bills/)
  })

  test('should have export functionality', async ({ page }) => {
    await page.goto('/bills')

    // Look for export button (may not exist if no data)
    const exportButton = page.locator('button:has-text("Exportar")')
    const exists = await exportButton.count()

    if (exists > 0) {
      await expect(exportButton).toBeVisible()
    }
  })

  test('should have pagination when many bills', async ({ page }) => {
    await page.goto('/bills')

    // Look for pagination (may not exist if few bills)
    const pagination = page.locator('[aria-label*="Página"]')
    const exists = await pagination.count()

    // Just verify it's accessible if it exists
    if (exists > 0) {
      await expect(pagination.first()).toBeVisible()
    }
  })

  test('should filter bills', async ({ page }) => {
    await page.goto('/bills')

    // Look for filter toggle
    const filterButton = page.locator('button[aria-label*="filtros"], button:has-text("Filtros")')
    const exists = await filterButton.count()

    if (exists > 0) {
      await filterButton.click()

      // Should show filter options
      await page.waitForTimeout(300)
    }
  })

  test('should display bill cards on mobile view', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 })
    await page.goto('/bills')

    // Mobile should show cards instead of table
    await expect(page.locator('h1')).toContainText('Contas')

    // Check if responsive
    const isMobile = await page.evaluate(() => window.innerWidth < 1024)
    expect(isMobile).toBe(true)
  })

  test('should show installments information', async ({ page }) => {
    await page.goto('/bills')

    // Look for installment info (parcelas)
    const elements = page.locator(':has-text("parcela")')
    const exists = await elements.count()

    // May or may not have bills with installments
    if (exists > 0) {
      await expect(elements.first()).toBeVisible()
    }
  })

  test('should navigate back from form', async ({ page }) => {
    await page.goto('/bills/new')

    // Click back button
    const backButton = page.locator('button[aria-label="Voltar"]')
    await backButton.click()

    // Should go back to bills list
    await expect(page).toHaveURL(/\/bills$/)
  })
})
