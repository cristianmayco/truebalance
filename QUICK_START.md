# Guia Rápido - TrueBalance

## 🚀 Início Rápido

### 1. Iniciar o Sistema

```bash
# Compilar e iniciar todos os serviços
docker compose build
docker compose up -d

# Verificar status
docker compose ps
```

### 2. Acessar a Aplicação

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## 📥 Importação de Dados

### Importação Unificada (Recomendado)

1. **No Dashboard**, clique em **"Importar Tudo"**
2. Selecione um arquivo:
   - **Excel (.xlsx ou .xls)**: Deve conter três abas (`Contas`, `Cartões de Crédito`, `Faturas`)
   - **CSV (.csv)**: Deve conter uma coluna `Tipo` indicando o tipo de entidade
3. Escolha a estratégia:
   - **Ignorar Duplicatas**: Registros duplicados serão pulados
   - **Criar Duplicatas**: Todos os registros serão importados
4. Clique em **"Importar"**

### Estrutura do Arquivo

#### Excel (XLS/XLSX)

#### Aba "Contas"
| Nome | Data | Valor Total | Número de Parcelas | Descrição | ID Cartão |
|------|------|-------------|-------------------|-----------|-----------|
| Compra Mercado | 10/01/2025 | 400,00 | 4 | Compras do mês | 1 |

#### Aba "Cartões de Crédito"
| Nome | Limite de Crédito | Dia de Fechamento | Dia de Vencimento | Permite Pagamento Parcial |
|------|-------------------|-------------------|-------------------|---------------------------|
| Nubank | 5000,00 | 10 | 17 | Sim |

#### Aba "Faturas"
| ID Cartão | Mês de Referência | Valor Total | Saldo Anterior | Fechada | Paga |
|-----------|-------------------|-------------|-----------------|---------|------|
| 1 | 01/2025 | 1200,00 | 0,00 | Não | Não |

#### CSV

O arquivo CSV pode ser importado de duas formas:

**1. CSV com coluna "Tipo" (para arquivos mistos):**

O arquivo CSV deve conter uma coluna **"Tipo"** na primeira coluna:

```csv
Tipo,Nome,Data,Valor Total,Número de Parcelas
Conta,Compra Mercado,10/01/2025,400,00,4
Conta,Conta de Luz,15/01/2025,150,00,1
Cartão,Nubank,5000,00,10,17
Fatura,1,01/2025,1200,00
```

**Valores aceitos na coluna Tipo:**
- Para contas: `Conta`, `Bill`, `Bills`
- Para cartões: `Cartão`, `Cartão de Crédito`, `Cartao`, `CreditCard`, `Credit_Card`
- Para faturas: `Fatura`, `Invoice`, `Invoices`

**2. CSV exportado (sem coluna "Tipo"):**

Arquivos CSV exportados pelo sistema podem ser importados diretamente. O sistema detecta automaticamente o tipo de entidade baseado nas colunas presentes:

```csv
"ID",Nome,Descrição,Data,Valor Total,Número de Parcelas,Valor da Parcela,Criado em,Atualizado em
49,aluguel,,,"R$ 800,00",1,"R$ 800,00",03/01/2026,03/01/2026
45,Starlink,,,"R$ 235,52",1,"R$ 235,52",03/01/2026,03/01/2026
```

**Nota:** Se a coluna "Data" estiver vazia, o sistema usará "Criado em" como fallback.

---

## 📤 Exportação de Dados

### Exportação Unificada

1. **No Dashboard**, clique em **"Exportar Tudo"**
2. Um arquivo Excel será baixado automaticamente
3. O arquivo contém todas as suas contas, cartões e faturas em abas separadas

### Via API

```bash
curl -X GET http://localhost:8080/unified/export -o backup.xlsx
```

---

## 📋 Formatos Suportados

### Importação
- ✅ Excel (.xlsx) - **Recomendado**
- ✅ Excel (.xls)
- ✅ CSV (.csv) - apenas para importação individual

### Exportação
- ✅ Excel (.xlsx) - formato padrão

---

## 🔍 Validações Importantes

### Datas
- Formato brasileiro: `dd/MM/yyyy` (ex: `10/01/2025`)
- Formato ISO também aceito: `yyyy-MM-dd`

### Valores Monetários
- Aceita: `R$ 400,00`, `400,00`, `400.00`
- Não aceita: `400,00,00` ou caracteres inválidos

### Mês de Referência (Faturas)
- Formato: `MM/yyyy` (ex: `01/2025`)
- Alternativo: `yyyy-MM` (ex: `2025-01`)

### Valores Booleanos
- Aceita: `true`, `sim`, `s`, `1`, `yes`, `y`
- Qualquer outro valor = `false`

---

## ⚠️ Limites

- **Tamanho máximo**: 5MB por arquivo
- **Registros máximos**: 1000 por tipo de entidade
- **Encoding**: UTF-8 (para CSV)

---

## 💡 Dicas

1. **Sempre faça backup antes de importar**
   - Use "Exportar Tudo" para criar backup
   - Mantenha backups regulares

2. **Teste com poucos registros primeiro**
   - Importe 5-10 registros para validar
   - Depois faça a importação completa

3. **Use "Ignorar Duplicatas" para segurança**
   - Evita criar registros duplicados acidentalmente
   - Use "Criar Duplicatas" apenas se necessário

4. **Verifique os resultados**
   - Analise o resumo após importação
   - Revise duplicatas e erros reportados

---

## 🆘 Problemas Comuns

### Arquivo não é importado
- ✅ Verifique se é XLS ou XLSX
- ✅ Confirme que tem menos de 5MB
- ✅ Valide que as abas têm os nomes corretos

### Erros de validação
- ✅ Revise a mensagem de erro
- ✅ Verifique a linha indicada
- ✅ Confirme formatos de data e valores

### Duplicatas não detectadas
- ✅ Verifique a estratégia escolhida
- ✅ Confirme os critérios de duplicata

---

## 📚 Documentação Completa

Para mais detalhes, consulte:
- **Guia Completo de Import/Export**: `truebalance-backend/docs/import-export-guide.md`
- **Documentação da API**: `truebalance-backend/docs/api-doc.md`
- **Swagger UI**: http://localhost:8080/swagger-ui.html
