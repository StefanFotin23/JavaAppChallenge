import urllib.request
import urllib.parse
import json
import random
import time
import concurrent.futures
import sys
import os
from datetime import datetime

BASE_URL = "http://127.0.0.1:8080/api/ekyc"

def send_request(url, method="GET", params=None):
    if params:
        encoded_params = urllib.parse.urlencode(params)
        if method == "GET":
            url = f"{url}?{encoded_params}"
            req = urllib.request.Request(url, method=method)
        else:
            data = encoded_params.encode('utf-8')
            req = urllib.request.Request(url, data=data, method=method)
    else:
        req = urllib.request.Request(url, method=method)
    
    try:
        with urllib.request.urlopen(req, timeout=5) as response:
            content = response.read().decode('utf-8')
            res_data = json.loads(content) if content.strip() else {}
            return response.status, res_data
    except Exception as e:
        return 500, str(e)

def main():
    print("=== eKYC Enterprise Load Test Simulator ===")
    
    # 1. Check connectivity & Reset stats
    print("Connecting to backend...")
    status, _ = send_request(f"{BASE_URL}/simulation/stats/reset", "POST")
    if status != 200:
        print("Error: Could not connect to Spring Boot application on localhost:8080.")
        sys.exit(1)
    
    # 2. Generate mock data
    print("Generating mock data (100 dossiers)...")
    status, res = send_request(f"{BASE_URL}/simulation/generate?numDossiers=100", "POST")
    if status == 200:
        print(f"Mock generation: {res.get('message')}")
    else:
        print("Warning: Mock generation endpoint returned non-200. Proceeding with existing data...")

    # 3. Retrieve dossier IDs
    status, dossier_ids = send_request(f"{BASE_URL}/dossiers/ids")
    if status != 200 or not dossier_ids:
        print("Error: Failed to retrieve dossier IDs from database.")
        sys.exit(1)
    
    print(f"Discovered {len(dossier_ids)} dossiers available for test.")
    
    # Configuration
    NUM_USERS = 20
    ACTIONS_PER_USER = 15
    print(f"Starting load simulation: {NUM_USERS} concurrent users, {ACTIONS_PER_USER} actions each...")
    
    stats = {
        "gets": 0,
        "validations": 0,
        "non_critical_updates": 0,
        "critical_updates": 0,
        "errors": 0,
        "latencies": []
    }
    
    def simulate_user(user_id):
        user_stats = {"gets": 0, "validations": 0, "non_critical_updates": 0, "critical_updates": 0, "errors": 0}
        local_latencies = []
        
        # Pick a random dossier for this user session
        dossier_id = random.choice(dossier_ids)
        
        # Load dossier to know party IDs
        t0 = time.time()
        status, dossier = send_request(f"{BASE_URL}/dossiers/{dossier_id}")
        local_latencies.append(time.time() - t0)
        
        if status != 200 or not dossier:
            user_stats["errors"] += 1
            return user_stats, local_latencies

        parties = dossier.get("parties", [])
        party_ids = [p.get("id") for p in parties if p.get("id")]
        
        for _ in range(ACTIONS_PER_USER):
            action = random.choice(["GET", "VALIDATE", "NON_CRITICAL_UPDATE", "CRITICAL_UPDATE"])
            
            t_action_start = time.time()
            if action == "GET":
                status, _ = send_request(f"{BASE_URL}/dossiers/{dossier_id}")
                if status == 200:
                    user_stats["gets"] += 1
                else:
                    user_stats["errors"] += 1
                    
            elif action == "VALIDATE":
                status, _ = send_request(f"{BASE_URL}/dossiers/{dossier_id}/validate", "POST")
                if status == 200:
                    user_stats["validations"] += 1
                else:
                    user_stats["errors"] += 1
                    
            elif action == "NON_CRITICAL_UPDATE" and party_ids:
                party_id = random.choice(party_ids)
                params = {
                    "street": f"Street {random.randint(1, 1000)}",
                    "city": "Zürich",
                    "country": "Switzerland"
                }
                status, _ = send_request(f"{BASE_URL}/dossiers/parties/{party_id}/address", "PUT", params)
                if status == 200:
                    user_stats["non_critical_updates"] += 1
                else:
                    user_stats["errors"] += 1
                    
            elif action == "CRITICAL_UPDATE" and party_ids:
                party_id = random.choice(party_ids)
                params = {
                    "street": "100 Zürich St",
                    "city": "Zürich",
                    "country": random.choice(["Cayman Islands", "Panama", "Bahamas"])
                }
                status, _ = send_request(f"{BASE_URL}/dossiers/parties/{party_id}/address", "PUT", params)
                if status == 200:
                    user_stats["critical_updates"] += 1
                else:
                    user_stats["errors"] += 1
            
            local_latencies.append(time.time() - t_action_start)
            
        return user_stats, local_latencies

    start_time = time.time()
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=NUM_USERS) as executor:
        futures = [executor.submit(simulate_user, i) for i in range(NUM_USERS)]
        for future in concurrent.futures.as_completed(futures):
            try:
                user_res, user_latencies = future.result()
                for k in stats:
                    if k in user_res:
                        stats[k] += user_res[k]
                stats["latencies"].extend(user_latencies)
            except Exception as exc:
                print(f"User thread generated an exception: {exc}")
                stats["errors"] += 1

    total_duration = time.time() - start_time
    
    # 4. Fetch final performance and database stats
    status, final_stats = send_request(f"{BASE_URL}/simulation/stats")
    if status == 200:
        risk_calculations = final_stats.get("riskRecalculationsCount", 0)
        db_metrics = final_stats.get("databaseMetrics", {})
        method_metrics = final_stats.get("methodMetrics", {})
    else:
        risk_calculations = 0
        db_metrics = {}
        method_metrics = {}

    # 5. Process Percentiles
    latencies = sorted(stats['latencies'])
    total_reqs = len(latencies)
    avg_latency = (sum(latencies) / total_reqs * 1000) if total_reqs > 0 else 0
    p50_latency = (latencies[int(total_reqs * 0.50)] * 1000) if total_reqs > 0 else 0
    p90_latency = (latencies[int(total_reqs * 0.90)] * 1000) if total_reqs > 0 else 0
    p95_latency = (latencies[int(total_reqs * 0.95)] * 1000) if total_reqs > 0 else 0
    min_latency = (latencies[0] * 1000) if total_reqs > 0 else 0
    max_latency = (latencies[-1] * 1000) if total_reqs > 0 else 0
    rps = total_reqs / total_duration if total_duration > 0 else 0

    # Console output
    print("\n" + "="*60)
    print("                LOAD TEST SUMMARY")
    print("="*60)
    print(f"Active Users (Threads)    : {NUM_USERS}")
    print(f"Total Requests Executed   : {total_reqs}")
    print(f"Total Duration (sec)      : {total_duration:.3f} s")
    print(f"Throughput (req/sec)      : {rps:.2f} rps")
    print(f"Errors Encountered        : {stats['errors']}")
    print("-"*60)
    print(f"Avg Request Latency       : {avg_latency:.2f} ms")
    print(f"Median (P50) Latency      : {p50_latency:.2f} ms")
    print(f"90th Percentile (P90)     : {p90_latency:.2f} ms")
    print(f"95th Percentile (P95)     : {p95_latency:.2f} ms")
    print(f"Min / Max Latency         : {min_latency:.2f} ms / {max_latency:.2f} ms")
    print("="*60)
    print("             INTERNAL METHOD METRICS")
    print("="*60)
    for method, m_stat in sorted(method_metrics.items()):
        print(f"{method:<45} : Calls: {m_stat.get('callCount'):<5} | Avg: {m_stat.get('avgDurationMs'):.2f} ms")
    print("="*60)
    print("             DATABASE METRICS (HIBERNATE)")
    print("="*60)
    print(f"JDBC Connections Acquired : {db_metrics.get('connectionsCount')}")
    print(f"SQL Statements Prepared   : {db_metrics.get('statementsPrepared')}")
    print(f"Entities Loaded           : {db_metrics.get('entitiesLoaded')}")
    print(f"Entities Inserted         : {db_metrics.get('entitiesInserted')}")
    print(f"Entities Updated          : {db_metrics.get('entitiesUpdated')}")
    print(f"Lazy Collections Loaded   : {db_metrics.get('collectionsLoaded')}")
    print("-"*60)
    print(f"Risk Recalculations Run   : {risk_calculations}")
    print("="*60)

    # 6. Save results to profiling package folder
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(script_dir)
    profiling_dir = os.path.join(project_root, "profiling")
    os.makedirs(profiling_dir, exist_ok=True)
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    file_name = f"ekyc_profile_report_{timestamp}_{NUM_USERS}users_{ACTIONS_PER_USER}actions.md"
    file_path = os.path.join(profiling_dir, file_name)

    # Generate Method metrics markdown table
    method_table_rows = ""
    for method, m_stat in sorted(method_metrics.items()):
        method_table_rows += f"| `{method}` | {m_stat.get('callCount')} | {m_stat.get('avgDurationMs'):.2f} ms |\n"

    report_content = f"""# eKYC Performance Profiling Report
Generated on: {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}

## 📊 Run Configuration
- **Simulated Users (Threads)**: {NUM_USERS}
- **Actions per User**: {ACTIONS_PER_USER}
- **Total Requests Executed**: {total_reqs}
- **Test Duration**: {total_duration:.3f} seconds
- **Throughput**: {rps:.2f} requests/sec

## ⏱️ Latency Distribution
- **Average Latency**: {avg_latency:.2f} ms
- **Median (P50) Latency**: {p50_latency:.2f} ms
- **90th Percentile (P90)**: {p90_latency:.2f} ms
- **95th Percentile (P95)**: {p95_latency:.2f} ms
- **Min Latency**: {min_latency:.2f} ms
- **Max Latency**: {max_latency:.2f} ms

## ⚙️ Operations Breakdown
- **GET Dossier Requests**: {stats['gets']}
- **Validation Actions**: {stats['validations']}
- **Non-Critical Updates (Street/City)**: {stats['non_critical_updates']}
- **Critical Updates (Country)**: {stats['critical_updates']}
- **Errors Encountered**: {stats['errors']}

## 🧠 Internal Method Metrics
| Intercepted Class & Method | Invocation Count | Avg Execution Latency |
| :--- | :---: | :---: |
{method_table_rows}

## 🗄️ Database Metrics (Hibernate)
| Database Operation / Stat | Metric Count Value |
| :--- | :---: |
| JDBC Connections Acquired | {db_metrics.get('connectionsCount')} |
| SQL Statements Prepared | {db_metrics.get('statementsPrepared')} |
| Entities Loaded | {db_metrics.get('entitiesLoaded')} |
| Entities Inserted | {db_metrics.get('entitiesInserted')} |
| Entities Updated | {db_metrics.get('entitiesUpdated')} |
| Lazy Collections Loaded | {db_metrics.get('collectionsLoaded')} |

## 🧠 Compliance Rules
- **Compliance Risk Recalculations Triggered**: {risk_calculations}

---
*Note: This report is generated dynamically by the simulate_load.py testing harness. Use these statistics as a baseline to benchmark optimizations such as JPQL FETCH JOIN query tuning or Redis caching strategies.*
"""
    
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(report_content)
    
    print(f"Saved detailed profiling report to: {file_path}")

if __name__ == "__main__":
    main()
