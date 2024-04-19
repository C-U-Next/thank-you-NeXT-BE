package com.develop.thankyounext.infrastructure.config.security.handler;

import com.develop.thankyounext.domain.entity.Member;
import com.develop.thankyounext.domain.repository.member.MemberRepository;
import com.develop.thankyounext.global.payload.ApiResponseDTO;
import com.develop.thankyounext.infrastructure.config.security.jwt.driver.JwtDriver;
import com.develop.thankyounext.infrastructure.converter.MemberConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtDriver jwtDriver;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    private final MemberConverter memberConverter;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String email = extractEmail(authentication);
        String accessToken = jwtDriver.createAccessToken(email);
        String refreshToken = jwtDriver.createRefreshToken();

        jwtDriver.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        Member member = memberRepository.findByEmail(email).orElseThrow(RuntimeException::new);// Error Handler 구현 필요
        log.info("member.name = {}", member.getName());
        log.info("member.password = {}", member.getPassword());
        log.info("member.getEmail = {}", member.getEmail());
        log.info("member.refreshToken size = {}", refreshToken.length());


        member.updateRefreshToken(refreshToken);
        memberRepository.saveAndFlush(member);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        String result = objectMapper.writeValueAsString(ApiResponseDTO.onSuccess(memberConverter.toAuthResult(member)));

        response.getWriter().write(result);
    }

    private String extractEmail(Authentication authentication) {
        UserDetails auth = (UserDetails) authentication.getPrincipal();
        return auth.getUsername();
    }
}
