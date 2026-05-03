package com.MediTrack.meditrack_backend.Auth_Module.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String role; // e.g., ROLE_ADMIN, ROLE_BIOMED, ROLE_USER
}