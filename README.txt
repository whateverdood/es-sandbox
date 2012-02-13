_______________________________
Before you build

1. Install Maven 3
2. Install ${es-sandbox}/lib/shawty-0.9.3.jar into your local maven repo:

$ mvn install:install-file -Dfile=lib/shawty-0.9.3.jar -DgroupId=com.googlecode \
    -DartifactId=shawty -Dversion=0.9.3 -Dpackaging=jar

You should be ready to build:

$ mvn clean package