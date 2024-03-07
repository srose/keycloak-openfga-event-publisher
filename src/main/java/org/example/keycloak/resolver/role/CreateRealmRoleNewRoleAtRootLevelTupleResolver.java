package org.example.keycloak.resolver.role;

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

public class CreateRealmRoleNewRoleAtRootLevelTupleResolver extends AbstractTupleResolver implements TupleResolver {

    public static final String REGEX = "roles/(.*)(?<!/composites)$";

    private final String realmId;
    private final String roleName;
    private final String roleId;

    public CreateRealmRoleNewRoleAtRootLevelTupleResolver(AdminEvent adminEvent, Matcher matcher) {
        super(adminEvent, matcher);

        this.realmId = adminEvent.getRealmId();
        this.roleName = matcher.group(1);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode representationNode = objectMapper.readTree(adminEvent.getRepresentation());
            this.roleId = representationNode.get("id").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Stream<ClientTupleKey> resolve(KeycloakAdapter keycloakAdapter, OpenFgaClient openFgaClient) {
        keycloakAdapter.addToRoleNameCache(realmId, roleId, roleName);

        //NOTHING TO DO, COMPOSITES WILL BE PROVIDED AFTERWARDS
        return Stream.empty();
    }

}
