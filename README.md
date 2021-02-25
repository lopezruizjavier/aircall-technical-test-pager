# Aircall Technical Test - Aircall Pager

## Solution

The project structure is an standard Java EE approach, 
the code is splitted in two folders: src (packages by features) and test. 
There are no extra dependencies besides jUnit and Mockito, that are managed with Gradle.

To avoid race conditions in concurrent modifications over the same service, 
the db adapters must lock the selected objects. This could be implemented 
with a `SELECT FOR UPDATE` statement with SQL and with transactional methods.

## How to run the tests (90% coverage)

There is one test per use case. Just run the following command:

```
./gradlew test
```