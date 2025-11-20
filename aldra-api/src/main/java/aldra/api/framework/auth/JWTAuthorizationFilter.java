package aldra.api.framework.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;

@Slf4j
public class JWTAuthorizationFilter extends AbstractPreAuthenticatedProcessingFilter {

  public JWTAuthorizationFilter(String pathPattern) {
    super();
    setRequiresAuthenticationRequestMatcher(createRequestMatcher(pathPattern));
  }

  private static RequestMatcher createRequestMatcher(String pathPattern) {
    return request -> new AntPathMatcher().match(pathPattern, request.getServletPath());
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
    return StringUtils.removeStart(request.getHeader("Authorization"), "Bearer ");
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "";
  }
}
