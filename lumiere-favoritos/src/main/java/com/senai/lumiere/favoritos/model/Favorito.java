package com.senai.lumiere.favoritos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "favoritos", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "titulo_id", "tipo_titulo"}))
@Getter
@Setter
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "titulo_id", nullable = false)
    private Long tituloId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_titulo", nullable = false)
    private TipoTitulo tipoTitulo;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

}
