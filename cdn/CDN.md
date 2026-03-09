
## 1. Problem Statement & Motivation

A single data center serving global users faces three primary issues:

* **High Latency:** Long physical distances increase propagation and transmission delays.
* **Bandwidth Bottlenecks:** Origin servers must send redundant data for every request, straining network paths.
* **Resource Scarcity:** Computational and bandwidth limits at a single site create a single point of failure and limit scaling.

A **CDN** solves this by placing **edge servers** (proxy servers) at the network edge, closer to end-users, to reduce the distance data travels.

---

## 2. System Requirements

### Functional Requirements

* **Retrieve & Deliver:** Support **Push** (origin pushes to CDN) and **Pull** (CDN fetches on-demand) models.
* **Request & Search:** Respond to user queries and search within the CDN infrastructure (including peer-to-peer searching within a Point of Presence (PoP)).
* **Update & Delete:** Allow for content updates (often via serverless scripts) and entry eviction.

### Non-Functional Requirements

* **Low Latency:** Minimize user-perceived delay.
* **High Availability:** Resist DDoS attacks and ensure service continuity even if origin or specific edge servers fail.
* **Scalability:** Scale horizontally as user demand grows.
* **Reliability:** No single point of failure.

---

## 3. High-Level Architecture

The system is composed of several key building blocks:

* **Routing System:** Directs clients to the most suitable (usually nearest) proxy server.
* **Edge Proxy Servers:** The "workhorses" that serve content from RAM (hot data) or SSD/HDD (cold data).
* **Distribution System:** Moves content from origin servers to the edge proxy servers.
* **Scrubber Servers:** Filter malicious traffic (DDoS protection) before it reaches the edge.
* **Management System:** Tracks metrics like latency, load, and accounting for billing.

---

## 4. Deep Dive & Design Decisions

### Caching Strategies: Push vs. Pull

| Strategy | Mechanism | Best Use Case |
| --- | --- | --- |
| **Push** | Origin automatically sends data to proxy servers. | Static, popular content (logos, CSS). |
| **Pull** | Proxy fetches data from origin only when a user requests it. | Dynamic or frequently changing content. |

### Routing: Finding the Nearest Proxy

To minimize latency, the **Routing System** uses several techniques:

1. **DNS Redirection:** The DNS server returns an IP or URI based on the user's location and current server loads.
2. **Anycast:** Multiple edge servers share a single IP address; BGP routing sends the user to the "closest" node based on network topology.
3. **HTTP Redirection:** The origin server provides a 302 redirect URL pointing to the CDN (simple but adds a round trip).

### Multi-Tier Architecture

CDNs use a tree-like hierarchy of caches. If an edge server (Tier 1) lacks content, it asks a parent proxy (Tier 2) before going to the origin. This handles **long-tail content** (less popular items) efficiently and reduces origin load.

---

## 5. Pros and Cons of Design Decisions

### A. Caching Models

* **Push Model Pros:** High availability; content is already there when the user asks.
* **Push Model Cons:** Wasteful if content isn't popular; struggles with rapidly changing data (redundant pushes).
* **Pull Model Pros:** Low storage consumption; handles dynamic content efficiently.
* **Pull Model Cons:** First user to request an item experiences a "cache miss" latency spike.

### B. Deployment Strategy (ISP vs. IXP)

* **In-ISP (e.g., Akamai/Netflix):** * *Pro:* Content is often just one hop away, extremely low latency.
* *Con:* Massive overhead in managing hardware across thousands of small locations.


* **Near IXP (e.g., Google):** * *Pro:* Easier to manage larger clusters; utilizes high-bandwidth backbones.
* *Con:* Slightly higher latency than in-ISP, though mitigated by techniques like **Split TCP**.



### C. Specialized (Private) vs. Public CDN

* **Private CDN (e.g., Netflix Open Connect) Pros:** Total control over the stack (custom TCP algorithms), lower long-term costs for massive scale, and better data protection.
* **Private CDN Cons:** Very high initial setup cost and operational complexity.

---

## 6. API
### Proxy -> Origin
GET /retrieveContent
- Parameters
	- proxy_serverid
	- content_type
	- content_version
	- description

### Deliver Content (Push Model: Origin -> Proxy)
POST /deliverContent
- Parameters
	- origin_id
	- server_list
	- content_version
	- content_type
	- description
### Request Content (Client -> Proxy)
GET /requestContent
- Parameters
	- user_id
	- content_id

## 7. Use Cases

### Hybrid CDN Sequence Diagram

The following sequence describes the interactions based on the provided architectural requirements:

#### 1. Static Content (Push Model)

* **Update Trigger**: The **Origin Server** identifies a static asset update.
* **Delegation**: The **Origin Server** provides the URI namespace delegation to the **Request Routing System**.
* **Push Delivery**: The **Origin Server** uses the `/deliverContent` API to send the asset to the **Distribution System**.
* **Global Distribution**: The **Distribution System** broadcasts the content to the specified `server_list` (Edge Proxy Servers).
* **Feedback Loop**: The **Distribution System** provides feedback to the **Request Routing System** so it knows which proxies have the content.

#### 2. Non-Frequent Content (Pull Model)

* **Cache Miss**: A user requests content that is not present on the **Edge Proxy Server**.
* **Peer Search**: The **Edge Proxy** may use `/searchContent` to check peer servers in the same Point of Presence (PoP).
* **Origin Retrieve**: If not found in the PoP, the **Edge Proxy** uses `/retrieveContent` (with `content_version = NULL`) to fetch the data from the **Origin Server**.
* **Storage**: The **Edge Proxy** stores the content in SSD/HDD and serves it to the user.

#### 3. End-User Request Flow

* **Resolution**: The **Client** requests a suitable proxy from the **Request Routing System** (via DNS or Anycast).
* **IP Return**: The **Routing System** returns the IP address of the nearest/least-loaded **Edge Proxy**.
* **Security Scrutiny**: The **Client** request passes through **Scrubber Servers** to filter malicious traffic.
* **Fulfillment**: The **Edge Proxy** serves the content from its cache (RAM for static, Disk for dynamic).
* **Accounting**: The **Edge Proxy** periodically sends usage statistics to the **Management System**.

---

### Summary of Component Knowledge

| Knowledge Point | Mechanism | Source |
| --- | --- | --- |
| **Content ID** | URI namespace delegation & manifest files. | **Origin  Routing System** |
| **Server List** | Maintained by the Control Core and Management System. | **Management System  Origin** |
| **Version Info** | Stored in local proxy metadata; validated via TTL/Leases. | **Proxy  Origin** |

## 8. Architecture 

The architecture of a hybrid CDN is designed to optimize both static and dynamic data flows by leveraging a multi-tier server hierarchy and intelligent routing.

### Hybrid CDN Architectural Components

The following components interact to manage the different data types:

* **Origin Servers**: Act as the source of truth, pushing static content and responding to pull requests for dynamic data.
* **Distribution System**: Manages the "Push" model by broadcasting updated static content from the origin to a specific `server_list` of edge proxies.
* **Request Routing System**: Directs clients to the nearest or least-loaded proxy server using URI namespace delegation provided by the origin.
* **Edge Proxy Servers (Tier 1)**: Located in Points of Presence (PoPs), these servers store "hot" data in RAM and "cold" or non-frequent data in SSD/HDD.
* **Parent Proxy Servers (Tier 2)**: Sit between the edge and the origin to handle cache misses, reducing the direct load on the origin server.
* **Scrubber Servers**: Positioned between the client and the proxy to filter out malicious traffic like DDoS attacks.

---

### Data Flow for Push and Pull Models

The hybrid nature is defined by how these components handle different types of content:

| Feature | Static Content (Push) | Non-Frequent Content (Pull) |
| --- | --- | --- |
| **Trigger** | Origin proactively pushes updates. | Client request triggers a fetch. |
| **API Used** | `/deliverContent`. | `/retrieveContent`. |
| **Storage** | Highly replicated across many PoPs. | Cached based on local popularity. |
| **Consistency** | Immediate update via origin push. | Managed via TTL, TTR, or Leases. |

### Internal Workflow

1. **Static Update**: The origin pushes static assets to the distribution system, which pre-warms the edge proxies.
2. **Client Request**: The client asks the routing system for a proxy and is directed to an edge server.
3. **Security Filtering**: Traffic is cleaned by scrubber servers before reaching the edge proxy.
4. **Content Delivery**: The edge proxy serves the request. If the dynamic data is missing, it pulls it from a parent proxy or the origin.
5. **Analytics**: The proxy sends accounting and performance data to the **Management System** for monitoring and billing.








