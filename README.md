JaCoCo Java Code Coverage Library
=================================

[![Build Status](https://travis-ci.org/jacoco/jacoco.svg?branch=master)](https://travis-ci.org/jacoco/jacoco)
[![Build status](https://ci.appveyor.com/api/projects/status/g28egytv4tb898d7/branch/master?svg=true)](https://ci.appveyor.com/project/JaCoCo/jacoco/branch/master)
[![Maven Central](https://img.shields.io/maven-central/v/org.jacoco/jacoco.svg)](http://search.maven.org/#search|ga|1|g%3Aorg.jacoco)

JaCoCo is a free Java code coverage library distributed under the Eclipse Public
License. Check the [project homepage](http://www.jacoco.org/jacoco)
for downloads, documentation and feedback.

Please use our [mailing list](https://groups.google.com/forum/?fromgroups=#!forum/jacoco)
for questions regarding JaCoCo which are not already covered by the
[extensive documentation](http://www.jacoco.org/jacoco/trunk/doc/).

Note:欢迎一起开发，有问题提issue
-------------------------------------------------------------------------

JaCoCo二次开发基于Git分支差分实现增量代码覆盖率。本仓库是fork自代码库https://github.com/fang-yan-peng/diff-jacoco.git, 在其基础上进行一些改动以满足自己项目上的需求，最终可通过jacoco.cli传入baseRevision、revision、coverageExcludes等参数来计算本地git项目变更覆盖率。
## 基于diff-jacoco的主要改动点
1. 修改jacococli命令行工具，添加变更覆盖率相关的参数，
   如projectPath(本地git项目路径), baseRevision(起始git revision), revision(当前git revison), excludes(覆盖率排除项)
2. 删除mysql部分，jacoco覆盖率数据本地保存
3. 删除与远程git仓库比较diff代码逻辑
4. 修改org.jacoco.report，coverageBuilder添加commitId之间的diff比较
5. jacococli支持不输入--classfiles参数，通过excludes来代替，即将覆盖率排除项后移到goal:report阶段，生成jacoco.exec阶段时可不填写覆盖率排除项

## 使用方法
####1. 打包
   mvn clean package  -Dmaven.javadoc.test=true -Dmaven.test.skip=true
####2. 获取jacococli的jar包
**/diff-jacoco/jacoco/target/jacoco-0.8.4.202202160912/lib/jacococli.jar
####3. 删除jar包一些授权信息，不然无法执行
zip -d /Users/han/git_repo/diff-jacoco/jacoco/target/jacoco-0.8.4.202202211159/lib/jacococli.jar 'META-INF/.SF' 'META-INF/.RSA' 'META-INF/*SF'
####4. 通过正常的途径获取jacoco.exec文件，可以用mvn插件，也可以通过on-the-fly模式
参考官网：https://www.jacoco.org/jacoco/trunk/doc/agent.html
####4. 计算增量覆盖率
java -jar jacococli.jar report jacoco.exec --projectPath /your_project_path --revision d6b2a0124f3998fca6416679e61ebbf7088e5d03 --baseRevision b01e9c1631316b518b33c6735836e0045a536071 --excludes "**/test/**,**/model/**" --sourcefiles /Users/han/git_repo/smartunitmng/app/service/src/main/java  --html report --xml report.xml

##适用场景&优势
 1. 持续集成CI场景，在流水线脚本中定制化计算变更覆盖率
 2. 与生成jacoco.exec阶段（goal:pre-agent）解耦，基于一份全量的jacoco.exec数据，生成多份定制化覆盖率数据
 3. 数据保存本地文件，无需mysql等数据库，轻量，使用方便
 4. 适用于企业内部自建质量管理平台（非sonarQube等）做定制化开发

## jacoco-diff原理
基于 JaCoCo 做相应改造，生成我们所需要的覆盖率模型，并通过 JaCoCo 开放的 API 实现相关功能。这里面主要需要解决的点在获取增量代码并解析生成覆盖率上。可以拆分成如下几个步骤：
1. 获取测试完成后的 exec 文件（二进制文件，里面有探针的覆盖执行信息）；
2. 通过jgit获取基线提交与被测提交之间的差异代码；
3. 对差异代码进行解析，切割为更小的颗粒度，我们选择方法作为最小纬度；
4. 改造 JaCoCo ，使它支持仅对差异代码生成覆盖率报告；生成报告时高亮线上变更行信息，未检出变更行不做处理。

