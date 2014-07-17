Smaller
=======

Summary
-------
Smaller is a small webservice for minification and transcoding sources.
It currently supports coffeescript, closure, uglifyjs, lessjs, typescript, 
jshint, eslint, browserify, sweetjs, yuicompressor, ycssmin, csso, 
embeddcss and svgo.


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
    * **Versions**: 1.3.0, 1.3.3, 1.4.0, 1.4.1, 1.4.2, 1.5.0, 1.6.1
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
    * **Versions**: 1.3.3, 1.4.0, 1.5.0, 1.6.3, 1.7.1
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
    + **Configuration**:
        + Just configure the options from the jshint docs
    * **Website**: http://github.com/jshint/jshint
    * **Versions**: 1.1.0, 2.4.3
+ eslint
    + **Name**: `esint`
    + **Description:** 
        Tests JavaScript files with eslint and returns it's output.
    + **Configuration**:
    * **Website**: http://eslint.org/
    * **Versions**: 0.7.4
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

* A client configuration for the choosen build-tool (Maven, Ant, Grunt, ...)
* A config file specified by 
  [Smaller config](https://github.com/KnisterPeter/smaller-config)

### Maven Plugin

Use the maven plugin to utilize the smaller webservice:

    <plugin>
      <groupId>de.matrixweb.smaller</groupId>
      <artifactId>smaller-maven-plugin</artifactId>
      <version>0.8.0-SNAPSHOT</version>
      <configuration>
        <host>localhost</host>
        <port>1148</port>
        <proxyhost>localhost</proxyhost>
        <proxyport>8080</proxyport>
        <target>target/smaller</target>
        <config-file>${basedir}/smaller.yml</config-file>
      </configuration>
    </plugin>

Or use the standalone maven plugin if you do not want to use the webservice:

    <plugin>
      <groupId>de.matrixweb.smaller</groupId>
      <artifactId>smaller-maven-standalone-plugin</artifactId>
      <version>0.8.0-SNAPSHOT</version>
      <configuration>
        <target>target/smaller</target>
        <config-file>${basedir}/smaller.yml</config-file>
      </configuration>
    </plugin>

### Ant Task

    <taskdef name="smaller" classname="de.matrixweb.smaller.clients.ant.SmallerTask" classpath="../../../../target/classes" />
    
    <target name="smaller">
      <smaller 
          host="localhost"
          port="1148"
          proxyhost="localhost"
          proxyport="8080"
          target="target/smaller"
          configFile="${basedir}/smaller.yml"
          />
    </target>

Or use the standalone ant task if you do not want to use the webservice, just 
replace the ant-client jar file with the standalone version.

### Webservice Standalone

Just start the standalone build which has a main method included.
Parameters are port and ip-address to bind to in this order.

### OSGi bundles

Start the osgi-server module with one of the supplied scripts start.sh or start-local.sh.
This either downloads dependencies from maven central or from a local maven repository.

### OSGi client

In this mode smaller scans installed OSGi bundles for matching resources and
builds on that. This is useful if Smaller should be embedded in an OSGi 
application during runtime.

The configuration is fetched from a bundle with the header 'Smaller-Config'.
There could be more than one of this bundles. For each environment in the
config files found, one servlet with the target process-address is registered.

For simple [Apache Karaf](http://karaf.apache.org/) deployment execute the following in the shell:

    features:addurl mvn:de.matrixweb.smaller/smaller-osgi/0.8.0/xml/features
    features:install smaller-browserify
    features:install smaller-lessjs
    ...

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

The config file format
----------------------

    # Block for build-server options
    build-server:
        output-only: true
        # Defines the environemnts to execute during build server run 
        environments:
            - js-prod
            - css
        
    # Block for development-server options
    dev-server:
        # ip to bind the server to 
        ip: 0.0.0.0
        # port to listen on
        port: 12345
        # the proxy to connect to
        proxyhost: localhost
        proxyport: 3000
        # 
        debug: false
        # True to enable live-reload injection into the page
        live-reload: true
        # Defines the environemnts to execute during dev server run 
        environments:
            - js
            - css
            - templates
        
    # The environments specify processing instructions
    # It is best to have one environment for each process-url you want
    # to be processed by smaller
    environments:
        js:
            # Defines the url to intercept/process
            process: "/app.js"
                
            # In case of js testing specify the used framework here
            test-framework: jasmine
            
            # The files block defines the document-roots and included/excluded
            # files which should be included for processing
            files:
                # Multiple document-roots could be specifed. If there are
                # naming conflicts the first file always wins.
                folder:
                    - "doc-root1"
                    - "doc-root2"
                includes:
                    - "**/*.coffee"
                    - "**/*.js"
                excludes:
                    - "**/*.jpg"
            
            # The same as the files block but for test-code to separate test
            # and production code
            test-files:
                folder:
                    - "./tests-root1"
                    - "./tests-root2"
                includes:
                excludes:
                
            # Defines the used processors with all its options.
            # Usually only the first processor needs a 'src' definition since
            # the output of the first is the input of the second (and so on).
            # The last one gives its output to the url specifed in the 'process'
            # block above.
            # The order in this block does not matter, it is specifed in the
            # 'pipeline' list below.
            processors:
                coffeeScript:
                    src: "/main.coffee"
                    options:
                        source-maps: true
                        bare: true
                browserify:
                    options:
                        aliases:
                            "./some-file": library
                            
            # The order in which to execute the processors
            pipeline:
                - coffeeScript
                - browserify
                
        js-prod:
            # This specifies the base configuration this one is derived from
            inherits: js
            # Folders are overwritten in an inherited configuration
            folder:
                - "doc-root1"
            processors:
                # Processors are added to the inherited configuration
                uglifyjs:
            # The pipeline is as well overwritten to define the order of execution
            # in the current environment
            pipeline:
                - coffeeScript
                - browserify
                - uglifyjs
        
        css:
            process: "/style.css"
            files:
                folder:
                    - "dir1"
                includes:
                    - "**/*.less"
                excludes:
                    - "**/*.bin"
            processors:
                lessjs:
                    src: "/main.less"
            pipeline:
                - lessjs
                
        templates:
            templates: handlebars
            files:
                folder:
                    - "dir1"
                    - "dir2"
                includes:
                    - "**/*.hbs"

Developer information
---------------------
[Additional documentation](https://github.com/KnisterPeter/Smaller/wiki)

Credits
-------

Thanks to [SinnerSchrader](http://www.sinnerschrader.com/) for their support
and the time to work on this project.
