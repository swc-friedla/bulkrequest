package org.example;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.example.permissions.Permission;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.representation.TokenIntrospectionResponse;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("USAGE: java -jar bulkrequest.jar <repeat> <username> <password>");
        }
        log.info("Running {} requests", args[0]);

        for (Integer i = 0; i < Integer.valueOf(args[0]); i++) {
            log.info("**** Running request {} of {} ****", i, args[0]);

            try {
                long startTime = System.currentTimeMillis();
                evaluateVisibleProjectsOfCurrentUser(args[1], args[2]);
                long endTime = System.currentTimeMillis();
                log.info("Time: {} ms ({} s)", (endTime - startTime), ((endTime - startTime) / 1000.0));
                if (((endTime - startTime) / 1000.0) > 5) {
                    log.warn("!!!!!!!!!!!!!!!");
                    log.warn("LONG DURATION {} s", ((endTime - startTime) / 1000.0));
                    log.warn("!!!!!!!!!!!!!!!");
                }
            } catch (Exception e) {
                log.error("Request error", e);
            }
            System.out.println();
        }
    }

    public static void evaluateVisibleProjectsOfCurrentUser(String username, String password) {
        log.info("Getting token for swc user");

        Keycloak kc = KeycloakBuilder.builder()
                .username(username)
                .password(password)
                .clientId("ppt")
                .clientSecret("748c4aeb-832e-4734-9902-ba1ff4e539c9")
                .realm("ey-test.poolparty.biz")
                .serverUrl("https://drpp-kc.ey.net/auth/")
                .build();

        String accessToken = kc.tokenManager().getAccessTokenString();

        AuthzClient authzClient = AuthzClient.create(App.class.getResourceAsStream("/ppt-keycloak.json"));

        AuthorizationRequest authorizationRequest = new AuthorizationRequest();
        authorizationRequest.setScope(Permission.PROJECT_VIEW.getAuthorizationScope());

        log.info("Exchanging access token for authorization");
        AuthorizationResponse authorizationResponse = authzClient.authorization(accessToken)
                .authorize(authorizationRequest);
        String rpt = authorizationResponse.getToken();

        log.info("Introspecting authorization ticket");
        TokenIntrospectionResponse requestingPartyToken = authzClient.protection().introspectRequestingPartyToken(rpt);
        List<String> projects = requestingPartyToken.getPermissions().stream()
                .filter(permission -> permission.getScopes().contains(Permission.PROJECT_VIEW.getAuthorizationScope()))
                .map(permission -> permission.getResourceName())
                .collect(toList());

        log.info("Access to {}", projects);
    }
}
