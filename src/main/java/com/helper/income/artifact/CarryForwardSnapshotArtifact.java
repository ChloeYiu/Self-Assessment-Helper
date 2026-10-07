package com.helper.income.artifact;

import com.helper.income.implementation.capitalgain.model.CarryForwardSnapshot;
import java.util.Objects;

/**
 * Processing artifact for a capital-gain carry-forward snapshot.
 */
public class CarryForwardSnapshotArtifact implements IncomeArtifact {
    private final CarryForwardSnapshot snapshot;

    /**
     * Creates an artifact for a carry-forward snapshot.
     */
    public CarryForwardSnapshotArtifact(CarryForwardSnapshot snapshot) {
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
    }

    /**
     * Returns the snapshot to export.
     */
    public CarryForwardSnapshot getSnapshot() {
        return snapshot;
    }

    @Override
    public IncomeArtifactType getArtifactType() {
        return IncomeArtifactType.CAPITAL_GAIN_CARRY_FORWARD_SNAPSHOT;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException();
    }
}
