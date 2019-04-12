package com.zcsoft.rc.analysis.app.autoconfigure.components;

import com.sharingif.cube.communication.JsonModel;
import com.sharingif.cube.communication.exception.JsonModelBusinessCommunicationExceptionHandler;
import com.sharingif.cube.communication.http.apache.transport.HttpJsonConnection;
import com.sharingif.cube.communication.http.transport.HandlerMethodCommunicationTransportRequestContextResolver;
import com.sharingif.cube.communication.remote.RemoteServices;
import com.sharingif.cube.communication.transport.ProxyInterfaceHandlerMethodCommunicationTransportFactory;
import com.sharingif.cube.communication.transport.transform.ProxyInterfaceHandlerMethodCommunicationTransform;
import com.sharingif.cube.core.handler.chain.MultiHandlerMethodChain;
import com.zcsoft.rc.collectors.api.warning.service.WarningApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * rc-collectors服务
 */
@Configuration
public class RcCollectorsRemoteContextAutoconfigure {

    @Bean(name = "rcCollectorsHttpJsonConnection")
    public HttpJsonConnection creatercCollectorsHttpJsonConnection(
            @Value("${rc.collectors.http.host}")String host
            ,@Value("${rc.collectors.http.port}")int port
            ,@Value("${rc.collectors.http.contextPath}") String contextPath
            ,@Value("${rc.collectors.http.so.timeout}")int soTimeout
    ) {
        HttpJsonConnection apacheHttpJsonConnection = new HttpJsonConnection(host, port, contextPath);
        apacheHttpJsonConnection.setSoTimeout(soTimeout);

        return apacheHttpJsonConnection;
    }


    @Bean(name= "rcCollectorsHttpJsonRemoteHandlerMethodTransportFactory")
    public ProxyInterfaceHandlerMethodCommunicationTransportFactory<String,String,JsonModel<Object>> createRcCollectorsHttpJsonRemoteHandlerMethodTransportFactory(
            HttpJsonConnection rcCollectorsHttpJsonConnection
            , ProxyInterfaceHandlerMethodCommunicationTransform<String,String,JsonModel<Object>> jsonModelProxyInterfaceHandlerMethodCommunicationTransform
            , JsonModelBusinessCommunicationExceptionHandler jsonModelBusinessCommunicationExceptionHandler
            , MultiHandlerMethodChain transportChains
    ) {
        ProxyInterfaceHandlerMethodCommunicationTransportFactory<String,String,JsonModel<Object>> httpJsonRemoteHandlerMethodTransportFactory = new ProxyInterfaceHandlerMethodCommunicationTransportFactory<String,String,JsonModel<Object>>();
        httpJsonRemoteHandlerMethodTransportFactory.setConnection(rcCollectorsHttpJsonConnection);
        httpJsonRemoteHandlerMethodTransportFactory.setTransform(jsonModelProxyInterfaceHandlerMethodCommunicationTransform);
        httpJsonRemoteHandlerMethodTransportFactory.setBusinessCommunicationExceptionHandler(jsonModelBusinessCommunicationExceptionHandler);
        httpJsonRemoteHandlerMethodTransportFactory.setHandlerMethodChain(transportChains);

        return httpJsonRemoteHandlerMethodTransportFactory;
    }

    @Bean(name = "rcCollectorsRemoteServices")
    public RemoteServices createRcCollectorsRemoteServices(
            HandlerMethodCommunicationTransportRequestContextResolver handlerMethodCommunicationTransportRequestContextResolver
            ,ProxyInterfaceHandlerMethodCommunicationTransportFactory<String,String,JsonModel<Object>> rcCollectorsHttpJsonRemoteHandlerMethodTransportFactory
    ) {
        List<String> services = new ArrayList<String>();

        services.add("com.zcsoft.rc.collectors.api.rc.service.CurrentRcApiService");
        services.add("com.zcsoft.rc.collectors.api.warning.service.WarningApiService");

        RemoteServices remoteServices = new RemoteServices();
        remoteServices.setRequestContextResolver(handlerMethodCommunicationTransportRequestContextResolver);
        remoteServices.setHandlerMethodCommunicationTransportFactory(rcCollectorsHttpJsonRemoteHandlerMethodTransportFactory);
        remoteServices.setServices(services);

        return remoteServices;
    }

}
