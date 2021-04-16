package com.criogas.bulkllenadoentregaapp.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestApiPipas {
    public static String API_KEY = "Y9qkon2EBjLLs6fyVXDIYuoYTnppemGaHDMZJzdlayMCI";
    public static String URL_BASE = "https://centralusdtpilot21.epicorsaas.com/SaaS719Pilot/";
    //public static String URL_BASE = "https://centralusdtapp21.epicorsaas.com/SaaS719/";
    public static String USER = "serviceconnect";
    public static String PASSWORD = "Cr10G@s2020";

    public String getToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Accept", "application/json");

            HttpEntity<String> request = new HttpEntity<>(null, headers);
            String url = RestApiPipas.URL_BASE + "TokenResource.svc/?username=" + RestApiPipas.USER + "&password=" + RestApiPipas.PASSWORD;

            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode accessToken = root.path("AccessToken");

            return accessToken.asText();
        } catch(Exception ex) {
            return "ERROR getToken: " + ex.getMessage();
        }
    }
}
