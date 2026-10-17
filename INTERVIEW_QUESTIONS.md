# Interview Q&A: OrangeHRM Automation with Selenium and Cucumber

Answers are tied to what is in this project (SauceDemo UI tests, Selenium 4.35, Cucumber 7.14, JUnit 4, Maven, Extent Reports, Log4j2, REST Assured, Datafaker).

---

## 1. Project overview

**Q1. Tell me about your project.**
It is a BDD test automation framework for SauceDemo, a demo shop. Test scenarios are written in Gherkin (`Login`, `Products`, `Checkout` features). They are implemented with Selenium WebDriver in Java using the Page Object Model. Cucumber runs them through JUnit, Maven builds and executes them, and Extent Reports produces HTML and PDF reports. It has 16 scenarios and 63 steps, and runs on Chrome, Edge or Firefox, with headless options.

**Q2. What is the framework architecture?**
- `features/`: Gherkin files (business-readable tests).
- `step_definitions/`: glue code that maps each step to Java.
- `pages/`: Page Objects (`LoginPage`, `ProductsPage`, `CartPage`, `CheckoutPage`) holding locators and actions.
- `utilities/`: `Driver`, `CommonMethods` (waits, clicks, helpers), `ConfigurationReader`, `DataGenerator`, `SftAssert`, `ScreenshotUtil`.
- `hooks/`: setup, teardown, screenshots and logging.
- `runners/`: `TestRunner` and `FailedTestRunner`.
- `Configuration.properties`: URL, browser, users and timeouts.

**Q3. What was your role, and what did you improve?**
I designed and maintained the framework. A key improvement was browser and login reuse: originally every scenario launched a new browser and logged in from the login page. Now one browser is launched per run, and a scenario logs in only if no session already exists. This cut the run time a lot (the full suite runs in about 30 seconds).

**Q4. Why BDD and Cucumber?**
Scenarios are plain English, so testers, developers and business people share one description. Steps are reusable across scenarios, and tags make it easy to select subsets.

---

## 2. Selenium

**Q5. What is Selenium WebDriver?**
An API that controls real browsers through browser-specific drivers (ChromeDriver, EdgeDriver, GeckoDriver) using the W3C WebDriver protocol.

**Q6. How do you handle drivers without downloading them?**
Selenium 4.6+ includes Selenium Manager, which finds or downloads the correct driver automatically. That is why there is no WebDriverManager dependency.

**Q7. What locators did you use and which is best?**
`id`, `css`, `xpath`, and others. Prefer `id`, then CSS, then XPath. In this project: `id="login-button"`, CSS `.inventory_item_name`, and a dynamic XPath for the "Add to cart" button per product name.

**Q8. Give an example of a dynamic XPath from your project.**
```
//div[contains(@class,'inventory_item')][.//div[contains(@class,'inventory_item_name') and normalize-space()='%s']]//button
```
It is formatted with `String.format` using the product name, so one locator handles all products.

**Q9. What are implicit, explicit and fluent waits? Which do you use?**
- Implicit: a global polling timeout for element lookup.
- Explicit (`WebDriverWait` + `ExpectedConditions`): waits for a specific condition.
- Fluent: explicit wait with custom polling and ignored exceptions.

I use explicit waits (`waitForElement`, `waitForClickability`, `waitForUrlContains`, `waitForTitle`) and a page-load timeout, because mixing implicit and explicit waits causes unpredictable delays.

**Q10. How do you wait for page and AJAX loading?**
`waitForPageAndAjaxToLoad()` waits until `document.readyState == 'complete'`, and, if jQuery is present, until `jQuery.active == 0`.

**Q11. What is `PageFactory` and `@FindBy`?**
`@FindBy` declares a locator on a field. `PageFactory.initElements(driver, this)` creates lazy proxies, so the element is looked up when it is used, not when the page object is created.

**Q12. What is a StaleElementReferenceException and how do you avoid it?**
The element was removed or re-rendered after it was found. Avoid it by re-finding the element, using locators with waits instead of stored elements, and waiting for the DOM to settle.

**Q13. How do you handle dropdowns?**
The `Select` class: `selectByVisibleText`. I used it for the product sort dropdown (`selectFromDropDownByVisibleText`).

**Q14. When do you use JavaScriptExecutor?**
When normal Selenium actions fail or are not enough: JS click (`jsClick`), `scrollIntoView`, drawing an outline on elements, reading `localStorage` or `readyState`. In this project I also clear the cart with `localStorage.removeItem('cart-contents')`.

**Q15. How do you handle alerts, frames, multiple windows?**
`driver.switchTo().alert()`, `.frame()`, `.window(handle)`; always switch back with `defaultContent()` or the original handle.

**Q16. What is the Actions class?**
Hover, drag and drop, double click, right click, key chords.

**Q17. How do you take screenshots?**
`TakesScreenshot.getScreenshotAs`. The `@AfterStep` hook attaches the PNG to the Cucumber scenario when a step fails or when `screenshotOnEveryStep=true`.

**Q18. What changed in Selenium 4?**
W3C-compliant protocol, relative locators, Selenium Manager, new window/tab API (`newWindow`), Chrome DevTools Protocol access, improved Grid, and `getRect`.

**Q19. Why `--incognito` and disabling the password manager?**
Incognito gives a clean profile. Chrome's "password found in a data breach" popup can cover the page and break clicks, so password-manager and leak-detection prefs are disabled.

**Q20. Headless mode: what is it and why use it?**
The browser runs without a UI (`--headless=new`), which is faster and works on CI servers. `--no-sandbox` and `--disable-dev-shm-usage` avoid crashes in containers.

---

## 3. Cucumber and BDD

**Q21. What is Gherkin? Name its keywords.**
A plain-text DSL: `Feature`, `Background`, `Scenario`, `Scenario Outline`, `Examples`, `Given/When/Then/And/But`, plus tags, doc strings and data tables.

**Q22. Difference between Scenario and Scenario Outline?**
A Scenario runs once. A Scenario Outline runs once per row of the `Examples` table with `<placeholders>`. I use it for invalid-login cases and the sort options.

**Q23. What is Background?**
Steps that run before every scenario in the feature. Here: `Given User is logged in as "standard"` in Products and Checkout, and `Given User is on the login page` in Login.

**Q24. What are tags and how are they used?**
Labels such as `@smoke`, `@regression`, `@login`, `@e2e`, `@ui`, `@api`, `@ignore`. They filter execution (`-Dcucumber.filter.tags="@smoke"`) and bind hooks (`@Before("not @api")`). `tags = "not @ignore"` in the runner skips disabled tests.

**Q25. How does a step find its code?**
Cucumber matches the step text to a Cucumber Expression or regex in an annotated method within the `glue` packages (`automation.step_definitions`, `automation.hooks`).

**Q26. What are Cucumber Expressions?**
Readable placeholders such as `{string}`, `{int}`, e.g. `@Given("User logs in as {string}")`. Regex is also supported.

**Q27. How do you pass tabular data to a step?**
A `DataTable` parameter: `dataTable.asList(String.class)`. Used for the list of products added to the cart.

**Q28. List the Cucumber hooks you used.**
`@BeforeAll`, `@Before`, `@BeforeStep`, `@AfterStep`, `@After`, `@AfterAll`, with `order` to control execution (for `@After`, higher order runs first).

**Q29. Hook order: `@After(order = 10)` vs `@After(order = 0)`?**
For `@Before`, lower order runs first. For `@After`, higher order runs first. So `after()` (order 10, soft assert `assertAll`) runs before `resetBrowserState()` (order 0).

**Q30. What is `dryRun`?**
It checks that every step has a definition without executing them.

**Q31. How does Cucumber share state between steps?**
Cucumber creates new step-definition objects for every scenario, so fields are not shared across scenarios. State within a scenario is shared through page objects, a context class or dependency injection (PicoContainer). In this project the state lives in the browser itself and in thread-local helpers such as the soft assert.

**Q32. Why is the step `User is logged in as "standard"` separate from `User logs in as "standard"`?**
The first ensures a logged-in state (login only when needed) and verifies the Products page. The second always performs a real login, used when testing the login itself.

**Q33. Imperative vs declarative scenarios?**
Declarative describes intent ("User completes an order"), imperative lists every click. Prefer declarative with details in page objects.

---

## 4. JUnit and runners

**Q34. How does Cucumber run with JUnit?**
`@RunWith(Cucumber.class)` and `@CucumberOptions` on a runner class; Maven Surefire discovers it.

**Q35. Explain `@CucumberOptions` in your runner.**
`features` (path), `glue` (step/hook packages), `plugin` (reports), `tags`, `monochrome` (readable console), `dryRun`.

**Q36. Why is JUnit 4 used, and how does JUnit 5 differ?**
`cucumber-junit` works with JUnit 4. JUnit 5 uses `cucumber-junit-platform-engine`, `@Suite`, and `@Test`/`@BeforeEach` from Jupiter.

**Q37. What is `FailedTestRunner`?**
It runs `@target/rerun.txt`, generated by the `rerun:` plugin, so only failed scenarios are executed again: `mvn test -Dtest=FailedTestRunner`.

**Q38. Why does Surefire only include `TestRunner.java`?**
`<includes>**/TestRunner.java</includes>` stops Surefire from also running `FailedTestRunner` on every `mvn test` (it needs a `rerun.txt` from a previous run).

---

## 5. Maven

**Q39. What is `pom.xml`?**
Project Object Model: coordinates, properties, dependencies, plugins.

**Q40. Why use `<properties>` for versions?**
One place to manage versions; `${selenium.version}`.

**Q41. Dependency scopes?**
`compile` (default), `test` (JUnit, cucumber-junit), `provided`, `runtime`.

**Q42. Which Maven plugins are used?**
`maven-compiler-plugin` (Java 17 via `maven.compiler.release`) and `maven-surefire-plugin` (runs tests, passes `-Dbrowser`).

**Q43. Maven lifecycle?**
`validate → compile → test → package → verify → install → deploy`. `mvn test` runs through the test phase.

**Q44. How do you pass the browser from the command line?**
`mvn test -Dbrowser=edge`. Surefire forwards it via `systemPropertyVariables`, and `ConfigurationReader` gives system properties priority over the file.

**Q45. `mvn clean` vs `mvn test`?**
`clean` deletes `target/`; `test` compiles and runs tests.

---

## 6. Design patterns and OOP

**Q46. What is the Page Object Model and why use it?**
Each page is a class with its locators and actions. Benefits: reuse, one place to fix locator changes, readable steps.

**Q47. Which design patterns are in the project?**
- Page Object Model.
- Singleton-like utility (`Driver` with a private constructor and static access).
- Factory (`createDriver(browser)` switch).
- ThreadLocal holder (driver pool, soft assert, last highlighted element).
- Template/base class (`CommonMethods` extended by pages and step definitions).

**Q48. OOP concepts used?**
Inheritance (pages extend `CommonMethods`), encapsulation (private driver pool, private helper methods), abstraction (steps call page methods), polymorphism (generic `configureChromium` for `ChromeOptions` and `EdgeOptions` via `<T extends ChromiumOptions<?>>`).

**Q49. Why make step definitions call page objects instead of Selenium directly?**
Separation of concerns: steps describe behavior; pages know the UI.

**Q50. What generics example is in the Driver?**
`private static <T extends ChromiumOptions<?>> T configureChromium(T options, ...)` shares one option setup between Chrome and Edge.

---

## 7. Driver management and thread safety

**Q51. Why is the driver in a `ThreadLocal`?**
Each thread gets its own `WebDriver`, so tests can run in parallel without sharing a browser.

**Q52. How does `getDriver()` work?**
If the thread has no driver, it reads the `browser` property, creates the browser and stores it; otherwise it returns the existing one (lazy initialization).

**Q53. Why call `driverPool.remove()` after `quit()`?**
Prevents memory leaks and stale drivers in thread pools.

**Q54. How do you stop the browser from relaunching for every scenario?**
`Driver.browserSetup()` checks `isBrowserAlive()` (calls `getWindowHandle()`); if alive it reuses the browser. The browser closes only in `@AfterAll`. If it died, a new one is launched.

**Q55. Trade-off of sharing one browser across scenarios?**
Faster, but state can leak between scenarios (cookies, cart, local storage). I mitigate with `@After` clearing the cart and by ending sessions when a scenario needs the login page. For strict isolation or parallel runs, use one browser per scenario/thread.

**Q56. How is parallel execution possible?**
ThreadLocal driver + soft assert per thread. You would add Cucumber's parallel execution setting (`cucumber.execution.parallel.enabled`) with the JUnit platform engine, or Surefire `parallel` configuration. Note: the JUnit 4 runner has limited scenario-level parallelism.

---

## 8. Login and session handling

**Q57. How did you avoid logging in for every test?**
`LoginPage.ensureLoggedIn(role)`:
1. If the `session-username` cookie exists and matches the requested user, skip login and just open `inventory.html`.
2. If another user is logged in, delete cookies, refresh, then log in as the requested user.
3. If nobody is logged in, open the app and call the normal `login()`.

**Q58. How do you detect "logged in"?**
`driver.manage().getCookieNamed("session-username") != null`. SauceDemo sets that cookie at login.

**Q59. Alternative ways to reuse a session?**
Save and restore cookies; log in through the API and inject the token; browser profile reuse; Playwright's `storageState`.

**Q60. What would you do if the app used a JWT in local storage instead?**
Read it with `JavascriptExecutor` (`localStorage.getItem`), or better, log in via API once and inject the token into storage.

**Q61. Why does the login feature still start on the login page?**
It tests the login itself. `ensureOnLoginPage()` clears any session and reloads the URL so each login scenario starts clean.

---

## 9. Configuration and data

**Q62. How is configuration managed?**
`Configuration.properties` read by `ConfigurationReader` (`getProperty`, `getInt`, `getBoolean`). A system property overrides the file, which suits CI.

**Q63. How do you generate test data?**
Datafaker (`DataGenerator.firstName()`, `lastName()`, `postalCode()`) for random checkout details.

**Q64. Why is credential data in the properties file acceptable here?**
These are public demo credentials. For real systems, use environment variables or a secrets manager, never commit them.

**Q65. How do you test negative scenarios?**
A Scenario Outline with a data table: locked-out user, wrong password, missing username/password, and checkout with missing fields, each asserting the exact error message.

---

## 10. Assertions

**Q66. Hard vs soft assertion?**
A hard assert stops at the first failure. A soft assert records failures and continues; `assertAll()` reports them all at the end.

**Q67. How is soft assert implemented here?**
`SftAssert` stored in a `ThreadLocal`; `resetSoftAssert()` in `@Before` gives each scenario a new one; `assertAll()` runs in the `@After(order = 10)` hook inside a `try/finally` so the status is always logged.

**Q68. What do you validate in checkout?**
Item total equals the sum of product prices, total equals item total plus tax (using `BigDecimal` via `parseMoney`), and the confirmation message "Thank you for your order!".

**Q69. Why `BigDecimal` for money?**
`double` has rounding errors (0.1 + 0.2); `BigDecimal` is exact.

**Q70. How do you verify sorting?**
Read displayed values, copy and sort the list with the expected `Comparator`, then compare actual vs expected lists.

---

## 11. Reporting and logging

**Q71. What reports does the project produce?**
Extent Spark HTML (`htmlReports/Automation_Report.html`), PDF (`ExtentPdf.pdf`), plus Cucumber HTML, JSON and JUnit XML in `target/cucumber-reports/`.

**Q72. How is Extent Reports connected to Cucumber?**
The adapter plugin `com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:` in the runner and `extent.properties` for output paths, screenshot folder and system info.

**Q73. Why a timestamp in the report folder?**
`basefolder.datetimepattern` keeps each run's report separate.

**Q74. Which logger and why?**
Log4j2 (`log4j2.properties`) writes to console and `logs/logs.log`. `LogColor` adds colors for readability.

**Q75. What does `StepNameListener` do?**
A custom Cucumber plugin (event listener) that logs each step's name as it executes.

**Q76. Why attach screenshots to the report?**
Faster failure analysis. Failed steps always attach one; `screenshotOnEveryStep` gives full visual evidence.

---

## 12. API testing

**Q77. How is API testing included?**
REST Assured and Jackson are in the pom, with `api.baseUri/username/password` for Restful-Booker. Scenarios tagged `@api` skip browser launch (`@Before("not @api")`).

**Q78. REST Assured structure?**
`given()` (request spec), `when()` (call), `then()` (assertions): `given().baseUri(...).body(...).when().post("/booking").then().statusCode(200)`.

**Q79. Common API checks?**
Status code, response body fields, headers, response time, schema, and negative cases (401, 404).

---

## 13. Git and CI

**Q80. How do you version this project?**
Git on branch `main`. `.gitignore` excludes `target/`, `Results/`, `logs/` and `test-output/` (generated files).

**Q81. How would you run it in CI (Jenkins/GitHub Actions)?**
Check out, set up JDK 17, run `mvn test -Dbrowser=chrome-headless -Dcucumber.filter.tags="@smoke"`, then publish the Extent/Cucumber reports as artifacts.

**Q82. Why headless in CI?**
Build servers have no display.

---

## 14. Scenario-based and tricky questions

**Q83. A test passes locally but fails in CI. What do you check?**
Headless differences (window size, `--window-size=1920,1080`), timing and waits, environment data, browser version, screen resolution, network speed, and screenshots/logs from CI.

**Q84. A test is flaky. How do you fix it?**
Find the root cause: replace sleeps with explicit waits, fix stale elements, remove shared state between tests, stabilize locators, avoid dependence on test order. Re-run only as a stopgap (`FailedTestRunner`).

**Q85. Element click is intercepted. What do you do?**
Wait for overlays to disappear, scroll into view, wait for clickability, close pop-ups, and as a last resort use a JS click.

**Q86. How do you handle a locator that changes often?**
Ask developers for stable `data-test` attributes (SauceDemo has `data-test`), keep locators in one page object, avoid brittle absolute XPath.

**Q87. How would you run tests on multiple browsers?**
Change `-Dbrowser`, or use a Maven profile or CI matrix. For remote/cross-browser, use Selenium Grid or a cloud provider with `RemoteWebDriver`.

**Q88. A scenario depends on another scenario's data. Is that okay?**
No. Scenarios must be independent so they can run in any order or in parallel. That is why each scenario ensures its own login and cart reset.

**Q89. How do you decide what to automate?**
Repetitive, stable, high-value, regression and smoke flows, data-driven cases. Not one-off, highly volatile or purely visual checks.

**Q90. What would you improve next?**
Parallel execution, dependency injection for shared context (PicoContainer), Selenium Grid/Docker, CI pipeline with reports, API login for session setup, retry for flaky tests, JUnit 5 migration, and a dedicated `TestContext`.

**Q91. How do you prevent state leaking when reusing the browser?**
Clear the cart after every scenario, end the session when a test needs a logged-out state, and relaunch the browser if it stops responding. Keep tests data-independent.

**Q92. What is the difference between `driver.close()` and `driver.quit()`?**
`close()` closes the current window; `quit()` closes all windows and ends the session. The framework uses `quit()` in `closeDriver()`.

**Q93. What is `driver.get()` vs `navigate().to()`?**
Both load a URL; `get` waits for page load. `navigate()` also provides `back`, `forward`, `refresh`.

**Q94. How do you handle downloads?**
Browser prefs set `download.default_directory` (the `downloads` folder), disable prompts, and the test checks for the file.

**Q95. Why `highlightElements`?**
It outlines elements in red (`clickAndDraw`) so recorded runs or screenshots show what was clicked. It is useful in demos, and can be turned off for speed.

---

## 15. Rapid-fire definitions

| Term | Short answer |
| --- | --- |
| BDD | Behavior-driven development: tests described as behavior in plain language |
| Gherkin | Syntax used to write Cucumber scenarios |
| Glue | Packages where Cucumber looks for steps and hooks |
| POM | Page Object Model; also Maven's Project Object Model (context matters) |
| Smoke test | Quick check of critical flows |
| Regression test | Re-checking existing features after changes |
| E2E | End-to-end flow through the whole application |
| ThreadLocal | A variable with a separate value per thread |
| Selenium Manager | Built-in driver binary resolver in Selenium 4.6+ |
| Surefire | Maven plugin that runs unit/integration tests |
| Extent Reports | Reporting library producing rich HTML/PDF reports |
| Log4j2 | Logging framework |
| REST Assured | Java DSL for REST API testing |
| Datafaker | Library to generate fake test data |
| Headless | Running a browser without a UI |
