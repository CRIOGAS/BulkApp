package com.criogas.bulkllenadoentregaapp.rest;

import com.criogas.bulkllenadoentregaapp.model.OrdenVenta;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class RestSalesOrder {

    /**
     * Obtiene los datos de la orden de venta
     * @param prmOrderNum
     * @return OrdenVenta o null
     */
    public OrdenVenta GetByID(int prmOrderNum) {
        try {
            RestApiPipas api = new RestApiPipas();
            String token = api.getToken();
            if(token.contains("ERROR")) {
                return null;
            }

            String dsGetByID = GetByID(token, prmOrderNum);
            OrdenVenta ov = GetByID_ToObject(dsGetByID);
            return ov;
        } catch(Exception ex) {
            return null;
        }
    }

    /**
     * Actualiza la revisión utilizada en al orden de venta para decidir la ingeniería a utilizar (UP1 O UP2)
     * @param prmOrderNum
     * @param prmRevision
     * @return
     */
    public String actualizaRevision(int prmOrderNum, String prmRevision) {
        RestApiPipas api = new RestApiPipas();
        String token = api.getToken();
        if(token.contains("ERROR")) {
            return token;
        }

        String dsGetByID = GetByID(token, prmOrderNum);
        String dsUpdateRevision = updateRevision(token, dsGetByID, prmRevision);
        return dsUpdateRevision;
    }

    private String updateRevision(String token, String prmDs, String prmNumRevision) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.SalesOrderSvc/Update";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(prmDs);
            JsonNode dsDetail = root.at("/returnObj/OrderDtl");
            Iterator<Map.Entry<String, JsonNode>> fields = dsDetail.get(0).fields();

            ObjectNode objNode = mapper.createObjectNode();

            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("RevisionNum")) {
                    objNode.put(fName, prmNumRevision);
                } else if(fName.equals("RowMod")) {
                    objNode.put(fName, "U");
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeOrderDtl = mapper.createObjectNode();
            nodeOrderDtl.set("OrderDtl", arrayNode);

            ObjectNode objDs = mapper.createObjectNode();
            objDs.set("ds", nodeOrderDtl);

            HttpEntity<String> request = new HttpEntity<>(objDs.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode rootResponse = mapper.readTree(response.getBody());

            if(rootResponse.toString().contains("28701")) {
                return "OK";
            } else {
                return rootResponse.toString();
            }
        } catch(Exception ex) {
            return "ERROR: updateRevision " + ex.getMessage();
        }
    }

    private String GetByID(String token, int prmOrderNum) {
        try {

            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.SalesOrderSvc/GetByID?orderNum=" + prmOrderNum;

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
            return "ERROR: " + ex.getMessage();
        }
    }

    private OrdenVenta GetByID_ToObject(String prmDs) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(prmDs);
            JsonNode ovHead = root.at("/returnObj/OrderHed").get(0);
            JsonNode ovDetail = root.at("/returnObj/OrderDtl").get(0);

            OrdenVenta ov = new OrdenVenta();
            ov.setFolio(ovHead.get("OrderNum").asInt());
            ov.setCliente(ovHead.get("BTCustNumName").asText());
            ov.setProducto(ovDetail.get("PartNum").asText());
            ov.setDesccorta(ovDetail.get("LineDesc").asText());
            ov.setQty(ovDetail.get("SellingQuantity").asDouble());
            ov.setPipa(ovHead.get("CAMION_c").asText());
            ov.setUdm(ovDetail.get("SalesUM").asText());
            ov.setRevision(ovDetail.get("RevisionNum").asText());

            return ov;

        } catch(Exception ex) {
            return null;
        }
    }

}
