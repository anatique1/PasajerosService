package com.example.pasajero.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "cuentas")
public class CuentaGoogle {

    /** Google sub is the stable account identifier; government document data is collected separately. */
    @Id
    private String id;
    private String correo;
    private String nombre;
    private String fotoUrl;
    private String rol = "PASAJERO";
    private Instant creadoEn;
    private Instant ultimoAcceso;
}
