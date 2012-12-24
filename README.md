Proxy plugin
============

The Proxy plugin can be used in Crawljax as follows:

    <dependency>
      <groupId>com.crawljax.plugins</groupId>
      <artifactId>proxy</artifactId>
      <version>1.0</version>
    </dependency>

Make sure to add the Crawljax Maven repository to your pom.xml:

    <repositories>
      <repository>
      <id>crawljax.mvn.repo</id>
      <url>https://github.com/crawljax/mvn-repo/raw/master</url>
      <snapshots>
       <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
      </snapshots>
      </repository>
    </repositories>
