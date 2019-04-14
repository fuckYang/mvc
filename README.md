# mvc
此项目是模仿spring、springMVC做的一个demo项目

核心类MyDispatcherServlet:
      工作流程:
      1、加载配置文件。
      2、根据配置文件新扫描符合条件的文件。
      3、实例化IOC容器。
      4、实例化依赖注入。
      5、初始化客户端url映射的controller及方法。
      
aop实现
     目前只以cglib动态代理来实现，并且只是简单的实现了日志打印的功能。
     
mvc实现
     目前返回的类型为String和自定义的一个Model类。

在pom.xml里面必须加上此配置
    <build>
            <plugins>
                <plugin>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.7.0</version>
                    <configuration>
                        <compilerArgument>-parameters</compilerArgument>
                        <encoding>UTF-8</encoding>
                        <source>1.8</source>
                        <target>1.8</target>
                    </configuration>
                </plugin>
            </plugins>
      </build>

所需jar包
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>servlet-api</artifactId>
      <version>3.0-alpha-1</version>
      <scope>provided</scope>
    </dependency>

      <!-- jstl依赖 -->
      <dependency>
          <groupId>jstl</groupId>
          <artifactId>jstl</artifactId>
          <version>1.2</version>
      </dependency>

      <dependency>
          <groupId>org.apache.logging.log4j</groupId>
          <artifactId>log4j-core</artifactId>
          <version>2.7</version>
      </dependency>
      <dependency>
          <groupId>cglib</groupId>
          <artifactId>cglib</artifactId>
          <version>3.2.10</version>
      </dependency>
      
      
      
