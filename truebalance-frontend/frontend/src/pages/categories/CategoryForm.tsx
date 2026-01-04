import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowLeft, Save } from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { useCategory, useCreateCategory, useUpdateCategory } from '@/hooks/useCategories'
import { useToast } from '@/contexts/ToastContext'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Card } from '@/components/ui/Card'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { ColorPicker } from '@/components/ui/ColorPicker'
import { z } from 'zod'

const categorySchema = z.object({
  name: z
    .string()
    .min(2, 'Nome deve ter no mínimo 2 caracteres')
    .max(100, 'Nome deve ter no máximo 100 caracteres'),
  description: z
    .string()
    .max(500, 'Descrição deve ter no máximo 500 caracteres')
    .optional(),
  color: z
    .union([
      z.string().regex(/^#[0-9A-Fa-f]{6}$/, 'Cor deve estar no formato hexadecimal (#RRGGBB)'),
      z.literal(''),
      z.undefined(),
    ])
    .optional()
    .nullable(),
})

type CategoryFormData = z.infer<typeof categorySchema>

export function CategoryForm() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const isEditMode = !!id
  const categoryId = id ? parseInt(id) : undefined

  const { showToast } = useToast()
  const { data: category, isLoading: isLoadingCategory } = useCategory(categoryId)
  const { mutate: createCategory, isPending: isCreating } = useCreateCategory()
  const { mutate: updateCategory, isPending: isUpdating } = useUpdateCategory()

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<CategoryFormData>({
    resolver: zodResolver(categorySchema),
    defaultValues: {
      name: '',
      description: '',
      color: '',
    },
  })

  // Load category data when editing
  useEffect(() => {
    if (category && isEditMode) {
      setValue('name', category.name)
      setValue('description', category.description || '')
      setValue('color', category.color || '')
    }
  }, [category, isEditMode, setValue])

  const onSubmit = (data: CategoryFormData) => {
    const payload = {
      name: data.name,
      description: data.description || undefined,
      color: data.color || undefined,
    }

    if (isEditMode && categoryId) {
      updateCategory(
        { id: categoryId, category: payload },
        {
          onSuccess: () => {
            showToast('success', 'Categoria atualizada com sucesso!')
            navigate('/categories')
          },
          onError: (error: any) => {
            showToast('error', error.response?.data?.message || 'Erro ao atualizar categoria')
          },
        }
      )
    } else {
      createCategory(payload, {
        onSuccess: () => {
          showToast('success', 'Categoria criada com sucesso!')
          navigate('/categories')
        },
        onError: (error: any) => {
          showToast('error', error.response?.data?.message || 'Erro ao criar categoria')
        },
      })
    }
  }

  if (isLoadingCategory && isEditMode) {
    return (
      <AppShell title={isEditMode ? 'Editar Categoria' : 'Nova Categoria'}>
        <LoadingSpinner fullScreen />
      </AppShell>
    )
  }

  const isSubmitting = isCreating || isUpdating

  return (
    <AppShell title={isEditMode ? 'Editar Categoria' : 'Nova Categoria'}>
      <div className="max-w-2xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => navigate('/categories')} aria-label="Voltar">
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              {isEditMode ? 'Editar Categoria' : 'Nova Categoria'}
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mt-1">
              {isEditMode ? 'Atualize as informações da categoria' : 'Cadastre uma nova categoria'}
            </p>
          </div>
        </div>

        {/* Form */}
        <Card>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <Input
              label="Nome da categoria"
              placeholder="Ex: Moradia, Saúde, Educação..."
              error={errors.name?.message}
              required
              {...register('name')}
            />

            <Input
              label="Descrição (opcional)"
              placeholder="Descreva esta categoria..."
              error={errors.description?.message}
              {...register('description')}
            />

            <ColorPicker
              value={watch('color') || undefined}
              onChange={(color) => {
                setValue('color', color || '', { shouldValidate: true })
              }}
              error={errors.color?.message}
            />

            {/* Actions */}
            <div className="flex justify-end gap-3 pt-4">
              <Button
                type="button"
                variant="secondary"
                onClick={() => navigate('/categories')}
                disabled={isSubmitting}
              >
                Cancelar
              </Button>
              <Button type="submit" loading={isSubmitting}>
                <Save className="w-4 h-4" />
                {isEditMode ? 'Atualizar' : 'Criar'} Categoria
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </AppShell>
  )
}
