package com.helper.income.artifact;

import com.helper.income.implementation.dividend.model.SecurityHoldingSnapshot;
import java.util.Objects;

/**
 * Processing artifact for a dividend holding snapshot.
 */
public class HoldingSnapshotArtifact implements IncomeArtifact {
    private final SecurityHoldingSnapshot snapshot;

    /**
     * Creates an artifact for a holding snapshot.
     */
    public HoldingSnapshotArtifact(SecurityHoldingSnapshot snapshot) {
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
    }

    /**
     * Returns the snapshot to export.
     */
    public SecurityHoldingSnapshot getSnapshot() {
        return snapshot;
    }

    @Override
    public IncomeArtifactType getArtifactType() {
        return IncomeArtifactType.DIVIDEND_HOLDING_SNAPSHOT;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException();
    }
}
