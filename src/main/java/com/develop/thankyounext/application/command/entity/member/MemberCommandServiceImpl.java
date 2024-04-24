package com.develop.thankyounext.application.command.entity.member;

import com.develop.thankyounext.domain.dto.base.common.AuthenticationDto;
import com.develop.thankyounext.domain.dto.member.MemberRequest.UpdateMember;
import com.develop.thankyounext.domain.dto.result.ResultResponse.AuthResult;
import com.develop.thankyounext.domain.dto.result.ResultResponse.MemberResult;
import com.develop.thankyounext.domain.entity.Member;
import com.develop.thankyounext.domain.enums.UserRoleEnum;
import com.develop.thankyounext.domain.repository.member.MemberRepository;
import com.develop.thankyounext.global.exception.handler.MemberHandler;
import com.develop.thankyounext.global.payload.code.status.ErrorStatus;
import com.develop.thankyounext.infrastructure.config.redis.RedisProvider;
import com.develop.thankyounext.infrastructure.config.security.provider.JwtProvider;
import com.develop.thankyounext.infrastructure.converter.MemberConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Consumer;

import static com.develop.thankyounext.domain.dto.auth.AuthRequest.SignUp;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberCommandServiceImpl implements MemberCommandService{

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisProvider redisProvider;
    private final MemberConverter memberConverter;

    @Override
    public MemberResult updateProfile(AuthenticationDto auth, UpdateMember request) {

        // 임시 로직
        Member currentMember = memberRepository.getReferenceById(1L);

        // TODO: 인증 객체 생성 필요
//        Member currentMember = memberRepository.getReferenceById(auth.id());

        updateMember(request, currentMember);

        return memberConverter.toMemberResult(currentMember);
    }

    @Override
    public AuthResult signup(SignUp request) {
        validateSignUpRequest(request);

        Member newMember = memberConverter.toMember(request);
        newMember.passwordEncode(passwordEncoder);
        newMember.updateRole(UserRoleEnum.USER);
        memberRepository.save(newMember);

        return memberConverter.toAuthResult(newMember);
    }

    @Override
    public void logout(String accessToken, Long memberId) {
        Long expiration = jwtProvider.getExpiration(accessToken);
        redisProvider.expireAccessToken(accessToken, expiration);
        memberRepository.findById(memberId)
                .ifPresent(member -> member.updateRefreshToken(null));
    }

    private static void updateMember(UpdateMember request, Member currentMember) {
        validateAndUpdatePassword(request, currentMember);
        updateIfPresent(request.name(), currentMember::updateName);
        updateIfPresent(request.description(), currentMember::updateDescription);
        updateIfPresent(request.studentId(), currentMember::updateStudentId);
        updateIfPresent(request.linkUrlList(), currentMember::updateLinkUrlList);
    }

    private static void validateAndUpdatePassword(UpdateMember request, Member currentMember) {
        if (request.password() != null) {
            if (!request.password().equals(request.passwordCheck())) {
                throw new MemberHandler(ErrorStatus.MEMBER_UPDATE_PASSWORD_NOT_EQUAL_BAD_REQUEST);
            }
            currentMember.updatePassword(request.password());
        }
    }

    private static <T> void updateIfPresent(T value, Consumer<T> updateFunction) {
        Optional.ofNullable(value).ifPresent(updateFunction);
    }

    private void validateSignUpRequest(SignUp request) {
        if (Boolean.TRUE.equals(memberRepository.existsByEmail(request.email()))) {
            throw new MemberHandler(ErrorStatus.MEMBER_EMAIL_EXISTED);
        }
        if (Boolean.TRUE.equals(memberRepository.existsByStudentId(request.studentId()))) {
            throw new MemberHandler(ErrorStatus.MEMBER_STUDENT_ID_EXISTED);
        }
    }
}
