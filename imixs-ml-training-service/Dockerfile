FROM open-liberty:kernel

# Imixs-Example Version 1.0.0
MAINTAINER ralph.soika@imixs.com


# Add config
COPY --chown=1001:0 ./src/docker/configuration/server.xml /config/

# Activate Debug Mode...
#COPY --chown=1001:0 ./src/docker/configuration/openliberty/jvm.options /config/

# Copy sample application
COPY ./target/*.war /config/dropins/

