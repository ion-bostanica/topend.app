# ADR-0003: In-memory persistence until the data model settles

Date: 2026-08-19 · Status: Accepted

## Context
POST /nutrition/consumptions (JSON) stores confirmed consumptions. The nutrition data model was
just designed (docs/dbml/nutrition.dbml) and MongoDB is on the classpath but unused.

## Decision
Store consumptions in a ConcurrentHashMap behind the use-case service. The DBML file is the
canonical model; no repositories/documents are written until the model has survived real usage.

## Consequences
- Data is lost on restart — acceptable for the current article scope.
- Swap path: introduce a repository out-port and a Mongo adapter matching the DBML; the
  controller/service layers stay untouched.
