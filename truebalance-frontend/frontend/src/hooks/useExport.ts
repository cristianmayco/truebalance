import { useState } from 'react';
import * as XLSX from 'xlsx';

export type ExportFormat = 'csv' | 'xlsx';

interface ExportOptions {
  filename: string;
  format: ExportFormat;
  sheetName?: string;
}

export function useExport() {
  const [isExporting, setIsExporting] = useState(false);

  const exportToCSV = (data: any[], filename: string) => {
    setIsExporting(true);

    try {
      // Create worksheet from data
      const worksheet = XLSX.utils.json_to_sheet(data);

      // Create workbook
      const workbook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(workbook, worksheet, 'Data');

      // Generate CSV
      const csv = XLSX.utils.sheet_to_csv(worksheet);

      // Create blob and download
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      const url = URL.createObjectURL(blob);

      link.setAttribute('href', url);
      link.setAttribute('download', `${filename}.csv`);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      URL.revokeObjectURL(url);

      return true;
    } catch (error) {
      console.error('Error exporting to CSV:', error);
      return false;
    } finally {
      setIsExporting(false);
    }
  };

  const exportToExcel = (
    data: any[],
    filename: string,
    sheetName: string = 'Data'
  ) => {
    setIsExporting(true);

    try {
      // Create worksheet from data
      const worksheet = XLSX.utils.json_to_sheet(data);

      // Auto-size columns
      const maxWidth = 50;
      const columnWidths = Object.keys(data[0] || {}).map((key) => {
        const maxLength = Math.max(
          key.length,
          ...data.map((row) => String(row[key] || '').length)
        );
        return { wch: Math.min(maxLength + 2, maxWidth) };
      });

      worksheet['!cols'] = columnWidths;

      // Create workbook
      const workbook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(workbook, worksheet, sheetName);

      // Generate Excel file
      XLSX.writeFile(workbook, `${filename}.xlsx`);

      return true;
    } catch (error) {
      console.error('Error exporting to Excel:', error);
      return false;
    } finally {
      setIsExporting(false);
    }
  };

  const exportMultiSheet = (
    sheets: Array<{ name: string; data: any[] }>,
    filename: string
  ) => {
    setIsExporting(true);

    try {
      // Create workbook
      const workbook = XLSX.utils.book_new();

      // Add each sheet
      sheets.forEach((sheet) => {
        const worksheet = XLSX.utils.json_to_sheet(sheet.data);

        // Auto-size columns
        const maxWidth = 50;
        const columnWidths = Object.keys(sheet.data[0] || {}).map((key) => {
          const maxLength = Math.max(
            key.length,
            ...sheet.data.map((row) => String(row[key] || '').length)
          );
          return { wch: Math.min(maxLength + 2, maxWidth) };
        });

        worksheet['!cols'] = columnWidths;

        XLSX.utils.book_append_sheet(workbook, worksheet, sheet.name);
      });

      // Generate Excel file
      XLSX.writeFile(workbook, `${filename}.xlsx`);

      return true;
    } catch (error) {
      console.error('Error exporting multi-sheet Excel:', error);
      return false;
    } finally {
      setIsExporting(false);
    }
  };

  const exportData = (
    data: any[],
    options: ExportOptions
  ): boolean => {
    if (!data || data.length === 0) {
      console.warn('No data to export');
      return false;
    }

    const { filename, format, sheetName = 'Data' } = options;

    if (format === 'csv') {
      return exportToCSV(data, filename);
    } else {
      return exportToExcel(data, filename, sheetName);
    }
  };

  return {
    isExporting,
    exportToCSV,
    exportToExcel,
    exportMultiSheet,
    exportData,
  };
}
