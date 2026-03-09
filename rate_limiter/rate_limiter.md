# System Design: The Rate Limiter
# 1. Requirements

## Functional Requirements

1. Limit number of requests per client in a time window.

   * Example: `100 requests / minute / API key`.

2. Configurable rate limits.

   * Per API
   * Per user
   * Per IP
   * Per service tier (free vs paid).

3. Return appropriate response when limit exceeded.

```
HTTP 429 Too Many Requests
```

4. Support different throttling strategies

* Hard throttling
* Soft throttling
* Elastic throttling

5. Support multiple limit types

```
user limit
IP limit
global limit
endpoint limit
```

6. Ability to update rules dynamically.

---

## Non-Functional Requirements

### 1. High Availability

Rate limiter should **not become single point of failure**.

### 2. Low Latency

Decision time should be extremely fast.

Typical target:

```
< 1 ms
```

### 3. Scalability

Must handle:

```
millions of requests/sec
```

### 4. Consistency

Avoid bypassing rate limit due to race conditions.

### 5. Fault tolerance

If rate limiter fails → system should still serve requests.

---

# 2. High Level Design

Rate limiter usually sits **before application servers**. The rate limiter can be deployed as **Middleware** between the client and the API servers.

```
                +-------------+
Client -------->| LoadBalancer|
                +-------------+
                       |
                       v
              +------------------+
              | API Gateway      |
              | / Rate Limiter   |
              +------------------+
                       |
                       v
               +---------------+
               | Rate Limiter  |
               | Service       |
               +---------------+
                 |           |
                 |           |
                 v           v
           +---------+   +---------+
           | Redis   |   | Rule DB |
           | Counters|   | Limits  |
           +---------+   +---------+
                 |
                 v
            +----------+
            | API      |
            | Servers  |
            +----------+
```

### Flow

```
Client request
     ↓
API Gateway
     ↓
Rate Limiter
     ↓
Check limit
     ↓
Allow → Forward to API server
Reject → HTTP 429
```

---

# 3. Low Level Design

Now we break the rate limiter into internal components.


```text
       +---------------------------------------+
       |           Rate Limiter                |
       |  +---------------------------------+  |      +------------------+
Request|  |     Client Identifier Builder   |  |----->|   Rule Database  |
------>|  +---------------------------------+  |      | (Persistent)     |
       |                 |                     |      +---------+--------+
       |  +---------------------------------+  |                |
       |  |         Decision Maker          |  |<-------+-------+
       |  | (Runs Algorithm: Token Bucket)  |  |        |
       |  +---------------------------------+  |  +-----v--------------+
       |                 |                     |  |   Rule Retriever   |
       |  +---------------------------------+  |  +---------+----------+
       |  |      Throttle Rules Cache       |  |            |
       |  |     (User/Service Counters)     |  |<-----------+
       |  +---------------------------------+  |
       +---------------------------------------+
                 |                     |
          [Allow Request]       [Reject Request]
                 |                     |
          (To API Server)        (HTTP 429 Error)

```

---

## Component Explanation

### 1. Client Identifier Builder

Generates key for rate limiting.

Example:

```
userID
IP
API_KEY
userID + endpoint
```

Example key:

```
rate:user123:/payment
```

---

### 2. Throttle Rule Cache

Stores rate limit rules.

Example:

```
user123 → 100 req/min
premium_user → 1000 req/min
```

Cache layer:

```
Redis
Memcached
```

Rules stored in DB and cached for performance. 

---

### 3. Decision Maker/Engine

Core logic.

Steps:

```
1 Get rule
2 Get current counter
3 Apply algorithm
4 Decide allow or reject
```

---

### 4. Rule Retriever/Counter Store

Usually stored in:

```
Redis
```

because it supports:

```
INCR
EXPIRE
Atomic operations
```

---

# 4. Algorithm

You can choose an algorithm based on your specific traffic patterns:

| Algorithm | Space Efficient | Allows Bursts? | Logic Summary |
| --- | --- | --- | --- |
| **Token Bucket** | Yes | Yes | Tokens added at rate $R$; request needs 1 token. |
| **Leaking Bucket** | Yes | No | Requests processed at constant outflow rate. |
| **Fixed Window** | Yes | Yes (at edges) | Counter per fixed time slice (e.g., 1 min). |
| **Sliding Log** | No | Yes | Tracks timestamp of every single request. |
| **Sliding Counter** | Yes | Smooths bursts | Weighted average of current and previous window. |

> **Sliding Window Counter Formula:** > 
> $$Rate = R_c + (R_p \times \frac{\text{time frame} - \text{overlap time}}{\text{time frame}})$$
> 
> 



---

## Token Bucket Algorithm

Each client has a bucket.

Parameters:

```
capacity = max tokens
refill_rate = tokens per second
tokens = current tokens
```

---

### Flow

```
1 request arrives
2 refill tokens based on elapsed time
3 if tokens > 0
      allow
      tokens--
  else
      reject
```

---

### Example

Limit:

```
100 requests/min
```

Bucket:

```
capacity = 100
refill_rate = 100/min
```

Timeline

```
t0 tokens=100

User sends 80 requests
tokens=20

1 minute later
tokens refilled → 100
```

---

### Pseudocode

```
now = current_time

tokens += (now-last_refill) * refill_rate
tokens = min(tokens, capacity)

if tokens >= 1:
    tokens -= 1
    allow
else:
    reject
```

---

# 5. API Design

Typical APIs exposed by rate limiter service.

---

## Check Rate Limit

```
POST /rate-limit/check
```

Request

```
{
  "client_id": "user123",
  "api": "/payment"
}
```

Response

```
{
  "allowed": true,
  "remaining": 54,
  "reset_time": 1712345678
}
```

---

## Update Rule

```
POST /rate-limit/rules
```

Request

```
{
  "client_id": "user123",
  "limit": 100,
  "window": "1m"
}
```

---

## Get Rule

```
GET /rate-limit/rules/{client}
```

---

# 6. Storage

Two storage types are needed.

---

# 1 Rules Database

Stores rate limit configuration.

Example schema

```
RateLimitRules

id
client_id
api
limit
window
updated_at
```

Database choices:

```
PostgreSQL
MySQL
DynamoDB
```

---

# 2 Counter Store

Stores request counters.

Use:

```
Redis
```

Example keys:

```
rate:user123:timestamp
rate:ip_10.1.1.1
```

Example value

```
count = 45
```

Redis commands:

```
INCR
EXPIRE
```

---

# 7. Failure Handling

Rate limiter failures must **not break system availability**.

---

## 1 Redis Failure

Solution

```
fallback to local memory limiter
```

or

```
allow request
```

Fail-open strategy.

---

## 2 Rate Limiter Instance Crash

Use multiple instances.

```
          +--------------+
          | LoadBalancer |
          +--------------+
            |    |    |
            v    v    v
         RL1   RL2   RL3
```

---

## 3 Cache Failure

Fallback to DB.

---

## 4 Race Conditions

Problem:

```
GET counter
increment
SET counter
```

Two requests may bypass limit.

Solution:

Use atomic operations.

Example Redis:

```
INCR
```

---

## 5 Thundering Herd

When many requests check counter simultaneously.

Solutions:

```
sharded counters
local caching
```

---

# 8. Scaling the System

For millions of users.

```
                +-------------+
                | LoadBalancer|
                +-------------+
                 /     |     \
                v      v      v
            +------+ +------+ +------+
            | RL1  | | RL2  | | RL3  |
            +------+ +------+ +------+
                 \      |      /
                  v     v     v
                    +---------+
                    | Redis   |
                    | Cluster |
                    +---------+
```

Sharding strategy:

```
hash(user_id) % N
```

---

# 9. Optimizations

### 1 Sharded counters

Reduce write contention.

### 2 Local cache

Avoid Redis hit for every request.

### 3 Async counter updates

Move some updates to background processing.

### 4 Edge rate limiting

Use CDN / API gateway.

Example:

```
Cloudflare
NGINX
Envoy
```

---

# Final Architecture Summary

```
Client
  |
  v
Load Balancer
  |
  v
API Gateway
  |
  v
Rate Limiter Service
  |
  +---- Rule Cache
  |
  +---- Decision Engine
  |
  +---- Redis Counter Store
  |
  v
API Servers
```

---

# Key Points

Mention these explicitly:

* Redis atomic operations
* Token bucket algorithm
* Sharded counters
* Fail-open strategy
* Distributed rate limiter
* Edge throttling

---
