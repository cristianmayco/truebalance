import { z } from 'zod'

export const creditCardSchema = z.object({
  name: z
    .string()
    .min(1, 'Nome do cartão é obrigatório')
    .min(3, 'Nome deve ter no mínimo 3 caracteres')
    .max(100, 'Nome deve ter no máximo 100 caracteres'),

  creditLimit: z
    .number({
      message: 'Limite de crédito é obrigatório',
    })
    .positive('Limite de crédito deve ser maior que zero')
    .finite('Limite de crédito inválido'),

  closingDay: z
    .number({
      message: 'Dia de fechamento é obrigatório',
    })
    .int('Dia de fechamento deve ser um número inteiro')
    .min(1, 'Dia de fechamento deve estar entre 1 e 31')
    .max(31, 'Dia de fechamento deve estar entre 1 e 31'),

  dueDay: z
    .number({
      message: 'Dia de vencimento é obrigatório',
    })
    .int('Dia de vencimento deve ser um número inteiro')
    .min(1, 'Dia de vencimento deve estar entre 1 e 31')
    .max(31, 'Dia de vencimento deve estar entre 1 e 31'),

  allowsPartialPayment: z.boolean().default(false),
})

export type CreditCardFormData = z.infer<typeof creditCardSchema>
