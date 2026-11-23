package cl.teqmed.rendiciones.model;

import jakarta.persistence.*;
import java.time.LocalDate;

// Una transferencia de Finanzas a un técnico, que paga una o más rendiciones
@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 20)
    private String numeroOperacion;

    @Column(nullable = false)
    private int monto;

    @Column(length = 200)
    private String observacion;

    @Column(nullable = false)
    private Long usuarioFinanzasId;

    public Pago() { }

    public Long getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getNumeroOperacion() { return numeroOperacion; }
    public void setNumeroOperacion(String numeroOperacion) { this.numeroOperacion = numeroOperacion; }
    public int getMonto() { return monto; }
    public void setMonto(int monto) { this.monto = monto; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public Long getUsuarioFinanzasId() { return usuarioFinanzasId; }
    public void setUsuarioFinanzasId(Long usuarioFinanzasId) { this.usuarioFinanzasId = usuarioFinanzasId; }
}
