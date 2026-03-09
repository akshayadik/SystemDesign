# System Design: Key Value Store

## 1. Requirements

### Functional Requirements

* **Core APIs:** Support `get(key)` and `put(key, value, context)` operations.
* **Configurable Consistency:** Allow applications to trade off consistency for higher availability ($r + w > n$).
* **Always-Writeable:** The system should prioritize writes even during network partitions (Availability over Consistency in CAP).
* **Hardware Heterogeneity:** Ability to add nodes with different capacities without a master-slave bottleneck.

### Non-Functional Requirements

* **Incremental Scalability:** Add/remove servers with minimal data movement.
* **Fault Tolerance:** No single point of failure; operation continues despite node crashes.
* **High Availability:** Low-latency responses and high uptime using peer-to-peer replication.

---

## 2. High-Level Architecture

The system follows a **Peer-to-Peer** decentralized architecture. There are no "master" nodes; every node can act as a **Coordinator** for a request.

### Core Components

* **Consistent Hashing (Ring):** Used to distribute keys across the cluster. Physical nodes are mapped to multiple **Virtual Nodes** to ensure even load distribution and handle hardware heterogeneity.
* **Replication Engine:** Replicates each data item at $n$ hosts. The coordinator replicates a key to the $n-1$ successor nodes in the ring (clockwise).
* **Gossip Protocol:** A decentralized membership and failure detection mechanism. Nodes randomly share their "token sets" (state/history) with peers to maintain a global view of the ring.

---

## 3. API Design

### `get(key)`

* **Input:** `key`
* **Output:** Returns an object or a collection of conflicting objects + **Context** (metadata/versioning).
* **Logic:** Coordinator requests the key from the top $n$ reachable nodes and waits for $r$ responses.

### `put(key, value, context)`

* **Input:** `key`, `value`, `context` (obtained from a previous `get` call).
* **Logic:** Coordinator creates a new version, updates the **Vector Clock**, and writes to $w$ nodes.

---

## 4. Storage & Data Management

### Data Integrity & Persistence

* **Local Storage:** Each node stores its assigned key ranges on disk.
* **Versioning (Vector Clocks):** To handle conflicts during concurrent writes or partitions, the system attaches a list of `(node, counter)` pairs to every object.
* **Anti-Entropy (Merkle Trees):** Nodes use Merkle trees to detect inconsistencies between replicas. This minimizes data transfer by only syncing the branches where hashes differ.

---

## 5. Key Services & Mechanisms

| Service/Mechanism | Purpose |
| --- | --- |
| **Sloppy Quorum** | Ensures $r$ or $w$ responses can be met even if the primary nodes for a key are down, by using the next healthy nodes in the ring. |
| **Hinted Handoff** | If a node is down, a "hint" is stored on a neighbor. Once the node returns, the neighbor "hands off" the missed data. |
| **Conflict Resolution** | If vector clocks show divergent histories (parallel updates), the system returns all versions to the client to resolve (similar to a Git merge). |
| **Quorum Tunables** | **(N, R, W)**: For example, $(3, 2, 2)$ provides a balance, while $(3, 1, 3)$ provides very fast reads but slow writes. |

---

