TaskDistribution
================

Sample code for the task distribution system (article under JGroupsArticles). The main class is Server. It
can be run with bin/run.sh. To change the config file or give the node a name:
cd bin
./run.sh -props ./udp.xml -name A

加上参数 `-Djgroups.bind_addr=127.0.0.1 -Djboss.tcpping.initial_hosts=127.0.0.1[7800] -Djava.net.preferIPv4Stack=true`