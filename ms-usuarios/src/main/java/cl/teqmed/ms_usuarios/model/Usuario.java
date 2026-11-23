package cl.teqmed.ms_usuarios.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincremental, como autoGenerate en Room
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String correo;

    @Column(nullable = false)
    private String clave;

    @Enumerated(EnumType.STRING) // guarda "TECNICO", no un número
    @Column(nullable = false, length = 20)
    private Rol rol;

    private Long jefaturaId; // null si es jefatura o finanzas
    private boolean tieneTarjeta;

    public Usuario() {
    } // JPA necesita un constructor vacío

    public Usuario(String nombre, String correo, String clave, Rol rol, Long jefaturaId, boolean tieneTarjeta) {
        this.nombre = nombre;
        this.correo = correo;
        this.clave = clave;
        this.rol = rol;
        this.jefaturaId = jefaturaId;
        this.tieneTarjeta = tieneTarjeta;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Long getJefaturaId() {
        return jefaturaId;
    }

    public void setJefaturaId(Long jefaturaId) {
        this.jefaturaId = jefaturaId;
    }

    public boolean isTieneTarjeta() {
        return tieneTarjeta;
    }

    public void setTieneTarjeta(boolean tieneTarjeta) {
        this.tieneTarjeta = tieneTarjeta;
    }
}