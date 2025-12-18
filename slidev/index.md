---
title: "jOOQ vs Hibernate — Be explicit, stay sane"
author: ""
date: 2025-12-18
theme: moon
class: lead
---

# jOOQ vs Hibernate ⚔️

> When "no SQL knowledge required" becomes a lie — pick predictability.

---

## TL;DR

- **Hibernate** promises to hide SQL and let you think in objects. ✅
- **jOOQ** exposes SQL as the primary API — you write real SQL, type-safe and composable. ✅

**Core point:** with Hibernate you eventually must understand SQL *and* what Hibernate does; with jOOQ you must understand SQL up front and there is far less magic.

---

## Outline

1. Why abstractions leak
2. Side-by-side examples
3. Debugging & performance
4. Testing & tooling
5. When to prefer jOOQ vs Hibernate

---

## 1) Why the abstraction leaks

- Hibernate abstracts SQL: object identity, lazy loading, flush, cascades, proxies, first-level cache, dirty checking 🔁
- These features are handy — but they add hidden lifecycle & side-effects.
- When queries or performance matter, you end up reading generated SQL and learning the same SQL you tried to avoid.

> The promise "you don't need to know SQL" turns false fast for non-trivial apps.

---

## 2) Side-by-side: simple read

.columns
.left[
### Hibernate (JPQL / repository)

```java
List<Order> orders = em.createQuery(
  "select o from Order o join fetch o.items where o.customer.id = :id", Order.class)
  .setParameter("id", customerId)
  .getResultList();
```

- Pros: object graph returned
- Cons: must know fetch strategy to avoid N+1
```
]
.right[
### jOOQ (SQL first)

```java
var r = ctx.select()
  .from(ORDERS)
  .join(ORDER_ITEMS).on(ORDER_ITEMS.ORDER_ID.eq(ORDERS.ID))
  .where(ORDERS.CUSTOMER_ID.eq(customerId))
  .fetch();

// Map rows explicitly to DTOs
```

- Pros: explicit SQL => known joins/rows
- Cons: you write SQL (but it's type-safe and composable)
```

---

## 2b) Side-by-side: create invoice with items

.columns
.left[
### Hibernate (cascades & entity graph)

```java
Invoice invoice = new Invoice();
invoice.setCustomer(customer);
invoice.addItem(new InvoiceItem(...));
em.persist(invoice);
// Hidden work: order of SQL, generated keys, flush timing
```

- Magic: cascade persist, flush on transaction.commit()
- Problem: implicit multiple statements, surprising order
```
]
.right[
### jOOQ (explicit inserts)

```java
var id = ctx.insertInto(INVOICE)
  .columns(INVOICE.CUSTOMER_ID, INVOICE.TOTAL)
  .values(customerId, total)
  .returning(INVOICE.ID)
  .fetchOne()
  .getId();

ctx.batchInsert(items.map(item ->
  DSL.insertInto(INVOICE_ITEM)
     .set(INVOICE_ITEM.INVOICE_ID, id)
     ...)).execute();
```

- Everything is explicit: 1 insert for invoice, then batch for items
- No hidden flush/ordering surprises
```

---

## 3) Debugging & performance ⚠️

- Hibernate issues you will encounter:
  - N+1 selects (because of lazy collections)
  - Unexpected updates due to dirty checking
  - Session/transaction boundaries affecting results
  - Second-level cache causing stale results
- jOOQ gives you: precise SQL control — easier to reason about performance and index usage

> When performance debugging, reading a SQL statement beats chasing lifecycle bugs.

---

## 4) Testing & migrations 🧪

- With Hibernate you must test both mapping *and* ORM behavior (flush timing, cascades). Tests can be brittle to internal lifecycle assumptions.
- With jOOQ, tests are often more straightforward — assert SQL result sets / snapshots and DB state. Also pairs well with migrations (Flyway/Liquibase) because you test at the SQL level.

---

## 5) Tooling & maintainability

- jOOQ:
  - Auto-generated types from schema — IDE autocomplete for tables/columns
  - SQL is first-class — easier to reason about schema changes
  - No magic => easier code review for DB operations
- Hibernate:
  - Great for CRUD with simple domain logic
  - Hides complexity until it leaks — then you need deep ORM knowledge

---

## When to choose which? ✅

- Prefer **Hibernate** when:
  - You have simple CRUD, complex object graphs, and want rapid domain modeling
  - You benefit from automatic state management and cascading

- Prefer **jOOQ** when:
  - You need predictable SQL, complex queries, or performance-sensitive code
  - Your team is comfortable with SQL and wants explicit control

---

## Quick checklist for migration or greenfield decisions

- Do we need advanced SQL / performance tuning? → jOOQ
- Do we prefer object graph convenience and are OK debugging ORM quirks? → Hibernate
- Want deterministic behavior & easier debugging? → jOOQ

---

## Example: debugging N+1

.columns
.left[
Hibernate:
```java
for(Order o : orders) {
   System.out.println(o.getItems().size()); // triggers additional query per order
}
```
]
.right[
jOOQ:
```sql
SELECT o.id, i.* FROM orders o JOIN order_items i ON i.order_id = o.id
```

- jOOQ's SQL shows you the join — no surprises

---

## Final thought 💡

- ORM convenience can be a productivity win — until edge-cases make the abstraction cost more than its benefit.
- jOOQ forces you to learn SQL up front, trading off some convenience for clarity, control, and predictable performance.

---

# Thanks — Questions? ✨

> Slide source: `slidev/index.md` in the repo
