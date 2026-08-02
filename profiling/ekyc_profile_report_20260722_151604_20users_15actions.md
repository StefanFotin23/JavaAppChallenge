# eKYC Performance Profiling Report
Generated on: 2026-07-22 15:16:04

## 📊 Run Configuration
- **Simulated Users (Threads)**: 20
- **Actions per User**: 15
- **Total Requests Executed**: 20
- **Test Duration**: 0.726 seconds
- **Throughput**: 27.55 requests/sec

## ⏱️ Latency Distribution
- **Average Latency**: 690.13 ms
- **Median (P50) Latency**: 691.15 ms
- **90th Percentile (P90)**: 716.22 ms
- **95th Percentile (P95)**: 717.43 ms
- **Min Latency**: 669.50 ms
- **Max Latency**: 717.43 ms

## ⚙️ Operations Breakdown
- **GET Dossier Requests**: 0
- **Validation Actions**: 0
- **Non-Critical Updates (Street/City)**: 0
- **Critical Updates (Country)**: 0
- **Errors Encountered**: 20

## 🧠 Internal Method Metrics
| Intercepted Class & Method | Invocation Count | Avg Execution Latency |
| :--- | :---: | :---: |
| `DossierWorkflowService.getDossierWithDetails()` | 20 | 75.16 ms |
| `EkycController.generateMockData()` | 1 | 2529.90 ms |
| `EkycController.getAllDossierIds()` | 1 | 92.84 ms |
| `EkycController.getDossier()` | 20 | 628.79 ms |
| `EkycController.resetStats()` | 1 | 6.66 ms |
| `EkycSimulationService.generateMockData()` | 1 | 2529.31 ms |


## 🗄️ Database Metrics (Hibernate)
| Database Operation / Stat | Metric Count Value |
| :--- | :---: |
| JDBC Connections Acquired | 22 |
| SQL Statements Prepared | 3703 |
| Entities Loaded | 890 |
| Entities Inserted | 3085 |
| Entities Updated | 0 |
| Lazy Collections Loaded | 20 |

## 🧠 Compliance Rules
- **Compliance Risk Recalculations Triggered**: 0

---
*Note: This report is generated dynamically by the simulate_load.py testing harness. Use these statistics as a baseline to benchmark optimizations such as JPQL FETCH JOIN query tuning or Redis caching strategies.*
