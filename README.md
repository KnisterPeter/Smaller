Smaller
=======

Summary
-------
Smaller is a small webservice for minification and transcoding sources.
It currently supports coffeescript, closure, uglifyjs, lessjs, typescript, 
jshint, browserify, sweetjs, yuicompressor, ycssmin, csso, embeddcss and svgo.


Starting as server-side only application for build preprocessing Smaller is
now embedded in the [Smaller Development Server](https://github.com/KnisterPeter/smaller-dev-server)
to be used for speed up local development with all the same build tools
used for build preprocessing.

Services
--------
+ Maven Plugin
+ Maven Standalone Plugin
+ Ant Task
+ Ant Standalone Task
+ Webservice Standalone
+ Embeddable Servlet
+ OSGi bundles

Processors
----------

##### For CSS

+ Less
    + **Name**: `lessjs`
    + **Description**: Compiles LESS files to CSS. This is a merge task.
    * **Website**: http://github.com/cloudhead/less.js
    * **Versions**: 1.3.0, 1.3.3, 1.4.0, 1.4.1, 1.4.2, 1.5.0
+ ycssmin
    + **Name**: `ycssmin`
    + **Description**: Minifies CSS files with ycssmin
    * **Website**: http://github.com/yui/ycssmin
+ csso
    + **Name**: `csso`
    + **Description**: Minifies CSS files with csso
    * **Website**: http://bem.info/tools/optimizers/csso/
+ YUICompressor
    + **Name**: `yuicompressor`
    + **Description**: Minifies CSS files with YUICompressor
    * **Website**: http://github.com/yui/yuicompressor
+ CSSEmbed
    + **Name**: `cssembed`
    + **Description**: Embeds all CSS url() images as Base64 encoded images
    + **Configuration**:
        + max-uri-length=[integer]
        + max-image-size=[integer]
    * **Website**: http://github.com/nzakas/cssembed/


### For JavaScript

+ Merge (default)
    + **Name**: `merge`
    + **Description:** 
        Joins all given source files by type (js, css) into one file. If not 
        other merge task is in the chain this one is prepended as first one.
+ Coffee-Script
    + **Name**: `coffeeScript`
    + **Description:** 
        Compiles all .coffee files into one JavaScript. Needs to be called 
        before `merge`. So set merge as second step (e.g. `coffeeScript,merge`)!  
        This processors - if configured - operates on all found js files.
    + **Configuration**:
        + bare=[true|false]
        + header=[true|false]
    * **Website**: http://github.com/jashkenas/coffee-script
    * **Versions**: 1.3.3, 1.4.0, 1.5.0, 1.6.3
+ Typescript
    + **Name**: `typescript`
    + **Description:** 
        Compiles all .ts files into one JavaScript. Needs to be called before 
        `merge`. So set merge as second step: `typescript,merge`!
    * **Website**: http://www.typescriptlang.org/
+ Closure Compiler
    + **Name**: `closure`
    + **Description:** 
        Minifies JavaScript files with Closure Compiler.
    * **Website**: https://code.google.com/p/closure-compiler/
+ UglifyJS
    + **Name**: `uglifyjs`
    + **Description:** 
        Minifies JavaScript files with UglifyJS.
    * **Website**: http://github.com/mishoo/UglifyJS
+ JS Hint
    + **Name**: `jshint`
    + **Description:** 
        Tests JavaScript files with JS Hint and returns it's output.
    * **Website**: http://github.com/jshint/jshint
+ browserify
    + **Name**: `browserify`
    + **Description:** 
        Resolves CommonJS modules to be browser ready. This is a merge task.
    + **Configuration**:
        + aliases=["file#alias", "file#alias"]
    * **Website**: http://browserify.org/
+ sweet.js
    + **Name**: `sweetjs`
    + **Description:** 
        Sweet.js brings the hygienic macros of languages like Scheme and Rust 
        to JavaScript.  
        This processors - if configured - operates on all found js files.
    * **Website**: http://sweetjs.org/

### For SVG

+ SVGO
    + **Name**: `svgo`
    + **Description**: Optmimizes SVG images  
      This processors - if configured - operates on all found svg files.
    * **Website**: https://github.com/svg/svgo
    * **Versions**: 0.3.7

Configurations
--------------

### General Configuration

A smaller configuration is specified by some information.

* First a task which contains the required processors, the input and 
  output files and the processor options if any.
* Second a base path which should be the document root of the application and 
  a set of files which should be specified relative to the document root.

### Maven Plugin

Use the maven plugin to utilize the smaller webservice:

    <plugin>
      <groupId>de.matrixweb.smaller</groupId>
      <artifactId>smaller-maven-plugin</artifactId>
      <version>0.7.0</version>
      <configuration>
        <host>localhost</host>
        <port>1148</port>
        <proxyhost>localhost</proxyhost>
        <proxyport>8080</proxyport>
        <target>target/smaller</target>
        <files>
          <!-- Should point to the document root -->
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
            <!-- The processors to use -->
            <processor>closure,uglifyjs,lessjs:1.3.0,cssembed,yuiCompressor</processor>
            <!-- The input files (at most one for js and one for css) -->
            <in>basic.json,style.less</in>
            <!-- The output files (at most one for js and one for css) -->
            <out>basic-min.js,style.css</out>
            <!-- The processor options specified by [processor-name]:[option-name]=[option-value] -->
            <options>global:source-maps=true;output:out-only=true;cssembed:max-uri-length=0</options>
          </task>
        </tasks>
      </configuration>
    </plugin>

Or use the standalone maven plugin if you do not want to use the webservice:

    <plugin>
      <groupId>de.matrixweb.smaller</groupId>
      <artifactId>smaller-maven-standalone-plugin</artifactId>
      <version>0.7.0</version>
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

Developer information
---------------------
[Additional documentation](https://github.com/KnisterPeter/Smaller/wiki)

Credits
-------

Thanks to [SinnerSchrader](http://www.sinnerschrader.com/) for their support
and the time to work on this project.
