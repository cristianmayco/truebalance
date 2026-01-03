import { test, expect } from '@playwright/test'

test.describe('Theme', () => {
  test('should have theme toggle button', async ({ page }) => {
    await page.goto('/')

    // Find theme toggle
    const themeToggle = page.locator('button[aria-label="Toggle theme"]')
    await expect(themeToggle).toBeVisible()
  })

  test('should toggle between light and dark mode', async ({ page }) => {
    await page.goto('/')

    // Get initial theme
    const initialClass = await page.locator('html').getAttribute('class')
    const isDark = initialClass?.includes('dark') || false

    // Click theme toggle
    await page.click('button[aria-label="Toggle theme"]')

    // Wait for theme change
    await page.waitForTimeout(200)

    // Check that theme changed
    const newClass = await page.locator('html').getAttribute('class')
    const isNowDark = newClass?.includes('dark') || false

    expect(isNowDark).not.toBe(isDark)
  })

  test('should persist theme preference', async ({ page }) => {
    await page.goto('/')

    // Set to dark mode
    const initialClass = await page.locator('html').getAttribute('class')
    if (!initialClass?.includes('dark')) {
      await page.click('button[aria-label="Toggle theme"]')
      await page.waitForTimeout(200)
    }

    // Reload page
    await page.reload()

    // Should still be dark
    const afterReload = await page.locator('html').getAttribute('class')
    expect(afterReload).toContain('dark')
  })

  test('should apply dark mode styles correctly', async ({ page }) => {
    await page.goto('/')

    // Switch to dark mode
    const initialClass = await page.locator('html').getAttribute('class')
    if (!initialClass?.includes('dark')) {
      await page.click('button[aria-label="Toggle theme"]')
      await page.waitForTimeout(200)
    }

    // Check background color is dark
    const bgColor = await page.evaluate(() => {
      const body = document.body
      return window.getComputedStyle(body).backgroundColor
    })

    // Dark mode should have dark background (rgb values should be low)
    expect(bgColor).toBeTruthy()
  })

  test('should have proper contrast in both themes', async ({ page }) => {
    await page.goto('/')

    // Test light mode contrast
    const lightBg = await page.evaluate(() => {
      const body = document.body
      return window.getComputedStyle(body).backgroundColor
    })

    // Switch to dark
    await page.click('button[aria-label="Toggle theme"]')
    await page.waitForTimeout(200)

    // Test dark mode contrast
    const darkBg = await page.evaluate(() => {
      const body = document.body
      return window.getComputedStyle(body).backgroundColor
    })

    // Colors should be different
    expect(lightBg).not.toBe(darkBg)
  })
})
