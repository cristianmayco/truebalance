-- Migration: Add use_absolute_value column to invoices table
-- Description: Adds a flag to indicate if invoice totalAmount should be calculated from installments or use absolute value
-- Date: 2026-01-03

ALTER TABLE invoices
ADD COLUMN use_absolute_value BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN invoices.use_absolute_value IS 'If true, totalAmount is not recalculated from installments. Useful for old invoices where not all bills are registered.';
