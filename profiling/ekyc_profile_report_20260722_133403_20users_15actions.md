# eKYC Performance Profiling Report
Generated on: 2026-07-22 13:34:03

## 📊 Run Configuration
- **Simulated Users (Threads)**: 20
- **Actions per User**: 15
- **Total Requests Executed**: 320
- **Test Duration**: 2.830 seconds
- **Throughput**: 113.06 requests/sec

## ⏱️ Latency Distribution
- **Average Latency**: 154.61 ms
- **Median (P50) Latency**: 137.56 ms
- **90th Percentile (P90)**: 262.63 ms
- **95th Percentile (P95)**: 303.19 ms
- **Min Latency**: 21.32 ms
- **Max Latency**: 447.82 ms

## ⚙️ Operations Breakdown
- **GET Dossier Requests**: 80
- **Validation Actions**: 72
- **Non-Critical Updates (Street/City)**: 80
- **Critical Updates (Country)**: 68
- **Errors Encountered**: 0

## 🧠 Internal Method Metrics
| Intercepted Class & Method | Invocation Count | Avg Execution Latency |
| :--- | :---: | :---: |
| `DossierWorkflowService.getDossierWithDetails()` | 172 | 100.90 ms |
| `DossierWorkflowService.updatePartyAddress()` | 148 | 33.16 ms |
| `EkycController.generateMockData()` | 1 | 3340.56 ms |
| `EkycController.getAllDossierIds()` | 1 | 209.14 ms |
| `EkycController.getDossier()` | 100 | 168.28 ms |
| `EkycController.resetStats()` | 1 | 0.24 ms |
| `EkycController.updatePartyAddress()` | 148 | 93.12 ms |
| `EkycController.validateDossier()` | 72 | 155.88 ms |
| `EkycSimulationService.generateMockData()` | 1 | 3339.89 ms |
| `RiskCalculationService.recalculatePartyRisk()` | 82 | 6.55 ms |
| `ValidationService.validateDossier()` | 72 | 3.71 ms |


## 🗄️ Database Metrics (Hibernate)
| Database Operation / Stat | Metric Count Value |
| :--- | :---: |
| JDBC Connections Acquired | 322 |
| SQL Statements Prepared | 8978 |
| Entities Loaded | 6007 |
| Entities Inserted | 3145 |
| Entities Updated | 143 |
| Lazy Collections Loaded | 172 |

## 🧠 Compliance Rules
- **Compliance Risk Recalculations Triggered**: 82

---
*Note: This report is generated dynamically by the simulate_load.py testing harness. Use these statistics as a baseline to benchmark optimizations such as JPQL FETCH JOIN query tuning or Redis caching strategies.*
