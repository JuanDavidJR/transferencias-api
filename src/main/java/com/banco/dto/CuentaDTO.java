package com.banco.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CuentaDTO {
    private String id;
    private String numeroCuenta;
    private String nombreTitular;
    private String email;
    private BigDecimal saldo;
    private String moneda;
    private Boolean activa;
}