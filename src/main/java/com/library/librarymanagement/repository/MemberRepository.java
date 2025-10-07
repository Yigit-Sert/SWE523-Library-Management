package com.library.librarymanagement.repository;

import com.library.librarymanagement.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Marks this interface as a Spring Data JPA repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    // JpaRepository<EntityClass, PrimaryKeyType>
    // This interface automatically inherits methods like save(), findById(), findAll(), deleteById(), etc.

    // You can add custom query methods here if needed, e.g.:
    // List<Member> findByNameContaining(String name);
}