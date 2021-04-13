package com.criogas.bulkllenadoentregaapp.model;

import java.io.Serializable;

/**
 * Created by yairg on 20/03/18.
 */

public class ConfigServer implements Serializable {
    public static final String TABLE_CONFIG_SERVER = "ConfigServer";
    public static final String IP_SERVER = "http://192.189.12.220";//"ipServer";
    public static final String PORT_SERVER = "8082";//"portServer";
    public static String SUCURSAL = "MfgSys";
    public static final String COL_SUCURSAL = "sucursal";

    private String ipServer;
    private String portServer;
    private String sucursal;

    public ConfigServer() {
    }

    public ConfigServer(String ipServer, String portServer, String sucursal) {
        this.ipServer = ipServer;
        this.portServer = portServer;
        this.sucursal = sucursal;
    }

    public String getIpServer() {
        return ipServer;
    }

    public void setIpServer(String ipServer) {
        this.ipServer = ipServer;
    }

    public String getPortServer() {
        return portServer;
    }

    public void setPortServer(String portServer) {
        this.portServer = portServer;
    }

    public String getSucursal() { return sucursal; }

    public void setSucursal(String sucursal) { this.sucursal = sucursal; }
}
