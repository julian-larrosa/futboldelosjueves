---
name: spring-boot-api
description: Convenciones de la API REST FDLJ (Spring Boot 4.1 / Java 21 / JPA / JWT). Controller -> service -> repository -> entity, DTOs record, mappers, excepciones y seguridad. Seguir los módulos existentes; no imponer arquitectura nueva.
license: MIT
metadata:
  stack: spring-boot-4-java-21
---

# Spring Boot API (convenciones FDLJ)

## Contexto
- Spring Boot 4.1.0, Java 21, Maven, PostgreSQL, JPA/Hibernate, Security + JWT (jjwt 0.13.0), springdoc 3.0.2, Lombok.
- Jackson 3: usar `tools.jackson.databind.json.JsonMapper` (NO `com.fasterxml.jackson`).
- Paquete raíz: `com.fdlj.fdlj`. Starter web: `spring-boot-starter-webmvc`.

## Capas (fijas)
controller -> service (interfaz en `service`, impl en `service.impl`) -> repository -> entity
- Directorios: `dto/request`, `dto/response`, `mapper`, `config`, `exception`, `security`, `entity/enums`.

## Controller
- `@RestController`, `@RequestMapping("/api/<recurso>")`, `@RequiredArgsConstructor`.
- Endpoints protegidos: `@SecurityRequirement(name = "bearerAuth")`.
- Retornar `ResponseEntity<ApiResponse<T>>`: `ApiResponse.created(data)` (201) u `ApiResponse.ok(data)` (200); DELETE -> `ResponseEntity.status(HttpStatus.NO_CONTENT).build()`.
- `@Valid @RequestBody` en create/update; nunca recibir entidades.
- Swagger por endpoint: `@Operation(summary, description)` en español + `@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = SwaggerConstants.X, description = "...")`. Usar constantes de `config.SwaggerConstants` (200/201/204/400/401/403/404/409/500).
- Paginación: `PagedResponse<T>`; `@RequestParam` `page` (0), `size` (10), `sort` (`"prop:asc"` por defecto); construir `PageRequest.of(page, size, Sort.by(dir, property))` parseando `sort.split(":")`.

## Service
- Interfaz en `service`; impl en `service.impl` con `@Service @RequiredArgsConstructor @Slf4j`.
- Escrituras: `@Transactional`. Lecturas: `@Transactional(readOnly = true)`.
- Logs `log.info(...)` en español.
- Lanzar excepciones del paquete `exception`; no filtrar excepciones JPA.

## Repository
- `interface XRepository extends JpaRepository<Entity, Long>`.
- Métodos derivados estilo existente: `findByActivoTrue`, `findByIdAndActivoTrue`, `existsByEmailAndActivoTrueAndIdNot`.
- Búsquedas: JPQL `@Query` con `@Param` y filtros LIKE null-safe `(:p IS NULL OR ...)`.

## Entity
- `@Entity @Table(name = "<plural_snake_case>")`, `@Getter @Setter @NoArgsConstructor`.
- `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`.
- `@Column(name = "...", nullable = ..., length = ...)` explícito; enums con `@Enumerated(EnumType.STRING)`.
- Soft delete: `boolean activo` con default `= true`.
- Colecciones: `Set`, `mappedBy`, `cascade = CascadeType.ALL, orphanRemoval = true`, `@OrderBy` cuando aplique.

## DTO
- Records de Java. Request: validación Jakarta (`@NotBlank`, `@Email`, `@Size`, `@NotNull`) con mensajes en español.
- Response: records envueltos en `ApiResponse<T>`; listas paginadas en `PagedResponse<T>`. Nunca exponer entidades.

## Mapper
- `@Component` en `mapper`; métodos `toEntity(request)`, `toResponse(entity)` y helpers de normalización (ej. `normalizeEmail` -> trim + lowercase).
- Usar repositorios solo si el mapeo requiere lookup adicional.

## Excepciones
- Excepciones custom en `exception` (`ResourceNotFoundException`, `ResourceAlreadyExistsException`, `InvalidCredentialsException`, `InvalidMatchStateException`).
- Registrar handler en `GlobalExceptionHandler` (`@RestControllerAdvice @Slf4j`) devolviendo `ErrorResponse.of(HttpStatus.X, "mensaje en español")`. Status usados: 400, 401, 404, 409.

## Seguridad
- `config/SecurityConfig`: JWT stateless; permitAll: `/api/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`; resto autenticado.

## Reglas
- Antes de crear un módulo nuevo, copiar el patrón de uno existente (ej. `PlayerController` / `PlayerServiceImpl` / `PlayerMapper` / `PlayerRepository`).
- Mensajes y descripciones en español.
- NO agregar arquitectura nueva, dependencias nuevas ni cambiar la política de seguridad sin preguntar.