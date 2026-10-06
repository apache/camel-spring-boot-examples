# Azure Examples — Test Account Setup

Some Azure examples require a real Azure Storage account (e.g. blob versioning).
See each example's own README for how to run it.

## Prerequisites

- [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) (`az`)
- An Azure subscription (a [free account](https://azure.microsoft.com/en-us/pricing/purchase-options/azure-account) works)

## Create a Test Storage Account

```bash
# Login
az login

# Create a resource group (delete it when done to avoid charges)
az group create --name camel-test-rg --location northeurope

# Create a General-purpose v2 storage account
az storage account create \
  --name cameltestblob \
  --resource-group camel-test-rg \
  --location northeurope \
  --sku Standard_LRS \
  --kind StorageV2

# Enable blob versioning (required for version ID operations)
az storage account blob-service-properties update \
  --account-name cameltestblob \
  --resource-group camel-test-rg \
  --enable-versioning true

# Get the access key
az storage account keys list \
  --account-name cameltestblob \
  --resource-group camel-test-rg \
  --query '[0].value' -o tsv
```

## Clean Up

Delete the resource group to remove all resources and stop billing:

```bash
az group delete --name camel-test-rg --yes
```
