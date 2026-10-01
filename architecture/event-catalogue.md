# Event Catalogue — v1

> Companion to the [context map](./context-map.md). The authoritative registry of every event carried on Kafka — the equivalent of an OpenAPI spec for the event-driven side of the system. An event appears here before any consumer is built against it; a consumer existing is what justifies an event's existence (see "Deliberately not yet catalogued" below).

## Topic Naming Convention

```
<domain>.<aggregate>.<event-name>.<version>
```

Example: `banking.ledger.transaction-posted.v1`. Decided once, up front — renaming a topic later is a breaking change for every consumer, so this isn't revisited per-event.

## Events

### `TransactionPosted`

| Field | Value |
|---|---|
| Topic | `banking.ledger.transaction-posted.v1` |
| Publisher | `core-banking-service` |
| Consumer(s) | `notification-service` |
| Trigger | A successful deposit or withdrawal (a new `LedgerEntry` row) |
| Partition key | `accountId` — guarantees events for one account are processed in order; no ordering guarantee *across* accounts |
| Delivery semantics | At-least-once (Kafka's default). Consumers must be idempotent — a redelivered event must not double-process. `notification-service` should dedupe by `ledgerEntryId` (built in M4, alongside the dead-letter topic) |
| Schema format | Protobuf, defined once in the shared `contracts/` Gradle module (M3) — not copy-pasted per service |

**Payload fields:**

| Field | Type | Notes |
|---|---|---|
| `eventId` | UUID | Unique per publish attempt — distinct from `ledgerEntryId`, since a retry of the same ledger entry reuses the same `ledgerEntryId` but gets a new `eventId` |
| `ledgerEntryId` | UUID | The `LedgerEntry.id` this event reports — the natural dedupe key for consumers |
| `accountId` | UUID | Partition key |
| `customerId` | UUID | Included so `notification-service` doesn't need to call back into `core-banking-service` or `identity-service` just to know who to notify |
| `type` | enum (`DEPOSIT`, `WITHDRAWAL`) | Matches `TransactionType` |
| `amount` | decimal | The transaction amount |
| `balanceAfter` | decimal | The resulting balance, matching `LedgerEntry.balanceAfter` |
| `occurredAt` | timestamp | When the ledger entry was created, not when the event was published (these can differ once the outbox pattern is added in M4) |

## Deliberately Not Yet Catalogued

Per the "never jump ahead" project rule, these are named here only so they aren't forgotten — not built, and not given topic names yet, since an event with no real consumer is speculative scope creep:

- **`CustomerRegistered`** (would be published by `identity-service`) — no consumer exists yet. Becomes real if the onboarding-choreography saga (noted as a stretch goal in [ADR-0004](./adr/ADR-0004.md)'s discussion) is ever built.
- **`AccountOpened`** (would be published by `core-banking-service`) — same reasoning; a natural extension once there's an actual consumer (e.g. a welcome notification).

## Related Decisions

- [ADR-0004](./adr/ADR-0004.md) — chose Kafka for inter-service events and the naive-publish-then-outbox learning sequence (M3/M4).
- [context-map.md](./context-map.md) — documents *why* `core-banking-service` → `notification-service` is event-driven rather than a synchronous call.
