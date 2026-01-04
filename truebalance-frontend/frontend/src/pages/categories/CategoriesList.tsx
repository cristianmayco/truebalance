import { useNavigate } from 'react-router-dom'
import { Plus, Edit, Trash2, Tag, BarChart3 } from 'lucide-react'
import { AppShell } from '@/components/layout/AppShell'
import { useCategories, useDeleteCategory } from '@/hooks/useCategories'
import { Button } from '@/components/ui/Button'
import { LoadingSpinner } from '@/components/ui/LoadingSpinner'
import { EmptyState } from '@/components/ui/EmptyState'
import { Card } from '@/components/ui/Card'
import { useToast } from '@/contexts/ToastContext'
import type { CategoryResponseDTO } from '@/types/dtos/category.dto'

export function CategoriesList() {
  const navigate = useNavigate()
  const { showToast } = useToast()
  const { data: categories = [], isLoading, error } = useCategories()
  const { mutate: deleteCategory } = useDeleteCategory()

  const handleNew = () => {
    navigate('/categories/new')
  }

  const handleEdit = (category: CategoryResponseDTO) => {
    navigate(`/categories/${category.id}/edit`)
  }

  const handleViewDashboard = (category: CategoryResponseDTO) => {
    navigate(`/categories/${category.id}/dashboard`)
  }

  const handleDelete = (category: CategoryResponseDTO) => {
    if (window.confirm(`Tem certeza que deseja deletar a categoria "${category.name}"?`)) {
      deleteCategory(category.id, {
        onSuccess: () => {
          showToast('success', 'Categoria deletada com sucesso!')
        },
        onError: (error: any) => {
          showToast('error', error.response?.data?.message || 'Erro ao deletar categoria')
        },
      })
    }
  }

  if (isLoading) {
    return (
      <AppShell title="Categorias">
        <LoadingSpinner fullScreen />
      </AppShell>
    )
  }

  if (error) {
    return (
      <AppShell title="Categorias">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <p className="text-red-600 dark:text-red-400 mb-4">
              Erro ao carregar categorias: {(error as any).message}
            </p>
            <Button onClick={() => window.location.reload()}>Tentar novamente</Button>
          </div>
        </div>
      </AppShell>
    )
  }

  const isEmpty = categories.length === 0

  return (
    <AppShell title="Categorias">
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Categorias</h1>
            <p className="text-gray-600 dark:text-gray-400 mt-1">
              Gerencie as categorias das suas contas
            </p>
          </div>
          <Button onClick={handleNew}>
            <Plus className="w-4 h-4" />
            Nova Categoria
          </Button>
        </div>

        {/* Content */}
        {isEmpty ? (
          <EmptyState
            icon={<Tag className="w-12 h-12" />}
            message="Nenhuma categoria encontrada"
            description="Comece criando sua primeira categoria para organizar suas contas."
            actionLabel="Nova Categoria"
            onAction={handleNew}
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {categories.map((category) => (
              <Card key={category.id} className="p-4 hover:shadow-lg transition-shadow cursor-pointer" onClick={() => handleViewDashboard(category)}>
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      {category.color && (
                        <div
                          className="w-4 h-4 rounded-full"
                          style={{ backgroundColor: category.color }}
                        />
                      )}
                      <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                        {category.name}
                      </h3>
                    </div>
                    {category.description && (
                      <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">
                        {category.description}
                      </p>
                    )}
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={(e) => {
                        e.stopPropagation()
                        handleViewDashboard(category)
                      }}
                      className="w-full mt-2"
                    >
                      <BarChart3 className="w-4 h-4" />
                      Ver Dashboard
                    </Button>
                  </div>
                  <div className="flex flex-col gap-2 ml-4" onClick={(e) => e.stopPropagation()}>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleEdit(category)}
                      aria-label={`Editar categoria ${category.name}`}
                      title="Editar"
                    >
                      <Edit className="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDelete(category)}
                      aria-label={`Deletar categoria ${category.name}`}
                      title="Deletar"
                    >
                      <Trash2 className="w-4 h-4 text-red-600" />
                    </Button>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </AppShell>
  )
}
