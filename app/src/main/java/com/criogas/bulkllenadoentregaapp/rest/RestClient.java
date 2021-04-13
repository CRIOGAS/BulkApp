package com.criogas.bulkllenadoentregaapp.rest;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class RestClient {

    public static String BASE_URL = "http://192.189.12.220:8082/"; //ORIZABA
    public static int DEFAULT_TIMEOUT = 120 * 1000;
    public static int DEFAULT_SLEEP_TIME_MILLIS = 60 * 1000;

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(
            Context context,
            String url,
            Header[] headers,
            RequestParams params,
            AsyncHttpResponseHandler responseHandler) throws Exception {


        client.setMaxRetriesAndTimeout(1, 5000);
        client.setTimeout(DEFAULT_TIMEOUT);
        client.get(context, getAbsoluteUrl(url), headers, params, responseHandler);
        //   client.setMaxRetriesAndTimeout(1,5000);
    }

    public static void post(
            Context context,
            String url,
            StringEntity entity,
            AsyncHttpResponseHandler responseHandler) {

        try {
            client.setMaxRetriesAndTimeout(0, 900000);
            client.setTimeout(DEFAULT_TIMEOUT);
            client.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
            //  client.setMaxRetriesAndTimeout(1,5000);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void postFile(
            String url,
            RequestParams params,
            AsyncHttpResponseHandler responseHandler) {

        try {
            client.setMaxRetriesAndTimeout(0, 60000);
            client.post(getAbsoluteUrl(url), params, responseHandler);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public void getFile(
            String url,
            FileAsyncHttpResponseHandler responseHandler) {

        try {
            client.setMaxRetriesAndTimeout(0, 60000);
            client.get(getAbsoluteUrl(url), responseHandler);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
