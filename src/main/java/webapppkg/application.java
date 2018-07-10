package webapppkg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

@SpringBootApplication
@RestController
public class application {
	
	@Value("${MSI_ENDPOINT}")
	private String msiEndpoint;
	
	@Value("${MSI_SECRET}")
	private String msiSecret;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SpringApplication.run(application.class, args);
	}

	@RequestMapping("/")
	Map<String, String> home() {
		
		Map<String, String> output = new HashMap<String, String>();
		
		String kvAccessToekn = getMSIToeknFromAppService("https://vault.azure.net", "2017-09-01", msiSecret, msiEndpoint);
		
		String jdbcConnstr = getSecretfromKeyVault(
				kvAccessToekn, "https://maksh-key-vault.vault.azure.net/secrets/jdbcsqlconnstr", 
				"2016-10-01"
			);
		
		output = getData(jdbcConnstr);
		
		output.put("DBAccessToken", getMSIToeknFromAppServiceForSQL("https://database.windows.net", msiSecret, msiEndpoint));
		
		return output;
	}
	
	Map<String, String> getData(String connString) {
		
		SQLServerDataSource ds = new SQLServerDataSource();
		
		Map<String, String> cartList = new HashMap<String, String>();
		Connection connection = null;

        try {
                connection = DriverManager.getConnection(connString);

                ResultSet rs = connection.createStatement()
                		.executeQuery("SELECT CartItem, Count(CartItem) from shopping group by CartItem");
        	    while(rs.next()){
        	        	cartList.put(rs.getString(1), rs.getString(2));
        	        }
                 
        	    connection.close();
               
        }
        catch (Exception e) {
        	cartList.put("Exception Occurred: ", e.getMessage());
        }
		
		return cartList;
	}

	String getMSIToeknFromAppService(String targetURL, String apiVersion, String msiSecret, String msiEndpoint) {
		
		String accessToken = "";
		
		try {
			
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Secret", msiSecret);
		headers.set("Metadata", "true");
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
		 
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(msiEndpoint)
			    .queryParam("resource", targetURL)
			    .queryParam("api-version", apiVersion);

		ResponseEntity<String> result = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(result.getBody());
		String token = root.get("access_token").asText();
		
		accessToken = token.toString();
		
		}
		catch(Exception e) {
			accessToken = e.getMessage();
		}
		return accessToken;
	}
	
	String getMSIToeknFromAppServiceForSQL(String targetURL, String msiSecret, String msiEndpoint) {
		
		String accessToken = "";
		
		try {
			
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Secret", msiSecret);
		headers.set("Metadata", "true");
		//headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
		 
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(msiEndpoint)
				//Help Needed - What should be api-version? I get api-version invalid error when running this code.
				.queryParam("api-version", "2018-02-01")
			    .queryParam("resource", targetURL);

		ResponseEntity<String> result = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(result.getBody());
		String token = root.get("access_token").asText();
		
		accessToken = token.toString();
		
		}
		catch(HttpClientErrorException  e) {
			accessToken = e.getMessage() + e.getLocalizedMessage() + e.getSuppressed() + e + e.getResponseBodyAsString();
		}
		catch(Exception e1) {
			accessToken = accessToken + e1.getMessage() + e1.getLocalizedMessage() + e1.getSuppressed() + e1 ;
		}
		return accessToken;
	}

	String getSecretfromKeyVault(String accessToken, String keyVaultURL, String apiVersion) {
		String secretValue = "";
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + accessToken);
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			HttpEntity<?> httpEntity = new HttpEntity<Object>(headers);
			
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(keyVaultURL)
					.queryParam("api-version", apiVersion);

			ResponseEntity<String> result = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(result.getBody());
			secretValue = root.get("value").asText();
		}
		catch(Exception e) {
			secretValue = e.getMessage();
		}
		return secretValue;
	}
}

