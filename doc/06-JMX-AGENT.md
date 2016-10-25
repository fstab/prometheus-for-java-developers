JMX Prometheus Bridge
=====================

JMX Remote
----------

```
Application         <----- JMX Exporter      <--------- Prometheus
RMI Port 5555 (*)          HTTP Port 9200
```

JMX Agent
---------

```
Application         <---------------------------------- Prometheus
+ Prometheus Agent
HTTP Port 9200
```
