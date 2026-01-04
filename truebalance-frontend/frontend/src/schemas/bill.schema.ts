import { z } from 'zod'

export const billSchema = z.object({
  name: z
    .string()
    .min(1, 'Nome da conta é obrigatório')
    .min(3, 'Nome deve ter no mínimo 3 caracteres')
    .max(100, 'Nome deve ter no máximo 100 caracteres'),

  date: z
    .string()
    .min(1, 'Data é obrigatória')
    .refine((date) => {
      const parsedDate = new Date(date)
      return !isNaN(parsedDate.getTime())
    }, 'Data inválida'),

  totalAmount: z
    .number({
      message: 'Valor total é obrigatório',
    })
    .positive('Valor total deve ser maior que zero')
    .finite('Valor total inválido'),

  numberOfInstallments: z
    .number({
      message: 'Número de parcelas é obrigatório',
    })
    .int('Número de parcelas deve ser um número inteiro')
    .min(1, 'Deve ter pelo menos 1 parcela')
    .max(120, 'Máximo de 120 parcelas permitidas'),

  description: z
    .string()
    .max(500, 'Descrição deve ter no máximo 500 caracteres')
    .optional(),

  category: z
    .string()
    .max(100, 'Categoria deve ter no máximo 100 caracteres')
    .optional(),

  categoryId: z
    .number()
    .int('ID da categoria deve ser um número inteiro')
    .positive('ID da categoria inválido')
    .optional()
    .nullable(),

  creditCardId: z
    .number()
    .int('ID do cartão deve ser um número inteiro')
    .positive('ID do cartão inválido')
    .optional()
    .nullable(),

  isRecurring: z.boolean().default(false),
})

// Override the type to ensure isRecurring is always boolean, not optional
export type BillFormData = Omit<z.infer<typeof billSchema>, 'isRecurring'> & {
  isRecurring: boolean
}
