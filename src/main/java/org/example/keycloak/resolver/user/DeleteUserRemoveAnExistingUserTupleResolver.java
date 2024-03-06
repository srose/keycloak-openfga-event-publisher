package org.example.keycloak.resolver.user;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import org.example.keycloak.resolver.AbstractTupleResolver;
import org.example.keycloak.resolver.TupleResolver;
import org.example.keycloak.support.KeycloakAdapter;
import org.keycloak.events.admin.AdminEvent;

import java.util.regex.Matcher;
import java.util.stream.Stream;

public class DeleteUserRemoveAnExistingUserTupleResolver extends AbstractTupleResolver implements TupleResolver {
    public static final String REGEX = "users/(.*)";

    private final String userId;

    public DeleteUserRemoveAnExistingUserTupleResolver(AdminEvent adminEvent, Matcher matcher) {
        super(adminEvent, matcher);

        userId = matcher.group(1);
    }

    @Override
    public Stream<ClientTupleKey> resolve(KeycloakAdapter keycloakAdapter, OpenFgaClient openFgaClient) {
        var roleTuples = existing_user_assignee_role(openFgaClient, userId);
        var groupTuples = existing_user_member_group_for_userId(openFgaClient, userId);
        return Stream.concat(roleTuples, groupTuples);
    }


}
