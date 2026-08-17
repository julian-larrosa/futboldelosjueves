---
name: git-github-workflow
description: Flujo Git/GitHub de FDLJ: rama de fase -> implementación -> suite completa de tests -> commit -> push -> PR -> merge a main vía PR. Nunca commitear ni mergear directo a main.
license: MIT
metadata:
  workflow: feature-branch-pr
---

# Git/GitHub Workflow (FDLJ)

## Repo
- Remote: `origin https://github.com/julian-larrosa/futboldelosjueves.git`.
- Rama protegida de destino: `main`.

## Flujo (en orden)
1. Partir de un main actualizado: `git checkout main` y `git pull`.
2. Crear rama de fase: `git checkout -b fase-<N>-<descripcion>` (ej. `fase-12-player-attributes`). Coincide con el historial (`fase-11-testing`, `fase-12`).
3. Implementar la fase en la rama.
4. Correr la suite completa de tests (ver skill java-testing): `.\mvnw.cmd test` — todo verde antes de commitear.
5. Commitear con Conventional Commits acorde al historial:
   - `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:` y opcionalmente con scope: `feat(auth): ...`.
   - Ej.: `feat: complete match lifecycle`, `fix: update player ratings`.
6. Push de la rama: `git push -u origin <rama>`.
7. Abrir PR a `main` (web de GitHub o `gh pr create --base main`) describiendo la fase y su cobertura de tests.
8. Merge a `main` SOLO vía PR (merge commit, como en el historial: "Merge pull request #N from ...").

## Restricciones
- NUNCA commitear directo a `main`.
- NUNCA mergear a `main` desde CLI (`git merge`, `git push origin main`) — el merge ocurre solo por PR.
- NUNCA force-push ni amend de commits ya pusheados.
- Un PR = una fase; evitar cambios mezclados.
- Después del merge, eliminar la rama de fase.