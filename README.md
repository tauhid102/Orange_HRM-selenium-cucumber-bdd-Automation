# OrangeHRM Automation with Selenium and Cucumber

UI and API test automation framework built with Selenium 4, Cucumber 7, JUnit 4, REST Assured and Extent Reports.
The UI tests run against the [OrangeHRM demo](https://opensource-demo.orangehrmlive.com/), a public HR application.

> **Status:** the Login feature (`@login`) targets OrangeHRM and passes. The Products and Checkout features and their
> page objects are left over from the earlier SauceDemo version and fail against OrangeHRM until they are rewritten
> (run them with `-Dcucumber.filter.tags="not @products and not @checkout"` to skip them).

## Prerequisites

| Tool | Version | Check with |
| --- | --- | --- |
| JDK | 17 or newer | `java -version` |
| Maven | 3.8 or newer | `mvn -version` |
| Browser | Chrome, Edge or Firefox (latest) | |
| Git | any | `git --version` |

No browser driver needs to be downloaded; Selenium Manager resolves it automatically. An internet connection is required
(dependencies, the application under test and the demo API).

## Setup

1. Clone the repository and open a terminal in the project folder:
   ```
   git clone <repository-url>
   cd selenium-cucumber-bdd-framework
   ```
2. Download dependencies and compile:
   ```
   mvn clean test-compile
   ```
3. Optional, run in Eclipse: **File > Import > Maven > Existing Maven Projects**, select the project folder, then
   **Maven > Update Project** (Alt+F5). Install the "Cucumber Eclipse" plugin if you want to run feature files directly.

## Configuration

Settings live in `Configuration.properties` in the project root:

| Key | Purpose |
| --- | --- |
| `url` | Application under test |
| `browser` | `chrome`, `chrome-headless`, `edge`, `edge-headless`, `firefox`, `firefox-headless` |
| `pageLoadTimeout` | Page load timeout in seconds |
| `highlightElements` | Outline elements in red while the test interacts with them |
| `screenshotOnEveryStep` | `true` attaches a screenshot to every step, `false` only on failure |
| `standard`, `password` | OrangeHRM demo user name and password (`Admin` / `admin123`, published on the login page) |
| `api.*` | Base URI and credentials for the API tests |

A `-Dbrowser=...` system property on the command line always overrides the file.

## Running the tests

```
mvn test                                      # all scenarios, browser from Configuration.properties
mvn test -Dbrowser=chrome                     # visible Chrome
mvn test -Dbrowser=chrome-headless            # no browser window
mvn test -Dbrowser=edge
mvn test -Dcucumber.filter.tags="@smoke"      # only scenarios with a tag
mvn test -Dcucumber.filter.tags="@login and not @regression"
mvn test -Dtest=FailedTestRunner              # re-run only the scenarios that failed last time (target/rerun.txt)
```

From an IDE, run `src/test/java/automation/runners/TestRunner.java` as a JUnit test.

Available tags include `@smoke`, `@regression`, `@e2e`, `@login`, `@products`, `@checkout` (the last two are
legacy SauceDemo), `@ui` and `@ignore` (skipped). Scenarios tagged `@api` never open a browser.

## How the browser and login are handled

- One browser is launched for the whole run and reused by every UI scenario. It is closed once at the end
  (`Hooks.afterAll`). If the browser stops responding, the next scenario launches a new one.
- Scenarios do not start from the login page. `User is logged in as "standard"` calls
  `LoginPage.ensureLoggedIn(role)`:
  - already logged in: login is skipped and the dashboard is opened;
  - not logged in: the normal `login()` is used.
- `User is on the login page` ends any open session and reloads the login form, so login scenarios always start clean.
- `User logs out` opens the user dropdown in the top bar and clicks **Logout** (`LoginPage.logoutFromApp()`).
- Logged in state is detected through the OrangeHRM `orangehrm` session cookie (`LoginPage.SESSION_COOKIE`). The cookie
  holds a session id, not the user name, so the logged in user cannot be compared. Change the cookie name if you point
  the framework at a different application.
- The demo site is slow and renders its pages after load. The first page load of a run can occasionally time out;
  re-run, or raise `pageLoadTimeout`.

## Project structure

```
Configuration.properties           run settings and test users
pom.xml                            dependencies and Surefire config
src/test/java/automation
  hooks/                           Cucumber hooks (browser lifecycle, screenshots, logging)
  pages/                           page objects (Login; Products, Cart, Checkout are legacy SauceDemo)
  runners/                         TestRunner and FailedTestRunner
  step_definitions/                step definitions
  utilities/                       Driver, CommonMethods, config reader, data generator, soft assert, screenshots
src/test/resources
  features/                        Gherkin feature files
  extent.properties, *.xml, *.yaml report and log config
```

## Reports and logs

Each run writes to a timestamped folder (all of these are git ignored):

- `Results/test-reports <date time>/htmlReports/Automation_Report.html` - Extent Spark report
- `Results/test-reports <date time>/PdfReport/ExtentPdf.pdf` - PDF report
- `target/cucumber-reports/` - Cucumber HTML, JSON and JUnit XML
- `logs/logs.log` - execution log

## Adding a test

1. Write the scenario in a file under `src/test/resources/features/`.
2. Add any new elements and actions to a page class in `pages/`.
3. Add the matching steps in `step_definitions/`. Reuse `User is logged in as "standard"` in a `Background` so the
   scenario does not log in again.

## Troubleshooting

- **`release version 17 not supported`**: Maven is using an older JDK. Set `JAVA_HOME` to JDK 17+.
- **Browser does not start**: update the browser, or try another with `-Dbrowser=edge`.
- **Dependency download fails**: check the network or proxy settings in `~/.m2/settings.xml`.
