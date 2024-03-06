package org.example.keycloak.support;

public interface KeycloakAdapter {
    String findRoleNameInRealm(String roleId, String realmId);

    String findGroupNameInRealm(String groupId, String realmId);

    void addToRoleNameCache(String realmId, String roleId, String roleName);

    String getRoleNameFromCache(String roleId, String realmId);

    void addToGroupNameCache(String realmId, String groupId, String groupName);

    String getGroupNameFromCache(String groupId, String realmId);
}
