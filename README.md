# Access Azure SQL Database from Java application running on Azure App Service using Azure Key Vault with Managed Service Identity (MSI)
This applicaiton runs on Azure App Service which supports Managed Service Identity (MSI). It illustrates following concepts -

1.	Addressing unavailability of **Java Maven Package for MSI** as of Jul-18. .Net has [Microsoft.Azure.Services.AppAuthentication nuget package](https://www.nuget.org/packages/Microsoft.Azure.Services.AppAuthentication) to easily use MSI based access. However, ADAL or MSAL Java libraries [do not support](https://docs.microsoft.com/en-us/azure/active-directory/managed-service-identity/known-issues) MSI yet.  

2. **Use of REST endpoint for MSI**. When MSI is enabled for App Service, a REST endpoint is created to obtain token. There are 2 environment variables, MSI_ENDPOINT and MSI_SECRET, that are also added in App Service

3. **Local development**. At the moment, MSI_ENDPOINT and MSI_SECRET environment variables work locally in App Service. So code needs to be writen locally but can be tested by deploying and running remotely on App Service. Azure Plugin for Eclipse eases deploying to Azure.

4. **Storing connectionstring in Azure Key Vault and accessing it**. Code uses MSI to access Key Vault to fetch SQL Connectionstring. Connectionstring uses SQL authentication with username and password. But it is safely stored in Azure Key Vault. 

5. **Using SQL Server Access Token authentication**. SQL Server  supports access token based authentication also. However, I couldn't get documented API version parameter `api-version=2018-02-01` in REST call to its endpoint working. There is function `getMSIToeknFromAppServiceForSQL` that can be corrected with right API version when it becomes available. 

## More Information
* [How to use Azure Managed Service Identity in App Service](https://docs.microsoft.com/en-us/azure/app-service/app-service-managed-service-identity)
* [Secure SQL Database connection with managed service identity](https://docs.microsoft.com/en-us/azure/app-service/app-service-web-tutorial-connect-msi)
* [Services that support Managed Service Identity](https://docs.microsoft.com/en-us/azure/active-directory/managed-service-identity/services-support-msi)
* [Use a Windows VM Managed Service Identity (MSI) to access Azure SQL](https://docs.microsoft.com/en-us/azure/active-directory/managed-service-identity/tutorial-windows-vm-access-sql)
