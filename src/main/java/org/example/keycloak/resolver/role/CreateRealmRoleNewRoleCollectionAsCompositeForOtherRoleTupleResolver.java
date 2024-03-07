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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Stream;

public class CreateRealmRoleNewRoleCollectionAsCompositeForOtherRoleTupleResolver extends AbstractTupleResolver implements TupleResolver {

    public static final String REGEX = "roles/(.*)/composites";

    private final String roleName;
    private final List<String> compositeRoleNames = new ArrayList<>();

    public CreateRealmRoleNewRoleCollectionAsCompositeForOtherRoleTupleResolver(AdminEvent adminEvent, Matcher matcher) {
        super(adminEvent, matcher);

        this.roleName = matcher.group(1);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode representationNode = objectMapper.readTree(adminEvent.getRepresentation());

            if (representationNode.isArray()) {
                for (JsonNode node : representationNode) {
                    compositeRoleNames.add(node.get("name").asText());
                }
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<ClientTupleKey> resolve(KeycloakAdapter keycloakAdapter, OpenFgaClient openFgaClient) {
        return compositeRoleNames.stream().map(compositeRoleName -> role_composite_role(this.roleName, compositeRoleName));
    }
}
