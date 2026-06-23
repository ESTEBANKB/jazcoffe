package com.cafeuniv.ppi.domain;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Rol de usuario (ADMIN, CAJERO, ...).
 * 
 * USO DE COLECCIONES:
 * - Tiene comentado un Set<Permiso> (relación muchos a muchos con permisos).
 * - Actualmente NO usa colecciones activas, solo campos primitivos.
 * - Las relaciones con usuarios se manejan desde la clase Usuario (ManyToOne).
 */
@Entity
@Table(name = "roles")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;
    
    // COMENTADO: Set<Permiso> para relación muchos a muchos con permisos
    // @ManyToMany
    // @JoinTable(
    //     name = "rol_permisos",
    //     joinColumns = @JoinColumn(name = "rol_id"),
    //     inverseJoinColumns = @JoinColumn(name = "permiso_id")
    // )
    // private Set<Permiso> permisos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    // public Set<Permiso> getPermisos() { return permisos; }
    // public void setPermisos(Set<Permiso> permisos) { this.permisos = permisos; }
}


