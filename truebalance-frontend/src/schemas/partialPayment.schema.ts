import { z } from 'zod';

/**
 * Schema de validação para pagamento parcial de fatura
 */
export const partialPaymentSchema = z.object({
  amount: z
    .number({
      required_error: 'O valor é obrigatório',
      invalid_type_error: 'O valor deve ser um número',
    })
    .positive('O valor deve ser maior que zero')
    .min(0.01, 'O valor mínimo é R$ 0,01'),

  description: z
    .string()
    .max(200, 'A descrição deve ter no máximo 200 caracteres')
    .optional(),

  paymentDate: z
    .date({
      required_error: 'A data de pagamento é obrigatória',
      invalid_type_error: 'Data inválida',
    })
    .default(() => new Date()),
});

/**
 * Tipo TypeScript inferido do schema
 */
export type PartialPaymentFormData = z.infer<typeof partialPaymentSchema>;

/**
 * Schema para validação de pagamento integral
 */
export const fullPaymentSchema = z.object({
  paymentDate: z
    .date({
      required_error: 'A data de pagamento é obrigatória',
      invalid_type_error: 'Data inválida',
    })
    .default(() => new Date()),

  description: z
    .string()
    .max(200, 'A descrição deve ter no máximo 200 caracteres')
    .optional(),
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
