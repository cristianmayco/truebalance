#!/bin/bash

# Script para remover todas as referências de importação dos controllers

cd /Users/cristian/IdeaProjects/truebalance/truebalance-backend

# Remover imports de InvoiceController
sed -i '' '/import.*InvoiceBulkImportRequestDTO/d' src/main/java/com/truebalance/truebalance/application/controller/InvoiceController.java
sed -i '' '/import.*InvoiceImportResultDTO/d' src/main/java/com/truebalance/truebalance/application/controller/InvoiceController.java
sed -i '' '/import.*FileImportService/d' src/main/java/com/truebalance/truebalance/application/controller/InvoiceController.java
sed -i '' '/import.*ImportInvoicesInBulk/d' src/main/java/com/truebalance/truebalance/application/controller/InvoiceController.java

# Remover imports de CreditCardController
sed -i '' '/import.*CreditCardBulkImportRequestDTO/d' src/main/java/com/truebalance/truebalance/application/controller/CreditCardController.java
sed -i '' '/import.*CreditCardImportResultDTO/d' src/main/java/com/truebalance/truebalance/application/controller/CreditCardController.java
sed -i '' '/import.*FileImportService/d' src/main/java/com/truebalance/truebalance/application/controller/CreditCardController.java
sed -i '' '/import.*ImportCreditCardsInBulk/d' src/main/java/com/truebalance/truebalance/application/controller/CreditCardController.java

echo "Imports removidos com sucesso!"
