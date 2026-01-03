import { useNavigate } from 'react-router-dom'
import { FileQuestion } from 'lucide-react'
import { Button } from '@/components/ui/Button'

export function NotFound() {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gray-50 dark:bg-slate-900">
      <div className="text-center">
        <FileQuestion className="w-24 h-24 text-gray-400 dark:text-slate-600 mx-auto mb-6" />
        <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-4">
          404
        </h1>
        <p className="text-gray-600 dark:text-slate-400 mb-8">
          Página não encontrada
        </p>
        <Button onClick={() => navigate('/')}>
          Voltar para o início
        </Button>
      </div>
    </div>
  )
}
