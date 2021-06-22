package com.criogas.bulkllenadoentregaapp.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class RestInvTransfer {
    /**
     *
     * @param prmPart
     * @param prmQty
     * @param prmFecha (yyyy-mm-dd)
     * @param prmBinOrigen
     * @param prmWhseDestino
     * @param prmBinDestino
     * @param prmReference
     * @return OK o ERROR...
     */
    public String invTransfer(String prmPart, double prmQty, String prmFecha, String prmBinOrigen, String prmWhseDestino, String prmBinDestino, String prmReference) {
        RestApiPipas api = new RestApiPipas();
        String token = api.getToken();
        if(token.contains("ERROR")) {
            return token;
        }

        String dsTransferRecord = GetTransferRecord(token, prmPart);
        if(dsTransferRecord.contains("ERROR")) {
            return dsTransferRecord;
        }

        String dsChangeTransferQty = ChangeTransferQty(token, dsTransferRecord, prmQty, prmFecha);
        if(dsChangeTransferQty.contains("ERROR")) {
            return dsChangeTransferQty;
        }

        String dsOnChangingTransferQty = OnChangingTransferQty(token, dsChangeTransferQty, prmQty);
        if(dsOnChangingTransferQty.contains("ERROR")) {
            return dsOnChangingTransferQty;
        }

        String dsChangeFromBin = ChangeFromBin(token, dsOnChangingTransferQty, prmBinOrigen);
        if(dsChangeFromBin.contains("ERROR")) {
            return dsChangeFromBin;
        }

        String dsChangeToWhse = ChangeToWhse(token, dsChangeFromBin, prmWhseDestino);
        if(dsChangeToWhse.contains("ERROR")) {
            return dsChangeToWhse;
        }

        String dsChangeToBin = ChangeToBin(token, dsChangeToWhse, prmBinDestino);
        if(dsChangeToBin.contains("ERROR")) {
            return dsChangeToWhse;
        }

        String dsMasterInventoryBinTests = MasterInventoryBinTests(token, dsChangeToBin, prmReference);
        if(dsMasterInventoryBinTests.contains("ERROR")) {
            return dsMasterInventoryBinTests;
        }

        String dsPreCommitTransfer = PreCommitTransfer(token, dsMasterInventoryBinTests);
        if(dsPreCommitTransfer.contains("ERROR")) {
            return dsPreCommitTransfer;
        }

        String dsCommitTransfer = CommitTransfer(token, dsPreCommitTransfer);
        if(dsCommitTransfer.contains("ERROR")) {
            return dsCommitTransfer;
        }

        return "OK";
    }

    private String GetTransferRecord(String token, String prmPart) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.InvTransferSvc/GetTransferRecord";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("iPartNum", prmPart);
            jsonPrm.put("sysRowID", "00000000-0000-0000-0000-000000000000");
            jsonPrm.put("iPCID", "");
            jsonPrm.put("uomCode", "");
            jsonPrm.put("ds", new JSONObject());

            r.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = r.postForEntity(url, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            if(!ds.toString().contains("Company")) {
                return "ERROR GetTransferRecord " + ds.toString();
            }

            return ds.toString();

        } catch(Exception ex) {
            return "ERROR GetTransferRecord: " + ex.getMessage();
        }
    }

    private String ChangeTransferQty(String token, String prmDs, double prmQty, String prmFecha) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.InvTransferSvc/ChangeTransferQty";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("proposedValue", prmQty);
            jsonPrm.put("ds", new JSONObject(prmDs));

            r.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = r.postForEntity(url, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds");

            ObjectMapper objMapper = new ObjectMapper();
            ObjectNode objNode = objMapper.createObjectNode();

            Iterator<Map.Entry<String, JsonNode>> fields = ds.get("InvTrans").get(0).fields();
            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("TransferQty") || fName.equals("TrackingQty")) {
                    objNode.put(fName, prmQty);
                } else if(fName.equals("RowMod")) {
                    objNode.put(fName, "U");
                } else if(fName.equals("TranDate")) {
                    objNode.put(fName, prmFecha + "T00:00:00.000Z");
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = objMapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode responseNode = objMapper.createObjectNode();
            responseNode.set("InvTrans", arrayNode);

            String strResponse = objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseNode);

            if(!strResponse.contains("Company")) {
                return "ERROR ChangeTransferQty " + strResponse;
            }

            return strResponse;
        } catch(Exception ex) {
            return "ERROR ChangeTransferQty: " + ex.getMessage();
        }
    }

    private String OnChangingTransferQty(String token, String prmDs, double prmQty) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.InvTransferSvc/OnChangingTransferQty";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("transferQty", prmQty);
            jsonPrm.put("ds", new JSONObject(prmDs));

            r.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = r.postForEntity(url, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/InvTrans");

            String strResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ds.get(0));
            if(!strResponse.contains("Company")) {
                return "ERROR OnChangingTransferQty " + strResponse;
            }
            return strResponse;

        } catch(Exception ex) {
            return "ERROR OnChangingTransferQty: " + ex.getMessage();
        }
    }

    private String ChangeFromBin(String token, String prmDs, String prmBinOrigen) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.InvTransferSvc/ChangeFromBin";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            /***MODIFICO EL DATASET DE JSON PARA CAMBIAR LOS VALORES DEL FromBinNum y RowMod***/
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDs);
            Iterator<Map.Entry<String, JsonNode>> fields = dsNode.fields();

            ObjectNode objNode = mapper.createObjectNode();

            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("FromBinNum")) {
                    objNode.put(fName, prmBinOrigen);
                } else if(fName.equals("RowMod")) {
                    objNode.put(fName, "U");
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeInvTrans = mapper.createObjectNode();
            nodeInvTrans.set("InvTrans", arrayNode);

            /***********************************************************/

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ipBinNum", prmBinOrigen);
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeInvTrans)));

            r.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = r.postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/InvTrans");

            String strResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ds.get(0));
            if(!strResponse.contains("Company")) {
                return "ERROR ChangeFromBin " + strResponse;
            }
            return strResponse;

        } catch(Exception ex) {
            return "ERROR ChangeFromBin: " + ex.getMessage();
        }
    }

    private String ChangeToWhse(String token, String prmDs, String prmWhseDestino) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.InvTransferSvc/ChangeToWhse";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            /***MODIFICO EL DATASET DE JSON PARA CAMBIAR LOS VALORES DEL RowMod***/
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDs);
            Iterator<Map.Entry<String, JsonNode>> fields = dsNode.fields();

            ObjectNode objNode = mapper.createObjectNode();

            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("RowMod")) {
                    objNode.put(fName, "U");
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeInvTrans = mapper.createObjectNode();
            nodeInvTrans.set("InvTrans", arrayNode);

            /***********************************************************/

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ipToWhse", prmWhseDestino);
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeInvTrans)));

            r.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = r.postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/InvTrans");

            String strResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ds.get(0));
            if(!strResponse.contains("Company")) {
                return "ERROR ChangeToWhse " + strResponse;
            }
            return strResponse;
        } catch(Exception ex) {
            return "ERROR ChangeToWhse: " + ex.getMessage();
        }
    }

    private String ChangeToBin(String token, String prmDs, String prmBinDestino) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.InvTransferSvc/ChangeToBin";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            /***MODIFICO EL DATASET DE JSON PARA CAMBIAR LOS VALORES DEL RowMod***/
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDs);
            Iterator<Map.Entry<String, JsonNode>> fields = dsNode.fields();

            ObjectNode objNode = mapper.createObjectNode();

            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("ToBinNum")) {
                    objNode.put(fName, "PIPA01");
                } else if(fName.equals("RowMod")) {
                    objNode.put(fName, "U");
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeInvTrans = mapper.createObjectNode();
            nodeInvTrans.set("InvTrans", arrayNode);

            /***********************************************************/

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ipToBinNum", prmBinDestino);
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeInvTrans)));

            r.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = r.postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/InvTrans");

            String strResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ds.get(0));
            if(!strResponse.contains("Company")) {
                return "ERROR ChangeToBin " + strResponse;
            }
            return strResponse;

        } catch(Exception ex) {
            return "ERROR ChangeToBin: " + ex.getMessage();
        }
    }

    private String MasterInventoryBinTests(String token, String prmDs, String prmReference) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.InvTransferSvc/MasterInventoryBinTests";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            /***MODIFICO EL DATASET DE JSON PARA CAMBIAR LOS VALORES DEL RowMod***/
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDs);
            Iterator<Map.Entry<String, JsonNode>> fields = dsNode.fields();

            ObjectNode objNode = mapper.createObjectNode();

            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                if(fName.equals("TranReference")) {
                    objNode.put(fName, prmReference);
                } else if(fName.equals("RowMod")) {
                    objNode.put(fName, "U");
                } else {
                    objNode.set(fName, fValue);
                }
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeInvTrans = mapper.createObjectNode();
            nodeInvTrans.set("InvTrans", arrayNode);

            /***********************************************************/

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeInvTrans)));

            r.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = r.postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());

            JsonNode dsResponse = root.at("/parameters");

            if(!dsResponse.get("pcNeqQtyMessage").asText().equals("")) {
                return "ERROR MasterInventoryBinTests " + dsResponse.get("pcNeqQtyMessage").asText();
            } else if(!dsResponse.get("pcFromPCBinMessage").asText().equals("")) {
                return "ERROR MasterInventoryBinTests " + dsResponse.get("pcFromPCBinMessage").asText();
            } else if(!dsResponse.get("pcToPCBinMessage").asText().equals("")) {
                return "ERROR MasterInventoryBinTests " + dsResponse.get("pcToPCBinMessage").asText();
            }

            JsonNode ds = root.at("/parameters/ds/InvTrans");

            String strResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ds.get(0));
            if(!strResponse.contains("Company")) {
                return "ERROR MasterInventoryBinTests " + strResponse;
            }
            return strResponse;

        } catch(Exception ex) {
            return "ERROR MasterInventoryBinTests: " + ex.getMessage();
        }
    }

    private String PreCommitTransfer(String token, String prmDs) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.InvTransferSvc/PreCommitTransfer";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            /***MODIFICO EL DATASET DE JSON PARA CAMBIAR LOS VALORES DEL RowMod***/
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDs);
            Iterator<Map.Entry<String, JsonNode>> fields = dsNode.fields();

            ObjectNode objNode = mapper.createObjectNode();

            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                objNode.set(fName, fValue);
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeInvTrans = mapper.createObjectNode();
            nodeInvTrans.set("InvTrans", arrayNode);

            /***********************************************************/

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeInvTrans)));

            r.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = r.postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/InvTrans");

            String strResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ds.get(0));
            if(!strResponse.contains("Company")) {
                return "ERROR PreCommitTransfer " + strResponse;
            }
            return strResponse;

        } catch(Exception ex) {
            return "ERROR PreCommitTransfer: " + ex.getMessage();
        }
    }

    private String CommitTransfer(String token, String prmDs) {
        try {
            String url = RestApiPipas.URL_BASE + "api/v2/odata/28701/Erp.BO.InvTransferSvc/CommitTransfer";

            RestTemplate r = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + token);
            headers.add("X-API-Key", RestApiPipas.API_KEY);
            headers.add("Accept", "application/json");

            /***MODIFICO EL DATASET DE JSON PARA CAMBIAR LOS VALORES DEL RowMod***/
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dsNode = mapper.readTree(prmDs);
            Iterator<Map.Entry<String, JsonNode>> fields = dsNode.fields();

            ObjectNode objNode = mapper.createObjectNode();

            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fName = field.getKey();
                JsonNode fValue = field.getValue();

                objNode.set(fName, fValue);
            }

            ArrayNode arrayNode = mapper.createArrayNode();
            arrayNode.addAll(Arrays.asList(objNode));

            ObjectNode nodeInvTrans = mapper.createObjectNode();
            nodeInvTrans.set("InvTrans", arrayNode);

            /***********************************************************/

            JSONObject jsonPrm = new JSONObject();
            jsonPrm.put("ds", new JSONObject(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeInvTrans)));

            r.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
            HttpEntity<String> request = new HttpEntity<>(jsonPrm.toString(), headers);
            ResponseEntity<String> response = r.postForEntity(url, request, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode ds = root.at("/parameters/ds/InvTrans");

            String strResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ds.get(0));
            if(!strResponse.contains("Company")) {
                return "ERROR CommitTransfer " + strResponse;
            }
            return strResponse;

        } catch(Exception ex) {
            return "ERROR CommitTransfer: " + ex.getMessage();
        }
    }
}

