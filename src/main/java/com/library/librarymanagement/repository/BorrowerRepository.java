package com.library.librarymanagement.repository;

import com.library.librarymanagement.model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
    // Member ve Book bilgilerini tek sorguda getiren özel sorgu.
    // Bu, "N+1 select" problemini çözer ve LazyInitializationException'ı önler.
    @Query("SELECT b FROM Borrower b JOIN FETCH b.member JOIN FETCH b.book")
    List<Borrower> findAllWithMemberAndBook();
}