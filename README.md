# Access Azure SQL Database in Java using Azure Key Vault with Managed Service Identity (MSI)
This applicaiton runs on Azure App Service which supports Managed Service Identity (MSI). It illustrates following concepts -

1.	Addressing unavailability of Java Package for MSI (.Net has [Microsoft.Azure.Services.AppAuthentication nuget package](https://www.nuget.org/packages/Microsoft.Azure.Services.AppAuthentication))

2. When MSI is enabled for App Service, a REST endpoint is created to obtain token. There are 2 environment variables, MSI_ENDPOINT and MSI_SECRET, that are also added in App Service

3. Because these environment variables work locally in App Service, code needs to be writen locally but tested by deploying and running it remotely on App Service. Azure Plugin for Eclipse eases deploying to Azure.

4. Code uses MSI to access Key Vault to fetch SQL Connectionstring. Connectionstring uses SQL authentication with username and password. But it is safely stored in Azure Key Vault. 

5. SQL Server also supports access token based authentication. However, API version parameter in REST call to its endpoint fails at the moment. There is function `getMSIToeknFromAppServiceForSQL` that can be corrected with right API version when it becomes available. 

## More Information
* [How to use Azure Managed Service Identity in App Service](https://docs.microsoft.com/en-us/azure/app-service/app-service-managed-service-identity)
* [Secure SQL Database connection with managed service identity](https://docs.microsoft.com/en-us/azure/app-service/app-service-web-tutorial-connect-msi)
* [Services that support Managed Service Identity](https://docs.microsoft.com/en-us/azure/active-directory/managed-service-identity/services-support-msi)
* [Use a Windows VM Managed Service Identity (MSI) to access Azure SQL](https://docs.microsoft.com/en-us/azure/active-directory/managed-service-identity/tutorial-windows-vm-access-sql)
