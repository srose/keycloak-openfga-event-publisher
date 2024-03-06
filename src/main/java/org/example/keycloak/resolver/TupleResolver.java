package org.example.keycloak.resolver;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import org.example.keycloak.support.KeycloakAdapter;

import java.util.stream.Stream;

public interface TupleResolver {

    enum OperationType {
        WRITE, DELETE, NONE
    }

    TupleResolver VOID_TUPLE_RESOLVER = new TupleResolver() {};

    default Stream<ClientTupleKey> resolve(KeycloakAdapter keycloakAdapter, OpenFgaClient openFgaClient) {
        return Stream.empty();
    }

    default OperationType getOperationType() {
        return OperationType.NONE;
    }

}


