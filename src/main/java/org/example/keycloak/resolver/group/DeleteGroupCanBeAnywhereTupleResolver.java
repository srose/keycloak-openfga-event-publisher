package org.example.keycloak.resolver.group;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import org.example.keycloak.resolver.AbstractTupleResolver;
import org.example.keycloak.resolver.TupleResolver;
import org.example.keycloak.support.KeycloakAdapter;
import org.keycloak.events.admin.AdminEvent;

import java.util.regex.Matcher;
import java.util.stream.Stream;

public class DeleteGroupCanBeAnywhereTupleResolver extends AbstractTupleResolver implements TupleResolver {

    public static final String REGEX = "groups/(.*)";
    private final String realmId;
    private final String groupId;

    public DeleteGroupCanBeAnywhereTupleResolver(AdminEvent adminEvent, Matcher matcher) {
        super(adminEvent, matcher);
        this.realmId = adminEvent.getRealmId();
        this.groupId = matcher.group(1);
    }

    @Override
    public Stream<ClientTupleKey> resolve(KeycloakAdapter keycloakAdapter, OpenFgaClient openFgaClient) {
        // no representation and reading from keycloak does not work, because the group (incl. subgroups) is already gone
        String groupName = keycloakAdapter.getGroupNameFromCache(groupId, realmId);


        if(groupName == null) {
            return Stream.empty();
        }

        return Stream.of(
                existing_user_member_group_for_groupName(openFgaClient, groupName),
                existing_group_parent_group_role(openFgaClient, groupName),
                existing_group_subgroup_group_for_subGroupName(openFgaClient, groupName),
                existing_group_subgroup_group_for_groupName(openFgaClient, groupName)
        ).flatMap(s -> s);
    }

}
