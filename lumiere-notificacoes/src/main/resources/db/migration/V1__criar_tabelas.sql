CREATE TABLE notificacoes_enviadas (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    titulo_id BIGINT NOT NULL,
    tipo_titulo VARCHAR(20) NOT NULL,
    enviada_em TIMESTAMP NOT NULL,
    CONSTRAINT uk_notificacoes_usuario_titulo UNIQUE (usuario_id, titulo_id, tipo_titulo)
);
