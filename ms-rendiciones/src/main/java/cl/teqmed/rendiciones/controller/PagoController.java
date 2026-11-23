package cl.teqmed.rendiciones.controller;

import cl.teqmed.rendiciones.dto.PagoDtos.*;
import cl.teqmed.rendiciones.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) { this.service = service; }

    @GetMapping
    public List<PagoDto> listar() { return service.listar(); }

    @PostMapping                                       // finanzas registra una transferencia
    @ResponseStatus(HttpStatus.CREATED)
    public PagoDto registrar(@Valid @RequestBody PagoRequest r) { return service.registrar(r); }
}
