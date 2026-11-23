package cl.teqmed.rendiciones.model;

import cl.teqmed.rendiciones.model.enums.*;
import jakarta.persistence.*;
import java.time.LocalDate;

// Índice único RUT + folio: un mismo comprobante no puede rendirse dos veces (RN-02).
// MySQL permite varios NULL, así que los tickets (sin RUT ni folio) no chocan entre sí.
@Entity
@Table(name = "gastos",
       uniqueConstraints = @UniqueConstraint(name = "uk_gasto_rut_folio", columnNames = {"rut_emisor", "folio"}))
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Clave foránea a la rendición (muchos gastos → una rendición)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rendicion_id")
    private Rendicion rendicion;

    // Clave foránea a la categoría
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false)
    private LocalDate fechaDocumento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDocumento tipoDocumento;

    @Column(name = "rut_emisor", length = 12)
    private String rutEmisor;

    @Column(name = "folio", length = 10)
    private String folio;

    @Column(nullable = false, length = 80)
    private String proveedor;

    @Column(nullable = false)
    private int monto;

    @Column(nullable = false)
    private int montoAdmitido;             // lo calculado según el tope

    @Column(length = 200)
    private String justificacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MedioPago medioPago;

    @Column(nullable = false, length = 20)
    private String centroCosto;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(length = 40)
    private String ordenTrabajo;

    @Column(length = 255)
    private String rutaFoto;

    private Double latitud;
    private Double longitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoGasto estado = EstadoGasto.BORRADOR;

    // Lo que deja la revisión de la jefatura
    private Integer montoAprobado;

    @Column(length = 200)
    private String comentarioRevision;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MotivoRechazo motivoRechazo;

    @Column(length = 200)
    private String respuestaTecnico;

    public Gasto() { }

    public Long getId() { return id; }
    public Rendicion getRendicion() { return rendicion; }
    public void setRendicion(Rendicion rendicion) { this.rendicion = rendicion; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public LocalDate getFechaDocumento() { return fechaDocumento; }
    public void setFechaDocumento(LocalDate fechaDocumento) { this.fechaDocumento = fechaDocumento; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getRutEmisor() { return rutEmisor; }
    public void setRutEmisor(String rutEmisor) { this.rutEmisor = rutEmisor; }
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public int getMonto() { return monto; }
    public void setMonto(int monto) { this.monto = monto; }
    public int getMontoAdmitido() { return montoAdmitido; }
    public void setMontoAdmitido(int montoAdmitido) { this.montoAdmitido = montoAdmitido; }
    public String getJustificacion() { return justificacion; }
    public void setJustificacion(String justificacion) { this.justificacion = justificacion; }
    public MedioPago getMedioPago() { return medioPago; }
    public void setMedioPago(MedioPago medioPago) { this.medioPago = medioPago; }
    public String getCentroCosto() { return centroCosto; }
    public void setCentroCosto(String centroCosto) { this.centroCosto = centroCosto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getOrdenTrabajo() { return ordenTrabajo; }
    public void setOrdenTrabajo(String ordenTrabajo) { this.ordenTrabajo = ordenTrabajo; }
    public String getRutaFoto() { return rutaFoto; }
    public void setRutaFoto(String rutaFoto) { this.rutaFoto = rutaFoto; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public EstadoGasto getEstado() { return estado; }
    public void setEstado(EstadoGasto estado) { this.estado = estado; }
    public Integer getMontoAprobado() { return montoAprobado; }
    public void setMontoAprobado(Integer montoAprobado) { this.montoAprobado = montoAprobado; }
    public String getComentarioRevision() { return comentarioRevision; }
    public void setComentarioRevision(String comentarioRevision) { this.comentarioRevision = comentarioRevision; }
    public MotivoRechazo getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(MotivoRechazo motivoRechazo) { this.motivoRechazo = motivoRechazo; }
    public String getRespuestaTecnico() { return respuestaTecnico; }
    public void setRespuestaTecnico(String respuestaTecnico) { this.respuestaTecnico = respuestaTecnico; }
}
