package aldra.api.framework.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;

@Slf4j
public class CognitoAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

  public CognitoAuthenticationFilter(String pathPattern, String httpMethod) {
    super(createRequestMatcher(pathPattern, httpMethod));
  }

  private static RequestMatcher createRequestMatcher(String pathPattern, String httpMethod) {
    return request -> {
      boolean methodMatches = httpMethod == null || httpMethod.equals(request.getMethod());
      boolean pathMatches = new AntPathMatcher().match(pathPattern, request.getServletPath());
      return methodMatches && pathMatches;
    };
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, IOException {
    val mapper = new ObjectMapper();
    val dto = mapper.readValue(request.getInputStream(), LoginRequest.class);
    val token = new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword());
    token.setDetails(authenticationDetailsSource.buildDetails(request));
    return getAuthenticationManager().authenticate(token);
  }
}
