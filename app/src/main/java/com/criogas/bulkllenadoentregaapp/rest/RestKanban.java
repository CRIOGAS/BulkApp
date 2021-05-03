package com.criogas.bulkllenadoentregaapp.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
public class RestKanban {

    /**
     * Regresa el num de trabajo
     * @param prmEmpleado - EMPLEADO FIJO 99999
     * @param prmPartNum
     * @param prmAlmacen
     * @param prmBin
     * @param prmRevision
     * @param prmCantidad
     * @return
     */
    public String ejecutaKanban(String prmEmpleado, String prmPartNum, String prmAlmacen, String prmBin, String prmRevision, double prmCantidad) {
        RestApiPipas RestApiPipas = new RestApiPipas();
        String token = RestApiPipas.getToken();
        if(token.contains("ERROR")) {
            return token;
        }

        String dsKanbanReceiptsGetNew = KanbanReceiptsGetNew(token);
        if(dsKanbanReceiptsGetNew.contains("ERROR")) {
            return dsKanbanReceiptsGetNew;
        }

        String dsChangeEmployee = ChangeEmployee(token, dsKanbanReceiptsGetNew, prmEmpleado);
        if(dsChangeEmployee.contains("ERROR")) {
            return dsChangeEmployee;
        }

        String dsChangePart = ChangePart(token, dsChangeEmployee, prmPartNum);
        if(dsChangePart.contains("ERROR")) {
            return dsChangePart;
        }

        String dsChangeWarehouse = ChangeWarehouse(token, dsChangePart, prmAlmacen);
        if(dsChangeWarehouse.contains("ERROR")) {
            return dsChangeWarehouse;
        }

        String dsChangeBin = ChangeBin(token, dsChangeWarehouse, prmBin);
        if(dsChangeBin.contains("ERROR")) {
            return dsChangeBin;
        }

        String dsChangeRevision = ChangeRevision(token, dsChangeBin, prmRevision);
        if(dsChangeRevision.contains("ERROR")) {
            return dsChangeRevision;
        }

        String dsPreProcessKanbanReceipts = PreProcessKanbanReceipts(token, dsChangeRevision, prmCantidad);
        if(dsPreProcessKanbanReceipts.contains("ERROR")) {
            return dsPreProcessKanbanReceipts;
        }

        String dsProcessKanbanReceipts = ProcessKanbanReceipts(token, dsPreProcessKanbanReceipts);
        if(dsProcessKanbanReceipts.contains("ERROR")) {
            return dsProcessKanbanReceipts;
        }

        return dsProcessKanbanReceipts;
    }

    private String KanbanReceiptsGetNew(String token) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.KanbanReceiptsSvc/KanbanReceiptsGetNew";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            HttpEntity<String> request = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/returnObj/KanbanReceipts");

            if(!ds.toString().contains("Company")) {
                return "ERROR KanbanReceiptsGetNew " + ds.toString();
            }
            return ds.toString();
        } catch(Exception ex) {
            return "ERROR KanbanReceiptsGetNew: " + ex.getMessage();
        }
    }

    private String ChangeEmployee(String token, String prmDS, String prmEmpleado) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.KanbanReceiptsSvc/ChangeEmployee";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDS);
            Iterator<Map.Entry<String, JsonNode>> fields = dsNode.get(0).fields();

            ObjectNode objNode = mapper.createObjectNode();
            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("EmployeeID")) {
                    objNode.put(fName, prmEmpleado);
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeKanbanReceipts = mapper.createObjectNode();
            nodeKanbanReceipts.set("KanbanReceipts", arrayNode);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeKanbanReceipts)));

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/KanbanReceipts");

            char ch = '"';
            if(root.toString().contains(ch + "lValidEmployee" + ch + ": false")) {
                return "ERROR ChangeEmployee: " + ds.toString();
            }

            return ds.toString();
        } catch(Exception ex) {
            return "ERROR ChangeEmployee: " + ex.getMessage();
        }
    }

    private String ChangePart(String token, String prmDS, String prmPart) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.KanbanReceiptsSvc/ChangePart";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDS);
            Iterator<Map.Entry<String, JsonNode>> fields = dsNode.get(0).fields();

            ObjectNode objNode = mapper.createObjectNode();
            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("PartNum")) {
                    objNode.put(fName, prmPart);
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeKanbanReceipts = mapper.createObjectNode();
            nodeKanbanReceipts.set("KanbanReceipts", arrayNode);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeKanbanReceipts)));
            jsonPrm.put("uomCode", "");

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/KanbanReceipts");

            char ch = '"';
            if(root.toString().contains(ch + "lValidPart" + ch + ": false")) {
                return "ERROR ChangePart: " + ds.toString();
            }

            return ds.toString();
        } catch(Exception ex) {
            return "ERROR ChangePart: " + ex.getMessage();
        }
    }

    private String ChangeWarehouse(String token, String prmDS, String prmAlmacen) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.KanbanReceiptsSvc/ChangeWarehouse";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDS);
            Iterator<Map.Entry<String, JsonNode>> fields = dsNode.get(0).fields();

            ObjectNode objNode = mapper.createObjectNode();
            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("WarehouseCode")) {
                    objNode.put(fName, prmAlmacen);
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeKanbanReceipts = mapper.createObjectNode();
            nodeKanbanReceipts.set("KanbanReceipts", arrayNode);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeKanbanReceipts)));

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/KanbanReceipts");

            char ch = '"';
            if(root.toString().contains(ch + "lValidWarehouse" + ch + ": false")) {
                return "ERROR ChangeWarehouse: " + ds.toString();
            }

            return ds.toString();
        } catch(Exception ex) {
            return "ERROR ChangeWarehouse: " + ex.getMessage();
        }
    }

    private String ChangeBin(String token, String prmDS, String prmBin) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.KanbanReceiptsSvc/ChangeBin";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDS);
            Iterator<Map.Entry<String, JsonNode>> fields = dsNode.get(0).fields();

            ObjectNode objNode = mapper.createObjectNode();
            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("BinNum")) {
                    objNode.put(fName, prmBin);
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeKanbanReceipts = mapper.createObjectNode();
            nodeKanbanReceipts.set("KanbanReceipts", arrayNode);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeKanbanReceipts)));

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/KanbanReceipts");

            char ch = '"';
            if(root.toString().contains(ch + "lValidBin" + ch + ": false")) {
                return "ERROR ChangeBin: " + ds.toString();
            }

            return ds.toString();
        } catch(Exception ex) {
            return "ERROR ChangeBin: " + ex.getMessage();
        }
    }

    private String ChangeRevision(String token, String prmDS, String prmRevision) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.KanbanReceiptsSvc/ChangeRevision";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDS);
            Iterator<Map.Entry<String, JsonNode>> fields = dsNode.get(0).fields();

            ObjectNode objNode = mapper.createObjectNode();
            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("RevisionNum")) {
                    objNode.put(fName, prmRevision);
                } else if(fName.equals("RowMod")) {
                    objNode.put(fName, "A");
                }
                else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeKanbanReceipts = mapper.createObjectNode();
            nodeKanbanReceipts.set("KanbanReceipts", arrayNode);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeKanbanReceipts)));

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/KanbanReceipts");

            char ch = '"';
            if(!root.toString().contains("Company")) {
                return "ERROR ChangeRevision: " + ds.toString();
            }

            return ds.toString();
        } catch(Exception ex) {
            return "ERROR ChangeRevision: " + ex.getMessage();
        }
    }

    private String PreProcessKanbanReceipts(String token, String prmDS, double prmCantidad) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.KanbanReceiptsSvc/PreProcessKanbanReceipts";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDS);
            Iterator<Map.Entry<String, JsonNode>> fields = dsNode.get(0).fields();

            ObjectNode objNode = mapper.createObjectNode();
            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("Quantity")) {
                    objNode.put(fName, prmCantidad);
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeKanbanReceipts = mapper.createObjectNode();
            nodeKanbanReceipts.set("KanbanReceipts", arrayNode);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeKanbanReceipts)));

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/KanbanReceipts");

            char ch = '"';
            if(!root.toString().contains("Company")) {
                return "ERROR PreProcessKanbanReceipts: " + ds.toString();
            }

            return ds.toString();
        } catch(Exception ex) {
            return "ERROR PreProcessKanbanReceipts: " + ex.getMessage();
        }
    }

    private String ProcessKanbanReceipts(String token, String prmDS) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.KanbanReceiptsSvc/ProcessKanbanReceipts";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDS);
            Iterator<Map.Entry<String, JsonNode>> fields = dsNode.get(0).fields();

            ObjectNode objNode = mapper.createObjectNode();
            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                objNode.set(fName, fValue);
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeKanbanReceipts = mapper.createObjectNode();
            nodeKanbanReceipts.set("KanbanReceipts", arrayNode);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeKanbanReceipts)));
            jsonPrm.put("dSerialNoQty", 0);

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/KanbanReceipts").get(0);

            char ch = '"';
            if(!root.toString().contains("Company")) {
                return "ERROR ProcessKanbanReceipts: " + ds.toString();
            }

            return ds.get("JobNum").toString();
        } catch(Exception ex) {
            return "ERROR ProcessKanbanReceipts: " + ex.getMessage();
        }
    }
}
