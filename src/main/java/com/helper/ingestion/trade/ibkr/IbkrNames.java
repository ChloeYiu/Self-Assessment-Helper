package com.helper.ingestion.trade.ibkr;

/**
 * Names used by the internal IBKR Flex tabular document contract.
 */
public final class IbkrNames {
    private IbkrNames() {
    }

    /** Table names in IBKR tabular documents. */
    public static final class Tables {
        public static final String TRADES = "Trades";
        public static final String SECURITIES = "Securities";

        private Tables() {
        }
    }

    /** Raw column names copied from IBKR Flex Trade XML attributes. */
    public static final class RawTradeColumns {
        public static final String ACCOUNT_ID = "accountId";
        public static final String ASSET_CATEGORY = "assetCategory";
        public static final String SYMBOL = "symbol";
        public static final String DESCRIPTION = "description";
        public static final String CONID = "conid";
        public static final String ISIN = "isin";
        public static final String SECURITY_ID = "securityID";
        public static final String SECURITY_ID_TYPE = "securityIDType";
        public static final String FIGI = "figi";
        public static final String LISTING_EXCHANGE = "listingExchange";
        public static final String TRADE_DATE = "tradeDate";
        public static final String SETTLE_DATE_TARGET = "settleDateTarget";
        public static final String BUY_SELL = "buySell";
        public static final String QUANTITY = "quantity";
        public static final String TRADE_PRICE = "tradePrice";
        public static final String PROCEEDS = "proceeds";
        public static final String IB_COMMISSION = "ibCommission";
        public static final String CURRENCY = "currency";
        public static final String FIFO_PNL_REALIZED = "fifoPnlRealized";

        private RawTradeColumns() {
        }
    }

    /** Column names used by the normalized IBKR Trades table consumed by domain extractors. */
    public static final class NormalizedTradeColumns {
        public static final String IDENTIFIER = "identifier";
        public static final String TRADE_ID = "tradeId";
        public static final String TRANSACTION_DATE = "transactionDate";
        public static final String TRADE_TYPE = "tradeType";
        public static final String QUANTITY = "quantity";
        public static final String GROSS_AMOUNT_GBP = "grossAmountGbp";
        public static final String FEE_GBP = "feeGbp";

        private NormalizedTradeColumns() {
        }
    }

    /** Column names used by the internal IBKR Securities table. */
    public static final class SecurityColumns {
        public static final String IDENTIFIER = "identifier";
        public static final String IDENTIFIER_TYPE = "identifierType";
        public static final String SYMBOL = "symbol";
        public static final String DESCRIPTION = "description";
        public static final String ASSET_CATEGORY = "assetCategory";
        public static final String CONID = "conid";
        public static final String ISIN = "isin";
        public static final String SECURITY_ID = "securityID";
        public static final String SECURITY_ID_TYPE = "securityIDType";
        public static final String FIGI = "figi";
        public static final String LISTING_EXCHANGE = "listingExchange";
        public static final String CURRENCY = "currency";

        private SecurityColumns() {
        }
    }
}
