# Functional Requirements Document (FRD) — v1

> **Note on provenance:** this document is a reconstruction. The original FRD was drafted collaboratively during Phase 0 planning but was only ever discussed in chat, never saved to a file — the literal original wording was lost once that conversation aged out of context. This version reflects the scope and decisions that were confirmed at the time, rewritten fresh. Reconstructed 2026-08-10.

## Scope

v1 targets a mobile-first retail banking platform covering account creation, authentication, deposits, withdrawals, and balance/transaction visibility — deliberately minimal, so the underlying architecture (ledger-as-source-of-truth, concurrency-safe balance handling) can be built correctly before breadth is added.

## Functional Requirements

| ID | Requirement |
|----|-------------|
| FR-1 | A prospective customer can **register** with an email, password, and full name. Email must be unique across all customers. |
| FR-2 | A registered customer can **log in** with their email and password and receive an authenticated session (JWT). |
| FR-3 | An authenticated customer can **open one account**. Each customer may hold at most one account in v1. |
| FR-4 | An authenticated customer can **deposit** a positive amount into their account. |
| FR-5 | An authenticated customer can **withdraw** a positive amount from their account, up to and including their current available balance. A withdrawal exceeding the current balance must be rejected. |
| FR-6 | An authenticated customer can **view their current account balance** at any time. |
| FR-7 | An authenticated customer can **view their transaction history** — a list of past deposits and withdrawals on their account, each entry showing a timestamp, transaction type, amount, and the resulting balance after that transaction. |

## Explicitly Out of Scope for v1

- Multi-currency accounts or conversion
- Loans or credit products
- Card issuing
- Fraud detection
- International transfers
- Multiple accounts per customer
- Password reset / account recovery flows

These were deliberately deferred so v1 stays small enough to build the core ledger/concurrency architecture correctly, with room to extend later.

## Related Decisions

- [ADR-0002](./adr/ADR-0002.md) — Ledger as source of truth (balance is derived, never stored directly) governs how FR-4 through FR-7 are implemented.
- [ADR-0003](./adr/ADR-0003.md) — Stateless JWT authentication governs how FR-1 and FR-2 are implemented.

(Adjust the ADR paths above if your actual ADR files live elsewhere — they weren't found on disk as of this document's creation, so these are placeholder references pending confirmation.)
