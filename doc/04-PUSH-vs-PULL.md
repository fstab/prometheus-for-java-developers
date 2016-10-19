Push vs Pull
============

```
node_exporter  <----HTTP---- prometheus server
port: 9000
```

* Prometheus server pulls metrics from exporters
  - Pro: Exporters don't need to know anything about the Prometheus Server(s)
  - Con: Service discovery needed when adding services/exporters
* exporter per service, not per machine
