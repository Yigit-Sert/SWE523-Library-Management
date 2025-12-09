package com.library.librarymanagement.repository;

import com.library.librarymanagement.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    // This interface automatically inherits methods like save(), findById(), findAll(), deleteById(), etc.
}