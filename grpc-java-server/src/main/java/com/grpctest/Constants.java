package com.grpctest;

import io.grpc.Metadata;

public class Constants {
    public static final int GRPC_SERVER_PORT = 8000;
    public static final Metadata.Key<String> CUSTOM_DATA = Metadata.Key.of("custom-data", Metadata.ASCII_STRING_MARSHALLER);
}
