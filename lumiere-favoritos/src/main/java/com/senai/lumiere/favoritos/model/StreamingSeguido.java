package com.senai.lumiere.favoritos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "streamings_seguidos", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "nome_streaming"}))
@Getter
@Setter
public class StreamingSeguido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "nome_streaming", nullable = false)
    private String nomeStreaming;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

}
