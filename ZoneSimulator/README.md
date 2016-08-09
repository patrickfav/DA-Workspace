# Zone Simulator

This is a task execution simulator (simulating parallel executions of task in an operating system) originally developed for
my first master thesis to process migration to other VMs if the response time is over a certain threshold. The thesis
was canceled due to circumstances out of my influence. It was a really good exercise to learn the whole
all aspects of concurrency in Java (i.e. the whole `java.util.concurrent` package).

## Build

Use maven

    mvn clean package

to build the jar or just run the tests with

    mvn clean test

There is a stub for a UI however it was never finished.

## Tech Stack

* Java 7
* Maven
* Reflections
* JavaFx (unfinished)

# License

Copyright 2012 Patrick Favre-Bulle

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.