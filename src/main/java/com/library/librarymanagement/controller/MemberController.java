package com.library.librarymanagement.controller;

import com.library.librarymanagement.dto.MemberDto;
import com.library.librarymanagement.model.Member;
import com.library.librarymanagement.service.MemberService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final ModelMapper modelMapper;

    @Autowired
    public MemberController(MemberService memberService, ModelMapper modelMapper) {
        this.memberService = memberService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public List<MemberDto> getAllMembers() {
        return memberService.findAllMembers().stream()
                .map(member -> modelMapper.map(member, MemberDto.class))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDto> getMemberById(@PathVariable Long id) {
        Member member = memberService.findMemberById(id);
        MemberDto memberDto = modelMapper.map(member, MemberDto.class);
        return ResponseEntity.ok(memberDto);
    }

    @PostMapping
    public ResponseEntity<MemberDto> createMember(@RequestBody MemberDto memberDto) {
        Member memberRequest = modelMapper.map(memberDto, Member.class);
        Member createdMember = memberService.saveMember(memberRequest);
        MemberDto memberResponse = modelMapper.map(createdMember, MemberDto.class);
        return new ResponseEntity<>(memberResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberDto> updateMember(@PathVariable Long id, @RequestBody MemberDto memberDto) {
        Member memberRequest = modelMapper.map(memberDto, Member.class);
        Member updatedMember = memberService.updateMember(id, memberRequest);
        MemberDto memberResponse = modelMapper.map(updatedMember, MemberDto.class);
        return ResponseEntity.ok(memberResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMemberById(id);
        return ResponseEntity.noContent().build();
    }
}