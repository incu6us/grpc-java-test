package com.grpctest;

import com.google.common.base.Verify;
import com.google.common.base.VerifyException;
import com.google.gson.Gson;
import com.google.rpc.DebugInfo;
import com.grpctest.store.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.protobuf.StatusProto;
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

    private static final Metadata.Key<DebugInfo> DEBUG_INFO_TRAILER_KEY =
            ProtoUtils.keyForProto(DebugInfo.getDefaultInstance());

    private static final DebugInfo DEBUG_INFO =
            DebugInfo.newBuilder()
                    .addStackEntries("stack_entry_1")
                    .addStackEntries("stack_entry_2")
                    .addStackEntries("stack_entry_3")
                    .setDetail("detailed error info.").build();

    private static final String DEBUG_DESC = "detailed error description";

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

        com.google.rpc.Status statusWithDetails = StatusProto.fromThrowable(t);
        log.info("status with details: {}", statusWithDetails);

        Metadata trailers = Status.trailersFromThrowable(t);
        log.info("trailers: {}", trailers);

        Verify.verify(trailers.containsKey(DEBUG_INFO_TRAILER_KEY));
        Verify.verify(status.getDescription().equals(DEBUG_DESC));
        try {
            Verify.verify(trailers.get(DEBUG_INFO_TRAILER_KEY).equals(DEBUG_INFO));
            log.info("debug info: {}", trailers.get(DEBUG_INFO_TRAILER_KEY).getAllFields().toString());
            log.info("debug info json: {}", new Gson().toJson(trailers.get(DEBUG_INFO_TRAILER_KEY)));
        } catch (IllegalArgumentException e) {
            throw new VerifyException(e);
        }
    }
}
