package com.nexr.radosgw.cli;

import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.RgwAdminBuilder;
import org.twonote.rgwadmin4j.model.*;

import java.util.List;
import java.util.Optional;

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
                Optional<BucketInfo> bucketInfo = RGW_ADMIN.getBucketInfo(s);
                if(bucketInfo.isPresent()) {
                    BucketInfo bi = bucketInfo.get();
                    System.out.println("\tBucket Name: " + bi.getBucket());
                }
            }
        }

        User user = RGW_ADMIN.createUser("cliTestUser");


//        Map<String, String> parameters = new HashMap();
//        parameters.put("suspended", "true");
//        User user = RGW_ADMIN.modifyUser(user.getUserId(),parameters);

        Optional<User> userInfo = RGW_ADMIN.getUserInfo(user.getUserId());
        if (userInfo.isPresent()) {
            User u = userInfo.get();
            System.out.println(u.getUserId());
            for(S3Credential pair : u.getS3Credentials() ) {
                System.out.println("\tAccessKey: " + pair.getAccessKey());
                System.out.println("\tSecretKey: " + pair.getSecretKey());
            }
        }

        SubUser subUser = RGW_ADMIN.createSubUser(user.getUserId(), "cliTestSubUser", SubUser.Permission.READ, CredentialType.S3);
        System.out.println("SubUser: " + subUser.getId() + "\tPermission: " + subUser.getPermission().toString());
        Optional<User> parent = RGW_ADMIN.getUserInfo(subUser.getParentUserId());
        if (userInfo.isPresent()) {
            User u = parent.get();
            for (S3Credential cred : u.getS3Credentials()) {
                if (cred.getUserId().endsWith(subUser.getId())) {
                    System.out.println("\tAccess Key: "+ cred.getAccessKey());
                    System.out.println("\tSecret Key: "+ cred.getSecretKey());
                }
            }
        }

//        Optional<SubUser> subUserInfo = RGW_ADMIN.getSubUserInfo(subUser.getParentUserId(), subUser.getId());
//        if (subUserInfo.isPresent()) {
//            SubUser subUser1 = subUserInfo.get();
//            System.out.println("Subuser: "+ subUser1.getId());
//            Optional<User> parent = RGW_ADMIN.getUserInfo(subUser1.getParentUserId());
//            if (userInfo.isPresent()) {
//                User u = parent.get();
//                for (S3Credential cred : u.getS3Credentials()) {
//                    if (cred.getUserId().endsWith(subUser1.getId())) {
//                        System.out.println("\tAccess Key: "+ cred.getAccessKey());
//                        System.out.println("\tSecret Key: "+ cred.getSecretKey());
//                    }
//                }
//            }
//        }

        List<SubUser> list = RGW_ADMIN.setSubUserPermission(subUser.getParentUserId(), subUser.getRelativeSubUserId(), SubUser.Permission.FULL);

        RGW_ADMIN.removeSubUser(subUser.getParentUserId(), subUser.getId());

        long maxObjects = 10000;
        long maxKBPerObject = 1000000;
        RGW_ADMIN.setUserQuota(user.getUserId(), maxObjects, maxKBPerObject );

        Optional<Quota> userQuota = RGW_ADMIN.getUserQuota(user.getUserId());
        if (userQuota.isPresent()) {
            Quota q = userQuota.get();
            System.out.println("Max Objects: " + q.getMaxObjects() + "\tMax Object Size: " + q.getMaxSizeKb() + "KB");
        }

//        long maxObjects = 10000;
//        long maxKBPerObject = 1000000;
//        RGW_ADMIN.setBucketQuota(user.getUserId(), maxObjects, maxKBPerObject);
//
//        Optional<Quota> bucketQuota = RGW_ADMIN.getBucketQuota(user.getUserId());
//        if (bucketQuota.isPresent()) {
//            Quota q = bucketQuota.get();
//            System.out.println("Max Objects: " + q.getMaxObjects() + "\tMax Object Size: " + q.getMaxSizeKb() + "KB");
//        }

        RGW_ADMIN.removeUser(user.getUserId());

    }
}
