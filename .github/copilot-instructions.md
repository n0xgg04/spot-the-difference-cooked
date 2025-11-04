<!--
This file is generated automatically by an AI assistant because the workspace
contained no discoverable project files on 2025-10-25. It is intended to
give concrete, repository-specific guidance for AI coding agents. Update this
file manually when the repository gains source files so guidance can be
populated with real examples discovered in the tree.
-->

# Copilot / AI agent quick start for this repository

This repository appears empty at c:\Users\PC\OneDrive\Desktop\game (no files
detected as of 2025-10-25). The guidance below tells an AI agent exactly what
to do to become productive once project files are added, and what evidence to
look for when inferring the project's architecture, workflows and conventions.

1. Confirm repo state

   - If the workspace is empty, report: "no source files found" and ask the
     user whether they intended to work in a different folder or to push code.

2. Primary discovery checklist (run in repository root)

   - Look for language/build manifests in this order:
     - `package.json`, `pnpm-lock.yaml`, `yarn.lock` (Node.js)
     - `pyproject.toml`, `requirements.txt`, `setup.py` (Python)
     - `go.mod` (Go)
     - `Cargo.toml` (Rust)
     - `Makefile`, `Dockerfile`, `README.md`, `.github/workflows/`
   - If you find one of these files, record it verbatim in your analysis and
     follow the specific commands listed below for build/test commands.

3. How to infer architecture and intent (what to look for)

   - Monorepo vs single package: multiple top-level language manifests (e.g.
     more than one `package.json` or a `workspaces` key) means monorepo.
   - Service boundaries: directories named `api`, `server`, `backend`,
     `client`, `ui`, `worker` usually map to components. Look for `Dockerfile`
     and `.github/workflows` that build specific folders.
   - Data flow hints: presence of `docker-compose.yml`, `k8s/`, `migrations/`,
     or config files containing DB connection strings (`DATABASE_URL`) imply
     persistence and integration points.
   - Integrations: search for `aws`, `gcp`, `azure`, `rabbitmq`, `kafka`,
     `sqs`, `stripe`, `auth0` keywords to surface external dependencies.

4. Concrete, reproducible developer workflows

   - Node.js (if `package.json` exists):
     ```powershell
     npm install
     npm test
     npm run build
     ```
   - Python (if `pyproject.toml`/`requirements.txt` exists):
     ```powershell
     python -m venv .venv; .\.venv\Scripts\Activate.ps1; pip install -r requirements.txt; pytest -q
     ```
   - Docker-based: if `Dockerfile` or `docker-compose.yml` found:
     ```powershell
     docker build -t repo-image .
     docker-compose up --build
     ```
   - CI: inspect `.github/workflows/*` to see test/build matrix and mirror it
     locally if necessary.

5. Project-specific conventions to surface (fill when files present)

   - Note any non-standard folder names or patterns and add examples, e.g.
     "This repo keeps services in `services/<name>/` and shared code in
     `libs/<name>/` — use relative imports `@libs/...` when editing." Replace
     this section with discovered conventions.

6. What to include in a code-change suggestion

   - One-line summary of change and the target file(s).
   - Minimal, self-contained edit that preserves local style. Prefer small
     focused patches. Reference the exact file path and function/class name.
   - When adding tests, place them adjacent to the changed code following the
     repo pattern (e.g. `__tests__`, `tests/`, or `*.spec.ts`).

7. When you cannot find evidence

   - Stop and ask the user one concise question: e.g. "I couldn't find source
     code in the workspace — are you working in a different folder or should I
     initialize a new project here?"

8. Update this file after discovery
   - Replace the top section with a summarized, concrete checklist of files
     and commands actually found. Keep the guidance short (20–50 lines).

If you'd like, I can populate specific examples after you add project files or
point me at the intended project directory. What should I do next?
