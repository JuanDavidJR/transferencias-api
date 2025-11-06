package com.banco.service;

import com.banco.dto.TransferenciaDTO;
import com.banco.exception.CuentaNoEncontradaException;
import com.banco.exception.SaldoInsuficienteException;
import com.banco.model.Cuenta;
import com.banco.model.Transferencia;
import com.banco.repository.CuentaRepository;
import com.banco.repository.TransferenciaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service

public class TransferenciaService {
    private static final Logger log = LoggerFactory.getLogger(TransferenciaService.class);
    @Autowired
    private TransferenciaRepository transferenciaRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    public Mono<TransferenciaDTO> realizarTransferencia(Transferencia transferencia) {
        log.info("Iniciando transferencia de {} a {}",
                transferencia.getCuentaOrigen(),
                transferencia.getCuentaDestino());

        return validarTransferencia(transferencia)
                .flatMap(this::procesarTransferencia)
                .doOnSuccess(t -> log.info("Transferencia exitosa: {}", t.getCodigoReferencia()))
                .doOnError(e -> log.error("Error en transferencia", e));
    }

    private Mono<Transferencia> validarTransferencia(Transferencia transferencia) {
        if (transferencia.getCuentaOrigen().equals(transferencia.getCuentaDestino())) {
            return Mono.error(new IllegalArgumentException(
                    "No puedes transferir a la misma cuenta"
            ));
        }

        return cuentaRepository.findByNumeroCuenta(transferencia.getCuentaOrigen())
                .switchIfEmpty(Mono.error(
                        new CuentaNoEncontradaException("Cuenta origen no existe")
                ))
                .flatMap(cuentaOrigen -> {
                    if (cuentaOrigen.getSaldo().compareTo(transferencia.getMonto()) < 0) {
                        return Mono.error(new SaldoInsuficienteException(
                                "Saldo insuficiente. Disponible: " + cuentaOrigen.getSaldo()
                        ));
                    }

                    return cuentaRepository.findByNumeroCuenta(transferencia.getCuentaDestino())
                            .switchIfEmpty(Mono.error(
                                    new CuentaNoEncontradaException("Cuenta destino no existe")
                            ))
                            .thenReturn(transferencia);
                });
    }

    private Mono<TransferenciaDTO> procesarTransferencia(Transferencia transferencia) {
        String codigoRef = "TRF" + System.currentTimeMillis();
        transferencia.setId(UUID.randomUUID().toString());
        transferencia.setCodigoReferencia(codigoRef);
        transferencia.setFechaTransferencia(LocalDateTime.now());
        transferencia.setFechaActualizacion(LocalDateTime.now());
        transferencia.setEstado("EXITOSA");

        return Mono.zip(
                        cuentaRepository.findByNumeroCuenta(transferencia.getCuentaOrigen()),
                        cuentaRepository.findByNumeroCuenta(transferencia.getCuentaDestino())
                )
                .flatMap(tuple -> {
                    Cuenta origen = tuple.getT1();
                    Cuenta destino = tuple.getT2();

                    origen.setSaldo(origen.getSaldo().subtract(transferencia.getMonto()));
                    destino.setSaldo(destino.getSaldo().add(transferencia.getMonto()));
                    origen.setUltimaActualizacion(LocalDateTime.now());
                    destino.setUltimaActualizacion(LocalDateTime.now());

                    return Mono.zip(
                            cuentaRepository.save(origen),
                            cuentaRepository.save(destino),
                            transferenciaRepository.save(transferencia)
                    );
                })
                .map(tuple -> convertirADTO(tuple.getT3()));
    }

    public Mono<TransferenciaDTO> obtenerTransferencia(String codigoReferencia) {
        log.info("Buscando transferencia: {}", codigoReferencia);
        return transferenciaRepository.findByCodigoReferencia(codigoReferencia)
                .map(this::convertirADTO)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Transferencia no encontrada")
                ));
    }

    public Flux<TransferenciaDTO> obtenerTransferenciasPorCuenta(String numeroCuenta) {
        return Flux.merge(
                        transferenciaRepository.findByCuentaOrigen(numeroCuenta),
                        transferenciaRepository.findByCuentaDestino(numeroCuenta)
                )
                .map(this::convertirADTO);
    }

    public Flux<TransferenciaDTO> obtenerTransferenciasExitosas() {
        return transferenciaRepository.findByEstado("EXITOSA")
                .map(this::convertirADTO);
    }

    private TransferenciaDTO convertirADTO(Transferencia t) {
        return TransferenciaDTO.builder()
                .id(t.getId())
                .cuentaOrigen(t.getCuentaOrigen())
                .cuentaDestino(t.getCuentaDestino())
                .monto(t.getMonto())
                .moneda(t.getMoneda())
                .estado(t.getEstado())
                .concepto(t.getConcepto())
                .fechaTransferencia(t.getFechaTransferencia())
                .codigoReferencia(t.getCodigoReferencia())
                .build();
    }
}