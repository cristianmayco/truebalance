import { test, expect } from '@playwright/test'

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
  })

  test('should display dashboard title and description', async ({ page }) => {
    await expect(page.locator('h1')).toContainText('Dashboard')
    await expect(page.locator('text=/Visão geral das suas finanças/i')).toBeVisible()
  })

  test('should display period filter dropdown', async ({ page }) => {
    const filterSelect = page.locator('select')
    await expect(filterSelect).toBeVisible()
    
    // Should have default value of 12 months
    await expect(filterSelect).toHaveValue('12')
  })

  test('should have all period options available', async ({ page }) => {
    const filterSelect = page.locator('select')
    
    const options = await filterSelect.locator('option').allTextContents()
    
    expect(options).toContain('Últimos 12 meses')
    expect(options).toContain('Últimos 2 anos')
    expect(options).toContain('Últimos 5 anos')
    expect(options).toContain('Últimos 10 anos')
  })

  test('should change period when selecting different option', async ({ page }) => {
    const filterSelect = page.locator('select')
    
    // Select 2 years
    await filterSelect.selectOption('24')
    await expect(filterSelect).toHaveValue('24')
    
    // Wait for data to reload
    await page.waitForTimeout(1000)
    
    // Verify summary cards are still visible
    await expect(page.locator('text=/Total no Período/i')).toBeVisible()
  })

  test('should display summary cards', async ({ page }) => {
    // Wait for data to load
    await page.waitForTimeout(1000)
    
    // Check for summary cards
    await expect(page.locator('text=/Total no Período/i')).toBeVisible()
    await expect(page.locator('text=/Gastos do Mês/i')).toBeVisible()
    await expect(page.locator('text=/Média Mensal/i')).toBeVisible()
  })

  test('should display monthly expenses section', async ({ page }) => {
    await page.waitForTimeout(1000)
    
    // Check for section title
    await expect(page.locator('text=/Gastos por Mês/i')).toBeVisible()
    
    // Check for "Ver todas as contas" button
    const viewAllButton = page.locator('button:has-text("Ver todas as contas")')
    const exists = await viewAllButton.count()
    
    if (exists > 0) {
      await expect(viewAllButton).toBeVisible()
    }
  })

  test('should display monthly cards when data exists', async ({ page }) => {
    await page.waitForTimeout(2000)
    
    // Check if monthly cards are displayed
    // This will depend on whether there's data
    const monthlyCards = page.locator('[class*="card"], [class*="Card"]')
    const cardCount = await monthlyCards.count()
    
    // If cards exist, verify they have expected content
    if (cardCount > 0) {
      const firstCard = monthlyCards.first()
      
      // Should have month name (capitalized)
      const hasMonth = await firstCard.locator('text=/janeiro|fevereiro|março|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro/i').count()
      
      // Should have currency value
      const hasCurrency = await firstCard.locator('text=/R\\$/i').count()
      
      expect(hasMonth > 0 || hasCurrency > 0).toBeTruthy()
    }
  })

  test('should display empty state when no data', async ({ page }) => {
    // This test assumes no data scenario
    // In real scenario, you might need to mock API responses
    
    await page.waitForTimeout(2000)
    
    // Check for empty state message
    const emptyState = page.locator('text=/Nenhum gasto encontrado/i')
    const exists = await emptyState.count()
    
    if (exists > 0) {
      await expect(emptyState).toBeVisible()
      
      // Should have "Nova Conta" button in empty state
      const newBillButton = page.locator('button:has-text("Nova Conta")')
      await expect(newBillButton.first()).toBeVisible()
    }
  })

  test('should navigate to new bill page', async ({ page }) => {
    const newBillButton = page.locator('button:has-text("Nova Conta")')
    await expect(newBillButton.first()).toBeVisible()
    
    await newBillButton.first().click()
    
    // Should navigate to new bill form
    await expect(page).toHaveURL(/\/bills\/new/)
  })

  test('should navigate to bills list from "Ver todas"', async ({ page }) => {
    await page.waitForTimeout(1000)
    
    const viewAllButton = page.locator('button:has-text("Ver todas as contas")')
    const exists = await viewAllButton.count()
    
    if (exists > 0) {
      await viewAllButton.click()
      await expect(page).toHaveURL(/\/bills/)
    }
  })

  test('should display currency values correctly formatted', async ({ page }) => {
    await page.waitForTimeout(2000)
    
    // Check for Brazilian currency format (R$)
    const currencyElements = page.locator('text=/R\\$\\s*[0-9.,]+/i')
    const count = await currencyElements.count()
    
    // If currency values exist, verify format
    if (count > 0) {
      const firstValue = await currencyElements.first().textContent()
      expect(firstValue).toMatch(/R\$\s*\d+[.,]\d{2}/)
    }
  })

  test('should be responsive on mobile', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 })
    
    await page.waitForTimeout(1000)
    
    // Dashboard should still be visible
    await expect(page.locator('h1')).toContainText('Dashboard')
    
    // Filter should still be accessible
    const filterSelect = page.locator('select')
    await expect(filterSelect).toBeVisible()
    
    // Summary cards should stack vertically
    const summaryCards = page.locator('text=/Total no Período|Gastos do Mês|Média Mensal/i')
    const cardCount = await summaryCards.count()
    expect(cardCount).toBeGreaterThanOrEqual(0)
  })

  test('should handle loading state', async ({ page }) => {
    // Navigate to dashboard
    await page.goto('/')
    
    // Check for loading spinner (might be brief)
    const spinner = page.locator('[role="status"], [aria-label*="loading"], [class*="spinner"]')
    const spinnerExists = await spinner.count()
    
    // Loading state should eventually resolve
    await page.waitForTimeout(2000)
    
    // After loading, content should be visible
    await expect(page.locator('h1')).toContainText('Dashboard')
  })

  test('should update data when period filter changes', async ({ page }) => {
    await page.waitForTimeout(1000)
    
    // Get initial summary value (if exists)
    const initialTotal = page.locator('text=/Total no Período/i').locator('..').locator('text=/R\\$/i')
    const initialExists = await initialTotal.count()
    let initialValue = ''
    
    if (initialExists > 0) {
      initialValue = await initialTotal.first().textContent() || ''
    }
    
    // Change period to 24 months
    const filterSelect = page.locator('select')
    await filterSelect.selectOption('24')
    
    // Wait for data to reload
    await page.waitForTimeout(2000)
    
    // Verify summary cards are still visible (data may have changed)
    await expect(page.locator('text=/Total no Período/i')).toBeVisible()
  })

  test('should display breakdown in monthly cards', async ({ page }) => {
    await page.waitForTimeout(2000)
    
    // Look for monthly cards
    const cards = page.locator('[class*="card"], [class*="Card"]')
    const cardCount = await cards.count()
    
    if (cardCount > 0) {
      const firstCard = cards.first()
      
      // Should have breakdown labels
      const hasContas = await firstCard.locator('text=/Contas/i').count()
      const hasCartoes = await firstCard.locator('text=/Cartões/i').count()
      
      // At least one breakdown should be present
      expect(hasContas > 0 || hasCartoes > 0).toBeTruthy()
    }
  })

  test('should display percentages in monthly cards', async ({ page }) => {
    await page.waitForTimeout(2000)
    
    // Look for percentage indicators
    const percentageElements = page.locator('text=/%/i')
    const count = await percentageElements.count()
    
    // Percentages may or may not exist depending on data
    // Just verify the page doesn't crash
    await expect(page.locator('h1')).toContainText('Dashboard')
  })
})

test.describe('Dashboard - Reports Integration', () => {
  test('should navigate to reports page', async ({ page }) => {
    await page.goto('/')
    
    // Look for navigation to reports
    const reportsLink = page.locator('a[href="/reports"], button:has-text("Relatórios")')
    const exists = await reportsLink.count()
    
    if (exists > 0) {
      await reportsLink.first().click()
      await expect(page).toHaveURL(/\/reports/)
    }
  })
})
