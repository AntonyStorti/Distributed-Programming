FROM ubuntu:24.04

# Aggiorna il sistema e installa Redis
RUN apt update && apt install -y redis-server

# Installa OpenJDK
RUN apt install -y openjdk-21-jdk

# Espone la porta Redis
EXPOSE 6379

# Imposta il comando di default per lanciare Redis con il clustering abilitato
CMD ["redis-server", "--cluster-enabled", "yes", "--appendonly", "yes", "--cluster-config-file", "/data/nodes.conf", "--cluster-node-timeout", "5000", "--bind", "0.0.0.0", "--protected-mode", "no"]
