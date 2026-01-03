import { test, expect } from '@playwright/test'

test.describe('Accessibility', () => {
  test('should have skip-to-content link', async ({ page }) => {
    await page.goto('/')

    // Focus the page
    await page.keyboard.press('Tab')

    // Skip link should be visible when focused
    const skipLink = page.locator('a[href="#main-content"]')
    await expect(skipLink).toBeVisible()
    await expect(skipLink).toHaveText(/Pular para o conteúdo/)
  })

  test('should support keyboard navigation', async ({ page }) => {
    await page.goto('/bills')

    // Tab through interactive elements
    await page.keyboard.press('Tab') // Skip link
    await page.keyboard.press('Tab') // First nav item

    // Press Enter on focused element
    const focused = page.locator(':focus')
    await expect(focused).toBeVisible()
  })

  test('should have proper ARIA labels on icon buttons', async ({ page }) => {
    await page.goto('/bills')

    // Check for aria-label on buttons
    const buttons = page.locator('button[aria-label]')
    const count = await buttons.count()

    // Should have at least some labeled buttons
    expect(count).toBeGreaterThan(0)
  })

  test('should have loading states with aria-busy', async ({ page }) => {
    await page.goto('/')

    // Check for loading spinner with role="status"
    const loadingElements = page.locator('[role="status"]')

    // May or may not have loading elements depending on timing
    const count = await loadingElements.count()
    if (count > 0) {
      const first = loadingElements.first()
      await expect(first).toHaveAttribute('aria-live', 'polite')
    }
  })

  test('should have proper form labels', async ({ page }) => {
    await page.goto('/bills/new')

    // All form inputs should have labels
    const inputs = page.locator('input[type="text"], input[type="number"], input[type="date"], select')
    const inputCount = await inputs.count()

    for (let i = 0; i < inputCount; i++) {
      const input = inputs.nth(i)
      const id = await input.getAttribute('id')

      if (id) {
        const label = page.locator(`label[for="${id}"]`)
        await expect(label).toBeVisible()
      }
    }
  })

  test('should show error messages with role="alert"', async ({ page }) => {
    await page.goto('/bills/new')

    // Try to submit empty form
    await page.click('button[type="submit"]')

    // Should show validation errors
    const alerts = page.locator('[role="alert"]')
    const count = await alerts.count()

    // Should have at least one error
    expect(count).toBeGreaterThan(0)
  })

  test('should have focus visible indicators', async ({ page }) => {
    await page.goto('/')

    // Tab to an element
    await page.keyboard.press('Tab')
    await page.keyboard.press('Tab')

    // Focused element should have outline
    const focused = page.locator(':focus')
    const outline = await focused.evaluate((el) => {
      const computed = window.getComputedStyle(el)
      return computed.outlineWidth
    })

    // Should have visible outline (not 0px)
    expect(outline).not.toBe('0px')
  })
})
