package com.banco.repository;

import com.banco.model.Transferencia;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TransferenciaRepository extends ReactiveMongoRepository<Transferencia, String> {
    Flux<Transferencia> findByCuentaOrigen(String cuentaOrigen);
    Flux<Transferencia> findByCuentaDestino(String cuentaDestino);
    Flux<Transferencia> findByEstado(String estado);
    Mono<Transferencia> findByCodigoReferencia(String codigoReferencia);
}