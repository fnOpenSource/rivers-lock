# Rivers Lock
	Rivers Lock，是一款轻量级的分布式锁软件，不依赖Zookeeper redis等第三方，支持集群局域网自动识别或者通过IP识别组成集群。
# Usage Example
```Java 
$ Java>=1.8
```
```Java
  river.setHosts("10.200.241.182");//可选
  river.setMininum_nodes(1);//可选
  river.setStartMode("P2P");//锁支持两种模型P2P与CS模型
  river.start(); 
  ReentrantLock lock = new ReentrantLock();
  if(lock.lock("锁名称")) {
    System.out.println("success");
    lock.unLock("锁名称");
  }
```
