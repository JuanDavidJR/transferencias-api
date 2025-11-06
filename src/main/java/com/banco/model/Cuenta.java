package com.banco.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cuentas")
public class Cuenta {
    @Id
    private String id;

    private String numeroCuenta;

    @NotBlank(message = "El nombre del titular es requerido")
    private String nombreTitular;

    @NotBlank(message = "El email es requerido")
    @Email
    private String email;

    @PositiveOrZero(message = "El saldo no puede ser negativo")
    private BigDecimal saldo;

    @NotBlank(message = "El tipo de moneda es requerido")
    private String moneda;

    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimaActualizacion;
    private Boolean activa;
}