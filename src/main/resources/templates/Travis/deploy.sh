#!/bin/bash

{{#facets.Docker}}
# Push Docker image
mvn deploy -P prod,docker -D skipTests=true -D docker.username=$REGISTRY_USERNAME -D docker.password=$REGISTRY_PASSWORD
{{/facets.Docker}}