# Teacher System (JavaFX)

Requires JDK 17+ and Maven.

    mvn javafx:run      # start the app
    mvn test            # run the grade-calculation tests

Flow: Dashboard -> Subject -> Section -> Student -> Grade Management (Midterm | Finals).
Saved scores are stored in  ~/.teacher-system/grades.properties

Grade math lives in  src/main/java/teacher/GradeCalculator.java  (weights, passing grade, GWA table).
Sample subjects/students are in  DataStore.java.
Icons exported from your SVG are in  src/main/resources/teacher/icons/.
