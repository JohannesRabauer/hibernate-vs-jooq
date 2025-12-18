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
.slide { background-color: #ffffff !important; }</style>

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
<div>
<strong>Hibernate</strong>

```java
for (Order o : orders) {
  System.out.println(o.getItems().size()); // triggers per-order query
}
```

- N+1 🔁

</div>
<div>
<strong>jOOQ</strong>

```sql
SELECT o.id, i.* FROM orders o JOIN order_items i ON i.order_id = o.id
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

## Final thought 💡

- Convenience helps — until magic hurts.
- jOOQ trades convenience for clarity & control.

<!--
Notes:
- Convenience is great until hidden behavior costs time to debug.
- Favor explicit SQL for clarity and long-term maintainability when appropriate.
-->