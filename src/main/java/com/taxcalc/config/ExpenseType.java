package com.taxcalc.config;

/**
 * Type-safe enum for all expense categories
 * Used as keys in expense maps for compile-time safety
 */
public enum ExpenseType {
    COUNCIL_TAX("Council Tax"),
    WATER("Water/Sewerage"),
    ELECTRICITY("Electricity"),
    GAS("Gas"),
    INTERNET("Internet/Utilities"),
    FURNITURE("Furniture & Furnishings"),
    REPAIRS("Repairs & Maintenance"),
    INSURANCE("Insurance"),
    ADVERTISING("Advertising"),
    MANAGEMENT_FEE("Management/Letting Agent Fee"),
    OTHER("Other");
    
    public final String label;
    
    ExpenseType(String label) {
        this.label = label;
    }
    
    /**
     * Get the normalized enum name for this expense type
     */
    public String getNormalizedName() {
        return this.name();  // Returns e.g., "COUNCIL_TAX"
    }
    
    /**
     * Get human-readable label
     */
    public String getLabel() {
        return label;
    }
}
