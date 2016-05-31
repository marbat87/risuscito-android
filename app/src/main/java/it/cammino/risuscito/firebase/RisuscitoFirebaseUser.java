package it.cammino.risuscito.firebase;

public class RisuscitoFirebaseUser {

//    private String id;
    private String username;
    private String email;

    public RisuscitoFirebaseUser(String user, String email) {
        this.username = user;
        this.email = email;
    }

//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
