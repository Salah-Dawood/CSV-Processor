# Concurrent CSV Data Processor

A Spring Boot application that reads employee records from an uploaded CSV file and applies a salary increment to each employee **concurrently**, using a fixed-size thread pool from Java's Executor framework.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Tech Stack](#2-tech-stack)
3. [Project Setup](#3-project-setup)
4. [Using the Application](#4-using-the-application)
5. [Business Rules](#5-business-rules)
6. [Project Structure and Code Documentation](#6-project-structure-and-code-documentation)
7. [Concurrency Design](#7-concurrency-design)
8. [Project Report](#8-project-report)

---

## 1. Overview

The application exposes one REST endpoint. A client uploads a CSV file of employees. The server parses the file, hands each employee to a pool of worker threads that calculates and applies the new salary, waits for all workers to finish, and returns the updated employees as JSON.

Each CSV row contains: employee ID, name, current salary, joined date, role, and the fraction of projects completed.

```
Client ──POST CSV──▶ CsvController ──▶ CsvService ──parse──▶ List<Employee>
                                            │
                                            ▼
                               ConcurrentProcessService
                          (fixed thread pool, 4 worker threads)
                                            │
                  ┌─────────────┬───────────┴─┬─────────────┐
                  ▼             ▼             ▼             ▼
               worker 1      worker 2      worker 3      worker 4
                  └──────── Employee.updateSalary() ────────┘
                                            │
Client ◀──JSON (updated employees)──────────┘
```

## 2. Tech Stack

| Component | Version                                                                                       |
|---|-----------------------------------------------------------------------------------------------|
| Java | 17                                                                                            |
| Spring Boot | 4.1.1 (`spring-boot-starter-webmvc`)                                                          |
| Build tool | Maven                                                                                         |
| Lombok | 1.18.48                                                                                       |
| Concurrency | `java.util.concurrent` (`ExecutorService`, `Executors.newFixedThreadPool`) |

## 3. Project Setup

### Prerequisites

- JDK 17 or newer (`java -version` to check)
- Git
- Maven is **not** required, since the project includes the Maven wrapper (`mvnw`)
- If you use an IDE (IntelliJ), install the **Lombok plugin** and enable *annotation processing*


## 4. Using the Application

### Endpoint

| | |
|---|---|
| URL | `http://localhost:9095/api/upload` |
| Method | `POST` |
| Content type | `multipart/form-data` |
| Form field | `file` (the CSV file) |

### CSV format

- **No header row.** The first line must already be an employee.
- Columns, in order: `id,name,salary,joinedDate,role,projectCompletion`
- `joinedDate` uses ISO format `yyyy-MM-dd`
- `role` must be exactly `Employee`, `Manager` or `Director` (case-sensitive)
- `projectCompletion` is a **fraction** between 0 and 1 (`0.75` means 75%, not `75`)

Example `employees.csv`:

```csv
1,Alice Johnson,5000,2019-03-15,Manager,0.85
2,Bob Smith,3000,2022-07-01,Employee,0.55
3,Carol White,9000,2015-01-10,Director,0.70
4,Dan Brown,3500,2026-06-01,Employee,0.90
```

### Example request

in Postman: *POST* to the URL above, *Body → form-data*, add a key named `file`, set its type to **File** and choose your CSV.

### Response

`200 OK` with a JSON array of the employees **after** the increment, each with the fields `id`, `name`, `salary`, `joinedDate`, `role` and `projectCompletion`.

If a line cannot be parsed (wrong number of columns, invalid number or date, a header row), the request fails with an error that includes the parsing message.

The console also logs each parsed row and which worker thread (`pool-N-thread-M`) processed which employee ID.

## 5. Business Rules

Rules as implemented in `Employee.updateSalary()` and `ConcurrentProcessService.validIncrement()`:

| Rule | Behaviour |
|---|---|
| **Project completion below 60%** | No increase. |
| **Years worked** | The number of *completed* years between the joined date and today. Less than one full year means no increase. |
| **Role** | Employee: 1%, Manager: 2%, Director: 5%. |
| **Completion above 80% (bonus)** | The raise is boosted by a factor of 1.5. |
| **Applying the raise** | The salary is increased once for every completed year of work. |

## 6. Project Structure

```
src/main/java/com/salah/csvprocessor
├── CsvprocessorApplication.java
├── controller
│   └── CsvController.java
├── model
│   └── Employee.java
└── service
    ├── CsvService.java
    └── ConcurrentProcessService.java
```

## 7. Concurrency Design

**Thread pooling.** `Executors.newFixedThreadPool(4)` creates a pool with at most four worker threads. Threads are created as tasks arrive and are then reused. If all four are busy, the remaining tasks wait in the pool's internal queue and run in submission order.

**Waiting for completion.** After submitting every task, the service calls `executor.shutdown()`, which stops the pool accepting new tasks but lets queued ones finish, followed by `awaitTermination(5, MINUTES)`, which blocks the request thread until all workers are done. This guarantees the response contains the *final* salaries rather than a half-processed list.

**One pool per request.** The pool is created inside `processEmployees` and shut down at the end of it, so every upload gets a fresh pool and no threads are left running afterwards.

**Thread safety.** `Employee.updateSalary()` is `synchronized`, so it holds the lock on that employee object while it reads and writes `salary`. The salary update is therefore safe even if two threads were to touch the same employee.

## 8. Project Report

### Objectives status

| Objective | Status | Where |
|---|---|---|
| Spring Boot project | Done | whole project |
| CSV file reading | Done | `CsvService.parseCsv` |
| Thread pooling with the Executor framework | Done | `ConcurrentProcessService` (`newFixedThreadPool`) |
| Concurrency control with locks and semaphores | Partial: `synchronized` only, no `Lock` or `Semaphore` | `Employee`, `ConcurrentProcessService` |
| Atomic variables | Not implemented | n/a |
| Less than 60% completion gets no increase | Done | `validIncrement` |
| Increase based on years worked | Implemented, see limitations | `Employee.updateSalary` |
| Role-based increase | Done | `Employee.updateSalary` |
| Bonus: above 80% completion gets a 1.5x raise | Implemented, see limitations | `Employee.updateSalary` |
| Bonus: frontend and interactivity | Not implemented | n/a |
| Documentation | This file | `README.md` |

### Outcomes

- A CSV of employees can be uploaded and returns the updated salaries as JSON.
- Processing is distributed across four named worker threads, visible in the console log.
- The request does not return until every employee has been processed.

### Challenges encountered

- **A loop that never ended.** An early version of the salary calculation looped once per year worked but never decreased its counter, so any eligible employee locked up a worker thread forever. With a fixed pool, a few such employees would have stalled all processing. Adding the missing decrement fixed it, and it showed how a single bug inside a task can block a whole pool.
- **Waiting for the workers.** Submitting tasks returns immediately, so without an explicit wait the response was sent before the salaries had changed. The `shutdown()` and `awaitTermination()` combination solved this.

### Known limitations and future work

1. **Input validation.** No header-row handling, roles are case-sensitive, and a bad line fails the whole upload.
