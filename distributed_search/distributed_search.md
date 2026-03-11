# System Design: The Distributed Search

# Distributed Text Search System Design

Goal:
Design a **scalable distributed system** that accepts a **text query** and returns the **most relevant documents in milliseconds**, even when indexing **billions of documents**.

Examples:

* Google search
* Amazon product search
* YouTube search
* Knowledge base search
* Log search

---

# 1. Requirements

## Functional Requirements

1. **Search(query)**
   Return relevant documents for a text query.

2. **Index documents**
   Convert documents into searchable structures.

3. **Add / Update documents**

4. **Delete documents**

5. **Ranking**
   Return most relevant results first.

6. **Multi-word queries**

Example

```
distributed search system
```

Optional advanced features:

* Phrase search
* Fuzzy search
* Autocomplete
* Spell correction
* Synonym search
* Personalized results

---

## Non-Functional Requirements

| Requirement       | Description              |
| ----------------- | ------------------------ |
| Low latency       | <200 ms response         |
| High throughput   | millions of queries/sec  |
| High availability | system always searchable |
| Scalability       | billions of documents    |
| Fault tolerance   | node failures tolerated  |
| Cost efficiency   | commodity machines       |

---

# 2. High Level Architecture

A search system operates in **two phases**:

### Offline Phase

Indexing documents.

### Online Phase

Serving user queries.

---

## High Level Architecture Diagram

```ascii
                     Users
                       |
                       v
                 +-----------+
                 | Load Balancer |
                 +------+----+
                        |
               +--------+--------+
               |                 |
               v                 v
          +--------+        +--------+
          | Search |        | Search |
          | Node   |        | Node   |
          +---+----+        +---+----+
              |                 |
              +--------+--------+
                       |
                       v
                 Result Merger
                       |
                       v
                Ranking Service
                       |
                       v
                 Top Results

---------------------------------------------------

                    INDEXING PIPELINE

Crawler → Document Storage → Indexer → Index Storage
```

---

## Core Components

### 1 Crawler

Collects documents.

Examples:

* web pages
* product descriptions
* blog posts

---

### 2 Document Storage

Stores raw documents.

Examples:

* distributed file system
* object storage
* blob store

---

### 3 Indexer

Builds the **search index** from documents.

Uses **distributed processing frameworks**.

---

### 4 Search Nodes

Handle user queries.

---

### 5 Index Storage

Stores distributed indexes.

---

# 3. Low Level Search Node Design

```ascii
             Query
               |
               v
        +-------------+
        | Query Parser|
        +------+------+ 
               |
               v
         +-----------+
         |Tokenizer  |
         +-----+-----+
               |
               v
         +-----------+
         |Index Lookup|
         +-----+-----+
               |
               v
         +-----------+
         |Ranking Engine|
         +-----+-----+
               |
               v
           Top Results
```

---

# 4. Storage Design

The key structure used is an **Inverted Index**.

Instead of storing:

```
document → words
```

we store

```
word → documents
```

This dramatically improves search speed .

---

## Example

Documents

```
D1: distributed search system
D2: search engine design
D3: distributed database
```

---

### Inverted Index

```
distributed → [D1, D3]
search      → [D1, D2]
system      → [D1]
engine      → [D2]
database    → [D3]
```

Each posting contains:

```
(docID, frequency, positions)
```

Example

```
search → [(D1,2,[3,8]), (D2,1,[2])]
```

---

# 5. Data Model

## Document

```json
{
  "doc_id": "123",
  "title": "Distributed Search",
  "content": "How distributed search works",
  "timestamp": 17000000
}
```

---

## Inverted Index Entry

```
Term: "search"

Posting List:

[
  {docID:1, freq:3, positions:[5,10,22]},
  {docID:8, freq:1, positions:[2]}
]
```

---

# 6. API Design

## Search API

```
GET /search?q=distributed+search
```

Response

```json
{
 "results":[
  {"doc_id":123,"score":0.91},
  {"doc_id":456,"score":0.84}
 ]
}
```

---

## Add Document

```
POST /document
```

```
{
 "doc_id":123,
 "content":"distributed search system"
}
```

---

## Delete Document

```
DELETE /document/{id}
```

---

# 7. Core Algorithms

## Tokenization

```
"Distributed Search Engine"
```

↓

```
["distributed","search","engine"]
```

---

## Stop Word Removal

Remove words like

```
the, is, to
```

Reduces index size but may slightly affect search semantics .

---

## Stemming

```
running → run
searching → search
```

---

## Inverted Index Construction

Pseudo algorithm

```
for document in documents:
    tokens = tokenize(document)
    for token in tokens:
        index[token].append(documentID)
```

---

## Query Processing

Example query

```
distributed search
```

Step 1: fetch posting lists

```
distributed → [D1,D3]
search → [D1,D2]
```

Step 2: intersection

```
[D1]
```

---

# 8. Ranking Algorithm

## TF-IDF

```
score = TF × IDF
```

Where

```
TF = term frequency
IDF = inverse document frequency
```

Modern search engines use:

* BM25
* ML ranking models

---

# 9. Distributed Index Partitioning

Two main approaches exist.

---

## Document Partitioning (Most Common)

Documents split across nodes.

```
Node1 → docs 1–1000
Node2 → docs 1001–2000
```

Query sent to all nodes.

---

### Architecture

```ascii
            Query
              |
              v
       +--------------+
       | Query Router |
       +------+-------+
              |
     +--------+--------+
     |        |        |
     v        v        v
   Node1    Node2    Node3
    |         |        |
    +---------+--------+
              |
              v
          Result Merge
```

---

## Term Partitioning

```
Node1 → terms A-F
Node2 → terms G-N
Node3 → terms O-Z
```

Drawback:

* heavy inter-node communication.

Therefore document partitioning is preferred .

---

# 10. Segment Based Index (Lucene Model)

Indexes are stored as **immutable segments**.

```
Index = Segment1 + Segment2 + Segment3
```

Benefits

* fast writes
* easy replication
* no locking

---

## Segment Merge

```
Segment1 + Segment2 + Segment3
        ↓
      Merge
        ↓
      Segment4
```

Improves query performance.

---

# 11. Real Time Indexing Pipeline

Instead of batch indexing:

```
Documents → Kafka → Index Workers → Segment Builder
```

Benefits

* scalable ingestion
* near real-time search

---

# 12. Query Processing Pipeline

```ascii
User Query
   |
   v
Query Parser
   |
   v
Query Rewrite
(spell correction)
   |
   v
Shard Router
   |
   v
Parallel Search
   |
   v
Top results per shard
   |
   v
Result Merger
   |
   v
Ranking Engine
   |
   v
Final Results
```

---

# 13. Ranking Pipeline

Large systems use **multi-stage ranking**.

---

## Stage 1 Candidate Retrieval

Fast ranking (BM25).

Return

```
top 1000 documents
```

---

## Stage 2 ML Ranking

Use features such as:

* click-through rate
* freshness
* popularity
* personalization

---

# 14. Query Caching

Popular queries cached.

```
cache["world cup"]
```

Cache types:

| Cache              | Purpose       |
| ------------------ | ------------- |
| Query cache        | store results |
| Posting list cache | index data    |
| result cache       | top K results |

---

# 15. Sharding and Replication

Example

```
1B documents → 100 shards
```

Each shard replicated.

```
Shard1 → NodeA NodeB NodeC
```

Benefits:

* fault tolerance
* scalability

---

# 16. Failure Handling

### Node failure

Replica promoted.

---

### Index corruption

Rebuild shard.

---

### Partial results

Return best results from available shards.

---

# 17. Multi Region Architecture

```ascii
        Global DNS
            |
     +------+------+
     |             |
     v             v
 US Search     EU Search
 Cluster        Cluster
```

Benefits

* low latency
* disaster recovery

---

# 18. Observability

Important metrics:

| Metric          | Purpose            |
| --------------- | ------------------ |
| query latency   | search performance |
| indexing lag    | freshness          |
| cache hit rate  | efficiency         |
| shard imbalance | load distribution  |

---

# 19. Trade-offs

| Design Choice         | Benefit          | Trade-off         |
| --------------------- | ---------------- | ----------------- |
| Inverted index        | fast search      | storage overhead  |
| document partitioning | simple           | broadcast queries |
| replication           | fault tolerance  | extra storage     |
| segment architecture  | fast writes      | merge cost        |
| caching               | faster queries   | stale results     |
| ML ranking            | better relevance | compute cost      |

---

# 20. Final Production Architecture

```ascii
                 Users
                   |
                   v
                 CDN
                   |
                   v
              Query Router
                   |
        +----------+-----------+
        |                      |
        v                      v
   Search Node            Search Node
        |                      |
        +----------+-----------+
                   |
                   v
              Result Merger
                   |
                   v
               ML Ranker
                   |
                   v
               Top Results

------------------------------------------------

Crawler → Kafka → Index Workers → Segment Builder
                                   |
                                   v
                             Distributed Index
```

---
