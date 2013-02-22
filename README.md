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

Use the maven plugin to utilize the smaller webservice:

    <plugin>
      <groupId>de.matrixweb.smaller</groupId>
      <artifactId>smaller-maven-plugin</artifactId>
      <version>0.6.0-SNAPSHOT</version>
      <configuration>
        <host>localhost</host>
        <port>1148</port>
        <proxyhost>localhost</proxyhost>
        <proxyport>8080</proxyport>
        <target>target/smaller</target>
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
        <tasks>
          <task>
            <processor>closure,uglifyjs,lessjs:1.3.0,cssembed,yuiCompressor</processor>
            <in>basic.json,style.less</in>
            <out>basic-min.js,style.css</out>
            <options>output:out-only=true;cssembed:max-uri-length=0</options>
          </task>
        </tasks>
      </configuration>
    </plugin>

Or use the standalone maven plugin if you do not want to use the webservice:

    <plugin>
      <groupId>de.matrixweb.smaller</groupId>
      <artifactId>smaller-maven-standalone-plugin</artifactId>
      <version>0.6.0-SNAPSHOT</version>
      <configuration>
        <target>target/smaller</target>
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
        <tasks>
          <task>
            <processor>closure,uglifyjs,lessjs,cssembed,yuiCompressor</processor>
            <in>basic.json,style.less</in>
            <out>basic-min.js,style.css</out>
            <options>cssembed:max-uri-length=0</options>
          </task>
        </tasks>
      </configuration>
    </plugin>

### Ant Task

    <taskdef name="smaller" classname="de.matrixweb.smaller.clients.ant.SmallerTask" classpath="../../../../target/classes" />
    
    <target name="smaller">
      <smaller 
          processor="closure,uglifyjs,lessjs,cssembed,yuiCompressor"
          in="basic.json,style.less"
          out="basic-min.js,style.css"
          options="output:out-only=true"
          target="target/smaller"
          host="localhost"
          port="1148"
          proxyhost="localhost"
          proxyport="8080">
        <fileset dir="src/main/webapp/resources" includes="**/*.js,**/*.less" excludes="**/*.bin" />
      </smaller>
    </target>

### Webservice Standalone

Just start the standalone build which has a main method included.
Parameters are port and ip-address to bind to in this order.

### OSGi bundles

Start the osgi-server module with one of the supplied scripts start.sh or start-local.sh.
This either downloads dependencies from maven central or from a local maven repository.

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
      <init-param>
        <param-name>options</param-name>
        <param-value>output:out-only=true</param-value>
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

Credits
-------

Thanks to [SinnerSchrader](http://www.sinnerschrader.com/) for their support
and the time to work on this project.
