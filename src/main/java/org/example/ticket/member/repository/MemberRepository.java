package org.example.ticket.member.repository;

import org.example.ticket.member.model.Member;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByWalletAddressOrPhoneNumber(String walletAddress, String phoneNumber);

    Boolean existsMemberByWalletAddress(String walletAddress);
    Optional<Member> findByWalletAddress(String walletAddress);
    boolean existsMemberByPhoneNumber(String phoneNumber);
    Member findByNickname(String nickname);

}