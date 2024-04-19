package com.develop.thankyounext.infrastructure.config.security.handler;

import com.develop.thankyounext.global.payload.ApiResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public static final String LOGIN_FAILURE_MESSAGE = "로그인에 실패하였습니다.";

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        String result = objectMapper.writeValueAsString(ApiResponseDTO.onFailure(HttpStatus.BAD_REQUEST.toString(), LOGIN_FAILURE_MESSAGE, null));
        response.getWriter().write(result);
    }
}
