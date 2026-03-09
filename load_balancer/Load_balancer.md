# Load Balancer – System Design Notes

## 1. Purpose & Responsibilities

A **load balancer (LB)** is a system component responsible for distributing incoming traffic across multiple backend instances to achieve **availability, scalability, and reliability**.

### Core Responsibilities
- Traffic distribution
- Health checking (liveness / readiness)
- Failover and retry handling
- Connection management
- TLS termination (optional)

### Adjacent but Non-Core Capabilities
> These are often integrated but not intrinsic to load balancers:
- Service discovery (e.g., Consul, etcd, Kubernetes)
- Observability and predictive analysis
- Security enforcement beyond basic controls
- Reduction of human intervention (outcome, not a feature)

---

## 2. Load Balancer Classification

### 2.1 By Traffic Scope

#### Local / Regional Load Balancer
- Deployed within a datacenter or region
- Acts as a reverse proxy
- Optimizes local resource utilization
- Examples: NGINX, HAProxy, Envoy, Cloud ALB

#### Global Server Load Balancer (GSLB)
- Routes traffic across regions or datacenters
- Uses geo-location, latency, or health signals
- Improves global availability and latency

#### DNS-based Load Balancing
- Simplest form of load distribution
- Uses DNS responses (round-robin, geo-DNS)
- Limited by DNS TTL and client-side caching
- Slow failover

#### Anycast-based Load Balancing
- Same IP advertised from multiple geographic locations
- Network routing directs traffic to nearest healthy endpoint
- Extremely fast failover
- Common in CDNs, DNS resolvers, edge platforms

---

### 2.2 By OSI Layer

#### Layer 4 (Transport Layer)
- Operates on TCP/UDP
- High performance, low latency
- No payload inspection
- Limited routing intelligence

#### Layer 7 (Application Layer)
- Operates on HTTP / HTTPS / gRPC
- Routing based on headers, cookies, paths, methods
- Enables advanced capabilities (auth, rate limiting, WAF)
- Higher CPU and memory overhead

---

## 3. Load Balancing Algorithms

### Static Algorithms
- Round Robin
- Weighted Round Robin
- Random

### Dynamic Algorithms
- Least Connections
- Least Response Time
- EWMA (latency-based)
- Power of Two Choices (P2C)

### Hash-Based Algorithms
- IP Hash
- URL Hash
- Consistent Hashing (important for cache affinity)

---

## 4. State Management & Session Affinity

### Stateless Load Balancer
- No client-to-backend affinity
- Ideal for stateless services
- Simplifies horizontal scaling and failover

### Stateful Load Balancer
- Maintains session stickiness
- Uses cookies, IP hashing, or headers
- Required for legacy or session-heavy applications
- Reduces load distribution flexibility

---

## 5. Multi-Tier Load Balancing Architecture

### Typical Tiered Model

- **Tier 0 – Edge**
  - Anycast, CDN, DNS
- **Tier 1 – Global Routing**
  - GSLB, Geo-DNS
- **Tier 2 – Regional Entry**
  - Layer 4 Load Balancer
- **Tier 3 – Service Routing**
  - Layer 7 Load Balancer / API Gateway

This layered approach enables **progressive traffic refinement** and **failure isolation**.

---

## 6. Deployment Models

### Hardware-based Load Balancer
- ASIC-based
- Very high throughput
- Expensive and less flexible
- Vendor lock-in

### Software-based Load Balancer
- Runs on commodity hardware
- Cloud-native and elastic
- Easier automation and integration

### Modern Variants
- eBPF-based load balancing (kernel-level)
- Service mesh sidecars (Envoy-based)
- Managed cloud load balancers

---

## 7. Failure Handling & Resilience

### Common Failure Scenarios
- Load balancer as single point of failure
- Partial backend failures
- Network partitions

### Mitigation Strategies
- Active-active or active-passive LBs
- Health checks with fast eviction
- Circuit breakers and retries
- Graceful connection draining

---

## 8. Observability & Security

### Observability
- Metrics: RPS, latency, error rates
- Logs: access and error logs
- Tracing: request propagation across services

### Security
- TLS termination
- Rate limiting
- DDoS mitigation
- Web Application Firewall (WAF) integration

---

## 9. Trade-offs & Design Considerations

| Dimension | Layer 4 LB | Layer 7 LB |
|--------|----------|----------|
| Performance | High | Moderate |
| Routing Intelligence | Low | High |
| Cost | Lower | Higher |
| Flexibility | Limited | Extensive |

### Key Design Decisions
- Stateless vs stateful services
- L4 vs L7 placement
- Centralized vs distributed LBs
- Cost vs latency vs complexity trade-offs

---

## Summary

Load balancers are foundational components in distributed systems, directly influencing **availability, scalability, performance, and fault tolerance**. Correct classification, algorithm choice, and placement are critical to meeting system non-functional requirements.

