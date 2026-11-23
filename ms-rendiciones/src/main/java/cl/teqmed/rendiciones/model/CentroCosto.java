package cl.teqmed.rendiciones.model;

import jakarta.persistence.*;

// El código es la clave primaria (ej. "CC-SERV-CONCE"), igual que en la app
@Entity
@Table(name = "centros_costo")
public class CentroCosto {

    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    public CentroCosto() { }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
