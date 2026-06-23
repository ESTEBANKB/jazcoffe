package com.cafeuniv.ppi.domain;

import jakarta.persistence.*;

/**
 * Usuario del sistema (admin/cajero) con credenciales y rol.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 255)
    private String hash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    /**
     * Campo para almacenar permisos personalizados del usuario en formato JSON.
     * Formato: array JSON de strings, ejemplo: ["POS_ACCESS", "INVENTARIO_VIEW"]
     */
    @Column(name = "permisos_json", columnDefinition = "TEXT")
    private String permisosJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getPermisosJson() { return permisosJson; }
    public void setPermisosJson(String permisosJson) { this.permisosJson = permisosJson; }
}


