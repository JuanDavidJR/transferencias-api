package com.banco.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferenciaDTO {
    private String id;
    private String cuentaOrigen;
    private String cuentaDestino;
    private BigDecimal monto;
    private String moneda;
    private String estado;
    private String concepto;
    private LocalDateTime fechaTransferencia;
    private String codigoReferencia;
}