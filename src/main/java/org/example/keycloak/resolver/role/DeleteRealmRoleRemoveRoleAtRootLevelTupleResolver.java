package org.example.keycloak.resolver.role;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import org.example.keycloak.resolver.AbstractTupleResolver;
import org.example.keycloak.resolver.TupleResolver;
import org.example.keycloak.support.KeycloakAdapter;
import org.keycloak.events.admin.AdminEvent;

import java.util.regex.Matcher;
import java.util.stream.Stream;

public class DeleteRealmRoleRemoveRoleAtRootLevelTupleResolver extends AbstractTupleResolver implements TupleResolver {
    public static final String REGEX = "roles-by-id/(.*)(?<!/composites)$";

    private final String roleId;
    private final String realmId;

    public DeleteRealmRoleRemoveRoleAtRootLevelTupleResolver(AdminEvent adminEvent, Matcher matcher) {
        super(adminEvent, matcher);

        this.realmId = adminEvent.getRealmId();
        this.roleId = matcher.group(1);
    }

    @Override
    public Stream<ClientTupleKey> resolve(KeycloakAdapter keycloakAdapter, OpenFgaClient openFgaClient) {
        // no representation and reading from keycloak does not work, because the role is already gone
        var roleName = keycloakAdapter.getRoleNameFromCache(roleId, realmId);

        if(roleName == null) {
            return Stream.empty();
        }

        return Stream.of(
                existing_user_assignee_role_for_roleName(openFgaClient, roleName),
                existing_group_parent_group_role_for_roleName(openFgaClient, roleName),
                existing_role_composite_role(openFgaClient, roleName),
                existing_role_composite_role_for_roleName(openFgaClient, roleName)
        ).flatMap(s -> s);
    }

}
