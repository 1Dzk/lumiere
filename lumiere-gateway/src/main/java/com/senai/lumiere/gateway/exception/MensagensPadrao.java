package com.senai.lumiere.gateway.exception;

/**
 * Mensagem fixa por status para os erros que o Spring/Tomcat produzem sozinhos (sem exceção nossa).
 * Texto fixo de propósito: a mensagem original do framework pode vazar detalhe interno.
 */
final class MensagensPadrao {

    private MensagensPadrao() {
    }

    static String para(int status) {
        return switch (status) {
            case 400 -> "Requisição inválida";
            case 404 -> "Recurso não encontrado";
            case 405 -> "Método HTTP não permitido para este endereço";
            case 406 -> "Formato de resposta não suportado";
            case 413 -> "Corpo da requisição grande demais";
            case 414 -> "URL grande demais";
            case 415 -> "Tipo de conteúdo não suportado";
            case 431 -> "Cabeçalhos da requisição grandes demais";
            default -> status >= 500 ? "Erro interno. Tente novamente." : "Requisição não pôde ser atendida";
        };
    }
}
