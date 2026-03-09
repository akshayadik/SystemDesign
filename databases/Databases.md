# Databases (System Design Notes)

## 1. Database Classification

### 1.1 Relational Databases (SQL)

* Schema-based (tables, rows, columns)
* Strong support for **ACID transactions**
* Rich querying via SQL
* Typically scale **vertically first**, then horizontally (sharding)

**Examples:** MySQL, PostgreSQL, Oracle

**Best for**

* Financial systems
* Strong consistency requirements
* Complex joins and constraints

---

### 1.2 Non-Relational Databases (NoSQL)

Designed to scale horizontally and handle large volumes of data.

#### a. Key-Value Stores

* Data stored as `<key, value>`
* Very fast reads/writes
* Limited querying

**Examples:** Redis, DynamoDB

**Use cases:** Caching, sessions, counters

---

#### b. Document Databases

* Semi-structured (JSON / BSON)
* Schema-flexible
* Queryable fields inside documents

**Examples:** MongoDB, CouchDB

**Use cases:** User profiles, content management

---

#### c. Column-Family (Wide-Column) Databases

* Data stored by column families
* Optimized for high write throughput
* Eventual consistency common

**Examples:** Cassandra, HBase

**Use cases:** Time-series data, analytics, logs

---

#### d. Graph Databases

* Nodes and edges with relationships
* Optimized for graph traversal

**Examples:** Neo4j

**Use cases:** Social networks, recommendation engines

---

## 2. Data Replication

Replication improves **availability, fault tolerance, and read scalability**.

### 2.1 Replication Modes

#### Synchronous Replication

* Write is acknowledged **after replicas confirm**
* Strong consistency
* Higher write latency

**Used when:** correctness > latency (banking)

---

#### Asynchronous Replication

* Leader responds immediately
* Replicas catch up later
* Risk of data loss on leader failure

**Used when:** availability and performance matter more

---

### 2.2 Replication Models

#### a. Single-Leader (Primary–Replica / Master–Slave)

* All writes go to leader
* Reads can be served from replicas

**Replication mechanisms**

* **Statement-based**: SQL statements replicated
* **Write Ahead Log (WAL)-based**: Binary log shipping
* **Logical replication**: Row-level changes

**Pros**

* Simple consistency model
* Easy conflict avoidance

**Cons**

* Leader is a write bottleneck

---

#### b. Multi-Leader

* Multiple leaders accept writes
* Used across regions

**Pros**

* Lower latency
* Regional independence

**Cons**

* Write conflicts
* Complex conflict resolution

---

#### c. Leaderless (Peer-to-Peer)

* No single leader
* Writes sent to multiple nodes

**Quorum rule**

```
(r + w) > n
```

* `r` = read replicas
* `w` = write replicas
* `n` = total replicas

**Used by:** Dynamo-style systems

---

## 3. Conflict Resolution

Occurs mainly in **multi-leader or leaderless systems**.

### Strategies

* **Last Write Wins (LWW)**

  * Simple but can lose updates
* **Application-level merge**

  * Domain-specific logic
* **Version vectors / clocks**

  * Detect causality

**Key trade-off:** simplicity vs correctness

---

## 4. Data Partitioning (Sharding)

Partitioning improves **scalability and throughput**.

### 4.1 Vertical Sharding

* Split by columns or features
* Example: user profile DB vs billing DB

**Pros**

* Clear separation of concerns

**Cons**

* Cross-shard queries become expensive

---

### 4.2 Horizontal Sharding

Split rows across shards.

#### a. Key-Range Based Sharding

* Data divided by value ranges

**Pros**

* Range queries efficient

**Cons**

* Hotspots (skewed keys)

---

#### b. Hash-Based Sharding

* Hash(key) → shard

**Pros**

* Even data distribution

**Cons**

* Range queries inefficient

---

#### c. Consistent Hashing

Minimizes data movement during scaling.

* **Fixed partitions**

  * Easier management
  * Rebalancing required
* **Dynamic partitions**

  * Better elasticity
  * More complex metadata

---

## 5. Indexing Strategy

Indexes improve read performance but impact writes.

### 5.1 Partitioned Indexes

#### Local Index (By Document / Shard)

* Index exists per shard
* Query fan-out required

**Pros:** scalable
**Cons:** higher query latency

---

#### Global Index (By Term / Field)

* Centralized index
* Direct shard lookup

**Pros:** faster reads
**Cons:** index becomes bottleneck

---

## 6. Transactions & Consistency (Missing but Critical)

### ACID Properties

* Atomicity
* Consistency
* Isolation
* Durability

### Isolation Levels

* Read Uncommitted
  * Transactions can read **uncommitted (dirty) data** from others.
  * Fast but unsafe; anomalies possible.
* Read Committed
  * Reads only **committed data**.
  * Prevents dirty reads, but **non-repeatable reads** can occur.
* Repeatable Read
  * Rows read once will **not change** during the transaction.
  * Prevents dirty and non-repeatable reads, but **phantom reads** may still happen.
* Serializable
  * Transactions behave as if executed **one after another**.
  * Strongest isolation, **lowest concurrency**.

### Distributed Transactions

* **2-Phase Commit (2PC)**
  * Coordinator asks participants to **prepare**, then **commit/abort**.
  * Ensures atomicity across services but is **blocking and slow**.
* **Saga Pattern**
  * Breaks a transaction into **local steps with compensations**.
  * Non-blocking and scalable, but **eventual consistency**.
  * Choreography - each local transaction publishes domain events that trigger local transactions in other services
  * Orchestration - an orchestrator (object) tells the participants what local transactions to execute

---

## 7. Consistency Models

* Strong Consistency
* Eventual Consistency
* Causal Consistency
* Read-your-writes

**CAP Theorem**

* Partition tolerance is mandatory
* Trade-off between Consistency and Availability

### Consistency vs Availability (CAP Trade-off)

* During a network partition:

  * **Consistency** → reject requests to avoid stale data
  * **Availability** → serve requests even if data may be stale
* Real systems **choose one per operation**, not globally.

---

## 8. Failure Modes & Recovery

* Replica lag
  * Followers fall behind leader due to async replication.
  * Causes **stale reads**.
* Split brain
  * Multiple nodes believe they are the leader.
  * Leads to **data divergence**.
* Data corruption
  * Data becomes invalid due to bugs, disk failure, or bit rot.
  * Silent and dangerous without checksums.
* Node loss
  * Node crashes or becomes unreachable.
  * Requires **replication and failover**.

**Recovery & Safety Mechanisms**
* Heartbeats
  * Periodic signals to check node health.
  * Used for failure detection.
* Leader election
  * Selects a new leader when the current one fails.
  * Prevents split brain.
* Checkpointing
  * Persist in-memory state to disk periodically.
  * Speeds up recovery.
* Backups & snapshots
  * Point-in-time copies of data.
  * Protect against **catastrophic data loss**.
---

## 9. “What Is Best?” → Trade-Off Framework

There is **no best database**, only **best fit**.

| Requirement            | Prefer                        |
| ---------------------- | ----------------------------- |
| Strong consistency     | SQL / Synchronous replication |
| High write throughput  | NoSQL, leaderless             |
| Global low latency     | Multi-leader                  |
| Complex queries        | Relational                    |
| Horizontal scalability | Sharded NoSQL                 |

---

## Tip (Very Important)

Always define database questions using this flow:

1. **Data model**
2. **Access pattern**
3. **Consistency requirement**
4. **Replication**
5. **Partitioning**
6. **Failure handling**
7. **Trade-offs**

