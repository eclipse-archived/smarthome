---
layout: documentation
---

{% include base.html %}

# H2SQL Persistence
The H2 SQL persistence service provides a high performing embedded database.

## Storage Overview
Data is stored with a timestamp with millisecond accuracy. If multiple data points are stored faster than each millisecond, the system will store the most recent value in each millisecond.

Most ESH data types are stored as a string. This ensures that all data types can be persisted and restored accurately. NumberItem is stored as a BigDecimal, and DimmerItem and RollershutterItem are retrieved as PercentType and stored as Integers in the database. This primarily reduces storage space (for NumberItem, this is approximately 60% of the size of storing all data as Strings in the database).

Each item is stored in a separate table - the table name being the item name.