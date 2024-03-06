package org.example.keycloak.event;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientTupleKeyWithoutCondition;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import org.example.keycloak.resolver.ResolverFactory;
import org.example.keycloak.support.KeycloakAdapter;
import org.jboss.logging.Logger;
import org.keycloak.events.admin.AdminEvent;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdminEventToFGAWriteTranslator {

    private final Logger log = Logger.getLogger(AdminEventToFGAWriteTranslator.class.toString());

    private final KeycloakAdapter keycloakAdapter;
    private final OpenFgaClient openFgaClient;

    public AdminEventToFGAWriteTranslator(KeycloakAdapter keycloakAdapter, OpenFgaClient openFgaClient) {
        this.keycloakAdapter = keycloakAdapter;
        this.openFgaClient = openFgaClient;
    }

    public synchronized Optional<ClientWriteRequest> translate(AdminEvent event) {


        return ResolverFactory.getInstance().tupleResolver(event).flatMap(tupleResolver -> {

            ClientWriteRequest writeRequest = new ClientWriteRequest();
            writeRequest.writes(Collections.emptyList());
            writeRequest.deletes(Collections.emptyList());

            Stream<ClientTupleKey> clientTupleKeyStream = tupleResolver.resolve(this.keycloakAdapter, this.openFgaClient);

            switch (tupleResolver.getOperationType()) {
                case WRITE:
                    writeRequest.writes(clientTupleKeyStream.collect(Collectors.toList()));
                    break;
                case DELETE:
                    writeRequest.deletes(clientTupleKeyStream.map(this::makeWithoutCondition).collect(Collectors.toList()));
                    break;
                case NONE:
                default:
                    log.debug("Operation type is not handled: " + event.getOperationType());
            }

            if((writeRequest.getWrites().isEmpty()) &&
                    (writeRequest.getDeletes().isEmpty())) {
                return Stream.empty();
            }

            return Stream.of(writeRequest);

        }).findAny();
    }

    private ClientTupleKeyWithoutCondition makeWithoutCondition(ClientTupleKey clientTupleKey) {
        return new ClientTupleKeyWithoutCondition()
                .user(clientTupleKey.getUser())
                .relation(clientTupleKey.getRelation())
                ._object(clientTupleKey.getObject());
    }


}
