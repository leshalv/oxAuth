/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client;

import static org.gluu.oxauth.model.discovery.WebFingerParam.HREF;
import static org.gluu.oxauth.model.discovery.WebFingerParam.LINKS;
import static org.gluu.oxauth.model.discovery.WebFingerParam.REL;
import static org.gluu.oxauth.model.discovery.WebFingerParam.REL_VALUE;
import static org.gluu.oxauth.model.discovery.WebFingerParam.RESOURCE;
import static org.gluu.oxauth.model.discovery.WebFingerParam.SUBJECT;

import java.io.InputStream;
import java.net.URISyntaxException;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.gluu.oxauth.model.discovery.WebFingerLink;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Javier Rojas Blum
 * @version December 26, 2016
 */
public class OpenIdConnectDiscoveryClient extends BaseClient<OpenIdConnectDiscoveryRequest, OpenIdConnectDiscoveryResponse> {

    private static final Logger LOG = Logger.getLogger(OpenIdConnectDiscoveryClient.class);

    private static final String MEDIA_TYPE = MediaType.APPLICATION_JSON;
    private static final String SCHEMA = "https://";
    private static final String PATH = "/.well-known/webfinger";

    public OpenIdConnectDiscoveryClient(String resource) throws URISyntaxException {
        setRequest(new OpenIdConnectDiscoveryRequest(resource));
        setUrl(SCHEMA + getRequest().getHost() + PATH);
    }

    @Override
    public String getHttpMethod() {
        return HttpMethod.GET;
    }

    public OpenIdConnectDiscoveryResponse exec() {
        initClientRequest();

        return _exec();
    }

    @Deprecated
    public OpenIdConnectDiscoveryResponse exec(ClientHttpEngine engine) {
    	resteasyClient = ((ResteasyClientBuilder) ResteasyClientBuilder.newBuilder()).httpEngine(engine).build();
    	webTarget = resteasyClient.target(getUrl());

        return _exec();
    }

    private OpenIdConnectDiscoveryResponse _exec() {
        OpenIdConnectDiscoveryResponse response = null;

        try {
            response = _exec2();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            closeConnection();
        }

        return response;
    }

    private OpenIdConnectDiscoveryResponse _exec2() {
        // Prepare request parameters
        if (StringUtils.isNotBlank(getRequest().getResource())) {
        	addReqParam(RESOURCE, getRequest().getResource());
        }
        addReqParam(REL, REL_VALUE);

        // Call REST Service and handle response
        Response clientResponse1;
        try {
            Builder clientRequest = webTarget.request();
            applyCookies(clientRequest);

            clientRequest.accept(MEDIA_TYPE);
//          clientRequest.setHttpMethod(getHttpMethod());
            clientResponse1 = clientRequest.buildGet().invoke();

            int status = clientResponse1.getStatus();

            setResponse(new OpenIdConnectDiscoveryResponse(status));

            String entity = clientResponse1.readEntity(String.class);
            getResponse().setEntity(entity);
            getResponse().setHeaders(clientResponse1.getMetadata());
            if (StringUtils.isNotBlank(entity)) {
                JSONObject jsonObj = new JSONObject(entity);
                getResponse().setSubject(jsonObj.getString(SUBJECT));
                JSONArray linksJsonArray = jsonObj.getJSONArray(LINKS);
                for (int i = 0; i < linksJsonArray.length(); i++) {
                    WebFingerLink webFingerLink = new WebFingerLink();
                    webFingerLink.setRel(linksJsonArray.getJSONObject(i).getString(REL));
                    webFingerLink.setHref(linksJsonArray.getJSONObject(i).getString(HREF));

                    getResponse().getLinks().add(webFingerLink);
                }
            }
        } catch (JSONException e) {
            LOG.error(e.getMessage(), e);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return getResponse();
    }

}