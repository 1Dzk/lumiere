CREATE TABLE favoritos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    titulo_id BIGINT NOT NULL,
    tipo_titulo VARCHAR(20) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    CONSTRAINT uk_favoritos_usuario_titulo UNIQUE (usuario_id, titulo_id, tipo_titulo)
);

CREATE TABLE desejos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    titulo_id BIGINT NOT NULL,
    tipo_titulo VARCHAR(20) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    CONSTRAINT uk_desejos_usuario_titulo UNIQUE (usuario_id, titulo_id, tipo_titulo)
);

CREATE TABLE streamings_seguidos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome_streaming VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    CONSTRAINT uk_streamings_seguidos_usuario_nome UNIQUE (usuario_id, nome_streaming)
);
