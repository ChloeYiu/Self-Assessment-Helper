package com.helper.pipeline.selfdirectedinvestment.consolidation;

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
public record ConsolidatedSecurityFacts(
        Security security,
        SecurityConfig config,
        List<Trade> trades,
        SecurityHoldingSnapshot holdingSnapshot,
        CarryForwardSnapshot carryForwardSnapshot
) {
    /** Creates immutable facts for one configured security. */
    public ConsolidatedSecurityFacts {
        security = Objects.requireNonNull(security, "security");
        config = Objects.requireNonNull(config, "config");
        trades = List.copyOf(Objects.requireNonNull(trades, "trades"));
    }

    /**
     * Mutable construction state used only while facts from multiple sources are consolidated.
     */
    static final class Builder {
        private final Security security;
        private final SecurityConfig config;
        private final List<Trade> trades = new ArrayList<>();
        private SecurityHoldingSnapshot holdingSnapshot;
        private CarryForwardSnapshot carryForwardSnapshot;

        Builder(Security security, SecurityConfig config) {
            this.security = Objects.requireNonNull(security, "security");
            this.config = Objects.requireNonNull(config, "config");
        }

        void addTrade(Trade trade) {
            trades.add(Objects.requireNonNull(trade, "trade"));
        }

        void setHoldingSnapshot(SecurityHoldingSnapshot snapshot) {
            if (holdingSnapshot != null) {
                throw new IllegalArgumentException("security can have only one holding snapshot");
            }
            holdingSnapshot = Objects.requireNonNull(snapshot, "snapshot");
        }

        void setCarryForwardSnapshot(CarryForwardSnapshot snapshot) {
            if (carryForwardSnapshot != null) {
                throw new IllegalArgumentException("security can have only one carry-forward snapshot");
            }
            carryForwardSnapshot = Objects.requireNonNull(snapshot, "snapshot");
        }

        ConsolidatedSecurityFacts build() {
            return new ConsolidatedSecurityFacts(
                    security,
                    config,
                    trades,
                    holdingSnapshot,
                    carryForwardSnapshot);
        }
    }
}
