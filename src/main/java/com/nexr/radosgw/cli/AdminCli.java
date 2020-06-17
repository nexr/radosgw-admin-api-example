package com.nexr.radosgw.cli;

import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.RgwAdminBuilder;
import org.twonote.rgwadmin4j.model.User;

import java.util.List;

public class AdminCli {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java -jar radosgw-admin-cli-0.1.jar <accessKey> <secretKey> <endpoint>");
            System.out.println("\t\t endpoint is form of <scheme>://<host>:<ip>/<rgw_admin_entry>, e.g. http://127.0.0.1:8080/admin");
            System.exit(1);
        }

        String accessKey = args[0];
        String secretKey = args[1];
        String endpoint = args[2];

        RgwAdmin RGW_ADMIN =
                new RgwAdminBuilder()
                        .accessKey(accessKey)
                        .secretKey(secretKey)
                        .endpoint(endpoint)
                        .build();

        // List user in the system
        List<User> users = RGW_ADMIN.listUserInfo();

        for(User u: users) {
            System.out.println("User Name: " + u.getDisplayName());
            List<String> buckets = RGW_ADMIN.listBucket(u.getUserId());
            for(String s: buckets) {
                System.out.println("\tBucket Name: " + s);
            }
        }


    }
}
