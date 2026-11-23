package cl.teqmed.rendiciones.model;

import cl.teqmed.rendiciones.model.enums.EstadoRendicion;
import cl.teqmed.rendiciones.model.enums.TipoRendicion;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rendiciones")
public class Rendicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String numero;                 // RND-2026-000001, lo asigna el servicio

    // El usuario vive en OTRO microservicio (ms-usuarios): aquí solo se guarda su id
    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 80)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoRendicion tipo;

    @Column(length = 80)
    private String destino;

    @Column(nullable = false)
    private LocalDate desde;

    @Column(nullable = false)
    private LocalDate hasta;

    @Column(nullable = false, length = 20)
    private String centroCosto;

    @Column(length = 200)
    private String observacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoRendicion estado = EstadoRendicion.BORRADOR;

    private LocalDateTime fechaEnvio;
    private Long pagoId;

    // Una rendición tiene muchos gastos. CASCADE: si se borra la rendición, se borran sus gastos
    @OneToMany(mappedBy = "rendicion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Gasto> gastos = new ArrayList<>();

    @OneToMany(mappedBy = "rendicion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialEstado> historial = new ArrayList<>();

    public Rendicion() { }

    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public TipoRendicion getTipo() { return tipo; }
    public void setTipo(TipoRendicion tipo) { this.tipo = tipo; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }
    public LocalDate getHasta() { return hasta; }
    public void setHasta(LocalDate hasta) { this.hasta = hasta; }
    public String getCentroCosto() { return centroCosto; }
    public void setCentroCosto(String centroCosto) { this.centroCosto = centroCosto; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public EstadoRendicion getEstado() { return estado; }
    public void setEstado(EstadoRendicion estado) { this.estado = estado; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
    public Long getPagoId() { return pagoId; }
    public void setPagoId(Long pagoId) { this.pagoId = pagoId; }
    public List<Gasto> getGastos() { return gastos; }
    public List<HistorialEstado> getHistorial() { return historial; }
}
