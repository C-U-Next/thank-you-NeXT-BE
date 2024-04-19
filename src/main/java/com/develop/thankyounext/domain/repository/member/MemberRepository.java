package com.develop.thankyounext.domain.repository.member;

import com.develop.thankyounext.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberQueryDSL {
     Optional<Member> findByRefreshToken(String refreshToken);

     Optional<Member> findByEmail(String email);

     Boolean existsByEmail(String email);

     Boolean existsByStudentId(String string);
}
