# {{#uppercase}}{{project.name}}{{/uppercase}}
{{#facets.Travis}}
[![Build Status]({{project.integration.home}}/{{project.repository.slug}}.svg?branch=master)]({{project.integration.home}}/{{project.repository.slug}})
{{/facets.Travis}}
{{#facets.CodeClimate}}
[![Maintainability](https://codeclimate.com/github/{{project.repository.slug}}/badges/gpa.svg)](https://codeclimate.com/github/{{project.repository.slug}})
{{/facets.CodeClimate}}
{{#facets.CodeCov}}
[![codecov](https://codecov.io/gh/{{project.repository.slug}}/branch/master/graph/badge.svg)](https://codecov.io/gh/{{project.repository.slug}})
{{/facets.CodeCov}}

{{#project.description}}
{{project.description}}

{{/project.description}} 
## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 
See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Things you need to build and run this service:
{{#facets.Java}}
- Java {{project.language.version}}+
{{/facets.Java}}
{{#facets.Maven}}
- Maven 3.6+
{{/facets.Maven}}

### Installing

A step by step series of examples that tell you how to get a development env running

Say what the step will be

{{#facets.Maven}}
```
mvn clean install
```
{{/facets.Maven}}

## Running the tests

{{#facets.Maven}}
```
mvn clean test
```
{{/facets.Maven}}

with Jacoco coverage

{{#facets.Maven}}
```
mvn clean test -P coverage
```
{{/facets.Maven}}

## Built With

{{#facets.Maven}}
* [Maven](https://maven.apache.org/) - Dependency Management
{{/facets.Maven}}

### Profiles

* dev: development profile. 
* prod: production packaging et runtime.

## Authors

* {{project.ownerName}} 

## License

* [Apache 2.0](./LICENSE)