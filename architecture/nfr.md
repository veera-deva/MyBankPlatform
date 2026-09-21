# Non-Functional Requirements (NFR) — v1

> **Note on provenance:** like the FRD, this document is a reconstruction. The original NFR discussion happened in chat during Phase 0 and was never saved to a file. This version is rewritten from the decisions actually confirmed and, where possible, from what was subsequently built and verified in Phase 1 — so these aren't aspirational, they're backed by working, tested code. Reconstructed 2026-08-10.

## Non-Functional Requirements

| ID | Requirement | Status |
|----|-------------|--------|
| NFR-1 | **Correctness under concurrency.** Two simultaneous operations on the same account (e.g. two withdrawals) must never corrupt the balance or allow an account to be overdrawn. | Implemented via pessimistic row locking in `LedgerService` (`AccountRepository.lockById`), proven by a Testcontainers-based concurrency test that fires simultaneous withdrawals against an account that can only cover one. |
| NFR-2 | **Auditability.** Every balance change must be traceable through an immutable, append-only record — no balance-affecting operation may overwrite or delete history. | Implemented via the `ledger_entries` table (insert-only in application code) and ADR-0002 (balance is derived from the ledger, never stored as a mutable column). |
| NFR-3 | **Monetary correctness.** All monetary values must use exact decimal arithmetic. Floating-point types must never be used for money. | Implemented via `NUMERIC(19,2)` columns and `BigDecimal` throughout the domain/service layer. |
| NFR-4 | **Resource identifiers must not be sequentially guessable.** Exposing predictable integer IDs for customers/accounts/transactions would allow enumeration of other customers' data (IDOR). | Implemented via UUID primary keys across all entities. |
| NFR-5 | **Credentials must never be stored in plaintext.** | Passwords are stored only as hashes (`customers.password_hash`); actual hashing (bcrypt/Argon2) is part of the Phase 2 Authentication task. |
| NFR-6 | **Authentication must be stateless.** No server-side session state, so the API can scale horizontally without sticky sessions. | Decided via ADR-0003 (JWT); implementation is part of the Phase 2 Authentication task. |
| NFR-7 | **Schema changes must be deterministic and environment-independent.** Dev, staging, and production databases must converge to the same schema state via the same instructions, not manual/ad-hoc changes. | Implemented via versioned Flyway migrations (`V1`–`V7` as of Phase 1 completion), with an established immutability rule: applied migrations are never edited, only superseded by new ones. |

## Related Decisions

- [ADR-0001](./adr/ADR-0001.md) — Modular monolith over microservices for v1.
- [ADR-0002](./adr/ADR-0002.md) — Ledger as source of truth.
- [ADR-0003](./adr/ADR-0003.md) — Stateless JWT authentication.

(Adjust the ADR paths above if your actual ADR files live elsewhere — they weren't found on disk as of this document's creation, so these are placeholder references pending confirmation.)
