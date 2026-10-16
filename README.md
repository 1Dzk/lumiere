# Lumiere

Catálogo de filmes e séries que mostra em quais streamings (Brasil e outros países) cada título está disponível. A navegação é pública; criar conta é só necessário pra favoritar um título, acompanhar lançamentos de um streaming ou ser avisado quando um filme/série desejado chegar em algum streaming.

Projeto Integrador III — Back-end e arquitetura distribuída (microsserviços Java/Spring Boot).

## Arquitetura

| Serviço | Pasta | Porta | Responsabilidade | Banco |
|---|---|---|---|---|
| `lumiere-auth` | `lumiere-auth/` | `8081` | Registro, login, emite JWT | PostgreSQL próprio |
| `lumiere-catalogo` | `lumiere-catalogo/` | `8082` | Busca, detalhes e streamings disponíveis (via TMDB), público | sem banco — cache em memória |
| `lumiere-favoritos` | `lumiere-favoritos/` | `8083` | Favoritos, streamings seguidos, desejos — exige JWT | PostgreSQL próprio |

`lumiere-favoritos` valida o JWT emitido pelo `lumiere-auth` **localmente**, usando o mesmo segredo compartilhado (`JWT_SECRET`) — não existe chamada em runtime entre os dois serviços.

Ainda não implementados: `lumiere-notificacoes` (avisa quando um título desejado fica disponível) e um API Gateway unificando as 3 portas.

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) rodando
- Uma API key da TMDB ([themoviedb.org](https://www.themoviedb.org/signup) → Configurações → API → "API Read Access Token")

## Configuração

```powershell
Copy-Item .env.example .env
```

Edite o `.env` e preencha:
- `JWT_SECRET` — qualquer string com pelo menos 32 caracteres (segredo compartilhado entre `lumiere-auth` e `lumiere-favoritos`)
- `TMDB_API_TOKEN` — o "API Read Access Token" da TMDB

O `.env` nunca deve ser commitado (já está no `.gitignore`).

## Executar

```powershell
docker compose up -d --build
```

Sobe os 5 containers (2 bancos Postgres + os 3 serviços) na mesma rede. Os apps só sobem depois que seus bancos ficam saudáveis (`healthcheck`).

Testar a jornada principal:

```powershell
# registro
curl -X POST http://localhost:8081/auth/registro -H "Content-Type: application/json" -d '{"nome":"Teste","email":"teste@lumiere.dev","senha":"senha12345"}'

# login (retorna o token)
curl -X POST http://localhost:8081/auth/login -H "Content-Type: application/json" -d '{"email":"teste@lumiere.dev","senha":"senha12345"}'

# catálogo (público, não exige token)
curl http://localhost:8082/catalogo/em-alta

# favoritar um título (troque SEU_TOKEN pelo token do login)
curl -X POST http://localhost:8083/favoritos -H "Content-Type: application/json" -H "Authorization: Bearer SEU_TOKEN" -d '{"tituloId":969681,"tipo":"FILME"}'
```

Derrubar tudo: `docker compose down` (adicione `-v` pra apagar também os dados dos bancos).

## Desenvolvimento sem Docker

Cada serviço também roda local via Maven Wrapper (`./mvnw spring-boot:run` dentro da pasta do serviço), desde que o Postgres correspondente esteja acessível e as variáveis de ambiente (`JWT_SECRET`, `TMDB_API_TOKEN`, `SPRING_DATASOURCE_*`) estejam configuradas.

## Limitações conhecidas

- Sem `lumiere-notificacoes` e sem API Gateway ainda — cada serviço é acessado direto na sua porta.
- `lumiere-favoritos` não valida o `tituloId` contra o `lumiere-catalogo` (confia no id enviado pelo cliente).
- Sem manifests Kubernetes ainda (próxima etapa).
