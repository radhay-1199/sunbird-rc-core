package io.opensaber.registry.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.node.ObjectNode;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.opensaber.registry.middleware.impl.RDFValidator;
import io.opensaber.registry.util.JsonKeys;
import io.opensaber.registry.middleware.util.Constants;

/**
 * 
 * @author jyotsna
 *
 */
public class RegistryControllerSteps {
	
	private static final String VALID_JSONLD1 = "school1.jsonld";
	private static final String VALID_JSONLD2 = "school2.jsonld";
	private static final String INVALID_LABEL_JSONLD = "invalid-label.jsonld";
	
	private RestTemplate restTemplate;
	private String baseUrl;
	private String jsonldString;
	private String file;
	
	
	@Before
	public void initializeData(){
		restTemplate = new RestTemplate();
		baseUrl = generateBaseUrl();
	}
	
	
	@Given("^First input data and base url are valid")
	public void jsonldData(){
		setJsonld(VALID_JSONLD1);
		assertNotNull(jsonldString);
		assertNotNull(restTemplate);
		assertNotNull(baseUrl);
	}
	
	@When("^Inserting first valid record into the registry")
	public void addEntity(){
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity entity = new HttpEntity(jsonldString,headers);
		ResponseEntity response = restTemplate.postForEntity(baseUrl+"/addEntity",
				entity,ObjectNode.class);
		ObjectNode obj = (ObjectNode)response.getBody();
		assertNotNull(obj);
		assertEquals(obj.get(JsonKeys.RESPONSE).asText(), JsonKeys.SUCCESS);
	}
	
	@Then("^Response for first valid record is (.*)")
	public void verifyResponse(String response){
		assertNotNull(response);
		assertTrue(response.contains(JsonKeys.SUCCESS));
	}

	@Given("^Valid duplicate data")
	public void jsonldDuplicateData(){
		setJsonld(VALID_JSONLD1);
		assertNotNull(jsonldString);
		assertNotNull(restTemplate);
		assertNotNull(baseUrl);
	}
	
	@When("^Inserting a duplicate record into the registry")
	public void addDuplicateEntity(){
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity entity = new HttpEntity(jsonldString,headers);
		ResponseEntity response = restTemplate.postForEntity(baseUrl+"/addEntity",
				entity,ObjectNode.class);
		ObjectNode obj = (ObjectNode)response.getBody();
		assertNotNull(obj);
		assertEquals(obj.get(JsonKeys.RESPONSE).asText(), Constants.DUPLICATE_RECORD_MESSAGE);
	}
	
	@Then("^Response for duplicate record is (.*)")
	public void verifyFailureResponse(String response){
		assertNotNull(response);
		assertTrue(response.contains(Constants.DUPLICATE_RECORD_MESSAGE));
	}
	
	@Given("^Second input data and base url are valid")
	public void newJsonldData(){
		setJsonld(VALID_JSONLD2);
		assertNotNull(jsonldString);
		assertNotNull(restTemplate);
		assertNotNull(baseUrl);
	}
	
	@When("^Inserting second valid record into the registry")
	public void addNewEntity(){
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity entity = new HttpEntity(jsonldString,headers);
		ResponseEntity response = restTemplate.postForEntity(baseUrl+"/addEntity",
				entity,ObjectNode.class);
		ObjectNode obj = (ObjectNode)response.getBody();
		assertNotNull(obj);
		assertEquals(obj.get(JsonKeys.RESPONSE).asText(), JsonKeys.SUCCESS);
	}
	
	@Then("^Response for second valid record is (.*)")
	public void verifyResponse2(String response){
		assertNotNull(response);
		assertTrue(response.contains(JsonKeys.SUCCESS));
	}
	
	@Given("^Base url is valid but input data has invalid root label")
	public void invalidJsonldData(){
		setJsonld(INVALID_LABEL_JSONLD);
		assertNotNull(jsonldString);
		assertNotNull(restTemplate);
		assertNotNull(baseUrl);
	}
	
	@When("^Inserting record with no label/invalid label into the registry")
	public void addInvalidEntity(){
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity entity = new HttpEntity(jsonldString,headers);
		ResponseEntity response = restTemplate.postForEntity(baseUrl+"/addEntity",
				entity,ObjectNode.class);
		ObjectNode obj = (ObjectNode)response.getBody();
		assertNotNull(obj);
		assertEquals(obj.get(JsonKeys.RESPONSE).asText(), Constants.FAILED_INSERTION_MESSAGE);
	}
	
	@Then("^Response for invalid record is (.*)")
	public void verifyFailureResponse2(String response){
		assertNotNull(response);
		assertTrue(response.contains(Constants.FAILED_INSERTION_MESSAGE));
	}

	private void setJsonld(String filename){

		try {
			String file = Paths.get(getPath(filename)).toString();
			/*Path filePath = Paths.get(file);
    		File jsonldFile = filePath.toFile();*/
			jsonldString = readFromFile(file);	
		} catch (Exception e) {
			jsonldString = StringUtils.EMPTY;
		}

	}

	private String readFromFile(String file) throws IOException,FileNotFoundException{
		BufferedReader reader = new BufferedReader(new FileReader (file));
		StringBuilder sb = new StringBuilder();
		try{
			String line = null;
			while((line = reader.readLine()) !=null){
				sb.append(line);
			}
		}catch(Exception e){
			return StringUtils.EMPTY;
		}finally{
			if(reader!=null){
				reader.close();
			}
		}
		return sb.toString();
	}

	private URI getPath(String file) throws URISyntaxException {
		return this.getClass().getClassLoader().getResource(file).toURI();
	}

	private String getJsonldString(){
		return "{\"@context\": {\"schema\": \"http://schema.org/\",\"opensaber\": \"http://open-saber.org/vocab/core/#\"},\"@type\": "
				+ "[\"schema:Person\",\"opensabre:Teacher\"],\"schema:identifier\": \"b6ad2941-fac3-4c72-94b7-eb638538f55f\",\"schema:image\": null,"
				+ "\"schema:nationality\": \"Indian\",\"schema:birthDate\": \"2011-12-06\",\"schema:name\": \"Marvin\",\"schema:gender\": \"male\","
				+ "\"schema:familyName\":\"Pande\",\"opensaber:languagesKnownISO\": [\"en\",\"hi\"]}";
	}
	
	private String getInvalidJsonldString(){
		return "{\"schema\": \"http://schema.org/\",\"opensaber\": \"http://open-saber.org/vocab/core/#\"},"
				+ "\"schema:identifier\": \"b6ad2941-fac3-4c72-94b7-eb638538f55f\",\"schema:image\": null,"
				+ "\"schema:nationality\": \"Indian\",\"schema:birthDate\": \"2011-12-06\",\"schema:name\": \"Marvin\",\"schema:gender\": \"male\","
				+ "\"schema:familyName\":\"Pande\",\"opensaber:languagesKnownISO\": [\"en\",\"hi\"]";
	}
	
	public String generateBaseUrl(){
		return "http://localhost:8080/registry";
	}

}
