package com.develop.thankyounext.infrastructure.config.security.filter;

import com.develop.thankyounext.domain.dto.base.common.AuthenticationDto;
import com.develop.thankyounext.domain.entity.Member;
import com.develop.thankyounext.domain.enums.UserRoleEnum;
import com.develop.thankyounext.domain.repository.member.MemberRepository;
import com.develop.thankyounext.infrastructure.config.redis.RedisProvider;
import com.develop.thankyounext.infrastructure.config.security.provider.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthProcessingFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RedisProvider redisProvider;
    private final MemberRepository memberRepository;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String refreshToken = jwtProvider.extractRefreshToken(request)
                .filter(jwtProvider::isTokenValid)
                .orElse(null);

        if (refreshToken != null) {
            checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
            return;
        }

        checkAccessTokenAndAuthentication(request, response, filterChain);
    }

    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
        memberRepository.findByRefreshToken(refreshToken)
                .ifPresent(member -> {
                    String reIssuedRefreshToken = reIssueRefreshToken(member);
                    jwtProvider.sendAccessAndRefreshToken(response, jwtProvider.createAccessToken(member.getEmail()),
                            reIssuedRefreshToken);
                });
    }

    private String reIssueRefreshToken(Member member) {
        String reIssuedRefreshToken = jwtProvider.createRefreshToken();
        member.updateRefreshToken(reIssuedRefreshToken);
        memberRepository.saveAndFlush(member);
        return reIssuedRefreshToken;
    }

    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain filterChain) throws ServletException, IOException {
        jwtProvider.extractAccessToken(request)
                .filter(jwtProvider::isTokenValid)
                .ifPresent(accessToken -> {
                    redisProvider.validBlackToken(accessToken);
                    jwtProvider.extractId(accessToken)
                            .flatMap(memberRepository::findById)
                            .ifPresent(this::saveAuthentication);
                });

        filterChain.doFilter(request, response);
    }

    public void saveAuthentication(Member myMember) {
        log.info("JwtAuthenticationProcessingFilter saveAuthentication 호출 확인");
        AuthenticationDto authInfoDTO = AuthenticationDto.builder()
                .id(myMember.getId())
                .email(myMember.getEmail())
                .authorities(
                        Set.of(myMember.getRole()).stream()
                                .map(UserRoleEnum::getAuthority)
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toUnmodifiableSet()))
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(authInfoDTO, null,
                        authoritiesMapper.mapAuthorities(authInfoDTO.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
