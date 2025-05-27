# http-sync-files

#### 介绍

文件同步工具,基于java-http开发. 支持通过配置不同的同步策略,在多台机器间定时或一次性同步多个文件


#### 软件架构
软件架构说明

java springboot，部分使用hutool处理，使用http传输文件。。。

#### 安装教程

1.  clone本仓库

2.  更改yml配置并且使用maven编译

3.  运行service中的jar，然后运行client中的jar

#### 使用说明

1.  service内置swagger,如需要正式使用请删除swagger依赖避免安全风险！！！

2.  目前只内置了简单的header key作为传输验证，如需要在互联网使用请自行更改为https使用，并且优化安全验证，maybe以后我会简单优化一下

3.  这只是一个非常轻量级的文件同步工具，适应学习和一些使用环境比较苛刻的地方（比如只能使用http传输文件的。。。）

4.  目前项目配置较为简陋，不支持多线程快速同步模式等，maybe以后我会慢慢添加？？？

#### 参与贡献

1.  Fork 本仓库

2.  新建 Feat_xxx 分支

3.  提交代码

4.  新建 Pull Request
