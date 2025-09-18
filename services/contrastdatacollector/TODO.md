❯ kubectl exec -it -n demo-5 simulation-console-contrastdatacollector-9c756655-rfn85  -- curl http://localhost:5000/logs
{"description":"Logs are written to stdout and collected by Fluent Bit","output_mode":"stdout"}
❯ kubectl exec -it -n demo-5 simulation-console-contrastdatacollector-9c756655-rfn85  -- curl -X POST http://localhost:5000/clear-tracking
{"message":"Tracking cleared","status":"success"}

1. Implement a reset button for OpenSearch
2. 
