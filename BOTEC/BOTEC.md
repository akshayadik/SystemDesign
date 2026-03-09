# Back-of-the-Envelope Numbers in Perspective

Suppose a service transitions from being mostly CPU-bound to mostly IO-bound. Using the latency table as a guide, how would this shift influence your high-level resource planning when applying BOTECs?


### Important Rates

| QPS handled by MySQL 			| 1000 			|
| QPS handled by key-value store 	| 10,000 		|
| QPS handled by cache server		| 100,000–1 M		|

### Important Latencies

| Component                                              | Time (nanoseconds)            |
|--------------------------------------------------------|------------------------------|
| L1 cache reference                                     | 0.9                          |
| L2 cache reference                                     | 2.8                          |
| L3 cache reference                                     | 12.9                         |
| Main memory reference                                  | 100                          |
| Compress 1 KB with Snzip                               | 3,000 (3 µs)                 |
| Read 1 MB sequentially from memory                     | 9,000 (9 µs)                 |
| Read 1 MB sequentially from SSD                        | 200,000 (200 µs)             |
| Round trip within same datacenter                      | 500,000 (500 µs)             |
| Read 1 MB from SSD (~1 GB/sec throughput)              | 1,000,000 (1 ms)             |
| Read 1 MB sequentially from disk                       | 2,000,000 (2 ms)             |
| Disk seek                                              | 4,000,000 (4 ms)             |
| Send packet SF ⇄ NYC (round trip)                      | 71,000,000 (71 ms)           |

