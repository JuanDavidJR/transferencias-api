package com.banco.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transferencias")
public class Transferencia {
    @Id
    private String id;

    @NotBlank
    private String cuentaOrigen;

    @NotBlank
    private String cuentaDestino;

    @Positive(message = "El monto debe ser positivo")
    private BigDecimal monto;

    private String moneda;
    private String estado;
    private String concepto;
    private LocalDateTime fechaTransferencia;
    private LocalDateTime fechaActualizacion;
    private String codigoReferencia;
    private String motivo;
}