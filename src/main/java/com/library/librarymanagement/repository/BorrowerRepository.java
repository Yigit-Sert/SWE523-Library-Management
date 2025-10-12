package com.library.librarymanagement.repository;

import com.library.librarymanagement.model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {

    @Query("SELECT b FROM Borrower b JOIN FETCH b.member JOIN FETCH b.book")
    List<Borrower> findAllWithMemberAndBook();

    @Query("SELECT b FROM Borrower b JOIN FETCH b.member JOIN FETCH b.book WHERE b.id = :id")
    Optional<Borrower> findByIdWithMemberAndBook(@Param("id") Long id);
}