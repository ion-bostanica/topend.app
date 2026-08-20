# TopEnd

Open source personal fitness app, built in public. Every feature ships alongside a short
write-up of what was learned building it.

The API is a Spring Boot (WebFlux) service that talks to **both Claude and OpenAI** through
separate connectors, switchable by config.

Current features:

- `POST /hello` — dual-provider chat hello world (article #1)
- `POST /nutrition/consumptions/extracts` (multipart photo) — an AI agent identifies the foods on a
  meal photo and resolves them against **USDA FoodData Central** via a tool the model calls,
  returning a structured nutrient breakdown (article #2)
- `POST /nutrition/consumptions` (JSON) — stores a confirmed consumption (in-memory for now;
  the data model lives in `docs/dbml/`)

## Project structure

```
topend.app/
├── api/                                    # Spring Boot service
│   ├── compose.yaml                        # backing services for local dev
│   ├── pom.xml
│   ├── config/
│   │   └── application.yaml.example        # local config template — copy and fill in
│   └── src/main/resources/application.yaml # committed defaults
├── docs/
│   ├── adr/                                # architecture decision records
│   ├── dbml/                               # data model (DBML)
│   ├── structurizr/                        # C4 model (Structurizr DSL)
│   └── FUNCTIONAL_SPEC.md
└── medium/                                 # article drafts and publishing assets
```

Each published article is tagged, so a tag always points at the repo state described by
that article.

## Prerequisites

- JDK 26 (`java.version` in `api/pom.xml`)
- Docker
- An Anthropic and/or OpenAI API key
- A (free) USDA FoodData Central API key — <https://fdc.nal.usda.gov/api-key-signup> —
  for the nutrition endpoints

## Run locally

Create your local config from the template and fill in your keys:

```bash
cd api
cp config/application.yaml.example config/application.yaml
```

Then edit `api/config/application.yaml`:

```yaml
spring:
  ai:
    default: openai          # anthropic | openai
    anthropic:
      api-key: <your Anthropic key>
    openai:
      api-key: <your OpenAI key>

fdc:
  api-key: <your FoodData Central key>
```

Only the key for the provider named in `default` has to be set (plus the FDC key if you
use the nutrition endpoints). `api/config/` is
gitignored — real keys never leave your machine. Spring Boot loads this file over the
committed defaults in `src/main/resources/application.yaml`.

Start the service:

```bash
./mvnw spring-boot:run
```

`spring-boot-docker-compose` starts the containers from `api/compose.yaml` on startup and
stops them on shutdown — no manual `docker compose up` needed.

The service listens on <http://localhost:8080>.

## API docs

With the app running:

- Swagger UI — <http://localhost:8080/swagger-ui.html> (redirects to `/swagger-ui/index.html`)
- OpenAPI JSON — <http://localhost:8080/v3/api-docs>

## Build

```bash
cd api && ./mvnw clean package
```

Produces a runnable jar under `api/target/`, startable with `java -jar`.

## Health

Actuator is on: <http://localhost:8080/actuator/health>

## License

See [LICENSE](LICENSE).
