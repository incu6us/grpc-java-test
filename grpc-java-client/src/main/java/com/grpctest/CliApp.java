package com.grpctest;

import com.grpctest.store.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@ShellComponent
public class CliApp {

    private static final Logger log = LoggerFactory.getLogger(CliApp.class);
    private static final int WAIT_TIMEOUT = 500;

    @ShellMethod(value = "store data to server", key = "store")
    public void addToStore(@ShellOption String server, @ShellOption int id, @ShellOption String message) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(server)
                .intercept(Arrays.asList(new HeaderInterceptor()))
                .usePlaintext()
                .build();

        StoreRequest storeRequest = StoreRequest.newBuilder().setId(id).setValue(message).build();

        try {
            StoreResponse storeResponse = StoreServiceGrpc.newBlockingStub(channel).addValue(storeRequest);
            log.info("STORE REQUEST: {}", storeResponse);
        }catch (Exception e){
            verifyStatusAndTrailers(e);
        }finally {
            channel.shutdown().awaitTermination(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
        }
    }

    @ShellMethod(value = "read method", key = "read")
    public void readFromStore(@ShellOption String server, @ShellOption int id) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(server)
                .intercept(Arrays.asList(new HeaderInterceptor()))
                .usePlaintext()
                .build();

        GetRequest getRequest = GetRequest.newBuilder().setId(id).build();

        try {
            StoreServiceGrpc.StoreServiceBlockingStub storeServiceBlockingStub = StoreServiceGrpc.newBlockingStub(channel);
            GetResponse getResponse = storeServiceBlockingStub.getValue(getRequest);
            log.info("ID: {}, DATA: {}", getResponse.getId(), getResponse.getValue());
        } catch (Exception e) {
            verifyStatusAndTrailers(e);
        } finally {
            channel.shutdown().awaitTermination(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
        }
    }

    static void verifyStatusAndTrailers(Throwable t) {
        Status status = Status.fromThrowable(t);
        log.info("status: {}", status);

        Metadata trailers = Status.trailersFromThrowable(t);
        log.info("trailers: {}", trailers);
    }
}
