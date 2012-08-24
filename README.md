Smaller
=======

Summary
-------
Smaller is a small webservice for minification and transcoding sources.
It currently supports coffeescript, closure, uglifyjs, lessjs, yuicompressor and
embeddcss.

Services
--------
+ Maven Plugin
+ Ant Task
+ Webservice Standalone
+ Embeddable Servlet
+ OSGi bundles

Configurations
--------------

### Maven Plugin

    <plugin>
      <groupId>de.matrixweb.smaller</groupId>
      <artifactId>smaller-maven-plugin</artifactId>
      <version>0.5.0-SNAPSHOT</version>
      <configuration>
        <processor>closure,uglifyjs,lessjs,cssembed,yuiCompressor</processor>
        <files>
          <directory>src/main/webapp/resources</directory>
          <includes>
            <include>**/*.js</include>
            <include>**/*.less</include>
          </includes>
          <excludes>
            <exclude>**/*.bin</exclude>
          </excludes>
        </files>
        <in>basic.json,style.less</in>
        <out>basic-min.js,style.css</out>
        <target>target/smaller</target>
        <host>localhost</host>
        <port>1148</port>
      </configuration>
    </plugin>

### Ant Task

    <taskdef name="smaller" classname="de.matrixweb.smaller.clients.ant.SmallerTask" classpath="../../../../target/classes" />
    
    <target name="smaller">
      <smaller 
          processor="closure,uglifyjs,lessjs,cssembed,yuiCompressor"
          in="basic.json,style.less"
          out="basic-min.js,style.css"
          target="target/smaller"
          host="localhost"
          port="1148">
        <fileset dir="src/main/webapp/resources" includes="**/*.js,**/*.less" excludes="**/*.bin" />
      </smaller>
    </target>

### Webservice Standalone

Just start the standalone build which has a main method included.
Parameters are port and ip-address to bind to in this order.

### Embeddable Servlet

    <servlet>
      <servlet-name>smaller</servlet-name>
      <servlet-class>de.matrixweb.smaller.servlet.SmallerServlet</servlet-class>
      <init-param>
        <param-name>processors</param-name>
        <param-value>closure,lessjs</param-value>
      </init-param>
      <init-param>
        <param-name>includes</param-name>
        <param-value>**/*.js,**/*.less</param-value>
      </init-param>
      <init-param>
        <param-name>excludes</param-name>
        <param-value>**/*.bin</param-value>
      </init-param>
    </servlet>
    <servlet-mapping>
      <servlet-name>smaller</servlet-name>
      <url-pattern>/css/style.css</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
      <servlet-name>smaller</servlet-name>
      <url-pattern>/js/masic-min.js</url-pattern>
    </servlet-mapping>

### OSGi bundles

TODO
