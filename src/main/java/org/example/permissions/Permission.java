package org.example.permissions;

import static org.example.permissions.PermissionType.GLOBAL;
import static org.example.permissions.PermissionType.PROJECT;
import static org.example.permissions.PermissionUsage.PPGS;
import static org.example.permissions.PermissionUsage.PPT;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum Permission {
    REMOTE_SYSTEMS(GLOBAL, PPT, "remote-systems"),
    MIGRATION(GLOBAL, PPT, "migration"),
    ADMIN_SCRIPTS(GLOBAL, PPT, "admin:scripts"),
    METADATA_MOVE(GLOBAL, PPT, "remote-systems.metadata"),
    CORPUS_LANGUAGE_MODEL_SETTINGS(GLOBAL, PPT, "corpus-language-model-settings"),
    SNAPSHOTS(GLOBAL, PPT, "snapshots:write"),
    USERS_CREATE(GLOBAL, PPT, "users:create"),
    USERS_READ(GLOBAL, PPT, "users:read"),
    USERS_WRITE(GLOBAL, PPT, "users:write"),
    USERS_DELETE(GLOBAL, PPT, "users:delete"),

    TRIPLES_TAB_DELETE(PROJECT, PPT, "triples:delete"),
    PROJECT_USER_ROLES(PROJECT, PPT, "project.users:write"),

    PUBLISH(GLOBAL, PPT, "projects:publish"), // not used at the moment
    API_SPARQL_UPDATE(GLOBAL, PPT, "projects:sparql-update"),

    ONTOLOGIES_CREATE(GLOBAL, PPT, "ontologies:create"),
    ONTOLOGIES_READ(GLOBAL, PPT, "ontologies:read"),
    ONTOLOGIES_WRITE(GLOBAL, PPT, "ontologies:write"),
    ONTOLOGIES_DELETE(GLOBAL, PPT, "ontologies:delete"),

    PROJECT_CREATE(GLOBAL, PPT, "projects:create"),
    PROJECT_DELETE(GLOBAL, PPT, "projects:delete"),
    PROJECT_GROUPS(PROJECT, PPT, "project.groups"),
    PROJECT_HISTORY_DELETE(PROJECT, PPT, "project-events:delete"),
    PROJECT_LINKING(PROJECT, PPT, "projects:link"),
    PROJECT_NOTIFICATIONS(PROJECT, PPT, "project-notifications"),

    SCHEMA_SETTINGS(PROJECT, PPT, "project-customschema"),

    WORKFLOW_SETTINGS(PROJECT, PPT, "workflows:configure"),
    WORKFLOW_APPROVE_REJECT(PROJECT, PPT, "workflows:update"),
    WORKFLOW_ASSIGN(PROJECT, PPT, "workflows:assign"),

    INSCHEME_SETTINGS(PROJECT, PPT, "inscheme"),
    SKOSXL_SETTINGS(PROJECT, PPT, "skosxl"),
    COLLABORATION_SETTINGS(PROJECT, PPT, "collaboration"),
    URI_SETTINGS(PROJECT, PPT, "advanced-uri-settings"),
    URI_EDIT(PROJECT, PPT, "concepts:change-uri"),
    LD_SETTINGS(PROJECT, PPT, "opendata"),
    QUALITY_SETTINGS(PROJECT, PPT, "quality-settings"),

    PROJECT_ADVANCED_MENU(PROJECT, PPT, "projects:advanced-configure"), // not used at the moment

    LOGIN(GLOBAL, PPT, "login"),

    API_READ(GLOBAL, PPT, "apis:read"),
    API_WRITE(GLOBAL, PPT, "apis:write"),

    CORPUS_CREATE(PROJECT, PPT, "corpora:create"),
    CORPUS_READ(PROJECT, PPT, "corpora:read"),
    CORPUS_WRITE(PROJECT, PPT, "corpora:write"),
    CORPUS_DELETE(PROJECT, PPT, "corpora:delete"),

    PROJECT_VIEW(PROJECT, PPT, "projects:view"),
    PROJECT_READ(PROJECT, PPT, "projects:read"),
    PROJECT_WRITE(PROJECT, PPT, "projects:write"),

    CLASSIFIER_CREATE(PROJECT, PPT, "classifiers:create"),
    CLASSIFIER_READ(PROJECT, PPT, "classifiers:read"),
    CLASSIFIER_WRITE(PROJECT, PPT, "classifiers:write"),
    CLASSIFIER_DELETE(PROJECT, PPT, "classifiers:delete"),

    WIKI(GLOBAL, PPT, "wiki:read"),

    GRAPHSEARCH_USER(GLOBAL, PPGS, "dashboard"),
    GRAPHSEARCH_ADMIN(GLOBAL, PPGS, "admin.dashboard");

    // ----------------

    private final PermissionType permissionType;
    private final PermissionUsage permissionUsage;
    private final String authorizationScope;

    Permission(PermissionType permissionType, PermissionUsage permissionUsage, String authorizationScope) {
        this.permissionType = permissionType;
        this.permissionUsage = permissionUsage;
        this.authorizationScope = authorizationScope;
    }

    private static Set<Permission> toSet(Permission... permissions) {
        return new HashSet<>(Arrays.asList(permissions));
    }

    public static Set<Permission> getSuperadminPermissions() {
        return toSet(Permission.values());
    }

    public static Set<Permission> getAdminPermissions() {
        Set<Permission> permissions = getUserPermissions();
        permissions.addAll(toSet(PUBLISH, API_SPARQL_UPDATE, ONTOLOGIES_CREATE, ONTOLOGIES_READ, ONTOLOGIES_WRITE,
                ONTOLOGIES_DELETE, PROJECT_CREATE, PROJECT_DELETE, PROJECT_GROUPS, PROJECT_HISTORY_DELETE,
                PROJECT_LINKING, SCHEMA_SETTINGS, WORKFLOW_SETTINGS, WORKFLOW_APPROVE_REJECT, INSCHEME_SETTINGS,
                SKOSXL_SETTINGS, COLLABORATION_SETTINGS, URI_SETTINGS, URI_EDIT, LD_SETTINGS, QUALITY_SETTINGS,
                PROJECT_ADVANCED_MENU, PROJECT_NOTIFICATIONS
        ));
        return permissions;
    }

    public static Set<Permission> getUserPermissions() {
        Set<Permission> permissions = getWikiEditorPermissions();
        permissions.addAll(toSet(LOGIN, PROJECT_READ, PROJECT_WRITE, CORPUS_CREATE, CORPUS_READ,
                CORPUS_WRITE, CORPUS_DELETE, API_READ, API_WRITE, GRAPHSEARCH_USER,
                CLASSIFIER_READ, CLASSIFIER_WRITE, CLASSIFIER_CREATE, CLASSIFIER_DELETE, WORKFLOW_ASSIGN
        ));
        return permissions;
    }

    public static Set<Permission> getReadOnlyUserPermissions() {
        return toSet(LOGIN, WIKI, PROJECT_READ);
    }

    public static Set<Permission> getWikiEditorPermissions() {
        return toSet(WIKI);
    }

    public static Set<Permission> getApiUserPermissions() {
        Set<Permission> permissions = getUserPermissions();
        permissions.remove(LOGIN);
        return permissions;
    }

    public static Set<Permission> getApiAdminPermissions() {
        Set<Permission> permissions = getAdminPermissions();
        permissions.remove(LOGIN);
        return permissions;
    }

    public static Set<Permission> getGlobalPermissions() {
        return Arrays.stream(values()).filter(p -> p.getPermissionType().equals(GLOBAL)).collect(Collectors.toSet());
    }

    public static Set<Permission> getProjectPermissions() {
        return Arrays.stream(values()).filter(p -> p.getPermissionType().equals(PROJECT)).collect(Collectors.toSet());
    }

    public static Set<Permission> getComponentPermissions(PermissionUsage permissionUsage) {
        return Arrays.stream(values())
                .filter(p -> p.getPermissionUsage() == permissionUsage)
                .collect(Collectors.toSet());
    }

    public static Permission getPermission(String authorizationScope) {
        return Arrays.stream(Permission.values())
                .filter(permission -> permission.authorizationScope.equals(authorizationScope))
                .findFirst().orElse(null);
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public PermissionUsage getPermissionUsage() {
        return permissionUsage;
    }

    public boolean isGlobal() {
        return permissionType.equals(GLOBAL);
    }

    public static boolean isGlobal(String authorizationScope) {
        Permission permission = getPermission(authorizationScope);
        return permission != null && permission.isGlobal();
    }

    public String getAuthorizationScope() {
        return authorizationScope;
    }

    @Override
    public String toString() {
        return "Permission{" +
                ", permissionType=" + permissionType +
                ", permissionUsage=" + permissionUsage +
                ", authorizationScope='" + authorizationScope + '\'' +
                '}';
    }
}
