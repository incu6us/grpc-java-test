package com.grpctest.server;

import com.grpctest.Constants;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class GRpcServer {

    @Autowired
    private StoreServiceImpl storeService;

    @PostConstruct
    public void run() throws IOException {
        Server srv = ServerBuilder
                .forPort(Constants.GRPC_SERVER_PORT)
                .intercept(new HeaderInterceptor())
                .addService(storeService).build();

        srv.start();
    }
}
