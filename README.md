# Lumiere

Catálogo de filmes e séries que mostra em quais streamings (Brasil e outros países) cada título está disponível. A navegação é pública; criar conta é só necessário pra favoritar um título, acompanhar lançamentos de um streaming ou ser avisado quando um filme/série desejado chegar em algum streaming.

Projeto Integrador III — Back-end e arquitetura distribuída (microsserviços Java/Spring Boot).

## Arquitetura

| Serviço | Pasta | Porta | Responsabilidade | Banco |
|---|---|---|---|---|
| `lumiere-gateway` | `lumiere-gateway/` | `8080` | Ponto de entrada único — repassa cada requisição pro serviço certo | sem banco |
| `lumiere-auth` | `lumiere-auth/` | `8081` | Registro, login, emite JWT | PostgreSQL próprio |
| `lumiere-catalogo` | `lumiere-catalogo/` | `8082` | Busca, detalhes e streamings disponíveis (via TMDB), público | sem banco — cache em memória |
| `lumiere-favoritos` | `lumiere-favoritos/` | `8083` | Favoritos, streamings seguidos, desejos — exige JWT | PostgreSQL próprio |
| `lumiere-notificacoes` | `lumiere-notificacoes/` | `8084` | Job agendado: avisa (no log) quando um título desejado fica disponível | PostgreSQL próprio |

`lumiere-favoritos` valida o JWT emitido pelo `lumiere-auth` **localmente**, usando o mesmo segredo compartilhado (`JWT_SECRET`) — não existe chamada em runtime entre os dois serviços.

O `lumiere-gateway` é o jeito recomendado de acessar o sistema (porta `8080`, repassa `/auth/**`, `/catalogo/**`, `/favoritos|streamings-seguidos|desejos/**` e `/notificacoes/**` pro serviço certo), mas cada serviço também continua acessível direto na sua própria porta.

### Decisões explícitas

- **`lumiere-favoritos` valida o `tituloId` contra o `lumiere-catalogo`** antes de salvar um favorito/desejo. Se o catálogo responder 404, o título é rejeitado (404). **Se o catálogo estiver fora do ar** (timeout/erro 5xx), a validação **deixa passar** — uma falha remota não pode virar "título inválido" pro usuário.
- **`GET /interno/desejos`** (em `lumiere-favoritos`) lista os desejos de **todos** os usuários, sem autenticação — é uma chamada serviço-a-serviço usada só pelo `lumiere-notificacoes` dentro da rede Docker, não é exposta publicamente pelo gateway.
- **`lumiere-notificacoes`** só loga o aviso (`docker logs lumiere-notificacoes-1` ou `GET /notificacoes/enviadas`) — não envia e-mail/push de verdade. O mecanismo de detecção (job agendado, evita repetir aviso) está pronto; o canal de envio real fica plugável pra depois.

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) rodando
- Uma API key da TMDB ([themoviedb.org](https://www.themoviedb.org/signup) → Configurações → API → "API Read Access Token")

## Configuração

Crie um arquivo `.env` na raiz do projeto com:

```
JWT_SECRET=qualquer-string-com-pelo-menos-32-caracteres
TMDB_API_TOKEN=seu-api-read-access-token-da-tmdb
NOTIFICACOES_INTERVALO_MS=60000
```

- `JWT_SECRET` — segredo compartilhado entre `lumiere-auth` e `lumiere-favoritos` (um assina o token, o outro valida)
- `TMDB_API_TOKEN` — o "API Read Access Token" da TMDB
- `NOTIFICACOES_INTERVALO_MS` — de quanto em quanto tempo o job de notificações roda (padrão `60000` = 1 minuto)

O `.env` nunca deve ser commitado (já está no `.gitignore`).

## Executar

```powershell
docker compose up -d --build
```

Sobe os 8 containers (3 bancos Postgres + os 5 serviços) na mesma rede. Os apps que têm banco só sobem depois que o banco fica saudável (`healthcheck`).

Testar a jornada principal (tudo via gateway, porta `8080`):

```powershell
# registro
curl -X POST http://localhost:8080/auth/registro -H "Content-Type: application/json" -d '{"nome":"Teste","email":"teste@lumiere.dev","senha":"senha12345"}'

# login (retorna o token)
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"email":"teste@lumiere.dev","senha":"senha12345"}'

# catálogo (público, não exige token)
curl http://localhost:8080/catalogo/em-alta

# favoritar um título (troque SEU_TOKEN pelo token do login)
curl -X POST http://localhost:8080/favoritos -H "Content-Type: application/json" -H "Authorization: Bearer SEU_TOKEN" -d '{"tituloId":969681,"tipo":"FILME"}'

# título que não existe no catálogo -> 404
curl -X POST http://localhost:8080/favoritos -H "Content-Type: application/json" -H "Authorization: Bearer SEU_TOKEN" -d '{"tituloId":999999999,"tipo":"FILME"}'

# desejar um título e esperar o job de notificações encontrar disponibilidade
curl -X POST http://localhost:8080/desejos -H "Content-Type: application/json" -H "Authorization: Bearer SEU_TOKEN" -d '{"tituloId":969681,"tipo":"FILME"}'
curl http://localhost:8080/notificacoes/enviadas
```

Derrubar tudo: `docker compose down` (adicione `-v` pra apagar também os dados dos bancos).

## Documentação das APIs (Swagger)

`lumiere-auth`, `lumiere-catalogo` e `lumiere-favoritos` expõem Swagger UI gerado automaticamente a partir dos controllers:
- http://localhost:8081/swagger-ui/index.html (auth)
- http://localhost:8082/swagger-ui/index.html (catálogo)
- http://localhost:8083/swagger-ui/index.html (favoritos)

`lumiere-gateway` não tem Swagger próprio (é um proxy — o `/**` dele intercepta a rota antes de chegar no Swagger); consulte a doc do serviço de destino.

## Health check

Todos os 5 serviços expõem `GET /actuator/health`. O `docker-compose.yml` usa isso pra só subir um serviço depois que suas dependências estiverem de verdade prontas (não só "o container iniciou").

## Banco de dados e migrações

`lumiere-auth`, `lumiere-favoritos` e `lumiere-notificacoes` usam [Flyway](https://flywaydb.org/) (`src/main/resources/db/migration/`) para versionar o schema — `spring.jpa.hibernate.ddl-auto=validate`, o Hibernate só confere se o schema bate, quem cria as tabelas é o Flyway na subida do serviço.

## Testes

Cada serviço tem testes unitários das regras de negócio e dos caminhos de falha (`src/test/java`), usando JUnit 5 + Mockito (já incluídos nos starters `*-test` do Spring Boot) e `MockRestServiceServer` pros clients HTTP. Rodar, dentro da pasta do serviço:

```powershell
.\mvnw.cmd test
```

## Desenvolvimento sem Docker

Cada serviço também roda local via Maven Wrapper (`./mvnw spring-boot:run` dentro da pasta do serviço), desde que o Postgres correspondente (quando o serviço tiver um) esteja acessível e as variáveis de ambiente (`JWT_SECRET`, `TMDB_API_TOKEN`, `SPRING_DATASOURCE_*`, `CATALOGO_URL`, `FAVORITOS_URL`, `LUMIERE_*_URL`) estejam configuradas.

## Limitações conhecidas

- Sem envio real de e-mail/push no `lumiere-notificacoes` (só log) — ver "Decisões explícitas" acima.
- `lumiere-notificacoes` só verifica a lista de desejos; ainda não detecta lançamento novo num streaming seguido (`/streamings-seguidos`).
- O `lumiere-gateway` demora ~1 min pra retornar 503 quando um serviço de destino está totalmente fora do ar (sem timeout de conexão configurado no `RestClient` ainda).
- Sem manifests Kubernetes ainda (próxima etapa).
