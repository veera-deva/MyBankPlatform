# Context Map — v1

> Companion to [ADR-0004](./adr/ADR-0004.md). Shows the bounded contexts chosen there and, for each relationship, which side is upstream/downstream and what protects the boundary — not just "calls," but who can break whom by changing.

## Bounded Contexts

| Context | Owns | Classification |
|---|---|---|
| `api-gateway` | Routing, edge auth only | Not a bounded context — no domain model of its own. Purely infrastructure; must never grow business logic. |
| `identity-service` | `Customer`, credentials, JWT issuance | **Upstream.** Exposes a stable, deliberate contract (issue/validate a token) that everything else conforms to. |
| `core-banking-service` | `Account`, `LedgerEntry` | **Core domain** — the most business-critical context, per [ADR-0001](./adr/ADR-0001.md)/[ADR-0002](./adr/ADR-0002.md). Downstream of `identity-service` for customer existence checks. |
| `notification-service` | Delivery/notification state only — no financial data | **Downstream** of `core-banking-service` via Kafka. Pure consumer. |
| `transfer-service` (later) | Orchestration state for transfers to/from **external** banks | **Downstream** of `core-banking-service` (debits/credits local accounts) and of an external payment rail it does not control. |

## Relationships

### `core-banking-service` → `identity-service` (Customer-Supplier + Anticorruption Layer)

- **Call:** gRPC, synchronous — "does this customer exist and is it active?" when opening an account. Today this is a JPA foreign key; once the databases split, it becomes a real network call.
- **Anticorruption Layer:** `core-banking-service` must only hold a bare `customerId`, never a copy of `identity-service`'s `Customer` shape (password hash, email, etc.). It depends on the minimum `identity-service` is willing to publish as a stable contract, not on `identity-service`'s internals.
- **Why Customer-Supplier, not Partnership:** `identity-service` can change its internal model freely as long as the gRPC contract stays stable. `core-banking-service` has no say in how `identity-service` evolves — it only consumes the contract.

### `core-banking-service` → `notification-service` (Published Language, event-driven)

- **Call:** Kafka, asynchronous. `core-banking-service` publishes `TransactionPosted`; see the [event catalogue](./event-catalogue.md) for the schema.
- **Published Language:** the event schema *is* the contract. `notification-service` has zero influence over it — it adapts to whatever `core-banking-service` publishes, which is why schema compatibility (M6) matters once this is live.
- **Why async, not gRPC:** notifications are not on the critical path of a deposit/withdrawal succeeding. A synchronous call here would make `core-banking-service`'s availability depend on `notification-service` being up, for no reason — the opposite of why services are split.

### `transfer-service` → `core-banking-service` (Customer-Supplier)

- **Call:** gRPC (or internal API), synchronous — debit the sending account, credit the receiving account.
- **Deliberately scoped to *external* transfers only.** A transfer between two of your own accounts never needs this path — both accounts live in `core-banking-service`'s single database, so that case is just one local transaction (see "Design Note" below). `transfer-service` exists specifically for money crossing the boundary of this system entirely.

### `transfer-service` → external payment rail (no DDD pattern applies — outside your system)

- **Call:** whatever protocol the external rail uses (stubbed by a local `external-bank-simulator` for learning).
- This is the one relationship in the whole system where a **saga** is genuinely required: the external system is outside your transaction boundary, can fail or time out independently, and a partial failure (local debit succeeded, external send failed or is unknown) needs either a compensating credit or an idempotent retry — not a rollback, since there's no shared transaction to roll back.

### `api-gateway` → everything

- **Call:** HTTP, routing only. The gateway must not become a second place where business rules live — any validation here beyond "is this a well-formed, authenticated request" belongs in the owning service.

## Design Note: why internal transfers are not a saga

The original roadmap framed `transfer-service` as "where sagas appear." Per [ADR-0004](./adr/ADR-0004.md), all accounts live in **one** `core-banking-service` database — nothing is sharded. A transfer between two of your own accounts is mechanically two ledger entries in one transaction, the same pattern as deposit/withdraw. Treating that as a saga would be manufacturing a distributed-transaction problem that doesn't actually exist here. The genuine saga scope is the external-bank boundary, documented above — if accounts are ever sharded across multiple `core-banking-service` databases later, this note should be revisited.
