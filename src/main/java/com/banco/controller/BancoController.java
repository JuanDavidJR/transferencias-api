package com.banco.controller;

import com.banco.dto.CuentaDTO;
import com.banco.dto.TransferenciaDTO;
import com.banco.exception.CuentaNoEncontradaException;
import com.banco.exception.SaldoInsuficienteException;
import com.banco.model.Cuenta;
import com.banco.model.Transferencia;
import com.banco.service.CuentaService;
import com.banco.service.TransferenciaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
public class BancoController {
    private static final Logger log = LoggerFactory.getLogger(BancoController.class);
    @Autowired
    private CuentaService cuentaService;

    @Autowired
    private TransferenciaService transferenciaService;

    // ========== ENDPOINTS DE CUENTAS ==========
    @PostMapping("/cuentas")
    public Mono<ResponseEntity<CuentaDTO>> crearCuenta(@Valid @RequestBody Cuenta cuenta) {
        return cuentaService.crearCuenta(cuenta)
                .map(c -> ResponseEntity.status(HttpStatus.CREATED).body(c));
    }

    @GetMapping("/cuentas/{numeroCuenta}")
    public Mono<ResponseEntity<CuentaDTO>> obtenerCuenta(@PathVariable String numeroCuenta) {
        return cuentaService.obtenerPorNumeroCuenta(numeroCuenta)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("/cuentas")
    public Flux<CuentaDTO> obtenerTodasCuentas() {
        return cuentaService.obtenerTodas();
    }

    @GetMapping("/cuentas/email/{email}")
    public Mono<ResponseEntity<CuentaDTO>> obtenerPorEmail(@PathVariable String email) {
        return cuentaService.obtenerPorEmail(email)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    // ========== ENDPOINTS DE TRANSFERENCIAS ==========
    @PostMapping("/transferencias")
    public Mono<ResponseEntity<TransferenciaDTO>> realizarTransferencia(
            @Valid @RequestBody Transferencia transferencia
    ) {
        return transferenciaService.realizarTransferencia(transferencia)
                .map(t -> ResponseEntity.status(HttpStatus.CREATED).body(t))
                .onErrorResume(e -> {
                    log.error("Error en transferencia: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @GetMapping("/transferencias/{codigoReferencia}")
    public Mono<ResponseEntity<TransferenciaDTO>> obtenerTransferencia(
            @PathVariable String codigoReferencia
    ) {
        return transferenciaService.obtenerTransferencia(codigoReferencia)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("/transferencias/historial/{numeroCuenta}")
    public Flux<TransferenciaDTO> obtenerHistorial(@PathVariable String numeroCuenta) {
        return transferenciaService.obtenerTransferenciasPorCuenta(numeroCuenta);
    }

    @GetMapping("/transferencias/estado/exitosas")
    public Flux<TransferenciaDTO> obtenerTransferenciasExitosas() {
        return transferenciaService.obtenerTransferenciasExitosas();
    }

    // ========== MANEJO DE EXCEPCIONES ==========
    @ExceptionHandler(SaldoInsuficienteException.class)
    public Mono<ResponseEntity<ErrorResponse>> manejarSaldoInsuficiente(
            SaldoInsuficienteException e
    ) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(new ErrorResponse("Saldo insuficiente", e.getMessage())));
    }

    @ExceptionHandler(CuentaNoEncontradaException.class)
    public Mono<ResponseEntity<ErrorResponse>> manejarCuentaNoEncontrada(
            CuentaNoEncontradaException e
    ) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Cuenta no encontrada", e.getMessage())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> manejarArgumentoIlegal(
            IllegalArgumentException e
    ) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Solicitud inválida", e.getMessage())));
    }
}

class ErrorResponse {
    public String tipo;
    public String mensaje;

    public ErrorResponse(String tipo, String mensaje) {
        this.tipo = tipo;
        this.mensaje = mensaje;
    }
}