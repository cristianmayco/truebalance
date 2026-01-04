import { z } from 'zod';

/**
 * Schema de validação para pagamento parcial de fatura
 */
export const partialPaymentSchema = z.object({
  amount: z
    .number({
      message: 'O valor é obrigatório',
    })
    .positive('O valor deve ser maior que zero')
    .min(0.01, 'O valor mínimo é R$ 0,01'),

  description: z
    .string()
    .max(200, 'A descrição deve ter no máximo 200 caracteres')
    .optional(),
  // Note: paymentDate is set by the backend, not sent from frontend
});

/**
 * Tipo TypeScript inferido do schema
 */
export type PartialPaymentFormData = z.infer<typeof partialPaymentSchema>;

/**
 * Schema para validação de pagamento integral
 */
export const fullPaymentSchema = z.object({
  description: z
    .string()
    .max(200, 'A descrição deve ter no máximo 200 caracteres')
    .optional(),
  // Note: paymentDate is not needed - marking as paid doesn't require a date
});

/**
 * Tipo TypeScript inferido do schema de pagamento integral
 */
export type FullPaymentFormData = z.infer<typeof fullPaymentSchema>;

/**
 * Schema unificado que valida tanto pagamento parcial quanto integral
 */
export const paymentSchema = z.discriminatedUnion('type', [
  z.object({
    type: z.literal('full'),
    paymentDate: z.date().default(() => new Date()),
    description: z.string().max(200).optional(),
  }),
  z.object({
    type: z.literal('partial'),
    amount: z.number().positive().min(0.01),
    paymentDate: z.date().default(() => new Date()),
    description: z.string().max(200).optional(),
  }),
]);

/**
 * Tipo TypeScript inferido do schema unificado
 */
export type PaymentFormData = z.infer<typeof paymentSchema>;
