# Standards & Current Information Lookup

This project uses Scala 3.8.2, Cats Effect 3, http4s 0.23, and Circe 0.14.

## Mandatory lookup rule

**Before answering any Scala/Cats Effect/http4s question, always check current best practices using the librarian agent with web search and GitHub code search.** Do not rely on pre-training knowledge alone — library APIs, recommended patterns, and conventions change.

## What to look up

### Always check:
- **Official documentation** — Typelevel docs, Scala docs, library-specific docs via Context7
- **GitHub examples** — real usage in established projects (Typelevel ecosystem, http4s, fs2, circe)
- **Current year (2026) content** — prefer recent blog posts, talks, and Stack Overflow answers

### Specifically for:
- Cats Effect: `Ref`, `Resource`, `IO` patterns, concurrent state management
- http4s: route DSL, WebSocket support, middleware, query parameter handling
- Scala 3: package conventions, given/using patterns, enum vs sealed trait, opaque types
- sbt: build configuration, plugins, cross-compilation

## Package lookup

When asked about library usage (e.g., "how do I use FS2 for WebSockets"), always fire a librarian agent to search for:
1. Official library documentation (via Context7)
2. Real code examples from GitHub (via `grep_app_searchGitHub`)
3. Recent blog posts or migration guides

## Why

The Scala/Typelevel ecosystem evolves. Patterns that were standard in 2022 may be outdated in 2026. Automatic lookup ensures answers reflect current best practices, not stale knowledge.
