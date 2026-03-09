# Scenario 1: Cloud-based file store#

1. Scalability
#### Why it is critical?
- The platform must handle varying and increasing request volumes depending on time (peak hours) and geography (multiple regions).
- User load will be elastic—regional spikes, seasonal surges, and enterprise-wide events.

#### Design implications

Horizontal scaling of stateless services
Auto-scaling based on demand (CPU, RPS, queue depth)
Geo-distributed deployments to reduce latency
CDN-backed file delivery for global access

#### Outcome
Scalability ensures the system can grow and shrink dynamically without service degradation.

2. Availability

#### Why it is critical
The requirement explicitly states worldwide 24/7 access to files.
Even short downtime directly violates business expectations for a multinational enterprise.

#### Design implications
Multi-region active-active or active-passive setup
Redundant services and data replicas
Automated failover and health checks
Strong SLAs (e.g., 99.9%+ uptime)

#### Outcome
Availability ensures the system is accessible at all times, regardless of failures or regional outages.

3. Fault Tolerance
Supports availability but is more of a means than a primary business requirement
Implemented via redundancy, retries, and graceful degradation

4. Reliability
Focuses on correctness and consistency over time
Important, but secondary to availability in this scenario (a temporarily stale file is often better than no access)

5. Maintainability
Important for long-term evolution
Does not directly enable global scale or 24/7 access in the short term

# Scenario 2: Financial trading platform

Imagine a banking application for financial transactions and buying online products. This platform allows users to obtain their account status, transfer money, pay utility bills, and generate bank statements.

List the following non-functional requirements in the correct order, starting from the most important non-functional requirement to the least important non-functional requirement:

1. Security
#### Why it comes first
The platform handles financial assets and sensitive personal data.
Any compromise (data breach, fraud, account takeover) results in irreversible financial loss, regulatory violations, and reputational damage.
A secure but slow transaction is acceptable; a fast but insecure transaction is catastrophic

### Security design considerations
- End-to-end encryption (TLS for data in transit, AES-256 at rest)
- Strong authentication & authorization
MFA (OTP, biometrics)
Role-based access control (RBAC)
- Fraud detection
Behavioral analytics
Anomaly detection on transactions
- Secure APIs
OAuth 2.0 / OpenID Connect
Rate limiting & request signing
- Regulatory compliance
PCI-DSS, GDPR, local banking regulations
- Auditability
Immutable logs for every transaction

2. Consistency
#### Why it comes next

- Banking systems cannot tolerate incorrect balances.
- Transactions must follow ACID properties, especially:
Atomicity – all-or-nothing transfers
Consistency – system moves from one valid state to another
Isolation – concurrent transfers don’t corrupt balances
Durability – committed transactions survive crashes

#### Consistency design considerations

- Strongly consistent databases
Relational DBs (PostgreSQL, Oracle)
Distributed systems with consensus (Raft / Paxos)
- Transaction management
Two-phase commit (2PC) where unavoidable
Saga pattern with compensations (for cross-service workflows)
- Idempotent APIs
Prevent double charges on retries
- Ledger-based architecture
Append-only transaction logs
Immutable financial records

3. Low latency
#### Why it is last
- Users expect responsiveness, but milliseconds are less important than correctness and safety.
- A transaction that takes 1–2 seconds but is correct and secure is acceptable.
- Latency optimization must never weaken security or consistency.

#### Latency optimization (within constraints)
- Read replicas for non-transactional queries
Account balance view
Statement generation
- Caching
Cached account summaries (read-only)
- Async processing
Notifications, emails, receipts
- Geographic routing
Route users to nearest region without breaking consistency
- Optimistic UI
UI feedback while backend transaction finalizes

# Scenario 3: Spacecraft operations#
A space agency relies on critical systems to operate spacecraft, conduct space missions, and gather valuable data for scientific research. Imagine a scenario where a spacecraft is on a mission to explore a distant planet, and it encounters a hardware malfunction or a communication disruption with the control center on Earth.

State one of the most important and relevant non-functional requirements from the list provided below, such that its inclusion in the design would enable us to recover from the scenario mentioned above. Please also provide proper reasoning behind your decision:

Reliability
Maintainability
Consistency
Fault tolerance
Availability
Scalability


### ✅ Most Important Non-Functional Requirement: **Fault Tolerance**

---

## Why **Fault Tolerance** is the correct choice

In the given scenario, the spacecraft is **already in operation** and encounters:

* a **hardware malfunction**, or
* a **communication disruption** with Earth.

The key requirement is not merely to stay “up” or “correct,” but to **continue operating safely and recover autonomously despite failures**.

> **Fault tolerance is the system’s ability to continue functioning correctly even when one or more components fail.**

This makes it the **most relevant NFR** for recovery in deep-space missions.

---

## Reasoning (Why others are secondary)

| NFR                 | Why it’s not primary in this scenario                                                |
| ------------------- | ------------------------------------------------------------------------------------ |
| **Reliability**     | Measures failure *rate* over time, but does not guarantee survival *after* a failure |
| **Availability**    | Depends on continuous connectivity—often impossible in deep space                    |
| **Maintainability** | Repairs or updates are slow, delayed, or impossible mid-mission                      |
| **Consistency**     | Data correctness matters, but cannot prevent mission failure                         |
| **Scalability**     | Irrelevant for a single spacecraft in mission mode                                   |

> Space missions assume failures **will happen**; success depends on surviving them.

---

## Core Design Aspects Enabled by Fault Tolerance

### 1️⃣ Hardware Redundancy

* Duplicate critical subsystems:

  * Power units
  * Navigation sensors
  * Communication modules
* Use **hot / warm / cold spares**
* Automatic switchover on failure

**Design principle:** *No single point of failure*

---

### 2️⃣ Autonomous Failure Detection & Recovery

* Onboard health monitoring
* Watchdog timers
* Fault isolation logic

Example:

* If primary antenna fails → switch to backup
* If sensor data becomes noisy → fallback to alternative sensor fusion

**Design principle:** *Self-healing without Earth intervention*

---

### 3️⃣ Graceful Degradation

* Mission continues in **safe or reduced mode**
* Non-essential systems shut down to preserve power
* Core mission objectives prioritized

Example:

* Suspend high-resolution imaging
* Preserve navigation, power, and thermal control

**Design principle:** *Reduced capability is better than total failure*

---

### 4️⃣ Store-and-Forward Communication

* Buffer telemetry and scientific data locally
* Retry transmission when communication resumes
* Handle long signal delays and blackouts

**Design principle:** *Operate independently of continuous connectivity*

---

### 5️⃣ Fail-Safe Defaults

* Predefined safe states:

  * Stabilize orientation
  * Point solar panels to sun
  * Enter low-power mode
* Prevent cascading failures

**Design principle:** *When uncertain, move to safety*

---

## Mission-Critical Architecture View

```
[ Sensor Failure ]
        ↓
[ Fault Detection ]
        ↓
[ Isolation Logic ]
        ↓
[ Redundant Component Activation ]
        ↓
[ Safe / Degraded Mode ]
        ↓
[ Mission Continues ]
```

---

## Final Answer (Exam / Interview Ready)

> **Fault Tolerance** is the most important non-functional requirement for this space mission scenario.
>
> Spacecraft operate in hostile and remote environments where hardware failures and communication disruptions are expected. Fault tolerance enables the system to detect failures, isolate faulty components, switch to redundant subsystems, and continue operating autonomously without immediate ground control support. This capability is essential for mission survival and recovery in deep-space operations.

---

## One-Line Insight (Systems Thinking)

> **In space systems, failure is inevitable; survival depends on fault tolerance.**


# Scenario 4: Twitter

Consider a scenario for Twitter’s recently introduced feature allowing users to stream live video directly within their tweets. As a result, the number of users streaming live video has surged exponentially. Users are consequently experiencing delays, and the system struggles to handle the increased load.

State one of the most important and relevant non-functional requirements from the list provided below, such that its inclusion in the design would enable us to handle the scenario mentioned above.

Reliability
Maintainability
Consistency
Fault tolerance
Availability
Scalability

Good thoughts! Reliability is indeed important, but in this scenario, the key issue is the system’s ability to handle the rapid increase in users streaming videos. That points more towards scalability, specifically horizontal scalability, to add more servers as needed. Keep exploring these concepts—you’re on the right track!




