---
name: java-testing
description: Convenciones de testing FDLJ (JUnit 5 + MockMvc, integration tests contra PostgreSQL fdlj_test). Reutilizar IntegrationTestBase, naming de tests, y correr la SUITE COMPLETA antes de dar una fase por terminada. No romper tests existentes.
license: MIT
metadata:
  framework: junit5-mockmvc
---

# Java Testing (FDLJ)

## Contexto
- Todos los tests son integration tests: `@SpringBootTest @AutoConfigureMockMvc @Transactional @ActiveProfiles("test")`.
- Corren contra PostgreSQL dedicada `fdlj_test` (localhost:5432, user/pass fdlj/fdlj, `ddl-auto=create-drop`).
- Levantar Postgres antes: `docker compose up -d`.
- JSON: Jackson 3 -> `tools.jackson.databind.json.JsonMapper` y `JsonNode`.

## Correr la suite
- Windows: `.\mvnw.cmd test` (equivalente: `mvn test`).
- La suite COMPLETA debe pasar ANTES de dar una fase por terminada.
- Si algo falla: arreglar la regresión; NO borrar ni debilitar tests existentes.

## Estructura
- Clases en `src/test/java/com/fdlj/fdlj/controller/*Test.java`.
- Extender `com.fdlj.fdlj.IntegrationTestBase` para reutilizar helpers de setup:
  - `adminToken()`, `registerPlayer(nombre)`, `createPlayer(nombre)`, `createMatch(token)`;
  - ciclo de vida: `openConvocatoria`, `closeConvocatoria`, `reopenConvocatoria`, `convocar`, `generateTeams`, `startMatch`, `updateStats`, `finishMatch`, `submitAttributeRatings`;
  - agregados: `setupFinishedMatch10(token)`, `setupFinishedMatchForRating(token)`;
  - utilidades: `bearer(token)`, `objectMapper`, `mockMvc`.
- No duplicar setup que ya está en la base.

## Escribir tests
- Naming: `metodo_escenario_resultado` (ej. `register_duplicateEmail_returns409`, `fullMatchLifecycle`).
- Autenticar siempre: `.header("Authorization", bearer(token))`.
- Datos únicos: emails/usernames con `UUID.randomUUID()`.
- Assert de status con `status().isX()` y payload con `jsonPath("$.data...")`.
- Serializar request records con `objectMapper.writeValueAsString(new XRequest(...))`.
- Cubrir éxito y errores (400/401/404/409) para cada endpoint nuevo.

## Reglas (no negociables)
- NO modificar tests existentes salvo que el requisito cambie su comportamiento.
- NO usar `@Disabled`, ni achicar alcances, ni quitar assertions.
- Un endpoint de producción sin tests NO es una fase terminada.
- Terminar toda fase corriendo la suite completa: `.\mvnw.cmd test`.