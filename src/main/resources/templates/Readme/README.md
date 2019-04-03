{{#project}}
{{#integration}}
[![Build Status](https://travis-ci.com/Byzaneo/sms.svg?branch=master)](https://travis-ci.com/Byzaneo/sms)
{{/integration}}
[![Maintainability](https://api.codeclimate.com/v1/badges/6a38f084735af25539cd/maintainability)](https://codeclimate.com/github/Byzaneo/sms/maintainability)
[![codecov](https://codecov.io/gh/Byzaneo/sms/branch/master/graph/badge.svg)](https://codecov.io/gh/Byzaneo/sms)

# SMS service

Text message service provided by [byzaneo.io](https://byzaneo.io).

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Things you need to build and run this service:
- Java 10+
- Maven 3.5+

### Installing

A step by step series of examples that tell you how to get a development env running

Say what the step will be

```
mvn clean install
```

## Running the tests

```
mvn clean test
```

with Jacoco coverage

```
mvn clean test -P coverage
```

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

### Profiles

* dev: development profile. 
* prod: production packaging et runtime.

## Authors

* Byzaneo Team 

## License

* [Apache 2.0](./LICENSE)
{{/project}}