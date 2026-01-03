# TrueBalance - Accessibility Documentation

## Overview

TrueBalance has been built with accessibility as a core principle, following WCAG 2.1 AA guidelines to ensure the application is usable by everyone, including people with disabilities.

## Accessibility Features

### 1. Keyboard Navigation

#### Skip to Content Link
- **Location**: Top of every page (hidden until focused)
- **Purpose**: Allows keyboard users to bypass navigation and jump directly to main content
- **Usage**: Press Tab on page load to reveal and activate

#### Focus Management
- **Visible Focus Indicators**: All interactive elements have clear, high-contrast focus outlines
- **Focus Color**: Purple (#9333ea) in light mode, lighter purple (#c084fc) in dark mode
- **Focus Offset**: 2px offset for better visibility
- **Keyboard-only**: Focus outlines only appear for keyboard navigation, not mouse clicks

#### Modal Focus Trap
- **Auto-focus**: Modals automatically receive focus when opened
- **Tab Trapping**: Tab and Shift+Tab cycle through modal elements only
- **Escape to Close**: Press Escape key to close modals
- **Focus Restoration**: Focus returns to trigger element when modal closes

### 2. Screen Reader Support

#### ARIA Labels
All icon-only buttons include descriptive aria-labels:
- Navigation buttons: "Voltar" (Back)
- Action buttons: "Editar conta" (Edit account), "Excluir conta" (Delete account)
- Pagination: "Página anterior" (Previous page), "Próxima página" (Next page)
- Theme toggle: "Toggle theme"
- Close buttons: "Fechar" (Close), "Fechar modal" (Close modal)
- Filter toggle: "Mostrar filtros" / "Ocultar filtros" (Show/Hide filters)

#### Form Accessibility
**Input Components (`Input.tsx`, `Select.tsx`)**:
- Proper label association using `htmlFor` and unique IDs
- Error messages linked via `aria-describedby`
- `aria-invalid="true"` when field has error
- `role="alert"` on error messages for immediate announcement
- Required field indicators: Visual asterisk (*) in label

**Example**:
```tsx
<Input
  label="Nome"
  error="Campo obrigatório"
  required
/>
```
Screen reader announces: "Nome, required, edit text, invalid, Campo obrigatório"

#### Loading States
**LoadingSpinner (`LoadingSpinner.tsx`)**:
- `role="status"` to identify as status indicator
- `aria-live="polite"` for non-intrusive announcements
- `aria-label` with customizable message (default: "Carregando...")
- Hidden visual spinner icon (`aria-hidden="true"`)
- Screen reader text via `.sr-only` class

**Button Loading State (`Button.tsx`)**:
- `aria-busy="true"` when loading
- Loading icon hidden from screen readers
- Button text changes to "Carregando..." or similar

#### Modal Dialogs
**Modal (`Modal.tsx`)**:
- `role="dialog"` for proper semantic meaning
- `aria-modal="true"` to indicate modal behavior
- `aria-labelledby` linking to modal title
- `aria-label` fallback if no title provided
- Close button with descriptive `aria-label`

#### Progress and Status
- **ProgressBar**: Full ARIA support with `aria-valuenow`, `aria-valuemin`, `aria-valuemax`
- **Skeleton Loaders**: Include `aria-live="polite"` and `aria-busy="true"`

### 3. Color and Contrast

#### WCAG AA Compliance
All text and interactive elements meet WCAG AA contrast requirements:
- **Normal text**: Minimum 4.5:1 contrast ratio
- **Large text**: Minimum 3:1 contrast ratio
- **Interactive elements**: Minimum 3:1 contrast ratio

#### Color Not Sole Indicator
Information is never conveyed by color alone:
- Success/error states include icons and text
- Status indicators use multiple visual cues
- Charts and graphs include labels and patterns

#### Dark Mode
- Full dark mode support with proper contrast ratios
- Automatic system preference detection
- Manual toggle available
- Focus indicators adjust for dark backgrounds

### 4. Motion and Animation

#### Reduced Motion Support
Respects `prefers-reduced-motion` user preference:
```css
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}
```

### 5. Semantic HTML

#### Proper Document Structure
- `<main>` landmark for primary content
- `<nav>` landmarks for navigation areas
- `<aside>` for sidebar
- Proper heading hierarchy (h1 → h2 → h3)
- Semantic buttons (`<button>`) instead of clickable divs

#### Navigation Landmarks
- **Desktop Sidebar**: `<aside>` with `<nav>`
- **Mobile Bottom Nav**: `<nav>`
- **Main Content**: `<main id="main-content">`
- **Skip Link Target**: Links directly to `#main-content`

### 6. Form Validation and Error Handling

#### Clear Error Messages
- Errors appear immediately below form fields
- `role="alert"` ensures screen reader announcement
- Error text color: Red with sufficient contrast
- Visual error indicators (red border, error icon)

#### Field Requirements
- Required fields marked with asterisk (*)
- `required` attribute on form elements
- Clear validation feedback

### 7. Responsive and Touch Friendly

#### Touch Targets
- Minimum touch target size: 44x44 pixels
- Adequate spacing between interactive elements
- Mobile-optimized navigation

#### Responsive Text
- Base font size: 16px (browser default)
- Relative units (rem, em) for scalability
- Text remains readable at 200% zoom

## Testing Accessibility

### Keyboard Testing
1. **Tab Navigation**: Press Tab to move through all interactive elements
2. **Skip Link**: Press Tab on page load to access skip link
3. **Modal Interaction**: Open modal, verify focus trap and Escape key
4. **Form Navigation**: Tab through form fields, verify labels read correctly

### Screen Reader Testing
Tested with:
- **NVDA** (Windows)
- **JAWS** (Windows)
- **VoiceOver** (macOS/iOS)
- **TalkBack** (Android)

#### Key Test Scenarios
1. Navigate through main pages
2. Fill out and submit forms
3. Interact with modals
4. Use filter and search functionality
5. Review error messages
6. Navigate data tables and charts

### Automated Testing Tools
Recommended tools:
- **axe DevTools** (Browser Extension)
- **Lighthouse** (Chrome DevTools)
- **WAVE** (Web Accessibility Evaluation Tool)
- **Pa11y** (Automated testing)

### Manual Testing Checklist
- [ ] All images have alt text
- [ ] All form fields have labels
- [ ] Color contrast passes WCAG AA
- [ ] Keyboard navigation works without mouse
- [ ] Screen reader announces all content correctly
- [ ] Focus indicators are clearly visible
- [ ] No content only conveyed by color
- [ ] Animations respect reduced motion preference
- [ ] Text is readable at 200% zoom
- [ ] Touch targets are at least 44x44px

## Accessibility Components Reference

### Component ARIA Attributes

| Component | ARIA Attributes | Purpose |
|-----------|----------------|---------|
| Input | `aria-invalid`, `aria-describedby` | Link errors to fields |
| Select | `aria-invalid`, `aria-describedby` | Link errors to fields |
| Button | `aria-busy` | Indicate loading state |
| LoadingSpinner | `role="status"`, `aria-live="polite"`, `aria-label` | Announce loading |
| Modal | `role="dialog"`, `aria-modal`, `aria-labelledby` | Proper dialog semantics |
| ProgressBar | `aria-valuenow`, `aria-valuemin`, `aria-valuemax` | Progress indication |
| Skeleton | `aria-live="polite"`, `aria-busy` | Loading placeholder |

### Utility Classes

```css
/* Screen reader only content */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}

/* Skip to content link */
.skip-to-content {
  position: absolute;
  top: -40px;
  left: 0;
  /* Appears when focused */
}

.skip-to-content:focus {
  top: 0;
}
```

## Known Limitations

1. **Charts and Graphs**: While charts include tooltips and labels, complex data visualizations may require additional context for screen reader users. Consider providing data tables as alternatives.

2. **Real-time Updates**: Some real-time data updates may not be announced immediately. Use ARIA live regions when implementing real-time features.

3. **Third-party Components**: Some chart libraries may have limited accessibility. Always test third-party components thoroughly.

## Future Improvements

- [ ] Add alternative text descriptions for complex charts
- [ ] Implement keyboard shortcuts for common actions
- [ ] Add high contrast mode toggle
- [ ] Improve mobile screen reader experience
- [ ] Add language attribute to HTML elements
- [ ] Implement breadcrumb navigation
- [ ] Add ARIA live regions for dynamic content updates

## Resources

### WCAG Guidelines
- [WCAG 2.1 Quick Reference](https://www.w3.org/WAI/WCAG21/quickref/)
- [ARIA Authoring Practices Guide](https://www.w3.org/WAI/ARIA/apg/)

### Testing Tools
- [axe DevTools](https://www.deque.com/axe/devtools/)
- [Lighthouse](https://developers.google.com/web/tools/lighthouse)
- [WAVE](https://wave.webaim.org/)
- [Color Contrast Checker](https://webaim.org/resources/contrastchecker/)

### Screen Readers
- [NVDA](https://www.nvaccess.org/) (Free, Windows)
- [JAWS](https://www.freedomscientific.com/products/software/jaws/) (Windows)
- VoiceOver (Built-in, macOS/iOS)
- TalkBack (Built-in, Android)

## Contact

If you encounter any accessibility issues or have suggestions for improvements, please:
- Open an issue on GitHub
- Contact the development team
- Submit a pull request with improvements

---

**Last Updated**: December 2024
**WCAG Version**: 2.1 Level AA
**Maintained by**: TrueBalance Development Team
