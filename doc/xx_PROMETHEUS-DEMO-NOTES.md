Prometheus for Java Developers
==============================

Docker image with all components downloaded:

```bash
docker run --rm -t \
    -p 9100:9100 -p 9090:9090 -p 3000:3000 -p 9093:9093 -p 8080:8080 -p 9200:9200 \
    -i fstab/prometheus-demo
```

Demo runs in a screen session, so we can run each tool in its own terminal.

```bash
screen
```

node_exporter
-------------

```console
> tar xfz node_exporter-0.12.0.linux-amd64.tar.gz
> cd node_exporter-0.12.0.linux-amd64
> ./node_exporter
```

View on [http://localhost:9100/metrics](http://localhost:9100/metrics)

Notes:

* As most Go programs, `node_exporter` is a single executable
* What is a TSDB? Basic datatype is float64 with timestamp.
* Browser shows text format, but Prometheus uses protobuf. ASCII format from OpenTSDB.
* `node_exporter` gets most info from `/proc` fs, which is mainly intended for native Linux hosts. For Docker containers look at `cAdviser`.

Prometheus Server
-----------------

```console
> tar xfz prometheus-1.2.1.linux-amd64.tar.gz
> cd prometheus-1.2.1.linux-amd64
```

Edit `prometheus.yml` and add config for node:

```yaml
- job_name: 'node'
  static_configs:
  - targets: ['localhost:9100']
```

View on [http://localhost:9090](http://localhost:9090)

* Show _Status_ -> _Configuration_
* Show _Status_ -> _Targets_

Notes:

* As most Go programs, `prometheus` is a single executable
* `scrape_interval` configured in `prometheus.yml`

PromQL Query Language
---------------------

```
node_network_transmit_bytes
node_network_transmit_bytes{device='eth0'}
node_network_transmit_bytes{device=~"eth.*"}
sum(node_network_transmit_bytes)
sum(node_network_transmit_bytes) by (device)
sum(node_network_transmit_bytes) by (instance)
sum(node_network_transmit_bytes + node_network_receive_bytes) by (device)
sum(node_network_transmit_bytes + node_network_receive_bytes) by (device) / 1024 / 1024

# All values that were recorded in the last 5m
node_network_transmit_bytes{device='eth0'}[5m]

# Per-second average rate of last 5m
rate(node_network_transmit_bytes{device='eth0'}[5m])
```

PromQL is well documented on [http://prometheus.io](http://prometheus.io)

Grafana
-------

```console
> tar xfz grafana-3.1.1-1470047149.linux-x64.tar.gz
> cd grafana-3.1.1-1470047149
> ./bin/grafana-server
```

View on [http://localhost:3000](http://localhost:3000), user _admin_, password _admin_

* Create Prometheus datasource
* Show Prometheus default dashboard
* Create dashboard with query form above: `rate(node_network_transmit_bytes{device='eth0'}[5m])`
* Show export as JSON and grafana.net

Alertmanager
============

```console
> tar xfz alertmanager-0.4.2.linux-amd64.tar.gz
> cd alertmanager-0.4.2.linux-amd64
> ./alertmanager -config.file simple.yml
```

View on [http://localhost:9093/](http://localhost:9093/)

Example alert:

```
# Alert for any instance that have a median request latency >1s.
ALERT APIHighRequestLatency
  IF api_http_request_latencies_second{quantile="0.5"} > 1
  FOR 1m
  ANNOTATIONS {
    summary = "High request latency on {{ $labels.instance }}",
    description = "{{ $labels.instance }} has a median request latency above 1s (current value: {{ $value }}s)",
  }
```

We skip the alerting demo, because we need to define alerts in Prometheus server, configure alertmanager URL, have a metric that triggers the alert, route the alert somewhere, which takes a while to configure.
