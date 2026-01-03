import { Component, ErrorInfo, ReactNode } from 'react'
import { AlertTriangle } from 'lucide-react'
import { Button } from './ui/Button'

interface Props {
  children: ReactNode
}

interface State {
  hasError: boolean
  error: Error | null
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo)
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center p-4 bg-gray-50 dark:bg-slate-900">
          <div className="max-w-md w-full bg-white dark:bg-slate-800 rounded-xl shadow-lg p-8 text-center">
            <div className="mb-4 flex justify-center">
              <AlertTriangle className="w-16 h-16 text-error" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">
              Algo deu errado
            </h2>
            <p className="text-gray-600 dark:text-slate-400 mb-6">
              {this.state.error?.message || 'Ocorreu um erro inesperado'}
            </p>
            <Button
              onClick={() => {
                this.setState({ hasError: false, error: null })
                window.location.href = '/'
              }}
            >
              Voltar para o início
            </Button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}
