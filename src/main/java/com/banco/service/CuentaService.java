package com.banco.service;

import com.banco.dto.CuentaDTO;
import com.banco.exception.CuentaNoEncontradaException;
import com.banco.model.Cuenta;
import com.banco.repository.CuentaRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CuentaService {
    private static final Logger log = LogManager.getLogger(CuentaService.class);
    @Autowired
    private CuentaRepository cuentaRepository;

    public Mono<CuentaDTO> crearCuenta(Cuenta cuenta) {
        log.info("Creando nueva cuenta para: {}", cuenta.getNombreTitular());
        cuenta.setId(UUID.randomUUID().toString());
        cuenta.setNumeroCuenta(generarNumeroCuenta());
        cuenta.setFechaCreacion(LocalDateTime.now());
        cuenta.setUltimaActualizacion(LocalDateTime.now());
        cuenta.setActiva(true);

        return cuentaRepository.save(cuenta)
                .map(this::convertirADTO)
                .doOnError(e -> log.error("Error al crear cuenta", e));
    }

    public Mono<CuentaDTO> obtenerPorNumeroCuenta(String numeroCuenta) {
        log.info("Buscando cuenta: {}", numeroCuenta);
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .map(this::convertirADTO)
                .switchIfEmpty(Mono.error(
                        new CuentaNoEncontradaException("Cuenta no encontrada: " + numeroCuenta)
                ));
    }

    public Flux<CuentaDTO> obtenerTodas() {
        return cuentaRepository.findAll()
                .map(this::convertirADTO);
    }

    public Mono<CuentaDTO> obtenerPorEmail(String email) {
        return cuentaRepository.findByEmail(email)
                .map(this::convertirADTO)
                .switchIfEmpty(Mono.error(
                        new CuentaNoEncontradaException("Cuenta no encontrada para email: " + email)
                ));
    }

    private String generarNumeroCuenta() {
        return "ACC" + System.currentTimeMillis() +
                (int)(Math.random() * 10000);
    }

    private CuentaDTO convertirADTO(Cuenta cuenta) {
        return CuentaDTO.builder()
                .id(cuenta.getId())
                .numeroCuenta(cuenta.getNumeroCuenta())
                .nombreTitular(cuenta.getNombreTitular())
                .email(cuenta.getEmail())
                .saldo(cuenta.getSaldo())
                .moneda(cuenta.getMoneda())
                .activa(cuenta.getActiva())
                .build();
    }
}