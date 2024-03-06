package org.example.keycloak.support;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;

public class KeycloakAdapterDefault implements KeycloakAdapter {

    private KeycloakSession session;

    public KeycloakAdapterDefault(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public String findRoleNameInRealm(String roleId, String realmId)  {
        return session.roles().getRoleById(session.realms().getRealm(realmId) ,roleId).getName();
    }

    @Override
    public String findGroupNameInRealm(String groupId, String realmId)  {
        return session.groups().getGroupById(session.realms().getRealm(realmId),groupId).getName();
    }

    @Override
    public void addToRoleNameCache(String realmId, String roleId, String roleName) {
        var realm = session.realms().getRealm(realmId);
        realm.setAttribute("role:" + roleId, roleName);
    }

    @Override
    public String getRoleNameFromCache(String roleId, String realmId) {
        return session.realms().getRealm(realmId).getAttribute("role:" + roleId);
    }

    @Override
    public void addToGroupNameCache(String realmId, String groupId, String groupName) {
        var realm = session.realms().getRealm(realmId);
        realm.setAttribute("group:" + groupId, groupName);
    }

    @Override
    public String getGroupNameFromCache(String groupId, String realmId) {
        return session.realms().getRealm(realmId).getAttribute("group:" + groupId);
    }

}
