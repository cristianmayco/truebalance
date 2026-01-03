import { useState } from 'react';
import { Download, FileSpreadsheet, FileText, Loader2 } from 'lucide-react';
import { useExport, ExportFormat } from '@/hooks/useExport';

interface ExportButtonProps {
  data: any[];
  filename: string;
  formatData?: (data: any[]) => any[];
  className?: string;
  variant?: 'default' | 'outline' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  showFormatSelector?: boolean;
}

export function ExportButton({
  data,
  filename,
  formatData,
  className = '',
  variant = 'outline',
  size = 'md',
  showFormatSelector = true,
}: ExportButtonProps) {
  const [showMenu, setShowMenu] = useState(false);
  const { isExporting, exportData } = useExport();

  const handleExport = (format: ExportFormat) => {
    const dataToExport = formatData ? formatData(data) : data;

    const success = exportData(dataToExport, {
      filename: `${filename}_${new Date().toISOString().split('T')[0]}`,
      format,
    });

    if (success) {
      setShowMenu(false);
    }
  };

  const variantClasses = {
    default: 'bg-primary-600 hover:bg-primary-700 text-white',
    outline:
      'border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-800 text-gray-700 dark:text-gray-300',
    ghost: 'hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-700 dark:text-gray-300',
  };

  const sizeClasses = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-3 text-base',
  };

  const baseClasses = `
    inline-flex items-center gap-2 font-medium rounded-lg
    transition-colors disabled:opacity-50 disabled:cursor-not-allowed
    ${variantClasses[variant]}
    ${sizeClasses[size]}
    ${className}
  `;

  if (!showFormatSelector) {
    return (
      <button
        onClick={() => handleExport('xlsx')}
        disabled={isExporting || !data || data.length === 0}
        className={baseClasses}
      >
        {isExporting ? (
          <Loader2 className="w-4 h-4 animate-spin" />
        ) : (
          <Download className="w-4 h-4" />
        )}
        {isExporting ? 'Exportando...' : 'Exportar'}
      </button>
    );
  }

  return (
    <div className="relative">
      <button
        onClick={() => setShowMenu(!showMenu)}
        disabled={isExporting || !data || data.length === 0}
        className={baseClasses}
      >
        {isExporting ? (
          <Loader2 className="w-4 h-4 animate-spin" />
        ) : (
          <Download className="w-4 h-4" />
        )}
        {isExporting ? 'Exportando...' : 'Exportar'}
      </button>

      {showMenu && !isExporting && (
        <>
          {/* Backdrop */}
          <div
            className="fixed inset-0 z-10"
            onClick={() => setShowMenu(false)}
          />

          {/* Menu */}
          <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-20">
            <div className="py-1">
              <button
                onClick={() => handleExport('xlsx')}
                className="w-full px-4 py-2 text-left text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2"
              >
                <FileSpreadsheet className="w-4 h-4 text-green-600" />
                <div>
                  <div className="font-medium">Excel (.xlsx)</div>
                  <div className="text-xs text-gray-500 dark:text-gray-400">
                    Recomendado
                  </div>
                </div>
              </button>

              <button
                onClick={() => handleExport('csv')}
                className="w-full px-4 py-2 text-left text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 flex items-center gap-2"
              >
                <FileText className="w-4 h-4 text-blue-600" />
                <div>
                  <div className="font-medium">CSV (.csv)</div>
                  <div className="text-xs text-gray-500 dark:text-gray-400">
                    Compatível
                  </div>
                </div>
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
