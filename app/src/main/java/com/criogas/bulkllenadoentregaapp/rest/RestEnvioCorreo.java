package com.criogas.bulkllenadoentregaapp.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestEnvioCorreo {
    public String enviaCorreo(int prmNumOV, String subject, String body, String prmCorreo, String prmCc, int prmReportStyle) {
        RestApiPipas restApi = new RestApiPipas();
        String token = restApi.getToken();
        if(token.contains("ERROR")) {
            return token;
        }

        String dsGetNewParameters = GetNewParameters(token);
        if(token.contains("ERROR")) {
            return dsGetNewParameters;
        }

        String dsSetPrintInventoryAttributes = SetPrintInventoryAttributes(token, dsGetNewParameters, prmNumOV);
        if(dsSetPrintInventoryAttributes.contains("ERROR")) {
            return dsSetPrintInventoryAttributes;
        }


        String dsSubmitToAgent = SubmitToAgent(token, dsSetPrintInventoryAttributes, prmReportStyle, subject, body, prmCorreo, prmCc);
        if(dsSubmitToAgent.contains("ERROR")) {
            return dsSubmitToAgent;
        }
        return dsSubmitToAgent;
    }

    private String GetNewParameters(String token) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.RPT.SalesOrderAckSvc/GetNewParameters";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            HttpEntity entity = new HttpEntity(headers);

            ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return root.toString();

        } catch(Exception ex) {
            return "ERROR GetNewParameters: " + ex.getMessage();
        }
    }

    private String SetPrintInventoryAttributes(String token, String prmDs, int prmOrderNum) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.RPT.SalesOrderAckSvc/SetPrintInventoryAttributes";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(prmDs);
            JsonNode dsSalesOrderAckParam = root.at("/returnObj/SalesOrderAckParam");
            JsonNode dsReportStyle = root.at("/returnObj/ReportStyle");

            Iterator<Map.Entry<String, JsonNode>> fields = dsSalesOrderAckParam.get(0).fields();

            ObjectNode objNode = mapper.createObjectNode();

            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("OrderNum")) {
                    objNode.put(fName, prmOrderNum);
                } else if(fName.equals("AutoAction")) {
                    objNode.put(fName, "PRINT");
                } else if(fName.equals("AgentID")) {
                    objNode.put(fName, "SystemTaskAgent");
                } else if(fName.equals("DateFormat")) {
                    objNode.put(fName, "dd/mm/yyyy");
                } else if(fName.equals("ReportCurrencyCode")) {
                    objNode.put(fName, "MXN");
                } else if(fName.equals("ReportCultureCode")) {
                    objNode.put(fName, "en-MX");
                } else if(fName.equals("SSRSRenderFormat")) {
                    objNode.put(fName, "PDF");
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodesSendOV = mapper.createObjectNode();
            nodesSendOV.set("SalesOrderAckParam", arrayNode);
            nodesSendOV.set("ReportStyle", dsReportStyle);

            ObjectNode objDs = mapper.createObjectNode();
            objDs.set("ds", nodesSendOV);

            HttpEntity<String> request = new HttpEntity<>(objDs.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            return ds.toString();

        } catch(Exception ex) {
            return "ERROR: SetPrintInventoryAttributes " + ex.getMessage();
        }
    }

    private String SubmitToAgent(String token, String prmDs, int prmReportStyle, String subject, String body, String prmCorreo, String prmCc) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.RPT.SalesOrderAckSvc/SubmitToAgent";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(prmDs);
            JsonNode dsSalesOrderAckParam = root.at("/SalesOrderAckParam");
            JsonNode dsReportStyle = root.at("/ReportStyle");

            Iterator<Map.Entry<String, JsonNode>> fields = dsSalesOrderAckParam.get(0).fields();

            ObjectNode objNode = mapper.createObjectNode();

            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("AutoAction")) {
                    objNode.put(fName, "SSRSPrint");
                } else if(fName.equals("ReportStyleNum")) {
                    objNode.put(fName, prmReportStyle);
                } else if(fName.equals("FaxSubject")) {
                    objNode.put(fName, subject);
                } else if(fName.equals("FaxTo")) {
                    objNode.put(fName, "");
                } else if(fName.equals("FaxNumber")) {
                    objNode.put(fName, "");
                } else if(fName.equals("EMailTo")) {
                    objNode.put(fName, prmCorreo);
                } else if(fName.equals("EMailCC")) {
                    objNode.put(fName, prmCc);
                } else if(fName.equals("EMailBCC")) {
                    objNode.put(fName, "");
                } else if(fName.equals("EMailBody")) {
                    objNode.put(fName, body);
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodesSendOV = mapper.createObjectNode();
            nodesSendOV.set("SalesOrderAckParam", arrayNode);
            nodesSendOV.set("ReportStyle", dsReportStyle);

            ObjectNode objDs = mapper.createObjectNode();
            objDs.put("agentID", "SystemTaskAgent");
            objDs.put("agentSchedNum", 0);
            objDs.put("agentTaskNum", 0);
            objDs.put("maintProgram", "Erp.UIRpt.SalesOrderAck");
            objDs.set("ds", nodesSendOV);

            HttpEntity<String> request = new HttpEntity<>(objDs.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            return ds.toString();

        } catch(Exception ex) {
            return "ERROR: SubmitToAgent " + ex.getMessage();
        }
    }
}
