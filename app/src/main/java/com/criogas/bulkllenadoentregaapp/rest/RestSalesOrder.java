package com.criogas.bulkllenadoentregaapp.rest;

import com.criogas.bulkllenadoentregaapp.model.OrdenVenta;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.JSONObject;
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
     * Regresa True si ya tiene un empaque. Falso si esta libre la OV
     * @param prmOrderNum
     * @return
     */
    public boolean existeEmpaque(int prmOrderNum) {
        RestApiPipas api = new RestApiPipas();
        String token = api.getToken();

        return getNumEmpaques(token, prmOrderNum);
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

    public String actualizaQtyEntregada(int prmOrderNum, double qtyEntregada) {
        RestApiPipas restApi = new RestApiPipas();
        String token = restApi.getToken();
        if(token.contains("ERROR")) {
            return token;
        }

        String dsGetByID = GetByID(token, prmOrderNum);

        String dsMasterUpdate = MasterUpdate(token, prmOrderNum, dsGetByID, qtyEntregada);
        if(dsMasterUpdate.contains("ERROR")) {
            return dsMasterUpdate;
        }
        return dsMasterUpdate;
    }

    public String attachFile(int prmOv, byte[] arrByteFile, String fileName) {
        RestApiPipas restApi = new RestApiPipas();
        String token = restApi.getToken();
        if(token.contains("ERROR")) {
            return token;
        }

        String dsUploadFileToDocTypeStorage = UploadFileToDocTypeStorage(token, arrByteFile, fileName);
        if(dsUploadFileToDocTypeStorage.contains("ERROR")) {
            return dsUploadFileToDocTypeStorage;
        }

        String dsGetByID = GetByID(token, prmOv);
        if(dsGetByID.contains("ERROR")) {
            return dsGetByID;
        }

        String dsGetNewOrderHedAttch = GetNewOrderHedAttch(token, dsGetByID, prmOv);
        if(dsGetNewOrderHedAttch.contains("ERROR")) {
            return dsGetNewOrderHedAttch;
        }

        String dsMasterUpdateAttach = MasterUpdateAttach(token, dsGetNewOrderHedAttch, prmOv, fileName);
        if(dsMasterUpdateAttach.contains("ERROR")) {
            return dsMasterUpdateAttach;
        }

        return dsMasterUpdateAttach;
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

    /**
     *
     * @param token
     * @param prmOrderNum
     * @return
     */
    private String GetByID(String token, int prmOrderNum) {
        try {

            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/BaqSvc/C_SALES_ORDER_BULK/Data?$filter=OrderHed_OrderNum eq " + prmOrderNum;

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

    /**
     *
     * @param prmDs
     * @return
     */
    private OrdenVenta GetByID_ToObject(String prmDs) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(prmDs);
            JsonNode ovHead = root.at("/value").get(0);
            //JsonNode ovDetail = root.at("/returnObj/OrderDtl").get(0);

            OrdenVenta ov = new OrdenVenta();
            ov.setFolio(ovHead.get("OrderHed_OrderNum").asInt());
            ov.setCliente(ovHead.get("Customer_CustID").asText());
            ov.setCliente(ovHead.get("Customer_Name").asText());
            ov.setProducto(ovHead.get("OrderDtl_PartNum").asText());
            ov.setDesccorta(ovHead.get("OrderDtl_LineDesc").asText());
            ov.setQty(ovHead.get("OrderDtl_SellingQuantity").asDouble());
            ov.setPipa(ovHead.get("OrderHed_CAMION_c").asText());
            ov.setUdm(ovHead.get("OrderDtl_SalesUM").asText());
            ov.setRevision(ovHead.get("OrderDtl_RevisionNum").asText());
            ov.setEmail1(ovHead.get("Customer_EMailAddress").asText());
            ov.setEmail2(ovHead.get("Customer_CustURL").asText());

            return ov;

        } catch(Exception ex) {
            return null;
        }
    }

    private boolean getNumEmpaques(String token, int prmOrderNum) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/BaqSvc/C_TIENE_EMPAQUE/Data?$filter=ShipDtl_OrderNum eq " + prmOrderNum;

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

            if(root.toString().contains("[]"))
                return false;
            else
                return true;

        } catch(Exception ex) {
            return true;
        }
    }

    private String MasterUpdate(String token, int prmOrderNum, String prmDs, double qty) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.SalesOrderSvc/MasterUpdate";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(prmDs);
            JsonNode dsDetail = root.at("/returnObj/OrderDtl");
            JsonNode dsTaxConnectStatus = root.at("/returnObj/TaxConnectStatus");

            Iterator<Map.Entry<String, JsonNode>> fields = dsDetail.get(0).fields();
            Iterator<Map.Entry<String, JsonNode>> fieldsTaxConnect = dsTaxConnectStatus.get(0).fields();

            ObjectNode objNode = mapper.createObjectNode();

            int custNum = 0;

            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("SellingQuantity")) {
                    objNode.put(fName, qty);
                } else if(fName.equals("RowMod")) {
                    objNode.put(fName, "U");
                } else {
                    objNode.set(fName, fValue);
                    if(fName.equals("CustNum")) {
                        custNum = fValue.asInt();
                    }
                }
            }

            ObjectNode objNodeTaxConnect = mapper.createObjectNode();
            while(fieldsTaxConnect.hasNext()) {
                Map.Entry<String, JsonNode> field = fieldsTaxConnect.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("ETCOffline")) {
                    objNodeTaxConnect.put(fName, true);
                } else if(fName.equals("RowMod")) {
                    objNodeTaxConnect.put(fName, "U");
                } else {
                    objNodeTaxConnect.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ArrayNode arrayNodeTaxConnect = mapper.createArrayNode();
            arrayNodeTaxConnect.addAll(Arrays.asList(objNodeTaxConnect));

            ObjectNode nodeOrderDtl = mapper.createObjectNode();
            nodeOrderDtl.set("OrderDtl", arrayNode);
            nodeOrderDtl.set("TaxConnectStatus", arrayNodeTaxConnect);

            ObjectNode objDs = mapper.createObjectNode();
            objDs.put("lCheckForOrderChangedMsg", false);
            objDs.put("lcheckForResponse", false);
            objDs.put("cTableName", "OrderDtl");
            objDs.put("iCustNum", custNum);
            objDs.put("iOrderNum", prmOrderNum);
            objDs.put("lweLicensed", true);

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
            return "ERROR: MasterUpdate " + ex.getMessage();
        }
    }

    private String UploadFileToDocTypeStorage(String token, byte[] prmArrByte, String prmFileName) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Ice.BO.AttachmentSvc/UploadFileToDocTypeStorage";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            byte[] arrByte = prmArrByte;

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode objDs = mapper.createObjectNode();
            objDs.put("docTypeID", "CRIO_DOC");
            objDs.put("parentTable", "OrderHed");
            objDs.put("fileName", prmFileName);
            objDs.put("data", arrByte);
            objDs.put("metadata", "");

            HttpEntity<String> request = new HttpEntity<>(objDs.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            return ds.toString();

        } catch(Exception ex) {
            return "ERROR UploadFile " + ex.getMessage();
        }
    }

    private String GetNewOrderHedAttch(String token, String prmDs, int prmOV) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.SalesOrderSvc/GetNewOrderHedAttch";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDs).at("/returnObj");

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dsNode)));
            jsonPrm.put("orderNum", prmOV);

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            return root.toString();
        } catch(Exception ex) {
            return "ERROR GetNewOrderHedAttch " + ex.getMessage();
        }
    }

    private String MasterUpdateAttach(String token, String prmDs, int prmOrderNum, String fileName) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.SalesOrderSvc/MasterUpdate";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(prmDs);
            JsonNode dsHead = root.at("/parameters/ds/OrderHed");
            JsonNode dsDetail = root.at("/parameters/ds/OrderDtl");
            JsonNode dsOrderHedAttch = root.at("/parameters/ds/OrderHedAttch");
            JsonNode dsOHOrderMsc = root.at("/parameters/ds/OHOrderMsc");
            JsonNode dsOrderDtlAttch = root.at("/parameters/ds/OrderDtlAttch");
            JsonNode dsOrderMsc = root.at("/parameters/ds/OrderMsc");
            JsonNode dsOrderRel = root.at("/parameters/ds/OrderRel");
            JsonNode dsOrderRelTax = root.at("/parameters/ds/OrderRelTax");
            JsonNode dsOrderHedUPS = root.at("/parameters/ds/OrderHedUPS");
            JsonNode dsOrderRepComm = root.at("/parameters/ds/OrderRepComm");
            JsonNode dsOrderSched = root.at("/parameters/ds/OrderSched");
            JsonNode dsHedTaxSum = root.at("/parameters/ds/HedTaxSum");
            JsonNode dsOrderHist = root.at("/parameters/ds/OrderHist");
            JsonNode dsPartSubs = root.at("/parameters/ds/PartSubs");
            JsonNode dsSelectedSerialNumbers = root.at("/parameters/ds/SelectedSerialNumbers");
            JsonNode dsSNFormat = root.at("/parameters/ds/SNFormat");
            JsonNode dsTaxConnectStatus = root.at("/parameters/ds/TaxConnectStatus");
            JsonNode dsExtensionTables = root.at("/parameters/ds/ExtensionTables");

            Iterator<Map.Entry<String, JsonNode>> fieldsAttach = dsOrderHedAttch.get(0).fields();
            Iterator<Map.Entry<String, JsonNode>> fieldsTaxConnect = dsTaxConnectStatus.get(0).fields();

            ObjectNode objNode = mapper.createObjectNode();

            int custNum = 0;

            while(fieldsAttach.hasNext()) {
                Map.Entry<String, JsonNode> field = fieldsAttach.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("DrawDesc")) {
                    objNode.put(fName, prmOrderNum + "_Bascula");
                } else if(fName.equals("FileName")) {
                    objNode.put(fName, "/28701/CRIO_DOC/OrderHed/" + fileName);
                } else if(fName.equals("DocTypeID")) {
                    objNode.put(fName, "CRIO_DOC");
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ObjectNode objNodeTaxConnect = mapper.createObjectNode();
            while(fieldsTaxConnect.hasNext()) {
                Map.Entry<String, JsonNode> field = fieldsTaxConnect.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("ETCOffline")) {
                    objNodeTaxConnect.put(fName, true);
                } else if(fName.equals("RowMod")) {
                    objNodeTaxConnect.put(fName, "U");
                } else {
                    objNodeTaxConnect.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ArrayNode arrayNodeTaxConnect = mapper.createArrayNode();
            arrayNodeTaxConnect.addAll(Arrays.asList(objNodeTaxConnect));

            ObjectNode nodeOrderDtl = mapper.createObjectNode();
            nodeOrderDtl.set("OrderHed", dsHead);
            nodeOrderDtl.set("OrderDtl", dsDetail);
            nodeOrderDtl.set("TaxConnectStatus", arrayNodeTaxConnect);
            nodeOrderDtl.set("OrderHedAttch", arrayNode);
            nodeOrderDtl.set("OHOrderMsc", dsOHOrderMsc);
            nodeOrderDtl.set("OrderDtlAttch", dsOrderDtlAttch);
            nodeOrderDtl.set("OrderMsc", dsOrderMsc);
            nodeOrderDtl.set("OrderRel", dsOrderRel);
            nodeOrderDtl.set("OrderRelTax", dsOrderRelTax);
            nodeOrderDtl.set("OrderHedUPS", dsOrderHedUPS);
            nodeOrderDtl.set("OrderRepComm", dsOrderRepComm);
            nodeOrderDtl.set("OrderSched", dsOrderSched);
            nodeOrderDtl.set("HedTaxSum", dsHedTaxSum);
            nodeOrderDtl.set("OrderHist", dsOrderHist);
            nodeOrderDtl.set("PartSubs", dsPartSubs);
            nodeOrderDtl.set("SelectedSerialNumbers", dsSelectedSerialNumbers);
            nodeOrderDtl.set("SNFormat", dsSNFormat);
            nodeOrderDtl.set("ExtensionTables", dsExtensionTables);

            ObjectNode objDs = mapper.createObjectNode();
            objDs.put("lCheckForOrderChangedMsg", true);
            objDs.put("lcheckForResponse", false);
            objDs.put("cTableName", "AutoAttachOrderHed");
            objDs.put("iCustNum", custNum);
            objDs.put("iOrderNum", prmOrderNum);
            objDs.put("lweLicensed", true);

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
            return "ERROR: MasterUpdate " + ex.getMessage();
        }
    }
}
