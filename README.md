```mermaid

erDiagram
    CUSTOMER ||--o{ INVOICE : places
    INVOICE ||--o{ INVOICE_ITEM : contains
    PRODUCT ||--o{ INVOICE_ITEM : appears_in
    CUSTOMER ||--o{ ADDRESS : has

    CUSTOMER {
        int customer_id PK
        string first_name
        string last_name
        string email
    }

    ADDRESS {
        int address_id PK
        int customer_id FK
        string street
        string city
        string country
    }

    INVOICE {
        int invoice_id PK
        int customer_id FK
        date invoice_date
        decimal total_amount
    }

    INVOICE_ITEM {
        int invoice_item_id PK
        int invoice_id FK
        int product_id FK
        int quantity
        decimal unit_price
    }

    PRODUCT {
        int product_id PK
        string product_name
        decimal price
    }
```
## Docker Compose Architecture

```mermaid
flowchart LR
        direction LR
        frontend[Frontend]
        backend_hibernate[backend-hibernate]
        backend_jooq[backend-jooq]
        postgres[(PostgreSQL)]
    end

    %% Frontend can call either backend
    frontend -->|calls / HTTP| backend_hibernate
    frontend -->|calls / HTTP| backend_jooq

    %% Backends contact the database
    backend_hibernate -->|JDBC| postgres
    backend_jooq -->|JDBC| postgres
```

This diagram shows that the `frontend` service calls either `backend-hibernate` or `backend-jooq`, and both backends connect to the `PostgreSQL` database.