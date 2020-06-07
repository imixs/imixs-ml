#FROM payara/server-full:5.201
FROM payara/micro:5.201

MAINTAINER ralph.soika@imixs.com

ENV PAYARA_ARGS --debug


# Deploy artefacts
COPY ./target/*.war $DEPLOY_DIR

