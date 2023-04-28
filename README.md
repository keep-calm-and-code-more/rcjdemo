# Rcjdemo

本项目为一个与RepChain进行数据互操作的Demo，适用于各种基于RepChain的区块链应用开发。

区块链应用与区块链进行数据互操作主要分为两部分：（1）向区块链提交数据；（2）获取区块链上的数据，以查看哪些数据成功被打包出块固化到区块链上。其中后者通常会将链上各个区块数据按顺序同步下来、进行解析之后持久化到本地的数据库中。具体区块链应用设计原理在此不赘述。

本项目采用了Spring boot，引入了RepChain Java版本的SDK（RCJava），本身能作为Web服务后端，给出了向RepChain提交数据以及同步解析来自RepChain数据的示例。



提交数据参见：`com.example.rcjdemo.RcjdemoApplicationTests`

解析数据参见：`com.example.rcjdemo.common.BlockSync`中的`onSuccess`方法

配置文件：`src/main/resources/application.yml`

其中数据库配置仅为提供完整示例，目前示例运行中不读写数据库。

运行RcjdemoApplicationTests提交数据的同时会启动RcjdemoApplication，开启同步。





使用时请注意分支需与RepChain配套。
