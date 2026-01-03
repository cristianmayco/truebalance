import { useEffect, useState } from 'react';

/**
 * Hook que retorna um valor debounced (atrasado)
 * Útil para evitar execuções desnecessárias em campos de busca
 *
 * @param value - Valor a ser debounced
 * @param delay - Delay em milissegundos (padrão: 500ms)
 * @returns Valor debounced
 *
 * @example
 * const [searchTerm, setSearchTerm] = useState('')
 * const debouncedSearchTerm = useDebounce(searchTerm, 500)
 *
 * useEffect(() => {
 *   // Só executa após 500ms sem mudanças
 *   if (debouncedSearchTerm) {
 *     fetchSearchResults(debouncedSearchTerm)
 *   }
 * }, [debouncedSearchTerm])
 */
export function useDebounce<T>(value: T, delay: number = 500): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    // Set timeout para atualizar o valor debounced após o delay
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    // Cleanup: cancela o timeout se value mudar antes do delay
    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

/**
 * Hook que retorna uma função debounced
 *
 * @param callback - Função a ser debounced
 * @param delay - Delay em milissegundos (padrão: 500ms)
 * @returns Função debounced
 *
 * @example
 * const handleSearch = useDebouncedCallback((searchTerm: string) => {
 *   fetchSearchResults(searchTerm)
 * }, 500)
 *
 * <input onChange={(e) => handleSearch(e.target.value)} />
 */
export function useDebouncedCallback<T extends (...args: any[]) => any>(
  callback: T,
  delay: number = 500
): (...args: Parameters<T>) => void {
  let timeoutId: ReturnType<typeof setTimeout> | undefined;

  useEffect(() => {
    return () => {
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
    };
  }, []);

  return (...args: Parameters<T>) => {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
    timeoutId = setTimeout(() => {
      callback(...args);
    }, delay);
  };
}
