# Testing Guide

This document explains the testing strategy and how to run tests for the Trainer App.

## Test Structure

The app uses a three-layer testing approach:

```
app/src/
├── test/                    # Unit Tests (JVM)
│   └── java/com/innotime/trainerapp/
│       └── presentation/
│           ├── util/
│           │   └── FormatUtilTest.kt
│           └── screen/training/
│               └── TrainingViewModelTest.kt
│
└── androidTest/             # Integration & UI Tests (Android)
    └── java/com/innotime/trainerapp/
        └── data/repository/
            └── AthleteRepositoryImplTest.kt
```

## Test Categories

### 1. Unit Tests (`src/test/`)

**What**: Test individual components in isolation (no Android dependencies)

**Coverage**:
- ✅ `FormatUtilTest` - Duration and date formatting
- ✅ `TrainingViewModelTest` - Business logic and state management

**Run**:
```bash
# All unit tests
./gradlew test

# Specific test class
./gradlew test --tests "FormatUtilTest"

# With coverage report
./gradlew testDebugUnitTestCoverage
```

**Tools**:
- **JUnit 4**: Test framework
- **Truth**: Fluent assertions (`assertThat()`)
- **MockK**: Mocking framework
- **Turbine**: Testing Kotlin Flows
- **Coroutines Test**: Testing coroutines

### 2. Integration Tests (`src/androidTest/`)

**What**: Test component interactions with real Android dependencies

**Coverage**:
- ✅ `AthleteRepositoryImplTest` - Repository with Room database

**Run**:
```bash
# All integration tests (requires emulator/device)
./gradlew connectedAndroidTest

# Specific test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.innotime.trainerapp.data.repository.AthleteRepositoryImplTest
```

**Tools**:
- **AndroidX Test**: Android testing framework
- **Room Testing**: In-memory database
- **Truth**: Assertions
- **Turbine**: Flow testing

### 3. UI Tests (To Be Implemented)

**What**: Test user interactions and screen behavior

**Planned Coverage**:
- Critical user flows (start session, add athlete, time run)
- Navigation between screens
- Timer display updates
- Data persistence

**Tools**:
- **Compose UI Test**: Testing Compose screens
- **Espresso**: UI interactions (if needed)

## Current Test Coverage

### Implemented Tests

| Component | Coverage | Tests | Status |
|-----------|----------|-------|--------|
| FormatUtil | 100% | 9 tests | ✅ Complete |
| TrainingViewModel | ~60% | 15 tests | ✅ Core logic |
| AthleteRepository | 100% | 11 tests | ✅ Complete |
| Other ViewModels | 0% | - | ⏳ Pending |
| Other Repositories | 0% | - | ⏳ Pending |
| UI Components | 0% | - | ⏳ Pending |
| **Overall** | **~15%** | **35 tests** | 🟡 Foundation |

### Target Coverage

- **Overall**: 70%+
- **Business Logic** (ViewModels, UseCases): 90%+
- **Data Layer** (Repositories, DAOs): 80%+
- **UI Layer** (Screens, Components): 50%+
- **Utilities**: 100%

## Writing Tests

### Unit Test Example

```kotlin
@Test
fun `descriptive test name in backticks`() = runTest {
    // Arrange
    val input = "test data"

    // Act
    val result = functionUnderTest(input)

    // Assert
    assertThat(result).isEqualTo("expected")
}
```

### Testing ViewModels

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {
    private lateinit var viewModel: MyViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MyViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test state flow`() = runTest {
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(expectedValue)
        }
    }
}
```

### Testing Repositories

```kotlin
class MyRepositoryTest {
    private lateinit var database: TrainerDatabase
    private lateinit var repository: MyRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TrainerDatabase::class.java
        ).build()

        repository = MyRepositoryImpl(database.myDao())
    }

    @After
    fun tearDown() {
        database.close()
    }
}
```

## Running Tests

### From Command Line

```bash
# Quick unit tests only
./gradlew test

# All tests (requires emulator)
./gradlew test connectedAndroidTest

# With coverage report
./gradlew testDebugUnitTestCoverage
# Report: app/build/reports/coverage/test/debug/index.html

# Specific package
./gradlew test --tests "com.innotime.trainerapp.presentation.*"

# Watch mode (continuous testing)
./gradlew test --continuous
```

### From Android Studio

1. **Single Test**: Right-click test method → Run
2. **Test Class**: Right-click class → Run
3. **All Tests**: Right-click `test` or `androidTest` folder → Run
4. **With Coverage**: Right-click → Run with Coverage

### CI/CD

```yaml
# Example GitHub Actions workflow
- name: Run Unit Tests
  run: ./gradlew test

- name: Run Integration Tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 29
    script: ./gradlew connectedAndroidTest

- name: Upload Coverage
  run: ./gradlew testDebugUnitTestCoverage
```

## Test Best Practices

### ✅ Do's

- Use descriptive test names (prefer backticks for readability)
- Follow AAA pattern: Arrange, Act, Assert
- Test one thing per test
- Use `runTest` for coroutine tests
- Use `test { }` from Turbine for Flow testing
- Mock external dependencies
- Test edge cases and error conditions
- Keep tests fast and independent

### ❌ Don'ts

- Don't test Android framework code
- Don't test third-party libraries
- Don't use real network calls
- Don't rely on test execution order
- Don't share mutable state between tests
- Don't test implementation details

## Common Testing Patterns

### Testing StateFlow

```kotlin
viewModel.stateFlow.test {
    // Wait for initial value
    val initial = awaitItem()

    // Trigger action
    viewModel.doSomething()

    // Assert new value
    val updated = awaitItem()
    assertThat(updated).isEqualTo(expected)
}
```

### Testing Suspend Functions

```kotlin
@Test
fun `test suspend function`() = runTest {
    val result = repository.suspendFunction()
    assertThat(result).isNotNull()
}
```

### Testing Error Handling

```kotlin
@Test
fun `test error case`() = runTest {
    coEvery { repository.getData() } throws Exception("Error")

    assertThrows<Exception> {
        viewModel.loadData()
    }
}
```

### Mocking with MockK

```kotlin
val mockRepo = mockk<MyRepository>()

// Simple return value
coEvery { mockRepo.getData() } returns data

// Verify call
coVerify { mockRepo.getData() }

// Verify with matcher
coVerify {
    mockRepo.saveData(
        match { it.id == "123" }
    )
}
```

## Debugging Tests

### View Test Output

```bash
# Verbose output
./gradlew test --info

# Stack traces
./gradlew test --stacktrace

# Debug specific test
./gradlew test --tests "TestClass.testMethod" --debug
```

### Common Issues

**"No tests found"**
- Check test is in correct directory (`src/test/` vs `src/androidTest/`)
- Verify `@Test` annotation
- Check package name matches source

**"Unresolved reference"**
- Sync Gradle files
- Invalidate Caches (File → Invalidate Caches)
- Check test dependencies in `build.gradle.kts`

**"Database already closed"**
- Ensure `@After` properly closes database
- Don't share database instances between tests

**Flow never emits**
- Use `advanceUntilIdle()` with `StandardTestDispatcher`
- Check coroutine scope is not cancelled

## Test Reports

After running tests, find reports here:

```
app/build/reports/
├── tests/
│   └── testDebugUnitTest/
│       └── index.html          # Unit test results
├── androidTests/
│   └── connected/
│       └── index.html          # Integration test results
└── coverage/
    └── test/debug/
        └── index.html          # Coverage report
```

## Continuous Testing

Enable continuous testing during development:

```bash
# Terminal 1: Run tests on file changes
./gradlew test --continuous

# Terminal 2: Make changes to code
```

## Next Steps

To improve test coverage:

1. **Add ViewModel Tests**:
   - Athletes screen logic
   - Groups screen logic
   - Results screen logic

2. **Add Repository Tests**:
   - RunRepository
   - TrainingRepository
   - GroupRepository

3. **Add UI Tests**:
   - Navigation flows
   - Timer updates
   - CRUD operations

4. **Add Edge Cases**:
   - Empty states
   - Error conditions
   - Boundary values

## Resources

- [Android Testing Fundamentals](https://developer.android.com/training/testing/fundamentals)
- [Testing Kotlin Coroutines](https://developer.android.com/kotlin/coroutines/test)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [MockK Documentation](https://mockk.io/)
- [Truth Assertions](https://truth.dev/)
- [Turbine for Flow Testing](https://github.com/cashapp/turbine)

---

**Note**: The current test suite provides a solid foundation. Expand coverage as the app evolves and requirements change.
