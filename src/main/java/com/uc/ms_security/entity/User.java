package com.uc.ms_security.entity;

//Estamos trabajando con base de datos relacional (los decoradores son propios de cada tipo de base de datos)

//¿Como pasar de una relacional a una no relacional con principios SOLID?
//¿Es bueno tener claves compuestas?

import jakarta.persistence.*; //validaciones: vamos a estar pendiente de lo que los DTOs levanten respecto algún campo
//lombok: ayuda para hacer los getters y setters, en tiempo de ejecución (los puedo utilizar pero no aparece) coloca los getter y los setters.
import lombok.Getter;
import lombok.NoArgsConstructor; //Ayuda a crear los constructores en tiempo de ejecución
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

//A continuación vemos los decoradores (sirve para que el framework sepa unas palabras claves)
@Entity //Palabra clave para definir que vamos a utilizar una base de datos relacional
@Table(name = "users") //como se va a crear la tabla de la entidad en base de datos.
@Getter //uso de la libreria
@Setter //uso de la libreria
@NoArgsConstructor //uso de la libreria

public class User {
    @Id // Este campo es el que lo va a definir de los demas en la base de datos (PrimaryKey)
    @GeneratedValue( //elementos propios de la base de datos
            strategy = GenerationType.IDENTITY //autoincrementable
    )
    private Long id; //este atributo se va a convertir en una columna.
    //long tiene una capacidad de número más grande

    @Column(
            nullable = false, //notnull
            length = 100 //varchar 100
    )
    private String name; //estructura de java

    @Column(
            nullable = false, //NO puede ser nulo
            unique = true, //unico
            length = 150 //150 caracteress
    )
    private String email;

    @Column(
            nullable = false, //no puede ser nulo
            length = 255
    )
    private String password;

    @Column(name = "auth_provider", length = 30)
    private String authProvider = "CREDENTIALS";

    @Column(name = "external_subject", length = 200)
    private String externalSubject;

        @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private Profile profile;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private List<Session> sessions = new ArrayList<>();

        public void addSession(Session session) {
            sessions.add(session);
            session.setUser(this);
        }

        public void removeSession(Session session) {
            sessions.remove(session);
            session.setUser(null);
        }

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private List<UserRole> userRoles = new ArrayList<>();
}
