import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class Team {
    final String name;
    final int points;
    final int goalDiff;

    Team(String name, int points, int goalDiff) {
        this.name = name;
        this.points = points;
        this.goalDiff = goalDiff;
    }

    @Override
    public String toString() {
        return name + "(" + points + "," + goalDiff + ")";
    }
}

class Leaderboard<T> {
    private final List<T> items = new ArrayList<>();
    private final Comparator<? super T> order;

    Leaderboard(Comparator<? super T> order) {
        this.order = order;
    }

    void add(T item) {
        items.add(item);
    }

    void addAll(List<? extends T> newItems) {
        items.addAll(newItems);
    }

    List<T> ranking() {
        List<T> sortedCopy = new ArrayList<>(items);
        sortedCopy.sort(order);
        return sortedCopy;
    }

    List<T> entriesInInsertionOrder() {
        return new ArrayList<>(items);
    }
}

public class LeaderboardDemo {
    private static final Comparator<Team> BY_RANK =
            Comparator.comparingInt((Team team) -> team.points)
                    .reversed()
                    .thenComparing(
                            Comparator.comparingInt((Team team) -> team.goalDiff).reversed())
                    .thenComparing(team -> team.name);

    private static final Comparator<Team> FAULTY_SUBTRACTION_ORDER =
            (first, second) -> second.points - first.points;

    public static void main(String[] args) throws IOException {
        Path resultsPath = Path.of("experimental_data", "q5_leaderboard_results.csv");
        try (BufferedWriter writer =
                     Files.newBufferedWriter(resultsPath, StandardCharsets.UTF_8)) {
            writeCsvHeader(writer);

            List<Team> assignmentTeams = List.of(
                    new Team("Panthers", 9, 4),
                    new Team("Eagles", 9, 6),
                    new Team("Cobras", 6, 1),
                    new Team("Bisons", 9, 4));
            Leaderboard<Team> assignmentBoard = new Leaderboard<>(BY_RANK);
            assignmentBoard.addAll(assignmentTeams);
            List<Team> assignmentRanking = assignmentBoard.ranking();
            List<Team> assignmentExpected = List.of(
                    assignmentTeams.get(1),
                    assignmentTeams.get(3),
                    assignmentTeams.get(0),
                    assignmentTeams.get(2));
            verifyRanking("assignment_expected_output", assignmentRanking,
                    assignmentExpected, writer);
            verifyInputUnchanged(assignmentBoard, assignmentTeams);
            System.out.println("Assignment expected output:");
            System.out.println(assignmentRanking);

            List<Team> tiedTeams = List.of(
                    new Team("Falcons", 12, 5),
                    new Team("Wolves", 12, 5),
                    new Team("Hawks", 12, 8),
                    new Team("Bears", 12, 8),
                    new Team("Tigers", 10, 5),
                    new Team("Lions", 10, 5));
            Leaderboard<Team> tieBoard = new Leaderboard<>(BY_RANK);
            tieBoard.addAll(tiedTeams);
            List<Team> tieRanking = tieBoard.ranking();
            List<Team> tieExpected = List.of(
                    tiedTeams.get(3),
                    tiedTeams.get(2),
                    tiedTeams.get(0),
                    tiedTeams.get(1),
                    tiedTeams.get(5),
                    tiedTeams.get(4));
            verifyRanking("multiple_tied_scores", tieRanking, tieExpected, writer);
            verifyComparatorLaws(tiedTeams);
            verifyInputUnchanged(tieBoard, tiedTeams);
            System.out.println("Multiple-tie test:");
            System.out.println(tieRanking);

            demonstrateFaultyComparator(writer);
        }
        System.out.println("All correct-comparator checks passed.");
        System.out.println("Raw ranking results: " + resultsPath);
    }

    private static void demonstrateFaultyComparator(BufferedWriter writer)
            throws IOException {
        Team maximum = new Team("Maximum", Integer.MAX_VALUE, 0);
        Team middle = new Team("Middle", 0, 0);
        Team minimum = new Team("Minimum", Integer.MIN_VALUE, 0);

        int maximumBeforeMiddle = Integer.signum(
                FAULTY_SUBTRACTION_ORDER.compare(maximum, middle));
        int middleBeforeMinimum = Integer.signum(
                FAULTY_SUBTRACTION_ORDER.compare(middle, minimum));
        int maximumBeforeMinimum = Integer.signum(
                FAULTY_SUBTRACTION_ORDER.compare(maximum, minimum));
        boolean transitivityViolated = maximumBeforeMiddle < 0
                && middleBeforeMinimum < 0
                && maximumBeforeMinimum > 0;
        if (!transitivityViolated) {
            throw new IllegalStateException(
                    "The deliberate subtraction comparator did not expose overflow.");
        }

        Leaderboard<Team> correctExtremeBoard = new Leaderboard<>(BY_RANK);
        correctExtremeBoard.addAll(List.of(maximum, middle, minimum));
        List<String> correctNames = names(correctExtremeBoard.ranking());
        List<String> expectedNames = List.of("Maximum", "Middle", "Minimum");
        if (!correctNames.equals(expectedNames)) {
            throw new IllegalStateException("Extreme-score ranking is incorrect.");
        }

        List<Team> faultyResult = new ArrayList<>(List.of(maximum, middle, minimum));
        faultyResult.sort(FAULTY_SUBTRACTION_ORDER);
        System.out.println("Faulty comparator pair signs (maximum/middle, "
                + "middle/minimum, maximum/minimum): "
                + maximumBeforeMiddle + ", " + middleBeforeMinimum + ", "
                + maximumBeforeMinimum);
        System.out.println("Faulty comparator violates transitivity: " + transitivityViolated);
        System.out.println("Correct extreme-score ranking: " + correctNames);
        System.out.println("Faulty sort output for this input: " + names(faultyResult));

        writeRow(writer, "faulty_comparator_transitivity", 1,
                Integer.toString(maximumBeforeMiddle), maximum.points, maximum.goalDiff,
                "-1 (Maximum before Middle)",
                maximumBeforeMiddle < 0);
        writeRow(writer, "faulty_comparator_transitivity", 2,
                Integer.toString(middleBeforeMinimum), middle.points, middle.goalDiff,
                "-1 (Middle before Minimum)",
                middleBeforeMinimum < 0);
        writeRow(writer, "faulty_comparator_transitivity", 3,
                Integer.toString(maximumBeforeMinimum), maximum.points, maximum.goalDiff,
                "-1 (Maximum before Minimum)",
                maximumBeforeMinimum < 0);
        for (int index = 0; index < faultyResult.size(); index++) {
            Team team = faultyResult.get(index);
            writeRow(writer, "faulty_sorted_output", index + 1, team.name,
                    team.points, team.goalDiff, expectedNames.get(index),
                    team.name.equals(expectedNames.get(index)));
        }
    }

    private static void verifyRanking(
            String testName,
            List<Team> actual,
            List<Team> expected,
            BufferedWriter writer) throws IOException {
        if (!actual.equals(expected)) {
            throw new IllegalStateException(
                    testName + " produced " + actual + " instead of " + expected);
        }
        for (int index = 0; index < actual.size(); index++) {
            Team team = actual.get(index);
            writeRow(writer, testName, index + 1, team.name, team.points, team.goalDiff,
                    expected.get(index).name, true);
        }
    }

    private static void verifyInputUnchanged(Leaderboard<Team> board, List<Team> original) {
        if (!board.entriesInInsertionOrder().equals(original)) {
            throw new IllegalStateException("ranking() modified the stored input order.");
        }
    }

    private static void verifyComparatorLaws(List<Team> teams) {
        for (Team first : teams) {
            if (BY_RANK.compare(first, first) != 0) {
                throw new IllegalStateException("Comparator is not reflexive.");
            }
            for (Team second : teams) {
                int forward = Integer.signum(BY_RANK.compare(first, second));
                int reverse = Integer.signum(BY_RANK.compare(second, first));
                if (forward != -reverse) {
                    throw new IllegalStateException("Comparator is not antisymmetric.");
                }
                for (Team third : teams) {
                    if (BY_RANK.compare(first, second) <= 0
                            && BY_RANK.compare(second, third) <= 0
                            && BY_RANK.compare(first, third) > 0) {
                        throw new IllegalStateException("Comparator is not transitive.");
                    }
                }
            }
        }
    }

    private static List<String> names(List<Team> teams) {
        List<String> result = new ArrayList<>(teams.size());
        for (Team team : teams) {
            result.add(team.name);
        }
        return result;
    }

    private static void writeCsvHeader(BufferedWriter writer) throws IOException {
        writer.write("test_name,position,observed_result,points,goal_difference,"
                + "expected_result,matches_expected");
        writer.newLine();
    }

    private static void writeRow(
            BufferedWriter writer,
            String testName,
            int position,
            String team,
            int points,
            int goalDifference,
            String expectedTeam,
            boolean matchesExpected) throws IOException {
        writer.write(String.join(",",
                testName,
                Integer.toString(position),
                team,
                Integer.toString(points),
                Integer.toString(goalDifference),
                expectedTeam,
                Boolean.toString(matchesExpected)));
        writer.newLine();
    }
}
