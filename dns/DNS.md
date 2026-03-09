# Domain name system (DNS)

* *Name servers*: Server which respond to users queries are called name server. It is not single server but cluster of servers.
* *Resource records*: DNS database stores the domain name and iP address information in database called Resource Records. Important information is type, name and value.
* *Caching:* Used caching at different level to reduce the latency.
* *Hierarchical*: DNS is structured in hierarchycal form.

### DNS Hierarchy

* *DNS Resolver:* It receives the request and froward it to root name server.
* *Root-level name server:* Root name server contains list of IP address of TDL listing. Based on these IPs request is routed to TDL server.
* *Top-level domain Name Server (TLD):* This server has list of authoative server which contains exact IP address of the domain. THis server forward request to authorative server.
* *Authoritative Name Server:* Authoritative server will return the IP address for domain.

### Important points to note
- Time-to-live (TTL): This ensure that how long the domain cache entry to be invalidated. Low value of TTL helps in rolling out changes quickly. Standard value is 120 seconds since this helps in increasing the availability of system.
- Caching helps to quickly respond the data and reduce latencies by quickly returning Resource Record.
- Mostly UDP protocol is used. 
- Structure is hierarchical. 
- *Anycast:*
    - Same IP address advertised from multiple geographic locations
    - Requests routed to nearest available DNS server
    - Improves latency and resilience

### Query type
- Query resolution can happen in iterative or recursive way.
- Iterative: Local server request for root, TLD, and authoritative servers for IP address.
- Recursive: Client ask resolver to resolve fully. End user send request to local server, local server further send the request to root DNS, root DNS forward the request to other name servers.
- Root and TLD servers never do recursion.


### Drawback
Drawbacks:
- Provides eventual consistency, not strong consistency
- Update propagation depends on TTL and resolver behavior
- Lookup latency is variable and outside application control
- Failure modes are opaque to applications
- Client and ISP resolvers may ignore TTL values
- Not suitable for fast failover or real-time routing


### Common DNS Record Types:
- A     → Domain → IPv4 address
- AAAA  → Domain → IPv6 address
- CNAME → Alias to another domain
- MX    → Mail server routing
- NS    → Authoritative name servers
- TXT   → Metadata (SPF, DKIM, verification)
- SRV   → Service discovery (host + port)

