package com.fdlj.fdlj.security;

import com.fdlj.fdlj.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting simple en memoria (ventana fija) para los endpoints de autenticacion.
 * No se registra como bean de servlet (se instancia en SecurityConfig) para evitar
 * que corra dos veces (contenedor + cadena de seguridad).
 */
public class AuthRateLimitFilter extends OncePerRequestFilter {

	private static final String AUTH_PATH_PREFIX = "/api/auth/";

	private final JsonMapper objectMapper;
	private final boolean enabled;
	private final int maxRequests;
	private final long windowSeconds;

	private final Map<String, Window> windows = new ConcurrentHashMap<>();

	private record Window(long bucket, int count) {
	}

	public AuthRateLimitFilter(JsonMapper objectMapper, boolean enabled, int maxRequests, long windowSeconds) {
		this.objectMapper = objectMapper;
		this.enabled = enabled;
		this.maxRequests = maxRequests;
		this.windowSeconds = windowSeconds;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !enabled || !request.getRequestURI().startsWith(AUTH_PATH_PREFIX);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String key = request.getRemoteAddr();
		long bucket = Instant.now().getEpochSecond() / windowSeconds;
		Window window = windows.compute(key, (k, current) ->
				(current == null || current.bucket() != bucket)
						? new Window(bucket, 1)
						: new Window(bucket, current.count() + 1));
		if (window.count() > maxRequests) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.getWriter().write(objectMapper.writeValueAsString(
					ErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS,
							"Demasiadas solicitudes. Intente nuevamente más tarde.")));
			return;
		}
		filterChain.doFilter(request, response);
	}
}