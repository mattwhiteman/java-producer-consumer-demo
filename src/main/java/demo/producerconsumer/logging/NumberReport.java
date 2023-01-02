package demo.producerconsumer.logging;

import java.util.Objects;

/**
 * Simple read-only POJO containing metrics for the data seen during this application run.
 */
public class NumberReport {
    private final int dupesThisRun;
    private final int uniquesThisRun;
    private final int totalUniques;

    public NumberReport(int dupesThisRun, int uniquesThisRun, int totalUniques) {
        this.dupesThisRun = dupesThisRun;
        this.uniquesThisRun = uniquesThisRun;
        this.totalUniques = totalUniques;
    }

    public int getDupesThisRun() {
        return dupesThisRun;
    }

    public int getUniquesThisRun() {
        return uniquesThisRun;
    }

    public int getTotalUniques() {
        return totalUniques;
    }

    @Override
    public String toString() {
        return "NumberReport{" +
                "dupesThisRun=" + dupesThisRun +
                ", uniquesThisRun=" + uniquesThisRun +
                ", totalUniques=" + totalUniques +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberReport that = (NumberReport) o;
        return dupesThisRun == that.dupesThisRun && uniquesThisRun == that.uniquesThisRun && totalUniques == that.totalUniques;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dupesThisRun, uniquesThisRun, totalUniques);
    }
}
