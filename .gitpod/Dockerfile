FROM gitpod/workspace-full

USER root

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && \
    sdk install java 21.0.1-tem && \
    sdk default java 21.0.1-tem"

# Overwrite settings.xml
COPY .gitpod/maven/settings.xml /home/gitpod/.m2/
