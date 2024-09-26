# Usa l'immagine base di Ubuntu 24.04
FROM ubuntu:24.04

# Installa OpenSSH server e altri pacchetti utili
RUN apt-get update && \
    apt-get install -y openssh-server sudo && \
    mkdir /var/run/sshd && \
    echo 'root:rootpassword' | chpasswd && \
    sed -i 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config && \
    sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config

# Abilita SSH
RUN service ssh start

# Imposta una password per l'utente root
RUN echo 'root:rootpassword' | chpasswd

# Espone la porta 22 per SSH
EXPOSE 22

# Comando di avvio per SSH
CMD ["/usr/sbin/sshd", "-D"]
