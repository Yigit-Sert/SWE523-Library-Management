package com.library.librarymanagement.controller;

import com.library.librarymanagement.model.Member;
import com.library.librarymanagement.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // Display a list of all members
    @GetMapping
    public String listMembers(Model model) {
        List<Member> members = memberService.findAllMembers();
        model.addAttribute("members", members);
        model.addAttribute("newMember", new Member()); // For the "Add Member" modal/form
        return "members/index"; // Corresponds to src/main/resources/templates/members/index.html
    }

    // Display form to add a new member (often integrated into the index page as a modal)
    // For simplicity, we'll handle the form submission directly in a POST method.
    // If a dedicated "add" page is needed:
    // @GetMapping("/new")
    // public String showAddForm(Model model) {
    //    model.addAttribute("member", new Member());
    //    return "members/add";
    // }

    // Handle adding a new member
    @PostMapping("/add")
    public String addMember(@ModelAttribute Member member, RedirectAttributes redirectAttributes) {
        memberService.saveMember(member);
        redirectAttributes.addFlashAttribute("message", "Member added successfully!");
        return "redirect:/members";
    }

    // Display form to edit an existing member
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return memberService.findMemberById(id).map(member -> {
            model.addAttribute("member", member);
            return "members/edit"; // Corresponds to src/main/resources/templates/members/edit.html
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("error", "Member not found!");
            return "redirect:/members";
        });
    }

    // Handle updating an existing member
    @PostMapping("/update/{id}")
    public String updateMember(@PathVariable Long id, @ModelAttribute Member member, RedirectAttributes redirectAttributes) {
        // Ensure the ID from the path is set to the member object
        member.setId(id);
        memberService.saveMember(member);
        redirectAttributes.addFlashAttribute("message", "Member updated successfully!");
        return "redirect:/members";
    }

    // Handle deleting a member
    @PostMapping("/delete/{id}")
    public String deleteMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        memberService.deleteMemberById(id);
        redirectAttributes.addFlashAttribute("message", "Member deleted successfully!");
        return "redirect:/members";
    }
}