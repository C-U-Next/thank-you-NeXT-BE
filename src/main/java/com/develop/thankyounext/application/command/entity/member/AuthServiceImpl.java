package com.develop.thankyounext.application.command.entity.member;

import com.develop.thankyounext.domain.dto.base.common.AuthenticationDto;
import com.develop.thankyounext.domain.entity.Member;
import com.develop.thankyounext.domain.repository.member.MemberRepository;
import com.develop.thankyounext.global.exception.handler.MemberHandler;
import com.develop.thankyounext.global.payload.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        return AuthenticationDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .password(member.getPassword())
                .build();
    }
}
