package com.criogas.bulkllenadoentregaapp.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author YairG
 */
public class RestCredit {
    public String liberaCreditoOv(String prmCveCliente, int prmOV) {
        RestApiPipas restApi = new RestApiPipas();
        String token = restApi.getToken();
        if (token.contains("ERROR")) {
            return token;
        }

        String dsGetOrders = GetOrders(token, prmCveCliente);
        if (dsGetOrders.contains("ERROR")) {
            return dsGetOrders;
        }

        String dsGetOrderJson = GetOrderJson(dsGetOrders, prmOV);
        if (dsGetOrderJson.contains("ERROR")) {
            return dsGetOrderJson;
        }

        String dsChangeOrderCreditHold = ChangeOrderCreditHold(token, dsGetOrderJson);
        if (dsChangeOrderCreditHold.contains("ERROR")) {
            return dsChangeOrderCreditHold;
        }

        String dsUpdateCMOrderHed = UpdateCMOrderHed(token, dsChangeOrderCreditHold);
        if (dsUpdateCMOrderHed.contains("ERROR")) {
            return dsUpdateCMOrderHed;
        }

        return "OK";
    }

    private String GetOrders(String token, String prmCveCliente) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CreditManagerSvc/GetOrders";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ipCustID", prmCveCliente);

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/returnObj");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR GetOrders: " + response.getBody();
            }

            return ds.toString();
        } catch (Exception ex) {
            return "ERROR GetOrders: " + ex.getMessage();
        }
    }

    private String GetOrderJson(String prmArrDS, int prmOV) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmArrDS).get("CMOrderHed");

            for(JsonNode objNode : dsNode) {
                if(objNode.get("OrderNum").asInt() == prmOV) {
                    return objNode.toString();
                }
            }

            return "";
        } catch(Exception ex) {
            return "ERROR GetOrderJson: " + ex.getMessage();
        }
    }

    private String ChangeOrderCreditHold(String token, String prmDS) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CreditManagerSvc/ChangeOrderCreditHold";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDS);

            ObjectNode objParentDs = ((ObjectNode)dsNode);
            objParentDs.put("CreditHold", false);

            dsNode = (JsonNode)objParentDs;

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(dsNode));

            ObjectNode CMOrderNode = mapper.createObjectNode();
            CMOrderNode.set("CMOrderHed", arrayNode);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(CMOrderNode)));

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/CMOrderHed");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR ChangeOrderCreditHold: " + response.getBody();
            }

            return ds.get(0).toString();
        } catch (Exception ex) {
            return "ERROR ChangeOrderCreditHold: " + ex.getMessage();
        }
    }

    private String UpdateCMOrderHed(String token, String prmDS) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CreditManagerSvc/UpdateCMOrderHed";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDS);

            ObjectNode objParentDs = ((ObjectNode)dsNode);
            objParentDs.put("CreditOverride", true);
            objParentDs.put("RowMod", "U");

            int custNum = objParentDs.get("CustNum").asInt();

            dsNode = (JsonNode)objParentDs;

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(dsNode));

            ObjectNode CMOrderNode = mapper.createObjectNode();
            CMOrderNode.set("CMOrderHed", arrayNode);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ipCustNum", custNum);
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(CMOrderNode)));

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/CMOrderHed");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR UpdateCMOrderHed: " + response.getBody();
            }

            return ds.get(0).toString();
        } catch (Exception ex) {
            return "ERROR UpdateCMOrderHed: " + ex.getMessage();
        }
    }
}
