package org.example.keycloak.resolver.role_mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import org.example.keycloak.resolver.AbstractTupleResolver;
import org.example.keycloak.resolver.TupleResolver;
import org.example.keycloak.support.KeycloakAdapter;
import org.keycloak.events.admin.AdminEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Stream;

public class CreateRealmRoleMappingAssignRoleToUserTupleResolver extends AbstractTupleResolver implements TupleResolver {


    public static final String REGEX = "users/(.*)/role-mappings/realm";
    private final String userId;
    private final List<String> roleNames = new ArrayList<>();

    public CreateRealmRoleMappingAssignRoleToUserTupleResolver(AdminEvent adminEvent, Matcher matcher) {
        super(adminEvent, matcher);

        this.userId = matcher.group(1);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode representationNode = objectMapper.readTree(adminEvent.getRepresentation());

            for (JsonNode roleNode : representationNode) {
                roleNames.add(roleNode.get("name").asText());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<ClientTupleKey> resolve(KeycloakAdapter keycloakAdapter, OpenFgaClient openFgaClient) {
        return roleNames.stream().map(roleName ->
                user_assignee_role(userId, roleName));
    }
}
