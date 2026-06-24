# Apache Camel Spring Boot Examples - AI Agent Guidelines

Guidelines for AI agents contributing examples to **apache/camel-spring-boot-examples**.

This repository contains runnable Maven examples that demonstrate Apache Camel
on Spring Boot. Each example is an independent Spring Boot application wired as a
Maven module of the aggregator build.

These guidelines complement the canonical, org-wide rules in the main
[apache/camel `AGENTS.md`](https://github.com/apache/camel/blob/main/AGENTS.md).
Read that file for the full *Rules of Engagement*; the section below repeats the
essentials and adds what is specific to this examples repository.

## Project Info

- Build: Maven 3.9+ (use the provided `./mvnw` wrapper)
- Java: 21 (set by `javaVersion` in the root `pom.xml`)
- Parent: `org.apache.camel.springboot.example:examples` (e.g. `4.21.0-SNAPSHOT`
  on `main`); pulls `camel-spring-boot-bom`
- Tests: JUnit Jupiter via `@CamelSpringBootTest` (`camel-test-spring-junit6`)
- JIRA project: `CAMEL` (https://issues.apache.org/jira/projects/CAMEL)

## Rules of Engagement (essentials)

- **Attribution**: every AI-generated PR description, review or JIRA comment MUST
  identify itself as AI-generated and name the human operator, e.g.
  `_Claude Code on behalf of [Human Name]_`.
- **JIRA ownership**: only pick **Unassigned** tickets. Before starting, assign
  the ticket to your operator and transition it to *In Progress*. Set
  `fixVersions` before closing.
- **One example per PR**, kept small and self-contained. Do not exceed 10 PRs per
  day per operator. Quality over quantity.
- **Branch from your own fork** (not apache/), with a descriptive name containing
  the topic and JIRA id (e.g. `CAMEL-12345-rest-openapi-example`). Delete the
  branch after merge/close. Never push to a branch you did not create.
- **Green CI is required**: `./mvnw clean install` and `./mvnw test` must pass.
- **Tests, docs and license headers** are required on every contribution.
- **Never merge** without at least one human approval; never approve your own PR.

## Repository structure

- One example per top-level directory, registered as a `<module>` in the root
  `pom.xml`. A new example is not built until it is listed there.
- Examples are grouped by category via the `<category>` property in each module's
  `pom.xml` (e.g. `Beginner`, `Messaging`, `Cloud`, `REST`, `AI`).

## Anatomy of an example

```
<example>/
├── pom.xml                       # parent = examples; artifactId camel-example-spring-boot-<name>
├── README.adoc                   # AsciiDoc docs incl. "how to run"
└── src/
    ├── main/java/...             # <Name>Application.java (@SpringBootApplication)
    │                             # <Name>Router.java (@Component extends RouteBuilder)
    ├── main/resources/
    │   └── application.properties # Spring + Camel config (ASF header)
    └── test/java/...             # <Name>Test (@CamelSpringBootTest @SpringBootTest)
```

- Use Spring Boot **starters** (`camel-<component>-starter`), never raw
  `camel-core` dependencies; versions come from `camel-spring-boot-bom`.
- Routes are auto-discovered: annotate the `RouteBuilder` with `@Component`.

## Build, run and validate

```bash
# one-time, from the repo root (resolves parent + shared config)
./mvnw clean install -DskipTests

# run a single example
cd <example>
../mvnw spring-boot:run
# or
../mvnw clean package && java -jar target/camel-example-spring-boot-*.jar

# tests
./mvnw test            # all examples
```

CI (`.github/workflows`) runs `./mvnw -V --no-transfer-progress clean install
-DskipTests` then `./mvnw ... test` on Java 21.

## Conventions

- **Maven coordinates**: parent `org.apache.camel.springboot.example:examples`;
  child `artifactId` = `camel-example-spring-boot-<name>` (matches the directory);
  `<name>` = `Camel SB Examples :: <Category> :: <Title>`.
- **Java naming**: `<Name>Application` (`@SpringBootApplication` + `main`),
  `<Name>Router` (extends `RouteBuilder`); tests end in `Test`.
- **License headers**: ASF header required on every `.java`, `.properties`,
  `.xml`, `.adoc`, `.yml`, `.sh`.
- **Config**: use `{{placeholder}}` in routes and
  `camel.component.<component>.<option>` keys in `application.properties`.
- **README**: AsciiDoc, with at least an Introduction and a "How to run"
  section showing `mvn spring-boot:run`.

## Adding a new example (checklist)

1. Create `<example>/` with a `pom.xml` declaring the `examples` parent and the
   `camel-example-spring-boot-<name>` artifactId plus a `<category>`.
2. Add `<Name>Application.java`, `<Name>Router.java`, `application.properties`,
   a `<Name>Test`, and a `README.adoc` — all with ASF headers.
3. Register the module in the root `pom.xml` `<modules>` block.
4. Run `./mvnw -pl <example> -am clean install` and confirm it starts and tests
   pass.
5. Open the PR from your fork, link the JIRA ticket, and request review from
   active committers.

## Links

- Camel Spring Boot docs: https://camel.apache.org/camel-spring-boot/latest/
- Camel Spring Boot starters list: https://camel.apache.org/camel-spring-boot/latest/list.html
- Canonical agent rules: https://github.com/apache/camel/blob/main/AGENTS.md
