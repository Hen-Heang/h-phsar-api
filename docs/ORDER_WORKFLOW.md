# Order Lifecycle (Step 3C)

This document is the source of truth for order statuses, who can change them,
and how the change is recorded. It replaces the informal/drifted status
meanings that existed before Step 3C (see "Migration notes" below for what
changed and why).

## Lifecycle diagram

```text
CART
  ├── DRAFT
  │     ├── PENDING
  │     └── CANCELLED
  └── PENDING
        ├── PROCESSING
        │      └── DISPATCHED
        │             └── COMPLETED
        ├── REJECTED
        └── CANCELLED
```

## Status meanings

| Status | Meaning | Terminal? |
| --- | --- | --- |
| `CART` | Buyer's active, editable shopping cart. Not yet a "real" order. | no |
| `DRAFT` | Buyer saved the cart for later; not currently being edited. | no |
| `PENDING` | Submitted to the supplier, awaiting accept/reject. | no |
| `PROCESSING` | Supplier accepted; preparing the order. Stock is deducted here. | no |
| `DISPATCHED` | Supplier has sent the order out. | no |
| `COMPLETED` | Buyer confirmed receipt. | **yes** |
| `REJECTED` | Supplier declined a pending order. | **yes** |
| `CANCELLED` | Buyer cancelled a draft or a not-yet-accepted pending order. | **yes** |

A separate `DELIVERED` checkpoint (package physically arrived, ahead of the
buyer's own transaction confirmation) was evaluated and rejected: the codebase
has no second actor/event in the gap between dispatch and the buyer's receipt
confirmation, so it would carry no real workflow meaning. See
`OrderStatus`'s Javadoc.

## Transition matrix

| Current | Allowed next statuses |
| --- | --- |
| `CART` | `DRAFT`, `PENDING` |
| `DRAFT` | `PENDING`, `CANCELLED` |
| `PENDING` | `PROCESSING`, `REJECTED`, `CANCELLED` |
| `PROCESSING` | `DISPATCHED` |
| `DISPATCHED` | `COMPLETED` |
| `COMPLETED` | none (terminal) |
| `REJECTED` | none (terminal) |
| `CANCELLED` | none (terminal) |

Enforced in code by `OrderStatus.canTransitionTo` (`model/order/OrderStatus.java`)
and, atomically, by `OrderStatusRepository.updateStatusIfCurrent` — every
transition is a single guarded `UPDATE ... WHERE status_id = (current)`, so two
concurrent requests can never both apply conflicting transitions (same pattern
as the Step 3B stock-deduction fix).

## Actor responsibilities

**Buyer** may: create/modify their own cart; save as draft; submit a cart or
draft; cancel their own draft or not-yet-accepted pending order; confirm
receipt of a dispatched order (the only path to `COMPLETED`).

**Supplier** may: view orders for their own store; accept or reject a pending
order; dispatch a processing order. Supplier responsibility ends at dispatch —
a supplier can no longer mark an order complete (see "What changed" below).

**Admin**: no order-mutation capability exists or was added in this step —
future scope only.

## Cancellation vs rejection

Two distinct terminal outcomes, never conflated:

- **`REJECTED`** — the supplier's decision, from `PENDING` only.
- **`CANCELLED`** — the buyer's decision, from `DRAFT` or `PENDING` (before
  acceptance) only. Once `PROCESSING`, a buyer can no longer cancel.

Cancelled/rejected orders are retained (never physically deleted) so their
history survives. The one exception is an empty, never-submitted `CART`:
`DELETE /orders/cart/cancel` still physically deletes it, same as before —
there is nothing meaningful to keep history for. Deleting a saved `DRAFT`
(`DELETE /history/draft/{id}`) also still physically deletes it, unchanged
from Step 2C, to avoid changing that already-tested behavior; use the new
`POST /orders/{id}/cancel` instead when you want the cancellation recorded.

## Stock deduction

Happens exactly once, at `PENDING -> PROCESSING` (supplier acceptance), via
the Step 3B atomic guarded UPDATE. Nothing else deducts stock.

## History (audit trail)

`tb_order_status_history` is append-only — nothing updates or deletes a row.
Every transition through `OrderStatusServiceImpl.transitionOrder` writes
exactly one row: `order_id`, `previous_status_id` (null only for a fresh
cart's implicit starting state — no transition recorded for cart creation
itself), `new_status_id`, `changed_by_account_id`, `changed_by_role`
(`SUPPLIER`/`BUYER` — no FK, since those two roles live in separate account
tables and one FK can't conditionally target either), an optional `reason`,
and `changed_at`.

Read via:
- `GET /api/v1/buyers/orders/{id}/history` (buyer, own orders only)
- `GET /api/v1/suppliers/orders/{orderId}/history` (supplier, own store's orders only)

## Migration notes (existing data)

The original seed named the nine statuses `PENDING, PROCESSING, CONFIRMED,
SHIPPING, DELIVERED, COMPLETED, CANCELLED, REJECTED, DRAFT` (ids 1-9), but the
mapper layer never agreed with those names. `DatabaseInitializer
#migrateOrderLifecycleStatuses` documents the full audit and performs, once,
in this order:

1. Rename id 9 `DRAFT` → `CART` (matches its actual use everywhere).
2. Rename id 8 `REJECTED` → `DRAFT` (its dominant actual use).
3. Rename id 3 `CONFIRMED` → `DISPATCHED`.
4. Insert a new id 10 = `REJECTED`.
5. Split the id-8 collision: any `tb_order` row at status 8 that has a
   `tb_buyer_notification` row of type 5 ("Order Declined") is moved to the
   new id 10 — that notification is only ever created by
   `declinePendingOrder`, so its presence is reliable proof of rejection, not
   a saved draft (drafts create no notification at all). Every row is
   classified this way (no guessing); counts of both buckets are logged.

ids 4 (`SHIPPING`) and 5 (`DELIVERED`) are retired — no code path ever wrote
them (see "What changed" below) — and are left alone rather than reused.

## What changed (bugs fixed by this step)

- **Draft submission was permanently unreachable.** `checkForCartOrPending`
  always matched the draft being submitted against itself. Fixed by excluding
  the draft's own id.
- **Buyer's "confirm transaction" endpoint was permanently unreachable.** It
  required status 4, which nothing ever set. It's now a delegating alias of
  the same buyer receipt-confirmation transition as `markOrderAsArrived`.
- **Supplier's invoice view, and the identical buyer-side check, were
  permanently unreachable.** Both were gated on status 5, which nothing ever
  set. Now gated on `COMPLETED` (6).
- **Two actors could both complete the same order.** Supplier's
  `orderDelivered` and buyer's `markOrderAsArrived`/`confirmTransaction`
  performed the identical `DISPATCHED -> COMPLETED` transition. Per the target
  model, only the buyer may. Supplier's endpoint stays mapped (no 404 for
  existing clients) but now returns a clear 403.
- **Supplier's "all orders" and "order history" listings silently excluded
  completed and rejected orders**, because they filtered on the dead ids 4/5
  instead of the real terminal ids.
- **Supplier dashboard's "completed" tile, and the monthly/yearly
  completed-order charts, always showed 0**, for the same reason.

## Known, deliberately out-of-scope issue

`BuyerReportServiceImpl`/`BuyerReportMapper.xml` (buyer spending/category/
shop reports) hardcode status id `5` — the same dead id — in roughly a dozen
call sites, plus a backwards accepted/rejected id swap. That entire reporting
feature appears to have never worked. It was **not** touched here: "Reports"
is explicitly out of scope for this step, the bug predates it, and fixing it
correctly requires deciding what each metric should mean (a reporting-design
question, not a lifecycle-transition one) — recommended as a dedicated
follow-up.
