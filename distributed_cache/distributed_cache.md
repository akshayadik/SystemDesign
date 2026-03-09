# System Design: Distributed Cache
Based on the provided documentation, here is the redesigned architecture for a highly robust distributed cache system. This design focuses on horizontal scalability, high availability through redundancy, and low-latency performance using optimized data structures.

---

## 1. Requirements

### Functional Requirements

* **Insert Data:** Users must be able to insert entries (key-value pairs) into the cache.
* **Retrieve Data:** Users must be able to retrieve data corresponding to a specific key.

### Non-Functional Requirements

* **High Performance:** Both insert and retrieve operations must be fast to enable rapid data retrieval.
* **Scalability:** The system must scale horizontally without bottlenecks as requests increase.
* **High Availability:** The system must survive component failures, network issues, or power outages to avoid overloading the database.
* **Consistency:** Data should be consistent across different cache servers so clients receive up-to-date information.
* **Affordability:** The system should be designed using commodity hardware rather than specialized, expensive components.

---

## 2. High-Level Architecture

The system utilizes a **Cache Client** library residing within the application servers to manage data routing.

* **Cache Client:** Uses a hash and search algorithm (Consistent Hashing) to choose the correct cache server for any given request.
* **Cache Servers:** Dedicated nodes that store data in RAM for low-latency access.
* **Database:** The persistent storage layer that the cache servers interact with during a "cache miss".

---

## 3. Detailed (Low-Level) Architecture

### Internals of a Cache Node

Each node uses two primary data structures to manage entries efficiently:

1. **Hash Map:** Used to store and locate entries in RAM in constant time ($O(1)$).
2. **Doubly Linked List:** Used to order entries by access frequency to facilitate the **LRU (Least Recently Used)** eviction policy.

### Redundancy & Sharding

To eliminate Single Points of Failure (SPOF):

* **Sharding:** Data is divided into shards, with each shard stored on different physical servers.
* **Replicas:** Each shard consists of a **Primary (Leader)** node and multiple **Secondary (Follower)** nodes.
* **Replication:** Writes are typically performed synchronously within a data center to ensure consistency, while asynchronous replication may be used across data centers for availability.

---

## 4. API Design

The interface is kept simple to ensure high performance.

| Method | Parameters | Description |
| --- | --- | --- |
| `Insert(key, value)` | `key` (Unique ID), `value` (Data) | Stores data; returns acknowledgment or error. |
| `Retrieve(key)` | `key` | Returns the data object associated with the key. |

*Note: A Delete API is often unnecessary as the system handles expiration via TTL and eviction algorithms locally.*

---

## 5. Algorithm Flow: Consistent Hashing

The system uses **Consistent Hashing** to identify which server maintains a specific shard.

1. **Server Mapping:** Cache shards are placed on a logical ring.
2. **Key Routing:** When a request arrives, the client hashes the key to find its position on the ring and moves clockwise to the nearest shard.
3. **Efficiency:** Finding a key requires $O(\log N)$ time, where $N$ is the number of shards. This method minimizes re-hashing when servers are added or removed.

---

## 6. Configuration & Monitoring

To make the system robust, it requires automated management:

* **Configuration Service:** A centralized service (like Zookeeper) that monitors the health of cache servers. It notifies clients when servers are added or fail, ensuring a consistent view of the cluster.
* **Monitoring Service:** Logs and reports metrics to track the health and performance of the caching layer.

---

## 7. Handling High Load & Robustness

* **Hotkeys/Hot Shards:** If a specific shard is overloaded, the system can use multiple replica nodes for reads or perform further sharding within that key range.
* **Cache Miss Latency:** Performance is heavily dependent on the **Cache Hit Rate**. For example, a 5% miss rate with LRU results in much better "Effective Access Time" than a 10% miss rate with other algorithms.
* **Recovery:** Rejoining servers are not allowed to serve requests until they are confirmed to be up-to-date, preventing the serving of stale data.

In a distributed cache, locating a shard across different machines involves a coordinated dance between the **Cache Client**, a **Configuration Service**, and the **Sharded Servers**.

The process ensures that even if data is spread across dozens of machines, the client always knows exactly where to go without guessing.

---

## 8. Use Case Flow
### Shard Location and Request Handling

* **Consistent Hashing:** The Cache Client uses a consistent hashing algorithm to map a key to a specific shard. This ensures that the client consistently targets the correct machine for a specific set of data.
* **Centralized Metadata:** The client obtains the updated list of all available cache servers and their health status from a **Configuration Service**.
* **Shard Redundancy:** Each shard is typically composed of a **Primary (Leader)** node and **Replica (Follower)** nodes.
* **Request Routing:** Once the shard is identified via the hash, the client sends the request directly to that specific server using TCP or UDP protocols.

---

### Sequence Diagram: Handling a Cache Request

The following flow illustrates how a request is resolved when shards are distributed across multiple machines.

1. **Server List Retrieval:** Upon initialization or update, the **Cache Client** requests the latest cluster membership (server list) from the **Configuration Service**.
2. **Request Initiation:** The Application sends a `Retrieve(key)` request to the Cache Client.
3. **Hashing:** The Client hashes the `key` using **Consistent Hashing** to determine which shard (e.g., Shard B) holds that data.
4. **Target Selection:** The Client looks up the IP address of the **Primary node** for Shard B from its local (updated) configuration.
5. **Direct Communication:** The Client sends a `GET` request directly to the Cache Server for Shard B.
6. **Internal Lookup:** The Cache Server uses its internal **Hash Map** to find the pointer to the data and updates its **Doubly Linked List** to mark the item as recently used.
7. **Data Return:** The Cache Server returns the value to the Client, which then passes it back to the Application.

---

### Handling Failures and Redundancy

If the primary machine for a shard is down, the system maintains robustness through these mechanisms:

* **Health Monitoring:** The Configuration Service continuously monitors the health of all nodes.
* **Failover:** If the Primary node fails, a **Follower (Replica)** is promoted to Primary to continue serving requests.
* **Client Notification:** The Configuration Service notifies all Cache Clients of the change so they can update their internal routing tables and avoid sending requests to the dead machine.

### 9. Leader-Follower

