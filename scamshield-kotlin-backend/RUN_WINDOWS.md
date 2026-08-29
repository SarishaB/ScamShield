# Windows quick start

1. Install JDK 21.
2. Open this folder in IntelliJ IDEA.
3. Make sure Gradle uses JDK 21.
4. Start PostgreSQL:
   `docker compose up -d postgres`
5. Run `gradle run` or use IntelliJ's Gradle task `application > run`.
6. Test `http://localhost:8080/health`.

If you have Gradle installed globally, `gradle buildFatJar` creates the fat JAR.
