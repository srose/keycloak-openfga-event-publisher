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

public class CreateGroupNewGroupInOtherGroupAkaSubgroupTupleResolver extends AbstractTupleResolver implements TupleResolver {

    private final String realmId;
    private final String groupId;
    private final String subGroupId;
    private final String subGroupName;

    public CreateGroupNewGroupInOtherGroupAkaSubgroupTupleResolver(AdminEvent adminEvent, Matcher matcher) {
        super(adminEvent, matcher);

        this.groupId = matcher.group(1);
        this.realmId = adminEvent.getRealmId();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode representationNode = objectMapper.readTree(adminEvent.getRepresentation());

            this.subGroupId = representationNode.get("id").asText();
            this.subGroupName = representationNode.get("name").asText();

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<ClientTupleKey> resolve(KeycloakAdapter keycloakAdapter, OpenFgaClient openFgaClient) {

        keycloakAdapter.addToGroupNameCache(realmId, subGroupId, subGroupName);

        var groupNameInRealm = keycloakAdapter.findGroupNameInRealm(groupId, realmId);
        if(groupNameInRealm == null) {
            return Stream.empty();
        }

        return Stream.of(group_subgroup_group(groupNameInRealm, subGroupName));
    }
}
