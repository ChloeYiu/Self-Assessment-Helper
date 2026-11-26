package com.helper.pipeline.selfdirectedinvestment.dividend;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.helper.income.config.SecurityConfigEnums.DividendCalculationMethodType;
import com.helper.pipeline.selfdirectedinvestment.dividend.method.NonUkAccumulatingFundDividendPipeline;
import org.junit.Test;

public class DividendMethodDispatcherTest {
    @Test
    public void findPipeline_returnsPipelineRegisteredForMethod() {
        DividendMethodDispatcher dispatcher = new DividendMethodDispatcher();

        assertTrue(dispatcher.findPipeline(
                DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND).orElseThrow()
                instanceof NonUkAccumulatingFundDividendPipeline);
    }

    @Test
    public void findPipeline_returnsEmptyWhenMethodIsNotRegistered() {
        DividendMethodDispatcher dispatcher = new DividendMethodDispatcher();

        assertFalse(dispatcher.findPipeline(
                DividendCalculationMethodType.UNSUPPORTED).isPresent());
    }

}
