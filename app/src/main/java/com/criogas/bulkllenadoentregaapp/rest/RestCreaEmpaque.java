package com.criogas.bulkllenadoentregaapp.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author YairG
 */
public class RestCreaEmpaque {

    public String creaEncabezadoEmpaque(int ov) {
        RestApiPipas restApi = new RestApiPipas();
        String token = restApi.getToken();
        if (token.contains("ERROR")) {
            return token;
        }

        String dsGetNewShipHead = GetNewShipHead(token);
        if (dsGetNewShipHead.contains("ERROR")) {
            return dsGetNewShipHead;
        }

        String dsGetHeadOrderInfo = GetHeadOrderInfo(token, dsGetNewShipHead, ov, "");
        if (dsGetHeadOrderInfo.contains("ERROR")) {
            return dsGetHeadOrderInfo;
        }

        String dsUpdateMaster = UpdateMaster(token, dsGetHeadOrderInfo, 0);
        if (dsUpdateMaster.contains("ERROR")) {
            return dsUpdateMaster;
        }

        String packNumReturn = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsObj = mapper.readTree(dsUpdateMaster);
            packNumReturn = dsObj.get("ShipHead").get(0).get("PackNum").toString();
        } catch(Exception ex) {
            packNumReturn = "ERROR CrearEncabezado " + ex.getMessage();
        }

        return packNumReturn;
    }

    /**
     *
     * @param prmEmpaque
     * @param prmOV
     * @param numLine - Siempre 1
     * @param prmCveProducto
     * @param prmWhHse - BAHIAORI
     * @param prmBin - PIPA01 - PIPA02 - ...
     * @param qty
     * @param prmUdm
     * @return
     */
    public String creaLinea(int prmEmpaque, int prmOV, int numLine, String prmCveProducto, String prmWhHse, String prmBin, double qty, String prmUdm) {
        RestApiPipas restApi = new RestApiPipas();
        String token = restApi.getToken();
        if (token.contains("ERROR")) {
            return token;
        }

        String dsGetByID = GetByID(token, prmEmpaque);
        if (dsGetByID.contains("ERROR")) {
            return dsGetByID;
        }

        String dsGetNewOrdrShipDtl = GetNewOrdrShipDtl(token, dsGetByID, prmEmpaque);
        if (dsGetNewOrdrShipDtl.contains("ERROR")) {
            return dsGetNewOrdrShipDtl;
        }

        String dsGetOrderInfo = GetOrderInfo(token, dsGetNewOrdrShipDtl, prmOV);
        if (dsGetOrderInfo.contains("ERROR")) {
            return dsGetOrderInfo;
        }

        String dsGetManifestInfo = GetManifestInfo(token, dsGetOrderInfo, prmEmpaque, prmOV);
        if (dsGetManifestInfo.contains("ERROR")) {
            return dsGetManifestInfo;
        }

        String dsGetOrderLineInfo = GetOrderLineInfo(token, dsGetManifestInfo, numLine, prmCveProducto);
        if (dsGetOrderLineInfo.contains("ERROR")) {
            return dsGetOrderLineInfo;
        }

        String dsGetOrderRelInfo = GetOrderRelInfo(token, dsGetOrderLineInfo);
        if (dsGetOrderRelInfo.contains("ERROR")) {
            return dsGetOrderRelInfo;
        }

        String dsGetQtyInfo = GetQtyInfo(token, dsGetOrderRelInfo, qty, numLine, prmUdm);
        if (dsGetQtyInfo.contains("ERROR")) {
            return dsGetQtyInfo;
        }

        String dsGetWhseInfo = GetWhseInfo(token, dsGetQtyInfo, prmWhHse);
        if (dsGetWhseInfo.contains("ERROR")) {
            return dsGetWhseInfo;
        }

        String dsCheckPCBinOutLocation = CheckPCBinOutLocation(token, dsGetWhseInfo, prmBin, numLine, false);
        if (dsCheckPCBinOutLocation.contains("ERROR")) {
            return dsCheckPCBinOutLocation;
        }

        String dsUpdateMaster = UpdateMaster(token, dsCheckPCBinOutLocation, prmEmpaque);
        if (dsUpdateMaster.contains("ERROR")) {
            return dsUpdateMaster;
        }

        return "OK";
    }

    private String GetNewShipHead(String token) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CustShipSvc/GetNewShipHead";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject());

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR GetNewShipHead: " + response.getBody();
            }

            return ds.toString();
        } catch (Exception ex) {
            return "ERROR GetNewShipHead: " + ex.getMessage();
        }
    }

    private String GetHeadOrderInfo(String token, String prmDS, int prmOV, String rowMod) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CustShipSvc/GetHeadOrderInfo";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsObj = mapper.readTree(prmDS);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("orderNum", prmOV);
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dsObj)));

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR GetHeadOrderInfo: " + response.getBody();
            }

            return ds.toString();
        } catch (Exception ex) {
            return "ERROR GetHeadOrderInfo: " + ex.getMessage();
        }
    }

    private String UpdateMaster(String token, String prmDS, int prmPackNum) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CustShipSvc/UpdateMaster";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsObj = mapper.readTree(prmDS);
            int custNum = Integer.parseInt(dsObj.get("ShipHead").get(0).get("CustNum").toString());

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("doValidateCreditHold", false);
            if(prmPackNum > 0)
                jsonPrm.put("doCheckShipDtl", true);
            else
                jsonPrm.put("doCheckShipDtl", false);

            jsonPrm.put("doLotValidation", false);
            jsonPrm.put("doCheckOrderComplete", false);
            jsonPrm.put("doPostUpdate", false);
            jsonPrm.put("doCheckCompliance", false);
            jsonPrm.put("ipShippedFlagChanged", false);
            jsonPrm.put("ipPackNum", prmPackNum);
            jsonPrm.put("ipBTCustNum", custNum);

            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dsObj)));

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR UpdateMaster: " + response.getBody();
            }

            String errorMsg = root.get("parameters").get("msg").toString();
            if(errorMsg.length() > 5) {
                return "ERROR UpdateMaster: " + errorMsg;
            }

            return ds.toString();
        } catch (Exception ex) {
            return "ERROR UpdateMaster: " + ex.getMessage();
        }
    }

    private String GetByID(String token, int prmEmpaque) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CustShipSvc/GetByID?packNum=" + prmEmpaque;

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
            JsonNode ds = root.at("/returnObj");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR GetByID: " + response.getBody();
            }

            return ds.toString();

        } catch(Exception ex) {
            return "ERROR: GetByID" + ex.getMessage();
        }
    }

    private String GetNewOrdrShipDtl(String token, String prmDS, int prmPackNum) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CustShipSvc/GetNewOrdrShipDtl";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsObj = mapper.readTree(prmDS);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dsObj)));
            jsonPrm.put("packNum", prmPackNum);
            jsonPrm.put("orderNum", 0);

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR GetNewOrdrShipDtl: " + response.getBody();
            }

            return ds.toString();
        } catch (Exception ex) {
            return "ERROR GetNewOrdrShipDtl: " + ex.getMessage();
        }
    }

    private String GetOrderInfo(String token, String prmDS, int prmOV) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CustShipSvc/GetOrderInfo";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsObj = mapper.readTree(prmDS);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("orderNum", prmOV);
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dsObj)));

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR GetOrderInfo: " + response.getBody();
            }

            return ds.toString();
        } catch (Exception ex) {
            return "ERROR GetOrderInfo: " + ex.getMessage();
        }
    }

    private String GetManifestInfo(String token, String prmDS, int prmPackNum, int prmOV) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CustShipSvc/GetManifestInfo";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsObj = mapper.readTree(prmDS);

            ObjectNode objParentDs = ((ObjectNode)dsObj);
            ObjectNode objShipHead = (ObjectNode)dsObj.get("ShipHead").get(0);
            objShipHead.put("RowMod", "U");

            objParentDs.replace("ShipHead", mapper.createArrayNode().add((JsonNode)objShipHead));
            dsObj = (JsonNode)objParentDs;

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ipSalesOrder", prmOV);
            jsonPrm.put("ipPackNum", prmPackNum);
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dsObj)));


            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR GetManifestInfo: " + response.getBody();
            }

            return ds.toString();
        } catch (Exception ex) {
            return "ERROR GetManifestInfo: " + ex.getMessage();
        }
    }

    private String GetOrderLineInfo(String token, String prmDS, int prmLine, String prmCveProducto) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CustShipSvc/GetOrderLineInfo";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsObj = mapper.readTree(prmDS);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dsObj)));
            jsonPrm.put("packLine", 0);
            jsonPrm.put("orderLine", prmLine);
            jsonPrm.put("subsPart", prmCveProducto);

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR GetOrderLineInfo: " + response.getBody();
            }

            return ds.toString();
        } catch (Exception ex) {
            return "ERROR GetOrderLineInfo: " + ex.getMessage();
        }
    }

    private String GetOrderRelInfo(String token, String prmDS) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CustShipSvc/GetOrderRelInfo";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsObj = mapper.readTree(prmDS);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dsObj)));
            jsonPrm.put("packLine", 0);
            jsonPrm.put("orderRelNum", 1);
            jsonPrm.put("allowNewShipTo", true);

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR GetOrderRelInfo: " + response.getBody();
            }

            return ds.toString();
        } catch (Exception ex) {
            return "ERROR GetOrderRelInfo: " + ex.getMessage();
        }
    }

    private String GetQtyInfo(String token, String prmDS, double qty, int prmNumLine, String prmUdm) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CustShipSvc/GetQtyInfo";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsObj = mapper.readTree(prmDS);

            ObjectNode objParentDs = ((ObjectNode)dsObj);
            ObjectNode objShipDtl = (ObjectNode)dsObj.get("ShipDtl").get(0);
            objShipDtl.put("OrderLine", prmNumLine);
            objShipDtl.put("InventoryShipUOM", prmUdm);

            objParentDs.replace("ShipDtl", mapper.createArrayNode().add((JsonNode)objShipDtl));
            dsObj = (JsonNode)objParentDs;

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dsObj)));
            jsonPrm.put("packLine", 0);
            jsonPrm.put("displayInvQty", qty);
            jsonPrm.put("ourJobShipQty", 0);

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR GetQtyInfo: " + response.getBody();
            }

            return ds.toString();
        } catch (Exception ex) {
            return "ERROR GetQtyInfo: " + ex.getMessage();
        }
    }

    private String GetWhseInfo(String token, String prmDS, String prmWhse) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CustShipSvc/GetWhseInfo";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsObj = mapper.readTree(prmDS);

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dsObj)));
            jsonPrm.put("packLine", 0);
            jsonPrm.put("whseCode", prmWhse);
            jsonPrm.put("whseField", "WarehouseCode");

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR GetWhseInfo: " + response.getBody();
            }

            return ds.toString();
        } catch (Exception ex) {
            return "ERROR GetWhseInfo: " + ex.getMessage();
        }
    }

    private String CheckPCBinOutLocation(String token, String prmDS, String prmBin, int prmNumLine, Boolean isMassShipment) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.CustShipSvc/CheckPCBinOutLocation";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsObj = mapper.readTree(prmDS);

            ObjectNode objParentDs = ((ObjectNode)dsObj);
            ObjectNode objShipDtl = (ObjectNode)dsObj.get("ShipDtl").get(0);
            objShipDtl.put("OrderLine", prmNumLine);
            objShipDtl.put("BinNum", prmBin);

            objParentDs.replace("ShipDtl", mapper.createArrayNode().add((JsonNode)objShipDtl));
            dsObj = (JsonNode)objParentDs;

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dsObj)));

            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            if (!response.getBody().toString().contains("Company")) {
                return "ERROR CheckPCBinOutLocation: " + response.getBody();
            }

            return ds.toString();
        } catch (Exception ex) {
            return "ERROR CheckPCBinOutLocation: " + ex.getMessage();
        }
    }
}
