import { test, expect } from '@playwright/test'

test.describe('Navigation', () => {
  test('should navigate between pages', async ({ page }) => {
    await page.goto('/')

    // Should be on dashboard
    await expect(page).toHaveTitle(/TrueBalance/)

    // Navigate to Bills
    await page.click('text=Contas')
    await expect(page).toHaveURL(/\/bills/)
    await expect(page.locator('h1')).toContainText('Contas')

    // Navigate to Credit Cards
    await page.click('text=Cartões')
    await expect(page).toHaveURL(/\/credit-cards/)
    await expect(page.locator('h1')).toContainText('Cartões')

    // Navigate to Reports
    await page.click('text=Relatórios')
    await expect(page).toHaveURL(/\/reports/)
    await expect(page.locator('h1')).toContainText('Relatórios')

    // Navigate to Consolidated
    await page.click('text=Consolidado')
    await expect(page).toHaveURL(/\/consolidated/)
  })

  test('should show active navigation state', async ({ page }) => {
    await page.goto('/bills')

    // Bills nav should be active
    const billsNav = page.locator('nav a[href="/bills"]')
    await expect(billsNav).toHaveClass(/bg-primary/)
  })

  test('should have working back navigation', async ({ page }) => {
    await page.goto('/bills')
    await page.goto('/credit-cards')

    // Go back
    await page.goBack()
    await expect(page).toHaveURL(/\/bills/)

    // Go forward
    await page.goForward()
    await expect(page).toHaveURL(/\/credit-cards/)
  })
})
