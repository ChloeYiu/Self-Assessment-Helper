package com.helper.pipeline.consolidation;

import com.helper.income.config.SecurityConfig;
import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.income.implementation.capitalgain.model.CarryForwardSnapshot;
import com.helper.income.implementation.dividend.model.SecurityHoldingSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Consolidated source facts and opening state for one supported security.
 */
public record ConsolidatedSecurity(
        Security security,
        SecurityConfig config,
        List<Trade> trades,
        List<SecurityHoldingSnapshot> holdingSnapshots,
        List<CarryForwardSnapshot> carryForwardSnapshots
) {
    /** Creates immutable facts for one configured security. */
    public ConsolidatedSecurity {
        security = Objects.requireNonNull(security, "security");
        config = Objects.requireNonNull(config, "config");
        trades = List.copyOf(Objects.requireNonNull(trades, "trades"));
        holdingSnapshots = List.copyOf(Objects.requireNonNull(holdingSnapshots, "holdingSnapshots"));
        carryForwardSnapshots = List.copyOf(
                Objects.requireNonNull(carryForwardSnapshots, "carryForwardSnapshots"));
    }

    /**
     * Mutable construction state used only while facts from multiple sources are consolidated.
     */
    static final class Builder {
        private final Security security;
        private final SecurityConfig config;
        private final List<Trade> trades = new ArrayList<>();
        private final List<SecurityHoldingSnapshot> holdingSnapshots = new ArrayList<>();
        private final List<CarryForwardSnapshot> carryForwardSnapshots = new ArrayList<>();

        Builder(Security security, SecurityConfig config) {
            this.security = Objects.requireNonNull(security, "security");
            this.config = Objects.requireNonNull(config, "config");
        }

        void addTrade(Trade trade) {
            trades.add(Objects.requireNonNull(trade, "trade"));
        }

        void addHoldingSnapshot(SecurityHoldingSnapshot snapshot) {
            holdingSnapshots.add(Objects.requireNonNull(snapshot, "snapshot"));
        }

        void addCarryForwardSnapshot(CarryForwardSnapshot snapshot) {
            carryForwardSnapshots.add(Objects.requireNonNull(snapshot, "snapshot"));
        }

        ConsolidatedSecurity build() {
            return new ConsolidatedSecurity(
                    security,
                    config,
                    trades,
                    holdingSnapshots,
                    carryForwardSnapshots);
        }
    }
}
