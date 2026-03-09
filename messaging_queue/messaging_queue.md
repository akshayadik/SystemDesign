# System Design: The Distributed Messaging Queue

Designing a distributed cache is a critical challenge in distributed systems, requiring a balance between extreme performance and high availability. While your provided document focuses on **Messaging Queues**, many principles—like partitioning, replication, and the use of load balancers—apply to a **Distributed Cache Service**.

Here is a structured design for a Distributed Cache.

---

## 1. Requirements

### Functional Requirements

* **Put(key, value, TTL)**: Store a piece of data with an optional Time-to-Live.
* **Get(key)**: Retrieve the value associated with a key.
* **Delete(key)**: Manually remove an entry.
* **Eviction**: Automatically remove old data when memory is full (e.g., LRU).

### Non-Functional Requirements

* **Performance (Low Latency)**: Sub-millisecond response times for operations.
* **Scalability**: The system must handle an increasing number of requests and data by adding more nodes (Horizontal Scaling).
* **Availability**: The cache should remain accessible even if a node fails.
* **Durability (Optional)**: While primarily in-memory, some caches require persistence to disk to survive restarts.

---

## 2. API Design

A standard REST or RPC-based API is used for interaction.

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/v1/cache/{key}` | Retrieves the value. Returns 404 if expired or missing. |
| `PUT` | `/v1/cache/{key}` | Stores/Updates value. Request body includes `value` and `ttl`. |
| `DELETE` | `/v1/cache/{key}` | Explicitly removes the key from the cache. |

---

## 3. High/Low Design and Architecture

### High-Level Components

* **Client Library**: Implements the hashing logic to determine which cache node holds the data.
* **Configuration/Metadata Service**: Keeps track of the active cache nodes in the cluster (e.g., using ZooKeeper).
* **Cache Nodes**: The servers that store the actual data in RAM.

### Low-Level Logic

* **Sharding (Partitioning)**: To scale, data is divided across multiple servers using **Consistent Hashing** to ensure minimal data movement when nodes are added or removed.
* **Replication**: For high availability, each shard is replicated (Primary-Secondary model). If the primary node fails, a secondary node is promoted.

---

## 4. Storage and Algorithms

### Storage Layer

* **In-Memory**: Data is stored in RAM using a **HashMap** for $O(1)$ lookup.
* **Eviction Policy (LRU)**: Since RAM is finite, a **Doubly Linked List** combined with the HashMap is used to track the "Least Recently Used" items and evict them first.

### Key Algorithms

* **Consistent Hashing**: Maps keys and servers to a logical ring ($0$ to $2^{n}-1$). This prevents a total "cache miss" storm when scaling.
* **Expiration Algorithm**:
* **Passive**: Delete a key only when it is accessed and found to be expired.
* **Active**: A background thread periodically samples and deletes expired keys.



---

## 5. Evaluation of Requirements

| Requirement | Strategy for Compliance |
| --- | --- |
| **Performance** | Use of in-memory storage (RAM) and $O(1)$ data structures. |
| **Scalability** | Horizontal scaling via sharding and consistent hashing. |
| **Availability** | Data replication (Primary-Secondary) and automated failover. |
| **Durability** | Append-only files (AOF) or snapshots for recovery after crashes. |

---

