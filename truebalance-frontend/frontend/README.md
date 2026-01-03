# TrueBalance Frontend

Modern React frontend built with Vite, TypeScript, and Tailwind CSS.

## Tech Stack

- React 18
- TypeScript 5
- Vite 6
- Tailwind CSS 3
- Docker (Development)

## Development

### Local Development (without Docker)

1. Install dependencies:
   ```bash
   npm install
   ```

2. Start dev server:
   ```bash
   npm run dev
   ```

3. Open http://localhost:3000

### Docker Development

1. Build and start container:
   ```bash
   docker-compose -f docker-compose.dev.yml up --build
   ```

2. Open http://localhost:3000

3. Stop container:
   ```bash
   docker-compose -f docker-compose.dev.yml down
   ```

## Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run lint` - Run ESLint
- `npm run format` - Format code with Prettier
- `npm run preview` - Preview production build

## Project Structure

```
frontend/
├── public/          # Static assets
├── src/
│   ├── assets/      # Images, fonts, etc.
│   ├── components/  # React components
│   ├── App.tsx      # Main app component
│   ├── main.tsx     # React entry point
│   └── index.css    # Global styles + Tailwind
└── ...config files
```
