Prometheus Demo
===============

* `node_exporter`: Provides metrics
* `prometheus`:    Metric database and query language
* `grafana`:       Dashboard
* `alertmanager`:  Handles alerts

```
node_exporter <--- Prometheus Server <--- Grafana Dashboard
                                  |
                                   -----> Alertmanager
```
