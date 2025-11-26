package aldra.api.framework.auth;

import aldra.api.adapter.web.dto.ErrorCode;
import aldra.api.adapter.web.dto.ExceptionResponseBase;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RequiredArgsConstructor
public class AuthFailureHandler implements AuthenticationFailureHandler {

  private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {
    ErrorCode errorCode = null;
    if (exception instanceof AuthException) {
      errorCode = ((AuthException) exception).errorCode();
    } else {
      val fromMessage = ErrorCode.fromMessage(exception.getMessage());
      if (Objects.nonNull(fromMessage)) {
        errorCode = fromMessage;
      } else {
        errorCode = ErrorCode.ES0000_0000;
      }
    }

    val res = new ExceptionResponseBase(errorCode);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setHeader("Content-Type", "application/json");
    response.getWriter().write(JSON_MAPPER.writeValueAsString(res));
  }
}
