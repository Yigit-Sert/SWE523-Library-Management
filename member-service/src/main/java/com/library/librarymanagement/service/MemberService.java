package com.library.librarymanagement.service;

import com.library.librarymanagement.exception.ResourceNotFoundException;
import com.library.librarymanagement.model.Member;
import com.library.librarymanagement.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> findAllMembers() {
        log.info("Fetching all members from database...");
        return memberRepository.findAll();
    }

    @Cacheable(value = "members", key = "#id")
    public Member findMemberById(Long id) {
        log.info("Fetching member from database: id={}", id);
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
    }

    public Member saveMember(Member member) {
        log.info("Saving new member to database...");
        return memberRepository.save(member);
    }

    @CachePut(value = "members", key = "#id")
    public Member updateMember(Long id, Member memberDetails) {
        log.info("Updating member and refreshing cache: id={}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        member.setName(memberDetails.getName());
        member.setAddress(memberDetails.getAddress());
        member.setTelephone(memberDetails.getTelephone());

        return memberRepository.save(member);
    }

    @CacheEvict(value = "members", key = "#id")
    public void deleteMemberById(Long id) {
        log.info("Deleting member and evicting from cache: id={}", id);
        if (!memberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Member not found with id: " + id);
        }
        memberRepository.deleteById(id);
    }
}