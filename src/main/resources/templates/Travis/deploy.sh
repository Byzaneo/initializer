#!/bin/bash

{{#facets.Docker}}
# Push Docker image
docker push "${IMAGE_NAME}:${TAG}"
{{/facets.Docker}}

{{#facets.Spinnaker}}
# Spinnaker WebHooks
curl --header "Content-Type: application/json" \
     --request POST \
     --data '{"branch":"$TRAVIS_BRANCH","build":"$TRAVIS_BUILD_NUMBER","commit":"$TRAVIS_COMMIT","commit_message":"$TRAVIS_COMMIT_MESSAGE","tag":"$TRAVIS_TAG","stage":"$TRAVIS_BUILD_STAGE_NAME"}' \
{{#project.deployment.username}}
     -u ${DEPLOYMENT_USERNAME}:${DEPLOYMENT_PASSWORD}
{{/project.deployment.username}}
     {{project.deployment.url}}/gate/webhooks/webhook/${PROJECT_NAME}
{{/facets.Spinnaker}}