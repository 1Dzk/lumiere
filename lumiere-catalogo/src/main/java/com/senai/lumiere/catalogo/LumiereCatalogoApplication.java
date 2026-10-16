package com.senai.lumiere.catalogo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LumiereCatalogoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LumiereCatalogoApplication.class, args);
    }

}
