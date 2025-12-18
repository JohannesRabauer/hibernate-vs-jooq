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

</div>
<div>
<strong>jOOQ</strong>

- Explicit join
- Predictable rows

</div>
</div>

<!--
Notes:
- Hibernate returns object graphs but fetch strategies can trigger extra queries (N+1).
- jOOQ expresses joins directly: you know exactly what query runs and what rows you get.
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

</div>
<div>
<strong>jOOQ</strong>

- Explicit insert
- Batch items

</div>
</div>

<!--
Notes:
- Hibernate uses cascades and flush semantics; inserts/updates may happen implicitly.
- jOOQ does each insert explicitly (invoice then items), giving predictable SQL and order.
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

<!--
Notes:
- Common issues: N+1 selects and unexpected updates from dirty checking.
- Reading the SQL helps you find hotspots and use indexes; jOOQ makes that direct.
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

<!--
Notes:
- Hibernate tests must cover mapping and lifecycle behaviors (can be brittle).
- With jOOQ you can assert DB rows and snapshots directly, which simplifies verification.
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