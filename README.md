# specdocs

[![Jitpack](https://jitpack.io/v/toss/specdocs.svg)](https://jitpack.io/#toss/specdocs)
[![Build Status](https://travis-ci.org/toss/specdocs.svg?branch=master)](https://travis-ci.org/toss/specdocs)
[![codecov](https://codecov.io/gh/toss/specdocs/branch/master/graph/badge.svg)](https://codecov.io/gh/toss/specdocs)

Document specification from unittests.

## Usage

### Getting started

1. [Add the dependency](https://jitpack.io/#toss/specdocs).

2. Implement AbstractSpecFromTestReporter.

    ```kotlin
    package im.toss.test.specdocs.examples

    import im.toss.test.specdocs.AbstractSpecFromTestReporter
    import im.toss.test.specdocs.SpecFilter

    class SpecFromTestReporter: AbstractSpecFromTestReporter() {
    }
    ```

3. Register it as a JUnit5 TestExecutionListener.

    ```bash
    mkdir -p src/test/resources/META-INF/services
    echo 'im.toss.test.specdocs.examples.SpecFromTestReporter' > src/test/resources/META-INF/services/org.junit.platform.launcher.TestExecutionListener

    ```

See [this](https://junit.org/junit5/docs/current/user-guide/#launcher-api-listeners-custom) for more details about registration of TestExecutionListener.

4. Run your unittests

Then you can see the generated specification document at `build/reports/specs/Specification.md`.

### Examples

Run [the examples](./src/test/kotlin/im/toss/specdocs/examples) in see the generated document at `build/reports/specs/Specification.md`.

## Maintainers

* [Yi EungJun](https://github.com/eungjun-yi)

## License

    Copyright 2019 Viva Republica, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

