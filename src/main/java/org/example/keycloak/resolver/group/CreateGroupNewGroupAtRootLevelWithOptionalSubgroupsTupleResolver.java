package org.example.keycloak.resolver.group;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import org.example.keycloak.resolver.AbstractTupleResolver;
import org.example.keycloak.resolver.TupleResolver;
import org.example.keycloak.support.KeycloakAdapter;
import org.keycloak.events.admin.AdminEvent;

import java.util.regex.Matcher;
import java.util.stream.Stream;

public class CreateGroupNewGroupAtRootLevelWithOptionalSubgroupsTupleResolver extends AbstractTupleResolver implements TupleResolver {

    public static final String REGEX = "groups/(.*)(?<!/children)$";

    private final String realmId;
    private final String groupName;
    private final String groupId;

    public CreateGroupNewGroupAtRootLevelWithOptionalSubgroupsTupleResolver(AdminEvent adminEvent, Matcher matcher) {
        super(adminEvent, matcher);

        realmId = adminEvent.getRealmId();
        groupId = matcher.group(1);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode representationNode = objectMapper.readTree(adminEvent.getRepresentation());
            this.groupName = representationNode.get("name").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<ClientTupleKey> resolve(KeycloakAdapter keycloakAdapter, OpenFgaClient openFgaClient) {
        keycloakAdapter.addToGroupNameCache(realmId, groupId, groupName);

        //NOTHING TO DO, A GROUP IS CREATED AT ROOT LEVEL, SUBGROUPS WILL BE PROVIDED IN SUBSEQUENT EVENTS
        return Stream.empty();
    }
}