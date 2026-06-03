# Chess Engine — Frontend

Vite 8 + React 19 + TypeScript 6. Talks to the Scala backend at `localhost:8080`.

## Commands

```sh
npm install        # install dependencies
npm run dev        # dev server at localhost:5173
npm run build      # production build → dist/
npm run preview    # preview production build locally
npm run lint       # ESLint
```

## Type generation

Types in `src/types/api.ts` are generated from `openapi.yaml`:

```sh
npm run gen:types
```

Re-run whenever the backend API changes.

## Prerequisites

Backend must be running on port 8080:

```sh
# from repo root
sbt ~reStart
```
