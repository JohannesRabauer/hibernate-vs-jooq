---
title: "jOOQ vs Hibernate — Be explicit, stay sane"
author: ""
date: 2025-12-18
transition: slide-left
theme: default
class: lead
background: https://picsum.photos/seed/mountainforest/1600/900
---

<style>
/* Remove theme overlay and force light appearance so images show clearly */
.sli-bg::before, .sli-bg::after, .slide::before { display: none !important; background: none !important; }
.slide { color: #111 !important; }
.slide h1, .slide h2, .slide h3 { color: #111 !important; -webkit-text-fill-color: #111 !important; }
img.slide-image { width: 100%; height: 15vh; max-height: 20vh; object-fit: cover; border-radius: 8px; box-shadow: 0 6px 20px rgba(0,0,0,0.08); margin: 0.8rem 0; }
@media (max-width: 768px) {
  img.slide-image { height: 22vh; max-height: 26vh; }
}
/* Force a light slide background */
.slide { background-color: #ffffff !important; }

/* Constrain code block width in side-by-side examples */
.slide div[grid] pre, .slide div[grid] code {
  max-width: 42ch;
  overflow-x: auto;
  white-space: pre;
  display: block;
}

/* Specific tweaks for N+1 example and small images */
.n1-left pre { max-width: 36ch; }
img.slide-image.small { height: 10vh; max-height: 12vh; }
@media (max-width: 768px) {
  img.slide-image.small { height: 16vh; max-height: 18vh; }
}

</style>

# jOOQ vs Hibernate ⚔️

> When "no SQL knowledge required" becomes a lie — pick predictability.

<!--
Notes:
- Predictability wins: understand the SQL your app runs.
- Short: learn SQL early to avoid hidden ORM surprises.
-->

---
incremental: true
background: https://picsum.photos/seed/lake/1600/900
class: text-left
---

## TL;DR

<img class="slide-image" src="https://picsum.photos/seed/lakereflection/1600/900" />

- Hibernate — Magic ❌
- jOOQ — SQL ✅

<!--
Notes:
- jOOQ = SQL: you write (type-safe) queries directly so behavior is explicit.
- Hibernate hides SQL which can cause unexpected behavior you must debug later.
-->

---
incremental: true
background: https://picsum.photos/seed/pathriver/1600/900
class: text-left
---

## Outline

<img class="slide-image" src="https://picsum.photos/seed/pathriver/1600/900" />

- Leak 🔍
- Compare ⚖️
- Debug ⚠️
- Test 🧪
- Choose ✅

<!--
Notes:
- Leak: abstraction hides lifecycle behavior (lazy, flush, cascades).
- Compare: see examples to understand trade-offs.
- Debug: SQL is easier to reason about and tune.
- Test: DB-level assertions are simpler with explicit SQL.
- Choose: pick the tool that matches your needs.
-->

---
incremental: true
background: https://picsum.photos/seed/fogforest/1600/900
class: text-left
---

## 1) Leak

<img class="slide-image" src="https://picsum.photos/seed/fogforest/1600/900" />

- Lifecycle magic ⚠️
- Lazy & Cascades 🧩
- You learn SQL 📚

<!--
Notes:
- Hibernate features (lazy loading, cascades, flush) add implicit lifecycle rules.
- These rules can cause surprising SQL or state changes you didn't intend.
-->

---
incremental: true
background: https://picsum.photos/seed/riverbridge/1600/900
class: text-left
---

## 2) Read

<img class="slide-image" src="https://picsum.photos/seed/riverbridge/1600/900" />

<div grid="~ cols-2 gap-6">
<div>
<strong>Hibernate</strong>

- Object graph
- Possible N+1

```java
// Load orders for a customer
List<Order> orders = em.createQuery(
  "select o from Order o where o.customer.id = :c", Order.class)
  .setParameter("c", customerId)
  .getResultList();

for (Order o : orders) {
  // if items are LAZY, this triggers one query per order -> N+1
  System.out.println(o.getItems().size());
}
```

</div>
<div>
<strong>jOOQ</strong>

- Explicit join
- Predictable rows

```java
var rows = ctx.select()
  .from(ORDERS)
  .join(ORDER_ITEMS).on(ORDER_ITEMS.ORDER_ID.eq(ORDERS.ID))
  .where(ORDERS.CUSTOMER_ID.eq(customerId))
  .fetch();

// You get all data in one query; map explicitly to DTOs
```

</div>
</div>

<!--
Notes:
- The Hibernate example shows how lazy associations can produce N+1 queries during iteration.
- The jOOQ example uses an explicit join so the SQL is obvious and predictable; you control what data is fetched.
-->

---
incremental: true
background: https://picsum.photos/seed/meadoworchard/1600/900
class: text-left
---

## 2b) Create

<img class="slide-image" src="https://picsum.photos/seed/meadoworchard/1600/900" />

<div grid="~ cols-2 gap-6">
<div>
<strong>Hibernate</strong>

- Cascade magic
- Hidden ordering

```java
Invoice invoice = new Invoice();
invoice.setCustomer(customer);
invoice.addItem(new InvoiceItem(...));
em.persist(invoice);
// On flush/commit Hibernate decides order and executes INSERTs; generated keys may be populated only after flush()
// If you rely on the invoice id immediately you may need em.flush() first
```

</div>
<div>
<strong>jOOQ</strong>

- Explicit insert
- Batch items

```java
var id = ctx.insertInto(INVOICE)
  .columns(INVOICE.CUSTOMER_ID, INVOICE.TOTAL)
  .values(customerId, total)
  .returning(INVOICE.ID)
  .fetchOne()
  .getId();

ctx.batchInsert(items.stream().map(it ->
  DSL.insertInto(INVOICE_ITEM)
     .set(INVOICE_ITEM.INVOICE_ID, id)
     .set(INVOICE_ITEM.AMOUNT, it.getAmount())
).collect(Collectors.toList())).execute();
```

</div>
</div>

<!--
Notes:
- Hibernate's cascade/flush model hides the exact SQL and ordering; that can surprise you (e.g., needing flush to obtain generated keys).
- jOOQ shows the exact sequence: insert parent, read id, then insert children — behavior is explicit and testable.
-->

---
incremental: true
background: https://picsum.photos/seed/cliffwave/1600/900
class: text-left
---

## 3) Debug

<img class="slide-image" src="https://picsum.photos/seed/cliffwave/1600/900" />

- N+1 🔁
- Dirty updates ⚠️
- Session boundaries ⏳
- jOOQ = SQL ✅

<div grid="~ cols-2 gap-6">
<div>
<strong>Hibernate — Dirty checking</strong>

```java
Order order = em.find(Order.class, orderId);
order.setStatus(Status.CANCELLED); // no explicit update call
// on flush/commit Hibernate detects the change and emits:
// UPDATE orders SET status = ? WHERE id = ?
```

- Implicit updates save boilerplate but can be surprising if you mutate entities from helper methods or across layers.

</div>
<div>
<strong>jOOQ — Explicit update</strong>

```java
int updated = ctx.update(ORDERS)
  .set(ORDERS.STATUS, Status.CANCELLED.name())
  .where(ORDERS.ID.eq(orderId))
  .execute();

if (updated == 0) {
  // handle missing row / concurrency conflict
}
```

- Explicit updates make side effects obvious and easier to test.

</div>
</div>

<!--
Notes:
- Dirty checking: Any managed entity field changes are detected on flush and persisted automatically; this is convenient but can lead to unexpected updates if you mutate entities in utility code.
- Session boundaries: changes are only tracked while entity is managed; modifying detached entities requires merge or explicit updates.
- To avoid surprises: prefer explicit updates for critical operations, keep entities small/immutable, or use DTOs for cross-layer data; jOOQ makes updates explicit and testable.
- Watch out for mutable collections: changing a collection in-place (e.g., list.add/remove) can mark the entity dirty and trigger updates or cascade operations.
-->
---
incremental: true
background: https://picsum.photos/seed/fieldsflowers/1600/900
class: text-left
---

## 4) Test

<img class="slide-image" src="https://picsum.photos/seed/fieldsflowers/1600/900" />

- Hibernate: brittle tests ⚠️
- jOOQ: DB snapshots ✅

<div grid="~ cols-2 gap-6">
<div>
<strong>Hibernate</strong>

```java
// JUnit + JPA example that can be brittle if you rely on managed state
@Test
@Transactional
void testCreateInvoice_shouldPersistItems() {
  Invoice invoice = new Invoice();
  invoice.addItem(new InvoiceItem("widget", 1));
  em.persist(invoice);

  // Without an explicit flush/clear, assertions that read via a new EntityManager or raw JDBC
  // can fail because data is still only in the persistence context.
  em.flush();
  em.clear();

  Integer count = jdbcTemplate.queryForObject(
    "SELECT count(*) FROM invoice_item WHERE invoice_id = ?", Integer.class, invoice.getId());
  assertEquals(1, count);
}
```

</div>
<div>
<strong>jOOQ</strong>

```java
// DB-level assertion is simple and deterministic with Testcontainers + jOOQ
@Test
void testCreateInvoice_directSql() {
  var id = ctx.insertInto(INVOICE)
    .columns(INVOICE.CUSTOMER_ID, INVOICE.TOTAL)
    .values(customerId, 100.0)
    .returning(INVOICE.ID)
    .fetchOne()
    .getId();

  ctx.insertInto(INVOICE_ITEM).set(INVOICE_ITEM.INVOICE_ID, id).set(INVOICE_ITEM.AMOUNT, 1).execute();

  int items = ctx.selectCount().from(INVOICE_ITEM).where(INVOICE_ITEM.INVOICE_ID.eq(id)).fetchOne(0, int.class);
  assertEquals(1, items);
}
```

</div>
</div>

<!--
Notes:
- "Brittle" tests rely on internal ORM lifecycle details (flush timing, cascades, entity state) that may change with minor refactors or different test setups.
- Causes: reliance on persistence context, lazy-loading, implicit flush on transaction boundaries, second-level caches, or test isolation differences.
- Mitigations:
  - Prefer DB-level assertions for behavioral verification (use JDBC, jOOQ, or queries) rather than assuming persistence context visibility.
  - Use `em.flush()` and `em.clear()` before assertions that read the DB directly, or read via a fresh EntityManager.
  - Use Testcontainers for a realistic DB and consistent environment.
  - Keep unit tests focused (mock ORM behavior) and use small integration tests for DB behavior.
- jOOQ tests that assert DB rows/snapshots tend to be more deterministic and easier to reason about because they check the actual persisted state.
-->


---
incremental: true
background: https://picsum.photos/seed/foresttrail/1600/900
class: text-left
---

## 5) Tools

<img class="slide-image" src="https://picsum.photos/seed/foresttrail/1600/900" />

- jOOQ: typed SQL ✨
- Hibernate: quick CRUD 🪄

<!--
Notes:
- jOOQ generates types for tables/columns so IDEs catch mistakes early.
- Hibernate helps model objects, but ORM debugging requires deeper understanding.
-->

---
incremental: true
background: https://picsum.photos/seed/sunrise/1600/900
class: text-left
---

## Choose

<img class="slide-image" src="https://picsum.photos/seed/sunrise/1600/900" />

- Hibernate: CRUD 🧩
- jOOQ: Performance ⚡

<!--
Notes:
- Use Hibernate for rapid CRUD and complex object graphs when SQL complexity is low.
- Use jOOQ when you need performance, complex queries, or deterministic behavior.
-->

---

## Checklist

<img class="slide-image" src="https://picsum.photos/seed/checklist/1600/900" />

- SQL perf? → jOOQ
- Quick CRUD? → Hibernate
- Debug & clarity? → jOOQ

<!--
Notes:
- SQL perf?: measure and prefer explicit queries to optimize hotspots.
- Quick CRUD?: Hibernate reduces boilerplate and speeds up development.
- Debug & clarity?: jOOQ reduces surprises and makes reasoning easier.
-->

---

## Example: N+1

<div grid="~ cols-2 gap-6">
<div class="n1-left">
<strong>Hibernate</strong>

```java
for (Order o : orders) {
  System.out.println(o.getItems().size()); 
  // triggers per-order query
}
```

- N+1 🔁

</div>
<div>
<strong>jOOQ</strong>

```sql
SELECT o.id, i.* FROM orders o 
JOIN order_items i ON i.order_id = o.id
```

- Explicit join ✅

</div>
</div>

<!--
Notes:
- Hibernate loop accesses lazy collections and can trigger one query per item (N+1).
- An explicit join returns all rows once; that's what you build with jOOQ.
-->

---
incremental: true
background: https://picsum.photos/seed/sunset/1600/900
class: text-left
---

## Update / Optimistic locking

<img class="slide-image small" src="https://picsum.photos/seed/updatepatch/1600/900" />

<div grid="~ cols-2 gap-6">
<div>
<strong>Hibernate</strong>

```java
@Entity
public class Invoice {
  @Id @GeneratedValue
  private Long id;
  @Version
  private Long version;
  private BigDecimal total;
}
// Usage
Invoice inv = em.find(Invoice.class, id);
inv.setTotal(newTotal);
// commit -> flush -> 
// UPDATE invoice SET total = ?, version = version + 1 
// WHERE id = ? AND version = ?
```

</div>
<div>
<strong>jOOQ</strong>

```java
int updated = ctx.update(INVOICE)
  .set(INVOICE.TOTAL, newTotal)
  .where(
    INVOICE.ID.eq(id).and(INVOICE.VERSION.eq(version))
  )
  .execute();

if (updated == 0) {
  // concurrency conflict -> reload & retry
}
```

</div>
</div>

<!--
Notes:
- Hibernate handles versioning for you but implicit flushes can surprise you; you must understand when flush occurs.
- With jOOQ you write defensive updates and check affected rows, which makes conflict handling explicit and testable.
-->

---
incremental: true
background: https://picsum.photos/seed/aggregation/1600/900
class: text-left
---

## Aggregation & complex queries

<img class="slide-image" src="https://picsum.photos/seed/aggregation/1600/900" />

<div grid="~ cols-2 gap-6">
<div>
<strong>Hibernate</strong>

```java
List<Tuple> stats = em.createQuery(
  "select i.customer.id, sum(i.total) from Invoice i group by i.customer.id", Tuple.class)
  .getResultList();

// For window functions you often need native SQL
List<Object[]> rows = em.createNativeQuery(
  "select *, row_number() over(partition by customer_id order by created_at desc) rn from orders")
  .getResultList();
```

</div>
<div>
<strong>jOOQ</strong>

```java
var result = ctx.select(ORDERS.CUSTOMER_ID, DSL.sum(ORDERS.TOTAL).as("sum_total"))
  .from(ORDERS)
  .groupBy(ORDERS.CUSTOMER_ID)
  .fetch();

var window = ctx.select(ORDERS.fields())
  .select(DSL.rowNumber().over().partitionBy(ORDERS.CUSTOMER_ID).orderBy(ORDERS.CREATED_AT.desc()).as("rn"))
  .from(ORDERS)
  .fetch();
```

</div>
</div>

<!--
Notes:
- Hibernate can express many aggregations, but complex SQL often forces native queries or result transformers.
- jOOQ embraces SQL features (windows, CTEs), so complex analytics queries stay readable and maintainable.
-->

---
incremental: true
background: https://picsum.photos/seed/bulkops/1600/900
class: text-left
---

## Bulk updates & deletes

<img class="slide-image" src="https://picsum.photos/seed/bulkops/1600/900" />

<div grid="~ cols-2 gap-6">
<div>
<strong>Hibernate</strong>

```java
int affected = em.createQuery("update Product p set p.available = false where p.soldOut = true")
  .executeUpdate();
em.clear(); // important to avoid stale entities
```

</div>
<div>
<strong>jOOQ</strong>

```java
int affected = ctx.update(PRODUCT)
  .set(PRODUCT.AVAILABLE, false)
  .where(PRODUCT.SOLD_OUT.eq(true))
  .execute();
// no persistence context to manage
```

</div>
</div>

<!--
Notes:
- JPQL bulk updates are efficient but you must manage the persistence context to avoid stale in-memory entities.
- With jOOQ you operate directly on the DB and then reload any needed entities, which makes side-effects explicit.
-->

---
incremental: true
background: https://picsum.photos/seed/pagination/1600/900
class: text-left
---

## Pagination & fetching strategies

<img class="slide-image" src="https://picsum.photos/seed/pagination/1600/900" />

<div grid="~ cols-2 gap-6">
<div>
<strong>Hibernate</strong>

```java
List<Order> page = em.createQuery("select o from Order o join fetch o.items where o.customer.id = :c", Order.class)
  .setParameter("c", customerId)
  .setFirstResult(20)
  .setMaxResults(10)
  .getResultList();

// Note: join fetch + pagination can be tricky; often fetch ids first then fetch associations.
```

</div>
<div>
<strong>jOOQ</strong>

```java
var page = ctx.selectFrom(ORDERS)
  .where(ORDERS.CUSTOMER_ID.eq(customerId))
  .orderBy(ORDERS.CREATED_AT.desc())
  .limit(10)
  .offset(20)
  .fetch();

// Keyset pagination example
var keyset = ctx.selectFrom(ORDERS)
  .where(ORDERS.CUSTOMER_ID.eq(customerId).and(ORDERS.CREATED_AT.lt(lastSeenDate)))
  .orderBy(ORDERS.CREATED_AT.desc())
  .limit(10)
  .fetch();
```

</div>
</div>

<!--
Notes:
- Hibernate pagination is convenient for entities, but joining associations or lazy-loading can reintroduce extra queries.
- jOOQ encourages selecting exactly the columns you need and using efficient pagination strategies (keyset for large offsets).
-->

---
incremental: true
background: https://picsum.photos/seed/sunset/1600/900
class: text-left
---

## Final thought 💡

- Convenience helps — until magic hurts.
- jOOQ trades convenience for clarity & control.

<!--
Notes:
- Convenience is great until hidden behavior costs time to debug.
- Favor explicit SQL for clarity and long-term maintainability when appropriate.
-->