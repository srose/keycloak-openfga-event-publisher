package org.example.keycloak.resolver.user;

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

public class CreateUserWithRolesAndGroupsTupleResolver extends AbstractTupleResolver implements TupleResolver {

    public static final String REGEX = "users/(.*)";

    private final String userId;
    private final List<String> realmRoles;
    private final List<String> groups;

    public CreateUserWithRolesAndGroupsTupleResolver(AdminEvent adminEvent, Matcher matcher) {
        super(adminEvent, matcher);

        userId = matcher.group(1);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(adminEvent.getRepresentation());

            realmRoles = new ArrayList<>();
            if(rootNode.has("realmRoles")) {
                for (JsonNode node : rootNode.get("realmRoles")) {
                    realmRoles.add(node.asText());
                }
            }

            groups = new ArrayList<>();
            if(rootNode.has("groups")) {
                for (JsonNode node : rootNode.get("groups")) {
                    groups.add(node.asText());
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<ClientTupleKey> resolve(KeycloakAdapter keycloakAdapter, OpenFgaClient openFgaClient) {
        var roleTuples = realmRoles.stream().map(
                role -> user_assignee_role(userId,role)
        );
        var groupTuples = groups.stream().map(
                group -> user_member_group(userId,getStringAfterLastSlashIfPresent(group))
        );
        return Stream.concat(roleTuples, groupTuples);
    }

    public String getStringAfterLastSlashIfPresent(String input) {
        int lastSlashIndex = input.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return input;
        } else {
            return input.substring(lastSlashIndex + 1);
        }
    }
}
