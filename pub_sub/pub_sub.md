# System Design: Pub/Sub
# 1. Requirements
## Functional Requirements
1. **Create Topic**
   * Producers create topics where messages will be published.
2. **Publish Messages**
   * Producers write messages to topics.
3. **Subscribe to Topic**
   * Consumers subscribe to topics to receive messages.
4. **Consume Messages**
   * Consumers read messages from subscribed topics.
5. **Message Retention**
   * Messages should be retained for configurable time (default e.g. 7 days).
6. **Offset Tracking**
   * Each consumer should read messages independently using offsets.
7. **Message Ordering**
   * Order must be preserved **within a partition**.
8. **Acknowledgement**
   * Consumer confirms message consumption.
9. **Delete Topic**
   * Topics can be deleted.
   
These operations match the pub-sub abstraction described in the source material. 

---

## Non-Functional Requirements

### Scalability

* Millions of messages/sec
* Millions of consumers
* Thousands of topics

### Availability

* Producers and consumers should operate even during node failures.

### Durability

* Once a message is accepted, it must not be lost.

### Fault Tolerance

* Broker failure should not affect message availability.

### Low Latency

* Message delivery should be near real-time (milliseconds).

### High Throughput

* Optimized for sequential disk writes.

### Concurrency

* Many producers and consumers reading/writing simultaneously.

---

# 2. API Design

Typical APIs exposed by a Pub/Sub service.

## Create Topic

```http
POST /topics
```

Request

```
{
  topic_id: "topic123",
  topic_name: "order-events",
  partitions: 8,
  retention_hours: 168
}
```

Response

```
{
  status: "created"
}
```

---

## Publish Message

```http
POST /topics/{topic_id}/publish
```

Request

```
{
  partition_id: optional,
  message: "order created",
  key: "user123"
}
```

If partition_id not specified → system assigns using **hash/round robin**.

Response

```
{
  offset: 928374
}
```

---

## Subscribe

```http
POST /topics/{topic_id}/subscribe
```

Request

```
{
  consumer_id: "service-A",
  delivery_mode: "push | pull"
}
```

Response

```
{
  subscription_id: "sub_345"
}
```

---

## Read Message (Pull)

```http
GET /topics/{topic_id}/consume
```

Request

```
{
  consumer_id: "service-A",
  offset: 1234
}
```

Response

```
{
  messages: [...]
}
```

---

## Acknowledge

```http
POST /ack
```

Request

```
{
  consumer_id: "service-A",
  topic_id: "order-events",
  partition: 2,
  offset: 1234
}
```

---

## Unsubscribe

```
DELETE /topics/{topic_id}/subscribe/{consumer_id}
```

---

# 3. Storage Design

A scalable Pub/Sub system uses **Broker + Partition + Segment architecture**.

## Core Components

### 1. Broker

Responsible for:

* receiving messages
* storing messages
* serving consumers

Multiple brokers form a **cluster**.

---

### 2. Topic

Logical stream of messages.

Example:

```
Topic: order-events
```

---

### 3. Partitions

Each topic is divided into partitions.

Example

```
order-events
 ├── partition 0
 ├── partition 1
 ├── partition 2
 └── partition 3
```

Why partitions?

* horizontal scalability
* parallel consumers
* higher throughput

Messages inside partition are **strictly ordered**. 

---

### 4. Segments

Each partition is stored as **log segments**.

```
partition-1
  ├── segment-0.log
  ├── segment-1.log
  └── segment-2.log
```

Messages are **append-only**.

Advantages:

* sequential disk writes
* fast reads
* easy retention deletion

---

### 5. Offset

Every message has an **offset**.

Example:

```
offset: 0
offset: 1
offset: 2
offset: 3
```

Consumers track their own offsets.

---

## Metadata Storage

We store metadata separately.

### Relational DB

Stores:

```
topics
subscriptions
consumer metadata
retention policies
```

Example schema

```
topics
---------
topic_id
partitions
retention_time
created_at

subscriptions
-------------
consumer_id
topic_id
delivery_mode
```

---

## Offset Store

Use **Key-Value store**.

Example:

```
Key:
consumerID_topic_partition

Value:
offset
```

Example

```
serviceA_topic1_p2 → 18293
```

Why KV store?

* fast reads
* high availability

---

# 4. Failure Handling

A robust pub/sub system must handle failures at multiple levels.

---

# Broker Failure

Solution:

Replication.

Each partition has **leader + followers**.

Example

```
Partition 1

Leader   → Broker 1
Follower → Broker 3
Follower → Broker 5
```

If leader fails:

1. Cluster manager detects failure
2. One follower becomes leader
3. Metadata updated

Replication factor usually = **3**. 

---

# Producer Failure

Possible issues:

* message lost before write
* duplicate writes

Solutions:

* producer retries
* idempotent writes
* message IDs

---

# Consumer Failure

If consumer crashes:

Offset remains stored.

When consumer restarts:

```
resume from stored offset
```

No message loss.

---

# Network Failure

Use:

* retry policies
* exponential backoff
* durable logs

---

# Data Loss Prevention

Techniques:

1. **Replication**
2. **Write-ahead log**
3. **ACK only after replication**

Flow

```
Producer
   ↓
Leader broker
   ↓
Replicate to followers
   ↓
ACK to producer
```

---

# 5. Back-of-the-Envelope Estimation

Example scale assumption.

## Assumptions

```
Topics = 100K
Consumers = 10M
Producers = 1M

Messages/sec = 10M
Avg message size = 1KB
Retention = 7 days
```

---

## Write Throughput

```
10M messages/sec
× 1 KB

= 10 GB/sec ingest
```

---

## Storage Per Day

```
10 GB/sec
× 86400 sec

≈ 864 TB / day
```

---

## Storage for 7 days

```
864 TB × 7

≈ 6 PB
```

With replication factor 3:

```
18 PB
```

---

## Broker Capacity

Assume one broker handles

```
1 GB/sec
```

Required brokers:

```
10 GB/sec ÷ 1 GB/sec
= 10 brokers
```

With replication:

```
≈ 30 brokers
```

---

# Final Architecture

High-level flow

```
Producer
   │
   ▼
Load Balancer
   │
   ▼
Broker Cluster
   │
   ├── Topic
   │      ├── Partition
   │      │      ├── Segment
   │      │      └── Segment
   │
   ▼
Consumers
```

Supporting systems

```
Cluster Manager
Metadata DB
Offset KV Store
Replication Service
Consumer Manager
```

---

# Tip (Very Important)

A strong answer usually ends with **real systems comparison**:

| System         | Technology           |
| -------------- | -------------------- |
| Apache Kafka   | Partition log broker |
| Google Pub/Sub | Managed pub/sub      |
| AWS SNS + SQS  | Fanout messaging     |
| RabbitMQ       | Broker queue         |

This design above is **very close to Kafka architecture**.

1. High-Level Pub/Sub Architecture
                    +-------------------+
                    |   Cluster Manager |
                    +---------+---------+
                              |
                              v
+-----------+      +------------------+       +----------------+
| Producers | ---> |   Load Balancer  | --->  | Broker Cluster |
+-----------+      +------------------+       +--------+-------+
                                                     |
                                                     v
                                            +----------------+
                                            |     Topics     |
                                            +--------+-------+
                                                     |
                                                     v
                                              +-------------+
                                              | Partitions  |
                                              +------+------+
                                                     |
                                                     v
                                                +---------+
                                                | Segments|
                                                +----+----+
                                                     |
                                                     v
                                              +--------------+
                                              |   Consumers  |
                                              +--------------+
                                              
2. Broker Cluster Detail
                   +----------------------+
                   |     Broker Cluster   |
                   +----------+-----------+
                              |
        -------------------------------------------------
        |                     |                        |
        v                     v                        v
  +-----------+        +-----------+           +-----------+
  | Broker 1  |        | Broker 2  |           | Broker 3  |
  +-----------+        +-----------+           +-----------+
        |                     |                        |
        |                     |                        |
   +---------+           +---------+              +---------+
   |Topic A  |           |Topic A  |              |Topic A  |
   +----+----+           +----+----+              +----+----+
        |                     |                        |
   +---------+           +---------+              +---------+
   |Partition|           |Partition|              |Partition|
   |   P1    |           |   P2    |              |   P3    |
   +---------+           +---------+              +---------+
   
3. Message Flow
Producer
   |
   | publish(message)
   v
+----------------+
| Load Balancer  |
+--------+-------+
         |
         v
+----------------+
| Broker Leader  |
+--------+-------+
         |
         | replicate
         v
+----------------+     +----------------+
| Broker Replica |     | Broker Replica |
+--------+-------+     +--------+-------+
         |
         v
+----------------+
| Topic Partition|
+--------+-------+
         |
         v
+----------------+
|   Consumers    |
+----------------+

4. Topic → Partition → Segment Layout

Topic: OrderEvents
|
+-- Partition 0
|      |
|      +-- Segment 0 (offset 0 - 1000)
|      +-- Segment 1 (offset 1001 - 2000)
|
+-- Partition 1
|      |
|      +-- Segment 0 (offset 0 - 900)
|      +-- Segment 1 (offset 901 - 1800)
|
+-- Partition 2
       |
       +-- Segment 0
       +-- Segment 1
       
5. Consumer Offset Tracking
Consumer A
   |
   v
+------------------+
| Offset Store (KV)|
+------------------+

Key: consumerA_topic1_partition0
Value: offset=1452

6. System Design
                 +------------------+
                 |  Metadata Store  |
                 +---------+--------+
                           |
                           v
+-----------+     +---------------+     +--------------+
| Producers | --> |  Broker Node  | --> |   Consumers  |
+-----------+     +-------+-------+     +--------------+
                          |
                          v
                    +-----------+
                    |   Topic   |
                    +-----+-----+
                          |
                -----------------------
                |         |           |
                v         v           v
           +--------+ +--------+ +--------+
           | Part 0 | | Part 1 | | Part 2 |
           +--------+ +--------+ +--------+
                                                                   


