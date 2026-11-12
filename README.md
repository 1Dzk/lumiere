<div align="center">

# 🌙 Lumiere

**Descubra onde assistir seus filmes e séries favoritos.**

Um catálogo que mostra em quais streamings cada título está disponível
e avisa quando aquele filme que você quer ver chegar na sua plataforma.

![Java](https://img.shields.io/badge/Java-Spring%20Boot-6DB33F?logo=springboot&logoColor=white&style=flat-square)
![Docker](https://img.shields.io/badge/Docker-pronto%20pra%20usar-2496ED?logo=docker&logoColor=white&style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white&style=flat-square)

</div>

---

## ✨ Como funciona

- 🎬 **Qualquer pessoa pode navegar** pelo catálogo, buscar títulos e ver onde assistir — sem criar conta
- ❤️ **Com uma conta**, você pode favoritar títulos, seguir streamings e montar sua lista de desejos
- 🔔 **Quando um título desejado chega em algum streaming**, o Lumiere registra o aviso para você

Os dados de filmes e séries vêm do [TMDB](https://www.themoviedb.org/).

> 🎨 O site que usa este backend está em **[lumiere-web](https://github.com/1Dzk/lumiere-web)**.

---

## 🧩 As peças do sistema

O Lumiere é feito de pequenos serviços, cada um cuidando de uma coisa:

| Serviço | O que faz |
|---|---|
| 🚪 **Gateway** | A porta de entrada: recebe tudo e encaminha para o serviço certo |
| 🔐 **Auth** | Cadastro e login de usuários |
| 🎞️ **Catálogo** | Busca de filmes e séries e onde assistir |
| ❤️ **Favoritos** | Favoritos, desejos e streamings seguidos de cada pessoa |
| 🔔 **Notificações** | Confere periodicamente se algum título desejado ficou disponível |

---

## 🚀 Como rodar

**Você vai precisar de:**

1. [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e aberto
2. Uma chave gratuita do TMDB — crie sua conta em [themoviedb.org](https://www.themoviedb.org/signup) e copie o *API Read Access Token* em **Configurações → API**

**Passo 1.** Crie um arquivo chamado `.env` na pasta do projeto:

```
JWT_SECRET=escolha-uma-frase-secreta-com-32-caracteres-ou-mais
TMDB_API_TOKEN=cole-aqui-o-seu-token-do-tmdb
```

**Passo 2.** Suba tudo com um comando:

```powershell
docker compose up -d --build
```

**Pronto!** 🎉 O sistema fica disponível em **http://localhost:8080**.

Para desligar: `docker compose down`

---

## 👀 Testando rapidinho

Com tudo rodando, veja o que está em alta agora:

```powershell
curl http://localhost:8080/catalogo/em-alta
```

<details>
<summary>📋 Quer testar o fluxo completo (cadastro, login, favoritos)?</summary>

<br>

```powershell
# criar uma conta
curl -X POST http://localhost:8080/auth/registro -H "Content-Type: application/json" -d '{"nome":"Teste","email":"teste@lumiere.dev","senha":"senha12345"}'

# entrar (a resposta traz o seu token)
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"email":"teste@lumiere.dev","senha":"senha12345"}'

# favoritar um filme (troque SEU_TOKEN pelo token do login)
curl -X POST http://localhost:8080/favoritos -H "Content-Type: application/json" -H "Authorization: Bearer SEU_TOKEN" -d '{"tituloId":969681,"tipo":"FILME"}'
```

</details>

---

## 📚 Documentação das APIs

Com o sistema rodando, uma página única reúne a documentação de tudo, com botão de teste em cada endpoint:

- 📖 **[Portal de documentação](http://localhost:8080/docs)**: abas para Gateway, Auth, Catálogo, Favoritos e Notificações, todas pela porta do gateway (o caminho real que o app usa)

Cada serviço também tem a sua página direta:

- 🔐 [Auth](http://localhost:8081/docs)
- 🎞️ [Catálogo](http://localhost:8082/docs)
- ❤️ [Favoritos](http://localhost:8083/swagger-ui/index.html)

---

## 🛠️ Para quem vai mexer no código

<details>
<summary>Detalhes técnicos</summary>

<br>

- Cada serviço é um projeto Spring Boot independente, com seu próprio banco PostgreSQL quando precisa de um
- Rodar os testes de um serviço: `.\mvnw.cmd test` dentro da pasta dele
- Rodar um serviço sem Docker: `./mvnw spring-boot:run` (precisa do Postgres e das variáveis de ambiente configuradas)
- Todos os serviços expõem `/actuator/health`

</details>

---

## 🔮 Próximos passos

- ✉️ Enviar e-mail ou push de verdade quando um título desejado chegar (hoje o aviso fica registrado nos logs)
- 📡 Avisar também sobre lançamentos nos streamings que você segue
- ☸️ Preparar o projeto para rodar em Kubernetes

---

<div align="center">

Feito por [Henrique](https://github.com/1Dzk) · Projeto Integrador III

</div>
