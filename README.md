# Rcjdemo

本项目为一个与RepChain进行数据互操作的Demo，适用于各种基于RepChain的区块链应用开发。

区块链应用与区块链进行数据互操作主要分为两部分：

1. 向区块链提交数据；
2. 获取区块链上的数据，以查看哪些数据成功被打包出块固化到区块链上。

其中后者通常会将链上各个区块数据按顺序同步下来、进行解析之后持久化到本地的数据库中。具体区块链应用设计原理在此不赘述。

本项目采用了Spring boot，引入了RepChain Java版本的SDK（RCJava），本身能作为Web服务后端，给出了向RepChain提交数据以及同步解析来自RepChain数据的示例。



提交数据参见：`com.example.rcjdemo.RcjdemoApplicationTests`

解析数据参见：`com.example.rcjdemo.common.BlockSync`中的`onSuccess`方法

配置文件：`src/main/resources/application.yml`

其中数据库配置仅为提供完整示例，目前示例运行中不读写数据库。

运行RcjdemoApplicationTests提交数据的同时会启动RcjdemoApplication，开启同步。


两个.py文件目前用不到，请忽视。


使用时请注意分支需与RepChain配套。

## 开启SSL认证

1. 注册用户并授权接口访问权限，参考代码 `RcjdemoApplicationTests.addSubmitterToRepchain`

2. 将授权用户的jks放入Repchain `mytrustkeystore.jks` 中，或者 [SDK提交](https://gitee.com/BTAJL/RCJava-core/blob/master/src/test/java/com/rcjava/multi_chain/ManageNodeCertTest.java#L183),
3. 修改 `application.yml` 中的配置,
```yaml
repchain:
  # 区块链地址
  host: 192.168.199.10:9081
  # 需要同步的区块初始高度
  blockHeight: 0
  # 开启ssl双向认证，此项一定要开启
  enableSSL: true
  # 服务端的证书的jks
  serverCertJksPath: D:\ideaProject\RCJava-core\jks\jdk13\node.jks
  # 服务端证书的jks密码
  serverJksPassword: 123
  # 用户自己持有的jks
#  jksPath: D:\ideaProject\RCJava-core\jks\jdk13\121000005l35120456.node1.jks
  jksPath: D:\ideaProject\rcjdemo\src\main\resources\jindian.jks
  # 用户的jks的password
  storePassword: 123
  # 用户的私钥的password
  keyPassword: 123
```
4. 修改同步程序，示例位于 `com.example.rcjdemo.common.BlockSync`

5. 提交时请使用Spring托管的客户端。
```java
    @Autowired
    private TranPostClient tranPostClient;
```

6. 自定义客户端请参考 `com.example.rcjdemo.common.RepchainConfiguration`