package com.senai.lumiere.notificacoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LumiereNotificacoesApplication {

    public static void main(String[] args) {
        SpringApplication.run(LumiereNotificacoesApplication.class, args);
    }

}
