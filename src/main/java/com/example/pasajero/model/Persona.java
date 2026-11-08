package com.example.pasajero.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public abstract class Persona {

    @Id
    private String id;
    private String nombre;
    private String apellido;
    private String documento;
    private String tipoDocumento;
    private String telefono;
    private String correo;
}
