/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      keyframes: {
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
        'slide-up': {
          '0%': { transform: 'translateY(100%)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
      },
      animation: {
        shimmer: 'shimmer 2s infinite linear',
        'shimmer-reduced': 'shimmer 4s infinite linear',
        'slide-up': 'slide-up 0.4s ease-out',
      },
      colors: {
        primary: {
          50: '#faf5ff',
          100: '#f3e8ff',
          200: '#e9d5ff',
          300: '#d8b4fe',
          400: '#c084fc',
          500: '#a855f7',
          600: '#9333ea',
          700: '#7e22ce',
          800: '#6b21a8',
          900: '#581c87',
        },
        success: {
          light: '#86efac',
          DEFAULT: '#22c55e',
          dark: '#16a34a',
        },
        warning: {
          light: '#fde047',
          DEFAULT: '#eab308',
          dark: '#ca8a04',
        },
        error: {
          light: '#fca5a5',
          DEFAULT: '#ef4444',
          dark: '#dc2626',
        },
        info: {
          light: '#93c5fd',
          DEFAULT: '#3b82f6',
          dark: '#2563eb',
        },
      },
    },
  },
  plugins: [],
}
