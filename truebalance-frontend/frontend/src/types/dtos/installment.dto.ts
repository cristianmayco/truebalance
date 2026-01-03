export interface InstallmentResponseDTO {
  id: number
  billId: number
  invoiceId: number
  installmentNumber: number
  amount: number
  dueDate: string
  createdAt: string
}
