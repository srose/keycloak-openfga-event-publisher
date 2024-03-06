package org.example.keycloak.resolver.group_membership;

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

public class DeleteGroupMembershipRemoveUserFromGroupTupleResolver extends AbstractTupleResolver implements TupleResolver {

    private final String userId;
    private final String groupName;

    public DeleteGroupMembershipRemoveUserFromGroupTupleResolver(AdminEvent adminEvent, Matcher matcher) {
        super(adminEvent, matcher);

        this.userId = matcher.group(1);

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
        return Stream.of(user_member_group(userId, groupName));
    }
}
