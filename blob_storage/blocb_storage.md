# System Design: Blob Storage
# 1. Problem Definition

Design a **distributed blob storage system** capable of storing:

* images
* videos
* backups
* logs
* ML datasets

### Target scale (example)

| Metric          | Value       |
| --------------- | ----------- |
| Objects stored  | 100 Billion |
| Avg object size | 10 MB       |
| Total storage   | 1 Exabyte   |
| Upload QPS      | 500K        |
| Read QPS        | 5M          |
| Availability    | 99.99%      |
| Durability      | 11 nines    |

Workload pattern:

```
Read-heavy system
90% reads
10% writes
```

---

# 2. Functional Requirements

1. Upload blob
2. Download blob
3. Delete blob
4. List blobs
5. Create/Delete containers (buckets)
6. Access control (private/public)
7. Object versioning
8. Multipart upload
9. Lifecycle management

---

# 3. Non-Functional Requirements

| Requirement     | Target                    |
| --------------- | ------------------------- |
| Durability      | 99.999999999%             |
| Availability    | 99.99%                    |
| Latency         | <100ms reads              |
| Throughput      | High streaming throughput |
| Scalability     | Exabyte scale             |
| Cost efficiency | Hot/cold tiers            |
| Security        | Encryption + IAM          |

---

# 4. Capacity Estimation

### Storage growth

Assume

```
100M uploads/day
avg size = 10MB
```

Daily storage:

```
100M × 10MB = 1PB/day
```

Replication factor = 3

```
3PB/day
```

Yearly:

```
≈ 1 Exabyte
```

---

### Bandwidth estimation

Uploads:

```
1PB/day ≈ 11.5 GB/sec
```

Downloads (10x read):

```
≈ 115 GB/sec
```

---

# 5. High Level Architecture

```
                Clients
                   |
                   v
              +---------+
              |  DNS    |
              +----+----+
                   |
                   v
              +---------+
              |   CDN   |
              +----+----+
                   |
                   v
           +---------------+
           | Load Balancer |
           +-------+-------+
                   |
        +----------+----------+
        |                     |
        v                     v
   +-----------+        +-----------+
   | API Nodes |        | API Nodes |
   +-----+-----+        +-----+-----+
         |                    |
         +---------+----------+
                   |
                   v
           +---------------+
           | Metadata Tier |
           +-------+-------+
                   |
                   v
           +---------------+
           | Chunk Manager |
           +-------+-------+
                   |
        +----------+----------+
        |                     |
        v                     v
   +-----------+        +-----------+
   | Storage   |        | Storage   |
   | Cluster A |        | Cluster B |
   +-----------+        +-----------+
```

---

# 6. Key Components

## 1. API Layer

Responsibilities:

* Authentication
* Authorization
* Request routing
* Multipart upload orchestration

Stateless → horizontally scalable.

---

## 2. Metadata Service

Stores:

```
account
bucket
object
chunk mapping
permissions
version
```

Example metadata:

```
blob_id: 897124
bucket: videos
size: 1.2GB

chunks:
  chunk1 -> node23
  chunk2 -> node77
  chunk3 -> node54
```

---

### Metadata DB choice

Must support:

* billions of objects
* low latency

Typical solutions:

```
DynamoDB
BigTable
Cassandra
Spanner
```

---

# 7. Storage Architecture

Blobs are split into **fixed-size chunks**.

Example:

```
Blob size: 1GB
Chunk size: 64MB

Chunks:
1GB / 64MB = 16 chunks
```

---

### Chunk Distribution

```
Chunk1 -> NodeA
Chunk2 -> NodeB
Chunk3 -> NodeC
Chunk4 -> NodeD
```

Replication factor:

```
3 replicas
```

```
Chunk1:
NodeA
NodeH
NodeZ
```

---

# 8. Metadata Sharding

Metadata is the biggest **scaling challenge**.

Shard by:

```
hash(account_id + bucket + blob_name)
```

```
Shard1 -> metadata servers
Shard2 -> metadata servers
Shard3 -> metadata servers
```

Benefits:

* even distribution
* fast lookup

---

# 9. Object Placement Strategy

Goal:

* avoid hotspot
* distribute load

Algorithm:

```
consistent hashing
```

Storage nodes arranged on a ring.

```
hash(blob_id) -> position on ring -> node
```

```
           [NodeA]
        /           \
   [NodeB]       [NodeC]
        \           /
          [NodeD]
```

---

# 10. Upload Flow

```
Client -> API server
```

1. Authenticate request
2. Generate blob ID
3. Split into chunks
4. Select storage nodes
5. Upload chunks in parallel
6. Replicate chunks
7. Save metadata
8. Return object URL

```
client
   |
   v
API
   |
   v
metadata service -> generate blob_id
   |
   v
chunk manager -> choose storage nodes
   |
   v
upload chunks
   |
   v
replicate chunks
```

---

# 11. Download Flow

```
Client
  |
  v
API Server
  |
  v
Metadata Service
  |
  v
Return chunk locations
  |
  v
Client downloads chunks directly
```

Parallel chunk download improves throughput.

---

# 12. Multipart Upload

For large objects (>5GB).

Steps:

```
1 initiate upload
2 upload chunks independently
3 commit upload
```

Benefits:

* retry individual parts
* parallel upload

---

# 13. Caching Strategy

### Layer 1 — CDN

Hot objects cached globally.

```
CloudFront / Akamai
```

---

### Layer 2 — Edge cache

Frequently accessed objects cached at edge nodes.

---

### Layer 3 — Metadata cache

Redis / Memcached.

---

# 14. Lifecycle Storage Tiers

Cost optimization.

```
Hot storage  -> frequently accessed
Cool storage -> occasionally accessed
Archive      -> rarely accessed
```

Example lifecycle:

```
Day 0-30   -> Hot
Day 30-90  -> Cool
Day 90+    -> Archive
```

---

# 15. Replication Strategy

### Intra-Region Replication

```
3 replicas across racks
```

```
Rack1 -> NodeA
Rack2 -> NodeB
Rack3 -> NodeC
```

---

### Cross-Region Replication

```
Region A -> primary
Region B -> async replica
Region C -> async replica
```

Provides disaster recovery.

---

# 16. Consistency Model

Modern object stores provide:

```
Strong consistency
```

Example:

```
PUT -> immediately visible
GET -> latest version
```

Implementation:

```
quorum replication
```

Write success when:

```
W >= majority
```

---

# 17. Failure Handling

### Node failure

```
heartbeat monitoring
```

Manager detects failure.

Re-replication triggered.

---

### Disk failure

Replace disk and rebuild replicas.

---

### Metadata server failure

Use:

```
leader election (Raft)
```

---

### Partial upload

Use:

```
multipart retry
```

---

# 18. Security

### Encryption at Rest

```
AES-256
```

### Encryption in Transit

```
TLS
```

### Access Control

IAM policies.

```
bucket policy
object ACL
signed URLs
```

---

# 19. Garbage Collection

Delete operation:

```
mark object as deleted
```

Actual deletion later.

```
background GC job
```

Benefits:

* fast delete response
* batch cleanup

---

# 20. Advanced Optimizations (FAANG Level)

### 1 Deduplication

Store identical chunks once.

Used in backup systems.

---

### 2 Erasure Coding

Instead of 3 replicas:

```
10 data + 4 parity blocks
```

Storage overhead:

```
1.4x instead of 3x
```

Used by large-scale storage.

---

### 3 Upload Acceleration

Use nearest edge node.

```
client -> edge -> storage region
```

---

### 4 Hot Object Detection

Track frequently accessed objects.

Move to SSD tier.

---

# 21. Real Production Systems

| Company   | Blob Storage |
| --------- | ------------ |
| Amazon    | S3           |
| Google    | GCS          |
| Microsoft | Azure Blob   |
| Facebook  | Haystack     |
| Dropbox   | Magic Pocket |

---

# 22. Key Discussion Points

Strong candidates should discuss:

1. Metadata scaling
2. Object chunking
3. Replication strategy
4. CDN integration
5. Consistency model
6. Failure recovery
7. Lifecycle storage tiers
8. Cost optimization
9. Erasure coding
10. Cross-region replication

---

