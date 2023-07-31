package org.example;

import static java.util.stream.Collectors.toList;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import javax.ws.rs.core.Request;

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
        if (args.length != 3) {
            System.out.println("USAGE: java -jar kcbulk.jar <repeat> <username> <password>");
            System.exit(0);
        }
        log.info("Running {} requests", args[0]);

        for (Integer i = 0; i < Integer.valueOf(args[0]); i++) {
            log.info("**** Running request {} of {} ****", i+1, args[0]);

            long endTime = 0;
            long startTime = 0;
            try {
                startTime = System.currentTimeMillis();
                evaluateVisibleProjectsOfCurrentUser(args[1], args[2]);
                //evaluateSvg();
                endTime = System.currentTimeMillis();
                log.info("Time: {} ms ({} s)", (endTime - startTime), ((endTime - startTime) / 1000.0));
                if (((endTime - startTime) / 1000.0) > 5) {
                    log.warn("LONG DURATION request {} of {} took {} s", i, args[0], ((endTime - startTime) / 1000.0));
                }
            } catch (Exception e) {
                endTime = System.currentTimeMillis();
                log.error("Request error for request {} of {} after {} s", i, args[0], ((endTime - startTime) / 1000.0), e);
            }

            log.info("");
        }
    }

    public static void evaluateSvg() throws IOException {
        URL url = new URL("https://ey-test.poolparty.biz/PoolParty/images/svg/pp_icon_messages_error28x28.svg");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setDoOutput(true);
        con.setConnectTimeout(150*1000);
        con.setReadTimeout(150*1000);

        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.flush();
        out.close();
    }

    public static void evaluateVisibleProjectsOfCurrentUser(String username, String password) {
        log.debug("Getting token for swc user");

        Keycloak kc = KeycloakBuilder.builder()
                .username(username)
                .password(password)
                .clientId("ppt")
                .clientSecret("748c4aeb-832e-4734-9902-ba1ff4e539c9")
                .realm("ey-test.poolparty.biz")
                .serverUrl("https://login-eu.poolparty.biz/auth/")
                .build();

        String accessToken = kc.tokenManager().getAccessTokenString();

        AuthzClient authzClient = AuthzClient.create(App.class.getResourceAsStream("/ppt-keycloak.json"));

        AuthorizationRequest authorizationRequest = new AuthorizationRequest();
        authorizationRequest.setScope(Permission.PROJECT_VIEW.getAuthorizationScope());

        log.debug("Exchanging access token for authorization");
        AuthorizationResponse authorizationResponse = authzClient.authorization(accessToken)
                .authorize(authorizationRequest);
        String rpt = authorizationResponse.getToken();

        log.debug("Introspecting authorization ticket");
        TokenIntrospectionResponse requestingPartyToken = authzClient.protection().introspectRequestingPartyToken(rpt);
        List<String> projects = requestingPartyToken.getPermissions().stream()
                .filter(permission -> permission.getScopes().contains(Permission.PROJECT_VIEW.getAuthorizationScope()))
                .map(permission -> permission.getResourceName())
                .collect(toList());

        log.debug("Access to {}", projects);
    }
}
