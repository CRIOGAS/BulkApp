package com.criogas.bulkllenadoentregaapp.rest;

import com.criogas.bulkllenadoentregaapp.model.OrdenVenta;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestSalesOrder {

    public OrdenVenta GetByID(String token, int prmOrderNum) {
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
