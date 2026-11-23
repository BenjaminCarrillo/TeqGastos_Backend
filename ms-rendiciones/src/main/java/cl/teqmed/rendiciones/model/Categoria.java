package cl.teqmed.rendiciones.model;

import jakarta.persistence.*;

// Categoría de gasto con sus topes (Apéndice C del SRS)
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    private Integer topePorGasto;      // null = sin tope por gasto
    private Integer topeDiario;        // null = sin tope diario
    private boolean permiteExceso;     // true = se admite con justificación

    public Categoria() { }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getTopePorGasto() { return topePorGasto; }
    public void setTopePorGasto(Integer topePorGasto) { this.topePorGasto = topePorGasto; }
    public Integer getTopeDiario() { return topeDiario; }
    public void setTopeDiario(Integer topeDiario) { this.topeDiario = topeDiario; }
    public boolean isPermiteExceso() { return permiteExceso; }
    public void setPermiteExceso(boolean permiteExceso) { this.permiteExceso = permiteExceso; }
}
