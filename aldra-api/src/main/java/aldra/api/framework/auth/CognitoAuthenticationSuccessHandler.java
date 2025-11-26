package aldra.api.framework.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RequiredArgsConstructor
public class CognitoAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_OK);
    response.setHeader("Content-Type", "application/json");
    response.getWriter().write(JSON_MAPPER.writeValueAsString(authentication.getDetails()));
  }
}
