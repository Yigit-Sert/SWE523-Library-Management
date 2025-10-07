package com.library.librarymanagement.model;

import jakarta.persistence.*; // Use jakarta.persistence for Spring Boot 3+
import lombok.Data; // For Lombok annotations
import lombok.NoArgsConstructor; // For Lombok annotations
import lombok.AllArgsConstructor; // For Lombok annotations

import java.util.HashSet;
import java.util.Set;

@Entity // Marks this class as a JPA entity, mapped to a database table
@Table(name = "members") // Specifies the table name in the database
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Lombok: Generates a no-argument constructor
@AllArgsConstructor // Lombok: Generates a constructor with all fields
public class Member {

    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increments the ID
    private Long id; // Using Long for ID is a common practice, allows for more flexibility

    @Column(name = "name", length = 16, nullable = false) // Maps to a column named 'name' with max length 16
    private String name;

    @Column(name = "address", length = 32) // Maps to 'address' column with max length 32
    private String address;

    @Column(name = "telephone", length = 15) // Maps to 'telephone' column with max length 15
    private String telephone;

    // --- Relationship with Borrowers ---
    // A Member can have many Borrower records (one-to-many relationship)
    // 'mappedBy' indicates that the 'member' field in the Borrower entity is the owner of the relationship
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Borrower> borrowings = new HashSet<>();
}