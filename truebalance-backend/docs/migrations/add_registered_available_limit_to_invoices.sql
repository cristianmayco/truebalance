-- Migration: Add registered available limit fields to invoices table
-- Description: Adds fields to allow registering a specific available limit for a closed invoice,
--              which will be used as the starting point for limit calculations instead of
--              calculating from all previous invoices.
-- Date: 2026-01-03

-- Add column to indicate if this invoice registers a specific available limit
ALTER TABLE invoices
ADD COLUMN register_available_limit BOOLEAN NOT NULL DEFAULT FALSE;

-- Add column to store the registered available limit value
ALTER TABLE invoices
ADD COLUMN registered_available_limit NUMERIC(10,2);

-- Add comments for documentation
COMMENT ON COLUMN invoices.register_available_limit IS 'If true, this invoice is a starting point for limit calculations and all previous invoices are ignored';
COMMENT ON COLUMN invoices.registered_available_limit IS 'The available limit value registered for this invoice. Only used when register_available_limit = true';

-- Add constraint: registered_available_limit must be set when register_available_limit is true
ALTER TABLE invoices
ADD CONSTRAINT check_registered_limit_when_flag_true
CHECK (
    (register_available_limit = FALSE) OR
    (register_available_limit = TRUE AND registered_available_limit IS NOT NULL)
);
