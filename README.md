# cdo-clientapi-common

>     modules
        primary modules without peer dependency  
          capi-core   
          capi-mapper  
          capi-redis   
        depned on capi-core   
          capi-web-oauth2   
          capi-webflux-oauth2   
          capi-jaxws   
          capi-web-security   
          capi-webflux-security   
          capi-webclient 
          capi-amdocs  
          capi-srpds  
        depned on capi-webclient  
          capi-kafka   

## Add env definition in workflow deploy yaml file, and add mvn build settings.xml
>     env:
       MAVEN_USER: ${{ secrets.MAVEN_USER }}
       MAVEN_TOKEN: ${{ secrets.MAVEN_TOKEN }}
       ...
       - name: Maven Buiild jar file
         run: mvn -B -e -Dmaven.test.skip=true clean package -s ${{ github.workspace }}/.maven/settings.xml

Where MAVEN_USER and MAVEN_TOKEN are project secrets (repository) for your github username and personal access token.

## Add maven settings file for github repository
.maven/settings.xml
>       <?xml version="1.0" encoding="UTF-8"?>
        <settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">
            <servers>
                <server>
                    <id>github</id>
                    <username>${env.MAVEN_USER}</username>
                    <password>${env.MAVEN_TOKEN}</password>
                </server>
                <server>
                    <id>cdo-clientapi-common</id>
                    <username>${env.MAVEN_USER}</username>
                    <password>${env.MAVEN_TOKEN}</password>
                </server>
            </servers>
            <profiles>
                <profile>
                    <id>github</id>
                    <repositories>
                        <repository>
                            <id>cdo-clientapi-common</id>
                            <url>https://maven.pkg.github.com/telus/cdo-clientapi-common</url>
                            <snapshots>
                                <enabled>true</enabled>
                            </snapshots>
                        </repository>
                    </repositories>
                </profile>
            </profiles>
            <activeProfiles>
                <activeProfile>github</activeProfile>
            </activeProfiles>
        </settings>
