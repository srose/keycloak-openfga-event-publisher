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

public class DeleteRealmRoleMappingRemoveRealmRoleFromGroupTupleResolver extends AbstractTupleResolver implements TupleResolver {

    public static final String REGEX = "groups/(.*)/role-mappings/realm";

    private final String realmId;
    private final String groupId;
    private final List<String> roleNames;

    public DeleteRealmRoleMappingRemoveRealmRoleFromGroupTupleResolver(AdminEvent adminEvent, Matcher matcher) {
        super(adminEvent, matcher);

        this.realmId = adminEvent.getRealmId();
        this.groupId = matcher.group(1);
        this.roleNames = new ArrayList<>();

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
        String groupNameInRealm = keycloakAdapter.findGroupNameInRealm(groupId, realmId);

        if(groupNameInRealm == null) {
            return Stream.empty();
        }

        return roleNames.stream().map(roleName ->
                group_parent_group_role(groupNameInRealm, roleName));
    }
}
