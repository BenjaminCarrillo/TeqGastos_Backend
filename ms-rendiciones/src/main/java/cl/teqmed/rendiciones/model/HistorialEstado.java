package cl.teqmed.rendiciones.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Cada cambio de estado de una rendición deja una fila aquí (trazabilidad para auditoría)
@Entity
@Table(name = "historial_estados")
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rendicion_id")
    private Rendicion rendicion;

    @Column(length = 30)
    private String estadoAnterior;

    @Column(nullable = false, length = 30)
    private String estadoNuevo;

    @Column(nullable = false)
    private Long usuarioId;            // quién hizo el cambio

    @Column(length = 200)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public HistorialEstado() { }

    public HistorialEstado(Rendicion rendicion, String estadoAnterior, String estadoNuevo, Long usuarioId, String comentario) {
        this.rendicion = rendicion;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.usuarioId = usuarioId;
        this.comentario = comentario;
    }

    public Long getId() { return id; }
    public Rendicion getRendicion() { return rendicion; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public Long getUsuarioId() { return usuarioId; }
    public String getComentario() { return comentario; }
    public LocalDateTime getFecha() { return fecha; }
}
