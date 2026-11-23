package com.helper.pipeline.selfdirectedinvestment.dividend;

import com.helper.config.TaxYear;
import com.helper.income.config.SecurityConfigEnums.DividendCalculationMethodType;
import com.helper.pipeline.selfdirectedinvestment.consolidation.ConsolidatedSecurityFacts;
import com.helper.support.registry.Registry;
import java.util.List;

/**
 * A method-specific strategy registered with {@link DividendMethodDispatcher}.
 *
 * <p>Each implementation prepares all securities assigned to its configured method.</p>
 */
public interface DividendMethodPipeline {
    /** Returns the one configured dividend method this strategy handles. */
    DividendCalculationMethodType getMethodType();

    /**
     * Prepares the combined dividend result for securities assigned to this method.
     *
     * <p>Implementations may load only the method-specific inputs they require. The
     * scaffold does not yet define that behavior.</p>
     *
     * @param taxYear tax year for the calculation run
     * @param registry prefetched shared run resources, including FX documents
     * @param securityFacts consolidated facts for supported securities using this method
     * @return prepared dividend sources and non-fatal preparation issues
     */
    DividendMethodResult prepareDividendSources(
            TaxYear taxYear,
            Registry registry,
            List<ConsolidatedSecurityFacts> securityFacts
    );
}
